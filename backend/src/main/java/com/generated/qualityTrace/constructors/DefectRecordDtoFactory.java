package com.generated.qualityTrace.constructors;

import com.generated.qualityTrace.models.DefectRecord;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 不良记录响应对象构造器。
 */
public final class DefectRecordDtoFactory {

  private DefectRecordDtoFactory() {}

  public static Map<String, Object> create() {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("id", null);
    m.put("batchId", null);
    m.put("defectType", null);
    m.put("defectQty", 0);
    m.put("severity", null);
    m.put("rootCause", null);
    m.put("dispositionStatus", null);
    return m;
  }

  public static Map<String, Object> response(DefectRecord d) {
    Map<String, Object> m = create();
    m.put("id", d.id);
    m.put("batchId", d.batchId);
    m.put("defectType", d.defectType);
    m.put("defectQty", d.defectQty);
    m.put("severity", d.severity);
    m.put("rootCause", d.rootCause);
    m.put("dispositionStatus", d.dispositionStatus);
    return m;
  }
}
