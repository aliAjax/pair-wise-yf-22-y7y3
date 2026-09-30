-- 质量追溯数据库初始化脚本（PostgreSQL 15）
-- 说明：当前后端以内存存储运行（无外部 DB 依赖），本脚本用于文档化数据模型，
-- 字段与 backend 中的 model 一一对齐。

CREATE TABLE IF NOT EXISTS work_order (
  id INTEGER PRIMARY KEY,
  order_no TEXT,
  product_code TEXT,
  product_name TEXT,
  planned_qty TEXT,
  line_code TEXT,
  start_at TEXT,
  status TEXT,
  created_at TEXT
);

CREATE TABLE IF NOT EXISTS product_batch (
  id INTEGER PRIMARY KEY,
  batch_no TEXT,
  work_order_id TEXT,
  quantity TEXT,
  material_lot_no TEXT,
  produced_at TEXT,
  batch_status TEXT,
  created_at TEXT
);

CREATE TABLE IF NOT EXISTS quality_inspection (
  id INTEGER PRIMARY KEY,
  batch_id TEXT,
  inspector_id TEXT,
  inspection_type TEXT,
  standard_version TEXT,
  result_status TEXT,
  inspected_at TEXT,
  created_at TEXT
);

CREATE TABLE IF NOT EXISTS inspection_item_result (
  id INTEGER PRIMARY KEY,
  inspection_id TEXT,
  item_code TEXT,
  item_name TEXT,
  measured_value TEXT,
  limit_min TEXT,
  limit_max TEXT,
  item_status TEXT
);

CREATE TABLE IF NOT EXISTS defect_record (
  id INTEGER PRIMARY KEY,
  batch_id TEXT,
  defect_type TEXT,
  defect_qty TEXT,
  severity TEXT,
  root_cause TEXT,
  disposition_status TEXT,
  created_at TEXT
);

CREATE TABLE IF NOT EXISTS audit_log (
  id INTEGER PRIMARY KEY,
  actor TEXT,
  action TEXT,
  target_type TEXT,
  target_id TEXT,
  created_at TEXT
);

-- 系统用户（RBAC）
CREATE TABLE IF NOT EXISTS app_user (
  id INTEGER PRIMARY KEY,
  username TEXT UNIQUE NOT NULL,
  password TEXT NOT NULL,
  display_name TEXT,
  role TEXT NOT NULL
);

-- 调查包（冻结批次）：同一工单只存在一个冻结批次（work_order_id 唯一）
CREATE TABLE IF NOT EXISTS investigation_package (
  id INTEGER PRIMARY KEY,
  package_no TEXT UNIQUE NOT NULL,
  work_order_id TEXT NOT NULL,
  status TEXT NOT NULL,
  frozen_at TEXT,
  frozen_by TEXT,
  snapshot_cutoff TEXT,
  shard_count INTEGER,
  exported_shard_count INTEGER,
  manifest_checksum TEXT,
  created_at TEXT,
  updated_at TEXT
);

-- 调查包分片：导出按分片推进，已完成分片凭 checksum 跳过（断点续传）
CREATE TABLE IF NOT EXISTS package_shard (
  id INTEGER PRIMARY KEY,
  package_no TEXT NOT NULL,
  shard_type TEXT NOT NULL,
  shard_seq INTEGER NOT NULL,
  ref_id TEXT,
  status TEXT NOT NULL,
  content TEXT,
  checksum TEXT,
  retry_count INTEGER DEFAULT 0,
  error_message TEXT,
  exported_at TEXT
);

CREATE INDEX IF NOT EXISTS idx_package_shard_no ON package_shard(package_no, shard_seq);
