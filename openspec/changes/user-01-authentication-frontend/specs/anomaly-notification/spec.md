## ADDED Requirements

### Requirement: 异常登录实时通知
系统 SHALL 在用户登录后检测到新设备/异地登录时自动弹出异常登录通知弹窗，显示登录时间、地点、设备信息和 IP 地址。

#### Scenario: 异常登录弹窗
- **WHEN** 用户从新设备/异地登录成功
- **THEN** 进入首页后弹出"检测到新设备登录"弹窗，包含登录信息和"是我本人操作"/"不是我，立即下线"按钮

#### Scenario: 确认本人操作
- **WHEN** 用户点击"是我本人操作"
- **THEN** 调用 `POST /api/v1/content/account-security/anomaly/confirm`，弹窗关闭，后续该设备不再弹出提醒

#### Scenario: 否认操作并下线
- **WHEN** 用户点击"不是我，立即下线"
- **THEN** 弹出二级确认弹窗，确认后调用下线接口并跳转到密码修改页

### Requirement: 异常登录通知列表
系统 SHALL 在消息中心通知列表中展示异常登录通知历史，每条通知包含登录信息和"确认/否认"操作按钮（未处理状态）。

#### Scenario: 查看历史通知
- **WHEN** 用户进入消息中心查看异常登录通知
- **THEN** 调用 `GET /api/v1/content/account-security/anomaly-notifications`，显示通知列表

#### Scenario: 已处理通知状态
- **WHEN** 通知已被处理
- **THEN** 列表中显示"已确认"或"已下线"状态标签

### Requirement: 信任设备跳过异常检测
信任设备登录时 SHALL 跳过异常登录检测，不弹出通知。

#### Scenario: 信任设备登录
- **WHEN** 用户从已信任设备登录
- **THEN** 不弹出异常登录通知
