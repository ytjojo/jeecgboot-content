# 验证审核文档 — circle-10-core-frontend

**验证日期**: 2026-06-04
**验证范围**: design.md、proposal.md、specs/*.md 与后端代码库一致性

---

## 1. 验证结果摘要

| 维度 | 状态 | 说明 |
|------|------|------|
| 后端 API 存在性 | **不通过** | 14 个接口中仅 8 个存在，6 个缺失 |
| 前后端字段一致性 | **不通过** | CircleVO 缺少前端依赖的 5 个字段 |
| 接口命名一致性 | **不通过** | 退出接口命名不一致（quit vs leave） |
| 文档完整性 | **通过** | design.md、proposal.md、specs 结构完整 |
| 搜索结果字段 | **不通过** | CircleSearchResultVO 缺少 category 字段 |

---

## 2. 后端 API 验证详情

### 2.1 已存在的 API（8 个）

| 设计文档名称 | 实际路径 | 方法 | 控制器 |
|-------------|---------|------|--------|
| createCircle | `/content/circle/create` | POST | CircleController |
| updateCircle | `/content/circle/update` | PUT | CircleController |
| joinCircle | `/content/circle/join` | POST | CircleController |
| quitCircle | `/content/circle/leave` | POST | CircleController |
| setModerator | `/content/circle/member/change-role` | POST | CircleMemberController |
| muteMember | `/content/circle/member/mute` | POST | CircleMemberController |
| unmuteMember | `/content/circle/member/unmute` | POST | CircleMemberController |
| removeMember | `/content/circle/member/remove` | POST | CircleMemberController |

**注意**: searchCircles (`GET /content/circle/search`) 也存在，由 CircleSearchController 提供。

### 2.2 缺失的 API（6 个）

| 设计文档名称 | 期望路径 | 缺失影响 | 严重程度 |
|-------------|---------|---------|---------|
| **getCircleDetail** | `GET /content/circle/{id}` | 详情页无法加载 | **P0 阻塞** |
| **getMyCircleList** | `GET /content/circle/my-list` | 已加入 Tab 无法加载 | **P0 阻塞** |
| **getPublicCircleList** | `GET /content/circle/public-list` | 发现 Tab 无法加载 | **P0 阻塞** |
| **checkCircleName** | `GET /content/circle/check-name` | 名称唯一性校验无法调用 | **P1 重要** |
| **getMemberList** | `GET /content/circle/member/list` | 成员管理页无法加载 | **P0 阻塞** |
| **getGovernanceLogList** | `GET /content/circle/governance-log/list` | 治理日志页无法加载 | **P1 重要** |

### 2.3 已有但未在设计中引用的 API

| 路径 | 方法 | 控制器 | 说明 |
|------|------|--------|------|
| `/api/circle/ranking/hot` | GET | CircleRankingController | 热门圈子榜单 |
| `/api/circle/ranking/new` | GET | CircleRankingController | 新增圈子榜单 |
| `/circle-join-review/pending/{circleId}` | GET | CircleJoinReviewController | 待审核申请列表 |
| `/circle-join-review/approve` | POST | CircleJoinReviewController | 批准加入申请 |
| `/circle-join-review/reject` | POST | CircleJoinReviewController | 拒绝加入申请 |
| `/api/circle/{circleId}/data/statistics` | GET | CircleDataController | 圈子数据统计 |

---

## 3. 数据模型不一致

### 3.1 CircleVO 字段缺失

当前 `CircleVO` 字段：
```
id, name, description, iconUrl, coverUrl, category, privacyType, joinType,
creatorId, memberCount, maxMemberCount, status, joined, myRole, createTime
```

前端 design.md 和 specs 依赖但 **CircleVO 中缺失**的字段：

| 字段 | 类型 | 用途 | 引用位置 |
|------|------|------|---------|
| `applyStatus` | String | 申请状态（PENDING/APPROVED/REJECTED） | design.md D4, specs circle-member-management |
| `isInvited` | Boolean | 当前用户是否受邀 | design.md D4, specs circle-member-management |
| `memberLimit` | Integer | 成员上限（区别于 maxMemberCount） | design.md 提到"默认 500 人" |

**说明**: `maxMemberCount` 可能等同于 `memberLimit`，需确认。`applyStatus` 和 `isInvited` 是前端加入按钮状态的核心驱动字段，必须补充。

### 3.2 CircleSearchResultVO 字段缺失

当前字段：`id, name, iconUrl, description, memberCount, joined`

缺失字段：`category`（分类标签）— specs 中搜索结果要求展示分类。

### 3.3 接口命名不一致

| 设计文档 | 后端实际 | 建议 |
|---------|---------|------|
| quitCircle | `/content/circle/leave` | 前端 API 封装使用 `leaveCircle` 名称，与后端一致 |

---

## 4. 后端治理日志服务分析

`ICircleGovernanceLogService` 仅提供**写入**方法：
- `logMute()` — 记录禁言
- `logUnmute()` — 记录解除禁言
- `logRemove()` — 记录移除
- `logRoleChange()` — 记录角色变更

**缺失**: 没有查询方法（`listLogs`/`getGovernanceLogs`），没有 REST 控制器暴露查询接口。`CircleGovernanceLog` 实体已存在，Mapper 已存在，但缺少分页查询的 Service 方法和 Controller 端点。

---

## 5. 前端文档问题列表

### 5.1 design.md 问题

| # | 问题 | 位置 | 建议 |
|---|------|------|------|
| D-1 | 提到"14 个 API 接口"但实际只有 8 个后端接口存在 | Context 段 | 更新为实际数量，标注缺失接口 |
| D-2 | 未提及后端缺失接口的风险 | Risks 段 | 新增 Risk: 后端 6 个接口未实现 |
| D-3 | Open Questions 中未提及 getCircleDetail 返回字段 | Open Questions | 补充: applyStatus/isInvited 字段确认 |

### 5.2 proposal.md 问题

| # | 问题 | 位置 | 建议 |
|---|------|------|------|
| P-1 | "依赖后端 14 个接口"描述不准确 | Impact 段 | 更新为实际数量 |
| P-2 | 未标注哪些接口已存在、哪些需后端补充 | Impact 段 | 分类列出 |

### 5.3 specs 问题

| # | 问题 | 文件 | 建议 |
|---|------|------|------|
| S-1 | 圈子列表页引用 my-list/public-list 接口不存在 | circle-crud/spec.md | 需后端补充或前端 Mock |
| S-2 | 名称唯一性校验引用 check-name 接口不存在 | circle-crud/spec.md | 后端 checkNameUnique 仅内部调用，需暴露 API |
| S-3 | 成员列表引用 getMemberList 接口不存在 | circle-member-management/spec.md | 需后端补充 |
| S-4 | 治理日志引用 getGovernanceLogList 接口不存在 | circle-governance-log/spec.md | 需后端补充 |
| S-5 | applyStatus/isInvited 字段在后端 CircleVO 中不存在 | circle-member-management/spec.md | 需后端补充到详情接口响应 |

---

## 6. 建议修复方案

### 6.1 后端需补充的接口（P0 — 阻塞前端开发）

1. **圈子详情接口**: `GET /content/circle/{id}` 返回 CircleVO（需补充 applyStatus、isInvited 字段）
2. **我的圈子列表**: `GET /content/circle/my-list` 分页返回当前用户已加入的圈子
3. **公开圈子列表**: `GET /content/circle/public-list` 分页返回公开圈子
4. **成员列表接口**: `GET /content/circle/member/list?circleId=xxx` 分页返回成员列表

### 6.2 后端需补充的接口（P1 — 影响功能完整性）

5. **名称唯一性校验**: `GET /content/circle/check-name?name=xxx` 暴露已有 service 方法
6. **治理日志查询**: `GET /content/circle/{circleId}/governance-log` 分页查询治理日志

### 6.3 后端需补充的字段

7. **CircleVO 补充字段**: `applyStatus`(String)、`isInvited`(Boolean)
8. **CircleSearchResultVO 补充字段**: `category`(String)

### 6.4 前端文档修复

9. 更新 design.md 中的接口数量和风险描述
10. 更新 proposal.md 中的 Impact 段
11. 在 specs 中标注缺失接口，添加 Mock 开发策略

---

## 7. 前端开发策略建议

由于 6 个后端接口缺失，建议前端采用以下策略：

1. **优先开发已有接口的功能**: 圈子创建（create）、更新（update）、加入（join）、退出（leave）、成员管理操作（change-role/mute/unmute/remove）、搜索（search）
2. **Mock 开发缺失接口**: 在 `src/api/content/circle.ts` 中定义完整接口类型，对缺失接口使用 Mock 数据
3. **后端接口就绪后切换**: API 层独立封装，切换成本低（与 design.md R1 风险应对一致）
