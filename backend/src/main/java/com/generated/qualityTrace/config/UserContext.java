package com.generated.qualityTrace.config;

/**
 * 基于 ThreadLocal 的当前用户上下文，避免层层传递 request。
 */
public final class UserContext {

  private static final ThreadLocal<CurrentUser> HOLDER = new ThreadLocal<>();

  private UserContext() {}

  public static void set(CurrentUser user) {
    HOLDER.set(user);
  }

  public static CurrentUser get() {
    return HOLDER.get();
  }

  public static Long getUserId() {
    CurrentUser u = HOLDER.get();
    return u == null ? null : u.userId();
  }

  public static String getUsername() {
    CurrentUser u = HOLDER.get();
    return u == null ? null : u.username();
  }

  public static String getRole() {
    CurrentUser u = HOLDER.get();
    return u == null ? null : u.role();
  }

  public static void clear() {
    HOLDER.remove();
  }
}
