# API 接口清单

## 频道公告
> 频道公告管理接口

### ChannelAnnouncementController (`/Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/controller/ChannelAnnouncementController.java`)
**Base Path**: `/api/v1/content/channel/announcement`

| 方法 | 路径 | 描述 | 入参 | 出参 | 行号 |
|------|------|------|------|------|------|
| PUT | `/api/v1/content/channel/announcement/{id}` | 更新公告 | id: String (path), req: ChannelAnnouncementReq (body) | `Void` | 33 |
| DELETE | `/api/v1/content/channel/announcement/{id}` | 删除公告 | id: String (path) | `Void` | 40 |
| GET | `/api/v1/content/channel/announcement/channel/{channelId}` | 获取频道公告 | channelId: String (path) | `ChannelAnnouncement` | 47 |

## 频道内容发布
> 频道内容发布相关接口

### ChannelPublishController (`/Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/controller/ChannelPublishController.java`)
**Base Path**: `/api/v1/content/channel/publish`

| 方法 | 路径 | 描述 | 入参 | 出参 | 行号 |
|------|------|------|------|------|------|
| POST | `/api/v1/content/channel/publish/add-existing` | 将已发布内容添加到频道 | req: ChannelAddExistingContentReq (body) | `List<ChannelPublishResultVO>` | 36 |
| GET | `/api/v1/content/channel/publish/available` | 获取用户可发布频道列表 | - | `List<AvailableChannelVO>` | 43 |

## 频道内容审核
> 频道待审区和审核相关接口

### ChannelContentReviewController (`/Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/controller/ChannelContentReviewController.java`)
**Base Path**: `/api/v1/content/channel/review`

| 方法 | 路径 | 描述 | 入参 | 出参 | 行号 |
|------|------|------|------|------|------|
| GET | `/api/v1/content/channel/review/stats` | 获取审核统计 | channelId: String (query) | `ReviewStatsVO` | 35 |

## 频道内容治理
> 频道内容置顶、精华、删除、恢复等治理操作接口

### ChannelContentGovernanceController (`/Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/controller/ChannelContentGovernanceController.java`)
**Base Path**: `/api/v1/content/channel/governance`

| 方法 | 路径 | 描述 | 入参 | 出参 | 行号 |
|------|------|------|------|------|------|
| GET | `/api/v1/content/channel/governance/content/list` | 获取频道内容列表 | req: GovernanceContentListReq (query) | `Page<GovernanceContentItemVO>` | 38 |
| GET | `/api/v1/content/channel/governance/recycle-bin/list` | 获取回收站列表 | req: RecycleBinListReq (query) | `Page<RecycleBinItemVO>` | 44 |

## 频道分类浏览
### ContentChannelBrowseController (`/Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/controller/ContentChannelBrowseController.java`)
**Base Path**: `/api/v1/content/channel/browse`

| 方法 | 路径 | 描述 | 入参 | 出参 | 行号 |
|------|------|------|------|------|------|
| GET | `/api/v1/content/channel/browse/category` | 按分类浏览频道 | req: ChannelBrowseQueryReq (query) | `IPage<ChannelBrowseItemVO>` | 23 |

## 频道分类管理
### ContentChannelCategoryController (`/Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/controller/ContentChannelCategoryController.java`)
**Base Path**: `/api/v1/content/channel/category`

| 方法 | 路径 | 描述 | 入参 | 出参 | 行号 |
|------|------|------|------|------|------|
| GET | `/api/v1/content/channel/category/tree` | 获取分类树 | - | `List<ChannelCategoryTreeVO>` | 27 |
| POST | `/api/v1/content/channel/category/create` | 创建分类 | req: ChannelCategoryCreateReq (body) | `ContentChannelCategory` | 33 |
| POST | `/api/v1/content/channel/category/update` | 更新分类 | req: ChannelCategoryUpdateReq (body) | `Void` | 39 |
| POST | `/api/v1/content/channel/category/disable` | 停用分类 | categoryId: String (query) | `Void` | 46 |
| POST | `/api/v1/content/channel/category/enable` | 启用分类 | categoryId: String (query) | `Void` | 53 |

## 频道发现
> 频道发现首页数据接口

### ContentChannelDiscoveryController (`/Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/controller/ContentChannelDiscoveryController.java`)
**Base Path**: `/api/v1/content/channel/discovery`

| 方法 | 路径 | 描述 | 入参 | 出参 | 行号 |
|------|------|------|------|------|------|
| GET | `/api/v1/content/channel/discovery/home` | 获取发现首页数据 | - | `` | 25 |

## 频道合并
> 频道合并管理接口

### ChannelMergeController (`/Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/controller/ChannelMergeController.java`)
**Base Path**: `/api/v1/content/channel/merge`

| 方法 | 路径 | 描述 | 入参 | 出参 | 行号 |
|------|------|------|------|------|------|
| POST | `/api/v1/content/channel/merge/validate` |  | - | `` | 27 |
| POST | `/api/v1/content/channel/merge/execute` |  | req: ChannelMergeReq (body) | `Object` | 34 |

## 频道后台管理
> 后台频道管理API

### ChannelAdminController (`/Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/controller/ChannelAdminController.java`)
**Base Path**: `/api/v1/content/admin/channels`

| 方法 | 路径 | 描述 | 入参 | 出参 | 行号 |
|------|------|------|------|------|------|
| POST | `/api/v1/content/admin/channels/create-system` |  | dto: CreateChannelDTO (body) | `ChannelVO` | 38 |
| POST | `/api/v1/content/admin/channels/{id}/review` |  | id: String (path), result: ReviewResult (query), reason: String (query) | `Void` | 49 |
| GET | `/api/v1/content/admin/channels/list` |  | current: Integer (query), size: Integer (query), query: ChannelListQuery (query) | `IPage<ChannelVO>` | 60 |

## 频道审核
> 频道审核管理接口

### ChannelReviewController (`/Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/controller/ChannelReviewController.java`)
**Base Path**: `/api/v1/content/channel/review`

| 方法 | 路径 | 描述 | 入参 | 出参 | 行号 |
|------|------|------|------|------|------|
| GET | `/api/v1/content/channel/review/detail/{id}` |  | "审核记录ID": = (query), id: String (path) | `ChannelReviewVO` | 43 |
| GET | `/api/v1/content/channel/review/list` |  | current: Integer (query), size: Integer (query), status: String (query), reviewType: String (query) | `Page<ChannelReviewVO>` | 57 |
| POST | `/api/v1/content/channel/review/action` |  | req: ChannelReviewActionReq (body) | `Void` | 71 |

## 频道成员管理
### ChannelMemberController (`/Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/controller/ChannelMemberController.java`)
**Base Path**: `/api/v1/content/channel/member`

| 方法 | 路径 | 描述 | 入参 | 出参 | 行号 |
|------|------|------|------|------|------|
| POST | `/api/v1/content/channel/member/join/free` | 自由加入频道 | channelId: String (query) | `String` | 43 |
| POST | `/api/v1/content/channel/member/join/apply` | 提交加入申请 | channelId: String (query), reason: String (query) | `String` | 52 |
| POST | `/api/v1/content/channel/member/leave` | 退出频道 | channelId: String (query) | `String` | 60 |
| POST | `/api/v1/content/channel/member/assign-role` | 分配角色 | memberId: String (query), role: MemberRole (query) | `String` | 75 |
| GET | `/api/v1/content/channel/member/list` | 成员列表 | channelId: String (query), role: Integer (query), pageNum: int (query), pageSize: int (query) | `IPage<ChannelMember>` | 86 |
| GET | `/api/v1/content/channel/member/search` | 搜索成员 | channelId: String (query), keyword: String (query), pageNum: int (query), pageSize: int (query) | `IPage<ChannelMember>` | 95 |
| GET | `/api/v1/content/channel/member/applications/pending` | 待审核列表 | channelId: String (query) | `List<ChannelJoinApplication>` | 101 |
| POST | `/api/v1/content/channel/member/applications/approve` | 批准申请 | applicationId: String (query), reason: String (query) | `String` | 108 |
| POST | `/api/v1/content/channel/member/applications/reject` | 拒绝申请 | applicationId: String (query), reason: String (query) | `String` | 117 |
| GET | `/api/v1/content/channel/member/relation` | 用户频道关系查询 | channelId: String (query) | `UserChannelRelationVO` | 125 |

## 频道排行榜
### ContentChannelRankingController (`/Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/controller/ContentChannelRankingController.java`)
**Base Path**: `/api/v1/content/channel/ranking`

| 方法 | 路径 | 描述 | 入参 | 出参 | 行号 |
|------|------|------|------|------|------|
| GET | `/api/v1/content/channel/ranking/hot` | 获取热门频道榜 | req: ChannelRankingQueryReq (query) | `List<ChannelRankingItemVO>` | 24 |
| GET | `/api/v1/content/channel/ranking/new` | 获取新晋频道榜 | req: ChannelRankingQueryReq (query) | `List<ChannelRankingItemVO>` | 30 |
| GET | `/api/v1/content/channel/ranking/system` | 获取系统频道榜 | req: ChannelRankingQueryReq (query) | `List<ChannelRankingItemVO>` | 36 |

## 频道推荐
### ContentChannelRecommendationController (`/Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/controller/ContentChannelRecommendationController.java`)
**Base Path**: `/api/v1/content/channel/recommendation`

| 方法 | 路径 | 描述 | 入参 | 出参 | 行号 |
|------|------|------|------|------|------|
| GET | `/api/v1/content/channel/recommendation/list` | 获取推荐频道列表 | userId: String (query), req: ChannelRecommendationQueryReq (query) | `IPage<ChannelRecommendationVO>` | 25 |
| GET | `/api/v1/content/channel/recommendation/cold-start` | 冷启动推荐（无行为数据用户） | req: ChannelRecommendationQueryReq (query) | `IPage<ChannelRecommendationVO>` | 32 |
| POST | `/api/v1/content/channel/recommendation/not-interested` | 标记不感兴趣 | userId: String (query), channelId: String (query) | `Void` | 40 |

## 频道搜索
### ContentChannelSearchController (`/Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/controller/ContentChannelSearchController.java`)
**Base Path**: `/api/v1/content/channel/search`

| 方法 | 路径 | 描述 | 入参 | 出参 | 行号 |
|------|------|------|------|------|------|
| GET | `/api/v1/content/channel/search/query` | 搜索频道 | userId: String (query), req: ChannelSearchQueryReq (query) | `IPage<ChannelSearchResultVO>` | 30 |
| POST | `/api/v1/content/channel/search/feedback` | 提交搜索反馈 | keyword: String (query), channelId: String (query), action: String (query) | `Void` | 39 |

## 频道数据导出
> 频道数据导出接口

### ChannelExportController (`/Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/controller/ChannelExportController.java`)
**Base Path**: `/api/v1/content/channel/export`

| 方法 | 路径 | 描述 | 入参 | 出参 | 行号 |
|------|------|------|------|------|------|
| POST | `/api/v1/content/channel/export/create` |  | req: ChannelExportReq (body) | `ChannelExportTaskVO` | 38 |
| GET | `/api/v1/content/channel/export/status` |  | taskId: String (query) | `ChannelExportTaskVO` | 44 |
| GET | `/api/v1/content/channel/export/history` |  | channelId: String (query), current: Integer (query), size: Integer (query) | `Page<ChannelExportTask>` | 57 |
| GET | `/api/v1/content/channel/export/download` |  | "任务ID": = (query), taskId: String (query), response: HttpServletResponse (query) | `void` | 71 |

## 频道标签管理
### ContentChannelTagController (`/Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/controller/ContentChannelTagController.java`)
**Base Path**: `/api/v1/content/channel/tag`

| 方法 | 路径 | 描述 | 入参 | 出参 | 行号 |
|------|------|------|------|------|------|
| GET | `/api/v1/content/channel/tag/list` | 获取频道标签列表 | channelId: String (query) | `List<ChannelTagVO>` | 26 |
| POST | `/api/v1/content/channel/tag/create` | 创建标签 | req: ChannelTagCreateReq (body) | `ContentChannelTag` | 32 |
| POST | `/api/v1/content/channel/tag/update` | 更新标签 | tagId: String (query), name: String (query) | `Void` | 38 |
| POST | `/api/v1/content/channel/tag/delete` | 删除标签 | tagId: String (query) | `Void` | 45 |

## 频道治理管理
### ChannelGovernanceController (`/Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/controller/ChannelGovernanceController.java`)
**Base Path**: `/api/v1/content/channel/governance`

| 方法 | 路径 | 描述 | 入参 | 出参 | 行号 |
|------|------|------|------|------|------|
| POST | `/api/v1/content/channel/governance/remove` | 移除成员 | memberId: String (query), reason: String (query) | `String` | 34 |
| POST | `/api/v1/content/channel/governance/mute` | 禁言成员 | channelId: String (query), userId: String (query), days: int (query), reason: String (query) | `String` | 45 |
| POST | `/api/v1/content/channel/governance/unmute` | 解除禁言 | channelId: String (query), userId: String (query) | `String` | 54 |
| POST | `/api/v1/content/channel/governance/blacklist/add` | 加入黑名单 | channelId: String (query), userId: String (query), reason: String (query) | `String` | 64 |
| POST | `/api/v1/content/channel/governance/blacklist/remove` | 移出黑名单 | channelId: String (query), userId: String (query) | `String` | 73 |
| GET | `/api/v1/content/channel/governance/blacklist/list` | 黑名单列表 | channelId: String (query) | `List<ChannelBlacklist>` | 81 |
| GET | `/api/v1/content/channel/governance/log` | 治理日志列表 | channelId: String (query), action: Integer (query), pageNum: int (query), pageSize: int (query) | `IPage<ChannelGovernanceLog>` | 91 |

## 频道生命周期
> 频道生命周期管理接口

### ChannelLifecycleController (`/Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/controller/ChannelLifecycleController.java`)
**Base Path**: `/api/v1/content/channel/lifecycle`

| 方法 | 路径 | 描述 | 入参 | 出参 | 行号 |
|------|------|------|------|------|------|
| POST | `/api/v1/content/channel/lifecycle/freeze` |  | req: ChannelLifecycleActionReq (body) | `Void` | 44 |
| POST | `/api/v1/content/channel/lifecycle/unfreeze` |  | req: ChannelLifecycleActionReq (body) | `Void` | 51 |
| POST | `/api/v1/content/channel/lifecycle/hide` |  | req: ChannelLifecycleActionReq (body) | `Void` | 58 |
| POST | `/api/v1/content/channel/lifecycle/close` |  | req: ChannelLifecycleActionReq (body) | `Void` | 65 |
| POST | `/api/v1/content/channel/lifecycle/archive` |  | req: ChannelLifecycleActionReq (body) | `Void` | 72 |
| POST | `/api/v1/content/channel/lifecycle/restrict-recommend` |  | req: ChannelLifecycleActionReq (body) | `Void` | 79 |
| POST | `/api/v1/content/channel/lifecycle/restore-visibility` |  | req: ChannelLifecycleActionReq (body) | `Void` | 86 |
| GET | `/api/v1/content/channel/lifecycle/logs` |  | req: ChannelLifecycleLogQueryReq (query) | `IPage<ChannelLifecycleLog>` | 95 |
| POST | `/api/v1/content/channel/lifecycle/appeal/submit` |  | req: ChannelAppealSubmitReq (body) | `ChannelAppeal` | 122 |
| POST | `/api/v1/content/channel/lifecycle/appeal/handle` |  | req: ChannelAppealHandleReq (body) | `ChannelAppeal` | 131 |
| GET | `/api/v1/content/channel/lifecycle/appeal/detail/{id}` |  | id: String (path) | `ChannelAppeal` | 149 |
| GET | `/api/v1/content/channel/lifecycle/appeal/list` |  | channelId: String (query), status: String (query), pageNum: Integer (query), pageSize: Integer (query) | `IPage<ChannelAppeal>` | 163 |

## 频道管理
> 用户端频道API

### ChannelController (`/Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/controller/ChannelController.java`)
**Base Path**: `/api/v1/content/channels`

| 方法 | 路径 | 描述 | 入参 | 出参 | 行号 |
|------|------|------|------|------|------|
| POST | `/api/v1/content/channels/create` |  | dto: CreateChannelDTO (body) | `ChannelVO` | 59 |
| GET | `/api/v1/content/channels/list` |  | current: Integer (query), size: Integer (query), query: ChannelListQuery (query) | `IPage<ChannelVO>` | 77 |
| GET | `/api/v1/content/channels/{id}/delete-check` |  | id: String (path) | `DeleteCheckResultVO` | 86 |
| GET | `/api/v1/content/channels/{id}/transfers` |  | id: String (path) | `List<ChannelTransferVO>` | 94 |
| GET | `/api/v1/content/channels/check-name` |  | name: String (query), excludeId: String (query) | `Boolean` | 111 |
| GET | `/api/v1/content/channels/{id}/transfer/pending` |  | id: String (path) | `ChannelTransferVO` | 118 |
| GET | `/api/v1/content/channels/{id}` |  | id: String (path) | `ChannelVO` | 133 |
| PUT | `/api/v1/content/channels/{id}` |  | id: String (path), dto: UpdateChannelDTO (body) | `Void` | 143 |
| POST | `/api/v1/content/channels/{id}/transfer` |  | id: String (path), toUserId: String (query) | `Void` | 151 |
| POST | `/api/v1/content/channels/transfer/{transferId}/confirm` |  | transferId: String (path) | `Void` | 159 |
| POST | `/api/v1/content/channels/transfer/{transferId}/reject` |  | transferId: String (path) | `Void` | 167 |
| DELETE | `/api/v1/content/channels/{id}` |  | id: String (path) | `Void` | 175 |
| POST | `/api/v1/content/channels/{id}/cancel-delete` |  | id: String (path) | `Void` | 183 |
| PUT | `/api/v1/content/channels/privacy` |  | req: UpdatePrivacyReq (body) | `Void` | 191 |
| PUT | `/api/v1/content/channels/join-method` |  | req: UpdateJoinMethodReq (body) | `Void` | 205 |

## 频道统计
> 频道数据统计看板接口

### ChannelStatsController (`/Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/controller/ChannelStatsController.java`)
**Base Path**: `/api/v1/content/channel/stats`

| 方法 | 路径 | 描述 | 入参 | 出参 | 行号 |
|------|------|------|------|------|------|
| GET | `/api/v1/content/channel/stats/core` |  | "频道ID": = (query), channelId: String (query) | `ChannelStatsVO` | 42 |
| GET | `/api/v1/content/channel/stats/trend` |  | - | `ChannelTrendVO` | 46 |
| GET | `/api/v1/content/channel/stats/hot-content` |  | "频道ID": = (query), channelId: String (query), limit: Integer (query), days: Integer (query) | `List<ChannelHotContentVO>` | 71 |
| GET | `/api/v1/content/channel/stats/interaction` |  | "频道ID": = (query), channelId: String (query) | `ChannelInteractionStatsVO` | 79 |
| GET | `/api/v1/content/channel/stats/user-analysis` |  | "频道ID": = (query), channelId: String (query), startDate: LocalDate (query), endDate: LocalDate (query) | `ChannelUserAnalysisVO` | 91 |

## 频道编辑精选
### ContentChannelEditorialPickController (`/Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/controller/ContentChannelEditorialPickController.java`)
**Base Path**: `/api/v1/content/channel/editorial-pick`

| 方法 | 路径 | 描述 | 入参 | 出参 | 行号 |
|------|------|------|------|------|------|
| GET | `/api/v1/content/channel/editorial-pick/list` | 获取有效精选列表 | - | `List<ChannelEditorialPickVO>` | 29 |
| GET | `/api/v1/content/channel/editorial-pick/page` | 分页查询精选列表（管理端） | req: ChannelEditorialPickQueryReq (query) | `IPage<ChannelEditorialPickVO>` | 35 |
| POST | `/api/v1/content/channel/editorial-pick/create` | 创建编辑精选 | req: ChannelEditorialPickCreateReq (body) | `ContentChannelEditorialPick` | 41 |
| POST | `/api/v1/content/channel/editorial-pick/update` | 更新编辑精选 | req: ChannelEditorialPickUpdateReq (body) | `Void` | 47 |
| POST | `/api/v1/content/channel/editorial-pick/remove` | 移除编辑精选 | pickId: String (query) | `Void` | 54 |

## 频道订阅管理
### ChannelSubscriptionController (`/Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/controller/ChannelSubscriptionController.java`)
**Base Path**: `/api/v1/content/channel/subscription`

| 方法 | 路径 | 描述 | 入参 | 出参 | 行号 |
|------|------|------|------|------|------|
| GET | `/api/v1/content/channel/subscription/status/{channelId}` | 订阅状态查询 | channelId: String (path) | `Boolean` | 28 |
| POST | `/api/v1/content/channel/subscription/subscribe` | 订阅频道 | channelId: String (query) | `String` | 35 |
| POST | `/api/v1/content/channel/subscription/unsubscribe` | 取消订阅 | channelId: String (query) | `String` | 43 |
| GET | `/api/v1/content/channel/subscription/list` | 订阅列表 | - | `List<ChannelSubscription>` | 51 |
| POST | `/api/v1/content/channel/subscription/group/create` | 创建分组 | groupName: String (query) | `ChannelSubscriptionGroup` | 58 |
| GET | `/api/v1/content/channel/subscription/group/list` | 分组列表 | - | `List<ChannelSubscriptionGroup>` | 65 |
| POST | `/api/v1/content/channel/subscription/group/rename` | 重命名分组 | groupId: String (query), newName: String (query) | `String` | 72 |
| POST | `/api/v1/content/channel/subscription/group/delete` | 删除分组 | groupId: String (query) | `String` | 80 |

## 频道邀请管理
### ChannelInviteController (`/Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/controller/ChannelInviteController.java`)
**Base Path**: `/api/v1/content/channel/invite`

| 方法 | 路径 | 描述 | 入参 | 出参 | 行号 |
|------|------|------|------|------|------|
| POST | `/api/v1/content/channel/invite/create` | 创建邀请 | channelId: String (query), type: Integer (query), maxUses: Integer (query), expireDays: Integer (query) | `ChannelInvite` | 33 |
| GET | `/api/v1/content/channel/invite/list` | 查看邀请列表 | channelId: String (query) | `List<ChannelInvite>` | 41 |
| POST | `/api/v1/content/channel/invite/revoke` | 撤销邀请 | inviteId: String (query) | `String` | 47 |
| POST | `/api/v1/content/channel/invite/use` | 使用邀请码加入 | channelId: String (query), code: String (query) | `String` | 56 |
