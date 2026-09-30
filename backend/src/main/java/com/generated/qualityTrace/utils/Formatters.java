package com.generated.qualityTrace.utils;

import com.generated.qualityTrace.constants.MissingReason;
import com.generated.qualityTrace.constants.PackageStatus;
import com.generated.qualityTrace.constants.ShardStatus;

/**
 * 混合格式化工具：审计定位、状态文案、缺件原因文案。
 * controller/清单/日志共用，改文案会同时影响多端展示。
 */
public final class Formatters {

  private Formatters() {}

  public static String audit(String type, long id) {
    return type + "#" + id;
  }

  public static String packageStatusText(PackageStatus status) {
    return switch (status) {
      case OPEN -> "待导出";
      case EXPORTING -> "导出中";
      case COMPLETED -> "已完成";
      case FAILED -> "部分失败可续传";
    };
  }

  public static String shardStatusText(ShardStatus status) {
    return switch (status) {
      case PENDING -> "未导出";
      case DONE -> "已完成";
      case FAILED -> "导出失败";
    };
  }

  public static String missingReasonText(MissingReason reason) {
    return com.generated.qualityTrace.constants.MissingReasonTexts.text(reason);
  }
}
