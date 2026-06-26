# circle-growth-api-conventions.md 文档审核报告

**审核日期**: 2026-06-25
**审核对象**: [docs/agent-context/circle-growth-api-conventions.md](file:///Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/docs/agent-context/circle-growth-api-conventions.md)
**审核依据**:
- 实际数据库表结构（Flyway SQL）
- 实际后端 Controller/Service/VO 代码
- PRD 需求文档（后端 + 前端）
- change-prd-mapping.yaml

---

## 一、核心结论：存在三套成长体系，文档混淆了概念

**实际存在三套独立成长体系**，文档只描述了两套且混淆了关键概念：

| 体系 | 数据库表 | Controller前缀 | 特点 |
|------|---------|---------------|------|
| **全局内容社区用户成长** | `content_user_*` 系列表 | `/api/v1/content/user/growth/` | **无circleId**，整个内容社区通用的积分/等级/勋章/兑换体系，有衰减降级机制 |
| **圈子等级** | `circle_level` | `/api/v1/content/circle/growth/level/` | 只有circleId，圈子本身的等级（L1-L5新芽圈→标杆圈） |
| **圈子内成员成长** | `circle_member_growth` 等5张表 | `/api/v1/content/circle/member_growth/` | **有circleId+userId**，用户在某个圈子内的经验/贡献/徽章/排名 |

> ⚠️ **致命冲突**：全局用户成长 和 圈子内成员成长 使用**完全相同的路径前缀** `/api/v1/content/user/growth/`！

---

## 二、严重错误（阻断开发级）

### 错误1：CircleLevelController 存在编译错误和错误接口

[CircleLevelController.java](file:///Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/growth/controller/CircleLevelController.java) 代码有严重问题：

| 问题 | 详情 |
|------|------|
| **代码无法编译** | `/benefit` 和 `/config` 方法缺少 import 语句（`ContentUserLevelBenefitSummaryVO`、`ContentUserLevelConfigVO`、`ContentUserLevelConfig`、`@Parameter`、`@NotBlank`、`@Size`、`List`） |
| **依赖未注入** | 使用了 `levelBenefitService` 和 `levelConfigService` 但没有 `@Resource` 声明 |
| **接口归错Controller** | `/benefit?userId=` 和 `/config` 是**全局用户成长**的接口（已在 [ContentUserGrowthController.java](file:///Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/controller/ContentUserGrowthController.java#L266-L283) 中存在），不应该出现在CircleLevelController |
| **参数错误** | 圈子等级权益接口应该传 `circleId`，不是 `userId` |

**规范文档错误地记录了这两个不存在的接口**：
- ❌ `/circle/growth/level/benefit`（无circleId，参数是userId）
- ❌ `/circle/growth/level/config`（返回的是全局用户等级配置，不是圈子等级配置）

### 错误2：数据库表设计缺失，规范文档假设不存在的字段

根据 [V3.9.1_67__circle_growth_system.sql](file:///Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/resources/flyway/sql/mysql/V3.9.1_67__circle_growth_system.sql)，实际表结构与规范文档/VO存在差异：

| 项 | 规范文档/VO说有 | 实际数据库 |
|----|---------------|-----------|
| 圈子等级权益表 | `benefits` 字段有已解锁/未解锁状态 | ❌ `circle_level` 表**没有权益字段**，也没有 `circle_level_benefit_config` 之类的配置表 |
| 圈子等级门槛配置表 | L1(0)/L2(100)/L3(300)/L4(600)/L5(850) | ❌ 数据库中没有圈子等级配置表，门槛值是硬编码还是缺失？ |
| 连续参与详细字段 | `streakDetail: boolean[]` 近7天状态 | ❌ `circle_member_growth` 表没有连续打卡相关字段，只有 `participationDays`（整数天数） |
| 今日经验字段 | `todayExp`/`dailyExpLimit` | ❌ 数据库没有这两个字段，应该从 `circle_growth_log` 按日期聚合计算 |

### 错误3：排行榜接口返回结构与文档不符

- **规范文档说**：`GET /user/growth/leaderboard` 返回 `LeaderboardVO`（包含 `entries`、`currentUser`、`totalCount`）
- **实际代码**：[LeaderboardController.java](file:///Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/growth/controller/LeaderboardController.java#L22-L29) 返回 `Result<List<LeaderboardEntryVO>>`，**没有包装对象**，当前用户排名信息在列表项中通过 `highlighted`/`gap` 字段体现

### 错误4：排行榜枚举值是大写，文档未说明

[CircleLeaderboardSnapshot.java](file:///Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/growth/entity/CircleLeaderboardSnapshot.java#L28-L32) 中注释明确：
- 维度 `dimension`: `EXP` / `CONTRIBUTION` / `POST`（大写）
- 周期 `period`: `WEEK` / `MONTH` / `ALL`（大写，且默认值是 `WEEK`）

规范文档中写的是 `WEEK/MONTH/ALL, EXP/CONTRIBUTION/POST` 但没强调枚举值是**大写字符串**，前端PRD中写的是小写 `week/month/all, experience/contribution/postCount`，这会导致对接失败。

---

## 三、中等错误（功能错误级）

### 错误5：连续参与接口返回类型错误

- **规范文档说**：`/user/growth/participation` 返回连续参与进度（应该包含7天详情）
- **实际代码**：[MemberGrowthController.java](file:///Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/growth/controller/MemberGrowthController.java#L28-L34) 返回 `Result<Integer>`（只返回一个整数天数）

### 错误6：Controller包位置不一致

所有圈子成长相关Controller（包括 `CircleLevelController`）都放在：
```
org.jeecg.modules.content.user.growth.controller
```
但圈子等级从领域上应该属于 `circle.growth` 包，而不是 `user.growth` 包。这会误导开发者。

### 错误7：成员等级"不降级"描述与字段矛盾

规范文档第74行说成员等级"不降级（经验值可扣减，等级不变）"，但前端PRD第101行明确写了"经验值扣减导致低于当前等级门槛时，等级下降一级"。

**实际数据库**：`circle_member_growth.level` 字段只是一个整数字段，没有任何防降级约束或触发器，**降级逻辑在代码层决定**，目前没有看到实现。规范文档的结论没有依据。

---

## 四、轻度错误（文档准确性级）

### 错误8：VO字段名与前端PRD不一致

| 字段 | 后端VO（实际） | 前端PRD（typescript） |
|------|--------------|---------------------|
| 经验值 | `expPoints` | `experience` |
| 贡献值 | `contributionPoints` | `contribution` |
| 下一等级门槛 | `nextLevelThreshold` | `nextLevelExp` |
| 连续参与天数 | `participationDays` | `streakDays` |
| 7天参与详情 | ❌ 不存在 | `streakDetail: boolean[]` |
| 今日经验 | `todayExp`（VO有，DB无） | `todayExp` |
| 徽章状态 | `status: EARNED/CLOSE/UNEARNED` | `earned: boolean + nearComplete: boolean` |
| 排行榜得分 | `score` | `value` |
| 排行榜排名 | `rankNum` | `rank` |
| 排行榜用户名 | `username` | `userName` |
| 排行榜头像 | `avatar` | `userAvatar` |

### 错误9：CircleLevelVO 字段名与前端PRD不一致

| 字段 | 后端VO | 前端PRD |
|------|-------|---------|
| 下一等级门槛 | `nextLevelThreshold` | `nextLevelScore` |
| 权益列表 | `List<CircleBenefitVO> benefits`（带解锁状态） | `benefits: string[]`（仅名称列表）+ `nextBenefits` |

### 错误10：数据库表数量与范围不符

实际数据库创建了**6张表**：
1. `circle_level` - 圈子等级
2. `circle_member_growth` - 成员成长记录
3. `circle_growth_log` - 成长行为流水
4. `circle_achievement` - 徽章配置（初始化了4种徽章）
5. `circle_member_achievement` - 成员已获得徽章
6. `circle_leaderboard_snapshot` - 排行榜快照

规范文档完全没提 `circle_growth_log`（行为流水，支持撤销和每日限额）和 `circle_achievement`（徽章配置表）。

---

## 五、修正建议

### 建议1：修复CircleLevelController代码

立即删除 `/benefit` 和 `/config` 两个错误方法，只保留 `/info` 接口。这两个接口已在 [ContentUserGrowthController.java](file:///Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/controller/ContentUserGrowthController.java) 中正确实现（全局用户成长）。

### 建议2：明确区分三套体系的API路径

当前全局用户成长和圈子内成员成长路径前缀冲突，建议调整：

| 体系 | 建议路径前缀 |
|------|------------|
| 全局用户成长 | `/api/v1/content/user/growth/`（保持不变，前端已对接） |
| 圈子等级 | `/api/v1/content/circle/growth/level/`（保持不变） |
| 圈子内成员成长 | **改为** `/api/v1/content/circle/member_growth/` |

或者保持路径但在规范文档中明确：
- `/user/growth/level/*` → 全局用户等级（有userId）
- `/circle/growth/level/*` → 圈子等级（有circleId）
- `/circle/member_growth/info?circleId=&userId=` → 成员在圈内的成长信息

### 建议3：补全缺失的数据库表或修正代码/文档

需要决定：
1. **圈子等级权益**：是新建 `circle_level_benefit_config` 表，还是硬编码在代码里？
2. **圈子等级门槛配置**：是新建配置表，还是硬编码？
3. **连续参与7天详情**：是在 `circle_member_growth` 加字段，还是从 `circle_growth_log` 实时计算？
4. **今日经验限额**：从 `circle_growth_log` 按 `biz_date` 聚合查询，不需要加字段

### 建议4：统一枚举值和字段名

- 明确所有枚举值是**大写字符串**（`EXP`/`CONTRIBUTION`/`POST`、`WEEK`/`MONTH`/`ALL`）
- 前后端对齐字段命名（建议以后端VO为准，因为代码已存在）

### 建议5：重写规范文档结构

建议规范文档按"三套体系"重新组织，明确：
1. 全局用户成长体系（已有，文档中不需要详细描述，只标注路径）
2. 圈子等级体系（对应circle_level表）
3. 圈子内成员成长体系（对应5张circle_*表）
4. 明确标注哪些接口已实现、哪些是规划中、哪些字段是VO聚合计算得出的

---

## 六、正确的API清单（基于实际可运行代码）

### 圈子等级（已实现可运行）
| 方法 | 路径 | 参数 | 说明 |
|------|------|------|------|
| GET | `/api/v1/content/circle/growth/level/info` | `circleId` | ✅ 获取圈子等级信息（唯一正确的接口） |

### 成员成长（已实现可运行）
| 方法 | 路径 | 参数 | 说明 |
|------|------|------|------|
| GET | `/api/v1/content/circle/member_growth/info` | `circleId`, `userId` | ✅ 成员经验/贡献/等级/排名 |
| GET | `/api/v1/content/circle/member_growth/participation` | `circleId`, `userId` | ✅ 连续参与天数（仅返回Integer） |
| GET | `/api/v1/content/circle/growth/achievement/list` | `circleId`, `userId` | ✅ 徽章列表 |
| GET | `/api/v1/content/circle/growth/leaderboard` | `circleId`, `dimension`, `period=WEEK`, `currentUserId` | ✅ 排行榜（返回List，无包装对象） |

### ❌ 不应在圈子成长中出现的接口（属于全局用户成长）
- `/api/v1/content/user/growth/level/benefit?userId=` → 全局用户等级权益
- `/api/v1/content/user/growth/level/config` → 全局用户等级配置
- 以及 `/summary`、`/badge/*`、`/point/*`、`/decay/*` 等所有全局用户成长接口

---

## 七、数据库表结构汇总（实际已创建）

| 表名 | 说明 | 关键字段 |
|------|------|---------|
| `circle_level` | 圈子等级 | circleId(UK), level(1-5), growthScore, memberScore, contentScore, activityScore |
| `circle_member_growth` | 成员成长记录 | circleId+userId(UK), expPoints, contributionPoints, level, postCount, commentCount, featuredCount |
| `circle_growth_log` | 成长行为流水 | circleId+userId+bizDate+actionType+bizId(UK), expPoints, contributionPoints, revoked |
| `circle_achievement` | 徽章配置 | achievementType(UK), name, description, iconUrl, conditionDesc（已初始化4种徽章） |
| `circle_member_achievement` | 成员已获得徽章 | circleId+userId+achievementType(UK), revoked |
| `circle_leaderboard_snapshot` | 排行榜快照 | circleId+dimension+period+userId(UK), score, rankNum, snapshotTime |

**初始化徽章数据**：
- CONTINUOUS_CREATOR: 持续创作者（累计10篇可见内容）
- QUALITY_CONTRIBUTOR: 优质贡献者（累计5篇精华）
- ACTIVE_PARTICIPANT: 活跃参与者（近7天3天有效参与）
- RISING_STAR: 圈内新星（近7天经验增长前10）
