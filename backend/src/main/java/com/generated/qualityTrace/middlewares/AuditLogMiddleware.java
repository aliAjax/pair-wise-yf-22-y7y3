package com.generated.qualityTrace.middlewares;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.generated.qualityTrace.repositories.AuditLogRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 审计日志中间件：请求完成后为写操作补一条访问轨迹。
 * 业务动作明细由 service 层记录，中间件只记录“谁在何时以什么角色调了哪个接口、结果码”。
 */
@Component
public class AuditLogMiddleware implements HandlerInterceptor {

  private static final String STARTED_AT = "auditStartedAt";

  private final AuditLogRepository auditLogRepository;

  public AuditLogMiddleware(AuditLogRepository auditLogRepository) {
    this.auditLogRepository = auditLogRepository;
  }

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
    request.setAttribute(STARTED_AT, System.currentTimeMillis());
    return true;
  }

  @Override
  public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                              Object handler, Exception ex) {
    String method = request.getMethod();
    if ("GET".equalsIgnoreCase(method) || "OPTIONS".equalsIgnoreCase(method)) {
      return;
    }
    String actor = AuthContext.actor();
    if ("anonymous".equals(actor)) {
      return;
    }
    long started = (long) request.getAttribute(STARTED_AT);
    String detail = "role=" + AuthContext.role()
        + " status=" + response.getStatus()
        + " costMs=" + (System.currentTimeMillis() - started);
    auditLogRepository.append(actor, "API_" + method, "HttpEndpoint",
        request.getRequestURI(), detail);
  }
}
