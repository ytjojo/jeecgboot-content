## ADDED Requirements

### Requirement: Verification badge display
系统 SHALL 在昵称右侧展示认证标识 Badge，支持 7 种认证类型：个人认证（蓝色对勾）、企业认证（金色徽章）、达人认证（紫色星标）、官方认证（红色盾牌）、实名认证（灰色盾牌）、手机验证（绿色手机）、邮箱验证（绿色邮箱）。

#### Scenario: Display single badge
- **WHEN** 已认证用户（仅一种认证）的昵称被渲染
- **THEN** 昵称右侧紧跟显示对应认证类型的 Badge 图标，鼠标 hover 显示 Tooltip 简要说明认证类型

#### Scenario: Display multiple badges
- **WHEN** 用户拥有多种认证
- **THEN** 按优先级排列显示（官方 > 企业 > 达人 > 个人 > 实名 > 手机 > 邮箱），最多显示 2 个，超出部分显示 "+N" 灰色圆角徽标

#### Scenario: No badges
- **WHEN** 用户无任何认证
- **THEN** 昵称右侧不显示任何标识

### Requirement: Badge component encapsulation
系统 SHALL 封装 `VerificationBadge` 组件，Props 包含 `badges: BadgeItem[]`，组件内部处理排序和展示逻辑。

#### Scenario: Component receives badge data
- **WHEN** 父组件传入 badges 数组
- **THEN** VerificationBadge 组件按优先级排序后渲染 Badge 列表，处理折叠逻辑

### Requirement: Badge click detail modal
系统 SHALL 支持点击认证标识打开认证详情弹窗，展示认证类型名称、认证时间、认证说明。

#### Scenario: Open badge detail modal on PC
- **WHEN** 用户在 PC 端点击认证标识
- **THEN** 打开 Modal 弹窗（400px 宽），显示认证图标、类型名称、认证时间、认证说明

#### Scenario: Open badge detail drawer on mobile
- **WHEN** 用户在移动端点击认证标识
- **THEN** 打开全屏 Drawer 抽屉，显示认证详情

#### Scenario: Enterprise badge extra info
- **WHEN** 用户查看企业认证详情
- **THEN** 弹窗额外显示企业名称

#### Scenario: Influencer badge extra info
- **WHEN** 用户查看达人认证详情
- **THEN** 弹窗额外显示认证领域

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
