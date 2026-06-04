# 验证审核文档

> **验证时间**: 2026-06-04
> **验证范围**: `user-08-feedback-support-frontend` change 目录
> **验证目标**: 后端 API 存在性、前后端接口一致性、文档完整性

---

## 1. 验证结果摘要

| 维度 | 状态 | 说明 |
|------|------|------|
| 后端 API 存在性 | 部分通过 | 7/21 个前端所需 API 在后端控制器中存在 |
| 前后端接口一致性 | 不通过 | API 路径、参数、响应结构均存在差异 |
| 文档完整性 | 不通过 | plan.md 中的 API 路径与后端实际路径不一致 |

**总体结论**: 前端 plan.md 中定义的 21 个 API 端点，仅有 7 个在后端控制器中有对应的 HTTP 端点。其余 14 个需要后端补充实现，或前端调整 API 路径以对接已有端点。

---

## 2. 后端 API 验证详情

### 2.1 已存在的后端端点

**用户端控制器** (`ContentUserSupportController`, base: `/content/user/support`):

| # | HTTP 方法 | 路径 | 功能 | 对应前端 API |
|---|-----------|------|------|-------------|
| 1 | POST | `/appeal/create` | 创建申诉 | `createAppeal` |
| 2 | POST | `/report/create` | 创建举报 | `createReport` |
| 3 | GET | `/appeal/progress` | 查询申诉进度 | 无直接对应（前端用 detail） |
| 4 | GET | `/appeal/list` | 查询申诉列表 | `getAppealList` |
| 5 | GET | `/report/progress` | 查询举报进度 | 无直接对应（前端用 detail） |
| 6 | GET | `/help-center` | 查询帮助中心 | 无直接对应（前端拆分为 categories + search） |
| 7 | GET | `/customer-service` | 查询客服入口 | 无直接对应（前端用 createSession） |

**管理端控制器** (`ContentUserSupportAdminController`, base: `/content/user/support/admin`):
- `POST /appeal/handle` - 处理申诉
- `POST /report/handle` - 处理举报
- `GET /report/list` - 查询举报列表（管理端）
- `GET /report/detail` - 查询举报详情（管理端）

### 2.2 服务层已实现但未暴露为 HTTP 端点的方法

以下方法在 `IContentUserSupportService` 接口中已定义且 `ContentUserSupportServiceImpl` 已实现，但**控制器中缺少对应的 HTTP 端点**：

| # | 服务方法 | 功能 | 前端需要 |
|---|----------|------|---------|
| 1 | `searchHelpArticles(userId, keyword)` | 搜索帮助文章 | 是 - `searchHelpArticles` |
| 2 | `listServiceSessions(req)` | 查询客服会话列表 | 是 - `getServiceSessionList` |
| 3 | `createServiceSession(userId, sessionType)` | 创建客服会话 | 是 - `createServiceSession` |
| 4 | `rateService(userId, sessionId, rating, comment)` | 提交服务评分 | 是 - `submitServiceRating` |
| 5 | `getChangelog(userId)` | 获取更新日志 | 是 - `getChangelogList` |

### 2.3 完全缺失的后端功能

以下前端 API 所需的功能在后端服务层和控制器中均不存在：

| # | 前端 API | 功能 | 后端状态 |
|---|----------|------|---------|
| 1 | `withdrawReport` | 撤回举报 | 完全缺失 |
| 2 | `getReportList` (用户端) | 用户查询自己的举报列表 | 完全缺失（仅有管理端列表） |
| 3 | `getReportDetail` (用户端) | 用户查询举报详情 | 完全缺失（仅有 progress 和管理端 detail） |
| 4 | `withdrawAppeal` | 撤回申诉 | 完全缺失 |
| 5 | `getAppealDetail` | 查询申诉详情 | 完全缺失（仅有 progress） |
| 6 | `getHelpCategories` | 获取帮助分类列表 | 完全缺失（help-center 返回的是混合结构） |
| 7 | `getHelpArticleDetail` | 获取帮助文章详情 | 完全缺失 |
| 8 | `submitArticleFeedback` | 提交文章有用/无用反馈 | 完全缺失 |
| 9 | `transferToHuman` | 转人工客服 | 完全缺失 |
| 10 | `sendChatMessage` | 发送客服消息 | 完全缺失（WebSocket） |
| 11 | `closeServiceSession` | 结束客服会话 | 完全缺失 |
| 12 | `getServiceSessionDetail` | 查询会话详情（含消息） | 完全缺失 |

---

## 3. 前后端接口差异详情

### 3.1 API 路径差异

| 前端 plan.md 路径 | 后端实际路径 | 差异说明 |
|-------------------|-------------|---------|
| `POST /content/user/support/report` | `POST /content/user/support/report/create` | 路径不同 |
| `POST /content/user/support/report/{id}/withdraw` | 不存在 | 完全缺失 |
| `GET /content/user/support/report/list` | 不存在（用户端） | 完全缺失 |
| `GET /content/user/support/report/{id}` | 不存在（用户端） | 完全缺失 |
| `POST /content/user/support/appeal` | `POST /content/user/support/appeal/create` | 路径不同 |
| `POST /content/user/support/appeal/{id}/withdraw` | 不存在 | 完全缺失 |
| `GET /content/user/support/appeal/list` | `GET /content/user/support/appeal/list` | 一致 |
| `GET /content/user/support/appeal/{id}` | 不存在（仅有 progress） | 完全缺失 |
| `GET /content/user/support/help/search` | 不存在（服务层有方法） | 控制器未暴露 |
| `GET /content/user/support/help/categories` | 不存在 | 完全缺失 |
| `GET /content/user/support/help/article/{id}` | 不存在 | 完全缺失 |
| `POST /content/user/support/help/article/{id}/feedback` | 不存在 | 完全缺失 |
| `GET /content/user/support/changelog/list` | 不存在（服务层有方法） | 控制器未暴露 |
| `POST /content/user/support/customer-service/session` | 不存在（服务层有方法） | 控制器未暴露 |
| `POST .../session/{id}/transfer` | 不存在 | 完全缺失 |
| `POST .../session/{id}/message` | 不存在 | 完全缺失（WebSocket） |
| `POST .../session/{id}/close` | 不存在 | 完全缺失 |
| `POST .../session/{id}/rating` | 不存在（服务层有方法） | 控制器未暴露 |
| `GET /content/user/support/customer-service/sessions` | 不存在（服务层有方法） | 控制器未暴露 |
| `GET .../session/{id}` | 不存在 | 完全缺失 |

### 3.2 数据结构差异

**帮助中心**:
- 前端期望: `getHelpCategories()` 返回 `HelpCategory[]`（含 id, name, icon, articleCount）
- 后端实际: `getHelpCenter()` 返回 `ContentHelpCenterVO`（含 faqCategories, guideEntries, releaseNotes）
- 差异: 结构完全不同，前端需要拆分对接

**帮助搜索**:
- 前端期望: `searchHelpArticles({ keyword, pageNo, pageSize })` 返回分页结果
- 后端实际: `searchHelpArticles(userId, keyword)` 返回 `List<ContentHelpSearchResultVO>`（无分页）
- 差异: 参数和返回结构不同

**更新日志**:
- 前端期望: 返回 `{ id, version, releaseDate, features[], improvements[], bugfixes[] }`
- 后端实际: 返回 `{ version, releaseDate, additions[], improvements[], fixes[] }`（无 id，字段名不同）
- 差异: `features` vs `additions`，`bugfixes` vs `fixes`，缺少 `id`

**客服会话**:
- 前端期望: `ServiceSession` 含 `type, status, agentName, queuePosition, estimatedWaitTime`
- 后端实际: `ContentServiceSessionVO` 含 `sessionType, status, rating, ratingComment, startTime, endTime, expired`
- 差异: 字段完全不同，后端无 `agentName`, `queuePosition`, `estimatedWaitTime`

**举报列表**:
- 前端期望: 用户端分页列表，含 `reportNo, targetSummary, reportTypeLabel, statusLabel`
- 后端实际: 仅有管理端列表（`ContentUserReportAdminPageVO`），字段为 `reportId, userId, targetType, targetId, reportType, status, resultStatus`
- 差异: 用户端列表完全缺失，管理端字段也不匹配

---

## 4. 前端文档问题列表

### 4.1 plan.md 问题

| # | 问题 | 严重程度 | 说明 |
|---|------|---------|------|
| P1 | API 路径与后端不一致 | 高 | 创建举报/申诉的路径多了一级 `/create` |
| P2 | 引用了不存在的 API | 高 | 14 个 API 端点在后端不存在 |
| P3 | 数据结构定义与后端不匹配 | 高 | 帮助中心、更新日志、客服会话的字段名/结构不同 |
| P4 | 缺少撤回功能的后端说明 | 中 | 撤回举报/申诉在后端完全未实现 |
| P5 | WebSocket 通信方案未明确 | 中 | 仅提到"复用现有 WebSocket 基础设施"，但未说明具体地址和协议 |
| P6 | 帮助文章详情 API 未定义 | 中 | 后端帮助中心仅有静态分类数据，无文章详情 |

### 4.2 design.md 问题

| # | 问题 | 严重程度 | 说明 |
|---|------|---------|------|
| D1 | Open Questions 未更新 | 低 | Q1-Q7 的假设状态未标记为已验证或已变更 |
| D2 | 客服优先级逻辑描述不完整 | 低 | 后端实际逻辑是 level>=15 或 growthValue>=400，文档仅提 LV.15+ |

### 4.3 proposal.md 问题

| # | 问题 | 严重程度 | 说明 |
|---|------|---------|------|
| R1 | "21个 API 接口" 数量需核实 | 低 | 实际前端 plan.md 定义了 21 个，但后端仅暴露 7 个 |

### 4.4 specs 问题

| # | 文件 | 问题 | 说明 |
|---|------|------|------|
| S1 | report-system/spec.md | 未引用后端 API 路径 | spec 描述了行为，但未与后端 API 对齐 |
| S2 | appeal-system/spec.md | 撤回功能后端未实现 | spec 要求撤回功能，但后端无此端点 |
| S3 | help-center/spec.md | 帮助中心结构与后端不匹配 | spec 描述的分类/搜索/文章详情与后端静态数据模型不同 |
| S4 | customer-service/spec.md | WebSocket 细节未明确 | spec 描述了断连重连等行为，但 WebSocket 协议未定义 |
| S5 | changelog/spec.md | 字段名与后端不一致 | spec 用 features/bugfixes，后端用 additions/fixes |

---

## 5. 建议修复方案

### 5.1 短期方案（前端适配已有后端）

调整前端 API 层以对接已有的后端端点：

1. **举报创建**: 改路径为 `POST /content/user/support/report/create`
2. **申诉创建**: 改路径为 `POST /content/user/support/appeal/create`
3. **申诉列表**: 路径已一致，但参数需调整（后端用 `userId` 参数）
4. **帮助中心**: 改为调用 `GET /content/user/support/help-center`，前端解析 `faqCategories`/`guideEntries`/`releaseNotes`
5. **客服入口**: 改为调用 `GET /content/user/support/customer-service` 获取路由信息
6. **更新日志**: 后端已有服务方法，需添加控制器端点后对接
7. **客服会话列表/创建/评分**: 后端已有服务方法，需添加控制器端点后对接

### 5.2 中期方案（后端补充端点）

需要后端补充的控制器端点（服务层已实现，仅需暴露 HTTP 接口）：

1. `GET /content/user/support/help/search?keyword=xxx` - 搜索帮助文章
2. `GET /content/user/support/changelog/list` - 更新日志列表
3. `POST /content/user/support/customer-service/session` - 创建客服会话
4. `GET /content/user/support/customer-service/sessions` - 客服会话历史列表
5. `POST /content/user/support/customer-service/session/{id}/rating` - 提交服务评分

### 5.3 长期方案（后端新增功能）

需要后端全新实现的功能：

1. **举报撤回** (`POST /report/{id}/withdraw`) - 需新增服务方法和控制器端点
2. **用户端举报列表** (`GET /report/list`) - 需新增用户端列表查询（区别于管理端）
3. **用户端举报详情** (`GET /report/{id}`) - 需新增用户端详情查询
4. **申诉撤回** (`POST /appeal/{id}/withdraw`) - 需新增服务方法和控制器端点
5. **申诉详情** (`GET /appeal/{id}`) - 需新增详情查询（区别于 progress）
6. **帮助分类列表** (`GET /help/categories`) - 需新增独立分类接口
7. **帮助文章详情** (`GET /help/article/{id}`) - 需新增文章详情接口
8. **文章反馈** (`POST /help/article/{id}/feedback`) - 需新增反馈接口
9. **转人工客服** (`POST /customer-service/session/{id}/transfer`) - 需新增转接逻辑
10. **发送消息** (`POST /customer-service/session/{id}/message`) - 需 WebSocket 实现
11. **结束会话** (`POST /customer-service/session/{id}/close`) - 需新增关闭逻辑
12. **会话详情** (`GET /customer-service/session/{id}`) - 需新增含消息的详情查询

### 5.4 文档修复建议

1. **plan.md**: 更新所有 API 路径以匹配后端实际路径，或标注"待后端实现"
2. **plan.md**: 更新数据结构定义以匹配后端 VO 类
3. **design.md**: 更新 Open Questions 状态
4. **specs**: 在各 spec 中添加"后端依赖"章节，标明哪些 API 已存在、哪些需要新增
5. **proposal.md**: 更新 API 数量和依赖说明

---

## 6. 后端 API 优先级建议

### P0 - 阻塞前端开发（必须先实现）
1. 用户端举报列表 (`GET /report/list` for user)
2. 用户端举报详情 (`GET /report/{id}` for user)
3. 申诉详情 (`GET /appeal/{id}`)
4. 客服会话创建 (`POST /customer-service/session`)
5. 客服会话列表 (`GET /customer-service/sessions`)

### P1 - 前端可先用 Mock 开发
1. 举报撤回 (`POST /report/{id}/withdraw`)
2. 申诉撤回 (`POST /appeal/{id}/withdraw`)
3. 帮助搜索 (`GET /help/search`)
4. 帮助分类 (`GET /help/categories`)
5. 帮助文章详情 (`GET /help/article/{id}`)
6. 文章反馈 (`POST /help/article/{id}/feedback`)
7. 更新日志 (`GET /changelog/list`)
8. 服务评分 (`POST /customer-service/session/{id}/rating`)

### P2 - 可后续迭代
1. 转人工客服 (`POST /customer-service/session/{id}/transfer`)
2. 发送消息 (WebSocket)
3. 结束会话 (`POST /customer-service/session/{id}/close`)
4. 会话详情含消息 (`GET /customer-service/session/{id}`)
