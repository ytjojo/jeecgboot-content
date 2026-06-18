# API 接口清单

## 圈子公告
> 圈子公告发布与查询

### CircleAnnouncementController (`/Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/controller/CircleAnnouncementController.java`)
**Base Path**: `/api/v1/content/circle/announcement`

| 方法 | 路径 | 描述 | 入参 | 出参 | 行号 |
|------|------|------|------|------|------|
| POST | `/api/v1/content/circle/announcement/` | 发布公告 | req: CircleAnnouncementReq (body), request: HttpServletRequest (query) | `String` | 39 |
| DELETE | `/api/v1/content/circle/announcement/{id}` | 删除公告 | id: String (path) | `String` | 49 |
| GET | `/api/v1/content/circle/announcement/active/{circleId}` | 获取圈子当前有效公告 | circleId: String (path) | `CircleAnnouncementVO` | 57 |
| GET | `/api/v1/content/circle/announcement/history/{circleId}` | 获取圈子历史公告列表 | circleId: String (path) | `List<CircleAnnouncementVO>` | 70 |

## 圈子内容
> 帖子列表与详情查询

### CircleContentController (`/Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/controller/CircleContentController.java`)
**Base Path**: `/api/v1/content/circle`

| 方法 | 路径 | 描述 | 入参 | 出参 | 行号 |
|------|------|------|------|------|------|
| GET | `/api/v1/content/circle/{circleId}/posts` | 查询圈子帖子列表（含作者勋章） | circleId: String (path) | `List<CircleContentVO>` | 29 |

## 圈子内容举报
> 内容举报提交与处理

### CircleReportController (`/Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/controller/CircleReportController.java`)
**Base Path**: `/api/v1/content/circle/report`

| 方法 | 路径 | 描述 | 入参 | 出参 | 行号 |
|------|------|------|------|------|------|
| POST | `/api/v1/content/circle/report/` | 提交举报 | req: CircleReportReq (body), request: HttpServletRequest (query) | `String` | 45 |
| GET | `/api/v1/content/circle/report/list/{circleId}` | 获取举报列表 | circleId: String (path), status: String (query), request: HttpServletRequest (query) | `List<CircleReportVO>` | 58 |
| POST | `/api/v1/content/circle/report/{reportId}/delete-content` | 处理举报 - 删除被举报内容 | reportId: String (path), circleId: String (query), request: HttpServletRequest (query) | `String` | 78 |
| POST | `/api/v1/content/circle/report/{reportId}/ignore` | 处理举报 - 忽略 | reportId: String (path), circleId: String (query), request: HttpServletRequest (query) | `String` | 89 |
| POST | `/api/v1/content/circle/report/{reportId}/mute` | 处理举报 - 禁言用户 | reportId: String (path), circleId: String (query), request: HttpServletRequest (query) | `String` | 100 |

## 圈子内容置顶与精华
> 内容置顶、精华标记管理

### CircleContentPinController (`/Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/controller/CircleContentPinController.java`)
**Base Path**: `/api/v1/content/circle/content`

| 方法 | 路径 | 描述 | 入参 | 出参 | 行号 |
|------|------|------|------|------|------|
| PUT | `/api/v1/content/circle/content/{contentId}/pin` | 切换置顶状态 | contentId: String (path), circleId: String (query), request: HttpServletRequest (query) | `String` | 30 |
| PUT | `/api/v1/content/circle/content/{contentId}/featured` | 切换精华状态 | contentId: String (path), circleId: String (query), request: HttpServletRequest (query) | `String` | 41 |

## 圈子加入审核
> 圈子加入申请审核与查询

### CircleJoinReviewController (`/Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/controller/CircleJoinReviewController.java`)
**Base Path**: `/api/v1/content/circle/join-review`

| 方法 | 路径 | 描述 | 入参 | 出参 | 行号 |
|------|------|------|------|------|------|
| GET | `/api/v1/content/circle/join-review/list` | 查询加入申请列表 | "圈子ID": = (query), circleId: String (query), status: String (query), current: Integer (query), size: Integer (query) | `Page<CircleJoinRequest>` | 45 |
| GET | `/api/v1/content/circle/join-review/pending/{circleId}` | 获取待审核申请列表 | circleId: String (path) | `List<CircleJoinRequestVO>` | 58 |
| POST | `/api/v1/content/circle/join-review/approve` | 批准加入申请 | req: CircleJoinReviewReq (body), circleId: String (query), request: HttpServletRequest (query) | `String` | 72 |
| POST | `/api/v1/content/circle/join-review/reject` | 拒绝加入申请 | req: CircleJoinReviewReq (body), circleId: String (query), request: HttpServletRequest (query) | `String` | 82 |

## 圈子成员管理
> 成员角色变更、禁言、移除等接口

### CircleMemberController (`/Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/controller/CircleMemberController.java`)
**Base Path**: `/api/v1/content/circle/member`

| 方法 | 路径 | 描述 | 入参 | 出参 | 行号 |
|------|------|------|------|------|------|
| POST | `/api/v1/content/circle/member/change-role` | 变更成员角色 | req: CircleMemberUpdateReq (body) | `String` | 39 |
| POST | `/api/v1/content/circle/member/mute` | 禁言成员 | req: CircleMemberUpdateReq (body) | `String` | 47 |
| POST | `/api/v1/content/circle/member/unmute` | 解除禁言 | req: CircleMemberUpdateReq (body) | `String` | 55 |
| POST | `/api/v1/content/circle/member/remove` | 移除成员 | req: CircleMemberUpdateReq (body) | `String` | 63 |
| GET | `/api/v1/content/circle/member/list` | 获取圈子成员列表 | "圈子ID": = (query), circleId: String (query), role: String (query), status: String (query), pageNum: Integer (query), pageSize: Integer (query) | `Page<CircleMemberVO>` | 76 |

## 圈子推荐
### CircleRecommendController (`/Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/controller/CircleRecommendController.java`)
**Base Path**: `/api/v1/content/circle`

| 方法 | 路径 | 描述 | 入参 | 出参 | 行号 |
|------|------|------|------|------|------|
| GET | `/api/v1/content/circle/recommend` | 获取推荐圈子 | limit: int (query) | `CircleRecommendVO` | 26 |
| POST | `/api/v1/content/circle/recommend/click` | 记录推荐点击 | sourceId: String (query) | `String` | 33 |
| POST | `/api/v1/content/circle/recommend/join` | 记录推荐加入转化 | sourceId: String (query) | `String` | 41 |
| POST | `/api/v1/content/circle/recommend/exposure` | 记录推荐曝光 | sourceId: String (query) | `String` | 49 |

## 圈子搜索
> 圈子搜索接口

### CircleSearchController (`/Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/controller/CircleSearchController.java`)
**Base Path**: `/api/v1/content/circle`

| 方法 | 路径 | 描述 | 入参 | 出参 | 行号 |
|------|------|------|------|------|------|
| GET | `/api/v1/content/circle/search` | 搜索圈子 | req: CircleSearchReq (query) | `Page<CircleSearchResultVO>` | 35 |

## 圈子数据统计
### CircleDataController (`/Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/controller/CircleDataController.java`)
**Base Path**: `/api/v1/content/circle`

| 方法 | 路径 | 描述 | 入参 | 出参 | 行号 |
|------|------|------|------|------|------|
| GET | `/api/v1/content/circle/{circleId}/data/statistics` | 获取圈子数据统计 | circleId: String (path), startDate: LocalDate (query), endDate: LocalDate (query) | `CircleDataStatisticsVO` | 30 |
| GET | `/api/v1/content/circle/{circleId}/data/export` | 导出圈子数据统计CSV | circleId: String (path), startDate: LocalDate (query), endDate: LocalDate (query), response: HttpServletResponse (query) | `void` | 40 |

## 圈子榜单
### CircleRankingController (`/Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/controller/CircleRankingController.java`)
**Base Path**: `/api/v1/content/circle/ranking`

| 方法 | 路径 | 描述 | 入参 | 出参 | 行号 |
|------|------|------|------|------|------|
| GET | `/api/v1/content/circle/ranking/hot` | 获取热门圈子榜单 | limit: int (query) | `CircleRankingVO` | 23 |
| GET | `/api/v1/content/circle/ranking/new` | 获取新增圈子榜单 | limit: int (query) | `CircleRankingVO` | 30 |

## 圈子治理日志
### CircleGovernanceLogController (`/Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/controller/CircleGovernanceLogController.java`)
**Base Path**: `/api/v1/content/circle/governance-log`

| 方法 | 路径 | 描述 | 入参 | 出参 | 行号 |
|------|------|------|------|------|------|
| GET | `/api/v1/content/circle/governance-log/list` | 分页查询治理日志 | circleId: String (query), pageNum: Integer (query), pageSize: Integer (query) | `IPage<CircleGovernanceLog>` | 27 |

## 圈子管理
> 圈子创建、更新、查询等接口

### CircleController (`/Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/controller/CircleController.java`)
**Base Path**: `/api/v1/content/circle`

| 方法 | 路径 | 描述 | 入参 | 出参 | 行号 |
|------|------|------|------|------|------|
| POST | `/api/v1/content/circle/create` | 创建圈子 | req: CircleCreateReq (body) | `CircleVO` | 53 |
| PUT | `/api/v1/content/circle/update` | 更新圈子信息 | req: CircleUpdateReq (body) | `String` | 60 |
| POST | `/api/v1/content/circle/join` | 加入圈子 | req: CircleJoinReq (body) | `String` | 68 |
| POST | `/api/v1/content/circle/leave` | 退出圈子 | req: CircleLeaveReq (body) | `String` | 76 |
| GET | `/api/v1/content/circle/my-list` | 获取我的圈子列表 | pageNum: Integer (query), pageSize: Integer (query) | `Page<CircleVO>` | 86 |
| GET | `/api/v1/content/circle/public-list` | 获取公开圈子列表 | pageNum: Integer (query), pageSize: Integer (query) | `Page<CircleVO>` | 123 |
| GET | `/api/v1/content/circle/{id}` | 获取圈子详情(PATH参数) | "圈子ID": = (query), id: String (path) | `CircleVO` | 149 |
| GET | `/api/v1/content/circle/check-name` | 检查圈子名称是否可用 | "圈子名称": = (query), name: String (query) | `Boolean` | 166 |
