package com.generated.qualityTrace.constructors;

import com.generated.qualityTrace.models.InvestigationPackage;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 调查包响应对象构造器。页面/服务不得直接散写默认结构，统一经此构造。
 */
public final class InvestigationPackageDtoFactory {

  private InvestigationPackageDtoFactory() {}

  /** 构造调查包响应（不含分片明细）。 */
  public static Map<String, Object> response(InvestigationPackage pkg) {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("id", pkg.id);
    m.put("packageNo", pkg.packageNo);
    m.put("workOrderId", pkg.workOrderId);
    m.put("status", pkg.status);
    m.put("frozenAt", pkg.frozenAt);
    m.put("frozenBy", pkg.frozenBy);
    m.put("snapshotCutoff", pkg.snapshotCutoff);
    m.put("shardCount", pkg.shardCount);
    m.put("exportedShardCount", pkg.exportedShardCount);
    m.put("manifestChecksum", pkg.manifestChecksum);
    m.put("createdAt", pkg.createdAt);
    m.put("updatedAt", pkg.updatedAt);
    return m;
  }

  /** 构造分片导出结果项。 */
  public static Map<String, Object> shardResult(String shardType, Integer shardSeq, String refId,
                                                String status, String checksum, Integer retryCount,
                                                String errorMessage) {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("shardType", shardType);
    m.put("shardSeq", shardSeq);
    m.put("refId", refId);
    m.put("status", status);
    m.put("checksum", checksum);
    m.put("retryCount", retryCount);
    m.put("errorMessage", errorMessage);
    return m;
  }
}
