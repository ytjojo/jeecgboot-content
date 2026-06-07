## ADDED Requirements

### Requirement: 治理频道列表
系统 SHALL 展示频道治理列表，支持筛选和分页。

#### Scenario: 频道列表展示
- **WHEN** 平台运营进入频道治理后台
- **THEN** 展示频道名称、频道类型、当前状态、订阅数、最后活跃时间、创建时间、操作

#### Scenario: 筛选条件
- **WHEN** 用户设置筛选条件（频道名称、频道类型、生命周期状态）
- **THEN** 列表按条件过滤

#### Scenario: 状态标签展示
- **WHEN** 列表展示频道状态
- **THEN** 使用不同颜色 Tag 区分：Active（绿色）、ReadonlyFrozen（橙色）、Hidden（红色）、Archived（灰色）、Merged（蓝色）、Closed（黑色）

#### Scenario: 跳转详情
- **WHEN** 用户点击"查看详情"
- **THEN** 跳转到频道治理详情页

### Requirement: 频道治理详情
系统 SHALL 展示频道治理详情，包括基本信息、操作按钮、Tab 切换。

#### Scenario: 基本信息展示
- **WHEN** 用户进入频道治理详情页
- **THEN** 展示频道名称、频道类型、当前状态、创建时间、订阅数、内容数、最后活跃时间

#### Scenario: 操作按钮动态展示
- **WHEN** 频道处于不同状态
- **THEN** 根据状态动态展示可用操作按钮

#### Scenario: Tab 切换
- **WHEN** 用户切换 Tab
- **THEN** 展示近期内容、互动数据、历史处罚、审计日志、申诉记录

### Requirement: 生命周期操作
系统 SHALL 支持频道生命周期操作，包括冻结、解冻、隐藏、限制推荐、永久关闭、归档、合并。

#### Scenario: 冻结操作
- **WHEN** 用户点击"冻结"按钮
- **THEN** 弹出确认 Modal，输入原因后频道进入 ReadonlyFrozen 状态

#### Scenario: 解冻操作
- **WHEN** 用户点击"解冻"按钮
- **THEN** 弹出确认 Modal，输入原因后频道恢复原状态

#### Scenario: 限制推荐操作
- **WHEN** 用户点击"限制推荐"按钮
- **THEN** 弹出确认 Modal，输入原因后频道内容不进入公共推荐流

#### Scenario: 强制隐藏操作
- **WHEN** 用户点击"强制隐藏"按钮
- **THEN** 弹出确认 Modal，输入原因后频道对外不可见

#### Scenario: 永久关闭操作
- **WHEN** 用户点击"永久关闭"按钮
- **THEN** 弹出确认 Modal，需输入频道名称确认，输入原因后频道永久关闭

#### Scenario: 归档操作
- **WHEN** 用户点击"归档"按钮
- **THEN** 弹出确认 Modal，输入原因后频道从发现入口消失

#### Scenario: 合并操作
- **WHEN** 用户点击"合并"按钮
- **THEN** 弹出确认 Modal，选择目标频道，展示影响范围预览，确认后执行合并

### Requirement: 频道合并申请
系统 SHALL 支持频道合并申请流程，包含目标频道选择、影响范围预览和合并执行。

#### Scenario: 发起合并申请
- **WHEN** 频道主在治理详情页点击"合并"按钮
- **THEN** 弹出合并申请 Modal，需选择目标频道、填写合并原因，展示合并影响范围预览（订阅者迁移数、内容迁移数、历史数据处理方式）

#### Scenario: 合并校验
- **WHEN** 用户选择目标频道后
- **THEN** 调用合并校验接口，校验源频道和目标频道是否满足合并条件（同类型、非已关闭/已合并状态），不满足则展示具体原因

#### Scenario: 组织频道合并需审批
- **WHEN** 合并的频道为组织频道
- **THEN** 提交后进入待审核状态，需组织最高管理员审批后方可执行合并

#### Scenario: 不可合并状态拦截
- **WHEN** 源频道处于 Closed、Merged、Deleted 状态
- **THEN** 合并按钮置灰，hover 提示"当前频道状态不允许合并"

#### Scenario: 合并执行成功
- **WHEN** 合并操作执行成功
- **THEN** 源频道状态变为 Merged，展示目标频道入口链接，Toast 提示"合并成功"

### Requirement: 操作确认弹窗
系统 SHALL 对所有高风险操作提供二次确认弹窗。

#### Scenario: 确认弹窗展示
- **WHEN** 用户点击高风险操作按钮
- **THEN** 弹出确认 Modal，展示操作名称、影响范围说明、原因输入框

#### Scenario: 原因必填
- **WHEN** 用户确认操作
- **THEN** 原因输入框必填，最少 10 个字符

#### Scenario: 永久关闭额外确认
- **WHEN** 操作类型为永久关闭
- **THEN** 需额外输入频道名称进行确认

#### Scenario: 操作成功反馈
- **WHEN** 操作成功
- **THEN** Modal 关闭，页面刷新，展示 Toast 提示

#### Scenario: 操作失败反馈
- **WHEN** 操作失败
- **THEN** Modal 保持打开，展示错误提示

### Requirement: 操作按钮权限
系统 SHALL 根据当前状态和用户权限控制操作按钮展示。

#### Scenario: Active 状态操作按钮
- **WHEN** 频道状态为 Active
- **THEN** 展示冻结、限制推荐、强制隐藏、归档、合并、永久关闭按钮

#### Scenario: ReadonlyFrozen 状态操作按钮
- **WHEN** 频道状态为 ReadonlyFrozen
- **THEN** 展示解冻、限制推荐、强制隐藏、永久关闭按钮（注：恢复可见仅适用于 Hidden 状态，Frozen 状态通过解冻恢复）

#### Scenario: Hidden 状态操作按钮
- **WHEN** 频道状态为 Hidden
- **THEN** 展示恢复可见、永久关闭按钮

#### Scenario: Archived 状态操作按钮
- **WHEN** 频道状态为 Archived
- **THEN** 展示恢复运营按钮（需确认）

#### Scenario: Merged 状态操作按钮
- **WHEN** 频道状态为 Merged
- **THEN** 无操作按钮，展示目标频道入口

#### Scenario: Closed 状态操作按钮
- **WHEN** 频道状态为 Closed
- **THEN** 无操作按钮，不可恢复
