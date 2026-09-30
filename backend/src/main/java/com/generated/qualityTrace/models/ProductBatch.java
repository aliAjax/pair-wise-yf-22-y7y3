package com.generated.qualityTrace.models;

/**
 * 产品批次实体，关联工单与检验/不良记录。
 */
public class ProductBatch {
  public Long id;
  public String batchNo;
  public Long workOrderId;
  public Integer quantity;
  public String materialLotNo;
  public String producedAt;
  public String batchStatus;
  public String createdAt;

  public ProductBatch() {}

  public ProductBatch(Long id, String batchNo, Long workOrderId, Integer quantity,
                      String materialLotNo, String producedAt, String batchStatus, String createdAt) {
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
