## Verification Report: circle-13-growth-incentive-frontend

**验证日期**: 2026-06-18
**验证范围**: 完整性(Completeness) · 正确性(Correctness) · 前后端接口对齐

---

### 背景：两套独立的成长体系

项目中有两套独立的成长体系，各自有独立的后端 Controller 和前端代码：

| 维度 | **圈子成长体系（circle-13 目标）** | **用户成长体系（已实现，独立功能）** |
|------|----------------------------------|-------------------------------------|
| **性质** | 圈内成长激励（等级、排行榜、徽章） | 全局用户成长（积分、等级、徽章、兑换） |
| **作用域** | 按圈子（需 `circleId`） | 按用户（需 `userId`） |
| **后端位置** | `content.user.growth.controller.*` | `content.user.controller.ContentUserGrowthController` |
| **前端实现** | ❌ **尚未开始** | ✅ 已实现（API层、Store、组件、页面） |

> **关键结论**: 圈子成长体系前端代码尚未实现，不是实现了错误的版本。用户成长体系是独立的合法功能，与 circle-13 无关。

---

### 摘要

| 维度 | 状态 |
|------|------|
| Completeness | **0/41 tasks 完成** — 圈子成长前端尚未开始实现 |
| Correctness | **4 个核心后端 API 已就绪**，VO 字段与 spec 设计完全一致 |
| 前后端接口对齐 | **接口路径、参数、VO 字段均对齐**，无偏差 |
| Coherence | **design.md 决策仍然有效**，降级策略正确 |

---

## 1. 完整性检查 (Completeness)

### 1.1 任务完成状态

全部 **41 个任务未开始**（0/41）：

| 阶段 | 任务数 | 完成 | 说明 |
|------|--------|------|------|
| §1 基础设施搭建 | 3 | 0 | API 封装、Store、目录结构均需新建 |
| §2 圈子等级展示组件 | 4 | 0 | 4 个组件/集成点待实现 |
| §3 成员个人成长信息页 | 6 | 0 | 页面 + 3 个子组件 + 路由 |
| §4 徽章系统 | 6 | 0 | 3 个组件 + 页面 + 路由 |
| §5 排行榜 | 5 | 0 | 2 个组件 + 页面 + 路由 |
| §6 通知与实时刷新 | 3 | 0 | WebSocket 对接待确认 |
| §7 响应式适配与交互细节 | 7 | 0 | 骨架屏、空状态、错误处理等 |
| §8 测试验证 | 7 | 0 | 功能/响应式/性能测试 |

### CRITICAL #1: 所有 41 个任务均未开始
> 圈子成长体系前端的全部代码均需新建。这不是修复现有代码的问题，而是从零建设。

### 1.2 现有相关代码（非 circle-13 scope）

以下代码属于**用户成长体系**，是独立功能，**不能**算作 circle-13 任务完成，但实现时可参考其模式：

**前端 API 层** (`src/api/content/growth/` — 用户成长 API 封装):
- `index.ts` + `types.ts` — 成长汇总、等级配置、等级权益、衰减规则
- `badge.ts` + `badge-types.ts` — 全局徽章（catalog, detail, worn, wear）
- `point.ts` + `point-types.ts` — 积分（ledger, exchange, gift）

**前端 Store** (`src/store/modules/growth.ts` — 用户成长状态):
- 状态字段: summary, levelConfigs, levelBenefit, decayRule, decayStatus
- 有升级事件广播模式 (`growthEmitter`) — 可复用此模式到 circleGrowth Store

**前端组件** (`src/components/content/` — 用户成长组件):
- BadgeCard, BadgeDetail, BadgeDisplay, BadgeGrid, GrowthProgress, LevelCard, LevelBenefitList, DecayWarning, ExchangeConfirm, GiftSendModal, LevelUpCongratsModal

**前端页面** (`src/views/content/growth/` — 用户成长页面):
- my-level, my-badges, badge-manage, point-detail, point-mall

---

## 2. 前后端接口对齐检查

### 2.1 核心接口：Spec 设计 vs 后端实际

#### 圈子等级信息
| 项目 | Spec 设计 | 后端实际 | 对齐 |
|------|---------|---------|------|
| API 路径 | `GET /api/v1/content/user/growth/level/info` | 同 | ✅ |
| 参数 | `?circleId={circleId}` | `?circleId` | ✅ |
| 返回 | `Result<CircleLevelVO>` | `Result<CircleLevelVO>` | ✅ |
| Controller | — | `CircleLevelController.java` | ✅ |

#### 成员成长信息
| 项目 | Spec 设计 | 后端实际 | 对齐 |
|------|---------|---------|------|
| API 路径 | `GET /api/v1/content/user/growth/info` | 同 | ✅ |
| 参数 | `?circleId={circleId}&userId={userId}` | `?circleId&userId` | ✅ |
| 返回 | `Result<MemberGrowthVO>` | `Result<MemberGrowthVO>` | ✅ |
| Controller | — | `MemberGrowthController.java` | ✅ |

#### 成就徽章列表
| 项目 | Spec 设计 | 后端实际 | 对齐 |
|------|---------|---------|------|
| API 路径 | `GET /api/v1/content/user/growth/achievement/list` | 同 | ✅ |
| 参数 | `?circleId={circleId}&userId={userId}` | `?circleId&userId` | ✅ |
| 返回 | `Result<List<AchievementVO>>` | `Result<List<AchievementVO>>` | ✅ |
| Controller | — | `AchievementController.java` | ✅ |

#### 排行榜
| 项目 | Spec 设计 | 后端实际 | 对齐 |
|------|---------|---------|------|
| API 路径 | `GET /api/v1/content/user/growth/leaderboard` | 同 | ✅ |
| 参数 | `?circleId=&dimension=&period=&currentUserId=` | 同，`period` 默认 `WEEK` | ✅ |
| 返回 | `Result<List<LeaderboardEntryVO>>` | 同 | ✅ |
| Controller | — | `LeaderboardController.java` | ✅ |

### 2.2 补充接口

| API 路径 | 后端 Controller | spec 提及 | 对接建议 |
|---------|----------------|----------|---------|
| `GET /.../participation?circleId=&userId=` | `MemberGrowthController` | ✅ 设计文档提及 | 可选对接，MemberGrowthVO 已含 `participationDays` |
| `GET /.../level/config` | `ContentUserGrowthController` | proposal 提及 | 可选对接（全局等级配置，无参数） |

### 2.3 接口检查结论

> **4 个核心接口全部对齐**：路径一致，参数一致，返回类型一致。可直接对接，无接口偏差。

---

## 3. 后端 VO 字段验证

### 3.1 CircleLevelVO — ✅ 完全一致

| Spec 预期字段 | 后端实际字段 | Java 类型 | 匹配 |
|-------------|------------|---------|------|
| `level` (currentLevel) | `level` | Integer | ✅ |
| `levelName` | `levelName` | String | ✅ |
| `growthScore` | `growthScore` | Integer | ✅ |
| `nextLevelThreshold` (nextLevelScore) | `nextLevelThreshold` | Integer | ✅ |
| `progressPercent` | `progressPercent` | Integer | ✅ |

### 3.2 MemberGrowthVO — ✅ 完全一致

| Spec 预期字段 | 后端实际字段 | Java 类型 | 匹配 |
|-------------|------------|---------|------|
| `circleId` | `circleId` | String | ✅ |
| `expPoints` (experience) | `expPoints` | Integer | ✅ |
| `contributionPoints` (contribution) | `contributionPoints` | Integer | ✅ |
| `level` (currentLevel) | `level` | Integer | ✅ |
| `postCount` | `postCount` | Integer | ✅ |
| `participationDays` (streakDays) | `participationDays` | Integer | ✅ |
| `rank` | `rank` | Integer | ✅ |

### 3.3 AchievementVO — ✅ 完全一致

| Spec 预期字段 | 后端实际字段 | Java 类型 | 匹配 |
|-------------|------------|---------|------|
| `achievementType` (badgeId) | `achievementType` | String | ✅ |
| `name` (badgeName) | `name` | String | ✅ |
| `description` | `description` | String | ✅ |
| `earned` | `earned` | Boolean | ✅ |
| `conditionDesc` | `conditionDesc` | String | ✅ |

### 3.4 LeaderboardEntryVO — ✅ 完全一致

| Spec 预期字段 | 后端实际字段 | Java 类型 | 匹配 |
|-------------|------------|---------|------|
| `userId` | `userId` | String | ✅ |
| `score` (value) | `score` | Integer | ✅ |
| `rankNum` (rank) | `rankNum` | Integer | ✅ |
| `highlighted` (isCurrentUser) | `highlighted` | Boolean | ✅ |

### 3.5 结论

> **4 个 VO 的字段与 spec 设计文档完全一致，无偏差。** Spec 正确预测了所有后端字段，design.md 中的 D9 降级策略仍然准确有效。

---

## 4. Spec 缺失字段与降级策略验证

### 4.1 design.md D9 降级策略准确性

| 缺失字段 | D9 降级方案 | 后端验证 | 状态 |
|---------|-----------|---------|------|
| `dailyExpLimit` | 前端硬编码 100 | `GrowthConstant.DAILY_EXP_CAP = 100` ✅ | 正确 |
| `todayExp` | 暂不展示 | MemberGrowthVO 确无此字段 | 有效 |
| `recentBadges` | 单独调用 achievement/list | AchievementController 可用 | 有效 |
| `badgeIcon` | 本地图标映射 | CircleAchievement entity 有 `iconUrl`，VO 不暴露 | 有效 |
| `earnedDate` | 暂不展示 | AchievementVO 确无此字段 | 有效 |
| `progress/targetValue` | 用 conditionDesc 替代 | 仅有 conditionDesc 文本 | 有效 |
| `username/userAvatar` | 通过 userId 调用用户接口 | LeaderboardEntryVO 仅有 userId | 有效 |
| `benefits` | 暂不展示权益列表 | CircleLevelVO 确无此字段 | 有效 |

### SUGGESTION #1: CircleAchievement.iconUrl 可暴露到 VO
> 后端 `CircleAchievement` entity 有 `iconUrl` 字段但 `AchievementVO` 未包含。如后端补充此字段到 VO，前端可直接使用后端图标 URL，避免本地图标映射维护成本。
> 文件: `jeecg-module-content/.../user/growth/entity/CircleAchievement.java:30` → `jeecg-module-content/.../user/growth/vo/AchievementVO.java`

---

## 5. 路径与架构分析

### 5.1 后端 Controller 路径分布

两套体系共享 `/api/v1/content/user/growth/` 前缀，但路径段不重叠：

```
/api/v1/content/user/growth/
├── /info                  ← MemberGrowthController (circle-13)  [GET]
├── /participation          ← MemberGrowthController (circle-13)  [GET]
├── /level/info             ← CircleLevelController (circle-13)  [GET]
├── /achievement/list       ← AchievementController (circle-13)  [GET]
├── /leaderboard            ← LeaderboardController (circle-13)  [GET]
├── /summary                ← ContentUserGrowthController (用户成长) [GET]
├── /badge/*                ← ContentUserGrowthController (用户成长) [GET/POST]
├── /level/config           ← ContentUserGrowthController (用户成长) [GET]
├── /level/benefit          ← ContentUserGrowthController (用户成长) [GET]
├── /decay/*                ← ContentUserGrowthController (用户成长) [GET]
└── /point/*                ← ContentUserGrowthController (用户成长) [GET/POST]
```

> **路径不重叠，无 Spring MVC 路由冲突。** 但开发时需注意 `/info` 在 circle 体系是「成员成长信息」，与用户体系的 `/summary` 语义不同。

### 5.2 参考价值

用户成长体系的前端代码对 circle-13 实现有以下参考价值：

| 用户成长组件 | circle-13 对应组件 | 可参考内容 |
|-------------|-------------------|-----------|
| `LevelCard` | `CircleLevelBadge` | 等级标识展示模式 |
| `GrowthProgress` | `CircleLevelProgress` | 进度条展示模式 |
| `BadgeCard` | `BadgeCard` (circle) | 徽章卡片三态展示模式（注：数据模型不同：`BadgeDetailVO` vs `AchievementVO`） |
| `LevelUpCongratsModal` | 通知 Toast | 升级通知展示模式 |
| growth Store `growthEmitter` | circleGrowth Store | 跨组件事件广播模式 |

### SUGGESTION #2: Store 的 growthEmitter 模式可复用
> `src/store/modules/growth.ts` 中的 `growthEmitter` (mitt 事件总线) 模式适合 circle-13 的通知刷新场景。新建 `circleGrowth.ts` 时可采用相同模式处理「等级提升 → Toast + 刷新数据」流程。

---

## 6. WebSocket 通知待确认

### 6.1 现状

Spec §6 依赖 WebSocket 通知实现等级提升和徽章获得的实时提示，但：

- 后端 `content.user.growth.controller.*` 包中 **未找到 WebSocket 推送端点**
- 后端 `ContentUserGrowthController` 有 `POST /record` 记录行为端点，但这是触发/写入接口，不是推送
- 项目是否有现有 WebSocket 基础设施（如 STOMP/WebSocket）需要确认

### 6.2 影响

| 任务 | 影响 |
|------|------|
| 6.1 接入 WebSocket 监听 | 需要先确认后端推送机制 |
| 6.2 Toast 提示 + 数据刷新 | 如无 WebSocket，需改为轮询 |
| 6.3 多页面集成监听 | 取决于 6.1 方案 |

### CRITICAL #2: WebSocket 通知机制未确认
> Spec §6 的 3 个任务依赖 WebSocket 推送。**建议**: 在实现 §1-§5 的同时确认后端 WebSocket 方案。如后端暂无推送，备选方案为 30 秒轮询 `GET /.../info` 检测数据变化。

---

## 7. 用户成长体系代码问题（独立发现，非 circle-13 scope）

在搜索过程中发现用户成长体系 (`ContentUserGrowthController`) 的前端 API 封装存在**参数缺失**问题，虽然是独立功能，但值得记录：

| 前端调用 | 后端接口 | 问题 |
|---------|---------|------|
| `getGrowthSummary()` — 无参数 | `GET /summary?userId=` — 需要 `userId` | 🔴 参数缺失 |
| `getBadgeCatalog()` — 无参数 | `GET /badge/catalog?userId=` — 需要 `userId` | 🔴 参数缺失 |
| `getBadgeDetail(badgeId)` | `GET /badge/detail?userId=&badgeCode=` — 参数名 `badgeCode` 不是 `badgeId` | 🔴 参数名不匹配 |
| `getLevelBenefit()` — 无参数 | `GET /level/benefit?userId=` — 需要 `userId` | 🔴 参数缺失 |
| `getDecayStatus()` — 无参数 | `GET /decay/status?userId=` — 需要 `userId` | 🔴 参数缺失 |
| `getFeatureUnlockStatus(featureCode)` | `GET /point/feature/unlock?userId=&featureCode=` — 缺少 `userId` | 🔴 参数缺失 |

> 这些问题在用户成长体系代码中，不在 circle-13 scope，但运行时会导致接口报错。建议在实施 circle-13 的同时修复。

---

## 8. 实施路线建议

### 8.1 推荐路径

按 spec tasks.md 的顺序从 §1 开始建设，后端 API 已全部就绪，可直接对接：

```
§1 基础设施 → §2 圈子等级 → §3 个人成长 → §4 徽章 → §5 排行榜 → §7 响应式 → §8 测试
                                                              ↑
                                                         §6 通知(待确认方案)
```

### 8.2 前端新建文件清单

```
□ src/api/content/circleGrowth.ts          — 4 个核心 API + TypeScript 类型
□ src/store/modules/circleGrowth.ts        — 按 circleId 缓存、事件广播
□ src/components/circle/growth/
    □ CircleLevelBadge.vue                 — 圈子等级标识
    □ CircleLevelProgress.vue              — 等级进度条
    □ GrowthOverviewCard.vue               — 成长概览卡片
    □ ParticipationStreak.vue              — 连续参与进度
    □ DailyExpBar.vue                      — 今日经验进度条
    □ BadgeCard.vue                        — 徽章卡片
    □ BadgeWall.vue                        — 徽章墙
    □ BadgeDetailModal.vue                 — 徽章详情弹窗
    □ LeaderboardTabs.vue                  — 排行榜维度/周期切换
    □ LeaderboardList.vue                  — 排行榜列表
□ src/views/circle/growth/index.vue        — 个人成长信息页
□ src/views/circle/badges/index.vue        — 徽章墙页
□ src/views/circle/leaderboard/index.vue   — 排行榜页
□ src/router/routes/modules/circle.ts      — 添加 3 个子路由
□ src/views/circle/Detail.vue             — 嵌入等级与成长区块
```

### 8.3 对接 checklist

```
□ getCircleLevel(circleId)           → GET /api/v1/content/user/growth/level/info?circleId=
□ getMemberGrowth(circleId, userId)  → GET /api/v1/content/user/growth/info?circleId=&userId=
□ getCircleBadges(circleId, userId)  → GET /api/v1/content/user/growth/achievement/list?circleId=&userId=
□ getLeaderboard(params)             → GET /api/v1/content/user/growth/leaderboard?...
```

---

## 最终评估

### 🔴 CRITICAL: 2 项（阻塞性）
1. **所有 41 个任务均未开始** — 圈子成长前端代码需从零建设
2. **WebSocket 通知机制未确认** — 影响 §6 的实现方案选型（推送 vs 轮询）

### 🔵 SUGGESTION: 2 项（改进建议）
1. **CircleAchievement.iconUrl 可暴露到 VO** — 省去前端本地图标映射
2. **growthEmitter 模式可复用** — 在新建 circleGrowth Store 时采用相同事件总线模式

### 结论

**2 个 CRITICAL issue。不可归档。**

但好消息是：
- ✅ 4 个核心后端 API **全部就绪**，接口路径、参数、返回类型与 spec 完全一致
- ✅ 4 个 VO 的字段与 design.md 完全对齐，无偏差
- ✅ design.md 的 D9 降级策略经过后端代码验证，全部正确
- ✅ 不存在前后端接口冲突或漂移——纯粹是前端尚未实现

**下一步**: 先确认 WebSocket 方案（§6），然后按 spec 任务顺序开始实现 §1 基础设施搭建。
