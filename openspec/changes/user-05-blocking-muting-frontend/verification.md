# Verification Report: user-05-blocking-muting-frontend

**审核日期**: 2026-06-03
**审核范围**: 前端 change 全部制品 + 后端 API 契约对比
**审核重点**: 后端 API 部分

## Summary

| 维度 | 状态 |
|------|------|
| Completeness | 0/45 tasks 完成，13 个 requirement 待实现 |
| Correctness | 6 个 CRITICAL 后端 API 契约问题，1 个已修复 |
| Coherence | 设计文档与 specs 基本一致，后端依赖需补充 |

---

## CRITICAL Issues（必须修复）

### 1. Filter Rule 缺少 HTTP Controller ✅ 已确认缺失

**问题**: `IContentUserFilterRuleService` 存在完整方法但无 HTTP 端点暴露。

**后端 Service 方法**:
- `saveContentTypeRule(userId, contentType)` — 添加内容类型屏蔽规则
- `saveTopicRule(userId, topic)` — 添加话题屏蔽规则
- `saveTopicRuleWithExpiry(userId, topic, days)` — 添加带过期时间的话题屏蔽规则（临时屏蔽）
- `saveKeywordRule(userId, keyword)` — 添加关键词屏蔽规则
- `saveRegexRule(userId, regex)` — 添加正则屏蔽规则
- `cancelRule(userId, ruleId)` — 删除单条规则
- `batchCancelRules(userId, ruleIds)` — 批量删除规则

**前端依赖**:
- 任务 1.3: 创建 `src/api/content/filterRule.ts`
- 任务 4.1-4.3: 不感兴趣反馈 → 屏蔽此类内容 / 屏蔽该话题
- 任务 6.11-6.13: 屏蔽词设置页（添加/删除/列表）

**建议**: 在 `ContentUserRelationController` 或新建 `ContentUserFilterRuleController` 中暴露以下端点:

```
POST   /content/user/filter-rule                    — 添加规则（body: ruleType + value）
DELETE /content/user/filter-rule/{ruleId}            — 删除单条规则
POST   /content/user/filter-rule/batch-delete        — 批量删除（body: ruleIds[]）
GET    /content/user/filter-rule/list                — 查询规则列表（param: userId, ruleType?）
```

---

### 2. Not Interested 缺少 HTTP Controller ✅ 已确认缺失

**问题**: `IContentUserNotInterestedService.recordFeedback(userId, contentId, contentType)` 无 HTTP 端点。

**前端依赖**:
- 任务 4.1-4.3: 不感兴趣反馈气泡，点击后调用接口

**建议**: 暴露端点:

```
POST /content/user/not-interested — 记录不感兴趣反馈（param: userId, body: {contentId, contentType}）
```

---

### 3. Mute List 缺少分页查询端点 ✅ 已确认缺失

**问题**: `GET /content/user/relation/blacklist` 存在用于黑名单分页查询，但屏蔽列表无对应端点。

**前端依赖**:
- 任务 6.5-6.6: 屏蔽列表管理页 → 屏蔽用户 Tab

**建议**: 新增端点:

```
GET /content/user/relation/mute-list — 分页查询屏蔽用户列表（param: userId, pageNo, pageSize）
```

**返回 VO**: 复用或新建 `ContentUserMuteListPageVO`，包含 `mutedUserId`, `nickname`, `avatar`, `muteTime` 字段。

---

### 4. 后端 API 路径已修复（blacklist → block/unblock）✅ 已修复

**修复内容**:

| 修复前 | 修复后 | 文件 |
|--------|--------|------|
| `POST /content/user/relation/blacklist` | `POST /content/user/relation/block` | `ContentUserRelationController.java` |
| `POST /content/user/relation/blacklist/cancel` | `POST /content/user/relation/unblock` | `ContentUserRelationController.java` |

**原因**: `blacklist` 是名词（集合），单个用户拉黑操作应使用动词 `block`/`unblock`。`GET /blacklist`（集合端点）语义正确，保持不变。

**同步修复的测试文件**:
- `ContentUserRelationControllerWebMvcTest.java` — 2 处路径更新
- `ContentUserControllerWebMvcTest.java` — 1 处路径更新

**验证**: `mvn test` 全量通过（1770 tests, 0 failures）。

---

### 5. 前端 Specs 引用的 API 路径与后端不一致

**问题**: 前端 specs 基于 PRD 定义的路径，与实际后端路径存在系统性偏差。

**路径映射对照表**:

| 前端 Specs 引用 | 实际后端路径 | HTTP Method | 参数风格 |
|----------------|-------------|-------------|----------|
| `POST /api/content/user/block` | `POST /content/user/relation/block` | POST | @RequestParam: userId, targetUserId |
| `POST /api/content/user/unblock` | `POST /content/user/relation/unblock` | POST | @RequestParam: userId, targetUserId |
| `POST /api/content/user/mute` | `POST /content/user/relation/mute` | POST | @RequestParam: userId, targetUserId |
| `POST /api/content/user/unmute` | `POST /content/user/relation/mute/cancel` | POST | @RequestParam: userId, targetUserId |
| `GET /api/content/user/blacklist` | `GET /content/user/relation/blacklist` | GET | @RequestParam: userId, pageNo, pageSize |
| `GET /api/content/user/relation` | `GET /content/user/relation/detail` | GET | @RequestParam: userId, targetUserId |
| `GET /api/content/user/block-mute-help` | `GET /content/user/relation/block-mute/help` | GET | 无参数 |
| `DELETE /api/content/user/filter-rule/:id` | 待创建 | POST | — |
| `POST /api/content/user/not-interested` | 待创建 | POST | — |

**参数风格差异**:
- 前端 PRD 设计: JSON Body + Path Variable
- 实际后端: `@RequestParam` 查询参数（GET/POST 均使用）

**建议**: 更新前端 specs 和 tasks 中的所有 API 路径引用，对齐实际后端实现。

---

### 6. 前端 Specs 使用 DELETE 方法但后端统一使用 POST

**问题**: specs 中屏蔽词删除使用 `DELETE /api/content/user/filter-rule/:id`，但后端所有写操作统一使用 POST。

**建议**: 前端 API 封装统一使用 `defHttp.post`，删除操作通过 POST + body 传递 ID。

---

## WARNING Issues（建议修复）

### 7. ContentUserRelationVO 字段命名注意

**说明**: 后端 `ContentUserRelationVO` 中拉黑状态字段名为 `blacklisted`（boolean），虽然 API 路径已改为 `block`/`unblock`，但 VO 字段名保持 `blacklisted` 不变。前端 Store 缓存时需注意映射。

**字段列表**:
- `blacklisted`: 是否拉黑对方
- `blockedByOwner`: 是否被对方拉黑
- `muted`: 是否屏蔽对方
- `followed`: 是否关注
- `mutualFollow`: 是否互关

### 8. 屏蔽用户列表端点路径待确认

**说明**: 新增 `GET /content/user/relation/mute-list` 路径仅为建议，需与后端开发者确认最终路径。

### 9. ContentBlockMuteHelpVO 已存在

**说明**: `GET /content/user/relation/block-mute/help` 端点已实现，返回 `ContentBlockMuteHelpVO`，包含:
- `blockConfirmation`: 拉黑确认文案
- `muteConfirmation`: 屏蔽确认文案
- `unblockConfirmation`: 解除拉黑文案
- `blockVsMuteComparison`: 拉黑与屏蔽对比说明

前端隐私设置页可直接使用此端点。

---

## SUGGESTION Issues（锦上添花）

### 10. 批量操作端点已存在

**说明**: 后端已有以下批量操作端点，前端可直接使用:
- `POST /content/user/relation/batch/unfollow` — 批量取消关注
- `POST /content/user/relation/batch/special-follow/cancel` — 批量取消特别关注

屏蔽列表的批量取消屏蔽功能需要对应的 filter-rule 批量删除端点（见 CRITICAL #1）。

### 11. 关注推荐端点已存在

**说明**: `GET /content/user/relation/recommendations` 已实现，支持 `interestTag` 参数过滤，前端关注推荐功能可直接对接。

---

## 后端已有端点完整清单

以下端点已实现，前端可直接对接:

| Method | Path | 说明 | 参数 |
|--------|------|------|------|
| POST | `/content/user/relation/follow` | 关注用户 | @RequestParam userId, @RequestBody ContentFollowReq |
| POST | `/content/user/relation/special-follow` | 特别关注 | @RequestParam userId, @RequestBody ContentFollowReq |
| POST | `/content/user/relation/unfollow` | 取消关注 | @RequestParam userId, targetUserId |
| POST | `/content/user/relation/block` | 拉黑用户 | @RequestParam userId, targetUserId |
| POST | `/content/user/relation/unblock` | 解除拉黑 | @RequestParam userId, targetUserId |
| POST | `/content/user/relation/mute` | 屏蔽用户 | @RequestParam userId, targetUserId |
| POST | `/content/user/relation/mute/cancel` | 解除屏蔽 | @RequestParam userId, targetUserId |
| POST | `/content/user/relation/special-follow/cancel` | 取消特别关注 | @RequestParam userId, targetUserId |
| GET | `/content/user/relation/detail` | 查询关系 | @RequestParam userId, targetUserId |
| GET | `/content/user/relation/groups` | 查询关注分组 | @RequestParam userId |
| POST | `/content/user/relation/group/create` | 创建分组 | @RequestParam userId, @RequestBody |
| POST | `/content/user/relation/group/rename` | 重命名分组 | @RequestParam userId, groupId, @RequestBody |
| POST | `/content/user/relation/group/delete` | 删除分组 | @RequestParam userId, groupId |
| POST | `/content/user/relation/group/move` | 移动到分组 | @RequestParam userId, @RequestBody |
| POST | `/content/user/relation/group/remove` | 移出分组 | @RequestParam userId, @RequestBody |
| POST | `/content/user/relation/batch/unfollow` | 批量取消关注 | @RequestParam userId, @RequestBody |
| POST | `/content/user/relation/batch/special-follow/cancel` | 批量取消特别关注 | @RequestParam userId, @RequestBody |
| GET | `/content/user/relation/follow-list` | 关注列表 | @RequestParam userId, relationGroupId?, keyword?, pageNo, pageSize |
| GET | `/content/user/relation/special-follow-list` | 特别关注列表 | @RequestParam userId, pageNo, pageSize |
| GET | `/content/user/relation/blacklist` | 黑名单 | @RequestParam userId, pageNo, pageSize |
| GET | `/content/user/relation/feed` | 关注流动态 | @RequestParam userId, pageNo, pageSize |
| GET | `/content/user/relation/block-mute/help` | 帮助说明 | 无参数 |
| GET | `/content/user/relation/mutual-follow-list` | 互关好友 | @RequestParam userId, keyword?, pageNo, pageSize |
| GET | `/content/user/relation/recommendations` | 关注推荐 | @RequestParam userId, interestTag?, pageNo, pageSize |

## 待创建端点汇总

| Method | Path | 说明 | 优先级 |
|--------|------|------|--------|
| POST | `/content/user/filter-rule` | 添加屏蔽规则 | CRITICAL |
| POST | `/content/user/filter-rule/delete` | 删除屏蔽规则 | CRITICAL |
| POST | `/content/user/filter-rule/batch-delete` | 批量删除屏蔽规则 | CRITICAL |
| GET | `/content/user/filter-rule/list` | 查询屏蔽规则列表 | CRITICAL |
| POST | `/content/user/not-interested` | 不感兴趣反馈 | CRITICAL |
| GET | `/content/user/relation/mute-list` | 屏蔽用户列表 | WARNING |

---

## Final Assessment

**5 个 CRITICAL 问题**（含 1 个已修复）需要在前端实现前解决:
1. Filter Rule Controller 缺失 → 阻塞任务 1.3, 4.1-4.3, 6.11-6.13
2. Not Interested Controller 缺失 → 阻塞任务 4.1-4.3
3. Mute List 端点缺失 → 阻塞任务 6.5-6.6
4. API 路径重命名 → ✅ 已修复
5. 前端 specs API 路径需对齐 → 可在前端实现时同步修正

**建议执行顺序**:
1. 先补充缺失的后端 Controller 端点（CRITICAL #1-3）
2. 更新前端 specs/tasks 中的 API 路径引用（CRITICAL #5）
3. 再开始前端实现
