package com.generated.qualityTrace.utils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.generated.qualityTrace.exceptions.ValidationException;

/** 入参校验，错误码 VALIDATION_FAILED。 */
public final class Validators {

  private Validators() {}

  public static Long requireWorkOrderId(String raw) {
    if (raw == null || raw.isBlank()) {
      throw new ValidationException("workOrderId is required");
    }
    try {
      long id = Long.parseLong(raw.trim());
      if (id <= 0) {
        throw new ValidationException("workOrderId must be positive");
      }
      return id;
    } catch (NumberFormatException e) {
      throw new ValidationException("workOrderId must be a number: " + raw);
    }
  }

  public static String requireText(String value, String field) {
    if (value == null || value.isBlank()) {
      throw new ValidationException(field + " is required");
    }
    return value.trim();
  }

  public static Long requirePositive(Number value, String field) {
    if (value == null || value.longValue() <= 0) {
      throw new ValidationException(field + " must be positive");
    }
    return value.longValue();
  }

  public static String requireEnum(String value, List<String> allowed, String field) {
    String normalized = requireText(value, field);
    if (!allowed.contains(normalized)) {
      throw new ValidationException(field + " must be one of " + allowed + " but was " + normalized);
    }
    return normalized;
  }

  /** 把 Jackson 异常转成统一入参异常。 */
  public static Map<String, Object> readJsonObject(String body, ObjectMapper mapper) {
    try {
      @SuppressWarnings("unchecked")
      Map<String, Object> parsed = mapper.readValue(body, LinkedHashMap.class);
      return parsed;
    } catch (JsonProcessingException e) {
      throw new ValidationException("request body must be valid JSON: " + e.getOriginalMessage());
    }
  }
}
