package com.generated.qualityTrace.types;

/**
 * 登录请求体。
 */
public class LoginPayload {
  public String username;
  public String password;

  public LoginPayload() {}

  public LoginPayload(String username, String password) {
    this.username = username;
    this.password = password;
  }
}
