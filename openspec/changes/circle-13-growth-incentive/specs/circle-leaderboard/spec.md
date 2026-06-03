## ADDED Requirements

### Requirement: Leaderboard Dimensions

排行榜 SHALL 展示经验值榜、贡献值榜和发帖数榜 3 个维度。

#### Scenario: Three leaderboard dimensions
- **WHEN** 成员进入圈子详情页查看排行榜
- **THEN** 系统 SHALL 展示经验值榜、贡献值榜和发帖数榜 3 个维度供切换

---

### Requirement: Leaderboard Period

排行榜 SHALL 支持本周、本月、累计 3 个周期切换。

#### Scenario: Period switching
- **WHEN** 成员选择榜单周期为本周或本月
- **THEN** 排行榜 SHALL 按对应周期展示排名

#### Scenario: Default period
- **WHEN** 成员首次进入排行榜
- **THEN** 系统 SHALL 默认展示本周周期的排名

---

### Requirement: Leaderboard Top 50 Display

每个榜单 SHALL 展示 Top 50 成员；不足 50 人时展示全部符合条件成员。

#### Scenario: Show top 50 members
- **WHEN** 排行榜数据已更新且符合条件成员超过 50 人
- **THEN** 每个榜单 SHALL 展示 Top 50 成员

#### Scenario: Show all when less than 50
- **WHEN** 排行榜数据已更新且符合条件成员不足 50 人
- **THEN** 榜单 SHALL 展示全部符合条件成员

---

### Requirement: Leaderboard Current User Highlight

排行榜 SHALL 高亮当前成员；当前成员未进入 Top 50 时仍展示其排名位置。

#### Scenario: Highlight current user in top 50
- **WHEN** 当前成员进入 Top 50
- **THEN** 排行榜 SHALL 高亮当前成员的排名行

#### Scenario: Show current user below top 50
- **WHEN** 当前成员未进入 Top 50
- **THEN** 榜单底部 SHALL 展示当前成员排名、数值和距离上一名的差距

---

### Requirement: Leaderboard Refresh

排行榜 SHALL 每小时更新一次。

#### Scenario: Hourly refresh
- **WHEN** 定时任务触发（每小时）
- **THEN** 系统 SHALL 从成长流水表聚合生成排行榜快照

#### Scenario: Show latest snapshot
- **WHEN** 成员查看排行榜
- **THEN** 系统 SHALL 展示最新的排行榜快照数据

---

### Requirement: Leaderboard Empty State

圈子没有符合条件的排行成员时 SHALL 展示空状态和参与发帖、评论的入口。

#### Scenario: Empty leaderboard
- **WHEN** 圈子没有符合条件的排行成员
- **THEN** 系统 SHALL 展示空状态和参与发帖、评论的入口

---

### Requirement: Leaderboard Member Filtering

榜单 SHALL 仅展示当前仍在圈子的成员，违规、封禁或退出圈子的成员不在榜单中展示。

#### Scenario: Exclude non-active members
- **WHEN** 成员已退出圈子或被封禁
- **THEN** 该成员 SHALL NOT 出现在排行榜中

#### Scenario: Exclude violating members
- **WHEN** 成员被判定违规
- **THEN** 该成员 SHALL NOT 出现在排行榜中
