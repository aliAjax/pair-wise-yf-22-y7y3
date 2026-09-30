package com.generated.qualityTrace.types;

/**
 * 续传导出请求。
 * failShardCode 仅用于演示断点续传：指定分片在本次尝试失败一次（不传则正常导出）。
 */
public record ExportPackageRequest(String failShardCode) {}
