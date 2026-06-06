# 后端遗留代码问题清单

> 记录时间: 2026-06-04
> 来源: verification-review.md 验证结果
> 更新时间: 2026-06-06 — 根据 review-report.md 修复 BLOCK 问题

## 总览

| 分类 | 数量 | 说明 |
|------|------|------|
| 完全缺失的端点 | 13 | spec 中引用但后端未实现 |
| ~~路径偏差~~ | ~~8~~ 0 | **已修复** — plan.md 全部路径已修正为 `/content/channel/...` |
| ~~架构差异~~ | ~~2~~ 0 | **已修复** — plan.md 已重构为统一入口 + action 模式 |

---

## P0 - 完全缺失的端点（阻塞前端开发）

### 1. 发布模块缺失（4 个）

| # | 端点 | 用途 | 建议 |
|---|------|------|------|
| 1 | `GET /content/channel/publish/available` | 获取用户可发布的频道列表 | 在 ChannelPublishController 中新增 |
| 2 | `GET /content/channel/publish/result` | 获取发布结果 | 在 ChannelPublishController 中新增 |
| 3 | `POST/PUT/DELETE /content/channel/publish/scheduled/{id}` | 定时发布 CRUD | 需新增定时发布相关端点 |
| 4 | `GET /content/channel/publish/limit/check` | 发布限额预校验 | 在 ChannelPublishController 中新增 |

### 2. 审核模块缺失（1 个）

| # | 端点 | 用途 | 建议 |
|---|------|------|------|
| 5 | `GET /content/channel/review/stats` | 审核统计（待审总数、超时数） | 在 ChannelContentReviewController 中新增 |

### 3. 治理模块缺失（4 个）

| # | 端点 | 用途 | 建议 |
|---|------|------|------|
| 6 | `GET /content/channel/governance/content/list` | 频道内容列表（带筛选排序分页） | 在 ChannelContentGovernanceController 中新增 |
| 7 | `GET /content/channel/governance/recycle-bin/list` | 回收站列表 | 在 ChannelContentGovernanceController 中新增 |
| 8 | `GET /content/channel/governance/log/list` | 治理日志列表 | 在 ChannelContentGovernanceController 中新增 |
| 9 | `GET /content/channel/governance/edit-assist/history/{contentId}` | 编辑协助历史 | 在 ChannelContentGovernanceController 中新增 |

### 4. 公告模块缺失（3 个）

| # | 端点 | 用途 | 建议 |
|---|------|------|------|
| 10 | `POST /content/channel/announcement/preview` | 公告预览安全过滤 | 在 ChannelAnnouncementController 中新增 |
| 11 | `GET /content/channel/announcement/{channelId}/history` | 公告历史版本 | 在 ChannelAnnouncementController 中新增 |
| 12 | `POST /content/channel/announcement/restore/{versionId}` | 历史版本恢复 | 在 ChannelAnnouncementController 中新增 |

### 5. 添加内容模块缺失（1 个）

| # | 端点 | 用途 | 建议 |
|---|------|------|------|
| 13 | `GET /content/channel/publish/add-existing/search` | 可添加内容搜索 | 在 ChannelPublishController 中新增 |

---

## ~~P1 - 架构差异（需适配）~~ — 已修复

### ~~1. 治理操作统一入口~~ — 已修复

**修复内容**: plan.md 中 `governance.ts` 已重构为 `executeGovernance` 统一入口函数，使用 `action` 字段区分操作类型（PIN/UNPIN/FEATURE/UNFEATURE/DELETE/RESTORE/MOVE/EDIT_ASSIST）。Store 层 `useChannelGovernanceStore` 已同步更新。

### ~~2. 审核操作统一入口~~ — 已修复

**修复内容**: plan.md 中 `review.ts` 已重构为 `executeReview` 函数，使用 `{reviewId, action: 'APPROVE'|'REJECT', rejectReason?}` 逐条审核模式。批量审核改为前端循环调用。Store 层 `useChannelReviewStore` 已同步更新。

**注意**: 存在两个审核控制器：
- `ChannelContentReviewController` (`/content/channel/review`) - 内容发布审核
- `ChannelReviewController` (`/jeecg-boot/api/v1/content/channel/review`) - 频道创建审核

---

## ~~P2 - 路径偏差（需修正 spec）~~ — 已修复

plan.md 中所有 API 路径已修正为 `/content/channel/...` 格式，与后端实际路径一致。

| 修正前 | 修正后 |
|--------|--------|
| `/api/channel/publish/submit` | `/content/channel/publish` |
| `/api/channel/review/list` | `/content/channel/review/list` |
| `/api/channel/governance/pin` | `/content/channel/governance` (action=PIN) |
| `/api/channel/announcement/{channelId}` | `/content/channel/announcement/channel/{channelId}` |
| `/api/channel/content/add` | `/content/channel/publish/add-existing` |

---

## 已存在的后端端点（可直接对接）

| 控制器 | 端点 | 用途 |
|--------|------|------|
| ChannelPublishController | `POST /content/channel/publish` | 提交发布 |
| ChannelContentReviewController | `POST /content/channel/review` | 内容审核（action 区分） |
| ChannelReviewController | `GET /jeecg-boot/api/v1/content/channel/review/list` | 频道审核列表 |
| ChannelContentGovernanceController | `POST /content/channel/governance` | 治理操作（action 区分） |
| ChannelAnnouncementController | CRUD `/content/channel/announcement/*` | 公告管理（3 个端点） |
| ChannelPublishController | `POST /content/channel/publish/add-existing` | 添加已有内容 |

---

## 附加修复（2026-06-06）

- **分页参数统一**: 所有列表接口分页参数从 `pageNo/pageSize` 改为 `current/size`，与后端 MyBatis-Plus `Page<T>` 一致
- **治理操作签名变更**: `deleteContent` 从批量(`contentIds[]`)改为逐条(`contentId`)，与后端 `ChannelGovernanceReq` 一致
- **审核操作签名变更**: `approveReview`/`rejectReview` 从批量(`ids[]`)改为逐条(`reviewId`)，与后端 `ChannelReviewReq` 一致

## 实施建议

1. **短期**（阻塞前端）: 实现 P0 中 #1, #5, #6, #7 共 4 个核心端点
2. **中期**: 实现剩余 P0 端点
3. ~~**长期**: 统一路径前缀~~ — **已完成**（2026-06-06）
