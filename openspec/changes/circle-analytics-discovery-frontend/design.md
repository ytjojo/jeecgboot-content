## Context

JeecgBoot Vue3 前端项目，基于 Vue 3 + TypeScript + Ant Design Vue 技术栈。圈子功能已完成基础能力（EPIC-10）和内容互动能力（EPIC-11），现有 CircleCard、JoinModal 等组件可复用。

当前痛点：管理员无法查看圈子运营数据，用户发现圈子仅依赖搜索。本 change 在前端实现数据统计面板和推荐发现能力，后端 API 已有或另行开发。

**约束**:
- 遵循项目现有组件、路由、状态管理规范
- PC 端和移动端响应式布局，最低适配 375px
- 图表库使用 ECharts（已确认），需封装为 TrendChart 组件
- 数据统计页仅圈子创建者和版主可访问

## Goals / Non-Goals

**Goals:**
- 提供圈子数据统计面板，支持核心指标展示、趋势图表、时间筛选和 CSV 导出
- 提供推荐圈子列表，推荐为空时自动降级为热门榜单
- 提供热门榜和新锐榜，帮助用户发现圈子
- 实现推荐曝光和点击上报的前端埋点
- 所有页面支持响应式布局

**Non-Goals:**
- 复杂机器学习推荐算法（后端实现）
- 成长激励体系（EPIC-13）
- 推荐算法权重配置后台
- 实时数据流计算

## Decisions

### Decision 1: 图表库选型 — ECharts

**选择**: ECharts
**理由**: PRD 已确认使用 ECharts。项目需确认是否已有依赖，若无则在开发前引入。ECharts 生态成熟，折线图 + Tooltip 满足需求，支持按需引入减少 bundle 体积。
**替代方案**: AntV G2 — PRD 明确排除，ECharts 社区更大、文档更完善。
**实现**: 封装 `useChart` composable 处理实例创建、resize 监听、销毁等生命周期，TrendChart 组件对外暴露统一 Props 接口。

### Decision 2: 推荐为空降级策略

**选择**: 推荐接口返回空数组或请求失败时，自动加载热门榜单作为兜底内容，页面顶部显示提示。
**理由**: 避免用户看到空白页面，热门榜单数据可用性高，降级体验平滑。
**关键行为**:
- 降级状态不持久化到 URL，刷新后重新请求推荐接口
- 用户手动切回推荐 Tab 时重新请求，不保持降级状态
- 推荐接口返回非空数组时自动退出降级模式

### Decision 3: 曝光上报方案 — IntersectionObserver + 批量合并

**选择**: IntersectionObserver 检测卡片可见性（threshold: 0.5），延迟 500ms 批量合并上报，页面离开时 sendBeacon 保底。
**理由**: 比 scroll 事件更精准、性能更好；批量合并减少请求数；sendBeacon 保证页面关闭时不丢失数据。
**替代方案**: 滚动事件 + 节流 — 性能差、不够精准；MutationObserver — 不适用于可见性检测。
**去重策略**: 维护 Set<string> 记录已上报圈子 ID，同一页面生命周期内仅上报一次。

### Decision 4: 复用 CircleCard 组件 — 组合模式

**选择**: 复用 EPIC-10 的 CircleCard，通过 slot 扩展排名标识，新增 RankCircleCard 组合组件。
**理由**: 遵循组合优于继承原则，CircleCard 保持精简不膨胀，榜单需求通过 RankCircleCard 组合实现。
**扩展方式**:
- CircleCard 新增 `source` prop 和 `prefix`/`extra` slot
- RankCircleCard 内部渲染 CircleCard，通过 prefix slot 插入排名序号
- 推荐列表直接使用 CircleCard，榜单列表使用 RankCircleCard

### Decision 5: 状态管理 — Pinia Store + 页面级缓存

**选择**: 新增 useCircleAnalyticsStore 和 useCircleRecommendStore 两个 Pinia store。
**理由**: 统计数据和推荐数据需要跨组件共享，Pinia 是 Vue 3 标准状态管理方案。
**缓存策略**:
- 统计数据：页面级缓存，keep-alive 场景下 5 分钟过期重新请求
- 推荐/榜单数据：会话级缓存，Tab 切换不重新请求
- 曝光上报：独立于 store，使用 composable 管理

### Decision 6: 时间范围最大跨度 — 90 天

**选择**: 自定义时间范围最大 90 天，DatePicker 使用 disabledDate 前端校验。
**理由**: PRD 默认假设，避免后端查询压力过大。前端优先校验，不依赖后端错误码。

## Risks / Trade-offs

**[ECharts bundle 体积]** → 按需引入折线图模块，使用 tree-shaking；移动端关闭动画、使用 canvas 渲染器
**[推荐接口不可用]** → 降级为热门榜单，保证页面可用性
**[曝光上报数据丢失]** → sendBeacon 保底 + beforeunload 备用；上报失败静默处理不阻塞用户
**[统计数据量大导致导出慢]** → 后端返回文件流，前端使用 Blob + createObjectURL 下载；PRD 提到大数据量分片下载
**[移动端图表性能]** → 减少数据点、关闭动画、超过 30 天按周聚合

## File Structure

```
src/views/circle/
  analytics/
    index.vue                    # 数据统计页主页面
    components/
      StatCard.vue               # 统计指标卡片
      TrendChart.vue             # 趋势折线图
      DataAnalyticsPanel.vue     # 数据统计面板
      TimeRangeSelector.vue      # 时间范围选择器
  list/
    index.vue                    # 圈子列表页（增强）
    components/
      RankCircleCard.vue         # 榜单圈子卡片（组合 CircleCard）
      RankList.vue               # 榜单列表
      RecommendationPanel.vue    # 推荐面板

src/api/circle/
  analytics.ts                   # 统计数据 API
  recommend.ts                   # 推荐 API
  ranking.ts                     # 榜单 API

src/store/modules/
  circleAnalytics.ts             # useCircleAnalyticsStore
  circleRecommend.ts             # useCircleRecommendStore

src/hooks/circle/
  useChart.ts                    # ECharts composable
  useRecommendTracking.ts        # 推荐曝光/点击上报 composable

src/views/circle/__tests__/
  analytics.test.ts              # 数据统计页测试
  recommendation.test.ts         # 推荐面板测试
  ranking.test.ts                # 榜单测试
  recommendTracking.test.ts      # 曝光上报测试
```

## Test Strategy

采用 TDD 驱动开发，测试先行：

**analytics.test.ts** — 数据统计页
- 指标卡片渲染与数值展示（含变化百分比方向标识）
- 时间范围切换触发数据刷新
- CSV 导出功能（mock fetch + Blob）
- 空状态、错误状态、权限不足状态展示
- 响应式布局断点测试

**recommendation.test.ts** — 推荐面板
- 推荐列表渲染与卡片展示
- 推荐为空时降级为热门榜单
- 降级状态切换与恢复
- 私有圈子卡片展示逻辑

**ranking.test.ts** — 榜单
- 热门榜排名 1-3 金银铜色标识
- 新锐榜创建时间展示
- 空状态处理

**recommendTracking.test.ts** — 曝光上报
- IntersectionObserver 触发曝光上报
- 去重策略（同一圈子不重复上报）
- 批量合并上报（500ms 防抖）
- 页面离开时 sendBeacon 保底
- 上报失败静默处理

## Migration Plan

N/A — 本 change 为纯前端新增功能，不涉及部署变更。新增路由和页面，不影响现有功能。

部署顺序：
1. 确认 ECharts 依赖已引入
2. 部署前端代码
3. 验证新路由和页面可访问
4. 验证推荐 API 和统计 API 可用

## Open Questions

1. **ECharts 依赖确认**: 项目是否已有 ECharts 依赖？若无需在 Milestone 1 启动前完成引入
2. **后端 API 就绪时间**: 统计、推荐、榜单接口是否已就绪？前端可先 mock 数据开发
3. **推荐算法**: 前端是否需要展示推荐理由（如"因为你加入了 XX 圈子"）？
4. **导出文件大小限制**: 单次导出的 CSV 文件是否有大小限制？
