# 后端遗留代码问题 — circle-10-core-frontend

**更新日期**: 2026-06-08（最终验证）
**关联 Change**: circle-10-core-frontend
**状态**: ✅ 15 个接口全部实现，所有关键字段已补充

---

## 概述

前端圈子模块依赖 15 个后端 API。2026-06-08 最终验证确认：**15 个接口全部已实现，CircleVO/CircleSearchResultVO/CircleMemberVO 字段完整**。

---

## ✅ 已解决 — 接口全部就绪

| # | 接口 | 实际路径 | 方法 | 控制器 |
|---|------|---------|------|--------|
| 1 | 创建圈子 | `/api/v1/content/circle/create` | POST | CircleController |
| 2 | 更新圈子 | `/api/v1/content/circle/update` | PUT | CircleController |
| 3 | 圈子详情 | `/api/v1/content/circle/{id}` | GET | CircleController |
| 4 | 圈子详情(备) | `/api/v1/content/circle/detail?id={id}` | GET | CircleController |
| 5 | 加入圈子 | `/api/v1/content/circle/join` | POST | CircleController |
| 6 | 退出圈子 | `/api/v1/content/circle/leave` | POST | CircleController | Body: `CircleLeaveReq` |
| 7 | 名称校验 | `/api/v1/content/circle/check-name?name=xxx` | GET | CircleController |
| 8 | 我的列表 | `/api/v1/content/circle/my-list?pageNum=1&pageSize=20` | GET | CircleController |
| 9 | 公开列表 | `/api/v1/content/circle/public-list?pageNum=1&pageSize=20` | GET | CircleController |
| 10 | 成员列表 | `/api/v1/content/circle/member/list?circleId=xxx&role=&status=&pageNum=1&pageSize=50` | GET | CircleMemberController |
| 11 | 变更角色 | `/api/v1/content/circle/member/change-role` | POST | CircleMemberController |
| 12 | 禁言 | `/api/v1/content/circle/member/mute` | POST | CircleMemberController |
| 13 | 解除禁言 | `/api/v1/content/circle/member/unmute` | POST | CircleMemberController | Body: `CircleMemberUpdateReq` |
| 14 | 移除成员 | `/api/v1/content/circle/member/remove` | POST | CircleMemberController |
| 15 | 搜索 | `/api/v1/content/circle/search?keyword=&pageNum=1&pageSize=20` | GET | CircleSearchController | 返回 `Page<CircleSearchResultVO>` |
| 16 | 治理日志 | `/api/v1/content/circle/governance-log/list?circleId=xxx&pageNum=1&pageSize=20` | GET | CircleGovernanceLogController |

---

## ✅ 已解决 — VO 字段完整

### CircleVO（15 个字段，全部就绪）

| 字段 | 类型 | 说明 | 状态 |
|------|------|------|------|
| id | String | 圈子ID | ✅ |
| name | String | 圈子名称 | ✅ |
| description | String | 圈子简介 | ✅ |
| iconUrl | String | 图标URL | ✅ |
| coverUrl | String | 封面图URL | ✅ |
| category | String | 分类标签 | ✅ |
| privacyType | String | 隐私类型 | ✅ |
| joinType | String | 加入方式 | ✅ |
| creatorId | String | 创建者ID | ✅ |
| memberCount | Integer | 成员数 | ✅ |
| maxMemberCount | Integer | 最大成员数（=memberLimit） | ✅ |
| status | String | 状态 | ✅ |
| joined | Boolean | 当前用户是否已加入 | ✅ |
| myRole | String | 当前用户角色（CREATOR/MODERATOR/MEMBER） | ✅ |
| **applyStatus** | String | **申请状态: PENDING/APPROVED/REJECTED/null** | ✅ 新增 |
| **isInvited** | Boolean | **当前用户是否被邀请** | ✅ 新增 |
| createTime | Date | 创建时间 | ✅ |

### CircleSearchResultVO（7 个字段，全部就绪）

| 字段 | 类型 | 说明 | 状态 |
|------|------|------|------|
| id | String | 圈子ID | ✅ |
| name | String | 圈子名称 | ✅ |
| iconUrl | String | 图标URL | ✅ |
| description | String | 简介 | ✅ |
| **category** | String | **分类标签** | ✅ 新增 |
| memberCount | Integer | 成员数 | ✅ |
| joined | Boolean | 当前用户是否已加入 | ✅ |

### CircleMemberVO（6 个字段，新增）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | String | 成员记录ID |
| userId | String | 用户ID |
| role | String | 角色（CREATOR/MODERATOR/MEMBER） |
| status | String | 状态（ACTIVE/MUTED/REMOVED） |
| muteEndTime | LocalDateTime | 禁言结束时间 |
| createTime | LocalDateTime | 加入时间 |

---

## ✅ 参数风格已统一

所有接口参数风格已统一，无遗留问题：

- **写操作**: 全部使用 `@RequestBody`（`CircleCreateReq` / `CircleUpdateReq` / `CircleJoinReq` / `CircleLeaveReq` / `CircleMemberUpdateReq`）
- **读操作**: 详情使用 Path 参数 `/{id}`，列表/搜索使用 Query 参数 + `pageNum`/`pageSize`
- **返回格式**: 所有列表/搜索接口统一返回 `Page<T>` 分页对象

---

## 已有但设计文档未引用的接口

以下后端接口已存在，设计文档未引用，可能在后续 EPIC 中使用：

| 路径 | 方法 | 控制器 | 说明 |
|------|------|--------|------|
| `/api/circle/ranking/hot` | GET | CircleRankingController | 热门圈子榜单 |
| `/api/circle/ranking/new` | GET | CircleRankingController | 新增圈子榜单 |
| `/circle-join-review/pending/{circleId}` | GET | CircleJoinReviewController | 待审核申请列表 |
| `/circle-join-review/approve` | POST | CircleJoinReviewController | 批准加入申请 |
| `/circle-join-review/reject` | POST | CircleJoinReviewController | 拒绝加入申请 |
| `/api/circle/{circleId}/data/statistics` | GET | CircleDataController | 圈子数据统计 |
| `/api/v1/content/circle/recommend` | GET | CircleRecommendController | 推荐圈子 |
