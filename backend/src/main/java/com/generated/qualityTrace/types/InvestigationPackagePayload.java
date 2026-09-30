package com.generated.qualityTrace.types;

/**
 * 冻结调查包请求体。
 */
public class InvestigationPackagePayload {
  /** 要核验的工单 id。 */
  public Long workOrderId;
  /** 冻结操作备注（可选）。 */
  public String remark;

  public InvestigationPackagePayload() {}

  public InvestigationPackagePayload(Long workOrderId, String remark) {
    this.workOrderId = workOrderId;
    this.remark = remark;
  }
}
