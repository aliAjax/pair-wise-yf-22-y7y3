package com.generated.qualityTrace.constructors;

import java.util.LinkedHashMap;
import java.util.Map;

import com.generated.qualityTrace.models.WorkOrder;

/** 生产工单响应 DTO 构造器：字段口径统一，页面/服务不散写结构。 */
public final class WorkOrderDtoFactory {

  private WorkOrderDtoFactory() {}

  public static Map<String, Object> create(WorkOrder wo) {
    Map<String, Object> map = new LinkedHashMap<>();
    map.put("id", wo.id);
    map.put("orderNo", wo.orderNo);
    map.put("productCode", wo.productCode);
    map.put("productName", wo.productName);
    map.put("plannedQty", wo.plannedQty);
    map.put("lineCode", wo.lineCode);
    map.put("startAt", wo.startAt);
    map.put("status", wo.status);
    map.put("createdAt", wo.createdAt);
    return map;
  }
}
