## ADDED Requirements

### Requirement: 审计日志自动记录
系统 SHALL 在每次用户状态变更时自动生成审计日志记录。

#### Scenario: 状态变更生成日志
- GIVEN: 用户状态从 NORMAL 变更为 MUTED
- WHEN: 状态变更事务提交
- THEN: 审计日志表新增一条记录，包含变更时间、用户 ID、原状态、新状态、操作人、原因、开始时间、结束时间

#### Scenario: 日志完整性
- GIVEN: 审计日志记录
- WHEN: 查询日志详情
- THEN: 包含 log_id、user_id、from_status、to_status、operator_id（系统/管理员 ID）、operator_type（SYSTEM/ADMIN）、trigger_reason、rule_id（可选）、start_time、end_time、remark、ip_address、created_at

### Requirement: 操作人追溯
系统 SHALL 记录状态变更的操作人信息，支持管理员操作和系统自动操作的区分。

#### Scenario: 管理员操作记录
- GIVEN: 管理员 ID 为 admin001 执行封禁操作
- WHEN: 状态变更完成
- THEN: 审计日志 operator_id = "admin001"，operator_type = "ADMIN"，ip_address = 管理员操作 IP

#### Scenario: 系统自动操作记录
- GIVEN: 定时任务自动解禁
- WHEN: 状态变更完成
- THEN: 审计日志 operator_id = "SYSTEM"，operator_type = "SYSTEM"，rule_id = 触发规则 ID

### Requirement: 审计日志查询
系统 SHALL 提供审计日志查询接口，支持按用户和时间范围查询。

#### Scenario: 查询用户状态历史
- GIVEN: 用户 ID 为 user001
- WHEN: 调用 GET /api/v1/content/user-status-audit/user/{userId}
- THEN: 返回该用户按时间倒序排列的所有状态变更记录

#### Scenario: 时间范围筛选
- GIVEN: 查询参数 startTime 和 endTime
- WHEN: 调用审计日志查询接口
- THEN: 仅返回指定时间范围内的日志记录

### Requirement: 审计日志导出
系统 SHALL 支持审计日志导出功能，用于合规审计。

#### Scenario: 导出审计报告
- GIVEN: 管理员请求导出审计日志
- WHEN: 调用导出接口并指定时间范围
- THEN: 系统生成包含所有日志记录的 Excel/CSV 文件

### Requirement: 审计日志防篡改
系统 SHALL 保证审计日志不可篡改，写入后不可修改或删除。

#### Scenario: 尝试修改日志
- GIVEN: 已写入的审计日志记录
- WHEN: 尝试通过 API 或直接数据库修改日志内容
- THEN: 操作被拒绝（无 UPDATE/DELETE 权限）

#### Scenario: 日志保留策略
- GIVEN: 审计日志记录
- WHEN: 日志超过 3 年
- THEN: 系统标记为可归档（不自动删除，需管理员确认）
