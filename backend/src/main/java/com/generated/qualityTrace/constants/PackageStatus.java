package com.generated.qualityTrace.constants;

/**
 * 调查包（冻结批次）状态。
 */
public final class PackageStatus {
  /** 已冻结，尚未开始导出。 */
  public static final String FROZEN = "FROZEN";
  /** 导出进行中（存在未完成分片）。 */
  public static final String EXPORTING = "EXPORTING";
  /** 部分分片导出失败，等待续传。 */
  public static final String PARTIAL = "PARTIAL";
  /** 全部分片（含汇总）导出完成。 */
  public static final String EXPORTED = "EXPORTED";

  private PackageStatus() {}
}
