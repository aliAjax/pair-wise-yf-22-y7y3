package com.generated.qualityTrace.middlewares;

import java.io.IOException;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.generated.qualityTrace.constants.ErrorCodes;
import com.generated.qualityTrace.constants.ErrorMessages;
import com.generated.qualityTrace.utils.JsonUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 简单固定窗口限流：每个 actor + 路径前缀每分钟最多 {@link #LIMIT_PER_MINUTE} 次。
 * 超限返回 429 RATE_LIMITED。
 */
@Component
public class RateLimitMiddleware implements HandlerInterceptor {

  static final int LIMIT_PER_MINUTE = 60;
  private static final long WINDOW_MS = 60_000L;

  private final Map<String, Deque<Long>> hits = new ConcurrentHashMap<>();

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws IOException {
    String key = AuthContext.actor() + "|" + bucket(request.getRequestURI());
    long now = System.currentTimeMillis();
    Deque<Long> deque = hits.computeIfAbsent(key, k -> new ConcurrentLinkedDeque<>());
    synchronized (deque) {
      while (!deque.isEmpty() && now - deque.peekFirst() > WINDOW_MS) {
        deque.pollFirst();
      }
      if (deque.size() >= LIMIT_PER_MINUTE) {
        writeLimited(response);
        return false;
      }
      deque.addLast(now);
    }
    return true;
  }

  private static String bucket(String path) {
    if (path.startsWith("/api/investigation-packages")) {
      return "packages";
    }
    if (path.startsWith("/api/quality-inspections")) {
      return "inspections";
    }
    if (path.startsWith("/api/defects")) {
      return "defects";
    }
    String[] parts = path.split("/");
    return parts.length > 1 ? parts[1] : path;
  }

  private void writeLimited(HttpServletResponse response) throws IOException {
    response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding("UTF-8");
    response.getWriter().write(JsonUtils.pretty(Map.of(
        "code", ErrorCodes.RATE_LIMITED,
        "message", ErrorMessages.RATE_LIMITED,
        "retryAfterSeconds", WINDOW_MS / 1000)));
  }
}
