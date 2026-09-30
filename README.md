# 制造业质量追溯 API 服务

面向小型制造工厂的批次质量追溯后端服务，覆盖工单、批次、检验项、不良记录和追溯查询；
**新增可续传的冻结调查包**：审计员去供应商现场核验前，一键固定"这版"工单、产品批次、质量检验和不良记录，
导出失败可从已完成分片恢复，重复申请同一工单取回同一批号，现场刚补录的内容自动进入下一包。

## 快速启动

```bash
cp .env.example .env && docker compose up -d
```

健康检查：

```bash
curl http://localhost:21114/health
# {"status":"ok","service":"quality-trace"}
```

## 冻结调查包：现场核验工作流

### 解决的问题

临时导表常与现场刚补录的检验、不良记录对不上。调查包在创建瞬间写入 `frozenAt` 冻结时间点，
之后所有查询/分片/清单都只取 `created_at <= frozenAt` 的记录：

- 冻结后现场提交的新检验、新不良**不进当前包**（`nextPackageHint` 预告条数），调用 `/next` 开**下一包**；
- 同一工单重复申请**幂等取回同一批号**（`FZ-<工单号>-<序号>`，如 `FZ-WO-20260920-1001-001`）；
- 导出按 5 个固定分片进行，**DONE 分片跳过、仅重试 PENDING/FAILED**，可反复重试直到 COMPLETED；
- 每个分片带 `recordCount` 与 `SHA-256`，离线清单汇总数量、摘要与**缺件原因**，无网络也能核对。

### 角色与种子账号（密码仅演示）

| 账号 | 角色 | 调查包权限 | 现场补录 |
|---|---|---|---|
| `auditor` / `auditor123` | AUDITOR 审计员 | 冻结/导出/下一包/下载原始分片/清单/核对 | 可 |
| `restricted` / `restricted123` | RESTRICTED_INSPECTOR 受限检验员 | 只读，**仅显示可核验代号**，数值/名称/原因打码，禁止导出与原始分片下载 | 否 |
| `inspector` / `insp123` | INSPECTOR 质检员 | 不可访问调查包 | 可提交检验/不良 |
| `manager` / `mgr123` | QUALITY_MANAGER 质量经理 | 不可访问调查包 | 否 |

### API 一览（均挂 `/api`，除登录与 `/health` 外需 `Authorization: Bearer <jwt>`）

| 方法与路径 | 说明 |
|---|---|
| `POST /api/auth/login` | 登录换 JWT |
| `POST /api/investigation-packages` | 冻结开包 `{"workOrderId":1001}`；同工单重复调用取回同一批号 |
| `GET  /api/investigation-packages/{packageNo}` | 包详情（受限检验员自动打码，只留代号） |
| `POST /api/investigation-packages/{packageNo}/export` | 分片导出（断点续传）；可传 `{"failShardCode":"defect"}` 演示失败恢复 |
| `POST /api/investigation-packages/{packageNo}/next` | 开下一包，冻结现场补录内容，旧包不变 |
| `GET  /api/investigation-packages/{packageNo}/manifest` | 下载离线清单（数量/摘要/缺件原因，含 STABLE SECTION） |
| `GET  /api/investigation-packages/{packageNo}/shards/{shardCode}` | 下载分片原始 JSON（仅审计员） |
| `POST /api/investigation-packages/{packageNo}/verify` | 服务端重算分片摘要核对；可带现场摘要二次比对 |
| `POST /api/quality-inspections` | 质检员现场补录检验（首检/巡检/终检） |
| `POST /api/defects` | 质检员现场补录不良 |
| `GET  /api/work-orders` `/api/batches` `/api/quality-inspections` `/api/inspection-item-results` | 基础数据查询 |
| `GET  /api/audit-logs` | 操作/追溯事件日志（仅审计员） |

分片顺序固定为：`work_order` → `batch` → `inspection` → `inspection_item` → `defect`。

### CLI 示例

```bash
# 1. 登录
TOK=$(curl -s -X POST http://localhost:21114/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"auditor","password":"auditor123"}' | jq -r .token)

# 2. 冻结（重复执行返回同一个 packageNo）
PNO=$(curl -s -X POST http://localhost:21114/api/investigation-packages \
  -H "Authorization: Bearer $TOK" -H 'Content-Type: application/json' \
  -d '{"workOrderId":1001}' | jq -r .packageNo)

# 3. 导出（若中途失败，原样重试即可：已完成分片自动跳过）
curl -s -X POST http://localhost:21114/api/investigation-packages/$PNO/export \
  -H "Authorization: Bearer $TOK" -H 'Content-Type: application/json' -d '{}'

# 4. 离线清单（现场截取 STABLE SECTION 本地算 SHA-256 与头部摘要比对）
curl -s http://localhost:21114/api/investigation-packages/$PNO/manifest \
  -H "Authorization: Bearer $TOK" -o manifest.txt

# 5. 现场补录发生后开下一包（旧包摘要不变）
curl -s -X POST http://localhost:21114/api/investigation-packages/$PNO/next \
  -H "Authorization: Bearer $TOK"
```

### 缺件原因码（离线清单 `missingItems[].reasonCode`）

| 原因码 | 含义 |
|---|---|
| `WORK_ORDER_NOT_FOUND` | 工单不存在，无法冻结其质量记录 |
| `NO_BATCH` | 工单在冻结时点前没有产品批次（下游检验/不良分片按此做根因传播） |
| `NO_INSPECTION` | 批次在冻结时点前没有质量检验记录 |
| `NO_INSPECTION_ITEM` | 检验单缺少检验项结果明细 |
| `NO_DEFECT` | 冻结时点前没有不良记录（零不良需现场确认） |
| `SHARD_EXPORT_FAILED` | 分片导出失败，断点续传重试即可 |

种子数据演示：1001 记录完整；1002 有批次/检验但缺检验项（`NO_INSPECTION_ITEM`）且无不良（`NO_DEFECT`）；
1003 只有工单无批次（连锁 `NO_BATCH` → `NO_INSPECTION`）。

### 一键端到端核验

```bash
# 需要服务已在 21114（或用 BASE 指定）；脚本覆盖 18 组 55 项断言
bash backend/scripts/e2e-packages.sh
```

构建期集成测试：

```bash
cd backend && mvn test
```

## 本地开发方式

- 需要 JDK 17、Maven 3.9+。
- 后端：`cd backend && mvn spring-boot:run`，接口统一挂在 `/api`，默认端口 8080。
- 数据全部为本地内存种子（`TraceDataStore`），不接第三方 API；DDL 见 `database/init.sql`。
- 配置经 `.env.example`、`docker-compose.yml`、`backend/src/main/resources/application.properties` 多处读取，
  新增配置需同步（`JWT_SECRET`、`JWT_TTL_HOURS`、`PORT` 等）。

## 技术栈

| 层 | 技术 |
|---|---|
| 前端 | - |
| 后端 | Spring Boot 3 + Java 17 + MyBatis-Plus（数据访问为本地内存仓储） |
| 数据库 | PostgreSQL 15（Docker 编排；应用内置内存种子即可独立运行） |
| 认证 | 自研 HS256 JWT + 拦截器式 RBAC |
| 部署 | Docker Compose |

## 项目目录结构

```text
backend/src/main/java/com/generated/qualityTrace/
├── routes/               # 路由常量，按实体分文件
├── controllers/          # REST 控制器（含 InvestigationPackageController、AuthController、AuditLogController）
├── services/             # 业务编排（冻结快照 FrozenSnapshotService、分片 ShardExportService、
│                         #   调查包 InvestigationPackageService、视图 PackageViewService、
│                         #   现场补录 OnsiteRecordService、JwtService、AuthService）
├── models/               # 5 大实体 + InvestigationPackage/InvestigationPackageShard/SystemUser/AuditEvent
├── repositories/         # 数据访问 + TraceDataStore 种子 + 包/分片/分片字节仓储
├── middlewares/          # Auth / Rbac / RateLimit / AuditLog / ErrorHandler + AuthContext
├── constants/            # 枚举、错误码、错误消息、日志模板、缺件文案
├── constructors/         # 响应 DTO 构造器
├── validators/  utils/   # 校验、哈希、JSON、受限打码 Redactor、Formatters
├── types/                # 请求 record
├── exceptions/           # ApiException 及 401/403/400 子类
└── config/               # WebMvcConfig（中间件链）/ AppConfig
```

## 环境变量说明

- `COMPOSE_PROJECT_NAME`: Compose 项目名，默认 `quality-trace`
- `BACKEND_PORT`: 后端宿主机端口，默认 `21114`
- `DB_PORT`: 数据库宿主机端口
- `DB_USER/DB_PASSWORD/DB_NAME`: 本地数据库凭据
- `JWT_SECRET`: JWT 签名密钥，默认仅本地开发
- `JWT_TTL_HOURS`: JWT 有效期小时数，默认 12

## Docker 部署说明

- 根 Compose 文件不写 `version`，顶层 `name: quality-trace`。
- 容器名均使用 `${COMPOSE_PROJECT_NAME:-quality-trace}` 前缀。
- 后端端口映射：`${BACKEND_PORT:-21114}:8080`，容器内 8080。
- 数据库使用命名卷 `db_data`，不绑定中文路径；配置 healthcheck，后端 `depends_on: condition: service_healthy`。
- 常见问题：端口占用改 `.env` 后重启；重置数据执行 `docker compose down -v`。

## 枚举/常量出现位置清单

原有三组业务枚举（改值需同步常量、model、日志、错误、格式化、筛选、展示）：

- **WorkOrderStatus**（PLANNED/RUNNING/PAUSED/FINISHED/CANCELLED）：
  `constants/WorkOrderStatus.java`、`repositories/TraceDataStore.java`（种子）、`models/WorkOrder.java`、
  `constructors/WorkOrderDtoFactory.java`、`utils/Formatters.java`、数据库 `init.sql`。
- **InspectionResultStatus**（PASS/FAIL/CONDITIONAL_PASS/RECHECK）：
  `constants/InspectionResultStatus.java`、`services/OnsiteRecordService.java`（校验白名单）、
  `repositories/TraceDataStore.java`、`models/QualityInspection.java`、`types/InspectionSubmitRequest.java`、`init.sql`。
- **DefectSeverity**（MINOR/MAJOR/CRITICAL）：
  `constants/DefectSeverity.java`、`services/OnsiteRecordService.java`（校验白名单）、
  `repositories/TraceDataStore.java`、`models/DefectRecord.java`、`types/DefectSubmitRequest.java`、`init.sql`。

调查包新增枚举（同样贯穿 model/service/controller/清单/DDL）：

- **UserRole**（AUDITOR/QUALITY_MANAGER/LINE_SUPERVISOR/INSPECTOR/RESTRICTED_INSPECTOR）：
  `constants/UserRole.java` → `middlewares/RbacMiddleware.java`（授权矩阵）、
  `utils/Redactor.java`（受限打码）、`repositories/TraceDataStore.java`（种子账号）、
  `services/AuthService.java`/`JwtService.java`（登录与令牌）、README 角色表、`init.sql`。
- **PackageStatus**（OPEN/EXPORTING/COMPLETED/FAILED）：
  `constants/PackageStatus.java` → `models/InvestigationPackage.java`、
  `services/InvestigationPackageService.java`（状态流转）、`PackageViewService.java`（展示）、
  `utils/Formatters.java`（中文文案）、离线清单 `packageStatus`、`init.sql`。
- **ShardStatus**（PENDING/DONE/FAILED）：
  `constants/ShardStatus.java` → `models/InvestigationPackageShard.java`、
  `services/InvestigationPackageService.java`（续传跳过/重试判定）、
  `ShardExportService.java`、`PackageViewService.java`、清单每片 `status`、`init.sql`。
- **ShardType**（work_order/batch/inspection/inspection_item/defect）：
  `constants/ShardType.java`（代号+中文名）→ 分片创建顺序、`ShardExportService.java`、
  RBAC/控制器路径 `{shardCode}`、清单 `shardCode/shardLabel`、README 分片顺序。
- **MissingReason**（WORK_ORDER_NOT_FOUND/NO_BATCH/NO_INSPECTION/NO_INSPECTION_ITEM/NO_DEFECT/SHARD_EXPORT_FAILED）：
  `constants/MissingReason.java` + `constants/MissingReasonTexts.java`（文案）→
  `ShardExportService.java`（缺件判定与根因传播）、`services/InvestigationPackageService.java`（清单缺件段）、
  分片 `missingReasons`、README 原因码表、`init.sql` 的 `package_shard_missing`。

横切常量：错误码 `constants/ErrorCodes.java`、错误消息 `constants/ErrorMessages.java`、
日志模板 `constants/LogTemplates.java`（冻结/幂等取回/下一包预告/续传导出/分片失败/清单下载/核对/现场补录），
被 `exceptions/`、`middlewares/ErrorHandlerMiddleware.java`、各 service 分别包装引用。

## 为什么会牵一发动全身

实体字段、枚举、日志模板、错误消息、DTO 构造器、校验器和格式化器被刻意拆散到多个目录；
冻结调查包进一步把一个动作铺到 `routes → controller → service(编排/快照/分片/视图) → repository → model →
日志模板 → 错误码 → 缺件文案 → DDL → README`，改动一个分片定义或角色口径都需要同步多层，
保证冻结、续传、打码、离线核对的一致性可被审查。

## License

MIT
