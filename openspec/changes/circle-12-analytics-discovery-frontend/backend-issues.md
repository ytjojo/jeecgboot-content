## 后端遗留代码问题清单

> 记录时间: 2026-06-04
> 关联 Change: circle-12-analytics-discovery-frontend

---

### BI-1: 缺失曝光上报接口 [CRITICAL]

**问题描述**: 前端 `recommend-tracking` 能力需要批量上报推荐卡片曝光事件，后端 `CircleRecommendController` 中不存在对应接口。

**期望接口**:
```
POST /api/circle/recommend/exposure
Content-Type: application/json

Request Body:
{
  "circleIds": ["id1", "id2", ...],
  "source": "recommend"
}
```

**现有后端代码位置**:
- Controller: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/controller/CircleRecommendController.java`
- Service: `ICircleRecommendService`

**建议实现**:
1. 在 `CircleRecommendController` 中新增 `recordExposure` 方法
2. 参数使用 `@RequestBody` 接收 `RecommendExposureReq`（需新建 DTO，包含 `circleIds` 和 `source`）
3. 在 `ICircleRecommendService` 中新增 `recordExposure` 方法
4. Service 实现中批量写入曝光记录

**优先级**: 高 — 阻塞前端曝光上报功能

---

### BI-2: 推荐和榜单 Item 缺少 iconUrl 字段 [WARNING]

**问题描述**: 前端需要展示圈子图标，但后端 `CircleRecommendItem` 和 `CircleRankingItem` 均未返回 `iconUrl` 字段。

**涉及 VO**:
- `CircleRecommendVO.CircleRecommendItem` — 缺少 `iconUrl`
- `CircleRankingVO.CircleRankingItem` — 缺少 `iconUrl`

**建议**:
- 方案 A: 后端补充 `iconUrl` 字段，从圈子表关联查询
- 方案 B: 前端使用默认图标占位，后续迭代补充

**优先级**: 中 — 不阻塞核心功能，但影响视觉体验

---

### BI-3: 榜单 Item 缺少 activeScore 字段 [SUGGESTION]

**问题描述**: 前端 `CircleRankVO` 类型定义中包含 `activeScore`（活跃度分数），后端 `CircleRankingItem` 未返回该字段。

**建议**: 后端在热门榜排序时可附带活跃度分数，便于前端展示或后续扩展。

**优先级**: 低 — 当前 spec 未要求展示活跃度分数

---

### BI-4: 统计数据无变化百分比字段 [WARNING]

**问题描述**: 前端 spec 要求展示"与上期对比的变化百分比"，后端 `CircleDataStatisticsVO` 仅返回当前值，无上期数据或变化百分比。

**涉及字段**: `memberChange`, `postChange`, `activeUserChange`

**建议**:
- 方案 A: 后端在 VO 中增加 `previousMemberCount`, `previousPostCount`, `previousActiveCount` 字段，前端自行计算百分比
- 方案 B: 前端通过两次请求（本期 + 上期）自行计算
- 方案 C: 后端直接返回变化百分比字段

**优先级**: 中 — 核心指标卡片需要该数据

---

### BI-5: CSV 导出文件名格式不一致 [SUGGESTION]

**问题描述**: spec 要求文件名格式为 `{圈子名称}_{startDate}_{endDate}.csv`，后端实际生成 `circle_data_{circleId}.csv`。

**建议**: 后端修改 `Content-Disposition` header 中的文件名格式，或前端自行构造文件名通过 Blob 下载。

**优先级**: 低

---

### BI-6: 推荐接口需登录但未登录用户也需热门榜 [WARNING]

**问题描述**: `CircleRecommendController.getRecommendations()` 内部调用 `SecureUtil.currentUser().getId()`，需要登录态。但 spec 要求未登录用户进入圈子列表页时默认展示热门榜。

**影响**: 未登录用户访问推荐接口会报错。

**建议**: 前端在未登录时跳过推荐接口请求，直接请求热门榜接口（`GET /api/circle/ranking/hot` 无认证要求则可直接使用）。需确认热门榜接口是否支持匿名访问。

**优先级**: 中
