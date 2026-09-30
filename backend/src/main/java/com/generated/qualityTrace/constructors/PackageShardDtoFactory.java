package com.generated.qualityTrace.constructors;

import com.generated.qualityTrace.models.PackageShard;
import com.generated.qualityTrace.utils.IdentityMasker;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 分片响应对象构造器。受限角色查看时对检验员身份做可核验代号脱敏。
 */
public final class PackageShardDtoFactory {

  private PackageShardDtoFactory() {}

  /**
   * 构造分片响应。
   *
   * @param shard  分片实体
   * @param masked 是否脱敏（质检员/产线主管为 true）
   */
  public static Map<String, Object> response(PackageShard shard, boolean masked) {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("id", shard.id);
    m.put("packageNo", shard.packageNo);
    m.put("shardType", shard.shardType);
    m.put("shardSeq", shard.shardSeq);
    m.put("refId", shard.refId);
    m.put("status", shard.status);
    m.put("retryCount", shard.retryCount);
    m.put("errorMessage", shard.errorMessage);
    m.put("exportedAt", shard.exportedAt);
    m.put("checksum", shard.checksum);
    String content = shard.content;
    if (masked && content != null) {
      content = IdentityMasker.maskInspectorFields(content);
    }
    m.put("content", content);
    return m;
  }
}
