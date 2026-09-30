package com.generated.qualityTrace.constants;

/** 分片状态：PENDING 未导出 -> DONE 已完成（可断点续传跳过）/ FAILED 本次导出失败（重试未完成部分）。 */
public enum ShardStatus {
  PENDING,
  DONE,
  FAILED
}
