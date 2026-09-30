package com.generated.qualityTrace.constructors;

import java.util.LinkedHashMap;
import java.util.Map;

import com.generated.qualityTrace.models.QualityInspection;

/** 质量检验响应 DTO 构造器（含检验项 ID 列表）。 */
public final class QualityInspectionDtoFactory {

  private QualityInspectionDtoFactory() {}

  public static Map<String, Object> create(QualityInspection q) {
    Map<String, Object> map = new LinkedHashMap<>();
    map.put("id", q.id);
    map.put("batchId", q.batchId);
    map.put("inspectorId", q.inspectorId);
    map.put("inspectionType", q.inspectionType);
    map.put("standardVersion", q.standardVersion);
    map.put("resultStatus", q.resultStatus);
    map.put("inspectedAt", q.inspectedAt);
    map.put("createdAt", q.createdAt);
    map.put("itemResultIds", q.itemResults.stream().map(i -> i.id).toList());
    return map;
  }
}
