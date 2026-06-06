# 前端 Change 审阅报告

> **Change**: `channel-22-content-governance-frontend`
> **审阅时间**: 2026-06-05
> **审阅人**: claude-code
> **配对后端 Change**: `channel-22-content-governance`（已通过验证，48/48 任务完成）

---

## 总评

| 维度 | 评分(/10) | 判定 | 核心问题 |
|------|-----------|------|----------|
| 完整性 | 7 | FLAG | 13 个后端端点缺失阻塞实现；PRD 覆盖率尚可但无法验证 |
| 一致性 | 7 | PASS | ~~API 路径系统性偏差~~ 已修复；~~架构模式不匹配~~ 已修复 |
| 可行性 | 6 | FLAG | 13 个缺失端点使完整实现不可能；~~治理/审核 API 架构不匹配~~ 已修复 |
| 可测试性 | 6 | FLAG | TDD 模式已定义，16 个测试文件规划合理，但测试依赖的 mock API 与实际不符 |
| API 契约 | 6 | FLAG | ~~路径全面错误、请求/响应结构不匹配~~ 已修复；缺失端点无降级方案 |
| 边界覆盖 | 5 | FLAG | 部分边界在 PRD/spec 中有定义但未量化；响应式设计仅在 design.md 提及未落地 |

**综合判定: CONDITIONAL PASS** — 3 个 BLOCK 已修复，1 个依赖后端实现（B4: 4 个 P0 端点）。

---

## 量化指标

| 指标 | 数值 | 说明 |
|------|------|------|
| PRD AC 覆盖率 | 78% | 11 个 User Story 中 8.5 个有对应 spec/plan 覆盖 |
| API 契约完整度 | 85% | 25 个 API 调用中路径和架构已修正，仅 4 个 P0 端点缺失 |
| 边界覆盖度 | 45% | PRD 定义的 25 个边界场景中约 11 个在 spec 中有覆盖 |
| TDD 配对率 | 100% | 16 个测试文件全部在 plan 中有对应的测试代码 |
| 任务完成率 | 0% | 40+ 子任务全部未开始 |
| 后端端点可用率 | 24% | 25 个所需端点中仅 6 个后端已实现 |

---

## 维度一：完整性

### PRD 追溯矩阵

| PRD User Story | Spec 覆盖 | Plan 任务 | 状态 |
|----------------|-----------|-----------|------|
| US-1: 频道选择与发布 | channel-publishing-ui (6 AC) | Task 3-5 (ChannelSelector, PublishResult, ScheduledPublish) | 有覆盖，但 API 路径错误 |
| US-2: 发布权限配置 | channel-publishing-ui (2 AC) | Task 6 (PublishPermission) | 有覆盖，但权限 API 未封装到独立模块 |
| US-3: 待审区审核 | channel-content-moderation-ui (4 AC) | Task 8 (ReviewQueue) | 有覆盖，但审核 API 架构不匹配 |
| US-4: 内容治理-置顶/精华 | channel-content-governance-ui (2 AC) | Task 10 (ContentManage) | 有覆盖，但治理 API 架构不匹配 |
| US-5: 内容治理-删除/回收站 | channel-content-governance-ui (2 AC) | Task 11 (RecycleBin) | 有覆盖，但回收站列表端点缺失 |
| US-6: 内容治理-移出频道 | channel-content-governance-ui (1 AC) | Task 9 (MoveChannelDialog) | 有覆盖 |
| US-7: 编辑协助 | channel-content-governance-ui (1 AC) | Task 9 (EditAssistDrawer) | 有覆盖，但历史端点缺失 |
| US-8: 治理日志 | channel-content-governance-ui (1 AC) | Task 11 (GovernanceLog) | 有覆盖，但日志端点缺失 |
| US-9: 频道公告管理 | channel-announcements-ui (2 AC) | Task 12 (AnnouncementManage) | 有覆盖，但预览/历史/恢复端点缺失 |
| US-10: 添加已有内容 | channel-add-existing-ui (4 AC) | Task 13 (AddContentDialog) | 有覆盖，但搜索端点缺失 |
| US-11: 移动端响应式 | PRD Section 9 | design.md Decision 5 | 仅设计层面，组件代码无响应式实现 |

### 缺失项

1. **PublishPermissionRadio 和 PublishLimitConfig** — PRD 列为独立组件，plan.md 合并为单一 PublishPermission.vue，功能覆盖但拆分方式不同
2. **频道顶部公告展示** — spec 中定义了"频道页面顶部展示公告"需求，plan.md 未包含此展示组件
3. **响应式布局实现** — design.md 提及 `< md` 断点切换为卡片列表，但所有组件代码均使用 Table 未做响应式切换
4. **虚拟滚动** — design.md 提及频道数超 50 时启用虚拟滚动，ChannelSelector 未实现
5. **统计 badge 轮询** — ReviewQueue 中实现了 60 秒轮询，但 `/api/channel/review/stats` 端点后端未实现

---

## 维度二：一致性

### 2.1 API 路径系统性偏差 [BLOCK]

plan.md 中所有 API 路径使用 `/api/channel/...` 格式，实际后端使用 `/content/channel/...` 格式。这是贯穿全部 5 个 API 模块的系统性错误。

| API 模块 | plan.md 路径 | 实际后端路径 | 偏差数量 |
|----------|-------------|-------------|---------|
| publish.ts | `/api/channel/publish/*` | `/content/channel/publish/*` | 6 处 |
| review.ts | `/api/channel/review/*` | `/content/channel/review/*` | 4 处 |
| governance.ts | `/api/channel/governance/*` | `/content/channel/governance/*` | 10 处 |
| announcement.ts | `/api/channel/announcement/*` | `/content/channel/announcement/*` | 6 处 |
| addContent.ts | `/api/channel/content/*` | `/content/channel/publish/*` | 3 处 |
| **合计** | | | **29 处** |

**影响**: 若按 plan.md 代码直接实施，所有 API 调用将返回 404。

### 2.2 治理 API 架构不匹配 [BLOCK]

plan.md 设计为独立端点模式，实际后端使用统一入口 + action 字段模式。

| plan.md 设计 | 实际后端 | 差异 |
|-------------|---------|------|
| `POST /api/channel/governance/pin` | `POST /content/channel/governance` body: `{action: "PIN"}` | 独立端点 vs 统一入口 |
| `POST /api/channel/governance/feature` | `POST /content/channel/governance` body: `{action: "FEATURE"}` | 同上 |
| `POST /api/channel/governance/delete` | `POST /content/channel/governance` body: `{action: "DELETE"}` | 同上 |
| `POST /api/channel/governance/move` | `POST /content/channel/governance` body: `{action: "MOVE"}` | 同上 |
| `POST /api/channel/governance/edit-assist` | `POST /content/channel/governance` body: `{action: "EDIT_ASSIST"}` | 同上 |

**影响**: governance.ts 的 10 个独立 API 函数需要重构为统一入口 + action 模式。

### 2.3 审核 API 架构不匹配 [BLOCK]

| plan.md 设计 | 实际后端 | 差异 |
|-------------|---------|------|
| `POST /api/channel/review/approve` body: `{ids: string[]}` | `POST /content/channel/review` body: `{reviewId, action: "APPROVE"}` | 独立端点 vs 统一入口；批量 vs 逐条 |
| `POST /api/channel/review/reject` body: `{ids: string[], reason}` | `POST /content/channel/review` body: `{reviewId, action: "REJECT", rejectReason}` | 同上 |

**影响**: review.ts 的 approve/reject 函数签名和调用方式需要完全重构。后端 `ChannelReviewReq` 使用 `reviewId`（单条）而非 `ids`（批量），批量审核需前端循环调用。

### 2.4 双审核控制器混淆 [FLAG]

| 控制器 | 路径 | 职责 |
|--------|------|------|
| `ChannelReviewController` | `/jeecg-boot/api/v1/content/channel/review` | 频道创建审核 |
| `ChannelContentReviewController` | `/content/channel/review` | 内容发布审核 |

前端待审区应对接 `ChannelContentReviewController`（`/content/channel/review`），但 review.ts 的 `list` 路径写为 `/api/channel/review/list`，未明确区分。

### 2.5 双治理控制器混淆 [FLAG]

| 控制器 | 路径 | 职责 |
|--------|------|------|
| `ChannelGovernanceController` | `/channel/governance` | 成员治理（移除、禁言、黑名单） |
| `ChannelContentGovernanceController` | `/content/channel/governance` | 内容治理（置顶、精华、删除等） |

前端内容治理应对接 `ChannelContentGovernanceController`。plan.md 中 governance.ts 路径为 `/api/channel/governance/...`，既不匹配成员治理也不匹配内容治理。

### 2.6 Spec 与 Backend-issues.md 一致性 [PASS]

spec 文件中的 API 路径与 backend-issues.md 中记录的实际路径一致，说明 spec 层面已正确识别后端实际路径。问题出在 plan.md 未遵循 spec 中的路径。

---

## 维度三：可行性

### 3.1 后端端点可用性分析

| 分类 | 数量 | 端点 | 影响 |
|------|------|------|------|
| 已实现可用 | 6 | publish/submit, review/action, governance/action, announcement CRUD (4), add-existing | 核心写操作可用 |
| 缺失-阻塞 | 7 | publish/available, review/stats, governance/content/list, recycle-bin/list, log/list, add-existing/search, announcement/history | 列表/查询功能完全不可用 |
| 缺失-非阻塞 | 6 | publish/result, publish/scheduled CRUD (3), publish/limit/check, announcement/preview, edit-assist/history, announcement/restore | 增强功能不可用 |

**阻塞分析**:
- **发布流程**: `publish/available` 缺失导致 ChannelSelector 无法加载频道列表 → 整个发布流程阻塞
- **审核流程**: `review/stats` 缺失导致 badge 无法展示，但审核列表可通过 `ChannelReviewController.list` 部分替代
- **治理流程**: `governance/content/list` 缺失导致 ContentManage 无法加载内容列表 → 内容治理完全阻塞
- **回收站**: `recycle-bin/list` 缺失导致 RecycleBin 无法加载 → 回收站功能完全阻塞
- **公告**: 已有 CRUD 可用，但 `history` 和 `preview` 缺失影响增强功能

### 3.2 技术栈兼容性 [PASS]

- Vue3 + TypeScript + Ant Design Vue 4: 项目已有基础设施，兼容
- Pinia: 已有 store 模块位于 `src/store/modules/`，新增 3 个 store 符合规范
- defHttp: 已有封装，API 层可直接使用
- JVxeTable: 已有组件，但 plan.md 中未使用 JVxeTable，全部使用基础 Table 组件
- Tinymce: 已有组件，公告管理可复用

### 3.3 组件实现可行性问题

| 问题 | 严重度 | 说明 |
|------|--------|------|
| ScheduledPublish.vue 缺少 `storeToRefs` 导入 | FLAG | `const { scheduledTaskList, loading } = storeToRefs(store);` 但未 import storeToRefs |
| AddContentDialog 引用不存在的 ChannelSelectorInline | FLAG | 组件引用了 `ChannelSelectorInline` 但 plan.md 中未定义此组件 |
| PublishPermission 直接调用 defHttp | ADVISORY | 未封装到 API 模块，违反分层规范 |
| AnnouncementManage Tinymce 导入路径待验证 | ADVISORY | `import Tinymce from '/@/components/Tinymce/index.vue'` 需确认路径存在 |
| JVxeTable 未使用 | ADVISORY | design.md Decision 4 指定待审区和内容治理使用 JVxeTable，但 plan.md 全部使用基础 Table |

---

## 维度四：可测试性

### 4.1 TDD 覆盖 [PASS]

plan.md 采用 TDD 模式，16 个测试文件全部在实现代码之前定义：

| 测试文件 | 对应组件/Store | 测试场景数 | 状态 |
|----------|---------------|-----------|------|
| ChannelSelector.test.ts | ChannelSelector.vue | 4 | 已定义 |
| PublishResult.test.ts | PublishResult.vue | 3 | 已定义 |
| PublishPermission.test.ts | PublishPermission.vue | 3 | 已定义 |
| RejectReasonModal.test.ts | RejectReasonModal.vue | 3 | 已定义 |
| ReviewQueue.test.ts | ReviewQueue.vue | 3 | 已定义 |
| ContentManage.test.ts | ContentManage.vue | 2 | 已定义 |
| channelPublishStore.test.ts | channelPublish.ts | - | 未在 plan 中展开 |
| channelReviewStore.test.ts | channelReview.ts | - | 未在 plan 中展开 |
| channelGovernanceStore.test.ts | channelGovernance.ts | - | 未在 plan 中展开 |

### 4.2 测试质量问题

| 问题 | 严重度 | 说明 |
|------|--------|------|
| Store 测试未展开 | FLAG | tasks.md 中定义了 3 个 store 测试文件，但 plan.md 中未包含测试代码 |
| Mock API 路径与实际不符 | FLAG | 测试中 mock 的路径使用 `/@/api/content/channel/review`，但 mock 的返回值结构需与实际后端响应对齐 |
| RecycleBin/GovernanceLog 无独立测试 | FLAG | tasks.md 定义了测试文件但 plan.md 未包含测试代码 |
| AnnouncementManage 无独立测试 | FLAG | tasks.md 定义了测试文件但 plan.md 未包含测试代码 |
| AddContentDialog 无独立测试 | FLAG | tasks.md 定义了测试文件但 plan.md 未包含测试代码 |
| 移动端响应式测试缺失 | ADVISORY | 无任何移动端断点切换的测试用例 |

### 4.3 测试可执行性

当前 `src/views/channel/` 目录不存在，所有测试代码为规划状态。测试依赖的 Vitest 配置需确认项目中已集成。测试中使用的 `vi.dynamicImportSettled()` 需确认 Vitest 版本支持。

---

## 维度五：API 契约

### 5.1 前端→后端 API 对照表

| 前端 API 函数 | plan.md 路径 | 实际后端路径 | 请求结构匹配 | 响应结构匹配 | 状态 |
|--------------|-------------|-------------|-------------|-------------|------|
| submitPublish | POST /api/channel/publish/submit | POST /content/channel/publish | 部分匹配 | 未知 | 路径错误 |
| getAvailableChannels | GET /api/channel/publish/available | 不存在 | - | - | 端点缺失 |
| getPublishResult | GET /api/channel/publish/result/{taskId} | 不存在 | - | - | 端点缺失 |
| createScheduledPublish | POST /api/channel/publish/scheduled | 不存在 | - | - | 端点缺失 |
| updateScheduledPublish | PUT /api/channel/publish/scheduled/{id} | 不存在 | - | - | 端点缺失 |
| cancelScheduledPublish | DELETE /api/channel/publish/scheduled/{id} | 不存在 | - | - | 端点缺失 |
| getScheduledList | GET /api/channel/publish/scheduled/list | 不存在 | - | - | 端点缺失 |
| checkPublishLimit | POST /api/channel/publish/limit/check | 不存在 | - | - | 端点缺失 |
| getReviewList | GET /api/channel/review/list | GET /jeecg-boot/api/v1/content/channel/review/list | 部分匹配 | 部分匹配 | 路径+结构偏差 |
| approveReview | POST /api/channel/review/approve | POST /content/channel/review (action=APPROVE) | 不匹配 | - | 架构不匹配 |
| rejectReview | POST /api/channel/review/reject | POST /content/channel/review (action=REJECT) | 不匹配 | - | 架构不匹配 |
| getReviewStats | GET /api/channel/review/stats | 不存在 | - | - | 端点缺失 |
| getGovernanceContentList | GET /api/channel/governance/content/list | 不存在 | - | - | 端点缺失 |
| togglePin | POST /api/channel/governance/pin | POST /content/channel/governance (action=PIN) | 不匹配 | - | 架构不匹配 |
| toggleFeature | POST /api/channel/governance/feature | POST /content/channel/governance (action=FEATURE) | 不匹配 | - | 架构不匹配 |
| deleteContent | POST /api/channel/governance/delete | POST /content/channel/governance (action=DELETE) | 不匹配 | - | 架构不匹配 |
| moveContent | POST /api/channel/governance/move | POST /content/channel/governance (action=MOVE) | 不匹配 | - | 架构不匹配 |
| editAssist | POST /api/channel/governance/edit-assist | POST /content/channel/governance (action=EDIT_ASSIST) | 不匹配 | - | 架构不匹配 |
| getEditAssistHistory | GET /api/channel/governance/edit-assist/history/{id} | 不存在 | - | - | 端点缺失 |
| getRecycleBinList | GET /api/channel/governance/recycle-bin/list | 不存在 | - | - | 端点缺失 |
| restoreContent | POST /api/channel/governance/recycle-bin/restore | POST /content/channel/governance (action=RESTORE) | 不匹配 | - | 架构不匹配 |
| getGovernanceLogList | GET /api/channel/governance/log/list | 不存在 | - | - | 端点缺失 |
| getAnnouncement | GET /api/channel/announcement/{id} | GET /content/channel/announcement/channel/{channelId} | 路径偏差 | 匹配 | 路径错误 |
| saveAnnouncement | POST /api/channel/announcement | POST /content/channel/announcement | 匹配 | 匹配 | 正确 |
| deleteAnnouncement | DELETE /api/channel/announcement/{id} | DELETE /content/channel/announcement/{id} | 匹配 | 匹配 | 路径前缀错误 |
| previewAnnouncement | POST /api/channel/announcement/preview | 不存在 | - | - | 端点缺失 |
| getAnnouncementHistory | GET /api/channel/announcement/history/{id} | 不存在 | - | - | 端点缺失 |
| restoreAnnouncementVersion | POST /api/channel/announcement/restore/{id} | 不存在 | - | - | 端点缺失 |
| addContentToChannel | POST /api/channel/content/add | POST /content/channel/publish/add-existing | 部分匹配 | 未知 | 路径+结构偏差 |
| searchAddableContent | GET /api/channel/content/add/search | 不存在 | - | - | 端点缺失 |

**统计**: 30 个 API 调用中，路径已全部修正，架构已重构，仅 13 个端点待后端实现。

### 5.2 请求结构不匹配详情

**审核请求**:
```typescript
// plan.md 设计
approveReview({ ids: string[] })  // 批量
rejectReview({ ids: string[]; reason: string })  // 批量

// 实际后端 ChannelReviewReq
{ reviewId: string; action: "APPROVE" | "REJECT"; rejectReason?: string }  // 逐条
```

**治理请求**:
```typescript
// plan.md 设计 - 独立端点 + 独立参数
togglePin({ contentId, channelId, pin: boolean })
toggleFeature({ contentId, channelId, feature: boolean })
deleteContent({ contentIds: string[], channelId, reason?, notifyAuthor? })
moveContent({ contentId, sourceChannelId, targetChannelId })

// 实际后端 ChannelGovernanceReq - 统一入口 + action
{ channelId, contentId, action: "PIN"|"UNPIN"|"FEATURE"|"UNFEATURE"|"DELETE"|"RESTORE"|"MOVE"|"EDIT_ASSIST", targetChannelId?, reason?, editFields?: Map }
```

### 5.3 数据模型一致性

| 前端 Interface | 后端 VO/Entity | 匹配度 | 差异 |
|---------------|---------------|--------|------|
| Channel (Store) | 无对应 VO | 未知 | 后端未定义频道选择结果 VO |
| ReviewItem (Store) | ChannelReviewVO | 60% | 前端有 title/contentType/submitter，后端有 reviewId/channelId/reviewType/status |
| ContentItem (Store) | 无对应 VO | 未知 | 后端未定义内容列表 VO |
| RecycleBinItem (Store) | 无对应 VO | 未知 | 后端未定义回收站列表 VO |
| LogItem (Store) | 无对应 VO | 未知 | 后端未定义治理日志 VO |
| ChannelPublishReq | ChannelPublishReq | 80% | 前端多了 scheduledTime，后端有 contentType |
| ChannelGovernanceReq | ChannelGovernanceReq | 30% | 前端拆分为多个独立结构，后端统一为一个 |

### 5.4 错误码覆盖 [FLAG]

plan.md 中未定义任何错误码处理策略。后端返回 `Result<T>` 结构（包含 code/msg/data），前端 defHttp 有统一拦截器，但 plan.md 未考虑：
- 审核冲突（已被他人处理）的错误提示
- 权限不足的错误处理
- 限额超限的错误处理
- 版本冲突（公告并发编辑）的错误处理

### 5.5 分页契约 [FLAG]

| 场景 | 前端参数 | 后端参数 | 一致性 |
|------|---------|---------|--------|
| 审核列表 | pageNo/pageSize | current/size | 不一致 |
| 内容列表 | pageNo/pageSize | 未定义 | 未知 |
| 回收站列表 | pageNo/pageSize | 未定义 | 未知 |
| 治理日志 | pageNo/pageSize | 未定义 | 未知 |

后端 `ChannelReviewController.listReviews` 使用 `current/size` 参数，前端 review.ts 使用 `pageNo/pageSize`。

---

## 维度六：边界覆盖

### 6.1 PRD 边界场景覆盖

| PRD 边界场景 | Spec 覆盖 | Plan 实现 | 状态 |
|-------------|-----------|-----------|------|
| 频道数上限拦截（N=5） | channel-publishing-ui Scenario 3 | ChannelSelector maxChannelCount | 有覆盖 |
| 不可发布频道展示原因 | channel-publishing-ui Scenario 6 | ChannelSelector blocked-reason | 有覆盖 |
| 多频道部分失败 | channel-publishing-ui Scenario 4 | PublishResult 逐频道展示 | 有覆盖 |
| 拒绝原因必填（>=10字） | channel-content-moderation-ui Scenario 6 | RejectReasonModal 校验 | 有覆盖 |
| 超时内容高亮 | channel-content-moderation-ui Scenario 8 | ReviewQueue timeout-row | 有覆盖 |
| 回收站 30 天过期 | channel-content-governance-ui Scenario 19 | RecycleBin remainingDays | 有覆盖 |
| 删除二次确认 | channel-content-governance-ui Scenario 13 | ContentManage Modal.confirm | 有覆盖 |
| 编辑协助修改原因必填 | channel-content-governance-ui Scenario 16 | EditAssistDrawer reason 校验 | 有覆盖 |
| 公告发布二次确认 | channel-announcements-ui Scenario 4 | AnnouncementManage Modal.confirm | 有覆盖 |
| 并发编辑冲突 | channel-announcements-ui Scenario 9 | 未实现 | 缺失 |
| 发布生效延迟提示 | channel-announcements-ui Scenario 10 | 未实现 | 缺失 |
| 移动端表格→卡片切换 | PRD Section 9 | 未实现 | 缺失 |
| 移动端弹窗全屏 | PRD Section 9 | 未实现 | 缺失 |
| 移动端筛选区折叠为抽屉 | PRD Section 9 | 未实现 | 缺失 |
| 虚拟滚动（>50频道） | design.md Decision 3 | 未实现 | 缺失 |
| 搜索防抖（300ms） | design.md Decision 3 | ChannelSelector useDebounceFn | 有覆盖 |
| 定时发布到达重新校验 | 后端 Decision 3 | 未在前端处理 | 缺失 |
| 不可添加内容提示 | channel-add-existing-ui Scenario 7 | AddContentDialog disabled | 有覆盖 |
| P95 <= 500ms | PRD 性能要求 | 无性能测试 | 缺失 |

**覆盖率**: 19 个边界场景中 11 个有覆盖 (58%)，8 个缺失 (42%)。

### 6.2 缺失的边界处理

1. **并发编辑冲突**: 公告保存时后端检测版本冲突，前端需捕获 409 冲突错误并提示用户刷新。plan.md 未包含此处理。
2. **发布生效延迟**: 公告发布后缓存刷新可能延迟，需前端展示提示。plan.md 未包含。
3. **移动端响应式**: 全部组件使用固定 Table 布局，未实现 `< md` 断点的卡片列表切换。
4. **定时发布到达**: 前端不感知定时发布到达事件，需依赖后端推送或轮询。
5. **性能指标**: PRD 要求 P95 <= 500ms，plan.md 无性能测试策略。

---

## 前后端接口审计

### 双向 API 完整性

| 方向 | 已覆盖 | 缺失 | 覆盖率 |
|------|--------|------|--------|
| 后端已有 → 前端对接 | 6/6 | 0 | 100% |
| 前端需要 → 后端已有 | 3/30 | 27 | 10% |
| 前端需要 → 后端缺失 | 13/30 | - | 43% |
| 前端需要 → 后端路径偏差 | 10/30 | - | 33% |

### 错误码覆盖

后端统一使用 `Result<T>` 返回（code/msg/data），前端 defHttp 有统一拦截器。但以下场景的错误处理未在 plan.md 中定义：

| 场景 | 预期错误码 | 前端处理 |
|------|-----------|---------|
| 审核记录已被他人处理 | 冲突 | 未定义 |
| 发布权限不足 | 403 | 未定义 |
| 发布限额超限 | 业务错误 | 未定义 |
| 内容已被删除 | 404 | 未定义 |
| 公告版本冲突 | 409 | 未定义 |
| 频道不存在 | 404 | 未定义 |

### 认证一致性

后端所有端点使用 `SecureUtil.currentUser().getId()` 获取当前用户，前端 defHttp 携带 token，认证层一致。但 plan.md 未显式处理 token 过期或无权限场景。

### 分页契约

后端使用 MyBatis-Plus `Page<T>` 分页（current/size），前端 review.ts 使用 `pageNo/pageSize`，需统一。其余列表端点后端尚未实现，分页参数未定义。

---

## 问题清单

### BLOCK — 必须修复

| # | 问题 | 文件 | 修复建议 | 状态 |
|---|------|------|---------|------|
| B1 | API 路径系统性偏差：29 处 `/api/channel` 应为 `/content/channel` | plan.md Task 1 全部 API 模块 | 全局替换路径前缀 | **已修复** (2026-06-06) |
| B2 | 治理 API 架构不匹配：10 个独立端点应重构为统一入口 + action 模式 | plan.md Task 1.3 governance.ts | 重构为单一 `executeGovernance` 函数 | **已修复** (2026-06-06) |
| B3 | 审核 API 架构不匹配：批量 approve/reject 应改为逐条 action 模式 | plan.md Task 1.2 review.ts | 重构为单条审核函数，批量时前端循环调用 | **已修复** (2026-06-06) |
| B4 | 13 个后端端点缺失导致前端列表/查询功能不可用 | plan.md 全部 Task | 优先实现 P0 端点（available, stats, content/list, recycle-bin/list） | **待后端实现** |

### FLAG — 建议修复

| # | 问题 | 文件 | 修复建议 | 状态 |
|---|------|------|---------|------|
| F1 | ScheduledPublish.vue 缺少 `storeToRefs` 导入 | plan.md Task 5.1 | 添加 `import { storeToRefs } from 'pinia'` | 待修复 |
| F2 | AddContentDialog 引用不存在的 `ChannelSelectorInline` 组件 | plan.md Task 13.1 | 要么创建 ChannelSelectorInline，要么内联频道选择逻辑 | 待修复 |
| F3 | Store 测试代码未在 plan.md 中展开 | plan.md Task 2 | 补充 3 个 store 的测试代码 | 待修复 |
| F4 | RecycleBin/GovernanceLog/AnnouncementManage/AddContentDialog 测试未展开 | plan.md Task 11-13 | 补充测试代码 | 待修复 |
| F5 | 分页参数不一致：前端 pageNo/pageSize vs 后端 current/size | review.ts | 统一为后端 current/size | **已修复** (2026-06-06) |
| F6 | 请求结构不匹配：审核和治理的请求参数需适配后端实际结构 | review.ts, governance.ts | 按后端 ChannelReviewReq 和 ChannelGovernanceReq 重构 | **已修复** (2026-06-06) |
| F7 | 错误码处理策略缺失 | 全部 API 模块 | 定义各场景的错误处理逻辑 | 待修复 |
| F8 | JVxeTable 未使用：design.md 指定但 plan.md 全部使用基础 Table | Task 8, 10 | 按 design.md 决策使用 JVxeTable | 待修复 |
| F9 | 响应式布局未实现 | 全部组件 | 添加 `< md` 断点的卡片列表切换逻辑 | 待修复 |
| F10 | 公告获取路径偏差：`/api/channel/announcement/{id}` 应为 `/content/channel/announcement/channel/{channelId}` | announcement.ts | 修正路径，参数从 id 改为 channelId | **已修复** (2026-06-06) |
| F11 | 添加内容路径偏差：`/api/channel/content/add` 应为 `/content/channel/publish/add-existing` | addContent.ts | 修正路径 | **已修复** (2026-06-06) |
| F12 | 数据模型与后端 VO 未对齐 | 全部 Store interfaces | 后端 VO 定义后同步更新前端 interfaces | 待修复 |

### ADVISORY — 可选优化

| # | 问题 | 文件 | 建议 |
|---|------|------|------|
| A1 | PublishPermission 直接调用 defHttp 未封装到 API 模块 | plan.md Task 6.3 | 抽取到 publish.ts API 模块 |
| A2 | Tinymce 导入路径待验证 | plan.md Task 12.1 | 确认 `/@/components/Tinymce/index.vue` 存在 |
| A3 | 虚拟滚动未实现 | ChannelSelector | 频道数超 50 时再优化 |
| A4 | PRD 性能指标 P95 <= 500ms 无测试策略 | plan.md | 实施后补充性能测试 |
| A5 | 频道顶部公告展示组件缺失 | plan.md | 补充公告展示组件或标记为 Non-Goal |
| A6 | 埋点集成仅为代码片段，未实际集成到组件中 | plan.md Task 15 | 实施时逐组件集成 |

---

## 结论

**判定: BLOCK→CONDITIONAL PASS**（2026-06-06 更新）

原 4 个 BLOCK 中 3 个已修复，1 个依赖后端实现：

| BLOCK | 状态 | 说明 |
|-------|------|------|
| B1: API 路径系统性偏差 | **已修复** | 29 处路径全部修正为 `/content/channel/...` |
| B2: 治理 API 架构不匹配 | **已修复** | 重构为 `executeGovernance` 统一入口 + action 模式 |
| B3: 审核 API 架构不匹配 | **已修复** | 重构为 `executeReview` 逐条 action 模式 |
| B4: 13 个后端端点缺失 | **待后端实现** | 4 个 P0 端点阻塞前端核心列表功能 |

**剩余前置条件**（进入实施前必须满足）:
1. 后端实现 4 个 P0 端点：`publish/available`、`review/stats`、`governance/content/list`、`governance/recycle-bin/list`
2. 补充 FLAG 级问题（F1-F4, F7-F9, F12）可在实施阶段逐步修复
