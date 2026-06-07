> **跨文档索引:** 本文件为实现任务清单，与 `plan.md` 中的详细步骤一一对应。
> - 任务 1 (API 层) ↔ plan.md Task 1-5
> - 任务 2 (隐私设置) ↔ plan.md Task 6-9
> - 任务 3 (订阅功能) ↔ plan.md Task 10-14
> - 任务 4 (申请加入) ↔ plan.md Task 15-19
> - 任务 5 (成员管理) ↔ plan.md Task 20-25
> - 任务 6 (黑名单/治理日志) ↔ plan.md Task 26-30
> - 任务 7 (上下文/路由) ↔ plan.md Task 31-34
> - 任务 8 (响应式) ↔ plan.md Task 35-38
> - 任务 9-10 (验证/测试) ↔ plan.md Test Strategy
>
> 详细代码示例、接口定义和实现说明请查阅 `plan.md`；设计约束和边界条件请查阅 `design.md`。

## 1. API 层与 Composable 基础

- [x] 1.1 创建 `src/api/content/channelSubscription.ts` — 订阅/取消订阅/查询状态/订阅列表/分组CRUD/提醒设置/移动分组 API
- [x] 1.2 创建 `src/api/content/channelMember.ts` — 提交申请/查询申请状态/待审列表/批准/拒绝/成员列表/修改角色/移除/禁言/解除禁言 API
- [x] 1.3 创建 `src/api/content/channelBlacklist.ts` — 加入黑名单/移出黑名单/黑名单列表 API
- [x] 1.4 创建 `src/api/content/channelInvite.ts` — 创建邀请/邀请列表/撤销邀请/使用邀请加入 API
- [x] 1.5 创建 `src/api/content/channelPrivacy.ts` — 更新隐私/更新加入方式 API
- [x] 1.6 创建 `src/api/content/channelGovernance.ts` — 治理日志列表 API
- [x] 1.7 创建 `src/composables/useChannelContext.ts` — 频道上下文 composable（状态隔离、loadContext、resetContext、权限 computed、provide/inject）
- [x] 1.8 创建 `src/hooks/web/useChannelOperation.ts` — 乐观更新封装 hook（订阅/申请操作的乐观更新与回滚）

## 2. 频道隐私设置页面

- [x] 2.1 创建 `src/views/channel/settings/PrivacySettings.vue` — 隐私设置组件（Radio.Group 公开/私有，系统频道锁定，skeleton 加载态）
- [x] 2.2 实现隐私变更影响确认弹窗（公开→私有/私有→公开两种 Modal，红色确认按钮）
- [x] 2.3 创建 `src/views/channel/settings/JoinMethodSettings.vue` — 加入方式配置组件（自由/审核/邀请三种模式，动态配置项）
- [x] 2.4 实现审核加入配置项（Switch 允许再次申请 + InputNumber 间隔小时数）
- [x] 2.5 创建 `src/views/channel/settings/InviteDrawer.vue` — 邀请创建 Drawer 表单（邀请类型、有效期、可用次数、复制按钮）
- [x] 2.6 实现邀请列表表格（Table：邀请码/链接、类型、有效期、已用次数/总次数、状态标签、操作列）

## 3. 订阅功能

- [x] 3.1 创建 `src/views/channel/components/SubscribeButton.vue` — 订阅按钮状态机（6种状态：未订阅、已订阅、待审核、冷却期、黑名单、已禁言）
- [x] 3.2 实现乐观更新逻辑（点击立即更新状态，失败回滚，成功后更新 useChannelContext 和使缓存失效）
- [x] 3.3 实现取消订阅二次确认 Modal
- [x] 3.4 创建 `src/views/channel/subscription/SubscriptionCard.vue` — 订阅频道卡片组件（头像、名称、摘要、来源标签、提醒开关、取消订阅）
- [x] 3.5 创建 `src/views/channel/subscription/SubscriptionList.vue` — 订阅列表页面（分组标签页、搜索过滤、卡片列表、新建分组 Modal）
- [x] 3.6 实现订阅列表空状态（"暂无订阅频道" + "去发现频道"按钮）

## 4. 申请加入与待审队列

- [x] 4.1 创建 `src/views/channel/components/JoinApplyModal.vue` — 申请加入 Modal（TextArea 10-200字验证、实时字数统计）
- [x] 4.2 实现申请状态按钮逻辑（已有未处理申请→待审核、已拒绝→重新申请、冷却期→剩余天数、黑名单→无法加入）
- [x] 4.3 创建 `src/views/channel/members/PendingApplications.vue` — 待审队列页面（Table、超时高亮、时间范围筛选）
- [x] 4.4 实现单条批准/拒绝操作（批准无确认，拒绝需填写原因 Modal）
- [x] 4.5 实现批量批准/拒绝操作（Checkbox 选择、批量接口调用、结果 Modal 展示成功/失败数量和原因）

## 5. 成员管理页面

- [x] 5.1 创建 `src/views/channel/members/MemberList.vue` — 成员列表页面（JVxeTable、角色标签颜色、治理状态标签、操作列 Dropdown）
- [x] 5.2 实现角色筛选（Select：全部/频道主/管理员/内容编辑/普通成员）和搜索（Input.Search 300ms 防抖）
- [x] 5.3 创建 `src/views/channel/members/RoleAssignModal.vue` — 角色分配确认 Modal
- [x] 5.4 创建 `src/views/channel/members/RemoveMemberModal.vue` — 移除成员确认 Modal（原因必填、冷却期提示）
- [x] 5.5 创建 `src/views/channel/members/MuteModal.vue` — 禁言 Modal（时长选择、原因必填）
- [x] 5.6 实现批量移除和批量禁言操作
- [x] 5.7 实现操作菜单权限控制（频道主→全部操作、管理员→移除/禁言/黑名单、编辑/成员→无操作）

## 6. 黑名单与治理日志

- [x] 6.1 创建 `src/views/channel/blacklist/BlacklistPage.vue` — 黑名单管理页面（Table、移出确认 Modal）
- [x] 6.2 创建 `src/views/channel/governance/GovernanceLog.vue` — 治理日志页面（Table、操作类型标签颜色、筛选区）
- [x] 6.3 创建 `src/views/channel/governance/GovernanceDetailDrawer.vue` — 治理详情 Drawer（Description 组件展示操作前后状态对比）
- [x] 6.4 实现从成员列表加入黑名单功能（确认 Modal、原因必填）

## 7. 频道上下文 Provider 与路由集成

- [x] 7.1 创建 `src/views/channel/components/ChannelContextProvider.vue` — 频道上下文 Provider 组件（provide/inject 封装）
- [x] 7.2 集成路由守卫（beforeRouteUpdate 中调用 resetContext + loadContext）
- [x] 7.3 注册新增路由（成员管理、待审队列、黑名单、治理日志、订阅列表）

## 8. 响应式适配

- [x] 8.1 移动端表格转卡片列表（待审队列、成员列表、黑名单、治理日志、邀请列表）
- [x] 8.2 移动端筛选区折叠为 Drawer（成员管理、治理日志）
- [x] 8.3 移动端批量操作降级为多选列表 + 底部固定操作栏
- [x] 8.4 移动端触控区域保证 44x44px，弹窗/抽屉从底部弹出

## 9. 验证

- [ ] 9.1 运行单元测试：useChannelContext 状态隔离、权限 computed、resetContext
- [ ] 9.2 运行组件测试：SubscribeButton 状态机、PrivacySettings 切换、JoinApplyModal 表单验证
- [ ] 9.3 运行集成测试：订阅流程、申请加入流程、成员管理流程、治理操作流程
- [ ] 9.4 响应式验证：桌面端和移动端各页面核心操作可用
- [ ] 9.5 性能验证：页面首屏 < 2s，核心操作 P95 <= 500ms

## 10. 测试编写

> **关联:** 以下 11 个测试文件对应 `design.md` Test Strategy 中列出的全部测试文件。每个任务需编写完整的测试代码，而非仅运行已有测试。

- [ ] 10.1 编写 `useChannelContext.test.ts` — 状态隔离（不同 channelId 独立）、权限 computed、resetContext、loadContext 异常处理
- [ ] 10.2 编写 `useChannelOperation.test.ts` — 乐观更新成功/失败回滚、并发操作处理、缓存失效触发
- [ ] 10.3 编写 `SubscribeButton.test.ts` — 6 种状态渲染、点击订阅/取消、乐观更新视觉反馈、错误态展示
- [ ] 10.4 编写 `JoinApplyModal.test.ts` — 表单验证（10-200字）、字数统计、提交成功/失败、关闭重置
- [ ] 10.5 编写 `SubscriptionList.test.ts` — 分组标签页切换、搜索过滤、卡片列表渲染、空状态、新建分组 Modal
- [ ] 10.6 编写 `PendingApplications.test.ts` — 待审列表渲染、超时高亮、单条批准/拒绝、批量操作、时间范围筛选
- [ ] 10.7 编写 `MemberList.test.ts` — 成员列表渲染、角色筛选、搜索防抖、操作菜单权限控制、批量操作
- [ ] 10.8 编写 `MuteModal.test.ts` — 时长选择、原因必填验证、提交确认、取消关闭
- [ ] 10.9 编写 `GovernanceDetailDrawer.test.ts` — Drawer 打开/关闭、操作前后状态对比渲染、Description 组件数据填充
- [ ] 10.10 编写 `ChannelPrivacySettings.test.ts` — 公开/私有切换、系统频道锁定、变更影响确认弹窗、skeleton 加载态
- [ ] 10.11 编写 `SubscriptionCard.test.ts` — 卡片信息渲染、提醒开关切换、取消订阅确认、来源标签展示
