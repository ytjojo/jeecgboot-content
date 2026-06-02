## 1. 基础设施与 API 层

- [ ] 1.1 确认项目 ECharts 依赖，若无则引入 echarts 包并配置按需引入
- [ ] 1.2 创建 `src/api/circle/analytics.ts` — 统计数据查询和导出 API 封装
- [ ] 1.3 创建 `src/api/circle/recommend.ts` — 推荐列表、曝光上报、点击上报 API 封装
- [ ] 1.4 创建 `src/api/circle/ranking.ts` — 热门榜和新锐榜 API 封装
- [ ] 1.5 定义 TypeScript 类型：CircleAnalyticsVO、TrendDataPoint、CircleRecommendVO、CircleRankVO、RecommendExposureReq、RecommendClickReq

## 2. 状态管理

- [ ] 2.1 创建 `src/store/modules/circleAnalytics.ts` — useCircleAnalyticsStore（统计数据、时间范围、加载/错误状态、导出状态）
- [ ] 2.2 创建 `src/store/modules/circleRecommend.ts` — useCircleRecommendStore（推荐列表、热门榜、新锐榜、Tab 状态、降级模式）

## 3. 通用组件

- [ ] 3.1 创建 `src/hooks/circle/useChart.ts` — ECharts composable（实例创建、resize 监听、销毁生命周期）
- [ ] 3.2 创建 `src/views/circle/analytics/components/StatCard.vue` — 统计指标卡片（数值动画、变化百分比方向标识、骨架块加载态）
- [ ] 3.3 创建 `src/views/circle/analytics/components/TrendChart.vue` — 趋势折线图（Tooltip、空状态）
- [ ] 3.4 创建 `src/views/circle/analytics/components/TimeRangeSelector.vue` — 时间范围选择器（快捷选项 + 自定义区间 + disabledDate 90 天限制）

## 4. 数据统计页

- [ ] 4.1 创建 `src/views/circle/analytics/components/DataAnalyticsPanel.vue` — 数据统计面板（集成时间筛选、指标卡片、图表、导出）
- [ ] 4.2 创建 `src/views/circle/analytics/index.vue` — 数据统计页主页面（路由配置、权限守卫、布局）
- [ ] 4.3 实现 CSV 导出功能（fetch + Blob + createObjectURL + `<a download>`）
- [ ] 4.4 实现空状态、错误状态、403 权限不足状态处理

## 5. 推荐与榜单组件

- [ ] 5.1 扩展 CircleCard 组件 — 新增 source prop 和 prefix/extra slot
- [ ] 5.2 创建 `src/views/circle/list/components/RankCircleCard.vue` — 榜单圈子卡片（组合 CircleCard + 排名标识）
- [ ] 5.3 创建 `src/views/circle/list/components/RankList.vue` — 榜单列表（热门榜金银铜色、新锐榜创建时间）
- [ ] 5.4 创建 `src/views/circle/list/components/RecommendationPanel.vue` — 推荐面板（卡片网格、降级逻辑、来源埋点）

## 6. 圈子列表页增强

- [ ] 6.1 修改 `src/views/circle/list/index.vue` — 增加 Tab 切换区（推荐/热门榜/新锐榜）
- [ ] 6.2 实现游客默认展示热门榜、已登录用户默认展示推荐 Tab 逻辑
- [ ] 6.3 实现 Tab 切换加载对应数据，骨架屏占位

## 7. 推荐曝光与点击上报

- [ ] 7.1 创建 `src/hooks/circle/useRecommendTracking.ts` — IntersectionObserver 曝光检测 composable
- [ ] 7.2 实现批量合并上报（延迟 500ms 防抖 + Set 去重）
- [ ] 7.3 实现页面离开保底上报（visibilitychange + sendBeacon + beforeunload）
- [ ] 7.4 实现点击上报（异步发送，不阻塞页面跳转）
- [ ] 7.5 实现上报失败静默处理（仅开发环境控制台日志）

## 8. 响应式布局

- [ ] 8.1 数据统计页响应式适配（PC 一行 4 卡片/平板一行 2/手机纵向堆叠）
- [ ] 8.2 趋势图表响应式适配（移动端横向滑动查看完整时间轴）
- [ ] 8.3 推荐卡片网格响应式适配（PC 一行 3/平板一行 2/手机一行 1-2）
- [ ] 8.4 榜单列表移动端卡片形式展示

## 9. 测试

- [ ] 9.1 编写 `analytics.test.ts` — 指标卡片渲染、时间范围切换、CSV 导出、空状态/错误状态/权限不足
- [ ] 9.2 编写 `recommendation.test.ts` — 推荐列表渲染、降级逻辑、降级状态切换与恢复
- [ ] 9.3 编写 `ranking.test.ts` — 热门榜排名标识、新锐榜创建时间、空状态
- [ ] 9.4 编写 `recommendTracking.test.ts` — IntersectionObserver 曝光、去重、批量合并、sendBeacon 保底、失败静默

## 10. 验证

- [ ] 10.1 运行全量测试，确保 100% 通过
- [ ] 10.2 验证数据统计页功能完整（指标卡片、图表、时间筛选、导出）
- [ ] 10.3 验证推荐与榜单功能完整（推荐列表、降级、热门榜、新锐榜）
- [ ] 10.4 验证响应式布局（375px/768px/992px 断点）
- [ ] 10.5 Code Review — 检查代码质量、命名、边界条件、安全性
