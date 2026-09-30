package com.generated.qualityTrace.config;

import com.generated.qualityTrace.constants.RoleConstants;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

/**
 * 角色权限矩阵。
 *
 * <p>审计员/质量经理可发起并导出调查包、查看明文身份；
 * 质检员/产线主管可查看包，但检验员身份只显示可核验代号。
 */
@Configuration
public class RoleConfig {

  private static final Set<String> PACKAGE_MANAGERS = Set.of(
      RoleConstants.AUDITOR,
      RoleConstants.QUALITY_MANAGER
  );

  private static final Set<String> PACKAGE_VIEWERS = Set.of(
      RoleConstants.AUDITOR,
      RoleConstants.QUALITY_MANAGER,
      RoleConstants.LINE_SUPERVISOR,
      RoleConstants.INSPECTOR
  );

  /** 能否发起冻结 / 导出调查包。 */
  public boolean canManagePackage(String role) {
    return PACKAGE_MANAGERS.contains(role);
  }

  /** 能否查看调查包与汇总。 */
  public boolean canViewPackage(String role) {
    return PACKAGE_VIEWERS.contains(role);
  }

  /**
   * 查看包内内容时是否需要对检验员身份脱敏。
   * 质检员与产线主管只看到可核验代号；审计员与质量经理可见明文。
   */
  public boolean masksInspectorIdentity(String role) {
    return RoleConstants.INSPECTOR.equals(role) || RoleConstants.LINE_SUPERVISOR.equals(role);
  }
}
