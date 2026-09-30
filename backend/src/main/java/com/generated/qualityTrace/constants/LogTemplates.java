package com.generated.qualityTrace.constants;

/**
 * 日志模板集中管理。每个实体至少 4 条模板，所有写操作都要记录日志；
 * 字段变更时同步修改模板与调用处。
 */
public final class LogTemplates {
  // 通用动作
  public static final String CREATE = "create";
  public static final String UPDATE = "update";
  public static final String STATUS = "status";
  public static final String EXPORT = "export";

  // 工单
  public static final String WORK_ORDER_CREATE = "创建工单 orderNo={0} productCode={1}";
  public static final String WORK_ORDER_START = "工单开工 orderNo={0}";
  public static final String WORK_ORDER_PAUSE = "工单暂停 orderNo={0}";
  public static final String WORK_ORDER_FINISH = "工单完工 orderNo={0}";

  // 批次
  public static final String BATCH_CREATE = "创建批次 batchNo={0} workOrderId={1}";
  public static final String BATCH_STATUS = "批次状态更新 batchNo={0} status={1}";
  public static final String BATCH_TRACE = "批次追溯 batchNo={0}";

  // 检验
  public static final String INSPECTION_SUBMIT = "提交检验 inspectionId={0} batchId={1}";
  public static final String INSPECTION_RESULT = "检验结论 inspectionId={0} result={1}";
  public static final String INSPECTION_ITEM_RECORD = "录入检验项 inspectionId={0} itemCode={1}";

  // 不良
  public static final String DEFECT_REGISTER = "登记不良 defectId={0} batchId={1} severity={2}";
  public static final String DEFECT_DISPOSITION = "不良处置 defectId={0} disposition={1}";
  public static final String DEFECT_CLOSE = "不良关闭 defectId={0}";

  // 调查包（冻结批次）
  public static final String PACKAGE_FREEZE = "冻结调查包 packageNo={0} workOrderId={1} cutoff={2} shards={3}";
  public static final String PACKAGE_IDEMPOTENT_HIT = "重复申请命中已冻结批次 packageNo={0} workOrderId={1}";
  public static final String PACKAGE_EXPORT_START = "开始导出 packageNo={0}";
  public static final String PACKAGE_EXPORT_RESUME = "续传导出 packageNo={0} 已完成分片={1} 待处理={2}";
  public static final String PACKAGE_SHARD_EXPORTED = "分片导出完成 packageNo={0} seq={1} type={2} checksum={3}";
  public static final String PACKAGE_SHARD_SKIP = "分片校验通过跳过 packageNo={0} seq={1}";
  public static final String PACKAGE_SHARD_FAILED = "分片导出失败 packageNo={0} seq={1} error={2}";
  public static final String PACKAGE_MANIFEST_BUILT = "汇总生成 packageNo={0} batches={1} inspections={2} defects={3} missingParts={4}";
  public static final String PACKAGE_EXPORT_DONE = "导出完成 packageNo={0} exportedShards={1}/{2} checksum={3}";

  // 认证
  public static final String AUTH_LOGIN = "用户登录 username={0} role={1}";
  public static final String AUTH_DENIED = "拒绝访问 username={0} role={1} path={2}";

  private LogTemplates() {}
}
