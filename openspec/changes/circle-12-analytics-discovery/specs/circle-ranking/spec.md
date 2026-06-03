## ADDED Requirements

### Requirement: 热门圈子榜单

系统 SHALL 提供按成员数和活跃度排名的热门圈子榜单，展示 Top 20。

#### Scenario: 正常展示
- **WHEN** 用户进入圈子列表页查看榜单
- **THEN** 展示按成员数/活跃度排名的热门圈子 Top 20

#### Scenario: 候选不足 20 个
- **WHEN** 候选公开圈子少于 20 个
- **THEN** 展示全部符合条件的公开圈子

### Requirement: 新增圈子榜单

系统 SHALL 提供按创建时间倒序展示的新增圈子榜单。

#### Scenario: 正常展示
- **WHEN** 用户进入圈子列表页查看新增榜单
- **THEN** 展示最近创建的圈子列表

### Requirement: 榜单刷新

系统 SHALL 保证热门圈子榜单和新增圈子榜单每小时更新。

#### Scenario: 定时刷新
- **WHEN** 每小时定时任务触发
- **THEN** 更新热门圈子榜单和新增圈子榜单

### Requirement: 榜单数据范围

系统 SHALL 保证榜单仅包含公开圈子。

#### Scenario: 公开圈子
- **WHEN** 系统生成榜单
- **THEN** 仅包含公开圈子，私有圈子不参与排名

### Requirement: 榜单空状态

系统 SHALL 在无符合条件的公开圈子时展示空状态。

#### Scenario: 无符合条件圈子
- **WHEN** 无符合条件的公开圈子
- **THEN** 展示空状态而非空白页面

### Requirement: 榜单查询性能

系统 SHALL 保证榜单加载响应时间 P95 <1 秒。

#### Scenario: 正常加载
- **WHEN** 用户请求榜单数据
- **THEN** 系统在 1 秒内返回结果
