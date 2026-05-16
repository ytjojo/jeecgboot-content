## ADDED Requirements

### Requirement: 查看第三方授权列表
系统 SHALL 允许用户查看所有已授权的第三方应用/服务列表，列表项包含应用名称、授权时间、授权范围。

#### Scenario: 加载授权列表
- **WHEN** 用户进入"第三方授权"页面
- **THEN** 系统展示所有已授权应用列表，每项包含应用名称、授权时间、授权范围

#### Scenario: 无授权记录
- **WHEN** 用户从未授权过任何第三方应用
- **THEN** 系统显示空状态提示

#### Scenario: 授权列表数据异常——null 值
- **WHEN** 应用名称字段为 null 或空字符串
- **THEN** 系统显示为"未知应用"

### Requirement: 查看授权详情
系统 SHALL 允许用户查看单个第三方应用的详细权限信息，包括可访问的数据范围（如个人资料、发布内容、联系人等）。

#### Scenario: 查看应用权限详情
- **WHEN** 用户点击某个已授权应用的"查看详情"
- **THEN** 系统展示该应用可访问的数据范围清单

#### Scenario: 授权范围数据异常——空值
- **WHEN** 授权范围字段为 null 或空
- **THEN** 系统显示"未知权限"并标记为可疑授权

### Requirement: 撤销第三方授权
系统 SHALL 允许用户随时撤销对第三方应用的授权，撤销后该应用的 Access Token 立即失效。

#### Scenario: 撤销授权
- **WHEN** 用户点击"撤销授权"并确认操作
- **THEN** 系统立即标记该授权为失效状态，并删除相关 Token 缓存

#### Scenario: 撤销后尝试访问
- **WHEN** 已撤销授权的应用尝试访问用户数据
- **THEN** 系统拒绝访问并返回"授权已撤销"错误

#### Scenario: 撤销授权数据异常——授权不存在
- **WHEN** 用户尝试撤销一个不存在的授权记录
- **THEN** 系统返回"授权记录不存在"错误

### Requirement: 重新授权流程
系统 SHALL 在用户撤销授权后，允许用户再次授权时重新走 OAuth 授权流程。

#### Scenario: 重新授权
- **WHEN** 用户对已撤销授权的应用再次发起授权请求
- **THEN** 系统引导用户重新完成 OAuth 授权流程

### Requirement: 第三方授权管理接口
系统 SHALL 提供 `/api/v1/auth/third-party` 接口集合，包含列出授权、查看详情、撤销授权三个操作。

#### Scenario: 列出授权接口
- **WHEN** 客户端调用 GET /api/v1/auth/third-party
- **THEN** 系统返回当前用户的所有已授权应用列表

#### Scenario: 撤销授权接口
- **WHEN** 客户端调用 DELETE /api/v1/auth/third-party/{authId}
- **THEN** 系统撤销指定授权并返回成功状态

#### Scenario: 撤销授权接口异常——越权
- **WHEN** 用户尝试撤销不属于自己的授权记录
- **THEN** 系统返回 403 权限不足错误
