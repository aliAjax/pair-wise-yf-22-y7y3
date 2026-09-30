package com.generated.qualityTrace.exceptions;

import com.generated.qualityTrace.constants.ErrorCodes;
import com.generated.qualityTrace.constants.ErrorMessages;

/** 403 角色无权。 */
public class RbacDeniedException extends ApiException {
  public RbacDeniedException(String role, String path) {
    super(ErrorCodes.RBAC_DENIED,
        ErrorMessages.RBAC_DENIED + ": role=" + role + " path=" + path, 403);
  }
}
