package com.generated.qualityTrace.constructors;

import com.generated.qualityTrace.models.WorkOrder;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 工单响应对象构造器。页面/服务不得直接散写默认结构。
 */
public final class WorkOrderDtoFactory {

  private WorkOrderDtoFactory() {}

  public static Map<String, Object> create() {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("id", null);
    m.put("orderNo", null);
    m.put("productCode", null);
    m.put("productName", null);
    m.put("plannedQty", 0);
    m.put("lineCode", null);
    m.put("startAt", null);
    m.put("status", null);
    return m;
  }

  public static Map<String, Object> response(WorkOrder wo) {
    Map<String, Object> m = create();
    m.put("id", wo.id);
    m.put("orderNo", wo.orderNo);
    m.put("productCode", wo.productCode);
    m.put("productName", wo.productName);
    m.put("plannedQty", wo.plannedQty);
    m.put("lineCode", wo.lineCode);
    m.put("startAt", wo.startAt);
    m.put("status", wo.status);
    return m;
  }
}
