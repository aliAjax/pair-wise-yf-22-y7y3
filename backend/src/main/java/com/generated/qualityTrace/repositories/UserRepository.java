package com.generated.qualityTrace.repositories;

import com.generated.qualityTrace.models.User;
import com.generated.qualityTrace.constants.RoleConstants;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 用户数据访问层（内存种子用户）。
 */
@Repository
public class UserRepository {

  private final Map<Long, User> store = new ConcurrentHashMap<>();
  private final AtomicLong idGen = new AtomicLong(0);

  public UserRepository() {
    seed();
  }

  private void seed() {
    save(new User(null, "auditor", "auditor123", "审计员-周岚", RoleConstants.AUDITOR));
    save(new User(null, "inspector", "inspector123", "质检员-小吴", RoleConstants.INSPECTOR));
    save(new User(null, "manager", "manager123", "质量经理-郑凯", RoleConstants.QUALITY_MANAGER));
    save(new User(null, "supervisor", "supervisor123", "产线主管-冯磊", RoleConstants.LINE_SUPERVISOR));
  }

  public Optional<User> findByUsername(String username) {
    return store.values().stream()
        .filter(u -> username.equals(u.username))
        .findFirst();
  }

  public Optional<User> findById(Long id) {
    return Optional.ofNullable(store.get(id));
  }

  public User save(User u) {
    if (u.id == null) {
      u.id = idGen.incrementAndGet();
    }
    store.put(u.id, u);
    return u;
  }
}
