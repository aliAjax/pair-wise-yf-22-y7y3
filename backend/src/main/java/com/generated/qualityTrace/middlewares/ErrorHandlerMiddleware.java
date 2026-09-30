package com.generated.qualityTrace.middlewares;

import com.generated.qualityTrace.constants.ErrorCodes;
import com.generated.qualityTrace.exceptions.ApiException;
import com.generated.qualityTrace.exceptions.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 全局异常处理中间件。
 *
 * <p>统一输出 {code, message} 错误体；service 层 {@link BusinessException} 与
 * controller 层 {@link ApiException} 分别包装后在此汇聚，避免在各处散落 try-catch 吞异常。
 */
@RestControllerAdvice
public class ErrorHandlerMiddleware {

  private static final Logger log = LoggerFactory.getLogger(ErrorHandlerMiddleware.class);

  @ExceptionHandler(ApiException.class)
  public ResponseEntity<Map<String, Object>> handleApi(ApiException ex) {
    return build(ex.getCode(), ex.getMessage(), ex.getStatus());
  }

  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<Map<String, Object>> handleBusiness(BusinessException ex) {
    return build(ex.getCode(), ex.getMessage(), HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<Map<String, Object>> handleIllegalArg(IllegalArgumentException ex) {
    return build(ErrorCodes.INVALID_PARAMETER, ex.getMessage(), HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
    log.error("未处理异常", ex);
    return build(ErrorCodes.INTERNAL_ERROR, "服务器内部错误", HttpStatus.INTERNAL_SERVER_ERROR);
  }

  private ResponseEntity<Map<String, Object>> build(String code, String message, HttpStatus status) {
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("code", code);
    body.put("message", message);
    return ResponseEntity.status(status).body(body);
  }
}
