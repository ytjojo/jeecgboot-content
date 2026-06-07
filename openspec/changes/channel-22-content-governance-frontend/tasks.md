## 1. API 层封装

- [ ] 1.1 创建 `src/api/content/channel/publish.ts`，封装发布相关 API（available、submit、result、scheduled CRUD、limit check）
- [ ] 1.2 创建 `src/api/content/channel/review.ts`，封装审核相关 API（list、executeReview[逐条action]、stats）
- [ ] 1.3 创建 `src/api/content/channel/governance.ts`，封装治理相关 API（content list、executeGovernance[统一入口+action]、edit-assist history、recycle-bin list、log list）
- [ ] 1.4 创建 `src/api/content/channel/announcement.ts`，封装公告相关 API（get、post、delete、preview、history、restore）
- [ ] 1.5 创建 `src/api/content/channel/addContent.ts`，封装添加内容相关 API（add、search、channels）

## 2. Store 层实现

- [ ] 2.1 创建 `src/store/modules/channelPublish.ts`，实现 useChannelPublishStore（selectedChannels、publishResult、scheduledTime、maxChannelCount、scheduledTaskList 及对应 actions）
- [ ] 2.2 创建 `src/store/modules/channelReview.ts`，实现 useChannelReviewStore（reviewList、filterParams、selectedIds、stats 及对应 actions）
- [ ] 2.3 创建 `src/store/modules/channelGovernance.ts`，实现 useChannelGovernanceStore（contentList、filterParams、recycleBinList、governanceLogList 及对应 actions）
- [ ] 2.4 编写 `channelPublishStore.test.ts` 测试文件
- [ ] 2.5 编写 `channelReviewStore.test.ts` 测试文件
- [ ] 2.6 编写 `channelGovernanceStore.test.ts` 测试文件

## 3. 频道选择与发布组件

- [ ] 3.1 创建 `src/views/channel/publish/ChannelSelector.vue`，实现频道选择组件（搜索防抖、分组展示、多选上限、权限状态、虚拟滚动、空状态）
- [ ] 3.2 编写 `ChannelSelector.test.ts` 测试文件
- [ ] 3.3 创建 `src/views/channel/publish/PublishResult.vue`，实现发布结果反馈组件（逐频道结果、失败重试、定时发布状态）
- [ ] 3.4 编写 `PublishResult.test.ts` 测试文件
- [ ] 3.5 创建 `src/views/channel/publish/ScheduledPublish.vue`，实现定时发布管理（时间选择器、任务列表、编辑/取消操作）

## 4. 发布权限与限额配置

- [ ] 4.1 创建 `src/views/channel/settings/PublishPermission.vue`，实现发布权限配置页（RadioGroup 四选一、限额配置表单、变更摘要确认）
- [ ] 4.2 编写 `PublishPermission.test.ts` 测试文件

## 5. 待审区管理

- [ ] 5.1 创建 `src/views/channel/components/RejectReasonModal.vue`，实现拒绝原因弹窗（预设原因标签、自定义原因必填 10 字校验）
- [ ] 5.2 编写 `RejectReasonModal.test.ts` 测试文件
- [ ] 5.3 创建 `src/views/channel/governance/ReviewQueue.vue`，实现待审区管理页（JVxeTable 列表、筛选、逐条/批量审核、超时标识、统计 badge、移动端卡片模式）
- [ ] 5.4 编写 `ReviewQueue.test.ts` 测试文件

## 6. 内容治理

- [x] 6.1 创建 `src/views/channel/components/GovernanceActionMenu.vue`，实现治理操作下拉菜单（权限控制菜单项）
- [x] 6.2 编写 `GovernanceActionMenu.test.ts` 测试文件
- [x] 6.3 创建 `src/views/channel/components/MoveChannelDialog.vue`，实现移出频道弹窗（目标频道选择、预期结果展示）
- [x] 6.4 编写 `MoveChannelDialog.test.ts` 测试文件
- [x] 6.5 创建 `src/views/channel/components/EditAssistDrawer.vue`，实现编辑协助抽屉（可编辑字段、修改原因必填、修订历史、作者通知）
- [x] 6.6 编写 `EditAssistDrawer.test.ts` 测试文件
- [x] 6.7 创建 `src/views/channel/governance/ContentManage.vue`，实现内容治理页（JVxeTable 列表、筛选排序、置顶/精华/删除/移出/编辑协助、批量操作、移动端卡片模式）
- [x] 6.8 编写 `ContentManage.test.ts` 测试文件

## 7. 回收站与治理日志

- [x] 7.1 创建 `src/views/channel/governance/RecycleBin.vue`，实现回收站页（Table 列表、单条/批量恢复、超期不可恢复、剩余天数倒计时）
- [x] 7.2 编写 `RecycleBin.test.ts` 测试文件
- [x] 7.3 创建 `src/views/channel/governance/GovernanceLog.vue`，实现治理日志页（Table 列表、操作类型筛选、操作对象跳转、180 天保留期）
- [x] 7.4 编写 `GovernanceLog.test.ts` 测试文件

## 8. 频道公告管理

- [x] 8.1 创建 `src/views/channel/governance/AnnouncementManage.vue`，实现公告管理页（Tinymce 编辑器、预览、发布/删除确认、历史版本管理、并发冲突处理、移动端 Tab 切换）
- [x] 8.2 编写 `AnnouncementManage.test.ts` 测试文件

## 9. 添加已发布内容到频道

- [ ] 9.1 创建 `src/views/channel/components/AddContentDialog.vue`，实现添加内容弹窗（内容搜索选择、目标频道选择、三种场景入口差异、不可添加内容提示、逐频道结果反馈）
- [ ] 9.2 编写 `AddContentDialog.test.ts` 测试文件

## 10. 治理后台容器与路由

- [ ] 10.1 创建 `src/views/channel/governance/index.vue`，实现治理后台 Tab 容器（待审区、内容管理、回收站、治理日志、公告管理 Tab 切换）
- [ ] 10.2 注册频道管理相关路由到前端路由配置

## 11. 埋点集成

- [ ] 11.1 在发布流程中集成发布相关埋点（channel_publish_submit、channel_publish_result、channel_publish_retry、publish_permission_save、scheduled_publish_trigger/manage）
- [ ] 11.2 在审核流程中集成审核相关埋点（review_approve、review_reject、review_timeout_alert）
- [ ] 11.3 在治理流程中集成治理相关埋点（governance_action、governance_undo、recycle_bin_restore）
- [ ] 11.4 在关键页面集成性能埋点（channel_selector_load、publish_submit_latency、review_list_load、governance_list_load）

## 12. 验证

- [ ] 12.1 运行全量单元测试，确保 100% 通过
- [ ] 12.2 检查所有页面 PC 端布局和交互
- [ ] 12.3 检查所有页面移动端响应式布局（< md 断点）
- [ ] 12.4 验证全部 API 对接正确性
- [ ] 12.5 Code Review 代码质量、命名规范、边界条件
