# 后端遗留代码问题清单

**创建日期**: 2026-06-04
**最后审核**: 2026-06-24（基于 main 分支 commit 84e8297d 重新审核）
**最终修正**: 2026-06-25 — 文档腐化修正完成，所有原列出的 VO 字段缺失已由后端修复，benefits 类型确认为 `List<CircleBenefitVO>`（`{name, unlocked}`）。仅剩 P3（WebSocket）待后端实现。
**关联 Change**: circle-13-growth-incentive-frontend

---

## 2026-06-24 重审结论

**原 8 个 VO 字段缺失问题中，7 个已由后端修复，1 个部分修复。原 2 个接口能力缺失问题均已修复。**

新增发现：
1. `CircleLevelController` 路径已从 `/api/v1/content/user/growth/level/info` 迁移至 `/api/v1/content/circle/growth/level/info`。其余 3 个 Controller（MemberGrowthController、AchievementController、LeaderboardController）仍使用 `/api/v1/content/user/growth/` 前缀，符合 D7 设计决策——圈子成长与用户成长使用不同前缀。
2. `benefits` 字段实现为 `List<CircleBenefitVO>`（`{name, unlocked}`），已含 unlocked 状态区分。✅ 完全符合需求。
3. AchievementVO 字段名与建议有差异：`iconUrl` 而非 `icon`，`currentProgress/targetProgress` 而非 `progress/targetValue`，`status` 枚举为 `EARNED/CLOSE/UNEARNED` 而非 `ACTIVE/REVOKED`。

---

## 一、VO 字段缺失问题

### 1.1 CircleLevelVO 缺失字段 — ✅ 已修复（2026-06-24 确认）

**文件**: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/growth/vo/CircleLevelVO.java`

| 原缺失字段 | 建议类型 | 实际后端字段 | 状态 |
|----------|------|------|------|
| `nextLevelConditions` | `List<LevelConditionVO>` | `List<LevelConditionVO> nextLevelConditions` | ✅ 已实现 |
| `benefits` | `List<CircleBenefitVO>` | `List<CircleBenefitVO> benefits`（含 name, unlocked） | ✅ 已实现 |

**额外补充字段**（原文档未提及，后端已实现）:
- `memberScore: Integer` — 成员规模得分
- `contentScore: Integer` — 内容贡献得分
- `activityScore: Integer` — 活跃互动得分

**LevelConditionVO 实际结构**（与建议一致）:
```java
@Data
public class LevelConditionVO {
    private String type;      // MEMBER, CONTENT, INTERACTION
    private String label;     // "成员数", "内容数", "互动数"
    private Integer current;  // 当前值
    private Integer required; // 上限值
    private Integer gap;      // 差距值 (required - current)
}
```

**CircleBenefitVO 实际结构**（与建议一致）:
```java
@Data
@Accessors(chain = true)
public class CircleBenefitVO {
    private String name;     // 权益名称
    private Boolean unlocked; // 是否已解锁
}
```

`benefits` 为 `List<CircleBenefitVO>`，前端可直接区分"已解锁/未解锁"权益。

### 1.2 MemberGrowthVO 缺失字段 — ✅ 已修复（2026-06-24 确认）

**文件**: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/growth/vo/MemberGrowthVO.java`

| 原缺失字段 | 建议类型 | 实际后端字段 | 状态 |
|----------|------|------|------|
| `dailyExpLimit` | `Integer` | `Integer dailyExpLimit` | ✅ 已实现 |
| `todayExp` | `Integer` | `Integer todayExp` | ✅ 已实现 |
| `recentBadges` | `List<AchievementVO>` | `List<AchievementVO> recentBadges`（最多3枚） | ✅ 已实现 |

**额外补充字段**（原文档未提及，后端已实现）:
- `levelName: String` — 等级名称
- `nextLevelThreshold: Integer` — 下一等级门槛
- `progressPercent: Integer` — 等级进度百分比

### 1.3 AchievementVO 缺失字段 — ✅ 已修复（2026-06-24 确认，字段名有差异）

**文件**: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/growth/vo/AchievementVO.java`

| 原建议字段 | 建议类型 | 实际后端字段 | 状态 |
|----------|------|------|------|
| `badgeId` | `String` | 无（使用 `achievementType` 作为唯一标识） | ⚠️ 字段名不同 |
| `icon` | `String` | `String iconUrl`（徽章图标URL） | ⚠️ 字段名不同 |
| `earnedDate` | `Date` | `Date earnedDate` | ✅ 已实现 |
| `progress` | `Integer` | `Integer currentProgress` | ⚠️ 字段名不同 |
| `targetValue` | `Integer` | `Integer targetProgress` | ⚠️ 字段名不同 |
| `status` | `String` (ACTIVE/REVOKED) | `String status`（枚举值: EARNED/CLOSE/UNEARNED） | ⚠️ 枚举值不同 |

**字段映射说明**:
- 唯一标识用 `achievementType`，不需要 `badgeId`
- 图标字段为 `iconUrl`（不是 `icon`）
- 进度字段为 `currentProgress`/`targetProgress`（不是 `progress`/`targetValue`）
- 状态枚举: `EARNED`(已获得) / `CLOSE`(即将达成) / `UNEARNED`(未获得)，**没有** REVOKED(撤销) 状态；"即将达成"通过 `status=CLOSE` 判断，不需要前端解析 `conditionDesc`

### 1.4 LeaderboardEntryVO 缺失字段 — ✅ 已修复（2026-06-24 确认）

**文件**: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/growth/vo/LeaderboardEntryVO.java`

| 原缺失字段 | 建议类型 | 实际后端字段 | 状态 |
|----------|------|------|------|
| `username` | `String` | `String username` | ✅ 已实现 |
| `avatar` | `String` | `String avatar` | ✅ 已实现 |

**额外补充字段**（原文档未提及，后端已实现）:
- `gap: Integer` — 与上一名得分差值

---

## 二、接口能力缺失问题

### 2.1 圈子等级分项指标数据 — ✅ 已修复

`CircleLevelVO` 已包含 `memberScore/contentScore/activityScore` 分项得分和 `nextLevelConditions` 详细条件，进度条展开功能可直接支持。

### 2.2 徽章进度查询 — ✅ 已修复

`AchievementVO` 已包含 `currentProgress/targetProgress/status` 字段，可直接展示「7/10」进度和即将达成状态。

---

## 三、API 路径变更（2026-06-24 新发现）

| 接口 | 旧路径（review-report 中已修正的） | 最新路径（2026-06-25 重审确认） | 状态 |
|------|------|------|------|
| 圈子等级信息 | `/api/v1/content/user/growth/level/info?circleId=` | `/api/v1/content/circle/growth/level/info?circleId=` | ⚠️ 已变更 |
| 等级权益摘要 | — | `/api/v1/content/circle/growth/level/benefit?userId=` | 🆕 新增接口 |
| 等级配置列表 | — | `/api/v1/content/circle/growth/level/config` | 🆕 新增接口 |
| 成员成长 | `/api/v1/content/circle/member_growth/info?circleId=&userId=` | `/api/v1/content/circle/member_growth/info?circleId=&userId=` | ✅ 未变 |
| 连续参与 | `/api/v1/content/circle/member_growth/participation?circleId=&userId=` | `/api/v1/content/circle/member_growth/participation?circleId=&userId=` | ✅ 未变 |
| 成就徽章 | `/api/v1/content/circle/growth/achievement/list?circleId=&userId=` | `/api/v1/content/circle/growth/achievement/list?circleId=&userId=` | ✅ 未变 |
| 排行榜 | `/api/v1/content/circle/growth/leaderboard?circleId=&dimension=&period=&currentUserId=` | `/api/v1/content/circle/growth/leaderboard?circleId=&dimension=&period=&currentUserId=` | ✅ 未变 |

**已确认**: `CircleLevelController` 已迁移至 `/circle/growth/` 前缀（数据主体是圈子），其余 3 个 Controller（成员成长、成就徽章、排行榜）保持在 `/user/growth/` 前缀（数据主体是用户）。双前缀是有意设计，不会统一迁移。详见 `docs/agent-context/circle-growth-api-conventions.md`。

---

## 四、术语不一致问题

| 前端文档术语 | 后端代码术语 | 建议 | 状态 |
|------------|------------|------|------|
| Badge (徽章) | Achievement (成就) | 统一为「成就徽章」，前端 API 封装层做术语映射 | 仍需处理 |
| BadgeVO | AchievementVO | 前端类型定义使用后端实际名称 | 仍需处理 |
| badgeId | achievementType | 前端适配后端字段名 | 仍需处理 |

---

## 五、剩余问题清单（2026-06-24 更新后）

| 优先级 | 问题 | 原因 |
|--------|------|------|
| P1 | CircleLevelController 路径与其它 Controller 前缀不同（/circle/growth/ vs /user/growth/） | 已确认：双前缀是有意设计，数据主体不同。见 circle-growth-api-conventions.md |
| P2 | benefits 字段为 List<CircleBenefitVO>（{name, unlocked}） | ✅ 已完整实现，可直接区分已解锁/未解锁权益 |
| P3 | WebSocket 通知机制未实现 | §6 通知功能方案待确认 |
