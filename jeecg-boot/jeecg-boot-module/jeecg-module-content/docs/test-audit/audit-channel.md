# Channel 子模块 单元测试缺测审计报告

> 审计范围：`jeecg-module-content/content/channel/`
> 审计时间：2026-06-02
> 审计方式：静态文件扫描 + 类签名比对（未执行任何业务代码修改）

---

## 1. 摘要

| 指标 | 数值 |
| --- | --- |
| 主代码 `.java` 文件总数 | **255** |
| 测试 `.java` 文件总数 | **48** |
| 应测主类（P0 + P1）| **约 110**（去重后） |
| 已测 P0+P1 主类 | **约 67** |
| 粗算覆盖率 | **≈ 60.9 %**（67 / 110） |
| **P0 缺测数** | **44**（其中 18 个 Controller 全部缺测） |
| P1 Mapper 缺测数 | 28（全部为 `BaseMapper<X>` 空接口，仅 `ChannelStatsMapper` 含 1 个自定义方法） |
| 可跳过（P3 POJO/常量）| 94 |

### 风险概览
- **🔴 最高风险**：`ChannelScheduledTask`（328 行，跨 6+ Service 编排）— 完全无测试。
- **🔴 高风险**：`ChannelBizManageService`（256 行，跨表事务编排，依赖 5 个 Service）— 完全无测试。
- **🔴 高风险**：18 个 Controller 全部无 `*ControllerTest`，但其中 13 个含多接口（增删改查 + 业务动作），与 HTTP 入口直接相连。
- **🟡 中风险**：`ChannelService`（主实体服务）+ `ChannelTransferService`/`ChannelAppealService` 等核心 Service 的 Impl 均无测试。

---

## 2. 已测主类清单

> 测试文件匹配规则：`XxxTest` / `XxxServiceTest` / `XxxBizServiceTest` / `XxxControllerTest` 等。

### 2.1 Controller（22 中已测 4）

| 主类 | 对应测试类 |
| --- | --- |
| `ChannelGovernanceController` | `ChannelGovernanceControllerTest` |
| `ChannelMemberController` | `ChannelMemberControllerTest` |
| `ChannelSubscriptionController` | `ChannelSubscriptionControllerTest` |
| `ContentChannelBrowseController` | `ContentChannelBrowseControllerTest` |

### 2.2 Biz（20 中已测 19）

| 主类（含接口与实现） | 对应测试类 |
| --- | --- |
| `ChannelAnnouncementBiz` / `ChannelAnnouncementBizImpl` | `ChannelAnnouncementBizTest` |
| `ChannelExportBiz` | `ChannelExportBizTest` |
| `ChannelGovernanceBiz` / `ChannelGovernanceBizImpl` | `ChannelGovernanceBizTest` |
| `ChannelGovernanceBizService` | `ChannelGovernanceBizServiceTest` |
| `ChannelLifecycleBiz` | `ChannelLifecycleBizTest` |
| `ChannelMemberBizService` | `ChannelMemberBizServiceTest` |
| `ChannelMergeBiz` | `ChannelMergeBizTest` |
| `ChannelPublishBiz` / `ChannelPublishBizImpl` | `ChannelPublishBizTest`（+ `ChannelAddExistingContentTest` 覆盖 addExistingContent 流程） |
| `ChannelReviewBiz` / `ChannelReviewBizImpl` | `ChannelReviewBizTest` |
| `ChannelStatsBiz` | `ChannelStatsBizTest` |
| `ContentChannelCategoryBiz` | `ContentChannelCategoryBizTest` |
| `ContentChannelDiscoveryBiz` | `ContentChannelDiscoveryBizTest` |
| `ScheduledPublishDispatchBiz` / `ScheduledPublishDispatchBizImpl` | `ScheduledPublishDispatchBizTest` |

> 注：`ChannelSubscriptionBizService` 也已被 `ChannelSubscriptionBizServiceTest` 覆盖（属于 ChannelSubscription 业务编排）。

### 2.3 Service（含 I- 前缀接口及 Impl）（33 服务中已测 21）

| 主类 | 对应测试类 |
| --- | --- |
| `ChannelAnnouncementService` (+ Impl) | `ChannelAnnouncementServiceTest` |
| `ChannelBlacklistService` (+ Impl) | `ChannelBlacklistServiceTest`（+ `ChannelBlacklistTest` 边界流） |
| `ChannelContentPublishService` (+ Impl) | `ChannelContentPublishServiceTest` |
| `ChannelContentReviewService` (+ Impl) | `ChannelContentReviewServiceTest` |
| `ChannelInviteService` (+ Impl) | `ChannelInviteServiceTest`（+ `ChannelInviteFlowTest` 流程） |
| `ChannelJoinApplicationService` (+ Impl) | `ChannelJoinApplicationServiceTest`（+ `ChannelJoinReviewTest` 跨服务） |
| `ChannelJoinMethodService` (+ Impl) | `ChannelJoinMethodServiceTest`（+ `ChannelJoinMethodTest`） |
| `ChannelMemberService` (+ Impl) | `ChannelMemberServiceTest` |
| `ChannelMuteService` (+ Impl) | `ChannelMuteServiceTest`（+ `ChannelMuteExpiryTest`） |
| `ChannelPrivacyService` (+ Impl) | `ChannelPrivacyServiceTest`（+ `ChannelPrivacyTest`） |
| `ChannelPublishLimitService` (+ Impl) | `ChannelPublishLimitServiceTest` |
| `ChannelRecycleBinService` (+ Impl) | `ChannelRecycleBinServiceTest` |
| `ChannelScheduledPublishService` (+ Impl) | `ChannelScheduledPublishServiceTest` |
| `ChannelSubscriptionService` (+ Impl) | `ChannelSubscriptionServiceTest` |
| `IContentChannelCategoryService` (+ Impl) | `ContentChannelCategoryServiceTest` |
| `IContentChannelEditorialPickService` (+ Impl) | `ContentChannelEditorialPickServiceTest` |
| `IContentChannelRankingService` (+ Impl) | `ContentChannelRankingServiceTest` |
| `IContentChannelRecommendationService` (+ Impl) | `ContentChannelRecommendationServiceTest` |
| `IContentChannelSearchService` (+ Impl) | `ContentChannelSearchServiceTest` |
| `IContentChannelTagService` (+ Impl) | `ContentChannelTagServiceTest` |
| `IContentChannelVisibilityService` (+ Impl) | `ContentChannelVisibilityServiceTest` |

### 2.4 Task / Scheduled

| 主类 | 对应测试类 |
| --- | --- |
| `ChannelRankingDailyTask` | `ChannelRankingDailyTaskTest` |
| `ChannelRecommendationRefreshTask` | `ChannelRecommendationRefreshTaskTest` |

---

## 3. 缺测主类清单（按优先级排序）

### 3.1 🔴 P0 缺测（44 个）

#### 3.1.1 Controller（18 个 — 全部缺测）

| 路径:行数 | 类名 | 缺测原因 / 风险 | 建议测试范围 |
| --- | --- | --- | --- |
| `controller/ChannelAdminController.java:51` | `ChannelAdminController` | 管理员入口端点，未与 BizManage 解耦 | 权限校验、调用下游 Service 的 mock、返回 `Result` 包装 |
| `controller/ChannelAnnouncementController.java:50` | `ChannelAnnouncementController` | 公告管理端点 | list/save/update/delete + 关联 Channel 校验 |
| `controller/ChannelContentGovernanceController.java:30` | `ChannelContentGovernanceController` | 内容治理动作端点 | 处理动作参数校验、调用 `ChannelGovernanceBiz` 验证 |
| `controller/ChannelContentReviewController.java:30` | `ChannelContentReviewController` | 内容审核端点 | 审核动作端到端、Result 包装 |
| `controller/ChannelController.java:105` | `ChannelController` | 核心频道 CRUD 端点，行数最大 | 全端点覆盖：list/detail/save/update/delete + 鉴权 |
| `controller/ChannelExportController.java:90` | `ChannelExportController` | 异步导出任务端点 | 任务创建/状态查询/下载的 Controller 层 mock 测试 |
| `controller/ChannelInviteController.java:61` | `ChannelInviteController` | 邀请流程端点 | 创建邀请/接受/拒绝，邀请码校验 |
| `controller/ChannelLifecycleController.java:163` | `ChannelLifecycleController` | 生命周期（行数第二大），多动作切换 | 归档/恢复/删除等状态机 |
| `controller/ChannelMergeController.java:49` | `ChannelMergeController` | 频道合并端点 | 合并请求参数校验、与 `ChannelMergeBiz` 的契约 |
| `controller/ChannelPublishController.java:39` | `ChannelPublishController` | 内容发布端点 | 与 `ChannelPublishBiz` 交互的 mock 测试 |
| `controller/ChannelReviewController.java:120` | `ChannelReviewController` | 频道入驻审核端点 | 审核流程：提交/通过/拒绝 |
| `controller/ChannelStatsController.java:85` | `ChannelStatsController` | 统计端点 | 趋势/排行/用户分析 3 个查询端点 |
| `controller/ContentChannelCategoryController.java:50` | `ContentChannelCategoryController` | 分类树端点 | 树形结构查询、新增/修改/删除 |
| `controller/ContentChannelEditorialPickController.java:50` | `ContentChannelEditorialPickController` | 编辑精选端点 | 上线/下线动作 |
| `controller/ContentChannelRankingController.java:39` | `ContentChannelRankingController` | 排行榜查询端点 | 排行规则维度切换 |
| `controller/ContentChannelRecommendationController.java:44` | `ContentChannelRecommendationController` | 个性化推荐端点 | 缓存命中/未命中分支 |
| `controller/ContentChannelSearchController.java:28` | `ContentChannelSearchController` | 全文搜索端点 | 分词/翻页/过滤 |
| `controller/ContentChannelTagController.java:42` | `ContentChannelTagController` | 标签管理端点 | 标签 CRUD + 关联/解绑 |

#### 3.1.2 Biz 跨表编排（1 个）

| 路径:行数 | 类名 | 缺测原因 / 风险 | 建议测试范围 |
| --- | --- | --- | --- |
| `biz/ChannelBizManageService.java:256` | `ChannelBizManageService` | **跨 Channel/Transfer/Review 三个领域、含 `@Transactional` 事务**；256 行是模块最大 Biz 之一 | 创建/转让/转让审核/分类变更等核心场景；事务回滚与异常分支；Status/Type 枚举守门 |

#### 3.1.3 Service 接口（12 个）

| 路径:行数 | 接口 | 风险 | 建议测试范围 |
| --- | --- | --- | --- |
| `service/ChannelContentGovernanceLogService.java:5` (+ Impl 27) | `ChannelContentGovernanceLogService` | 内容治理日志查询 | 治理日志分页、按 Channel/Action 过滤 |
| `service/ChannelEditAssistService.java:5` (+ Impl 26) | `ChannelEditAssistService` | 编辑辅助（命名重复内容） | 文案校验、敏感词/违禁词检查 |
| `service/ChannelGovernanceLogService.java:8` (+ Impl 28) | `ChannelGovernanceLogService` | 频道治理日志 | 治理日志查询、统计周期 |
| `service/ChannelMemberListService.java:11` (+ Impl 38) | `ChannelMemberListService` | 成员列表查询 | 角色过滤、分页、排序 |
| `service/ChannelService.java:15` (+ Impl 37) | `ChannelService` | **核心 Channel 实体 Service（JeecgServiceImpl）** | CRUD 基础流程、租户隔离 |
| `service/ChannelSubscriptionGroupService.java:19` (+ Impl 58) | `ChannelSubscriptionGroupService` | 订阅分组 | 创建分组、添加订阅源 |
| `service/ChannelTransferService.java:13` (+ Impl 64) | `ChannelTransferService` | 频道所有权转移 | 转让申请、接受、撤销 |
| `service/IChannelAppealService.java:11` (+ Impl 58) | `IChannelAppealService` | 申诉处理 | 提交申诉、审核申诉、状态机 |
| `service/IChannelExportTaskService.java:7` (+ Impl 12) | `IChannelExportTaskService` | 导出任务管理 | 任务状态推进 |
| `service/IChannelLifecycleLogService.java:7` (+ Impl 12) | `IChannelLifecycleLogService` | 生命周期日志 | 状态变迁记录 |
| `service/IChannelReviewService.java:16` (+ Impl 67) | `IChannelReviewService` | 入驻审核 | 提交审核、审核动作、结果流转 |
| `service/IChannelStatsService.java:14` (+ Impl 31) | `IChannelStatsService` | 统计聚合 | 指标聚合、统计周期 |

#### 3.1.4 Scheduled（1 个 — 关键）

| 路径:行数 | 类名 | 风险 | 建议测试范围 |
| --- | --- | --- | --- |
| `scheduled/ChannelScheduledTask.java:328` | `ChannelScheduledTask` | **全模块最大单文件**；含 `@Scheduled` 注解；编排 6+ Service；含过期转移、自动审核、回收站清理、导出清理、调度分发 | 5 个定时入口的 mock 验证；过期判断边界；空集合/大批量场景 |

### 3.2 🟡 P1 缺测：Mapper（28 个）

> 28 个 Mapper 全部继承 `BaseMapper<X>`，仅 `ChannelStatsMapper` 含 1 个自定义方法 `selectTrendData`（配套 `ChannelStatsMapper.xml` 18 行）。其它均为空接口，由 MyBatis-Plus 自动生成实现。

| 路径:行数 | Mapper | 备注 |
| --- | --- | --- |
| `mapper/ChannelAnnouncementMapper.java:7` | `ChannelAnnouncementMapper` | 仅 BaseMapper |
| `mapper/ChannelAppealMapper.java:9` | `ChannelAppealMapper` | 仅 BaseMapper |
| `mapper/ChannelBlacklistMapper.java:9` | `ChannelBlacklistMapper` | 仅 BaseMapper |
| `mapper/ChannelContentEditHistoryMapper.java:7` | `ChannelContentEditHistoryMapper` | 仅 BaseMapper |
| `mapper/ChannelContentGovernanceLogMapper.java:7` | `ChannelContentGovernanceLogMapper` | 仅 BaseMapper |
| `mapper/ChannelContentPublishMapper.java:7` | `ChannelContentPublishMapper` | 仅 BaseMapper |
| `mapper/ChannelContentReviewMapper.java:7` | `ChannelContentReviewMapper` | 仅 BaseMapper |
| `mapper/ChannelExportTaskMapper.java:9` | `ChannelExportTaskMapper` | 仅 BaseMapper |
| `mapper/ChannelGovernanceLogMapper.java:9` | `ChannelGovernanceLogMapper` | 仅 BaseMapper |
| `mapper/ChannelInviteMapper.java:9` | `ChannelInviteMapper` | 仅 BaseMapper |
| `mapper/ChannelJoinApplicationMapper.java:9` | `ChannelJoinApplicationMapper` | 仅 BaseMapper |
| `mapper/ChannelLifecycleLogMapper.java:9` | `ChannelLifecycleLogMapper` | 仅 BaseMapper |
| `mapper/ChannelMapper.java:9` | `ChannelMapper` | 仅 BaseMapper |
| `mapper/ChannelMemberMapper.java:9` | `ChannelMemberMapper` | 仅 BaseMapper |
| `mapper/ChannelMuteMapper.java:9` | `ChannelMuteMapper` | 仅 BaseMapper |
| `mapper/ChannelPublishLimitMapper.java:7` | `ChannelPublishLimitServiceImpl` 配套 Mapper | 仅 BaseMapper |
| `mapper/ChannelRecycleBinMapper.java:7` | `ChannelRecycleBinMapper` | 仅 BaseMapper |
| `mapper/ChannelReviewMapper.java:9` | `ChannelReviewMapper` | 仅 BaseMapper |
| `mapper/ChannelScheduledPublishMapper.java:7` | `ChannelScheduledPublishMapper` | 仅 BaseMapper |
| `mapper/ChannelStatsMapper.java:18` | `ChannelStatsMapper` | **含 `selectTrendData` 自定义方法（XML 18 行），建议 P1 优先级最高** |
| `mapper/ChannelSubscriptionGroupMapper.java:9` | `ChannelSubscriptionGroupMapper` | 仅 BaseMapper |
| `mapper/ChannelSubscriptionMapper.java:9` | `ChannelSubscriptionMapper` | 仅 BaseMapper |
| `mapper/ChannelTransferMapper.java:9` | `ChannelTransferMapper` | 仅 BaseMapper |
| `mapper/ContentChannelCategoryMapper.java:9` | `ContentChannelCategoryMapper` | 仅 BaseMapper |
| `mapper/ContentChannelEditorialPickMapper.java:9` | `ContentChannelEditorialPickMapper` | 仅 BaseMapper |
| `mapper/ContentChannelNotInterestedMapper.java:9` | `ContentChannelNotInterestedMapper` | 仅 BaseMapper |
| `mapper/ContentChannelRankingSnapshotMapper.java:9` | `ContentChannelRankingSnapshotMapper` | 仅 BaseMapper |
| `mapper/ContentChannelRecommendationCacheMapper.java:9` | `ContentChannelRecommendationCacheMapper` | 仅 BaseMapper |
| `mapper/ContentChannelTagMapper.java:9` | `ContentChannelTagMapper` | 仅 BaseMapper |
| `mapper/ContentChannelTagRelationMapper.java:9` | `ContentChannelTagRelationMapper` | 仅 BaseMapper |

> 建议处理方式：
> - 27 个纯 `BaseMapper` 用一个 `MapperCompilationSmokeTest` 统一覆盖（编译期验证 + 启动期 bean 注入验证即可）。
> - `ChannelStatsMapper` 必须独立写 `@MybatisTest` 集成测试，覆盖 `selectTrendData` 4 个参数分支。

### 3.3 🟢 P2 缺测：Enum / Util（19 个）

| 路径:行数 | 类型 | 说明 |
| --- | --- | --- |
| `enums/ApplicationStatus.java:20` | enum | 入站申请状态 |
| `enums/ChannelAppealStatus.java:16` | enum | 申诉状态 |
| `enums/ChannelExportStatus.java:16` | enum | 导出任务状态 |
| `enums/ChannelLifecycleStatus.java:30` | enum | 生命周期（含业务守门方法） |
| `enums/ChannelReviewStatus.java:16` | enum | 审核状态 |
| `enums/ChannelStatus.java:23` | enum | 频道状态 |
| `enums/ChannelType.java:20` | enum | 频道类型 |
| `enums/ChannelViolationType.java:16` | enum | 违规类型 |
| `enums/ContentGovernanceAction.java:21` | enum | 内容治理动作 |
| `enums/GovernanceAction.java:22` | enum | 治理动作 |
| `enums/InviteStatus.java:21` | enum | 邀请状态 |
| `enums/JoinMethod.java:20` | enum | 加入方式 |
| `enums/MemberRole.java:21` | enum | 成员角色（含权限判断） |
| `enums/PrivacyType.java:19` | enum | 隐私类型 |
| `enums/PublishPermissionEnum.java:16` | enum | 发布权限 |
| `enums/PublishStatusEnum.java:18` | enum | 发布状态 |
| `enums/ReviewResult.java:20` | enum | 审核结果 |
| `enums/TransferStatus.java:29` | enum | 转移状态 |
| `util/ChannelConvertUtil.java:16` | util | `toVO(Channel)` 单方法 |

> 说明：纯枚举 + 简单工具类通常不需单测；如 `MemberRole` 含权限方法、`ChannelLifecycleStatus` 含迁移守门方法，建议低优先级补少量断言。

---

## 4. 可跳过 / POJO 清单（94 个）

> 全为纯数据载体或常量，无业务逻辑，按 P3 跳过。

### 4.1 Entity（30 个）

```
entity/Channel.java                              :59
entity/ChannelAnnouncement.java                  :31
entity/ChannelAppeal.java                        :60
entity/ChannelBlacklist.java                     :36
entity/ChannelContentEditHistory.java            :34
entity/ChannelContentGovernanceLog.java          :37
entity/ChannelContentPublish.java                :43
entity/ChannelContentReview.java                 :47
entity/ChannelExportTask.java                    :67
entity/ChannelGovernanceLog.java                 :42
entity/ChannelInvite.java                        :42
entity/ChannelJoinApplication.java               :39
entity/ChannelLifecycleLog.java                  :51
entity/ChannelMember.java                        :33
entity/ChannelMute.java                          :48
entity/ChannelPublishLimit.java                  :28
entity/ChannelRecycleBin.java                    :50
entity/ChannelReview.java                        :60
entity/ChannelScheduledPublish.java              :38
entity/ChannelStats.java                         :70
entity/ChannelSubscription.java                  :28
entity/ChannelSubscriptionGroup.java             :25
entity/ChannelTransfer.java                      :34
entity/ContentChannelCategory.java               :37
entity/ContentChannelEditorialPick.java          :36
entity/ContentChannelNotInterested.java          :30
entity/ContentChannelRankingSnapshot.java        :37
entity/ContentChannelRecommendationCache.java    :36
entity/ContentChannelTag.java                    :25
entity/ContentChannelTagRelation.java            :25
```

### 4.2 DTO（14 个）

```
dto/AssignRoleDTO.java           :19
dto/BatchOperationDTO.java       :19
dto/BlacklistDTO.java            :21
dto/ChannelVisibilityDTO.java    :19
dto/CreateChannelDTO.java        :41
dto/CreateInviteDTO.java         :25
dto/JoinApplyDTO.java            :17
dto/MuteMemberDTO.java           :26
dto/RemoveMemberDTO.java         :17
dto/ReviewApplicationDTO.java    :22
dto/SubscribeDTO.java            :14
dto/UpdateChannelDTO.java        :27
dto/UpdateJoinMethodDTO.java     :19
dto/UpdatePrivacyDTO.java        :19
```

### 4.3 VO（25 个）

```
vo/BlacklistVO.java                      :32
vo/ChannelAppealVO.java                  :46
vo/ChannelBrowseItemVO.java              :30
vo/ChannelCategoryTreeVO.java            :29
vo/ChannelEditorialPickVO.java           :32
vo/ChannelExportTaskVO.java              :28
vo/ChannelHotContentVO.java              :31
vo/ChannelLifecycleLogVO.java            :40
vo/ChannelListVO.java                    :32
vo/ChannelRankingItemVO.java             :33
vo/ChannelRecommendationVO.java          :41
vo/ChannelReviewVO.java                  :43
vo/ChannelSearchResultVO.java            :33
vo/ChannelStatsVO.java                   :46
vo/ChannelTagVO.java                     :18
vo/ChannelTrendVO.java                   :29
vo/ChannelUserAnalysisVO.java            :26
vo/ChannelVO.java                        :50
vo/GovernanceLogVO.java                  :38
vo/InviteVO.java                         :44
vo/JoinApplicationVO.java                :38
vo/MemberListVO.java                     :17
vo/MemberVO.java                         :38
vo/SubscriptionListVO.java               :17
vo/SubscriptionVO.java                   :32
vo/publish/ChannelPublishResultVO.java   :18
```

### 4.4 Req（22 个）

```
req/ChannelAppealHandleReq.java                    :21
req/ChannelAppealSubmitReq.java                    :24
req/ChannelExportReq.java                          :26
req/ChannelLifecycleActionReq.java                 :15
req/ChannelLifecycleLogQueryReq.java               :35
req/ChannelMergeReq.java                           :18
req/ChannelReviewActionReq.java                    :18
req/ChannelStatsReq.java                           :23
req/announcement/ChannelAnnouncementReq.java       :22
req/create/ChannelCategoryCreateReq.java           :27
req/create/ChannelEditorialPickCreateReq.java      :32
req/create/ChannelTagCreateReq.java                :20
req/governance/ChannelGovernanceReq.java           :32
req/publish/ChannelAddExistingContentReq.java      :27
req/publish/ChannelPublishReq.java                 :27
req/query/ChannelBrowseQueryReq.java               :24
req/query/ChannelCategoryQueryReq.java             :15
req/query/ChannelRankingQueryReq.java              :12
req/query/ChannelRecommendationQueryReq.java       :15
req/query/ChannelSearchQueryReq.java               :27
req/review/ChannelReviewReq.java                   :21
req/update/ChannelCategoryUpdateReq.java           :22
req/update/ChannelEditorialPickUpdateReq.java      :27
```

### 4.5 Constant（3 个）

```
constant/ChannelConstants.java          :14   (final 类，含 String 常量)
constant/ChannelMemberConstants.java    :30   (纯 interface 常量容器)
constant/ChannelStatsConstant.java      :14   (纯 interface 常量容器)
```

---

## 5. 修复优先级建议

| 优先级 | 数量 | 工作量预估 | 建议做法 |
| --- | --- | --- | --- |
| 🔴 P0-1：核心编排 | 2（`ChannelScheduledTask` + `ChannelBizManageService`） | 3–5 天 | 拆分子任务：定时任务用 `@MockBean` 注入 Service；Biz 用事务回滚测试 |
| 🔴 P0-2：Controller | 18 | 4–6 天 | 用 `@WebMvcTest` + `MockMvc`，逐端点覆盖 happy-path + 鉴权失败 + 参数校验失败 |
| 🔴 P0-3：Service/Impl | 12 + 12 | 3–4 天 | 优先覆盖 Impl，用 Mockito + AssertJ |
| 🟡 P1：Mapper | 28 | 0.5–1 天 | 27 个 `BaseMapper` 写 1 个 `MapperSmokeTest`；`ChannelStatsMapper` 单独写 `@MybatisTest` |
| 🟢 P2：Enum/Util | 19 | 可选 | 仅对含业务守门方法的枚举补 1–2 个断言 |
| ⚪ P3：POJO | 94 | 跳过 | — |

---

## 6. 附录：测试文件全量清单（48 个）

```
biz/ChannelAddExistingContentTest.java             :118
biz/ChannelAnnouncementBizTest.java                :74
biz/ChannelExportBizTest.java                      :232
biz/ChannelGovernanceBizServiceTest.java           :94
biz/ChannelGovernanceBizTest.java                  :155
biz/ChannelLifecycleBizTest.java                   :82
biz/ChannelMemberBizServiceTest.java               :138
biz/ChannelMergeBizTest.java                       :207
biz/ChannelPublishBizTest.java                     :91
biz/ChannelReviewBizTest.java                      :46
biz/ChannelStatsBizTest.java                       :117
biz/ChannelSubscriptionBizServiceTest.java         :127
biz/ContentChannelCategoryBizTest.java             :57
biz/ContentChannelDiscoveryBizTest.java            :81
biz/ScheduledPublishDispatchBizTest.java           :55
controller/ChannelGovernanceControllerTest.java    :98
controller/ChannelMemberControllerTest.java        :151
controller/ChannelSubscriptionControllerTest.java  :90
controller/ContentChannelBrowseControllerTest.java :47
service/ChannelAnnouncementServiceTest.java        :44
service/ChannelBlacklistServiceTest.java           :63
service/ChannelBlacklistTest.java                  :105
service/ChannelContentPublishServiceTest.java      :59
service/ChannelContentReviewServiceTest.java       :50
service/ChannelInviteFlowTest.java                 :172
service/ChannelInviteServiceTest.java              :85
service/ChannelJoinApplicationServiceTest.java     :78
service/ChannelJoinMethodServiceTest.java          :47
service/ChannelJoinMethodTest.java                 :81
service/ChannelJoinReviewTest.java                 :126
service/ChannelMemberServiceTest.java              :92
service/ChannelMuteExpiryTest.java                 :103
service/ChannelMuteServiceTest.java                :66
service/ChannelPrivacyServiceTest.java             :74
service/ChannelPrivacyTest.java                    :97
service/ChannelPublishLimitServiceTest.java        :46
service/ChannelRecycleBinServiceTest.java          :60
service/ChannelScheduledPublishServiceTest.java    :48
service/ChannelSubscriptionServiceTest.java        :77
service/ContentChannelCategoryServiceTest.java     :204
service/ContentChannelEditorialPickServiceTest.java:129
service/ContentChannelRankingServiceTest.java      :112
service/ContentChannelRecommendationServiceTest.java:113
service/ContentChannelSearchServiceTest.java       :47
service/ContentChannelTagServiceTest.java          :158
service/ContentChannelVisibilityServiceTest.java   :95
task/ChannelRankingDailyTaskTest.java              :25
task/ChannelRecommendationRefreshTaskTest.java     :25
```
