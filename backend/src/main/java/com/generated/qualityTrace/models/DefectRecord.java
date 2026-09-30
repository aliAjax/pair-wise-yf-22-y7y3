package com.generated.qualityTrace.models;

/** 不良记录（DefectRecord），归属产品批次。 */
public class DefectRecord {
  public Long id;
  public Long batchId;
  public String defectType;
  public Long defectQty;
  public String severity;
  public String rootCause;
  public String dispositionStatus;
  public String createdAt;

  public DefectRecord() {}

  public DefectRecord(Long id, Long batchId, String defectType, Long defectQty, String severity,
                      String rootCause, String dispositionStatus, String createdAt) {
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
