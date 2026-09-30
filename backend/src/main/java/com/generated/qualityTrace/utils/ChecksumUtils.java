package com.generated.qualityTrace.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 分片校验值工具（SHA-256）。
 *
 * <p>已完成分片在续传时凭校验值跳过：只有重新计算的 checksum 与记录一致才认为内容未被篡改，
 * 否则判定分片损坏并重新导出。
 */
public final class ChecksumUtils {

  private ChecksumUtils() {}

  public static String sha256(String content) {
    if (content == null) {
      content = "";
    }
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(content.getBytes(StandardCharsets.UTF_8));
      StringBuilder sb = new StringBuilder();
      for (byte b : hash) {
        sb.append(String.format("%02x", b));
      }
      return sb.toString();
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 不可用", e);
    }
  }

  /** 校验内容与期望校验值是否一致。 */
  public static boolean verify(String content, String expected) {
    if (expected == null || expected.isEmpty()) {
      return false;
    }
    return expected.equalsIgnoreCase(sha256(content));
  }

  /** 串联多个分片校验值，生成包级总校验值。 */
  public static String combine(java.util.List<String> shardChecksums) {
    return sha256(String.join("|", shardChecksums));
  }
}
