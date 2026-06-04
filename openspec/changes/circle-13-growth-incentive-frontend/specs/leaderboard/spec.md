## ADDED Requirements

### Requirement: 排行榜维度切换

系统 SHALL 支持经验值榜、贡献值榜和发帖数榜 3 个维度切换，默认选中「经验值」。

#### Scenario: 默认展示经验值榜
- **WHEN** 用户进入排行榜页
- **THEN** 默认展示经验值维度的排行榜数据

#### Scenario: 切换到贡献值榜
- **WHEN** 用户点击「贡献值」Tab
- **THEN** 展示贡献值维度的排行榜数据

#### Scenario: 切换到发帖数榜
- **WHEN** 用户点击「发帖数」Tab
- **THEN** 展示发帖数维度的排行榜数据

### Requirement: 排行榜周期切换

系统 SHALL 支持本周、本月、累计 3 个周期切换，默认选中「本周」。

#### Scenario: 默认展示本周数据
- **WHEN** 用户进入排行榜页
- **THEN** 默认展示本周周期的排行榜数据

#### Scenario: 切换到本月数据
- **WHEN** 用户切换到「本月」周期
- **THEN** 展示本月周期的排行榜数据

#### Scenario: 切换到累计数据
- **WHEN** 用户切换到「累计」周期
- **THEN** 展示累计周期的排行榜数据

### Requirement: 排行榜样列表展示

系统 SHALL 展示 Top 50 成员列表，包含排名、头像、用户名、对应维度数值。排名前三名使用金银铜色序号标识。

#### Scenario: 展示 Top 50 列表
- **WHEN** 排行榜数据加载成功
- **THEN** 展示最多 50 名成员的排名列表，包含排名序号、头像、用户名、数值

#### Scenario: 不足 50 人展示全部
- **WHEN** 符合条件的成员不足 50 人
- **THEN** 展示全部符合条件的成员

#### Scenario: 前三名特殊标识
- **WHEN** 排行榜列表中有成员排名前 3
- **THEN** 使用金色（#1）、银色（#2）、铜色（#3）序号标识

#### Scenario: 点击头像跳转个人资料
- **WHEN** 用户点击列表中的头像或用户名
- **THEN** 跳转到该成员的个人资料页

### Requirement: 当前用户排名高亮

系统 SHALL 在排行榜中高亮当前用户排名。Top 50 内使用主题色背景高亮，Top 50 外在榜单底部固定展示「我的排名」区域。

#### Scenario: 当前用户在 Top 50 内
- **WHEN** 当前用户排名在 Top 50 以内
- **THEN** 该行使用主题色背景高亮显示

#### Scenario: 当前用户未进入 Top 50
- **WHEN** 当前用户排名在 Top 50 以外
- **THEN** 榜单底部固定展示「我的排名」区域，包含排名、头像、用户名、数值、距上一名差距

#### Scenario: 距上一名差距展示
- **WHEN** 当前用户未进入 Top 50 且有距上一名差距数据
- **THEN** 展示「距上一名差 X 点」文案

### Requirement: 排行榜样空状态

系统 SHALL 在圈子无符合条件的排行成员时展示空状态和参与入口。

#### Scenario: 无排行数据时展示空状态
- **WHEN** 排行榜接口返回空数据
- **THEN** 展示空状态插图和「去发帖」「去评论」操作入口

### Requirement: 排行榜样反作弊规则

系统 SHALL 支持并列排名处理和异常数据过滤的展示。

#### Scenario: 并列排名展示
- **WHEN** 多名成员数值相同
- **THEN** 按经验值 > 贡献值 > 发帖数 > 注册时间的优先级排序展示

### Requirement: 排行榜 API 对接

系统 SHALL 通过 GET `/content/user/growth/leaderboard?circleId={circleId}&dimension={dimension}&period={period}&currentUserId={userId}` 接口获取排行榜数据，支持 dimension 和 period 查询参数。

#### Scenario: 接口请求成功
- **WHEN** 排行榜页加载或切换维度/周期
- **THEN** 调用排行榜接口传入 circleId、dimension、period、currentUserId 参数，解析 LeaderboardEntryVO 列表并渲染

#### Scenario: 接口请求失败
- **WHEN** 排行榜接口请求失败
- **THEN** 展示错误提示和「重试」按钮

#### Scenario: 加载中状态
- **WHEN** 排行榜接口请求中
- **THEN** 展示骨架屏占位
