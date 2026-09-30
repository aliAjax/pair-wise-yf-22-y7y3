package com.generated.qualityTrace.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.generated.qualityTrace.models.WorkOrder;
import com.generated.qualityTrace.repositories.WorkOrderRepository;

@Service
public class WorkOrderService {

  private final WorkOrderRepository repo;

  public WorkOrderService(WorkOrderRepository repo) {
    this.repo = repo;
  }

  public List<WorkOrder> list() {
    return repo.findAll();
  }
}
