package com.generated.qualityTrace.middlewares;

import com.generated.qualityTrace.config.CurrentUser;
import com.generated.qualityTrace.config.RoleConfig;
import com.generated.qualityTrace.config.UserContext;
import com.generated.qualityTrace.constants.ErrorCodes;
import com.generated.qualityTrace.constants.ErrorMessages;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * RBAC 中间件：按角色矩阵限制调查包接口。
 *
 * <p>冻结/导出仅审计员、质量经理可用；查看对所有角色开放，
 * 但质检员/产线主管在响应侧看到的检验员身份已脱敏（见 RoleConfig）。
 */
@Component
public class RbacMiddleware extends OncePerRequestFilter {

  private final RoleConfig roleConfig;

  public RbacMiddleware(RoleConfig roleConfig) {
    this.roleConfig = roleConfig;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                  FilterChain chain) throws ServletException, IOException {
    String path = request.getRequestURI();
    String method = request.getMethod();

    if (!path.startsWith("/api/") || path.equals("/api/auth/login")) {
      chain.doFilter(request, response);
      return;
    }

    CurrentUser user = UserContext.get();
    String role = user == null ? null : user.role();

    if (isFreeze(method, path) || isExport(method, path)) {
      if (!roleConfig.canManagePackage(role)) {
        AuthMiddleware.writeError(response, HttpServletResponse.SC_FORBIDDEN,
            ErrorCodes.RBAC_DENIED, ErrorMessages.RBAC_DENIED);
        return;
      }
    } else if (isView(method, path)) {
      if (!roleConfig.canViewPackage(role)) {
        AuthMiddleware.writeError(response, HttpServletResponse.SC_FORBIDDEN,
            ErrorCodes.RBAC_DENIED, ErrorMessages.RBAC_DENIED);
        return;
      }
    }

    chain.doFilter(request, response);
  }

  private boolean isFreeze(String method, String path) {
    return "POST".equals(method) && "/api/investigation-packages".equals(path);
  }

  private boolean isExport(String method, String path) {
    return "POST".equals(method)
        && path.startsWith("/api/investigation-packages/")
        && path.endsWith("/export");
  }

  private boolean isView(String method, String path) {
    return "GET".equals(method) && path.startsWith("/api/investigation-packages");
  }
}
