package com.generated.qualityTrace.repositories;

import com.generated.qualityTrace.models.ProductBatch;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 产品批次数据访问层。
 */
@Repository
public class ProductBatchRepository {

  private final Map<Long, ProductBatch> store = new ConcurrentHashMap<>();
  private final AtomicLong idGen = new AtomicLong(0);

  public ProductBatchRepository() {
    seed();
  }

  private void seed() {
    save(new ProductBatch(null, "B-2026-0901", 1L, 200, "ML-001",
        "2026-09-02T10:00:00Z", "PRODUCED", "2026-09-02T10:00:00Z"));
    save(new ProductBatch(null, "B-2026-0902", 1L, 300, "ML-002",
        "2026-09-03T10:00:00Z", "PRODUCED", "2026-09-03T10:00:00Z"));
    save(new ProductBatch(null, "B-2026-0903", 2L, 0, null,
        null, "PLANNED", "2026-09-02T09:00:00Z"));
    save(new ProductBatch(null, "B-2026-0815", 3L, 120, "ML-003",
        "2026-08-20T10:00:00Z", "PRODUCED", "2026-08-20T10:00:00Z"));
  }

  public List<ProductBatch> findAll() {
    return new ArrayList<>(store.values());
  }

  public Optional<ProductBatch> findById(Long id) {
    return Optional.ofNullable(store.get(id));
  }

  public List<ProductBatch> findByWorkOrderId(Long workOrderId) {
    List<ProductBatch> out = new ArrayList<>();
    for (ProductBatch b : store.values()) {
      if (workOrderId.equals(b.workOrderId)) {
        out.add(b);
      }
    }
    return out;
  }

  public ProductBatch save(ProductBatch b) {
    if (b.id == null) {
      b.id = idGen.incrementAndGet();
    }
    store.put(b.id, b);
    return b;
  }
}
