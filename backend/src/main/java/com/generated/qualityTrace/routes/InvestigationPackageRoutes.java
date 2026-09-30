package com.generated.qualityTrace.routes;

/**
 * 调查包（冻结批次）路由常量。
 */
public final class InvestigationPackageRoutes {

  public static final String PATH = "/api/investigation-packages";
  public static final String BY_WORK_ORDER = PATH + "/work-order/{workOrderId}";
  public static final String EXPORT = PATH + "/{packageNo}/export";
  public static final String MANIFEST = PATH + "/{packageNo}/manifest";

  private InvestigationPackageRoutes() {}
}
