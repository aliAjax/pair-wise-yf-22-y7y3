package com.generated.qualityTrace.middlewares;

import com.generated.qualityTrace.config.CurrentUser;
import com.generated.qualityTrace.config.UserContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 操作日志中间件：记录所有写操作（POST/PUT/PATCH/DELETE）的操作人、路径与结果状态，
 * 与数据库 audit_log 表对应（持久化由审计服务落库，此处输出结构化日志）。
 */
@Component
public class AuditLogMiddleware extends OncePerRequestFilter {

  private static final Logger audit = LoggerFactory.getLogger("AUDIT_LOG");

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                  FilterChain chain) throws ServletException, IOException {
    String method = request.getMethod();
    boolean write = "POST".equals(method) || "PUT".equals(method)
        || "PATCH".equals(method) || "DELETE".equals(method);

    long start = System.currentTimeMillis();
    try {
      chain.doFilter(request, response);
    } finally {
      if (write && request.getRequestURI().startsWith("/api/")) {
        CurrentUser user = UserContext.get();
        String actor = user == null ? "anonymous" : user.username() + "(" + user.role() + ")";
        audit.info("actor={} method={} path={} status={} costMs={}",
            actor, method, request.getRequestURI(), response.getStatus(),
            System.currentTimeMillis() - start);
      }
    }
  }
}
