package com.generated.qualityTrace.exceptions;

import com.generated.qualityTrace.constants.ErrorCodes;
import com.generated.qualityTrace.constants.ErrorMessages;

/** 401 未认证。 */
public class AuthRequiredException extends ApiException {
  public AuthRequiredException() {
    super(ErrorCodes.AUTH_REQUIRED, ErrorMessages.AUTH_REQUIRED, 401);
  }

  public AuthRequiredException(String message) {
    super(ErrorCodes.AUTH_REQUIRED, message, 401);
  }
}
