package com.generated.qualityTrace.controllers.support;

import com.generated.qualityTrace.constants.ErrorCodes;
import com.generated.qualityTrace.exceptions.ApiException;
import com.generated.qualityTrace.exceptions.BusinessException;
import org.springframework.http.HttpStatus;

/**
 * controller 层异常包装支持：把 service 层 {@link BusinessException} 包装为
 * controller 层 {@link ApiException}，体现两层分别包装异常。
 */
public final class ControllerSupport {

  private ControllerSupport() {}

  public static ApiException wrap(BusinessException e) {
    HttpStatus status = isNotFound(e.getCode()) ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
    return new ApiException(e.getCode(), e.getMessage(), status);
  }

  private static boolean isNotFound(String code) {
    return ErrorCodes.PACKAGE_NOT_FOUND.equals(code)
        || ErrorCodes.WORK_ORDER_NOT_FOUND.equals(code);
  }
}
