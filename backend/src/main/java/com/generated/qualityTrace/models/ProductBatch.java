package com.generated.qualityTrace.models;

/** 产品批次（ProductBatch），归属于生产工单。 */
public class ProductBatch {
  public Long id;
  public String batchNo;
  public Long workOrderId;
  public Long quantity;
  public String materialLotNo;
  public String producedAt;
  public String batchStatus;
  public String createdAt;

  public ProductBatch() {}

  public ProductBatch(Long id, String batchNo, Long workOrderId, Long quantity, String materialLotNo,
                      String producedAt, String batchStatus, String createdAt) {
    this.id = id;
    this.batchNo = batchNo;
    this.workOrderId = workOrderId;
    this.quantity = quantity;
    this.materialLotNo = materialLotNo;
    this.producedAt = producedAt;
    this.batchStatus = batchStatus;
    this.createdAt = createdAt;
  }
}
