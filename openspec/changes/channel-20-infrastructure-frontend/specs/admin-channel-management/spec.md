## ADDED Requirements

> **API 路径**:
> - 创建系统频道: `POST /api/v1/admin/channels/create-system` (已存在)
> - 审核频道: `POST /api/v1/admin/channels/{id}/review` (已存在)
> - 频道列表查询: 需后端提供管理端列表接口（待确认）
> **Controller**: ChannelAdminController
> **前端封装**: `src/api/content/channel/index.ts` - `createSystemChannel()`, `reviewChannel()`

### Requirement: 后台频道列表展示

后台频道管理页 SHALL 使用 JVxeTable 表格展示所有频道，列包含：频道图标、频道名称、频道类型（Tag）、审核状态（Tag）、归属（用户/组织名）、分类、置顶权重（仅系统频道）、创建时间、操作。支持列排序（创建时间、置顶权重），分页默认每页 20 条。

#### Scenario: 管理员查看全量频道列表
- **WHEN** 管理员进入后台频道管理页
- **THEN** 表格展示所有频道，支持分页、排序

### Requirement: 后台频道筛选与搜索

后台频道管理页 SHALL 支持按频道类型、审核状态、归属分类、创建时间范围进行筛选，支持频道名称模糊搜索。筛选区默认展开，支持收起。

#### Scenario: 多维度筛选
- **WHEN** 管理员选择频道类型为"系统频道"并设置创建时间范围
- **THEN** 列表仅显示符合条件的频道

#### Scenario: 名称模糊搜索
- **WHEN** 管理员在搜索框输入频道名称关键词
- **THEN** 列表显示名称包含关键词的频道

### Requirement: 后台频道操作

后台频道管理页 SHALL 根据频道状态显示不同操作：所有状态显示"查看详情"，系统频道显示"编辑"按钮，DeleteCooling 状态显示"强制删除"按钮（需二次确认）。

#### Scenario: 查看频道详情
- **WHEN** 管理员点击"查看详情"
- **THEN** 打开 Drawer 展示频道完整信息

#### Scenario: 编辑系统频道
- **WHEN** 管理员点击系统频道的"编辑"按钮
- **THEN** 打开编辑表单，修改直接生效

#### Scenario: 强制删除冷静期频道
- **WHEN** 管理员点击 DeleteCooling 状态频道的"强制删除"并确认
- **THEN** 频道被立即永久删除

### Requirement: 批量审核操作

后台频道管理页 SHALL 支持批量审核：选中多条 PendingReview 记录后可批量通过或拒绝。

#### Scenario: 批量通过审核
- **WHEN** 管理员选中多条 PendingReview 记录并点击"批量通过"
- **THEN** 所有选中频道状态变为 Active

#### Scenario: 批量拒绝审核
- **WHEN** 管理员选中多条 PendingReview 记录并点击"批量拒绝"
- **THEN** 弹窗要求填写拒绝原因，确认后所有选中频道状态变为 Rejected
