package com.generated.qualityTrace.controllers;

import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.generated.qualityTrace.middlewares.AuthContext;
import com.generated.qualityTrace.models.InvestigationPackage;
import com.generated.qualityTrace.services.InvestigationPackageService;
import com.generated.qualityTrace.services.PackageViewService;
import com.generated.qualityTrace.types.ExportPackageRequest;
import com.generated.qualityTrace.types.FreezePackageRequest;
import com.generated.qualityTrace.types.ManifestVerifyRequest;
import com.generated.qualityTrace.utils.Validators;

/**
 * 冻结调查包接口（审计员现场核验用）。
 * POST   /api/investigation-packages                 冻结开包（幂等：同工单取回同批号）
 * GET    /api/investigation-packages/{packageNo}     包详情（受限身份自动打码）
 * POST   /api/investigation-packages/{packageNo}/export     分片导出（断点续传）
 * POST   /api/investigation-packages/{packageNo}/next       开下一包（冻结后新内容）
 * GET    /api/investigation-packages/{packageNo}/manifest   离线清单（数量/摘要/缺件原因）
 * GET    /api/investigation-packages/{packageNo}/shards/{shardCode}  分片原始内容
 * POST   /api/investigation-packages/{packageNo}/verify     离线核对重算
 */
@RestController
@RequestMapping("/api/investigation-packages")
public class InvestigationPackageController {

  private final InvestigationPackageService packageService;
  private final PackageViewService viewService;

  public InvestigationPackageController(InvestigationPackageService packageService,
                                        PackageViewService viewService) {
    this.packageService = packageService;
    this.viewService = viewService;
  }

  /** 开始调查：固定这版工单/批次/检验/不良；重复申请同一工单返回同一批号。 */
  @PostMapping
  public Map<String, Object> freeze(@RequestBody(required = false) FreezePackageRequest request) {
    Long workOrderId = request == null ? null
        : Validators.requirePositive(request.workOrderId(), "workOrderId");
    InvestigationPackage pkg = packageService.getOrFreeze(workOrderId, AuthContext.actor());
    return viewService.detail(pkg, AuthContext.role());
  }

  /** 包详情：受限检验员只显示可核验代号。 */
  @GetMapping("/{packageNo}")
  public Map<String, Object> detail(@PathVariable String packageNo) {
    InvestigationPackage pkg = packageService.requirePackage(packageNo);
    return viewService.detail(pkg, AuthContext.role());
  }

  /**
   * 导出（可续传）。已完成 DONE 分片直接跳过，只重试未完成/失败分片。
   * 可选 {"failShardCode":"defect"} 演示导出失败后续传。
   */
  @PostMapping("/{packageNo}/export")
  public Map<String, Object> export(@PathVariable String packageNo,
                                    @RequestBody(required = false) ExportPackageRequest request) {
    String failShardCode = request == null ? null : request.failShardCode();
    InvestigationPackageService.ExportResult result =
        packageService.export(packageNo, AuthContext.actor(), failShardCode);
    return viewService.exportResult(result, AuthContext.role());
  }

  /** 现场补录后开下一包：重新冻结，补录内容进入新批号，旧包不变。 */
  @PostMapping("/{packageNo}/next")
  public Map<String, Object> next(@PathVariable String packageNo) {
    InvestigationPackage next = packageService.freezeNext(packageNo, AuthContext.actor());
    return viewService.detail(next, AuthContext.role());
  }

  /** 离线清单下载：application/json 附件，含数量、摘要、缺件原因。 */
  @GetMapping("/{packageNo}/manifest")
  public ResponseEntity<byte[]> manifest(@PathVariable String packageNo) {
    String role = AuthContext.role();
    InvestigationPackageService.Manifest manifest =
        packageService.buildManifest(packageNo, AuthContext.actor(), role);
    packageService.logManifestDownload(packageNo, AuthContext.actor(), role);
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=\"manifest-" + packageNo + ".json.txt\"")
        .contentType(MediaType.parseMediaType("application/json; charset=UTF-8"))
        .body(packageService.manifestBytes(manifest));
  }

  /** 分片原始内容仅审计员可取；受限检验员只能在详情里看到打码后的可核验代号。 */
  @GetMapping("/{packageNo}/shards/{shardCode}")
  public ResponseEntity<byte[]> shard(@PathVariable String packageNo,
                                      @PathVariable String shardCode) {
    if (com.generated.qualityTrace.utils.Redactor.restricted(AuthContext.role())) {
      throw new com.generated.qualityTrace.exceptions.RbacDeniedException(
          AuthContext.role(), "GET /shards/" + shardCode + " (restricted: codes only)");
    }
    byte[] content = packageService.getShardContent(packageNo, shardCode);
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=\"" + shardCode + "-" + packageNo + ".json\"")
        .contentType(MediaType.parseMediaType("application/json; charset=UTF-8"))
        .body(content);
  }

  /** 离线核对：服务端重算分片摘要并与清单比对；可传现场摘要二次确认。 */
  @PostMapping("/{packageNo}/verify")
  public Map<String, Object> verify(@PathVariable String packageNo,
                                    @RequestBody(required = false) ManifestVerifyRequest request) {
    InvestigationPackageService.VerifyResult result =
        packageService.verify(packageNo, AuthContext.actor());
    Map<String, Object> body = new java.util.LinkedHashMap<>();
    body.put("packageNo", result.packageNo());
    body.put("manifestChecksum", result.manifestChecksum());
    body.put("allShardsMatch", result.allMatch());
    if (request != null && request.expectedManifestChecksum() != null
        && !request.expectedManifestChecksum().isBlank()) {
      body.put("expectedManifestChecksum", request.expectedManifestChecksum());
      body.put("manifestChecksumMatch",
          request.expectedManifestChecksum().trim().equals(result.manifestChecksum()));
    }
    body.put("shards", result.shards());
    return body;
  }
}
