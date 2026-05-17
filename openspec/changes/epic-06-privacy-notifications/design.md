## Context

当前内容社区系统（基于 Spring Boot 3 + JeecgBoot）已具备基础用户认证（EPIC-01）和内容互动功能，但缺少精细化的隐私控制和通知管理能力。用户在通知接收、可见性边界、第三方授权方面缺乏自主控制权，影响用户体验和合规要求。

现有架构：
- 后端：Spring Boot 3 + MyBatis Plus + Redis，jeecg-module-content 模块负责内容社区
- 用户关系：ContentUserRelation 实体已存在，支持关注/拉黑等关系类型
- 通知：通知发送服务存在但缺乏用户偏好层
- 前端：Vue 3 + Ant Design Vue

## Goals / Non-Goals

**Goals:**
- 用户可精确控制每类通知的开关与推送渠道（App/短信/邮件）
- 用户可配置免打扰时段，安全通知白名单豁免
- 用户可控制动态、在线状态、搜索引擎索引的可见性
- 用户可查看和撤销第三方 OAuth 授权
- 统一的账户安全设置入口
- 严格执行通知退订规则，非安全通知不得在用户关闭后继续发送

**Non-Goals:**
- 数据导出功能（GDPR 合规工具，可选）
- 完整的 GDPR 合规实现
- 短信/邮件推送服务的基础设施搭建（假设已有）
- 两步验证、密码修改的具体实现（仅提供入口，功能在 EPIC-01 已实现）

## Decisions

### 1. 通知偏好存储方案
**决策**：使用独立的 `content_notification_preference` 表，每用户每类通知一条记录，JSON 字段存储渠道配置。
**理由**：比宽表更灵活，新增通知类型无需改表结构。
**替代方案**：用户表增加 JSON 偏好字段 — 查询效率略低但更灵活，考虑到通知类型会增长，独立表更利于扩展和审计。

### 2. 免打扰判断时机
**决策**：在通知发送服务层（Service）判断免打扰时段，而非消息队列层。
**理由**：逻辑集中，便于测试和调试；免打扰是用户级配置，在发送前拦截最安全。
**替代方案**：消息队列层过滤 — 性能更好但逻辑分散，且紧急通知白名单难统一管理。

### 3. 可见性缓存策略
**决策**：Redis 缓存可见性设置，用户修改后立即使对应 key 失效（主动失效而非 TTL）。
**理由**：隐私设置对时效性要求高，主动失效确保 5 分钟内必生效。
**替代方案**：纯 TTL 失效 — 实现简单但最坏情况下 5 分钟内可能泄露隐私。

### 4. 搜索引擎索引控制
**决策**：在 SSR 渲染或前端模板中根据用户设置动态输出 `noindex` meta 标签，同时在 HTTP Response Header 中设置 `X-Robots-Tag: noindex`。
**理由**：双保险，meta 标签覆盖爬虫，header 覆盖不支持 meta 的爬虫。

### 5. 第三方授权撤销
**决策**：撤销时仅使本系统的 Access Token 失效，不主动通知第三方应用（可选 webhook）。
**理由**：OAuth 标准不要求主动通知；第三方应用下次使用失效 Token 时会自动发现。

### 6. 通知退订合规
**决策**：通知发送前统一通过 `NotificationDispatchService` 检查用户偏好，该服务为唯一入口，所有通知发送必须经过。
**理由**：集中控制确保不会出现绕过用户偏好的旁路。

## Risks / Trade-offs

- [缓存不一致] Redis 缓存与数据库不同步 → 主动失效 + 5 分钟兜底 TTL
- [免打扰时区] 跨时区用户时段计算错误 → 存储用户本地时区，判断时转换为 UTC
- [通知性能] 每次发送前检查偏好增加延迟 → Redis 缓存偏好数据，单次检查 <1ms
- [搜索引擎延迟] noindex 设置后已收录页面不会立即移除 → 文档中明确告知用户需等待重新抓取
- [Token 撤销延迟] 已签发的 Token 在撤销后仍有效直到过期 → 增加 Token 黑名单/版本号机制

## Migration Plan

1. 执行 DDL 创建新表：`content_notification_preference`、`content_dnd_config`、`content_visibility_setting`、`content_third_party_auth`
2. 初始化默认通知偏好数据（所有通知默认开启 App 内渠道）
3. 部署通知发送层改造代码
4. 灰度发布：先对 10% 用户生效，验证无异常后全量
5. 回滚：保留旧表结构，回滚代码即可恢复旧行为；新表数据不删除

## File Structure

**后端新增文件：**
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/privacy/entity/NotificationPreference.java`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/privacy/entity/DndConfig.java`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/privacy/entity/VisibilitySetting.java`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/privacy/mapper/NotificationPreferenceMapper.java`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/privacy/mapper/DndConfigMapper.java`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/privacy/mapper/VisibilitySettingMapper.java`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/privacy/service/NotificationPreferenceService.java`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/privacy/service/DndConfigService.java`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/privacy/service/VisibilitySettingService.java`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/privacy/service/ThirdPartyAuthService.java`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/privacy/service/NotificationDispatchService.java`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/privacy/controller/PrivacyController.java`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/privacy/enums/NotificationChannelEnum.java`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/privacy/enums/NotificationTypeEnum.java`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/privacy/enums/VisibilityLevelEnum.java`
- `jeecg-boot/db/migration/V6_1__create_privacy_tables.sql`

**后端修改文件：**
- 现有通知发送服务：增加对 NotificationDispatchService 的调用
- 用户主页查询接口：增加可见性判定中间件
- 用户在线状态查询接口：增加可见性过滤

**前端新增文件：**
- `jeecgboot-vue3/src/views/content/privacy/NotificationSetting.vue`
- `jeecgboot-vue3/src/views/content/privacy/PrivacySetting.vue`
- `jeecgboot-vue3/src/views/content/privacy/ThirdPartyAuthSetting.vue`
- `jeecgboot-vue3/src/views/content/privacy/AccountSecurityEntry.vue`
- `jeecgboot-vue3/src/api/content/privacy.ts`

**测试文件：**
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/privacy/service/NotificationPreferenceServiceTest.java`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/privacy/service/DndConfigServiceTest.java`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/privacy/service/VisibilitySettingServiceTest.java`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/privacy/service/ThirdPartyAuthServiceTest.java`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/privacy/service/NotificationDispatchServiceTest.java`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/privacy/controller/PrivacyControllerTest.java`

## Test Strategy

| 测试文件 | 测试策略 |
|---------|---------|
| NotificationPreferenceServiceTest | 单元测试：CRUD、渠道配置、即时生效验证 |
| DndConfigServiceTest | 单元测试：时段判断逻辑、时区转换、安全白名单、临时关闭 |
| VisibilitySettingServiceTest | 单元测试：可见性等级判定、缓存失效、跨用户查询 |
| ThirdPartyAuthServiceTest | 单元测试：授权列表查询、Token 撤销、撤销后访问拒绝 |
| NotificationDispatchServiceTest | 单元测试：偏好检查、DND 过滤、安全白名单、退订合规 |
| PrivacyControllerTest | 集成测试：API 端点权限验证、参数校验、端到端流程 |
