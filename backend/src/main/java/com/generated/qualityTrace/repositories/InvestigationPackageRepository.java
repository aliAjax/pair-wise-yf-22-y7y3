package com.generated.qualityTrace.repositories;

import com.generated.qualityTrace.models.InvestigationPackage;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 调查包（冻结批次）数据访问层。
 *
 * <p>以 workOrderId 唯一约束保证幂等：同一工单只存在一个冻结批次，重复申请返回同一批号。
 */
@Repository
public class InvestigationPackageRepository {

  private final Map<Long, InvestigationPackage> store = new ConcurrentHashMap<>();
  private final AtomicLong idGen = new AtomicLong(0);

  public List<InvestigationPackage> findAll() {
    return new ArrayList<>(store.values());
  }

  public Optional<InvestigationPackage> findByPackageNo(String packageNo) {
    return store.values().stream()
        .filter(p -> packageNo.equals(p.packageNo))
        .findFirst();
  }

  /** 按工单查找已冻结批次（幂等查询）。 */
  public Optional<InvestigationPackage> findByWorkOrderId(Long workOrderId) {
    return store.values().stream()
        .filter(p -> workOrderId.equals(p.workOrderId))
        .findFirst();
  }

  public InvestigationPackage save(InvestigationPackage pkg) {
    if (pkg.id == null) {
      // 幂等：同一工单已有批次则直接返回，不再插入新批号
      Optional<InvestigationPackage> existing = findByWorkOrderId(pkg.workOrderId);
      if (existing.isPresent()) {
        return existing.get();
      }
      pkg.id = idGen.incrementAndGet();
    }
    store.put(pkg.id, pkg);
    return pkg;
  }

  public void deleteByPackageNo(String packageNo) {
    store.values().removeIf(p -> packageNo.equals(p.packageNo));
  }
}
