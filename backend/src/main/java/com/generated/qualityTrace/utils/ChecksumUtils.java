package com.generated.qualityTrace.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/** 分片/清单摘要工具，现场离线核对数量与摘要。 */
public final class ChecksumUtils {

  private static final char[] HEX = "0123456789abcdef".toCharArray();

  private ChecksumUtils() {}

  public static String sha256Hex(byte[] content) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(content);
      char[] out = new char[hash.length * 2];
      for (int i = 0; i < hash.length; i++) {
        int v = hash[i] & 0xFF;
        out[i * 2] = HEX[v >>> 4];
        out[i * 2 + 1] = HEX[v & 0x0F];
      }
      return new String(out);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 unavailable", e);
    }
  }

  public static String sha256Text(String text) {
    return sha256Hex(text.getBytes(StandardCharsets.UTF_8));
  }
}
