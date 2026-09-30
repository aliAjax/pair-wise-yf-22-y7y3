package com.generated.qualityTrace.constants;

/**
 * 离线核对时的缺件原因码。
 * 汇总清单会列出每一项缺件的原因码与引用标识，供审计员在无库环境下核对。
 */
public final class MissingPartReason {
  /** 工单下没有任何产品批次。 */
  public static final String MISSING_BATCH = "MISSING_BATCH";
  /** 批次没有任何质量检验记录。 */
  public static final String MISSING_INSPECTION = "MISSING_INSPECTION";
  /** 检验单没有任何检验项结果。 */
  public static final String MISSING_INSPECTION_ITEMS = "MISSING_INSPECTION_ITEMS";
  /** 不良记录缺少根本原因。 */
  public static final String MISSING_ROOT_CAUSE = "MISSING_ROOT_CAUSE";
  /** 批次缺少材料批号。 */
  public static final String MISSING_MATERIAL_LOT = "MISSING_MATERIAL_LOT";
  /** 检验缺少标准版本号。 */
  public static final String MISSING_STANDARD_VERSION = "MISSING_STANDARD_VERSION";

  private MissingPartReason() {}
}
