## ADDED Requirements

### Requirement: Continuous Creator Badge

成员累计发布 10 篇可见内容后 SHALL 自动获得"持续创作者"徽章。

#### Scenario: Badge awarded on 10 posts
- **WHEN** 成员累计发布 10 篇可见内容且成就状态更新
- **THEN** 成员 SHALL 自动获得"持续创作者"徽章

#### Scenario: Badge not awarded before threshold
- **WHEN** 成员累计可见内容少于 10 篇
- **THEN** 成员 SHALL NOT 获得"持续创作者"徽章

---

### Requirement: Quality Contributor Badge

成员累计获得 5 篇精华内容后 SHALL 自动获得"优质贡献者"徽章。

#### Scenario: Badge awarded on 5 featured posts
- **WHEN** 成员累计获得 5 篇精华内容且成就状态更新
- **THEN** 成员 SHALL 自动获得"优质贡献者"徽章

---

### Requirement: Active Participant Badge

成员近 7 天至少 3 天完成有效参与行为后 SHALL 自动获得"活跃参与者"徽章。

#### Scenario: Badge awarded on 3-day participation
- **WHEN** 成员近 7 天至少 3 天完成有效参与行为且成就状态更新
- **THEN** 成员 SHALL 自动获得"活跃参与者"徽章

---

### Requirement: Rising Star Badge

近 7 天经验值增长排名前 10 的成员 SHALL 获得"圈内新星"徽章。

#### Scenario: Badge awarded to top 10 growth members
- **WHEN** 成员近 7 天经验值增长排名进入圈子前 10
- **THEN** 成员 SHALL 获得"圈内新星"徽章

#### Scenario: Badge revoked when out of top 10
- **WHEN** 成员近 7 天经验值增长排名跌出圈子前 10
- **THEN** 成员的"圈内新星"徽章 SHALL 被撤销

---

### Requirement: Badge Notification

成员获得新徽章后 SHALL 发送站内通知提醒。

#### Scenario: Notification on new badge
- **WHEN** 成员获得新徽章且徽章发放完成
- **THEN** 系统 SHALL 发送站内通知提醒

---

### Requirement: Badge Display

成员查看个人资料时 SHALL 展示已获得徽章、未获得徽章、每个徽章的达成条件和当前进度。

#### Scenario: View earned and unearned badges
- **WHEN** 成员查看个人资料
- **THEN** 系统 SHALL 展示已获得徽章和未获得徽章，每个徽章显示达成条件和当前进度

#### Scenario: Badge wall states
- **WHEN** 成员查看徽章墙
- **THEN** 徽章 SHALL 区分已获得、未获得、即将达成 3 种状态

---

### Requirement: Badge Per Circle

徽章 SHALL 按圈子分别获得和展示，不跨圈子合并。

#### Scenario: Badge scope per circle
- **WHEN** 成员在不同圈子达成徽章条件
- **THEN** 各圈子的徽章 SHALL 独立获得和展示

---

### Requirement: Badge Revocation

徽章撤销条件 SHALL 与内容违规、内容删除和成员退出圈子状态保持一致。

#### Scenario: Badge revoked on content violation
- **WHEN** 导致徽章获得的内容被判定违规或删除
- **THEN** 对应徽章 SHALL 被撤销

#### Scenario: Badge revoked on member exit
- **WHEN** 成员退出圈子
- **THEN** 该圈子的所有徽章 SHALL 被撤销
