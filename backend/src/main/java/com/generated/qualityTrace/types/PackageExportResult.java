package com.generated.qualityTrace.types;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 导出/续传结果。
 */
public class PackageExportResult {
  public String packageNo;
  /** 导出后包状态：PARTIAL / EXPORTED。 */
  public String status;
  public int totalShards;
  public int exportedShards;
  public String manifestChecksum;
  /** 是否为已完成包的幂等返回（未重复导出）。 */
  public boolean idempotent;
  /** 每个分片的处理结果：type / seq / status / checksum / retryCount / errorMessage。 */
  public List<Map<String, Object>> shardResults = new ArrayList<>();

  public PackageExportResult() {}
}
