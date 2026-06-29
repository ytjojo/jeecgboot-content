# 修复计划 — circle-12-analytics-discovery-frontend（前端）

**生成时间**: 2026-06-30
**审核文档数**: 6（backend-issues、drift-report、review-report-20260627、review-report、verification-review、verify-report-20260627）
**总问题数**: 14
**误报过滤说明**: backend-issues.md BI-1（曝光上报接口缺失）为误报，后端recordExposure接口已存在（单sourceId参数版本，与前端sendBeacon逐个上报模式兼容）；review-report.md中7个BLOCK问题在代码中已修复，以verify-report-20260627（最新代码验证）为准。

---

## 修复项

### F-P0-001 - 路由未配置/circle/:id/analytics

**来源**: drift-report-20260627-084036.md, verify-report-20260627-084036.md, review-report-20260627-084036.md
**位置**: jeecgboot-vue3/src/router/routes/modules/circle.ts（children数组）
**优先级**: P0
**依赖**: 无
**类型**: 代码修复-前端

**修复步骤**:
1. 在circle.ts的children数组中添加新路由项（放在`:id`详情路由之后）：
```typescript
{
  path: ':id/analytics',
  name: 'CircleAnalytics',
  component: () => import('/@/views/circle/analytics/index.vue'),
  meta: { title: '数据统计' },
}
```
2. 确认路由路径`/circle/:id/analytics`可正确匹配
3. 检查是否需要添加菜单权限配置（项目使用BACK模式动态路由，菜单由后端配置，静态路由注册即可）

**验证方式**:
- 浏览器直接访问`/circle/{circleId}/analytics`可正常加载页面
- 路由name为CircleAnalytics，无重复命名

---

### F-P0-002 - useRecommendTracking composable完全缺失

**来源**: drift-report-20260627-084036.md, verify-report-20260627-084036.md
**位置**: 需新建 jeecgboot-vue3/src/hooks/circle/useRecommendTracking.ts
**优先级**: P0
**依赖**: F-P0-001（无强依赖，可并行）
**类型**: 代码修复-前端

**修复步骤**:
1. 创建`src/hooks/circle/useRecommendTracking.ts`composable
2. 实现IntersectionObserver可见性检测：监听推荐卡片DOM元素，进入视口时记录
3. 实现500ms批量合并：使用setTimeout/防抖，收集曝光事件后批量上报
4. 实现Set去重：同一sourceId只上报一次曝光
5. 实现sendBeacon保底：页面卸载前使用navigator.sendBeacon发送剩余队列
6. 提供useRecommendTracking()返回{track, flush}方法
7. RecommendList/HotRankList/NewRankList组件中接入该composable
8. 将circleRecommendStore中的reportExposure调用迁移到composable中
9. 注意：后端recordExposure接口参数是sourceId（单个），逐个上报即可

**验证方式**:
- 滚动推荐列表，进入视口的卡片触发曝光上报
- Network面板可见POST /api/v1/content/circle/recommend/exposure请求
- 同一卡片重复进入视口不重复上报
- 页面快速关闭时sendBeacon发送剩余数据

---

### F-P1-001 - analytics/index.vue权限校验API路径错误

**来源**: drift-report-20260627-084036.md, verify-report-20260627-084036.md
**位置**: jeecgboot-vue3/src/views/circle/analytics/index.vue（onMounted权限校验）
**优先级**: P1
**依赖**: F-P0-001（路由配置完成后验证）
**类型**: 代码修复-前端

**修复步骤**:
1. 移除onMounted中直接调用defHttp.get('/api/circle/detail')的权限校验逻辑
2. 改为复用circleStore中已有的currentCircle数据（进入圈子详情页时已加载）
3. 从circleStore.currentCircle?.myRole判断当前用户角色是否为CREATOR或MODERATOR
4. 如果currentCircle不存在（直接访问URL的情况），调用正确路径的圈子详情接口：`/api/v1/content/circle/{id}`
5. 非授权用户展示已有的403 a-result页面
6. 消除多余的网络请求，提升页面加载速度

**验证方式**:
- 创建者/版主进入analytics页面正常显示数据
- 普通成员进入显示403无权限提示
- 不出现`/api/circle/detail`的错误路径请求

---

### F-P1-002 - circleAnalyticsStore.calcChange不返回'new'状态

**来源**: verify-report-20260627-084036.md
**位置**: jeecgboot-vue3/src/store/modules/circleAnalytics.ts（calcChange/memberChange等computed）
**优先级**: P1
**依赖**: 无
**类型**: 代码修复-前端

**修复步骤**:
1. 修改circleAnalyticsStore中的变化百分比计算逻辑
2. 当上期值为0且本期值大于0时，返回`'new'`而非100%
3. 当本期值和上期值都为0时，返回`null`
4. 其他情况正常计算百分比：`Math.round((current - previous) / previous * 100)`
5. 确保返回类型为`number | 'new' | null`，与StatCard组件的change prop类型一致
6. 注意：当前后端不返回上期数据，需要先实现上期数据获取（若后端补充previous*字段），或前端通过两次日期范围请求获取上期数据

**验证方式**:
- 上期为0本期>0时StatCard显示"新增"文案
- 两边都为0时显示"--"
- 正常变化时显示百分比和上下箭头

---

### F-P1-003 - DataAnalyticsPanel成员总数(累计值)不应显示变化百分比

**来源**: verify-report-20260627-084036.md
**位置**: jeecgboot-vue3/src/views/circle/analytics/components/DataAnalyticsPanel.vue（statCards配置）
**优先级**: P1
**依赖**: F-P1-002（change计算修复后）
**类型**: 代码修复-前端

**修复步骤**:
1. 修改DataAnalyticsPanel.vue中的statCards数组配置
2. "成员总数"卡片不传入change prop（累计值不应有环比变化）
3. "新增成员"卡片使用store.newMemberChange（需在store中补充该计算）
4. "帖子总数"同理累计值不显示变化，或根据实际业务需求调整
5. "活跃用户"卡片使用store.activeChange
6. 确保新增成员数的上期对比正确（需要上期数据支持）

**验证方式**:
- "成员总数"卡片不显示变化百分比箭头
- "新增成员"卡片显示新增变化（'new'或百分比）
- 页面布局无错乱

---

### F-P1-004 - 缺少5分钟缓存逻辑(lastFetchTime/isCacheExpired)

**来源**: drift-report-20260627-084036.md, verify-report-20260627-084036.md
**位置**: jeecgboot-vue3/src/store/modules/circleAnalytics.ts
**优先级**: P1
**依赖**: 无
**类型**: 代码修复-前端

**修复步骤**:
1. 在circleAnalyticsStore的state中添加`lastFetchTime: null as number | null`
2. 添加getter`isCacheExpired`：判断当前时间 - lastFetchTime > 5分钟（300000ms）
3. 在fetchAnalytics action中：如果数据已加载且未过期，直接返回不重新请求
4. fetchAnalytics成功后更新lastFetchTime为Date.now()
5. setDateRange修改日期范围时重置lastFetchTime（需要重新请求）
6. clearData时同时重置lastFetchTime
7. keep-alive场景下页面激活时检查isCacheExpired决定是否刷新

**验证方式**:
- 5分钟内重复进入analytics页面不重复请求接口
- 切换时间范围后重新请求数据
- 超过5分钟后自动重新获取

---

### F-P2-001 - TimeRangeSelector disabledDate逻辑需验证90天跨度

**来源**: verify-report-20260627-084036.md
**位置**: jeecgboot-vue3/src/views/circle/analytics/components/TimeRangeSelector.vue
**优先级**: P2
**依赖**: 无
**类型**: 代码修复-前端

**修复步骤**:
1. 审查disabledDate函数的当前实现
2. 修复问题：startDate未选择时，endDate应只能选择今天及之前；startDate选定后，endDate范围为startDate至startDate+90天
3. 参考Ant Design Vue RangePicker的标准disabledDate写法
4. 添加快捷选项（近7天/近30天）的日期范围自动设置，确保不超过90天
5. 补充onCalendarChange事件处理，在面板中实时禁用超范围日期

**验证方式**:
- 无法选择超过90天的日期范围
- 快捷选项正确设置日期且不超过限制
- 结束日期不能早于开始日期

---

### F-DOC-P1-001 - design.md/plan.md API路径需更新为/api/v1/content/circle/

**来源**: review-report-20260627-084036.md, drift-report-20260627-084036.md
**位置**: design.md、plan.md（所有API路径示例）
**优先级**: P1
**依赖**: 无
**类型**: 文档修复

**修复步骤**:
1. 全局替换design.md和plan.md中所有API路径：`/api/circle/` → `/api/v1/content/circle/`
2. 检查backend-issues.md中的API路径也一并更新
3. verification-review.md中的旧路径说明标注为已修正
4. 确保与实际代码中defHttp调用的路径完全一致

**验证方式**:
- 文档中无`/api/circle/`路径（除说明旧版路径的情况外）
- 所有API路径前缀为`/api/v1/content/circle/`

---

### F-DOC-P1-002 - design.md File Structure路径与实际不符

**来源**: review-report-20260627-084036.md, verify-report-20260627-084036.md
**位置**: design.md File Structure章节
**优先级**: P1
**依赖**: 无
**类型**: 文档修复

**修复步骤**:
1. 将API文件路径从`src/api/circle/*.ts`更新为`src/api/content/circle/*.ts`
2. 将类型定义路径从`src/api/circle/types.ts`更新为`src/api/content/model/circleAnalyticsModel.ts`
3. 将发现组件路径从`src/views/circle/list/components/`更新为`src/views/circle/discovery/components/`
4. 补充实际存在的文件路径：analytics.ts、recommend.ts、ranking.ts、circleAnalyticsModel.ts
5. 说明实际目录结构比初始设计更合理（独立discovery目录而非塞在list下）

**验证方式**:
- File Structure中所有路径与jeecgboot-vue3/src/下实际文件一致

---

### F-DOC-P1-003 - design.md Decision 1需更新(useChart→复用useECharts)

**来源**: review-report-20260627-084036.md
**位置**: design.md Decision 1（图表选型）
**优先级**: P1
**依赖**: 无
**类型**: 文档修复

**修复步骤**:
1. 更新Decision 1：不创建自定义useChart composable，直接复用项目已有的`/@/hooks/web/useECharts`
2. 说明理由：项目已有成熟的useECharts封装，无需重复造轮子
3. 更新File Structure，移除`src/hooks/circle/useChart.ts`（仅保留useRecommendTracking.ts）
4. 同步plan.md中关于useChart的描述，改为useECharts

**验证方式**:
- design.md中无useChart的创建计划
- 明确说明复用useECharts

---

### F-DOC-P1-004 - tasks.md任务状态需更新(实际代码已大量实现)

**来源**: review-report-20260627-084036.md
**位置**: tasks.md
**优先级**: P1
**依赖**: 无
**类型**: 文档修复

**修复步骤**:
1. 对照实际代码文件，将已完成的任务标记为[x]
2. API层（5.1）全部标记完成：analytics.ts、recommend.ts、ranking.ts、circleAnalyticsModel.ts
3. Store层（5.2）标记完成：circleAnalytics.ts、circleRecommend.ts
4. 统计页组件（5.3）标记完成：StatCard、TrendChart、TimeRangeSelector、DataAnalyticsPanel、analytics/index.vue
5. 发现页组件（5.4）标记完成：HotRankList、NewRankList、RankingList、RecommendList
6. 列表页集成（5.3相关）标记完成：List.vue Tab集成
7. 未完成的项（路由配置、useRecommendTracking、测试）保持[ ]
8. 确保任务描述与实际实现一致，路径错误的修正路径

**验证方式**:
- tasks.md中[x]标记的项均有对应代码文件存在
- 未标记的项确实未实现（路由、composable、测试）

---

### F-DOC-P2-001 - backend-issues.md需更新(recordExposure已存在)

**来源**: backend-issues.md
**位置**: backend-issues.md
**优先级**: P2
**依赖**: 无
**类型**: 文档修复

**修复步骤**:
1. BI-1（曝光上报接口缺失）：标记为已解决，后端已实现`POST /api/v1/content/circle/recommend/exposure`接口（参数为sourceId单条上报），前端需适配为逐个上报而非批量circleIds
2. BI-2（iconUrl缺失）：更新为"前端使用首字头像avatar兜底，后端暂不补充iconUrl字段"，状态标记为已绕过
3. BI-3（activeScore字段）：标注为暂缓，当前不需要活跃度分数展示
4. BI-4（变化百分比字段）：标注为"前端通过store.calcChange自行计算"
5. BI-5（CSV文件名）：标注为"前端通过Blob下载时自行设置文件名"
6. BI-6（未登录用户推荐）：更新为"前端未登录时跳过推荐接口，直接展示热门榜"
7. 为每个BI项添加当前状态标注

**验证方式**:
- backend-issues.md中每个问题都有状态说明（已解决/绕过/暂缓）
- BI-1明确标注接口已存在及参数差异

---

### F-DOC-P2-002 - design.md需记录曝光上报架构决策

**来源**: drift-report-20260627-084036.md
**位置**: design.md Decision 3（曝光上报方案）
**优先级**: P2
**依赖**: F-P0-002（composable实现后）
**类型**: 文档修复

**修复步骤**:
1. 更新Decision 3：曝光上报采用独立composable（useRecommendTracking）管理，store只维护数据状态
2. 说明composable职责：IntersectionObserver可见性检测、500ms批量合并、Set去重、sendBeacon保底
3. 说明store职责：reportExposure/reportClick action仅做API调用
4. 同步更新相关描述，确保架构决策记录与实际实现一致

**验证方式**:
- design.md Decision 3与F-P0-002实现方案一致

---

### F-DOC-P2-003 - 类型定义路径文档需更新

**来源**: review-report-20260627-084036.md
**位置**: design.md、plan.md类型定义章节
**优先级**: P2
**依赖**: F-DOC-P1-002（已包含在File Structure更新中）
**类型**: 文档修复

**修复步骤**:
1. 将所有`src/api/circle/types.ts`引用更新为`src/api/content/model/circleAnalyticsModel.ts`
2. 说明类型定义遵循项目规范放在content/model/目录下
3. plan.md中所有import路径示例更新为正确路径
4. 检查是否有遗漏的路径引用

**验证方式**:
- 文档中无`src/api/circle/types.ts`的过时路径
- 所有类型路径指向circleAnalyticsModel.ts

---

## 修复依赖关系图

```
F-P0-001 (路由配置) ── F-P1-001 (权限校验路径)
F-P0-002 (曝光composable) ── F-DOC-P2-002 (架构决策文档)
F-P1-002 (change计算) ── F-P1-003 (卡片change显示)
F-DOC-P1-002 (路径文档) ── F-DOC-P2-003 (类型路径)
```

## 跨ChangePair依赖说明

| 前端FixItem | 依赖后端FixItem | 说明 |
|------------|----------------|------|
| F-P1-002 (change计算) | 后端BI-4(变化百分比) | 前端自行计算，后端暂不返回previous字段 |
| F-P0-002 (曝光composable) | B-BLOCK-002 (exposure_time字段) | 后端字段修复后曝光数据才能正常落库 |
| F-P1-001 (权限校验) | B-BLOCK-001 (权限校验实现) | 后端权限校验完成后前端403体验才完整 |

## 按优先级统计

| 优先级 | 代码修复 | 文档修复 | 合计 |
|--------|---------|---------|------|
| P0 | 2 | 0 | 2 |
| P1 | 4 | 4 | 8 |
| P2 | 1 | 3 | 4 |
| **合计** | **7** | **7** | **14** |
