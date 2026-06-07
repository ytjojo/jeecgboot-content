# 验证审核文档: channel-22-content-governance-frontend

**验证日期**: 2026-06-04
**Schema**: use-tdd-plan
**验证范围**: 完整性、正确性、一致性

---

## 验证结果摘要

| 维度 | 状态 | 详情 |
|------|------|------|
| 完整性 | 0/40+ 任务完成 | 所有任务均未开始实现 |
| 正确性 | 5 组 API 路径存在重大偏差 | spec 引用路径与实际后端路径不一致 |
| 一致性 | 设计文档与后端实现存在 6 处不一致 | API 架构模式差异、缺失端点 |

---

## 1. 任务完整性

tasks.md 中 12 大类共 40+ 子任务，**全部为 `- [ ]` 未完成状态**。

| 任务组 | 总数 | 完成 | 状态 |
|--------|------|------|------|
| 1. API 层封装 | 5 | 0 | 未开始 |
| 2. Store 层实现 | 6 | 0 | 未开始 |
| 3. 频道选择与发布组件 | 5 | 0 | 未开始 |
| 4. 发布权限与限额配置 | 2 | 0 | 未开始 |
| 5. 待审区管理 | 4 | 0 | 未开始 |
| 6. 内容治理 | 8 | 0 | 未开始 |
| 7. 回收站与治理日志 | 4 | 0 | 未开始 |
| 8. 频道公告管理 | 2 | 0 | 未开始 |
| 9. 添加已发布内容到频道 | 2 | 0 | 未开始 |
| 10. 治理后台容器与路由 | 2 | 0 | 未开始 |
| 11. 埋点集成 | 4 | 0 | 未开始 |
| 12. 验证 | 5 | 0 | 未开始 |

---

## 2. 后端 API 验证详情

### 2.1 已存在的后端控制器

| 控制器 | 路径前缀 | 文件 |
|--------|----------|------|
| ChannelPublishController | `/api/v1/content/channel/publish` | `controller/ChannelPublishController.java` |
| ChannelContentReviewController | `/api/v1/content/channel/review` | `controller/ChannelContentReviewController.java` |
| ChannelReviewController | `/api/v1/content/channel/review` | `controller/ChannelReviewController.java` |
| ChannelContentGovernanceController | `/api/v1/content/channel/governance` | `controller/ChannelContentGovernanceController.java` |
| ChannelGovernanceController | `/channel/governance` | `controller/ChannelGovernanceController.java` |
| ChannelAnnouncementController | `/api/v1/content/channel/announcement` | `controller/ChannelAnnouncementController.java` |

### 2.2 API 路径偏差对照表

#### 发布模块

| spec 引用路径 | 实际后端路径 | 状态 |
|---------------|-------------|------|
| `/api/channel/publish/available` | **不存在** | 缺失 - 无获取可发布频道列表的端点 |
| `/api/channel/publish/submit` | `POST /api/v1/content/channel/publish` | 路径偏差 |
| `/api/channel/publish/result` | **不存在** | 缺失 - 无获取发布结果的独立端点 |
| `/api/channel/publish/scheduled/{id}` PUT | **不存在** | 缺失 - 定时发布 CRUD 无独立端点 |
| `/api/channel/publish/scheduled/{id}` DELETE | **不存在** | 缺失 |
| `/api/channel/publish/limit/check` | **不存在** | 缺失 - 限额预校验无端点 |

#### 审核模块

| spec 引用路径 | 实际后端路径 | 状态 |
|---------------|-------------|------|
| `/api/channel/review/list` | `GET /api/v1/content/channel/review/list` | 路径偏差（两处） |
| `/api/channel/review/approve` | `POST /api/v1/content/channel/review` (action=APPROVE) | 架构差异 |
| `/api/channel/review/reject` | `POST /api/v1/content/channel/review` (action=REJECT) | 架构差异 |
| `/api/channel/review/stats` | **不存在** | 缺失 - 无审核统计端点 |

**架构差异说明**: spec 设计为独立的 approve/reject 端点，实际后端使用统一的 `POST /api/v1/content/channel/review` 端点 + `action` 字段区分操作。另外存在两个审核控制器：
- `ChannelContentReviewController` (`/api/v1/content/channel/review`) - 内容发布审核
- `ChannelReviewController` (`/api/v1/content/channel/review`) - 频道创建审核

前端 spec 中的"待审区"应使用 `ChannelReviewController` 的 `GET /list` 端点。

#### 治理模块

| spec 引用路径 | 实际后端路径 | 状态 |
|---------------|-------------|------|
| `/api/channel/governance/content/list` | **不存在** | 缺失 - 无频道内容列表端点 |
| `/api/channel/governance/pin` | `POST /api/v1/content/channel/governance` (action=PIN) | 架构差异 |
| `/api/channel/governance/feature` | `POST /api/v1/content/channel/governance` (action=FEATURE) | 架构差异 |
| `/api/channel/governance/delete` | `POST /api/v1/content/channel/governance` (action=DELETE) | 架构差异 |
| `/api/channel/governance/move` | `POST /api/v1/content/channel/governance` (action=MOVE) | 架构差异 |
| `/api/channel/governance/edit-assist` | `POST /api/v1/content/channel/governance` (action=EDIT_ASSIST) | 架构差异 |
| `/api/channel/governance/recycle-bin/list` | **不存在** | 缺失 - 回收站列表查询无端点 |
| `/api/channel/governance/recycle-bin/restore` | `POST /api/v1/content/channel/governance` (action=RESTORE) | 架构差异 |
| `/api/channel/governance/log/list` | **不存在** | 缺失 - 治理日志查询无端点 |
| `/api/channel/governance/edit-assist/history/{contentId}` | **不存在** | 缺失 - 编辑协助历史查询无端点 |

**架构差异说明**: 实际后端将所有治理操作（PIN/UNPIN/FEATURE/UNFEATURE/DELETE/RESTORE/MOVE/EDIT_ASSIST）统一到 `POST /api/v1/content/channel/governance` 一个端点，通过 `ChannelGovernanceReq.action` 字段区分。spec 设计为多个独立端点。

#### 公告模块

| spec 引用路径 | 实际后端路径 | 状态 |
|---------------|-------------|------|
| `GET /api/channel/announcement/{channelId}` | `GET /api/v1/content/channel/announcement/channel/{channelId}` | 路径偏差 |
| `POST /api/channel/announcement` | `POST /api/v1/content/channel/announcement` | 路径偏差 |
| `PUT /api/channel/announcement/{id}` | `PUT /api/v1/content/channel/announcement/{id}` | 路径偏差 |
| `DELETE /api/channel/announcement/{id}` | `DELETE /api/v1/content/channel/announcement/{id}` | 路径偏差 |
| `/api/channel/announcement/preview` | **不存在** | 缺失 - 公告预览/安全过滤无端点 |
| `/api/channel/announcement/{channelId}/history` | **不存在** | 缺失 - 公告历史版本查询无端点 |
| `/api/channel/announcement/restore/{versionId}` | **不存在** | 缺失 - 历史版本恢复无端点 |

#### 添加内容模块

| spec 引用路径 | 实际后端路径 | 状态 |
|---------------|-------------|------|
| `/api/channel/content/add/search` | **不存在** | 缺失 - 内容搜索无端点 |
| `/api/channel/content/add` | `POST /api/v1/content/channel/publish/add-existing` | 路径偏差 |

### 2.3 缺失的后端端点汇总

以下端点在 spec 中引用但后端**完全未实现**：

1. **`GET /publish/available`** - 获取用户可发布的频道列表
2. **`GET /publish/result`** - 获取发布结果
3. **定时发布 CRUD** - 创建/编辑/取消定时发布任务的独立端点
4. **`GET /publish/limit/check`** - 发布限额预校验
5. **`GET /review/stats`** - 审核统计（待审总数、超时数）
6. **`GET /governance/content/list`** - 频道内容列表（带筛选排序分页）
7. **`GET /governance/recycle-bin/list`** - 回收站列表
8. **`GET /governance/log/list`** - 治理日志列表
9. **`GET /governance/edit-assist/history/{contentId}`** - 编辑协助历史
10. **`POST /announcement/preview`** - 公告预览安全过滤
11. **`GET /announcement/{channelId}/history`** - 公告历史版本
12. **`POST /announcement/restore/{versionId}`** - 历史版本恢复
13. **`GET /content/add/search`** - 可添加内容搜索

---

## 3. 前端文档问题列表

### 3.1 CRITICAL - API 路径全局偏差

所有 spec 文件中的 API 路径使用 `/api/channel/...` 格式，但实际后端路径为 `/api/v1/content/channel/...`。这是系统性偏差，需要统一修正。

**影响文件**:
- `specs/channel-publishing-ui/spec.md` - 7 处路径引用
- `specs/channel-content-moderation-ui/spec.md` - 4 处路径引用
- `specs/channel-content-governance-ui/spec.md` - 10 处路径引用
- `specs/channel-announcements-ui/spec.md` - 6 处路径引用
- `specs/channel-add-existing-content-ui/spec.md` - 2 处路径引用

### 3.2 CRITICAL - API 架构模式差异

spec 设计为 RESTful 风格的独立端点，实际后端使用统一入口 + action 字段模式。

**治理操作**: spec 设计了 `/pin`、`/feature`、`/delete`、`/move`、`/edit-assist` 等独立端点，实际统一为 `POST /api/v1/content/channel/governance` + `action` 字段。

**审核操作**: spec 设计了 `/approve`、`/reject` 独立端点，实际统一为 `POST /api/v1/content/channel/review` + `action` 字段。

### 3.3 CRITICAL - 13 个后端端点缺失

spec 中引用的 13 个端点在后端完全不存在（详见 2.3 节）。前端无法实现以下功能：
- 可发布频道列表查询
- 定时发布管理
- 发布限额预校验
- 审核统计
- 频道内容列表（带筛选排序）
- 回收站列表查询
- 治理日志查询
- 编辑协助历史查询
- 公告预览/历史版本/版本恢复
- 可添加内容搜索

### 3.4 WARNING - 审核控制器职责混淆

存在两个审核控制器：
- `ChannelContentReviewController` (`/api/v1/content/channel/review`) - 处理内容发布审核（APPROVE/REJECT）
- `ChannelReviewController` (`/api/v1/content/channel/review`) - 处理频道创建审核（approved/rejected/returned）

spec 中的"待审区"功能应明确使用哪个控制器。根据业务语义，内容发布审核应使用 `ChannelContentReviewController`，但其列表查询能力需要确认。

### 3.5 WARNING - 设计文档 API 数量与实际不符

design.md 声称"对接后端 EPIC-22 全部 REST API（发布、审核、治理、公告、添加内容共 5 组约 25 个接口）"，但实际后端仅有 8 个端点（2 个发布 + 1 个内容审核 + 1 个频道审核 + 1 个治理 + 3 个公告），差距显著。

### 3.6 SUGGESTION - tasks.md 中 API 封装任务路径需修正

tasks.md 中的任务描述引用了 spec 中的 API 路径，实现时需要根据实际后端路径调整。建议在每个 API 封装任务中注明实际后端路径。

---

## 4. 建议修复方案

### 4.1 修正 spec 中的 API 路径（优先级：高）

将所有 spec 文件中的 `/api/channel/...` 修正为实际后端路径：

| spec 路径模式 | 修正为 |
|--------------|--------|
| `/api/channel/publish/submit` | `/api/v1/content/channel/publish` |
| `/api/channel/review/list` | `/api/v1/content/channel/review/list` |
| `/api/channel/review/approve` | `/api/v1/content/channel/review` (body: `{reviewId, action: "APPROVE"}`) |
| `/api/channel/review/reject` | `/api/v1/content/channel/review` (body: `{reviewId, action: "REJECT", rejectReason}`) |
| `/api/channel/governance/pin` | `/api/v1/content/channel/governance` (body: `{channelId, contentId, action: "PIN"}`) |
| `/api/channel/announcement/{channelId}` | `/api/v1/content/channel/announcement/channel/{channelId}` |
| `/api/channel/content/add` | `/api/v1/content/channel/publish/add-existing` |

### 4.2 创建后端遗留代码文档（优先级：高）

在 change 目录中创建 `backend-issues.md`，列出 13 个缺失端点，供后端团队参考实现。

### 4.3 统一审核控制器路径（优先级：中）

建议后端统一审核控制器路径前缀，或在前端 API 封装层做适配。

### 4.4 更新 design.md API 数量描述（优先级：低）

将"约 25 个接口"修正为实际已实现的端点数量，并标注缺失端点。

---

## 5. 最终评估

**存在 3 个 CRITICAL 级别问题，必须在归档前修复：**

1. API 路径系统性偏差（29 处路径引用需修正）
2. API 架构模式差异（治理和审核操作需适配统一入口模式）
3. 13 个后端端点缺失（需创建 backend-issues.md 记录）

**修复建议优先级：**
1. 先修正 spec 中的 API 路径，使其与实际后端一致
2. 创建 backend-issues.md 记录缺失端点
3. 更新 design.md 中的 API 描述
4. 然后再开始前端实现工作
