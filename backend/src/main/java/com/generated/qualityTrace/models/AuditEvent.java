package com.generated.qualityTrace.models;

/** 操作/追溯事件日志，审计现场补录、冻结、导出、下载、核对。 */
public class AuditEvent {
  public Long id;
  public String actor;
  public String action;
  public String targetType;
  public String targetId;
  public String detail;
  public String createdAt;

  public AuditEvent() {}

  public AuditEvent(Long id, String actor, String action, String targetType, String targetId,
                    String detail, String createdAt) {
    this.id = id;
    this.actor = actor;
    this.action = action;
    this.targetType = targetType;
    this.targetId = targetId;
    this.detail = detail;
    this.createdAt = createdAt;
  }
}
