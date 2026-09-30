package com.generated.qualityTrace.routes;

/** 冻结调查包相关路由常量，route/controller/interceptor 共用。 */
public final class InvestigationPackageRoutes {
  public static final String PATH = "/api/investigation-packages";
  public static final String EXPORT = "/{packageNo}/export";
  public static final String NEXT = "/{packageNo}/next";
  public static final String MANIFEST = "/{packageNo}/manifest";
  public static final String VERIFY = "/{packageNo}/verify";
  public static final String SHARD_CONTENT = "/{packageNo}/shards/{shardCode}";

  private InvestigationPackageRoutes() {}
}
