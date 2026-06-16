## 1. API 层与 Store 基础设施

- [x] 1.1 创建 `src/api/content/channel/governance.ts`：封装治理操作接口（`executeGovernance`，通过 action 参数支持 `'PIN'`/`'FEATURE'`/`'UNPIN'`/`'UNFEATURE'`/`'DELETE'`/`'MOVE'`）
- [x] 1.2 创建 `src/api/content/channel/announcement.ts`：封装公告接口（`getAnnouncement`、`saveAnnouncement`、`deleteAnnouncement`、`previewAnnouncement`、`getAnnouncementHistory`、`restoreAnnouncementVersion`）
- [x] 1.3 @成员查询：复用 `src/api/content/circle.ts` 的 `getMemberList`，在 `useMention` composable 中封装（`src/views/circle/composables/useMention.ts`）
- [x] 1.4 创建 `src/api/content/channelMember.ts`：封装申请审核与成员管理接口（`applyToJoin`、`getPendingApplications`、`approveApplications`、`rejectApplications`、`getMemberList`、`updateMemberRole`、`removeMembers`、`muteMember`、`unmuteMember`）
- [x] 1.5 创建 `src/api/support/report.ts`：封装举报接口（`createReport`、`withdrawReport`、`getReportList`、`getReportDetail`）
- [x] 1.6 创建 store 基础设施：`src/store/modules/channelReview.ts`（`useChannelReviewStore`，审核列表与统计）、`src/store/modules/channelGovernance.ts`（`useChannelGovernanceStore`，置顶/精华/删除/回收站/治理日志）、`src/store/modules/circle.ts`（`useCircleStore`，圈子状态/角色判断）

## 2. 内容置顶与精华

- [x] 2.1 创建 `GovernanceActionMenu.vue`（`src/views/channel/components/GovernanceActionMenu.vue`）：根据角色权限动态展示置顶/精华/删除/移动菜单项
- [x] 2.2 在 `ContentManage.vue` 中集成 `GovernanceActionMenu`，置顶内容排序逻辑（置顶在前，多条按 pinned_at 倒序）
- [x] 2.3 在内容列表卡片中添加置顶标识和精华标识（金色徽章）展示
- [x] 2.4 在内容详情页（`circle/Detail.vue`）集成 `GovernanceActionMenu`：创建 `CircleContentCard.vue`（置顶/精华 Tag 徽章 + GovernanceActionMenu + `executeGovernance` API 对接），Detail.vue feed Tab 中使用 `feedItems` 列表渲染
- [x] 2.5 实现置顶/精华操作后的即时列表更新（`fetchList` 自动刷新）

## 3. 圈子公告

- [x] 3.1 创建内联公告展示栏组件（`src/views/circle/components/CircleAnnouncementBar.vue`）：展示公告内容、展开/收起、过期自动隐藏、加载失败静默处理。4 tests。
- [x] 3.2 创建 `AnnouncementManage.vue`（`src/views/channel/governance/AnnouncementManage.vue`）：Tinymce 富文本编辑器（`defineAsyncComponent` 按需加载） + 表单校验
- [x] 3.3 实现公告发布逻辑：已有公告时弹出替换确认框，发布成功后更新列表
- [x] 3.4 实现公告删除逻辑：确认后调用 API，公告从列表移除
- [x] 3.5 在治理页（`src/views/channel/governance/index.vue`）集成"公告管理"Tab 页
- [x] 3.6 实现公告过期自动隐藏：`CircleAnnouncementBar.vue` 前端检查 `expireAt`，过期则隐藏。后端 `CircleAnnouncement` 已有 `expireAt` 字段支持。Circle 公告线已对齐；Channel 公告线（`ChannelAnnouncement`）仍待 expireAt 字段补齐。

## 4. @成员功能

- [x] 4.1 创建 `MentionMemberPicker` 组件（`src/views/circle/components/MentionMemberPicker.vue`）：浮层展示成员列表、搜索过滤、键盘导航、加载/错误/空状态，12 tests
- [x] 4.2 实现 @触发逻辑：`useMention` composable 中 `onInput(value, cursorPos)` 检测 `@` 字符输入，弹出浮层，成员列表懒加载+缓存
- [x] 4.3 实现成员搜索：`useMention` composable 中 `searchMembers(keyword)`，防抖 300ms，在缓存列表中按 nickname 模糊匹配过滤
- [x] 4.4 实现提及标记插入：`selectMember(member)` 返回 `@{userId:xxx}昵称` 格式文本；`MyComment.vue` 中 `insertMention` 智能替换光标前最后一个 `@` 及其后搜索文本
- [x] 4.5 实现 @提及内容解析渲染：`renderContent(content)` 正则匹配纯文本 `@{userId:xxx}昵称` 和富文本 `<span class="mention">` 两种格式，返回 `{type, content, userId?}` 片段数组
- [x] 4.6 在评论输入框（`src/views/circle/components/MyComment.vue`）中集成 `MentionMemberPicker`：支持 @ 触发、键盘导航、成员选择插入、提交

## 5. 加入申请审核

- [x] 5.1 创建审核队列页面 `ReviewQueue.vue`（`src/views/channel/governance/ReviewQueue.vue`）：Table + useTable，支持待处理/已处理/全部标签页切换
- [x] 5.2 在 `ReviewQueue.vue` 中内联展示申请信息、超时警告标识、操作按钮
- [x] 5.3 实现批准申请逻辑：确认框 + 调用 `approveApplications` API + 行移出列表 + Toast 反馈
- [x] 5.4 实现拒绝申请逻辑：`RejectReasonModal.vue` 拒绝原因输入弹窗（必填） + 调用 `rejectApplications` API + 行移出列表
- [x] 5.5 实现批量批准功能：勾选多条申请后批量调用批准 API
- [x] 5.6 实现超时提醒：超过 3 天未处理的申请显示橙色警告标识（`rowClassName` + stats）
- [x] 5.7 实现管理入口角标：治理页"待审区"Tab 显示待审核数量 Badge，通过 `useChannelReviewStore.fetchStats` 获取 `pendingCount`（`governance/index.vue`）
- [x] 5.8 实现移动端响应式（`ReviewQueue.vue`）：≤768px 切换为卡片列表 + 下拉筛选 + 底部固定操作栏（`position: fixed`，带阴影）

## 6. 内容举报处理

- [x] 6.1 创建举报提交弹窗 `ReportModal.vue`（`src/views/support/report/components/ReportModal.vue`）：举报原因单选 + 补充说明 + 表单校验
- [x] 6.2 实现举报提交逻辑：调用 `createReport` API，成功后 Toast 反馈
- [x] 6.3 实现重复举报检测：已举报内容显示重复提示（`DUPLICATE_REPORT` 错误处理）
- [x] 6.4 创建举报处理页面（`src/views/support/report/admin/ReportList.vue`）：Table + Tabs（待处理/已处理/已忽略），使用 `getReportList` API 分状态查询
- [x] 6.5 创建 `ReportCard` 组件（`src/views/support/report/admin/ReportCard.vue`）：移动端卡片展示举报信息、状态标签、操作按钮
- [x] 6.6 实现查看被举报内容详情：`ReportDetailDrawer.vue` Drawer 抽屉展示（举报编号/类型/状态/内容/说明/证据图片/时间）
- [x] 6.7 实现删除被举报内容逻辑：`ReportList.vue` 确认框（okType=danger） + 调用 `POST /api/v1/content/circle/report/{id}/delete-content?circleId=` → `deleteReportContent`（`src/api/content/circle/report.ts`），成功刷新列表
- [x] 6.8 实现忽略举报逻辑：`ReportList.vue` 确认框 + 调用 `POST /api/v1/content/circle/report/{id}/ignore?circleId=` → `ignoreReport`
- [x] 6.9 实现禁言用户逻辑：`ReportList.vue` 确认框（okType=danger） + 调用 `POST /api/v1/content/circle/report/{id}/mute?circleId=` → `muteReportUser`（注：后端 mute 端点不接受时长参数，禁言时长由后端决定）
- [x] 6.10 实现权限控制：举报处理页入口通过 `circleStore.isCreator/isModerator` 控制（`ReportList.vue` 仅管理员可见）
- [x] 6.11 实现移动端响应式：≤768px 表格切换为 `ReportCard` 卡片列表，操作按钮保留
