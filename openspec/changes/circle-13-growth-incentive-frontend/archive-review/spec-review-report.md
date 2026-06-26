# 前端成长激励 Spec 审查报告

审查日期: 2026-06-24（基于 main 分支 commit 84e8297d）
最后重审: 2026-06-25（重新核对 main 分支最新代码）
审查范围: `circle-13-growth-incentive-frontend` 的 proposal.md / design.md / specs/ / tasks.md
审查依据: 后端实际 Controller/VO 代码 + 前端已有代码结构

> **⚠️ 重要发现**: `CircleLevelController` 路径已从 `/api/v1/content/user/growth/level/info` 迁移至 `/api/v1/content/circle/growth/level/info`，并新增 `/benefit` 和 `/config` 两个接口；其余 3 个 Controller 仍在 `/api/v1/content/user/growth/` 下，路径前缀不一致。

---

## 一、两套成长体系的关系澄清

代码中存在**两套独立但路径前缀相似**的成长体系，并非"混用"而是"并存"：

| 维度 | 用户全局成长（已存在） | 圈子内成长激励（本次 change） |
|------|----------------------|--------------------------|
| Controller 包 | `content.user.controller.ContentUserGrowthController` | `content.user.growth.controller` 下 4 个 Controller |
| 路径前缀 | `/api/v1/content/user/growth` | 混合：圈子等级为 `/api/v1/content/circle/growth/level`，其余 3 个为 `/api/v1/content/user/growth` ⚠️ |
| 关键参数 | 必传 `userId`，无 `circleId` | 必传 `circleId`，部分传 `userId` |
| 功能范围 | 全局积分兑换、勋章佩戴、成长衰减、等级权益 | 圈子等级(L1-L5)、圈内经验/贡献、圈内成就徽章、圈内排行榜 |
| 前端已对接 | `src/api/content/growth/` 目录 + `src/store/modules/growth.ts` | 未实现 |

路径前缀相同是因为后端设计上把两者都归在 `user/growth/` 下，通过子路径和 `circleId` 参数区分。前端需注意文件组织避免冲突。

---

## 二、问题清单

### 🔴 严重问题（必须在实现前修正）

#### 问题 1：API 文件路径冲突

- **位置**: tasks.md 1.1
- **现状**: tasks.md 说创建 `src/api/content/growth.ts`（单文件）
- **冲突**: 已存在 `src/api/content/growth/` 目录（含全局成长的 index.ts/types.ts/badge.ts/point.ts），同名文件和目录不能在同一文件系统中共存
- **修正方案**: 在现有 `src/api/content/growth/` 目录下**新增 `circle.ts`** 文件，导出圈子成长的 4 个接口和类型；更新 `src/api/content/growth/index.ts` 统一 re-export（可选）

#### 问题 2：design.md D9"缺失字段降级"策略大面积错误

design.md D9 表格声称多个字段后端未提供、需前端降级处理，但**后端 VO 实际已提供这些字段**：

| design.md 声称缺失/需降级 | 后端 VO 实际字段 | VO 文件 |
|---|---|---|
| `benefits` 暂不展示权益 | `benefits: List<String>` | CircleLevelVO |
| `nextLevelConditions` 不支持展开分项 | `nextLevelConditions: List<LevelConditionVO>`（含 type/label/current/required/gap） | CircleLevelVO |
| `memberScore/contentScore/activityScore` 无分项得分 | 均已提供，类型 Integer | CircleLevelVO |
| `todayExp` 暂不展示今日经验 | `todayExp: Integer` | MemberGrowthVO |
| `dailyExpLimit` 前端硬编码 100 | `dailyExpLimit: Integer`（后端常量 `GrowthConstant.DAILY_EXP_CAP = 100`） | MemberGrowthVO |
| `recentBadges` 需额外调接口 | `recentBadges: List<AchievementVO>`（最多 3 枚） | MemberGrowthVO |
| `nextLevelThreshold/progressPercent/levelName` 未提及 | 均已提供 | MemberGrowthVO |
| `badgeIcon` 使用本地兜底图标 | `iconUrl: String` | AchievementVO |
| `earnedDate` 暂不展示 | `earnedDate: Date` | AchievementVO |
| `progress/targetValue` 用 conditionDesc 替代 | `currentProgress: Integer` + `targetProgress: Integer` | AchievementVO |
| "即将达成"需前端解析 conditionDesc | `status: String`（值为 EARNED/CLOSE/UNEARNED，CLOSE 即即将达成） | AchievementVO |
| `username/userAvatar` 额外调用户接口 | `username: String` + `avatar: String`（后端已批量填充） | LeaderboardEntryVO |
| `gapToPrev` 暂不展示距上一名差距 | `gap: Integer`（与上一名得分差值） | LeaderboardEntryVO |

**影响**: 按错误降级策略实现会导致：权益列表无法展示、分项条件进度条做不了、进度数值只能显示文本、今日经验条无法展示、用户名头像需发额外请求（实际已返回）、徽章即将达成判断需要脆弱的文本解析。

**修正方案**: 重写 design.md D9 表格，基于后端真实 VO 更新；相应更新字段映射表和 specs 中的"暂不支持"场景。

#### 问题 3：design.md 后端 VO 字段映射表与实际代码不符

design.md "后端 VO 字段映射"章节（第 133-179 行）遗漏了上述所有已存在字段。需按本报告第二节的字段清单完整更新四个 VO 的映射表。

#### 问题 4：排行榜 API 参数枚举值未明确

- **位置**: tasks.md 5.1/5.4、specs/leaderboard/spec.md
- **问题**: 文档说"本周/本月/累计"、"经验值/贡献值/发帖数"但未说明传给后端的实际参数值
- **后端实际枚举值**（从 `LeaderboardServiceImpl.java` 确认）:
  - `period`: `"WEEK"` / `"MONTH"` / `"ALL"`（默认 `"WEEK"`）
  - `dimension`: `"EXP"` / `"CONTRIBUTION"` / `"POST"`
- **修正方案**: 在 design.md 中明确参数值映射表；tasks.md 中补充参数值说明

---

### 🟡 中等问题

#### 问题 5："徽章"(Badge/Achievement) 术语在两套体系中易混淆

- **全局体系 badge**: 有佩戴(wear)、回收(recycle)、目录(catalog)功能，VO 字段为 `badgeId/rarity/worn/badgeCode`，面向平台全局展示
- **圈子体系 achievement**: 有 `status(EARNED/CLOSE/UNEARNED)`、`currentProgress/targetProgress`、`achievementType`，面向圈内成就
- **风险**: 前端组件 `BadgeCard/BadgeWall/BadgeDetailModal` 命名可能与全局勋章系统混淆
- **建议**: design.md D7 已有术语映射，补充说明圈子"徽章"(achievement)和全局"勋章"(badge)是不同功能域；UI 层用"徽章"，类型/API 层严格用 Achievement 命名

#### 问题 6：spec 中多个"暂不支持/暂不展示"与后端实现矛盾

| Spec 文件 | 错误描述 | 实际后端支持 |
|---|---|---|
| specs/circle-level/spec.md | "权益列表暂不展示（未提供 benefits 字段）" | CircleLevelVO.benefits 已提供 |
| specs/circle-level/spec.md | "点击进度条不支持展开分项指标" | nextLevelConditions 已提供，可支持展开 |
| specs/member-growth/spec.md | "今日经验值暂不展示" | todayExp/dailyExpLimit 已提供 |
| specs/member-growth/spec.md | "需单独调用 achievement/list 获取 recentBadges" | MemberGrowthVO.recentBadges 已包含 |
| specs/leaderboard/spec.md | "距上一名差距暂不展示" | LeaderboardEntryVO.gap 已提供 |
| specs/leaderboard/spec.md | "用户名头像需额外调用用户接口" | username/avatar 已返回 |

**修正方案**: 更新对应 spec 场景，将"暂不支持"改为"支持"。

#### 问题 7：tasks.md 任务 3.5 Promise.all 应简化

- **现状**: "使用 Promise.all 并行请求等级和成长信息"（隐含还需要单独请求徽章）
- **实际**: MemberGrowthVO 已包含 `recentBadges`，个人成长页只需并行请求 `getCircleLevel` + `getMemberGrowth` 两个接口
- **徽章墙页**: 仍需单独调用 `getCircleBadges`(achievement/list) 获取全量
- **修正方案**: 明确 3.5 只需 2 个接口并行，徽章摘要从成长信息中取

---

### 🟢 轻微问题

#### 问题 8：design.md Open Questions 状态未更新

Q1、Q3、Q4 已在后端实现中给出答案（独立接口、highlighted 字段、iconUrl 字段），但文档中仍标记为问题，应更新为"已确认"并补充结论。

---

## 三、Store/目录/路由结论

| 项目 | tasks.md 设计 | 结论 |
|------|-------------|------|
| Pinia Store 文件名 | `src/store/modules/circleGrowth.ts` | ✅ 正确，与现有 `growth.ts`(全局)明确区分 |
| 组件目录 | `src/components/circle/growth/` | ✅ 正确 |
| 页面目录 | `src/views/circle/{growth,badges,leaderboard}/` | ✅ 正确 |
| API 文件 | `src/api/content/growth.ts` | ❌ 应改为 `src/api/content/growth/circle.ts` |
| 路由挂载 | `/circle/:id/{growth,badges,leaderboard}` 子路由 | ✅ 正确 |

---

## 四、后端 VO 完整字段参考（用于修正字段映射）

### CircleLevelVO（圈子等级）
| 字段 | 类型 | 说明 |
|------|------|------|
| level | Integer | 当前等级 L1-L5 |
| levelName | String | 等级名称 |
| growthScore | Integer | 成长分 |
| nextLevelThreshold | Integer | 下一等级门槛 |
| progressPercent | Integer | 进度百分比 |
| benefits | List\<CircleBenefitVO\> | 全部权益列表（含 `{name, unlocked}`） |
| memberScore | Integer | 成员规模得分 |
| contentScore | Integer | 内容贡献得分 |
| activityScore | Integer | 活跃互动得分 |
| nextLevelConditions | List\<LevelConditionVO\> | 下一等级各项条件 |

### LevelConditionVO
| 字段 | 类型 | 说明 |
|------|------|------|
| type | String | 维度: MEMBER/CONTENT/INTERACTION |
| label | String | 维度标签 |
| current | Integer | 当前值 |
| required | Integer | 目标值 |
| gap | Integer | 差距值 |

### MemberGrowthVO（成员成长）
| 字段 | 类型 | 说明 |
|------|------|------|
| circleId | String | 圈子ID |
| expPoints | Integer | 经验值 |
| contributionPoints | Integer | 贡献值 |
| level | Integer | 成员等级 |
| levelName | String | 等级名称 |
| postCount | Integer | 发帖数 |
| participationDays | Integer | 连续参与天数 |
| rank | Integer | 圈内排名 |
| nextLevelThreshold | Integer | 下一等级门槛 |
| progressPercent | Integer | 等级进度百分比 |
| todayExp | Integer | 今日已获经验值 |
| dailyExpLimit | Integer | 每日经验上限 |
| recentBadges | List\<AchievementVO\> | 最近获得徽章(最多3枚) |

### AchievementVO（成就徽章）
| 字段 | 类型 | 说明 |
|------|------|------|
| achievementType | String | 徽章类型标识 |
| name | String | 徽章名称 |
| description | String | 徽章描述 |
| iconUrl | String | 徽章图标URL |
| earned | Boolean | 是否已获得 |
| earnedDate | Date | 获得时间 |
| conditionDesc | String | 达成条件描述 |
| currentProgress | Integer | 当前进度数值 |
| targetProgress | Integer | 目标数值 |
| status | String | 状态: EARNED/CLOSE/UNEARNED |

### LeaderboardEntryVO（排行榜条目）
| 字段 | 类型 | 说明 |
|------|------|------|
| userId | String | 用户ID |
| score | Integer | 得分 |
| rankNum | Integer | 排名 |
| highlighted | Boolean | 是否高亮当前用户 |
| gap | Integer | 与上一名得分差值 |
| username | String | 用户名 |
| avatar | String | 用户头像URL |

---

## 五、后端接口完整路径（2026-06-25 更新）

| 功能 | 方法 | 路径 | 参数 | 归属 Controller |
|------|------|------|------|----------------|
| 圈子等级信息 | GET | `/api/v1/content/circle/growth/level/info` ⚠️ | circleId | CircleLevelController |
| 等级权益摘要 | GET | `/api/v1/content/circle/growth/level/benefit` 🆕 | userId | CircleLevelController |
| 等级配置列表 | GET | `/api/v1/content/circle/growth/level/config` 🆕 | 无 | CircleLevelController |
| 成员成长信息 | GET | `/api/v1/content/user/growth/info` | circleId, userId | MemberGrowthController |
| 连续参与天数 | GET | `/api/v1/content/user/growth/participation` | circleId, userId | MemberGrowthController |
| 成就徽章列表 | GET | `/api/v1/content/circle/growth/achievement/list` | circleId, userId | AchievementController |
| 圈子排行榜 | GET | `/api/v1/content/user/growth/leaderboard` | circleId, dimension(EXP/CONTRIBUTION/POST), period(WEEK/MONTH/ALL,默认WEEK), currentUserId | LeaderboardController |

---

## 六、建议修正顺序

1. **必须先修正**: 问题 1(API路径)、问题 2-3(D9和字段映射)、问题 4(参数枚举)
2. **随后修正**: 问题 6(spec场景更新)、问题 7(任务3.5)
3. **可选**: 问题 5(术语补充说明)、问题 8(Open Questions清理)
