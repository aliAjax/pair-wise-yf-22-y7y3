package com.generated.qualityTrace.repositories;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.generated.qualityTrace.models.AuditEvent;

/** 操作日志 + 追溯事件日志仓储。 */
@Repository
public class AuditLogRepository {

  private final TraceDataStore store;

  public AuditLogRepository(TraceDataStore store) {
    this.store = store;
  }

  public void append(String actor, String action, String targetType, String targetId, String detail) {
    store.appendAudit(actor, action, targetType, targetId, detail);
  }

  public List<AuditEvent> findAll() {
    return store.listAuditEvents();
  }
}
