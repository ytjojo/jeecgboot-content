## ADDED Requirements

### Requirement: 第三方授权列表展示
系统 SHALL 使用 Table 组件展示所有已授权的第三方应用列表，包含应用名称、授权时间、授权范围、操作列。

#### Scenario: 正常展示授权列表
- **WHEN** 用户进入第三方授权页
- **THEN** 调用 `GET /content/user/auth/third-party`，Table 展示所有已授权应用，授权时间格式为 YYYY-MM-DD HH:mm

#### Scenario: 应用名称为 null
- **WHEN** 授权记录的应用名称字段为 null
- **THEN** 显示"未知应用"

#### Scenario: 授权范围为空
- **WHEN** 授权记录的授权范围字段为 null 或空
- **THEN** 显示"未知权限"并标记为红色 Tag（可疑授权）

#### Scenario: 授权范围正常展示
- **WHEN** 授权记录包含多个授权范围
- **THEN** 多个范围用 Tag 标签展示

#### Scenario: 列表为空
- **WHEN** 用户无任何已授权的第三方应用
- **THEN** 显示空状态插图 + 文案"暂无已授权的第三方应用"

### Requirement: 查看授权详情
系统 SHALL 提供"查看详情"按钮，点击后打开 Modal 弹窗展示该应用可访问的数据范围清单。

#### Scenario: 查看授权详情
- **WHEN** 用户点击某行的"查看详情"按钮
- **THEN** 调用 `GET /content/user/auth/third-party/{authId}`，打开 Modal 弹窗展示该应用可访问的数据范围清单（个人资料、发布内容、联系人等）

### Requirement: 撤销第三方授权
系统 SHALL 提供"撤销授权"按钮，点击后弹出二次确认弹窗，确认后调用撤销接口。

#### Scenario: 撤销授权成功
- **WHEN** 用户点击"撤销授权"，在确认弹窗中点击"确认撤销"
- **THEN** 调用 `DELETE /content/user/auth/third-party/{authId}`，成功后全局提示"授权已撤销"，该行从列表中移除

#### Scenario: 撤销授权确认弹窗
- **WHEN** 用户点击"撤销授权"按钮
- **THEN** 弹出确认弹窗，标题"撤销授权"，内容"撤销后该应用将无法访问你的数据，是否确认？"

#### Scenario: 撤销授权失败
- **WHEN** 撤销授权请求失败
- **THEN** 全局错误提示，保留该行

#### Scenario: 授权记录不存在
- **WHEN** 撤销的授权记录不存在（后端返回 404）
- **THEN** 提示"授权记录不存在"

#### Scenario: 越权操作
- **WHEN** 撤销操作返回 403
- **THEN** 提示"权限不足"

### Requirement: authId 类型处理
系统 SHALL 在路由参数传递时将 authId 转为 string，API 调用时转回 number。

#### Scenario: authId 类型转换
- **WHEN** 前端从路由参数获取 authId
- **THEN** 传递给 API 时转换为 number 类型
