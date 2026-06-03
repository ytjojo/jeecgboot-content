## 1. Flyway Migration

- [x] 1.1 编写 `V3.9.1_53__content_privacy_notifications.sql`：新增 `online_status_visibility` 列，数据映射，`content_user_third_party_auth` 表，`content_notification_audit_log` 表
- [x] 1.2 测试：在本地 MySQL 执行 migration，验证旧数据正确映射到新列，新表创建成功，回滚 SQL 可执行

## 2. 在线状态可见性扩展

- [x] 2.1 将 `ContentUserPrivacySetting.onlineStatusVisible`（Boolean）扩展为 `onlineStatusVisibility`（String），新增对应 req/vo 字段
- [x] 2.2 测试：验证 `ContentUserPrivacyUpdateReq` 中 `onlineStatusVisibility` 的 `@Pattern` 校验，以及 null/空值/越界场景处理
- [x] 2.3 更新 `IContentUserVisibilityPolicyService` 和 `ContentUserVisibilityPolicyServiceImpl`，支持三级可见性判定（PUBLIC/HIDDEN/MUTUAL_ONLY）
- [x] 2.4 测试：互关判定逻辑——验证非互关用户看不到在线状态，互关用户可以看到

## 3. 免打扰多时段增强

- [x] 3.1 扩展 `ContentNotificationDndRuleVO` 和 `ContentNotificationDndRuleReq`：从单时段改为 `dndRules[]`，新增 `dayType`、`summaryMode`、`temporaryDisable` 字段
- [x] 3.2 测试：验证多时段 JSON 序列化/反序列化，旧单时段格式自动升级为单元素列表
- [x] 3.3 更新 `ContentUserNotificationSettingServiceImpl.isInDnd()` 支持多时段判定逻辑
- [x] 3.4 测试：验证多时段重叠、跨午夜、工作日/周末分类、临时关闭 1 小时等场景
- [x] 3.5 更新 `ContentUserSettingsController`：新增免打扰规则独立更新接口
- [x] 3.6 测试：验证 `/notification/dnd/update` 接口的入参校验和返回结果

## 4. 第三方授权管理

- [x] 4.1 新建 `ContentUserThirdPartyAuth` 实体（app_name、auth_time、scopes、token_hash、status、user_id）
- [x] 4.2 测试：验证实体字段约束——app_name 不能为空、auth_time 不能为 null、scopes 越界场景
- [x] 4.3 新建 `ContentUserThirdPartyAuthMapper` 和 XML
- [x] 4.4 测试：验证 mapper CRUD，特别是按 userId 查询和按 authId + userId 撤销
- [x] 4.5 新建 `IContentUserThirdPartyAuthService` 和 `ContentUserThirdPartyAuthServiceImpl`（列出授权、撤销授权）
- [x] 4.6 测试：验证撤销授权后 token hash 被清除、状态标记为 revoked、再次访问被拒绝
- [x] 4.7 新建 `ContentUserThirdPartyAuthController`（GET /api/v1/auth/third-party, DELETE /api/v1/auth/third-party/{authId}）
- [x] 4.8 测试：验证接口鉴权——未登录返回 401，越权操作返回 403

## 5. 账户安全设置聚合接口

- [x] 5.1 新建 `ContentUserSecuritySettingVO`，聚合设备管理、密码修改、两步验证、登录提醒的状态
- [x] 5.2 测试：验证 VO 各状态字段在 null 时的默认值（安全优先，默认开启）
- [x] 5.3 新建 `IContentUserSecuritySettingService` 实现安全状态聚合（调用已有服务的状态查询方法）
- [x] 5.4 测试：验证聚合逻辑正确收集各子功能状态，子服务调用失败时有降级处理
- [x] 5.5 在 `ContentUserSettingsController` 新增 GET `/security` 接口
- [x] 5.6 测试：验证接口返回格式符合 Result<T> 规范，未登录返回 401

## 6. 通知退订合规引擎

- [x] 6.1 在 `ContentUserNotificationSettingServiceImpl.canSendNotice()` 中增加退订强制校验（确认类型开关 + 渠道 + DND）
- [x] 6.2 测试：验证关闭所有通知渠道后 canSendNotice 返回 false
- [x] 6.3 新增 `ContentNotificationAuditLog` 实体和 `ContentNotificationAuditLogMapper`
- [x] 6.4 测试：验证审计日志写入——发送/跳过决策都能记录，包含用户 ID（脱敏）、类型、渠道、决策原因
- [x] 6.5 在 `canSendNotice()` 中增加审计日志写入（try-catch 不影响主流程）
- [x] 6.6 测试：验证审计日志写入失败时不影响 canSendNotice 返回值

## 7. Redis 缓存层

- [x] 7.1 新建 `ContentUserSettingsCacheService`，提供 `getPrivacySetting(userId)`、`getNotificationSetting(userId)`、`evictPrivacy(userId)`、`evictNotification(userId)`
- [x] 7.2 测试：验证缓存 key 格式为 `content:privacy:{userId}` 和 `content:notification:{userId}`，TTL 300 秒
- [x] 7.3 在设置更新时同步调用 `evictPrivacy()` 和 `evictNotification()`
- [x] 7.4 测试：验证缓存主动删除后下次读取从数据库加载

## 8. 前端页面（Vue3）

- [x] 8.1 新建通知设置页 `NotificationSettings.vue`：六类通知开关 + 渠道选择 + 免打扰多时段配置
- [x] 8.2 测试：验证表单校验——免打扰启用时开始/结束时间必填，渠道选择至少保留一个或允许全关
- [x] 8.3 新建隐私设置页 `PrivacySettings.vue`：动态可见性 + 在线状态可见性 + 搜索引擎索引
- [x] 8.4 测试：验证可见性选项的联动——选择"仅自己可见"时提示效果
- [x] 8.5 新建第三方授权管理页 `ThirdPartyAuth.vue`：授权列表 + 撤销确认弹窗
- [x] 8.6 测试：验证撤销授权后列表自动刷新
- [x] 8.7 新建账户安全设置页 `AccountSecurity.vue`：四个安全功能入口卡片
- [x] 8.8 测试：验证各入口跳转路径正确，安全状态展示正确
