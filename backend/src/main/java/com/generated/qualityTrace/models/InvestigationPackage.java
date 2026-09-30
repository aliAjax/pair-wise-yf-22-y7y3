package com.generated.qualityTrace.models;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.generated.qualityTrace.constants.PackageStatus;

/**
 * 冻结调查包（InvestigationPackage）。
 * frozenAt 为冻结时间点：只收录 createdAt &lt;= frozenAt 的工单/批次/检验/不良，
 * 冻结之后现场补录的内容进入下一包。
 */
public class InvestigationPackage {
  /** 批号前缀。 */
  public static final String PACKAGE_NO_PREFIX = "FZ";

  public Long id;
  /** 对外批号，如 FZ-WO1001-001；同一工单重复申请取回同一批号。 */
  public String packageNo;
  public Long workOrderId;
  public String frozenAt;
  public String createdBy;
  public String createdAt;
  public PackageStatus status;
  /** 同一工单上的包序号，从 1 开始。 */
  public int sequence;
  public List<InvestigationPackageShard> shards = new ArrayList<>();

  public InvestigationPackage() {}

  public static String buildPackageNo(String orderNo, int sequence) {
    return PACKAGE_NO_PREFIX + "-" + orderNo + "-" + String.format("%03d", sequence);
  }

  public Instant frozenInstant() {
    return Instant.parse(frozenAt);
  }
}
