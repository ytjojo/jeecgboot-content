# circle-13-growth-incentive-frontend 审核报告

**审核日期**: 2026-06-06
**最后重审**: 2026-06-24（基于 main 分支 commit 84e8297d）
**审核人**: AI Agent (opsx:review)
**Change 类型**: 前端 change
**Domain**: Circle (圈子域)
**Epic**: EPIC-13
**配对后端 Change**: circle-13-growth-incentive (已存在)

---

## 🔄 2026-06-24 重审补充

> **后端代码迭代已修复了原报告中通过"降级策略"解决的 VO 字段缺失问题。**

原 B3（AchievementVO 缺失字段）、B4（LeaderboardEntryVO 缺失字段）、B6（dailyExpLimit 缺失）的修复方式是"D9 降级策略"，但截至 2026-06-24，**后端已实际补充了这些字段**，降级策略不再需要。此外：

- **新发现问题**: `CircleLevelController` 路径已从 `/api/v1/content/user/growth/level/info` 迁移至 `/api/v1/content/circle/growth/level/info`，文档中引用的圈子等级 API 路径需更新。
- **字段名差异**: 后端实际字段名与本报告建议略有不同（如 `iconUrl` 而非 `icon`，`currentProgress/targetProgress` 而非 `progress/targetValue`，`benefits` 为 `List<String>` 而非 `List<LevelBenefitVO>`），实现时需以实际 VO 字段为准。
- **D9 降级策略需大幅更新**: 原 D9 中 todayExp/dailyExpLimit/recentBadges/iconUrl/earnedDate/progress/username/avatar/benefits/nextLevelConditions/gap 的降级处理已过时，应直接使用后端字段。

详见各问题表的"2026-06-24 状态"列。

---

## 一、总览

### 1.1 六维度评分

| 维度 | 得分 | 评级 | 说明 |
|------|------|------|------|
| 完整性 (Completeness) | 8/10 | ✅ PASS | 社交分享能力已移至 Non-Goals，徽章种类改为由后端接口决定 |
| 一致性 (Consistency) | 8/10 | ✅ PASS | API 路径已更正，术语映射已明确（D7），字段映射已补充 |
| 可实现性 (Feasibility) | 7/10 | ⚠️ FLAG | 核心字段已对齐，缺失字段已制定降级策略（D9），部分展示增强待后端补充 |
| 可测试性 (Testability) | 8/10 | ✅ PASS | Scenario 可量化，tasks.md 含 7 项测试任务，PRD 含完整测试要点 |
| 接口契约 (API Contract) | 8/10 | ✅ PASS | 4 个接口路径已正确，VO 字段映射已明确，排行榜结构已适配 |
| 边界覆盖 (Boundary) | 7/10 | ⚠️ FLAG | 基本边界已覆盖，请求竞态和断线重连留待实现时处理 |

### 1.2 量化指标

| 指标 | 数值 | 目标 | 状态 |
|------|------|------|------|
| PRD AC 覆盖率 | 90%+ (37/41 AC) | >= 90% | ✅ 达标（缺失字段已降级处理） |
| API 契约完整率 | 100% (4/4 接口路径正确) | 100% | ✅ 达标 |
| 边界覆盖率 | 75% (15/20 边界) | >= 80% | ⚠️ 接近达标 |
| TDD 配对率 | 100% (4/4 spec 均有对应测试任务) | 100% | ✅ 达标 |

### 1.3 问题统计

| 级别 | 数量 | 说明 |
|------|------|------|
| BLOCK | 6 → 0 | 全部已修复 ✅ |
| FLAG | 8 → 2 | 6 个已修复，2 个留待实现时处理（F6 请求竞态、F7 断线重连） |
| ADVISORY | 4 | 改进建议，不阻塞 apply |

---

## 二、维度详细审核

### 2.1 完整性 (Completeness) — 7/10

**文档结构**:
- proposal.md: 结构完整 (Why / What / Capabilities / Impact) ✅
- design.md: 结构完整 (Context / Goals / Decisions / Risks / Open Questions) ✅
- specs/ (4 个): 均采用 ADDED Requirements + Scenario 格式 ✅
- tasks.md: 8 个阶段 41 个任务，覆盖全生命周期 ✅

**内容覆盖缺口**:

| # | 缺失项 | 来源 | 影响 | 级别 |
|---|--------|------|------|------|
| C1 | PRD 1.3 范围中「社交分享能力：用户个人主页展示已获得徽章和圈子等级」在 proposal.md 和 design.md 中均未覆盖 | PRD 1.3 | 功能缺失 | FLAG |
| C2 | PRD US-13.3.1 定义 6 种徽章，但 badge-system/spec.md 仅覆盖 4 种（缺少「内容里程碑」100 篇、「社交达人」5 人邀请） | PRD / spec | 功能缺失 | FLAG |
| C3 | member-growth/spec.md 依赖 `dailyExpLimit` 字段，但后端 VO 未提供，spec 未说明降级方案 | spec / backend-issues | 可实现性风险 | BLOCK |
| C4 | member-growth/spec.md 依赖 `recentBadges` 字段，后端 VO 未提供 | spec / backend-issues | 功能降级 | FLAG |

### 2.2 一致性 (Consistency) — 4/10

**API 路径一致性**:

| 前端 spec 引用 | 实际后端路径 | 一致性 |
|---------------|-------------|--------|
| `GET /api/v1/content/circle/{circleId}/level` | `GET /api/v1/content/user/growth/level/info?circleId={circleId}` | ❌ 不一致 |
| `GET /api/v1/content/circle/{circleId}/growth/me` | `GET /api/v1/content/circle/member_growth/info?circleId={circleId}&userId={userId}` | ❌ 不一致 |
| `GET /api/v1/content/circle/{circleId}/badges` | `GET /api/v1/content/circle/growth/achievement/list?circleId={circleId}&userId={userId}` | ❌ 不一致 |
| `GET /api/v1/content/circle/{circleId}/leaderboard` | `GET /api/v1/content/circle/growth/leaderboard?circleId={circleId}&dimension={dimension}&period={period}&currentUserId={userId}` | ❌ 不一致 |

**术语一致性**:

| 前端文档 | 后端代码 | 一致 |
|---------|---------|------|
| Badge (徽章) | Achievement (成就) | ❌ |
| BadgeVO | AchievementVO | ❌ |
| badgeId | achievementType | ❌ |
| badgeIcon | (无对应字段) | ❌ |

**内部一致性**:
- proposal.md API 描述与 design.md 一致 ✅
- design.md 与 specs 之间 API 路径描述一致（但都与后端不一致） ⚠️
- PRD 中 LeaderboardVO 使用 `entries` 数组 + `currentUser` 字段，后端返回扁平数组 ❌

### 2.3 可实现性 (Feasibility) — 5/10

**后端 VO 字段缺失影响**:

| VO | 缺失字段 | 影响的前端组件 | 严重度 |
|----|---------|--------------|--------|
| CircleLevelVO | `nextLevelConditions`, `benefits` | CircleLevelProgress, 等级权益区块 | P1 |
| MemberGrowthVO | `dailyExpLimit`, `todayExp`, `recentBadges` | DailyExpBar, 徽章摘要区 | P1 |
| AchievementVO | `badgeId`, `icon`, `earnedDate`, `progress`, `targetValue`, `status` | BadgeCard, BadgeWall, BadgeDetailModal | P0 |
| LeaderboardEntryVO | `username`, `avatar` | LeaderboardList | P0 |

**技术可行性问题**:

| # | 问题 | 影响 | 级别 |
|---|------|------|------|
| F1 | WebSocket 通知消息体格式未确认，design.md 仅假设「包含」 | 通知功能无法实现 | FLAG |
| F2 | `dailyExpLimit` 后端未提供，PRD 定义为固定 100，但 spec 期望从 API 获取 | DailyExpBar 组件数据源不确定 | BLOCK |
| F3 | 排行榜样 `currentUser` 由后端返回还是前端匹配未确认（design.md Q3 假设后端返回） | 高亮逻辑实现方案不确定 | FLAG |
| F4 | 徽章图标资源来源未确认（design.md Q4 假设后端返回 URL） | BadgeCard 图标展示方案不确定 | FLAG |

### 2.4 可测试性 (Testability) — 8/10

**Scenario 质量**:
- 所有 Scenario 均有明确的 WHEN/THEN 结构 ✅
- 数值规则可量化（L1-L5 门槛、经验值 10/3/30、每日上限 100） ✅
- 状态转换可验证（等级升降、徽章获得/撤销） ✅

**测试任务覆盖**:
- tasks.md 第 8 阶段包含 7 项测试任务 ✅
- 覆盖功能测试、响应式测试、性能测试 ✅
- PRD 11.1-11.4 节含 16 项功能测试 + 7 项交互测试 + 5 项响应式测试 + 4 项性能测试 ✅

**测试缺口**:
- 无单元测试任务（仅功能测试） ⚠️
- 无 Mock 数据策略说明 ⚠️

### 2.5 接口契约 (API Contract) — 3/10

**路径完整率**: 0/4 (全部不匹配)

**参数完整率**:

| 接口 | 前端 spec 参数 | 后端实际参数 | 匹配 |
|------|--------------|-------------|------|
| 圈子等级 | circleId (path) | circleId (query) | ❌ 参数传递方式不同 |
| 成员成长 | circleId (path) | circleId, userId (query) | ❌ 缺少 userId |
| 成就徽章 | circleId (path) | circleId, userId (query) | ❌ 缺少 userId |
| 排行榜 | circleId (path), dimension, period | circleId, dimension, period, currentUserId (query) | ❌ 缺少 currentUserId |

**响应结构匹配率**:

| VO | 前端期望字段数 | 后端实际字段数 | 缺失字段数 | 匹配率 |
|----|--------------|--------------|-----------|--------|
| CircleLevelVO | 11 | 5 | 6 | 45% |
| MemberGrowthVO | 12 | 7 | 5 | 58% |
| AchievementVO | 9 | 5 | 4 | 44% |
| LeaderboardEntryVO | 6 | 4 | 2 | 67% |

**排行榜响应结构差异**:
- 前端 PRD 期望: `{ dimension, period, entries: [...], currentUser: {...}, currentUserGapToPrev, totalCount }`
- 后端实际: 扁平 `List<LeaderboardEntryVO>` 数组
- 差异: 缺少包装对象、currentUser 分离、gapToPrev、totalCount

### 2.6 边界覆盖 (Boundary) — 7/10

**已覆盖边界**:

| 边界类型 | 场景 | 来源 |
|---------|------|------|
| 最大值 | L5 最高等级展示、每日经验 100 上限 | spec |
| 最小值 | 0 经验值、无徽章、无排行成员 | spec |
| 空状态 | 无排行数据、无参与行为、无徽章 | spec |
| 权限 | 私有圈子未加入成员 | spec |
| 状态转换 | 等级升降、徽章获得/撤销 | spec |
| 数据过期 | 排行榜每小时刷新、等级 30 分钟更新 | design |

**未覆盖边界**:

| # | 边界场景 | 影响 | 级别 |
|---|---------|------|------|
| B1 | 并发经验值操作（同一用户快速发帖+评论） | 每日上限校验准确性 | FLAG |
| B2 | 网络超时/慢响应（非失败） | 骨架屏展示时长、重试逻辑 | ADVISORY |
| B3 | 排行榜维度/周期快速切换 | 请求竞态、数据错乱 | FLAG |
| B4 | WebSocket 断线重连 | 通知丢失、数据不同步 | FLAG |
| B5 | 徽章在查看过程中被撤销 | 实时状态更新 | ADVISORY |
| B6 | 等级在查看过程中变化 | 进度条实时更新 | ADVISORY |
| B7 | 空圈子（0 成员）的等级和排行展示 | 边界数据展示 | ADVISORY |

---

## 三、前后端衔接审计

### 3.1 触发条件

配对后端 change `circle-13-growth-incentive` 目录已存在，触发前后端衔接审计。

### 3.2 接口清单双向对比

| 功能 | 前端引用接口 | 后端定义接口 | 匹配状态 |
|------|------------|------------|---------|
| 圈子等级 | `GET /api/v1/content/circle/{circleId}/level` | `GET /api/v1/content/user/growth/level/info?circleId=` | ❌ 路径+参数不匹配 |
| 成员成长 | `GET /api/v1/content/circle/{circleId}/growth/me` | `GET /api/v1/content/circle/member_growth/info?circleId=&userId=` | ❌ 路径+参数不匹配 |
| 成就徽章 | `GET /api/v1/content/circle/{circleId}/badges` | `GET /api/v1/content/circle/growth/achievement/list?circleId=&userId=` | ❌ 路径+参数不匹配 |
| 排行榜样 | `GET /api/v1/content/circle/{circleId}/leaderboard` | `GET /api/v1/content/circle/growth/leaderboard?circleId=&dimension=&period=&currentUserId=` | ❌ 路径+参数不匹配 |

**后端已有但前端未引用的接口**:

| 接口 | 路径 | 归属 | 可能用途 |
|------|------|------|---------|
| 连续参与 | `GET /api/v1/content/circle/member_growth/participation?circleId=&userId=` | MemberGrowthController | ParticipationStreak 组件数据源（圈子成长） |
| 等级权益摘要 | `GET /api/v1/content/circle/growth/level/benefit?userId=` | CircleLevelController | 权益展示（注意路径: `/circle/growth/` 前缀） |
| 等级配置列表 | `GET /api/v1/content/circle/growth/level/config` | CircleLevelController | 等级门槛配置（注意路径: `/circle/growth/` 前缀） |
| 成长汇总 | `GET /api/v1/content/user/growth/summary?userId=` | ContentUserGrowthController | 用户全局成长，非圈子 |
| 勋章分类目录 | `GET /api/v1/content/user/growth/badge/catalog?userId=` | ContentUserGrowthController | 用户全局成长 |
| 勋章详情 | `GET /api/v1/content/user/growth/badge/detail?userId=&badgeCode=` | ContentUserGrowthController | 用户全局成长 |
| 佩戴勋章 | `POST /api/v1/content/user/growth/badge/wear` | ContentUserGrowthController | 用户全局成长 |
| 查询佩戴勋章 | `GET /api/v1/content/user/growth/badge/worn?userId=` | ContentUserGrowthController | 用户全局成长 |

### 3.3 数据模型一致性

**CircleLevelVO 字段对比**:

| 前端期望字段 | 后端实际字段 | 匹配 |
|------------|------------|------|
| circleId | (无) | ❌ |
| currentLevel | level | ⚠️ 命名不同 |
| levelName | levelName | ✅ |
| growthScore | growthScore | ✅ |
| nextLevelScore | nextLevelThreshold | ⚠️ 命名不同 |
| progressPercent | progressPercent | ✅ |
| memberGap | (无) | ❌ |
| contentGap | (无) | ❌ |
| interactionGap | (无) | ❌ |
| benefits | (无) | ❌ |
| nextBenefits | (无) | ❌ |

**MemberGrowthVO 字段对比**:

| 前端期望字段 | 后端实际字段 | 匹配 |
|------------|------------|------|
| experience | expPoints | ⚠️ 命名不同 |
| contribution | contributionPoints | ⚠️ 命名不同 |
| currentLevel | level | ⚠️ 命名不同 |
| levelName | (无) | ❌ |
| nextLevelExp | (无) | ❌ |
| rank | rank | ✅ |
| streakDays | participationDays | ⚠️ 命名不同 |
| streakDetail | (无) | ❌ |
| todayExp | (无) | ❌ |
| dailyExpCap | (无) | ❌ |
| badges | (无) | ❌ |
| totalBadges | (无) | ❌ |
| (无) | postCount | 后端多出 |
| (无) | circleId | 后端多出 |

**AchievementVO 字段对比**:

| 前端期望字段 | 后端实际字段 | 匹配 |
|------------|------------|------|
| badgeId | achievementType | ⚠️ 命名不同 |
| badgeName | name | ⚠️ 命名不同 |
| badgeIcon | (无) | ❌ |
| description | description | ✅ |
| earned | earned | ✅ |
| earnedDate | (无) | ❌ |
| progress | (无) | ❌ |
| target | (无) | ❌ |
| nearComplete | (无) | ❌ |
| (无) | conditionDesc | 后端多出 |

**LeaderboardEntryVO 字段对比**:

| 前端期望字段 | 后端实际字段 | 匹配 |
|------------|------------|------|
| rank | rankNum | ⚠️ 命名不同 |
| userId | userId | ✅ |
| userName | (无) | ❌ |
| userAvatar | (无) | ❌ |
| value | score | ⚠️ 命名不同 |
| isCurrentUser | highlighted | ⚠️ 命名不同 |

### 3.4 错误码覆盖

前端 specs 中所有接口的错误处理 Scenario 仅描述「展示错误提示和重试按钮」，未定义具体错误码。后端 error handling 策略未在 change 文档中说明。

**结论**: 错误码覆盖不充分，建议前后端协商统一错误码规范。

### 3.5 认证鉴权一致性

- 前端 specs 未提及认证方式（Token / Cookie）
- 后端 design.md 未提及接口鉴权策略
- 排行榜接口需传 `currentUserId`，鉴权方式影响该参数的获取方式

**结论**: 认证鉴权未对齐，需确认。

### 3.6 分页契约

- 排行榜: 前端 PRD 期望 Top 50，后端 spec 同意 Top 50，但后端返回扁平数组，前端期望包装对象含 `totalCount`
- 徽章列表: 无分页（返回全量），前后端一致 ✅
- 等级信息: 单条数据，无需分页 ✅

---

## 四、PRD 追溯矩阵

### 4.1 用户故事覆盖

| PRD 用户故事 | 对应 Spec | 对应 Task | 覆盖状态 |
|-------------|-----------|-----------|---------|
| US-13.1.1 圈子等级计算与展示 | circle-level/spec.md | 2.1-2.4 | ✅ 已覆盖 |
| US-13.2.1 成员经验值系统 | member-growth/spec.md | 3.1-3.5 | ✅ 已覆盖 |
| US-13.2.2 连续参与进度 | member-growth/spec.md | 3.2 | ✅ 已覆盖 |
| US-13.3.1 成就徽章系统 | badge-system/spec.md | 4.1-4.5 | ⚠️ 部分覆盖（缺 2 种徽章） |
| US-13.3.2 圈子内排行榜 | leaderboard/spec.md | 5.1-5.5 | ✅ 已覆盖 |
| PRD 1.3 社交分享能力 | (无对应 spec) | (无对应 task) | ❌ 未覆盖 |

### 4.2 验收标准覆盖

| PRD AC | 对应 Spec Scenario | 覆盖 |
|--------|-------------------|------|
| AC-13.1.1.1 等级提升展示 | circle-level Scenario: 最高等级展示 | ✅ |
| AC-13.1.1.2 等级提升通知 | badge-system Scenario: 获得新徽章 Toast 提示 | ⚠️ 仅覆盖徽章通知 |
| AC-13.1.1.3 等级详情展示 | circle-level Scenario: 展示等级进度 | ✅ |
| AC-13.1.1.4 差距条件展示 | circle-level Scenario: 展示差距条件 | ✅ |
| AC-13.2.1.1 发帖获得经验 | member-growth (后端逻辑) | N/A 前端仅展示 |
| AC-13.2.1.4 个人资料展示 | member-growth Scenario: 展示成长概览数据 | ✅ |
| AC-13.2.1.5 每日上限 | member-growth Scenario: 达到每日上限 | ✅ |
| AC-13.3.1.1 持续创作者徽章 | badge-system spec | ✅ |
| AC-13.3.1.2 优质贡献者徽章 | badge-system spec | ✅ |
| AC-13.3.1.3 活跃参与者徽章 | badge-system spec | ✅ |
| AC-13.3.1.4 徽章通知 | badge-system Scenario: 获得新徽章 Toast 提示 | ✅ |
| AC-13.3.1.5 徽章展示 | badge-system Scenario: 展示已获得/未获得徽章 | ✅ |
| AC-13.3.2.1 三维度排行榜 | leaderboard Scenario: 维度切换 | ✅ |
| AC-13.3.2.2 Top 50 展示 | leaderboard Scenario: 展示 Top 50 列表 | ✅ |
| AC-13.3.2.3 当前用户排名 | leaderboard Scenario: 当前用户高亮 | ✅ |
| AC-13.3.2.4 空状态 | leaderboard Scenario: 无排行数据 | ✅ |
| AC-13.3.2.5 三周期切换 | leaderboard Scenario: 周期切换 | ✅ |

---

## 五、问题清单

### BLOCK (必须修复)

| # | 维度 | 问题 | 原修复方式（2026-06-06） | 2026-06-24 状态 |
|---|------|------|----------------------|---------------|
| B1 | 一致性 | 4 个 API 路径全部与后端不匹配 | 更新路径为 `/user/growth/` 前缀 | ✅ 已修复；⚠️ **新变更**: CircleLevelController 路径已迁移至 `/circle/growth/level/info`，需再次更新 |
| B2 | 一致性 | 前端使用 Badge 术语，后端使用 Achievement 术语 | design.md 新增 D7 术语映射 | ✅ 已修复 |
| B3 | 可实现性 | AchievementVO 缺失 icon/earnedDate/progress/targetValue/status | D9 降级策略（本地图标、不展示进度/日期、解析文本判断状态） | ✅ **后端已实际补充字段**: `iconUrl`, `earnedDate`, `currentProgress`, `targetProgress`, `status(EARNED/CLOSE/UNEARNED)`；D9 降级策略可移除，直接使用后端字段 |
| B4 | 可实现性 | LeaderboardEntryVO 缺失 username/avatar | 通过 userId 额外调用用户接口 | ✅ **后端已实际补充字段**: `username`, `avatar`, `gap`；无需额外调用用户接口 |
| B5 | 接口契约 | 排行榜响应结构不匹配（前端期望包装对象，后端返回扁平数组） | D8 前端包装策略 | ✅ 已修复 |
| B6 | 完整性 | member-growth/spec.md 依赖 dailyExpLimit 字段，后端未提供 | spec 改为前端硬编码 100 | ✅ **后端已实际补充字段**: `dailyExpLimit`, `todayExp`, `recentBadges`, `levelName`, `nextLevelThreshold`, `progressPercent`；无需前端硬编码 |

### FLAG (建议修复)

| # | 维度 | 问题 | 修复建议 | 修复状态（2026-06-24更新） |
|---|------|------|---------|------------------------|
| F1 | 完整性 | PRD「社交分享能力」未在 proposal/specs 中覆盖 | 补充 spec 和 task，或移至 Non-Goals | ✅ 已修复：design.md Non-Goals 已明确标注 |
| F2 | 完整性 | 6 种徽章仅覆盖 4 种（缺内容里程碑、社交达人） | 补充 2 种徽章的 spec，或明确标注为后续迭代 | ✅ 已修复：badge-system/spec.md 改为由后端接口返回数据决定 |
| F3 | 可实现性 | WebSocket 通知消息体格式未确认 | 与后端确认消息体结构，补充到 design.md | ⚠️ 仍待确认：后端未发现 WebSocket 端点 |
| F4 | 可实现性 | 排行榜 currentUser 获取方式未确认 | 与后端确认，更新 design.md Q3 | ✅ 已修复：后端通过 highlighted 字段标识 |
| F5 | 可实现性 | 徽章图标资源来源未确认 | 与产品/设计确认，更新 design.md Q4 | ✅ **后端已提供 iconUrl 字段**，可直接使用；本地图标映射可作为加载失败兜底 |
| F6 | 边界 | 排行榜维度/周期快速切换可能导致请求竞态 | 在 API 封装层增加请求取消逻辑 | ⏳ 待实现时处理 |
| F7 | 边界 | WebSocket 断线重连后通知丢失 | 增加重连后主动拉取最新数据的逻辑 | ⏳ 待实现时处理 |
| F8 | 一致性 | MemberGrowthVO 前端字段名与后端差异较大 | 在 API 封装层统一做字段名映射 | ✅ 已修复：design.md 有字段映射表；⚠️ 字段映射表需更新为后端最新字段 |
| F9 | 一致性 | CircleLevelController 路径已迁移至 `/circle/growth/` | 更新 tasks.md/design.md/specs 中圈子等级 API 路径 | 🔴 **新发现需修复** |

### ADVISORY (改进建议)

| # | 维度 | 问题 | 建议 |
|---|------|------|------|
| A1 | 边界 | 网络超时/慢响应未覆盖 | 补充超时处理 Scenario |
| A2 | 边界 | 徽章在查看过程中被撤销的实时更新 | 考虑 WebSocket 推送撤销事件 |
| A3 | 边界 | 等级在查看过程中变化的实时更新 | 考虑轮询或 WebSocket 推送 |
| A4 | 可测试性 | 无单元测试任务 | 补充组件单元测试 task（Vitest + Vue Test Utils） |

---

## 六、最终结论

### 6.1 审核结论（2026-06-24 更新）: 需要修正文档后 APPROVED

**2026-06-06 原结论**: 通过 (PASS with flags) — 所有 6 个 BLOCK 级别问题通过文档修正和降级策略已修复。

**2026-06-24 更新**: 后端代码迭代已实际补充了原本缺失的 VO 字段（B3/B4/B6 不再需要降级策略），但引入了 1 个新的 BLOCK 级问题：
- 🔴 **B1（新）**: `CircleLevelController` 路径已迁移至 `/api/v1/content/circle/growth/level/info`，文档中引用的旧路径 `/api/v1/content/user/growth/level/info` 需更新
- 📝 **D9 降级策略需重写**: 原本为弥补字段缺失制定的降级方案已过时，应改为直接使用后端字段

其余原 BLOCK 和 FLAG 问题已全部修复或确认。

### 6.2 修复方案总结（2026-06-24 更新）

1. **API 路径** — 已从 RESTful 风格修正为 `/user/growth/` 前缀；⚠️ 圈子等级接口需再次修正为 `/circle/growth/level/info`
2. **术语映射** — design.md D7 决策保留
3. **VO 字段** — 原降级策略已过时，后端已提供完整字段；注意字段名差异（iconUrl/currentProgress/targetProgress/status 枚举/List&lt;String&gt; benefits）
4. **排行榜结构** — D8 前端包装策略保留（后端仍返回扁平数组）
5. **dailyExpLimit** — 后端已提供字段，无需前端硬编码
6. **社交分享能力** — 移至 Non-Goals
7. **徽章种类** — 由后端接口返回数据决定
8. **字段映射** — design.md 字段映射表需更新为后端最新字段

### 6.3 修复优先级（2026-06-24 更新）

1. **P0 — 必须修正**: CircleLevelController 路径更新 (F9/B1)、design.md D9 降级策略重写、design.md 字段映射表更新
2. **P1 — 已完成**: 术语映射 (B2)、排行榜结构 (B5)
3. **P2 — 待实现时处理**: F6(请求竞态)、F7(断线重连)、F3(WebSocket 确认)
