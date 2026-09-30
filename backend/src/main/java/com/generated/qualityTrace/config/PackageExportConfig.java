package com.generated.qualityTrace.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * 调查包导出相关配置。
 *
 * <p>全局配置分散经过 .env.example、docker-compose.yml 与本配置类读取；
 * 演示环境默认开启「模拟分片失败」，用于演示断点续传，生产可通过环境变量关闭。
 */
@Configuration
public class PackageExportConfig {

  /** 是否在首次导出时模拟一个分片失败（演示续传）。 */
  @Value("${PACKAGE_EXPORT_SIMULATE_FAILURE:true}")
  private boolean simulateFailure;

  /** 首次导出时模拟失败的分片序号（从 1 开始）。 */
  @Value("${PACKAGE_EXPORT_FAIL_ON_SHARD:3}")
  private int failOnShard;

  /** 单分片最大重试次数。 */
  @Value("${PACKAGE_EXPORT_MAX_RETRY:3}")
  private int maxRetry;

  /** 导出分片大小提示（每片最多承载的记录数，超出则继续拆分）。 */
  @Value("${PACKAGE_EXPORT_SHARD_SIZE:200}")
  private int shardSize;

  public boolean isSimulateFailure() {
    return simulateFailure;
  }

  public int getFailOnShard() {
    return failOnShard;
  }

  public int getMaxRetry() {
    return maxRetry;
  }

  public int getShardSize() {
    return shardSize;
  }
}
