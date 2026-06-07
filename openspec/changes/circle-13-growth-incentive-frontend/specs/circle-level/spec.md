## ADDED Requirements

### Requirement: 圈子等级标识展示

系统 SHALL 在圈子详情页、圈子列表卡片中展示圈子等级标识（L1-L5），使用不同颜色区分等级：L1 灰色、L2 绿色、L3 蓝色、L4 橙色、L5 金色。

#### Scenario: 圈子详情页展示等级标识
- **WHEN** 用户进入圈子详情页
- **THEN** 页面顶部圈子基本信息区域展示等级标识图标和等级名称（如「L3 优质圈」）

#### Scenario: 圈子列表卡片展示等级标识
- **WHEN** 用户浏览圈子列表
- **THEN** 每个圈子卡片上展示对应等级标识

#### Scenario: 最高等级展示
- **WHEN** 圈子达到 L5 标杆圈
- **THEN** 等级标识使用金色，进度条显示为满格，文案变为「已达最高等级」

### Requirement: 圈子等级进度展示

系统 SHALL 在圈子详情页展示等级进度条，包含当前等级、下一等级、成长分百分比。后端 CircleLevelVO 提供 `level`、`levelName`、`growthScore`、`nextLevelThreshold`、`progressPercent` 五个字段。未提供 `memberGap`、`contentGap`、`interactionGap` 分项差距字段，差距条件展示降级为仅显示成长分和进度百分比。

#### Scenario: 展示等级进度
- **WHEN** 用户进入圈子详情页
- **THEN** 展示等级进度条，包含当前等级标签（`level` + `levelName`）、下一等级标签、进度百分比（`progressPercent`）、成长分数值（`growthScore` / `nextLevelThreshold`）

#### Scenario: 展示差距条件
- **WHEN** 圈子未达到最高等级
- **THEN** 进度条下方展示「成长分 `growthScore` / `nextLevelThreshold`」（注：后端未提供分项差距字段，暂不展示成员/内容/互动分项差距）

#### Scenario: 点击进度条展开分项指标
- **WHEN** 用户点击等级进度条
- **THEN** 暂不支持展开分项指标（后端未提供 `memberGap`、`contentGap`、`interactionGap` 字段）

### Requirement: 圈子等级权益展示

系统 SHALL 在圈子详情页展示等级相关信息。后端 CircleLevelVO 未提供 `benefits` 和 `nextBenefits` 字段，权益列表展示暂不支持，仅展示等级标识和进度条。

#### Scenario: 展示已解锁权益
- **WHEN** 用户进入圈子详情页
- **THEN** 暂不展示权益列表（后端未提供 `benefits` 字段），仅展示等级标识和进度条

#### Scenario: 展示未解锁权益
- **WHEN** 圈子未达到最高等级
- **THEN** 暂不展示下一等级权益（后端未提供 `nextBenefits` 字段）

#### Scenario: 私有圈子未加入成员
- **WHEN** 未加入私有圈子的用户访问圈子详情页
- **THEN** 不展示成长区块，显示「加入圈子后查看」提示

### Requirement: 圈子等级信息 API 对接

系统 SHALL 通过 GET `/content/user/growth/level/info?circleId={circleId}` 接口获取圈子等级信息，包含等级、成长分、下一等级门槛、进度百分比。

#### Scenario: 接口请求成功
- **WHEN** 圈子详情页加载
- **THEN** 调用等级信息接口，解析 CircleLevelVO 响应数据并渲染到页面

#### Scenario: 接口请求失败
- **WHEN** 等级信息接口请求失败
- **THEN** 展示错误提示和「重试」按钮

#### Scenario: 加载中状态
- **WHEN** 等级信息接口请求中
- **THEN** 展示骨架屏（Skeleton）占位
