package com.generated.qualityTrace.types;

/** 现场补录检验请求（首检/巡检/终检）。 */
public record InspectionSubmitRequest(Long batchId, String inspectionType, String standardVersion,
                                      String resultStatus) {}
