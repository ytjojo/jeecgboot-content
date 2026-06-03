## ADDED Requirements

### Requirement: Verification badge display driven by visualStyleKey
系统 SHALL 在昵称右侧展示认证标识 Badge，使用后端 `visualStyleKey` 字段映射图标/颜色，对接 `GET /content/user/profile/badge/list?userId=X`。

#### Scenario: Display badges by visualStyleKey
- **WHEN** 父组件传入 `ContentUserVerificationBadgeVO[]` 列表
- **THEN** VerificationBadge 组件按优先级 OFFICIAL > ENTERPRISE > CREATOR > INDIVIDUAL > REAL_NAME > MOBILE > EMAIL 排序后渲染，每个 Badge 按 `visualStyleKey` 查找图标与颜色

#### Scenario: visualStyleKey dictionary coverage
- **WHEN** 后端下发的 `visualStyleKey` 为 `INDIVIDUAL` / `ENTERPRISE` / `CREATOR` / `OFFICIAL` / `REAL_NAME` / `MOBILE` / `EMAIL`
- **THEN** 前端维护的字典命中对应图标（对勾/徽章/星标/盾牌/手机/邮箱）和颜色

#### Scenario: Unknown visualStyleKey falls back to DEFAULT
- **WHEN** 后端下发的 `visualStyleKey` 不在已知字典中
- **THEN** 前端使用 `DEFAULT` 兜底样式（灰色对勾）

#### Scenario: No badges
- **WHEN** 后端返回的 badges 数组为空
- **THEN** 昵称右侧不显示任何标识

### Requirement: Badge component encapsulation
系统 SHALL 封装 `VerificationBadge` 组件，Props 包含 `badges: ContentUserVerificationBadgeVO[]`，组件内部处理排序、折叠、`visualStyleKey` 字典查找、Tooltip 和详情弹窗。

#### Scenario: Component receives badge data
- **WHEN** 父组件传入 badges 数组
- **THEN** VerificationBadge 组件按优先级排序后渲染 Badge 列表，处理折叠逻辑

### Requirement: Badge click detail modal
系统 SHALL 支持点击认证标识打开认证详情弹窗，调用 `GET /content/user/profile/badge/detail?badgeId=Y` 加载详情。

#### Scenario: Open badge detail modal on PC
- **WHEN** 用户在 PC 端点击认证标识
- **THEN** 打开 Modal 弹窗（400px 宽），显示认证类型名称、认证时间、认证说明

#### Scenario: Open badge detail drawer on mobile
- **WHEN** 用户在移动端点击认证标识
- **THEN** 打开全屏 Drawer 抽屉，显示认证详情

#### Scenario: Enterprise badge extra info
- **WHEN** 用户查看企业认证详情
- **THEN** 弹窗额外显示企业名称（来自 `description` 字段解析）

#### Scenario: Influencer badge extra info
- **WHEN** 用户查看达人认证详情
- **THEN** 弹窗额外显示认证领域（来自 `description` 字段解析）

### Requirement: Badge expansion interaction
系统 SHALL 支持点击 "+N" 徽标展开显示所有认证标识。

#### Scenario: Expand collapsed badges
- **WHEN** 用户点击 "+N" 徽标
- **THEN** 展开显示所有认证标识，点击空白处或选择标识后收起

### Requirement: Nickname truncation with badge
系统 SHALL 在昵称过长时截断显示，认证标识仍紧跟截断后的昵称，不换行。

#### Scenario: Long nickname on PC
- **WHEN** 用户昵称超过 200px 显示宽度
- **THEN** 昵称显示省略号，hover 时 Tooltip 显示完整昵称，认证标识紧跟显示

#### Scenario: Long nickname on mobile
- **WHEN** 用户昵称超过 120px 显示宽度
- **THEN** 昵称显示省略号，长按显示完整昵称，认证标识紧跟显示

### Requirement: Badge independent from review status
认证标识 SHALL 独立于昵称审核状态展示，昵称审核中时认证标识仍正常显示。

#### Scenario: Badge visible during nickname review
- **WHEN** 用户昵称处于审核中状态
- **THEN** 认证标识仍正常显示在昵称右侧，不随审核状态变化
