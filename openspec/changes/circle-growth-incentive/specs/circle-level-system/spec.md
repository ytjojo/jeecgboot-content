## ADDED Requirements

### Requirement: Circle Level Calculation

系统 SHALL 根据圈子的成员规模、内容贡献和活跃互动三类指标计算圈子成长分，总分范围 0-1000 分。

#### Scenario: Circle growth score calculation
- **WHEN** 定时任务触发（每 30 分钟）
- **THEN** 系统从圈子成员表、内容表、互动表聚合三类指标，计算成长分并写入圈子等级记录

#### Scenario: Growth score within valid range
- **WHEN** 成长分计算完成
- **THEN** 成长分 SHALL 限制在 0-1000 分范围内

---

### Requirement: Circle Level Threshold

系统 SHALL 按以下门槛划分圈子等级：L1 新芽圈（0 分）、L2 活跃圈（100 分）、L3 优质圈（300 分）、L4 热门圈（600 分）、L5 标杆圈（850 分）。

#### Scenario: Level upgrade on threshold reached
- **WHEN** 圈子成长分达到或超过下一等级门槛
- **THEN** 圈子等级 SHALL 提升至对应等级

#### Scenario: Level display on circle pages
- **WHEN** 用户查看圈子主页、圈子详情或圈子列表卡片
- **THEN** 页面 SHALL 展示当前等级名称和等级标识

---

### Requirement: Circle Level Progress Display

系统 SHALL 在圈子详情页展示当前等级、成长分、下一等级所需条件、进度百分比和已解锁权益。

#### Scenario: View level progress with next level conditions
- **WHEN** 用户查看圈子详情且圈子尚未达到最高等级
- **THEN** 系统 SHALL 展示距离下一等级还需补足的成员规模、内容贡献或活跃互动条件

#### Scenario: View unlocked benefits
- **WHEN** 用户查看圈子详情
- **THEN** 系统 SHALL 展示当前等级已解锁的权益列表

---

### Requirement: Circle Level Upgrade Notification

系统 SHALL 在圈子等级提升时，向圈子创建者和近 7 天有发帖、评论或点赞行为的活跃成员发送站内通知。

#### Scenario: Notify creator and active members on upgrade
- **WHEN** 圈子等级状态发生变化
- **THEN** 系统 SHALL 在 5 分钟内向圈子创建者和活跃成员发送站内通知

#### Scenario: Notification delivery target
- **WHEN** 圈子等级提升
- **THEN** 通知接收者 SHALL 包括圈子创建者和近 7 天有发帖、评论或点赞行为的成员

---

### Requirement: Circle Level Benefits

等级权益 SHALL 包括主页等级标识、排行榜展示入口、徽章墙展示位和圈子推荐权重提升。不包含付费或商业化权益。

#### Scenario: Benefits unlock on level up
- **WHEN** 圈子等级提升
- **THEN** 对应等级的权益 SHALL 可用并在圈子主页展示

#### Scenario: No paid benefits
- **WHEN** 用户查看任何等级的权益
- **THEN** 权益列表 SHALL NOT 包含付费或商业化内容
