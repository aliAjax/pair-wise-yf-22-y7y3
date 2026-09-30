package com.generated.qualityTrace.exceptions;

/**
 * 业务异常（service 层包装）。携带错误码与已填充的错误消息，
 * 由 controller 层再次包装为 {@link ApiException}，最终经全局异常中间件输出。
 */
public class BusinessException extends RuntimeException {

  private final String code;

  public BusinessException(String code, String message) {
    super(message);
    this.code = code;
  }

  public String getCode() {
    return code;
  }
}
