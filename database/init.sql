CREATE TABLE IF NOT EXISTS work_order (
  id BIGINT PRIMARY KEY,
  order_no TEXT,
  product_code TEXT,
  product_name TEXT,
  planned_qty BIGINT,
  line_code TEXT,
  start_at TEXT,
  status TEXT,
  created_at TEXT
);

CREATE TABLE IF NOT EXISTS product_batch (
  id BIGINT PRIMARY KEY,
  batch_no TEXT,
  work_order_id BIGINT,
  quantity BIGINT,
  material_lot_no TEXT,
  produced_at TEXT,
  batch_status TEXT,
  created_at TEXT
);

CREATE TABLE IF NOT EXISTS quality_inspection (
  id BIGINT PRIMARY KEY,
  batch_id BIGINT,
  inspector_id TEXT,
  inspection_type TEXT,
  standard_version TEXT,
  result_status TEXT,
  inspected_at TEXT,
  created_at TEXT
);

CREATE TABLE IF NOT EXISTS inspection_item_result (
  id BIGINT PRIMARY KEY,
  inspection_id BIGINT,
  item_code TEXT,
  item_name TEXT,
  measured_value TEXT,
  limit_min TEXT,
  limit_max TEXT,
  item_status TEXT,
  created_at TEXT
);

CREATE TABLE IF NOT EXISTS defect_record (
  id BIGINT PRIMARY KEY,
  batch_id BIGINT,
  defect_type TEXT,
  defect_qty BIGINT,
  severity TEXT,
  root_cause TEXT,
  disposition_status TEXT,
  created_at TEXT
);

CREATE TABLE IF NOT EXISTS audit_log (
  id BIGINT PRIMARY KEY,
  actor TEXT,
  action TEXT,
  target_type TEXT,
  target_id TEXT,
  detail TEXT,
  created_at TEXT
);

-- RBAC 种子用户（密码仅演示）：审计员 / 质检员 / 受限检验员 / 质量经理
CREATE TABLE IF NOT EXISTS system_user (
  id BIGINT PRIMARY KEY,
  username TEXT UNIQUE,
  password TEXT,
  display_name TEXT,
  role TEXT
);

-- 冻结调查包：同一工单最新一包即幂等取回的批号
CREATE TABLE IF NOT EXISTS investigation_package (
  id BIGINT PRIMARY KEY,
  package_no TEXT UNIQUE,
  work_order_id BIGINT,
  sequence_no INTEGER,
  frozen_at TEXT,
  created_by TEXT,
  created_at TEXT,
  status TEXT
);
CREATE INDEX IF NOT EXISTS idx_package_work_order
  ON investigation_package (work_order_id, sequence_no);

-- 分片：DONE + checksum 落库后，导出失败续传只重试 PENDING/FAILED
CREATE TABLE IF NOT EXISTS investigation_package_shard (
  id BIGINT PRIMARY KEY,
  package_id BIGINT,
  shard_index INTEGER,
  shard_code TEXT,
  shard_label TEXT,
  status TEXT,
  record_count INTEGER,
  checksum_sha256 TEXT,
  completed_at TEXT,
  last_error TEXT
);

-- 缺件原因（离线清单逐项展示）
CREATE TABLE IF NOT EXISTS package_shard_missing (
  id BIGINT PRIMARY KEY,
  shard_id BIGINT,
  reason_code TEXT
);
