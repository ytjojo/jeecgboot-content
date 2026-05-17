## ADDED Requirements

### Requirement: User can configure notification type toggles
The system SHALL allow users to individually enable or disable each notification type including like, comment, follow, favorite, mention, and private message.

#### Scenario: View all notification types
- GIVEN: 用户进入通知设置页面
- WHEN: 用户查看通知类型列表
- THEN: 系统显示所有可配置的通知类型（点赞、评论、关注、收藏、@我、私信）

#### Scenario: Disable a notification type
- GIVEN: 用户想关闭点赞通知
- WHEN: 用户将"点赞"通知开关关闭
- THEN: 系统不再发送点赞相关的通知

#### Scenario: Enable all notification types
- GIVEN: 用户想接收所有通知
- WHEN: 用户将所有通知类型开关打开
- THEN: 系统发送所有类型的通知

### Requirement: User can select notification delivery channels
The system SHALL allow users to select delivery channels (in-app, SMS, email) for each notification type independently.

#### Scenario: Receive notifications via single channel only
- GIVEN: 用户只想通过 App 接收评论通知
- WHEN: 用户关闭短信和邮件渠道，仅保留 App 内通知
- THEN: 系统仅在 App 内推送评论通知

#### Scenario: Receive notifications via all channels
- GIVEN: 用户想接收所有渠道的 @我 通知
- WHEN: 用户开启所有渠道开关
- THEN: 系统通过 App、短信、邮件三种方式通知用户

#### Scenario: Mixed channel configuration
- GIVEN: 用户对不同通知类型配置不同渠道
- WHEN: 点赞仅 App 内通知，评论开启 App + 邮件
- THEN: 系统按各自配置分别发送通知

### Requirement: Notification settings take effect immediately
The system SHALL apply notification setting changes immediately after successful save.

#### Scenario: Settings take effect after save
- GIVEN: 用户修改了通知设置
- WHEN: 保存成功
- THEN: 新设置立即生效，后续通知按新规则发送

### Requirement: Security notifications are unaffected by regular toggles
The system SHALL NOT allow regular notification toggles to affect security notifications (e.g., login from new location, password change).

#### Scenario: Security notifications always sent
- GIVEN: 用户关闭了所有普通通知
- WHEN: 系统检测到异地登录
- THEN: 系统仍然发送安全类通知
