package com.generated.qualityTrace.models;

/**
 * 检验项结果，决定 QualityInspection 的最终结论。
 */
public class InspectionItemResult {
  public Long id;
  public Long inspectionId;
  public String itemCode;
  public String itemName;
  public String measuredValue;
  public String limitMin;
  public String limitMax;
  public String itemStatus;

  public InspectionItemResult() {}

  public InspectionItemResult(Long id, Long inspectionId, String itemCode, String itemName,
                              String measuredValue, String limitMin, String limitMax, String itemStatus) {
    this.id = id;
    this.inspectionId = inspectionId;
    this.itemCode = itemCode;
    this.itemName = itemName;
    this.measuredValue = measuredValue;
    this.limitMin = limitMin;
    this.limitMax = limitMax;
    this.itemStatus = itemStatus;
  }
}
