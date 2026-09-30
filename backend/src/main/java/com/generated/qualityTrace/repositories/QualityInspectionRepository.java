package com.generated.qualityTrace.repositories;

import com.generated.qualityTrace.models.QualityInspection;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 质量检验数据访问层。
 */
@Repository
public class QualityInspectionRepository {

  private final Map<Long, QualityInspection> store = new ConcurrentHashMap<>();
  private final AtomicLong idGen = new AtomicLong(0);

  public QualityInspectionRepository() {
    seed();
  }

  private void seed() {
    save(new QualityInspection(null, 1L, "U-101", "FIRST", "STD-1.0",
        "PASS", "2026-09-02T11:00:00Z", "2026-09-02T11:00:00Z"));
    save(new QualityInspection(null, 1L, "U-102", "PATROL", null,
        "CONDITIONAL_PASS", "2026-09-03T11:00:00Z", "2026-09-03T11:00:00Z"));
    save(new QualityInspection(null, 2L, "U-101", "FINAL", "STD-1.1",
        "FAIL", "2026-09-04T11:00:00Z", "2026-09-04T11:00:00Z"));
    save(new QualityInspection(null, 4L, "U-103", "FINAL", "STD-2.0",
        "PASS", "2026-08-21T11:00:00Z", "2026-08-21T11:00:00Z"));
  }

  public List<QualityInspection> findAll() {
    return new ArrayList<>(store.values());
  }

  public Optional<QualityInspection> findById(Long id) {
    return Optional.ofNullable(store.get(id));
  }

  public List<QualityInspection> findByBatchId(Long batchId) {
    List<QualityInspection> out = new ArrayList<>();
    for (QualityInspection ins : store.values()) {
      if (batchId.equals(ins.batchId)) {
        out.add(ins);
      }
    }
    return out;
  }

  public QualityInspection save(QualityInspection ins) {
    if (ins.id == null) {
      ins.id = idGen.incrementAndGet();
    }
    store.put(ins.id, ins);
    return ins;
  }
}
