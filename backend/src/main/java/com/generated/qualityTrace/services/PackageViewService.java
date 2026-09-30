package com.generated.qualityTrace.services;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.generated.qualityTrace.constants.ShardStatus;
import com.generated.qualityTrace.models.DefectRecord;
import com.generated.qualityTrace.models.InvestigationPackage;
import com.generated.qualityTrace.models.InvestigationPackageShard;
import com.generated.qualityTrace.models.ProductBatch;
import com.generated.qualityTrace.models.QualityInspection;
import com.generated.qualityTrace.models.WorkOrder;
import com.generated.qualityTrace.utils.Redactor;

/**
 * 包视图服务：把冻结快照 + 分片状态组装成对外视图。
 * 受限检验员（RESTRICTED_INSPECTOR）看到的记录仅含可核验代号；
 * 数量、状态、缺件原因、摘要所有角色一致，保证现场离线核对口径相同。
 */
@Service
public class PackageViewService {

  private final FrozenSnapshotService snapshotService;

  public PackageViewService(FrozenSnapshotService snapshotService) {
    this.snapshotService = snapshotService;
  }

  public Map<String, Object> detail(InvestigationPackage pkg, String role) {
    FrozenSnapshotService.Snapshot snapshot =
        snapshotService.take(pkg.workOrderId, pkg.frozenInstant());

    Map<String, Object> view = new LinkedHashMap<>();
    view.put("packageNo", pkg.packageNo);
    view.put("workOrderId", pkg.workOrderId);
    view.put("sequence", pkg.sequence);
    view.put("frozenAt", pkg.frozenAt);
    view.put("createdBy", pkg.createdBy);
    view.put("status", pkg.status.name());
    view.put("roleView", role);
    view.put("restricted", Redactor.restricted(role));

    WorkOrder wo = snapshot.workOrder();
    view.put("workOrder", wo == null ? null : Redactor.workOrder(wo, role));
    view.put("batches", snapshot.batches().stream().map(b -> Redactor.batch(b, role)).toList());
    view.put("inspections", snapshot.inspections().stream()
        .map(i -> Redactor.inspection(i, role)).toList());
    view.put("defects", snapshot.defects().stream().map(d -> Redactor.defect(d, role)).toList());

    List<Map<String, Object>> shardViews = new ArrayList<>();
    for (InvestigationPackageShard shard : pkg.shards) {
      shardViews.add(shard(shard));
    }
    view.put("shards", shardViews);
    view.put("nextPackageHint", Map.of(
        "newInspectionCountAfterFreeze", snapshot.newInspectionCount(),
        "newDefectCountAfterFreeze", snapshot.newDefectCount(),
        "message", Redactor.restricted(role)
            ? "冻结后新内容已隔离至下一包"
            : "冻结后现场补录的检验/不良不进本包，调用 /next 开下一包"));
    return view;
  }

  public Map<String, Object> shard(InvestigationPackageShard shard) {
    Map<String, Object> map = new LinkedHashMap<>();
    map.put("shardIndex", shard.shardIndex);
    map.put("shardCode", shard.shardCode());
    map.put("shardLabel", shard.shardLabel());
    map.put("status", shard.status.name());
    map.put("recordCount", shard.status == ShardStatus.DONE ? shard.recordCount : 0);
    map.put("checksumSha256", shard.checksum == null ? "" : shard.checksum);
    map.put("completedAt", shard.completedAt == null ? "" : shard.completedAt);
    map.put("missingReasons", shard.missingReasons.stream().map(Enum::name).toList());
    map.put("lastError", shard.lastError == null ? "" : shard.lastError);
    return map;
  }

  public Map<String, Object> exportResult(InvestigationPackageService.ExportResult result, String role) {
    Map<String, Object> map = new LinkedHashMap<>();
    map.put("package", detail(result.pkg(), role));
    map.put("resume", Map.of(
        "doneSkipped", result.doneSkipped(),
        "succeeded", result.succeeded(),
        "failed", result.failed(),
        "failedShardCodes", result.failedShardCodes(),
        "retryHint", result.failed() > 0
            ? "已完成分片已跳过，对失败分片再次调用 export 即可续传"
            : "全部分片完成"));
    return map;
  }
}
