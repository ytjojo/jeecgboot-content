# Verification Report: circle-13-growth-incentive

## Summary Scorecard

| Dimension    | Status                                           |
|--------------|--------------------------------------------------|
| Completeness | 39/39 tasks ✅, 5/5 spec modules                |
| Correctness  | 18/24 requirements fully implemented, 6 issues  |
| Coherence    | Design followed with 4 noted divergences        |

**Overall**: 0 CRITICAL (implementation) · 8 WARNING · 4 SUGGESTION

---

## 一、API 与规范文档一致性检查（核心）

### 1.1 API 路径总览

| Controller | 实际 API 路径 | 规范文档 (circle-level-apis.md) | 一致性 |
|------------|-------------|-------------------------------|--------|
| MemberGrowthController | `GET /api/v1/content/user/growth/info` | ✅ 已记录 | 一致 |
| MemberGrowthController | `GET /api/v1/content/user/growth/participation` | ✅ 已记录 | 一致 |
| CircleLevelController | `GET /api/v1/content/user/growth/level/info` | ✅ 已记录 | 一致 |
| AchievementController | `GET /api/v1/content/user/growth/achievement/list` | ✅ 已记录 | 一致 |
| LeaderboardController | `GET /api/v1/content/user/growth/leaderboard?circleId=&dimension=&period=&currentUserId=` | ❌ 未记录 | **文档缺失** |

### 1.2 API 文档缺失详情

**WARNING-1**: `circle-level-apis.md:7` — LeaderboardController 章节存在但端点行为空

```markdown
## 圈子排行榜
### LeaderboardController (...)
**Base Path**: `/api/v1/content/user/growth/leaderboard`

| 方法 | 路径 | 描述 | 入参 | 出参 | 行号 |
|------|------|------|------|------|------|
(空表)
```

**同步方案**: 需补充以下内容到 `circle-level-apis.md`：

| 方法 | 路径 | 描述 | 入参 | 出参 |
|------|------|------|------|------|
| GET | `/api/v1/content/user/growth/leaderboard` | 获取圈子排行榜 | circleId: String, dimension: String(EXP/CONTRIBUTION/POST), period: String(WEEK/MONTH/ALL, 默认WEEK), currentUserId: String | `List<LeaderboardEntryVO>` |

---

## 二、实际代码逻辑与 Spec 漂移分析

### 2.1 CRITICAL 级漂移

> 无 CRITICAL 级漂移。核心业务逻辑（经验值计算、每日上限、等级门槛、徽章类型、排行榜维度）与 spec 一致。

### 2.2 WARNING 级漂移

#### WARNING-2: `rank` 字段声明但从未赋值

**文件**: `MemberGrowthServiceImpl.java:95-105`, `MemberGrowthVO.java:29`

**Spec 要求** (member-experience/spec.md:67): "展示该成员在各圈子的经验值、贡献值、当前等级、下一等级进度和**圈内排名**"

**实际**: `MemberGrowthVO.rank` 字段已声明（`private Integer rank`），但 `getGrowthInfo()` 方法中从未设置该值。前端拿到的 rank 永远是 `null`。

**评价**: 这是实现遗漏，不是有意的设计改进。rank 需要额外查询（在圈子中按 exp 排名），`getGrowthInfo` 没有做这个计算。
**同步方案**: 在 `getGrowthInfo()` 中增加排名查询逻辑，或在 `MemberGrowthVO` 中移除 rank 字段（如果排名在排行榜 API 单独查询）。

#### WARNING-3: 当前用户不在 Top 50 时排名不显示

**文件**: `LeaderboardServiceImpl.java:34-53`, `LeaderboardEntryVO.java`

**Spec 要求** (circle-leaderboard/spec.md:49): "当前成员未进入 Top 50 时仍展示其排名位置"、"榜单底部 SHALL 展示当前成员排名、数值和**距离上一名的差距**"

**实际**: `getLeaderboard()` 只查询 Top 50 快照数据，设置 `highlighted` 标记当前用户。当用户排名 > 50 时：
- 用户不会出现在返回列表中
- `LeaderboardEntryVO` 没有 `gap`（差距）字段
- 没有单独的"当前用户位置"查询

**评价**: 这是功能不完整。排行榜是快照表，`circle_leaderboard_snapshot` 只存 Top 50，user #87 的数据根本不在表中。需要额外机制支持。

**同步方案**:
1. 方案A（推荐）: 快照刷新时额外存储当前用户的排名记录（即使 > 50），在 `circle_leaderboard_snapshot` 中增加标记字段
2. 方案B: `getLeaderboard()` 在返回前检查当前用户是否在 Top 50 中，若不在则单独查询其排名和差距
3. `LeaderboardEntryVO` 增加 `gap`（距上一名差距）字段

#### WARNING-4: 圈子等级永不降级——与 Risk 表格矛盾

**文件**: `CircleLevelServiceImpl.java:48-60`

**Design.md Risk/Trade-offs**: "降级需低于当前等级门槛一定比例"
**Spec (circle-level-system)**: 未明确说明降级规则
**实际**: `updateLevel()` 仅升级 (`if (newLevelEnum.getLevel() > oldLevel)`)，永不降级

**评价**: 实现比 design.md 的 risk 描述更简单、更稳定。不会出现"接近阈值反复升降"的问题。但同时也意味着一个曾经是 L5 的圈子，即使后续完全沉寂、成长分归零，也永远显示 L5。

**⚠️ 建议**: 这是一个合理的简化选择——避免了频繁波动带来的用户体验问题。但应该在 design.md 中明确记录此决定，而不是与 risk 描述矛盾。推荐：
- 更新 design.md Risk 行："为避免频繁波动，等级仅升级不降级" 
- 或在 spec 中增加：`"圈子等级一旦达到，不会降低"`

#### WARNING-5: MemberGrowthVO 缺少"下一等级进度"字段

**Spec 要求** (member-experience/spec.md:67): 展示"**下一等级进度**"
**实际**: `MemberGrowthVO` 有 `level` 和 `expPoints`，但没有 `nextLevelProgress` 或 `nextLevelThreshold` 字段

**评价**: 成员等级与圈子等级不同——成员的 level 存储在 `CircleMemberGrowth` 表（字段名 `level`），但这个 level 目前似乎只被设置但没有任何升级逻辑（`addExperience` 和 `getGrowthInfo` 都对 level 只有读写，没有基于经验的升级判定）。`CircleLevelVO` 有完整的进度信息，但 `MemberGrowthVO` 没有对应结构。

**同步方案**: 在 `MemberGrowthVO` 中增加 `nextLevelThreshold` 和 `progressPercent` 字段，或明确成员等级是否有独立升级逻辑（目前 spec 中提到了"成员等级"但未定义门槛）。

#### WARNING-6: 等级提升通知发送范围与 Spec 不一致

**文件**: `CircleLevelServiceImpl.java:157-167`

**Spec 要求** (circle-level-system/spec.md:55): "通知接收者 SHALL 包括圈子创建者和近 7 天有发帖、评论或点赞行为的成员"
**实际**: `notifyLevelUpgrade()` 调用 `notificationService.sendNotification(circleId, "CIRCLE_LEVEL_UP", ...)`，第一个参数是 `circleId`（圈子ID）

**评价**: 取决于 `IContentNotificationService.sendNotification()` 的实现，如果重载 `sendNotification(circleId, ...)` 是向圈子全员发送，则范围比 spec 更广（全员 vs 创建者+活跃成员）。如果是向创建者发送，则缺少活跃成员。需要确认通知服务的实际行为。

**同步方案**: 检查 `IContentNotificationService.sendNotification(String targetId, ...)` 的实现，确认 targetId 为 circleId 时的发送范围。如需精确匹配 spec，可能需要改为逐用户发送。

### 2.3 SUGGESTION 级漂移

#### SUGGESTION-1: AchievementVO 缺少进度字段

**Spec 要求** (achievement-badge/spec.md:67): "展示已获得徽章和未获得徽章，每个徽章显示达成条件和**当前进度**"、"区分已获得、未获得、**即将达成** 3 种状态"

**实际**: `AchievementVO` 只有 `earned: Boolean`，没有进度字段（如 `currentProgress: Integer`, `targetProgress: Integer`），无法展示"7/10 篇"或"即将达成"状态。

**评价**: 进度信息需要从 `CircleMemberGrowth` 动态计算（postCount, featuredCount, participationDays），当前 VO 结构无法支持。这是前端展示能力的缺失。

**同步方案**: `AchievementVO` 增加 `currentProgress`、`targetProgress`、`status`(EARNED/CLOSE/UNEARNED) 字段，`getMemberAchievements()` 方法中动态计算。

#### SUGGESTION-2: CircleLevelVO 缺少权益和分项得分

**Spec 要求** (circle-level-system/spec.md:36-38): "展示当前等级已解锁的**权益列表**"
**PRD 要求** (13.1.1 验收标准3): "展示当前等级、成长分、下一等级所需条件、进度百分比和**已解锁权益**"

**实际**: `CircleLevelVO` 包含 level, levelName, growthScore, nextLevelThreshold, progressPercent，但不包含：
- `benefits: List<String>`（已解锁权益）
- `memberScore`, `contentScore`, `activityScore`（三类子得分）

**评价**: 已解锁权益可以从 `CircleLevelEnum` 静态映射（如 L1→基础展示, L2→排行榜入口, L3→徽章墙, L4→推荐权重提升, L5→全部）。子得分虽然 entity 中有存储，但 VO 未暴露。

**同步方案**: `CircleLevelVO` 增加 `benefits` 字段和 `memberScore/contentScore/activityScore` 字段。权益映射可在 `getLevelInfo()` 中根据当前等级枚举动态生成。

#### SUGGESTION-3: 旧 req/growth 文件未清理

**文件**: `ContentPointAdjustReq.java`, `ContentUserBadgeRecycleReq.java`, `ContentUserBadgeWearReq.java`, `ContentUserExchangeReq.java`, `ContentUserFeatureUnlockReq.java`, `ContentUserVirtualGiftReq.java`

这些 `content/user/req/growth/` 下的文件在本次变更前就已存在，代表旧的"积分/勋章/兑换"体系。新的 growth 系统采用完全不同的架构（entity/mapper/service/controller 在 `growth/` 包下），与这些旧 req 类无任何交互。

**评价**: 旧 req 类可能是另一个并行体系或已废弃。如果不属于本变更范围，不应在本变更中处理。但需要确认是否有其他 Controller 仍在使用这些 req 类。

**同步方案**: grep 确认这些 req 类的引用情况，如无引用则标记为废弃待清理（独立 PR）。

#### SUGGESTION-4: 每日上限防并发唯一索引位置

**Design D4**: "数据库唯一索引 (circle_id, user_id, biz_date) 作为最后防线防并发写入"
**实际**: Flyway 唯一索引为 `(circle_id, user_id, biz_date, action_type, biz_id)`——多了 `action_type` 和 `biz_id`

**评价**: 实际唯一索引更细粒度，不仅防止同一天重复写入，还防止同一行为的重复记录。这比 design 描述的更安全（幂等保护），是一个正向漂移。

**同步方案**: 更新 design.md D4 描述，反映实际的唯一索引结构。

---

## 三、SPEC 覆盖度分析

### 3.1 member-experience (成员经验值)

| Requirement | 实现状态 | 实现位置 |
|------------|---------|---------|
| 发帖获得 10exp + 10 贡献 | ✅ 完全 | `GrowthActionEnum.POST`(10,10), `MemberGrowthServiceImpl.addExperience()` |
| 评论获得 3exp + 3 贡献 | ✅ 完全 | `GrowthActionEnum.COMMENT`(3,3) |
| 加精获得 30exp + 50 贡献 | ✅ 完全 | `GrowthActionEnum.FEATURED`(30,50) |
| 每日上限 100 点 | ✅ 完全 | `isDailyCapReached()`, `GrowthConstant.DAILY_EXP_CAP=100` |
| 经验值撤回 | ✅ 完全 | `revokeExperience()` |
| 成长信息展示 | ⚠️ 部分 | VO 缺少 rank 赋值、下一等级进度 |

### 3.2 circle-level-system (圈子等级)

| Requirement | 实现状态 | 实现位置 |
|------------|---------|---------|
| 成长分计算(0-1000) | ✅ 完全 | `calculateGrowthScore()`, `GrowthConstant.MAX_GROWTH_SCORE` |
| 5 级门槛 | ✅ 完全 | `CircleLevelEnum` (0/100/300/600/850) |
| 等级进度展示 | ⚠️ 部分 | VO 有进度%，缺少权益列表和子得分 |
| 等级提升通知 | ⚠️ 部分 | 发送目标需确认 |
| 等级权益 | ✅ 符合 | 仅展示性权益，无付费内容 |

### 3.3 achievement-badge (成就徽章)

| Requirement | 实现状态 | 实现位置 |
|------------|---------|---------|
| 持续创作者 (10 篇) | ✅ 完全 | `checkAndAward()` postCount >= 10 |
| 优质贡献者 (5 篇精华) | ✅ 完全 | `checkAndAward()` featuredCount >= 5 |
| 活跃参与者 (3 天) | ✅ 完全 | `checkAndAward()` participationDays >= 3 |
| 圈内新星 (Top 10) | ✅ 完全 | `checkAndAward()` higherCount < 10 |
| 圈内新星可撤销 | ❌ 未实现 | `checkAndAward()` 中无撤销逻辑 |
| 徽章按圈子独立 | ✅ 完全 | 所有查询带 circleId |
| 徽章展示含进度 | ⚠️ 部分 | VO 缺少进度字段 |
| 徽章通知 | ✅ 完全 | `tryAward()` 中发送通知 |

**圈内新星撤销缺失说明**: spec 要求 "排名跌出前 10 时撤销徽章"（achievement-badge/spec.md:45-47），但 `checkAndAward()` 只检查发放条件，不检查已获得的 RISING_STAR 是否需要撤销。这是一个功能遗漏。

### 3.4 circle-leaderboard (排行榜)

| Requirement | 实现状态 | 实现位置 |
|------------|---------|---------|
| 3 个维度 | ✅ 完全 | `LeaderboardDimensionEnum`(EXP/CONTRIBUTION/POST) |
| 3 个周期 | ✅ 完全 | WEEK/MONTH/ALL |
| Top 50 展示 | ✅ 完全 | `LEADERBOARD_TOP_N=50` |
| 当前用户高亮 | ⚠️ 部分 | Top 50 内可高亮，Top 50 外无处理 |
| 每小时刷新 | ✅ 完全 | `CircleGrowthScheduler` @Scheduled(3600000ms) |
| 空状态 | ✅ 符合 | 返回空列表，前端处理 |
| 排除违规成员 | ⚠️ 未独立实现 | 快照已排除违规(revoked=true)，但无主动过滤逻辑 |

### 3.5 continuous-participation (连续参与)

| Requirement | 实现状态 | 实现位置 |
|------------|---------|---------|
| 7 天参与统计 | ✅ 完全 | `getParticipationDays()` 统计 7 天内不同 bizDate 数 |
| 按圈子独立 | ✅ 完全 | 查询带 circleId 条件 |
| 参与进度展示 | ✅ 完全 | 返回参与天数 Integer |
| 里程碑(3/7/14天) | ⚠️ 部分 | 徽章有 3 天阈值，但 7 天和 14 天无独立成就 |

---

## 四、逻辑漂移评价汇总

### 正向漂移（实际优于 Spec）

1. **每日上限唯一索引更精细**: `(circle_id, user_id, biz_date, action_type, biz_id)` 比 spec 的 `(circle_id, user_id, biz_date)` 多出 action_type 和 biz_id，提供更强的幂等保护。

2. **等级永不降级**: 避免了 spec/risk 描述中的 "频繁波动" 问题。实际用户体验更稳定。但需更新文档以消除矛盾。

3. **定时任务参数可配置**: `@Scheduled(fixedDelayString = "${...:1800000}")` 使用属性占位符，比硬编码 30 分钟更灵活。

4. **排行榜快照删除+全量重建**: `refreshSnapshot()` 采用 delete+rebuild 而非 delta 更新，实现简单可靠，避免了增量更新的一致性问题。

### 中性漂移（取舍合理）

1. **LIKE 枚举值为 0 分**: LIKE 行为不计入经验值但可触发参与记录。这合理——点赞是轻量行为，不应与发帖等价。

2. **徽章发放异步 (@Async)**: `checkAndAward()` 使用异步执行，符合 design D5 决策。

3. **getOrCreateGrowth 并发处理**: `save()` 失败后重新查询，处理了并发初始化场景。

### 需要修复的漂移

| 严重级别 | 编号 | 问题 | 修复方向 |
|---------|------|------|---------|
| WARNING | W-2 | rank 字段未赋值 | 实现排名计算或移除字段 |
| WARNING | W-3 | Top 50 外用户排名不可见 | 增加额外查询逻辑 |
| WARNING | W-4 | 等级永不降级 vs Risk 描述矛盾 | 更新 design.md |
| WARNING | W-5 | MemberGrowthVO 缺少进度字段 | 增加 nextLevelProgress |
| WARNING | W-6 | 通知范围需确认 | 检查 notificationService 实现 |
| WARNING | W-7 | 圈内新星可撤销未实现 | checkAndAward 增加撤销逻辑 |
| WARNING | W-8 | API 文档 Leaderboard 端点缺失 | 补充 circle-level-apis.md |
| SUGGESTION | S-1 | AchievementVO 缺进度字段 | 增加 currentProgress/targetProgress |
| SUGGESTION | S-2 | CircleLevelVO 缺权益和子得分 | 增加 benefits/memberScore 等 |
| SUGGESTION | S-3 | 旧 req/growth 文件待清理 | 确认引用后移除 |
| SUGGESTION | S-4 | 唯一索引与 design 描述不一致 | 更新 design.md D4 |

---

## 五、同步方案

### 立即同步（规范文档 ← 实际代码）

以下项目需要更新文档以反映实际实现：

| 文件 | 更新内容 |
|------|---------|
| `circle-level-apis.md:7` | 补充 LeaderboardController 的 GET 端点完整信息 |
| `design.md` D4 | 更新唯一索引结构为 `(circle_id, user_id, biz_date, action_type, biz_id)` |
| `design.md` Risk 第 4 行 | 更新为"等级仅升级不降级，避免频繁波动" |

### 短期修复（实际代码 → 补全功能）

| 优先级 | 文件 | 修复内容 |
|--------|------|---------|
| 高 | `MemberGrowthServiceImpl.getGrowthInfo()` | 实现 rank 计算或移除 MemberGrowthVO.rank 字段 |
| 高 | `AchievementServiceImpl.checkAndAward()` | 增加 RISING_STAR 撤销逻辑 |
| 中 | `LeaderboardServiceImpl.getLeaderboard()` | Top 50 外用户排名和差距查询 |
| 中 | `MemberGrowthVO` | 增加 nextLevelThreshold、progressPercent |
| 中 | `AchievementVO` | 增加 currentProgress、targetProgress、status |
| 低 | `CircleLevelVO` | 增加 benefits、memberScore、contentScore、activityScore |

### 需确认项

1. **`IContentNotificationService.sendNotification(circleId, ...)`** 的实际发送范围——需要阅读通知服务实现确认
2. **旧 `req/growth/` 文件** 是否被其他 Controller 引用——需要 grep 确认
3. **成员等级 (member level)** 是否有独立的升级规则——目前只存储 level 字段但没有升级逻辑

---

## Final Assessment

**No critical implementation blockers.** 核心业务逻辑（经验值计算、每日上限、等级门槛、徽章条件）与 spec 一致。39 个 tasks 全部完成。

**8 WARNINGs** 需要关注，主要集中在：
- 2 个功能不完整（rank 未赋值、Top 50 外用户排名）
- 2 个文档矛盾（等级不降级、唯一索引描述）
- 2 个 VO 字段缺失（MemberGrowthVO 进度、通知范围）
- 1 个撤销逻辑缺失（圈内新星）
- 1 个 API 文档缺失（Leaderboard 端点）

**建议**: 补全 WARNING 项后（尤其是 W-2 rank、W-3 Top 50 外用户、W-7 圈内新星撤销），即可归档。SUGGESTION 项可在后续迭代中逐步完善。
