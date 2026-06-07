# 验证审核文档：user-07-social-extensions-frontend

**验证日期**: 2026-06-04
**验证范围**: 后端 API 存在性、前后端接口一致性、文档完整性

## 验证结果摘要

| 维度 | 状态 |
|------|------|
| 完成度 | 0/53 任务完成 |
| 后端 API 一致性 | 6 个路径不匹配，3 个端点缺失 |
| 文档完整性 | proposal + design + 7 个 specs 齐全 |

## 后端 API 验证详情

### 1. 互关关系 API (`ContentUserRelationController`)

**实际基础路径**: `/api/v1/content/user/relation`

| Spec 中的路径 | 实际路径 | 状态 |
|--------------|---------|------|
| `GET /content/user-relation/mutual-follow-list` | `GET /api/v1/content/user/relation/mutual-follow-list` | 路径不匹配（`user-relation` vs `user/relation`） |
| `GET /content/user-relation/mutual-status` | 不存在 | **缺失** - controller 中无此端点 |

**详情**:
- `/mutual-follow-list` 端点存在（第 278 行），但路径前缀应为 `/api/v1/content/user/relation` 而非 `/content/user-relation`
- `/mutual-status` 端点在 controller 中不存在，仅有 `/mutual-follow-list`、`/recommendations`、`/follow-list` 等端点

### 2. 粉丝分析 API (`ContentFanAnalyticsController`)

**实际基础路径**: `/api/v1/content/user/fan`

| Spec 中的路径 | 实际路径 | 状态 |
|--------------|---------|------|
| `GET /content/fan-analytics/list` | `GET /api/v1/content/user/fan/list` | 路径不匹配 |
| `GET /content/fan-analytics/trend` | `GET /api/v1/content/user/fan/trend` | 路径不匹配 |

**详情**:
- 两个端点都存在，但基础路径完全不同：spec 用 `fan-analytics`，实际用 `user/fan`

### 3. 邀请系统 API (`ContentInviteController`)

**实际基础路径**: `/api/v1/content/user/invite`

| Spec 中的路径 | 实际路径 | 状态 |
|--------------|---------|------|
| `GET /content/invite/code` | `POST /api/v1/content/user/invite/generate` | **方法+路径不匹配**（GET vs POST，且路径不同） |
| `GET /content/invite/info/:inviteCode` | 不存在 | **缺失** - controller 中无此端点 |
| `GET /content/invite/records` | `GET /api/v1/content/user/invite/records` | 路径不匹配 |
| `GET /content/invite/stats` | `GET /api/v1/content/user/invite/stats` | 路径不匹配 |

**详情**:
- 邀请码生成是 `POST /generate` 而非 `GET /code`
- 邀请码校验/查询端点（`/info/:inviteCode`）不存在
- `POST /bind` 端点存在（用于绑定邀请关系）

### 4. 治理操作 API (`ContentUserGovernanceController`)

**实际基础路径**: `/api/v1/content/user/governance`

| Spec 中的路径 | 实际路径 | 状态 |
|--------------|---------|------|
| `POST /content/user-governance/delete-comment` | `POST /api/v1/content/user/governance/moderator/comment/delete` | **路径不匹配**（路径结构和端点名都不同） |
| `POST /content/user-governance/warn-user` | `POST /api/v1/content/user/governance/moderator/user/warn` | **路径不匹配** |

**详情**:
- 两个端点都存在，但路径结构差异大：spec 用扁平结构，实际用 `/moderator/` 前缀嵌套
- 实际端点名是 `/moderator/comment/delete` 和 `/moderator/user/warn`

### 5. 审计日志 API

| Spec 中的路径 | 实际路径 | 状态 |
|--------------|---------|------|
| `GET /content/user-governance/audit-log` | 不存在 | **缺失** - 无审计日志查询端点 |

**详情**:
- `ContentUserAuditLog` 实体类存在，但无专用的查询 API controller 端点
- `UserStatusController`（路径 `/api/v1/content/user-status`）有 `/history` 端点，但这是用户状态变更历史，不是治理操作审计日志

## Spec 文档 API 路径汇总（需修正）

| Spec 文件 | 引用的 API 路径 | 应修正为 |
|-----------|----------------|---------|
| mutual-follow/spec.md | `GET /content/user-relation/mutual-follow-list` | `GET /api/v1/content/user/relation/mutual-follow-list` |
| mutual-follow/spec.md | `GET /content/user-relation/mutual-status` | 需后端补充或移除此需求 |
| fan-analytics/spec.md | `GET /content/fan-analytics/list` | `GET /api/v1/content/user/fan/list` |
| fan-analytics/spec.md | `GET /content/fan-analytics/trend` | `GET /api/v1/content/user/fan/trend` |
| invite-system/spec.md | `GET /content/invite/code` | `POST /api/v1/content/user/invite/generate`（方法和路径都不同） |
| invite-system/spec.md | `GET /content/invite/info/:inviteCode` | 需后端补充 |
| invite-system/spec.md | `GET /content/invite/records` | `GET /api/v1/content/user/invite/records` |
| invite-system/spec.md | `GET /content/invite/stats` | `GET /api/v1/content/user/invite/stats` |
| moderation/spec.md | `POST /content/user-governance/delete-comment` | `POST /api/v1/content/user/governance/moderator/comment/delete` |
| moderation/spec.md | `POST /content/user-governance/warn-user` | `POST /api/v1/content/user/governance/moderator/user/warn` |
| audit-log/spec.md | `GET /content/user-governance/audit-log` | 需后端补充 |

## 前端文档问题列表

### CRITICAL（必须修复）

1. **所有 specs 中的 API 路径与实际后端不一致** — 前缀模式完全不同：
   - Spec 使用 `content/{feature}` 模式（如 `content/fan-analytics`）
   - 实际使用 `content/user/{feature}` 模式（如 `content/user/fan`）

2. **3 个后端端点缺失**：
   - `GET mutual-status`（互关状态批量查询）— 用于增量评论加载场景
   - `GET /info/:inviteCode`（邀请码校验）— 用于落地页校验邀请码有效性
   - `GET audit-log`（审计日志查询）— 用于审计日志页

3. **邀请码生成方法错误** — Spec 写的是 GET 请求获取/生成，实际是 POST 请求生成

### WARNING（建议修复）

1. **design.md 中的 API 路径也需同步修正** — 确保 design 文档与 specs 一致

2. **invite-system/spec.md 中"Reuse existing invite code"场景** — Spec 描述首次访问自动生成、后续复用，但实际 POST /generate 是显式生成操作，语义不同

3. **moderation/spec.md 中 API 请求体字段名** — Spec 用 `{ commentId, reason }` 和 `{ userId, reason }`，需确认实际 controller 的参数名是否一致

### SUGGESTION（可选优化）

1. **proposal.md 中"对接 10+ 个后端接口"** — 实际确认的后端端点约 8 个（含缺失的），数量描述可更精确

## 建议修复方案

### 方案 A：修正前端 Spec（推荐）
1. 修正所有 specs 中的 API 路径，使其与实际后端一致
2. 对于 3 个缺失的端点，在 `backend-issues.md` 中记录，标注需要后端补充
3. 修正 design.md 中的 API 路径描述

### 方案 B：修正后端路径（不推荐）
- 修改后端 controller 路径以匹配 spec — 风险高，可能影响已有的前端调用

**推荐方案 A**：前端 spec 应该适配已存在的后端 API，缺失的端点单独追踪。
