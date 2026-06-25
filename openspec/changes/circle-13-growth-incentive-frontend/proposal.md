## Why

圈子目前缺少可感知的成长激励体系，成员无法追踪自己的参与度和贡献价值，缺乏持续回访和互动的正向驱动。EPIC-10（圈子基础能力）和 EPIC-11（圈子内容与互动）前端部分已完成，现在需要在此基础上构建等级、经验值、成就徽章和排行榜体系，形成可感知、可追踪、可持续的激励循环，提升成员活跃度和留存率。

## What Changes

- 新增圈子等级展示模块：在圈子详情页嵌入等级标识、成长进度条、已解锁权益展示
- 新增成员个人成长信息页：展示经验值、贡献值、当前等级、连续参与进度、徽章摘要、今日经验上限
- 新增徽章墙页：分组展示已获得/未获得徽章，支持徽章详情弹窗
- 新增排行榜页：支持经验值/贡献值/发帖数三维度、本周/本月/累计三周期切换，高亮当前用户排名
- 新增成长相关 API 封装：圈子等级(`GET /api/v1/content/circle/growth/level/info`)、成员成长(`GET /api/v1/content/circle/growth/info`)、成就徽章列表(`GET /api/v1/content/circle/growth/achievement/list`)、排行榜(`GET /api/v1/content/circle/growth/leaderboard`)四个核心接口
- 后端已提供但本期可选对接的补充接口：连续参与(`GET /api/v1/content/user/growth/participation`)、成长汇总(`GET /api/v1/content/user/growth/summary`)、勋章分类目录(`GET /api/v1/content/user/growth/badge/catalog`)、勋章详情(`GET /api/v1/content/user/growth/badge/detail`)、佩戴勋章(`POST /api/v1/content/user/growth/badge/wear`)、等级权益摘要(`GET /api/v1/content/circle/growth/level/benefit`)、等级配置(`GET /api/v1/content/circle/growth/level/config`)
- 新增 Pinia Store：`circleGrowth` 管理成长数据缓存
- 新增 10 个 Vue 组件：`CircleLevelBadge`、`CircleLevelProgress`、`GrowthOverviewCard`、`ParticipationStreak`、`DailyExpBar`、`BadgeCard`、`BadgeWall`、`BadgeDetailModal`、`LeaderboardList`、`LeaderboardTabs`
- 接入站内通知 WebSocket：等级提升和徽章获得时触发 Toast 提示和数据刷新

## Scope Clarification — 三个独立成长体系

本 change 涉及三个易混淆的成长体系，前端在页面设计、API 调用和数据模型中需严格区分：

| 体系 | 数据库表 | API 前缀 | 前端展示场景 |
|------|---------|---------|-------------|
| **全局内容社区用户成长** | `content_user_*` 系列表 | `/api/v1/content/user/growth/` | 用户个人主页的全局积分/等级/勋章（跨圈子通用），有衰减降级机制（属于 EPIC-03，不在本 change 范围） |
| **圈子等级** | `circle_level` | `/api/v1/content/circle/growth/level/` | 圈子详情页的等级标识、成长进度、权益展示（L1-L5） |
| **圈子内成员成长** | `circle_member_growth` 等 5 张表 | `/api/v1/content/user/growth/` | 个人成长信息页的经验值、贡献值、徽章、排行榜（单圈子维度） |

**前端区分要点**：
- **API 路径区分**：全局用户成长与圈子内成员成长共享 `/api/v1/content/user/growth/` 前缀，通过 `circleId` 参数区分——无 circleId 走全局体系，有 circleId 走圈子内成员成长体系
- **页面归属**：圈子详情页等级区块 → 圈子等级体系；个人成长信息页/徽章墙/排行榜 → 圈子内成员成长体系；用户个人主页全局积分/等级 → 全局用户成长体系（EPIC-03，不在本 change 范围）
- **数据隔离**：同一用户在不同圈子有独立的经验值、贡献值、徽章和排名，需按 `circleId` 分别展示，不可跨圈子合并

**本 change 范围**：仅覆盖后两个体系（圈子等级 + 圈子内成员成长）。

## Capabilities

### New Capabilities

- `circle-level`: 圈子等级计算与展示，包含等级标识、成长进度条、等级权益展示
- `member-growth`: 成员个人成长信息，包含经验值系统、贡献值、连续参与进度、每日经验上限
- `badge-system`: 成就徽章系统，包含徽章定义、获得/撤销规则、徽章墙展示、徽章详情
- `leaderboard`: 圈子内排行榜，包含多维度、多周期切换、当前用户排名高亮、反作弊规则

### Modified Capabilities

（无现有 spec 需要修改）

## Impact

- **前端路由**: 新增 3 个页面路由（个人成长页、徽章墙页、排行榜页），圈子详情页新增等级与成长区块
- **API 依赖**: 依赖后端 4 个已有接口（`/api/v1/content/circle/growth/level/info`、`/api/v1/content/circle/growth/info`、`/api/v1/content/circle/growth/achievement/list`、`/api/v1/content/circle/growth/leaderboard`）
- **状态管理**: 新增 `circleGrowth` Pinia Store，涉及缓存策略和 WebSocket 通知刷新
- **组件库**: 新增 10 个 Vue 组件，复用项目已有 `Page`、`Description`、`Modal`、`CountTo`、`CardList` 等组件
- **前置依赖**: EPIC-10 圈子基础能力、EPIC-11 圈子内容与互动前端部分已完成
- **响应式适配**: 需适配桌面端（>=1200px）、平板端（768-1199px）、移动端（375-767px）三个断点
