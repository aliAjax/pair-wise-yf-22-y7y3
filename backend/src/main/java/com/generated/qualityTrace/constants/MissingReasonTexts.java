package com.generated.qualityTrace.constants;

import java.util.Map;

/** 缺件原因中文文案，离线清单与 Formatters 共用。 */
public final class MissingReasonTexts {

  private static final Map<MissingReason, String> TEXTS = Map.of(
      MissingReason.WORK_ORDER_NOT_FOUND, "工单不存在，无法冻结其质量记录",
      MissingReason.NO_BATCH, "该工单在冻结时点前没有产品批次",
      MissingReason.NO_INSPECTION, "该批次在冻结时点前没有质量检验记录",
      MissingReason.NO_INSPECTION_ITEM, "检验单缺少检验项结果明细",
      MissingReason.NO_DEFECT, "冻结时点前没有不良记录（如确为零不良可现场确认）",
      MissingReason.SHARD_EXPORT_FAILED, "分片导出失败，请断点续传重试该分片");

  private MissingReasonTexts() {}

  public static String text(MissingReason reason) {
    return TEXTS.get(reason);
  }
}
