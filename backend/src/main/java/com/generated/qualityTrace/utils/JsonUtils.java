package com.generated.qualityTrace.utils;

import java.util.LinkedHashMap;
import java.util.Map;

/** 统一 JSON 序列化，保证分片内容/清单字段顺序稳定，摘要可重复。 */
public final class JsonUtils {

  private static final com.fasterxml.jackson.databind.ObjectMapper MAPPER =
      new com.fasterxml.jackson.databind.ObjectMapper();

  private JsonUtils() {}

  public static String pretty(Object value) {
    try {
      return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(value);
    } catch (Exception e) {
      throw new IllegalStateException("json serialize failed", e);
    }
  }

  public static byte[] prettyBytes(Object value) {
    return pretty(value).getBytes(java.nio.charset.StandardCharsets.UTF_8);
  }

  public static Map<String, Object> ordered(Object... kv) {
    Map<String, Object> map = new LinkedHashMap<>();
    for (int i = 0; i + 1 < kv.length; i += 2) {
      map.put(String.valueOf(kv[i]), kv[i + 1]);
    }
    return map;
  }
}
