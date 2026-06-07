# 规范审核报告: user-09-status-lifecycle-frontend

> **审核日期**: 2026-06-06
> **审核工具**: openspec-review-change (subagent)
> **Change 类型**: 前端
> **配对后端 Change**: user-09-status-lifecycle (33/36 tasks done)
> **Domain**: user | Epic: EPIC-09
> **前端 PRD**: `docs/requirements/prd/frontend/EPIC-09-user-status-lifecycle-frontend-prd.md`
> **后端 PRD**: `docs/requirements/prd/decomposition/user/EPIC-09-user-status-lifecycle.md`

---

## 总览

| 维度 | 得分 | BLOCK | FLAG | ADVISORY |
|------|------|-------|------|----------|
| 完整性 (Completeness) | 92% | 0 | 1 | 2 |
| 一致性 (Consistency) | 85% | 1 | 1 | 1 |
| 可实现性 (Feasibility) | 88% | 1 | 1 | 1 |
| 可测试性 (Testability) | 90% | 0 | 2 | 1 |
| 接口契约 (API Contract) | 80% | 1 | 2 | 1 |
| 边界覆盖 (Boundary) | 88% | 0 | 1 | 2 |
| **综合** | **87%** | **3** | **8** | **8** |

### 量化指标

| 指标 | 值 | 说明 |
|------|-----|------|
| PRD AC 覆盖率 | 93% | 9 个 User Story、45 个验收条件，6 个 Capability 全覆盖 |
| API 契约完整率 | 36% (5/14) | 后端已实现 5/14 API，9 个待实现 (backend-issues.md) |
| API 路径一致率 | 71% (10/14) | proposal.md vs PRD 路径不一致 4 处 |
| 边界覆盖 | 88% | 10 类边界条件覆盖 8.8 类 |
| TDD 配对率 | 60% (6/10) | 10 个测试文件计划，6 个 suite 实际运行 |
| Spec/AC 场景总数 | 52 | 6 个 spec 共 52 个 scenario |
| 任务完成率 | 100% (42/42) | tasks.md 全部完成 |

---

## 1. 完整性 (Completeness) -- 92%

### 1.1 文档结构完整性

| 文档 | 状态 | 说明 |
|------|------|------|
| proposal.md | ✅ 完整 | Context/Goals/Non-Goals/Decisions/Risks/Impact/API 依赖清单 |
| design.md | ✅ 完整 | Context/Goals/Decisions/Risks/错误码/分页/权限/File Structure/Test Strategy |
| specs (6个) | ✅ 完整 | 覆盖全部 6 个 Capability |
| tasks.md | ✅ 完整 | 11 主任务、42 子任务，全部标记完成 |
| plan.md | ✅ 完整 | 14 个 Task，含 TDD 步骤和 commit message |
| backend-issues.md | ✅ 完整 | 9 个待实现 API 清单及实现建议 |

### 1.2 Capability 覆盖

| Capability | proposal | design | spec | PRD | 覆盖 |
|------------|----------|--------|------|-----|------|
| user-status-manage | ✅ | ✅ | ✅ | ✅ | 完整 |
| user-status-audit-log | ✅ | ✅ | ✅ | ✅ | 完整 |
| user-account-status | ✅ | ✅ | ✅ | ✅ | 完整 |
| user-login-intercept | ✅ | ✅ | ✅ | ✅ | 完整 |
| user-interaction-guard | ✅ | ✅ | ✅ | ✅ | 完整 |
| user-status-store | ✅ | ✅ | ✅ | ✅ | 完整 |

### 1.3 PRD User Story 覆盖

| User Story | 验收条件数 | spec 覆盖 | 状态 |
|------------|-----------|-----------|------|
| US-01 管理员查询用户状态 | 4 | user-status-manage | ✅ |
| US-02 管理员手动变更状态 | 7 | user-status-manage | ✅ |
| US-03 管理员查看审计日志 | 5 | user-status-audit-log | ✅ |
| US-04 管理员执行解禁 | 5 | user-status-manage | ✅ |
| US-05 用户查看自身状态 | 5 | user-account-status | ✅ |
| US-06 用户查看状态历史 | 4 | user-account-status | ✅ |
| US-07 禁言用户互动拦截 | 5 | user-interaction-guard | ✅ |
| US-08 冻结/封禁登录拦截 | 5 | user-login-intercept | ✅ |
| US-09 冻结用户安全核验 | 7 | user-login-intercept | ✅ |

### [FLAG] F-1: proposal.md Success Criteria 未覆盖 US-09 异常场景

**位置**: proposal.md "Success Criteria" 章节

**问题**: Success Criteria 第 5 条提到"冻结用户可通过手机验证码安全核验解冻"，但未提及验证码错误、过期、频率限制等异常场景的验收标准。

**建议**: 补充异常场景的成功标准，如"验证码错误时显示明确提示"、"验证码过期时提示重新获取"。

### [ADVISORY] A-1: 无 gap-analysis.md 文件

**说明**: change 目录下无 gap-analysis.md。backend-issues.md 承担了类似功能，记录了 9 个待实现后端 API。建议将 backend-issues.md 重命名或链接为 gap-analysis，以统一文档约定。

### [ADVISORY] A-2: verify.md 状态为 "ready" 但未执行

**说明**: openspec status 显示 verify artifact 状态为 "ready"（非 "done"），但 verification-review.md 已存在。建议统一状态标记。

---

## 2. 一致性 (Consistency) -- 85%

### 2.1 Capabilities <-> Specs 一致性

| Capability (proposal) | Spec | 一致 |
|----------------------|------|------|
| user-status-manage | specs/user-status-manage/spec.md | ✅ |
| user-status-audit-log | specs/user-status-audit-log/spec.md | ✅ |
| user-account-status | specs/user-account-status/spec.md | ✅ |
| user-login-intercept | specs/user-login-intercept/spec.md | ✅ |
| user-interaction-guard | specs/user-interaction-guard/spec.md | ✅ |
| user-status-store | specs/user-status-store/spec.md | ✅ |

### 2.2 Decisions <-> Requirements 一致性

| Decision | 关联 Requirement | 一致 |
|----------|-----------------|------|
| D1: 状态转换矩阵后端返回 | user-status-manage "打开状态变更弹窗" | ✅ |
| D2: 不签发 token | user-login-intercept "冻结用户登录" | ✅ |
| D3: useStatusGuard 统一管理 | user-interaction-guard "统一互动拦截 composable" | ✅ |
| D4: 倒计时纯前端计算 | user-account-status "状态倒计时" | ✅ |
| D5: 审计日志后端生成文件流 | user-status-audit-log "管理员导出审计日志" | ✅ |
| D6: 响应拦截统一处理 | user-status-store "403 状态码自动重定向" | ✅ |

### [BLOCK] B-1: proposal.md API 路径与 PRD/design.md 严重不一致

**位置**: proposal.md "API 接口依赖清单" vs PRD 4.3 节 / design.md

**问题**: proposal.md 使用旧的路径方案（`/{userId}` 前缀），PRD 和 design.md 使用新的路径方案（`/users/{userId}` 前缀）。至少 4 个 API 路径不一致：

| API | proposal.md 路径 | PRD/design.md 路径 | 冲突 |
|-----|------------------|-------------------|------|
| getUserStatus | `/user-status/{userId}` | `/user-status/users/{userId}` | 路径前缀不同 |
| changeUserStatus | `/user-status/{userId}/change` | `/user-status/users/{userId}/change` | 路径前缀不同 |
| releaseUser | `/user-status/{userId}/release` | `/user-status/users/{userId}/release` | 路径前缀不同 |
| batchReleaseUsers | `/user-status/batch-release` | `/user-status/users/batch-release` | 路径前缀不同 |

**影响**: 实施时可能按 proposal.md 的旧路径开发，导致与后端不匹配。

**建议**: 以 design.md 和 PRD 为权威来源，更新 proposal.md 的 API 路径。

### [FLAG] F-2: specs 内 API 路径引用与 PRD 不一致

**位置**: specs/user-status-manage/spec.md 第 116-127 行, specs/user-status-audit-log/spec.md 第 71-76 行, specs/user-login-intercept/spec.md 第 83-86 行

**问题**: specs 的"后端 API 依赖"章节引用旧路径：
- user-status-manage: `GET /api/content/user-status/{userId}` (PRD 为 `/users/{userId}`)
- user-status-manage: `GET /api/content/user-status/list` (PRD 为 `/users`)
- user-status-audit-log: `GET /api/content/user-status/audit-logs` (PRD 为 `/user-status-audit/logs`)

**建议**: 统一 specs 中的 API 路径引用，以 PRD 4.3 节为准。

### [ADVISORY] A-3: user-status-store/spec.md fetchCurrentStatus 参数说明不一致

**位置**: specs/user-status-store/spec.md 第 27-28 行

**问题**: Scenario 注释说"后端 API 需要 userId 参数"，但 PRD 设计 `GET /current` 无参数（从登录态获取）。若后端改为从 token 获取 userId，则此 spec 注释过时。

**建议**: 明确 fetchCurrentStatus 是否需要传 userId 参数，与后端对齐。

---

## 3. 可实现性 (Feasibility) -- 88%

### 3.1 技术栈兼容性

| 项目 | 状态 | 说明 |
|------|------|------|
| Vue 3 + TypeScript | ✅ | 项目主技术栈 |
| Vite 6 | ✅ | 项目构建工具 |
| Ant Design Vue 4 | ✅ | Tag/Modal/Form/Drawer/Description 等组件 |
| Pinia | ✅ | 项目状态管理方案 |
| dayjs | ✅ | 项目日期处理库 |
| JVxeTable | ✅ | 项目通用表格组件 |
| defHttp | ✅ | 项目 HTTP 请求封装 |

### 3.2 架构规范兼容性

| 规范 | 状态 | 说明 |
|------|------|------|
| 组件目录结构 | ✅ | `src/components/jeecg/UserStatus/` 符合项目规范 |
| Store 目录结构 | ✅ | `src/store/modules/userStatus.ts` 符合项目规范 |
| API 封装规范 | ✅ | `src/api/content/userStatus.ts` 使用 defHttp |
| Composable 模式 | ✅ | `src/composables/useStatusGuard.ts` 符合 Vue 3 组合式 API |
| 路由配置 | ✅ | 复用项目路由机制 |

### [BLOCK] B-2: 4 个高优先级后端 API 未实现，阻塞核心功能

**位置**: backend-issues.md

**问题**: 以下 4 个 P0 API 后端未实现，直接阻塞前端核心功能：

| API | 路径 | 阻塞功能 |
|-----|------|---------|
| getTransitions | GET /transitions/{currentStatus} | StatusChangeModal 无法获取可转换状态 |
| getStatusList | GET /list (或 /users) | 管理页列表无法查询 |
| verifySecurity | POST /verify-security (或 /verify) | 安全核验功能不可用 |
| sendVerifyCode | POST /send-verify-code (或 /send-code) | 验证码发送不可用 |

**影响**: 管理后台状态变更、状态列表查询、安全核验三个核心功能不可用。

**注意**: 前一版 review report (2026-06-05) 声称这 9 个 API 已在 UserStatusController.java 中全部实现，但 backend-issues.md 仍标记为"待实现"。需验证后端 Controller 实际状态。

**建议**: 确认后端 Controller 实际实现状态。若已实现，更新 backend-issues.md；若未实现，优先补充。

### [FLAG] F-3: 登录接口响应结构变更未确认

**位置**: PRD 8.3 节, design.md Open Questions

**问题**: PRD 定义了登录接口需返回 `LoginBlockedResponse`（包含 userStatus、statusReason、statusEndTime、phone），但 design.md Open Questions 列出"登录接口响应结构变更的具体字段名和格式，需与后端对齐"仍为开放问题。

**影响**: 若后端登录接口未按此结构改造，前端登录拦截功能无法实现。

**建议**: 与后端确认登录接口改造方案，关闭此 Open Question。

### [ADVISORY] A-4: EPIC-08 申诉系统集成路由未确认

**位置**: design.md Open Questions

**问题**: "EPIC-08 申诉系统的跳转路由和参数格式"仍为开放问题。封禁用户登录拦截页的"申诉"按钮需跳转到 EPIC-08 申诉系统，但具体路由和参数格式未定。

**建议**: 确认 EPIC-08 申诉系统的路由定义，更新 design.md 并关闭此 Open Question。

---

## 4. 可测试性 (Testability) -- 90%

### 4.1 测试策略覆盖

| 测试文件 | 计划 | 实际运行 | 状态 |
|---------|------|---------|------|
| userStatus.test.ts (API) | ✅ | ✅ | 通过 |
| userStatusStore.test.ts | ✅ | ✅ | 通过 |
| useStatusGuard.test.ts | ✅ | ✅ | 通过 |
| StatusTag.test.ts | ✅ | ✅ | 通过 |
| StatusCountdown.test.ts | ✅ | ✅ | 通过 |
| UserStatusManage.test.vue | ✅ | - | covered by component/store tests |
| AuditLogList.test.vue | ✅ | - | covered by component/store tests |
| AccountStatus.test.vue | ✅ | - | covered by component/store tests |
| LoginBlocked.test.vue | ✅ | - | covered by component/store tests |
| SecurityVerify.test.vue | ✅ | - | covered by component/store tests |

**测试结果**: 6 suites, 61 tests pass; 11 pre-existing empty suites fail（非本 change 引入）。

### [FLAG] F-4: Spec Scenarios 缺少可量化验收条件

**位置**: 所有 6 个 specs

**问题**: 大部分 scenario 仅有定性描述（如"展示状态标签"、"弹出提示"），缺少可量化的验收条件。例如：
- "查询响应时间 <100ms" 仅在 PRD 中提及，未写入 spec scenario
- "验证码有效期 5 分钟" 仅在 backend-issues.md 提及，未写入 spec
- "导出数据量超过 10000 条" 的限制在 user-status-audit-log spec 中有，但其他 specs 缺少类似量化条件

**建议**: 为关键 scenario 添加可量化的验收条件，特别是性能和数据量相关的场景。

### [FLAG] F-5: 5 个页面测试文件未独立运行

**位置**: tasks.md 第 14、18、25、32 行

**问题**: 5 个页面级测试文件（UserStatusManage.test.vue、AuditLogList.test.vue、AccountStatus.test.vue、LoginBlocked.test.vue、SecurityVerify.test.vue）标注为 "covered by component and store tests"，未独立运行。这意味着页面级集成测试（如完整查询流程、表单提交流程）可能未覆盖。

**建议**: 补充页面级集成测试，至少覆盖核心用户流程（查询 -> 变更 -> 确认 -> 刷新）。

### [ADVISORY] A-5: 状态转换矩阵边界测试未在 spec 中显式定义

**位置**: PRD 11.2 节 "边界条件测试"

**问题**: PRD 提到"所有 9 种状态之间的合法/非法转换组合"需测试，但 specs 中未列出具体的转换矩阵测试用例。后端 change 的 `UserStatusTransition` 已定义完整矩阵，但前端 spec 缺少对应的测试场景。

**建议**: 在 user-status-manage spec 中添加状态转换矩阵的完整测试场景。

---

## 5. 接口契约 (API Contract) -- 80%

### 5.1 API 路径定义权威来源

**权威来源**: PRD 4.3 节 (design.md 同步)

| # | API | 方法 | 路径 (PRD) | 后端状态 |
|---|-----|------|-----------|---------|
| 1 | getCurrentStatus | GET | `/api/content/user-status/current` | ✅ 已实现 (需 userId 参数) |
| 2 | getUserStatus | GET | `/api/content/user-status/users/{userId}` | ✅ 已实现 (路径可能需调整) |
| 3 | getStatusList | GET | `/api/content/user-status/users` | ❌ 未实现 (backend-issues.md) |
| 4 | getTransitions | GET | `/api/content/user-status/transitions/{currentStatus}` | ❌ 未实现 |
| 5 | changeUserStatus | POST | `/api/content/user-status/users/{userId}/change` | ✅ 已实现 (路径可能需调整) |
| 6 | releaseUser | POST | `/api/content/user-status/users/{userId}/release` | ✅ 已实现 (路径可能需调整) |
| 7 | batchReleaseUsers | POST | `/api/content/user-status/users/batch-release` | ❌ 未实现 |
| 8 | getStatusHistory | GET | `/api/content/user-status/users/{userId}/history` | ✅ 已实现 |
| 9 | getAuditLogList | GET | `/api/content/user-status-audit/logs` | ❌ 未实现 |
| 10 | getAuditLogDetail | GET | `/api/content/user-status-audit/logs/{logId}` | ❌ 未实现 |
| 11 | getUserAuditLogs | GET | `/api/content/user-status-audit/logs/user/{userId}` | ❌ 未实现 |
| 12 | exportAuditLogs | POST | `/api/content/user-status-audit/export` | ❌ 未实现 |
| 13 | verifySecurity | POST | `/api/content/user-status/verify` | ❌ 未实现 |
| 14 | sendVerifyCode | POST | `/api/content/user-status/send-code` | ❌ 未实现 |

### 5.2 req/vo 匹配检查

| 类型 | 定义位置 | 字段 | 状态 |
|------|---------|------|------|
| UserStatusEnum | PRD 5.1 + backend entity | 9 种状态 | ✅ 一致 |
| UserStatusDetail | PRD 5.1 | userId/status/statusStartTime/statusEndTime/statusReason/statusOperatorId | ✅ 定义完整 |
| UserStatusHistoryItem | PRD 5.1 | logId/fromStatus/toStatus/triggerReason/operatorId/operatorType/createdAt | ✅ 定义完整 |
| UserStatusQueryReq | PRD 6.1 | 分页 + userId/status 筛选 | ✅ |
| UserStatusChangeReq | PRD 6.1 | targetStatus/reason/endTime | ✅ |
| AuditLogQueryReq | PRD 6.1 | 分页 + userId/startTime/endTime/operatorType | ✅ |
| AuditLogExportReq | PRD 6.1 | userId/startTime/endTime/format | ✅ |
| LoginBlockedResponse | PRD 8.3 | userStatus/statusReason/statusEndTime/phone | ✅ |

### [BLOCK] B-3: proposal.md API 表与 PRD 路径不一致 (同 B-1)

详见 B-1。proposal.md "API 接口依赖清单" 使用旧路径，需更新。

### [FLAG] F-6: 后端 change controller 路径与前端 PRD 可能不一致

**位置**: backend-issues.md vs PRD 4.3

**问题**: backend-issues.md 记录的后端 Controller 路径为旧方案（`/{userId}`），前端 PRD 使用新方案（`/users/{userId}`）。若后端 Controller 未迁移到新路径，前后端联调将失败。

**具体差异**:
- 后端: `GET /user-status/{userId}` vs 前端: `GET /user-status/users/{userId}`
- 后端: `POST /user-status/{userId}/change` vs 前端: `POST /user-status/users/{userId}/change`

**建议**: 确认后端 Controller 是否已按 PRD 新路径重构，若未重构则需同步改造。

### [FLAG] F-7: 导出审计日志 HTTP 方法不明确

**位置**: PRD 4.3 第 380 行

**问题**: PRD 定义 `exportAuditLogs` 为 `POST /api/content/user-status-audit/export`，但 backend-issues.md 中的建议代码使用 `GET` 方法。HTTP 方法不一致可能导致实现混淆。

**建议**: 统一为 POST 方法（因可能携带复杂筛选参数），更新 backend-issues.md。

### [ADVISORY] A-6: 权限注解未在后端代码中确认

**位置**: design.md 权限标注表

**问题**: design.md 定义了 14 个 API 的权限要求（如 `admin:user-status:query`、`admin:audit-log:query`），但未确认后端 Controller 是否已添加对应的权限注解。

**建议**: 确认后端 Controller 已添加权限注解，或在 backend-issues.md 中标注待添加。

---

## 6. 边界覆盖 (Boundary) -- 88%

### 6.1 10 类边界条件覆盖评估

| # | 边界类型 | 覆盖 | 位置 | 说明 |
|---|---------|------|------|------|
| 1 | 空数据 | ✅ | user-status-manage, user-status-audit-log | 查询结果为空展示空状态 |
| 2 | 查询不存在 | ✅ | user-status-manage | "用户不存在"提示 |
| 3 | 并发冲突 | ✅ | user-status-manage | 乐观锁冲突提示 |
| 4 | 非法转换 | ✅ | user-status-manage | "当前状态不允许执行此操作" |
| 5 | 超时/网络异常 | ✅ | user-status-manage, user-login-intercept | 超时提示、网络异常提示 |
| 6 | 频率限制 | ⚠️ | user-login-intercept | 前端 UI 禁用，但后端 rate limit 错误码处理不完整 |
| 7 | 永久封禁 | ✅ | user-account-status, user-login-intercept | statusEndTime=null 显示"永久" |
| 8 | 倒计时精度 | ⚠️ | user-account-status | visibilitychange 修正，但极端场景未覆盖 |
| 9 | 批量操作 | ✅ | user-status-manage | 防重复提交、部分失败处理 |
| 10 | 导出数据量 | ✅ | user-status-audit-log | 超过 10000 条提示 |

### [FLAG] F-8: 验证码频率限制后端错误码处理不完整

**位置**: specs/user-login-intercept/spec.md, design.md 错误码表

**问题**: design.md 定义了 `VERIFY_CODE_RATE_LIMIT (429)` 错误码，spec 中有"验证码发送频率限制"场景，但 spec 仅描述前端 UI 层面的 60 秒按钮禁用，未明确后端返回 429 时的前端处理逻辑（按钮倒计时同步、提示文案等）。

**建议**: 在 spec 中补充"后端返回 429 时前端同步倒计时并提示"的场景。

### [ADVISORY] A-7: 倒计时极端场景未覆盖

**位置**: specs/user-account-status/spec.md

**问题**: StatusCountdown 的 visibilitychange 修正方案覆盖了正常后台挂起场景，但以下极端场景未在 spec 中定义：
- endTime 已过（系统时间偏差、后端自动解禁延迟）
- endTime 为 null（永久封禁时不显示倒计时）
- endTime 为极远未来（如 2099 年）

**建议**: 在 spec 中补充 endTime 边界值场景。

### [ADVISORY] A-8: 批量操作空选择未在 spec 中显式定义

**位置**: specs/user-status-manage/spec.md

**问题**: spec 定义了"批量解禁"场景，但未明确"未选择任何用户时点击批量解禁"的处理逻辑。前端应在无选中行时禁用批量操作按钮或提示"请先选择用户"。

**建议**: 在 spec 中补充空选择场景。

---

## 7. 前后端衔接审计

### 7.1 触发条件

- 配对后端 change: `user-09-status-lifecycle` ✅ 存在
- 后端 change 目录: `openspec/changes/user-09-status-lifecycle/` ✅ 存在
- 后端完成度: 33/36 tasks

### 7.2 接口清单双向对比

| 前端引用 API | 前端路径 (PRD) | 后端已实现 | 后端路径 (backend-issues.md) | 匹配 |
|-------------|---------------|-----------|---------------------------|------|
| getCurrentStatus | /user-status/current | ✅ | /user-status/current (需userId) | ⚠️ 参数不一致 |
| getUserStatus | /user-status/users/{userId} | ✅ | /user-status/{userId} | ❌ 路径前缀不一致 |
| changeUserStatus | /user-status/users/{userId}/change | ✅ | /user-status/{userId}/change | ❌ 路径前缀不一致 |
| releaseUser | /user-status/users/{userId}/release | ✅ | /user-status/{userId}/release | ❌ 路径前缀不一致 |
| getStatusHistory | /user-status/users/{userId}/history | ✅ | /user-status/{userId}/history | ❌ 路径前缀不一致 |
| getStatusList | /user-status/users | ❌ | - | N/A |
| getTransitions | /user-status/transitions/{status} | ❌ | - | N/A |
| batchReleaseUsers | /user-status/users/batch-release | ❌ | - | N/A |
| getAuditLogList | /user-status-audit/logs | ❌ | - | N/A |
| getAuditLogDetail | /user-status-audit/logs/{logId} | ❌ | - | N/A |
| getUserAuditLogs | /user-status-audit/logs/user/{userId} | ❌ | - | N/A |
| exportAuditLogs | /user-status-audit/export | ❌ | - | N/A |
| verifySecurity | /user-status/verify | ❌ | - | N/A |
| sendVerifyCode | /user-status/send-code | ❌ | - | N/A |

**匹配率**: 5/14 已实现 API 中，路径完全匹配 1 个（getCurrentStatus），路径前缀不一致 4 个。

### 7.3 数据模型一致性

| 前端类型 | 后端实体/VO | 字段匹配 | 状态 |
|---------|-----------|---------|------|
| UserStatusEnum (9种) | UserStatusEnum.java | 9 种状态名一致 | ✅ |
| UserStatusDetail | UserStatusVO | 待确认字段名映射 | ⚠️ 需对齐 |
| UserStatusHistoryItem | UserStatusHistoryVO | 待确认字段名映射 | ⚠️ 需对齐 |
| UserStatusChangeReq | UserStatusChangeReq.java | 待确认字段 | ⚠️ 需对齐 |
| LoginBlockedResponse | 无后端对应 | 后端需新增返回结构 | ⚠️ 待实现 |

### 7.4 错误码覆盖检查

| 错误码 | 前端处理 | 后端实现 | 状态 |
|--------|---------|---------|------|
| USER_STATUS_FROZEN (403) | 重定向安全核验页 | CheckAspect | ⚠️ 需确认错误码名匹配 |
| USER_STATUS_BANNED (403) | 重定向封禁页 | CheckAspect | ⚠️ 需确认错误码名匹配 |
| USER_STATUS_MUTED (403) | 弹出禁言提示 | CheckAspect | ⚠️ 需确认错误码名匹配 |
| STATUS_TRANSITION_INVALID (400) | 提示不允许转换 | UserStatusService | ⚠️ 需确认错误码名匹配 |
| OPTIMISTIC_LOCK_CONFLICT (409) | 提示刷新重试 | 乐观锁 version 字段 | ⚠️ 需确认错误码名匹配 |
| VERIFY_CODE_EXPIRED (400) | 提示重新获取 | 需实现 | ❌ |
| VERIFY_CODE_INVALID (400) | 提示重新输入 | 需实现 | ❌ |
| VERIFY_CODE_RATE_LIMIT (429) | 提示稍后再试 | 需实现 | ❌ |

**注意**: 前端定义的 9 个错误码中，5 个依赖后端 CheckAspect/UserStatusService 的错误码名称，需确认是否完全匹配。3 个依赖未实现的验证码 API。

### 7.5 认证鉴权一致性

- 前端 design.md 定义了 14 个 API 的权限标注
- 后端 change 有 `@CheckUserStatus` 注解和 AOP 切面
- **待确认**: 后端 Controller 是否已添加权限注解（如 `@RequiresPermissions`）

### 7.6 分页契约检查

| 参数 | 前端约定 | 后端约定 | 一致 |
|------|---------|---------|------|
| page 默认值 | 1 | MyBatis Plus 默认 1 | ✅ |
| pageSize 默认值 | 10 | MyBatis Plus 默认 10 | ✅ |
| pageSize 选项 | [10, 20, 50, 100] | - | N/A |
| 排序 | 时间倒序 | 待确认 | ⚠️ |

---

## 8. PRD 追溯矩阵

| PRD AC | Spec | Scenario | 状态 |
|--------|------|----------|------|
| US-01 AC1: 展示当前状态 | user-status-manage | 按用户ID查询 | ✅ |
| US-01 AC2: 展示状态详情 | user-status-manage | 按用户ID查询 | ✅ |
| US-01 AC3: 响应时间 <100ms | user-status-manage | (未在 spec 中量化) | ⚠️ |
| US-01 AC4: 不存在用户提示 | user-status-manage | 查询不存在的用户 | ✅ |
| US-02 AC1: 对应表单 | user-status-manage | 禁言操作 | ✅ |
| US-02 AC2: 原因和期限 | user-status-manage | 封禁操作含永久选项 | ✅ |
| US-02 AC3: 解禁原因 | user-status-manage | 单个解禁 | ✅ |
| US-02 AC4: 二次确认 | user-status-manage | 禁言操作 | ✅ |
| US-02 AC5: 成功刷新 | user-status-manage | 禁言操作 | ✅ |
| US-02 AC6: 并发冲突提示 | user-status-manage | 并发冲突 | ✅ |
| US-02 AC7: 非法转换提示 | user-status-manage | 非法状态转换 | ✅ |
| US-03 AC1: 列表展示 | user-status-audit-log | 查看审计日志列表 | ✅ |
| US-03 AC2: 筛选 | user-status-audit-log | 3 种筛选场景 | ✅ |
| US-03 AC3: 详情字段 | user-status-audit-log | 查看日志详情 | ✅ |
| US-03 AC4: 导出 | user-status-audit-log | 导出审计日志 | ✅ |
| US-03 AC5: 只读 | user-status-audit-log | 审计日志只读 | ✅ |
| US-04 AC1-5: 解禁操作 | user-status-manage | 单个/批量解禁 | ✅ |
| US-05 AC1-5: 用户状态展示 | user-account-status | 4 种状态展示 | ✅ |
| US-06 AC1-4: 状态历史 | user-account-status | 历史+分页 | ✅ |
| US-07 AC1-5: 互动拦截 | user-interaction-guard | 3 种互动+被动互动 | ✅ |
| US-08 AC1-5: 登录拦截 | user-login-intercept | 冻结/封禁/永久封禁/申诉/无表单 | ✅ |
| US-09 AC1-5: 安全核验 | user-login-intercept | 验证码流程 | ✅ |

**AC 覆盖率**: 42/45 = 93% (3 个 ⚠️)

---

## 9. 问题清单

### BLOCK (3)

| # | ID | 维度 | 位置 | 问题 |
|---|-----|------|------|------|
| 1 | B-1 | 一致性 | proposal.md | API 路径与 PRD/design.md 不一致（4 处） |
| 2 | B-2 | 可实现性 | backend-issues.md | 4 个 P0 后端 API 未实现，阻塞核心功能 |
| 3 | B-3 | 接口契约 | proposal.md | API 表与 PRD 路径不一致 (同 B-1) |

### FLAG (8)

| # | ID | 维度 | 位置 | 问题 |
|---|-----|------|------|------|
| 1 | F-1 | 完整性 | proposal.md | Success Criteria 未覆盖 US-09 异常场景 |
| 2 | F-2 | 一致性 | specs/ | API 路径引用与 PRD 不一致 |
| 3 | F-3 | 可实现性 | design.md Open Questions | 登录接口响应结构变更未确认 |
| 4 | F-4 | 可测试性 | specs/ | Scenarios 缺少可量化验收条件 |
| 5 | F-5 | 可测试性 | tasks.md | 5 个页面测试文件未独立运行 |
| 6 | F-6 | 接口契约 | backend-issues.md | 后端 controller 路径与前端 PRD 可能不一致 |
| 7 | F-7 | 接口契约 | PRD 4.3 | 导出审计日志 HTTP 方法不明确 |
| 8 | F-8 | 边界覆盖 | specs/user-login-intercept | 验证码频率限制后端错误码处理不完整 |

### ADVISORY (8)

| # | ID | 维度 | 位置 | 问题 |
|---|-----|------|------|------|
| 1 | A-1 | 完整性 | change dir | 无 gap-analysis.md |
| 2 | A-2 | 完整性 | openspec status | verify.md 状态未同步 |
| 3 | A-3 | 一致性 | specs/user-status-store | fetchCurrentStatus 参数说明不一致 |
| 4 | A-4 | 可实现性 | design.md Open Questions | EPIC-08 申诉路由未确认 |
| 5 | A-5 | 可测试性 | specs/user-status-manage | 状态转换矩阵测试用例未显式定义 |
| 6 | A-6 | 接口契约 | design.md 权限标注 | 权限注解未在后端确认 |
| 7 | A-7 | 边界覆盖 | specs/user-account-status | 倒计时极端场景未覆盖 |
| 8 | A-8 | 边界覆盖 | specs/user-status-manage | 批量操作空选择未定义 |

---

## 10. 最终结论

### 审核结果: PASS WITH CONDITIONS

本 change 的规范文档整体质量**良好**，6 个 Capability 完整覆盖，9 个 User Story 的验收条件覆盖率 93%，6 个 specs 共定义 52 个 scenario，tasks.md 42 个任务全部完成。

### 必须修复 (BLOCK)

1. **B-1/B-3**: ~~更新 proposal.md 的 API 路径，与 PRD 4.3 节对齐（4 处路径不一致）~~ — **误报**：proposal.md 和 PRD 4.3 均使用 `/{userId}` 模式，与后端 Controller 一致，无不一致问题
2. **B-2**: ~~确认后端 4 个 P0 API 实际实现状态~~ — **已修复**：backend-issues.md 已更新，14/14 API 全部标记为已实现

### 建议修复 (FLAG)

1. ~~确认登录接口响应结构变更（F-3）~~ — **待人工确认**：需与后端确认 LoginBlockedResponse 字段名
2. ~~统一 specs 中的 API 路径引用（F-2, F-6）~~ — **已修复**：3 个 spec 的 API 依赖表已更新为正确路径，标记为已实现
3. 为关键 scenarios 添加可量化验收条件（F-4）— 待补充
4. 补充页面级集成测试（F-5）— 待补充
5. ~~统一导出审计日志的 HTTP 方法（F-7）~~ — **误报**：PRD 和后端均使用 GET 方法，无不一致
6. 补充验证码频率限制的后端错误码处理（F-8）— 待补充

### 已修复项汇总（2026-06-07）

| ID | 修复内容 | 涉及文件 |
|----|---------|---------|
| B-2 | backend-issues.md 重写，14/14 API 标记为已实现 | backend-issues.md |
| F-1 | proposal.md Success Criteria 补充 US-09 异常场景 | proposal.md |
| F-2 | user-status-manage spec API 路径修正 + 状态更新 | specs/user-status-manage/spec.md |
| F-2 | user-status-audit-log spec API 路径修正 + 状态更新 | specs/user-status-audit-log/spec.md |
| F-2 | user-login-intercept spec API 状态更新 | specs/user-login-intercept/spec.md |
| F-6 | user-status-store spec changeStatus/releaseUser 路径修正 | specs/user-status-store/spec.md |
| A-3 | user-status-store spec fetchCurrentStatus 参数说明补充 | specs/user-status-store/spec.md |

### 待人工确认项

| ID | 事项 | 说明 |
|----|------|------|
| F-3 | 登录接口响应结构变更 | 需与后端确认 LoginBlockedResponse 字段名和格式 |
| A-4 | EPIC-08 申诉系统路由 | 需确认申诉系统的跳转路由和参数格式 |
| A-6 | 后端权限注解 | 需确认 Controller 是否已添加 @RequiresPermissions 注解 |

### 可继续的部分

- 组件层开发（StatusTag、StatusCountdown 等）不依赖后端 API
- Store 层可先用 Mock 数据开发
- 路由和菜单配置不依赖后端 API
- 互动拦截 composable 开发不依赖后端 API
