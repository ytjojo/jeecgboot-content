## ADDED Requirements

### Requirement: ReviewQueueTable 待审区管理页面

系统 SHALL 提供待审区管理页面，支持待审内容列表展示、筛选、逐条/批量审核操作。

#### Scenario: 加载待审区列表
- **WHEN** 管理员进入待审区 Tab
- **THEN** 调用 `GET /jeecg-boot/api/v1/content/channel/review/list` 加载待审列表，展示标题、内容类型、提交者、提交时间、来源场景、命中规则，每行提供"通过"和"拒绝"按钮

#### Scenario: 筛选待审内容
- **WHEN** 管理员使用筛选栏（内容类型、提交者、提交时间、审核状态、超时状态）
- **THEN** 列表根据筛选条件刷新，搜索使用防抖

#### Scenario: 单条审核通过
- **WHEN** 管理员点击某条内容的"通过"按钮
- **THEN** 调用 `POST /content/channel/review`（body: `{reviewId, action: "APPROVE"}`），成功后从列表移除，展示"审核完成"toast

#### Scenario: 单条审核拒绝
- **WHEN** 管理员点击某条内容的"拒绝"按钮
- **THEN** 弹出 RejectReasonModal，管理员填写拒绝原因后调用 `POST /content/channel/review`（body: `{reviewId, action: "REJECT", rejectReason}`），成功后从列表移除

#### Scenario: 批量审核通过
- **WHEN** 管理员勾选多条内容后点击"批量通过"
- **THEN** 展示确认弹窗"确认通过选中的 {N} 条内容？通过后内容将在频道中展示。"，确认后逐条调用 API

#### Scenario: 批量审核拒绝
- **WHEN** 管理员勾选多条内容后点击"批量拒绝"
- **THEN** 展示确认弹窗"确认拒绝选中的 {N} 条内容？将向 {N} 位提交者发送拒绝通知。"，确认后逐条调用 API

#### Scenario: 超时内容高亮标识
- **WHEN** 待审内容提交时间距当前超过 24 小时
- **THEN** 该行展示红色边框或背景高亮标识

#### Scenario: 待审区空状态
- **WHEN** 待审区无待审核内容
- **THEN** 展示"暂无待审核内容"文案 + 引导返回频道管理页

#### Scenario: 移动端卡片列表模式
- **WHEN** 视口宽度 < md 断点
- **THEN** 表格切换为卡片列表，每张卡片展示标题、类型、提交者、时间，审核按钮固定在卡片底部；批量操作收纳到顶部菜单

---

### Requirement: RejectReasonModal 拒绝原因弹窗

系统 SHALL 提供 RejectReasonModal 组件，用于审核拒绝时填写拒绝原因。

#### Scenario: 展示拒绝原因表单
- **WHEN** 管理员点击"拒绝"触发弹窗
- **THEN** 展示预设原因快捷标签 + 自定义原因文本域（必填，最少 10 字）

#### Scenario: 校验拒绝原因
- **WHEN** 管理员提交拒绝但原因不足 10 字
- **THEN** 展示校验错误提示，阻止提交

#### Scenario: 提交拒绝
- **WHEN** 管理员填写原因后点击"确认拒绝"
- **THEN** 调用审核拒绝 API，成功后关闭弹窗，展示"已拒绝并通知提交者"toast

---

### Requirement: 待审区统计 badge

系统 SHALL 展示待审区统计 badge，包含待审总数和超时数。

#### Scenario: 展示待审统计 badge
- **WHEN** 管理员进入频道管理后台
- **THEN** 待审区 Tab 展示 badge，显示待审总数；超时数由 `GET /content/channel/review/stats` 接口返回（**后端待实现**），前端每 60 秒刷新一次

#### Scenario: 超时提醒通知
- **WHEN** 待审内容超过 24 小时未处理
- **THEN** 通过站内消息通知所有有审核权限的管理员，首次超时时提醒一次，之后每 12 小时重复提醒

---

### Requirement: useChannelReviewStore 审核状态管理

系统 SHALL 提供 `useChannelReviewStore` Pinia Store，管理待审区相关的前端状态。

#### Scenario: 管理待审列表和筛选
- **WHEN** 管理员进入待审区或修改筛选条件
- **THEN** Store 维护 `reviewList` 和 `filterParams`，提供 `fetchList` action

#### Scenario: 管理批量选中项
- **WHEN** 管理员勾选/取消勾选待审内容
- **THEN** Store 维护 `selectedIds` 数组

#### Scenario: 管理审核操作
- **WHEN** 管理员执行审核操作
- **THEN** Store 提供 `approve`、`reject`、`batchApprove`、`batchReject` action，操作完成后自动刷新列表和统计
