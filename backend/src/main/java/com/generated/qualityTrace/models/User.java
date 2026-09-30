package com.generated.qualityTrace.models;

/**
 * 系统用户（用于 JWT 登录与 RBAC）。
 */
public class User {
  public Long id;
  public String username;
  public String password;
  public String displayName;
  /** 取值见 constants/RoleConstants：INSPECTOR / LINE_SUPERVISOR / QUALITY_MANAGER / AUDITOR。 */
  public String role;

  public User() {}

  public User(Long id, String username, String password, String displayName, String role) {
    this.id = id;
    this.username = username;
    this.password = password;
    this.displayName = displayName;
    this.role = role;
  }
}
