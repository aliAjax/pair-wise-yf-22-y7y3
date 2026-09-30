package com.generated.qualityTrace.utils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.Map;

/**
 * JSON 工具：统一 ObjectMapper 配置，供分片序列化/反序列化与离线汇总解析使用。
 */
public final class JsonUtils {

  private static final ObjectMapper MAPPER = new ObjectMapper()
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

  private JsonUtils() {}

  public static ObjectMapper mapper() {
    return MAPPER;
  }

  public static String toJson(Object value) {
    try {
      return MAPPER.writeValueAsString(value);
    } catch (Exception e) {
      throw new IllegalStateException("JSON 序列化失败", e);
    }
  }

  @SuppressWarnings("unchecked")
  public static Map<String, Object> toMap(String json) {
    if (json == null || json.isEmpty()) {
      return Collections.emptyMap();
    }
    try {
      return MAPPER.readValue(json, Map.class);
    } catch (Exception e) {
      throw new IllegalStateException("JSON 反序列化失败", e);
    }
  }
}
