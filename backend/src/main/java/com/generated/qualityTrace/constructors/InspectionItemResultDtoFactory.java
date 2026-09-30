package com.generated.qualityTrace.constructors;

import com.generated.qualityTrace.models.InspectionItemResult;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 检验项结果响应对象构造器。
 */
public final class InspectionItemResultDtoFactory {

  private InspectionItemResultDtoFactory() {}

  public static Map<String, Object> create() {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("id", null);
    m.put("inspectionId", null);
    m.put("itemCode", null);
    m.put("itemName", null);
    m.put("measuredValue", null);
    m.put("limitMin", null);
    m.put("limitMax", null);
    m.put("itemStatus", null);
    return m;
  }

  public static Map<String, Object> response(InspectionItemResult item) {
    Map<String, Object> m = create();
    m.put("id", item.id);
    m.put("inspectionId", item.inspectionId);
    m.put("itemCode", item.itemCode);
    m.put("itemName", item.itemName);
    m.put("measuredValue", item.measuredValue);
    m.put("limitMin", item.limitMin);
    m.put("limitMax", item.limitMax);
    m.put("itemStatus", item.itemStatus);
    return m;
  }
}
