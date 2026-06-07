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

- **接口**: `GET /api/v1/content/circle/{id}`
- **返回**: `CircleVO`（需补充字段，见下方）
- **影响**: 圈子详情页 (`/circle/:id`) 无法加载，影响详情展示、加入/退出操作、成员管理入口
- **现有代码位置**: `CircleController.java` 缺少 detail 端点
- **建议实现**: 在 `CircleController` 中新增 `@GetMapping("/{id}")` 方法，调用 `ICircleBiz` 或 `ICircleService`

### 2. 我的圈子列表接口

- **接口**: `GET /api/v1/content/circle/my-list`
- **参数**: `pageNum`, `pageSize`（分页）
- **返回**: 分页 `CircleVO` 列表
- **影响**: 圈子列表页「已加入」Tab 无法加载
- **建议实现**: 新增端点，查询 `content_circle_member` 表关联 `content_circle` 表，按加入时间倒序

### 3. 公开圈子列表接口

- **接口**: `GET /api/v1/content/circle/public-list`
- **参数**: `pageNum`, `pageSize`（分页）
- **返回**: 分页 `CircleVO` 列表
- **影响**: 圈子列表页「发现」Tab 无法加载
- **注意**: `CircleRankingController` 已有 `/api/circle/ranking/hot` 和 `/api/circle/ranking/new`，可参考其实现
- **建议实现**: 新增端点，查询 `privacyType = PUBLIC AND status = ACTIVE` 的圈子，按 memberCount 倒序

### 4. 成员列表接口

- **接口**: `GET /api/v1/content/circle/member/list`
- **参数**: `circleId`, `role`（可选筛选）, `status`（可选筛选）, `keyword`（昵称搜索，可选）, `pageNum`, `pageSize`
- **返回**: 分页成员列表（含头像、昵称、角色、状态、加入时间）
- **影响**: 成员管理页 (`/circle/:id/members`) 无法加载
- **建议实现**: 在 `CircleMemberController` 中新增 list 端点，查询 `content_circle_member` 表

---

## P1 — 影响功能完整性

### 5. 名称唯一性校验接口

- **接口**: `GET /api/v1/content/circle/check-name`
- **参数**: `name`（圈子名称）
- **返回**: `{ available: true/false }`
- **影响**: 创建圈子时名称唯一性实时校验无法调用
- **现有代码**: `CircleServiceImpl.checkNameUnique(String name)` 已实现校验逻辑，但仅在 `createCircle` 内部调用，未暴露为 REST API
- **建议实现**: 在 `CircleController` 中新增 `@GetMapping("/check-name")` 方法，复用已有 service 方法

### 6. 治理日志查询接口

- **接口**: `GET /api/v1/content/circle/{circleId}/governance-log`
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

### 成员上限默认值对齐

**问题**: 前端 PRD 定义成员上限默认值为 500 人，后端 circle-member-management spec 场景中提到"圈子成员数已达到上限（10,000 人）"。前后端默认值不一致。

**影响**: 前端展示"成员数/上限"时可能显示错误的上限值，满员判断可能出错。

**建议**: 成员上限由后端 `maxMemberCount` 字段驱动，前端不做默认值假设。后端确认：
1. `maxMemberCount` 的默认值是多少？
2. 是否允许创建时自定义上限？
3. `maxMemberCount` 是否等同于 PRD 中的 `memberLimit`？

### 前端需处理的错误消息

后端使用 `JeecgBootException` + 中文消息字符串返回错误（非错误码枚举）。前端需根据 `message` 字段匹配并做对应处理：

| 错误场景 | 后端返回消息 | 前端处理 |
|---------|------------|---------|
| 圈子不存在 | "圈子不存在" | 展示 404 页面 |
| 名称已存在 | 创建时抛异常 | 行内提示 "该圈子名称已存在" |
| 已是成员 | "您已是圈子成员" | Toast 提示，按钮变为「已加入」 |
| 申请已提交 | "申请已提交，请等待审核" | Toast 提示，按钮变为「申请中」 |
| 仅邀请加入 | "该圈子仅限邀请加入" | 展示禁用按钮 + 提示 |
| 密码错误 | "密码错误" | Modal 内提示，清空输入框 |
| 圈子满员 | 满员错误（待后端定义） | Toast 提示 "圈子已满员" |
| 创建者不可退出 | "创建者不可退出圈子" | Toast 提示 |
| 非成员退出 | "您不是该圈子成员" | Toast 提示 |
| 权限不足 | "仅创建者可修改圈子信息" 等 | Toast 提示 + 403 页面 |
| 目标非成员 | "目标用户不是圈子成员" | Toast 提示 + 刷新列表 |
| 角色不可变更 | "创建者角色不可变更" | Toast 提示 |

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
| `/api/v1/content/circle/recommend` | GET | CircleRecommendController | 推荐圈子 |
| `/api/v1/content/circle/recommend/click` | POST | CircleRecommendController | 推荐点击 |
| `/api/v1/content/circle/recommend/join` | POST | CircleRecommendController | 推荐加入 |

---

## 接口命名对照表

| 设计文档名称 | 后端实际路径 | 方法 | 一致性 |
|-------------|-------------|------|--------|
| createCircle | /api/v1/content/circle/create | POST | 一致 |
| updateCircle | /api/v1/content/circle/update | PUT | 一致 |
| getCircleDetail | `GET /api/v1/content/circle/{id}` | GET | 需新增（建议 path param 风格） |
| getMyCircleList | **不存在** | - | 需新增 |
| getPublicCircleList | **不存在** | - | 需新增 |
| checkCircleName | **不存在** | - | 需新增（service 已有） |
| joinCircle | /api/v1/content/circle/join | POST | 一致 |
| leaveCircle | /api/v1/content/circle/leave | POST | 一致（已对齐为 leaveCircle） |
| getMemberList | **不存在** | - | 需新增 |
| changeRole | /api/v1/content/circle/member/change-role | POST | 一致（已对齐为 changeRole） |
| muteMember | /api/v1/content/circle/member/mute | POST | 一致 |
| unmuteMember | /api/v1/content/circle/member/unmute | POST | 一致 |
| removeMember | /api/v1/content/circle/member/remove | POST | 一致 |
| searchCircles | /api/v1/content/circle/search | GET | 一致 |
| getGovernanceLogList | **不存在** | - | 需新增 |
