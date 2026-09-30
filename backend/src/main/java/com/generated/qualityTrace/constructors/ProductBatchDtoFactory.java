package com.generated.qualityTrace.constructors;

import com.generated.qualityTrace.models.ProductBatch;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 产品批次响应对象构造器。
 */
public final class ProductBatchDtoFactory {

  private ProductBatchDtoFactory() {}

  public static Map<String, Object> create() {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("id", null);
    m.put("batchNo", null);
    m.put("workOrderId", null);
    m.put("quantity", 0);
    m.put("materialLotNo", null);
    m.put("producedAt", null);
    m.put("batchStatus", null);
    return m;
  }

  public static Map<String, Object> response(ProductBatch b) {
    Map<String, Object> m = create();
    m.put("id", b.id);
    m.put("batchNo", b.batchNo);
    m.put("workOrderId", b.workOrderId);
    m.put("quantity", b.quantity);
    m.put("materialLotNo", b.materialLotNo);
    m.put("producedAt", b.producedAt);
    m.put("batchStatus", b.batchStatus);
    return m;
  }
}
