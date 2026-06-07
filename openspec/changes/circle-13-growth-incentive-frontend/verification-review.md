# circle-13-growth-incentive-frontend 验证审核报告

**审核日期**: 2026-06-04
**审核范围**: design.md, proposal.md, specs/*.md 中的后端 API 引用与实际代码库一致性

---

## 一、验证结果摘要

| 项目 | 状态 | 说明 |
|------|------|------|
| 后端 API 路径 | 严重不匹配 | 4 个 API 路径全部与实际后端不一致 |
| VO 字段定义 | 严重不匹配 | 4 个 VO 均缺少前端文档引用的多个字段 |
| 文档完整性 | 基本完整 | design.md、proposal.md、4 个 spec.md 结构规范 |
| 前后端接口一致性 | 不通过 | 需修正所有 API 路径和 VO 映射 |

---

## 二、后端 API 验证详情

### 2.1 API 路径对照表

| # | 前端文档引用的路径 | 实际后端路径 | 状态 |
|---|-------------------|-------------|------|
| 1 | `GET /api/v1/content/circle/{circleId}/level` | `GET /api/v1/content/user/growth/level/info?circleId={circleId}` | 不匹配 |
| 2 | `GET /api/v1/content/circle/{circleId}/growth/me` | `GET /api/v1/content/user/growth/info?circleId={circleId}&userId={userId}` | 不匹配 |
| 3 | `GET /api/v1/content/circle/{circleId}/badges` | `GET /api/v1/content/user/growth/achievement/list?circleId={circleId}&userId={userId}` | 不匹配 |
| 4 | `GET /api/v1/content/circle/{circleId}/leaderboard` | `GET /api/v1/content/user/growth/leaderboard?circleId={circleId}&dimension={dimension}&period={period}&currentUserId={userId}` | 不匹配 |

### 2.2 后端 Controller 位置

| 功能 | Controller 类 | 基础路径 |
|------|--------------|---------|
| 圈子等级 | `CircleLevelController` | `/api/v1/content/user/growth/level` |
| 成员成长 | `MemberGrowthController` | `/api/v1/content/user/growth` |
| 成就徽章 | `AchievementController` | `/api/v1/content/user/growth/achievement` |
| 排行榜 | `LeaderboardController` | `/api/v1/content/user/growth/leaderboard` |

所有 Controller 均位于: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/api/v1/content/user/growth/controller/`

### 2.3 补充发现的额外 API

后端还提供了以下前端文档未提及的接口:

| 接口 | 路径 | 说明 |
|------|------|------|
| 连续参与天数 | `GET /api/v1/content/user/growth/participation?circleId=&userId=` | MemberGrowthController |
| 成长汇总 | `GET /api/v1/content/user/growth/summary?userId=` | ContentUserGrowthController |
| 勋章分类目录 | `GET /api/v1/content/user/growth/badge/catalog?userId=` | ContentUserGrowthController |
| 勋章详情 | `GET /api/v1/content/user/growth/badge/detail?userId=&badgeCode=` | ContentUserGrowthController |
| 佩戴勋章 | `POST /api/v1/content/user/growth/badge/wear` | ContentUserGrowthController |
| 查询佩戴勋章 | `GET /api/v1/content/user/growth/badge/worn?userId=` | ContentUserGrowthController |
| 等级权益摘要 | `GET /api/v1/content/user/growth/level/benefit?userId=` | ContentUserGrowthController |
| 等级配置 | `GET /api/v1/content/user/growth/level/config` | ContentUserGrowthController |

---

## 三、VO 字段不匹配详情

### 3.1 CircleLevelVO (圈子等级)

**前端文档期望字段**:
- level, levelName, growthScore, nextLevelThreshold, progressPercent
- nextLevelConditions (成员数、内容数、互动数差距)
- benefits (权益列表: 已解锁/未解锁)

**实际后端字段**:
```java
private Integer level;
private String levelName;
private Integer growthScore;
private Integer nextLevelThreshold;
private Integer progressPercent;
```

**缺失字段**: `nextLevelConditions`, `benefits`

### 3.2 MemberGrowthVO (成员成长)

**前端文档期望字段**:
- expPoints, contributionPoints, rank, level
- dailyExpLimit, todayExp (每日经验上限/今日经验)
- levelProgress (等级进度)
- recentBadges (最近徽章摘要)

**实际后端字段**:
```java
private String circleId;
private Integer expPoints;
private Integer contributionPoints;
private Integer level;
private Integer postCount;
private Integer participationDays;
private Integer rank;
```

**缺失字段**: `dailyExpLimit`, `todayExp`, `levelProgress`, `recentBadges`
**额外字段**: `postCount`, `participationDays` (前端文档未提及但后端已提供)

### 3.3 AchievementVO (成就徽章)

**前端文档期望字段**:
- badgeId, name, icon, description
- earned, earnedDate (获得时间)
- progress, targetValue (进度/目标值)
- status (active/revoked)
- conditionDesc (达成条件)

**实际后端字段**:
```java
private String achievementType;
private String name;
private String description;
private Boolean earned;
private String conditionDesc;
```

**缺失字段**: `badgeId`, `icon`, `earnedDate`, `progress`, `targetValue`, `status`

### 3.4 LeaderboardEntryVO (排行榜条目)

**前端文档期望字段**:
- userId, username, avatar, score, rankNum
- highlighted (当前用户高亮)

**实际后端字段**:
```java
private String userId;
private Integer score;
private Integer rankNum;
private Boolean highlighted;
```

**缺失字段**: `username`, `avatar`

---

## 四、前端文档问题列表

### 4.1 design.md 问题

| 编号 | 问题 | 严重度 | 位置 |
|------|------|--------|------|
| D1 | API 路径全部使用 RESTful 风格 `/api/v1/content/circle/{circleId}/xxx`，与实际后端 `/api/v1/content/user/growth/xxx?circleId=` 不一致 | 高 | Risks / Trade-offs 第1条 |
| D2 | 未提及后端已有 `participation` 接口可直接获取连续参与天数 | 中 | Decisions D3 |
| D3 | 未区分「圈子等级」(CircleLevel) 和「用户成长」(ContentUserGrowth) 两套独立体系 | 中 | Context |

### 4.2 proposal.md 问题

| 编号 | 问题 | 严重度 | 位置 |
|------|------|--------|------|
| P1 | API 依赖描述为「4 个新接口」，实际后端路径和参数与文档不符 | 高 | Impact > API 依赖 |
| P2 | 未提及 `AchievementController` 的存在，文档中使用「徽章」但后端使用「成就(Achievement)」 | 中 | Capabilities > badge-system |

### 4.3 specs/circle-level/spec.md 问题

| 编号 | 问题 | 严重度 | 位置 |
|------|------|--------|------|
| S1 | API 路径 `GET /api/v1/content/circle/{circleId}/level` 应为 `GET /api/v1/content/user/growth/level/info?circleId=` | 高 | Requirement: 圈子等级信息 API 对接 |
| S2 | 期望 `CircleLevelVO` 包含 `nextLevelConditions` 和 `benefits`，后端 VO 缺失 | 高 | Scenario: 展示差距条件 / 展示已解锁权益 |
| S3 | 进度条「分项指标展开」功能后端无对应数据支撑 | 中 | Scenario: 点击进度条展开分项指标 |

### 4.4 specs/member-growth/spec.md 问题

| 编号 | 问题 | 严重度 | 位置 |
|------|------|--------|------|
| S4 | API 路径 `GET /api/v1/content/circle/{circleId}/growth/me` 应为 `GET /api/v1/content/user/growth/info?circleId=&userId=` | 高 | Requirement: 成员成长信息 API 对接 |
| S5 | 期望 `MemberGrowthVO` 包含 `dailyExpLimit`, `todayExp`，后端 VO 缺失 | 高 | Requirement: 每日经验上限展示 |
| S6 | 期望包含 `recentBadges`，后端 VO 缺失 | 中 | Requirement: 徽章摘要展示 |
| S7 | 后端已有 `participationDays` 字段，但文档仍描述为需前端计算 | 低 | Requirement: 连续参与进度展示 |

### 4.5 specs/badge-system/spec.md 问题

| 编号 | 问题 | 严重度 | 位置 |
|------|------|--------|------|
| S8 | API 路径 `GET /api/v1/content/circle/{circleId}/badges` 应为 `GET /api/v1/content/user/growth/achievement/list?circleId=&userId=` | 高 | Requirement: 徽章列表 API 对接 |
| S9 | 期望 `BadgeVO` 包含 `icon`, `earnedDate`, `progress`, `targetValue`, `status`，后端 `AchievementVO` 缺失 | 高 | 多个 Scenario |
| S10 | 后端使用 `Achievement` 命名而非 `Badge`，文档术语不一致 | 中 | 全文 |

### 4.6 specs/leaderboard/spec.md 问题

| 编号 | 问题 | 严重度 | 位置 |
|------|------|--------|------|
| S11 | API 路径 `GET /api/v1/content/circle/{circleId}/leaderboard` 应为 `GET /api/v1/content/user/growth/leaderboard?circleId=&dimension=&period=&currentUserId=` | 高 | Requirement: 排行榜样 API 对接 |
| S12 | 期望 `LeaderboardVO` 包含 `username`, `avatar`，后端 `LeaderboardEntryVO` 缺失 | 高 | Scenario: 展示 Top 50 列表 |
| S13 | 排行榜样接口需传 `currentUserId` 参数，文档未提及 | 中 | Requirement: 排行榜样 API 对接 |

---

## 五、建议修复方案

### 5.1 高优先级修复 (API 路径)

**修复方式**: 更新所有 spec.md 和 design.md 中的 API 路径，使其与实际后端一致。

| 文档 | 旧路径 | 新路径 |
|------|--------|--------|
| circle-level/spec.md | `GET /api/v1/content/circle/{circleId}/level` | `GET /api/v1/content/user/growth/level/info?circleId={circleId}` |
| member-growth/spec.md | `GET /api/v1/content/circle/{circleId}/growth/me` | `GET /api/v1/content/user/growth/info?circleId={circleId}&userId={userId}` |
| badge-system/spec.md | `GET /api/v1/content/circle/{circleId}/badges` | `GET /api/v1/content/user/growth/achievement/list?circleId={circleId}&userId={userId}` |
| leaderboard/spec.md | `GET /api/v1/content/circle/{circleId}/leaderboard` | `GET /api/v1/content/user/growth/leaderboard?circleId={circleId}&dimension={dimension}&period={period}&currentUserId={userId}` |

### 5.2 高优先级修复 (VO 字段)

**方案 A (推荐)**: 修改后端 VO 补充缺失字段
- CircleLevelVO: 增加 `nextLevelConditions`, `benefits`
- MemberGrowthVO: 增加 `dailyExpLimit`, `todayExp`, `recentBadges`
- AchievementVO: 增加 `badgeId`, `icon`, `earnedDate`, `progress`, `targetValue`, `status`
- LeaderboardEntryVO: 增加 `username`, `avatar`

**方案 B**: 修改前端文档适配现有 VO 字段，降级 UI 设计

### 5.3 中优先级修复 (术语统一)

- 统一使用后端术语「Achievement(成就)」替代文档中的「Badge(徽章)」
- 或在 API 封装层做术语映射

### 5.4 低优先级修复

- 更新 design.md 补充后端已有接口说明
- 更新 proposal.md 的 Impact 部分反映实际 API 路径

---

## 六、后端遗留代码清单

详见 `backend-issues.md`。
