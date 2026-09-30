package com.generated.qualityTrace.types;

/** 现场补录不良请求。 */
public record DefectSubmitRequest(Long batchId, String defectType, Long defectQty, String severity,
                                  String rootCause, String dispositionStatus) {}
