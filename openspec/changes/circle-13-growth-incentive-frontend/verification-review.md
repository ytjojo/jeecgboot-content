# circle-13-growth-incentive-frontend 验证审核报告

**审核日期**: 2026-06-04
**最后重审**: 2026-06-24（基于 main 分支 commit 84e8297d）
**审核范围**: design.md, proposal.md, specs/*.md 中的后端 API 引用与实际代码库一致性

---

## 🔄 2026-06-24 重审结论

> **原报告中的大部分问题已由后端代码迭代修复，但 API 路径发生新变更。**

| 项目 | 2026-06-04 状态 | 2026-06-24 状态 |
|------|---------------|---------------|
| 后端 API 路径 | 严重不匹配（4个RESTful路径错误） | ⚠️ 路径已修正到 `/user/growth/` 前缀后，**CircleLevelController 又迁移至 `/circle/growth/` 前缀**，其余 3 个仍在 `/user/growth/`，前缀不一致 |
| VO 字段定义 | 严重不匹配（4个VO缺字段） | ✅ **已全部修复** — 原报告列出的所有缺失字段后端已补充 |
| 文档完整性 | 基本完整 | 基本完整，但 design.md D9 降级策略现已过时（字段已存在，无需降级） |
| 前后端接口一致性 | 不通过 | ⚠️ 需更新前端文档中的圈子等级路径为 `/api/v1/content/circle/growth/level/info`，并更新 VO 字段映射 |

**关键变更**:
1. ✅ CircleLevelVO 已补充 `benefits(List<String>)`, `memberScore`, `contentScore`, `activityScore`, `nextLevelConditions(List<LevelConditionVO>)`
2. ✅ MemberGrowthVO 已补充 `levelName`, `nextLevelThreshold`, `progressPercent`, `todayExp`, `dailyExpLimit`, `recentBadges(List<AchievementVO>)`
3. ✅ AchievementVO 已补充 `iconUrl`, `earnedDate`, `currentProgress`, `targetProgress`, `status(EARNED/CLOSE/UNEARNED)`
4. ✅ LeaderboardEntryVO 已补充 `gap`, `username`, `avatar`
5. ⚠️ CircleLevelController 路径从 `/api/v1/content/user/growth/level/info` 改为 `/api/v1/content/circle/growth/level/info`

**注意字段名差异**（与原建议不同）:
- AchievementVO: `iconUrl`（非 `icon`）, `currentProgress/targetProgress`（非 `progress/targetValue`）, `achievementType`（无 `badgeId`）, `status` 枚举为 EARNED/CLOSE/UNEARNED（非 ACTIVE/REVOKED）
- CircleLevelVO.benefits: `List<String>`（非 `List<LevelBenefitVO>`，无 unlocked 状态）

---

## 一、验证结果摘要

| 项目 | 状态 | 说明 |
|------|------|------|
| 后端 API 路径 | ⚠️ 部分变更 | CircleLevelController 迁移至 `/circle/growth/`，其余保持 `/user/growth/` |
| VO 字段定义 | ✅ 已修复 | 所有原缺失字段后端已补充（字段名有少量差异，见上文） |
| 文档完整性 | 基本完整 | design.md、proposal.md、4 个 spec.md 结构规范 |
| 前后端接口一致性 | ⚠️ 需更新 | 圈子等级路径需更正为 `/circle/growth/level/info`，D9 降级策略需移除 |

---

## 二、后端 API 验证详情

### 2.1 API 路径对照表（2026-06-24 更新）

| # | 前端文档当前引用路径 | 后端实际路径（2026-06-24） | 状态 |
|---|-------------------|------------------------|------|
| 1 | `GET /api/v1/content/user/growth/level/info?circleId=` | `GET /api/v1/content/circle/growth/level/info?circleId=` | ⚠️ 需修正为 `/circle/growth/` |
| 2 | `GET /api/v1/content/user/growth/info?circleId=&userId=` | `GET /api/v1/content/user/growth/info?circleId=&userId=` | ✅ 一致 |
| 3 | `GET /api/v1/content/user/growth/achievement/list?circleId=&userId=` | `GET /api/v1/content/user/growth/achievement/list?circleId=&userId=` | ✅ 一致 |
| 4 | `GET /api/v1/content/user/growth/leaderboard?circleId=&dimension=&period=&currentUserId=` | `GET /api/v1/content/user/growth/leaderboard?circleId=&dimension=&period=&currentUserId=` | ✅ 一致 |

> 注: 前端文档中的路径已在 review-report.md（2026-06-06）中从旧 RESTful 风格修正为 `/user/growth/` 前缀，但尚未反映 CircleLevelController 的最新迁移。

### 2.2 后端 Controller 位置（2026-06-24 更新）

| 功能 | Controller 类 | 基础路径 |
|------|--------------|---------|
| 圈子等级 | `CircleLevelController` | `/api/v1/content/circle/growth/level` ⚠️ 已迁移 |
| 成员成长 | `MemberGrowthController` | `/api/v1/content/user/growth` |
| 成就徽章 | `AchievementController` | `/api/v1/content/user/growth/achievement` |
| 排行榜 | `LeaderboardController` | `/api/v1/content/user/growth/leaderboard` |

所有 Controller 均位于: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/growth/controller/`

**待确认**: CircleLevelController 迁移至 `/circle/growth/` 是否是前缀统一的第一步？其余 3 个 Controller 是否也会迁移？

### 2.3 补充发现的额外 API

后端还提供了以下前端文档未提及的接口:

| 接口 | 路径 | 归属 Controller | 说明 |
|------|------|----------------|------|
| 连续参与天数 | `GET /api/v1/content/user/growth/participation?circleId=&userId=` | MemberGrowthController | 返回 `Result<Integer>`，圈子成长相关 |
| 等级权益摘要 | `GET /api/v1/content/circle/growth/level/benefit?userId=` | CircleLevelController | 返回 `Result<ContentUserLevelBenefitSummaryVO>`，注意路径为 `/circle/growth/` 前缀 |
| 等级配置列表 | `GET /api/v1/content/circle/growth/level/config` | CircleLevelController | 返回 `Result<List<ContentUserLevelConfigVO>>`，注意路径为 `/circle/growth/` 前缀 |
| 成长汇总 | `GET /api/v1/content/user/growth/summary?userId=` | ContentUserGrowthController | 用户全局成长，非圈子 |
| 勋章分类目录 | `GET /api/v1/content/user/growth/badge/catalog?userId=` | ContentUserGrowthController | 用户全局成长 |
| 勋章详情 | `GET /api/v1/content/user/growth/badge/detail?userId=&badgeCode=` | ContentUserGrowthController | 用户全局成长 |
| 佩戴勋章 | `POST /api/v1/content/user/growth/badge/wear` | ContentUserGrowthController | 用户全局成长 |
| 查询佩戴勋章 | `GET /api/v1/content/user/growth/badge/worn?userId=` | ContentUserGrowthController | 用户全局成长 |

---

## 三、VO 字段验证详情（2026-06-24 更新）

### 3.1 CircleLevelVO (圈子等级) — ✅ 已修复

**后端实际字段（2026-06-24）**:
```java
private Integer level;
private String levelName;
private Integer growthScore;
private Integer nextLevelThreshold;
private Integer progressPercent;
private List<String> benefits;           // ✅ 新增
private Integer memberScore;             // ✅ 新增
private Integer contentScore;            // ✅ 新增
private Integer activityScore;           // ✅ 新增
private List<LevelConditionVO> nextLevelConditions;  // ✅ 新增
```

`LevelConditionVO` 结构: `type(String)`, `label(String)`, `current(Integer)`, `required(Integer)`, `gap(Integer)`

**注意**: `benefits` 为 `List<String>`（权益名称列表），不是 `List<LevelBenefitVO>`，无 unlocked/locked 状态区分。

### 3.2 MemberGrowthVO (成员成长) — ✅ 已修复

**后端实际字段（2026-06-24）**:
```java
private String circleId;
private Integer expPoints;
private Integer contributionPoints;
private Integer level;
private String levelName;                // ✅ 新增
private Integer postCount;
private Integer participationDays;
private Integer rank;
private Integer nextLevelThreshold;      // ✅ 新增
private Integer progressPercent;         // ✅ 新增
private Integer todayExp;                // ✅ 新增
private Integer dailyExpLimit;           // ✅ 新增
private List<AchievementVO> recentBadges; // ✅ 新增
```

### 3.3 AchievementVO (成就徽章) — ✅ 已修复（字段名有差异）

**后端实际字段（2026-06-24）**:
```java
private String achievementType;
private String name;
private String description;
private String iconUrl;                  // ✅ 新增（原建议名 icon）
private Boolean earned;
private Date earnedDate;                 // ✅ 新增
private String conditionDesc;
private Integer currentProgress;         // ✅ 新增（原建议名 progress）
private Integer targetProgress;          // ✅ 新增（原建议名 targetValue）
private String status;                   // ✅ 新增（枚举: EARNED/CLOSE/UNEARNED）
```

**字段名差异**:
- 无 `badgeId`，使用 `achievementType` 作为唯一标识
- `iconUrl` 而非 `icon`
- `currentProgress/targetProgress` 而非 `progress/targetValue`
- `status` 枚举为 `EARNED/CLOSE/UNEARNED`（已获得/即将达成/未获得），非 `ACTIVE/REVOKED`

### 3.4 LeaderboardEntryVO (排行榜条目) — ✅ 已修复

**后端实际字段（2026-06-24）**:
```java
private String userId;
private Integer score;
private Integer rankNum;
private Boolean highlighted;
private Integer gap;                     // ✅ 新增（与上一名得分差值）
private String username;                 // ✅ 新增
private String avatar;                   // ✅ 新增
```

---

## 四、前端文档问题列表（2026-06-24 更新状态）

### 4.1 design.md 问题

| 编号 | 问题 | 严重度 | 2026-06-24 状态 |
|------|------|--------|---------------|
| D1 | API 路径使用 RESTful 风格，与后端不一致 | 高 | ✅ 已在 review-report 中修复路径；⚠️ 需更新圈子等级路径为 `/circle/growth/level/info` |
| D2 | 未提及后端已有 `participation` 接口 | 中 | ⚠️ 待更新 |
| D3 | 未区分「圈子成长」和「用户全局成长」两套体系 | 中 | ⚠️ 待更新 |
| D9 | 缺失字段降级策略 — 现大部分字段已存在，降级策略需移除/更新 | 高 | 🔴 **需修正**: D9 中 todayExp/dailyExpLimit/recentBadges/badgeIcon/earnedDate/progress/username/avatar/benefits/nextLevelConditions 的降级说明均已过时 |

### 4.2 proposal.md 问题

| 编号 | 问题 | 严重度 | 2026-06-24 状态 |
|------|------|--------|---------------|
| P1 | API 依赖描述路径与实际不符 | 高 | ✅ 已在 review-report 中修复 |
| P2 | 术语 Badge vs Achievement 不一致 | 中 | ✅ 已在 review-report 中修复（D7 术语映射） |

### 4.3 specs/circle-level/spec.md 问题

| 编号 | 问题 | 严重度 | 2026-06-24 状态 |
|------|------|--------|---------------|
| S1 | API 路径错误 | 高 | ✅ 已修复；⚠️ 需更新为 `/circle/growth/level/info` |
| S2 | 期望 nextLevelConditions/benefits，后端 VO 缺失 | 高 | ✅ 后端已补充字段 |
| S3 | 分项指标展开无数据支撑 | 中 | ✅ 后端已提供 nextLevelConditions，可支持展开 |

### 4.4 specs/member-growth/spec.md 问题

| 编号 | 问题 | 严重度 | 2026-06-24 状态 |
|------|------|--------|---------------|
| S4 | API 路径错误 | 高 | ✅ 已修复 |
| S5 | 期望 dailyExpLimit/todayExp，后端 VO 缺失 | 高 | ✅ 后端已补充字段 |
| S6 | 期望 recentBadges，后端 VO 缺失 | 中 | ✅ 后端已补充字段 |
| S7 | participationDays 文档描述需前端计算 | 低 | ✅ 后端已提供字段 |

### 4.5 specs/badge-system/spec.md 问题

| 编号 | 问题 | 严重度 | 2026-06-24 状态 |
|------|------|--------|---------------|
| S8 | API 路径错误 | 高 | ✅ 已修复 |
| S9 | 期望 icon/earnedDate/progress/targetValue/status，后端 VO 缺失 | 高 | ✅ 后端已补充字段（注意字段名差异: iconUrl/currentProgress/targetProgress, status 枚举为 EARNED/CLOSE/UNEARNED） |
| S10 | Badge vs Achievement 术语不一致 | 中 | ✅ 已在 review-report 中修复 |

### 4.6 specs/leaderboard/spec.md 问题

| 编号 | 问题 | 严重度 | 2026-06-24 状态 |
|------|------|--------|---------------|
| S11 | API 路径错误 | 高 | ✅ 已修复 |
| S12 | 期望 username/avatar，后端 VO 缺失 | 高 | ✅ 后端已补充字段；额外补充了 gap 字段 |
| S13 | 缺少 currentUserId 参数说明 | 中 | ✅ 已在 review-report 中修复 |

---

## 五、建议修复方案（2026-06-24 更新）

### 5.1 需立即修正（路径变更）

| 文档 | 当前路径 | 修正后路径 |
|------|--------|----------|
| tasks.md 1.1 | `/api/v1/content/user/growth/level/info` | `/api/v1/content/circle/growth/level/info` |
| design.md D9 字段映射表 | 旧路径 | 更新为 `/circle/growth/level/info` |

### 5.2 需修正（D9 降级策略过时）

design.md D9 中以下降级策略已过时，应改为直接使用后端字段：
- `benefits` → 后端已提供 `List<String> benefits`，可展示已解锁权益列表
- `nextLevelConditions` → 后端已提供 `List<LevelConditionVO>`，可支持分项进度展开
- `todayExp`/`dailyExpLimit` → 后端已提供，可直接展示今日经验条
- `recentBadges` → MemberGrowthVO 已包含（最多3枚），无需额外调接口
- `iconUrl` → AchievementVO 已提供 `iconUrl`，无需本地图标映射兜底（仍可保留本地兜底作为降级）
- `earnedDate` → 后端已提供，可展示获得时间
- `currentProgress/targetProgress` → 后端已提供数值，可展示进度条
- `status=CLOSE` → 可直接判断"即将达成"状态，无需解析 conditionDesc
- `username`/`avatar` → 后端已批量返回，无需额外调用户接口
- `gap` → 后端已提供，可展示距上一名差距
- `memberScore/contentScore/activityScore` → 后端已提供分项得分
- `levelName/nextLevelThreshold/progressPercent` → 后端已提供

> 仍可保留本地图标映射作为 `iconUrl` 加载失败时的兜底，但不应作为默认方案。

### 5.3 仍待确认

1. CircleLevelController 迁移至 `/circle/growth/` 后，其余 3 个 Controller 是否会统一迁移？
2. `benefits` 为 `List<String>` 无 unlocked 状态，如果需要展示"未解锁权益"，需后端改 VO 结构。
3. WebSocket 通知机制仍未实现。

---

## 六、后端遗留代码清单

详见 `backend-issues.md`（2026-06-24 已更新）。
