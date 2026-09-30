package com.generated.qualityTrace.models;

/**
 * 调查包（冻结批次）。
 *
 * <p>开工单时冻结当前版本的工单、产品批次、质量检验与不良记录，形成不可变快照。
 * 现场在冻结瞬间之后新提交的内容不属于本包（进入下一包），由 {@code snapshotCutoff} 界定。
 *
 * <p>同一工单重复申请只返回同一个批次号（{@code packageNo} 由工单派生，天然幂等）。
 */
public class InvestigationPackage {
  public Long id;
  /** 冻结批次号，由工单派生，同一工单多次申请保持一致。 */
  public String packageNo;
  public Long workOrderId;
  /** 取值见 constants/PackageStatus：FROZEN / EXPORTING / PARTIAL / EXPORTED。 */
  public String status;
  /** 冻结完成时间。 */
  public String frozenAt;
  /** 冻结操作人（审计员）用户标识。 */
  public String frozenBy;
  /** 快照截止时间：仅收录 createdAt <= 该时间的记录，之后新提交内容进入下一包。 */
  public String snapshotCutoff;
  public Integer shardCount;
  public Integer exportedShardCount;
  /** 全部数据分片校验值串联后的总校验值，供离线核对。 */
  public String manifestChecksum;
  public String createdAt;
  public String updatedAt;

  public InvestigationPackage() {}

  public InvestigationPackage(Long id, String packageNo, Long workOrderId, String status,
                              String frozenAt, String frozenBy, String snapshotCutoff,
                              Integer shardCount, Integer exportedShardCount,
                              String manifestChecksum, String createdAt, String updatedAt) {
    this.id = id;
    this.packageNo = packageNo;
    this.workOrderId = workOrderId;
    this.status = status;
    this.frozenAt = frozenAt;
    this.frozenBy = frozenBy;
    this.snapshotCutoff = snapshotCutoff;
    this.shardCount = shardCount;
    this.exportedShardCount = exportedShardCount;
    this.manifestChecksum = manifestChecksum;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }
}
