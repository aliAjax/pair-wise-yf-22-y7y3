package com.generated.qualityTrace.models;

/** 生产工单（WorkOrder）。 */
public class WorkOrder {
  public Long id;
  public String orderNo;
  public String productCode;
  public String productName;
  public Long plannedQty;
  public String lineCode;
  public String startAt;
  public String status;
  /** 入库/补录时间，冻结切片按该时间判定“这版”记录。 */
  public String createdAt;

  public WorkOrder() {}

  public WorkOrder(Long id, String orderNo, String productCode, String productName, Long plannedQty,
                   String lineCode, String startAt, String status, String createdAt) {
    this.id = id;
    this.orderNo = orderNo;
    this.productCode = productCode;
    this.productName = productName;
    this.plannedQty = plannedQty;
    this.lineCode = lineCode;
    this.startAt = startAt;
    this.status = status;
    this.createdAt = createdAt;
  }
}
