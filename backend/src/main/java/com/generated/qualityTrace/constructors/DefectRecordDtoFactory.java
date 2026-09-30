package com.generated.qualityTrace.constructors;

import java.util.LinkedHashMap;
import java.util.Map;

import com.generated.qualityTrace.models.DefectRecord;

/** 不良记录响应 DTO 构造器。 */
public final class DefectRecordDtoFactory {

  private DefectRecordDtoFactory() {}

  public static Map<String, Object> create(DefectRecord d) {
    Map<String, Object> map = new LinkedHashMap<>();
    map.put("id", d.id);
    map.put("batchId", d.batchId);
    map.put("defectType", d.defectType);
    map.put("defectQty", d.defectQty);
    map.put("severity", d.severity);
    map.put("rootCause", d.rootCause);
    map.put("dispositionStatus", d.dispositionStatus);
    map.put("createdAt", d.createdAt);
    return map;
  }
}
