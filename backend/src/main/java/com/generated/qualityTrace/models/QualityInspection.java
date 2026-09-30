package com.generated.qualityTrace.models;

/**
 * 质量检验实体，包含若干检验项结果（InspectionItemResult）。
 */
public class QualityInspection {
  public Long id;
  public Long batchId;
  /** 检验员身份标识；受限角色在包内只看到可核验代号。 */
  public String inspectorId;
  public String inspectionType;
  public String standardVersion;
  public String resultStatus;
  public String inspectedAt;
  public String createdAt;

  public QualityInspection() {}

  public QualityInspection(Long id, Long batchId, String inspectorId, String inspectionType,
                           String standardVersion, String resultStatus, String inspectedAt, String createdAt) {
    this.id = id;
    this.batchId = batchId;
    this.inspectorId = inspectorId;
    this.inspectionType = inspectionType;
    this.standardVersion = standardVersion;
    this.resultStatus = resultStatus;
    this.inspectedAt = inspectedAt;
    this.createdAt = createdAt;
  }
}
