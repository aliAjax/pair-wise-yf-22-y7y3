package com.generated.qualityTrace.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 身份脱敏工具。
 *
 * <p>受限检验员角色在包内看不到检验员真实身份，只能看到「可核验代号」：
 * 代号由真实标识经单向哈希派生（{@code INS-} + 哈希前 8 位），同一人在不同记录中代号一致，
 * 可跨记录核对，但无法反推出真实身份。
 */
public final class IdentityMasker {

  private static final String CODE_PREFIX = "INS-";
  private static final Pattern INSPECTOR_ID_PATTERN =
      Pattern.compile("\"inspectorId\"\\s*:\\s*\"([^\"]*)\"");

  private IdentityMasker() {}

  /** 将真实检验员标识转为可核验代号。 */
  public static String toVerifiableCode(String rawInspectorId) {
    if (rawInspectorId == null || rawInspectorId.isEmpty()) {
      return CODE_PREFIX + "UNKNOWN";
    }
    String hash = ChecksumUtils.sha256(rawInspectorId).substring(0, 8).toUpperCase();
    return CODE_PREFIX + hash;
  }

  /**
   * 对分片 JSON 内容中的 inspectorId 字段做脱敏。
   * 仅替换值，保留 JSON 结构与校验值可复算性（脱敏在导出响应侧进行，不改写已冻结内容）。
   */
  public static String maskInspectorFields(String shardContentJson) {
    if (shardContentJson == null) {
      return null;
    }
    Matcher matcher = INSPECTOR_ID_PATTERN.matcher(shardContentJson);
    StringBuilder sb = new StringBuilder();
    while (matcher.find()) {
      String raw = matcher.group(1);
      String code = toVerifiableCode(raw);
      matcher.appendReplacement(sb, Matcher.quoteReplacement("\"inspectorId\":\"" + code + "\""));
    }
    matcher.appendTail(sb);
    return sb.toString();
  }
}
