## ADDED Requirements

### Requirement: 状态转换规则定义
系统 SHALL 定义完整的状态转换规则表，明确每种状态可以转换到哪些目标状态。

#### Scenario: 合法状态转换
- GIVEN: 用户当前状态为 MUTED（禁言）
- WHEN: 禁言期限到期触发自动解禁
- THEN: 状态允许转换为 NORMAL（正常）

#### Scenario: 非法状态转换拒绝
- GIVEN: 用户当前状态为 GUEST（游客）
- WHEN: 尝试直接转换为 BANNED（封禁）
- THEN: 系统拒绝转换并抛出非法状态转换异常

#### Scenario: 管理员强制转换
- GIVEN: 管理员具有状态管理权限
- WHEN: 管理员执行强制状态变更
- THEN: 系统允许从任意状态转换到任意状态（需记录审计日志）

### Requirement: 状态变更触发器
系统 SHALL 支持多种触发方式发起状态变更，包括用户操作、系统自动、管理员手动。

#### Scenario: 用户操作触发
- GIVEN: 用户提交注销申请
- WHEN: 系统处理注销请求
- THEN: 触发状态从 NORMAL 转换为 DEACTIVATING

#### Scenario: 系统自动触发
- GIVEN: 定时任务检测到禁言到期
- WHEN: 执行自动解禁逻辑
- THEN: 触发状态从 MUTED 转换为 NORMAL

#### Scenario: 管理员手动触发
- GIVEN: 管理员在后台执行封禁操作
- WHEN: 提交封禁请求（含原因、期限）
- THEN: 触发状态从当前状态转换为 BANNED

### Requirement: 状态变更事务保证
系统 SHALL 保证状态变更操作的原子性，状态变更和审计日志写入在同一事务中。

#### Scenario: 状态变更成功
- GIVEN: 合法的状态变更请求
- WHEN: 执行状态变更
- THEN: 用户状态字段更新成功且审计日志写入成功

#### Scenario: 状态变更失败回滚
- GIVEN: 状态变更过程中发生异常
- WHEN: 事务回滚
- THEN: 用户状态字段保持不变且审计日志不写入

### Requirement: 并发状态变更控制
系统 SHALL 使用乐观锁防止并发状态变更冲突。

#### Scenario: 并发更新冲突
- GIVEN: 两个管理员同时对同一用户执行状态变更
- WHEN: 第二个变更提交时检测到版本冲突
- THEN: 系统拒绝第二个变更并提示"状态已变更，请刷新后重试"
