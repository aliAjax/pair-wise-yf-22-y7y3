package com.generated.qualityTrace.constants;

/**
 * 错误码集中定义。service 与 controller 分别包装异常时引用同一组码，
 * 禁止在各处散落字面量。
 */
public final class ErrorCodes {
  public static final String AUTH_REQUIRED = "AUTH_REQUIRED";
  public static final String AUTH_INVALID = "AUTH_INVALID";
  public static final String RBAC_DENIED = "RBAC_DENIED";

  public static final String WORK_ORDER_NOT_FOUND = "WORK_ORDER_NOT_FOUND";
  public static final String PACKAGE_NOT_FOUND = "PACKAGE_NOT_FOUND";
  public static final String PACKAGE_ALREADY_FROZEN = "PACKAGE_ALREADY_FROZEN";
  public static final String PACKAGE_NOT_EXPORTABLE = "PACKAGE_NOT_EXPORTABLE";
  public static final String SHARD_CHECKSUM_MISMATCH = "SHARD_CHECKSUM_MISMATCH";
  public static final String SHARD_EXPORT_FAILED = "SHARD_EXPORT_FAILED";
  public static final String MANIFEST_INCOMPLETE = "MANIFEST_INCOMPLETE";
  public static final String INVALID_PARAMETER = "INVALID_PARAMETER";
  public static final String RATE_LIMITED = "RATE_LIMITED";
  public static final String INTERNAL_ERROR = "INTERNAL_ERROR";

  private ErrorCodes() {}
}
