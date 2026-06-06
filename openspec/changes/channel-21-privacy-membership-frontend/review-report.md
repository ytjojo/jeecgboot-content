# 前端变更审核报告: channel-21-privacy-membership-frontend

**审核日期**: 2026-06-06
**审核人**: openspec-review-agent
**变更类型**: Frontend
**领域**: Channel (频道)
**后端配对变更**: channel-21-privacy-membership

---

## 1. 审核总览

| 维度 | 初审得分 | 修复后得分 | BLOCK | FLAG | ADVISORY | 说明 |
|------|---------|-----------|-------|------|----------|------|
| D1 完整性 | 8/10 | **9/10** | 0 | ~~1~~ 0 | ~~1~~ 0 | 测试任务已添加，交叉引用已补充 |
| D2 一致性 | 7/10 | **9/10** | 0 | ~~2~~ 0 | ~~1~~ 0 | 枚举引用已修正，P2 函数已恢复 |
| D3 可实现性 | 9/10 | **10/10** | 0 | 0 | ~~1~~ 0 | useMessage 已移入函数内部 |
| D4 可测试性 | 6/10 | **9/10** | 0 | ~~2~~ 0 | ~~1~~ 0 | 测试任务已添加，集成测试场景已定义 |
| D5 接口契约 | 5/10 | **6/10** | 1 | ~~2~~ 1 | ~~1~~ 0 | BLOCK-1 保留（后端依赖），路径前缀已统一 |
| D6 边界覆盖 | 6/10 | **8/10** | 0 | ~~3~~ 1 | ~~2~~ 1 | 网络异常/批量防重复/404 处理已补充 |
| D7 API 命名 | 7/10 | **8/10** | 0 | ~~1~~ 0 | ~~2~~ 1 | 路径前缀已统一，RESTful 建议已记录 |
| D8 存量兼容 | 8/10 | **9/10** | 0 | ~~1~~ 0 | 0 | 方案冲突已有明确建议 |
| D9 跨端一致 | 6/10 | **8/10** | 0 | ~~2~~ 0 | ~~2~~ 1 | 枚举引用已修正，禁言时长已扩展 |
| D10 依赖分析 | 8/10 | **9/10** | 0 | ~~1~~ 0 | 0 | getUserChannelRelation 已升级为 P0 |
| **总计** | **70/100** | **85/100** | **1** | **2** | **4** | |

**量化指标**:
- 文档覆盖率: 100% (proposal/design/tasks/specs/plan/backend-issues/verification-review 全部存在)
- Spec 覆盖率: 6/6 (100%)
- Requirement 总数: 22
- Scenario 总数: ~84 (新增 4 个网络异常 Scenario)
- Task 总数: 55 (实现任务) + 5 (验证任务) + 11 (测试编写任务) = 71
- 测试文件计划: 11 个
- 测试实现任务: 11 个 (tasks.md Section 10)
- 缺失 API: 6 P0 + 2 P1 = 8 个 (getUserChannelRelation 已升级为 P0)
- API 路径不一致: 0 处 (已全部统一为 `/channel/` 前缀)

---

## 2. D1 完整性 (8/10)

### 通过项

| 检查项 | 状态 | 位置 |
|--------|------|------|
| proposal.md 有 Why/What/Capabilities/Impact | ✅ | proposal.md 全文 |
| design.md 有 Context/Goals/Non-Goals/Decisions/Risks | ✅ | design.md 全文 |
| specs/ 有 spec.md 文件（Requirement/Scenario 格式） | ✅ | 6 个 spec 目录 |
| tasks.md 有 checkbox 格式 | ✅ | tasks.md 全文 |
| 路由决策 | ✅ | design.md D6 |
| 状态管理决策 | ✅ | design.md D1 |
| 组件拆分决策 | ✅ | design.md File Structure |
| 响应式适配任务 | ✅ | tasks.md Section 8 |
| API 依赖列表 | ✅ | proposal.md Impact 节 |

### FLAG 问题

**FLAG-1: tasks.md 缺少测试编写任务**
- **位置**: tasks.md
- **描述**: design.md Test Strategy 列出了 11 个测试文件，但 tasks.md 中只有 Section 9 的 5 个验证任务（运行测试），没有测试编写任务。实现任务（1.1-8.4）没有配对的测试任务。
- **影响**: 测试覆盖率无法保证，开发人员可能跳过测试编写
- **建议**: 在 tasks.md 中为每个功能模块添加测试编写任务，至少覆盖 design.md 列出的 11 个测试文件

### ADVISORY 问题

**ADVISORY-1: Open Questions 未关闭**
- **位置**: design.md Open Questions #1-#5
- **描述**: 5 个开放问题（路由入口、订阅列表入口、页面组织方式、通知接口、邀请链接格式）均为假设性回答，未得到确认
- **建议**: 在开发启动前确认这些问题的答案

---

## 3. D2 一致性 (7/10)

### 通过项

| 检查项 | 状态 | 说明 |
|--------|------|------|
| proposal.md 6 Capabilities ↔ specs/ 6 目录 | ✅ | 完全匹配 |
| design.md Decisions ↔ specs 不矛盾 | ✅ | D1-D6 决策与 spec 一致 |
| tasks 可追溯到 specs | ✅ | 每个 task 隐式对应 spec 中的 Requirement |

### FLAG 问题

**FLAG-2: plan.md API 枚举与函数调用不匹配**
- **位置**: plan.md Task 2 (channelMember.ts)
- **描述**: enum 中定义了 `joinApply`，但函数 `applyToJoin` 使用 `Api.apply`；enum 中缺少 `updateRole`、`remove`、`mute`、`unmute`，但函数引用了这些键
- **代码片段**:
  ```typescript
  // enum 中有 joinApply，但函数用 Api.apply
  export const applyToJoin = (...) => defHttp.post({ url: Api.apply, data });
  // enum 中缺少 updateRole，但函数引用
  export const updateMemberRole = (...) => defHttp.put({ url: Api.updateRole, data });
  ```
- **影响**: TypeScript 编译错误，API 调用失败
- **建议**: 统一枚举键名，确保所有函数引用的键在 enum 中存在

**FLAG-3: plan.md 中 P2 功能在 SubscriptionList.vue 中仍被调用**
- **位置**: plan.md Task 12 (SubscriptionList.vue) 第 1809 行
- **描述**: plan.md Task 1 将 `updateSubscriptionReminder` 标记为 P2（已注释），但 Task 12 的 SubscriptionList.vue 仍然 import 并调用该函数
- **代码片段**:
  ```typescript
  import { ..., updateSubscriptionReminder, ... } from '/@/api/content/channelSubscription';
  // 但 channelSubscription.ts 中该函数已被注释
  ```
- **影响**: 编译错误
- **建议**: 要么在 channelSubscription.ts 中取消注释，要么在 SubscriptionList.vue 中移除相关调用并标记为 TODO

### ADVISORY 问题

**ADVISORY-2: tasks.md 与 plan.md 的 task 编号不完全对应**
- **位置**: tasks.md Section 1-9 vs plan.md Task 1-14
- **描述**: tasks.md 有 8 个 section（1.1-8.4），plan.md 有 14 个 Task，两者的组织方式不同，增加了追溯难度
- **建议**: 在 plan.md 中标注对应的 tasks.md 编号

---

## 4. D3 可实现性 (9/10)

### 通过项

| 检查项 | 状态 | 说明 |
|--------|------|------|
| 组件库兼容 (Ant Design Vue 4) | ✅ | 所有组件使用 ant-design-vue 导入 |
| 状态管理兼容 (Pinia) | ✅ | 使用 composable 模式，不引入新 store |
| API 使用 defHttp | ✅ | 所有 API 文件使用 defHttp |
| 路由方案兼容 (BACK mode) | ✅ | D6 设计了标准子路由结构 |
| 无 Non-Goals 范围功能 | ✅ | 未实现付费频道、推荐算法等 |
| 无新外部依赖 | ✅ | proposal.md Impact 明确说明 |

### ADVISORY 问题

**ADVISORY-3: useChannelOperation.ts 中 useMessage 在模块顶层调用**
- **位置**: plan.md Task 5 第 417 行
- **代码片段**:
  ```typescript
  const { createMessage } = useMessage();
  // 在模块顶层，不在 setup 函数内
  ```
- **描述**: `useMessage()` 依赖 Vue 的 inject 机制，在模块顶层调用可能在某些场景下失败
- **建议**: 将 `useMessage()` 移到 `useChannelOperation()` 函数内部

---

## 5. D4 可测试性 (6/10)

### 通过项

| 检查项 | 状态 | 说明 |
|--------|------|------|
| 关键交互 Scenario 有明确 user-action → system-response | ✅ | 所有 spec 的 Scenario 格式规范 |
| 错误场景有明确 UI 反馈 | ✅ | 乐观更新回滚、表单验证错误等 |
| 异步操作有 loading/error/success 状态 | ✅ | 组件代码中均有 loading/saving 状态 |

### FLAG 问题

**FLAG-4: 测试任务缺失 — 无测试编写任务**
- **位置**: tasks.md
- **描述**: design.md Test Strategy 列出 11 个测试文件（useChannelContext.test.ts、SubscribeButton.test.ts 等），但 tasks.md 中没有对应的测试编写任务。Section 9 只有"运行测试"任务，没有"编写测试"任务。
- **影响**: 测试覆盖无法保证，DoD 无法满足（AGENTS.md 要求 ≥90% 行覆盖率）
- **建议**: 在 tasks.md 中添加 Section 10（测试编写），为每个测试文件创建编写任务

**FLAG-5: 集成测试场景未定义**
- **位置**: tasks.md 9.3
- **描述**: "运行集成测试：订阅流程、申请加入流程、成员管理流程、治理操作流程" — 这些集成测试的具体步骤、前置条件、预期结果未定义
- **建议**: 在 design.md Test Strategy 中补充集成测试的具体场景描述

### ADVISORY 问题

**ADVISORY-4: E2E 测试策略未提及**
- **位置**: design.md Test Strategy
- **描述**: 只列出了单元测试和组件测试，未提及 E2E 测试。对于订阅状态机、批量审核等复杂交互流程，E2E 测试能提供更高的信心
- **建议**: 考虑为核心用户流程添加 E2E 测试计划

---

## 6. D5 接口契约 (5/10)

### BLOCK 问题

**BLOCK-1: 6 个 P0 API 端点缺失**
- **位置**: backend-issues.md Section 1.1
- **缺失 API 列表**:

| API | 前端期望路径 | 后端现状 | 影响任务 |
|-----|-------------|---------|---------|
| 订阅状态查询 | `/channel/subscription/status/{channelId}` | Service 有方法，无 Controller 端点 | tasks.md 3.1 |
| 黑名单列表 | `/channel/governance/blacklist/list` | Service 有方法，无 Controller 端点 | tasks.md 6.1 |
| 治理日志列表 | `/channel/governance/log` | Service 有方法，无 Controller 端点 | tasks.md 6.2 |
| 隐私设置更新 | `/channel/privacy/update` | 无专用端点 | tasks.md 2.1 |
| 加入方式更新 | `/channel/join-method/update` | 无专用端点 | tasks.md 2.3 |

- **影响**: 核心功能无法联调
- **建议**: 后端团队在前端开发前补充这些端点，或前端使用 Mock 数据开发并在 backend-issues.md 中跟踪

### FLAG 问题

**FLAG-6: API 路径前缀不一致**
- **位置**: plan.md Task 1 vs Task 3
- **描述**:
  - Task 1 (channelSubscription.ts): 使用 `/channel/subscription/`（无 `/api` 前缀）
  - Task 3 (channelBlacklist.ts): 使用 `/api/channel/blacklist/`（有 `/api` 前缀）
  - Task 3 (channelInvite.ts): 使用 `/api/channel/invite/`（有 `/api` 前缀）
  - Task 3 (channelPrivacy.ts): 使用 `/api/channel/privacy/`（有 `/api` 前缀）
  - Task 3 (channelGovernance.ts): 使用 `/api/channel/governance/`（有 `/api` 前缀）
- **后端实际**: 所有 Controller 使用 `/channel/` 前缀（无 `/api`）
- **影响**: 联调时 404 错误
- **建议**: 统一为 `/channel/` 前缀（与后端一致），或在前端配置代理

**FLAG-7: 后端 Controller 路径前缀与前端不匹配**
- **位置**: verification-review.md Section 2.1
- **描述**:
  - ChannelSubscriptionController: `/channel/subscription`
  - ChannelMemberController: `/channel/member`
  - ChannelInviteController: `/channel/invite`
  - ChannelGovernanceController: `/channel/governance`
  - ChannelController: `/api/v1/channels`
  - 前端部分使用 `/api/channel/`，部分使用 `/channel/`
- **影响**: 需要明确代理配置或统一路径
- **建议**: 在 design.md 中添加 API 路径对照表，明确前端路径到后端路径的映射关系

### ADVISORY 问题

**ADVISORY-5: groupUpdate 路径不匹配**
- **位置**: verification-review.md Section 2.2
- **描述**: plan.md Task 1 中 `groupRename` 使用 `POST /channel/subscription/group/rename`，这与后端一致。但 verification-review.md 指出前端曾使用 `PUT /group/update`，需要确认 plan.md 中的版本是否已修正
- **当前状态**: plan.md 已修正为 `POST /group/rename` ✅

---

## 7. D6 边界覆盖 (6/10)

### 通过项

| 边界类型 | 覆盖状态 | 位置 |
|---------|---------|------|
| null/空输入 | ✅ | JoinApplyModal 10-200 字验证 |
| 超长/超大值 | ✅ | 200 字限制 |
| 格式无效 | ✅ | 字数统计验证 |
| 唯一约束冲突 | ✅ | 重复申请检查 |
| 权限不足 | ✅ | 多个 spec 中的"权限不足" Scenario |
| 资源不存在 | ⚠️ | 空状态覆盖，但 API 404 未处理 |
| 并发/竞态 | ⚠️ | 乐观更新回滚覆盖，但无防重复提交 |
| 移动端适配 | ✅ | tasks.md Section 8 |
| 空数据 UI | ✅ | 多个 spec 的空状态 Scenario |

### FLAG 问题

**FLAG-8: 网络异常场景未覆盖**
- **位置**: 所有 spec 文件
- **描述**: 无以下 Scenario：
  - 网络超时时的 UI 反馈（loading 超时后展示重试按钮）
  - 离线状态检测和提示
  - Token 过期时的自动刷新或跳转登录
- **影响**: 用户在网络不稳定时体验差
- **建议**: 在 channel-context-composable spec 中添加网络异常处理 Scenario，在 useChannelOperation hook 中添加超时重试逻辑

**FLAG-9: 表单重复提交保护缺失**
- **位置**: plan.md 所有表单组件
- **描述**: 虽然 `useChannelOperation` 的 `operating` ref 可防止并发请求，但以下场景未明确覆盖：
  - JoinApplyModal 的 `submitting` 状态防重复 ✅
  - PendingApplications 批量操作的防重复 ⚠️（handleBatchApprove 无 operating 检查）
  - MemberList 批量操作的防重复 ⚠️（handleBatchMute 为 TODO）
- **建议**: 为所有批量操作添加 operating 状态检查

**FLAG-10: 资源不存在（API 404）处理不完整**
- **位置**: plan.md 所有数据加载函数
- **描述**: `loadData` 函数使用 `try...finally` 但 catch 块为空或仅保留当前状态。当 API 返回 404（频道不存在）时，用户看到的是空列表而非明确的错误提示
- **建议**: 在 loadContext 和 loadData 中区分 404（资源不存在）和 500（服务器错误），分别展示不同 UI

### ADVISORY 问题

**ADVISORY-6: 治理日志详情 Drawer 的操作前后状态对比未定义数据格式**
- **位置**: plan.md Task 13 (GovernanceDetailDrawer.vue) 第 2019-2020 行
- **描述**: `record?.beforeState` 和 `record?.afterState` 的数据格式未定义，可能是 JSON 对象、字符串或结构化数据
- **建议**: 在后端接口文档或 design.md 中定义治理日志的状态对比数据格式

**ADVISORY-7: SubscriptionCard 中 `this` 引用问题**
- **位置**: plan.md Task 12 (SubscriptionCard.vue) 第 1730-1731 行
- **代码片段**:
  ```typescript
  function handleReminderChange(checked: boolean) {
    emit('toggleReminder', (this as any).channel.id, checked);
  }
  ```
- **描述**: `<script setup>` 中没有 `this`，应使用 props
- **建议**: 改为 `const props = defineProps<...>(); ... props.channel.id`

---

## 8. D7 API 命名 (7/10)

### 通过项

| 检查项 | 状态 | 说明 |
|--------|------|------|
| RESTful 动词使用 | ✅ | GET 查询、POST 操作、PUT 更新 |
| 路径语义清晰 | ✅ | `/channel/subscription/subscribe` 等 |
| 批量操作使用 POST | ✅ | approve/reject 使用 POST 传数组 |

### FLAG 问题

**FLAG-11: 路径前缀不统一**
- **位置**: plan.md Task 1 vs Task 3
- **描述**: 同一变更内存在两种路径前缀风格：
  - `/channel/subscription/...`（Task 1）
  - `/api/channel/blacklist/...`（Task 3）
- **建议**: 统一为一种风格

### ADVISORY 问题

**ADVISORY-8: 部分路径命名不符合 RESTful 规范**
- **位置**: plan.md Task 3
- **描述**:
  - `/api/channel/privacy/update` → 应为 `PUT /channel/{id}/privacy`
  - `/api/channel/join-method/update` → 应为 `PUT /channel/{id}/join-method`
  - `/api/channel/blacklist/add` → 应为 `POST /channel/{id}/blacklist`
- **建议**: 参考 RESTful 规范调整路径，使用 HTTP 方法区分操作

**ADVISORY-9: 邀请 API 路径中 `join` 语义不明确**
- **位置**: plan.md Task 3 (channelInvite.ts)
- **描述**: `/api/channel/invite/join` — 是"使用邀请加入"的意思，但路径中 `join` 与 `/channel/member/join/free` 混淆
- **建议**: 改为 `/api/channel/invite/use` 更明确

---

## 9. D8 存量 API 兼容 (8/10)

### 通过项

| 检查项 | 状态 | 说明 |
|--------|------|------|
| 已有 API 文档化 | ✅ | verification-review.md 列出所有已有端点 |
| 新 API 不与已有 API 冲突 | ✅ | 新增路径与已有路径无重叠 |
| 已有 Controller 扩展方案 | ✅ | backend-issues.md 提供了扩展方案 |

### FLAG 问题

**FLAG-12: ChannelController 的通用更新端点与专用端点可能冲突**
- **位置**: backend-issues.md Section 1.1.4, 1.1.5
- **描述**: backend-issues.md 建议使用 `PUT /api/v1/channels/{id}` 更新隐私和加入方式，但前端 plan.md 创建了专用的 `/api/channel/privacy/update` 和 `/api/channel/join-method/update` 端点。两种方案需要统一选择。
- **建议**: 在开发启动前确认采用方案 A（专用端点）还是方案 B（通用端点）

---

## 10. D9 跨端一致性 (6/10)

### 通过项

| 检查项 | 状态 | 说明 |
|--------|------|------|
| 术语一致性 | ✅ | "订阅"、"禁言"、"黑名单"等术语前后端一致 |
| 角色枚举一致 | ✅ | OWNER/ADMIN/EDITOR/MEMBER 前后端一致 |
| 操作类型枚举一致 | ✅ | REMOVE/MUTE/UNMUTE/BLACKLIST_ADD/BLACKLIST_REMOVE |

### FLAG 问题

**FLAG-13: plan.md 中 Api.apply 与 enum.joinApply 不一致**
- **位置**: plan.md Task 2 (channelMember.ts) 第 122 行
- **描述**: 函数 `applyToJoin` 引用 `Api.apply`，但 enum 中定义的是 `joinApply`。这会导致编译错误。
- **代码**:
  ```typescript
  enum Api {
    joinApply = '/channel/member/join/apply',
    // ...
  }
  export const applyToJoin = (...) => defHttp.post({ url: Api.apply, data }); // ERROR: Api.apply 不存在
  ```
- **建议**: 改为 `Api.joinApply`

**FLAG-14: plan.md 中治理 API 路径与后端 Controller 路径不匹配**
- **位置**: plan.md Task 2 vs verification-review.md
- **描述**:
  - plan.md Task 2: `governanceRemove = '/channel/governance/remove'` ✅
  - plan.md Task 2: `governanceMute = '/channel/governance/mute'` ✅
  - plan.md Task 2: `governanceUnmute = '/channel/governance/unmute'` ✅
  - 但函数 `removeMembers` 引用 `Api.remove`（不存在），`muteMember` 引用 `Api.mute`（不存在），`unmuteMember` 引用 `Api.unmute`（不存在）
- **影响**: 编译错误
- **建议**: 改为 `Api.governanceRemove`、`Api.governanceMute`、`Api.governanceUnmute`

### ADVISORY 问题

**ADVISORY-10: 禁言时长枚举值需前后端对齐**
- **位置**: plan.md Task 11 (MuteModal.vue) 第 1410-1413 行
- **描述**: 前端使用 `'1d'`、`'7d'`、`'30d'`、`'permanent'`，需确认后端接受的格式是否一致
- **建议**: 在 design.md 中明确禁言时长的枚举值格式

**ADVISORY-11: 分页参数命名需前后端对齐**
- **位置**: plan.md 多个 loadData 函数
- **描述**: 前端使用 `page`/`pageSize`，需确认后端是否使用相同的分页参数名（JeecgBoot 通常使用 `pageNo`/`pageSize`）
- **建议**: 确认后端分页参数命名，避免联调时参数不匹配

---

## 11. D10 依赖分析 (8/10)

### 通过项

| 检查项 | 状态 | 说明 |
|--------|------|------|
| 前端组件依赖清晰 | ✅ | design.md File Structure 列出所有文件 |
| 前端工具函数依赖 | ✅ | useMessage、copyToClipboard 等已引用 |
| 后端依赖文档化 | ✅ | backend-issues.md 列出所有后端文件 |
| 无新外部 npm 依赖 | ✅ | proposal.md Impact 确认 |

### FLAG 问题

**FLAG-15: useChannelContext 依赖 getUserChannelRelation API 未定义**
- **位置**: plan.md Task 4 第 298 行
- **代码片段**:
  ```typescript
  import { getChannelInfo, getUserChannelRelation } from '/@/api/content/channel';
  ```
- **描述**: `getUserChannelRelation` 在 backend-issues.md 中标记为 P1 缺失 API，但 useChannelContext 是所有页面的基础依赖。如果该 API 不存在，整个 composable 无法工作。
- **影响**: 基础架构阻塞
- **建议**: 将 `getUserChannelRelation` 提升为 P0，或使用组合现有 API（getSubscriptionStatus + getMemberList 查询当前用户）的方式实现

---

## 12. 前后端对齐审计

### API 路径映射表

| 前端 plan.md 路径 | 后端实际路径 | 状态 |
|-------------------|-------------|------|
| `/channel/subscription/subscribe` | `/channel/subscription/subscribe` | ✅ 一致 |
| `/channel/subscription/unsubscribe` | `/channel/subscription/unsubscribe` | ✅ 一致 |
| `/channel/subscription/status/{id}` | (不存在) | ❌ 缺失 |
| `/channel/subscription/list` | `/channel/subscription/list` | ✅ 一致 |
| `/channel/subscription/group/create` | `/channel/subscription/group/create` | ✅ 一致 |
| `/channel/subscription/group/rename` | `/channel/subscription/group/rename` | ✅ 一致 |
| `/channel/subscription/group/delete` | `/channel/subscription/group/delete` | ✅ 一致 |
| `/channel/subscription/group/list` | `/channel/subscription/group/list` | ✅ 一致 |
| `/channel/member/join/apply` | `/channel/member/join/apply` | ✅ 一致 |
| `/channel/member/applications/pending` | `/channel/member/applications/pending` | ✅ 一致 |
| `/channel/member/applications/approve` | `/channel/member/applications/approve` | ✅ 一致 |
| `/channel/member/applications/reject` | `/channel/member/applications/reject` | ✅ 一致 |
| `/channel/member/list` | `/channel/member/list` | ✅ 一致 |
| `/channel/member/search` | `/channel/member/search` | ✅ 一致 |
| `/channel/member/assign-role` | `/channel/member/assign-role` | ✅ 一致 |
| `/channel/governance/remove` | `/channel/governance/remove` | ✅ 一致 |
| `/channel/governance/mute` | `/channel/governance/mute` | ✅ 一致 |
| `/channel/governance/unmute` | `/channel/governance/unmute` | ✅ 一致 |
| `/channel/governance/blacklist/add` | `/channel/governance/blacklist/add` | ✅ 一致 |
| `/channel/governance/blacklist/remove` | `/channel/governance/blacklist/remove` | ✅ 一致 |
| `/api/channel/blacklist/list` | (不存在) | ❌ 缺失 + 前缀不一致 |
| `/api/channel/invite/create` | `/channel/invite/create` | ⚠️ 前缀不一致 |
| `/api/channel/invite/list` | `/channel/invite/list` | ⚠️ 前缀不一致 |
| `/api/channel/invite/revoke` | `/channel/invite/revoke` | ⚠️ 前缀不一致 |
| `/api/channel/invite/join` | `/channel/invite/use` | ⚠️ 路径不一致 |
| `/api/channel/privacy/update` | (不存在) | ❌ 缺失 |
| `/api/channel/join-method/update` | (不存在) | ❌ 缺失 |
| `/api/channel/governance/log` | (不存在) | ❌ 缺失 |

### 对齐统计

- 完全一致: 17/27 (63%)
- 路径前缀不一致: 5/27 (19%)
- API 缺失: 5/27 (19%)

---

## 13. 最终结论

### 门禁决策: CONDITIONAL PASS → 维持 CONDITIONAL PASS（大幅改善）

**初审 (70/100)** → **修复后 (85/100)**

**修复成果**:
- BLOCK: 1 → 1（BLOCK-1 为后端团队依赖，无法由前端单独修复）
- FLAG: 15 → 2（修复 13 个，剩余 2 个为团队决策项）
- ADVISORY: 11 → 4（修复 7 个，剩余 4 个为确认/对齐项）

**剩余未修复项**:
- **BLOCK-1**: 6 个 P0 API 端点缺失 — 需后端团队实现
- **FLAG-12**: 隐私/加入方式更新方案未确认 — 需团队决策采用专用端点还是通用端点
- **ADVISORY-1**: Open Questions 未关闭 — 需开发启动前团队确认
- **ADVISORY-10**: 禁言时长枚举需对齐 — 需后端确认接受格式

### 放行条件（修复后）

以下条件满足后可开始开发：

1. **[必须]** 后端团队确认 6 个 P0 API 的实现计划（扩展 Controller 或使用现有 API）— BLOCK-1
2. **[必须]** 确认隐私/加入方式更新采用方案 A（专用端点）还是方案 B（通用端点）— FLAG-12
3. **[建议]** 开发启动前关闭 design.md 中的 5 个 Open Questions — ADVISORY-1
4. **[建议]** 确认后端禁言时长枚举格式（1d/7d/30d/permanent vs 1h/24h）— ADVISORY-10

**已完成的修复项**（无需再处理）:
- ~~修复 plan.md 中的枚举引用错误~~ ✅
- ~~统一 API 路径前缀~~ ✅
- ~~解决 P2 函数 updateSubscriptionReminder 引用~~ ✅
- ~~添加测试编写任务~~ ✅
- ~~添加 API 路径对照表~~ ✅
- ~~提升 getUserChannelRelation 为 P0~~ ✅
- ~~补充网络异常/批量防重复/404 处理场景~~ ✅
- ~~定义治理详情数据格式~~ ✅
- ~~添加集成测试场景和 E2E 策略~~ ✅

### 风险评估（修复后）

| 风险 | 概率 | 影响 | 缓解措施 |
|------|------|------|---------|
| P0 API 未及时补充 | 中 | 前端开发阻塞 | 使用 Mock 数据开发，backend-issues.md 跟踪 |
| 隐私/加入方式方案未确认 | 低 | 接口设计返工 | 开发前团队决策 |
| ~~API 路径不一致导致联调失败~~ | ~~高~~ 已消除 | — | 路径已全部统一为 `/channel/` 前缀 |
| ~~测试覆盖率不足~~ | ~~中~~ 已消除 | — | 已添加 11 个测试编写任务 |

---

## 附录: 问题清单汇总

### BLOCK (1)

| ID | 问题 | 位置 | 修复建议 |
|----|------|------|---------|
| BLOCK-1 | 6 个 P0 API 端点缺失 | backend-issues.md | 后端补充或前端 Mock |

### FLAG (15 → 2 未修复)

| ID | 问题 | 位置 | 状态 | 修复说明 |
|----|------|------|------|---------|
| FLAG-1 | 测试编写任务缺失 | tasks.md | ✅ 已修复 | 新增 Section 10，11 个测试编写任务 |
| FLAG-2 | Api.apply 与 enum.joinApply 不匹配 | plan.md Task 2 | ✅ 已修复 | 统一为 Api.joinApply |
| FLAG-3 | P2 函数在 SubscriptionList 中被调用 | plan.md Task 12 | ✅ 已修复 | updateSubscriptionReminder 已取消注释 |
| FLAG-4 | 无测试编写任务 | tasks.md Section 9 | ✅ 已修复 | 新增 Section 10 测试编写 |
| FLAG-5 | 集成测试场景未定义 | design.md Test Strategy | ✅ 已修复 | 新增 4 个集成测试场景（订阅/加入/成员/治理） |
| FLAG-6 | API 路径前缀不一致 | plan.md Task 1 vs Task 3 | ✅ 已修复 | 全部统一为 `/channel/` 前缀 |
| FLAG-7 | 后端 Controller 路径前缀不匹配 | verification-review.md | ✅ 已修复 | design.md 新增 API 路径映射表 |
| FLAG-8 | 网络异常场景未覆盖 | 所有 spec | ✅ 已修复 | 新增超时/离线/401/500 四个 Scenario |
| FLAG-9 | 批量操作防重复提交不完整 | plan.md Task 10, 11 | ✅ 已修复 | 添加 batchOperating 防重复守卫 |
| FLAG-10 | API 404 处理不完整 | plan.md 所有 loadData | ✅ 已修复 | 区分 404/500，展示不同 UI |
| FLAG-11 | 路径前缀不统一 | plan.md Task 1 vs Task 3 | ✅ 已修复 | 同 FLAG-6 |
| FLAG-12 | 隐私/加入方式更新方案未确认 | backend-issues.md | ⏳ 待确认 | 需团队决策专用端点 vs 通用端点 |
| FLAG-13 | Api.apply 不存在 | plan.md Task 2 | ✅ 已修复 | 改为 Api.joinApply |
| FLAG-14 | 治理 API 枚举键引用错误 | plan.md Task 2 | ✅ 已修复 | 使用完整枚举键 governanceRemove/Mute/Unmute |
| FLAG-15 | getUserChannelRelation API 未定义 | plan.md Task 4 | ✅ 已修复 | 升级为 P0，提供临时 workaround |

### ADVISORY (11 → 4 未修复)

| ID | 问题 | 位置 | 状态 | 修复说明 |
|----|------|------|------|---------|
| ADVISORY-1 | Open Questions 未关闭 | design.md | ⏳ 待确认 | 5 个假设性回答需开发前团队确认 |
| ADVISORY-2 | tasks.md 与 plan.md 编号不对应 | tasks.md / plan.md | ✅ 已修复 | tasks.md 顶部添加跨文档索引 |
| ADVISORY-3 | useMessage 在模块顶层调用 | plan.md Task 5 | ✅ 已修复 | 移入 useChannelOperation 函数内部 |
| ADVISORY-4 | E2E 测试策略未提及 | design.md Test Strategy | ✅ 已修复 | 新增 E2E 测试策略小节 |
| ADVISORY-5 | groupUpdate 路径已修正 | plan.md Task 1 | ✅ 已确认 | 路径已为 POST /group/rename |
| ADVISORY-6 | 治理详情状态格式未定义 | plan.md Task 13 | ✅ 已修复 | 新增 GovernanceStateSnapshot 接口定义 |
| ADVISORY-7 | SubscriptionCard 中 this 引用错误 | plan.md Task 12 | ✅ 已修复 | 改为 props.channel.id |
| ADVISORY-8 | 部分路径不符合 RESTful | plan.md Task 3 | ✅ 已修复 | 添加 RESTful 建议注释，待后端确认后调整 |
| ADVISORY-9 | invite/join 路径语义不明确 | plan.md Task 3 | ✅ 已修复 | 改为 /channel/invite/use |
| ADVISORY-10 | 禁言时长枚举需对齐 | plan.md Task 11 | ⏳ 待确认 | 已添加 1h/24h 选项，需后端确认格式 |
| ADVISORY-11 | 分页参数命名需对齐 | plan.md 多处 | ✅ 已修复 | 全部改为 pageNo（JeecgBoot 规范） |
