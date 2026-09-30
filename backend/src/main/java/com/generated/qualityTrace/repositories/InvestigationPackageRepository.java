package com.generated.qualityTrace.repositories;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Repository;

import com.generated.qualityTrace.models.InvestigationPackage;

/**
 * 冻结调查包仓储：包 + 分片的持久状态。
 * 已完成分片（DONE + checksum）在这里落状态，导出失败后只重试未完成部分。
 */
@Repository
public class InvestigationPackageRepository {

  private final ConcurrentHashMap<String, InvestigationPackage> packages = new ConcurrentHashMap<>();
  private final AtomicLong idSeq = new AtomicLong(9000);

  public synchronized InvestigationPackage save(InvestigationPackage pkg) {
    if (pkg.id == null) {
      pkg.id = idSeq.incrementAndGet();
    }
    packages.put(pkg.packageNo, pkg);
    return pkg;
  }

  public Optional<InvestigationPackage> findByPackageNo(String packageNo) {
    return Optional.ofNullable(packages.get(packageNo));
  }

  /** 幂等键：同一工单取回当前（最新）那一包。 */
  public Optional<InvestigationPackage> findLatestByWorkOrder(Long workOrderId) {
    return packages.values().stream()
        .filter(p -> p.workOrderId.equals(workOrderId))
        .max((a, b) -> Integer.compare(a.sequence, b.sequence));
  }

  public List<InvestigationPackage> findAll() {
    List<InvestigationPackage> list = new ArrayList<>(packages.values());
    list.sort((a, b) -> Long.compare(b.id, a.id));
    return list;
  }
}
