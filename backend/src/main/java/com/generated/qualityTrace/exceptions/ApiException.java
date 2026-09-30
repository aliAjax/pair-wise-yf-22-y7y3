package com.generated.qualityTrace.exceptions;

import com.generated.qualityTrace.constants.ErrorCodes;

/** 业务异常基类，service/controller 各自包装后交给全局异常处理器。 */
public class ApiException extends RuntimeException {
  private final String code;
  private final int httpStatus;

  public ApiException(String code, String message, int httpStatus) {
    super(message);
    this.code = code;
    this.httpStatus = httpStatus;
  }

  public String getCode() {
    return code;
  }

  public int getHttpStatus() {
    return httpStatus;
  }

  public static ApiException notFound(String message) {
    return new ApiException(ErrorCodes.PACKAGE_NOT_FOUND, message, 404);
  }
}
