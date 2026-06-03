## ADDED Requirements

### Requirement: 自动解禁机制
系统 SHALL 通过定时任务自动检查并解除到期的处罚状态。

#### Scenario: 禁言到期自动解禁
- GIVEN: 用户禁言到期时间已到
- WHEN: 定时任务执行扫描
- THEN: 系统将用户状态从 MUTED 恢复为 NORMAL，记录审计日志，发送通知给用户

#### Scenario: 封禁到期自动解封
- GIVEN: 用户临时封禁到期时间已到
- WHEN: 定时任务执行扫描
- THEN: 系统将用户状态从 BANNED 恢复为 NORMAL，记录审计日志

#### Scenario: 永久封禁不解禁
- GIVEN: 用户被永久封禁（status_end_time 为空或为极远未来时间）
- WHEN: 定时任务执行扫描
- THEN: 系统跳过该用户，不执行解禁

### Requirement: 人工解禁机制
系统 SHALL 支持管理员手动解除用户处罚状态。

#### Scenario: 管理员提前解禁
- GIVEN: 管理员具有状态管理权限
- WHEN: 调用 POST /api/content/user-status/{userId}/release 并指定原因
- THEN: 系统立即将用户状态恢复为 NORMAL，记录操作人和原因到审计日志

#### Scenario: 解禁后功能恢复
- GIVEN: 用户状态刚从 MUTED 恢复为 NORMAL
- WHEN: 用户执行之前被限制的操作（如发表评论）
- THEN: 操作正常通过

### Requirement: 申诉恢复机制
系统 SHALL 与申诉系统（EPIC-08）集成，支持申诉成功后自动恢复用户状态。

#### Scenario: 申诉成功恢复
- GIVEN: 申诉系统返回申诉成功结果
- WHEN: 系统处理申诉回调
- THEN: 自动将用户状态恢复为 NORMAL，记录申诉编号、审核人、恢复原因到审计日志

#### Scenario: 申诉驳回维持原状
- GIVEN: 申诉系统返回申诉驳回结果
- WHEN: 系统处理申诉回调
- THEN: 用户状态保持不变，通知用户申诉被驳回及原因

### Requirement: 解禁通知
系统 SHALL 在用户状态恢复时发送通知给用户。

#### Scenario: 自动解禁通知
- GIVEN: 用户被自动解禁
- WHEN: 解禁操作完成
- THEN: 系统发送站内通知"您的账号已恢复正常，处罚已解除"

#### Scenario: 申诉成功通知
- GIVEN: 用户申诉成功
- WHEN: 状态恢复完成
- THEN: 系统发送站内通知"申诉成功，您的账号已恢复正常"

### Requirement: 解禁失败处理
系统 SHALL 在自动解禁失败时进行告警并转人工处理。

#### Scenario: 解禁异常告警
- GIVEN: 自动解禁过程中发生数据库异常
- WHEN: 异常被捕获
- THEN: 系统记录错误日志，发送告警通知运维人员，该用户保持原状态等待人工处理
