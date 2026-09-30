package com.generated.qualityTrace.constants;

public final class LogTemplates {
  public static final String CREATE = "create";
  public static final String UPDATE = "update";
  public static final String STATUS = "status";
  public static final String EXPORT = "export";

  /** 调查包冻结：actor 冻结人, workOrderId, packageNo, frozenAt。 */
  public static final String PACKAGE_FREEZE =
      "investigation package frozen: actor={} workOrderId={} packageNo={} frozenAt={}";
  /** 同一工单重复申请幂等取回。 */
  public static final String PACKAGE_REUSE =
      "investigation package reused idempotently: actor={} workOrderId={} packageNo={}";
  /** 现场新内容落入下一包。 */
  public static final String PACKAGE_ROLLOVER =
      "new on-site content rolls to next package: workOrderId={} packageNo={} newInspections={} newDefects={}";
  /** 分片导出（含断点续传：doneSkipped=跳过数）。 */
  public static final String PACKAGE_EXPORT =
      "package export attempt: actor={} packageNo={} doneSkipped={} succeeded={} failed={}";
  /** 分片单条失败。 */
  public static final String SHARD_FAILED =
      "shard export failed: packageNo={} shardCode={} reason={}";
  /** 离线清单下载。 */
  public static final String MANIFEST_DOWNLOAD =
      "offline manifest downloaded: actor={} role={} packageNo={}";
  /** 离线核对。 */
  public static final String MANIFEST_VERIFY =
      "offline manifest verified: actor={} packageNo={} match={}";
  /** 现场补录。 */
  public static final String ONSITE_SUBMIT =
      "on-site record submitted: actor={} type={} id={}";

  private LogTemplates() {}
}
