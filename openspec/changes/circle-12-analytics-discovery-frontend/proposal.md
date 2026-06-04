## Why

圈子功能已具备基础能力（EPIC-10）和内容互动能力（EPIC-11），但管理员缺乏数据支撑来优化运营决策，用户发现圈子的方式单一（仅搜索）。需要提供圈子运营数据统计面板和基于兴趣的推荐发现能力，帮助管理员了解圈子发展状况，帮助用户发现感兴趣的圈子。

## What Changes

**新增圈子数据统计页面**
- 新增 `/circle/:id/analytics` 路由，展示圈子运营数据面板
- 核心指标卡片（成员总数、新增成员、发帖总数、活跃用户数）+ 与上期对比变化
- 成员增长、内容发布、活跃度三条趋势折线图
- 时间范围筛选（近 7 天/近 30 天/自定义，最大 90 天）
- 数据导出为 CSV 文件
- 仅圈子创建者和版主可访问，普通成员展示权限不足提示

**增强圈子列表页**
- 在现有 `/circle` 页面增加 Tab 切换区：推荐（默认）、热门榜、新锐榜
- 推荐 Tab：基于用户兴趣展示推荐圈子卡片网格，推荐为空时自动降级为热门榜单
- 热门榜 Tab：按成员数和活跃度排名的 Top 20 圈子
- 新锐榜 Tab：按创建时间倒序展示的新增圈子
- 推荐来源追踪：通过 URL 参数 `source` 标识来源（recommend/hot_rank/new_rank）

**新增推荐曝光与点击上报**
- 使用 IntersectionObserver 检测推荐卡片可见性
- 延迟 500ms 批量合并上报曝光事件
- 点击时即时上报点击事件
- 页面离开时使用 sendBeacon 保底上报

## Success Criteria

- 圈子创建者/版主可在数据统计页查看核心指标和趋势图表，支持时间筛选和 CSV 导出
- 已登录用户进入圈子列表页可看到基于兴趣的推荐圈子，推荐为空时自动降级为热门榜单
- 热门榜展示 Top 20 圈子，新锐榜按创建时间倒序展示
- 推荐曝光和点击事件正确上报，支持来源追踪
- 所有页面支持 PC 端和移动端响应式布局（最低适配 375px）
- 数据统计页首屏加载 < 2s，接口响应 P95 < 1s

## Non-Goals

- 复杂机器学习推荐算法（后端实现）
- 成长激励体系（EPIC-13）
- 付费功能
- 推荐算法权重配置后台
- 实时数据流计算

## Capabilities

### New Capabilities

- `circle-analytics-panel`: 圈子数据统计面板页面，包含核心指标卡片、趋势图表、时间筛选和 CSV 导出功能
- `circle-recommendation`: 圈子推荐列表展示，基于用户兴趣推荐圈子，支持推荐为空时降级为热门榜单
- `circle-ranking`: 热门圈子榜单和新锐圈子榜单展示
- `recommend-tracking`: 推荐来源追踪，包含曝光上报（IntersectionObserver + 批量合并）和点击上报

### Modified Capabilities

（无现有 capability 需要修改）

## Impact

- **路由**: 新增 `/circle/:id/analytics` 路由，修改 `/circle` 路由对应页面
- **API**: 对接 7 个接口（统计数据查询/导出、推荐列表/曝光/点击、热门榜/新锐榜），其中曝光上报接口需后端补充开发
- **组件**: 新增 StatCard、TrendChart、RankCircleCard、RankList、DataAnalyticsPanel、RecommendationPanel、TimeRangeSelector 组件；复用 EPIC-10 的 CircleCard 和 JoinModal
- **状态管理**: 新增 useCircleAnalyticsStore 和 useCircleRecommendStore 两个 Pinia store
- **依赖**: 确认项目已有 ECharts 依赖，若无需在开发前引入
- **权限**: 数据统计页需路由守卫拦截非创建者/版主访问
