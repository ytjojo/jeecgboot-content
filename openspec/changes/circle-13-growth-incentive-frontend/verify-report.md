## Verification Report: circle-13-growth-incentive-frontend

**首次验证日期**: 2026-06-18
**最后重审**: 2026-06-24（基于 main 分支 commit 84e8297d）
**验证范围**: 完整性(Completeness) · 正确性(Correctness) · 前后端接口对齐

---

### 🔴 2026-06-24 重大勘误

> **本报告（2026-06-18 版本）中存在多处严重错误结论，已在 2026-06-24 重审中纠正。**

原报告错误地声称：
1. ❌ "VO 字段与 spec 设计完全一致" — **实际是 spec 只描述了基础字段，后端 VO 已扩展了大量字段（benefits/nextLevelConditions/todayExp/dailyExpLimit/recentBadges/iconUrl/earnedDate/currentProgress/targetProgress/status/username/avatar/gap 等），spec 和 design.md D9 降级策略严重过时**
2. ❌ "design.md 的 D9 降级策略经过后端代码验证，全部正确" — **实际 D9 中 todayExp/dailyExpLimit/recentBadges/badgeIcon/earnedDate/progress/username/avatar/benefits/nextLevelConditions 等降级项后端都已提供字段，降级策略不再需要**
3. ❌ "4 个核心 API 路径一致" — **CircleLevelController 已迁移至 `/api/v1/content/circle/growth/level/info`，不再是 `/api/v1/content/user/growth/level/info`**
4. ❌ "CircleAchievement.iconUrl 未暴露到 VO" — **AchievementVO 已有 `iconUrl` 字段**
5. ❌ 前端文件清单建议 `src/api/content/circleGrowth.ts`（单文件）— **已存在 `src/api/content/growth/` 目录，应在此目录下新增 `circle.ts`**

以下为修正后的完整报告。

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

### 摘要（2026-06-24 修正）

| 维度 | 状态 |
|------|------|
| Completeness | **0/41 tasks 完成** — 圈子成长前端尚未开始实现 |
| Correctness | ⚠️ **4 个核心后端 API 已就绪**，但 CircleLevelController 路径已变更，后端 VO 已扩展大量字段（spec/design.md D9 需更新） |
| 前后端接口对齐 | ⚠️ **圈子等级路径需更新为 `/circle/growth/level/info`**，其余 3 个接口路径一致；VO 字段后端已远超 spec 描述 |
| Coherence | 🔴 **design.md D9 降级策略严重过时**，多个"暂不支持"的字段后端已提供 |

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

**前端 API 层** (`src/api/content/growth/` — 用户成长 API 封装, **已存在目录**):
- `index.ts` + `types.ts` — 成长汇总、等级配置、等级权益、衰减规则
- `badge.ts` + `badge-types.ts` — 全局徽章（catalog, detail, worn, wear）
- `point.ts` + `point-types.ts` — 积分（ledger, exchange, gift）
- ⚠️ **注意**: 应在现有 `src/api/content/growth/` 目录下**新增 `circle.ts`**，而不是创建 `src/api/content/circleGrowth.ts`（同名文件和目录不能共存）

**前端 Store** (`src/store/modules/growth.ts` — 用户成长状态):
- 状态字段: summary, levelConfigs, levelBenefit, decayRule, decayStatus
- 有升级事件广播模式 (`growthEmitter`) — 可复用此模式到 circleGrowth Store

**前端组件** (`src/components/content/` — 用户成长组件):
- BadgeCard, BadgeDetail, BadgeDisplay, BadgeGrid, GrowthProgress, LevelCard, LevelBenefitList, DecayWarning, ExchangeConfirm, GiftSendModal, LevelUpCongratsModal

**前端页面** (`src/views/content/growth/` — 用户成长页面):
- my-level, my-badges, badge-manage, point-detail, point-mall

---

## 2. 前后端接口对齐检查（2026-06-24 修正）

### 2.1 核心接口：Spec 设计 vs 后端实际

#### 圈子等级信息
| 项目 | Spec 设计 | 后端实际（2026-06-24） | 对齐 |
|------|---------|---------|------|
| API 路径 | `GET /api/v1/content/user/growth/level/info` | `GET /api/v1/content/circle/growth/level/info` | ❌ **路径已变更** |
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
| 参数 | `?circleId=&dimension=&period=&currentUserId=` | 同，`period` 默认 `WEEK`，枚举值 WEEK/MONTH/ALL | ✅ |
| 返回 | `Result<List<LeaderboardEntryVO>>` | 同 | ✅ |
| Controller | — | `LeaderboardController.java` | ✅ |

### 2.2 补充接口

| API 路径 | 后端 Controller | spec 提及 | 对接建议 |
|---------|----------------|----------|---------|
| `GET /.../participation?circleId=&userId=` | `MemberGrowthController` | ✅ 设计文档提及 | 返回 `Result<Integer>`，可选对接；MemberGrowthVO 已含 `participationDays` |
| `GET /.../circle/growth/level/benefit?userId=` | `CircleLevelController` | ❌ 未提及 | 等级权益摘要，路径为 `/circle/growth/` 前缀 |
| `GET /.../circle/growth/level/config` | `CircleLevelController` | ❌ 未提及 | 等级配置列表，路径为 `/circle/growth/` 前缀 |

### 2.3 接口检查结论（2026-06-24 修正）

> ⚠️ **3 个核心接口路径一致，1 个（圈子等级）路径已迁移至 `/circle/growth/` 前缀。** 返回类型一致。但需注意：后端 VO 字段比 spec 描述的丰富得多（见第 3 节），spec 和 design.md 中基于"字段缺失"的降级策略需重写。

---

## 3. 后端 VO 字段验证（2026-06-24 重大修正）

### 3.1 CircleLevelVO — ⚠️ 后端字段远超 spec 描述

| Spec 预期字段 | 后端实际字段 | Java 类型 | 匹配 |
|-------------|------------|---------|------|
| `level` | `level` | Integer | ✅ |
| `levelName` | `levelName` | String | ✅ |
| `growthScore` | `growthScore` | Integer | ✅ |
| `nextLevelThreshold` | `nextLevelThreshold` | Integer | ✅ |
| `progressPercent` | `progressPercent` | Integer | ✅ |
| *(spec D9 声称缺失)* | **`benefits`** | **List\<String\>** | ✅ **后端已提供**（权益名称列表） |
| *(spec D9 声称缺失)* | **`memberScore`** | **Integer** | ✅ **后端已提供**（成员规模得分） |
| *(spec D9 声称缺失)* | **`contentScore`** | **Integer** | ✅ **后端已提供**（内容贡献得分） |
| *(spec D9 声称缺失)* | **`activityScore`** | **Integer** | ✅ **后端已提供**（活跃互动得分） |
| *(spec D9 声称缺失)* | **`nextLevelConditions`** | **List\<LevelConditionVO\>** | ✅ **后端已提供**（含 type/label/current/required/gap） |

**LevelConditionVO 结构**: `type(String)`, `label(String)`, `current(Integer)`, `required(Integer)`, `gap(Integer)`

> **注意**: `benefits` 为 `List<String>`（权益名称列表），不是 `List<LevelBenefitVO>`，没有 unlocked/locked 状态区分。

### 3.2 MemberGrowthVO — ⚠️ 后端字段远超 spec 描述

| Spec 预期字段 | 后端实际字段 | Java 类型 | 匹配 |
|-------------|------------|---------|------|
| `circleId` | `circleId` | String | ✅ |
| `expPoints` | `expPoints` | Integer | ✅ |
| `contributionPoints` | `contributionPoints` | Integer | ✅ |
| `level` | `level` | Integer | ✅ |
| `postCount` | `postCount` | Integer | ✅ |
| `participationDays` | `participationDays` | Integer | ✅ |
| `rank` | `rank` | Integer | ✅ |
| *(spec D9 声称缺失)* | **`levelName`** | **String** | ✅ **后端已提供** |
| *(spec D9 声称缺失)* | **`nextLevelThreshold`** | **Integer** | ✅ **后端已提供** |
| *(spec D9 声称缺失)* | **`progressPercent`** | **Integer** | ✅ **后端已提供** |
| *(spec D9 声称 todayExp 暂不展示)* | **`todayExp`** | **Integer** | ✅ **后端已提供**（今日已获经验值） |
| *(spec D9 声称前端硬编码 100)* | **`dailyExpLimit`** | **Integer** | ✅ **后端已提供**（每日经验上限） |
| *(spec D9 声称需额外调接口)* | **`recentBadges`** | **List\<AchievementVO\>** | ✅ **后端已提供**（最多3枚） |

### 3.3 AchievementVO — ⚠️ 后端字段远超 spec 描述（注意字段名差异）

| Spec 预期字段 | 后端实际字段 | Java 类型 | 匹配 |
|-------------|------------|---------|------|
| `achievementType` | `achievementType` | String | ✅ |
| `name` | `name` | String | ✅ |
| `description` | `description` | String | ✅ |
| `earned` | `earned` | Boolean | ✅ |
| `conditionDesc` | `conditionDesc` | String | ✅ |
| *(spec D9 声称本地图标映射)* | **`iconUrl`** | **String** | ✅ **后端已提供**（非 `icon`/`badgeIcon`） |
| *(spec D9 声称暂不展示)* | **`earnedDate`** | **Date** | ✅ **后端已提供** |
| *(spec D9 声称 conditionDesc 替代)* | **`currentProgress`** | **Integer** | ✅ **后端已提供**（非 `progress`） |
| *(spec D9 声称 conditionDesc 替代)* | **`targetProgress`** | **Integer** | ✅ **后端已提供**（非 `targetValue`/`target`） |
| *(spec D9 声称需解析文本)* | **`status`** | **String** | ✅ **后端已提供**（枚举: EARNED/CLOSE/UNEARNED，非 ACTIVE/REVOKED） |

### 3.4 LeaderboardEntryVO — ⚠️ 后端字段远超 spec 描述

| Spec 预期字段 | 后端实际字段 | Java 类型 | 匹配 |
|-------------|------------|---------|------|
| `userId` | `userId` | String | ✅ |
| `score` | `score` | Integer | ✅ |
| `rankNum` | `rankNum` | Integer | ✅ |
| `highlighted` | `highlighted` | Boolean | ✅ |
| *(spec D9 声称额外调用户接口)* | **`username`** | **String** | ✅ **后端已提供** |
| *(spec D9 声称额外调用户接口)* | **`avatar`** | **String** | ✅ **后端已提供** |
| *(spec D9 声称暂不展示)* | **`gap`** | **Integer** | ✅ **后端已提供**（与上一名得分差值） |

### 3.5 结论（2026-06-24 修正）

> 🔴 **原报告结论"4个VO的字段与spec设计文档完全一致"是错误的。**
>
> 实际情况：spec 仅描述了后端最初版本的基础字段，后端已迭代扩展了大量字段（CircleLevelVO 新增 5 个字段，MemberGrowthVO 新增 6 个字段，AchievementVO 新增 5 个字段，LeaderboardEntryVO 新增 3 个字段）。design.md D9 降级策略基于"字段缺失"的假设，现已严重过时，必须重写。
>
> 字段名注意事项：
> - AchievementVO 用 `iconUrl`（非 `icon`/`badgeIcon`），`currentProgress/targetProgress`（非 `progress/targetValue`）
> - AchievementVO `status` 枚举: `EARNED`(已获得)/`CLOSE`(即将达成)/`UNEARNED`(未获得)
> - CircleLevelVO.benefits 为 `List<String>` 而非 `List<LevelBenefitVO>`

---

## 4. Spec 缺失字段与降级策略验证（2026-06-24 修正）

### 4.1 design.md D9 降级策略 — 🔴 大部分已过时

| 原 D9 声称缺失字段 | 原 D9 降级方案 | 后端验证（2026-06-24） | 状态 |
|---------|-----------|---------|------|
| `dailyExpLimit` | 前端硬编码 100 | ✅ MemberGrowthVO 已有 `dailyExpLimit` 字段 | 🔴 **过时** — 直接使用后端字段 |
| `todayExp` | 暂不展示 | ✅ MemberGrowthVO 已有 `todayExp` 字段 | 🔴 **过时** — 可直接展示今日经验条 |
| `recentBadges` | 单独调用 achievement/list | ✅ MemberGrowthVO 已有 `recentBadges`(最多3枚) | 🔴 **过时** — 从成长信息中取，徽章墙页仍需单独调全量接口 |
| `badgeIcon` | 本地图标映射 | ✅ AchievementVO 已有 `iconUrl` 字段 | ⚠️ **部分过时** — 可优先使用后端 iconUrl，本地映射作为加载失败兜底 |
| `earnedDate` | 暂不展示 | ✅ AchievementVO 已有 `earnedDate` 字段 | 🔴 **过时** — 可展示获得时间 |
| `progress/targetValue` | 用 conditionDesc 替代 | ✅ AchievementVO 已有 `currentProgress/targetProgress` 数值字段 | 🔴 **过时** — 可展示数值进度条 |
| "即将达成"判断 | 解析 conditionDesc 文本 | ✅ AchievementVO 已有 `status=CLOSE` 枚举值 | 🔴 **过时** — 直接判断 status 字段 |
| `username/userAvatar` | 通过 userId 额外调用用户接口 | ✅ LeaderboardEntryVO 已有 `username/avatar` 字段 | 🔴 **过时** — 直接使用后端返回值 |
| `gapToPrev` | 暂不展示 | ✅ LeaderboardEntryVO 已有 `gap` 字段 | 🔴 **过时** — 可展示距上一名差距 |
| `benefits` | 暂不展示权益列表 | ✅ CircleLevelVO 已有 `List<String> benefits` 字段 | 🔴 **过时** — 可展示已解锁权益名称列表（注意：无 unlocked 状态区分，为 List<String>） |
| `nextLevelConditions` | 不支持展开分项 | ✅ CircleLevelVO 已有 `nextLevelConditions`（含 LevelConditionVO） | 🔴 **过时** — 可支持分项进度展开 |
| `memberScore/contentScore/activityScore` | D9 未提及 | ✅ CircleLevelVO 已提供三项分项得分 | 🆕 **spec 未描述但后端已提供** |
| `levelName/nextLevelThreshold/progressPercent` (MemberGrowthVO) | D9 未提及 | ✅ MemberGrowthVO 已提供 | 🆕 **spec 未描述但后端已提供** |

### SUGGESTION #1（2026-06-24 已实现）: CircleAchievement.iconUrl 已暴露到 VO
> ~~后端 `CircleAchievement` entity 有 `iconUrl` 字段但 `AchievementVO` 未包含~~ → **✅ 已实现**: AchievementVO 已包含 `iconUrl` 字段，前端可直接使用。

---

## 5. 路径与架构分析（2026-06-24 修正）

### 5.1 后端 Controller 路径分布

注意：**CircleLevelController 已迁移至 `/circle/growth/` 前缀**（数据主体是圈子），其余 Controller 保持在 `/user/growth/` 下（数据主体是用户）。双前缀是有意设计，不会统一迁移。详见 `docs/agent-context/circle-growth-api-conventions.md`。

```
/api/v1/content/circle/growth/
└── /level/
    ├── /info              ← CircleLevelController (circle-13)  [GET] ⚠️ 已迁移
    ├── /benefit           ← CircleLevelController (circle-13)  [GET] 新增：等级权益摘要
    └── /config            ← CircleLevelController (circle-13)  [GET] 新增：等级配置列表

/api/v1/content/user/growth/
├── /info                  ← MemberGrowthController (circle-13)  [GET]
├── /participation          ← MemberGrowthController (circle-13)  [GET]
├── /achievement/list       ← AchievementController (circle-13)  [GET]
├── /leaderboard            ← LeaderboardController (circle-13)  [GET]
├── /summary                ← ContentUserGrowthController (用户成长) [GET]
├── /badge/*                ← ContentUserGrowthController (用户成长) [GET/POST]
├── /decay/*                ← ContentUserGrowthController (用户成长) [GET]
└── /point/*                ← ContentUserGrowthController (用户成长) [GET/POST]
```

> ⚠️ **双前缀设计**: CircleLevelController 使用 `/circle/growth/` 前缀（数据主体是圈子），其余 3 个成长接口使用 `/user/growth/` 前缀（数据主体是用户）。前端对接时需注意此差异。这是有意设计，不会统一迁移。规范见 `docs/agent-context/circle-growth-api-conventions.md`。

### 5.2 参考价值

用户成长体系的前端代码对 circle-13 实现有以下参考价值：

| 用户成长组件 | circle-13 对应组件 | 可参考内容 |
|-------------|-------------------|-----------|
| `LevelCard` | `CircleLevelBadge` | 等级标识展示模式 |
| `GrowthProgress` | `CircleLevelProgress` | 进度条展示模式（可参考分项展开） |
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

## 8. 实施路线建议（2026-06-24 修正）

### 8.1 推荐路径

按 spec tasks.md 的顺序从 §1 开始建设，但**需先修正 design.md D9 降级策略和 API 路径**：

```
§0 文档修正（D9重写、路径更新、字段映射表更新）→ §1 基础设施 → §2 圈子等级 → §3 个人成长 → §4 徽章 → §5 排行榜 → §7 响应式 → §8 测试
                                                                                                        ↑
                                                                                                   §6 通知(待确认方案)
```

### 8.2 前端新建文件清单（2026-06-24 修正）

```
□ src/api/content/growth/circle.ts         — 4 个核心 API + TypeScript 类型（在现有 growth/ 目录下新增，非 circleGrowth.ts）
□ src/store/modules/circleGrowth.ts        — 按 circleId 缓存、事件广播
□ src/components/circle/growth/
    □ CircleLevelBadge.vue                 — 圈子等级标识
    □ CircleLevelProgress.vue              — 等级进度条（支持分项展开，后端已提供 nextLevelConditions）
    □ GrowthOverviewCard.vue               — 成长概览卡片
    □ ParticipationStreak.vue              — 连续参与进度
    □ DailyExpBar.vue                      — 今日经验进度条（后端已提供 todayExp/dailyExpLimit）
    □ BadgeCard.vue                        — 徽章卡片（支持进度数值、CLOSE即将达成状态、iconUrl）
    □ BadgeWall.vue                        — 徽章墙
    □ BadgeDetailModal.vue                 — 徽章详情弹窗（支持 earnedDate 展示）
    □ LeaderboardTabs.vue                  — 排行榜维度/周期切换（参数: WEEK/MONTH/ALL, EXP/CONTRIBUTION/POST）
    □ LeaderboardList.vue                  — 排行榜列表（直接使用 username/avatar/gap）
□ src/views/circle/growth/index.vue        — 个人成长信息页
□ src/views/circle/badges/index.vue        — 徽章墙页
□ src/views/circle/leaderboard/index.vue   — 排行榜页
□ src/router/routes/modules/circle.ts      — 添加 3 个子路由
□ src/views/circle/Detail.vue             — 嵌入等级与成长区块
```

### 8.3 对接 checklist（2026-06-24 修正）

```
□ getCircleLevel(circleId)           → GET /api/v1/content/circle/growth/level/info?circleId=  ⚠️ 路径已变更
□ getLevelBenefit(userId)            → GET /api/v1/content/circle/growth/level/benefit?userId=  ⚠️ 新增接口，/circle/growth/ 前缀
□ getLevelConfigs()                  → GET /api/v1/content/circle/growth/level/config           ⚠️ 新增接口，/circle/growth/ 前缀
□ getMemberGrowth(circleId, userId)  → GET /api/v1/content/user/growth/info?circleId=&userId=
□ getParticipationDays(circleId, userId) → GET /api/v1/content/user/growth/participation?circleId=&userId= (可选)
□ getCircleBadges(circleId, userId)  → GET /api/v1/content/user/growth/achievement/list?circleId=&userId=
□ getLeaderboard(params)             → GET /api/v1/content/user/growth/leaderboard?circleId=&dimension=EXP|CONTRIBUTION|POST&period=WEEK|MONTH|ALL&currentUserId=
```

---

## 最终评估（2026-06-24 修正）

### 🔴 CRITICAL: 3 项（阻塞性）
1. **所有 41 个任务均未开始** — 圈子成长前端代码需从零建设
2. **WebSocket 通知机制未确认** — 影响 §6 的实现方案选型（推送 vs 轮询）
3. **🔴 design.md D9 降级策略和字段映射表严重过时** + **CircleLevelController 路径已变更** — 必须在实现前修正文档，否则会基于错误假设实现多余的降级逻辑和错误的 API 路径

### 🔵 SUGGESTION: 1 项（改进建议）
1. ~~**CircleAchievement.iconUrl 可暴露到 VO**~~ → ✅ 已实现
2. **growthEmitter 模式可复用** — 在新建 circleGrowth Store 时采用相同事件总线模式

### 结论

**3 个 CRITICAL issue。不可归档，必须先修正文档。**

当前状态：
- ✅ 4 个核心后端 API **全部就绪**，返回类型一致
- ⚠️ **1 个接口（圈子等级）路径已迁移**，需更新文档路径
- ✅ **后端 VO 字段已大幅扩展**，原"缺失字段"问题已全部修复（字段名有少量差异需注意）
- 🔴 **design.md D9 降级策略和字段映射表必须重写**，以反映后端实际字段
- ⚠️ **API 文件应在现有 `src/api/content/growth/` 目录下新增 `circle.ts`**，而非创建独立 `circleGrowth.ts` 文件
- ✅ 不存在后端 VO 缺失字段的问题——纯粹是前端文档未更新

**下一步**:
1. **立即修正**: 更新 design.md D9 降级策略、字段映射表、API 路径（圈子等级改为 `/circle/growth/level/info`）
2. 更新 tasks.md 中 API 文件路径为 `src/api/content/growth/circle.ts`
3. 更新 specs 中"暂不支持/暂不展示"的场景描述
4. 确认 WebSocket 方案（§6）
5. ~~确认其余 3 个 Controller 是否也会迁移至 `/circle/growth/` 前缀~~ **已确认：不迁移。双前缀设计，见 circle-growth-api-conventions.md**
6. 按 spec 任务顺序开始实现 §1 基础设施搭建
