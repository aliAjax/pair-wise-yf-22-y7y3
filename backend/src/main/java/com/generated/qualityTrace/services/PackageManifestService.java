package com.generated.qualityTrace.services;

import com.generated.qualityTrace.constants.ErrorCodes;
import com.generated.qualityTrace.constants.ErrorMessages;
import com.generated.qualityTrace.constants.MissingPartReason;
import com.generated.qualityTrace.constants.ShardType;
import com.generated.qualityTrace.constructors.PackageManifestDtoFactory;
import com.generated.qualityTrace.exceptions.BusinessException;
import com.generated.qualityTrace.models.PackageShard;
import com.generated.qualityTrace.models.InvestigationPackage;
import com.generated.qualityTrace.repositories.InvestigationPackageRepository;
import com.generated.qualityTrace.repositories.PackageShardRepository;
import com.generated.qualityTrace.types.PackageManifestPayload;
import com.generated.qualityTrace.utils.ChecksumUtils;
import com.generated.qualityTrace.utils.Formatters;
import com.generated.qualityTrace.utils.JsonUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 包内离线汇总服务。
 *
 * <p>生成数量核对、检验结论摘要、不良严重度/处置摘要与缺件原因清单，
 * 并给出各分片校验值与包级总校验值，使审计员在无库环境下也能离线核对。
 */
@Service
public class PackageManifestService {

  private final InvestigationPackageRepository packageRepo;
  private final PackageShardRepository shardRepo;

  public PackageManifestService(InvestigationPackageRepository packageRepo,
                                PackageShardRepository shardRepo) {
    this.packageRepo = packageRepo;
    this.shardRepo = shardRepo;
  }

  public PackageManifestPayload buildManifest(String packageNo) {
    InvestigationPackage pkg = packageRepo.findByPackageNo(packageNo)
        .orElseThrow(() -> new BusinessException(ErrorCodes.PACKAGE_NOT_FOUND,
            Formatters.format(ErrorMessages.PACKAGE_NOT_FOUND, packageNo)));
    List<PackageShard> shards = shardRepo.findByPackageNo(packageNo);

    PackageManifestPayload manifest = PackageManifestDtoFactory.empty();
    manifest.packageNo = pkg.packageNo;
    manifest.workOrderId = pkg.workOrderId;
    manifest.frozenAt = pkg.frozenAt;
    manifest.snapshotCutoff = pkg.snapshotCutoff;
    manifest.generatedAt = Formatters.nowIso();

    List<Map<String, Object>> batchContents = new ArrayList<>();
    List<Map<String, Object>> inspectionContents = new ArrayList<>();
    List<Map<String, Object>> itemContents = new ArrayList<>();
    List<Map<String, Object>> defectContents = new ArrayList<>();
    List<String> dataChecksums = new ArrayList<>();

    for (PackageShard s : shards) {
      if (ShardType.MANIFEST.equals(s.shardType)) {
        continue;
      }
      dataChecksums.add(s.checksum);
      Map<String, Object> content = JsonUtils.toMap(s.content);
      switch (s.shardType) {
        case ShardType.WORK_ORDER:
          manifest.orderNo = (String) content.get("orderNo");
          break;
        case ShardType.BATCH:
          batchContents.add(content);
          break;
        case ShardType.INSPECTION:
          inspectionContents.add(content);
          break;
        case ShardType.ITEM:
          itemContents.add(content);
          break;
        case ShardType.DEFECT:
          defectContents.add(content);
          break;
        default:
          break;
      }
    }

    // 数量核对
    int defectQtyTotal = defectContents.stream()
        .mapToInt(c -> toInt(c.get("defectQty")))
        .sum();
    manifest.counts.put("batchCount", batchContents.size());
    manifest.counts.put("inspectionCount", inspectionContents.size());
    manifest.counts.put("inspectionItemCount", itemContents.size());
    manifest.counts.put("defectCount", defectContents.size());
    manifest.counts.put("defectQtyTotal", defectQtyTotal);

    // 检验结论摘要
    for (Map<String, Object> c : inspectionContents) {
      String result = c.get("resultStatus") == null ? "UNKNOWN" : String.valueOf(c.get("resultStatus"));
      manifest.inspectionSummary.merge(result, 1, Integer::sum);
    }
    // 不良严重度与处置摘要
    for (Map<String, Object> c : defectContents) {
      String severity = c.get("severity") == null ? "UNKNOWN" : String.valueOf(c.get("severity"));
      manifest.defectSeveritySummary.merge(severity, 1, Integer::sum);
      String disposition = c.get("dispositionStatus") == null ? "UNKNOWN" : String.valueOf(c.get("dispositionStatus"));
      manifest.dispositionSummary.merge(disposition, 1, Integer::sum);
    }

    // 缺件原因
    List<Map<String, Object>> missingParts = new ArrayList<>();
    if (batchContents.isEmpty()) {
      missingParts.add(PackageManifestDtoFactory.missingPart(MissingPartReason.MISSING_BATCH,
          "工单下没有任何产品批次", manifest.orderNo));
    }
    Set<Long> inspectedBatchIds = inspectionContents.stream()
        .map(c -> toLong(c.get("batchId")))
        .collect(Collectors.toSet());
    for (Map<String, Object> b : batchContents) {
      Long bid = toLong(b.get("id"));
      String batchNo = String.valueOf(b.get("batchNo"));
      if (isBlank(b.get("materialLotNo"))) {
        missingParts.add(PackageManifestDtoFactory.missingPart(MissingPartReason.MISSING_MATERIAL_LOT,
            "批次缺少材料批号", batchNo));
      }
      if (!inspectedBatchIds.contains(bid)) {
        missingParts.add(PackageManifestDtoFactory.missingPart(MissingPartReason.MISSING_INSPECTION,
            "批次没有任何质量检验记录", batchNo));
      }
    }
    Set<Long> itemInspectionIds = itemContents.stream()
        .map(c -> toLong(c.get("inspectionId")))
        .collect(Collectors.toSet());
    for (Map<String, Object> ins : inspectionContents) {
      Long iid = toLong(ins.get("id"));
      String refId = "INS-" + iid;
      if (isBlank(ins.get("standardVersion"))) {
        missingParts.add(PackageManifestDtoFactory.missingPart(MissingPartReason.MISSING_STANDARD_VERSION,
            "检验缺少标准版本号", refId));
      }
      if (!itemInspectionIds.contains(iid)) {
        missingParts.add(PackageManifestDtoFactory.missingPart(MissingPartReason.MISSING_INSPECTION_ITEMS,
            "检验单没有任何检验项结果", refId));
      }
    }
    for (Map<String, Object> d : defectContents) {
      if (isBlank(d.get("rootCause"))) {
        missingParts.add(PackageManifestDtoFactory.missingPart(MissingPartReason.MISSING_ROOT_CAUSE,
            "不良记录缺少根本原因", "DEF-" + d.get("id")));
      }
    }
    manifest.missingParts = missingParts;

    // 分片清单（供离线逐片核对校验值）：仅列数据分片，MANIFEST 自身不列入避免自指
    List<Map<String, Object>> shardList = new ArrayList<>();
    for (PackageShard s : shards) {
      if (ShardType.MANIFEST.equals(s.shardType)) {
        continue;
      }
      Map<String, Object> sm = new LinkedHashMap<>();
      sm.put("shardType", s.shardType);
      sm.put("shardSeq", s.shardSeq);
      sm.put("refId", s.refId);
      sm.put("status", s.status);
      sm.put("checksum", s.checksum);
      shardList.add(sm);
    }
    manifest.shards = shardList;

    // 包级总校验值：各数据分片校验值串联后 SHA-256
    manifest.manifestChecksum = ChecksumUtils.combine(dataChecksums);
    return manifest;
  }

  private int toInt(Object v) {
    if (v == null) {
      return 0;
    }
    if (v instanceof Number) {
      return ((Number) v).intValue();
    }
    try {
      return Integer.parseInt(String.valueOf(v));
    } catch (NumberFormatException e) {
      return 0;
    }
  }

  private Long toLong(Object v) {
    if (v == null) {
      return null;
    }
    if (v instanceof Number) {
      return ((Number) v).longValue();
    }
    try {
      return Long.parseLong(String.valueOf(v));
    } catch (NumberFormatException e) {
      return null;
    }
  }

  private boolean isBlank(Object v) {
    return v == null || String.valueOf(v).trim().isEmpty() || "null".equals(String.valueOf(v));
  }
}
