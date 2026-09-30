package com.generated.qualityTrace.constants;

/**
 * 包内缺件原因码，随离线清单带出，现场无网络也能核对“为什么少东西”。
 * WORK_ORDER_NOT_FOUND 工单不存在（种子数据中演示一个缺件工单）。
 * NO_BATCH 工单下尚无产品批次。
 * NO_INSPECTION 批次下没有质量检验记录。
 * NO_INSPECTION_ITEM 检验单缺少检验项结果。
 * NO_DEFECT 批次下没有不良记录（正常情况，仅作完整性标注）。
 * SHARD_EXPORT_FAILED 分片导出失败，需断点续传重试。
 */
public enum MissingReason {
  WORK_ORDER_NOT_FOUND,
  NO_BATCH,
  NO_INSPECTION,
  NO_INSPECTION_ITEM,
  NO_DEFECT,
  SHARD_EXPORT_FAILED
}
