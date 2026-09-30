package com.generated.qualityTrace.middlewares;

import java.util.Set;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.generated.qualityTrace.constants.UserRole;
import com.generated.qualityTrace.exceptions.RbacDeniedException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * RBAC 中间件：按“方法 + 路径前缀”授权。
 * 调查包：仅 AUDITOR / RESTRICTED_INSPECTOR 可访问，二者看到的字段不同；
 * 现场补录（POST 检验/不良）：仅 INSPECTOR / AUDITOR；
 * RESTRICTED_INSPECTOR 只能看不能写。
 */
@Component
public class RbacMiddleware implements HandlerInterceptor {

  private static final String PACKAGES_PREFIX = "/api/investigation-packages";
  private static final String INSPECTIONS_PATH = "/api/quality-inspections";
  private static final String DEFECTS_PATH = "/api/defects";
  private static final String AUDIT_LOGS_PATH = "/api/audit-logs";

  private static final Set<String> PACKAGE_READERS =
      Set.of(UserRole.AUDITOR.name(), UserRole.RESTRICTED_INSPECTOR.name());
  private static final Set<String> ONSITE_WRITERS =
      Set.of(UserRole.INSPECTOR.name(), UserRole.AUDITOR.name());
  private static final Set<String> AUDIT_READERS = Set.of(UserRole.AUDITOR.name());

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
    String role = AuthContext.role();
    String method = request.getMethod();
    String path = request.getRequestURI();

    if (path.startsWith(PACKAGES_PREFIX)) {
      if (!PACKAGE_READERS.contains(role)) {
        throw new RbacDeniedException(role, method + " " + path);
      }
      // 受限检验员只读：禁止导出重试、开下一包等写动作。
      if (!"GET".equalsIgnoreCase(method) && UserRole.RESTRICTED_INSPECTOR.name().equals(role)) {
        throw new RbacDeniedException(role, method + " " + path);
      }
      return true;
    }

    if ("POST".equalsIgnoreCase(method)
        && (path.equals(INSPECTIONS_PATH) || path.equals(DEFECTS_PATH))) {
      if (!ONSITE_WRITERS.contains(role)) {
        throw new RbacDeniedException(role, method + " " + path);
      }
      return true;
    }

    if (path.startsWith(AUDIT_LOGS_PATH) && !AUDIT_READERS.contains(role)) {
      throw new RbacDeniedException(role, method + " " + path);
    }

    return true;
  }
}
