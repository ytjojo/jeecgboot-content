# 后端遗留代码问题 — circle-10-core-frontend

**创建日期**: 2026-06-04
**关联 Change**: circle-10-core-frontend
**状态**: 待后端处理

---

## 概述

前端圈子模块依赖 14 个后端 API，当前仅 8 个已实现。本文档列出所有后端需补充的接口和字段，按优先级排序。

---

## P0 — 阻塞前端核心页面开发

### 1. 圈子详情接口

- **接口**: `GET /content/circle/{id}`
- **返回**: `CircleVO`（需补充字段，见下方）
- **影响**: 圈子详情页 (`/circle/:id`) 无法加载，影响详情展示、加入/退出操作、成员管理入口
- **现有代码位置**: `CircleController.java` 缺少 detail 端点
- **建议实现**: 在 `CircleController` 中新增 `@GetMapping("/{id}")` 方法，调用 `ICircleBiz` 或 `ICircleService`

### 2. 我的圈子列表接口

- **接口**: `GET /content/circle/my-list`
- **参数**: `pageNum`, `pageSize`（分页）
- **返回**: 分页 `CircleVO` 列表
- **影响**: 圈子列表页「已加入」Tab 无法加载
- **建议实现**: 新增端点，查询 `content_circle_member` 表关联 `content_circle` 表，按加入时间倒序

### 3. 公开圈子列表接口

- **接口**: `GET /content/circle/public-list`
- **参数**: `pageNum`, `pageSize`（分页）
- **返回**: 分页 `CircleVO` 列表
- **影响**: 圈子列表页「发现」Tab 无法加载
- **注意**: `CircleRankingController` 已有 `/api/circle/ranking/hot` 和 `/api/circle/ranking/new`，可参考其实现
- **建议实现**: 新增端点，查询 `privacyType = PUBLIC AND status = ACTIVE` 的圈子，按 memberCount 倒序

### 4. 成员列表接口

- **接口**: `GET /content/circle/member/list`
- **参数**: `circleId`, `role`（可选筛选）, `status`（可选筛选）, `keyword`（昵称搜索，可选）, `pageNum`, `pageSize`
- **返回**: 分页成员列表（含头像、昵称、角色、状态、加入时间）
- **影响**: 成员管理页 (`/circle/:id/members`) 无法加载
- **建议实现**: 在 `CircleMemberController` 中新增 list 端点，查询 `content_circle_member` 表

---

## P1 — 影响功能完整性

### 5. 名称唯一性校验接口

- **接口**: `GET /content/circle/check-name`
- **参数**: `name`（圈子名称）
- **返回**: `{ available: true/false }`
- **影响**: 创建圈子时名称唯一性实时校验无法调用
- **现有代码**: `CircleServiceImpl.checkNameUnique(String name)` 已实现校验逻辑，但仅在 `createCircle` 内部调用，未暴露为 REST API
- **建议实现**: 在 `CircleController` 中新增 `@GetMapping("/check-name")` 方法，复用已有 service 方法

### 6. 治理日志查询接口

- **接口**: `GET /content/circle/{circleId}/governance-log`
- **参数**: `circleId`, `action`（可选筛选：MUTE/UNMUTE/REMOVE/ROLE_CHANGE）, `targetKeyword`（操作对象昵称，可选）, `startDate`, `endDate`, `pageNum`, `pageSize`
- **返回**: 分页治理日志列表（时间、操作者、操作对象、操作类型、详情）
- **影响**: 治理日志页 (`/circle/:id/governance-log`) 无法加载
- **现有代码**: `ICircleGovernanceLogService` 仅有写入方法（logMute/logUnmute/logRemove/logRoleChange），`CircleGovernanceLogMapper` 已存在，缺少查询方法和 Controller 端点
- **建议实现**:
  1. 在 `ICircleGovernanceLogService` 中新增 `Page<CircleGovernanceLog> listLogs(String circleId, String action, String startDate, String endDate, int pageNum, int pageSize)` 方法
  2. 新建 `CircleGovernanceLogController` 或在现有控制器中新增端点

---

## 需补充的字段

### CircleVO 补充字段

当前 `CircleVO` 定义于 `jeecg-module-content/.../circle/vo/CircleVO.java`，需补充以下字段：

| 字段 | 类型 | 说明 | 用途 |
|------|------|------|------|
| `applyStatus` | `String` | 当前用户的申请状态：PENDING / APPROVED / REJECTED / null | 前端加入按钮状态判断 |
| `isInvited` | `Boolean` | 当前用户是否受邀加入该圈子 | 前端邀请加入按钮状态判断 |

**说明**: 这两个字段需要在详情接口中根据当前用户 ID 查询 `content_circle_join_request` 表和邀请表后填充，不是 Circle 实体的直接字段。

### CircleSearchResultVO 补充字段

当前定义于 `jeecg-module-content/.../circle/vo/CircleSearchResultVO.java`，需补充：

| 字段 | 类型 | 说明 |
|------|------|------|
| `category` | `String` | 圈子分类标签 |

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
| `/api/circle/{circleId}/data/export` | GET | CircleDataController | 数据导出 CSV |
| `/content/circle/recommend` | GET | CircleRecommendController | 推荐圈子 |
| `/content/circle/recommend/click` | POST | CircleRecommendController | 推荐点击 |
| `/content/circle/recommend/join` | POST | CircleRecommendController | 推荐加入 |

---

## 接口命名对照表

| 设计文档名称 | 后端实际路径 | 方法 | 一致性 |
|-------------|-------------|------|--------|
| createCircle | /content/circle/create | POST | 一致 |
| updateCircle | /content/circle/update | PUT | 一致 |
| getCircleDetail | **不存在** | - | 需新增 |
| getMyCircleList | **不存在** | - | 需新增 |
| getPublicCircleList | **不存在** | - | 需新增 |
| checkCircleName | **不存在** | - | 需新增（service 已有） |
| joinCircle | /content/circle/join | POST | 一致 |
| quitCircle | /content/circle/leave | POST | 命名不同（quit vs leave） |
| getMemberList | **不存在** | - | 需新增 |
| setModerator | /content/circle/member/change-role | POST | 命名不同 |
| muteMember | /content/circle/member/mute | POST | 一致 |
| unmuteMember | /content/circle/member/unmute | POST | 一致 |
| removeMember | /content/circle/member/remove | POST | 一致 |
| searchCircles | /content/circle/search | GET | 一致 |
| getGovernanceLogList | **不存在** | - | 需新增 |
