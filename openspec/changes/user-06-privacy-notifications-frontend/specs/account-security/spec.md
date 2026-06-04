## ADDED Requirements

### Requirement: 安全功能入口卡片展示
系统 SHALL 以 2x2 网格布局展示四个安全功能入口卡片（设备管理、密码修改、两步验证、登录提醒），每张卡片包含图标、功能名称、状态描述和操作区域。

#### Scenario: 正常加载安全设置
- **WHEN** 用户进入账户安全页
- **THEN** 调用 `GET /content/user/settings/security`，展示四个安全功能卡片，状态从接口返回的 Boolean 字段获取

#### Scenario: 安全功能状态为 null
- **WHEN** 后端返回某安全功能的状态字段为 null
- **THEN** 默认显示"已启用"（安全优先）

#### Scenario: 加载中显示骨架屏
- **WHEN** 页面数据加载中
- **THEN** 显示骨架屏（a-skeleton），加载完成后替换为实际内容

### Requirement: 安全功能入口跳转
系统 SHALL 支持点击设备管理、密码修改、两步验证卡片跳转到对应页面。

#### Scenario: 点击设备管理卡片
- **WHEN** 用户点击"设备管理"卡片
- **THEN** 跳转到设备管理页面（EPIC-01 已实现）

#### Scenario: 点击密码修改卡片
- **WHEN** 用户点击"密码修改"卡片
- **THEN** 跳转到密码修改流程（EPIC-01 已实现）

#### Scenario: 点击两步验证卡片
- **WHEN** 用户点击"两步验证"卡片
- **THEN** 跳转到两步验证绑定流程（EPIC-01 已实现）

### Requirement: 登录提醒开关
系统 SHALL 在登录提醒卡片右侧提供 Switch 开关，支持直接切换，无需跳转。

> **后端状态**: `GET /content/user/settings/security` 已实现，可读取 `loginAlertEnabled` 状态。但 `POST /content/user/settings/security/update` 尚未实现，需后端补充更新端点。详见 `backend-issues.md`。

#### Scenario: 切换登录提醒开关
- **WHEN** 用户点击登录提醒的 Switch 开关
- **THEN** 调用 `POST /content/user/settings/security/update` 接口更新 `loginAlertEnabled` 状态，成功后卡片状态描述更新

#### Scenario: 开关切换失败
- **WHEN** 登录提醒开关切换请求失败
- **THEN** 全局错误提示，Switch 恢复原状态
