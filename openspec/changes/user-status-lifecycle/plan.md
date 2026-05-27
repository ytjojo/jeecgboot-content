# Execution Plan
> **For agentic workers:** Use superpowers:subagent-driven-development
> to implement this plan task-by-task.

## Steps

### Step 1: RED — 用户状态枚举与实体层测试
- Test file: `src/test/java/org/jeecg/modules/content/userstatus/entity/UserStatusEnumTest.java`
- Assertion: 验证枚举包含 9 种状态、状态码唯一、元数据完整
- Expected failure: 枚举类不存在
- Verify: `mvn test -pl jeecg-boot-module/jeecg-module-content -Dtest=UserStatusEnumTest`
- Commit: "test: add UserStatusEnum test cases"

### Step 2: GREEN — 实现用户状态枚举与实体
- Pass test from: Step 1
- Minimal code: 创建 UserStatusEnum（9 种状态）、UserStatusAuditLog 实体、UserStatusTransition 转换规则、UserRestriction 限制定义
- Verify: `mvn test -pl jeecg-boot-module/jeecg-module-content -Dtest=UserStatusEnumTest`
- Commit: "feat: implement UserStatusEnum and entity classes"

### Step 3: RED — 状态机核心逻辑测试
- Test file: `src/test/java/org/jeecg/modules/content/userstatus/service/UserStatusServiceTest.java`
- Assertion: 合法转换通过、非法转换拒绝、管理员强制转换、并发冲突检测
- Expected failure: UserStatusService 不存在
- Verify: `mvn test -pl jeecg-boot-module/jeecg-module-content -Dtest=UserStatusServiceTest`
- Commit: "test: add UserStatusService state machine tests"

### Step 4: GREEN — 实现状态机核心逻辑
- Pass test from: Step 3
- Minimal code: UserStatusService（状态转换验证、乐观锁）、UserStatusServiceImpl
- Verify: `mvn test -pl jeecg-boot-module/jeecg-module-content -Dtest=UserStatusServiceTest`
- Commit: "feat: implement UserStatusService state machine"

### Step 5: RED — 审计日志服务测试
- Test file: `src/test/java/org/jeecg/modules/content/userstatus/service/UserStatusAuditLogServiceTest.java`
- Assertion: 日志写入成功、按用户查询、时间范围筛选、防篡改验证
- Expected failure: UserStatusAuditLogService 不存在
- Verify: `mvn test -pl jeecg-boot-module/jeecg-module-content -Dtest=UserStatusAuditLogServiceTest`
- Commit: "test: add UserStatusAuditLogService tests"

### Step 6: GREEN — 实现审计日志服务
- Pass test from: Step 5
- Minimal code: UserStatusAuditLogMapper（含 XML）、UserStatusAuditLogServiceImpl、审计日志查询接口
- Verify: `mvn test -pl jeecg-boot-module/jeecg-module-content -Dtest=UserStatusAuditLogServiceTest`
- Commit: "feat: implement audit log service and mapper"

### Step 7: RED — 状态变更编排测试
- Test file: `src/test/java/org/jeecg/modules/content/userstatus/biz/UserStatusBizManageServiceTest.java`
- Assertion: 状态变更 + 审计日志在同一事务、事务回滚验证
- Expected failure: UserStatusBizManageService 不存在
- Verify: `mvn test -pl jeecg-boot-module/jeecg-module-content -Dtest=UserStatusBizManageServiceTest`
- Commit: "test: add UserStatusBizManageService tests"

### Step 8: GREEN — 实现状态变更编排
- Pass test from: Step 7
- Minimal code: UserStatusBizManageService（编排状态变更 + 审计写入）
- Verify: `mvn test -pl jeecg-boot-module/jeecg-module-content -Dtest=UserStatusBizManageServiceTest`
- Commit: "feat: implement UserStatusBizManageService"

### Step 9: RED — 功能限制 AOP 切面测试
- Test file: `src/test/java/org/jeecg/modules/content/userstatus/aspect/UserStatusCheckAspectTest.java`
- Assertion: 各状态拦截正确、错误响应格式正确、注解参数解析
- Expected failure: UserStatusCheckAspect 不存在
- Verify: `mvn test -pl jeecg-boot-module/jeecg-module-content -Dtest=UserStatusCheckAspectTest`
- Commit: "test: add UserStatusCheckAspect tests"

### Step 10: GREEN — 实现功能限制注解与切面
- Pass test from: Step 9
- Minimal code: @CheckUserStatus 注解、UserStatusCheckAspect 切面
- Verify: `mvn test -pl jeecg-boot-module/jeecg-module-content -Dtest=UserStatusCheckAspectTest`
- Commit: "feat: implement @CheckUserStatus annotation and AOP aspect"

### Step 11: RED — 自动解禁定时任务测试
- Test file: `src/test/java/org/jeecg/modules/content/userstatus/scheduler/UserStatusAutoReleaseSchedulerTest.java`
- Assertion: 到期解禁、永久封禁跳过、异常告警
- Expected failure: UserStatusAutoReleaseScheduler 不存在
- Verify: `mvn test -pl jeecg-boot-module/jeecg-module-content -Dtest=UserStatusAutoReleaseSchedulerTest`
- Commit: "test: add auto-release scheduler tests"

### Step 12: GREEN — 实现自动解禁与恢复机制
- Pass test from: Step 11
- Minimal code: UserStatusAutoReleaseScheduler、人工解禁接口、申诉恢复回调接口
- Verify: `mvn test -pl jeecg-boot-module/jeecg-module-content -Dtest=UserStatusAutoReleaseSchedulerTest`
- Commit: "feat: implement auto-release scheduler and recovery APIs"

### Step 13: GREEN — 实现状态管理 Controller
- Pass test from: 所有前置测试
- Minimal code: UserStatusController（状态查询、状态变更、状态历史）、请求/响应对象
- Verify: `mvn test -pl jeecg-boot-module/jeecg-module-content`
- Commit: "feat: implement UserStatusController APIs"

### Step 14: 验证 — 全量测试
- Run: `mvn test -pl jeecg-boot-module/jeecg-module-content`
- Verify: 所有测试通过，覆盖率满足要求
- Commit: "chore: verify all user-status-lifecycle tests pass"
