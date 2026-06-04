# 验证审核文档

**验证时间**: 2026-06-04
**验证目标**: `openspec/changes/user-06-privacy-notifications-frontend`

---

## 验证结果摘要

| 类别 | 状态 | 说明 |
|------|------|------|
| 后端通知设置 API | 部分通过 | GET/POST 存在，但 VO 缺少订阅更新渠道字段 |
| 后端隐私设置 API | 缺失 GET 端点 | 仅有 POST 更新，无独立 GET 查询端点 |
| 后端安全设置 API | 缺失更新端点 | GET 查询存在，无 POST 更新端点（登录提醒开关无法保存） |
| 后端第三方授权 API | 全部通过 | 3 个端点均存在 |
| 前端文档完整性 | 存在问题 | 字段名不一致、通知类型数量不匹配 |

---

## 后端 API 验证详情

### 1. 通知设置 API（3 个端点）

| API 路径 | 方法 | 后端状态 | 代码位置 |
|----------|------|----------|----------|
| `/content/user/settings/notification` | GET | 存在 | `ContentUserSettingsController.java:61` |
| `/content/user/settings/notification/update` | POST | 存在 | `ContentUserSettingsController.java:69` |
| `/content/user/settings/notification/dnd/update` | POST | 存在 | `ContentUserSettingsController.java:109` |

**问题**: `ContentNotificationChannelConfigVO` 仅包含 6 个渠道字段（like/comment/follow/favorite/mention/privateMessage），缺少 `subscriptionChannels`。但实体 `ContentUserNotificationSetting` 有 `subscriptionNoticeEnabled` 字段（第 7 类通知"订阅更新"）。

**影响**: 前端文档声称 7 类通知，但后端渠道配置 VO 只支持 6 类的渠道配置。

### 2. 隐私设置 API（2 个端点）

| API 路径 | 方法 | 后端状态 | 代码位置 |
|----------|------|----------|----------|
| `/content/user/settings/privacy` | GET | **缺失** | 无对应端点 |
| `/content/user/settings/privacy/update` | POST | 存在 | `ContentUserSettingsController.java:50` |

**问题**: design.md 和 specs 中引用 `GET /content/user/settings/privacy` 获取隐私设置，但后端 `ContentUserSettingsController` 中没有该 GET 端点。隐私数据存储在 `ContentUserPrivacySetting` 实体中，通过 `ContentUserProfileServiceImpl` 管理，但仅在 `GET /content/user/profile/detail` 中作为 profile 的一部分返回，没有独立的隐私设置查询 API。

**影响**: 前端隐私设置页面无法独立加载隐私数据，需要依赖 profile 接口或后端补充 GET 端点。

### 3. 安全设置 API（1 个端点 + 1 个缺失）

| API 路径 | 方法 | 后端状态 | 代码位置 |
|----------|------|----------|----------|
| `/content/user/settings/security` | GET | 存在 | `ContentUserSettingsController.java:119` |
| `/content/user/settings/security/update` | POST | **缺失** | 无对应端点 |

**问题**: `ContentUserSecuritySettingVO` 包含 `loginAlertEnabled` 字段，GET 端点可查询，但没有 POST 更新端点。specs 中 account-security 要求"切换登录提醒 Switch 开关时调用接口更新"，后端无法支持此操作。

**影响**: 登录提醒开关只能读取状态，无法保存用户切换操作。

### 4. 第三方授权 API（3 个端点）

| API 路径 | 方法 | 后端状态 | 代码位置 |
|----------|------|----------|----------|
| `/content/user/auth/third-party/` | GET | 存在 | `ContentUserThirdPartyAuthController.java:29` |
| `/content/user/auth/third-party/{authId}` | GET | 存在 | `ContentUserThirdPartyAuthController.java:38` |
| `/content/user/auth/third-party/{authId}` | DELETE | 存在 | `ContentUserThirdPartyAuthController.java:48` |

**验证结果**: 全部通过，无问题。

---

## 前端文档问题列表

### 问题 1: 通知类型数量不匹配
- **位置**: design.md / specs/notification-settings/spec.md
- **问题**: 文档声称"七类通知"（点赞、评论、关注、收藏、@我、私信、订阅更新），但后端 `ContentNotificationChannelConfigVO` 仅有 6 个渠道字段，缺少 `subscriptionChannels`
- **建议**: 后端需补充 `subscriptionChannels` 字段到 `ContentNotificationChannelConfigVO`，或前端文档改为 6 类通知

### 问题 2: 隐私设置 GET 端点缺失
- **位置**: specs/privacy-settings/spec.md (Scenario: 页面数据加载)
- **问题**: 引用 `GET /content/user/settings/privacy` 但后端不存在
- **建议方案**:
  - **方案 A（推荐）**: 后端在 `ContentUserSettingsController` 中补充 `@GetMapping("/privacy")` 端点
  - **方案 B**: 前端改为调用 `GET /content/user/profile/detail` 并从返回数据中提取隐私字段

### 问题 3: 安全设置更新端点缺失
- **位置**: specs/account-security/spec.md (Requirement: 登录提醒开关)
- **问题**: 要求切换登录提醒时调用接口更新，但后端无 `POST /content/user/settings/security/update`
- **建议**: 后端需补充安全设置更新端点，或前端暂时隐藏登录提醒 Switch 的保存功能

### 问题 4: 收藏夹字段名差异
- **位置**: specs/privacy-settings/spec.md (Scenario: 收藏夹字段名映射)
- **问题**: 文档提到后端 `favoriteVisibility` 与前端 `favoritesVisibility` 命名不一致
- **实际情况**: 后端实体字段确实是 `favoriteVisibility`（无 s），Req/VO 也一致。这是文档中预先标注的前端映射约定，非实际 bug
- **建议**: 保留此提醒，前端 API 封装层做字段映射

### 问题 5: 前端目录尚未创建
- **位置**: `jeecgboot-vue3/src/views/content/settings/`
- **问题**: settings 目录不存在，所有任务均未开始
- **影响**: 无，这是正常的待开发状态

---

## 建议修复方案

### 高优先级（阻塞前端开发）

1. **后端补充隐私设置 GET 端点**
   - 在 `ContentUserSettingsController` 中添加 `@GetMapping("/privacy")` 端点
   - 返回 `ContentUserPrivacySetting` 实体数据（或封装为 VO）

2. **后端补充安全设置更新端点**
   - 在 `ContentUserSettingsController` 中添加 `@PostMapping("/security/update")` 端点
   - 支持 `loginAlertEnabled` 字段的更新

3. **后端补充订阅更新渠道字段**
   - 在 `ContentNotificationChannelConfigVO` 中添加 `subscriptionChannels` 字段

### 中优先级（文档修正）

4. **修正 design.md 中的 API 依赖描述**
   - 明确标注哪些 API 已存在、哪些需要后端补充

5. **修正 specs 中的 API 路径引用**
   - 隐私设置 GET 路径需根据后端实际实现调整

### 低优先级（前端注意事项）

6. **前端 API 封装层处理字段映射**
   - `favoriteVisibility` <-> `favoritesVisibility` 映射
   - userId 注入拦截器兼容性验证
