package com.generated.qualityTrace.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.generated.qualityTrace.models.WorkOrder;

@Repository
public class WorkOrderRepository {

  private final TraceDataStore store;

  public WorkOrderRepository(TraceDataStore store) {
    this.store = store;
  }

  public List<WorkOrder> findAll() {
    return store.listWorkOrders();
  }

  public Optional<WorkOrder> findById(Long id) {
    return store.findWorkOrder(id);
  }
}
