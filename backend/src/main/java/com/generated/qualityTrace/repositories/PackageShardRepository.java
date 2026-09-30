package com.generated.qualityTrace.repositories;

import com.generated.qualityTrace.models.PackageShard;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 调查包分片数据访问层。分片按 shardSeq 顺序导出，MANIFEST 固定在最后。
 */
@Repository
public class PackageShardRepository {

  private final Map<Long, PackageShard> store = new ConcurrentHashMap<>();
  private final AtomicLong idGen = new AtomicLong(0);

  /** 按包号取分片，按 shardSeq 升序。 */
  public List<PackageShard> findByPackageNo(String packageNo) {
    List<PackageShard> out = new ArrayList<>();
    for (PackageShard s : store.values()) {
      if (packageNo.equals(s.packageNo)) {
        out.add(s);
      }
    }
    out.sort(Comparator.comparingInt(s -> s.shardSeq == null ? 0 : s.shardSeq));
    return out;
  }

  public PackageShard save(PackageShard shard) {
    if (shard.id == null) {
      shard.id = idGen.incrementAndGet();
    }
    store.put(shard.id, shard);
    return shard;
  }

  public List<PackageShard> saveAll(List<PackageShard> shards) {
    for (PackageShard s : shards) {
      save(s);
    }
    return shards;
  }

  public void deleteByPackageNo(String packageNo) {
    store.values().removeIf(s -> packageNo.equals(s.packageNo));
  }
}
