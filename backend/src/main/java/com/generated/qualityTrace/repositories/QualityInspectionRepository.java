package com.generated.qualityTrace.repositories;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.generated.qualityTrace.models.QualityInspection;

@Repository
public class QualityInspectionRepository {

  private final TraceDataStore store;

  public QualityInspectionRepository(TraceDataStore store) {
    this.store = store;
  }

  public List<QualityInspection> findAll() {
    return store.listBatches().stream()
        .flatMap(b -> store.findInspectionsByBatch(b.id, Instant.MAX).stream())
        .toList();
  }

  public List<QualityInspection> findFrozenByBatch(Long batchId, Instant frozenAt) {
    return store.findInspectionsByBatch(batchId, frozenAt);
  }

  public QualityInspection submit(Long batchId, String inspectorId, String inspectionType,
                                  String standardVersion, String resultStatus) {
    return store.submitInspection(batchId, inspectorId, inspectionType, standardVersion, resultStatus);
  }
}
