package com.generated.qualityTrace.services;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.generated.qualityTrace.constants.MissingReason;
import com.generated.qualityTrace.constants.ShardType;
import com.generated.qualityTrace.models.InvestigationPackage;
import com.generated.qualityTrace.models.InvestigationPackageShard;
import com.generated.qualityTrace.utils.ChecksumUtils;
import com.generated.qualityTrace.utils.JsonUtils;

/**
 * 分片导出器：把冻结快照切成 5 个稳定 JSON 分片并计算 SHA-256。
 * 分片内容是冻结时刻数据，受限检验员看到的是同内容的打码视图（见 PackageViewService）。
 */
@Service
public class ShardExportService {

  /** 单片导出结果。 */
  public record ShardPayload(ShardType type, int recordCount, byte[] content,
                             List<MissingReason> missingReasons) {}

  /**
   * 导出单个分片。
   * @param failShardCode 演示断点续传：命中的分片本次直接失败（完成后不再命中）。
   */
  public ShardPayload export(InvestigationPackage pkg, ShardType type,
                             FrozenSnapshotService.Snapshot snapshot, String failShardCode) {
    if (type.getCode().equals(failShardCode)) {
      throw new ShardExportException(MissingReason.SHARD_EXPORT_FAILED,
          "simulated transient export failure on shard " + type.getCode());
    }

    Map<String, Object> root = JsonUtils.ordered(
        "packageNo", pkg.packageNo,
        "workOrderId", pkg.workOrderId,
        "frozenAt", pkg.frozenAt,
        "shardCode", type.getCode(),
        "shardLabel", type.getLabel());

    List<MissingReason> missing = new ArrayList<>();

    switch (type) {
      case WORK_ORDER -> {
        if (snapshot.workOrder() == null) {
          missing.add(MissingReason.WORK_ORDER_NOT_FOUND);
          root.put("records", List.of());
        } else {
          List<Map<String, Object>> records = new ArrayList<>();
          Map<String, Object> wo = JsonUtils.ordered(
              "id", snapshot.workOrder().id,
              "orderNo", snapshot.workOrder().orderNo,
              "productCode", snapshot.workOrder().productCode,
              "productName", snapshot.workOrder().productName,
              "plannedQty", snapshot.workOrder().plannedQty,
              "lineCode", snapshot.workOrder().lineCode,
              "startAt", snapshot.workOrder().startAt,
              "status", snapshot.workOrder().status,
              "createdAt", snapshot.workOrder().createdAt);
          records.add(wo);
          root.put("records", records);
        }
      }
      case BATCH -> {
        if (snapshot.workOrder() == null) {
          missing.add(MissingReason.WORK_ORDER_NOT_FOUND);
        }
        if (snapshot.batches().isEmpty()) {
          missing.add(MissingReason.NO_BATCH);
        }
        root.put("records", snapshot.batches().stream().map(b -> JsonUtils.ordered(
            "id", b.id,
            "batchNo", b.batchNo,
            "workOrderId", b.workOrderId,
            "quantity", b.quantity,
            "materialLotNo", b.materialLotNo,
            "producedAt", b.producedAt,
            "batchStatus", b.batchStatus,
            "createdAt", b.createdAt)).toList());
      }
      case INSPECTION -> {
        if (snapshot.workOrder() == null) {
          missing.add(MissingReason.WORK_ORDER_NOT_FOUND);
        } else if (snapshot.batches().isEmpty()) {
          missing.add(MissingReason.NO_BATCH);
        } else if (snapshot.inspections().isEmpty()) {
          missing.add(MissingReason.NO_INSPECTION);
        }
        root.put("records", snapshot.inspections().stream().map(i -> {
          Map<String, Object> map = JsonUtils.ordered(
              "id", i.id,
              "batchId", i.batchId,
              "inspectorId", i.inspectorId,
              "inspectionType", i.inspectionType,
              "standardVersion", i.standardVersion,
              "resultStatus", i.resultStatus,
              "inspectedAt", i.inspectedAt,
              "createdAt", i.createdAt,
              "itemResultIds", i.itemResults.stream().map(it -> it.id).toList());
          return map;
        }).toList());
      }
      case INSPECTION_ITEM -> {
        if (snapshot.workOrder() == null) {
          missing.add(MissingReason.WORK_ORDER_NOT_FOUND);
        } else if (snapshot.inspections().isEmpty()) {
          missing.add(MissingReason.NO_INSPECTION);
        } else if (snapshot.itemResultCount() == 0) {
          missing.add(MissingReason.NO_INSPECTION_ITEM);
        }
        List<Map<String, Object>> items = new ArrayList<>();
        snapshot.inspections().forEach(i -> i.itemResults.forEach(item -> items.add(JsonUtils.ordered(
            "id", item.id,
            "inspectionId", item.inspectionId,
            "itemCode", item.itemCode,
            "itemName", item.itemName,
            "measuredValue", item.measuredValue,
            "limitMin", item.limitMin,
            "limitMax", item.limitMax,
            "itemStatus", item.itemStatus,
            "createdAt", item.createdAt))));
        root.put("records", items);
      }
      case DEFECT -> {
        if (snapshot.workOrder() == null) {
          missing.add(MissingReason.WORK_ORDER_NOT_FOUND);
        } else if (snapshot.batches().isEmpty()) {
          missing.add(MissingReason.NO_BATCH);
        }
        if (snapshot.defects().isEmpty()) {
          missing.add(MissingReason.NO_DEFECT);
        }
        root.put("records", snapshot.defects().stream().map(d -> JsonUtils.ordered(
            "id", d.id,
            "batchId", d.batchId,
            "defectType", d.defectType,
            "defectQty", d.defectQty,
            "severity", d.severity,
            "rootCause", d.rootCause,
            "dispositionStatus", d.dispositionStatus,
            "createdAt", d.createdAt)).toList());
      }
    }

    byte[] content = JsonUtils.prettyBytes(root);
    int count = countRecords(type, snapshot);
    return new ShardPayload(type, count, content, missing);
  }

  /** 按分片类型统计记录数（缺件时为 0）。 */
  public int countRecords(ShardType type, FrozenSnapshotService.Snapshot snapshot) {
    return switch (type) {
      case WORK_ORDER -> snapshot.workOrder() == null ? 0 : 1;
      case BATCH -> snapshot.batches().size();
      case INSPECTION -> snapshot.inspections().size();
      case INSPECTION_ITEM -> snapshot.itemResultCount();
      case DEFECT -> snapshot.defects().size();
    };
  }

  public String checksum(byte[] content) {
    return ChecksumUtils.sha256Hex(content);
  }

  /** 单分片导出失败（可重试），携带缺件/失败原因。 */
  public static class ShardExportException extends RuntimeException {
    private final MissingReason reason;

    public ShardExportException(MissingReason reason, String message) {
      super(message);
      this.reason = reason;
    }

    public MissingReason getReason() {
      return reason;
    }
  }

  /** 供清单文本化复用。 */
  public static String asText(byte[] content) {
    return new String(content, StandardCharsets.UTF_8);
  }
}
