# 用户状态与生命周期治理 — 前端实现计划

> **For agentic workers:** Use superpowers:subagent-driven-development to implement this plan task-by-task.

**Goal:** 实现 EPIC-09 用户状态生命周期管理的全部前端能力，包括管理员后台（状态管理、审计日志）、用户端（状态展示、登录拦截、安全核验、互动拦截）。

**Architecture:** 基于 Vue 3 + TypeScript + Pinia 架构，新增 UserStatusStore 缓存用户状态，useStatusGuard composable 统一互动拦截。API 层封装 14 个接口，复用 JVxeTable/Form/Modal/Drawer 等现有组件。登录拦截采用不签发 token 方案，互动拦截采用前端 UX 优化 + 后端安全兜底双重机制。

**Tech Stack:** Vue 3, TypeScript, Vite 6, Ant Design Vue 4, Pinia, dayjs

---

## Task 1: API 封装与类型定义

- [ ] **Step 1.1:** 创建 `jeecgboot-vue3/src/api/content/userStatus.ts`，定义 Api 枚举（14 个接口路径）和所有请求/响应类型（UserStatusEnum、UserStatusDetail、UserStatusHistoryItem、UserStatusQueryReq、UserStatusChangeReq、AuditLogQueryReq、AuditLogExportReq、LoginBlockedResponse）
- [ ] **Step 1.2:** 实现所有 API 封装函数（getCurrentStatus、getUserStatus、getStatusList、getTransitions、changeUserStatus、releaseUser、batchReleaseUsers、getStatusHistory、getAuditLogList、getAuditLogDetail、getUserAuditLogs、exportAuditLogs、verifySecurity、sendVerifyCode），使用 defHttp
- [ ] **Step 1.3:** 编写 `userStatus.test.ts`，mock HTTP 请求验证每个 API 函数的 URL、方法、参数正确性
- [ ] **Step 1.4:** 运行测试，确保 API 封装测试 100% 通过

**Commit:** `feat(user-status): add API layer with 14 endpoints and TypeScript types`

---

## Task 2: UserStatusStore (Pinia)

- [ ] **Step 2.1:** 创建 `jeecgboot-vue3/src/store/modules/userStatus.ts`，定义 UserStatusState 接口和初始状态
- [ ] **Step 2.2:** 实现 fetchCurrentStatus、fetchUserStatus、fetchStatusHistory、fetchTransitions actions
- [ ] **Step 2.3:** 实现 changeStatus、releaseUser、batchRelease、verifySecurity、refreshStatus actions
- [ ] **Step 2.4:** 编写 `userStatusStore.test.ts`，测试 actions 的 API 调用、状态更新、错误处理
- [ ] **Step 2.5:** 运行测试，确保 Store 测试 100% 通过

**Commit:** `feat(user-status): add UserStatusStore with Pinia`

---

## Task 3: 通用组件 — StatusTag 和 StatusCountdown

- [ ] **Step 3.1:** 创建 `jeecgboot-vue3/src/components/jeecg/UserStatus/StatusTag.vue`，实现 9 种状态颜色映射（Props: status, showTooltip）
- [ ] **Step 3.2:** 创建 `jeecgboot-vue3/src/components/jeecg/UserStatus/StatusCountdown.vue`，实现 dayjs 倒计时、visibilitychange 修正、归零回调 fetchCurrentStatus
- [ ] **Step 3.3:** 编写 `StatusTag.test.vue`，验证 9 种状态的颜色和文本渲染
- [ ] **Step 3.4:** 编写 `StatusCountdown.test.vue`，验证倒计时逻辑、visibilitychange 修正、归零回调
- [ ] **Step 3.5:** 运行测试，确保组件测试 100% 通过

**Commit:** `feat(user-status): add StatusTag and StatusCountdown components`

---

## Task 4: 通用组件 — 弹窗和抽屉

- [ ] **Step 4.1:** 创建 `jeecgboot-vue3/src/components/jeecg/UserStatus/StatusChangeModal.vue`，实现 getTransitions 调用、原因/期限表单、二次确认
- [ ] **Step 4.2:** 创建 `jeecgboot-vue3/src/components/jeecg/UserStatus/StatusReleaseModal.vue`，实现解禁原因表单、二次确认
- [ ] **Step 4.3:** 创建 `jeecgboot-vue3/src/components/jeecg/UserStatus/StatusHistoryDrawer.vue`，实现抽屉内嵌表格展示状态历史
- [ ] **Step 4.4:** 创建 `jeecgboot-vue3/src/components/jeecg/UserStatus/AuditLogDetailModal.vue`，实现 Description 组件展示完整字段
- [ ] **Step 4.5:** 运行构建验证组件无编译错误

**Commit:** `feat(user-status): add modal and drawer components for status management`

---

## Task 5: useStatusGuard composable

- [ ] **Step 5.1:** 创建 `jeecgboot-vue3/src/composables/useStatusGuard.ts`，实现 canPerformAction（缓存优先判断）和 showBlockModal
- [ ] **Step 5.2:** 编写 `useStatusGuard.test.ts`，测试 canPerformAction 的缓存判断逻辑和 showBlockModal 调用
- [ ] **Step 5.3:** 运行测试，确保 composable 测试 100% 通过

**Commit:** `feat(user-status): add useStatusGuard composable for interaction blocking`

---

## Task 6: 管理后台 — 用户状态管理页

- [ ] **Step 6.1:** 创建 `jeecgboot-vue3/src/views/content/user-status/manage/index.vue`，实现查询表单（用户ID/用户名、状态下拉）+ JVxeTable 列表
- [ ] **Step 6.2:** 实现操作列（查看详情、变更状态、解禁、状态历史），集成 StatusChangeModal、StatusReleaseModal、StatusHistoryDrawer
- [ ] **Step 6.3:** 实现批量解禁功能（行选择 + 批量操作按钮）
- [ ] **Step 6.4:** 编写 `UserStatusManage.test.vue`，测试查询、变更、解禁流程
- [ ] **Step 6.5:** 运行测试，确保页面测试 100% 通过

**Commit:** `feat(user-status): add user status management page for admin`

---

## Task 7: 管理后台 — 审计日志页

- [ ] **Step 7.1:** 创建 `jeecgboot-vue3/src/views/content/user-status/audit-log/index.vue`，实现筛选表单（用户ID、操作类型、时间范围）+ JVxeTable 列表
- [ ] **Step 7.2:** 实现查看详情（AuditLogDetailModal）和导出功能（blob 下载）
- [ ] **Step 7.3:** 编写 `AuditLogList.test.vue`，测试筛选、详情查看、导出流程
- [ ] **Step 7.4:** 运行测试，确保页面测试 100% 通过

**Commit:** `feat(user-status): add audit log page for admin`

---

## Task 8: 用户端 — 账号状态页

- [ ] **Step 8.1:** 创建 `jeecgboot-vue3/src/views/user/account-status/index.vue`，实现当前状态卡片（StatusTag + StatusCountdown + 处罚详情）
- [ ] **Step 8.2:** 实现操作按钮（申诉跳转 EPIC-08、安全核验跳转）和状态历史列表
- [ ] **Step 8.3:** 编写 `AccountStatus.test.vue`，测试状态展示和历史列表
- [ ] **Step 8.4:** 运行测试，确保页面测试 100% 通过

**Commit:** `feat(user-status): add account status page for users`

---

## Task 9: 用户端 — 登录拦截与安全核验

- [ ] **Step 9.1:** 创建 `jeecgboot-vue3/src/views/login/blocked/index.vue`，实现冻结/封禁提示、申诉按钮、返回登录页
- [ ] **Step 9.2:** 创建 `jeecgboot-vue3/src/views/login/verify/index.vue`，实现手机验证码表单、60 秒倒计时、验证通过跳转首页
- [ ] **Step 9.3:** 编写 `LoginBlocked.test.vue` 和 `SecurityVerify.test.vue`
- [ ] **Step 9.4:** 运行测试，确保页面测试 100% 通过

**Commit:** `feat(user-status): add login blocked and security verify pages`

---

## Task 10: 路由与菜单配置

- [ ] **Step 10.1:** 添加管理后台路由配置（/content/user-status/manage、/content/user-status/audit-log）
- [ ] **Step 10.2:** 添加用户端路由配置（/user/account-status、/login/blocked、/login/verify）
- [ ] **Step 10.3:** 配置后台管理菜单项（用户状态管理、审计日志）
- [ ] **Step 10.4:** 配置路由守卫：/login/blocked 和 /login/verify 不需要登录态，无拦截数据时重定向到登录页

**Commit:** `feat(user-status): add routes and menu configuration`

---

## Task 11: 响应拦截与 Store 集成

- [ ] **Step 11.1:** 在 HTTP 响应拦截器中添加 403 + 用户状态错误码处理（刷新 UserStatusStore、重定向拦截页）
- [ ] **Step 11.2:** 处理乐观锁冲突和非法状态转换错误
- [ ] **Step 11.3:** 集成 useUserStore：登录成功后调用 fetchCurrentStatus()，FROZEN/BANNED 时重定向
- [ ] **Step 11.4:** 集成 usePermissionStore：状态变更后刷新权限码

**Commit:** `feat(user-status): add response interceptor and store integration`

---

## Task 12: 集成 useStatusGuard 到互动入口

- [ ] **Step 12.1:** 在 CommentInput.vue 中集成 useStatusGuard 拦截
- [ ] **Step 12.2:** 在 MessageInput.vue 中集成 useStatusGuard 拦截
- [ ] **Step 12.3:** 在 PostEditor.vue 中集成 useStatusGuard 拦截

**Commit:** `feat(user-status): integrate useStatusGuard into interaction components`

---

## Task 13: 响应式适配

- [ ] **Step 13.1:** 用户状态管理页移动端适配（表格改卡片列表、操作按钮改下拉菜单）
- [ ] **Step 13.2:** 审计日志页移动端适配（表格精简列数、详情改全屏页面）
- [ ] **Step 13.3:** 账号状态页移动端适配（卡片全宽、历史改时间轴）
- [ ] **Step 13.4:** 登录拦截页和安全核验页移动端适配（居中卡片、按钮全宽）

**Commit:** `feat(user-status): add responsive layout for mobile and tablet`

---

## Task 14: 验证与 Code Review

- [ ] **Step 14.1:** 运行全量单元测试，确保 100% 通过
- [ ] **Step 14.2:** Code Review：检查代码质量、命名规范、边界条件、安全性
- [ ] **Step 14.3:** 功能验证：管理员状态查询/变更/解禁流程
- [ ] **Step 14.4:** 功能验证：审计日志查询/筛选/导出流程
- [ ] **Step 14.5:** 功能验证：用户端状态展示和倒计时
- [ ] **Step 14.6:** 功能验证：登录拦截和安全核验流程
- [ ] **Step 14.7:** 功能验证：互动拦截（评论/私信/动态）
- [ ] **Step 14.8:** 响应式布局验证：移动端/平板/桌面端

**Commit:** `chore(user-status): final verification and code review`
