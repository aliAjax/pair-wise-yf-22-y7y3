package com.generated.qualityTrace.services;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.generated.qualityTrace.constants.ErrorMessages;
import com.generated.qualityTrace.constants.LogTemplates;
import com.generated.qualityTrace.constants.MissingReason;
import com.generated.qualityTrace.constants.PackageStatus;
import com.generated.qualityTrace.constants.ShardStatus;
import com.generated.qualityTrace.constants.ShardType;
import com.generated.qualityTrace.exceptions.ApiException;
import com.generated.qualityTrace.constants.ErrorCodes;
import com.generated.qualityTrace.models.InvestigationPackage;
import com.generated.qualityTrace.models.InvestigationPackageShard;
import com.generated.qualityTrace.models.WorkOrder;
import com.generated.qualityTrace.repositories.AuditLogRepository;
import com.generated.qualityTrace.repositories.InvestigationPackageRepository;
import com.generated.qualityTrace.repositories.ShardBlobRepository;
import com.generated.qualityTrace.repositories.WorkOrderRepository;
import com.generated.qualityTrace.utils.ChecksumUtils;
import com.generated.qualityTrace.utils.JsonUtils;

/**
 * 冻结调查包编排：
 * 1) 开包即冻结（frozenAt 固定这版数据）；
 * 2) 同一工单重复申请幂等取回同一批号；
 * 3) 导出按分片进行，DONE 分片续传时跳过、仅重试 PENDING/FAILED；
 * 4) 冻结后现场新内容不进本包，可开下一包；
 * 5) 离线清单（数量/摘要/缺件原因）可下载、可重算核对。
 */
@Service
public class InvestigationPackageService {

  private static final Logger log = LoggerFactory.getLogger(InvestigationPackageService.class);

  private final InvestigationPackageRepository packageRepository;
  private final ShardBlobRepository blobRepository;
  private final WorkOrderRepository workOrderRepository;
  private final FrozenSnapshotService snapshotService;
  private final ShardExportService shardExportService;
  private final AuditLogRepository auditLogRepository;

  public InvestigationPackageService(InvestigationPackageRepository packageRepository,
                                     ShardBlobRepository blobRepository,
                                     WorkOrderRepository workOrderRepository,
                                     FrozenSnapshotService snapshotService,
                                     ShardExportService shardExportService,
                                     AuditLogRepository auditLogRepository) {
    this.packageRepository = packageRepository;
    this.blobRepository = blobRepository;
    this.workOrderRepository = workOrderRepository;
    this.snapshotService = snapshotService;
    this.shardExportService = shardExportService;
    this.auditLogRepository = auditLogRepository;
  }

  /** 冻结开包（或幂等取回当前包）。 */
  public synchronized InvestigationPackage getOrFreeze(Long workOrderId, String actor) {
    Optional<InvestigationPackage> existing = packageRepository.findLatestByWorkOrder(workOrderId);
    if (existing.isPresent()) {
      InvestigationPackage pkg = existing.get();
      log.info(LogTemplates.PACKAGE_REUSE, actor, workOrderId, pkg.packageNo);
      auditLogRepository.append(actor, "PACKAGE_REUSE", "InvestigationPackage", pkg.packageNo,
          "workOrderId=" + workOrderId);
      return pkg;
    }
    return doFreeze(workOrderId, actor, 1);
  }

  /** 冻结下一包：冻结时间点为现在，现场已补录内容进入此包。 */
  public synchronized InvestigationPackage freezeNext(String packageNo, String actor) {
    InvestigationPackage current = requirePackage(packageNo);
    return doFreeze(current.workOrderId, actor, current.sequence + 1);
  }

  private InvestigationPackage doFreeze(Long workOrderId, String actor, int sequence) {
    WorkOrder workOrder = workOrderRepository.findById(workOrderId)
        .orElseThrow(() -> new ApiException(ErrorCodes.WORK_ORDER_NOT_FOUND,
            String.format(ErrorMessages.WORK_ORDER_NOT_FOUND, workOrderId), 404));

    String frozenAt = Instant.now().toString();
    InvestigationPackage pkg = new InvestigationPackage();
    pkg.packageNo = InvestigationPackage.buildPackageNo(workOrder.orderNo, sequence);
    pkg.workOrderId = workOrderId;
    pkg.frozenAt = frozenAt;
    pkg.createdBy = actor;
    pkg.createdAt = Instant.now().toString();
    pkg.status = PackageStatus.OPEN;
    pkg.sequence = sequence;

    int index = 1;
    for (ShardType type : ShardType.values()) {
      InvestigationPackageShard shard = new InvestigationPackageShard();
      shard.packageId = pkg.id;
      shard.shardType = type;
      shard.shardIndex = index++;
      shard.status = ShardStatus.PENDING;
      pkg.shards.add(shard);
    }
    packageRepository.save(pkg);

    log.info(LogTemplates.PACKAGE_FREEZE, actor, workOrderId, pkg.packageNo, frozenAt);
    auditLogRepository.append(actor, "PACKAGE_FREEZE", "InvestigationPackage", pkg.packageNo,
        "workOrderId=" + workOrderId + " frozenAt=" + frozenAt + " sequence=" + sequence);
    return pkg;
  }

  /**
   * 断点续传导出：
   * DONE 且摘要可校验的分片直接跳过（已完成分片恢复）；
   * 只重试 PENDING / FAILED 分片；任一失败包置 FAILED，全部成功置 COMPLETED。
   *
   * @param failShardCode 可空。演示用：让指定分片在本次尝试中失败一次（命中即模拟传输错误）。
   */
  public synchronized ExportResult export(String packageNo, String actor, String failShardCode) {
    InvestigationPackage pkg = requirePackage(packageNo);
    FrozenSnapshotService.Snapshot snapshot =
        snapshotService.take(pkg.workOrderId, pkg.frozenInstant());

    pkg.status = PackageStatus.EXPORTING;
    int doneSkipped = 0;
    int succeeded = 0;
    int failed = 0;
    List<String> failedCodes = new ArrayList<>();
    String effectiveFailCode = failShardCode;

    for (InvestigationPackageShard shard : pkg.shards) {
      if (shard.status == ShardStatus.DONE && verifyStoredBlob(pkg.packageNo, shard)) {
        doneSkipped++;
        continue;
      }
      try {
        ShardExportService.ShardPayload payload =
            shardExportService.export(pkg, shard.shardType, snapshot, effectiveFailCode);
        // 该注入只对第一个待处理分片生效一次，保证重试可以成功（真实场景对应临时网络错误已恢复）。
        if (effectiveFailCode != null && effectiveFailCode.equals(shard.shardType.getCode())) {
          effectiveFailCode = null;
        }
        blobRepository.save(pkg.packageNo, shard.shardCode(), payload.content());
        shard.recordCount = payload.recordCount();
        shard.checksum = shardExportService.checksum(payload.content());
        shard.status = ShardStatus.DONE;
        shard.completedAt = Instant.now().toString();
        shard.lastError = null;
        shard.missingReasons = new ArrayList<>(payload.missingReasons());
        succeeded++;
      } catch (ShardExportService.ShardExportException e) {
        shard.status = ShardStatus.FAILED;
        shard.lastError = e.getMessage();
        if (!shard.missingReasons.contains(e.getReason())) {
          shard.missingReasons.add(e.getReason());
        }
        failed++;
        failedCodes.add(shard.shardCode());
        log.warn(LogTemplates.SHARD_FAILED, pkg.packageNo, shard.shardCode(), e.getReason());
        effectiveFailCode = null;
      }
    }

    boolean anyFailed = failed > 0;
    pkg.status = anyFailed ? PackageStatus.FAILED
        : (pkg.shards.stream().allMatch(s -> s.status == ShardStatus.DONE)
            ? PackageStatus.COMPLETED : PackageStatus.FAILED);
    packageRepository.save(pkg);

    log.info(LogTemplates.PACKAGE_EXPORT, actor, pkg.packageNo, doneSkipped, succeeded, failed);
    auditLogRepository.append(actor, "PACKAGE_EXPORT", "InvestigationPackage", pkg.packageNo,
        "doneSkipped=" + doneSkipped + " succeeded=" + succeeded + " failed=" + failed
            + " status=" + pkg.status);
    return new ExportResult(pkg, doneSkipped, succeeded, failed, failedCodes,
        snapshot.newInspectionCount(), snapshot.newDefectCount());
  }

  private boolean verifyStoredBlob(String packageNo, InvestigationPackageShard shard) {
    Optional<byte[]> blob = blobRepository.find(packageNo, shard.shardCode());
    return blob.isPresent()
        && shardExportService.checksum(blob.get()).equals(shard.checksum);
  }

  public InvestigationPackage requirePackage(String packageNo) {
    return packageRepository.findByPackageNo(packageNo)
        .orElseThrow(() -> new ApiException(ErrorCodes.PACKAGE_NOT_FOUND,
            String.format(ErrorMessages.PACKAGE_NOT_FOUND, packageNo), 404));
  }

  public byte[] getShardContent(String packageNo, String shardCode) {
    InvestigationPackage pkg = requirePackage(packageNo);
    InvestigationPackageShard shard = findShard(pkg, shardCode);
    if (shard.status != ShardStatus.DONE) {
      throw new ApiException(ErrorCodes.SHARD_NOT_FOUND,
          String.format(ErrorMessages.SHARD_NOT_FOUND, shardCode, packageNo) + " (not done)", 409);
    }
    return blobRepository.find(packageNo, shardCode)
        .orElseThrow(() -> new ApiException(ErrorCodes.SHARD_NOT_FOUND,
            String.format(ErrorMessages.SHARD_NOT_FOUND, shardCode, packageNo), 404));
  }

  private InvestigationPackageShard findShard(InvestigationPackage pkg, String shardCode) {
    return pkg.shards.stream()
        .filter(s -> s.shardCode().equals(shardCode))
        .findFirst()
        .orElseThrow(() -> new ApiException(ErrorCodes.SHARD_NOT_FOUND,
            String.format(ErrorMessages.SHARD_NOT_FOUND, shardCode, pkg.packageNo), 404));
  }

  // ---- 离线清单 ----

  /** 生成离线清单（结构与角色无关；受限身份在视图层对记录内容打码，清单数字/摘要不变）。 */
  public Manifest buildManifest(String packageNo, String actor, String role) {
    InvestigationPackage pkg = requirePackage(packageNo);
    FrozenSnapshotService.Snapshot snapshot =
        snapshotService.take(pkg.workOrderId, pkg.frozenInstant());

    List<Map<String, Object>> shardEntries = new ArrayList<>();
    int totalRecords = 0;
    int doneCount = 0;
    for (InvestigationPackageShard shard : pkg.shards) {
      boolean done = shard.status == ShardStatus.DONE;
      if (done) {
        totalRecords += shard.recordCount;
        doneCount++;
      }
      shardEntries.add(JsonUtils.ordered(
          "shardIndex", shard.shardIndex,
          "shardCode", shard.shardCode(),
          "shardLabel", shard.shardLabel(),
          "status", shard.status.name(),
          "recordCount", done ? shard.recordCount : 0,
          "checksumSha256", shard.checksum == null ? "" : shard.checksum,
          "completedAt", shard.completedAt == null ? "" : shard.completedAt,
          "missingReasons", shard.missingReasons.stream().map(Enum::name).toList(),
          "lastError", shard.lastError == null ? "" : shard.lastError));
    }

    List<Map<String, Object>> missingEntries = new ArrayList<>();
    for (InvestigationPackageShard shard : pkg.shards) {
      for (MissingReason reason : shard.missingReasons) {
        missingEntries.add(JsonUtils.ordered(
            "shardCode", shard.shardCode(),
            "shardLabel", shard.shardLabel(),
            "reasonCode", reason.name(),
            "reasonText", reasonText(reason)));
      }
    }

    // 稳定正文：只含冻结时点数据 + 分片状态/摘要/缺件，不含生成时间，保证离线可重复核对。
    Map<String, Object> stableBody = JsonUtils.ordered(
        "packageNo", pkg.packageNo,
        "workOrderId", pkg.workOrderId,
        "sequence", pkg.sequence,
        "frozenAt", pkg.frozenAt,
        "createdBy", pkg.createdBy,
        "packageStatus", pkg.status.name(),
        "summary", JsonUtils.ordered(
            "shardTotal", pkg.shards.size(),
            "shardDone", doneCount,
            "recordTotal", totalRecords,
            "workOrderCount", snapshot.workOrder() == null ? 0 : 1,
            "batchCount", snapshot.batches().size(),
            "inspectionCount", snapshot.inspections().size(),
            "inspectionItemCount", snapshot.itemResultCount(),
            "defectCount", snapshot.defects().size()),
        "shards", shardEntries,
        "missingItems", missingEntries,
        "nextPackageHint", JsonUtils.ordered(
            "newInspectionCountAfterFreeze", snapshot.newInspectionCount(),
            "newDefectCountAfterFreeze", snapshot.newDefectCount(),
            "note", "冻结后现场补录进入下一包"));

    // 对外整体：稳定正文 + 生成元数据（元数据不参与摘要计算）。
    Map<String, Object> body = new LinkedHashMap<>(stableBody);
    body.put("manifestChecksum", ChecksumUtils.sha256Text(JsonUtils.pretty(stableBody)));
    body.put("generatedAt", Instant.now().toString());
    body.put("generatedFor", role);

    String bodyText = JsonUtils.pretty(body);
    String manifestChecksum = ChecksumUtils.sha256Text(JsonUtils.pretty(stableBody));
    return new Manifest(pkg, body, bodyText, manifestChecksum, snapshot, stableBody);
  }

  /** 离线核对：重算各 DONE 分片摘要并比对清单。 */
  public VerifyResult verify(String packageNo, String actor) {
    InvestigationPackage pkg = requirePackage(packageNo);
    Manifest manifest = buildManifest(packageNo, actor, actor);

    List<Map<String, Object>> checks = new ArrayList<>();
    boolean allMatch = true;
    for (InvestigationPackageShard shard : pkg.shards) {
      boolean match = false;
      String recomputed = "";
      if (shard.status == ShardStatus.DONE) {
        recomputed = blobRepository.find(pkg.packageNo, shard.shardCode())
            .map(b -> shardExportService.checksum(b)).orElse("");
        match = recomputed.equals(shard.checksum);
      }
      if (shard.status == ShardStatus.DONE && !match) {
        allMatch = false;
      }
      checks.add(JsonUtils.ordered(
          "shardCode", shard.shardCode(),
          "status", shard.status.name(),
          "manifestChecksum", shard.checksum == null ? "" : shard.checksum,
          "recomputedChecksum", recomputed,
          "match", shard.status == ShardStatus.DONE && match));
    }
    if (pkg.shards.stream().anyMatch(s -> s.status != ShardStatus.DONE)) {
      allMatch = false;
    }
    boolean match = allMatch;
    log.info(LogTemplates.MANIFEST_VERIFY, actor, pkg.packageNo, match);
    auditLogRepository.append(actor, "MANIFEST_VERIFY", "InvestigationPackage", pkg.packageNo,
        "match=" + match);
    return new VerifyResult(pkg.packageNo, manifest.manifestChecksum(), match, checks);
  }

  /** 供控制器下载清单文件时记日志。 */
  public void logManifestDownload(String packageNo, String actor, String role) {
    log.info(LogTemplates.MANIFEST_DOWNLOAD, actor, role, packageNo);
    auditLogRepository.append(actor, "MANIFEST_DOWNLOAD", "InvestigationPackage", packageNo,
        "role=" + role);
  }

  public static String reasonText(MissingReason reason) {
    return com.generated.qualityTrace.constants.MissingReasonTexts.text(reason);
  }

  /**
   * 离线清单文件：文本头 + 稳定正文（STABLE SECTION，可截取后本地 SHA-256 与摘要比对）
   * + 生成元数据。分片摘要逐片列出，数量/缺件原因均可离线阅读。
   */
  public byte[] manifestBytes(Manifest manifest) {
    String stableText = JsonUtils.pretty(manifest.stableBody());
    String header = "# 离线核对清单 " + manifest.pkg.packageNo + "\n"
        + "# 清单稳定正文摘要 SHA-256: " + manifest.manifestChecksum() + "\n"
        + "# 离线核对方法：截取 STABLE SECTION BEGIN/END 之间正文，本地计算 SHA-256 与本摘要比对；\n"
        + "# 各分片可直接对照 shards[].checksumSha256 与 recordCount、missingItems。\n\n"
        + "# === STABLE SECTION BEGIN ===\n"
        + stableText
        + "\n# === STABLE SECTION END ===\n\n"
        + "# 生成元数据（不参与摘要计算）\n";
    java.util.Map<String, Object> meta = JsonUtils.ordered(
        "manifestChecksum", manifest.manifestChecksum(),
        "generatedAt", manifest.body().get("generatedAt"),
        "generatedFor", manifest.body().get("generatedFor"));
    return (header + JsonUtils.pretty(meta) + "\n").getBytes(StandardCharsets.UTF_8);
  }

  /** 导出结果视图。 */
  public record ExportResult(InvestigationPackage pkg, int doneSkipped, int succeeded, int failed,
                             List<String> failedShardCodes, int newInspectionCount,
                             int newDefectCount) {}

  /** 清单结果。stableBody 为参与摘要计算的稳定正文（离线可无网络重算）。 */
  public record Manifest(InvestigationPackage pkg, Map<String, Object> body, String bodyText,
                         String manifestChecksum, FrozenSnapshotService.Snapshot snapshot,
                         Map<String, Object> stableBody) {}

  /** 离线核对结果。 */
  public record VerifyResult(String packageNo, String manifestChecksum, boolean allMatch,
                             List<Map<String, Object>> shards) {}
}
