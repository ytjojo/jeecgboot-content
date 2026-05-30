## ADDED Requirements

### Requirement: 审核状态机

系统 SHALL 管理频道审核状态流转，支持 PendingReview → Active（通过）、PendingReview → Rejected（拒绝）、PendingReview → Draft（退回修改）三种转换。

#### Scenario: 审核通过
- **WHEN** 管理员审核通过一个处于 PendingReview 状态的频道
- **THEN** 频道状态 SHALL 变更为 Active，频道可公开展示

#### Scenario: 审核拒绝
- **WHEN** 管理员审核拒绝一个处于 PendingReview 状态的频道
- **THEN** 频道状态 SHALL 变更为 Rejected，频道主收到拒绝原因

#### Scenario: 退回修改
- **WHEN** 管理员退回修改一个处于 PendingReview 状态的频道
- **THEN** 频道状态 SHALL 变更为 Draft，频道主收到修改建议

---

### Requirement: 审核记录持久化

每次审核操作 SHALL 在 content_channel_review 表中创建完整记录，包含审核人、审核结果、审核原因、操作时间。

#### Scenario: 审核记录写入
- **WHEN** 管理员对频道执行审核操作（通过/拒绝/退回修改）
- **THEN** 系统 SHALL 创建一条审核记录，包含 channel_id、reviewer_id、result、reason、created_time

#### Scenario: 审核记录可追溯
- **WHEN** 查询频道的审核历史
- **THEN** 系统 SHALL 返回该频道的所有审核记录，按时间倒序排列

---

### Requirement: 关键字段修改触发审核

频道名称、简介、图标、封面、分类等关键字段修改 SHALL 重新进入审核流程。

#### Scenario: 修改名称触发审核
- **WHEN** 频道主修改频道名称并提交
- **THEN** 频道 SHALL 进入 PendingReview 状态，审核期间按原名称展示

#### Scenario: 审核期间原信息保持展示
- **WHEN** 频道处于 PendingReview 状态（因关键字段修改）
- **THEN** 用户端 SHALL 展示审核通过的原信息，不展示待审核的新信息

#### Scenario: 系统频道修改无需审核
- **WHEN** 管理员修改系统频道信息
- **THEN** 修改 SHALL 直接生效，无需进入审核流程
