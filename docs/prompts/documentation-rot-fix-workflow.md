# 文档腐化修复工作流 — 圈子成长激励体系

> **使用场景**: 当 openspec change 目录下的 design.md、tasks.md、specs/*.md 以及 PRD 文档与后端实际 API 路径、VO 字段、数据库表结构出现不一致时，按此工作流系统性修正。
>
> **核心原则**: PRD 需求描述 + 实际数据库表是双真理。代码实现（Controller、VO）服务于需求和数据模型。文档必须与 PRD 需求、数据库表、代码实现三者对齐。

---

## 一、三套成长体系概念定义（修正前必读）

在进行任何修正前，必须理解这三套独立体系的边界。**混淆它们是文档腐化的根本原因**。

| 体系 | 数据库表 | Controller 前缀 | 特点 |
|------|---------|---------------|------|
| **全局内容社区用户成长** | `content_user_*` 系列表 | `/api/v1/content/user/growth/` | **无 circleId**，整个内容社区通用的积分/等级/勋章/兑换体系，有衰减降级机制 |
| **圈子等级** | `circle_level` | `/api/v1/content/circle/growth/level/` | 只有 circleId，圈子本身的等级（L1-L5 新芽圈→标杆圈） |
| **圈子内成员成长** | `circle_member_growth` 等 5 张表 | `/api/v1/content/user/growth/` | **有 circleId + userId**，用户在某个圈子内的经验/贡献/徽章/排名 |

**关键区分规则**（按「谁在成长」判断）：

```
谁是成长的主体？
  ├── 用户（在整个平台的成长） → 第 1 列「全局用户成长」
  │    特征: 无 circleId，只有 userId
  │    数据: content_user_* 系列表
  │    API:  /api/v1/content/user/growth/
  │
  ├── 圈子本身（圈子的等级成长） → 第 2 列「圈子等级」
  │    特征: 有 circleId，无 userId（关注圈子这个主体）
  │    数据: circle_level 表
  │    API:  /api/v1/content/circle/growth/level/
  │
  └── 用户在圈子内的成长 → 第 3 列「圈子内成员成长」
       特征: 同时有 circleId + userId（关注用户在特定圈子内的表现）
       数据: circle_member_growth 等 5 张表
       API:  /api/v1/content/user/growth/
```

**容易混淆的陷阱**：
- 第 1 列和第 3 列的 API 前缀**相同**（都是 `/api/v1/content/user/growth/`），只能通过 **是否有 circleId 参数** 来区分
- 第 2 列和第 3 列**都有 circleId**，但第 2 列关注「圈子本身的等级」，第 3 列关注「成员在圈子内的表现」
- 判断口诀：**看表不看路径**——`circle_level` 表 = 第 2 列，`circle_member_growth` 表 = 第 3 列，`content_user_*` 表 = 第 1 列

---

## 二、真理来源（Ground Truth）

修正时以 PRD 需求和实际数据库表为**根本真理**，代码实现必须服务于它们。

### 真理层级

```
        ┌──────────────────────────────────────┐
        │  PRD 文字描述（需求定义，什么是「对」）  │  ← 第一真理
        │  实际数据库表（数据模型，真实数据结构）   │  ← 第一真理
        └──────────────────────────────────────┘
                        │
                        │ 代码实现必须对齐 PRD 需求和数据库表
                        │ 若代码与需求/表不一致 → 代码有 bug/缺失
                        ▼
        ┌──────────────────────────────────────┐
        │  后端 Controller（API 路径）          │  ← 实现细节
        │  后端 VO 类（接口出参字段）            │  ← 实现细节
        └──────────────────────────────────────┘
                        │
                        │ 文档应对齐 PRD 需求 + 数据库表 + 代码实现三者
                        ▼
        ┌──────────────────────────────────────┐
        │  openspec change 文档                │  ← 待修正
        │  design.md / tasks.md / specs/*.md   │
        └──────────────────────────────────────┘
```

### 各来源的权威性定义

| 来源 | 权威性 | 说明 |
|------|--------|------|
| **PRD 文字描述** | 需求真相 | 定义了「系统应该做什么」：经验值规则、等级门槛、徽章种类、有效参与行为定义、产品目标、验收标准。**PRD 的业务逻辑不可修改**。 |
| **实际数据库表** | 数据真相 | Flyway DDL 中 `CREATE TABLE` 语句定义的字段名、字段类型、约束、索引。**表的字段名和结构是数据模型的真实表达**。VO 字段名应与表字段名对齐（Java 驼峰 ↔ SQL 下划线）。 |
| 后端 Controller 注解 | API 路径真相 | `@RequestMapping` + `@GetMapping` 定义了实际的 HTTP 路径。路径应服务于 PRD 的概念分组。若路径与 PRD 概念分组冲突 → 路径可能有问题，需对照 PRD 和数据库表检查。 |
| 后端 VO 类 | 接口出参真相 | VO 字段应完整映射数据库表字段，满足 PRD 需求。若 PRD 要求展示某数据、数据库有此字段、但 VO 缺失 → VO 有缺失，需补充。 |
| openspec change 文档 | **待修正对象** | design.md / tasks.md / specs/ 必须与上述所有来源对齐 |

### 关键修正方向（与常见做法不同）

```
❌ 错误方向: 以代码为准，修改 PRD 和 spec 去匹配代码
✅ 正确方向: 以 PRD 需求 + 数据库表为准，检查代码是否正确实现，修正文档使之对齐

具体判断:
  - PRD 描述的需求 vs 代码实现 → 若代码未实现 PRD 需求 → 代码有 bug，文档标注「待后端实现」
  - 数据库表字段 vs VO 字段 → 若 VO 缺少数据库已有字段 → VO 有缺失，需补充到 VO
  - 数据库表字段 vs PRD TypeScript interface → PRD interface 字段应对齐数据库字段名
  - API 路径 vs PRD 概念分组 → 路径应体现 PRD 的体系区分（圈子等级 /circle/growth/ vs 用户成长 /user/growth/）
```

### 数据获取优先级

| 步骤 | 读取内容 | 目的 |
|------|---------|------|
| 1 | **PRD 全文** | 理解需求：等级体系、经验值规则、徽章种类、排行榜维度、验收标准 |
| 2 | **Flyway DDL SQL** | 获取数据库表名、字段名、字段类型、约束（这是真实的数据模型） |
| 3 | Controller 注解 | 获取实际 API 路径 |
| 4 | VO 类字段 | 获取实际接口出参字段，与 DDL 表字段对比检查是否有缺失 |
| 5 | openspec 文档 | 查找与以上来源不一致之处，分类修正 |

---

## 三、错误类型分类与修正规则

### 类型 A：API 路径前缀与 PRD 概念分组不符

**症状**: 文档中 API 路径使用了错误的前缀，将圈子等级 API 和用户成长 API 混用。或者 API 路径与 PRD 描述的概念分组不匹配。

**判断规则**（以 PRD 概念分组 + 数据库表为判断基准）:
```
PRD 描述了「圈子等级的 5 级体系（L1-L5）」，数据库表是 circle_level（只有 circleId，表示圈子本身的等级）:
  → 这是第 2 列「圈子等级」体系
  → API 路径应体现圈子成长概念: /api/v1/content/circle/growth/level/

PRD 描述了「成员经验值、贡献值、徽章」，数据库表是 circle_member_growth 等（有 circleId+userId）:
  → 这是第 3 列「圈子内成员成长」体系
  → API 路径: /api/v1/content/user/growth/

PRD 描述了「全局积分、勋章」，数据库表是 content_user_* 系列（无 circleId）:
  → 这是第 1 列「全局内容社区用户成长」体系
  → API 路径: /api/v1/content/user/growth/
```

**修正方法**:
1. 从 PRD 识别功能属于哪个体系（第 1/2/3 列）
2. 检查 Controller 实际 `@RequestMapping` 是否与 PRD 概念分组一致
3. 文档中路径修正为实际 Controller 路径
4. 如果 Controller 路径与 PRD 概念分组冲突 → 标注「API 路径与 PRD 概念分组不一致，需确认是否调整」

### 类型 B：字段名不匹配（需求 ↔ 数据库 ↔ 代码）

**症状**: PRD 描述、数据库表字段、VO 字段三者之间命名不一致。

**根本原则**: 数据库表字段名是数据模型的**真实表达**。PRD 定义「有什么数据」，数据库定义「字段叫什么」，VO 和前端 interface 应与数据库对齐。

**修正方向**（不是单向对齐，而是逐步关联）:
```
PRD 概念（如「经验值」）
  → 对应数据库字段: exp_points（来自 DDL）
  → 对应 VO 字段: expPoints（Java 驼峰映射）
  → 前端 interface 字段: expPoints（与 VO 一致）

PRD 中的 TypeScript interface 字段名 → 应对齐数据库字段名（驼峰化）
VO 字段名 → 应对齐数据库字段名（驼峰化），若缺失则 VO 有 bug
```

**修正方法**:
1. 从 Flyway DDL 提取所有表的字段名（这是标准名）
2. 从 PRD 提取数据概念列表（如「经验值」「贡献值」「成长分」）
3. 建立 PRD 概念 ↔ 数据库字段的映射关系
4. 读取每个 VO 的 Java 源文件，检查是否完整映射了数据库字段
5. 修正 PRD TypeScript interface 字段名 → 对齐数据库字段名（驼峰化）
6. 修正前端 design.md 字段映射表 → 以数据库字段名为准

**字段名三层对照表（已验证：数据库 DDL ↔ VO 源码）**:

| PRD 概念 | 数据库字段 | VO 实际字段 | VO 文件 | 对齐状态 |
|----------|-----------|------------|--------|---------|
| 经验值 | `exp_points` | `expPoints` | MemberGrowthVO | ✅ DB↔VO 对齐 |
| 贡献值 | `contribution_points` | `contributionPoints` | MemberGrowthVO | ✅ DB↔VO 对齐 |
| 发帖数 | `post_count` | `postCount` | MemberGrowthVO | ✅ DB↔VO 对齐 |
| 评论数 | `comment_count` | ❌ VO 无此字段 | — | ⚠️ DB 有，VO 缺失 |
| 精华数 | `featured_count` | ❌ VO 无此字段 | — | ⚠️ DB 有，VO 缺失 |
| 成长分 | `growth_score` | `growthScore` | CircleLevelVO | ✅ DB↔VO 对齐 |
| 成员得分 | `member_score` | `memberScore` | CircleLevelVO | ✅ DB↔VO 对齐 |
| 内容得分 | `content_score` | `contentScore` | CircleLevelVO | ✅ DB↔VO 对齐 |
| 活跃得分 | `activity_score` | `activityScore` | CircleLevelVO | ✅ DB↔VO 对齐 |
| 徽章类型 | `achievement_type` | `achievementType` | AchievementVO | ✅ DB↔VO 对齐 |
| 徽章名称 | `name` | `name` | AchievementVO | ✅ DB↔VO 对齐 |
| 徽章图标 | `icon_url` | `iconUrl` | AchievementVO | ✅ DB↔VO 对齐 |
| 条件描述 | `condition_desc` | `conditionDesc` | AchievementVO | ✅ DB↔VO 对齐 |
| 排行榜得分 | `score` | `score` | LeaderboardEntryVO | ✅ DB↔VO 对齐 |
| 排行榜排名 | `rank_num` | `rankNum` | LeaderboardEntryVO | ✅ DB↔VO 对齐 |
| 是否撤销 | `revoked` | ❌ VO 无此字段 | — | ⚠️ DB 有，VO 可能内部使用 |
| 业务日期 | `biz_date` | ❌ VO 无此字段 | — | 仅内部流水表使用 |
| 快照时间 | `snapshot_time` | ❌ VO 无此字段 | — | 仅内部快照表使用 |

**结论**: 数据库 ↔ VO 基本对齐（核心业务字段全部一致）。主要问题是 **PRD TypeScript interface 的字段名与 VO 不一致**，需逐字段修正。详见上方 PRD 修正对照表。

**PRD TypeScript interface 字段修正对照表**（已核实数据库 DDL + VO 源码）:

以下是 PRD 中 4 个 interface 的字段与实际 VO 的逐一对比。修正方向：PRD interface 字段 → VO 实际字段（即 API 实际返回的 JSON 字段名）。

#### CircleLevelVO（PRD §5.2 vs 实际 CircleLevelVO.java）

| PRD interface 字段 | 实际 VO 字段 | 数据库字段 | 判断 |
|-------------------|-------------|-----------|------|
| `circleId` | ❌ VO 无此字段 | `circle_level.circle_id` | VO 缺失，调用方已知 circleId |
| `currentLevel` | `level` | `level` | PRD 需改为 `level` |
| `levelName` | `levelName` ✅ | — | 一致 |
| `growthScore` | `growthScore` ✅ | `growth_score` | 一致 |
| `nextLevelScore` | `nextLevelThreshold` | — | PRD 需改为 `nextLevelThreshold` |
| `progressPercent` | `progressPercent` ✅ | — | 一致 |
| `memberGap` | `memberScore` | `member_score` | PRD 概念错误！VO 提供的是「得分」非「差距」，差距在 `nextLevelConditions[].gap` |
| `contentGap` | `contentScore` | `content_score` | 同上 |
| `interactionGap` | `activityScore` | `activity_score` | 同上 |
| `benefits: string[]` | `benefits: List<CircleBenefitVO>` | — | PRD 类型错误！实际是 `{name, unlocked}[]` 非 `string[]` |
| `nextBenefits: string[]` | `nextLevelConditions: List<LevelConditionVO>` | — | PRD 概念错误！是条件列表 `{type, label, current, required, gap}[]` |

#### MemberGrowthVO（PRD §5.2 vs 实际 MemberGrowthVO.java）

| PRD interface 字段 | 实际 VO 字段 | 数据库字段 | 判断 |
|-------------------|-------------|-----------|------|
| `experience` | `expPoints` | `exp_points` | PRD 需改为 `expPoints` |
| `contribution` | `contributionPoints` | `contribution_points` | PRD 需改为 `contributionPoints` |
| `currentLevel` | `level` | `level` | PRD 需改为 `level` |
| `levelName` | `levelName` ✅ | — | 一致 |
| `nextLevelExp` | `nextLevelThreshold` | — | PRD 需改为 `nextLevelThreshold` |
| `rank` | `rank` ✅ | — | 一致 |
| `streakDays` | `participationDays` | —（VO 计算字段） | PRD 需改为 `participationDays` |
| `streakDetail: boolean[]` | ❌ VO 无此字段 | — | 需单独调 `/participation` 接口 |
| `todayExp` | `todayExp` ✅ | —（VO 计算字段） | 一致 |
| `dailyExpCap` | `dailyExpLimit` | —（常量值） | PRD 需改为 `dailyExpLimit` |
| `badges: BadgeSummary[]` | `recentBadges: List<AchievementVO>` | — | PRD 需改为 `recentBadges`，类型改为 `AchievementVO` |
| `totalBadges` | ❌ VO 无此字段 | — | VO 缺失 |
| `totalBadgeCount` | ❌ VO 无此字段 | — | VO 缺失 |
| — | `postCount` | `post_count` | PRD interface 遗漏此字段 |
| — | `progressPercent` | — | PRD interface 遗漏此字段 |

#### AchievementVO（PRD §5.2 BadgeVO vs 实际 AchievementVO.java）

| PRD interface 字段 | 实际 VO 字段 | 数据库字段 | 判断 |
|-------------------|-------------|-----------|------|
| `badgeId` | `achievementType` | `achievement_type` | PRD 需改为 `achievementType` |
| `badgeName` | `name` | `name` | PRD 需改为 `name` |
| `badgeIcon` | `iconUrl` | `icon_url` | PRD 需改为 `iconUrl` |
| `description` | `description` ✅ | `description` | 一致 |
| `earned` | `earned` ✅ | — | 一致 |
| `earnedDate` | `earnedDate` ✅ | — | 一致 |
| `progress` | `currentProgress` | — | PRD 需改为 `currentProgress` |
| `target` | `targetProgress` | — | PRD 需改为 `targetProgress` |
| `nearComplete` | `status`（值 `CLOSE`） | — | PRD 需改为 `status`，用 `=== 'CLOSE'` 判断即将达成 |
| — | `conditionDesc` | `condition_desc` | PRD interface 遗漏此字段 |
| — | `status: 'EARNED'\|'CLOSE'\|'UNEARNED'` | — | PRD interface 遗漏此字段 |

#### LeaderboardVO / LeaderboardEntryVO（PRD §5.2 vs 实际 LeaderboardEntryVO.java）

| PRD interface 字段 | 实际 VO 字段 | 数据库字段 | 判断 |
|-------------------|-------------|-----------|------|
| `LeaderboardVO.entries` | 后端返回 `Result<List<LeaderboardEntryVO>>` 扁平数组 | — | PRD 结构错误，不是嵌套对象 |
| `LeaderboardVO.currentUser` | ❌ 无独立字段，通过 `highlighted` 识别 | — | 前端从数组中 filter `highlighted === true` |
| `LeaderboardVO.currentUserGapToPrev` | ❌ 无顶层字段，在 entry 内 | — | 从当前用户 entry 的 `gap` 字段取值 |
| `LeaderboardVO.totalCount` | ❌ 需确认后端是否返回 | — | 待核实 |
| `LeaderboardEntryVO.rank` | `rankNum` | `rank_num` | PRD 需改为 `rankNum` |
| `LeaderboardEntryVO.userId` | `userId` ✅ | `user_id` | 一致 |
| `LeaderboardEntryVO.userName` | `username` | — | PRD 需改为 `username` |
| `LeaderboardEntryVO.userAvatar` | `avatar` | — | PRD 需改为 `avatar` |
| `LeaderboardEntryVO.value` | `score` | `score` | PRD 需改为 `score` |
| `LeaderboardEntryVO.isCurrentUser` | `highlighted` | — | PRD 需改为 `highlighted` |
| — | `gap` | — | PRD interface 遗漏此字段 |

#### 新增 VO（PRD 未提及，但后端已实现）

| VO | 用途 | 关键字段 |
|----|------|---------|
| `LevelConditionVO` | 下一等级条件项 | `type`, `label`, `current`, `required`, `gap` |
| `CircleBenefitVO` | 等级权益项 | `name`, `unlocked` |

**修正原则**: PRD interface 字段名必须与 VO 字段名一致（即 API 实际返回的 JSON key）。PRD 的文字描述（如「展示经验值」）保持不变，只修正 interface 代码块中的字段名和类型。

### 类型 C：代码实现与 PRD 需求/数据库表不一致

**症状**: 
- PRD 描述了某功能/数据，数据库表有对应字段，但 VO 缺失该字段 → **VO 有 bug**
- PRD 描述了某功能/数据，数据库表有对应字段，VO 也有，但 design.md/spec 声称「后端未提供」→ **文档腐化**
- design.md 声称某字段缺失需「前端硬编码」降级，但数据库表有此字段且 VO 已提供 → **文档腐化**

**这是最严重的腐化类型**，会导致前端按错误策略实现而丢失功能。

**检查方法（以 PRD + 数据库表为判断基准）**:
1. 从 PRD 提取需要展示的数据概念
2. 从数据库 DDL 确认数据是否存在（字段名 + 类型）
3. 从 VO 代码确认字段是否已映射
4. 若数据库有 + VO 有 → 文档中「暂不支持」声明是腐化，删除
5. 若数据库有 + VO 无 → VO 缺失，标注「后端 VO 需补充字段」
6. 若数据库无 + PRD 有需求 → 数据库设计缺失，标注「需增加数据库字段」

**已知文档腐化清单（数据库有字段 + VO 已提供，但文档声称缺失）**:

| 文档位置 | 错误声明 | 数据库字段 | VO 实际状态 |
|---------|---------|-----------|------------|
| 前端 design.md D9 | `dailyExpLimit` 缺失，前端硬编码 100 | —（常量值） | MemberGrowthVO.dailyExpLimit ✅ |
| 前端 design.md D9 | `todayExp` 暂不展示 | —（计算值） | MemberGrowthVO.todayExp ✅ |
| 前端 design.md D9 | `recentBadges` 需单独调接口 | —（关联查询） | MemberGrowthVO.recentBadges ✅ |
| 前端 design.md D9 | `badgeIcon` 使用本地兜底图标 | `circle_achievement.icon_url` | AchievementVO.iconUrl ✅ |
| 前端 design.md D9 | `earnedDate` 暂不展示 | `circle_member_achievement.create_time` | AchievementVO.earnedDate ✅ |
| 前端 design.md D9 | `progress/targetValue` 用 conditionDesc 替代 | —（计算值） | AchievementVO.currentProgress / targetProgress ✅ |
| 前端 design.md D9 | 即将达成需前端解析 conditionDesc | —（计算值） | AchievementVO.status (EARNED/CLOSE/UNEARNED) ✅ |
| 前端 design.md D9 | `username/userAvatar` 额外调用户接口 | —（关联查询） | LeaderboardEntryVO.username / avatar ✅ |
| 前端 design.md D9 | `gapToPrev` 暂不展示 | —（计算值） | LeaderboardEntryVO.gap ✅ |
| 前端 design.md D9 | `nextLevelConditions/benefits` 暂不展示 | `circle_level.growth_score` 等 | CircleLevelVO.benefits / nextLevelConditions ✅ |
| specs/circle-level/spec.md | 权益列表暂不展示 | — | benefits ✅ |
| specs/circle-level/spec.md | 不支持展开分项指标 | `circle_level.member_score/content_score/activity_score` | nextLevelConditions ✅ |
| specs/member-growth/spec.md | 今日经验值暂不展示 | — | todayExp/dailyExpLimit ✅ |
| specs/member-growth/spec.md | 需单独调 achievement/list 获取徽章 | `circle_member_achievement` 表 | recentBadges ✅ |

### 类型 D：数据库表名/版本号错误

**症状**: tasks.md 或 design.md 中引用的 Flyway 版本号、表名与实际迁移脚本不一致。

**修正方法**: 直接读取 Flyway SQL 文件名和 CREATE TABLE 语句，更新文档中的引用。

### 类型 E：API 接口清单不完整

**症状**: 文档中的 API 清单缺少已实现的新接口。

**已知缺失**:
- `GET /api/v1/content/circle/growth/level/benefit?userId=` — 等级权益摘要（CircleLevelController）
- `GET /api/v1/content/circle/growth/level/config` — 等级配置列表（CircleLevelController）
- `GET /api/v1/content/user/growth/participation?circleId=&userId=` — 连续参与天数（MemberGrowthController）

### 类型 F：术语混淆

**症状**: 将「徽章」(Achievement) 和「勋章」(Badge) 术语混用，或将圈子等级与全局用户等级混用。

**修正方法**:
- 圈子体系 API/类型层使用 Achievement（与后端命名一致）
- 前端 UI 层可使用「徽章」（用户友好），但代码变量/类型命名使用 Achievement
- 全局体系的 Badge 指「勋章」（有佩戴/回收功能），与圈子体系的徽章是不同功能域

---

## 四、修正工作流

### 第 0 步：进入 worktree（硬性要求）

```
必须先在 git worktree 中进行所有修改，禁止直接改主 worktree。
```

### 第 1 步：收集真相数据（按正确顺序）

**必须按以下顺序执行**，因为 PRD + 数据库表是真理，代码是实现：

```
步骤 1.1: 精读 PRD 需求文档
步骤 1.2: 读取数据库 DDL，提取表名和字段名
步骤 1.3: 对比 PRD 需求和数据库表 → 确认数据模型能否支撑需求
步骤 1.4: 读取 Controller 和 VO 代码 → 检查是否正确实现了 PRD 需求 + 完整映射了数据库字段
```

#### 1.1 提取 PRD 需求要点

从 PRD 中提取以下清单：
- 功能点列表（等级展示、经验值系统、连续参与、徽章系统、排行榜）
- 每个功能点的验收标准
- 每个功能点需要展示的数据概念（如「经验值」「贡献值」「成长分」「排名」）
- PRD 中定义的 TypeScript interface 和 API 路径（先记录下来，不做修正）

#### 1.2 提取数据库表结构（数据真理）

```bash
# 查找所有圈子成长相关的 Flyway 迁移 SQL
find jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/resources/flyway/sql/mysql/ \
  -name "*circle_growth*" -o -name "*circle_level*"
```

读取完整的 DDL，提取**每个表的所有字段名、类型和注释**，输出「数据库表-字段清单」。

#### 1.3 需求-数据对照

将 PRD 需求中的数据概念映射到数据库表字段：

```
PRD: 「展示经验值」 → circle_member_growth.exp_points
PRD: 「展示贡献值」 → circle_member_growth.contribution_points
PRD: 「展示成长分」 → circle_level.growth_score
PRD: 「展示排名」 → circle_leaderboard_snapshot.rank_num
...
```

发现 PRD 有描述但数据库无字段 → 数据库设计有缺失，标注
发现数据库有字段但 PRD 未提及 → PRD 可能未覆盖，标注

#### 1.4 检查代码实现完整性

```bash
# 获取所有 Controller 的实际 API 路径
grep -rn "@RequestMapping\|@GetMapping\|@PostMapping" \
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/growth/controller/

# 列出所有 VO 文件
find jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/growth/vo/ -name "*.java"
```

对每个 VO：
- 列出 VO 所有字段
- 对比数据库表字段 → VO 是否完整映射了数据库字段？
- 对比 PRD 需求 → VO 是否提供了 PRD 需要的所有数据？

**产出物**: 
- PRD 需求要点清单
- 数据库表-字段清单（完整 DDL 字段名）
- PRD 概念 ↔ 数据库字段映射表
- VO 完整性报告（VO 是否完整映射了数据库字段）
- API 路径清单

### 第 2 步：差异分析

逐文件对比，按类型 A-F 分类记录每个差异：

| 文件 | 行/节 | 错误类型 | 文档中内容 | 实际内容 | 严重程度 |
|------|-------|---------|-----------|---------|---------|
| frontend-prd.md | 5.1 接口清单 | A-路径错误 | `/api/v1/content/user/growth/level/info` | `/api/v1/content/circle/growth/level/info` | 🔴 严重 |
| frontend-prd.md | 5.2 CircleLevelVO | B-字段名 | `memberGap` | `memberScore` + `nextLevelConditions` | 🔴 严重 |
| frontend-design.md | D9 降级策略 | C-误判 | `dailyExpLimit` 缺失需硬编码 | 实际已提供 | 🔴 严重 |

**建议使用 subagent 分工**:
- Subagent A：检查 `/circle/growth/` 前缀相关的 API 路径和 VO 字段
- Subagent B：检查 `/user/growth/` 前缀相关的 API 路径和 VO 字段
- Subagent C：检查前端 design.md 和 specs/ 中的降级策略声明

### 第 3 步：修正文档

按优先级修正，每类错误统一处理：

#### 修正顺序

```
1. 后端 design.md — 先修正后端设计文档中的 API 路径和字段映射
2. 后端 tasks.md — 修正任务描述中的 Flyway 版本号、表名等
3. 后端 specs/*.md — 修正 spec 中的 API 路径和字段引用
4. 前端 PRD — 修正 API 路径、VO 接口定义、字段名
5. 前端 design.md — 重写 D9 降级策略表、更新 VO 字段映射表、修正 API 路径
6. 前端 tasks.md — 修正 API 文件路径冲突、接口数量、参数枚举
7. 前端 specs/*.md — 删除错误的「暂不支持」声明，更新为正确对接说明
```

#### 修正规则

- **PRD 中的 TypeScript interface**: 字段名必须与 Java VO 字段名一致（或建立明确的映射表）
- **API 枚举值**: 必须与后端实际枚举值一致。如 `dimension: "EXP"/"CONTRIBUTION"/"POST"`，`period: "WEEK"/"MONTH"/"ALL"`
- **「暂不支持/暂不展示」声明**: 每个都需验证，误判的删除，确实缺失的保留
- **不改变 PRD 的业务逻辑描述**: 经验值规则（发帖+10/评论+3）、等级门槛（0/100/300/600/1000）、徽章种类（6种）等以 PRD 文字描述为准

### 第 4 步：验证

修正完毕后逐项验证：

- [ ] 所有 API 路径与实际 Controller @RequestMapping 一致
- [ ] 所有 VO 字段名与实际 Java 字段名一致
- [ ] 不存在误判的「降级/暂不支持」声明
- [ ] 三套体系（全局用户成长/圈子等级/圈内成员成长）的 API 前缀正确区分
- [ ] API 接口清单完整（含 benefit、config、participation 接口）
- [ ] 数据库表名和 Flyway 版本号正确
- [ ] 术语使用一致（Achievement vs Badge、circleId vs 无 circleId）
- [ ] 前端 tasks.md 的 API 文件路径不与现有 growth/ 目录冲突

---

## 五、需修正的文件清单

### 后端 change (`openspec/changes/circle-13-growth-incentive/`)

| 文件 | 主要问题 |
|------|---------|
| `design.md` | Flyway 版本号 V3.9.1_66 → V3.9.1_67；MemberGrowthVO 字段列表不完整 |
| `tasks.md` | Flyway 版本号；表名引用 |
| `specs/circle-level-system/spec.md` | API 路径前缀 |
| `specs/member-experience/spec.md` | 字段名可能不匹配 |
| `specs/continuous-participation/spec.md` | 接口路径 |
| `specs/achievement-badge/spec.md` | 徽章数量（6种 vs 初始化4种） |
| `specs/circle-leaderboard/spec.md` | 排行榜参数枚举值 |

### 前端 change (`openspec/changes/circle-13-growth-incentive-frontend/`)

| 文件 | 主要问题 |
|------|---------|
| `design.md` | **最严重**：D9 降级策略大面积错误；VO 字段映射表遗漏大量字段；Q4 状态应更新 |
| `tasks.md` | API 文件路径冲突（growth.ts vs growth/ 目录）；任务 3.5 Promise.all 接口数量错误 |
| `specs/circle-level/spec.md` | 错误声明 benefits/分项指标「暂不支持」 |
| `specs/member-growth/spec.md` | 错误声明 todayExp/recentBadges「暂不支持」 |
| `specs/badge-system/spec.md` | 字段名和降级策略 |
| `specs/leaderboard/spec.md` | 枚举参数值未明确；错误声明 username/avatar 需额外调用 |
| `spec-review-report.md` | 已经是正确的审查报告，可作为修正参考 |

### PRD

| 文件 | 主要问题 |
|------|---------|
| `docs/requirements/prd/frontend/EPIC-13-circle-growth-incentive-frontend-prd.md` | API 路径全部为 `/user/growth/` 前缀；VO 字段名与后端不一致；缺 benefit/config 接口；TypeScript interface 字段名需对齐 |

---

## 六、快速修正检查清单（Copilot/Agent 使用）

将以下检查清单嵌入修正 prompt 中，逐项核验：

```
## API 路径检查
- [ ] 每个 API 路径是否与 Controller @RequestMapping 完全一致？
- [ ] 圈子等级接口是否使用 /circle/growth/level/ 前缀（而非 /user/growth/level/）？
- [ ] 接口清单是否包含 benefit、config、participation 三个额外接口？

## VO 字段检查
- [ ] CircleLevelVO 字段是否包含 benefits、memberScore、contentScore、activityScore、nextLevelConditions？
- [ ] MemberGrowthVO 字段是否包含 todayExp、dailyExpLimit、recentBadges、nextLevelThreshold、progressPercent？
- [ ] AchievementVO 字段是否包含 iconUrl、earnedDate、currentProgress、targetProgress、status？
- [ ] LeaderboardEntryVO 字段是否包含 highlighted、gap、username、avatar？

## 降级策略检查
- [ ] 文档中每个「暂不支持/暂不展示/降级」声明是否已逐项验证？
- [ ] 所有错误的降级声明是否已删除？
- [ ] 确实缺失的字段是否标注了「待后端补充」？

## 术语检查
- [ ] 代码/API/类型层是否使用 Achievement（非 Badge）？
- [ ] 圈子等级接口（/circle/growth/）和成员成长接口（/user/growth/）是否区分使用？
- [ ] 全局用户成长体系（无 circleId）和圈子内成员成长（有 circleId）是否区分描述？

## 数据库检查
- [ ] Flyway 版本号是否与实际 SQL 文件名一致？
- [ ] 数据库表名是否与 CREATE TABLE 语句一致？
```

---

## 七、使用示例

### 示例 1：修正 PRD 中的 API 路径

**错误**（PRD 5.1 节）:
```
| 获取圈子等级信息 | GET | /api/v1/content/user/growth/level/info?circleId={circleId} |
```

**实际**（CircleLevelController.java）:
```java
@RequestMapping("/api/v1/content/circle/growth/level")
@GetMapping("/info")
```

**修正后**:
```
| 获取圈子等级信息 | GET | /api/v1/content/circle/growth/level/info?circleId={circleId} |
```

### 示例 2：修正前端 design.md D9 降级策略

**错误声明**:
```
| `dailyExpLimit` | 前端硬编码 100（PRD 定义） |
```

**实际**（MemberGrowthVO.java）:
```java
private Integer dailyExpLimit; // 每日经验上限，后端从 GrowthConstant.DAILY_EXP_CAP = 100 取值
```

**修正后**（删除该行，或改为）:
```
| `dailyExpLimit` | ✅ 后端已提供，直接使用 `dailyExpLimit` 字段 |
```

### 示例 3：修正 spec 中的「暂不支持」

**错误**（specs/circle-level/spec.md）:
```
权益列表展示暂不支持（后端未提供 benefits 字段）
```

**实际**（CircleLevelVO.java）:
```java
private List<String> benefits; // 已解锁权益列表
```

**修正后**（重写 Scenario）:
```
展示已解锁权益列表（从 CircleLevelVO.benefits 字段获取），已解锁权益用勾选图标，未解锁权益用锁定图标并置灰
```

---

## 八、注意事项

1. **PRD 文字需求 + 数据库表是双真理**。PRD 定义了「系统应该做什么」，数据库表定义了「数据真实存在什么字段」。两者之间如果有冲突（PRD 描述了某数据但数据库无对应字段，或数据库有字段但 PRD 未提及），这是真正的设计问题，需标注出来。代码实现（Controller、VO）必须同时对齐 PRD 需求和数据库表结构。

2. **PRD 的 TypeScript interface 字段名应对齐数据库字段名**。PRD 中 interface 代码块里的字段名应与数据库字段名（驼峰化后）一致。这不是修改需求，而是让需求的字段命名与数据模型一致。

3. **PRD 的业务逻辑描述不可修改**。经验值规则（发帖+10/评论+3）、等级门槛（0/100/300/600/1000）、徽章种类（6 种）、每日上限（100）等以 PRD 文字描述为准。如果代码实现与 PRD 描述不一致（如代码只初始化了 4 种徽章但 PRD 定义 6 种），这是代码不完整，不是 PRD 错了。

4. **数据库表字段是 VO 字段的标准名**。VO 字段名必须与数据库字段名对应（Java 驼峰 ↔ SQL 下划线）。若 VO 缺少数据库已有字段 → VO 有 bug。若 VO 多出数据库没有的字段 → VO 可能越界。

5. **不要顺手重构无关内容**。仅修正错误，不重新设计文档结构或改进措辞。

6. **区分「前端 PRD」和「前端 design.md」**。PRD 是需求定义，design.md 是实现方案。两者的修正策略不同：PRD 修正 API 路径和 interface 字段名但保留业务描述；design.md 修正实现策略和降级方案（降级方案中的字段判断应基于数据库表 + VO 的实际情况）。

7. **`spec-review-report.md` 已经是正确的审查报告**，可以直接作为修正参考使用。该报告基于后端实际代码做了全面验证，其结论与本文档的「数据库表 + 代码检查」方法一致。

8. **OpenSpec specs/ 目录也是文档，不是真理**。`openspec/changes/*/specs/` 下的 spec.md 文件是设计规格文档，同样可能出现腐化，需按本文档方法修正。
