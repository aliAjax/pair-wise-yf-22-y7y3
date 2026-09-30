package com.generated.qualityTrace.services;

import com.generated.qualityTrace.constructors.WorkOrderDtoFactory;
import com.generated.qualityTrace.models.WorkOrder;
import com.generated.qualityTrace.repositories.WorkOrderRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 工单服务。列表响应经 {@link WorkOrderDtoFactory} 构造，不直接散写结构。
 */
@Service
public class WorkOrderService {

  private final WorkOrderRepository repo;

  public WorkOrderService(WorkOrderRepository repo) {
    this.repo = repo;
  }

  public List<Map<String, Object>> list() {
    List<Map<String, Object>> out = new ArrayList<>();
    for (WorkOrder wo : repo.findAll()) {
      out.add(WorkOrderDtoFactory.response(wo));
    }
    return out;
  }
}
