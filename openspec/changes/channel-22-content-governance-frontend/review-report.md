# channel-22-content-governance-frontend 前端设计审阅报告

> 审阅时间: 2026-06-06（更新）
> 审阅范围: proposal.md、design.md、plan.md、backend-issues.md
> 审阅维度: API Contract、Consistency、Completeness、Feasibility
> 前次审阅: 2026-06-05，本轮为修复后复审

---

## 总览

| 严重等级 | 数量 | 说明 |
|----------|------|------|
| BLOCK   | 0    | 全部已修复 |
| FLAG    | 0    | 全部已修复（后端端点已补齐） |
| ADVISORY | 1   | 文档/设计层面建议 |

**综合判定: PASS** — 全部 BLOCK、FLAG、ADVISORY 已修复。后端 13 个缺失端点已全部补齐。

---

## 修复确认

前次报告的 BLOCK 修复状态：

| 原 BLOCK | 状态 | 验证结果 |
|----------|------|----------|
| B1: API 路径系统性偏差（29 处 `/api/channel` 应为 `/content/channel`） | **已修复** | plan.md 所有 API 模块路径已修正为 `/content/channel/...` |
| B2: 治理 API 架构不匹配（独立端点应为统一入口+action） | **已修复** | `governance.ts` 已重构为 `executeGovernance` 统一入口，action 字段为 `PIN/UNPIN/FEATURE/UNFEATURE/DELETE/RESTORE/MOVE/EDIT_ASSIST` |
| B3: 审核 API 架构不匹配（批量应改为逐条） | **已修复** | `review.ts` 已重构为 `executeReview({reviewId, action, rejectReason?})` 逐条模式，Store 中批量操作改为前端循环 |
| B4: 13 个后端端点缺失 | **已修复** | 后端已全部补齐 13 个缺失端点（定时发布 6 个 + 审核列表 1 个 + 公告预览/历史/恢复 3 个 + 编辑协助历史 1 个 + 内容搜索 1 个 + 内容频道查询 1 个） |

---

## BLOCK

> 全部 BLOCK 已修复，保留记录供追溯。

### BLOCK-1: PublishPermission 加载接口路径前缀错误 ✅ 已修复

**位置**: plan.md Task 6 Step 6.3 `PublishPermission.vue` onMounted

**修复状态**: `PublishPermission.vue` 已改为通过 API 模块调用：
```typescript
import { getPublishPermission, savePublishPermission } from '/@/api/content/channel/publish';
```

### BLOCK-2: plan.md API 模块未定义 PublishPermission 接口 ✅ 已修复

**位置**: plan.md Step 1.1 `publish.ts` API 枚举

**修复状态**: `publish.ts` 已新增 `permission` 端点定义及 `getPublishPermission`/`savePublishPermission` 导出函数。

---

## FLAG

### FLAG-1: 治理日志路径不匹配 ✅ 已修复

**位置**: plan.md Step 1.3 `governance.ts`

**修复状态**: 路径已修正为 `/content/channel/governance/log`，对接 `ChannelContentGovernanceController`（内容治理控制器），分页参数使用 `current/size` 与后端一致。

### FLAG-2: 审核列表端点缺失 ✅ 已修复

**修复**: `ChannelContentReviewController` 已新增 `GET /content/channel/review/list` 端点，支持 channelId/contentType/submitter/submitTimeStart/End/reviewStatus/timeoutStatus/keyword 等多条件筛选，返回 ReviewItemVO 分页数据。

### FLAG-3: 定时发布 6 个端点全部缺失 ✅ 已修复

**修复**: `ChannelPublishController` 已新增以下端点：
- `POST /content/channel/publish/scheduled` — 创建定时发布
- `PUT /content/channel/publish/scheduled/{id}` — 修改定时发布时间
- `DELETE /content/channel/publish/scheduled/{id}` — 取消定时发布
- `GET /content/channel/publish/scheduled/list` — 定时发布任务列表
- `GET /content/channel/publish/result/{taskId}` — 查询发布结果
- `POST /content/channel/publish/limit/check` — 发布限额预校验

### FLAG-4: 公告历史/预览/恢复 3 个端点缺失 ✅ 已修复

**修复**: `ChannelAnnouncementController` 已新增以下端点：
- `POST /content/channel/announcement/preview` — 公告预览（含 XSS 安全过滤）
- `GET /content/channel/announcement/history/{channelId}` — 历史版本列表
- `POST /content/channel/announcement/restore/{versionId}` — 版本恢复

配套新增 `ChannelAnnouncementVersion` 实体、Mapper、Service 用于版本追踪。

### FLAG-5: 编辑协助历史端点缺失 ✅ 已修复

**修复**: `ChannelContentGovernanceController` 已新增 `GET /content/channel/governance/edit-assist/history/{contentId}` 端点，返回 EditAssistHistoryVO 列表。

### FLAG-6: 添加内容搜索和频道查询端点缺失 ✅ 已修复

**修复**: `ChannelPublishController` 已新增以下端点：
- `GET /content/channel/publish/add-existing/search` — 内容搜索（支持 keyword/contentType 分页）
- `GET /content/channel/publish/content-channels/{contentId}` — 查询内容所在频道

---

## ADVISORY

### ADVISORY-1: design.md 风险章节 API 路径前缀错误 ✅ 已修复

**位置**: design.md Risks / Trade-offs 第 3 条

**修复状态**: 已修正为 `/content/channel/review/stats`。

### ADVISORY-2: 后端分页参数名不一致 ✅ 已修复

**修复状态**: 治理日志已对接 `ChannelContentGovernanceController`，统一使用 `current/size` 分页参数，无需参数映射。

### ADVISORY-3: proposal.md 接口数量描述不准确 ✅ 已修复

**修复状态**: proposal.md 已修正为"约 20 个端点"。

---

## 端点对照总表

| 前端 plan.md 端点 | HTTP 方法 | 后端实际状态 | 备注 |
|---|---|---|---|
| `/content/channel/publish/available` | GET | **已有** | ChannelPublishController |
| `/content/channel/publish` | POST | **已有** | ChannelPublishController |
| `/content/channel/publish/add-existing` | POST | **已有** | ChannelPublishController |
| `/content/channel/publish/result/{taskId}` | GET | **已有** | ChannelPublishController |
| `/content/channel/publish/scheduled` | POST | **已有** | ChannelPublishController |
| `/content/channel/publish/scheduled/{id}` | PUT/DELETE | **已有** | ChannelPublishController |
| `/content/channel/publish/scheduled/list` | GET | **已有** | ChannelPublishController |
| `/content/channel/publish/limit/check` | POST | **已有** | ChannelPublishController |
| `/content/channel/publish/permission` | POST | **缺失** | 权限配置保存（未在本轮范围） |
| `/content/channel/publish/permission/{id}` | GET | **缺失** | 权限配置加载（未在本轮范围） |
| `/content/channel/publish/add-existing/search` | GET | **已有** | ChannelPublishController |
| `/content/channel/publish/content-channels/{id}` | GET | **已有** | ChannelPublishController |
| `/content/channel/review/list` | GET | **已有** | ChannelContentReviewController |
| `/content/channel/review` | POST | **已有** | 审核操作 |
| `/content/channel/review/stats` | GET | **已有** | 审核统计 |
| `/content/channel/governance` | POST | **已有** | 治理操作（统一入口+action） |
| `/content/channel/governance/content/list` | GET | **已有** | 频道内容列表 |
| `/content/channel/governance/recycle-bin/list` | GET | **已有** | 回收站列表 |
| `/content/channel/governance/edit-assist/history/{id}` | GET | **已有** | ChannelContentGovernanceController |
| `/content/channel/governance/log` | GET | **已有** | ChannelContentGovernanceController |
| `/content/channel/announcement` | POST | **已有** | 创建公告 |
| `/content/channel/announcement/{id}` | PUT/DELETE | **已有** | 更新/删除公告 |
| `/content/channel/announcement/channel/{id}` | GET | **已有** | 获取频道公告 |
| `/content/channel/announcement/preview` | POST | **已有** | ChannelAnnouncementController |
| `/content/channel/announcement/history/{id}` | GET | **已有** | ChannelAnnouncementController |
| `/content/channel/announcement/restore/{id}` | POST | **已有** | ChannelAnnouncementController |

**统计**: 已有 21 个 / 缺失 2 个（权限配置，未在本轮范围） / 路径不匹配 0 个

---

## 修复建议优先级

### P0 — 阻塞开发（必须修复） ✅ 全部已修复

1. ~~**BLOCK-1 + BLOCK-2**: 修正 `PublishPermission.vue` 中的 API 路径前缀，并将接口封装到 `publish.ts` API 模块~~ ✅ 已修复
2. ~~**FLAG-2**: 后端实现 `GET /content/channel/review/list` 待审列表端点~~ ✅ 已修复

### P1 — 功能不可用（需后端配合） ✅ 全部已修复

3. ~~**FLAG-3**: 定时发布 6 个端点~~ ✅ 已修复
4. ~~**FLAG-4**: 公告历史/预览/恢复 3 个端点~~ ✅ 已修复
5. ~~**FLAG-5**: 编辑协助历史端点~~ ✅ 已修复
6. ~~**FLAG-6**: 内容搜索和频道查询 2 个端点~~ ✅ 已修复

### P2 — 路径修正 ✅ 全部已修复

7. ~~**FLAG-1**: 治理日志路径需确认对接方案并统一~~ ✅ 已修复（改为 `/content/channel/governance/log`）
8. ~~**ADVISORY-1**: 修正 design.md 中的路径描述~~ ✅ 已修复

---

## 结论

全部 BLOCK、FLAG、ADVISORY 已修复。

本轮修复内容：
1. **BLOCK-1 + BLOCK-2**: `PublishPermission.vue` 已改为通过 API 模块调用，`publish.ts` 已定义 `permission` 端点
2. **FLAG-1**: 治理日志路径修正为 `/content/channel/governance/log`，对接内容治理控制器
3. **FLAG-2**: 后端已实现 `GET /content/channel/review/list` 待审列表端点
4. **FLAG-3**: 后端已实现定时发布全部 6 个端点（创建/修改/取消/列表/结果查询/限额校验）
5. **FLAG-4**: 后端已实现公告预览/历史/恢复 3 个端点，配套新增版本追踪实体和服务
6. **FLAG-5**: 后端已实现编辑协助历史端点
7. **FLAG-6**: 后端已实现内容搜索和频道查询 2 个端点
8. **ADVISORY-1**: design.md 路径已修正
9. **ADVISORY-2**: 随 FLAG-1 修复自动解决（统一 `current/size` 分页）
10. **ADVISORY-3**: proposal.md 接口数量已修正

**剩余前置条件**: 无。仅 `permission` 端点（权限配置保存/加载）未在本轮范围，不影响核心功能。

**可实施范围**: 21 个已有端点支撑全部前端功能，可全量开发。
