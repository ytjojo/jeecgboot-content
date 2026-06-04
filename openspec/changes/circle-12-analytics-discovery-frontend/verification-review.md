## 验证报告: circle-12-analytics-discovery-frontend

### Summary

| 维度 | 状态 |
|------|------|
| Completeness | 0/36 任务完成，4 个 spec 覆盖 |
| Correctness | 5/6 后端 API 存在，1 个缺失；多处接口不一致 |
| Coherence | 文件结构遵循设计，API 路径和类型定义需修正 |

---

### CRITICAL 问题（必须在归档前修复）

#### C1: 后端缺失曝光上报接口

**问题**: `recommend-tracking/spec.md` 要求 `POST /recommend/exposure` 批量上报曝光事件，但后端 `CircleRecommendController` 中不存在该端点。

**后端现有接口**:
- `POST /api/circle/recommend/click` - 记录点击 ✅
- `POST /api/circle/recommend/join` - 记录加入转化 ✅
- `POST /api/circle/recommend/exposure` - **缺失** ❌

**影响**: 前端曝光上报功能无法对接后端。

**建议**: 在后端 `CircleRecommendController` 中新增曝光上报接口，或在 `backend-issues.md` 中记录待开发。

---

#### C2: 前端 API 路径与后端不一致

**问题**: plan.md 中前端 API 路径使用 `/content/circle/` 前缀，后端实际路径为 `/api/circle/`。

| 功能 | 前端 plan.md 路径 | 后端实际路径 |
|------|-------------------|-------------|
| 统计数据查询 | `/content/circle/analytics/{circleId}` | `/api/circle/{circleId}/data/statistics` |
| 统计数据导出 | `/content/circle/analytics/export/{circleId}` | `/api/circle/{circleId}/data/export` |
| 推荐列表 | `/content/circle/recommend` | `/api/circle/recommend` |
| 推荐点击 | `/content/circle/recommend/click` | `/api/circle/recommend/click` |
| 曝光上报 | `/content/circle/recommend/exposure` | **不存在** |
| 热门榜 | `/content/circle/ranking/hot` | `/api/circle/ranking/hot` |
| 新锐榜 | `/content/circle/ranking/new` | `/api/circle/ranking/new` |

**影响**: 前端代码中的 API 路径全部无法命中后端接口。

**建议**: 统一修正 plan.md 中的 API 路径为后端实际路径。

---

#### C3: 统计数据响应结构不匹配

**问题**: 前端 `CircleAnalyticsVO` 类型定义与后端 `CircleDataStatisticsVO` 字段完全不同。

**后端 CircleDataStatisticsVO**:
```java
memberCount, newMemberCount, postCount, newPostCount, activeCount,
List<DailyTrend> { date, newMemberCount, newPostCount, activeCount }
```

**前端 CircleAnalyticsVO (plan.md)**:
```typescript
memberTotal, memberNew, memberChange, postTotal, postNew, postChange,
activeUserCount, activeUserChange,
memberTrend[], postTrend[], activeTrend[]
```

**差异**:
- 后端没有"变化百分比"字段（`memberChange`, `postChange`, `activeUserChange`），前端需自行计算
- 后端使用 `DailyTrend` 统一趋势数组，前端拆分为 3 个独立趋势数组
- 后端没有 `memberTotal`/`postTotal` 概念，只有当前值

**影响**: 前端类型定义与后端响应不匹配，API 对接会失败。

**建议**: 修正前端类型定义，适配后端实际响应结构；变化百分比由前端根据上期数据计算。

---

#### C4: 推荐接口返回类型不一致

**问题**: 前端 plan.md 中 `getRecommendList` 返回 `CircleRecommendVO[]`（数组），但后端返回 `CircleRecommendVO`（单个对象，内含 `items` 列表）。

**后端实际结构**:
```java
CircleRecommendVO {
    List<CircleRecommendItem> items;  // 列表在 items 字段内
}
```

**前端期望**:
```typescript
CircleRecommendVO[]  // 直接是数组
```

**影响**: 前端解析推荐数据时会得到 undefined。

**建议**: 修正前端 API 返回类型为 `CircleRecommendVO`，从响应中取 `.items`。

---

### WARNING 问题（应修复）

#### W1: 推荐 Item 缺少 iconUrl 字段

**问题**: 前端 `CircleRecommendVO` 类型需要 `iconUrl` 字段展示圈子图标，后端 `CircleRecommendItem` 没有该字段。

**后端现有字段**: circleId, circleName, description, memberCount, category, privacyType, sourceId

**缺失**: iconUrl（圈子图标）

**建议**: 后端补充 iconUrl 字段，或前端使用默认图标占位。

---

#### W2: 榜单 Item 缺少 iconUrl 和 activeScore 字段

**问题**: 前端 `CircleRankVO` 需要 `iconUrl` 和 `activeScore`，后端 `CircleRankingItem` 没有这些字段。

**后端现有字段**: rank, circleId, circleName, description, memberCount, category, createTime

**缺失**: iconUrl, activeScore

**建议**: 后端补充字段，或前端调整类型定义。

---

#### W3: 推荐接口需要认证但 spec 未明确

**问题**: `CircleRecommendController.getRecommendations()` 调用 `SecureUtil.currentUser().getId()`，需要登录态。但 `circle-ranking/spec.md` 要求未登录用户也能看到热门榜。

**影响**: 未登录用户访问推荐接口会报错，需要前端做好降级处理。

**建议**: 确认推荐接口是否支持匿名访问；如不支持，前端需在未登录时跳过推荐请求直接展示热门榜。

---

### SUGGESTION 问题（建议改进）

#### S1: CSV 导出文件名格式不一致

**问题**: spec 要求文件名格式为 `{圈子名称}_{startDate}_{endDate}.csv`，后端实际生成 `circle_data_{circleId}.csv`。

**建议**: 统一文件名格式，或前端自行构造文件名。

---

#### S2: 推荐 click 接口参数名不一致

**问题**: 前端 `RecommendClickReq` 使用 `circleId` + `source`，后端 `recordClick` 使用 `sourceId`。

**建议**: 统一参数命名，确认 `sourceId` 是否等同于 `circleId`。

---

### 后端 API 验证详情

| API | 后端 Controller | 状态 | 备注 |
|-----|----------------|------|------|
| 圈子统计数据查询 | CircleDataController.getStatistics | ✅ 存在 | 路径: GET /api/circle/{circleId}/data/statistics |
| 圈子数据导出 CSV | CircleDataController.exportCsv | ✅ 存在 | 路径: GET /api/circle/{circleId}/data/export |
| 获取推荐圈子 | CircleRecommendController.getRecommendations | ✅ 存在 | 路径: GET /api/circle/recommend |
| 记录推荐点击 | CircleRecommendController.recordClick | ✅ 存在 | 路径: POST /api/circle/recommend/click |
| 推荐曝光上报 | - | ❌ 不存在 | 需后端开发 |
| 热门圈子榜单 | CircleRankingController.getHotRanking | ✅ 存在 | 路径: GET /api/circle/ranking/hot |
| 新增圈子榜单 | CircleRankingController.getNewRanking | ✅ 存在 | 路径: GET /api/circle/ranking/new |

---

### 最终评估

**3 个 CRITICAL 问题需要在归档前修复：**
1. 后端缺失曝光上报接口（C1）
2. 前端 API 路径与后端不一致（C2）
3. 统计数据和推荐数据的响应结构不匹配（C3, C4）

**建议操作：**
1. 修正 plan.md 中的 API 路径和类型定义，适配后端实际接口
2. 创建 `backend-issues.md` 记录后端待开发项（曝光上报接口、iconUrl 字段补充）
3. 修正前端 TypeScript 类型定义以匹配后端实际响应
