package com.generated.qualityTrace.models;

/**
 * 调查包分片。
 *
 * <p>导出按分片推进：已完成分片凭 {@code checksum} 跳过（断点续传），
 * 失败分片记录 {@code errorMessage} 与 {@code retryCount}，重试时只补未完成部分。
 */
public class PackageShard {
  public Long id;
  public String packageNo;
  /** 取值见 constants/ShardType：WORK_ORDER / BATCH / INSPECTION / DEFECT / MANIFEST。 */
  public String shardType;
  /** 分片在包内的顺序号，MANIFEST 固定在最后。 */
  public Integer shardSeq;
  /** 分片所快照的实体标识（如批次号、检验单号），便于离线核对定位。 */
  public String refId;
  /** 取值见 constants/ShardStatus：PENDING / EXPORTED / FAILED。 */
  public String status;
  /** 快照内容（JSON 文本），冻结后不可变。 */
  public String content;
  /** 分片内容 SHA-256，用于离线校验是否被篡改。 */
  public String checksum;
  public Integer retryCount;
  public String errorMessage;
  public String exportedAt;

  public PackageShard() {}

  public PackageShard(Long id, String packageNo, String shardType, Integer shardSeq, String refId,
                      String status, String content, String checksum, Integer retryCount,
                      String errorMessage, String exportedAt) {
    this.id = id;
    this.packageNo = packageNo;
    this.shardType = shardType;
    this.shardSeq = shardSeq;
    this.refId = refId;
    this.status = status;
    this.content = content;
    this.checksum = checksum;
    this.retryCount = retryCount;
    this.errorMessage = errorMessage;
    this.exportedAt = exportedAt;
  }
}
