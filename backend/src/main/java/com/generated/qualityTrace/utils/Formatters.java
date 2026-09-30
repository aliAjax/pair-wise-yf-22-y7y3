package com.generated.qualityTrace.utils;

import com.generated.qualityTrace.constants.DefectSeverity;
import com.generated.qualityTrace.constants.InspectionResultStatus;
import com.generated.qualityTrace.constants.WorkOrderStatus;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * 格式化器：混合日期、状态文案、风险等级与日志模板填充。
 *
 * <p>故意把多种格式化逻辑集中在一个工具类，让 controller/service/validator 共同依赖，
 * 形成牵一发动全身的修改面（状态文案变更需同步多处）。
 */
public final class Formatters {

  private static final DateTimeFormatter ISO = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
      .withZone(ZoneOffset.UTC);

  private Formatters() {}

  public static String audit(String type, long id) {
    return type + "#" + id;
  }

  /** 填充日志/错误模板中的 {0}、{1} 占位符。 */
  public static String format(String template, Object... args) {
    String result = template;
    for (int i = 0; i < args.length; i++) {
      result = result.replace("{" + i + "}", String.valueOf(args[i]));
    }
    return result;
  }

  public static String formatInstant(Instant instant) {
    return instant == null ? "" : ISO.format(instant);
  }

  public static String nowIso() {
    return ISO.format(Instant.now());
  }

  /** 工单状态中文文案。 */
  public static String workOrderStatusText(String status) {
    if (status == null) {
      return "未知";
    }
    switch (status) {
      case WorkOrderStatus.PLANNED: return "已计划";
      case WorkOrderStatus.RUNNING: return "生产中";
      case WorkOrderStatus.PAUSED: return "已暂停";
      case WorkOrderStatus.FINISHED: return "已完工";
      case WorkOrderStatus.CANCELLED: return "已取消";
      default: return status;
    }
  }

  /** 检验结论中文文案。 */
  public static String inspectionResultText(String status) {
    if (status == null) {
      return "未知";
    }
    switch (status) {
      case InspectionResultStatus.PASS: return "合格";
      case InspectionResultStatus.FAIL: return "不合格";
      case InspectionResultStatus.CONDITIONAL_PASS: return "条件放行";
      case InspectionResultStatus.RECHECK: return "复检";
      default: return status;
    }
  }

  /** 不良严重度中文文案。 */
  public static String defectSeverityText(String severity) {
    if (severity == null) {
      return "未知";
    }
    switch (severity) {
      case DefectSeverity.MINOR: return "轻微";
      case DefectSeverity.MAJOR: return "主要";
      case DefectSeverity.CRITICAL: return "严重";
      default: return severity;
    }
  }

  /** 风险等级：由不良严重度映射，供包内摘要离线核对。 */
  public static String riskLevel(String severity) {
    if (severity == null) {
      return "UNKNOWN";
    }
    switch (severity) {
      case DefectSeverity.CRITICAL: return "HIGH";
      case DefectSeverity.MAJOR: return "MEDIUM";
      case DefectSeverity.MINOR: return "LOW";
      default: return "UNKNOWN";
    }
  }
}
