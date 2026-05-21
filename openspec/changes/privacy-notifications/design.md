## Context

内容社区模块（`jeecg-module-content`）已具备以下基础能力：
- `ContentUserNotificationSetting` 实体 + 完整的通知开关/渠道配置/免打扰服务（`IContentUserNotificationSettingService`），含 `canSendNotice()` 判断逻辑和安全通知白名单豁免。
- `ContentUserPrivacySetting` 实体，包含生日/性别/地区/职业/主页/动态可见性、在线状态、搜索引擎索引等字段。
- `ContentUserSettingsController` 提供 `/content/user/settings/privacy/update`、`/content/user/settings/notification`、`/content/user/settings/notification/update` 三个接口。
- `IContentUserVisibilityPolicyService` 提供可见性检查能力。

EPIC-06 在此基础上扩展：增强的免打扰（多时段、工作日/周末差异化、临时关闭）、在线状态可见性分级、第三方授权管理、账户安全入口统一页、通知退订合规引擎。

## Goals / Non-Goals

**Goals:**
- 扩展免打扰能力为多时段配置（工作日/周末/节假日），支持临时关闭按钮。
- 扩展在线状态可见性为三档：公开/隐藏/仅互关可见。
- 新增第三方 OAuth 授权管理：查看授权列表、撤销授权、Token 立即失效。
- 新增账户安全设置统一入口：前端集中页面导航到设备管理、密码修改、两步验证、登录提醒。
- 新增通知退订合规校验层，确保通知发送前强制执行用户偏好检查。
- 新增 Redis 缓存隐私设置和通知偏好，变更后 5 分钟内失效。

**Non-Goals:**
- 不实现数据导出功能（GDPR 工具属后续迭代）。
- 不实现两步验证具体逻辑（仅统一入口导航，具体流程在 EPIC-01 中）。
- 不实现第三方 OAuth 服务端（仅管理已授权应用的授权状态）。
- 不改变现有通知设置的核心数据模型和 API 路径。

## Decisions

### 1. 免打扰增强：扩展 JSON 字段而非新增表
**Decision**: 将 `dndRuleJson` 从单时段扩展为多时段列表 `dndRules[]`，每个规则包含 `enabled`、`startTime`、`endTime`、`dayType`（workday/weekend/holiday）、`summaryMode`。
**Rationale**: 保持现有表结构不变，只扩展 JSON 内部格式，避免 migration。
**Alternatives considered**: 新建独立免打扰规则表——增加维护复杂度，当前数据量不需要关系化。

### 2. 在线状态可见性：扩展 `onlineStatusVisible` 为字符串枚举
**Decision**: 将 `ContentUserPrivacySetting.onlineStatusVisible` 从 `Boolean` 改为 `String` 类型的 `onlineStatusVisibility`，取值 `PUBLIC`/`HIDDEN`/`MUTUAL_ONLY`。
**Rationale**: 支持三级可见性，比新增独立列更简洁。需要 Flyway migration 兼容旧 Boolean 值。
**Alternatives considered**: 新增独立字段保留旧列——遗留成本高，旧数据可通过映射迁移。

### 3. 第三方授权管理：独立表 `content_user_third_party_auth`
**Decision**: 新建授权记录表，存储应用名称、授权时间、授权范围、access token hash、刷新 token hash、状态。
**Rationale**: OAuth 授权是结构化关系数据，不适合存 JSON。需要独立查询、过滤和撤销操作。
**Alternatives considered**: 存 JSON——查询和审计困难，且 OAuth 标准需要结构化存储。

### 4. 通知退订合规：嵌入现有 `canSendNotice()` 作为最终关卡
**Decision**: 不新建独立合规服务，而是在 `canSendNotice()` 方法中增加退订强制校验，并增加审计日志表 `content_notification_audit_log` 记录每次发送决策。
**Rationale**: 现有方法已是通知发送的判断入口，扩展比新建更自然，减少调用方改动。
**Alternatives considered**: 新建独立合规服务——增加调用链路复杂度，且现有 `canSendNotice()` 已是合理的单一判断点。

### 5. 隐私设置缓存：Redis 5 分钟 TTL
**Decision**: 使用 `content:privacy:{userId}` 和 `content:notification:{userId}` 两个 Redis key，TTL 300 秒。设置变更后主动删除缓存键（而非等待过期）。
**Rationale**: 主动删除保证即时生效，TTL 兜底防止缓存键丢失。与 PRD 中"5 分钟内失效"的要求一致。

### 6. 账户安全入口：纯前端页面 + 后端导航接口
**Decision**: 后端只提供 `/content/user/settings/security` 返回安全功能列表和各功能的状态（是否已启用），前端据此渲染统一页面。具体安全操作跳转到已有接口。
**Rationale**: 避免后端重复实现已在 EPIC-01 中的逻辑，本史诗仅做聚合入口。

## Risks / Trade-offs

| Risk | Impact | Mitigation |
|------|--------|------------|
| `onlineStatusVisible` 类型变更导致旧数据迁移失败 | 高 | Flyway migration 分两步：先新增列，再迁移数据，最后删除旧列 |
| Redis 缓存主动删除失败导致隐私设置延迟生效 | 中 | 设置 TTL 兜底 300 秒；写入时同步删除 |
| 免打扰 JSON 扩展导致向后不兼容 | 中 | 服务层增加版本兼容逻辑：检测旧格式自动升级为单元素列表 |
| 通知审计日志表写入量大 | 中 | 初期直接写表，后续可按月分表或异步写入 |
| 第三方授权撤销后 Token 未完全失效 | 高 | 撤销时同时删除 Redis 中的 token 缓存 + 数据库标记失效 |

## Migration Plan

### 部署步骤
1. 执行 Flyway migration: `V3.9.1_53__content_privacy_notifications.sql`
   - 新增 `online_status_visibility` 列（VARCHAR）
   - 新增 `content_user_third_party_auth` 表
   - 新增 `content_notification_audit_log` 表
   - 数据迁移：将 `online_status_visible` (Boolean) 映射到 `online_status_visibility`
2. 部署后端代码更新
3. 部署前端新增页面
4. 清理旧 `online_status_visible` 列（可选，下个版本执行）

### 回滚方案
- 新增表和列不影响旧代码运行（旧代码不读取新列），可仅回滚代码保留表结构
- 如需完全回滚：执行 `DROP TABLE content_user_third_party_auth`、`DROP TABLE content_notification_audit_log`、`ALTER TABLE content_user_privacy_setting DROP COLUMN online_status_visibility`
- 旧 `online_status_visible` 列在回滚时仍保留原数据（迁移只写新列，不删旧列）

## Open Questions

1. 免打扰是否支持自定义时区？——当前假设使用系统时区，国际化场景需确认。
2. 第三方授权管理是否需要通知第三方应用（webhook 回调）？——PRD 标注为"可选"，首期不做。
3. 通知审计日志是否保留敏感信息（如用户 ID）？——需确认合规要求，建议仅保留脱敏标识。
