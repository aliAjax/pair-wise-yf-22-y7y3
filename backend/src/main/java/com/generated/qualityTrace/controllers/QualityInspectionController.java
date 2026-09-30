package com.generated.qualityTrace.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.generated.qualityTrace.middlewares.AuthContext;
import com.generated.qualityTrace.models.QualityInspection;
import com.generated.qualityTrace.services.OnsiteRecordService;
import com.generated.qualityTrace.services.QualityInspectionService;
import com.generated.qualityTrace.types.InspectionSubmitRequest;

/**
 * 质量检验接口。
 * GET  查询（含冻结前/后全部）；
 * POST 现场补录检验，仅质检员/审计员；createdAt=now，已冻结包不含该记录，进入下一包。
 */
@RestController
@RequestMapping("/api/quality-inspections")
public class QualityInspectionController {

  private final QualityInspectionService service;
  private final OnsiteRecordService onsiteRecordService;

  public QualityInspectionController(QualityInspectionService service,
                                     OnsiteRecordService onsiteRecordService) {
    this.service = service;
    this.onsiteRecordService = onsiteRecordService;
  }

  @GetMapping
  public List<QualityInspection> list() {
    return service.list();
  }

  @PostMapping
  public Map<String, Object> submit(@RequestBody InspectionSubmitRequest request) {
    return onsiteRecordService.submitInspection(Map.of(
        "batchId", request.batchId() == null ? "" : request.batchId(),
        "inspectionType", request.inspectionType() == null ? "" : request.inspectionType(),
        "standardVersion", request.standardVersion() == null ? "" : request.standardVersion(),
        "resultStatus", request.resultStatus() == null ? "" : request.resultStatus()),
        AuthContext.actor());
  }
}
