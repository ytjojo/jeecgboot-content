## Why

内容社区当前缺少统一的用户状态管理模型和审计机制。管理员无法高效查询和变更用户状态，用户无法查看自身账号状态及处罚详情，违规用户的互动拦截和登录拦截能力缺失。本变更补齐用户状态生命周期管理的前端能力，支撑平台治理合规运营。

## What Changes

**用户状态管理（管理员后台）**
- 新增用户状态管理页：支持按用户ID/用户名查询状态、手动变更状态（禁言/冻结/封禁/解禁等）、批量解禁操作
- 新增审计日志页：日志列表查询、筛选、详情查看、导出 Excel/CSV
- 状态变更基于后端动态返回的转换矩阵，前端不硬编码状态转换规则

**用户端状态展示**
- 新增个人中心"账号状态"页：展示当前状态标签、处罚详情、剩余时间倒计时、历史记录
- 新增登录拦截页：冻结/封禁用户登录时的拦截提示，不签发 token
- 新增安全核验页：冻结用户通过手机验证码解冻
- 新增互动拦截：禁言用户发表评论/私信/发动态时统一拦截提示

**状态一致性保障**
- UserStatusStore (Pinia) 缓存当前用户状态
- 请求级校验：后端独立校验状态，前端拦截仅作 UX 优化
- 互动 API 返回状态拦截错误码时，前端自动刷新 Store 并弹出提示

## Success Criteria

1. 管理员可通过用户状态管理页查询用户状态、执行状态变更和解禁操作，响应时间 <100ms
2. 管理员可通过审计日志页查看、筛选、导出审计日志
3. 用户可在个人中心查看自身状态、处罚详情和剩余时间倒计时
4. 冻结/封禁用户登录时被拦截，不签发 token，展示明确提示
5. 冻结用户可通过手机验证码安全核验解冻
6. 禁言用户发表评论/私信/发动态时被拦截并展示禁言信息
7. 所有状态变更自动记录审计日志
8. 移动端/平板/桌面端响应式布局适配

## Non-Goals

- 独立风控引擎 UI（机器学习异常检测界面）
- 审计日志冷存储管理界面
- 补偿机制管理界面（仅预留接口）
- WebSocket/SSE 实时推送状态变更（L3 层，后续迭代）

## Capabilities

### New Capabilities

- `user-status-manage`: 管理员后台用户状态管理页，包含状态查询、状态变更、批量解禁、状态历史查看
- `user-status-audit-log`: 管理员后台审计日志页，包含日志列表查询、筛选、详情查看、导出
- `user-account-status`: 用户端个人中心账号状态页，展示当前状态、处罚详情、剩余时间倒计时、历史记录
- `user-login-intercept`: 冻结/封禁用户登录拦截页及安全核验页
- `user-interaction-guard`: 禁言用户互动拦截（评论/私信/动态），统一 useStatusGuard composable
- `user-status-store`: UserStatusStore (Pinia) 状态管理，缓存当前用户状态并提供一致性保障

### Modified Capabilities

（无现有 spec 需要修改）

## Impact

- **新增页面**: 5 个页面组件（用户状态管理页、审计日志页、账号状态页、登录拦截页、安全核验页）
- **新增组件**: 6 个功能组件（StatusTag、StatusChangeModal、StatusReleaseModal、StatusHistoryDrawer、AuditLogDetailModal、StatusCountdown）
- **新增 Store**: UserStatusStore (Pinia)
- **新增 API 封装**: src/api/content/userStatus.ts，14 个接口
- **新增 Composable**: useStatusGuard（互动拦截）
- **路由变更**: 新增 5 条路由，需在后台管理菜单中添加菜单项
- **依赖**: EPIC-09 后端 API、EPIC-08 申诉系统、现有 JVxeTable/Form/Modal/Drawer 组件
- **登录流程变更**: 登录接口响应结构变更，冻结/封禁用户不签发 token

### API 接口依赖清单

| # | API 名称 | HTTP 方法 | 路径 | 后端状态 | 优先级 |
|---|---------|-----------|------|---------|--------|
| 1 | getCurrentStatus | GET | /api/content/user-status/current | ✅ 已实现 | - |
| 2 | getUserStatus | GET | /api/content/user-status/{userId} | ✅ 已实现 | - |
| 3 | changeUserStatus | POST | /api/content/user-status/{userId}/change | ✅ 已实现 | - |
| 4 | getStatusHistory | GET | /api/content/user-status/{userId}/history | ✅ 已实现 | - |
| 5 | releaseUser | POST | /api/content/user-status/{userId}/release | ✅ 已实现 | - |
| 6 | getTransitions | GET | /api/content/user-status/transitions/{currentStatus} | ❌ 未实现 | P0 |
| 7 | getStatusList | GET | /api/content/user-status/list | ❌ 未实现 | P0 |
| 8 | verifySecurity | POST | /api/content/user-status/verify-security | ❌ 未实现 | P0 |
| 9 | sendVerifyCode | POST | /api/content/user-status/send-verify-code | ❌ 未实现 | P0 |
| 10 | getAuditLogList | GET | /api/content/user-status/audit-logs | ❌ 未实现 | P1 |
| 11 | getAuditLogDetail | GET | /api/content/user-status/audit-logs/{logId} | ❌ 未实现 | P1 |
| 12 | batchReleaseUsers | POST | /api/content/user-status/batch-release | ❌ 未实现 | P1 |
| 13 | exportAuditLogs | GET | /api/content/user-status/audit-logs/export | ❌ 未实现 | P1 |
| 14 | getUserAuditLogs | GET | /api/content/user-status/users/{userId}/audit-logs | ❌ 未实现 | P1 |
