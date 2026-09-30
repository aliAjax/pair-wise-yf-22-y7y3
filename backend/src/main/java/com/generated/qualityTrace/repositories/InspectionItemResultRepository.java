package com.generated.qualityTrace.repositories;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.generated.qualityTrace.models.InspectionItemResult;

@Repository
public class InspectionItemResultRepository {

  private final TraceDataStore store;

  public InspectionItemResultRepository(TraceDataStore store) {
    this.store = store;
  }

  public List<InspectionItemResult> findAll() {
    return store.listBatches().stream()
        .flatMap(b -> store.findInspectionsByBatch(b.id, java.time.Instant.MAX).stream())
        .flatMap(i -> i.itemResults.stream())
        .toList();
  }
}
