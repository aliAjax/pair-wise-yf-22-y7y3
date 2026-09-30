package com.generated.qualityTrace.utils;

/**
 * 冻结批次号生成器。
 *
 * <p>批次号由工单标识派生（{@code IP-} + 6 位工单 id），同一工单多次申请必然得到同一批号，
 * 从生成规则上保证幂等；不依赖随机序列，避免并发下重复申请产生不同批号。
 */
public final class PackageNoGenerator {

  private static final String PREFIX = "IP-";

  private PackageNoGenerator() {}

  public static String forWorkOrder(Long workOrderId) {
    if (workOrderId == null) {
      throw new IllegalArgumentException("workOrderId 不能为空");
    }
    return PREFIX + String.format("%06d", workOrderId);
  }
}
