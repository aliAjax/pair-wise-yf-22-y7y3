package com.generated.qualityTrace.repositories;

import com.generated.qualityTrace.models.InspectionItemResult;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 检验项结果数据访问层。
 */
@Repository
public class InspectionItemResultRepository {

  private final Map<Long, InspectionItemResult> store = new ConcurrentHashMap<>();
  private final AtomicLong idGen = new AtomicLong(0);

  public InspectionItemResultRepository() {
    seed();
  }

  private void seed() {
    save(new InspectionItemResult(null, 1L, "IT-01", "外径", "50.02", "49.9", "50.1", "PASS"));
    save(new InspectionItemResult(null, 1L, "IT-02", "长度", "100.5", "100.0", "101.0", "PASS"));
    save(new InspectionItemResult(null, 2L, "IT-01", "外径", "50.08", "49.9", "50.1", "CONDITIONAL_PASS"));
    save(new InspectionItemResult(null, 3L, "IT-03", "硬度", "58", "60", "65", "FAIL"));
  }

  public List<InspectionItemResult> findAll() {
    return new ArrayList<>(store.values());
  }

  public List<InspectionItemResult> findByInspectionId(Long inspectionId) {
    List<InspectionItemResult> out = new ArrayList<>();
    for (InspectionItemResult item : store.values()) {
      if (inspectionId.equals(item.inspectionId)) {
        out.add(item);
      }
    }
    return out;
  }

  public InspectionItemResult save(InspectionItemResult item) {
    if (item.id == null) {
      item.id = idGen.incrementAndGet();
    }
    store.put(item.id, item);
    return item;
  }
}
