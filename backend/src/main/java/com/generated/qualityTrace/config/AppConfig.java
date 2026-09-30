package com.generated.qualityTrace.config;

import com.generated.qualityTrace.middlewares.AuditLogMiddleware;
import com.generated.qualityTrace.middlewares.AuthMiddleware;
import com.generated.qualityTrace.middlewares.RateLimitMiddleware;
import com.generated.qualityTrace.middlewares.RbacMiddleware;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 应用配置：注册限流、认证、RBAC、审计日志过滤器并指定顺序。
 * 全局配置同时经 .env / docker-compose.yml 注入，见 PackageExportConfig。
 */
@Configuration
public class AppConfig {

  @Bean
  public FilterRegistrationBean<RateLimitMiddleware> rateLimitFilter(RateLimitMiddleware filter) {
    FilterRegistrationBean<RateLimitMiddleware> reg = new FilterRegistrationBean<>(filter);
    reg.addUrlPatterns("/api/*");
    reg.setOrder(0);
    reg.setName("rateLimitMiddleware");
    return reg;
  }

  @Bean
  public FilterRegistrationBean<AuthMiddleware> authFilter(AuthMiddleware filter) {
    FilterRegistrationBean<AuthMiddleware> reg = new FilterRegistrationBean<>(filter);
    reg.addUrlPatterns("/api/*");
    reg.setOrder(1);
    reg.setName("authMiddleware");
    return reg;
  }

  @Bean
  public FilterRegistrationBean<RbacMiddleware> rbacFilter(RbacMiddleware filter) {
    FilterRegistrationBean<RbacMiddleware> reg = new FilterRegistrationBean<>(filter);
    reg.addUrlPatterns("/api/*");
    reg.setOrder(2);
    reg.setName("rbacMiddleware");
    return reg;
  }

  @Bean
  public FilterRegistrationBean<AuditLogMiddleware> auditLogFilter(AuditLogMiddleware filter) {
    FilterRegistrationBean<AuditLogMiddleware> reg = new FilterRegistrationBean<>(filter);
    reg.addUrlPatterns("/api/*");
    reg.setOrder(3);
    reg.setName("auditLogMiddleware");
    return reg;
  }
}
