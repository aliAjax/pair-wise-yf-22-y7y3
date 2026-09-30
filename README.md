# 制造业质量追溯 API 服务（quality-trace）

面向小型制造工厂的批次质量追溯后端服务，覆盖工单、批次、检验项、不良记录、**调查包冻结批次与断点续传导出**。

## 快速启动

```bash
cp .env.example .env && docker compose up -d
```

后端健康检查：<http://localhost:21114/health>

> 说明：当前版本使用内存仓储（`repositories` 包下的 In-Memory 实现），不依赖外部数据库即可启动；
> `database/init.sql` 用于文档化数据模型。

## 业务背景：调查包为什么要做成「可续传的冻结批次」

客户投诉后，审计员要带某个工单的质量记录去供应商现场核验。临时导表常和现场刚补录的检验、不良记录对不上。
为此把调查包做成**可续传的冻结批次**：

1. **开始时冻结版本**：开工单即固定当前版本的工单、产品批次、质量检验、检验项与不良记录，形成不可变快照；
   现场在冻结瞬间之后新提交的内容不计入本包（由 `snapshotCutoff` 界定），进入下一包。
2. **导出失败可续传**：导出按分片推进，已完成分片凭 `checksum` 跳过，失败分片记录原因与重试次数，
   重试时只补未完成部分，不重复导出已完成分片。
3. **重复申请同一批号**：同一工单重复申请只返回同一个批次号（批号由工单派生 + 工单唯一查询保证幂等）。
4. **角色权限不同**：审计员/质量经理可发起并导出、查看明文；质检员/产线主管可查看但检验员身份只显示**可核验代号**。
5. **包内离线核对**：汇总清单给出数量、检验结论摘要、不良严重度/处置摘要与缺件原因，
   并附各分片校验值与包级总校验值，审计员在无库环境下也能离线核对。

## 调查包接口

| 方法 | 路径 | 角色 | 说明 |
|---|---|---|---|
| POST | `/api/investigation-packages` | 审计员 / 质量经理 | 按工单冻结调查包（幂等，同一工单返回同一批号） |
| GET | `/api/investigation-packages/{packageNo}` | 全部角色 | 查看包与分片明细（受限角色检验员身份脱敏） |
| POST | `/api/investigation-packages/{packageNo}/export` | 审计员 / 质量经理 | 导出 / 续传（已完成分片跳过，失败分片重试） |
| GET | `/api/investigation-packages/{packageNo}/manifest` | 全部角色 | 离线汇总清单（数量、摘要、缺件原因、校验值） |
| GET | `/api/investigation-packages/work-order/{workOrderId}` | 全部角色 | 按工单取回冻结批次 |

### 典型流程

```bash
# 1. 登录（审计员）
curl -s -X POST http://localhost:21114/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"auditor","password":"auditor123"}'
# → {"token":"...","user":{"role":"AUDITOR",...}}

TOKEN=<上一步返回的 token>

# 2. 冻结工单 1 的调查包（重复调用返回同一批号）
curl -s -X POST http://localhost:21114/api/investigation-packages \
  -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"workOrderId":1}'
# → {"packageNo":"IP-000001","status":"FROZEN","shardCount":13,...}

# 3. 导出（演示环境默认在第 3 个分片模拟失败，返回 PARTIAL）
curl -s -X POST http://localhost:21114/api/investigation-packages/IP-000001/export \
  -H "Authorization: Bearer $TOKEN"
# → {"status":"PARTIAL","exportedShards":2,...}

# 4. 续传（已完成分片校验通过跳过，失败分片重试，最终 EXPORTED）
curl -s -X POST http://localhost:21114/api/investigation-packages/IP-000001/export \
  -H "Authorization: Bearer $TOKEN"
# → {"status":"EXPORTED","exportedShards":13,"manifestChecksum":"..."}

# 5. 离线汇总清单
curl -s http://localhost:21114/api/investigation-packages/IP-000001/manifest \
  -H "Authorization: Bearer $TOKEN"
```

### 角色与脱敏

| 用户名 | 密码 | 角色 | 冻结/导出 | 查看 | 检验员身份 |
|---|---|---|---|---|---|
| auditor | auditor123 | AUDITOR | ✅ | ✅ | 明文 |
| manager | manager123 | QUALITY_MANAGER | ✅ | ✅ | 明文 |
| supervisor | supervisor123 | LINE_SUPERVISOR | ❌ | ✅ | 可核验代号 |
| inspector | inspector123 | INSPECTOR | ❌ | ✅ | 可核验代号 |

「可核验代号」由真实标识经单向哈希派生（如 `INS-A1B2C3D4`），同一检验员在不同记录中代号一致、可跨记录核对，
但无法反推出真实身份。

### 离线汇总清单（manifest）包含

- **数量**：`batchCount` / `inspectionCount` / `inspectionItemCount` / `defectCount` / `defectQtyTotal`
- **摘要**：检验结论分布（PASS/FAIL/CONDITIONAL_PASS/RECHECK）、不良严重度分布（MINOR/MAJOR/CRITICAL）、处置分布
- **缺件原因**：`MISSING_BATCH` / `MISSING_INSPECTION` / `MISSING_INSPECTION_ITEMS` /
  `MISSING_ROOT_CAUSE` / `MISSING_MATERIAL_LOT` / `MISSING_STANDARD_VERSION`，每项带 `refId`
- **校验值**：每分片 `checksum` + 包级 `manifestChecksum`（各数据分片校验值串联后 SHA-256）

## 本地开发方式

- 后端：进入 `backend` 后按技术栈运行开发命令，接口统一挂在 `/api`。
- 无 JDK/Maven 环境时，直接用 `docker compose up -d` 一键启动。

## 技术栈

| 层 | 技术 |
|---|---|
| 后端 | Spring Boot 3 + Java 17 |
| 认证 | JWT（HMAC-SHA256，JDK 原生）+ RBAC |
| 持久化 | 内存仓储（In-Memory），`database/init.sql` 文档化模型 |
| 部署 | Docker Compose |

## 项目目录结构

```text
backend/src/main/java/com/generated/qualityTrace/
├── config/            # AppConfig、RoleConfig、PackageExportConfig、UserContext
├── constants/         # 枚举、错误码、错误消息、日志模板、状态文案
├── constructors/       # 请求/响应 DTO 构造器（含调查包/分片/汇总工厂）
├── controllers/        # 按实体分文件（含 InvestigationPackageController、AuthController）
├── repositories/       # 数据访问层（内存实现 + 种子数据）
├── services/           # 按实体分文件（含冻结、续传导出、离线汇总服务）
├── models/             # 实体（WorkOrder/ProductBatch/.../InvestigationPackage/PackageShard/User）
├── middlewares/        # 认证、RBAC、审计日志、限流、全局异常处理
├── routes/             # 路由常量
├── types/              # 请求体与响应载荷
├── validators/         # 入参校验
├── exceptions/         # BusinessException、ApiException
└── utils/              # 格式化、校验值、代号脱敏、批号生成、JWT、JSON
```

## 环境变量说明

- `COMPOSE_PROJECT_NAME`：Compose 项目名，默认 `quality-trace`
- `BACKEND_PORT`：后端端口，默认 `21114`
- `DB_PORT`：数据库宿主机端口
- `DB_USER/DB_PASSWORD/DB_NAME`：本地数据库凭据
- `JWT_SECRET`：JWT 签名密钥
- `PACKAGE_EXPORT_SIMULATE_FAILURE`：是否模拟分片失败（演示断点续传），默认 `true`
- `PACKAGE_EXPORT_FAIL_ON_SHARD`：模拟失败的分片序号，默认 `3`

## Docker 部署说明

- 根 Compose 文件不写 `version`，顶层 `name: quality-trace`。
- 容器名均使用 `${COMPOSE_PROJECT_NAME:-quality-trace}` 前缀。
- 数据库使用命名卷，避免绑定中文路径。
- 常见问题：端口占用时修改 `.env` 中端口后重启；需要重置数据时执行 `docker compose down -v`。

## 枚举/常量出现位置清单

- **WorkOrderStatus**（PLANNED/RUNNING/PAUSED/FINISHED/CANCELLED）：
  `constants/WorkOrderStatus`、`utils/Formatters#workOrderStatusText`、
  `models/WorkOrder#status`、种子数据、`README`。
- **InspectionResultStatus**（PASS/FAIL/CONDITIONAL_PASS/RECHECK）：
  `constants/InspectionResultStatus`、`utils/Formatters#inspectionResultText`、
  `models/QualityInspection#resultStatus`、`PackageManifestService` 检验结论摘要、种子数据。
- **DefectSeverity**（MINOR/MAJOR/CRITICAL）：
  `constants/DefectSeverity`、`utils/Formatters#defectSeverityText/#riskLevel`、
  `models/DefectRecord#severity`、`PackageManifestService` 严重度摘要、种子数据。
- **PackageStatus**（FROZEN/EXPORTING/PARTIAL/EXPORTED）：
  `constants/PackageStatus`、`models/InvestigationPackage#status`、
  `InvestigationPackageService`、`PackageExportService`。
- **ShardStatus**（PENDING/EXPORTED/FAILED）：
  `constants/ShardStatus`、`models/PackageShard#status`、`PackageExportService` 续传判断。
- **ShardType**（WORK_ORDER/BATCH/INSPECTION/ITEM/DEFECT/MANIFEST）：
  `constants/ShardType`、`models/PackageShard#shardType`、
  `InvestigationPackageService` 分片构建、`PackageManifestService` 汇总解析。
- **MissingPartReason**：`constants/MissingPartReason`、`PackageManifestService` 缺件清单。
- **RoleConstants**：`constants/RoleConstants`、`config/RoleConfig`、
  `middlewares/RbacMiddleware`、`models/User#role`、种子用户。

## 为什么该项目会牵一发动全身的说明

- 日志模板集中在 `constants/LogTemplates`，每个实体 ≥ 4 条，所有写操作都记录日志；字段变更需同步改模板与调用处。
- 错误码集中在 `constants/ErrorCodes`，消息模板集中在 `constants/ErrorMessages`；
  service 抛 `BusinessException`、controller 包装为 `ApiException`，最后经 `ErrorHandlerMiddleware` 输出，
  禁止只在一个全局位置吞掉全部异常。
- 每个核心实体都有独立 `constructors/*DtoFactory`，页面/服务不直接散写默认结构。
- `utils/Formatters` 混合日期、状态文案、风险等级与模板填充，被 controller/service/validator 共同依赖。
- 全局配置分散经过 `.env.example`、`docker-compose.yml`、`config/*` 读取，新增配置需同步多处。

## License

MIT
