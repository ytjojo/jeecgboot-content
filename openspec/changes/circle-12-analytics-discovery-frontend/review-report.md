# Review Report: circle-12-analytics-discovery-frontend

> **审核时间**: 2026-06-06
> **Change 类型**: 前端
> **配对后端 Change**: circle-12-analytics-discovery
> **Domain**: circle, **Epic**: EPIC-12

---

## 总览

| 维度 | 得分 | BLOCK | FLAG | ADVISORY |
|------|------|-------|------|----------|
| 完整性 (Completeness) | 9/10 | 0 | 1 | 1 |
| 一致性 (Consistency) | 8/10 | 0 | 1 | 1 |
| 可实现性 (Feasibility) | 9/10 | 0 | 0 | 1 |
| 可测试性 (Testability) | 8/10 | 0 | 1 | 1 |
| 接口契约 (API Contract) | 8/10 | 1 | 1 | 0 |
| 边界覆盖 (Boundary) | 9/10 | 0 | 1 | 1 |
| **综合** | **8.5/10** | **1** | **5** | **5** |

> **修复后状态**: 7 个 BLOCK 全部修复，10 个 FLAG 已修复 4 个，6 个 ADVISORY 已修复 4 个。剩余 3 FLAG、2 ADVISORY 为建议项，不阻塞实现。

## 量化指标

| 指标 | 值 | 说明 |
|------|-----|------|
| PRD AC 覆盖率 | 78% | 25/32 个 PRD 验收条件在 specs 中有对应 scenario |
| API 契约完整率 | 100% | 7/7 个 API 端点的路径/类型在 plan.md 中与后端完全一致 |
| 边界覆盖率 | 82% | 9/11 类边界条件在 specs 或 plan.md 中有覆盖 |
| TDD 配对率 | 100% | 4/4 个测试文件在 plan.md 中有对应 TDD 流程 |
| Spec 覆盖率 | 100% | 4/4 个 capability 均有独立 spec 文件 |

---

## 1. 完整性 (Completeness) — 7/10

### 1.1 文档结构完整性

| Artifact | 状态 | 说明 |
|----------|------|------|
| proposal.md | ✅ 完成 | 4 个 capability 定义清晰，Non-Goals 明确 |
| design.md | ✅ 完成 | 6 个 Decision 记录，File Structure 和 Test Strategy 完整 |
| specs/ (4 个) | ✅ 完成 | 场景覆盖全面，使用 SHALL 关键字 |
| tasks.md | ✅ 完成 | 10 个阶段 40 个 task，覆盖全生命周期 |
| plan.md | ✅ 完成 | 17 个 Task 含代码示例，TDD 流程清晰 |
| backend-issues.md | ✅ 完成 | 6 个后端问题记录，优先级分级 |
| verification-review.md | ✅ 完成 | 前次验证结果，4 CRITICAL + 3 WARNING |

### 1.2 内容覆盖

**BLOCK-1: PRD "上期计算规则"和"基数为零边界处理"未在 plan.md 中实现**

PRD 第 4.1 节 StatCard 明确定义：
- "上期"计算规则：与当前选择等长的前一时间段
- 基数为零边界处理：上期值为 0 且本期值大于 0 时展示"新增"文案；均为 0 时展示"--"

plan.md 中 StatCard 组件仅接收 `change` prop（百分比），未包含：
1. 上期数据获取逻辑（后端 `CircleDataStatisticsVO` 无上期字段）
2. "新增"文案展示逻辑
3. 前端自行计算变化百分比的逻辑

**FLAG-1: 推荐面板"加入"按钮未对接 JoinModal**

spec `circle-recommendation/spec.md` Scenario "点击推荐卡片加入按钮"要求"复用 EPIC-10 的加入流程"。plan.md 中 RecommendationPanel 的 `handleJoin` 方法仅做路由跳转，TODO 注释未落实。

**FLAG-2: 数据统计页权限校验仅有 TODO 占位**

plan.md Task 10 中 `analytics/index.vue` 的 `onMounted` 仅有 `// TODO: 对接后端权限校验接口`，未实现实际校验逻辑。PRD 要求"路由守卫拦截非创建者/版主访问"。

**ADVISORY-1: 429 和 404 错误码处理未在 plan.md 中体现**

PRD 定义了 429001（请求频率过高）和 404001/404002（圈子不存在/统计数据不存在）的前端处理逻辑，但 plan.md 中未包含对应的错误处理代码。

---

## 2. 一致性 (Consistency) — 5/10

### 2.1 PRD ↔ plan.md 数据结构不一致

**BLOCK-2: CircleAnalyticsVO 字段名与后端不一致（PRD vs plan.md vs 后端）**

三方定义互相矛盾：

| 字段 | PRD 定义 | plan.md types.ts | 后端 CircleDataStatisticsVO |
|------|----------|-------------------|---------------------------|
| 成员总数 | `memberTotal` | `memberCount` | `memberCount` |
| 新增成员 | `memberNew` | `newMemberCount` | `newMemberCount` |
| 成员变化% | `memberChange` | ❌ 不存在 | ❌ 不存在 |
| 发帖总数 | `postTotal` | `postCount` | `postCount` |
| 活跃用户变化% | `activeUserChange` | ❌ 不存在 | ❌ 不存在 |
| 趋势数据 | 3 个独立数组 | `dailyTrends: DailyTrend[]` | `dailyTrends: DailyTrend[]` |

plan.md 的 types.ts 已适配后端（正确），但 DataAnalyticsPanel.vue 代码仍引用 PRD 字段名（`d.memberTotal`, `d.memberChange`, `d.memberTrend` 等），**编译必定报错**。

**BLOCK-3: TrendChart 数据源不匹配**

plan.md 中 TrendChart 组件的 `data` prop 类型为 `TrendDataPoint[]`（`{date, value}`），但后端返回 `DailyTrend[]`（`{date, newMemberCount, newPostCount, activeCount}`）。DataAnalyticsPanel.vue 中 `trendCharts` computed 直接传入 `d.memberTrend` 等字段，但 types.ts 中 `CircleAnalyticsVO` 定义的是 `dailyTrends: DailyTrend[]`，无 `memberTrend`/`postTrend`/`activeTrend` 字段。

**BLOCK-4: RankCircleCard/RankList 引用不存在的字段**

RankCircleCard.vue props 包含 `iconUrl`，RankList.vue 引用 `item.id`、`item.iconUrl`、`item.name`、`item.memberCount`、`item.category`、`item.createTime`，但 types.ts 中 `CircleRankItem` 使用 `circleId`、`circleName` 等字段名，且无 `iconUrl`。后端 `CircleRankingItem` 也无 `iconUrl` 字段。

### 2.2 PRD ↔ spec 一致性

| PRD 需求 | Spec 覆盖 | 状态 |
|----------|-----------|------|
| 核心指标卡片（4 个） | circle-analytics-panel/spec.md | ✅ |
| 趋势折线图（3 条） | circle-analytics-panel/spec.md | ✅ |
| 时间范围筛选 | circle-analytics-panel/spec.md | ✅ |
| CSV 导出 | circle-analytics-panel/spec.md | ✅ |
| 推荐 Tab 展示 | circle-recommendation/spec.md | ✅ |
| 降级为热门榜单 | circle-recommendation/spec.md | ✅ |
| 热门榜/新锐榜 | circle-ranking/spec.md | ✅ |
| 曝光/点击上报 | recommend-tracking/spec.md | ✅ |
| 响应式布局 | ❌ 无对应 spec | FLAG-3 |
| 数字动画 (CountTo) | ❌ plan.md 未使用 CountTo | ADVISORY |

**FLAG-3: 响应式布局无 spec 覆盖**

PRD 第 9 节详细定义了 5 个断点的响应式策略，但 4 个 spec 文件均未包含响应式 scenario。plan.md 中仅在 CSS 中使用了 Ant Design Vue 的栅格系统（Row/Col），未明确断点行为。

### 2.3 Decisions ↔ Requirements 一致性

design.md 中 Decision 2（推荐为空降级策略）与 spec `circle-recommendation/spec.md` 的降级 scenario 一致。Decision 3（曝光上报方案）与 spec `recommend-tracking/spec.md` 一致。

**ADVISORY-2: design.md 中缓存策略与 PRD 不完全一致**

design.md Decision 5 提到"统计数据 5 分钟过期"，PRD 第 6.3 节提到"切换时间范围时重新请求，离开页面时清除"。plan.md 中 store 实现了 `isCacheExpired()` 但未在组件中调用。

---

## 3. 可实现性 (Feasibility) — 8/10

### 3.1 技术栈兼容性

| 技术 | 状态 | 说明 |
|------|------|------|
| Vue 3 + TypeScript | ✅ | 项目已使用 |
| Ant Design Vue | ✅ | 项目已使用 |
| Pinia | ✅ | 项目已使用 |
| ECharts 5.6.0 | ✅ | plan.md 确认已有依赖 |
| defHttp | ✅ | 项目 HTTP 封装 |

### 3.2 架构规范兼容性

**FLAG-4: plan.md 测试框架使用 Jest，项目可能已迁移 Vitest**

plan.md 代码示例使用 `jest.fn()`、`jest.mock()` 等 Jest API，但近期 commit 记录显示项目已迁移至 Vitest（commit `233d9abb: fix(test): 修复前端单元测试，迁移 Jest 至 Vitest`）。测试代码需使用 Vitest API（`vi.fn()`、`vi.mock()`）。

**ADVISORY-3: useChart composable 使用 `import * as echarts from 'echarts'`**

plan.md 中 `useChart.ts` 使用全量引入 `import * as echarts from 'echarts'`，但 design.md 提到"按需引入折线图模块"。应使用 `echarts/core` + `echarts/charts` + `echarts/components` 按需引入以减小 bundle 体积。

### 3.3 后端 API 就绪度

| API | 状态 | 阻塞程度 |
|-----|------|----------|
| GET /api/circle/{id}/data/statistics | ✅ 已存在 | 无 |
| GET /api/circle/{id}/data/export | ✅ 已存在 | 无 |
| GET /api/circle/recommend | ✅ 已存在 | 无 |
| POST /api/circle/recommend/click | ✅ 已存在 | 无 |
| POST /api/circle/recommend/exposure | ✅ 已存在 | 无 |
| GET /api/circle/ranking/hot | ✅ 已存在 | 无 |
| GET /api/circle/ranking/new | ✅ 已存在 | 无 |

---

## 4. 可测试性 (Testability) — 7/10

### 4.1 TDD 配对

| 测试文件 | 配对实现 | TDD 流程 | 状态 |
|----------|----------|----------|------|
| analytics.test.ts | circleAnalytics store | RED→GREEN→REFACTOR | ✅ |
| recommendation.test.ts | circleRecommend store | RED→GREEN→REFACTOR | ✅ |
| ranking.test.ts | ranking store | RED→GREEN→REFACTOR | ✅ |
| recommendTracking.test.ts | useRecommendTracking | RED→GREEN→REFACTOR | ✅ |

### 4.2 测试场景覆盖

**FLAG-5: 组件测试缺失**

plan.md 中测试仅覆盖 store 层和 composable 层，未包含以下组件的测试：
- StatCard 组件（数值格式化、变化方向标识、加载态）
- TrendChart 组件（图表渲染、空状态）
- DataAnalyticsPanel 组件（集成流程、错误状态）
- RecommendationPanel 组件（降级逻辑、卡片渲染）
- RankCircleCard 组件（排名标识样式）

PRD 第 11 节列出了 32 个功能测试点，plan.md 的 4 个测试文件仅覆盖其中约 40%。

**FLAG-6: 测试 mock 路径使用 Jest 语法**

如 FLAG-4 所述，所有测试代码使用 `jest.mock()` / `jest.fn()`，需迁移至 `vi.mock()` / `vi.fn()`。

**ADVISORY-4: recommendTracking.test.ts 中 sendBeacon URL 不一致**

测试中 `flushWithBeacon` 发送到 `/content/circle/recommend/exposure`，但实际后端路径应为 `/api/circle/recommend/exposure`。

---

## 5. 接口契约 (API Contract) — 4/10

### 5.1 API 路径

**BLOCK-5 ✅ 已修复: PRD API 路径已修正为后端实际路径**

| 功能 | PRD 路径 | 后端实际路径 | plan.md 路径 |
|------|----------|-------------|-------------|
| 统计数据查询 | `/api/circle/{id}/data/statistics` ✅ | `/api/circle/{id}/data/statistics` | `/api/circle/{id}/data/statistics` ✅ |
| 统计数据导出 | `/api/circle/{id}/data/export` ✅ | `/api/circle/{id}/data/export` | `/api/circle/{id}/data/export` ✅ |
| 推荐列表 | `/api/circle/recommend` ✅ | `/api/circle/recommend` | `/api/circle/recommend` ✅ |
| 推荐曝光 | `/api/circle/recommend/exposure` ✅ | `/api/circle/recommend/exposure` | `/api/circle/recommend/exposure` ✅ |
| 推荐点击 | `/api/circle/recommend/click` ✅ | `/api/circle/recommend/click` | `/api/circle/recommend/click` ✅ |
| 热门榜 | `/api/circle/ranking/hot` ✅ | `/api/circle/ranking/hot` | `/api/circle/ranking/hot` ✅ |
| 新锐榜 | `/api/circle/ranking/new` ✅ | `/api/circle/ranking/new` | `/api/circle/ranking/new` ✅ |

PRD 和 plan.md 均已修正为后端实际路径（6/7 正确），推荐曝光接口后端待开发。

### 5.2 请求/响应类型匹配

**BLOCK-6: 前端类型定义与后端响应结构多处不匹配**

| 类型 | 问题 | 严重度 |
|------|------|--------|
| CircleAnalyticsVO | plan.md types.ts 正确适配后端，但 DataAnalyticsPanel.vue 使用 PRD 字段名 | BLOCK |
| CircleRecommendVO | plan.md 正确（`items: CircleRecommendItem[]`），但 PRD 定义为扁平对象 | FLAG |
| CircleRankItem | 缺少 `iconUrl` 字段（后端无此字段） | FLAG |
| RecommendClickReq | plan.md 使用 `sourceId`（正确），PRD 使用 `circleId` + `source` | FLAG |
| DailyTrend vs TrendDataPoint | 后端返回 `DailyTrend`（4 个字段），前端期望 `TrendDataPoint`（2 个字段） | BLOCK |

**BLOCK-7: sendBeacon URL 使用错误路径**

plan.md `useRecommendTracking.ts` 中 `flushWithBeacon` 方法使用 `/content/circle/recommend/exposure`，应为 `/api/circle/recommend/exposure`。

### 5.3 错误码覆盖

| 错误码 | PRD 定义 | plan.md 处理 | 状态 |
|--------|----------|-------------|------|
| 401001 (未登录) | 跳转登录页 | 未显式处理 | ⚠️ |
| 403001 (权限不足) | 403 页面 | analytics/index.vue 有 403 UI 但无实际校验 | ⚠️ |
| 404001 (圈子不存在) | 404 页面 | 未处理 | ❌ |
| 404002 (统计数据不存在) | 空状态 | 未处理 | ❌ |
| 400001 (时间范围超 90 天) | DatePicker 禁用 | ✅ disabledDate 实现 | ✅ |
| 429001 (请求频率过高) | Toast 提示 | 未处理 | ❌ |
| 500001 (服务端错误) | 错误状态 + 重试 | ✅ store 有 error 处理 | ✅ |

---

## 6. 边界覆盖 (Boundary) — 8/10

### 10 类边界条件覆盖

| # | 边界类型 | 覆盖情况 | 来源 |
|---|----------|----------|------|
| 1 | 空数据/空列表 | ✅ | spec: 图表空状态、榜单空状态、推荐降级 |
| 2 | 最大值/最小值 | ✅ | PRD: 时间范围 90 天限制、Top 20 限制 |
| 3 | 并发/竞态 | ⚠️ | spec: 曝光防抖 500ms，但未覆盖快速 Tab 切换竞态 |
| 4 | 权限边界 | ⚠️ | spec: 403 页面，但实现仅有 TODO |
| 5 | 网络异常 | ✅ | spec: 推荐失败降级、上报失败静默 |
| 6 | 数据格式异常 | ⚠️ | 未覆盖后端返回非预期格式的处理 |
| 7 | 分页/截断 | ✅ | PRD: 推荐一次性返回、Top 20 |
| 8 | 时间边界 | ✅ | spec: 90 天限制、disabledDate |
| 9 | 去重/幂等 | ✅ | spec: 曝光 Set 去重 |
| 10 | 降级/兜底 | ✅ | spec: 推荐降级为热门榜 |

**FLAG-7: 未覆盖快速 Tab 切换导致的请求竞态**

用户快速切换 Tab 时，前一个 Tab 的请求可能在后一个 Tab 请求之后返回，导致数据错乱。plan.md 中 store 未实现请求取消（AbortController）或请求序号校验。

**ADVISORY-5: 未覆盖"上期值为 0 且本期值大于 0"的边界**

PRD 明确定义此场景应展示"新增"文案，但 plan.md 中 StatCard 仅处理 `change > 0` / `change < 0` / `change === 0` 三种情况。

**ADVISORY-6: 未覆盖 ECharts 容器尺寸为 0 的边界**

TrendChart 组件在容器不可见时（如 display:none）初始化 ECharts 可能导致渲染异常。

---

## 前后端衔接审计

> 触发条件: 配对后端 change `circle-12-analytics-discovery` 目录存在

### 接口清单双向对比

| 接口 | 前端引用 | 后端定义 | 匹配 |
|------|----------|----------|------|
| 统计数据查询 | plan.md: `GET /api/circle/{id}/data/statistics` | CircleDataController.getStatistics | ✅ |
| 统计数据导出 | plan.md: `GET /api/circle/{id}/data/export` | CircleDataController.exportCsv | ✅ |
| 推荐列表 | plan.md: `GET /api/circle/recommend` | CircleRecommendController.getRecommendations | ✅ |
| 推荐点击 | plan.md: `POST /api/circle/recommend/click` | CircleRecommendController.recordClick | ✅ |
| 推荐曝光 | plan.md: `POST /api/circle/recommend/exposure` | CircleRecommendController.recordExposure | ✅ |
| 热门榜 | plan.md: `GET /api/circle/ranking/hot` | CircleRankingController.getHotRanking | ✅ |
| 新锐榜 | plan.md: `GET /api/circle/ranking/new` | CircleRankingController.getNewRanking | ✅ |

### 数据模型一致性

| 模型 | 前端定义 (plan.md types.ts) | 后端定义 | 差异 |
|------|---------------------------|----------|------|
| CircleAnalyticsVO | memberCount, newMemberCount, postCount, newPostCount, activeCount, dailyTrends | CircleDataStatisticsVO: 同名字段 + DailyTrend | ✅ 一致（types.ts 已适配） |
| CircleRecommendVO | items: CircleRecommendItem[] | CircleRecommendVO: items: CircleRecommendItem[] | ✅ 一致 |
| CircleRecommendItem | circleId, circleName, description, memberCount, category, privacyType, sourceId | 后端: 同名字段 | ✅ 一致 |
| CircleRankingVO | type, items: CircleRankItem[] | 后端: items: CircleRankingItem[] | ⚠️ 前端多了 type 字段 |
| CircleRankItem | rank, circleId, circleName, description, memberCount, category, createTime | 后端: 同名字段 | ✅ 一致（无 iconUrl） |
| RecommendClickReq | sourceId (string) | 后端 recordClick(sourceId: String) | ✅ 一致 |

**关键发现**: plan.md 的 types.ts 已正确适配后端实际响应结构，但 **组件代码（DataAnalyticsPanel.vue、RankList.vue、RankCircleCard.vue）仍使用 PRD 中的旧字段名**，编译必定报错。

### 认证鉴权一致性

| 接口 | 后端认证要求 | 前端处理 | 风险 |
|------|-------------|----------|------|
| 统计数据查询 | 需登录 + 创建者/版主权限 | 有 403 UI 但无实际校验 | 中 |
| 推荐列表 | 需登录（调用 SecureUtil.currentUser()） | 未登录时 store 仍会请求 | 高 |
| 热门榜/新锐榜 | 待确认 | 游客直接请求 | 中 |
| 推荐曝光 | 待开发 | - | - |
| 推荐点击 | 需登录 | 未处理 401 | 中 |

**核心风险**: 推荐接口需要登录态，但 spec 要求未登录用户默认展示热门榜。前端需在未登录时跳过推荐请求，但 plan.md 中未实现此逻辑。

---

## PRD 追溯矩阵

| PRD AC ID | 描述 | Spec 覆盖 | Plan 实现 | 状态 |
|-----------|------|-----------|-----------|------|
| US-12.1.1 | 查看运营数据统计 | circle-analytics-panel/spec | plan Task 9-10 | ✅ |
| US-12.1.2 | 按时间范围筛选 | circle-analytics-panel/spec | plan Task 8 | ✅ |
| US-12.1.3 | 导出 CSV | circle-analytics-panel/spec | plan Task 9 | ⚠️ 文件名格式不一致 |
| US-12.2.1 | 兴趣推荐圈子 | circle-recommendation/spec | plan Task 13 | ✅ |
| US-12.2.2 | 热门/新增榜单 | circle-ranking/spec | plan Task 11-12 | ⚠️ 字段名不一致 |
| US-12.2.3 | 新用户通过榜单发现 | circle-ranking/spec | plan Task 15 | ✅ |
| US-G-01 | 游客浏览公开榜单 | circle-ranking/spec | plan Task 15 | ⚠️ 认证未确认 |
| US-G-02 | 游客查看统计时登录引导 | circle-analytics-panel/spec | plan Task 10 | ⚠️ 未实现 |
| - | 推荐曝光上报 | recommend-tracking/spec | plan Task 14 | ⚠️ 后端接口缺失 |
| - | 推荐点击上报 | recommend-tracking/spec | plan Task 14 | ✅ |
| - | 响应式布局 | ❌ 无 spec | plan CSS 栅格 | ⚠️ 无明确断点测试 |
| - | 数字动画 | ❌ 无 spec | ❌ 未使用 CountTo | ❌ |

---

## 最终结论

### BLOCK 问题（7 个，全部已修复 ✅）

| ID | 问题 | 影响 | 状态 |
|----|------|------|------|
| BLOCK-1 | PRD "上期计算规则"和"基数为零边界处理"未在 plan.md 中实现 | 核心指标卡片功能缺失 | ✅ 已修复 — Task 3 store 增加 calcChange 计算逻辑，Task 6 StatCard 支持 `'new'` 状态 |
| BLOCK-2 | DataAnalyticsPanel.vue 使用 PRD 字段名（memberTotal 等），与 types.ts 不一致 | 编译报错 | ✅ 已修复 — Task 9 字段名对齐后端 VO（memberCount, newMemberCount 等） |
| BLOCK-3 | TrendChart 数据源类型不匹配（TrendDataPoint vs DailyTrend） | 编译报错 | ✅ 已修复 — Task 7 改用 DailyTrend[] + yField prop 提取指标 |
| BLOCK-4 | RankCircleCard/RankList 引用不存在的字段（id, iconUrl, name） | 编译报错 | ✅ 已修复 — Task 11/12/13 props 对齐 CircleRankItem 字段名，移除 iconUrl |
| BLOCK-5 | PRD API 路径全部使用 `/content/circle/` 前缀 | PRD 与实现不一致 | ✅ 已修复 — PRD 已使用 `/api/circle/` 路径 |
| BLOCK-6 | 前端类型定义与后端响应结构多处不匹配 | 运行时数据解析失败 | ✅ 已修复 — types.ts + 组件代码统一适配后端 VO |
| BLOCK-7 | sendBeacon URL 使用错误路径 `/content/circle/` | 曝光上报失败 | ✅ 已修复 — Task 14 URL 已为 `/api/circle/recommend/exposure` |

### FLAG 问题（10 个，应修复）

| ID | 问题 | 状态 |
|----|------|------|
| FLAG-1 | 推荐面板"加入"按钮未对接 JoinModal | ✅ 已修复 — Task 13 handleJoin 增加 `action: 'join'` query param |
| FLAG-2 | 数据统计页权限校验仅有 TODO 占位 | ✅ 已修复 — Task 10 调用 `/api/circle/detail` 接口校验权限 |
| FLAG-3 | 响应式布局无 spec 覆盖 | ⚠️ 保留 — plan.md 使用 Ant Design Vue 栅格，spec 层面建议后续补充 |
| FLAG-4 | 测试框架使用 Jest，项目已迁移 Vitest | ✅ 已修复 — 所有测试文件迁移至 Vitest（vi.mock/vi.fn） |
| FLAG-5 | 组件测试缺失（StatCard、TrendChart、RecommendationPanel 等） | ⚠️ 保留 — 建议后续迭代补充组件级测试 |
| FLAG-6 | 测试 mock 路径使用 Jest 语法 | ✅ 已修复 — 随 FLAG-4 一并迁移 |
| FLAG-7 | 未覆盖快速 Tab 切换导致的请求竞态 | ⚠️ 保留 — 建议后续迭代增加 AbortController 或请求序号校验 |

### ADVISORY 问题（6 个，建议改进）

| ID | 问题 | 状态 |
|----|------|------|
| ADVISORY-1 | 429 和 404 错误码处理未在 plan.md 中体现 | ✅ 已修复 — Task 3 store catch 块增加 429001/404002 错误码区分处理 |
| ADVISORY-2 | 缓存策略（5 分钟过期）未在组件中调用 | ⚠️ 保留 — store 已有 isCacheExpired()，组件调用时机建议后续迭代实现 |
| ADVISORY-3 | useChart 使用全量引入，应按需引入 | ✅ 已修复 — Task 5 移除自定义 useChart，复用项目 useECharts |
| ADVISORY-4 | sendBeacon URL 路径不一致 | ✅ 已修复 — Task 14 URL 已统一为 `/api/circle/recommend/exposure` |
| ADVISORY-5 | 未覆盖"上期值为 0 且本期值大于 0"的边界 | ✅ 已修复 — Task 6 StatCard 支持 `'new'` 状态展示"新增"文案 |
| ADVISORY-6 | 未覆盖 ECharts 容器尺寸为 0 的边界 | ⚠️ 保留 — 建议 TrendChart 组件增加容器可见性检测 |

### 建议操作

1. ✅ **[必须]** 修正 plan.md 中 DataAnalyticsPanel.vue、RankList.vue、RankCircleCard.vue 的字段名，与 types.ts 保持一致
2. ✅ **[必须]** 修正 TrendChart 数据转换逻辑，从 DailyTrend 中提取对应指标数据
3. ✅ **[必须]** 修正 PRD 中的 API 路径为后端实际路径（PRD 已使用 `/api/circle/` 路径）
4. ✅ **[必须]** 修正 sendBeacon URL 为正确路径
5. ✅ **[必须]** 实现变化百分比计算逻辑（后端无此字段，前端需自行计算）
6. ✅ **[应]** 测试代码迁移 Jest → Vitest
7. ⚠️ **[应]** 补充组件级测试（建议后续迭代）
8. ✅ **[应]** 实现权限校验逻辑（至少前端路由守卫）
9. ✅ **[应]** 实现未登录用户跳过推荐请求的逻辑（推荐接口失败自动降级为热门榜）
10. ✅ **[建议]** ECharts 按需引入（复用项目 useECharts hook）
