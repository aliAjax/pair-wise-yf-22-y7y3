package com.generated.qualityTrace.repositories;

import java.util.Optional;

import org.springframework.stereotype.Repository;

/** 分片内容以字节快照形式保存（导出时生成），断点续传时 DONE 分片直接复用。 */
@Repository
public class ShardBlobRepository {

  private final java.util.concurrent.ConcurrentHashMap<String, byte[]> blobs =
      new java.util.concurrent.ConcurrentHashMap<>();

  private static String key(String packageNo, String shardCode) {
    return packageNo + "/" + shardCode;
  }

  public void save(String packageNo, String shardCode, byte[] content) {
    blobs.put(key(packageNo, shardCode), content);
  }

  public Optional<byte[]> find(String packageNo, String shardCode) {
    return Optional.ofNullable(blobs.get(key(packageNo, shardCode)));
  }

  public void delete(String packageNo, String shardCode) {
    blobs.remove(key(packageNo, shardCode));
  }
}
