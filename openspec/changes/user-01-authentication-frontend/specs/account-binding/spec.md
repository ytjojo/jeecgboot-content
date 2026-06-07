## ADDED Requirements

### Requirement: 账号安全页展示绑定状态
系统 SHALL 在账号安全页展示手机号、邮箱、微信、Apple、Google 的绑定状态，已绑定项显示脱敏信息和换绑/解绑按钮，未绑定项显示绑定按钮。

#### Scenario: 查看绑定状态
- **WHEN** 用户进入个人中心 → 账号安全
- **THEN** 系统调用 `GET /api/v1/content/account-security/status`，显示各渠道绑定状态

#### Scenario: 加载中状态
- **WHEN** 绑定状态正在加载
- **THEN** 显示骨架屏

### Requirement: 绑定手机号
系统 SHALL 支持用户绑定手机号，需通过短信验证码验证。

#### Scenario: 绑定手机号成功
- **WHEN** 用户输入新手机号和验证码，点击确认
- **THEN** 系统调用 `POST /api/v1/content/account-security/bind/mobile`，显示"绑定成功"，列表状态实时更新

#### Scenario: 手机号已被其他账户绑定
- **WHEN** 用户输入已被其他账户绑定的手机号
- **THEN** 系统提示"该手机号已被其他账户绑定"

### Requirement: 绑定邮箱
系统 SHALL 支持用户绑定邮箱，需通过验证邮件确认。

#### Scenario: 绑定邮箱成功
- **WHEN** 用户输入邮箱并点击确认
- **THEN** 系统调用 `POST /api/v1/content/account-security/bind/email`，发送验证邮件，提示"验证邮件已发送"

### Requirement: 绑定第三方账号
系统 SHALL 支持用户绑定微信/Apple/Google 账号，通过第三方授权流程完成。

#### Scenario: 绑定第三方账号成功
- **WHEN** 用户完成第三方授权
- **THEN** 系统调用 `POST /api/v1/content/account-security/bind/third-party`，显示"绑定成功"，列表状态实时更新

#### Scenario: 第三方授权取消
- **WHEN** 用户在授权页取消
- **THEN** 返回后提示"授权已取消"

### Requirement: 换绑手机号
系统 SHALL 支持换绑手机号，需验证原手机号和新手机号（双向验证码）。

#### Scenario: 换绑手机号成功
- **WHEN** 用户输入原手机号验证码和新手机号验证码，点击确认换绑
- **THEN** 系统调用 `POST /api/v1/content/account-security/rebind/mobile`，显示"换绑成功"，列表状态实时更新

#### Scenario: 新手机号已被其他账户绑定
- **WHEN** 用户输入已被绑定的新手机号
- **THEN** 系统提示"该手机号已被其他账户绑定"

### Requirement: 解绑联系方式
系统 SHALL 支持解绑手机号/邮箱/第三方账号，但至少需保留一种联系方式。

#### Scenario: 解绑成功
- **WHEN** 用户确认解绑非最后一种联系方式
- **THEN** 系统调用对应解绑接口，显示"解绑成功"，列表状态实时更新

#### Scenario: 解绑最后一种联系方式
- **WHEN** 用户尝试解绑唯一的手机号/邮箱
- **THEN** 系统拒绝并提示"至少需要保留一种联系方式，请先绑定其他方式"

#### Scenario: 解绑确认弹窗
- **WHEN** 用户点击"解绑"按钮
- **THEN** 弹出确认弹窗"确定解绑吗？解绑后将无法使用该方式登录"

### Requirement: 账号安全页操作入口
账号安全页 SHALL 提供"修改密码"、"设备管理"、"账号注销"（红色文字）操作入口。

#### Scenario: 点击设备管理入口
- **WHEN** 用户点击"设备管理"
- **THEN** 跳转到设备管理页

#### Scenario: 点击账号注销入口
- **WHEN** 用户点击"账号注销"
- **THEN** 跳转到账号注销页
