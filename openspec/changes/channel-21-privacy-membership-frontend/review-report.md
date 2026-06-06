# OpenSpec Change 审核报告

**变更名称**: channel-21-privacy-membership-frontend
**变更类型**: 前端 change（配对后端 change: channel-21-privacy-membership）
**审核日期**: 2026-06-06
**审核人**: openspec-review-agent
**PRD**: docs/requirements/prd/frontend/EPIC-21-channel-privacy-membership-frontend-prd.md
**Domain**: channel | **Epic**: EPIC-21

---

## 1. 总览表

| 维度 | 得分 | BLOCK | FLAG | ADVISORY | 说明 |
|------|------|-------|------|----------|------|
| 完整性 (Completeness) | 8.5/10 | 0 | 1 | 2 | 文档结构齐全，6 spec 覆盖全部 capability，Open Questions 未闭环 |
| 一致性 (Consistency) | 7.0/10 | 1 | 2 | 1 | API 路径前缀不一致，groupUpdate 路径不匹配 |
| 可实现性 (Feasibility) | 8.0/10 | 0 | 1 | 1 | 技术栈兼容，composable 方案合理，但依赖 6 个未实现后端 API |
| 可测试性 (Testability) | 7.5/10 | 0 | 1 | 2 | 11 个测试文件有 TDD 配对，但 scenario 缺少量化验收标准 |
| 接口契约 (API Contract) | 6.0/10 | 2 | 2 | 1 | 6 个 P0 API 端点缺失，路径前缀不一致，批量响应格式未对齐 |
| 边界覆盖 (Boundary) | 8.0/10 | 0 | 1 | 1 | 覆盖 8/10 类边界条件，慢网络和 offline 处理部分缺失 |

**综合得分: 7.5/10**

**BLOCK 总计: 3** | **FLAG 总计: 8** | **ADVISORY 总计: 8**

---

## 2. 量化指标

| 指标 | 值 | 说明 |
|------|-----|------|
| PRD AC 覆盖率 | 85% | 13 个功能项中 11 个有对应 spec scenario，2 个（邀请管理、订阅列表部分细节）覆盖不完整 |
| API 契约完整率 | 76% | 前端定义 ~25 个 API，后端已有 19 个端点，6 个 P0 缺失 |
| 边界覆盖率 | 80% | 10 类边界条件中覆盖 8 类（权限、空态、加载态、网络错误、并发、重复提交、状态机、输入校验），缺失慢网络和 offline 检测 |
| TDD 配对率 | 100% | 11 个测试文件均有对应的实现任务（tasks.md 10.1-10.11） |
| Spec 文件数 | 6/6 | 全部 6 个 capability 均有独立 spec |
| Open Questions | 5 未闭环 | design.md 和 PRD 中的假设条件未验证 |

---

## 3. 维度详细审核

### 3.1 完整性 (Completeness) — 8.5/10

**文档结构**:
- [x] proposal.md — 包含 Why/What/Success Criteria/Non-Goals/Capabilities/Impact
- [x] design.md — 包含 Context/Goals/Decisions/Risks/File Structure/Data Formats/Test Strategy/API Mapping
- [x] specs/ — 6 个 spec 文件，覆盖全部 6 个 capability
- [x] tasks.md — 59 个任务，10 个分组，与 plan.md 一一对应
- [x] plan.md — 详细实现步骤，含代码示例和验证命令
- [x] backend-issues.md — 后端 API 缺失清单和修复建议
- [x] verification-review.md — 前后端一致性验证报告
- [x] 前端 PRD — 完整的功能说明、API 对接、状态管理、响应式设计

**内容覆盖**:
- proposal.md 的 6 个 Capability 全部在 specs/ 中有对应 spec
- PRD 的 13 个功能项中 11 个有完整 spec scenario
- design.md 的 Test Strategy 覆盖 11 个测试文件

**FLAG-1**: PRD 中的"邀请管理"功能（3.6 节）在 spec 中仅覆盖了邀请创建 Drawer，缺少独立的邀请管理列表页面 spec。`channel-privacy-settings/spec.md` 中的"邀请创建 Drawer" scenario 未覆盖邀请列表页面的完整交互（复制、撤销、状态更新）。

**ADVISORY-1**: design.md 中有 5 个 Open Questions 未闭环。建议在 apply 前确认：
- 频道主页的具体入口和路由
- 订阅列表页面的入口位置
- 成员管理页的页面组织方式
- 通知系统接口
- 邀请链接格式

**ADVISORY-2**: tasks.md 中任务 9.4（响应式验证）和 9.5（性能验证）为手动验证步骤，缺少自动化测试覆盖。建议补充 E2E 测试或至少提供验证 checklist。

---

### 3.2 一致性 (Consistency) — 7.0/10

**Capabilities ↔ Specs 对应**:
| Capability | Spec 文件 | 状态 |
|-----------|----------|------|
| channel-privacy-settings | specs/channel-privacy-settings/spec.md | ✅ 一致 |
| channel-subscription | specs/channel-subscription/spec.md | ✅ 一致 |
| channel-join-application | specs/channel-join-application/spec.md | ✅ 一致 |
| channel-member-management | specs/channel-member-management/spec.md | ✅ 一致 |
| channel-governance | specs/channel-governance/spec.md | ✅ 一致 |
| channel-context-composable | specs/channel-context-composable/spec.md | ✅ 一致 |

**Decisions ↔ Requirements 一致性**:
- D1（composable 而非 Pinia）→ channel-context-composable spec 中有 provide/inject scenario ✅
- D2（乐观更新）→ channel-subscription spec 中有乐观更新 scenario ✅
- D3（批量单次请求）→ channel-join-application 和 channel-member-management spec 中有批量操作 scenario ✅
- D4（JVxeTable）→ channel-member-management spec 中描述使用 JVxeTable ✅
- D5（表格转卡片）→ design.md 中有响应式策略描述 ✅
- D6（路由结构）→ PRD 中有路由路径定义 ✅

**BLOCK-1**: API 路径前缀不一致。design.md API 路径映射表中使用 `/channel/subscription/*`（无 `/api` 前缀），但前端 PRD 5.1-5.5 节使用 `/api/channel/subscription/*`（有 `/api` 前缀）。plan.md 中 Task 1 使用 `/channel/subscription/*`（无 `/api`）。三方不一致，联调时将导致 404。

**FLAG-2**: plan.md 中 `channelSubscription.ts` 的 `groupRename` 使用 `POST /group/rename`（与后端一致），但 design.md API 路径映射表中写的是 `PUT /group/update`。design.md 描述与实际实现不一致。

**FLAG-3**: frontend PRD 5.2 节中 `applyToJoin` 路径为 `/api/channel/member/apply`，但后端实际路径为 `/channel/member/join/apply`（多一层 `/join/`）。plan.md Task 2 中使用 `/channel/member/join/apply` 是正确的，但 PRD 描述不一致。

**ADVISORY-3**: proposal.md 中"约 25 个 API 接口调用"的表述与实际 API 数量（plan.md 中定义的 ~22 个函数）略有出入，建议精确化。

---

### 3.3 可实现性 (Feasibility) — 8.0/10

**技术栈兼容性**:
- Vue 3 + TypeScript + Ant Design Vue 4 + Vben Admin ✅ 与项目既有技术栈一致
- defHttp 封装 ✅ 项目标准 HTTP 客户端
- Vue Router + provide/inject ✅ Vue 3 标准模式
- 无新外部依赖 ✅

**架构规范**:
- useChannelContext composable 方案合理，天然按 channelId 隔离 ✅
- 乐观更新策略通过 useChannelOperation hook 统一封装 ✅
- 路由守卫配合 resetContext + loadContext 确保数据刷新 ✅
- 响应式策略（表格转卡片）符合移动端最佳实践 ✅

**FLAG-4**: 6 个 P0 后端 API 端点缺失（详见 backend-issues.md）：
1. 订阅状态查询 `/status/{channelId}` — SubscribeButton 状态机核心依赖
2. 黑名单列表 `/blacklist/list` — BlacklistPage 核心依赖
3. 治理日志列表 `/governance/log` — GovernanceLog 核心依赖
4. 隐私设置更新 `/privacy/update` — PrivacySettings 核心依赖
5. 加入方式更新 `/join-method/update` — JoinMethodSettings 核心依赖
6. 用户频道关系查询 `/relation` — useChannelContext 核心依赖

backend-issues.md 已提供临时方案（组合已有 API），但建议在 apply 前确认后端排期。

**ADVISORY-4**: plan.md Task 4 中 `useChannelContext` 导入了 `getChannelInfo` 和 `getUserChannelRelation` 两个函数，但这些函数在后端 API 中尚无独立端点。plan.md 中已标注 `TODO`，但实现时需先确认 API 路径或使用临时方案。

---

### 3.4 可测试性 (Testability) — 7.5/10

**TDD 配对情况**:
| 测试文件 | 对应实现任务 | 配对状态 |
|---------|------------|---------|
| useChannelContext.test.ts | Task 1.7 (composable) + Task 4 | ✅ |
| useChannelOperation.test.ts | Task 1.8 (hook) | ✅ |
| SubscribeButton.test.ts | Task 3.1-3.3 | ✅ |
| JoinApplyModal.test.ts | Task 4.1-4.2 | ✅ |
| SubscriptionList.test.ts | Task 3.5-3.6 | ✅ |
| PendingApplications.test.ts | Task 4.3-4.5 | ✅ |
| MemberList.test.ts | Task 5.1-5.2 | ✅ |
| MuteModal.test.ts | Task 5.5 | ✅ |
| GovernanceDetailDrawer.test.ts | Task 6.3 | ✅ |
| ChannelPrivacySettings.test.ts | Task 2.1-2.2 | ✅ |
| SubscriptionCard.test.ts | Task 3.4 | ✅ |

**FLAG-5**: spec 中的 scenario 使用自然语言描述，缺少可量化的验收标准。例如：
- "按钮立即变为已订阅状态" — 未定义"立即"的时间阈值（建议 < 100ms）
- "实时过滤频道列表" — 未定义防抖时间（PRD 中有 300ms，但 spec 中未体现）
- "页面首屏加载 < 2s" — 仅在 proposal.md 和 PRD 中定义，spec 中未引用

**ADVISORY-5**: design.md Test Strategy 中列出的集成测试场景（4 个场景）未在 tasks.md 中有对应的任务项。建议将集成测试场景纳入 tasks.md 或明确标记为 E2E 测试范围。

**ADVISORY-6**: tasks.md 中 Task 9.1-9.5 为"验证"任务，但描述为"运行测试"而非"编写测试"。实际测试编写在 Task 10.1-10.11。建议将验证任务明确标注为"运行已有测试并验证通过"，避免歧义。

---

### 3.5 接口契约 (API Contract) — 6.0/10

**前端 API 定义 vs 后端端点对比**:

| 前端 API | 前端路径 | 后端端点 | 状态 |
|---------|---------|---------|------|
| subscribeChannel | POST /channel/subscription/subscribe | POST /channel/subscription/subscribe | ✅ |
| unsubscribeChannel | POST /channel/subscription/unsubscribe | POST /channel/subscription/unsubscribe | ✅ |
| getSubscriptionStatus | GET /channel/subscription/status/{id} | ❌ 不存在 | **BLOCK-2** |
| getSubscriptionList | GET /channel/subscription/list | GET /channel/subscription/list | ✅ |
| createSubscriptionGroup | POST /channel/subscription/group/create | POST /channel/subscription/group/create | ✅ |
| renameSubscriptionGroup | POST /channel/subscription/group/rename | POST /channel/subscription/group/rename | ✅ |
| deleteSubscriptionGroup | DELETE /channel/subscription/group/delete | POST /channel/subscription/group/delete | ⚠️ HTTP 方法不一致 |
| getSubscriptionGroupList | GET /channel/subscription/group/list | GET /channel/subscription/group/list | ✅ |
| updateSubscriptionReminder | PUT /channel/subscription/reminder | ❌ 不存在 | FLAG-6 (P2) |
| applyToJoin | POST /channel/member/join/apply | POST /channel/member/join/apply | ✅ |
| getPendingApplications | GET /channel/member/applications/pending | GET /channel/member/applications/pending | ✅ |
| approveApplications | POST /channel/member/applications/approve | POST /channel/member/applications/approve | ✅ |
| rejectApplications | POST /channel/member/applications/reject | POST /channel/member/applications/reject | ✅ |
| getMemberList | GET /channel/member/list | GET /channel/member/list | ✅ |
| updateMemberRole | PUT /channel/member/assign-role | POST /channel/member/assign-role | ⚠️ HTTP 方法不一致 |
| removeMembers | POST /channel/governance/remove | POST /channel/governance/remove | ✅ |
| muteMember | POST /channel/governance/mute | POST /channel/governance/mute | ✅ |
| unmuteMember | POST /channel/governance/unmute | POST /channel/governance/unmute | ✅ |
| addToBlacklist | POST /channel/governance/blacklist/add | POST /channel/governance/blacklist/add | ✅ |
| removeFromBlacklist | POST /channel/governance/blacklist/remove | POST /channel/governance/blacklist/remove | ✅ |
| getBlacklist | GET /channel/governance/blacklist/list | ❌ 不存在 | **BLOCK-3** |
| createInvite | POST /channel/invite/create | POST /channel/invite/create | ✅ |
| getInviteList | GET /channel/invite/list | GET /channel/invite/list | ✅ |
| revokeInvite | POST /channel/invite/revoke | POST /channel/invite/revoke | ✅ |
| joinByInvite | POST /channel/invite/use | POST /channel/invite/use | ✅ |
| updateChannelPrivacy | PUT /channel/privacy/update | ❌ 不存在 | FLAG-7 (P0) |
| updateJoinMethod | PUT /channel/join-method/update | ❌ 不存在 | FLAG-7 (P0) |
| getGovernanceLog | GET /channel/governance/log | ❌ 不存在 | **BLOCK-2** |

**BLOCK-2**: 3 个核心 API 端点在后端完全缺失（订阅状态查询、治理日志列表、用户频道关系查询），且无临时替代方案。这些是 SubscribeButton 状态机、GovernanceLog、useChannelContext 的核心依赖，缺失将导致对应页面无法运行。

**BLOCK-3**: getBlacklist 路径为 `/channel/governance/blacklist/list`，但后端 ChannelGovernanceController 中无此端点。Service 层有 `listBlacklistedUserIds()` 但未暴露为 API。

**FLAG-6**: 更新提醒设置和移动频道到分组的后端 API 缺失，但 plan.md 中已将 `moveGroup` 标注为 P2 并注释，`reminder` 端点仍在代码中。建议在 plan.md 中明确标注 `reminder` 为 P2 或确认后端排期。

**FLAG-7**: 隐私设置更新和加入方式更新的后端 API 缺失。backend-issues.md 建议使用方案 C（通过 `PUT /{id}` 通用更新接口），但前端 plan.md 中仍使用独立路径。需在 apply 前确认最终方案。

**ADVISORY-7**: `deleteSubscriptionGroup` 前端使用 `defHttp.delete`（HTTP DELETE），后端实际为 `POST /group/delete`。`updateMemberRole` 前端使用 `defHttp.put`，后端实际为 `POST /assign-role`。HTTP 方法不一致可能导致联调问题。

---

### 3.6 边界覆盖 (Boundary) — 8.0/10

| 边界类型 | 覆盖情况 | 来源 |
|---------|---------|------|
| 1. 权限不足 | ✅ 已覆盖 | 所有 spec 中均有权限不足 scenario |
| 2. 空状态 | ✅ 已覆盖 | 黑名单、待审队列、订阅列表、成员列表均有空态 scenario |
| 3. 加载态 | ✅ 已覆盖 | PrivacySettings spec 有 skeleton scenario，PRD 有通用加载规范 |
| 4. 网络错误 | ✅ 已覆盖 | channel-context-composable spec 有 loadContext 失败处理、网络超时、500 错误 |
| 5. 并发操作 | ✅ 已覆盖 | design.md Test Strategy 提到 useChannelOperation 并发操作处理 |
| 6. 重复提交 | ✅ 已覆盖 | PRD 有"防重复提交：提交按钮 loading + 禁用"规范 |
| 7. 超时 | ✅ 已覆盖 | channel-context-composable spec 有 10 秒超时 scenario |
| 8. 输入校验 | ✅ 已覆盖 | JoinApplyModal spec 有 10-200 字验证 scenario |
| 9. 状态机转换 | ⚠️ 部分覆盖 | SubscribeButton 有 6 种状态，但缺少状态转换的完整组合验证 |
| 10. 离线/慢网络 | ⚠️ 部分覆盖 | channel-context-composable spec 有离线检测 scenario，但其他组件缺少离线态处理 |

**FLAG-8**: channel-context-composable spec 中有离线检测 scenario（`navigator.onLine === false`），但 SubscribeButton、JoinApplyModal 等交互组件的 spec 中未明确离线态行为。建议在 SubscribeButton spec 中补充离线态 scenario。

**ADVISORY-8**: 状态机转换覆盖不完整。SubscribeButton 的 6 种状态之间的转换路径（如：黑名单用户 → 移出黑名单 → 未订阅）未在 spec 中逐一列出。建议补充状态转换矩阵或至少覆盖关键转换路径。

---

## 4. 前后端衔接审计

### 4.1 接口清单双向对比

**前端引用的 API 数量**: ~25 个
**后端已定义的端点数量**: 19 个
**缺失端点数量**: 6 个（3 个 P0 BLOCK + 3 个 P0/P1 FLAG）

详见 3.5 节 API 契约对比表。

### 4.2 数据模型一致性

| 前端模型 | 后端模型 | 一致性 |
|---------|---------|--------|
| ChannelInfo (composable) | ChannelController 返回值 | ⚠️ 前端期望 privacyType/joinMethod/isSystem，后端 Channel Entity 字段需确认 |
| UserChannelRelation | 无独立 VO | ⚠️ 后端无 getUserChannelRelation 端点，需组合多个接口 |
| SubscriptionVO | ChannelSubscription Entity | ✅ 订阅状态字段一致 |
| MemberVO | ChannelMember Entity + Role | ✅ 角色枚举一致（OWNER/ADMIN/EDITOR/MEMBER） |
| JoinApplicationVO | ChannelJoinApplication Entity | ✅ 申请状态一致 |
| GovernanceLogVO | ChannelGovernanceLog Entity | ✅ beforeState/afterState JSON 格式一致 |

### 4.3 错误码覆盖检查

**现状**: 前端 PRD 和 spec 中未定义错误码映射。plan.md 中的 API 调用未包含错误码处理逻辑。

**建议**: 在 plan.md 中补充错误码处理策略，至少覆盖：
- 400: 参数校验失败（申请理由字数、邀请有效期等）
- 403: 权限不足（非管理员访问成员管理）
- 404: 频道/成员不存在
- 409: 冲突（重复订阅、重复申请）
- 429: 操作过于频繁

### 4.4 认证鉴权一致性

**现状**: 前端通过 `defHttp` 自动携带 token，后端通过 `SecureUtil.currentUser()` 获取当前用户。认证流程一致。

**注意**: plan.md 中未提及 token 过期时的刷新策略，但 channel-context-composable spec 中有 401 处理 scenario（清除认证信息 + 重定向登录）。

### 4.5 分页契约检查

**现状**: 前端 PRD 中成员列表、待审队列、订阅列表均支持分页，但未明确分页参数格式。

**后端分页约定**: MyBatis Plus 的 `pageNum` + `pageSize` 参数。

**建议**: 在 plan.md 中统一使用 `{ pageNum: 1, pageSize: 20 }` 分页参数格式，与后端 MyBatis Plus 分页保持一致。

---

## 5. PRD 追溯矩阵

| PRD 功能项 | PRD 章节 | Capability | Spec 文件 | Tasks 覆盖 |
|-----------|---------|-----------|----------|-----------|
| 频道隐私设置 | 3.1 | channel-privacy-settings | channel-privacy-settings/spec.md | 2.1-2.2 ✅ |
| 加入方式配置 | 3.2 | channel-privacy-settings | channel-privacy-settings/spec.md | 2.3-2.6 ✅ |
| 订阅/取消订阅 | 3.3 | channel-subscription | channel-subscription/spec.md | 3.1-3.3 ✅ |
| 申请加入私有频道 | 3.4 | channel-join-application | channel-join-application/spec.md | 4.1-4.2 ✅ |
| 加入申请审核 | 3.5 | channel-join-application | channel-join-application/spec.md | 4.3-4.5 ✅ |
| 邀请管理 | 3.6 | channel-privacy-settings | channel-privacy-settings/spec.md | 2.5-2.6 ⚠️ 仅覆盖创建，缺独立列表页 |
| 订阅列表管理 | 3.7 | channel-subscription | channel-subscription/spec.md | 3.4-3.6 ✅ |
| 成员列表与角色分配 | 3.8 | channel-member-management | channel-member-management/spec.md | 5.1-5.7 ✅ |
| 黑名单管理 | 3.9 | channel-governance | channel-governance/spec.md | 6.1 ✅ |
| 治理操作日志 | 3.10 | channel-governance | channel-governance/spec.md | 6.2-6.3 ✅ |
| useChannelContext | 6.2 | channel-context-composable | channel-context-composable/spec.md | 1.7, 4 ✅ |
| 响应式设计 | 8 | (跨 capability) | design.md 5 节 | 8.1-8.4 ✅ |
| 性能要求 | 9 | (跨 capability) | proposal.md | 9.5 ✅ |

**PRD AC 覆盖率**: 11/13 功能项完全覆盖 = 85%

---

## 6. 最终结论

### 评估: 有条件通过 (Conditional Pass)

change 的文档质量整体良好，设计决策合理，6 个 spec 覆盖全部 capability，59 个任务与 plan.md 一一对应，11 个测试文件有完整 TDD 配对。

### 必须修复 (BLOCK) — 3 项

| ID | 问题 | 修复建议 |
|----|------|---------|
| BLOCK-1 | API 路径前缀 `/api/channel/` vs `/channel/` 三方不一致 | 统一为 `/channel/`（与后端一致），在前端 HTTP 配置中添加代理或前缀映射 |
| BLOCK-2 | 3 个 P0 API 端点缺失（订阅状态、治理日志、用户频道关系） | 确认后端排期，或在 plan.md 中实现 backend-issues.md 提供的临时方案 |
| BLOCK-3 | 黑名单列表 API 路径与后端 Controller 不匹配 | 确认后端是否在 ChannelGovernanceController 中添加 `/blacklist/list` 端点 |

### 建议修复 (FLAG) — 8 项

| ID | 问题 | 优先级 |
|----|------|-------|
| FLAG-1 | 邀请管理列表页 spec 缺失 | P1 |
| FLAG-2 | design.md groupUpdate 路径描述与 plan.md 不一致 | P1 |
| FLAG-3 | PRD 中 applyToJoin 路径与后端不一致 | P1 |
| FLAG-4 | 6 个 P0 后端 API 依赖未确认 | P0 |
| FLAG-5 | spec scenario 缺少量化验收标准 | P2 |
| FLAG-6 | reminder API 缺失，plan.md 未明确标注 P2 | P2 |
| FLAG-7 | 隐私/加入方式更新 API 方案未确认 | P0 |
| FLAG-8 | 交互组件离线态处理不完整 | P2 |

### 建议改进 (ADVISORY) — 8 项

| ID | 建议 |
|----|------|
| ADVISORY-1 | 闭环 design.md 中 5 个 Open Questions |
| ADVISORY-2 | 补充响应式和性能验证的自动化测试 |
| ADVISORY-3 | 精确化 proposal.md 中的 API 数量表述 |
| ADVISORY-4 | 确认 useChannelContext 依赖的 API 路径 |
| ADVISORY-5 | 将集成测试场景纳入 tasks.md |
| ADVISORY-6 | 明确 tasks.md 验证任务的描述 |
| ADVISORY-7 | 统一 HTTP 方法（DELETE vs POST、PUT vs POST） |
| ADVISORY-8 | 补充 SubscribeButton 状态转换矩阵 |

### 建议操作

1. **立即修复**: BLOCK-1（API 路径前缀统一），可在 plan.md 中一步修正
2. **短期确认**: BLOCK-2/3、FLAG-4/7 — 与后端团队确认缺失 API 的实现计划或采用临时方案
3. **apply 前完成**: FLAG-1/2/3 的文档修正
4. **实现过程中**: 逐步处理 FLAG-5/6/8 和 ADVISORY 项
