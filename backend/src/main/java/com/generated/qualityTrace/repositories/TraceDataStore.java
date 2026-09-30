package com.generated.qualityTrace.repositories;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Component;

import com.generated.qualityTrace.constants.DefectSeverity;
import com.generated.qualityTrace.constants.InspectionResultStatus;
import com.generated.qualityTrace.constants.UserRole;
import com.generated.qualityTrace.constants.WorkOrderStatus;
import com.generated.qualityTrace.models.AuditEvent;
import com.generated.qualityTrace.models.DefectRecord;
import com.generated.qualityTrace.models.InspectionItemResult;
import com.generated.qualityTrace.models.ProductBatch;
import com.generated.qualityTrace.models.QualityInspection;
import com.generated.qualityTrace.models.SystemUser;
import com.generated.qualityTrace.models.WorkOrder;

/**
 * 本地内存数据源（项目约定：全部本地数据，禁止第三方 API）。
 * 所有记录都带 createdAt，冻结调查包按 createdAt &lt;= frozenAt 切片，
 * 冻结后现场补录的检验/不良不会进入当前包。
 */
@Component
public class TraceDataStore {

  private final Map<Long, WorkOrder> workOrders = new ConcurrentHashMap<>();
  private final Map<Long, ProductBatch> batches = new ConcurrentHashMap<>();
  private final Map<Long, QualityInspection> inspections = new ConcurrentHashMap<>();
  private final Map<Long, InspectionItemResult> itemResults = new ConcurrentHashMap<>();
  private final Map<Long, DefectRecord> defects = new ConcurrentHashMap<>();
  private final Map<String, SystemUser> users = new ConcurrentHashMap<>();
  private final List<AuditEvent> auditEvents = java.util.Collections.synchronizedList(new ArrayList<>());

  // 补录序列在 seed() 后按现有最大 ID 初始化，避免与种子数据主键冲突。
  private final AtomicLong workOrderSeq = new AtomicLong();
  private final AtomicLong batchSeq = new AtomicLong();
  private final AtomicLong inspectionSeq = new AtomicLong();
  private final AtomicLong itemSeq = new AtomicLong();
  private final AtomicLong defectSeq = new AtomicLong();
  private final AtomicLong auditSeq = new AtomicLong();

  public TraceDataStore() {
    seed();
    // 序列从各表现有最大 ID 起步，补录主键绝不覆盖种子/既有记录。
    workOrderSeq.set(workOrders.keySet().stream().mapToLong(Long::longValue).max().orElse(0));
    batchSeq.set(batches.keySet().stream().mapToLong(Long::longValue).max().orElse(0));
    inspectionSeq.set(inspections.keySet().stream().mapToLong(Long::longValue).max().orElse(0));
    itemSeq.set(itemResults.keySet().stream().mapToLong(Long::longValue).max().orElse(0));
    defectSeq.set(defects.keySet().stream().mapToLong(Long::longValue).max().orElse(0));
  }

  private void seed() {
    Instant base = Instant.now().minus(7, ChronoUnit.DAYS).truncatedTo(ChronoUnit.SECONDS);

    users.put("auditor", new SystemUser(1L, "auditor", "auditor123", "审计员-王审", UserRole.AUDITOR.name()));
    users.put("inspector", new SystemUser(2L, "inspector", "insp123", "质检员-李检", UserRole.INSPECTOR.name()));
    users.put("restricted", new SystemUser(3L, "restricted", "restricted123", "受限检验员-赵核", UserRole.RESTRICTED_INSPECTOR.name()));
    users.put("manager", new SystemUser(4L, "manager", "mgr123", "质量经理-孙理", UserRole.QUALITY_MANAGER.name()));

    // 工单 1001：批次 + 检验（含检验项）+ 不良，记录完整。
    WorkOrder wo1 = new WorkOrder(1001L, "WO-20260920-1001", "P-AXLE-01", "驱动轴总成", 500L,
        "LINE-A", base.toString(), WorkOrderStatus.RUNNING.name(), base.toString());
    workOrders.put(wo1.id, wo1);

    ProductBatch b1 = new ProductBatch(2001L, "B-1001-01", 1001L, 250L, "MAT-STEEL-77",
        base.plus(1, ChronoUnit.DAYS).toString(), "ACTIVE", base.plus(1, ChronoUnit.DAYS).toString());
    batches.put(b1.id, b1);

    QualityInspection q1 = new QualityInspection(3001L, 2001L, "inspector", "FIRST",
        "STD-AXLE-v3.2", InspectionResultStatus.PASS.name(),
        base.plus(2, ChronoUnit.DAYS).toString(), base.plus(2, ChronoUnit.DAYS).toString());
    inspections.put(q1.id, q1);
    itemResults.put(4001L, new InspectionItemResult(4001L, 3001L, "DIM-01", "轴径", "25.02",
        "24.98", "25.05", "PASS", q1.createdAt));
    itemResults.put(4002L, new InspectionItemResult(4002L, 3001L, "SFC-02", "表面粗糙度", "Ra1.6",
        "Ra0.8", "Ra1.8", "PASS", q1.createdAt));
    defects.put(5001L, new DefectRecord(5001L, 2001L, "SCRATCH", 3L, DefectSeverity.MINOR.name(),
        "转运划伤", "DISPOSED", base.plus(3, ChronoUnit.DAYS).toString()));

    // 工单 1002：有批次，但缺检验项结果（演示缺件原因 NO_INSPECTION_ITEM）。
    WorkOrder wo2 = new WorkOrder(1002L, "WO-20260921-1002", "P-GEAR-02", "从动齿轮", 300L,
        "LINE-B", base.plus(1, ChronoUnit.DAYS).toString(), WorkOrderStatus.PAUSED.name(),
        base.plus(1, ChronoUnit.DAYS).toString());
    workOrders.put(wo2.id, wo2);
    ProductBatch b2 = new ProductBatch(2002L, "B-1002-01", 1002L, 120L, "MAT-STEEL-88",
        base.plus(2, ChronoUnit.DAYS).toString(), "HOLD", base.plus(2, ChronoUnit.DAYS).toString());
    batches.put(b2.id, b2);
    QualityInspection q2 = new QualityInspection(3002L, 2002L, "inspector", "PATROL",
        "STD-GEAR-v2.0", InspectionResultStatus.RECHECK.name(),
        base.plus(3, ChronoUnit.DAYS).toString(), base.plus(3, ChronoUnit.DAYS).toString());
    inspections.put(q2.id, q2);
    // 3002 无任何 InspectionItemResult。

    // 工单 1003：工单本身存在，但没有批次（演示 NO_BATCH 连锁缺件）。
    WorkOrder wo3 = new WorkOrder(1003L, "WO-20260922-1003", "P-BOLT-03", "高强度螺栓", 2000L,
        "LINE-C", base.plus(2, ChronoUnit.DAYS).toString(), WorkOrderStatus.PLANNED.name(),
        base.plus(2, ChronoUnit.DAYS).toString());
    workOrders.put(wo3.id, wo3);
  }

  // ---- 基础查询 ----

  public List<WorkOrder> listWorkOrders() {
    List<WorkOrder> list = new ArrayList<>(workOrders.values());
    list.sort(Comparator.comparing(w -> w.id));
    return list;
  }

  public Optional<WorkOrder> findWorkOrder(Long id) {
    return Optional.ofNullable(workOrders.get(id));
  }

  public List<ProductBatch> listBatches() {
    List<ProductBatch> list = new ArrayList<>(batches.values());
    list.sort(Comparator.comparing(b -> b.id));
    return list;
  }

  public List<ProductBatch> findBatchesByWorkOrder(Long workOrderId, Instant frozenAt) {
    List<ProductBatch> result = new ArrayList<>();
    for (ProductBatch batch : batches.values()) {
      if (batch.workOrderId.equals(workOrderId) && !Instant.parse(batch.createdAt).isAfter(frozenAt)) {
        result.add(batch);
      }
    }
    result.sort(Comparator.comparing(b -> b.id));
    return result;
  }

  public List<QualityInspection> findInspectionsByBatch(Long batchId, Instant frozenAt) {
    List<QualityInspection> result = new ArrayList<>();
    for (QualityInspection inspection : inspections.values()) {
      if (inspection.batchId.equals(batchId) && !Instant.parse(inspection.createdAt).isAfter(frozenAt)) {
        QualityInspection copy = new QualityInspection(inspection.id, inspection.batchId,
            inspection.inspectorId, inspection.inspectionType, inspection.standardVersion,
            inspection.resultStatus, inspection.inspectedAt, inspection.createdAt);
        for (InspectionItemResult item : itemResults.values()) {
          if (item.inspectionId.equals(inspection.id) && !Instant.parse(item.createdAt).isAfter(frozenAt)) {
            copy.itemResults.add(item);
          }
        }
        copy.itemResults.sort(Comparator.comparing(i -> i.id));
        result.add(copy);
      }
    }
    result.sort(Comparator.comparing(i -> i.id));
    return result;
  }

  public List<DefectRecord> findDefectsByBatchIds(List<Long> batchIds, Instant frozenAt) {
    List<DefectRecord> result = new ArrayList<>();
    for (DefectRecord defect : defects.values()) {
      if (batchIds.contains(defect.batchId) && !Instant.parse(defect.createdAt).isAfter(frozenAt)) {
        result.add(defect);
      }
    }
    result.sort(Comparator.comparing(d -> d.id));
    return result;
  }

  /** 冻结之后新增的检验数量（下一包预告）。 */
  public int countInspectionsAfter(Long workOrderId, Instant frozenAt) {
    int count = 0;
    for (ProductBatch batch : findBatchesByWorkOrder(workOrderId, Instant.MAX)) {
      for (QualityInspection inspection : inspections.values()) {
        if (inspection.batchId.equals(batch.id) && Instant.parse(inspection.createdAt).isAfter(frozenAt)) {
          count++;
        }
      }
    }
    return count;
  }

  /** 冻结之后新增的不良数量（下一包预告）。 */
  public int countDefectsAfter(Long workOrderId, Instant frozenAt) {
    int count = 0;
    for (ProductBatch batch : findBatchesByWorkOrder(workOrderId, Instant.MAX)) {
      for (DefectRecord defect : defects.values()) {
        if (defect.batchId.equals(batch.id) && Instant.parse(defect.createdAt).isAfter(frozenAt)) {
          count++;
        }
      }
    }
    return count;
  }

  // ---- 现场补录 ----

  public synchronized QualityInspection submitInspection(Long batchId, String inspectorId,
                                                         String inspectionType,
                                                         String standardVersion,
                                                         String resultStatus) {
    long id = inspectionSeq.incrementAndGet();
    String now = Instant.now().toString();
    QualityInspection inspection = new QualityInspection(id, batchId, inspectorId, inspectionType,
        standardVersion, resultStatus, now, now);
    inspections.put(id, inspection);
    return inspection;
  }

  public synchronized DefectRecord submitDefect(Long batchId, String defectType, Long defectQty,
                                                String severity, String rootCause,
                                                String dispositionStatus) {
    long id = defectSeq.incrementAndGet();
    String now = Instant.now().toString();
    DefectRecord defect = new DefectRecord(id, batchId, defectType, defectQty, severity,
        rootCause, dispositionStatus, now);
    defects.put(id, defect);
    return defect;
  }

  // ---- 用户/日志 ----

  public Optional<SystemUser> findUser(String username) {
    return Optional.ofNullable(users.get(username));
  }

  public List<AuditEvent> listAuditEvents() {
    synchronized (auditEvents) {
      return new ArrayList<>(auditEvents);
    }
  }

  public void appendAudit(String actor, String action, String targetType, String targetId, String detail) {
    auditEvents.add(new AuditEvent(auditSeq.incrementAndGet(), actor, action, targetType, targetId,
        detail, Instant.now().toString()));
  }
}
