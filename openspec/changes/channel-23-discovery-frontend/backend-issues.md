## 后端遗留问题清单

本文档记录 channel-23-discovery-frontend 前端 change 所依赖但后端尚未实现的 API 端点。

---

### BI-1: 发现页聚合接口 Controller 缺失（CRITICAL）

**现状**: `ContentChannelDiscoveryBiz.java` 已实现 `getDiscoveryData(userId)` 聚合逻辑，但无 Controller 暴露 HTTP 端点。

**需要补充**:
- 创建 `ContentChannelDiscoveryController`
- 映射 `GET /api/v1/content/channel/discovery/home`
- 入参: `userId`（从登录态获取或 @RequestParam）
- 返回: `Result<Map<String, Object>>` 包含 recommendations、hotRanking、editorialPicks 三个字段

**涉及文件**:
- 已有: `ContentChannelDiscoveryBiz.java`
- 需新建: `ContentChannelDiscoveryController.java`

---

### BI-2: 分类启用接口缺失（CRITICAL）

**现状**: `ContentChannelCategoryController` 仅有 `POST /api/v1/content/channel/category/disable`，无启用接口。`IContentChannelCategoryService` 仅有 `disableCategory` 方法。

**需要补充**:
- 在 `IContentChannelCategoryService` 添加 `enableCategory(String categoryId)`
- 在 `ContentChannelCategoryController` 添加 `POST /api/v1/content/channel/category/enable`
- 逻辑: 将分类状态从停用改为启用，刷新分类树缓存

**涉及文件**:
- `IContentChannelCategoryService.java` — 添加接口方法
- `ContentChannelCategoryServiceImpl.java` — 实现方法
- `ContentChannelCategoryController.java` — 添加端点

---

### BI-3: 标签编辑/更新接口缺失（CRITICAL）

**现状**: `ContentChannelTagController` 仅有 `create` 和 `delete`，无更新接口。`IContentChannelTagService` 无 `updateTag` 方法。

**需要补充**:
- 创建 `ChannelTagUpdateReq`（含 tagId 和 newName）
- 在 `IContentChannelTagService` 添加 `updateTag(ChannelTagUpdateReq req)`
- 在 `ContentChannelTagController` 添加 `POST /api/v1/content/channel/tag/update`
- 校验: 空名称、重复名称、超 20 字符

**涉及文件**:
- 需新建: `ChannelTagUpdateReq.java`
- `IContentChannelTagService.java` — 添加接口方法
- `ContentChannelTagServiceImpl.java` — 实现方法
- `ContentChannelTagController.java` — 添加端点

---

### BI-4: 搜索结果反馈接口缺失（MEDIUM）

**现状**: `ContentChannelSearchController` 仅有 `search` 查询方法。

**需要补充**:
- 在 `ContentChannelSearchController` 添加 `POST /api/v1/content/channel/search/feedback`
- 入参: `userId`、`query`（搜索关键词）、`helpful`（boolean）
- 用途: 记录用户对搜索结果的反馈，用于搜索质量优化

**涉及文件**:
- `ContentChannelSearchController.java` — 添加端点
- `IContentChannelSearchService.java` — 添加接口方法

---

### BI-5: 精选管理 admin 分页列表接口缺失（MEDIUM）

**现状**: `IContentChannelEditorialPickService` 仅有 `listActivePicks()` 返回有效精选列表，运营后台需要按状态筛选的分页列表。

**需要补充**:
- 创建 `ChannelEditorialPickQueryReq`（含 status 筛选、pageNo、pageSize）
- 在 `IContentChannelEditorialPickService` 添加 `IPage<ChannelEditorialPickVO> listPicksPage(ChannelEditorialPickQueryReq req)`
- 在 `ContentChannelEditorialPickController` 添加 `GET /api/v1/content/channel/editorial-pick/page`
- 支持状态筛选: 全部/生效中/已过期/状态异常

**涉及文件**:
- 需新建: `ChannelEditorialPickQueryReq.java`
- `IContentChannelEditorialPickService.java` — 添加接口方法
- `ContentChannelEditorialPickServiceImpl.java` — 实现方法
- `ContentChannelEditorialPickController.java` — 添加端点

---

### BI-6: BrowseController 方法体未实现（LOW）

**现状**: `ContentChannelBrowseController.browseByCategory()` 返回空 `Result.OK()`，未调用 visibilityService 过滤并返回分页数据。

**需要补充**:
- 实现 `browseByCategory` 方法体
- 调用 `IContentChannelVisibilityService` 过滤频道
- 按分类 ID 查询频道列表并分页返回

**涉及文件**:
- `ContentChannelBrowseController.java` — 补充方法实现

---

## 优先级排序

| 优先级 | 编号 | 说明 | 阻塞前端 |
|---|---|---|---|
| P0 | BI-1 | 聚合接口 Controller | 是（发现页核心功能） |
| P0 | BI-2 | 分类启用接口 | 是（分类管理功能） |
| P0 | BI-3 | 标签编辑接口 | 是（标签管理功能） |
| P1 | BI-4 | 搜索反馈接口 | 部分（可先跳过反馈功能） |
| P1 | BI-5 | 精选 admin 列表 | 部分（可先用 listActivePicks） |
| P2 | BI-6 | BrowseController 实现 | 是（分类浏览页） |
