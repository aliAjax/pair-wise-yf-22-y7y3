package com.generated.qualityTrace.services;

import com.generated.qualityTrace.config.PackageExportConfig;
import com.generated.qualityTrace.constants.ErrorCodes;
import com.generated.qualityTrace.constants.ErrorMessages;
import com.generated.qualityTrace.constants.LogTemplates;
import com.generated.qualityTrace.constants.PackageStatus;
import com.generated.qualityTrace.constants.ShardStatus;
import com.generated.qualityTrace.constants.ShardType;
import com.generated.qualityTrace.constructors.InvestigationPackageDtoFactory;
import com.generated.qualityTrace.exceptions.BusinessException;
import com.generated.qualityTrace.models.InvestigationPackage;
import com.generated.qualityTrace.models.PackageShard;
import com.generated.qualityTrace.repositories.InvestigationPackageRepository;
import com.generated.qualityTrace.repositories.PackageShardRepository;
import com.generated.qualityTrace.types.PackageExportResult;
import com.generated.qualityTrace.types.PackageManifestPayload;
import com.generated.qualityTrace.utils.ChecksumUtils;
import com.generated.qualityTrace.utils.Formatters;
import com.generated.qualityTrace.utils.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 调查包导出服务，支持断点续传。
 *
 * <p>导出按分片顺序推进：
 * <ul>
 *   <li>已完成分片（EXPORTED）凭校验值跳过，不重复导出；</li>
 *   <li>失败分片（FAILED）记录原因与重试次数，续传时只补未完成部分；</li>
 *   <li>全部数据分片完成后才生成 MANIFEST 汇总分片。</li>
 * </ul>
 */
@Service
public class PackageExportService {

  private static final Logger log = LoggerFactory.getLogger(PackageExportService.class);

  private final InvestigationPackageRepository packageRepo;
  private final PackageShardRepository shardRepo;
  private final PackageManifestService manifestService;
  private final PackageExportConfig config;

  public PackageExportService(InvestigationPackageRepository packageRepo,
                               PackageShardRepository shardRepo,
                               PackageManifestService manifestService,
                               PackageExportConfig config) {
    this.packageRepo = packageRepo;
    this.shardRepo = shardRepo;
    this.manifestService = manifestService;
    this.config = config;
  }

  /**
   * 导出（或续传）指定调查包。
   */
  public PackageExportResult export(String packageNo, String operator) {
    InvestigationPackage pkg = packageRepo.findByPackageNo(packageNo)
        .orElseThrow(() -> new BusinessException(ErrorCodes.PACKAGE_NOT_FOUND,
            Formatters.format(ErrorMessages.PACKAGE_NOT_FOUND, packageNo)));
    List<PackageShard> shards = shardRepo.findByPackageNo(packageNo);

    // 幂等：已导出完成的包直接返回，不重复导出
    boolean alreadyDone = PackageStatus.EXPORTED.equals(pkg.status)
        && shards.stream().allMatch(s -> ShardStatus.EXPORTED.equals(s.status));
    if (alreadyDone) {
      log.info(Formatters.format(LogTemplates.PACKAGE_EXPORT_DONE, packageNo,
          shards.size(), shards.size(), pkg.manifestChecksum));
      PackageExportResult r = new PackageExportResult();
      r.packageNo = packageNo;
      r.status = PackageStatus.EXPORTED;
      r.totalShards = shards.size();
      r.exportedShards = shards.size();
      r.manifestChecksum = pkg.manifestChecksum;
      r.idempotent = true;
      return r;
    }

    long completedBefore = shards.stream()
        .filter(s -> ShardStatus.EXPORTED.equals(s.status) && !ShardType.MANIFEST.equals(s.shardType))
        .count();
    log.info(Formatters.format(LogTemplates.PACKAGE_EXPORT_RESUME, packageNo, completedBefore,
        shards.size() - completedBefore));

    PackageExportResult result = new PackageExportResult();
    result.packageNo = packageNo;
    result.totalShards = shards.size();

    for (PackageShard shard : shards) {
      if (ShardType.MANIFEST.equals(shard.shardType)) {
        continue; // 数据分片全部完成后再生成汇总
      }

      if (ShardStatus.EXPORTED.equals(shard.status)) {
        if (ChecksumUtils.verify(shard.content, shard.checksum)) {
          result.shardResults.add(InvestigationPackageDtoFactory.shardResult(
              shard.shardType, shard.shardSeq, shard.refId, ShardStatus.EXPORTED,
              shard.checksum, shard.retryCount, null));
          log.info(Formatters.format(LogTemplates.PACKAGE_SHARD_SKIP, packageNo, shard.shardSeq));
          continue;
        }
        // 校验值不一致：内容可能被篡改，重置后重新导出
        log.warn(Formatters.format(LogTemplates.PACKAGE_SHARD_FAILED, packageNo, shard.shardSeq,
            "校验值不一致，重新导出"));
        shard.status = ShardStatus.PENDING;
      }

      boolean simulateFail = config.isSimulateFailure()
          && shard.shardSeq != null
          && shard.shardSeq == config.getFailOnShard()
          && (shard.retryCount == null || shard.retryCount == 0);

      if (simulateFail) {
        shard.status = ShardStatus.FAILED;
        shard.retryCount = (shard.retryCount == null ? 0 : shard.retryCount) + 1;
        shard.errorMessage = Formatters.format(ErrorMessages.SHARD_EXPORT_FAILED,
            "seq=" + shard.shardSeq, "模拟导出中断（演示断点续传）");
        result.shardResults.add(InvestigationPackageDtoFactory.shardResult(
            shard.shardType, shard.shardSeq, shard.refId, ShardStatus.FAILED,
            shard.checksum, shard.retryCount, shard.errorMessage));
        log.error(Formatters.format(LogTemplates.PACKAGE_SHARD_FAILED, packageNo, shard.shardSeq,
            shard.errorMessage));
        break;
      }

      // 导出成功
      shard.status = ShardStatus.EXPORTED;
      shard.retryCount = (shard.retryCount == null ? 0 : shard.retryCount) + 1;
      shard.exportedAt = Formatters.nowIso();
      shard.errorMessage = null;
      result.shardResults.add(InvestigationPackageDtoFactory.shardResult(
          shard.shardType, shard.shardSeq, shard.refId, ShardStatus.EXPORTED,
          shard.checksum, shard.retryCount, null));
      log.info(Formatters.format(LogTemplates.PACKAGE_SHARD_EXPORTED, packageNo, shard.shardSeq,
          shard.shardType, shard.checksum));
    }

    long exportedDataCount = shards.stream()
        .filter(s -> ShardStatus.EXPORTED.equals(s.status) && !ShardType.MANIFEST.equals(s.shardType))
        .count();
    pkg.exportedShardCount = (int) exportedDataCount;

    boolean dataAllExported = shards.stream()
        .filter(s -> !ShardType.MANIFEST.equals(s.shardType))
        .allMatch(s -> ShardStatus.EXPORTED.equals(s.status));

    if (dataAllExported) {
      PackageShard manifestShard = shards.stream()
          .filter(s -> ShardType.MANIFEST.equals(s.shardType))
          .findFirst()
          .orElseThrow(() -> new BusinessException(ErrorCodes.MANIFEST_INCOMPLETE,
              Formatters.format(ErrorMessages.MANIFEST_INCOMPLETE, "汇总分片缺失")));
      PackageManifestPayload manifest = manifestService.buildManifest(packageNo);
      String manifestJson = JsonUtils.toJson(manifest);
      manifestShard.content = manifestJson;
      manifestShard.checksum = ChecksumUtils.sha256(manifestJson);
      manifestShard.status = ShardStatus.EXPORTED;
      manifestShard.exportedAt = Formatters.nowIso();
      manifestShard.errorMessage = null;

      pkg.manifestChecksum = manifest.manifestChecksum;
      pkg.exportedShardCount = (int) shards.stream().filter(s -> ShardStatus.EXPORTED.equals(s.status)).count();
      pkg.status = PackageStatus.EXPORTED;
      result.exportedShards = pkg.exportedShardCount;
      result.manifestChecksum = pkg.manifestChecksum;
      result.status = PackageStatus.EXPORTED;
      log.info(Formatters.format(LogTemplates.PACKAGE_MANIFEST_BUILT, packageNo,
          manifest.counts.get("batchCount"), manifest.counts.get("inspectionCount"),
          manifest.counts.get("defectCount"), manifest.missingParts.size()));
      log.info(Formatters.format(LogTemplates.PACKAGE_EXPORT_DONE, packageNo,
          pkg.exportedShardCount, shards.size(), pkg.manifestChecksum));
    } else {
      pkg.status = PackageStatus.PARTIAL;
      result.status = PackageStatus.PARTIAL;
      result.exportedShards = (int) exportedDataCount;
      log.warn("调查包 {} 部分分片未完成，等待续传（已导出 {}/{}）", packageNo, exportedDataCount,
          shards.size() - 1);
    }

    pkg.updatedAt = Formatters.nowIso();
    packageRepo.save(pkg);
    shardRepo.saveAll(shards);
    return result;
  }
}
