package com.generated.qualityTrace.types;

/** 离线核对请求：可携带现场重算的清单摘要做二次比对（可选）。 */
public record ManifestVerifyRequest(String expectedManifestChecksum) {}
