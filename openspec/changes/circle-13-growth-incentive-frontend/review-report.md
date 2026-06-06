# circle-13-growth-incentive-frontend 审核报告

**审核日期**: 2026-06-06
**审核人**: AI Agent (opsx:review)
**Change 类型**: 前端 change
**Domain**: Circle (圈子域)
**Epic**: EPIC-13
**配对后端 Change**: circle-13-growth-incentive (已存在)

---

## 一、总览

### 1.1 六维度评分

| 维度 | 得分 | 评级 | 说明 |
|------|------|------|------|
| 完整性 (Completeness) | 7/10 | ⚠️ FLAG | 文档结构规范，但 PRD 中「社交分享能力」未覆盖，6 种徽章仅覆盖 4 种 |
| 一致性 (Consistency) | 4/10 | 🚨 BLOCK | 4 个 API 路径与实际后端全部不匹配，术语不一致（Badge vs Achievement） |
| 可实现性 (Feasibility) | 5/10 | 🚨 BLOCK | 多个前端依赖的 VO 字段后端未实现，WebSocket 通知格式未确认 |
| 可测试性 (Testability) | 8/10 | ✅ PASS | Scenario 可量化，tasks.md 含 7 项测试任务，PRD 含完整测试要点 |
| 接口契约 (API Contract) | 3/10 | 🚨 BLOCK | 4 个接口路径全部错误，12 个 VO 字段缺失，排行榜响应结构不匹配 |
| 边界覆盖 (Boundary) | 7/10 | ⚠️ FLAG | 基本边界已覆盖，缺少并发操作、网络异常、WebSocket 重连等边界 |

### 1.2 量化指标

| 指标 | 数值 | 目标 | 状态 |
|------|------|------|------|
| PRD AC 覆盖率 | 78% (32/41 AC) | >= 90% | ❌ 未达标 |
| API 契约完整率 | 0% (0/4 接口路径正确) | 100% | ❌ 未达标 |
| 边界覆盖率 | 65% (13/20 边界) | >= 80% | ❌ 未达标 |
| TDD 配对率 | 100% (4/4 spec 均有对应测试任务) | 100% | ✅ 达标 |

### 1.3 问题统计

| 级别 | 数量 | 说明 |
|------|------|------|
| BLOCK | 6 | 必须修复后才能 apply |
| FLAG | 8 | 建议修复，不阻塞但有风险 |
| ADVISORY | 4 | 改进建议 |

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
| `GET /content/circle/{circleId}/level` | `GET /content/user/growth/level/info?circleId={circleId}` | ❌ 不一致 |
| `GET /content/circle/{circleId}/growth/me` | `GET /content/user/growth/info?circleId={circleId}&userId={userId}` | ❌ 不一致 |
| `GET /content/circle/{circleId}/badges` | `GET /content/user/growth/achievement/list?circleId={circleId}&userId={userId}` | ❌ 不一致 |
| `GET /content/circle/{circleId}/leaderboard` | `GET /content/user/growth/leaderboard?circleId={circleId}&dimension={dimension}&period={period}&currentUserId={userId}` | ❌ 不一致 |

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
| 圈子等级 | `GET /content/circle/{circleId}/level` | `GET /content/user/growth/level/info?circleId=` | ❌ 路径+参数不匹配 |
| 成员成长 | `GET /content/circle/{circleId}/growth/me` | `GET /content/user/growth/info?circleId=&userId=` | ❌ 路径+参数不匹配 |
| 成就徽章 | `GET /content/circle/{circleId}/badges` | `GET /content/user/growth/achievement/list?circleId=&userId=` | ❌ 路径+参数不匹配 |
| 排行榜 | `GET /content/circle/{circleId}/leaderboard` | `GET /content/user/growth/leaderboard?circleId=&dimension=&period=&currentUserId=` | ❌ 路径+参数不匹配 |

**后端已有但前端未引用的接口**:

| 接口 | 路径 | 可能用途 |
|------|------|---------|
| 连续参与 | `GET /content/user/growth/participation?circleId=&userId=` | ParticipationStreak 组件数据源 |
| 成长汇总 | `GET /content/user/growth/summary?userId=` | 概览数据 |
| 勋章分类目录 | `GET /content/user/growth/badge/catalog?userId=` | 徽章分类展示 |
| 勋章详情 | `GET /content/user/growth/badge/detail?userId=&badgeCode=` | BadgeDetailModal 数据源 |
| 佩戴勋章 | `POST /content/user/growth/badge/wear` | 徽章佩戴功能 |
| 查询佩戴勋章 | `GET /content/user/growth/badge/worn?userId=` | 个人主页展示 |
| 等级权益摘要 | `GET /content/user/growth/level/benefit?userId=` | 权益展示 |
| 等级配置 | `GET /content/user/growth/level/config` | 等级门槛配置 |

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

| # | 维度 | 问题 | 修复建议 |
|---|------|------|---------|
| B1 | 一致性 | 4 个 API 路径全部与后端不匹配 | 更新所有 spec.md 和 design.md 中的 API 路径为后端实际路径 |
| B2 | 一致性 | 前端使用 Badge 术语，后端使用 Achievement 术语 | 统一术语：前端 API 封装层做映射，或全文替换为 Achievement |
| B3 | 可实现性 | AchievementVO 缺失 icon/earnedDate/progress/targetValue/status | 后端补充字段，或前端降级 UI 设计 |
| B4 | 可实现性 | LeaderboardEntryVO 缺失 username/avatar | 后端补充字段，或前端额外调用用户信息接口 |
| B5 | 接口契约 | 排行榜响应结构不匹配（前端期望包装对象，后端返回扁平数组） | 前后端协商统一响应结构 |
| B6 | 完整性 | member-growth/spec.md 依赖 dailyExpLimit 字段，后端未提供 | 后端补充字段或 spec 改为前端硬编码 100 |

### FLAG (建议修复)

| # | 维度 | 问题 | 修复建议 |
|---|------|------|---------|
| F1 | 完整性 | PRD「社交分享能力」未在 proposal/specs 中覆盖 | 补充 spec 和 task，或移至 Non-Goals |
| F2 | 完整性 | 6 种徽章仅覆盖 4 种（缺内容里程碑、社交达人） | 补充 2 种徽章的 spec，或明确标注为后续迭代 |
| F3 | 可实现性 | WebSocket 通知消息体格式未确认 | 与后端确认消息体结构，补充到 design.md |
| F4 | 可实现性 | 排行榜 currentUser 获取方式未确认 | 与后端确认，更新 design.md Q3 |
| F5 | 可实现性 | 徽章图标资源来源未确认 | 与产品/设计确认，更新 design.md Q4 |
| F6 | 边界 | 排行榜维度/周期快速切换可能导致请求竞态 | 在 API 封装层增加请求取消逻辑 |
| F7 | 边界 | WebSocket 断线重连后通知丢失 | 增加重连后主动拉取最新数据的逻辑 |
| F8 | 一致性 | MemberGrowthVO 前端字段名与后端差异较大 | 在 API 封装层统一做字段名映射 |

### ADVISORY (改进建议)

| # | 维度 | 问题 | 建议 |
|---|------|------|------|
| A1 | 边界 | 网络超时/慢响应未覆盖 | 补充超时处理 Scenario |
| A2 | 边界 | 徽章在查看过程中被撤销的实时更新 | 考虑 WebSocket 推送撤销事件 |
| A3 | 边界 | 等级在查看过程中变化的实时更新 | 考虑轮询或 WebSocket 推送 |
| A4 | 可测试性 | 无单元测试任务 | 补充组件单元测试 task（Vitest + Vue Test Utils） |

---

## 六、最终结论

### 6.1 审核结论: 不通过 (BLOCKED)

本 change 存在 **6 个 BLOCK 级别问题**，核心阻塞项为：

1. **API 路径全部错误** — 4 个接口路径与实际后端实现完全不匹配，前端代码将无法正确调用后端
2. **VO 字段严重缺失** — AchievementVO 缺失 6 个字段、LeaderboardEntryVO 缺失 2 个字段，徽章墙和排行榜核心功能无法实现
3. **排行榜响应结构不匹配** — 前端期望包装对象结构，后端返回扁平数组

### 6.2 修复优先级

1. **P0 — 立即修复**: 更新 API 路径 (B1)、补充 AchievementVO 字段 (B3)、补充 LeaderboardEntryVO 字段 (B4)
2. **P1 — 修复后可 apply**: 统一术语 (B2)、解决 dailyExpLimit 数据源 (B6)、对齐排行榜响应结构 (B5)
3. **P2 — apply 后修复**: FLAG 和 ADVISORY 问题

### 6.3 修复后预期

修复 BLOCK 问题后：
- API 契约完整率: 0% → 100%
- PRD AC 覆盖率: 78% → 90%+
- 总体评级: BLOCKED → PASS (with flags)
