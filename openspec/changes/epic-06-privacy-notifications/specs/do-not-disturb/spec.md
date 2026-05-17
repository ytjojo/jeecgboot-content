## ADDED Requirements

### Requirement: User can configure do-not-disturb time periods
The system SHALL allow users to set one or more do-not-disturb (DND) time periods during which non-urgent notifications are suppressed.

#### Scenario: Set nightly DND period
- GIVEN: 用户想在夜间免打扰
- WHEN: 用户设置免打扰时段为 23:00-07:00
- THEN: 系统在该时段内不发送非紧急通知

#### Scenario: Different DND for weekdays and weekends
- GIVEN: 用户有多个免打扰时段
- WHEN: 用户设置工作日和周末不同时段
- THEN: 系统按不同规则执行

### Requirement: System sends summary after DND period ends
The system SHALL deliver a summary notification at the end of a DND period if the user has summary mode configured.

#### Scenario: Summary notification after DND ends
- GIVEN: 在免打扰时段内有新通知
- WHEN: 时段结束
- THEN: 系统汇总发送摘要通知（如配置了摘要模式）

### Requirement: User can temporarily disable DND
The system SHALL allow users to temporarily disable DND for a fixed duration (e.g., 1 hour).

#### Scenario: Temporarily close DND
- GIVEN: 用户想临时关闭免打扰
- WHEN: 用户点击"暂时关闭"
- THEN: 系统在接下来 1 小时内正常发送通知

### Requirement: Security notifications bypass DND
The system SHALL always deliver security notifications (e.g., login from new location, password change) even during DND periods.

#### Scenario: Security notification during DND
- GIVEN: 安全类通知（如异地登录提醒）
- WHEN: 在免打扰时段触发
- THEN: 系统仍然发送（白名单豁免）

### Requirement: DND respects user timezone
The system SHALL calculate DND periods using the user's configured timezone.

#### Scenario: Cross-timezone DND calculation
- GIVEN: 用户位于 UTC+8 时区，设置 DND 为 23:00-07:00
- WHEN: 服务器在 UTC 时间 15:00（即 UTC+8 的 23:00）判断是否发送通知
- THEN: 系统正确识别为免打扰时段并阻止非紧急通知
