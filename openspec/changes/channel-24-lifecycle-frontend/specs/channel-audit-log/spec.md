## ADDED Requirements

### Requirement: 审计日志列表
系统 SHALL 展示频道审计日志列表，支持筛选和分页。

#### Scenario: 日志列表展示
- **WHEN** 平台运营进入审计日志页面
- **THEN** 展示操作时间、频道名称、操作人、操作类型、前后状态、原因、影响范围

#### Scenario: 筛选条件
- **WHEN** 用户设置筛选条件（频道名称、操作人、操作类型、操作时间范围）
- **THEN** 列表按条件过滤

#### Scenario: 操作类型标签展示
- **WHEN** 列表展示操作类型
- **THEN** 使用不同颜色 Tag 区分

#### Scenario: 日志排序
- **WHEN** 列表加载完成
- **THEN** 按操作时间倒序展示

#### Scenario: 分页展示
- **WHEN** 日志数量较多
- **THEN** 支持分页，默认每页 20 条

### Requirement: 审计日志详情
系统 SHALL 支持查看审计日志详情。

#### Scenario: 查看日志详情
- **WHEN** 用户点击日志行
- **THEN** 展示完整审计信息：操作人、操作对象、前后状态、原因、时间、影响范围、通知结果

### Requirement: 频道审计日志
系统 SHALL 支持按频道查看审计日志。

#### Scenario: 频道审计日志查询
- **WHEN** 用户在频道治理详情页查看审计日志 Tab
- **THEN** 展示该频道的操作日志，支持分页

### Requirement: 日志保留期
系统 SHALL 保留审计日志不少于 180 天。

#### Scenario: 日志保留期验证
- **WHEN** 查询审计日志
- **THEN** 日志保留期不少于 180 天
