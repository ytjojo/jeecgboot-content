## Context

EPIC-06 覆盖内容社区用户的通知偏好、免打扰、隐私可见性、第三方授权和账户安全入口。内容模块已存在 `ContentUserNotificationSetting`、`ContentUserPrivacySetting`、`ContentUserSettingsController`、`ContentUserVisibilityPolicyServiceImpl` 等基础实现，但当前能力仍偏设置存取，缺少退订决策审计、分项动态可见性、在线状态枚举、第三方授权撤销和统一账户安全入口契约。

约束：后端遵循内容模块 `controller / biz / service / mapper / entity / req / vo / dto` 分层；Controller 返回 `Result<T>`；跨聚合编排放 `biz`；数据库变更必须同步实体、Mapper、Flyway SQL、rollback SQL 和测试。

## Goals / Non-Goals

**Goals:**

- 补齐通知类型、渠道、免打扰、临时关闭和发送决策审计。
- 补齐浏览记录、点赞动态、收藏夹、在线状态、搜索引擎索引的可见性判定。
- 提供第三方授权列表、详情和撤销能力，并确保撤销后令牌不可继续使用。
- 提供账户安全设置聚合入口，复用已有设备管理和密码修改流程。
- 用 TDD 方式覆盖服务决策、请求校验、Controller 契约和数据库迁移。

**Non-Goals:**

- 不重写 EPIC-01 已有登录、密码修改、设备管理和 OAuth 授权主流程。
- 不实现数据导出、完整 GDPR 工具或搜索引擎已收录页面的主动删除。
- 不新增外部短信、邮件、推送或第三方 OAuth 依赖。

## Decisions

1. **通知发送前置决策集中到通知设置服务**
   - 选择：扩展 `IContentUserNotificationSettingService`，新增返回决策对象的方法，统一输出是否发送、可用渠道、跳过原因和是否安全白名单。
   - 理由：当前服务已持有通知开关、渠道 JSON 和 DND 规则，继续在这里做单用户单通知类型决策最小改动。
   - 替代方案：新增独立 notification 模块。暂不采用，因为会扩大边界且当前内容模块已经有用户通知设置表。

2. **免打扰临时关闭与摘要模式存入现有 DND JSON**
   - 选择：在 `dndRuleJson` 中扩展 `temporaryDisabledUntil`、`summaryEnabled`、`weekdayWindows`、`weekendWindows` 等字段，并保持默认 `{}` 向后兼容。
   - 理由：免打扰规则是用户偏好配置，字段组合变化较快，用 JSON 扩展比连续加列更轻。
   - 替代方案：新增 DND 规则表。暂不采用，除非后续需要无限多时间段和复杂统计。

3. **隐私可见性扩展实体字段，判定仍在 `ContentUserVisibilityPolicyServiceImpl`**
   - 选择：在 `content_user_privacy_setting` 增加浏览记录、点赞动态、收藏夹、在线状态可见性字段，在线状态用字符串枚举表达 `PUBLIC / MUTUAL_ONLY / HIDDEN`。
   - 理由：隐私判定属于查询层公共策略，集中在已有 visibility policy 服务可避免各接口重复判断。
   - 替代方案：把隐私判断分散到主页、收藏、在线状态接口。暂不采用，容易产生隐私绕过。

4. **第三方授权管理在内容用户域建用户可见投影**
   - 选择：新增 `ContentThirdPartyAuthorization` 及撤销服务，记录应用名、授权范围、授权时间、状态和令牌标识；撤销时先标记授权，再调用现有或待接入的令牌失效适配器。
   - 理由：用户侧管理需要稳定展示授权范围，即使底层 OAuth 表结构变化也不直接泄露认证模块内部结构。
   - 替代方案：Controller 直接查询系统 OAuth 表。暂不采用，因为会跨越内容模块边界并耦合认证存储。

5. **账户安全入口只聚合入口与偏好，不复制安全流程**
   - 选择：新增 `ContentAccountSecuritySettingVO` 和服务方法，返回设备管理、密码修改、两步验证、登录提醒入口；登录提醒偏好落在用户通知/安全设置中。
   - 理由：PRD 明确本史诗主要提供统一入口，核心安全流程复用已有能力。
   - 替代方案：在本变更内实现两步验证完整流程。暂不采用，超出 EPIC-06 范围且风险高。

## Risks / Trade-offs

- [Risk] 现有 `onlineStatusVisible` 是 Boolean，无法表达互关可见 → Mitigation：新增枚举字段并保留旧字段兼容读取，迁移默认值由旧 Boolean 映射。
- [Risk] JSON DND 规则扩展可能产生格式兼容问题 → Mitigation：读 JSON 时使用默认值补齐，非法格式显式失败并覆盖请求校验测试。
- [Risk] 隐私缓存未及时失效导致短暂泄露 → Mitigation：更新隐私设置时删除用户隐私缓存 key，并为缓存设置不超过 5 分钟 TTL。
- [Risk] 第三方令牌失效依赖认证模块适配 → Mitigation：先定义内容模块接口 `ContentThirdPartyTokenRevocationPort`，默认实现记录待失效状态；接入系统令牌服务时替换实现。
- [Risk] 通知退订只在设置服务实现但发送方未调用 → Mitigation：所有内容模块产生通知的服务任务必须改为调用发送决策方法，测试覆盖跳过行为。

## File Structure

- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/entity/ContentUserNotificationDeliveryLog.java`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/entity/ContentThirdPartyAuthorization.java`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/mapper/ContentUserNotificationDeliveryLogMapper.java`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/mapper/ContentThirdPartyAuthorizationMapper.java`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/IContentThirdPartyAuthorizationService.java`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/ContentThirdPartyTokenRevocationPort.java`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/impl/ContentThirdPartyAuthorizationServiceImpl.java`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/impl/ContentNoopThirdPartyTokenRevocationPort.java`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/dto/ContentNotificationDecisionDTO.java`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/req/settings/ContentThirdPartyAuthorizationRevokeReq.java`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/vo/ContentThirdPartyAuthorizationVO.java`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/vo/ContentThirdPartyAuthorizationDetailVO.java`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/vo/ContentAccountSecuritySettingVO.java`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/controller/ContentUserSettingsController.java`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/entity/ContentUserNotificationSetting.java`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/entity/ContentUserPrivacySetting.java`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/IContentUserNotificationSettingService.java`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/IContentUserVisibilityPolicyService.java`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/impl/ContentUserNotificationSettingServiceImpl.java`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/impl/ContentUserVisibilityPolicyServiceImpl.java`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/resources/flyway/sql/mysql/V3.9.1_58__content_privacy_notifications.sql`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/resources/flyway/sql/mysql/V3.9.1_58__content_privacy_notifications_rollback.sql`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserNotificationSettingServiceTest.java`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserVisibilityPolicyServiceTest.java`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentThirdPartyAuthorizationServiceTest.java`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/controller/ContentUserControllerWebMvcTest.java`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/req/ContentUserReqValidationTest.java`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentPrivacyNotificationsMigrationTest.java`

## Test Strategy

- `ContentUserNotificationSettingServiceTest`: 覆盖通知类型开关、渠道过滤、跨午夜 DND、多时段 DND、临时关闭、安全通知白名单、决策日志写入和非法类型显式失败。
- `ContentUserVisibilityPolicyServiceTest`: 覆盖浏览记录/点赞动态/收藏夹的公开、关注者、仅自己规则，在线状态公开/互关/隐藏规则，搜索引擎 noindex 判定和缓存失效调用。
- `ContentThirdPartyAuthorizationServiceTest`: 覆盖授权列表、详情、越权访问拦截、撤销幂等、令牌失效端口调用和撤销后访问拒绝。
- `ContentUserControllerWebMvcTest`: 覆盖设置页接口返回 `Result<T>`、账户安全入口、第三方授权列表/详情/撤销、通知设置保存和隐私设置保存的 HTTP 契约。
- `ContentUserReqValidationTest`: 覆盖通知渠道、DND 时间、临时关闭时长、隐私可见性枚举、第三方撤销入参的校验消息。
- `ContentPrivacyNotificationsMigrationTest`: 覆盖新增表、字段、索引和 rollback SQL 文件存在且包含反向 DDL。

## Migration Plan

1. 新增 Flyway migration：
   - 为 `content_user_privacy_setting` 增加 `browse_history_visibility`、`like_activity_visibility`、`favorite_visibility`、`online_status_visibility` 等字段。
   - 新增 `content_user_notification_delivery_log` 记录发送/跳过决策。
   - 新增 `content_third_party_authorization` 存储用户可见授权投影。
   - 补充必要唯一索引：用户隐私设置按 `user_id`，授权按 `user_id + app_id`，通知日志按 `user_id + notice_type + create_time` 辅助审计。
2. 数据默认值：
   - 隐私可见性默认 `PUBLIC`，在线状态由旧 `online_status_visible` 映射：true 为 `PUBLIC`，false 为 `HIDDEN`。
   - 通知决策日志不回填历史数据。
3. 回滚方案：
   - rollback SQL 删除新增索引和新增表。
   - rollback SQL 删除 `content_user_privacy_setting` 新增字段。
   - 回滚不恢复被撤销的第三方授权状态，生产回滚前需暂停授权撤销入口并保留审计备份。
4. 验收：
   - migration 测试通过。
   - 目标服务和 Controller 测试通过。
   - `openspec validate --change epic-06-privacy-notifications --strict` 通过。

## Open Questions

- 第三方令牌失效是否已有系统模块服务可直接调用；若没有，本变更先落默认端口和撤销状态，后续接入真实令牌失效实现。
- 搜索引擎 noindex 应由后端 API Header 返回，还是由前端 SSR/meta 层消费布尔值后渲染；当前设计先由后端提供判定结果。
