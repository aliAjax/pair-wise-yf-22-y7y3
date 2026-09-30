package com.generated.qualityTrace.repositories;

import com.generated.qualityTrace.models.WorkOrder;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 工单数据访问层（内存实现，种子数据在构造时初始化）。
 * 字段与 database/init.sql 的 work_order 表对齐。
 */
@Repository
public class WorkOrderRepository {

  private final Map<Long, WorkOrder> store = new ConcurrentHashMap<>();
  private final AtomicLong idGen = new AtomicLong(0);

  public WorkOrderRepository() {
    seed();
  }

  private void seed() {
    save(new WorkOrder(null, "WO-2026-0001", "P-100", "轴承座", 500, "L-A",
        "2026-09-01T08:00:00Z", "RUNNING", "2026-09-01T08:00:00Z"));
    save(new WorkOrder(null, "WO-2026-0002", "P-200", "法兰盘", 300, "L-B",
        null, "PLANNED", "2026-09-02T08:00:00Z"));
    save(new WorkOrder(null, "WO-2026-0003", "P-300", "齿轮箱", 120, "L-A",
        "2026-08-15T08:00:00Z", "FINISHED", "2026-08-15T08:00:00Z"));
    save(new WorkOrder(null, "WO-2026-0004", "P-400", "联轴器", 80, "L-C",
        null, "PLANNED", "2026-09-05T08:00:00Z"));
  }

  public List<WorkOrder> findAll() {
    return new ArrayList<>(store.values());
  }

  public Optional<WorkOrder> findById(Long id) {
    return Optional.ofNullable(store.get(id));
  }

  public Optional<WorkOrder> findByOrderNo(String orderNo) {
    return store.values().stream().filter(w -> orderNo.equals(w.orderNo)).findFirst();
  }

  public WorkOrder save(WorkOrder wo) {
    if (wo.id == null) {
      wo.id = idGen.incrementAndGet();
    }
    store.put(wo.id, wo);
    return wo;
  }
}
