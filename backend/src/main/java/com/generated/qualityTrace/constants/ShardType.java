package com.generated.qualityTrace.constants;

/** 调查包固定 5 个分片，顺序即现场核验顺序：工单、批次、检验、检验项、不良。 */
public enum ShardType {
  WORK_ORDER("work_order", "生产工单"),
  BATCH("batch", "产品批次"),
  INSPECTION("inspection", "质量检验"),
  INSPECTION_ITEM("inspection_item", "检验项结果"),
  DEFECT("defect", "不良记录");

  private final String code;
  private final String label;

  ShardType(String code, String label) {
    this.code = code;
    this.label = label;
  }

  public String getCode() {
    return code;
  }

  public String getLabel() {
    return label;
  }
}
