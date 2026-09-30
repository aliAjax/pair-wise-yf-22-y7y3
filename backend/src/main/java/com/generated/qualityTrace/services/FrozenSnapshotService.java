package com.generated.qualityTrace.services;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.generated.qualityTrace.models.DefectRecord;
import com.generated.qualityTrace.models.ProductBatch;
import com.generated.qualityTrace.models.QualityInspection;
import com.generated.qualityTrace.models.WorkOrder;
import com.generated.qualityTrace.repositories.DefectRecordRepository;
import com.generated.qualityTrace.repositories.ProductBatchRepository;
import com.generated.qualityTrace.repositories.QualityInspectionRepository;
import com.generated.qualityTrace.repositories.TraceDataStore;
import com.generated.qualityTrace.repositories.WorkOrderRepository;

/**
 * 冻结快照：按 frozenAt 固定“这版”工单/批次/检验（含检验项）/不良。
 * 冻结之后现场补录的数据一律不进当前快照（由 createdAt 判定）。
 */
@Service
public class FrozenSnapshotService {

  private final WorkOrderRepository workOrderRepository;
  private final ProductBatchRepository batchRepository;
  private final QualityInspectionRepository inspectionRepository;
  private final DefectRecordRepository defectRepository;
  private final TraceDataStore store;

  public FrozenSnapshotService(WorkOrderRepository workOrderRepository,
                               ProductBatchRepository batchRepository,
                               QualityInspectionRepository inspectionRepository,
                               DefectRecordRepository defectRepository,
                               TraceDataStore store) {
    this.workOrderRepository = workOrderRepository;
    this.batchRepository = batchRepository;
    this.inspectionRepository = inspectionRepository;
    this.defectRepository = defectRepository;
    this.store = store;
  }

  /** 一版冻结切片。 */
  public Snapshot take(Long workOrderId, Instant frozenAt) {
    WorkOrder workOrder = workOrderRepository.findById(workOrderId).orElse(null);
    List<ProductBatch> batches = workOrder == null
        ? List.of()
        : batchRepository.findFrozenByWorkOrder(workOrderId, frozenAt);

    List<QualityInspection> inspections = new ArrayList<>();
    for (ProductBatch batch : batches) {
      inspections.addAll(inspectionRepository.findFrozenByBatch(batch.id, frozenAt));
    }

    List<Long> batchIds = batches.stream().map(b -> b.id).toList();
    List<DefectRecord> defects = defectRepository.findFrozenByBatchIds(batchIds, frozenAt);

    int newInspections = workOrder == null ? 0 : store.countInspectionsAfter(workOrderId, frozenAt);
    int newDefects = workOrder == null ? 0 : store.countDefectsAfter(workOrderId, frozenAt);

    return new Snapshot(workOrder, batches, inspections, defects, newInspections, newDefects);
  }

  /** 冻结切片结果（不可变值对象）。 */
  public record Snapshot(WorkOrder workOrder,
                         List<ProductBatch> batches,
                         List<QualityInspection> inspections,
                         List<DefectRecord> defects,
                         int newInspectionCount,
                         int newDefectCount) {
    public int itemResultCount() {
      return inspections.stream().mapToInt(i -> i.itemResults.size()).sum();
    }
  }
}
