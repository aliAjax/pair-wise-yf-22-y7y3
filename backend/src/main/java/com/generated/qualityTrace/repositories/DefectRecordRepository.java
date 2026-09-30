package com.generated.qualityTrace.repositories;

import com.generated.qualityTrace.models.DefectRecord;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 不良记录数据访问层。
 */
@Repository
public class DefectRecordRepository {

  private final Map<Long, DefectRecord> store = new ConcurrentHashMap<>();
  private final AtomicLong idGen = new AtomicLong(0);

  public DefectRecordRepository() {
    seed();
  }

  private void seed() {
    save(new DefectRecord(null, 2L, "SCRATCH", 5, "MINOR",
        "操作不当", "CLOSED", "2026-09-04T14:00:00Z"));
    save(new DefectRecord(null, 2L, "CRACK", 2, "CRITICAL",
        null, "OPEN", "2026-09-04T15:00:00Z"));
  }

  public List<DefectRecord> findAll() {
    return new ArrayList<>(store.values());
  }

  public Optional<DefectRecord> findById(Long id) {
    return Optional.ofNullable(store.get(id));
  }

  public List<DefectRecord> findByBatchId(Long batchId) {
    List<DefectRecord> out = new ArrayList<>();
    for (DefectRecord d : store.values()) {
      if (batchId.equals(d.batchId)) {
        out.add(d);
      }
    }
    return out;
  }

  public DefectRecord save(DefectRecord d) {
    if (d.id == null) {
      d.id = idGen.incrementAndGet();
    }
    store.put(d.id, d);
    return d;
  }
}
