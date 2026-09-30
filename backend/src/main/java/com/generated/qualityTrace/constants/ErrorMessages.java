package com.generated.qualityTrace.constants;

/**
 * 错误消息模板集中管理，配合 ErrorCodes 使用。
 * 占位符使用 {0}、{1} 形式，由调用处经 Formatters 填充。
 */
public final class ErrorMessages {
  public static final String AUTH_REQUIRED = "缺少身份凭证";
  public static final String AUTH_INVALID = "身份凭证无效或已过期";
  public static final String RBAC_DENIED = "当前角色无权执行该操作";

  public static final String WORK_ORDER_NOT_FOUND = "工单不存在: {0}";
  public static final String PACKAGE_NOT_FOUND = "调查包不存在: {0}";
  public static final String PACKAGE_ALREADY_FROZEN = "工单 {0} 已冻结调查包 {1}，重复申请返回同一批号";
  public static final String PACKAGE_NOT_EXPORTABLE = "调查包 {0} 当前状态 {1} 不允许导出";
  public static final String SHARD_CHECKSUM_MISMATCH = "分片 {0} 校验值不一致，内容可能被篡改";
  public static final String SHARD_EXPORT_FAILED = "分片 {0} 导出失败: {1}";
  public static final String MANIFEST_INCOMPLETE = "仍有 {0} 个分片未完成，无法生成汇总";
  public static final String INVALID_PARAMETER = "参数非法: {0}";

  private ErrorMessages() {}
}
