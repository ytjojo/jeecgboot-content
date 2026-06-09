## ADDED Requirements

### Requirement: 治理日志列表
> **后端接口**: `GET /api/v1/content/circle/governance-log/list?circleId=xxx&pageNum=1&pageSize=20` 已实现，可直接对接。分页参数统一使用 `pageNum`/`pageSize`。

系统 SHALL 提供治理日志页（`/circle/:id/governance-log`），以 Table 形式展示治理操作记录（时间、操作者、操作对象、操作类型、详情），按时间倒序排列，支持分页（每页 20 条）。

#### Scenario: 查看治理日志
- **WHEN** 创建者进入治理日志页
- **THEN** 展示近 30 天的治理操作记录，按时间倒序排列

#### Scenario: 操作类型筛选
- **WHEN** 用户选择操作类型筛选（全部/禁言/解除禁言/移除/角色变更）
- **THEN** 立即刷新列表，展示筛选结果

#### Scenario: 操作对象搜索
- **WHEN** 用户输入操作对象昵称关键词
- **THEN** 展示匹配的治理日志

#### Scenario: 日期范围筛选
- **WHEN** 用户选择日期范围
- **THEN** 展示指定日期范围内的治理日志，默认近 30 天

#### Scenario: 禁言详情展示
- **WHEN** 操作类型为禁言
- **THEN** 详情列展示禁言时长和原因

#### Scenario: 移除详情展示
- **WHEN** 操作类型为移除
- **THEN** 详情列展示移除原因

#### Scenario: 角色变更详情展示
- **WHEN** 操作类型为角色变更
- **THEN** 详情列展示变更前后角色（如 成员→版主）

#### Scenario: 无日志
- **WHEN** 治理日志列表无数据
- **THEN** 展示空状态 "暂无治理记录"

### Requirement: 治理日志访问权限
系统 SHALL 仅允许圈子创建者访问治理日志页。非创建者访问时展示 403 页面。

#### Scenario: 创建者访问
- **WHEN** 创建者访问治理日志页
- **THEN** 正常展示治理日志

#### Scenario: 非创建者访问
- **WHEN** 非创建者（版主/成员）访问治理日志页
- **THEN** 展示 403 页面

### Requirement: 治理日志数据保留
系统 SHALL 仅展示 180 天内的治理日志数据。日志保留策略由后端控制，前端不做本地缓存。

#### Scenario: 日志保留期限
- **WHEN** 查询治理日志
- **THEN** 仅返回 180 天内的数据，超过 180 天的日志由后端自动归档
