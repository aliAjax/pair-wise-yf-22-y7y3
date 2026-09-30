package com.generated.qualityTrace.middlewares;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.generated.qualityTrace.constants.ErrorCodes;
import com.generated.qualityTrace.constants.ErrorMessages;
import com.generated.qualityTrace.exceptions.ApiException;
import com.generated.qualityTrace.utils.JsonUtils;

/**
 * 全局异常处理：service/controller 各自包装的 ApiException 在此统一成错误信封，
 * 未预期异常记 500 INTERNAL_ERROR，不吞错误码。
 */
@RestControllerAdvice
public class ErrorHandlerMiddleware {

  private static final Logger log = LoggerFactory.getLogger(ErrorHandlerMiddleware.class);

  @ExceptionHandler(ApiException.class)
  public ResponseEntity<Map<String, Object>> handleApi(ApiException ex) {
    if (ex.getHttpStatus() >= 500) {
      log.error("api exception: code={} message={}", ex.getCode(), ex.getMessage());
    } else {
      log.warn("api exception: code={} message={}", ex.getCode(), ex.getMessage());
    }
    return ResponseEntity.status(ex.getHttpStatus()).body(JsonUtils.ordered(
        "code", ex.getCode(),
        "message", ex.getMessage()));
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<Map<String, Object>> handleUnreadable(HttpMessageNotReadableException ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(JsonUtils.ordered(
        "code", ErrorCodes.VALIDATION_FAILED,
        "message", ErrorMessages.VALIDATION_FAILED + ": unreadable JSON body"));
  }

  @ExceptionHandler(NoResourceFoundException.class)
  public ResponseEntity<Map<String, Object>> handleNoResource(NoResourceFoundException ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(JsonUtils.ordered(
        "code", ErrorCodes.ROUTE_NOT_FOUND,
        "message", "route or resource not found"));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, Object>> handleUnexpected(Exception ex) {
    log.error("unexpected exception", ex);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(JsonUtils.ordered(
        "code", ErrorCodes.INTERNAL_ERROR,
        "message", ErrorMessages.INTERNAL_ERROR));
  }
}
