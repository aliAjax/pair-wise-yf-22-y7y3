package com.generated.qualityTrace.repositories;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.generated.qualityTrace.models.DefectRecord;

@Repository
public class DefectRecordRepository {

  private final TraceDataStore store;

  public DefectRecordRepository(TraceDataStore store) {
    this.store = store;
  }

  public List<DefectRecord> findAll() {
    return store.findDefectsByBatchIds(
        store.listBatches().stream().map(b -> b.id).toList(), Instant.MAX);
  }

  public List<DefectRecord> findFrozenByBatchIds(List<Long> batchIds, Instant frozenAt) {
    return store.findDefectsByBatchIds(batchIds, frozenAt);
  }

  public DefectRecord submit(Long batchId, String defectType, Long defectQty, String severity,
                             String rootCause, String dispositionStatus) {
    return store.submitDefect(batchId, defectType, defectQty, severity, rootCause, dispositionStatus);
  }
}
