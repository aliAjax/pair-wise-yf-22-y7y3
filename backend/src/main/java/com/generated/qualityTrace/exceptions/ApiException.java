package com.generated.qualityTrace.exceptions;

import org.springframework.http.HttpStatus;

/**
 * 接口异常（controller 层包装）。在 {@link BusinessException} 基础上携带 HTTP 状态码，
 * 体现 service 与 controller 分别包装异常，而非全局吞掉。
 */
public class ApiException extends RuntimeException {

  private final String code;
  private final HttpStatus status;

  public ApiException(String code, String message, HttpStatus status) {
    super(message);
    this.code = code;
    this.status = status;
  }

  public String getCode() {
    return code;
  }

  public HttpStatus getStatus() {
    return status;
  }
}
