## ADDED Requirements

### Requirement: Experience Points from Content Creation

成员在圈子内发布内容成功后 SHALL 获得 10 点经验值和 10 点贡献值。

#### Scenario: Gain exp on post creation
- **WHEN** 成员在圈子内发布内容且内容进入可见状态
- **THEN** 成员 SHALL 获得 10 点经验值和 10 点贡献值

#### Scenario: No exp for non-visible content
- **WHEN** 成员发布的内容未进入可见状态（审核中、被拦截）
- **THEN** 成员 SHALL NOT 获得经验值和贡献值

---

### Requirement: Experience Points from Commenting

成员在圈子内发表评论成功后 SHALL 获得 3 点经验值和 3 点贡献值。

#### Scenario: Gain exp on comment creation
- **WHEN** 成员在圈子内发表评论且评论进入可见状态
- **THEN** 成员 SHALL 获得 3 点经验值和 3 点贡献值

---

### Requirement: Experience Points from Featured Content

成员的内容被标记为精华后 SHALL 获得额外 30 点经验值和 50 点贡献值。

#### Scenario: Gain bonus exp on content featured
- **WHEN** 成员的内容被标记为精华且加精操作完成
- **THEN** 成员 SHALL 获得额外 30 点经验值和 50 点贡献值

---

### Requirement: Daily Experience Cap

同一成员在单个圈子内每日最多 SHALL 获得 100 点经验值，超出后行为仍记录但不增加经验值。

#### Scenario: Daily cap enforcement
- **WHEN** 成员当日在同一圈子的经验值累计达到 100 点
- **THEN** 后续行为 SHALL 仍记录为完成但不再增加经验值

#### Scenario: Daily cap resets per day
- **WHEN** 进入新的自然日
- **THEN** 成员的经验值计数 SHALL 重置为 0

---

### Requirement: Experience Points Revocation

内容被删除、撤回或判定违规后，对应经验值和贡献值 SHALL 从展示口径中移除。

#### Scenario: Revoke exp on content deletion
- **WHEN** 成员的内容被删除或撤回
- **THEN** 该内容对应的经验值和贡献值 SHALL 从成员成长记录中扣除

#### Scenario: Revoke exp on content violation
- **WHEN** 成员的内容被判定违规
- **THEN** 该内容对应的经验值和贡献值 SHALL 从成员成长记录中扣除

---

### Requirement: Member Growth Profile Display

成员查看个人资料时 SHALL 展示其在各圈子的经验值、贡献值、当前等级、下一等级进度和圈内排名。

#### Scenario: View growth info on profile
- **WHEN** 成员查看个人资料
- **THEN** 系统 SHALL 展示该成员在各圈子的经验值、贡献值、当前等级、下一等级进度和圈内排名

#### Scenario: Growth info respects circle visibility
- **WHEN** 私有圈子的成员查看成长信息
- **THEN** 详细成长信息 SHALL 仅对已加入成员、创建者、版主和系统管理员展示
