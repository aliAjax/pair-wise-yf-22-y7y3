package com.generated.qualityTrace.constructors;

import com.generated.qualityTrace.types.PackageManifestPayload;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 离线汇总清单构造器。提供零值默认清单与缺件项构造，服务不直接散写结构。
 */
public final class PackageManifestDtoFactory {

  private PackageManifestDtoFactory() {}

  /** 构造零值汇总清单（空包/无数据时的默认结构）。 */
  public static PackageManifestPayload empty() {
    PackageManifestPayload p = new PackageManifestPayload();
    p.counts = new LinkedHashMap<>();
    p.counts.put("batchCount", 0);
    p.counts.put("inspectionCount", 0);
    p.counts.put("inspectionItemCount", 0);
    p.counts.put("defectCount", 0);
    p.counts.put("defectQtyTotal", 0);

    p.inspectionSummary = new LinkedHashMap<>();
    p.inspectionSummary.put("PASS", 0);
    p.inspectionSummary.put("FAIL", 0);
    p.inspectionSummary.put("CONDITIONAL_PASS", 0);
    p.inspectionSummary.put("RECHECK", 0);

    p.defectSeveritySummary = new LinkedHashMap<>();
    p.defectSeveritySummary.put("MINOR", 0);
    p.defectSeveritySummary.put("MAJOR", 0);
    p.defectSeveritySummary.put("CRITICAL", 0);

    p.dispositionSummary = new LinkedHashMap<>();
    p.missingParts = new ArrayList<>();
    p.shards = new ArrayList<>();
    return p;
  }

  /** 构造一条缺件原因项。 */
  public static Map<String, Object> missingPart(String code, String reason, String refId) {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("code", code);
    m.put("reason", reason);
    m.put("refId", refId);
    return m;
  }
}
