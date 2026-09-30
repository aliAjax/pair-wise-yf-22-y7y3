package com.generated.qualityTrace.repositories;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.generated.qualityTrace.models.ProductBatch;

@Repository
public class ProductBatchRepository {

  private final TraceDataStore store;

  public ProductBatchRepository(TraceDataStore store) {
    this.store = store;
  }

  public List<ProductBatch> findAll() {
    return store.listBatches();
  }

  /** 冻结切片：只取当前工单且不晚于冻结时间点的批次。 */
  public List<ProductBatch> findFrozenByWorkOrder(Long workOrderId, Instant frozenAt) {
    return store.findBatchesByWorkOrder(workOrderId, frozenAt);
  }
}
