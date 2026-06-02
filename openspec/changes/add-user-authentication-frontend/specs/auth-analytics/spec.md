## ADDED Requirements

### Requirement: 注册漏斗事件上报
系统 SHALL 在注册流程各环节上报埋点事件，包含 `register_page_view`、`register_tab_switch`、`register_form_start`、`register_captcha_click`、`register_captcha_success`、`register_captcha_fail`、`register_submit`、`register_success`、`register_fail`、`register_agreement_click`。

#### Scenario: 注册页加载上报
- **WHEN** 注册页加载完成
- **THEN** 上报 `register_page_view` 事件，参数包含 source（来源页面）和 tab（手机号/邮箱）

#### Scenario: 注册成功上报
- **WHEN** 用户注册成功
- **THEN** 上报 `register_success` 事件，参数包含 tab_type 和 login_method

#### Scenario: 注册失败上报
- **WHEN** 用户注册失败
- **THEN** 上报 `register_fail` 事件，参数包含 tab_type、error_code、error_msg

### Requirement: 登录漏斗事件上报
系统 SHALL 在登录流程各环节上报埋点事件，包含 `login_page_view`、`login_submit`、`login_success`、`login_fail`、`login_lockout`、`login_third_party_click`、`login_third_party_auth`、`login_third_party_cancel`。

#### Scenario: 登录成功上报
- **WHEN** 用户登录成功
- **THEN** 上报 `login_success` 事件，参数包含 login_method 和 redirect_target

#### Scenario: 第三方登录点击上报
- **WHEN** 用户点击第三方登录按钮
- **THEN** 上报 `login_third_party_click` 事件，参数包含 provider（wechat/apple/google）

#### Scenario: 账号锁定上报
- **WHEN** 账号被锁定
- **THEN** 上报 `login_lockout` 事件，参数包含 lock_duration

### Requirement: 账号安全事件上报
系统 SHALL 在账号安全操作时上报埋点事件，包含 `password_reset_start`、`password_reset_success`、`account_bind`、`account_unbind`、`account_rebind`、`device_revoke`、`anomaly_confirm`、`anomaly_deny`、`account_cancel_apply`、`account_cancel_revoke`。

#### Scenario: 绑定成功上报
- **WHEN** 用户绑定手机号/邮箱/第三方账号成功
- **THEN** 上报 `account_bind` 事件，参数包含 bind_type

#### Scenario: 设备下线上报
- **WHEN** 用户主动下线设备
- **THEN** 上报 `device_revoke` 事件，参数包含 is_current_device

#### Scenario: 注销申请上报
- **WHEN** 用户申请注销
- **THEN** 上报 `account_cancel_apply` 事件
