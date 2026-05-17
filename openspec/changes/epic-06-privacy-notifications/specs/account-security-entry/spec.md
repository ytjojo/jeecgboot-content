## ADDED Requirements

### Requirement: Unified account security settings entry point
The system SHALL provide a unified "Account & Security" page that aggregates entry points for all security-related features.

#### Scenario: View all security features
- GIVEN: 用户进入"账户与安全"页面
- WHEN: 页面加载
- THEN: 用户看到所有安全功能的入口（设备管理、密码修改、两步验证、登录提醒）

#### Scenario: Navigate to device management
- GIVEN: 用户想查看登录设备
- WHEN: 用户点击"设备管理"
- THEN: 用户跳转到设备管理页面（EPIC-01 已实现）

#### Scenario: Navigate to password change
- GIVEN: 用户想修改密码
- WHEN: 用户点击"密码修改"
- THEN: 用户进入密码修改流程，需输入原密码和新密码

#### Scenario: Navigate to two-factor authentication setup
- GIVEN: 用户想启用两步验证
- WHEN: 用户点击"两步验证"
- THEN: 系统引导用户绑定手机或邮箱作为第二步验证方式

#### Scenario: Enable login notification
- GIVEN: 用户想设置登录提醒
- WHEN: 用户开启"登录提醒"
- THEN: 系统在每次新设备登录时发送通知
