package com.generated.qualityTrace.constants;

/**
 * RBAC 角色。
 * AUDITOR 审计员：可读冻结调查包的完整质量记录。
 * QUALITY_MANAGER 质量经理 / LINE_SUPERVISOR 产线主管：质量记录写操作角色。
 * INSPECTOR 质检员：可在现场提交检验、不良记录。
 * RESTRICTED_INSPECTOR 受限检验员：调查包内仅显示可核验代号，敏感值打码。
 */
public enum UserRole {
  AUDITOR,
  QUALITY_MANAGER,
  LINE_SUPERVISOR,
  INSPECTOR,
  RESTRICTED_INSPECTOR;

  public static boolean contains(String raw) {
    if (raw == null) {
      return false;
    }
    for (UserRole role : values()) {
      if (role.name().equals(raw)) {
        return true;
      }
    }
    return false;
  }
}
