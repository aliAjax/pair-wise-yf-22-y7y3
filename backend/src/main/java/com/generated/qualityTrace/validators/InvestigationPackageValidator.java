package com.generated.qualityTrace.validators;

import com.generated.qualityTrace.constants.ErrorMessages;
import com.generated.qualityTrace.types.InvestigationPackagePayload;
import com.generated.qualityTrace.utils.Formatters;

/**
 * 调查包入参校验。校验失败抛出 IllegalArgumentException，由 controller 层包装为业务异常。
 */
public final class InvestigationPackageValidator {

  private InvestigationPackageValidator() {}

  /** 校验冻结请求：工单 id 必填且为正整数。 */
  public static void validateFreeze(InvestigationPackagePayload payload) {
    if (payload == null || payload.workOrderId == null) {
      throw new IllegalArgumentException(
          Formatters.format(ErrorMessages.INVALID_PARAMETER, "workOrderId 不能为空"));
    }
    if (payload.workOrderId <= 0) {
      throw new IllegalArgumentException(
          Formatters.format(ErrorMessages.INVALID_PARAMETER, "workOrderId 必须为正整数"));
    }
  }

  /** 校验导出/续传请求（包号由路径传入，请求体可空）。 */
  public static void validateExport(String packageNo) {
    if (packageNo == null || packageNo.trim().isEmpty()) {
      throw new IllegalArgumentException(
          Formatters.format(ErrorMessages.INVALID_PARAMETER, "packageNo 不能为空"));
    }
  }
}
