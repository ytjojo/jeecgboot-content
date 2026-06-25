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

系统 SHALL 在圈子详情页展示等级进度条，包含当前等级、下一等级、成长分百分比。后端 CircleLevelVO 提供 `level`、`levelName`、`growthScore`、`nextLevelThreshold`、`progressPercent`、`memberScore`、`contentScore`、`activityScore`、`nextLevelConditions` 等字段。`nextLevelConditions` 为 `LevelConditionVO[]`，每项包含 `type`、`label`、`current`、`required`、`gap`，可直接展示各项差距。

#### Scenario: 展示等级进度
- **WHEN** 用户进入圈子详情页
- **THEN** 展示等级进度条，包含当前等级标签（`level` + `levelName`）、下一等级标签、进度百分比（`progressPercent`）、成长分数值（`growthScore` / `nextLevelThreshold`）

#### Scenario: 展示差距条件
- **WHEN** 圈子未达到最高等级
- **THEN** 进度条下方展示各维度当前得分和差距：「成员 `memberScore`」「内容 `contentScore`」「活跃 `activityScore`」；若 `nextLevelConditions` 有值，按条件项展示 `label`、`current`/`required` 和 `gap`。

#### Scenario: 点击进度条展开分项指标
- **WHEN** 用户点击等级进度条
- **THEN** 展开显示详细的成员规模（`memberScore`）、内容贡献（`contentScore`）、活跃互动（`activityScore`）三类指标分项及 `nextLevelConditions` 各项差距

### Requirement: 圈子等级权益展示

系统 SHALL 在圈子详情页展示等级权益信息。后端 CircleLevelVO 提供 `benefits`（`CircleBenefitVO[]`：`{name, unlocked}`）和 `nextLevelConditions`（`LevelConditionVO[]`：`{type, label, current, required, gap}`）字段，可直接对接展示。

#### Scenario: 展示已解锁权益
- **WHEN** 用户进入圈子详情页
- **THEN** 展示已解锁权益列表（从 `benefits` 字段获取）：已解锁权益（`unlocked: true`）用勾选图标，未解锁权益用锁定图标并置灰

#### Scenario: 展示未解锁权益
- **WHEN** 圈子未达到最高等级
- **THEN** 展示下一等级条件（从 `nextLevelConditions` 字段获取）：按条件项展示 `label`、`current`/`required` 和 `gap`

#### Scenario: 私有圈子未加入成员
- **WHEN** 未加入私有圈子的用户访问圈子详情页
- **THEN** 不展示成长区块，显示「加入圈子后查看」提示

### Requirement: 圈子等级信息 API 对接

系统 SHALL 通过 GET `/api/v1/content/circle/growth/level/info?circleId={circleId}` 接口获取圈子等级信息，包含等级、成长分、下一等级门槛、进度百分比。

#### Scenario: 接口请求成功
- **WHEN** 圈子详情页加载
- **THEN** 调用等级信息接口，解析 CircleLevelVO 响应数据并渲染到页面

#### Scenario: 接口请求失败
- **WHEN** 等级信息接口请求失败
- **THEN** 展示错误提示和「重试」按钮

#### Scenario: 加载中状态
- **WHEN** 等级信息接口请求中
- **THEN** 展示骨架屏（Skeleton）占位
