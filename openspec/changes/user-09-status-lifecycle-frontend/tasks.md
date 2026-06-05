## 1. API 封装与类型定义

- [x] 1.1 创建 `src/api/content/userStatus.ts`，封装 14 个 API 接口（getCurrentStatus、getUserStatus、getStatusList、getTransitions、changeUserStatus、releaseUser、batchReleaseUsers、getStatusHistory、getAuditLogList、getAuditLogDetail、getUserAuditLogs、exportAuditLogs、verifySecurity、sendVerifyCode）
- [x] 1.2 定义 TypeScript 类型：UserStatusEnum、UserStatusDetail、UserStatusHistoryItem、UserStatusQueryReq、UserStatusChangeReq、AuditLogQueryReq、AuditLogExportReq、LoginBlockedResponse

## 2. UserStatusStore (Pinia)

- [x] 2.1 创建 `src/store/modules/userStatus.ts`，实现 UserStatusStore（state、actions：fetchCurrentStatus、fetchUserStatus、fetchStatusHistory、fetchTransitions、changeStatus、releaseUser、batchRelease、verifySecurity、refreshStatus）
- [x] 2.2 编写 `userStatusStore.test.ts` 单元测试（actions mock、状态管理、缓存逻辑）

## 3. 通用组件

- [x] 3.1 创建 `src/components/jeecg/UserStatus/StatusTag.vue`（9 种状态颜色映射、Props: status/showTooltip）
- [x] 3.2 创建 `src/components/jeecg/UserStatus/StatusCountdown.vue`（dayjs 倒计时、visibilitychange 修正、归零回调 fetchCurrentStatus）
- [x] 3.3 创建 `src/components/jeecg/UserStatus/StatusChangeModal.vue`（调用 getTransitions 获取可转换状态、原因/期限表单、二次确认）
- [x] 3.4 创建 `src/components/jeecg/UserStatus/StatusReleaseModal.vue`（解禁原因表单、二次确认）
- [x] 3.5 创建 `src/components/jeecg/UserStatus/StatusHistoryDrawer.vue`（抽屉内嵌表格展示状态历史）
- [x] 3.6 创建 `src/components/jeecg/UserStatus/AuditLogDetailModal.vue`（Description 组件展示完整字段）
- [x] 3.7 编写组件单元测试：StatusTag.test.ts、StatusCountdown.test.ts

## 4. 管理后台页面

- [x] 4.1 创建 `src/views/content/user-status/manage/index.vue`（用户状态管理页：查询表单 + JVxeTable + 操作列）
- [x] 4.2 创建 `src/views/content/user-status/audit-log/index.vue`（审计日志页：筛选表单 + JVxeTable + 导出）
- [ ] 4.3 编写页面测试：UserStatusManage.test.vue、AuditLogList.test.vue

## 5. 用户端页面

- [x] 5.1 创建 `src/views/user/account-status/index.vue`（账号状态页：当前状态卡片 + StatusCountdown + 历史列表）
- [x] 5.2 创建 `src/views/login/blocked/index.vue`（登录拦截页：冻结/封禁提示 + 申诉/安全核验按钮）
- [x] 5.3 创建 `src/views/login/verify/index.vue`（安全核验页：手机验证码表单 + 60 秒倒计时）
- [ ] 5.4 编写页面测试：AccountStatus.test.vue、LoginBlocked.test.vue、SecurityVerify.test.vue

## 6. useStatusGuard composable

- [x] 6.1 创建 `src/composables/useStatusGuard.ts`（canPerformAction、showBlockModal）
- [ ] 6.2 集成到互动入口组件（CommentInput、MessageInput、PostEditor）
- [x] 6.3 编写 `useStatusGuard.test.ts` 单元测试

## 7. 路由与菜单配置

- [x] 7.1 添加管理后台路由：/content/user-status/manage、/content/user-status/audit-log
- [x] 7.2 添加用户端路由：/user/account-status、/login/blocked、/login/verify
- [x] 7.3 配置后台管理菜单项（用户状态管理、审计日志）
- [x] 7.4 配置路由守卫：/login/blocked 和 /login/verify 不需要登录态，无拦截数据时重定向到登录页

## 8. 响应拦截与错误处理

- [x] 8.1 在 HTTP 响应拦截器中添加 403 + 用户状态错误码处理（自动刷新 UserStatusStore、重定向拦截页）
- [x] 8.2 处理乐观锁冲突错误（提示"状态已变更，请刷新后重试"）
- [x] 8.3 处理非法状态转换错误（提示"当前状态不允许执行此操作"）

## 9. 与现有 Store 集成

- [x] 9.1 集成 useUserStore：登录成功后调用 fetchCurrentStatus()，FROZEN/BANNED 时重定向拦截页
- [x] 9.2 集成 usePermissionStore：状态变更后刷新权限码

## 10. 响应式适配

- [ ] 10.1 用户状态管理页移动端适配（表格改卡片列表、操作按钮改下拉菜单）
- [ ] 10.2 审计日志页移动端适配（表格精简列数、详情改全屏页面）
- [ ] 10.3 账号状态页移动端适配（卡片全宽、历史改时间轴）
- [ ] 10.4 登录拦截页和安全核验页移动端适配（居中卡片、按钮全宽）

## 11. 验证

- [ ] 11.1 运行全量单元测试，确保 100% 通过
- [ ] 11.2 Code Review：检查代码质量、命名规范、边界条件、安全性
- [ ] 11.3 功能验证：管理员状态查询/变更/解禁流程
- [ ] 11.4 功能验证：审计日志查询/筛选/导出流程
- [ ] 11.5 功能验证：用户端状态展示和倒计时
- [ ] 11.6 功能验证：登录拦截和安全核验流程
- [ ] 11.7 功能验证：互动拦截（评论/私信/动态）
- [ ] 11.8 响应式布局验证：移动端/平板/桌面端
