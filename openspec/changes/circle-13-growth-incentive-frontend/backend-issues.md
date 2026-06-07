# 后端遗留代码问题清单

**创建日期**: 2026-06-04
**关联 Change**: circle-13-growth-incentive-frontend

---

## 一、VO 字段缺失问题

前端 UI 设计需要的字段在后端 VO 中不存在，需要后端补充。

### 1.1 CircleLevelVO 缺失字段

**文件**: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/growth/vo/CircleLevelVO.java`

| 缺失字段 | 类型 | 用途 | 影响的前端组件 |
|----------|------|------|--------------|
| `nextLevelConditions` | `List<LevelConditionVO>` | 展示距下一等级还需补足的条件（成员数+X, 内容+Y, 互动+Z） | CircleLevelProgress |
| `benefits` | `List<LevelBenefitVO>` | 展示已解锁/未解锁权益列表 | 圈子详情页等级区块 |

**建议 LevelConditionVO 结构**:
```java
@Data
public class LevelConditionVO {
    private String type;      // MEMBER, CONTENT, INTERACTION
    private String label;     // "成员数", "内容数", "互动数"
    private Integer current;  // 当前值
    private Integer required; // 要求值
    private Integer gap;      // 差距值
}
```

**建议 LevelBenefitVO 结构**:
```java
@Data
public class LevelBenefitVO {
    private String name;        // 权益名称
    private String description; // 权益描述
    private Boolean unlocked;   // 是否已解锁
}
```

### 1.2 MemberGrowthVO 缺失字段

**文件**: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/growth/vo/MemberGrowthVO.java`

| 缺失字段 | 类型 | 用途 | 影响的前端组件 |
|----------|------|------|--------------|
| `dailyExpLimit` | `Integer` | 每日经验值上限（固定 100） | DailyExpBar |
| `todayExp` | `Integer` | 今日已获经验值 | DailyExpBar |
| `recentBadges` | `List<AchievementVO>` | 最近获得的 3 枚徽章摘要 | 个人成长页徽章摘要区 |

**说明**: `dailyExpLimit` 可由后端配置返回或前端硬编码；`todayExp` 需后端查询当日积分记录汇总；`recentBadges` 需后端按获得时间倒序取前 3 条。

### 1.3 AchievementVO 缺失字段

**文件**: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/growth/vo/AchievementVO.java`

| 缺失字段 | 类型 | 用途 | 影响的前端组件 |
|----------|------|------|--------------|
| `badgeId` | `String` | 徽章唯一标识 | BadgeCard, BadgeDetailModal |
| `icon` | `String` | 徽章图标 URL | BadgeCard, BadgeWall |
| `earnedDate` | `Date` | 获得时间 | BadgeDetailModal |
| `progress` | `Integer` | 当前进度值 | BadgeCard (未获得状态) |
| `targetValue` | `Integer` | 目标值 | BadgeCard (进度 7/10) |
| `status` | `String` | 状态: ACTIVE / REVOKED | BadgeWall (撤销状态展示) |

### 1.4 LeaderboardEntryVO 缺失字段

**文件**: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/growth/vo/LeaderboardEntryVO.java`

| 缺失字段 | 类型 | 用途 | 影响的前端组件 |
|----------|------|------|--------------|
| `username` | `String` | 用户名 | LeaderboardList |
| `avatar` | `String` | 用户头像 URL | LeaderboardList |

**说明**: 当前 VO 只有 `userId`，前端需要额外调用用户信息接口获取头像和用户名，或由后端在排行榜接口中直接返回。

---

## 二、接口能力缺失问题

### 2.1 圈子等级分项指标数据

**需求**: 前端需要展示「成员规模、内容贡献、活跃互动」三类分项指标数据，用于进度条展开详情。

**现状**: `CircleLevelController` 只返回汇总的成长分和进度百分比，无分项数据。

**建议**: 在 `CircleLevelVO` 中增加分项指标，或新增 `GET /api/v1/content/user/growth/level/detail?circleId=` 接口返回分项数据。

### 2.2 徽章进度查询

**需求**: 前端需要展示未获得徽章的当前进度（如「7/10」）。

**现状**: `AchievementVO` 只有 `earned` (Boolean) 和 `conditionDesc` (String)，无结构化的进度数据。

**建议**: 在 `AchievementVO` 中增加 `progress` 和 `targetValue` 字段，或后端在 `conditionDesc` 中返回结构化进度信息。

---

## 三、术语不一致问题

| 前端文档术语 | 后端代码术语 | 建议 |
|------------|------------|------|
| Badge (徽章) | Achievement (成就) | 统一为「成就徽章」，前端 API 封装层做术语映射 |
| BadgeVO | AchievementVO | 前端类型定义使用后端实际名称 |
| badgeId | achievementType | 前端适配后端字段名 |

---

## 四、优先级排序

| 优先级 | 问题 | 原因 |
|--------|------|------|
| P0 | AchievementVO 缺失 icon/earnedDate/progress/targetValue/status | 徽章墙核心功能无法实现 |
| P0 | LeaderboardEntryVO 缺失 username/avatar | 排行榜样列表无法展示用户信息 |
| P1 | CircleLevelVO 缺失 nextLevelConditions/benefits | 等级详情展示受限 |
| P1 | MemberGrowthVO 缺失 todayExp/dailyExpLimit | 每日经验进度条无法实现 |
| P2 | MemberGrowthVO 缺失 recentBadges | 徽章摘要区可降级为独立请求 |
| P2 | 圈子等级分项指标数据 | 进度条展开为增强功能 |
