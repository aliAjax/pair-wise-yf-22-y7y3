package com.generated.qualityTrace.constructors;

import java.util.Map;

import com.generated.qualityTrace.models.InvestigationPackage;
import com.generated.qualityTrace.models.InvestigationPackageShard;
import com.generated.qualityTrace.services.InvestigationPackageService;

/**
 * 调查包响应 DTO 构造器：controller 不散写默认结构，
 * 包摘要、分片行、导出结果、清单、核对结果统一在此构造。
 */
public final class InvestigationPackageDtoFactory {

  private InvestigationPackageDtoFactory() {}

  public static Map<String, Object> packageHeader(InvestigationPackage pkg) {
    return com.generated.qualityTrace.utils.JsonUtils.ordered(
        "packageNo", pkg.packageNo,
        "workOrderId", pkg.workOrderId,
        "sequence", pkg.sequence,
        "frozenAt", pkg.frozenAt,
        "status", pkg.status.name(),
        "createdBy", pkg.createdBy,
        "createdAt", pkg.createdAt);
  }

  public static Map<String, Object> shardRow(InvestigationPackageShard shard) {
    return com.generated.qualityTrace.utils.JsonUtils.ordered(
        "shardIndex", shard.shardIndex,
        "shardCode", shard.shardCode(),
        "shardLabel", shard.shardLabel(),
        "status", shard.status.name(),
        "recordCount", shard.recordCount,
        "checksumSha256", shard.checksum == null ? "" : shard.checksum,
        "missingReasons", shard.missingReasons.stream().map(Enum::name).toList());
  }

  public static Map<String, Object> exportResult(InvestigationPackageService.ExportResult result) {
    return com.generated.qualityTrace.utils.JsonUtils.ordered(
        "packageNo", result.pkg().packageNo,
        "status", result.pkg().status.name(),
        "resume", Map.of(
            "doneSkipped", result.doneSkipped(),
            "succeeded", result.succeeded(),
            "failed", result.failed(),
            "failedShardCodes", result.failedShardCodes()),
        "nextPackageHint", Map.of(
            "newInspectionCountAfterFreeze", result.newInspectionCount(),
            "newDefectCountAfterFreeze", result.newDefectCount()));
  }
}
