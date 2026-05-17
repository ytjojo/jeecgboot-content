## ADDED Requirements

### Requirement: System must enforce notification unsubscribe rules
The system SHALL NOT send non-essential notifications through any channel once the user has disabled that notification type, except for security notifications.

#### Scenario: Marketing notification blocked after opt-out
- GIVEN: 用户关闭了营销类通知
- WHEN: 系统准备发送营销通知
- THEN: 系统检查用户偏好并跳过发送

#### Scenario: All channels blocked
- GIVEN: 用户关闭了所有推送渠道
- WHEN: 系统准备发送非安全通知
- THEN: 系统不发送任何通知

#### Scenario: Security notification whitelist bypass
- GIVEN: 有安全类通知（如异地登录、密码修改）
- WHEN: 即使用户关闭了通知
- THEN: 系统仍然发送（白名单机制）

#### Scenario: Audit notification compliance
- GIVEN: 用户投诉收到已关闭的通知
- WHEN: 系统收到投诉
- THEN: 系统检查日志并确认是否违规发送

#### Scenario: Remediation for violation
- GIVEN: 系统发现违规发送
- WHEN: 确认后
- THEN: 系统修复 bug 并向用户道歉

### Requirement: Notification dispatch service is the single enforcement point
The system SHALL route all notification dispatches through a centralized dispatch service that checks user preferences before sending.

#### Scenario: Dispatch service checks preferences
- GIVEN: 任何通知准备发送
- WHEN: 通知到达发送服务
- THEN: 服务必须检查用户偏好配置后才决定是否发送

### Requirement: Security notification whitelist is configurable
The system SHALL maintain a configurable whitelist of notification types that always bypass user preferences and DND settings.

#### Scenario: Whitelist configuration
- GIVEN: 管理员配置安全通知白名单
- WHEN: 新增或移除白名单项
- THEN: 系统按新配置执行安全通知发送
