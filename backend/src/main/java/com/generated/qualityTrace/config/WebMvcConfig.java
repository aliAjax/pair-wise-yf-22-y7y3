package com.generated.qualityTrace.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.generated.qualityTrace.middlewares.AuditLogMiddleware;
import com.generated.qualityTrace.middlewares.AuthMiddleware;
import com.generated.qualityTrace.middlewares.RateLimitMiddleware;
import com.generated.qualityTrace.middlewares.RbacMiddleware;
import com.generated.qualityTrace.routes.AuthRoutes;

/**
 * 中间件链（/api/**，登录与健康检查除外）：
 * 认证 -> 角色授权 -> 限流 -> 审计。
 * 认证先于限流，限流键可精确到 actor。
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

  private final AuthMiddleware authMiddleware;
  private final RbacMiddleware rbacMiddleware;
  private final AuditLogMiddleware auditLogMiddleware;
  private final RateLimitMiddleware rateLimitMiddleware;

  public WebMvcConfig(AuthMiddleware authMiddleware, RbacMiddleware rbacMiddleware,
                      AuditLogMiddleware auditLogMiddleware, RateLimitMiddleware rateLimitMiddleware) {
    this.authMiddleware = authMiddleware;
    this.rbacMiddleware = rbacMiddleware;
    this.auditLogMiddleware = auditLogMiddleware;
    this.rateLimitMiddleware = rateLimitMiddleware;
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(authMiddleware)
        .addPathPatterns("/api/**")
        .excludePathPatterns(AuthRoutes.LOGIN);
    registry.addInterceptor(rbacMiddleware).addPathPatterns("/api/**");
    registry.addInterceptor(rateLimitMiddleware).addPathPatterns("/api/**");
    registry.addInterceptor(auditLogMiddleware).addPathPatterns("/api/**");
  }
}
