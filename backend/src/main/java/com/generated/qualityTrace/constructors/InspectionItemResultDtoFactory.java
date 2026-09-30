package com.generated.qualityTrace.constructors;

import java.util.LinkedHashMap;
import java.util.Map;

import com.generated.qualityTrace.models.InspectionItemResult;

/** 检验项结果响应 DTO 构造器。 */
public final class InspectionItemResultDtoFactory {

  private InspectionItemResultDtoFactory() {}

  public static Map<String, Object> create(InspectionItemResult item) {
    Map<String, Object> map = new LinkedHashMap<>();
    map.put("id", item.id);
    map.put("inspectionId", item.inspectionId);
    map.put("itemCode", item.itemCode);
    map.put("itemName", item.itemName);
    map.put("measuredValue", item.measuredValue);
    map.put("limitMin", item.limitMin);
    map.put("limitMax", item.limitMax);
    map.put("itemStatus", item.itemStatus);
    map.put("createdAt", item.createdAt);
    return map;
  }
}
