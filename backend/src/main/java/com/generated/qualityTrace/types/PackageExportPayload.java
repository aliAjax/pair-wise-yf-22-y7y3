package com.generated.qualityTrace.types;

/**
 * 导出/续传调查包请求体（可空，仅用于携带操作备注）。
 */
public class PackageExportPayload {
  /** 续传备注（可选）。 */
  public String operatorNote;

  public PackageExportPayload() {}

  public PackageExportPayload(String operatorNote) {
    this.operatorNote = operatorNote;
  }
}
