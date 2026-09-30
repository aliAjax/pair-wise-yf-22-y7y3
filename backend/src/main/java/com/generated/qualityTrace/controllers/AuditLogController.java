package com.generated.qualityTrace.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.generated.qualityTrace.models.AuditEvent;
import com.generated.qualityTrace.repositories.AuditLogRepository;

/** 操作/追溯事件日志查询（仅审计员，见 RbacMiddleware）。 */
@RestController
public class AuditLogController {

  private final AuditLogRepository auditLogRepository;

  public AuditLogController(AuditLogRepository auditLogRepository) {
    this.auditLogRepository = auditLogRepository;
  }

  @GetMapping("/api/audit-logs")
  public List<AuditEvent> list() {
    return auditLogRepository.findAll();
  }
}
