# 后端 API 待补充清单

> **生成时间**: 2026-06-04
> **关联 change**: `user-08-feedback-support-frontend`
> **验证来源**: `verification-review.md`

---

## 1. 总览

| 类别 | 数量 | 说明 |
|------|------|------|
| 已存在端点 | 7 | 后端控制器中已有对应 HTTP 端点 |
| ~~服务层已有但未暴露~~ | ~~5~~ → 0 | ✅ 全部已在控制器中暴露（2026-06-05） |
| ~~完全缺失（P0）~~ | ~~3~~ → 0 | ✅ 全部已实现（2026-06-05） |
| ~~完全缺失（P1）~~ | ~~5~~ → 0 | ✅ 全部已实现（2026-06-05） |
| 完全缺失（P2） | 4 | 后端未实现，可后续迭代 |

---

## 2. 已存在的后端端点（前端可直接对接）

以下端点已在后端控制器中存在，前端需调整 API 路径以匹配：

| # | HTTP 方法 | 后端路径 | 控制器 | 对应前端 API |
|---|-----------|---------|--------|-------------|
| 1 | POST | `/api/v1/content/user/support/report/create` | ContentUserSupportController | `createReport`（前端路径需含 `/create`） |
| 2 | POST | `/api/v1/content/user/support/appeal/create` | ContentUserSupportController | `createAppeal`（前端路径需含 `/create`） |
| 3 | GET | `/api/v1/content/user/support/appeal/progress` | ContentUserSupportController | 无直接对应（前端用 detail） |
| 4 | GET | `/api/v1/content/user/support/appeal/list` | ContentUserSupportController | `getAppealList`（参数需调整） |
| 5 | GET | `/api/v1/content/user/support/report/progress` | ContentUserSupportController | 无直接对应（前端用 detail） |
| 6 | GET | `/api/v1/content/user/support/help-center` | ContentUserSupportController | 前端需解析 faqCategories/guideEntries/releaseNotes |
| 7 | GET | `/api/v1/content/user/support/customer-service` | ContentUserSupportController | 获取客服入口路由信息 |

**管理端端点**（ContentUserSupportAdminController, base: `/api/v1/content/user/support/admin`）:
- `POST /appeal/handle` - 处理申诉
- `POST /report/handle` - 处理举报
- `GET /report/list` - 查询举报列表（管理端）
- `GET /report/detail` - 查询举报详情（管理端）

---

## 3. 服务层已有但未暴露 HTTP 端点

~~以下方法在 `IContentUserSupportService` 接口已定义且 `ContentUserSupportServiceImpl` 已实现，但控制器中缺少对应的 HTTP 端点。~~

**✅ 以下 5 个方法全部已在 `ContentUserSupportController` 中暴露为 HTTP 端点（2026-06-05）**

### 3.1 搜索帮助文章 ✅

| 项目 | 内容 |
|------|------|
| 服务方法 | `searchHelpArticles(String userId, String keyword)` |
| 返回类型 | `List<ContentHelpSearchResultVO>` |
| 前端需要 | `GET /api/v1/content/user/support/help/search?keyword=xxx` |
| 实现难度 | 低 -- 仅需添加控制器端点 |
| 注意事项 | 后端无分页支持，前端期望分页结果。建议后端先暴露无分页版本，前端适配 |
| 状态 | ✅ 已在控制器中暴露 |

### 3.2 查询客服会话列表 ✅

| 项目 | 内容 |
|------|------|
| 服务方法 | `listServiceSessions(ContentServiceSessionPageVO req)` |
| 返回类型 | `ContentServiceSessionPageVO`（含 records/total/pageNo/pageSize） |
| 前端需要 | `GET /api/v1/content/user/support/customer-service/sessions?pageNo=1&pageSize=20` |
| 实现难度 | 低 -- 仅需添加控制器端点 |
| 状态 | ✅ 已在控制器中暴露 |

### 3.3 创建客服会话 ✅

| 项目 | 内容 |
|------|------|
| 服务方法 | `createServiceSession(String userId, String sessionType)` |
| 返回类型 | 会话对象 |
| 前端需要 | `POST /api/v1/content/user/support/customer-service/session` |
| 实现难度 | 低 -- 仅需添加控制器端点 |
| 状态 | ✅ 已在控制器中暴露 |

### 3.4 提交服务评分 ✅

| 项目 | 内容 |
|------|------|
| 服务方法 | `rateService(String userId, String sessionId, Integer rating, String comment)` |
| 返回类型 | void |
| 前端需要 | `POST /api/v1/content/user/support/customer-service/session/{id}/rating` |
| 实现难度 | 低 -- 仅需添加控制器端点 |
| 状态 | ✅ 已在控制器中暴露 |

### 3.5 获取更新日志 ✅

| 项目 | 内容 |
|------|------|
| 服务方法 | `getChangelog(String userId)` |
| 返回类型 | 更新日志列表 |
| 前端需要 | `GET /api/v1/content/user/support/changelog/list` |
| 实现难度 | 低 -- 仅需添加控制器端点 |
| 数据结构差异 | 后端字段为 `additions/improvements/fixes`，前端期望 `features/improvements/bugfixes`，需前端做映射 |
| 状态 | ✅ 已在控制器中暴露 |

---

## 4. 完全缺失的后端功能

~~以下功能在后端服务层和控制器中均不存在，需要全新实现。~~

**✅ P0 + P1 全部已实现（2026-06-05），P2 保留待后续迭代**

### ~~P0 -- 阻塞前端开发~~ ✅

#### 4.1 用户端举报列表 ✅

| 项目 | 内容 |
|------|------|
| 前端 API | `GET /api/v1/content/user/support/report/list` |
| 功能 | 用户查询自己的举报记录，支持分页、状态筛选、类型筛选 |
| 后端现状 | 仅有管理端列表 `/admin/report/list`，用户端完全缺失 |
| 实现建议 | 新增服务方法 `listReportsForUser(userId, query)`，返回分页结果 |
| 优先级 | P0 |
| 状态 | ✅ 已实现 |

#### 4.2 用户端举报详情 ✅

| 项目 | 内容 |
|------|------|
| 前端 API | `GET /api/v1/content/user/support/report/{id}` |
| 功能 | 用户查询自己举报的详情（含处理结果） |
| 后端现状 | 仅有 `/report/progress`（返回进度）和管理端 `/admin/report/detail` |
| 实现建议 | 新增服务方法 `getReportDetailForUser(userId, reportId)` |
| 优先级 | P0 |
| 状态 | ✅ 已实现 |

#### 4.3 申诉详情 ✅

| 项目 | 内容 |
|------|------|
| 前端 API | `GET /api/v1/content/user/support/appeal/{id}` |
| 功能 | 查询申诉详情（含审核结果、审核时间） |
| 后端现状 | 仅有 `/appeal/progress`（返回进度信息），无完整详情 |
| 实现建议 | 新增服务方法 `getAppealDetail(userId, appealId)` |
| 优先级 | P0 |
| 状态 | ✅ 已实现 |

### ~~P1 -- 前端可先用 Mock 开发~~ ✅

#### 4.4 举报撤回 ✅

| 项目 | 内容 |
|------|------|
| 前端 API | `POST /api/v1/content/user/support/report/{id}/withdraw` |
| 功能 | 用户撤回待处理状态的举报 |
| 后端现状 | 完全缺失，服务层无此方法 |
| 实现建议 | 新增服务方法 `withdrawReport(userId, reportId)`，校验状态为 pending 后更新为 withdrawn |
| 优先级 | P1 |
| 状态 | ✅ 已实现 |

#### 4.5 申诉撤回 ✅

| 项目 | 内容 |
|------|------|
| 前端 API | `POST /api/v1/content/user/support/appeal/{id}/withdraw` |
| 功能 | 用户撤回审核中状态的申诉 |
| 后端现状 | 完全缺失，服务层无此方法 |
| 实现建议 | 新增服务方法 `withdrawAppeal(userId, appealId)`，校验状态为 reviewing 后更新为 withdrawn |
| 优先级 | P1 |
| 状态 | ✅ 已实现 |

#### 4.6 帮助分类列表 ✅

| 项目 | 内容 |
|------|------|
| 前端 API | `GET /api/v1/content/user/support/help/categories` |
| 功能 | 获取帮助中心分类列表（id, name, icon, articleCount） |
| 后端现状 | 完全缺失。`getHelpCenter` 返回混合结构，非独立分类接口 |
| 实现建议 | 从 `getHelpCenter` 的 `faqCategories` 拆分独立接口，或新增服务方法 |
| 优先级 | P1 |
| 状态 | ✅ 已实现 |

#### 4.7 帮助文章详情 ✅

| 项目 | 内容 |
|------|------|
| 前端 API | `GET /api/v1/content/user/support/help/article/{id}` |
| 功能 | 获取帮助文章详情（标题、Markdown 内容、分类、浏览数） |
| 后端现状 | 完全缺失，帮助中心仅有静态分类数据 |
| 实现建议 | 需要新增文章数据模型和查询接口 |
| 优先级 | P1 |
| 状态 | ✅ 已实现 |

#### 4.8 文章反馈 ✅

| 项目 | 内容 |
|------|------|
| 前端 API | `POST /api/v1/content/user/support/help/article/{id}/feedback` |
| 功能 | 提交文章有用/无用反馈 |
| 后端现状 | 完全缺失 |
| 实现建议 | 新增服务方法 `submitArticleFeedback(userId, articleId, helpful)` |
| 优先级 | P1 |
| 状态 | ✅ 已实现 |

### P2 -- 可后续迭代

#### 4.9 转人工客服

| 项目 | 内容 |
|------|------|
| 前端 API | `POST /api/v1/content/user/support/customer-service/session/{id}/transfer` |
| 功能 | 将智能客服会话转为人工客服，加入排队 |
| 后端现状 | 完全缺失 |
| 实现建议 | 需要实现排队队列逻辑，优先级规则：level>=15 或 growthValue>=400 |
| 优先级 | P2 |

#### 4.10 发送客服消息（WebSocket）

| 项目 | 内容 |
|------|------|
| 前端 API | WebSocket 消息（非 REST） |
| 功能 | 客服实时对话消息收发 |
| 后端现状 | 完全缺失，后端无 WebSocket 端点和消息协议 |
| 实现建议 | 需要新增 WebSocket 端点、消息协议定义、消息持久化 |
| 优先级 | P2 |

#### 4.11 结束客服会话

| 项目 | 内容 |
|------|------|
| 前端 API | `POST /api/v1/content/user/support/customer-service/session/{id}/close` |
| 功能 | 用户主动结束客服会话 |
| 后端现状 | 完全缺失 |
| 实现建议 | 新增服务方法 `closeServiceSession(userId, sessionId)` |
| 优先级 | P2 |

#### 4.12 会话详情含消息

| 项目 | 内容 |
|------|------|
| 前端 API | `GET /api/v1/content/user/support/customer-service/session/{id}` |
| 功能 | 查询会话详情，包含完整消息列表 |
| 后端现状 | 完全缺失 |
| 实现建议 | 新增服务方法 `getServiceSessionDetail(userId, sessionId)`，返回会话信息 + 消息列表 |
| 优先级 | P2 |

---

## 5. 数据结构差异汇总

### 5.1 帮助中心

| 字段 | 前端期望 | 后端实际 |
|------|---------|---------|
| 分类列表 | `HelpCategory[]`（id, name, icon, articleCount） | `ContentHelpCenterVO.faqCategories`（结构不同） |
| 搜索结果 | 分页 `{ records[], total }` | `List<ContentHelpSearchResultVO>`（无分页） |

### 5.2 更新日志

| 字段 | 前端期望 | 后端实际 |
|------|---------|---------|
| id | 有 | 无 |
| 新增功能字段名 | `features` | `additions` |
| 修复问题字段名 | `bugfixes` | `fixes` |

### 5.3 客服会话

| 字段 | 前端期望 | 后端实际 |
|------|---------|---------|
| 类型字段名 | `type` | `sessionType` |
| 客服名称 | `agentName` | 无 |
| 排队位置 | `queuePosition` | 无 |
| 预计等待时间 | `estimatedWaitTime` | 无 |
| 评分 | 无 | `rating` |
| 评分评论 | 无 | `ratingComment` |
| 开始时间 | 无 | `startTime` |
| 结束时间 | 无 | `endTime` |
| 是否过期 | 无 | `expired` |

### 5.4 举报列表（用户端）

| 字段 | 前端期望 | 后端实际 |
|------|---------|---------|
| 整个接口 | 用户端分页列表 | 完全缺失（仅有管理端列表） |
| 管理端字段 | - | `reportId, userId, targetType, targetId, reportType, status, resultStatus` |

---

## 6. 实施建议

### ✅ 6.1 短期 — 已完成（2026-06-05）

1. ✅ 前端调整已存在 API 的路径（`/report/create`、`/appeal/create`）
2. ✅ 前端适配 `getHelpCenter` 返回的混合结构
3. ✅ 前端对更新日志字段做映射（additions -> features, fixes -> bugfixes）
4. ✅ 对完全缺失的 API 使用 Mock 数据开发

### ✅ 6.2 中期 — 已完成（2026-06-05）

~~按优先级顺序实现：~~
1. ✅ P0: 用户端举报列表、举报详情、申诉详情 — 全部已实现
2. ✅ P1: 撤回功能、帮助中心接口、更新日志端点 — 全部已实现
3. ✅ 服务层已有方法暴露为 HTTP 端点（5个） — 全部已暴露

### 6.3 长期（后端新增功能）— 待后续迭代

1. 客服 WebSocket 基础设施
2. 排队队列和优先级逻辑
3. 消息持久化和会话管理
