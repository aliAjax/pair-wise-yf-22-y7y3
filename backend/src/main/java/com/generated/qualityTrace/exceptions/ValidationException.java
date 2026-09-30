package com.generated.qualityTrace.exceptions;

import com.generated.qualityTrace.constants.ErrorCodes;
import com.generated.qualityTrace.constants.ErrorMessages;

/** 400 入参/业务校验失败。 */
public class ValidationException extends ApiException {
  public ValidationException(String message) {
    super(ErrorCodes.VALIDATION_FAILED, ErrorMessages.VALIDATION_FAILED + ": " + message, 400);
  }
}
