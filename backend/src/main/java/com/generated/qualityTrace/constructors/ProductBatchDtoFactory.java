package com.generated.qualityTrace.constructors;

import java.util.LinkedHashMap;
import java.util.Map;

import com.generated.qualityTrace.models.ProductBatch;

/** 产品批次响应 DTO 构造器。 */
public final class ProductBatchDtoFactory {

  private ProductBatchDtoFactory() {}

  public static Map<String, Object> create(ProductBatch b) {
    Map<String, Object> map = new LinkedHashMap<>();
    map.put("id", b.id);
    map.put("batchNo", b.batchNo);
    map.put("workOrderId", b.workOrderId);
    map.put("quantity", b.quantity);
    map.put("materialLotNo", b.materialLotNo);
    map.put("producedAt", b.producedAt);
    map.put("batchStatus", b.batchStatus);
    map.put("createdAt", b.createdAt);
    return map;
  }
}
