package com.generated.qualityTrace.models;

/**
 * 生产工单实体。字段与 database/init.sql 中的 work_order 表对齐。
 */
public class WorkOrder {
  public Long id;
  public String orderNo;
  public String productCode;
  public String productName;
  public Integer plannedQty;
  public String lineCode;
  public String startAt;
  public String status;
  /** 记录创建时间，作为冻结批次快照截止时间（snapshotCutoff）的取值依据。 */
  public String createdAt;

  public WorkOrder() {}

  public WorkOrder(Long id, String orderNo, String productCode, String productName,
                   Integer plannedQty, String lineCode, String startAt, String status, String createdAt) {
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
