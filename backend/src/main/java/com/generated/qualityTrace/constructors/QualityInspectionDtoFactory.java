package com.generated.qualityTrace.constructors;

import com.generated.qualityTrace.models.QualityInspection;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 质量检验响应对象构造器。
 */
public final class QualityInspectionDtoFactory {

  private QualityInspectionDtoFactory() {}

  public static Map<String, Object> create() {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("id", null);
    m.put("batchId", null);
    m.put("inspectorId", null);
    m.put("inspectionType", null);
    m.put("standardVersion", null);
    m.put("resultStatus", null);
    m.put("inspectedAt", null);
    return m;
  }

  public static Map<String, Object> response(QualityInspection ins) {
    Map<String, Object> m = create();
    m.put("id", ins.id);
    m.put("batchId", ins.batchId);
    m.put("inspectorId", ins.inspectorId);
    m.put("inspectionType", ins.inspectionType);
    m.put("standardVersion", ins.standardVersion);
    m.put("resultStatus", ins.resultStatus);
    m.put("inspectedAt", ins.inspectedAt);
    return m;
  }
}
