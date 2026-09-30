package com.generated.qualityTrace.services;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.generated.qualityTrace.constants.ErrorMessages;
import com.generated.qualityTrace.exceptions.ApiException;
import com.generated.qualityTrace.constants.ErrorCodes;

/**
 * 轻量 JWT 服务（HS256），无第三方依赖。
 * payload: sub 用户名、role 角色、exp 过期秒。
 */
@Service
public class JwtService {

  private final byte[] secret;
  private final Duration ttl;

  public JwtService(@Value("${jwt.secret:local-dev-secret}") String secret,
                    @Value("${jwt.ttl-hours:12}") long ttlHours) {
    this.secret = secret.getBytes(StandardCharsets.UTF_8);
    this.ttl = Duration.ofHours(ttlHours);
  }

  public String issue(String subject, String role) {
    long exp = Instant.now().plus(ttl).getEpochSecond();
    String header = base64Url("{\"alg\":\"HS256\",\"typ\":\"JWT\"}");
    String payload = base64Url("{\"sub\":\"" + escape(subject) + "\",\"role\":\"" + escape(role)
        + "\",\"exp\":" + exp + "}");
    String signingInput = header + "." + payload;
    return signingInput + "." + sign(signingInput);
  }

  /** 校验并返回 payload；失败抛 401。 */
  public Claims verify(String token) {
    String[] parts = token.split("\\.");
    if (parts.length != 3) {
      throw new ApiException(ErrorCodes.AUTH_INVALID, ErrorMessages.AUTH_INVALID, 401);
    }
    String signingInput = parts[0] + "." + parts[1];
    if (!constantTimeEquals(sign(signingInput), parts[2])) {
      throw new ApiException(ErrorCodes.AUTH_INVALID, ErrorMessages.AUTH_INVALID, 401);
    }
    String json = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
    String sub = extract(json, "sub");
    String role = extract(json, "role");
    long exp = Long.parseLong(extract(json, "exp"));
    if (Instant.now().getEpochSecond() > exp) {
      throw new ApiException(ErrorCodes.AUTH_INVALID, ErrorMessages.AUTH_INVALID, 401);
    }
    return new Claims(sub, role);
  }

  public record Claims(String subject, String role) {}

  private String sign(String input) {
    try {
      Mac mac = Mac.getInstance("HmacSHA256");
      mac.init(new SecretKeySpec(secret, "HmacSHA256"));
      return Base64.getUrlEncoder().withoutPadding()
          .encodeToString(mac.doFinal(input.getBytes(StandardCharsets.UTF_8)));
    } catch (Exception e) {
      throw new IllegalStateException("HMAC-SHA256 unavailable", e);
    }
  }

  private static String extract(String json, String key) {
    String quoted = "\"" + key + "\":\"";
    int start = json.indexOf(quoted);
    if (start >= 0) {
      start += quoted.length();
      int end = json.indexOf('"', start);
      return json.substring(start, end);
    }
    String numeric = "\"" + key + "\":";
    start = json.indexOf(numeric);
    if (start < 0) {
      throw new ApiException(ErrorCodes.AUTH_INVALID, ErrorMessages.AUTH_INVALID, 401);
    }
    start += numeric.length();
    int end = start;
    while (end < json.length() && Character.isDigit(json.charAt(end))) {
      end++;
    }
    return json.substring(start, end);
  }

  private static String escape(String value) {
    return value.replace("\\", "\\\\").replace("\"", "\\\"");
  }

  private static String base64Url(String value) {
    return Base64.getUrlEncoder().withoutPadding()
        .encodeToString(value.getBytes(StandardCharsets.UTF_8));
  }

  private static boolean constantTimeEquals(String a, String b) {
    if (a.length() != b.length()) {
      return false;
    }
    int diff = 0;
    for (int i = 0; i < a.length(); i++) {
      diff |= a.charAt(i) ^ b.charAt(i);
    }
    return diff == 0;
  }
}
