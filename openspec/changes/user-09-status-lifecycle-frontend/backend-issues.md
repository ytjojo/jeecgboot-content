# 后端 API 实现状态

**Change**: user-09-status-lifecycle-frontend
**创建时间**: 2026-06-04
**更新时间**: 2026-06-07
**状态**: ✅ 全部已实现

---

## 概述

前端 user-09-status-lifecycle-frontend 变更依赖 14 个后端 API，全部已在 `UserStatusController.java` 中实现（2026-06-07 确认）。

后端 `user-09-status-lifecycle` change 已完成核心服务层（状态机、审计日志、AOP、调度器、Controller 层 14 个端点）。

---

## 已实现的 API（14/14）

| # | API | 路径 | Controller 方法 | 状态 |
|---|-----|------|-----------------|------|
| 1 | getCurrentStatus | GET /api/content/user-status/current?userId={userId} | getCurrentUserStatus() | ✅ |
| 2 | getUserStatus | GET /api/content/user-status/{userId} | getUserStatus() | ✅ |
| 3 | changeUserStatus | POST /api/content/user-status/{userId}/change | changeUserStatus() | ✅ |
| 4 | getStatusHistory | GET /api/content/user-status/{userId}/history | getUserStatusHistory() | ✅ |
| 5 | releaseUser | POST /api/content/user-status/{userId}/release | releaseUserStatus() | ✅ |
| 6 | getTransitions | GET /api/content/user-status/transitions/{currentStatus} | getTransitions() | ✅ |
| 7 | getStatusList | GET /api/content/user-status/list | getStatusList() | ✅ |
| 8 | sendVerifyCode | POST /api/content/user-status/send-verify-code | sendVerifyCode() | ✅ |
| 9 | verifySecurity | POST /api/content/user-status/verify-security | verifySecurity() | ✅ |
| 10 | getAuditLogList | GET /api/content/user-status/audit-logs | getAuditLogList() | ✅ |
| 11 | getAuditLogDetail | GET /api/content/user-status/audit-logs/{logId} | getAuditLogDetail() | ✅ |
| 12 | batchReleaseUsers | POST /api/content/user-status/batch-release | batchReleaseUsers() | ✅ |
| 13 | exportAuditLogs | GET /api/content/user-status/audit-logs/export | exportAuditLogs() | ✅ |
| 14 | getUserAuditLogs | GET /api/content/user-status/users/{userId}/audit-logs | getUserAuditLogs() | ✅ |

### 参数注意点

- `getCurrentStatus` 需要 `userId` 参数，前端需从 `useUserStore` 获取当前登录用户 ID
- `changeUserStatus` 和 `releaseUser` 需要 `operatorId` 参数，前端需从 `useUserStore` 获取当前用户 ID

---

## 后端组件状态

### UserStatusCheckAspect
- **路径**: `aspect/UserStatusCheckAspect.java`
- **用途**: 状态检查切面，用于拦截违规用户操作
- **状态**: ✅ 已实现

### UserStatusAutoReleaseScheduler
- **路径**: `scheduler/UserStatusAutoReleaseScheduler.java`
- **用途**: 定时任务，自动释放到期的状态
- **状态**: ✅ 已实现

---

## 相关文件

- 后端控制器: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/userstatus/controller/UserStatusController.java`
- 状态枚举: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/userstatus/entity/UserStatusEnum.java`
- 状态转换: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/userstatus/model/UserStatusTransition.java`
- 业务服务: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/userstatus/biz/UserStatusBizManageService.java`
- 审计日志服务: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/userstatus/service/UserStatusAuditLogService.java`
