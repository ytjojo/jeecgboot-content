## ADDED Requirements

> **API 路径**:
> - 审核队列列表: `GET /jeecg-boot/api/v1/content/channel/review/list` (已存在)
> - 审核操作: `POST /jeecg-boot/api/v1/content/channel/review/action` (已存在)
> **Controller**: ChannelReviewController
> **前端封装**: `src/api/content/channel/index.ts` - `getReviewList()`, `reviewAction()`

### Requirement: 审核队列列表展示

审核队列页 SHALL 使用 Table 组件展示所有 PendingReview 状态的频道，列包含：频道名称、频道类型、提交人、提交时间、等待时长（超过 24h 标红）、操作。顶部显示待审核数量统计（如"待审核频道：12 个"）。超过 24 小时未处理的记录行背景色高亮。

#### Scenario: 查看审核队列
- **WHEN** 管理员进入审核队列页
- **THEN** 列表展示所有 PendingReview 频道，顶部显示待审核数量

#### Scenario: 审核超时高亮
- **WHEN** 有频道等待审核超过 24 小时
- **THEN** 该记录行背景色高亮，等待时长列显示"已超时"标红

#### Scenario: 无待审核频道
- **WHEN** 审核队列为空
- **THEN** 显示空状态"暂无待审核频道"

### Requirement: 审核详情展示

管理员 SHALL 能点击待审核频道打开 Drawer（宽度 560px）查看完整信息。如为编辑触发的审核，展示修改前后对比（diff 视图）：文本字段左右并排对比（删除红色删除线，新增绿色背景），图片字段左右并排展示，未修改字段折叠显示。对比区域顶部显示"共修改 N 个字段"摘要。

#### Scenario: 新建频道审核详情
- **WHEN** 管理员点击新建频道的审核详情
- **THEN** Drawer 展示频道完整信息（名称、简介、图标、封面、分类、隐私设置）

#### Scenario: 编辑触发的审核 diff 对比
- **WHEN** 管理员点击编辑触发的审核详情
- **THEN** Drawer 展示修改前后 diff 对比，显示"共修改 N 个字段"，文本字段左右对比，图片字段并排展示

### Requirement: 审核操作

审核详情 Drawer 底部 SHALL 提供三个操作按钮："通过"（绿色）、"拒绝"（红色）、"退回修改"（橙色）。通过无需填写原因直接执行，拒绝必须填写拒绝原因，退回修改必须填写修改建议。

#### Scenario: 通过审核
- **WHEN** 管理员点击"通过"
- **THEN** 频道状态变为 Active，Drawer 关闭，列表刷新，顶部提示"审核完成"

#### Scenario: 拒绝审核
- **WHEN** 管理员填写拒绝原因并点击"拒绝"
- **THEN** 频道状态变为 Rejected，Drawer 关闭，列表刷新

#### Scenario: 退回修改
- **WHEN** 管理员填写修改建议并点击"退回修改"
- **THEN** 频道状态退回，频道主收到退回修改通知

### Requirement: 并发审核防护

当两人同时打开同一审核详情时，后操作者提交 SHALL 收到提示"该频道已被审核"。

#### Scenario: 并发审核拦截
- **WHEN** 管理员 A 已完成审核，管理员 B 随后尝试审核同一频道
- **THEN** 管理员 B 收到提示"该频道已被审核"，操作被阻止
