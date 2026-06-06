# channel-22-content-governance-frontend 前端设计审阅报告

> 审阅时间: 2026-06-06（更新）
> 审阅范围: proposal.md、design.md、plan.md、backend-issues.md
> 审阅维度: API Contract、Consistency、Completeness、Feasibility
> 前次审阅: 2026-06-05，本轮为修复后复审

---

## 总览

| 严重等级 | 数量 | 说明 |
|----------|------|------|
| BLOCK   | 2    | PublishPermission 路径前缀错误 + API 模块缺失定义 |
| FLAG    | 6    | 端点缺失或路径不匹配 |
| ADVISORY | 3   | 文档/设计层面建议 |

**综合判定: CONDITIONAL PASS** — 前次 4 个 BLOCK 中 3 个已修复（路径系统性偏差、治理架构不匹配、审核架构不匹配），本轮发现 2 个新 BLOCK。

---

## 修复确认

前次报告的 BLOCK 修复状态：

| 原 BLOCK | 状态 | 验证结果 |
|----------|------|----------|
| B1: API 路径系统性偏差（29 处 `/api/channel` 应为 `/content/channel`） | **已修复** | plan.md 所有 API 模块路径已修正为 `/content/channel/...` |
| B2: 治理 API 架构不匹配（独立端点应为统一入口+action） | **已修复** | `governance.ts` 已重构为 `executeGovernance` 统一入口，action 字段为 `PIN/UNPIN/FEATURE/UNFEATURE/DELETE/RESTORE/MOVE/EDIT_ASSIST` |
| B3: 审核 API 架构不匹配（批量应改为逐条） | **已修复** | `review.ts` 已重构为 `executeReview({reviewId, action, rejectReason?})` 逐条模式，Store 中批量操作改为前端循环 |
| B4: 13 个后端端点缺失 | **部分缓解** | 后端已新增 `GET /content/channel/publish/available` 和 `GET /content/channel/review/stats`，但仍有 11 个端点缺失 |

---

## BLOCK

### BLOCK-1: PublishPermission 加载接口路径前缀错误

**位置**: plan.md Task 6 Step 6.3 `PublishPermission.vue` onMounted

```typescript
// 当前写法 (WRONG)
const res = await defHttp.get({ url: `/api/channel/publish/permission/${props.channelId}` });
```

后端 `ChannelPublishController` 的 `@RequestMapping` 是 `/content/channel/publish`。此处使用了已修复的旧前缀 `/api/channel/...`，运行时将返回 404。

注意：plan.md 其他 API 模块已全部修正为 `/content/channel/...`，但 `PublishPermission.vue` 是在 Task 6 中直接使用 `defHttp` 而非 API 模块，属于"漏网之鱼"。

**修复**: 路径改为 `/content/channel/publish/permission/{channelId}`。

### BLOCK-2: plan.md API 模块未定义 PublishPermission 接口

**位置**: plan.md Step 1.1 `publish.ts` API 枚举 + Step 6.3 `PublishPermission.vue`

`publish.ts` 定义了 `available`、`submit`、`result`、`scheduled`、`limitCheck` 等端点，但**没有定义**发布权限配置的 GET/POST 端点。而 `PublishPermission.vue` 直接使用 `defHttp` 调用：

```typescript
// 保存 — 直接用 defHttp，未走 publish.ts
await defHttp.post({ url: '/content/channel/publish/permission', data: { ... } });
// 加载 — 直接用 defHttp 且路径前缀错误
const res = await defHttp.get({ url: `/api/channel/publish/permission/${props.channelId}` });
```

违反 design.md 中"API 调用统一使用 defHttp 封装"的约束（应封装到 API 模块），且保存和加载使用了不一致的路径前缀。

**修复**: 在 `publish.ts` 中新增 `permission` 和 `getPermission` 端点定义，`PublishPermission.vue` 通过 API 模块调用。同时需后端实现这两个端点。

---

## FLAG

### FLAG-1: 治理日志路径不匹配 — 前端 `/content/channel/governance/log/list` vs 后端 `/channel/governance/log`

**位置**: plan.md Step 1.3 `governance.ts`

```typescript
// plan.md 定义
logList = '/content/channel/governance/log/list',
```

后端 `ChannelGovernanceController`（注意：不是 `ChannelContentGovernanceController`）的实际路径：

```java
@RequestMapping("/channel/governance")
@GetMapping("/log")
// 实际完整路径: /channel/governance/log
```

存在两个差异：
1. 路径前缀：`/content/channel/` vs `/channel/`
2. 路径后缀：`/log/list` vs `/log`
3. 分页参数：前端 Store 用 `current/size`，后端用 `pageNum/pageSize`

另外需注意 `ChannelGovernanceController` 是**成员治理**控制器（移除、禁言、黑名单），而 `ChannelContentGovernanceController` 是**内容治理**控制器。治理日志查询应对接哪个控制器需明确。

**建议**: 如需对接 `ChannelGovernanceController`，修正前端路径为 `/channel/governance/log`，并添加参数映射。如需在 `ChannelContentGovernanceController` 中新增端点，路径设计为 `/content/channel/governance/log` 并使用 `current/size` 分页。

### FLAG-2: 审核列表端点缺失 — `GET /content/channel/review/list` 后端未实现

**位置**: plan.md Step 1.2 `review.ts`

```typescript
list = '/content/channel/review/list',
```

`ChannelContentReviewController` 只有两个端点：
- `POST /content/channel/review` — 审核操作
- `GET /content/channel/review/stats` — 审核统计

没有 `GET /content/channel/review/list` 端点。前端 `ReviewQueue.vue` 和 `useChannelReviewStore.fetchList()` 依赖此端点。

注意：`ChannelReviewController` 有 `GET /jeecg-boot/api/v1/content/channel/review/list`，但那是**频道创建审核**，数据模型（ChannelReviewVO 包含 reviewId/channelId/reviewType/status/submitTime/timeoutFlag）与前端期望的 ReviewItem（id/title/contentType/submitter/submitTime/sourceScene/hitRule/isTimeout）完全不同。

**修复**: 需在 `ChannelContentReviewController` 中新增待审列表查询端点，返回前端所需的字段。

### FLAG-3: 定时发布 6 个端点全部缺失

**位置**: plan.md Step 1.1 `publish.ts`

```typescript
scheduled = '/content/channel/publish/scheduled',
scheduledList = '/content/channel/publish/scheduled/list',
```

后端 `ChannelPublishController` 无任何定时发布相关端点。涉及：
- `POST /content/channel/publish/scheduled` — 创建
- `PUT /content/channel/publish/scheduled/{id}` — 修改时间
- `DELETE /content/channel/publish/scheduled/{id}` — 取消
- `GET /content/channel/publish/scheduled/list` — 任务列表

前端 `ScheduledPublish.vue` 组件和 `channelPublishStore` 的 `fetchScheduledTasks`、`editScheduledTime`、`cancelScheduledTask` 均依赖这些端点。

**影响**: 定时发布功能完全不可用。

### FLAG-4: 公告历史/预览/恢复 3 个端点缺失

**位置**: plan.md Step 1.4 `announcement.ts`

```typescript
preview = '/content/channel/announcement/preview',
history = '/content/channel/announcement/history',
restore = '/content/channel/announcement/restore',
```

`ChannelAnnouncementController` 只有 CRUD 4 个端点，缺少：
- `POST /content/channel/announcement/preview` — 公告预览安全过滤
- `GET /content/channel/announcement/history/{channelId}` — 历史版本列表
- `POST /content/channel/announcement/restore/{versionId}` — 版本恢复

前端 `AnnouncementManage.vue` 的 `handlePreview`、`loadHistory`、`handleRestoreVersion` 均依赖这些端点。

### FLAG-5: 编辑协助历史端点缺失

**位置**: plan.md Step 1.3 `governance.ts`

```typescript
editAssistHistory = '/content/channel/governance/edit-assist/history',
```

`ChannelContentGovernanceController` 无此端点。前端 `EditAssistDrawer.vue` 的修订历史展示依赖此端点。

### FLAG-6: 添加内容搜索和频道查询端点缺失

**位置**: plan.md Step 1.5 `addContent.ts`

```typescript
search = '/content/channel/publish/add-existing/search',
channels = '/content/channel/publish/content-channels',
```

`ChannelPublishController` 无此两个端点。前端 `AddContentDialog.vue` 的 `handleSearchContent` 依赖 `search` 端点。

---

## ADVISORY

### ADVISORY-1: design.md 风险章节 API 路径前缀错误

**位置**: design.md Risks / Trade-offs 第 3 条

```markdown
前端每 60 秒轮询 `/api/channel/review/stats` 更新 badge
```

应为 `/content/channel/review/stats`。文档描述不影响运行时，但会误导开发者。

### ADVISORY-2: 后端分页参数名不一致

后端存在两种分页参数命名：
- `ChannelContentGovernanceController` → `current/size`（MyBatis-Plus Page）
- `ChannelGovernanceController` → `pageNum/pageSize`

plan.md 前端统一使用 `current/size`。治理日志如果对接 `ChannelGovernanceController`，需做参数映射。

### ADVISORY-3: proposal.md 接口数量描述不准确

proposal.md 写"对接后端 EPIC-22 全部 REST API（发布、审核、治理、公告、添加内容共 5 组约 25 个接口）"，但 plan.md 实际定义约 20 个端点（含 11 个缺失）。建议修正为实际数量。

---

## 端点对照总表

| 前端 plan.md 端点 | HTTP 方法 | 后端实际状态 | 备注 |
|---|---|---|---|
| `/content/channel/publish/available` | GET | **已有** | ChannelPublishController |
| `/content/channel/publish` | POST | **已有** | ChannelPublishController |
| `/content/channel/publish/add-existing` | POST | **已有** | ChannelPublishController |
| `/content/channel/publish/result/{taskId}` | GET | **缺失** | |
| `/content/channel/publish/scheduled` | POST | **缺失** | 定时发布创建 |
| `/content/channel/publish/scheduled/{id}` | PUT/DELETE | **缺失** | 定时发布修改/取消 |
| `/content/channel/publish/scheduled/list` | GET | **缺失** | 定时发布列表 |
| `/content/channel/publish/limit/check` | POST | **缺失** | 限额预校验 |
| `/content/channel/publish/permission` | POST | **缺失** | 权限配置保存 |
| `/content/channel/publish/permission/{id}` | GET | **缺失** + 前缀错误 | 权限配置加载 |
| `/content/channel/publish/add-existing/search` | GET | **缺失** | 内容搜索 |
| `/content/channel/publish/content-channels/{id}` | GET | **缺失** | 内容所在频道 |
| `/content/channel/review/list` | GET | **缺失** | 待审列表 |
| `/content/channel/review` | POST | **已有** | 审核操作 |
| `/content/channel/review/stats` | GET | **已有** | 审核统计 |
| `/content/channel/governance` | POST | **已有** | 治理操作（统一入口+action） |
| `/content/channel/governance/content/list` | GET | **已有** | 频道内容列表 |
| `/content/channel/governance/recycle-bin/list` | GET | **已有** | 回收站列表 |
| `/content/channel/governance/edit-assist/history/{id}` | GET | **缺失** | 编辑协助历史 |
| `/content/channel/governance/log/list` | GET | **路径不匹配** | 后端在 `/channel/governance/log` |
| `/content/channel/announcement` | POST | **已有** | 创建公告 |
| `/content/channel/announcement/{id}` | PUT/DELETE | **已有** | 更新/删除公告 |
| `/content/channel/announcement/channel/{id}` | GET | **已有** | 获取频道公告 |
| `/content/channel/announcement/preview` | POST | **缺失** | 公告预览 |
| `/content/channel/announcement/history/{id}` | GET | **缺失** | 公告历史 |
| `/content/channel/announcement/restore/{id}` | POST | **缺失** | 版本恢复 |

**统计**: 已有 9 个 / 缺失 14 个 / 路径不匹配 2 个

---

## 修复建议优先级

### P0 — 阻塞开发（必须修复）

1. **BLOCK-1 + BLOCK-2**: 修正 `PublishPermission.vue` 中的 API 路径前缀，并将接口封装到 `publish.ts` API 模块
2. **FLAG-2**: 后端实现 `GET /content/channel/review/list` 待审列表端点

### P1 — 功能不可用（需后端配合）

3. **FLAG-3**: 定时发布 6 个端点
4. **FLAG-4**: 公告历史/预览/恢复 3 个端点
5. **FLAG-5**: 编辑协助历史端点
6. **FLAG-6**: 内容搜索和频道查询 2 个端点

### P2 — 路径修正

7. **FLAG-1**: 治理日志路径需确认对接方案并统一
8. **ADVISORY-1**: 修正 design.md 中的路径描述

---

## 结论

前次 BLOCK 修复效果良好，路径系统性偏差和 API 架构不匹配问题已全部解决。本轮发现 2 个新 BLOCK（PublishPermission 组件的路径前缀遗漏和 API 模块定义缺失），均为前次修复时的遗漏项。

**剩余前置条件**（进入实施前必须满足）:
1. 修复 BLOCK-1 和 BLOCK-2（PublishPermission 路径和 API 模块定义）
2. 后端实现 FLAG-2 待审列表端点（ReviewQueue 核心依赖）
3. 确认 FLAG-1 治理日志对接方案

**可实施范围**: 9 个已有端点支撑的功能（发布提交、审核操作、审核统计、治理操作、频道内容列表、回收站列表、公告 CRUD、添加已有内容、可用频道查询）可先行开发，其余功能待后端端点就绪后补充。
