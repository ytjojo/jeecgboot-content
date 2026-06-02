## ADDED Requirements

### Requirement: ContentGovernance 内容治理页面

系统 SHALL 提供内容治理页面，支持频道内内容的搜索、筛选、排序和治理操作（置顶、精华、删除、移出、编辑协助）。

#### Scenario: 加载频道内容列表
- **WHEN** 管理员进入内容管理 Tab
- **THEN** 调用 `/api/channel/governance/content/list` 加载内容列表，展示标题、类型、作者、发布时间、状态，每行提供治理操作按钮

#### Scenario: 筛选和排序内容
- **WHEN** 管理员使用筛选栏（内容类型、状态、作者、时间范围）或切换排序（最新发布/最多点赞）
- **THEN** 列表根据条件刷新

#### Scenario: 置顶/取消置顶内容
- **WHEN** 管理员点击"置顶"
- **THEN** 直接调用 `/api/channel/governance/pine` API，内容移至列表顶部并展示置顶标识；再次点击取消置顶

#### Scenario: 标记/取消精华
- **WHEN** 管理员点击"标记精华"
- **THEN** 直接调用 `/api/channel/governance/feature` API，内容添加精华标识；再次点击取消精华

#### Scenario: 删除内容到回收站
- **WHEN** 管理员点击"删除"
- **THEN** 弹出确认弹窗"确认将《{标题}》从频道删除？内容将进入回收站，30天内可恢复。"，可选填写删除原因，勾选"通知作者"（默认勾选），确认后调用 API

#### Scenario: 移出频道
- **WHEN** 管理员点击"移出频道"
- **THEN** 弹出 MoveChannelDialog，选择目标频道后展示预期结果（直接展示/进入待审），确认后调用 `/api/channel/governance/move`

#### Scenario: 编辑协助
- **WHEN** 管理员点击"编辑协助"
- **THEN** 打开 EditAssistDrawer（右侧滑出），展示可编辑字段（标题、标签、摘要）和修订历史

#### Scenario: 批量治理操作
- **WHEN** 管理员勾选多条内容
- **THEN** 底部固定操作栏展示批量操作按钮（批量删除、批量置顶、批量精华）

#### Scenario: 移动端卡片列表模式
- **WHEN** 视口宽度 < md 断点
- **THEN** 表格切换为卡片列表，治理操作收纳到 Dropdown "更多"菜单；筛选区折叠为"筛选"按钮触发的抽屉

---

### Requirement: GovernanceActionMenu 治理操作菜单

系统 SHALL 提供 GovernanceActionMenu 组件，以 Dropdown 形式展示治理操作选项。

#### Scenario: 展示操作菜单
- **WHEN** 管理员点击内容行的"更多"按钮
- **THEN** 展示下拉菜单，包含：置顶/取消置顶、标记精华/取消精华、移出频道、编辑协助、删除

#### Scenario: 权限控制菜单项
- **WHEN** 当前用户角色不具有某操作权限
- **THEN** 对应菜单项隐藏或置灰

---

### Requirement: MoveChannelDialog 移出频道弹窗

系统 SHALL 提供 MoveChannelDialog 组件，用于选择内容移出的目标频道。

#### Scenario: 展示目标频道选择器
- **WHEN** 管理员触发移出频道操作
- **THEN** 弹窗展示简化版 ChannelSelector，管理员选择目标频道

#### Scenario: 展示预期结果
- **WHEN** 管理员选择目标频道
- **THEN** 展示预期结果："将进入目标频道待审区"或"将直接展示"

#### Scenario: 确认移出
- **WHEN** 管理员确认移出
- **THEN** 调用 `/api/channel/governance/move` API，成功后从当前列表移除，toast "已移出"

---

### Requirement: EditAssistDrawer 编辑协助抽屉

系统 SHALL 提供 EditAssistDrawer 组件，用于管理员协助修正频道内容。

#### Scenario: 展示编辑表单
- **WHEN** 管理员触发编辑协助
- **THEN** 右侧滑出 Drawer，顶部展示原作者信息和内容标题（只读），中部展示可编辑字段表单（标题、标签、摘要），底部展示修订历史列表

#### Scenario: 填写修改原因并保存
- **WHEN** 管理员修改字段后点击保存
- **THEN** 必填"修改原因"，保存后调用 `/api/channel/governance/edit-assist` API

#### Scenario: 保存后通知作者
- **WHEN** 编辑协助保存成功
- **THEN** 自动向原作者发送站内消息通知，通知内容包含修改人、修改字段、修改前后对比（diff 格式）、修改原因，通知中附带"申诉"链接

#### Scenario: 查看修订历史
- **WHEN** 管理员在 EditAssistDrawer 底部查看修订历史
- **THEN** 调用 `/api/channel/governance/edit-assist/history/{contentId}` 展示修订历史列表（修改人、时间、字段、前后摘要）

#### Scenario: 作者查看修订记录
- **WHEN** 原作者在内容详情页查看
- **THEN** 展示"修订记录"入口，可查看针对该内容的所有编辑协助记录

---

### Requirement: RecycleBinTable 回收站页面

系统 SHALL 提供回收站页面，展示已删除内容并支持恢复操作。

#### Scenario: 加载回收站列表
- **WHEN** 管理员进入回收站 Tab
- **THEN** 调用 `/api/channel/governance/recycle-bin/list` 加载列表，展示标题、类型、原作者、删除人、删除时间、删除原因、剩余天数

#### Scenario: 恢复单条内容
- **WHEN** 管理员点击"恢复"
- **THEN** 展示确认弹窗，确认后调用 `/api/channel/governance/recycle-bin/restore` API

#### Scenario: 批量恢复
- **WHEN** 管理员勾选多条后点击"批量恢复"
- **THEN** 逐条调用恢复 API，逐条展示结果

#### Scenario: 超过 30 天不可恢复
- **WHEN** 已删除内容超过 30 天保留期
- **THEN** 该行置灰展示，"恢复"按钮不可点击，标注"已过保留期"

#### Scenario: 剩余天数倒计时
- **WHEN** 回收站列表展示
- **THEN** 每条内容展示剩余天数倒计时

---

### Requirement: GovernanceLogTable 治理日志页面

系统 SHALL 提供治理日志页面，展示所有治理操作记录。

#### Scenario: 加载治理日志列表
- **WHEN** 管理员进入治理日志 Tab
- **THEN** 调用 `/api/channel/governance/log/list` 加载列表，展示时间、操作者、操作类型、操作对象、结果、原因/备注

#### Scenario: 按操作类型筛选
- **WHEN** 管理员选择操作类型筛选（置顶/精华/删除/恢复/移出/编辑协助/公告变更）
- **THEN** 列表根据筛选条件刷新

#### Scenario: 点击操作对象跳转
- **WHEN** 管理员点击操作对象名称
- **THEN** 跳转到对应内容详情页

#### Scenario: 日志保留期
- **WHEN** 操作记录超过 180 天
- **THEN** 不再展示

---

### Requirement: useChannelGovernanceStore 治理状态管理

系统 SHALL 提供 `useChannelGovernanceStore` Pinia Store，管理内容治理相关的前端状态。

#### Scenario: 管理内容列表和筛选
- **WHEN** 管理员进入内容治理页或修改筛选条件
- **THEN** Store 维护 `contentList` 和 `filterParams`，提供 `fetchList` action

#### Scenario: 管理治理操作
- **WHEN** 管理员执行治理操作
- **THEN** Store 提供 `pin`、`feature`、`deleteContent`、`moveContent`、`editAssist`、`restore` action

#### Scenario: 管理回收站和日志
- **WHEN** 管理员查看回收站或治理日志
- **THEN** Store 维护 `recycleBinList` 和 `governanceLogList`
