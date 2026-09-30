package com.generated.qualityTrace.constants;

/**
 * 系统角色（RBAC）。与 database/init.sql 中 user 表的 role 对齐。
 */
public final class RoleConstants {
  /** 质检员：受限身份，包内检验员只显示可核验代号。 */
  public static final String INSPECTOR = "INSPECTOR";
  /** 产线主管：可查看包，检验员身份同样脱敏。 */
  public static final String LINE_SUPERVISOR = "LINE_SUPERVISOR";
  /** 质量经理：可发起/导出调查包，可查看明文身份。 */
  public static final String QUALITY_MANAGER = "QUALITY_MANAGER";
  /** 审计员：可发起/导出调查包，可查看明文身份。 */
  public static final String AUDITOR = "AUDITOR";

  private RoleConstants() {}
}
