package com.generated.qualityTrace.controllers;

import com.generated.qualityTrace.config.RoleConfig;
import com.generated.qualityTrace.config.UserContext;
import com.generated.qualityTrace.constants.ErrorCodes;
import com.generated.qualityTrace.constants.ErrorMessages;
import com.generated.qualityTrace.constructors.InvestigationPackageDtoFactory;
import com.generated.qualityTrace.constructors.PackageShardDtoFactory;
import com.generated.qualityTrace.exceptions.ApiException;
import com.generated.qualityTrace.exceptions.BusinessException;
import com.generated.qualityTrace.models.InvestigationPackage;
import com.generated.qualityTrace.models.PackageShard;
import com.generated.qualityTrace.services.InvestigationPackageService;
import com.generated.qualityTrace.services.PackageExportService;
import com.generated.qualityTrace.services.PackageManifestService;
import com.generated.qualityTrace.types.InvestigationPackagePayload;
import com.generated.qualityTrace.types.PackageExportResult;
import com.generated.qualityTrace.types.PackageManifestPayload;
import com.generated.qualityTrace.utils.Formatters;
import com.generated.qualityTrace.validators.InvestigationPackageValidator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 调查包（冻结批次）接口。
 *
 * <ul>
 *   <li>POST /api/investigation-packages：按工单冻结调查包（幂等，同一工单返回同一批号）；</li>
 *   <li>GET  /api/investigation-packages/{packageNo}：查看包与分片（受限角色检验员身份脱敏）；</li>
 *   <li>POST /api/investigation-packages/{packageNo}/export：导出/续传（断点续传）；</li>
 *   <li>GET  /api/investigation-packages/{packageNo}/manifest：离线汇总清单；</li>
 *   <li>GET  /api/investigation-packages/work-order/{workOrderId}：按工单取回冻结批次。</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/investigation-packages")
public class InvestigationPackageController {

  private final InvestigationPackageService packageService;
  private final PackageExportService exportService;
  private final PackageManifestService manifestService;
  private final RoleConfig roleConfig;

  public InvestigationPackageController(InvestigationPackageService packageService,
                                        PackageExportService exportService,
                                        PackageManifestService manifestService,
                                        RoleConfig roleConfig) {
    this.packageService = packageService;
    this.exportService = exportService;
    this.manifestService = manifestService;
    this.roleConfig = roleConfig;
  }

  /** 冻结调查包（幂等：同一工单重复申请返回同一批号）。 */
  @PostMapping
  public ResponseEntity<Map<String, Object>> freeze(@RequestBody InvestigationPackagePayload payload) {
    try {
      InvestigationPackageValidator.validateFreeze(payload);
      InvestigationPackage pkg = packageService.freeze(payload.workOrderId, UserContext.getUsername());
      return ResponseEntity.ok(InvestigationPackageDtoFactory.response(pkg));
    } catch (BusinessException e) {
      throw wrap(e);
    } catch (IllegalArgumentException e) {
      throw new ApiException(ErrorCodes.INVALID_PARAMETER, e.getMessage(), HttpStatus.BAD_REQUEST);
    }
  }

  /** 查看调查包及其分片明细（质检员/产线主管看到的检验员身份为可核验代号）。 */
  @GetMapping("/{packageNo}")
  public ResponseEntity<Map<String, Object>> get(@PathVariable String packageNo) {
    try {
      InvestigationPackage pkg = mustGet(packageNo);
      boolean masked = roleConfig.masksInspectorIdentity(UserContext.getRole());
      List<PackageShard> shards = packageService.getShards(packageNo);
      List<Map<String, Object>> shardViews = shards.stream()
          .map(s -> PackageShardDtoFactory.response(s, masked))
          .collect(Collectors.toList());
      Map<String, Object> body = InvestigationPackageDtoFactory.response(pkg);
      body.put("shards", shardViews);
      return ResponseEntity.ok(body);
    } catch (BusinessException e) {
      throw wrap(e);
    }
  }

  /** 导出/续传：已完成分片凭校验值跳过，失败分片重试。 */
  @PostMapping("/{packageNo}/export")
  public ResponseEntity<PackageExportResult> export(@PathVariable String packageNo) {
    try {
      InvestigationPackageValidator.validateExport(packageNo);
      PackageExportResult result = exportService.export(packageNo, UserContext.getUsername());
      return ResponseEntity.ok(result);
    } catch (BusinessException e) {
      throw wrap(e);
    } catch (IllegalArgumentException e) {
      throw new ApiException(ErrorCodes.INVALID_PARAMETER, e.getMessage(), HttpStatus.BAD_REQUEST);
    }
  }

  /** 离线汇总清单：数量、摘要、缺件原因、分片校验值。 */
  @GetMapping("/{packageNo}/manifest")
  public ResponseEntity<PackageManifestPayload> manifest(@PathVariable String packageNo) {
    try {
      mustGet(packageNo);
      PackageManifestPayload manifest = manifestService.buildManifest(packageNo);
      return ResponseEntity.ok(manifest);
    } catch (BusinessException e) {
      throw wrap(e);
    }
  }

  /** 按工单取回冻结批次（重复申请同一工单取回同一批号）。 */
  @GetMapping("/work-order/{workOrderId}")
  public ResponseEntity<Map<String, Object>> byWorkOrder(@PathVariable Long workOrderId) {
    try {
      InvestigationPackage pkg = packageService.getByWorkOrderId(workOrderId)
          .orElseThrow(() -> new BusinessException(ErrorCodes.PACKAGE_NOT_FOUND,
              Formatters.format(ErrorMessages.PACKAGE_NOT_FOUND, "workOrderId=" + workOrderId)));
      return ResponseEntity.ok(InvestigationPackageDtoFactory.response(pkg));
    } catch (BusinessException e) {
      throw wrap(e);
    }
  }

  private InvestigationPackage mustGet(String packageNo) {
    return packageService.getByPackageNo(packageNo)
        .orElseThrow(() -> new BusinessException(ErrorCodes.PACKAGE_NOT_FOUND,
            Formatters.format(ErrorMessages.PACKAGE_NOT_FOUND, packageNo)));
  }

  /** service 层 BusinessException 到 controller 层 ApiException 的分别包装。 */
  private ApiException wrap(BusinessException e) {
    HttpStatus status = ErrorCodes.PACKAGE_NOT_FOUND.equals(e.getCode())
        || ErrorCodes.WORK_ORDER_NOT_FOUND.equals(e.getCode())
        ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
    return new ApiException(e.getCode(), e.getMessage(), status);
  }
}
