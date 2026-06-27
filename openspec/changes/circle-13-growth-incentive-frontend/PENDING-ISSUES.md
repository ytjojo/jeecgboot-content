# 圈子成长激励 — 综合待完成清单

> **生成日期**: 2026-06-25
> **来源**: backend-issues.md + review-report.md + spec-review-report.md + verification-review.md + verify-report.md (前后端)
> **说明**: 所有 review/verification 报告中的已解决问题已标记，本文档仅列出**尚未解决**的问题。

---

## 一、已解决项目确认 ✅

以下问题已在 2026-06-25 commit `7370dc57` 中全部修正，**不再阻塞**：

| 类别 | 问题 | 修正内容 |
|------|------|---------|
| 文档腐化 | API 路径/prefix 错误 | 前端 PRD + design.md + 4 个 spec + 后端 design.md 全部修正为实际路径 |
| 文档腐化 | VO 字段名与后端不一致 | 前端 PRD 4 个 interface + design.md 字段映射表全部对齐 |
| 文档腐化 | D9"缺失字段降级"大面积错误 | 重写为"字段对接说明"，删除 20 处"暂不支持"声明 |
| 文档腐化 | Flyway 版本号 V3.9.1_63/66 | 统一修正为 V3.9.1_67 |
| 文档腐化 | tasks.md API 函数 4→7 缺失 | 补全 benefit/config/participation |
| 文档腐化 | spec 中不存在 `/badge/catalog` API 引用 | 删除，改为标注数据库仅初始化 4 种徽章 |
| 后端已实现 | VO 字段缺失（all） | CircleLevelVO/MemberGrowthVO/AchievementVO/LeaderboardEntryVO 所有原缺失字段后端已补充 |
| 后端已实现 | benefits 类型 | `List<CircleBenefitVO>`（`{name, unlocked}`），可区分已解锁/未解锁 |
| 已确认 | CircleLevelController 路径迁移 | `/api/v1/content/circle/growth/level/`（双前缀设计，其余保持 `/user/growth/`） |

---

## 二、待完成后端事项

### P1 — 数据库缺失

| 编号 | 问题 | 详情 | 影响 |
|------|------|------|------|
| DB-1 | 徽章初始化数据不完整 | PRD 定义 6 种徽章，`circle_achievement` 只 INSERT 了 4 条（ach_001~004）。缺少「内容里程碑」(CONTENT_MILESTONE) 和「社交达人」(SOCIAL_BUTTERFLY) | 前端最多展示 4 种徽章，`AchievementTypeEnum` 可能只定义 4 种枚举值 |
| DB-2 | 社交达人徽章缺少邀请追踪表 | `content_invite_record` 无 `circle_id` 字段，无法统计"邀请用户加入**特定圈子**"的次数 | 社交达人徽章无法实现 |

**建议方案**:
- DB-1: 补充 Flyway 迁移脚本，INSERT `ach_005`(CONTENT_MILESTONE) 和 `ach_006`(SOCIAL_BUTTERFLY)，同步更新 `AchievementTypeEnum`
- DB-2: 选项 A — `content_invite_record` 表增加 `circle_id` 字段；选项 B — 新建 `circle_invite_record` 表

### P1 — API 能力缺失

| 编号 | 问题 | 详情 | 影响 |
|------|------|------|------|
| API-1 | `/participation` 只返回 `Integer` | 返回连续参与天数，不支持 7 天每日状态 `boolean[7]` | 前端 7 天时间轴组件（实心圆/空心圆/横线）无法完整实现 |
| API-2 | WebSocket 通知机制未实现 | 等级提升/徽章获得通知需通过 WebSocket 推送，当前后端未实现 | §6 通知任务无法执行，需改为轮询方案 |

**建议方案**:
- API-1: 选项 A — 新建 `circle_participation_calendar` 表；选项 B — `/participation` 返回 `List<LocalDate>`；选项 C — 返回 `ParticipationVO { days, dailyStatus: List<DayStatus> }`
- API-2: 在 `AchievementServiceImpl.tryAward()` 和 `CircleLevelServiceImpl` 中增加 WebSocket 推送调用。备选：前端 30 秒轮询

### P2 — VO 字段缺失

| 编号 | 问题 | 详情 | 影响                 |
|------|------|------|--------------------|
| VO-1 | MemberGrowthVO 缺少 `totalBadges` | PRD 个人成长页需展示"已获得徽章总数" | 前端无法展示徽章总数         |
| VO-2 | MemberGrowthVO 缺少 `totalBadgeCount` | PRD 个人成长页需展示"徽章总数（含未获得）" | 前端无法展示徽章总数         |
| VO-3 | CircleLevelVO 无 `circleId` | 前端可能需要用于缓存 key | 低影响，调用方已知 circleId |

---

## 三、待确认问题

| 编号 | 问题 | 当前默认假设 | 确认方 | 确认结果 |
|------|------|------------|--------|--------|
| Q-1 | WebSocket 通知消息体格式 | 假设包含 circleId 和通知类型字段 | 后端 | 后端需进行设计 |
| Q-2 | 连续参与 7 天窗口是自然周还是滚动 7 天 | 滚动 7 天（PRD 描述"近 7 天"） | 产品 | 滚动 7 天 |

---

## 四、前端实现待办（41 个 tasks 均未开始）

### 4.1 文档修正后的对接要点

所有 VO 字段均已可用，前端对接时注意：

| 对接项 | 后端实际字段 | 注意 |
|--------|------------|------|
| 徽章图标 | `AchievementVO.iconUrl` | 直接使用 URL，可选本地兜底 |
| 徽章进度 | `AchievementVO.currentProgress` / `targetProgress` | 数值进度条 |
| 徽章状态 | `AchievementVO.status` = `EARNED` / `CLOSE` / `UNEARNED` | `CLOSE` = 即将达成（>= 80%） |
| 今日经验 | `MemberGrowthVO.todayExp` / `dailyExpLimit` | 直接使用，无需硬编码 |
| 徽章摘要 | `MemberGrowthVO.recentBadges`（最多 3 枚） | 个人成长页直接使用，徽章墙页仍需调 achievement/list |
| 用户名头像 | `LeaderboardEntryVO.username` / `avatar` | 直接使用，无需额外查询 |
| 差距 | `LeaderboardEntryVO.gap` | 距上一名得分差值 |
| 权益列表 | `CircleLevelVO.benefits` = `CircleBenefitVO[]` | `{name, unlocked}` 可区分已解锁/未解锁 |
| 分项条件 | `CircleLevelVO.nextLevelConditions` = `LevelConditionVO[]` | `{type, label, current, required, gap}` |
| 等级配置 | `/circle/growth/level/benefit` 和 `/config` | 注意 `/circle/growth/` 前缀 |
| 排行榜参数 | `dimension`: `EXP`/`CONTRIBUTION`/`POST`, `period`: `WEEK`/`MONTH`/`ALL` | 大写枚举值 |

### 4.2 文件组织注意

- **API 文件**: 在现有 `src/api/content/growth/` 目录下新增 `circle.ts`（不能创建 `src/api/content/growth.ts`，会和目录冲突）
- **Store**: `src/store/modules/circleGrowth.ts`（与已有 `growth.ts` 区分）
- **组件**: `src/components/circle/growth/`

---

## 五、优先级排序

| 优先级 | 编号 | 阻塞什么 | 建议处理 |
|--------|------|---------|---------|
| 🔴 P0 | API-2 | §6 通知功能 | 确认 WebSocket 方案 → 实现 |
| 🔴 P0 | DB-1 | 徽章墙只展示 4 种 | 补充 INSERT ach_005/ach_006 |
| 🟡 P1 | DB-2 | 社交达人徽章 | 补充 circle_id 或新建表 |
| 🟡 P1 | API-1 | 7 天时间轴组件 | 扩展 /participation 接口 |
| 🟢 P2 | VO-1/VO-2 | 徽章总数展示 | MemberGrowthVO 补充字段 |
| 🟢 P2 | VO-3 | 缓存 key | 低优先级 |
| ⚪ Q-1/Q-2 | — | 等确认 | — |

---

## 六、相关文档索引

- 文档腐化修正工作流: `docs/prompts/documentation-rot-fix-workflow.md`
- 前端 PRD: `docs/requirements/prd/frontend/EPIC-13-circle-growth-incentive-frontend-prd.md`
- 后端 PRD: `docs/requirements/prd/decomposition/circle/EPIC-13-circle-growth-incentive.md`
- 后端 DDL: `V3.9.1_67__circle_growth_system.sql`
