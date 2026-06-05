## Context

JeecgBoot 内容社区前端基于 Vue 3 + TypeScript + Vite 6 + Ant Design Vue 4 + Pinia 技术栈。当前缺少用户状态管理的前端能力，管理员无法查询/变更用户状态，用户无法查看自身状态，违规用户的互动和登录拦截缺失。

后端 EPIC-09 已定义 14 个 API 接口（用户状态管理 + 审计日志），本变更实现对应的前端页面和交互逻辑。

约束：
- 复用现有 JVxeTable、Form、Modal、Drawer、Description 等组件
- 遵循项目既有目录结构和编码规范
- 登录接口响应结构需配合后端变更

## Goals / Non-Goals

**Goals:**
- 管理员可查询用户状态、执行状态变更和解禁操作
- 管理员可查看和导出审计日志
- 用户可查看自身状态、处罚详情和剩余时间
- 冻结/封禁用户登录时被拦截，不签发 token
- 禁言用户互动时被统一拦截
- 状态变更自动记录审计日志

**Non-Goals:**
- WebSocket/SSE 实时推送（L3 层）
- 风控引擎 UI
- 审计日志冷存储管理界面

## Decisions

### D1: 状态转换矩阵由后端 API 动态返回

**选择**: 前端不硬编码状态转换规则，通过 `GET /api/content/user-status/transitions/{currentStatus}` 获取可转换状态列表。

**理由**: 状态转换规则可能随业务变化，后端统一管理避免前后端规则不一致。前端仅做二次校验。

**后端依赖**: 此 API 当前未实现，需要后端补充。后端已有 `UserStatusTransition.getAllowedTransitions()` 方法，只需在 Controller 层暴露端点。

**替代方案**: 前端硬编码转换矩阵 — 优点是无额外请求，缺点是维护成本高、前后端可能不一致。

### D2: 登录拦截方案 — 不签发 token

**选择**: 冻结/封禁用户登录时，后端不签发 token，直接返回 403 + 状态信息，前端跳转拦截页。

**理由**: 避免"登录成功再拦截"的安全隐患（token 已签发但功能受限）。

**替代方案**: 登录成功后前端检查状态再拦截 — 安全风险高，token 泄露。

### D3: 互动拦截采用 useStatusGuard composable 统一管理

**选择**: 抽取 `useStatusGuard` composable，所有互动入口（评论/私信/动态）统一调用。

**理由**: 避免各入口散落独立的状态检查逻辑，便于维护和扩展。前端拦截仅作 UX 优化，后端独立校验为安全兜底。

### D4: 状态倒计时纯前端计算

**选择**: 基于 `dayjs(endTime).diff(dayjs())` 计算剩余时间，`setInterval` 每秒更新。监听 `visibilitychange` 修正后台挂起偏差。倒计时归零时调用后端验证是否已自动解禁。

**理由**: 简单高效，无需额外 WebSocket 连接。

### D5: 审计日志导出由后端生成文件流

**选择**: 前端调用导出 API，后端生成 Excel/CSV 文件流，前端通过 blob 下载。

**理由**: 万级数据量前端生成性能差，后端生成更可靠。

### D6: 响应拦截统一处理状态错误码

**选择**: 在 HTTP 响应拦截器中，403 + 用户状态相关错误码时，自动刷新 UserStatusStore 并重定向到对应拦截页。

**理由**: 统一处理避免各业务组件重复判断。

## Risks / Trade-offs

- **[风险] 状态一致性延迟** — 在线用户被管理员变更状态后，前端缓存可能仍是旧值。缓解：L1 本地缓存 + L2 请求级校验双重保障，关键操作后端独立校验。
- **[风险] 登录接口变更影响现有登录流程** — 缓解：登录拦截页路由守卫检查，无拦截数据时重定向到登录页。
- **[风险] 倒计时精度偏差** — 页面后台挂起时 setInterval 不准确。缓解：监听 visibilitychange 事件修正。
- **[权衡] 状态查询额外请求** — 每次页面刷新需调用 fetchCurrentStatus()。权衡：缓存到 Pinia Store，单次会话内有效。

## 错误码定义

前端需处理以下错误码，统一在 HTTP 响应拦截器中处理：

| 错误码 | HTTP 状态 | 含义 | 前端处理 |
|--------|-----------|------|----------|
| USER_STATUS_FROZEN | 403 | 用户已冻结 | 刷新 Store，重定向到 /login/verify |
| USER_STATUS_BANNED | 403 | 用户已封禁 | 刷新 Store，重定向到 /login/blocked |
| USER_STATUS_MUTED | 403 | 用户已禁言 | 刷新 Store，弹出禁言提示 |
| USER_STATUS_NOT_FOUND | 404 | 用户资料不存在 | 提示"用户不存在" |
| STATUS_TRANSITION_INVALID | 400 | 非法状态转换 | 提示"当前状态不允许执行此操作" |
| OPTIMISTIC_LOCK_CONFLICT | 409 | 乐观锁冲突 | 提示"状态已变更，请刷新后重试" |
| VERIFY_CODE_EXPIRED | 400 | 验证码已过期 | 提示"验证码已过期，请重新获取" |
| VERIFY_CODE_INVALID | 400 | 验证码错误 | 提示"验证码错误，请重新输入" |
| VERIFY_CODE_RATE_LIMIT | 429 | 验证码发送过于频繁 | 提示"请稍后再试"，按钮显示倒计时 |

## 分页参数约定

| 参数 | 默认值 | 说明 |
|------|--------|------|
| page | 1 | 页码，从 1 开始 |
| pageSize | 10 | 每页条数 |
| pageSize 选项 | [10, 20, 50, 100] | 前端下拉选项 |

## 权限标注

| API | 权限要求 | 说明 |
|-----|---------|------|
| GET /current | 登录用户 | 查询自身状态 |
| GET /{userId} | admin:user-status:query | 管理员查询指定用户 |
| POST /{userId}/change | admin:user-status:manage | 管理员变更状态 |
| GET /{userId}/history | admin:user-status:query | 管理员查看历史 |
| POST /{userId}/release | admin:user-status:manage | 管理员解禁 |
| GET /transitions/{status} | admin:user-status:query | 获取可转换状态 |
| GET /list | admin:user-status:query | 分页查询列表 |
| POST /batch-release | admin:user-status:manage | 批量解禁 |
| GET /audit-logs | admin:audit-log:query | 审计日志查询 |
| GET /audit-logs/{id} | admin:audit-log:query | 审计日志详情 |
| GET /audit-logs/export | admin:audit-log:export | 导出审计日志 |
| GET /users/{id}/audit-logs | admin:audit-log:query | 用户审计日志 |
| POST /send-verify-code | 登录用户 | 发送验证码 |
| POST /verify-security | 登录用户 | 安全核验 |

## File Structure

```
jeecgboot-vue3/src/
├── api/content/
│   └── userStatus.ts                          # API 封装（14 个接口）
├── components/jeecg/UserStatus/
│   ├── StatusTag.vue                          # 状态标签组件
│   ├── StatusChangeModal.vue                  # 状态变更弹窗
│   ├── StatusReleaseModal.vue                 # 解禁确认弹窗
│   ├── StatusHistoryDrawer.vue                # 状态历史抽屉
│   ├── AuditLogDetailModal.vue                # 审计日志详情弹窗
│   └── StatusCountdown.vue                    # 状态倒计时组件
├── composables/
│   └── useStatusGuard.ts                      # 互动拦截 composable
├── store/modules/
│   └── userStatus.ts                          # UserStatusStore (Pinia)
├── views/content/user-status/
│   ├── manage/index.vue                       # 用户状态管理页
│   └── audit-log/index.vue                    # 审计日志页
├── views/user/account-status/
│   └── index.vue                              # 账号状态页
└── views/login/
    ├── blocked/index.vue                      # 登录拦截页
    └── verify/index.vue                       # 安全核验页
```

## Test Strategy

每个组件和页面编写对应的单元测试文件：

- `userStatus.test.ts` — API 封装函数测试（mock HTTP 请求）
- `userStatusStore.test.ts` — Pinia Store 测试（actions、getters、状态管理）
- `useStatusGuard.test.ts` — 互动拦截 composable 测试
- `StatusTag.test.vue` — 状态标签渲染测试（9 种状态颜色映射）
- `StatusCountdown.test.vue` — 倒计时逻辑测试（精度修正、归零回调）
- `UserStatusManage.test.vue` — 管理页集成测试（查询、变更、解禁流程）
- `AuditLogList.test.vue` — 审计日志页测试（筛选、导出）
- `AccountStatus.test.vue` — 用户端状态页测试
- `LoginBlocked.test.vue` — 登录拦截页测试
- `SecurityVerify.test.vue` — 安全核验页测试（验证码流程）

## Migration Plan

N/A — 本变更为纯前端新增功能，不涉及部署变更。登录接口响应结构变更需与后端同步上线。

## Open Questions

- 登录接口响应结构变更的具体字段名和格式，需与后端对齐
- EPIC-08 申诉系统的跳转路由和参数格式
- 后台管理菜单配置方式（是否需要新增菜单数据脚本）

## 后端 API 依赖清单

以下 API 在后端尚未实现，需要后端开发完成后前端才能完整联调：

### 高优先级（阻塞核心功能）
| API | 路径 | 用途 | 后端基础 |
|-----|------|------|----------|
| getTransitions | GET /transitions/{currentStatus} | 获取可转换状态列表 | UserStatusTransition 已实现 |
| getStatusList | GET /list | 管理员分页查询用户状态 | 需新增 |
| verifySecurity | POST /verify-security | 安全核验解冻 | 需新增 |
| sendVerifyCode | POST /send-verify-code | 发送手机验证码 | 需新增 |

### 中优先级（影响完整功能）
| API | 路径 | 用途 | 后端基础 |
|-----|------|------|----------|
| getAuditLogList | GET /audit-logs | 审计日志分页查询 | UserStatusAuditLogService 需扩展 |
| getAuditLogDetail | GET /audit-logs/{logId} | 审计日志详情 | UserStatusAuditLogService 需扩展 |
| batchReleaseUsers | POST /batch-release | 批量解禁 | UserStatusBizManageService 已有 batchChangeStatus |
| exportAuditLogs | GET /audit-logs/export | 导出审计日志 | 需新增 |
| getUserAuditLogs | GET /users/{userId}/audit-logs | 用户审计日志 | UserStatusAuditLogService 需扩展 |
