## ADDED Requirements

### Requirement: Continuous Participation Tracking

系统 SHALL 按圈子分别统计成员近 7 天的连续参与进度，有效参与行为包括发帖、评论、点赞和内容被加精。

#### Scenario: Track participation across 7 days
- **WHEN** 成员在圈子内完成发帖、评论、点赞或内容被加精
- **THEN** 系统 SHALL 将当日标记为已参与

#### Scenario: Participation tracked per circle
- **WHEN** 成员在不同圈子有参与行为
- **THEN** 各圈子的连续参与进度 SHALL 独立统计，不跨圈子合并

---

### Requirement: Continuous Participation Display

成员查看个人成长信息时 SHALL 展示连续参与进度和已完成天数。

#### Scenario: View participation progress
- **WHEN** 成员近 7 天内至少 3 天在圈子完成有效参与行为
- **THEN** 系统 SHALL 展示连续参与进度和已完成天数

#### Scenario: Empty state for inactive members
- **WHEN** 成员近 7 天没有任何有效参与行为
- **THEN** 系统 SHALL 展示空状态和可参与的圈内行为入口

---

### Requirement: Participation Milestone Achievement

连续参与进度达到 3 天、7 天或 14 天时 SHALL 展示对应成就进度或已获得徽章。

#### Scenario: Milestone reached at 3 days
- **WHEN** 成员连续参与进度达到 3 天
- **THEN** 系统 SHALL 展示对应成就进度

#### Scenario: Milestone reached at 7 days
- **WHEN** 成员连续参与进度达到 7 天
- **THEN** 系统 SHALL 展示对应成就进度或已获得徽章

#### Scenario: Milestone reached at 14 days
- **WHEN** 成员连续参与进度达到 14 天
- **THEN** 系统 SHALL 展示对应成就进度或已获得徽章
