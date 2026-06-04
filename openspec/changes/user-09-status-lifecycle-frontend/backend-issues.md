# 后端遗留问题清单

**Change**: user-09-status-lifecycle-frontend
**创建时间**: 2026-06-04
**状态**: 待后端开发

---

## 概述

前端 user-09-status-lifecycle-frontend 变更依赖 14 个后端 API，其中 5 个已实现，9 个需要后端补充。

---

## 已实现的 API（5/14）

| API | 路径 | Controller 方法 | 状态 |
|-----|------|-----------------|------|
| getCurrentStatus | GET /api/content/user-status/current | getCurrentUserStatus() | ✅ |
| getUserStatus | GET /api/content/user-status/{userId} | getUserStatus() | ✅ |
| changeUserStatus | POST /api/content/user-status/{userId}/change | changeUserStatus() | ✅ |
| getStatusHistory | GET /api/content/user-status/{userId}/history | getUserStatusHistory() | ✅ |
| releaseUser | POST /api/content/user-status/{userId}/release | releaseUserStatus() | ✅ |

---

## 待实现的 API（9/14）

### 高优先级（阻塞核心功能）

#### 1. getTransitions - 获取可转换状态列表
- **路径**: `GET /api/content/user-status/transitions/{currentStatus}`
- **用途**: 状态变更弹窗根据当前状态获取允许转换的目标状态列表
- **后端基础**: `UserStatusTransition.getAllowedTransitions()` 已实现
- **实现建议**:
  ```java
  @GetMapping("/transitions/{currentStatus}")
  public Result<Set<UserStatusEnum>> getTransitions(@PathVariable String currentStatus) {
      UserStatusEnum status = UserStatusEnum.fromNameOrThrow(currentStatus);
      return Result.OK(UserStatusTransition.getAllowedTransitions(status));
  }
  ```
- **关联前端任务**: 3.3 StatusChangeModal.vue

#### 2. getStatusList - 管理员分页查询用户状态
- **路径**: `GET /api/content/user-status/list`
- **用途**: 管理页列表查询，支持按用户ID、状态筛选
- **参数**: userId (可选), status (可选), page, pageSize
- **后端基础**: 需新增，可复用 ContentUserProfileMapper
- **实现建议**:
  ```java
  @GetMapping("/list")
  public Result<IPage<UserStatusVO>> getStatusList(
      @RequestParam(required = false) String userId,
      @RequestParam(required = false) String status,
      @RequestParam(defaultValue = "1") Integer page,
      @RequestParam(defaultValue = "10") Integer pageSize) {
      // 查询 ContentUserProfile 表，筛选状态
  }
  ```
- **关联前端任务**: 4.1 UserStatusManage.vue

#### 3. verifySecurity - 安全核验
- **路径**: `POST /api/content/user-status/verify-security`
- **用途**: 冻结用户通过手机验证码完成安全核验，恢复为正常状态
- **参数**: phone, verifyCode
- **后端基础**: 需新增
- **实现要点**:
  1. 校验验证码（从 Redis 获取）
  2. 将用户状态从 FROZEN 恢复为 NORMAL
  3. 写入审计日志
  4. 返回成功/失败
- **关联前端任务**: 5.3 SecurityVerify.vue

#### 4. sendVerifyCode - 发送验证码
- **路径**: `POST /api/content/user-status/send-verify-code`
- **用途**: 发送手机验证码用于安全核验
- **参数**: phone
- **后端基础**: 需新增，可复用现有短信服务
- **实现要点**:
  1. 生成 6 位随机验证码
  2. 存储到 Redis，key 为 `verify:code:{phone}`，有效期 5 分钟
  3. 调用短信服务发送
  4. 限制 60 秒内只能发送一次
- **关联前端任务**: 5.3 SecurityVerify.vue

---

### 中优先级（影响完整功能）

#### 5. getAuditLogList - 审计日志分页查询
- **路径**: `GET /api/content/user-status/audit-logs`
- **用途**: 审计日志页列表查询
- **参数**: userId (可选), startTime, endTime, operatorType (可选), page, pageSize
- **后端基础**: `UserStatusAuditLogService` 需扩展查询方法
- **实现建议**:
  ```java
  @GetMapping("/audit-logs")
  public Result<IPage<UserStatusAuditLog>> getAuditLogList(
      @RequestParam(required = false) String userId,
      @RequestParam(required = false) String startTime,
      @RequestParam(required = false) String endTime,
      @RequestParam(required = false) String operatorType,
      @RequestParam(defaultValue = "1") Integer page,
      @RequestParam(defaultValue = "10") Integer pageSize) {
      // 查询 UserStatusAuditLog 表
  }
  ```
- **关联前端任务**: 4.2 AuditLogList.vue

#### 6. getAuditLogDetail - 审计日志详情
- **路径**: `GET /api/content/user-status/audit-logs/{logId}`
- **用途**: 查看审计日志完整字段
- **后端基础**: `UserStatusAuditLogService` 需扩展
- **关联前端任务**: 3.6 AuditLogDetailModal.vue

#### 7. batchReleaseUsers - 批量解禁
- **路径**: `POST /api/content/user-status/batch-release`
- **用途**: 管理员批量解禁用户
- **参数**: userIds[], reason, operatorId
- **后端基础**: `UserStatusBizManageService.batchChangeStatus()` 已实现
- **实现建议**:
  ```java
  @PostMapping("/batch-release")
  public Result<Void> batchReleaseUsers(
      @RequestBody List<String> userIds,
      @RequestParam String reason,
      @RequestParam String operatorId) {
      bizManageService.batchChangeStatus(userIds, null, UserStatusEnum.NORMAL, reason, operatorId, "ADMIN");
      return Result.OK();
  }
  ```
- **关联前端任务**: 4.1 UserStatusManage.vue

#### 8. exportAuditLogs - 导出审计日志
- **路径**: `GET /api/content/user-status/audit-logs/export`
- **用途**: 导出审计日志为 Excel/CSV
- **参数**: userId (可选), startTime, endTime, format (excel/csv)
- **后端基础**: 需新增，可使用 EasyExcel
- **关联前端任务**: 4.2 AuditLogList.vue

#### 9. getUserAuditLogs - 用户审计日志
- **路径**: `GET /api/content/user-status/users/{userId}/audit-logs`
- **用途**: 查询指定用户的审计日志
- **后端基础**: `UserStatusAuditLogService.queryByUserId()` 已存在，需包装分页
- **关联前端任务**: 3.5 StatusHistoryDrawer.vue

---

## 后端已有但需确认的组件

### UserStatusCheckAspect
- **路径**: `aspect/UserStatusCheckAspect.java`
- **用途**: 状态检查切面，用于拦截违规用户操作
- **状态**: 已实现，需确认是否满足前端 useStatusGuard 的需求

### UserStatusAutoReleaseScheduler
- **路径**: `scheduler/UserStatusAutoReleaseScheduler.java`
- **用途**: 定时任务，自动释放到期的状态
- **状态**: 已实现，前端倒计时归零时需调用后端验证

---

## 实施建议

### 后端开发顺序
1. **第一批**（阻塞前端开发）:
   - getTransitions
   - getStatusList
   
2. **第二批**（阻塞联调）:
   - sendVerifyCode
   - verifySecurity
   
3. **第三批**（完善功能）:
   - getAuditLogList
   - getAuditLogDetail
   - batchReleaseUsers
   - exportAuditLogs
   - getUserAuditLogs

### 前端先行方案
如果后端开发滞后，前端可：
1. 使用 Mock 数据开发 UI 组件
2. API 层预留接口定义，后端就绪后切换
3. 优先开发不依赖后端 API 的部分（组件、路由、菜单）

---

## 相关文件

- 后端控制器: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/userstatus/controller/UserStatusController.java`
- 状态枚举: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/userstatus/entity/UserStatusEnum.java`
- 状态转换: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/userstatus/model/UserStatusTransition.java`
- 业务服务: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/userstatus/biz/UserStatusBizManageService.java`
- 审计日志服务: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/userstatus/service/UserStatusAuditLogService.java`
