package com.generated.qualityTrace.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.generated.qualityTrace.middlewares.AuthContext;
import com.generated.qualityTrace.models.DefectRecord;
import com.generated.qualityTrace.services.DefectRecordService;
import com.generated.qualityTrace.services.OnsiteRecordService;
import com.generated.qualityTrace.types.DefectSubmitRequest;

/**
 * 不良记录接口。
 * GET  查询；
 * POST 现场补录不良，仅质检员/审计员；已冻结包不含该记录，进入下一包。
 */
@RestController
@RequestMapping("/api/defects")
public class DefectRecordController {

  private final DefectRecordService service;
  private final OnsiteRecordService onsiteRecordService;

  public DefectRecordController(DefectRecordService service,
                                OnsiteRecordService onsiteRecordService) {
    this.service = service;
    this.onsiteRecordService = onsiteRecordService;
  }

  @GetMapping
  public List<DefectRecord> list() {
    return service.list();
  }

  @PostMapping
  public Map<String, Object> submit(@RequestBody DefectSubmitRequest request) {
    Map<String, Object> body = new java.util.LinkedHashMap<>();
    body.put("batchId", request.batchId());
    body.put("defectType", request.defectType());
    body.put("defectQty", request.defectQty());
    body.put("severity", request.severity());
    body.put("rootCause", request.rootCause());
    body.put("dispositionStatus", request.dispositionStatus());
    return onsiteRecordService.submitDefect(body, AuthContext.actor());
  }
}
