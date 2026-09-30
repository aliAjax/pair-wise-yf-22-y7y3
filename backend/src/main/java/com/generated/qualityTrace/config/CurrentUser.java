package com.generated.qualityTrace.config;

/**
 * 当前登录用户（从 JWT 解析），在请求线程内经由 {@link UserContext} 传递。
 */
public record CurrentUser(Long userId, String username, String role) {
  public boolean hasRole(String expected) {
    return expected != null && expected.equals(this.role);
  }
}
