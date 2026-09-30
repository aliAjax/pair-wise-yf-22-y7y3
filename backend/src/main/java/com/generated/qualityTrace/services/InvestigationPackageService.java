package com.generated.qualityTrace.services;

import com.generated.qualityTrace.constants.ErrorCodes;
import com.generated.qualityTrace.constants.ErrorMessages;
import com.generated.qualityTrace.constants.LogTemplates;
import com.generated.qualityTrace.constants.PackageStatus;
import com.generated.qualityTrace.constants.ShardStatus;
import com.generated.qualityTrace.constants.ShardType;
import com.generated.qualityTrace.exceptions.BusinessException;
import com.generated.qualityTrace.models.DefectRecord;
import com.generated.qualityTrace.models.InspectionItemResult;
import com.generated.qualityTrace.models.InvestigationPackage;
import com.generated.qualityTrace.models.PackageShard;
import com.generated.qualityTrace.models.ProductBatch;
import com.generated.qualityTrace.models.QualityInspection;
import com.generated.qualityTrace.models.WorkOrder;
import com.generated.qualityTrace.repositories.DefectRecordRepository;
import com.generated.qualityTrace.repositories.InspectionItemResultRepository;
import com.generated.qualityTrace.repositories.InvestigationPackageRepository;
import com.generated.qualityTrace.repositories.PackageShardRepository;
import com.generated.qualityTrace.repositories.ProductBatchRepository;
import com.generated.qualityTrace.repositories.QualityInspectionRepository;
import com.generated.qualityTrace.repositories.WorkOrderRepository;
import com.generated.qualityTrace.utils.ChecksumUtils;
import com.generated.qualityTrace.utils.Formatters;
import com.generated.qualityTrace.utils.JsonUtils;
import com.generated.qualityTrace.utils.PackageNoGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 调查包冻结服务。
 *
 * <p>开工单时固定当前版本的工单、产品批次、质量检验、检验项与不良记录，形成不可变快照；
 * 现场在冻结瞬间之后新提交的内容不计入本包（snapshotCutoff 之后），进入下一包。
 *
 * <p>同一工单重复申请只返回同一批次号（幂等），由 {@link PackageNoGenerator} 与
 * 工单唯一查询共同保证。
 */
@Service
public class InvestigationPackageService {

  private static final Logger log = LoggerFactory.getLogger(InvestigationPackageService.class);

  private final InvestigationPackageRepository packageRepo;
  private final PackageShardRepository shardRepo;
  private final WorkOrderRepository workOrderRepo;
  private final ProductBatchRepository batchRepo;
  private final QualityInspectionRepository inspectionRepo;
  private final InspectionItemResultRepository itemRepo;
  private final DefectRecordRepository defectRepo;

  public InvestigationPackageService(InvestigationPackageRepository packageRepo,
                                     PackageShardRepository shardRepo,
                                     WorkOrderRepository workOrderRepo,
                                     ProductBatchRepository batchRepo,
                                     QualityInspectionRepository inspectionRepo,
                                     InspectionItemResultRepository itemRepo,
                                     DefectRecordRepository defectRepo) {
    this.packageRepo = packageRepo;
    this.shardRepo = shardRepo;
    this.workOrderRepo = workOrderRepo;
    this.batchRepo = batchRepo;
    this.inspectionRepo = inspectionRepo;
    this.itemRepo = itemRepo;
    this.defectRepo = defectRepo;
  }

  /**
   * 冻结（或幂等取回）指定工单的调查包。
   */
  public InvestigationPackage freeze(Long workOrderId, String operator) {
    Optional<InvestigationPackage> existing = packageRepo.findByWorkOrderId(workOrderId);
    if (existing.isPresent()) {
      InvestigationPackage pkg = existing.get();
      log.info(Formatters.format(LogTemplates.PACKAGE_IDEMPOTENT_HIT, pkg.packageNo, workOrderId));
      return pkg;
    }

    WorkOrder workOrder = workOrderRepo.findById(workOrderId)
        .orElseThrow(() -> new BusinessException(ErrorCodes.WORK_ORDER_NOT_FOUND,
            Formatters.format(ErrorMessages.WORK_ORDER_NOT_FOUND, workOrderId)));

    String now = Formatters.nowIso();
    String packageNo = PackageNoGenerator.forWorkOrder(workOrderId);

    // 按工单聚合全链路数据
    List<ProductBatch> batches = batchRepo.findByWorkOrderId(workOrderId);
    List<QualityInspection> inspections = new ArrayList<>();
    List<InspectionItemResult> items = new ArrayList<>();
    List<DefectRecord> defects = new ArrayList<>();
    for (ProductBatch b : batches) {
      if (!withinCutoff(b.createdAt, now)) {
        continue;
      }
      List<QualityInspection> batchInspections = inspectionRepo.findByBatchId(b.id);
      for (QualityInspection ins : batchInspections) {
        if (!withinCutoff(ins.createdAt, now)) {
          continue;
        }
        inspections.add(ins);
        for (InspectionItemResult it : itemRepo.findByInspectionId(ins.id)) {
          items.add(it);
        }
      }
      for (DefectRecord d : defectRepo.findByBatchId(b.id)) {
        if (withinCutoff(d.createdAt, now)) {
          defects.add(d);
        }
      }
    }

    List<PackageShard> shards = new ArrayList<>();
    int seq = 1;
    shards.add(buildShard(packageNo, ShardType.WORK_ORDER, seq++, workOrder.orderNo, workOrder));
    for (ProductBatch b : batches) {
      if (withinCutoff(b.createdAt, now)) {
        shards.add(buildShard(packageNo, ShardType.BATCH, seq++, b.batchNo, b));
      }
    }
    for (QualityInspection ins : inspections) {
      shards.add(buildShard(packageNo, ShardType.INSPECTION, seq++, "INS-" + ins.id, ins));
    }
    for (InspectionItemResult it : items) {
      shards.add(buildShard(packageNo, ShardType.ITEM, seq++, "ITEM-" + it.id, it));
    }
    for (DefectRecord d : defects) {
      shards.add(buildShard(packageNo, ShardType.DEFECT, seq++, "DEF-" + d.id, d));
    }
    // MANIFEST 固定最后，初始 PENDING，待数据分片全部导出后生成
    shards.add(new PackageShard(null, packageNo, ShardType.MANIFEST, seq, "MANIFEST",
        ShardStatus.PENDING, null, null, 0, null, null));

    InvestigationPackage pkg = new InvestigationPackage(null, packageNo, workOrderId,
        PackageStatus.FROZEN, now, operator, now, shards.size(), 0, null, now, now);
    packageRepo.save(pkg);
    shardRepo.saveAll(shards);

    log.info(Formatters.format(LogTemplates.PACKAGE_FREEZE, packageNo, workOrderId, now, shards.size()));
    return pkg;
  }

  public Optional<InvestigationPackage> getByPackageNo(String packageNo) {
    return packageRepo.findByPackageNo(packageNo);
  }

  public Optional<InvestigationPackage> getByWorkOrderId(Long workOrderId) {
    return packageRepo.findByWorkOrderId(workOrderId);
  }

  public List<PackageShard> getShards(String packageNo) {
    return shardRepo.findByPackageNo(packageNo);
  }

  private PackageShard buildShard(String packageNo, String type, int seq, String refId, Object entity) {
    String content = JsonUtils.toJson(entity);
    return new PackageShard(null, packageNo, type, seq, refId,
        ShardStatus.PENDING, content, ChecksumUtils.sha256(content), 0, null, null);
  }

  /** 快照截止判断：仅收录 createdAt <= cutoff 的记录（null 视为无法判定，收录）。 */
  private boolean withinCutoff(String createdAt, String cutoff) {
    return createdAt == null || createdAt.compareTo(cutoff) <= 0;
  }
}
