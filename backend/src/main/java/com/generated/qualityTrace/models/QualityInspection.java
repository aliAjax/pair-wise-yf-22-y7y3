package com.generated.qualityTrace.models;

import java.util.ArrayList;
import java.util.List;

/** 质量检验（QualityInspection）：首检 / 巡检 / 终检，归属产品批次。 */
public class QualityInspection {
  public Long id;
  public Long batchId;
  public String inspectorId;
  public String inspectionType;
  public String standardVersion;
  public String resultStatus;
  public String inspectedAt;
  public String createdAt;
  public List<InspectionItemResult> itemResults = new ArrayList<>();

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
