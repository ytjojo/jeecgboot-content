## Verification Report: channel-23-discovery-frontend

### Summary

| Dimension | Status |
|-----------|--------|
| Completeness | 0/54 tasks complete, 8/8 spec files present |
| Correctness | 6 API 路径不一致或缺失，2 个后端功能缺失 |
| Coherence | 设计决策基本合理，存在若干 API 路径偏差 |

### 后端 API 验证详情

#### 已存在且一致的 API（13 个）

| 前端文档引用 | 后端实际端点 | 状态 |
|---|---|---|
| `/content/channel/browse/category` | `GET /content/channel/browse/category` (ContentChannelBrowseController) | 一致 |
| `/content/channel/recommendation/list` | `GET /content/channel/recommendation/list` (ContentChannelRecommendationController) | 一致 |
| `/content/channel/recommendation/cold-start` | `GET /content/channel/recommendation/cold-start` | 一致 |
| `/content/channel/recommendation/not-interested` | `POST /content/channel/recommendation/not-interested` | 一致 |
| `/content/channel/ranking/hot` | `GET /content/channel/ranking/hot` | 一致 |
| `/content/channel/ranking/new` | `GET /content/channel/ranking/new` | 一致 |
| `/content/channel/ranking/system` | `GET /content/channel/ranking/system` | 一致 |
| `/content/channel/category/tree` | `GET /content/channel/category/tree` | 一致 |
| `/content/channel/category/create` | `POST /content/channel/category/create` | 一致 |
| `/content/channel/category/update` | `POST /content/channel/category/update` | 一致 |
| `/content/channel/category/disable` | `POST /content/channel/category/disable` | 一致 |
| `/content/channel/editorial-pick/list` | `GET /content/channel/editorial-pick/list` | 一致 |
| `/content/channel/tag/list` | `GET /content/channel/tag/list` | 一致 |

#### 路径不一致的 API（3 个）

| 前端文档引用 | 后端实际端点 | 问题 |
|---|---|---|
| `/content/channel/discovery/home` (聚合接口) | 无对应 Controller，仅 `ContentChannelDiscoveryBiz` 业务类存在 | **缺失 Controller**：Biz 类已有聚合逻辑，但未暴露 HTTP 端点 |
| `/content/channel/search` (design.md 引用) | `GET /content/channel/search/query` | **路径不一致**：前端文档引用 `/content/channel/search`，后端实际为 `/content/channel/search/query` |
| `/content/channel/ranking/list` (tasks.md 7.2 引用) | 分别为 `/content/channel/ranking/hot`、`/new`、`/system` | **路径不一致**：前端引用单一 list 接口，后端为三个独立端点 |

#### 缺失的 API（3 个）

| 前端文档/需求引用 | 后端状态 | 说明 |
|---|---|---|
| 分类启用接口 (spec: channel-category-manage "启用分类") | `ContentChannelCategoryController` 仅有 `disableCategory`，无 `enableCategory` | **缺失**：specs 要求支持启用已停用分类 |
| 标签编辑/更新接口 (spec: channel-tag-manage "编辑标签") | `ContentChannelTagController` 仅有 `create` 和 `delete`，无 `update` | **缺失**：specs 要求支持 inline edit 编辑标签名称 |
| 搜索结果反馈接口 (spec: channel-search "结果有帮助") | `ContentChannelSearchController` 仅有 `search` 查询，无反馈端点 | **缺失**：specs 要求"结果有帮助"反馈功能 |
| 精选管理分页列表接口 (admin 端) | `IContentChannelEditorialPickService` 仅有 `listActivePicks()`，无分页/状态筛选方法 | **缺失**：admin 页面需要按状态筛选的分页列表 |

#### 已确认存在的后端服务

- `ChannelVisibilityService` / `IContentChannelVisibilityService` — 已存在，负责频道可见性过滤

---

## CRITICAL Issues

### ~~C1: 聚合接口 `/content/channel/discovery/home` 缺少 Controller 端点~~ [已解决]
- **状态**: ✅ 已实现
- **位置**: `ContentChannelDiscoveryController.java:24` — `@GetMapping("/home")`

### ~~C2: 分类启用接口缺失~~ [已解决]
- **状态**: ✅ 已实现
- **位置**: `ContentChannelCategoryController.java:53` — `enableCategory(@RequestParam String categoryId)`

### ~~C3: 标签编辑/更新接口缺失~~ [已解决]
- **状态**: ✅ 已实现
- **位置**: `ContentChannelTagController.java:37` — `@PostMapping("/update")`

### ~~C4: 搜索结果反馈接口缺失~~ [已解决]
- **状态**: ✅ 已实现
- **位置**: `ContentChannelSearchController.java:35` — `@PostMapping("/feedback")`

### C5: 精选管理 admin 分页列表接口缺失 [仍未解决]
- **影响**: 运营后台精选管理页无法按状态筛选展示全部精选
- **位置**: `IContentChannelEditorialPickService` 仅有 `listActivePicks()`
- **建议**: 添加 `IPage<ChannelEditorialPickVO> listPicksPage(ChannelEditorialPickQueryReq req)` 支持状态筛选和分页

### C6: 所有 54 个实现任务均未完成
- **影响**: 前端代码尚未开始实现
- **建议**: 按 tasks.md 中的任务优先级逐步实现

---

## WARNING Issues

### W1: 搜索 API 路径不一致 [已修复]
- **位置**: specs/channel-search/spec.md 已修正为 `GET /content/channel/search/query`

### W2: 排行榜 API 路径不一致 [已修复]
- **位置**: tasks.md 7.2 已修正为分别引用 `/content/channel/ranking/hot`、`/new`、`/system`

### W3: 推荐接口需要 userId 参数
- **位置**: `ContentChannelRecommendationController.java:24` — `getRecommendations` 需要 `@RequestParam String userId`
- **说明**: 前端需从 useUserStore 获取当前用户 ID 传递给推荐和搜索接口
- **建议**: 在 design.md 中明确说明 userId 参数来源

### W4: 搜索接口也需要 userId 参数
- **位置**: `ContentChannelSearchController.java:24` — `search` 需要 `@RequestParam String userId`
- **建议**: 同 W3，在前端 API 封装中自动注入 userId

### W5: 排行榜查询参数结构未明确
- **位置**: `ContentChannelRankingController.java` 使用 `ChannelRankingQueryReq`
- **说明**: 前端文档未明确说明 `dimension` 参数的枚举值（DAILY/WEEKLY/MONTHLY）
- **建议**: 在 specs/channel-ranking/spec.md 中补充维度参数的枚举值说明

---

## SUGGESTION Issues

### S1: BrowseController 返回空 Result
- **位置**: `ContentChannelBrowseController.java:24` — `return Result.OK()` 未返回实际数据
- **建议**: 实现 `browseByCategory` 方法体，调用 visibilityService 过滤并返回分页数据

### S2: 聚合接口降级策略需前端实现
- **说明**: design.md 决策 3 提到聚合接口失败时降级为并行调用子接口，但后端无聚合 Controller
- **建议**: 前端实现降级逻辑时，直接并行调用 recommendation/list、ranking/hot、editorial-pick/list

### S3: 标签删除接口使用 POST 而非 DELETE
- **位置**: `ContentChannelTagController.java:39` — `@PostMapping("/delete")`
- **说明**: RESTful 规范建议使用 DELETE 方法，但项目整体风格使用 POST，保持一致即可

---

## 建议修复方案

### 优先级 1（阻塞前端开发）— 更新于 2026-06-07
1. ~~创建 `ContentChannelDiscoveryController` 暴露聚合接口~~ ✅ 已完成
2. ~~添加分类启用接口 `enableCategory`~~ ✅ 已完成
3. ~~添加标签更新接口 `updateTag`~~ ✅ 已完成
4. 修正 design.md 和前端 PRD 中的 API 路径（search 和 ranking）

### 优先级 2（补充功能完整性）
5. ~~添加搜索反馈接口~~ ✅ 已完成
6. 添加精选管理 admin 分页列表接口（BI-5，仍未解决）
7. 在 specs 中补充 API 参数说明（userId、dimension 枚举）

### 优先级 3（代码质量）
8. 实现 BrowseController 方法体（BI-6，仍未解决）
9. 统一 API 路径命名风格

---

**Final Assessment**: 6 个 CRITICAL 问题中，C1-C4 已全部解决（聚合接口、分类启用、标签更新、搜索反馈均已实现），C5（精选 admin 分页）仍未解决但不阻塞核心功能。C6（所有任务未完成）仍有效。剩余后端待办：BI-5（精选 admin 分页列表）和 BI-6（BrowseController 实现）。
