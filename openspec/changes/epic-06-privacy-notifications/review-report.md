# EPIC-06 Privacy Notifications 实现审核报告

> **审核日期**: 2026-05-27
> **审核依据**: `openspec/changes/epic-06-privacy-notifications/plan.md`
> **审核范围**: Step 1.1 ~ Step 7.4 全部 27 个子步骤

---

## 总览

| 状态 | 数量 | 占比 |
|------|------|------|
| ✅ 已完成 | 7 | 25.9% |
| ⚠️ 部分完成 | 17 | 63.0% |
| ❌ 未完成 | 2 | 7.4% |
| ❓ 未确认 | 1 | 3.7% |

---

## Step 1: 数据库迁移与基础实体

### Step 1.1: Flyway 迁移和回滚 SQL — ⚠️ 部分完成

| 检查项 | 状态 | 说明 |
|--------|------|------|
| 迁移文件存在 | ✅ | `V3.9.1_58__content_privacy_notifications.sql` 存在 |
| 回滚文件存在 | ✅ | `V3.9.1_58__content_privacy_notifications_rollback.sql` 存在 |
| `online_status_visibility` 字段 | ✅ | 已包含，含旧数据迁移逻辑 |
| `browse_history_visibility` 字段 | ❌ | 迁移 SQL 中无此字段 |
| `like_activity_visibility` 字段 | ❌ | 迁移 SQL 中无此字段 |
| `favorite_visibility` 字段 | ❌ | 迁移 SQL 中无此字段 |
| `content_third_party_authorization` 表 | ⚠️ | 功能存在，但实际表名为 `content_user_third_party_auth` |
| `content_user_notification_delivery_log` 表 | ⚠️ | 功能存在，但实际表名为 `content_notification_audit_log` |

### Step 1.2: 实体、Mapper、DTO、Req、VO — ⚠️ 部分完成

**文件名映射关系（计划 → 实际）：**

| 计划文件名 | 实际文件名 | 状态 |
|------------|------------|------|
| `ContentThirdPartyAuthorization.java` | `ContentUserThirdPartyAuth.java` | ✅ 存在，功能等价 |
| `ContentUserNotificationDeliveryLog.java` | `ContentNotificationAuditLog.java` | ✅ 存在，功能等价 |
| `ContentThirdPartyAuthorizationMapper.java` | `ContentUserThirdPartyAuthMapper.java` | ✅ 存在，功能等价 |
| `ContentUserNotificationDeliveryLogMapper.java` | `ContentNotificationAuditLogMapper.java` | ✅ 存在，功能等价 |
| `ContentNotificationDecisionDTO.java` | `ContentSubscriptionNotificationDecisionVO.java` | ✅ 存在，功能等价（注意：位置从 dto 变为 vo） |
| `ContentThirdPartyAuthorizationVO.java` | `ContentThirdPartyAuthVO.java` | ✅ 存在，功能等价 |
| `ContentAccountSecuritySettingVO.java` | `ContentUserSecuritySettingVO.java` | ✅ 存在，功能等价 |
| `ContentThirdPartyAuthorizationRevokeReq.java` | — | ❌ 不存在，撤销逻辑直接在 Mapper 中实现 |
| `ContentThirdPartyAuthorizationDetailVO.java` | — | ❌ 不存在，仅有列表 VO 无详情 VO |

### Step 1.3: 迁移和 Mapper 测试 — ❌ 未完成

| 检查项 | 状态 |
|--------|------|
| `ContentPrivacyNotificationsMigrationTest.java` | ❌ 文件不存在 |

---

## Step 2: 通知设置

### Step 2.1: 通知设置 API 和服务契约 — ⚠️ 部分完成

| 检查项 | 状态 | 说明 |
|--------|------|------|
| 保留 `getSetting` | ✅ | 已保留 |
| 保留 `updateSetting` | ✅ | 已保留 |
| 保留 `canSendNotice` | ✅ | 已保留 |
| `updateDndRule` 新方法 | ✅ | 已实现，返回 `ContentNotificationDndRuleVO` |
| `listSupportedNoticeTypes()` | ❌ | 接口中无此方法 |
| 独立 `saveChannelPreference()` | ❌ | 渠道偏好通过 `updateSetting` 间接实现，无独立方法 |

### Step 2.2: 通知发送决策逻辑 — ⚠️ 部分完成

| 检查项 | 状态 | 说明 |
|--------|------|------|
| 禁用类型检查 | ✅ | `isNoticeTypeEnabled()` 已实现 |
| 禁用渠道检查 | ✅ | `getChannels()` 已实现 |
| DND 窗口检查 | ✅ | `isInDnd()` 已实现，支持多时段 |
| 跨午夜 DND 处理 | ✅ | `isInSingleDnd()` 正确处理 |
| 临时 DND 禁用 | ✅ | 检查 `temporaryDisableUntil` |
| 安全通知绕过 DND | ✅ | `SECURITY` 类型直接返回 `true` |
| 独立 `decideNoticeDelivery()` 方法 | ❌ | 逻辑分散在 `canSendNotice()` 中，签名不同 |
| 全渠道退订检查 | ❌ | 无"全部退订"总开关 |

### Step 2.3: 持久化通知投递决策日志 — ⚠️ 部分完成

| 检查项 | 状态 | 说明 |
|--------|------|------|
| 注入日志 Mapper | ✅ | 使用 `ContentNotificationAuditLogMapper` |
| 写入决策日志 | ✅ | `canSendNotice()` 中写入审计日志 |
| Mapper 名称匹配 | ⚠️ | 计划要求 `ContentUserNotificationDeliveryLogMapper`，实际为 `ContentNotificationAuditLogMapper` |

### Step 2.4: 通知偏好服务测试 — ✅ 已完成

测试方法（共 10 个）：
1. `shouldUpdateNotificationChannelsAndDndRule` — 更新通知渠道和免打扰规则
2. `shouldBlockDisabledNoticeType` — 禁用通知类型时阻止发送
3. `shouldBlockNoticeDuringDndWindow` — DND 窗口内阻止通知
4. `shouldAllowSecurityNoticeEvenWhenDndEnabled` — 安全通知绕过 DND
5. `shouldAutoUpgradeOldDndFormatToMultiPeriod` — 旧格式自动升级
6. `shouldBlockNoticeDuringMultiPeriodDnd` — 多时段 DND
7. `shouldAllowNoticeOutsideAllDndPeriods` — 非 DND 时段允许
8. `shouldRespectTemporaryDisable` — 临时关闭免打扰
9. `shouldEvictNotificationCacheOnSettingUpdate` — 更新设置驱逐缓存
10. `shouldEvictNotificationCacheOnDndRuleUpdate` — 更新 DND 驱逐缓存

---

## Step 3: 隐私可见性

### Step 3.1: 扩展隐私设置存储和更新契约 — ⚠️ 部分完成

| 检查项 | 状态 |
|--------|------|
| `onlineStatusVisibility` 字段 | ✅ 已存在（实体、Req、Service 均已处理） |
| `browseHistoryVisibility` 字段 | ❌ 实体、Req、Service 中均缺失 |
| `likeActivityVisibility` 字段 | ❌ 实体、Req、Service 中均缺失 |
| `favoriteVisibility` 字段 | ❌ 实体、Req、Service 中均缺失 |
| `allowSearchEngineIndex` 字段 | ✅ 已存在 |

### Step 3.2: 扩展可见性策略逻辑 — ⚠️ 部分完成

| 检查项 | 状态 | 说明 |
|--------|------|------|
| `canViewOnlineStatus` | ✅ | 已实现，支持 PUBLIC/HIDDEN/MUTUAL_ONLY |
| 所有者始终可见 | ✅ | 所有方法开头均有 `Objects.equals` 检查 |
| 公开访问 `PUBLIC` | ✅ | 已实现 |
| 仅关注者 `FOLLOWERS_ONLY` | ✅ | 已实现 |
| 互相关注 `MUTUAL_ONLY` | ✅ | 已实现 |
| 仅自己/隐藏 `PRIVATE`/`HIDDEN` | ✅ | 已实现 |
| 活动可见性方法（如 `canViewActivity`） | ❌ | 接口和实现中均不存在 |

### Step 3.3: Noindex 决策支持 — ⚠️ 部分完成

| 检查项 | 状态 |
|--------|------|
| `allowSearchEngineIndex` 字段 | ✅ 存在，默认 `true` |
| `shouldNoindexProfile()` 或等效方法 | ❌ 无任何服务方法消费该字段生成 noindex 决策 |

### Step 3.4: 缓存失效 — ✅ 已完成

- `ContentUserProfileServiceImpl.updatePrivacy()` 在 insert/update 两条路径均有缓存失效
- 清除 `PROFILE_CACHE_PREFIX`、`PRIVACY_CACHE_PREFIX`、`PROFILE_PUBLIC_CACHE_PREFIX`
- 调用 `settingsCacheService.evictPrivacy(userId)` 清除 `content:privacy:` 前缀 key

### Step 3.5: 隐私可见性服务测试 — ✅ 已完成

测试方法（共 8 个）：
1. `shouldRejectUserSearchWhenOwnerDisablesSearch` — 搜索禁用
2. `shouldStillAllowContentViewWhenViewerMutedOwner` — 静默关系
3. `shouldRejectContentViewWhenOwnerBlocksViewer` — 拉黑拒绝
4. `shouldRejectContentViewWhenViewerHasBlacklistedOwner` — 黑名单拒绝
5. `shouldRejectOnlineStatusWhenMutualOnlyAndNotMutualFollow` — 单向关注拒绝
6. `shouldAllowOnlineStatusWhenMutualOnlyAndMutualFollow` — 互关允许
7. `shouldAllowOnlineStatusWhenPublic` — 公开允许
8. `shouldRejectOnlineStatusWhenHidden` — 隐藏拒绝

---

## Step 4: 第三方授权管理

### Step 4.1: 第三方授权服务 — ⚠️ 部分完成

**文件名映射关系：**

| 计划文件名 | 实际文件名 | 状态 |
|------------|------------|------|
| `IContentThirdPartyAuthorizationService.java` | `IContentUserThirdPartyAuthService.java` | ✅ 功能等价 |
| `ContentThirdPartyAuthorizationServiceImpl.java` | `ContentUserThirdPartyAuthServiceImpl.java` | ✅ 功能等价 |
| `ContentThirdPartyAuthorizationServiceTest.java` | `ContentUserThirdPartyAuthServiceTest.java` | ✅ 功能等价 |

**方法检查：**

| 计划方法 | 实际方法 | 状态 |
|----------|----------|------|
| `list` | `listActiveAuths(String userId)` | ✅ |
| `detail` | — | ❌ 接口中无查询单条详情的方法 |
| `revoke` | `revokeAuth(String userId, String authId)` | ✅ |
| `revoked-access check` | — | ❌ 无独立方法（revokeAuth 内部有校验） |

### Step 4.2: Token 撤销端口 — ❌ 未完成

| 检查项 | 状态 |
|--------|------|
| `ContentThirdPartyTokenRevocationPort.java` | ❌ 文件不存在 |
| `ContentNoopThirdPartyTokenRevocationPort.java` | ❌ 文件不存在 |
| revokeAuth 中调用 token 撤销 | ❌ 仅更新数据库状态，无 token 撤销调用 |

### Step 4.3: 第三方授权 API 契约 — ⚠️ 部分完成

| 检查项 | 状态 | 说明 |
|--------|------|------|
| 列表端点 | ✅ | `GET /content/user/auth/third-party/`（在独立 Controller 中） |
| 详情端点 | ❌ | Controller 中无详情查询端点 |
| 撤销端点 | ✅ | `DELETE /content/user/auth/third-party/{authId}`（在独立 Controller 中） |
| 返回 VO 而非实体 | ✅ | 返回 `ContentThirdPartyAuthVO` |
| `ContentThirdPartyAuthorizationDetailVO` | ❌ | 文件不存在 |
| 端点位置 | ⚠️ | 在 `ContentUserThirdPartyAuthController` 而非 `ContentUserSettingsController` |

### Step 4.4: 第三方授权测试 — ⚠️ 部分完成

| 检查项 | 状态 |
|--------|------|
| 撤销访问拒绝测试 | ✅ `revokeAuthShouldThrowWhenAuthAlreadyRevoked` |
| 重新授权需求测试 | ❌ 缺失 |
| 所有者隔离测试 | ❌ 缺失 |
| HTTP 响应形状测试 | ✅ 存在 |

---

## Step 5: 账户安全设置

### Step 5.1: 账户安全设置响应模型 — ✅ 已完成

`ContentUserSecuritySettingVO.java` 包含：
- `deviceManagementEnabled` — 设备管理（默认 true）
- `passwordChangeEnabled` — 密码更改（默认 true）
- `twoFactorEnabled` — 两步验证（默认 false）
- `loginAlertEnabled` — 登录提醒（默认 true）

### Step 5.2: 现有设备和密码流 — ⚠️ 部分完成

- `IContentUserSecuritySettingService` 仅有 `getSecuritySetting(userId)` 只读方法
- 实现类硬编码返回默认值，注释标注"后续接入真实服务"
- 无实际设备管理/密码更改业务接入

### Step 5.3: 登录提醒偏好 — ⚠️ 部分完成

- `ContentNotificationDndRuleReq.java` 和 `ContentNotificationDndRuleVO.java` 存在
- 登录提醒偏好仅在 VO 中作为只读字段返回默认值
- 无独立的登录提醒偏好保存（update）方法或端点

### Step 5.4: 账户安全控制器和验证测试 — ⚠️ 部分完成

| 检查项 | 状态 |
|--------|------|
| `/security` 端点的 WebMvc 测试 | ❌ 缺失 |
| 登录提醒更新请求验证测试 | ❌ 缺失 |

---

## Step 6: 控制器集成与验证

### Step 6.1: 扩展设置控制器端点 — ⚠️ 部分完成

**ContentUserSettingsController 已有端点：**
- `POST /content/user/settings/privacy/update` — 隐私设置更新 ✅
- `GET /content/user/settings/notification` — 通知偏好查询 ✅
- `POST /content/user/settings/notification/update` — 通知偏好更新 ✅
- `POST /content/user/settings/notification/dnd/update` — 免打扰规则更新 ✅
- `GET /content/user/settings/feed` — 关注流设置查询 ✅
- `POST /content/user/settings/feed/update` — 关注流设置更新 ✅
- `GET /content/user/settings/visibility/content` — 内容可见性检查 ✅
- `GET /content/user/settings/security` — 账户安全设置查询 ✅

**缺失：** 第三方授权端点在独立 `ContentUserThirdPartyAuthController` 中，未在 SettingsController 中

### Step 6.2: 请求验证 — ✅ 已完成

- Bean Validation 注解完备（`@Pattern`, `@Valid`, `@NotNull`, `@Size`）
- 验证消息全部为中文，符合项目风格

### Step 6.3: 设置 WebMvc 测试 — ⚠️ 部分完成

`ContentUserSettingsControllerWebMvcTest.java` 仅覆盖 2 个测试用例：
1. `shouldReturn200ForValidDndRuleUpdate` — 免打扰规则更新
2. `shouldRejectInvalidTimeFormat` — 非法时间格式校验

**未覆盖的端点：** 隐私更新、通知查询/更新、关注流、可见性检查、安全设置（共 6 个）

---

## Step 7: 回归测试与最终验证

### Step 7.1: 回归测试 — ✅ 已完成

- `ContentSubscriptionNotificationPreferenceServiceTest.java` — 4 个测试方法
- `ContentUserProfileServiceTest.java` — 15 个测试方法

### Step 7.2: 运行目标 Maven 测试 — ❓ 未确认

- 无测试运行输出的直接证据（无 surefire-reports、无 mvn 相关 commit）

### Step 7.3: 运行 OpenSpec 验证 — ⚠️ 部分完成

- openspec 产出文件存在且有更新记录
- 无正式 `openspec validate` 命令运行的证据

### Step 7.4: 刷新 Graphify 元数据 — ✅ 已完成

- graphify-out 目录今日（2026-05-27）刷新并提交
- 最新 commit: `45725732e [graphify]`

---

## 计划 vs 实际文件名完整映射表

| 计划中文件名 | 实际文件名 | 功能是否等价 |
|---|---|---|
| `ContentThirdPartyAuthorization.java` | `ContentUserThirdPartyAuth.java` | ✅ |
| `ContentUserNotificationDeliveryLog.java` | `ContentNotificationAuditLog.java` | ✅ |
| `ContentThirdPartyAuthorizationMapper.java` | `ContentUserThirdPartyAuthMapper.java` | ✅ |
| `ContentUserNotificationDeliveryLogMapper.java` | `ContentNotificationAuditLogMapper.java` | ✅ |
| `ContentNotificationDecisionDTO.java` | `ContentSubscriptionNotificationDecisionVO.java` | ✅ |
| `ContentThirdPartyAuthorizationVO.java` | `ContentThirdPartyAuthVO.java` | ✅ |
| `ContentThirdPartyAuthorizationDetailVO.java` | — | ❌ 不存在 |
| `ContentThirdPartyAuthorizationRevokeReq.java` | — | ❌ 不存在 |
| `ContentAccountSecuritySettingVO.java` | `ContentUserSecuritySettingVO.java` | ✅ |
| `IContentThirdPartyAuthorizationService.java` | `IContentUserThirdPartyAuthService.java` | ✅ |
| `ContentThirdPartyAuthorizationServiceImpl.java` | `ContentUserThirdPartyAuthServiceImpl.java` | ✅ |
| `ContentNoopThirdPartyTokenRevocationPort.java` | — | ❌ 不存在 |
| `ContentThirdPartyTokenRevocationPort.java` | — | ❌ 不存在 |
| `ContentPrivacyNotificationsMigrationTest.java` | — | ❌ 不存在 |
| `ContentThirdPartyAuthorizationServiceTest.java` | `ContentUserThirdPartyAuthServiceTest.java` | ✅ |

---

## 关键未完成项汇总（按优先级排序）

### P0 — 核心功能缺失

1. **`browse_history_visibility` / `like_activity_visibility` / `favorite_visibility` 字段**
   - 影响范围：迁移 SQL、实体、Req、Service 全链路缺失
   - 涉及步骤：1.1、1.2、3.1、3.2

2. **Token 撤销端口（Step 4.2）**
   - `ContentThirdPartyTokenRevocationPort.java` 和 `ContentNoopThirdPartyTokenRevocationPort.java` 完全不存在
   - revokeAuth 中未调用任何 token 撤销逻辑

3. **`ContentPrivacyNotificationsMigrationTest.java`（Step 1.3）**
   - 迁移合同测试完全缺失

### P1 — 功能不完整

4. **第三方授权详情端点和 VO（Step 4.3）**
   - 缺少 `ContentThirdPartyAuthorizationDetailVO.java`
   - Controller 中无详情查询端点

5. **`shouldNoindexProfile()` 决策方法（Step 3.3）**
   - `allowSearchEngineIndex` 字段存在但无服务方法消费

6. **活动可见性方法（Step 3.2）**
   - `canViewActivity` 等方法在接口和实现中均缺失

### P2 — 测试覆盖不足

7. **`ContentUserSettingsControllerWebMvcTest` 覆盖不足（Step 6.3）**
   - 仅 2 个测试用例，6 个端点无测试

8. **第三方授权所有者隔离测试和重新授权测试（Step 4.4）**

9. **账户安全端点测试和登录提醒验证测试（Step 5.4）**

10. **Maven 测试运行未确认（Step 7.2）**
