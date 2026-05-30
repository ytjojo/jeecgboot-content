## ADDED Requirements

### Requirement: 处理加入申请

系统 SHALL 支持圈子版主或创建者审核用户的加入申请。管理员 MUST 能批准或拒绝申请。批准后用户成功加入圈子，拒绝后用户收到拒绝通知。

#### Scenario: 管理员批准加入申请
- **WHEN** 圈子设置为需要审核，管理员批准某用户的加入申请
- **THEN** 该用户成功加入圈子（circle_member 表新增记录），申请状态更新为 APPROVED

#### Scenario: 管理员拒绝加入申请
- **WHEN** 管理员拒绝某用户的加入申请
- **THEN** 申请状态更新为 REJECTED，该用户收到拒绝通知

#### Scenario: 查看待处理申请列表
- **WHEN** 管理员查询待处理加入申请
- **THEN** 系统返回该圈子 status 为 PENDING 的申请列表，按申请时间排序

---

### Requirement: 加入申请超时提醒

系统 MUST 对超过 3 天未处理的加入申请自动提醒管理员。

#### Scenario: 超时申请自动提醒
- **WHEN** 加入申请创建时间超过 3 天且状态仍为 PENDING
- **THEN** 系统自动向圈子创建者和版主发送提醒通知

#### Scenario: 已处理申请不重复提醒
- **WHEN** 加入申请已被处理（APPROVED/REJECTED）
- **THEN** 系统不再对该申请发送超时提醒

---

### Requirement: 加入申请审核日志

系统 MUST 记录加入申请审核操作的审核日志，包含操作人、操作时间、操作类型、操作对象、操作结果和拒绝原因。记录 MUST 保留不少于 180 天。

#### Scenario: 批准操作写入日志
- **WHEN** 管理员批准加入申请
- **THEN** 系统在 circle_audit_log 表写入记录，包含 operator_id、action(APPROVE_JOIN)、target_id(user_id)、result(APPROVED)、created_at

#### Scenario: 拒绝操作写入日志
- **WHEN** 管理员拒绝加入申请并填写拒绝原因
- **THEN** 系统在 circle_audit_log 表写入记录，包含 operator_id、action(REJECT_JOIN)、target_id(user_id)、result(REJECTED)、reason、created_at
