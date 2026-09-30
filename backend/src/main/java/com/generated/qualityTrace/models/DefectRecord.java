package com.generated.qualityTrace.models;

/**
 * 不良记录实体，反向影响批次状态与追溯结论。
 */
public class DefectRecord {
  public Long id;
  public Long batchId;
  public String defectType;
  public Integer defectQty;
  public String severity;
  public String rootCause;
  public String dispositionStatus;
  public String createdAt;

  public DefectRecord() {}

  public DefectRecord(Long id, Long batchId, String defectType, Integer defectQty,
                      String severity, String rootCause, String dispositionStatus, String createdAt) {
    this.id = id;
    this.batchId = batchId;
    this.defectType = defectType;
    this.defectQty = defectQty;
    this.severity = severity;
    this.rootCause = rootCause;
    this.dispositionStatus = dispositionStatus;
    this.createdAt = createdAt;
  }
}
