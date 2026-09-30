package com.generated.qualityTrace.models;

import com.generated.qualityTrace.constants.MissingReason;
import com.generated.qualityTrace.constants.ShardStatus;
import com.generated.qualityTrace.constants.ShardType;

/**
 * 调查包分片：每个分片独立导出、独立落盘摘要。
 * DONE 分片在断点续传时直接跳过；PENDING/FAILED 分片重试。
 */
public class InvestigationPackageShard {
  public Long id;
  public Long packageId;
  public ShardType shardType;
  /** 分片序号，从 1 开始，即导出/核验顺序。 */
  public int shardIndex;
  public ShardStatus status;
  public int recordCount;
  /** 分片内容 SHA-256（十六进制），供离线核对。 */
  public String checksum;
  public String completedAt;
  public String lastError;
  /** 缺件原因码集合（离线清单原样带出）。 */
  public java.util.List<MissingReason> missingReasons = new java.util.ArrayList<>();

  public String shardCode() {
    return shardType.getCode();
  }

  public String shardLabel() {
    return shardType.getLabel();
  }
}
