package com.generated.qualityTrace.constants;

/** 冻结调查包状态：OPEN 冻结进行中 -> EXPORTING 分片导出中 -> COMPLETED 全部分片完成 / FAILED 存在失败分片。 */
public enum PackageStatus {
  OPEN,
  EXPORTING,
  COMPLETED,
  FAILED
}
