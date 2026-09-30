package com.generated.qualityTrace.constants;

public final class ErrorMessages {
  public static final String AUTH_REQUIRED = "missing token";
  public static final String AUTH_INVALID = "invalid or expired token";
  public static final String RBAC_DENIED = "role denied";
  public static final String VALIDATION_FAILED = "request validation failed";
  public static final String PACKAGE_NOT_FOUND = "investigation package not found: %s";
  public static final String WORK_ORDER_NOT_FOUND = "work order not found: %s";
  public static final String SHARD_NOT_FOUND = "shard %s not found in package %s";
  public static final String PACKAGE_SEALED = "frozen package is sealed and cannot be modified: %s";
  public static final String RATE_LIMITED = "too many requests, slow down";
  public static final String INTERNAL_ERROR = "internal server error";

  private ErrorMessages() {}
}
