package com.generated.qualityTrace.utils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * 轻量 JWT 签发/校验（HMAC-SHA256）。
 *
 * <p>不引入第三方依赖，使用 JDK 自带 {@link Mac} 实现标准三段式 JWT：
 * header.payload.signature。密钥来自配置 {@code JWT_SECRET}，过期时间由调用方指定。
 */
public final class JwtTokenService {

  private static final String HMAC_ALG = "HmacSHA256";
  private static final Base64.Encoder B64 = Base64.getUrlEncoder().withoutPadding();
  private static final Base64.Decoder B64_DEC = Base64.getUrlDecoder();

  private JwtTokenService() {}

  public static String issue(Long userId, String username, String role, long ttlMillis, String secret) {
    long exp = System.currentTimeMillis() + ttlMillis;
    String header = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
    String payload = "{\"sub\":\"" + userId + "\",\"username\":\"" + username + "\",\"role\":\"" + role
        + "\",\"exp\":" + exp + "}";
    String h = B64.encodeToString(header.getBytes(StandardCharsets.UTF_8));
    String p = B64.encodeToString(payload.getBytes(StandardCharsets.UTF_8));
    String sig = sign(h + "." + p, secret);
    return h + "." + p + "." + sig;
  }

  /** 校验令牌签名与过期时间，返回 payload；非法或过期返回 null。 */
  public static String[] verify(String token, String secret) {
    if (token == null) {
      return null;
    }
    String[] parts = token.split("\\.");
    if (parts.length != 3) {
      return null;
    }
    String expectedSig = sign(parts[0] + "." + parts[1], secret);
    if (!expectedSig.equals(parts[2])) {
      return null;
    }
    String payload = new String(B64_DEC.decode(parts[1]), StandardCharsets.UTF_8);
    Long exp = extractLong(payload, "exp");
    if (exp == null || exp < System.currentTimeMillis()) {
      return null;
    }
    Long sub = extractLong(payload, "sub");
    String username = extractString(payload, "username");
    String role = extractString(payload, "role");
    if (sub == null || username == null || role == null) {
      return null;
    }
    return new String[] { String.valueOf(sub), username, role };
  }

  private static String sign(String data, String secret) {
    try {
      Mac mac = Mac.getInstance(HMAC_ALG);
      mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALG));
      return B64.encodeToString(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
    } catch (Exception e) {
      throw new IllegalStateException("JWT 签名失败", e);
    }
  }

  private static Long extractLong(String json, String key) {
    String v = extractString(json, key);
    if (v == null) {
      return null;
    }
    try {
      return Long.parseLong(v);
    } catch (NumberFormatException e) {
      return null;
    }
  }

  /** 极简 JSON 字段提取（仅用于本服务自签 token 的受控 payload）。 */
  private static String extractString(String json, String key) {
    String pat = "\"" + key + "\":";
    int i = json.indexOf(pat);
    if (i < 0) {
      return null;
    }
    int start = i + pat.length();
    if (start >= json.length()) {
      return null;
    }
    char c = json.charAt(start);
    if (c == '"') {
      int end = json.indexOf('"', start + 1);
      return end > start ? json.substring(start + 1, end) : null;
    }
    int end = start;
    while (end < json.length() && json.charAt(end) != ',' && json.charAt(end) != '}') {
      end++;
    }
    return json.substring(start, end).trim();
  }
}
