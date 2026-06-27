# 验证审核报告：user-09-status-lifecycle-frontend

**验证时间**: 2026-06-04
**验证人**: openspec-verify-change agent
**Change 状态**: 未开始实施（0/42 任务完成）

---

## 验证结果摘要

| 维度 | 状态 | 详情 |
|------|------|------|
| 完整性 | ⚠️ 部分缺失 | 42 个任务均未完成；9 个后端 API 缺失 |
| 正确性 | ⚠️ 需要对齐 | 前端文档引用的 API 与后端实际不一致 |
| 一致性 | ✅ 基本一致 | 设计文档与 specs 一致，文件结构合理 |

---

## 后端 API 验证详情

### 已存在的 API（5/14）

| API | 路径 | 后端文件 | 状态 |
|-----|------|----------|------|
| getCurrentStatus | GET /api/v1/content/user-status/current | UserStatusController.java:50 | ✅ 存在 |
| getUserStatus | GET /api/v1/content/user-status/{userId} | UserStatusController.java:56 | ✅ 存在 |
| changeUserStatus | POST /api/v1/content/user-status/{userId}/change | UserStatusController.java:76 | ✅ 存在 |
| getStatusHistory | GET /api/v1/content/user-status/{userId}/history | UserStatusController.java:101 | ✅ 存在 |
| releaseUser | POST /api/v1/content/user-status/{userId}/release | UserStatusController.java:108 | ✅ 存在 |

### 缺失的 API（9/14）

| API | 预期路径 | 用途 | 优先级 |
|-----|----------|------|--------|
| getStatusList | GET /api/v1/content/user-status/list | 管理员分页查询用户状态列表 | 🔴 高 |
| getTransitions | GET /api/v1/content/user-status/transitions/{currentStatus} | 获取可转换状态列表 | 🔴 高 |
| batchReleaseUsers | POST /api/v1/content/user-status/batch-release | 批量解禁用户 | 🟡 中 |
| getAuditLogList | GET /api/v1/content/user-status/audit-logs | 审计日志分页查询 | 🔴 高 |
| getAuditLogDetail | GET /api/v1/content/user-status/audit-logs/{logId} | 审计日志详情 | 🟡 中 |
| getUserAuditLogs | GET /api/v1/content/user-status/users/{userId}/audit-logs | 用户审计日志 | 🟡 中 |
| exportAuditLogs | GET /api/v1/content/user-status/audit-logs/export | 导出审计日志 | 🟡 中 |
| verifySecurity | POST /api/v1/content/user-status/verify-security | 安全核验（冻结解冻） | 🔴 高 |
| sendVerifyCode | POST /api/v1/content/user-status/send-verify-code | 发送手机验证码 | 🔴 高 |

### 后端已有的基础设施

| 组件 | 路径 | 说明 |
|------|------|------|
| UserStatusEnum | entity/UserStatusEnum.java | 9 种状态枚举，完整 |
| UserStatusTransition | model/UserStatusTransition.java | 状态转换规则矩阵，完整 |
| UserStatusService | service/UserStatusService.java | 接口定义完整，含 getAllowedTransitions |
| UserStatusBizManageService | biz/UserStatusBizManageService.java | 含 batchChangeStatus 方法 |
| UserStatusAuditLog | entity/UserStatusAuditLog.java | 审计日志实体 |
| UserStatusAuditLogService | service/UserStatusAuditLogService.java | 审计日志服务 |
| UserStatusCheckAspect | aspect/UserStatusCheckAspect.java | 状态检查切面 |

---

## 前端文档问题列表

### 问题 1：API 路径不一致（CRITICAL）

**位置**: design.md, specs/user-status-manage/spec.md

**问题**: 
- design.md 第 31 行引用 `GET /api/v1/content/user-status/transitions/{currentStatus}`
- specs/user-status-manage/spec.md 第 31 行同样引用此路径
- 但后端 UserStatusController 中**没有**此端点

**影响**: 前端调用 transitions API 将返回 404

**建议**: 
1. 在 UserStatusController 中添加 `GET /transitions/{currentStatus}` 端点
2. 或者修改前端文档，说明 transitions 由前端本地判断（不推荐，违反 D1 决策）

### 问题 2：getCurrentStatus API 参数差异（WARNING）

**位置**: design.md, user-status-store/spec.md

**问题**:
- specs 中定义 `GET /api/v1/content/user-status/current` 无参数
- 后端实际实现需要 `@RequestParam("userId")` 参数

**影响**: 前端调用时需要传 userId 参数

**建议**: 
1. 修改后端 API，从登录态获取 userId（推荐）
2. 或修改前端文档，明确需要传 userId

### 问题 3：审计日志 API 完全缺失（CRITICAL）

**位置**: specs/user-status-audit-log/spec.md

**问题**: 
- 审计日志 spec 定义了 4 个需求（列表、详情、导出、只读）
- 但后端没有审计日志相关的 Controller 端点
- UserStatusAuditLogService 只有 writeAuditLog 方法，缺少查询方法

**影响**: 审计日志页面无法实现

**建议**: 
1. 创建 AuditLogController 或在 UserStatusController 中添加审计日志端点
2. 在 UserStatusAuditLogService 中添加 queryByPage、queryById、export 等方法

### 问题 4：安全核验 API 缺失（CRITICAL）

**位置**: specs/user-login-intercept/spec.md

**问题**:
- spec 定义了"安全核验解冻"需求
- 需要 verifySecurity 和 sendVerifyCode 两个 API
- 后端完全没有相关实现

**影响**: 冻结用户无法通过前端完成安全核验

**建议**:
1. 在 UserStatusController 中添加 `/verify-security` 和 `/send-verify-code` 端点
2. 实现手机验证码发送和验证逻辑
3. 验证通过后自动将 FROZEN 状态恢复为 NORMAL

### 问题 5：批量解禁 API 缺失（WARNING）

**位置**: specs/user-status-manage/spec.md

**问题**:
- spec 定义了"批量解禁"场景
- UserStatusBizManageService 有 batchChangeStatus 方法
- 但 Controller 层没有暴露批量解禁端点

**影响**: 管理员无法通过前端批量解禁

**建议**: 在 UserStatusController 中添加 `POST /batch-release` 端点

---

## 建议修复方案

### 方案 A：后端补充 API（推荐）

优先级排序：
1. 🔴 **高优先级**（阻塞核心功能）：
   - `GET /transitions/{currentStatus}` - 状态变更弹窗依赖
   - `GET /list` - 管理页列表查询依赖
   - `POST /verify-security` - 冻结解冻依赖
   - `POST /send-verify-code` - 安全核验依赖

2. 🟡 **中优先级**（影响完整功能）：
   - `GET /audit-logs` - 审计日志页依赖
   - `GET /audit-logs/{logId}` - 日志详情依赖
   - `POST /batch-release` - 批量操作依赖
   - `GET /audit-logs/export` - 导出功能依赖

### 方案 B：前端先行实现（临时方案）

如果后端 API 开发滞后，前端可先：
1. 使用 Mock 数据开发 UI
2. 封装 API 层时预留接口定义
3. 后端就绪后切换真实调用

---

## 文档修复建议

### design.md 修复

1. **第 52 行**：getCurrentStatus API 说明需补充"需传 userId 参数"或"从登录态获取"
2. **第 81 行**：File Structure 中缺少 AuditLogController 相关文件
3. **第 124-126 行**：Open Questions 需补充"后端 API 缺失清单"

### specs 修复

1. **user-status-store/spec.md 第 23 行**：fetchCurrentStatus 场景需明确 userId 来源
2. **user-login-intercept/spec.md**：需添加"后端 API 依赖"章节，列出 verifySecurity 和 sendVerifyCode

---

## 最终评估

**验证结果**: ⚠️ 需要修复后继续

**阻塞问题**:
- 4 个高优先级后端 API 缺失，阻塞核心功能实现
- 前端文档与后端实现存在不一致

**建议行动**:
1. 先补充后端缺失的 API（或创建 backend-issues.md 记录）
2. 修复前端文档中的 API 路径和参数描述
3. 确认后再开始前端实施

**可继续的部分**:
- 组件层开发（StatusTag、StatusCountdown 等）可先行
- Store 层可先用 Mock 数据开发
- 路由和菜单配置不依赖后端 API
