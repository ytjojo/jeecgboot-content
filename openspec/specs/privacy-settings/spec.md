# privacy-settings Specification

## Purpose
TBD - created by archiving change complete-profile-management-frontend. Update Purpose after archive.
## Requirements
### Requirement: Privacy settings page covering 15 visibility fields
系统 SHALL 提供隐私设置页面，覆盖 15 个 `*Visibility` 字段和 2 个 Boolean 字段，调用 `POST /content/user/profile/privacy/update?userId=X`。

#### Scenario: Load privacy settings
- **WHEN** 用户进入隐私设置页
- **THEN** 页面调用 `GET /content/user/profile/detail?ownerUserId=X&viewerUserId=X` 读取当前隐私配置，按 7 个分组（基础资料/扩展/主页/认证/活动/在线状态/布尔开关）展示

#### Scenario: Save privacy settings
- **WHEN** 用户修改任一 visibility 字段后点击保存
- **THEN** 系统调用 `POST /content/user/profile/privacy/update?userId=X` 提交 `ContentUserPrivacyUpdateReq`，成功后显示"隐私设置已更新"

### Requirement: Field visibility levels
系统 SHALL 支持 4 种 visibility 级别：`PUBLIC` / `FOLLOWERS_ONLY` / `MUTUAL_ONLY` / `PRIVATE`，通过 Select 组件选择。

#### Scenario: Set field to followers only
- **WHEN** 用户将"简介"字段的可见性设为 `FOLLOWERS_ONLY`
- **THEN** 保存后仅关注该用户的人可以看到其简介

#### Scenario: Nickname and avatar visibility
- **WHEN** 隐私设置不包含 nickname / avatar 的 visibility 字段
- **THEN** 昵称和头像保持公开，不出现在隐私设置列表中

### Requirement: Visibility field coverage (15 fields)
隐私设置 SHALL 覆盖以下 15 个 `*Visibility` 字段（按 7 个分组）：

- **基础资料 (5)**: `bioVisibility` / `genderVisibility` / `birthdayVisibility` / `regionVisibility` / `professionVisibility`
- **扩展资料 (1)**: `personalLinkVisibility`
- **主页 (3)**: `homepageBackgroundVisibility` / `themeColorVisibility` / `homepageModuleVisibility`
- **认证 (2)**: `certificationVisibility` / `verificationBadgesVisibility`
- **活动 (3)**: `profileCompletionVisibility` / `profileReviewStatusVisibility` / `recentActivityVisibility`
- **在线状态 (1)**: `onlineStatusVisibility` ← 特殊枚举
- **布尔开关 (2)**: `showMutualFollowersCount` / `showRecentActivityHighlight`

#### Scenario: User configures all 15 visibility fields
- **WHEN** 用户分别为每个字段设置 visibility
- **THEN** 保存请求 `ContentUserPrivacyUpdateReq` 包含全部 15 个字段值

### Requirement: Online status visibility has reduced enum
系统 SHALL 为 `onlineStatusVisibility` 字段提供特殊枚举选项：`PUBLIC` / `HIDDEN` / `MUTUAL_ONLY`，不含 `PRIVATE`。

#### Scenario: onlineStatusVisibility Select options
- **WHEN** 用户点击 `onlineStatusVisibility` 字段的 Select
- **THEN** 选项仅显示 `PUBLIC` / `HIDDEN` / `MUTUAL_ONLY`，不显示 `PRIVATE`

#### Scenario: Backend rejects invalid onlineStatusVisibility
- **WHEN** 用户提交 `onlineStatusVisibility=PRIVATE`
- **THEN** 后端返回校验错误，前端显示"该字段不支持 PRIVATE 级别"

### Requirement: Boolean switch fields
系统 SHALL 为 `showMutualFollowersCount` 和 `showRecentActivityHighlight` 字段提供 Switch 开关组件。

#### Scenario: Toggle boolean field
- **WHEN** 用户切换 `showMutualFollowersCount` Switch
- **THEN** 保存时 `ContentUserPrivacyUpdateReq` 中该字段为 `true` 或 `false`

### Requirement: Privacy cache immediate effect
系统 SHALL 在隐私设置保存成功后立即调用 `GET /content/user/profile/detail?ownerUserId=X&viewerUserId=X` 刷新本地缓存。

#### Scenario: Refresh local cache after save
- **WHEN** 用户保存隐私设置成功
- **THEN** 前端主动调用 detail 接口刷新本地缓存，提示"隐私设置已更新，新设置将立即对新访问者生效"

#### Scenario: Silent cache inconsistency handling
- **WHEN** 因网络问题导致刷新失败
- **THEN** 系统不向用户展示技术细节，静默处理

### Requirement: Privacy settings responsive layout
隐私设置页面 SHALL 适配 PC/移动端布局。

#### Scenario: PC layout
- **WHEN** 用户在 PC 端访问隐私设置
- **THEN** 列表最大宽度 640px 居中，按 7 个分组折叠面板展示

#### Scenario: Mobile layout
- **WHEN** 用户在移动端访问隐私设置
- **THEN** 全宽单列，Select 组件改为 ActionSheet 底部选择器

