## 1. 数据库与实体层

- [x] 1.1 创建审计日志表 content_user_status_audit_log 的 SQL 脚本（含字段：log_id, user_id, from_status, to_status, operator_id, operator_type, trigger_reason, rule_id, start_time, end_time, remark, ip_address, created_at）
- [x] 1.2 用户表增加状态字段（user_status, status_start_time, status_end_time, status_reason, status_operator_id, version）
- [x] 1.3 创建 UserStatusEnum 枚举类（9 种状态，含状态码、名称、描述）
- [x] 1.4 创建 UserStatusAuditLog 实体类
- [x] 1.5 创建 UserStatusTransition 状态转换规则定义（Map<状态, Set<允许目标状态>>）
- [x] 1.6 创建 UserRestriction 功能限制定义（每种状态对应的受限操作列表）

## 2. 状态机核心逻辑（先写测试）

- [x] 2.1 编写 UserStatusServiceTest 测试用例：合法状态转换、非法转换拒绝、管理员强制转换、并发冲突
- [x] 2.2 实现 UserStatusService：状态转换验证、状态变更执行（含乐观锁）
- [x] 2.3 编写 UserStatusBizManageServiceTest 测试用例：状态变更编排（含审计写入）、事务回滚
- [x] 2.4 实现 UserStatusBizManageService：编排状态变更 + 审计日志写入

## 3. 审计日志系统（先写测试）

- [x] 3.1 创建 UserStatusAuditLogMapper（含 XML 映射文件）
- [x] 3.2 编写 UserStatusAuditLogServiceTest 测试用例：日志写入、按用户查询、时间范围筛选、防篡改
- [x] 3.3 实现 UserStatusAuditLogService：日志写入、查询、导出
- [x] 3.4 实现审计日志查询接口 GET /api/v1/content/user-status-audit/user/{userId}

## 4. 功能限制策略（先写测试）

- [x] 4.1 创建 @CheckUserStatus 注解（allow/forbid 参数）
- [x] 4.2 编写 UserStatusCheckAspectTest 测试用例：各状态拦截、注解参数解析、错误响应格式
- [x] 4.3 实现 UserStatusCheckAspect AOP 切面
- [ ] 4.4 在现有内容社区接口上添加 @CheckUserStatus 注解（评论、私信、动态发布等）

## 5. 状态查询与管理接口

- [x] 5.1 创建 UserStatusVO、UserStatusHistoryVO 响应对象
- [x] 5.2 创建 UserStatusChangeReq、UserStatusQueryReq 请求对象
- [x] 5.3 实现 UserStatusController：当前状态查询、指定用户状态查询（管理员）、状态变更（管理员）、状态历史查询

## 6. 自动解禁与恢复机制（先写测试）

- [x] 6.1 编写 UserStatusAutoReleaseSchedulerTest 测试用例：到期解禁、永久封禁跳过、异常处理
- [x] 6.2 实现 UserStatusAutoReleaseScheduler 定时任务（每 5 分钟扫描）
- [x] 6.3 实现人工解禁接口 POST /api/v1/content/user-status/{userId}/release
- [ ] 6.4 实现申诉恢复回调接口（与 EPIC-08 集成）
- [ ] 6.5 实现解禁通知（站内消息）

## 7. 验证与集成

- [x] 7.1 运行全部单元测试，确保通过（36 tests, 0 failures）
- [x] 7.2 验证状态机所有合法/非法转换路径（UserStatusServiceTest 11 tests）
- [x] 7.3 验证功能限制注解在各接口上的拦截效果（UserStatusCheckAspectTest 5 tests）
- [x] 7.4 验证审计日志完整性和查询功能（UserStatusAuditLogServiceTest 5 tests）
- [x] 7.5 验证自动解禁定时任务正常执行（UserStatusAutoReleaseSchedulerTest 4 tests）
