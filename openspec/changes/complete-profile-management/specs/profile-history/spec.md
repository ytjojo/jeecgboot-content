## ADDED Requirements

### Requirement: History record page
系统 SHALL 提供历史记录页面，支持查看曾用昵称和头像。入口为个人中心 → 编辑资料 → "历史记录"链接。页面包含标签页切换（昵称历史/头像历史）、当前值展示区、历史记录列表。

#### Scenario: Load nickname history
- **WHEN** 用户进入历史记录页并切换到"昵称历史" Tab
- **THEN** 页面顶部显示当前昵称（带绿色"当前"标签），下方倒序展示曾用昵称列表，每条包含昵称文本和修改时间

#### Scenario: Load avatar history
- **WHEN** 用户切换到"头像历史" Tab
- **THEN** 页面顶部显示当前头像缩略图（带蓝色边框和绿色"当前"标签），下方倒序展示曾用头像列表

#### Scenario: Empty history
- **WHEN** 用户无历史记录
- **THEN** 显示空状态插图和文案"暂无历史记录"

### Requirement: Restore historical value
系统 SHALL 支持恢复曾用昵称和头像，恢复操作等同于一次新的资料修改，受频率限制。

#### Scenario: Restore historical nickname
- **WHEN** 用户点击某条昵称记录的"恢复"按钮
- **THEN** 系统弹出确认框"确定恢复为 {旧昵称} 吗？"，确认后调用 `/content/user/profile/history/restore` 接口，成功提示"{昵称}已恢复"

#### Scenario: Restore historical avatar
- **WHEN** 用户点击某条头像记录的"恢复"按钮
- **THEN** 系统弹出确认框"确定恢复为该头像吗？"，确认后调用恢复接口，成功提示"头像已恢复"

#### Scenario: Restore nickname conflict
- **WHEN** 用户尝试恢复已被其他用户占用的昵称
- **THEN** 系统显示错误提示"该昵称已被使用"

#### Scenario: Restore hit frequency limit
- **WHEN** 用户当日修改次数已达上限后尝试恢复
- **THEN** "恢复"按钮置灰，Tooltip 提示"今日修改次数已达上限"

### Requirement: History record display limit
系统 SHALL 最多显示 20 条历史记录，按时间倒序排列。

#### Scenario: Display within limit
- **WHEN** 用户有 15 条历史记录
- **THEN** 列表按时间倒序展示全部 15 条记录

#### Scenario: Exceed display limit
- **WHEN** 用户有超过 20 条历史记录
- **THEN** 仅显示最新 20 条，列表底部说明"最多保留 20 条记录，保留期限 180 天"

### Requirement: History page loading and restoring states
系统 SHALL 在数据加载和恢复操作中显示对应的加载状态。

#### Scenario: Loading state
- **WHEN** 历史记录数据加载中
- **THEN** 列表显示骨架屏

#### Scenario: Restoring state
- **WHEN** 用户点击"恢复"按钮后请求进行中
- **THEN** 对应记录的"恢复"按钮显示 loading 状态

### Requirement: History page responsive layout
历史记录页面 SHALL 适配 PC/移动端布局。

#### Scenario: PC layout
- **WHEN** 用户在 PC 端访问历史记录
- **THEN** 列表最大宽度 640px 居中，昵称/头像内容与时间左右排列

#### Scenario: Mobile layout
- **WHEN** 用户在移动端访问历史记录
- **THEN** 全宽布局，内容与时间上下排列
