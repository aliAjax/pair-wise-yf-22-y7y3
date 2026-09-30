package com.generated.qualityTrace.middlewares;

import com.generated.qualityTrace.constants.ErrorCodes;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 限流中间件：按 IP 统计滑动窗口内请求数，超限返回 429。
 */
@Component
public class RateLimitMiddleware extends OncePerRequestFilter {

  private final int maxRequests;
  private final long windowMillis;
  private final Map<String, long[]> windows = new ConcurrentHashMap<>();

  public RateLimitMiddleware(@Value("${RATE_LIMIT_MAX:120}") int maxRequests,
                             @Value("${RATE_LIMIT_WINDOW_MILLIS:60000}") long windowMillis) {
    this.maxRequests = maxRequests;
    this.windowMillis = windowMillis;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                  FilterChain chain) throws ServletException, IOException {
    String key = clientIp(request);
    long now = System.currentTimeMillis();
    long[] slot = windows.compute(key, (k, prev) -> {
      if (prev == null || now - prev[0] > windowMillis) {
        return new long[] { now, 1L };
      }
      prev[1]++;
      return prev;
    });

    if (slot[1] > maxRequests) {
      AuthMiddleware.writeError(response, HttpServletResponse.SC_TOO_MANY_REQUESTS,
          ErrorCodes.RATE_LIMITED, "请求过于频繁，请稍后再试");
      return;
    }
    chain.doFilter(request, response);
  }

  private String clientIp(HttpServletRequest request) {
    String xff = request.getHeader("X-Forwarded-For");
    return (xff != null && !xff.isEmpty()) ? xff.split(",")[0].trim() : request.getRemoteAddr();
  }
}
