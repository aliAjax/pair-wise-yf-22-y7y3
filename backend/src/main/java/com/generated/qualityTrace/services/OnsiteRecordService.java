package com.generated.qualityTrace.services;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.generated.qualityTrace.constants.DefectSeverity;
import com.generated.qualityTrace.constants.InspectionResultStatus;
import com.generated.qualityTrace.constants.LogTemplates;
import com.generated.qualityTrace.models.DefectRecord;
import com.generated.qualityTrace.models.ProductBatch;
import com.generated.qualityTrace.models.QualityInspection;
import com.generated.qualityTrace.repositories.AuditLogRepository;
import com.generated.qualityTrace.repositories.DefectRecordRepository;
import com.generated.qualityTrace.repositories.ProductBatchRepository;
import com.generated.qualityTrace.repositories.QualityInspectionRepository;
import com.generated.qualityTrace.utils.Validators;

/**
 * 现场补录：质检员在供应商现场提交检验/不良。
 * createdAt=now，必然晚于既有冻结包的 frozenAt，因此新内容只进下一包。
 */
@Service
public class OnsiteRecordService {

  private static final Logger log = LoggerFactory.getLogger(OnsiteRecordService.class);

  private final ProductBatchRepository batchRepository;
  private final QualityInspectionRepository inspectionRepository;
  private final DefectRecordRepository defectRepository;
  private final AuditLogRepository auditLogRepository;

  public OnsiteRecordService(ProductBatchRepository batchRepository,
                             QualityInspectionRepository inspectionRepository,
                             DefectRecordRepository defectRepository,
                             AuditLogRepository auditLogRepository) {
    this.batchRepository = batchRepository;
    this.inspectionRepository = inspectionRepository;
    this.defectRepository = defectRepository;
    this.auditLogRepository = auditLogRepository;
  }

  public Map<String, Object> submitInspection(Map<String, Object> body, String actor) {
    Long batchId = Validators.requirePositive(asNumber(body.get("batchId")), "batchId");
    requireBatch(batchId);
    String type = Validators.requireText(asString(body.get("inspectionType")), "inspectionType");
    String standardVersion = Validators.requireText(asString(body.get("standardVersion")), "standardVersion");
    String resultStatus = Validators.requireEnum(asString(body.get("resultStatus")),
        List.of(InspectionResultStatus.PASS.name(), InspectionResultStatus.FAIL.name(),
            InspectionResultStatus.CONDITIONAL_PASS.name(), InspectionResultStatus.RECHECK.name()),
        "resultStatus");

    QualityInspection saved = inspectionRepository.submit(batchId, actor, type,
        standardVersion, resultStatus);
    log.info(LogTemplates.ONSITE_SUBMIT, actor, "QualityInspection", saved.id);
    auditLogRepository.append(actor, "ONSITE_INSPECTION_SUBMIT", "QualityInspection",
        String.valueOf(saved.id), "batchId=" + batchId + " resultStatus=" + resultStatus);
    return toView(saved);
  }

  public Map<String, Object> submitDefect(Map<String, Object> body, String actor) {
    Long batchId = Validators.requirePositive(asNumber(body.get("batchId")), "batchId");
    requireBatch(batchId);
    String defectType = Validators.requireText(asString(body.get("defectType")), "defectType");
    Long defectQty = Validators.requirePositive(asNumber(body.get("defectQty")), "defectQty");
    String severity = Validators.requireEnum(asString(body.get("severity")),
        List.of(DefectSeverity.MINOR.name(), DefectSeverity.MAJOR.name(),
            DefectSeverity.CRITICAL.name()),
        "severity");
    String rootCause = Validators.requireText(asString(body.get("rootCause")), "rootCause");
    String dispositionStatus =
        Validators.requireText(asString(body.get("dispositionStatus")), "dispositionStatus");

    DefectRecord saved = defectRepository.submit(batchId, defectType, defectQty, severity,
        rootCause, dispositionStatus);
    log.info(LogTemplates.ONSITE_SUBMIT, actor, "DefectRecord", saved.id);
    auditLogRepository.append(actor, "ONSITE_DEFECT_SUBMIT", "DefectRecord",
        String.valueOf(saved.id), "batchId=" + batchId + " severity=" + severity);
    return toView(saved);
  }

  private void requireBatch(Long batchId) {
    boolean exists = batchRepository.findAll().stream().anyMatch(b -> b.id.equals(batchId));
    if (!exists) {
      throw new com.generated.qualityTrace.exceptions.ValidationException("batchId not found: " + batchId);
    }
  }

  private static Number asNumber(Object value) {
    if (value == null) {
      return null;
    }
    if (value instanceof Number n) {
      return n;
    }
    return Long.parseLong(String.valueOf(value));
  }

  private static String asString(Object value) {
    return value == null ? null : String.valueOf(value);
  }

  private Map<String, Object> toView(QualityInspection q) {
    Map<String, Object> map = new LinkedHashMap<>();
    map.put("id", q.id);
    map.put("batchId", q.batchId);
    map.put("inspectorId", q.inspectorId);
    map.put("inspectionType", q.inspectionType);
    map.put("standardVersion", q.standardVersion);
    map.put("resultStatus", q.resultStatus);
    map.put("inspectedAt", q.inspectedAt);
    map.put("createdAt", q.createdAt);
    map.put("frozenPolicy", "createdAt 晚于已冻结包的 frozenAt，进入该工单下一包");
    return map;
  }

  private Map<String, Object> toView(DefectRecord d) {
    Map<String, Object> map = new LinkedHashMap<>();
    map.put("id", d.id);
    map.put("batchId", d.batchId);
    map.put("defectType", d.defectType);
    map.put("defectQty", d.defectQty);
    map.put("severity", d.severity);
    map.put("rootCause", d.rootCause);
    map.put("dispositionStatus", d.dispositionStatus);
    map.put("createdAt", d.createdAt);
    map.put("frozenPolicy", "createdAt 晚于已冻结包的 frozenAt，进入该工单下一包");
    return map;
  }
}
