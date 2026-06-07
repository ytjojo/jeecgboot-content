# 后端遗留代码问题清单

**创建日期**: 2026-06-04
**关联 Change**: user-07-social-extensions-frontend
**说明**: 本文档记录前端 specs 中引用但后端尚未实现的 API 端点，需后端补充后前端才能完成对应功能。

---

## 问题 1：互关状态批量查询接口缺失

**优先级**: P1（影响增量评论加载场景）

**Spec 引用**: `specs/mutual-follow/spec.md` — "Batch query mutual status" 场景

**需求描述**:
前端在增量加载评论时，需要批量查询一批用户的互关状态。评论列表接口自带 `mutualFollow` 字段可覆盖首屏场景，但动态加载新评论时需要独立的批量查询接口。

**建议接口设计**:
```
GET /api/v1/content/user/relation/mutual-status
参数: userIds (逗号分隔的用户ID列表)
返回: Map<userId, boolean>  (userId -> 是否互关)
```

**现有相关代码**:
- Controller: `ContentUserRelationController`（路径 `/api/v1/content/user/relation`）
- Service: `IContentUserRelationService` / `ContentUserRelationServiceImpl`
- Mapper: `ContentUserRelationMapper`

**影响范围**: 任务 7.2（Feed 流私密内容可见性控制）、评论区互关标识增量场景

---

## 问题 2：邀请码校验/查询接口缺失

**优先级**: P1（影响邀请落地页功能）

**Spec 引用**: `specs/invite-system/spec.md` — "Fetch invite info for landing page" 场景、落地页各 Scenario

**需求描述**:
邀请落地页（`/invite/:inviteCode`）需要根据邀请码查询邀请人信息和邀请码有效性（有效/过期/名额已满/无效）。当前 controller 仅有 `POST /generate`（生成邀请码）、`POST /bind`（绑定邀请关系）、`GET /records`（邀请记录）、`GET /stats`（邀请统计）。

**建议接口设计**:
```
GET /api/v1/content/user/invite/info/{inviteCode}
返回: {
  valid: boolean,
  expired: boolean,
  maxReached: boolean,
  inviterNickname: string,
  inviterAvatar: string,
  rewardInfo: string
}
```

**现有相关代码**:
- Controller: `ContentInviteController`（路径 `/api/v1/content/user/invite`）
- Service: `IContentInviteService` / `ContentInviteServiceImpl`
- Entity: `ContentInviteCode`
- Mapper: `ContentInviteCodeMapper`

**影响范围**: 任务 6.1-6.6（邀请落地页全部功能）

---

## 问题 3：治理操作审计日志查询接口缺失

**优先级**: P2（影响审计日志页功能）

**Spec 引用**: `specs/audit-log/spec.md` — "Fetch audit log list" 场景

**需求描述**:
审计日志页需要查询治理操作（删除评论、警告用户、封禁、禁言、撤销等）的日志记录，支持按操作人、操作类型、时间范围筛选。当前 `ContentUserAuditLog` 实体类已存在，但无专用的查询 API 端点。

**建议接口设计**:
```
GET /api/v1/content/user/governance/audit-log
参数: page, pageSize, operatorName, operationType, startTime, endTime
返回: 分页结果，字段包含 operator, operationTime, operationType, targetUser, reason, ipAddress
```

**现有相关代码**:
- Entity: `ContentUserAuditLog`（路径 `jeecg-module-content/.../user/entity/ContentUserAuditLog.java`）
- VO: `ContentUserAuditLogVO`
- Governance Controller: `ContentUserGovernanceController`（路径 `/api/v1/content/user/governance`）
- 注意：`UserStatusAuditLog` 是另一个独立的审计日志实体（用户状态变更），与治理操作审计日志不同

**影响范围**: 任务 9.1-9.4（审计日志页全部功能）

---

## 补充说明

### 已存在的后端 API（前端可直接对接）

以下 API 已在后端实现，前端 specs 已修正为正确路径：

| 功能 | API 路径 | Controller |
|------|---------|------------|
| 互关好友列表 | `GET /api/v1/content/user/relation/mutual-follow-list` | ContentUserRelationController |
| 粉丝列表 | `GET /api/v1/content/user/fan/list` | ContentFanAnalyticsController |
| 粉丝趋势 | `GET /api/v1/content/user/fan/trend` | ContentFanAnalyticsController |
| 邀请码生成 | `POST /api/v1/content/user/invite/generate` | ContentInviteController |
| 邀请记录 | `GET /api/v1/content/user/invite/records` | ContentInviteController |
| 邀请统计 | `GET /api/v1/content/user/invite/stats` | ContentInviteController |
| 删除评论 | `POST /api/v1/content/user/governance/moderator/comment/delete` | ContentUserGovernanceController |
| 警告用户 | `POST /api/v1/content/user/governance/moderator/user/warn` | ContentUserGovernanceController |
