package com.generated.qualityTrace.middlewares;

import com.generated.qualityTrace.services.JwtService;

/**
 * 请求级登录上下文（线程绑定）。
 * AuthMiddleware 写入，RbacMiddleware / controller / AuditLogMiddleware 读取。
 */
public final class AuthContext {

  private static final ThreadLocal<JwtService.Claims> CURRENT = new ThreadLocal<>();

  private AuthContext() {}

  public static void set(JwtService.Claims claims) {
    CURRENT.set(claims);
  }

  public static JwtService.Claims get() {
    return CURRENT.get();
  }

  public static String actor() {
    JwtService.Claims claims = CURRENT.get();
    return claims == null ? "anonymous" : claims.subject();
  }

  public static String role() {
    JwtService.Claims claims = CURRENT.get();
    return claims == null ? null : claims.role();
  }

  public static void clear() {
    CURRENT.remove();
  }
}
