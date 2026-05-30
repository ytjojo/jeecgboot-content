## ADDED Requirements

### Requirement: 提交内容举报

系统 SHALL 支持圈子成员对疑似违规内容发起举报。举报提交后 MUST 进入待处理状态。

#### Scenario: 成员举报内容
- **WHEN** 圈子成员对某内容提交举报
- **THEN** 系统创建举报记录，status 设为 PENDING，记录举报者、被举报内容、举报原因和时间

#### Scenario: 重复举报
- **WHEN** 同一用户对同一内容重复提交举报
- **THEN** 系统拒绝重复举报，返回"已提交过举报"提示

---

### Requirement: 处理内容举报

系统 SHALL 支持圈子版主或创建者处理成员举报的内容。管理员 MUST 能删除内容、忽略举报或对被举报用户执行禁言。

#### Scenario: 管理员删除被举报内容
- **WHEN** 管理员对举报选择删除内容
- **THEN** 内容被移除，举报状态更新为 RESOLVED，举报者收到处理结果通知

#### Scenario: 管理员忽略举报
- **WHEN** 管理员选择忽略举报
- **THEN** 举报状态更新为 IGNORED，举报者收到处理结果通知

#### Scenario: 管理员禁言被举报用户
- **WHEN** 管理员对被举报用户执行禁言操作
- **THEN** 该用户被禁言，禁言操作记录可追溯，举报状态更新为 RESOLVED

#### Scenario: 查看举报列表
- **WHEN** 管理员查询举报列表
- **THEN** 系统返回该圈子的举报列表，支持按状态（PENDING/RESOLVED/IGNORED）筛选

---

### Requirement: 举报处理通知

系统 MUST 在举报处理完成后通知举报者处理结果。

#### Scenario: 删除内容后通知举报者
- **WHEN** 管理员删除被举报内容
- **THEN** 举报者收到通知，告知其举报已被处理，内容已被删除

#### Scenario: 忽略举报后通知举报者
- **WHEN** 管理员忽略举报
- **THEN** 举报者收到通知，告知其举报已被审核，内容未违规

---

### Requirement: 举报处理审核日志

系统 MUST 记录举报处理和禁言操作的审核日志，包含操作人、操作时间、操作类型、操作对象、操作结果。记录 MUST 保留不少于 180 天。

#### Scenario: 举报处理写入日志
- **WHEN** 管理员处理举报（删除/忽略/禁言）
- **THEN** 系统在 circle_audit_log 表写入记录，包含 operator_id、action(DELETE_REPORTED/IGNORE_REPORT/MUTE_FROM_REPORT)、target_id、result、created_at

---

### Requirement: 举报首次处理响应时间

系统 MUST 确保举报首次处理响应时间 P95 <24 小时。

#### Scenario: 举报处理时效
- **WHEN** 内容被举报
- **THEN** 举报 MUST 在 24 小时内被管理员首次处理（P95）
