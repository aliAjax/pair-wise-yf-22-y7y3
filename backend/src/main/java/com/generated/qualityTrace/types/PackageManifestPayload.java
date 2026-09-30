package com.generated.qualityTrace.types;

import java.util.List;
import java.util.Map;

/**
 * 包内离线汇总清单（manifest）。
 *
 * <p>审计员在供应商现场无库环境下，仅凭此清单即可核对数量、摘要与缺件原因，
 * 并可凭各分片校验值与总校验值离线验证包内容未被篡改。
 */
public class PackageManifestPayload {
  public String packageNo;
  public Long workOrderId;
  public String orderNo;
  public String frozenAt;
  public String snapshotCutoff;
  public String generatedAt;

  /** 数量核对：batches / inspections / inspectionItems / defects / defectQtyTotal。 */
  public Map<String, Integer> counts;
  /** 检验结论分布：PASS / FAIL / CONDITIONAL_PASS / RECHECK。 */
  public Map<String, Integer> inspectionSummary;
  /** 不良严重度分布：MINOR / MAJOR / CRITICAL。 */
  public Map<String, Integer> defectSeveritySummary;
  /** 不良处置状态分布。 */
  public Map<String, Integer> dispositionSummary;
  /** 缺件原因清单：code / reason / refId。 */
  public List<Map<String, Object>> missingParts;
  /** 分片清单：type / seq / refId / status / checksum。 */
  public List<Map<String, Object>> shards;
  /** 包级总校验值（各分片校验值串联后 SHA-256）。 */
  public String manifestChecksum;

  public PackageManifestPayload() {}
}
