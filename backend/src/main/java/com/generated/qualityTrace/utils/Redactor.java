package com.generated.qualityTrace.utils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.generated.qualityTrace.constants.UserRole;
import com.generated.qualityTrace.models.DefectRecord;
import com.generated.qualityTrace.models.InspectionItemResult;
import com.generated.qualityTrace.models.ProductBatch;
import com.generated.qualityTrace.models.QualityInspection;
import com.generated.qualityTrace.models.WorkOrder;

/**
 * 受限检验员打码器：只显示可核验代号，隐藏数值/名称/原因等敏感内容。
 * 审计员看全量。数量、状态、缺件原因不受影响，保证离线可核对。
 */
public final class Redactor {

  private static final String MASK = "****";

  private Redactor() {}

  public static boolean restricted(String role) {
    return UserRole.RESTRICTED_INSPECTOR.name().equals(role);
  }

  public static Map<String, Object> workOrder(WorkOrder wo, String role) {
    Map<String, Object> map = new LinkedHashMap<>();
    map.put("id", wo.id);
    map.put("orderNo", wo.orderNo);
    map.put("productCode", wo.productCode);
    map.put("status", wo.status);
    map.put("createdAt", wo.createdAt);
    if (!restricted(role)) {
      map.put("productName", wo.productName);
      map.put("plannedQty", wo.plannedQty);
      map.put("lineCode", wo.lineCode);
      map.put("startAt", wo.startAt);
    } else {
      map.put("productName", MASK);
      map.put("plannedQty", MASK);
      map.put("lineCode", MASK);
      map.put("startAt", MASK);
    }
    return map;
  }

  public static Map<String, Object> batch(ProductBatch b, String role) {
    Map<String, Object> map = new LinkedHashMap<>();
    map.put("id", b.id);
    map.put("batchNo", b.batchNo);
    map.put("workOrderId", b.workOrderId);
    map.put("batchStatus", b.batchStatus);
    map.put("createdAt", b.createdAt);
    if (!restricted(role)) {
      map.put("quantity", b.quantity);
      map.put("materialLotNo", b.materialLotNo);
      map.put("producedAt", b.producedAt);
    } else {
      map.put("quantity", MASK);
      map.put("materialLotNo", MASK);
      map.put("producedAt", MASK);
    }
    return map;
  }

  public static Map<String, Object> inspection(QualityInspection q, String role) {
    Map<String, Object> map = new LinkedHashMap<>();
    map.put("id", q.id);
    map.put("batchId", q.batchId);
    map.put("inspectionType", q.inspectionType);
    map.put("resultStatus", q.resultStatus);
    map.put("itemCount", q.itemResults.size());
    if (!restricted(role)) {
      map.put("inspectorId", q.inspectorId);
      map.put("standardVersion", q.standardVersion);
      map.put("inspectedAt", q.inspectedAt);
      map.put("createdAt", q.createdAt);
      List<Map<String, Object>> items = new ArrayList<>();
      for (InspectionItemResult item : q.itemResults) {
        items.add(item(item, role));
      }
      map.put("itemResults", items);
    } else {
      map.put("inspectorId", MASK);
      map.put("standardVersion", MASK);
      map.put("inspectedAt", MASK);
      map.put("createdAt", q.createdAt);
      // 受限身份只回传可核验代号（itemCode + 判定状态）。
      List<Map<String, Object>> items = new ArrayList<>();
      for (InspectionItemResult item : q.itemResults) {
        Map<String, Object> codeOnly = new LinkedHashMap<>();
        codeOnly.put("id", item.id);
        codeOnly.put("itemCode", item.itemCode);
        codeOnly.put("itemStatus", item.itemStatus);
        items.add(codeOnly);
      }
      map.put("itemResults", items);
    }
    return map;
  }

  public static Map<String, Object> item(InspectionItemResult item, String role) {
    Map<String, Object> map = new LinkedHashMap<>();
    map.put("id", item.id);
    map.put("inspectionId", item.inspectionId);
    map.put("itemCode", item.itemCode);
    map.put("itemStatus", item.itemStatus);
    if (!restricted(role)) {
      map.put("itemName", item.itemName);
      map.put("measuredValue", item.measuredValue);
      map.put("limitMin", item.limitMin);
      map.put("limitMax", item.limitMax);
    } else {
      map.put("itemName", MASK);
      map.put("measuredValue", MASK);
      map.put("limitMin", MASK);
      map.put("limitMax", MASK);
    }
    return map;
  }

  public static Map<String, Object> defect(DefectRecord d, String role) {
    Map<String, Object> map = new LinkedHashMap<>();
    map.put("id", d.id);
    map.put("batchId", d.batchId);
    map.put("defectType", d.defectType);
    map.put("severity", d.severity);
    map.put("dispositionStatus", d.dispositionStatus);
    if (!restricted(role)) {
      map.put("defectQty", d.defectQty);
      map.put("rootCause", d.rootCause);
      map.put("createdAt", d.createdAt);
    } else {
      map.put("defectQty", MASK);
      map.put("rootCause", MASK);
      map.put("createdAt", d.createdAt);
    }
    return map;
  }
}
