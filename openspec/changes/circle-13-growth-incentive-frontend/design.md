## Context

EPIC-10（圈子基础能力）和 EPIC-11（圈子内容与互动）前端部分已完成，圈子详情页、列表页等基础页面已上线。当前圈子缺少成长激励体系，成员无法感知自己的参与价值和成长进度。

后端 EPIC-13 已实现独立的圈子成长服务，提供以下 API：
- 圈子等级: `GET /api/v1/content/user/growth/level/info?circleId=` (CircleLevelController)
- 成员成长: `GET /api/v1/content/user/growth/info?circleId=&userId=` (MemberGrowthController)
- 成就徽章: `GET /api/v1/content/user/growth/achievement/list?circleId=&userId=` (AchievementController)
- 排行榜: `GET /api/v1/content/user/growth/leaderboard?circleId=&dimension=&period=&currentUserId=` (LeaderboardController)
- 连续参与: `GET /api/v1/content/user/growth/participation?circleId=&userId=` (MemberGrowthController)

前端需要对接这些 API 并构建完整的用户界面。

项目技术栈：Vue 3 + TypeScript + Ant Design Vue + Pinia + Vite，使用 `defHttp` 封装 API 请求。

## Goals / Non-Goals

**Goals:**

- 构建圈子等级展示模块，让成员和创建者感知社区发展状态
- 构建成员个人成长信息页，展示经验值、贡献值、连续参与、徽章等成长数据
- 构建徽章墙页面，激励成员通过特定行为获得成就
- 构建排行榜页面，促进成员间的良性竞争
- 所有页面适配桌面端、平板端、移动端三个断点
- 页面加载性能 P95 < 1 秒

**Non-Goals:**

- 不实现付费功能、商业化能力、实物奖励
- 不实现跨圈子积分兑换或成长数据合并
- 不实现自定义徽章或用户创建徽章
- 不实现外部社交平台分享（朋友圈、微博等）（PRD 1.3「社交分享能力」移至 Non-Goals）
- 不实现排行榜虚拟滚动（Top 50 数据量小，暂不需要）
- 不实现用户个人主页展示已获得徽章和圈子等级（PRD 1.3 社交分享能力的一部分，移至后续迭代）

## Decisions

### D1: 组件组织方式 — 按功能域集中存放

**选择**: 所有成长相关组件放在 `src/components/circle/growth/` 目录下

**理由**: 成长体系是一个内聚的功能域，10 个组件之间有较强的关联性（如 BadgeCard 被 BadgeWall 使用，LeaderboardTabs 被 LeaderboardList 使用）。集中存放便于维护和发现。

**替代方案**: 按页面拆分到各页面目录下 — 组件复用关系不清晰，BadgeCard 同时被徽章墙和个人成长页使用。

### D2: 状态管理 — 独立 Pinia Store + 按 circleId 缓存

**选择**: 新增 `src/store/modules/circleGrowth.ts`，以 `circleId` 为 key 缓存各类数据

**理由**: 成长数据跨页面共享（圈子详情页展示等级，个人成长页展示经验值，排行榜页展示排名），独立 Store 避免数据重复请求。按 circleId 缓存支持用户在不同圈子间切换时快速展示。

**替代方案**: 各页面独立请求不缓存 — 用户频繁切换页面时重复请求，体验差。

### D3: API 并行请求策略

**选择**: 个人成长页使用 `Promise.all` 并行请求等级信息、成长信息、徽章摘要三个接口

**理由**: 三个接口无依赖关系，并行请求可将总加载时间从 3 * RTT 降低为 1 * RTT，满足 P95 < 1 秒的性能要求。

### D4: 通知处理 — 复用已有 WebSocket 通道

**选择**: 监听已有站内通知 WebSocket 消息，识别等级提升和徽章获得事件后触发 Toast 和数据刷新

**理由**: 项目已有完整的 WebSocket 通知基础设施，无需新建通道。前端只需解析消息体中的通知类型字段即可。

### D5: 路由设计 — 子路由挂载

**选择**: 成长相关页面作为圈子详情页的子路由

**路由结构**:
- `/circle/:id` — 圈子详情页（内嵌等级与成长区块）
- `/circle/:id/growth` — 个人成长信息页
- `/circle/:id/badges` — 徽章墙页
- `/circle/:id/leaderboard` — 排行榜页

**理由**: 信息层级从属于圈子，子路由结构清晰表达从属关系，且可复用圈子详情页的布局容器。

### D6: 响应式方案 — 断点 + 自适应布局

**选择**: 使用 CSS 媒体查询 + flexbox/grid 布局，三个断点（1200px / 768px / 375px）

**理由**: 项目已有响应式基础，沿用现有方案保持一致性。成长组件数据密度适中，不需要独立的移动端页面。

## Decisions (补充)

### D7: 术语映射 — 前端 UI 用「徽章」，API 层用 Achievement

**选择**: 前端用户界面保留「徽章」术语（用户友好），API 封装层和 TypeScript 类型使用后端的 Achievement 命名，在封装层做字段映射。

**理由**: 后端已使用 Achievement 命名体系（AchievementController、AchievementVO），强行改动后端成本高。前端 UI 层用「徽章」更符合中文用户认知。API 封装层负责映射：`achievementType` → `badgeId`、`name` → `badgeName` 等。

### D8: 排行榜响应结构适配 — 前端包装

**选择**: 后端返回 `Result<List<LeaderboardEntryVO>>` 扁平数组，前端在 API 封装层包装为 `{ entries, currentUser, totalCount }` 结构。

**理由**: 后端排行榜接口已实现并返回扁平数组，`highlighted` 字段标识当前用户。前端封装层根据 `highlighted` 字段提取 currentUser，无需额外接口调用。

### D9: 缺失字段降级策略

**选择**: 对后端未提供的字段，按以下策略处理：

| 缺失字段 | 降级方案 |
|---------|---------|
| `dailyExpLimit` | 前端硬编码 100（PRD 定义） |
| `todayExp` | 暂不展示，后续后端补充 |
| `recentBadges` | 单独调用 `GET /api/v1/content/user/growth/achievement/list` 获取 |
| `badgeIcon` | 使用本地兜底图标（按 achievementType 映射） |
| `earnedDate` | 暂不展示获得时间 |
| `progress` / `targetValue` | 使用 `conditionDesc` 文本描述替代进度条 |
| `username` / `userAvatar` | 通过 userId 额外调用用户信息接口获取 |
| `nextLevelConditions` / `benefits` | 暂不展示差距条件和权益列表，仅展示进度条 |

**理由**: 后端已实现核心功能，缺失字段多为展示增强。优先保证核心流程可用，展示增强项通过降级处理。

## Risks / Trade-offs

- **[后端接口已就绪]** → 4 个核心接口已实现，可直接对接。部分 VO 字段缺失需降级处理（见 D9）。
- **[WebSocket 通知消息体格式不确定]** → 前端按假设实现通知监听，预留消息体解析的容错逻辑（字段缺失时降级处理，仅 Toast 提示不刷新数据）。
- **[徽章图标资源来源]** → 后端 AchievementVO 未返回 icon 字段，前端按 achievementType 使用本地兜底图标映射。
- **[经验值/排行榜数据实时性]** → 经验值通过 WebSocket 或轮询刷新（延迟 < 10 秒），排行榜每小时更新一次，接受一定延迟。
- **[连续参与进度窗口定义未确认]** → 先按滚动 7 天实现（PRD 描述为「近 7 天」），如需改为自然周再调整。

## Open Questions

| 编号 | 问题 | 影响 | 当前状态 |
|------|------|------|----------|
| Q1 | 圈子等级信息是新增接口还是扩展已有圈子详情接口 | API 对接 | **已确认**: 后端已实现独立接口 `GET /api/v1/content/user/growth/level/info` |
| Q2 | WebSocket 通知消息体是否包含圈子 ID 和通知类型 | 通知解析 | 假设包含，前端预留容错 |
| Q3 | 排行榜 currentUser 由后端返回还是前端匹配 | 高亮逻辑 | **已确认**: 后端通过 `highlighted` 字段标识当前用户 |
| Q4 | 徽章图标由后端返回 URL 还是前端本地维护 | 徽章展示 | **已确认**: 后端未返回 icon 字段，前端使用本地图标映射 |
| Q5 | 连续参与 7 天窗口是自然周还是滚动 7 天 | 参与展示 | 滚动 7 天 |

## 后端 VO 字段映射

### CircleLevelVO

| 后端字段 | 前端映射 | 说明 |
|---------|---------|------|
| `level` | `currentLevel` | 当前等级 |
| `levelName` | `levelName` | 等级名称 |
| `growthScore` | `growthScore` | 成长分 |
| `nextLevelThreshold` | `nextLevelScore` | 下一等级门槛 |
| `progressPercent` | `progressPercent` | 进度百分比 |

### MemberGrowthVO

| 后端字段 | 前端映射 | 说明 |
|---------|---------|------|
| `circleId` | `circleId` | 圈子ID |
| `expPoints` | `experience` | 经验值 |
| `contributionPoints` | `contribution` | 贡献值 |
| `level` | `currentLevel` | 成员等级 |
| `postCount` | `postCount` | 发帖数 |
| `participationDays` | `streakDays` | 连续参与天数 |
| `rank` | `rank` | 圈内排名 |

### AchievementVO

| 后端字段 | 前端映射 | 说明 |
|---------|---------|------|
| `achievementType` | `badgeId` | 徽章类型标识 |
| `name` | `badgeName` | 徽章名称 |
| `description` | `description` | 徽章描述 |
| `earned` | `earned` | 是否已获得 |
| `conditionDesc` | `conditionDesc` | 达成条件描述 |
| (无) | `badgeIcon` | 降级：本地图标映射 |
| (无) | `earnedDate` | 降级：暂不展示 |
| (无) | `progress` / `targetValue` | 降级：用 conditionDesc 替代 |

### LeaderboardEntryVO

| 后端字段 | 前端映射 | 说明 |
|---------|---------|------|
| `userId` | `userId` | 用户ID |
| `score` | `value` | 得分 |
| `rankNum` | `rank` | 排名 |
| `highlighted` | `isCurrentUser` | 是否当前用户 |
| (无) | `userName` | 降级：通过 userId 调用用户接口 |
| (无) | `userAvatar` | 降级：通过 userId 调用用户接口 |
