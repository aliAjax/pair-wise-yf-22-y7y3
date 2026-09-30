package com.generated.qualityTrace.validators;

import com.generated.qualityTrace.constants.ErrorMessages;
import com.generated.qualityTrace.types.LoginPayload;
import com.generated.qualityTrace.utils.Formatters;

/**
 * 登录入参校验。
 */
public final class AuthValidator {

  private AuthValidator() {}

  public static void validateLogin(LoginPayload payload) {
    if (payload == null || payload.username == null || payload.username.trim().isEmpty()) {
      throw new IllegalArgumentException(
          Formatters.format(ErrorMessages.INVALID_PARAMETER, "username 不能为空"));
    }
    if (payload.password == null || payload.password.isEmpty()) {
      throw new IllegalArgumentException(
          Formatters.format(ErrorMessages.INVALID_PARAMETER, "password 不能为空"));
    }
  }
}
