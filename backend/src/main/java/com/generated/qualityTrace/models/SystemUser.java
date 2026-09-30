package com.generated.qualityTrace.models;

/** 系统用户（种子数据），密码仅演示用。 */
public class SystemUser {
  public Long id;
  public String username;
  public String password;
  public String displayName;
  public String role;

  public SystemUser() {}

  public SystemUser(Long id, String username, String password, String displayName, String role) {
    this.id = id;
    this.username = username;
    this.password = password;
    this.displayName = displayName;
    this.role = role;
  }
}
