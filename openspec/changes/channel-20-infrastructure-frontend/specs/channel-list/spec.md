## ADDED Requirements

> **API 路径**: `GET /api/v1/channels/list` (待后端实现)
> **Controller**: ChannelController
> **前端封装**: `src/api/content/channel/index.ts` - `getChannelList()`

### Requirement: 我的频道列表展示

用户 SHALL 能在"我的频道"页面查看自己创建的所有频道列表，使用 JVxeTable 表格展示，列包含：频道图标、频道名称、频道类型（Tag 标签）、审核状态（Tag 标签）、创建时间（支持排序）、操作。创建时间列默认倒序排列。

#### Scenario: 用户查看频道列表
- **WHEN** 用户进入"我的频道"页面
- **THEN** 表格展示用户创建的所有频道，按创建时间倒序排列，每页 10 条

#### Scenario: 审核状态排序
- **WHEN** 用户点击审核状态列排序
- **THEN** PendingReview 和 Rejected 状态置顶显示，因为这些需要用户关注

### Requirement: 频道列表筛选

系统 SHALL 支持按频道类型和审核状态进行下拉筛选。

#### Scenario: 按频道类型筛选
- **WHEN** 用户选择频道类型为"个人频道"
- **THEN** 列表仅显示个人频道类型的记录

#### Scenario: 按审核状态筛选
- **WHEN** 用户选择审核状态为"已拒绝"
- **THEN** 列表仅显示 Rejected 状态的频道

### Requirement: 频道列表操作

系统 SHALL 根据频道审核状态显示不同操作按钮：Active 显示"管理"、PendingReview 显示"查看详情"、Rejected 显示"重新提交"、DeleteCooling 显示"撤销删除"。

#### Scenario: Active 状态频道操作
- **WHEN** 用户点击 Active 状态频道的"管理"按钮
- **THEN** 跳转到频道管理页 `/content/channel/manage/:id`

#### Scenario: Rejected 状态频道操作
- **WHEN** 用户点击 Rejected 状态频道的"重新提交"按钮
- **THEN** 跳转到频道创建页，预填之前的信息供用户修改后重新提交

#### Scenario: DeleteCooling 状态频道操作
- **WHEN** 用户点击 DeleteCooling 状态频道的"撤销删除"按钮
- **THEN** 弹出确认弹窗，确认后频道恢复为 Active 状态

### Requirement: 频道列表空状态

当用户没有任何频道时，系统 SHALL 显示空状态插图和"创建你的第一个频道"引导按钮。

#### Scenario: 无频道时展示空状态
- **WHEN** 用户进入"我的频道"页面且没有任何频道
- **THEN** 显示空状态插图和"创建你的第一个频道"按钮，点击跳转到创建页
