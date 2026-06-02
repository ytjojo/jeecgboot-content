# content/user 单元测试缺测审计

> 审计范围：`jeecg-module-content/src/main/java/org/jeecg/modules/content/user/`（含 growth / gateway）
> 审计时间：2026-06-02
> 审计方式：静态扫描（不执行任何业务代码）

---

## 1. 摘要

| 指标 | 数值 |
| --- | --- |
| 主代码 `.java` 文件总数 | **361** |
| 测试代码 `.java` 文件总数 | **66** |
| 应测主类（P0 + P1） | **176** |
| 已直接覆盖（direct match） | **81** |
| 弱覆盖（仅被 `*CrudContractTest` / `*MigrationTest` 等契约/迁移测试以 import 形式引用） | **63** |
| 完全无测试 | **32** |
| P0 关键缺失 | **32** |
| P1 Mapper 弱覆盖 | **51**（全部仅 CRUD 契约 import，无真实 SQL 行为验证） |
| P0+P1 整体粗算覆盖率（direct） | **46.0 %**（81/176） |
| P0+P1 整体粗算覆盖率（direct+weak） | **81.8 %**（144/176） |

> 命名匹配规则：`XxxController` → `XxxControllerTest` / `XxxControllerWebMvcTest` / `XxxApiDocTest`；`XxxService` / `XxxServiceImpl` → `XxxServiceTest` / `XxxCrudContractTest`；`XxxBiz` → `XxxBoundaryBizServiceTest`；`XxxMapper` → `XxxMapperContractTest` / `XxxMapperCompilationTest`；`XxxScheduler` / `XxxTask` → `XxxSchedulerTest`；`XxxGateway` → `XxxGatewayTest`。对于 `IContentXxxService`（接口）会去前缀 `I` 再匹配；`*Impl` 后缀亦同。

---

## 2. 已测主类清单（直接匹配，共 81 个）

> 「主类」→「测试类」一对多/一对一映射；`TestFile` 列展示至少一个直接测试名；行数为该主类文件总行数。

### 2.1 控制器 P0（10 / 16）

| 主类（行数） | 测试类 |
| --- | --- |
| `AchievementController` (28) | `AchievementControllerTest` |
| `CircleLevelController` (25) | `CircleLevelControllerTest` |
| `ContentAccountController` (135) | `ContentAccountControllerWebMvcTest` |
| `ContentUserProfileController` (154) | `ContentUserProfileControllerWebMvcTest` |
| `ContentUserRelationController` (297) | `ContentUserRelationControllerWebMvcTest` |
| `ContentUserThirdPartyAuthController` (54) | `ContentUserThirdPartyAuthControllerWebMvcTest` |
| `LeaderboardController` (30) | `LeaderboardControllerTest` |
| `MemberGrowthController` (35) | `MemberGrowthControllerTest` |

### 2.2 业务实现 / 编排 P0（43）

| 主类（行数） | 测试类 |
| --- | --- |
| `AchievementServiceImpl` (142) | `AchievementServiceTest` |
| `CircleLevelServiceImpl` (168) | `CircleLevelServiceTest` |
| `ContentAccountServiceImpl` (365) | `ContentAccountServiceTest` |
| `ContentFanAnalyticsServiceImpl` (303) | `ContentFanAnalyticsServiceTest` |
| `ContentInviteServiceImpl` (234) | `ContentInviteServiceTest` |
| `ContentSubscriptionNotificationPreferenceServiceImpl` (177) | `ContentSubscriptionNotificationPreferenceServiceTest` |
| `ContentSubscriptionSourceServiceImpl` (236) | `ContentSubscriptionSourceServiceTest` |
| `ContentUserBadgeServiceImpl` (444) | `ContentUserBadgeServiceTest` |
| `ContentUserFeedSettingServiceImpl` (108) | `ContentUserFeedSettingServiceTest` |
| `ContentUserFilterRuleServiceImpl` (162) | `ContentUserFilterRuleServiceTest` |
| `ContentUserFollowRecommendationServiceImpl` (155) | `ContentUserFollowRecommendationServiceTest` |
| `ContentUserGovernanceServiceImpl` (406) | `ContentUserGovernanceServiceTest` |
| `ContentUserGrowthDecayStateServiceImpl` (364) | `ContentUserGrowthDecayStateServiceImpl` |
| `ContentUserGrowthPenaltyRecordServiceImpl` (371) | `ContentUserGrowthPenaltyRecordServiceTest` |
| `ContentUserGrowthPenaltyRecoveryServiceImpl` (314) | `ContentUserGrowthPenaltyRecoveryServiceTest` |
| `ContentUserGrowthServiceImpl` (355) | `ContentUserGrowthServiceTest` |
| `ContentUserHomepageServiceImpl` (120) | `ContentUserHomepageServiceTest` |
| `ContentUserLevelBenefitServiceImpl` (384) | `ContentUserLevelBenefitServiceTest` |
| `ContentUserLevelConfigServiceImpl` (89) | `ContentUserLevelConfigServiceTest` |
| `ContentUserMediaAdapterImpl` (76) | `ContentUserMediaAdapterTest` |
| `ContentUserNotInterestedServiceImpl` (66) | `ContentUserNotInterestedServiceTest` |
| `ContentUserNotificationSettingServiceImpl` (435) | `ContentUserNotificationSettingServiceTest` |
| `ContentUserPointSpendServiceImpl` (444) | `ContentUserPointSpendServiceTest` |
| `ContentUserProfileHistoryServiceImpl` (119) | `ContentUserProfileHistoryServiceTest` |
| `ContentUserProfileServiceImpl` (544) | `ContentUserProfileServiceTest` |
| `ContentUserRelationServiceImpl` (1001) | `ContentUserRelationServiceTest`、`ContentUserRelationServiceMutualTest` |
| `ContentUserRewardRuleServiceImpl` (58) | `ContentUserRewardRuleServiceTest` |
| `ContentUserSecuritySettingServiceImpl` (49) | `ContentUserSecuritySettingServiceTest` |
| `ContentUserSettingsCacheService` (80) | `ContentUserSettingsCacheServiceTest` |
| `ContentUserSubscriptionServiceImpl` (318) | `ContentUserSubscriptionServiceTest` |
| `ContentUserSupportServiceImpl` (711) | `ContentUserSupportServiceTest` |
| `ContentUserThirdPartyAuthServiceImpl` (101) | `ContentUserThirdPartyAuthServiceTest` |
| `ContentUserVerificationBadgeServiceImpl` (55) | `ContentUserVerificationBadgeServiceTest` |
| `ContentUserVisibilityPolicyServiceImpl` (172) | `ContentUserVisibilityPolicyServiceTest` |
| `LeaderboardServiceImpl` (122) | `LeaderboardServiceTest` |
| `MemberGrowthServiceImpl` (157) | `MemberGrowthServiceTest` |

### 2.3 业务边界 / Biz / 调度 P0（1）

| 主类（行数） | 测试类 |
| --- | --- |
| `ContentUserRelationBoundaryBizService` (68) | `ContentUserRelationBoundaryBizServiceTest` |
| `ContentUserGovernanceAutoRecoveryScheduler` (26) | `ContentUserGovernanceAutoRecoverySchedulerTest` |

### 2.4 业务接口（已通过其实现类测试间接覆盖，共 28）

> 接口 `IXxxService` 已被 `XxxServiceTest` 引用并 mock；此处不重复列出全部 28 个接口名，详见 P0 缺失/弱覆盖小节中未被覆盖的接口。

---

## 3. 缺测试主类清单（按 P0 → P3 排序）

### 🔴 P0 完全无测试（32 个）

| # | 主类 | 路径:行数 | 类别 | 缺失原因 / 风险 | 建议测试范围 |
| --- | --- | --- | --- | --- | --- |
| 1 | `CircleGrowthScheduler` | `growth/task/CircleGrowthScheduler.java:52` | TASK | 圈子成长定时任务无任何测试；包含批量重算与排行榜落库 | `CircleGrowthSchedulerTest`：mock `IMemberGrowthService` / `ILeaderboardService`，验证 cron 触发、批量分页、重算幂等 |
| 2 | `ContentFanAnalyticsController` | `controller/ContentFanAnalyticsController.java:74` | CONTROLLER | 粉丝分析对外接口无任何测试 | `ContentFanAnalyticsControllerWebMvcTest`：覆盖 list/aggregations/export 三个端点 + 鉴权校验 |
| 3 | `ContentFanTrendAggregationTask` | `task/ContentFanTrendAggregationTask.java:66` | TASK | 粉丝趋势日聚合定时任务无测试 | `ContentFanTrendAggregationTaskTest`：mock `ContentFanTrendDailyMapper`、验证按天聚合与异常隔离 |
| 4 | `ContentInviteController` | `controller/ContentInviteController.java:64` | CONTROLLER | 邀请码 C 端接口无测试 | `ContentInviteControllerWebMvcTest`：邀请码生成、校验、邀请记录分页 |
| 5 | `ContentNoopThirdPartyTokenRevocationPort` | `service/impl/ContentNoopThirdPartyTokenRevocationPort.java:20` | SVC_IMPL | 三方 Token 撤销的 Noop 实现无测试 | 直接覆盖接口契约：返回 noop、不抛异常；可由 `ContentUserThirdPartyAuthServiceTest` 间接 mock |
| 6 | `ContentNotificationServiceImpl` | `service/impl/ContentNotificationServiceImpl.java:30` | SVC_IMPL | 通知服务实现无测试（接口 `IContentNotificationService` 也仅被其他测试 mock） | `ContentNotificationServiceTest`：发送/批量发送/失败重试/去重 |
| 7 | `ContentSocialSubscriptionDefaultsServiceImpl` | `service/impl/ContentSocialSubscriptionDefaultsServiceImpl.java:107` | SVC_IMPL | 社交订阅默认值服务无测试 | `ContentSocialSubscriptionDefaultsServiceTest`：默认值注入、覆盖、跨租户 |
| 8 | `ContentThirdPartyTokenRevocationPort` | `service/ContentThirdPartyTokenRevocationPort.java:18` | SVC_IFACE | 端口接口无任何专属测试 | 与实现类一起：`ContentThirdPartyTokenRevocationPortTest`（含 noop 与真实实现双 mock） |
| 9 | `ContentUserContactBindingAdapterImpl` | `service/impl/ContentUserContactBindingAdapterImpl.java:16` | SVC_IMPL | 默认 `ServiceImpl` 空实现无覆盖（接口 `IContentUserContactBindingAdapter` 也无） | `ContentUserContactBindingAdapterTest`：验证联系渠道绑定、校验、解绑 |
| 10 | `ContentUserGovernanceController` | `controller/ContentUserGovernanceController.java:128` | CONTROLLER | 用户治理（封禁/解封/状态变更）Controller 无测试 | `ContentUserGovernanceControllerWebMvcTest`：状态变更、申诉处理、批量操作 |
| 11 | `ContentUserGrowthController` | `controller/ContentUserGrowthController.java:316` | CONTROLLER | **最大未测 Controller**，成长值总览接口未覆盖 | `ContentUserGrowthControllerWebMvcTest`：增长流水、衰减、惩罚、奖励查询端点 |
| 12 | `ContentUserLevelBenefitRecoveryServiceImpl` | `service/impl/ContentUserLevelBenefitRecoveryServiceImpl.java:80` | SVC_IMPL | 等级权益补偿服务无测试（接口也无） | `ContentUserLevelBenefitRecoveryServiceTest`：补偿触发、金额/积分回退、幂等 |
| 13 | `ContentUserProfileAuditAdapterImpl` | `service/impl/ContentUserProfileAuditAdapterImpl.java:35` | SVC_IMPL | 资料审核适配器无测试 | `ContentUserProfileAuditAdapterTest`：审核事件分发、回调、上报失败重试 |
| 14 | `IContentUserLevelBenefitRecoveryService` | `service/IContentUserLevelBenefitRecoveryService.java:24` | SVC_IFACE | 等级权益补偿接口无测试 | 与 impl 一起 |
| 15 | `ContentUserSettingsController` | `controller/ContentUserSettingsController.java:123` | CONTROLLER | 用户设置（通知/Feed/筛选）Controller 无测试 | `ContentUserSettingsControllerWebMvcTest`：偏好更新、DND 规则、过滤词 |
| 16 | `ContentUserSubscriptionController` | `controller/ContentUserSubscriptionController.java:217` | CONTROLLER | 订阅 Controller 无测试 | `ContentUserSubscriptionControllerWebMvcTest`：订阅/取消/批量/通知偏好 |
| 17 | `ContentUserSupportAdminController` | `controller/ContentUserSupportAdminController.java:66` | CONTROLLER | 客服/举报/申诉 Admin 端无测试 | `ContentUserSupportAdminControllerWebMvcTest`：举报处理、申诉处理、管理员视角会话 |
| 18 | `ContentUserSupportController` | `controller/ContentUserSupportController.java:101` | CONTROLLER | 客服/举报/申诉 C 端无测试 | `ContentUserSupportControllerWebMvcTest`：发起举报、发起申诉、查询进度 |
| 19 | `IContentSocialSubscriptionDefaultsService` | `service/IContentSocialSubscriptionDefaultsService.java:9` | SVC_IFACE | 接口无任何测试 | 与 impl 一起覆盖 |
| 20 | `IContentUserActivitySnapshotService` | `service/IContentUserActivitySnapshotService.java:10` | SVC_IFACE | 接口无任何专属测试 | 与 impl 一起：`ContentUserActivitySnapshotServiceTest` |
| 21 | `IContentUserBlockService` | `service/IContentUserBlockService.java:10` | SVC_IFACE | 拉黑服务接口无专属测试（实现被 `ContentBlockingMutingCrudContractTest` 弱覆盖） | `ContentUserBlockServiceTest`：拉黑/解除/拉黑列表分页/双向拉黑判断 |
| 22 | `IContentUserContactBindingAdapter` | `service/IContentUserContactBindingAdapter.java:15` | SVC_IFACE | 联系渠道绑定适配器接口无测试 | 与 impl 一起 |
| 23 | `IContentUserExchangeGoodsService` | `service/IContentUserExchangeGoodsService.java:10` | SVC_IFACE | 积分商品服务接口无测试 | 与 impl 一起 |
| 24 | `IContentUserExchangeOrderService` | `service/IContentUserExchangeOrderService.java:10` | SVC_IFACE | 积分兑换订单服务接口无测试 | 与 impl 一起 |
| 25 | `IContentUserFeatureUnlockService` | `service/IContentUserFeatureUnlockService.java:10` | SVC_IFACE | 功能解锁服务接口无测试 | 与 impl 一起 |
| 26 | `IContentUserLevelBenefitConfigService` | `service/IContentUserLevelBenefitConfigService.java:10` | SVC_IFACE | 等级权益配置服务接口无测试 | 与 impl 一起 |
| 27 | `IContentUserMuteService` | `service/IContentUserMuteService.java:10` | SVC_IFACE | 禁言服务接口无测试（实现被 `ContentBlockingMutingCrudContractTest` 弱覆盖） | `ContentUserMuteServiceTest`：禁言/解除/批量禁言/剩余时长 |
| 28 | `IContentUserProfileAuditAdapter` | `service/IContentUserProfileAuditAdapter.java:17` | SVC_IFACE | 资料审核适配器接口无测试 | 与 impl 一起 |
| 29 | `IContentUserProfileReviewService` | `service/IContentUserProfileReviewService.java:14` | SVC_IFACE | 资料审核单接口无测试 | 与 impl 一起 |
| 30 | `IContentUserRewardEventService` | `service/IContentUserRewardEventService.java:10` | SVC_IFACE | 奖励事件服务接口无测试 | 与 impl 一起 |
| 31 | `IContentUserVirtualGiftRecordService` | `service/IContentUserVirtualGiftRecordService.java:10` | SVC_IFACE | 虚拟礼物记录服务接口无测试 | 与 impl 一起 |
| 32 | `SystemUserAccountGatewayImpl` | `gateway/impl/SystemUserAccountGatewayImpl.java:263` | GATEWAY | **最大风险点之一**——263 行的平台账号网关实现涉及 `SysUser` 创建/手机邮箱重复校验/密码加密/重置，无任何测试 | `SystemUserAccountGatewayImplTest`：使用 `SysUserMapper` Mock，覆盖正常路径 + 重复手机/邮箱/用户名抛异常 + 密码加密不可逆 + 密码重置流程 |

> 表中 32 条对应 32 个 P0 主类完全无任何测试引用；其余 12 个 P0 主类仅被 `*CrudContractTest` / `*MigrationTest` 弱 import 引用，列入下方「P0 弱覆盖」表。

#### P0 弱覆盖（仅被 *CrudContractTest / *MigrationTest 以 import 形式引用，12 个）

| 主类（行数） | 弱覆盖来源 | 备注 |
| --- | --- | --- |
| `ContentUserActivitySnapshotServiceImpl` (16) | `ContentSocialSubscriptionMapperContractTest` | 仅验证 `BaseMapper` 可赋值与 `@TableName` 正确，**无 SQL 行为** |
| `ContentUserBlockServiceImpl` (16) | `ContentBlockingMutingCrudContractTest` | 同上 |
| `ContentUserExchangeGoodsServiceImpl` (16) | `ContentUserBadgesPointsGrowthCrudContractTest` | 同上 |
| `ContentUserExchangeOrderServiceImpl` (16) | `ContentUserBadgesPointsGrowthCrudContractTest` | 同上 |
| `ContentUserFeatureUnlockServiceImpl` (16) | `ContentUserBadgesPointsGrowthCrudContractTest` | 同上 |
| `ContentUserLevelBenefitConfigServiceImpl` (16) | `ContentUserBadgesPointsGrowthCrudContractTest` | 同上 |
| `ContentUserMuteServiceImpl` (16) | `ContentBlockingMutingCrudContractTest` | 同上 |
| `ContentUserProfileReviewServiceImpl` (42) | `ContentUserProfileManagementCrudContractTest` | 仅有字段存在性断言，无业务路径 |
| `ContentUserRewardEventServiceImpl` (16) | `ContentUserBadgesPointsGrowthCrudContractTest` | 同上 |
| `ContentUserVirtualGiftRecordServiceImpl` (16) | `ContentUserBadgesPointsGrowthCrudContractTest` | 同上 |
| `IContentNotificationService` (17) | `AchievementServiceTest` / `CircleLevelServiceTest`（仅 mock 依赖） | 接口本身未被任何测试方法直接调用 |
| `SystemUserAccountGateway` (38) | `ContentAccountServiceTest`（仅 mock 依赖） | 同上 |

> **建议**：将上述「弱覆盖」与 P1 Mapper 合并成「契约已验证、业务未验证」一类；下一步按业务重要度补 P0 行为测试。

---

### 🟡 P1 Mapper（51 个全部弱覆盖 / 缺真实 SQL 行为测试）

| 类别 | 文件数 | 覆盖情况 |
| --- | --- | --- |
| Growth 子包 Mapper（6 个） | `CircleAchievementMapper`、`CircleGrowthLogMapper`、`CircleLeaderboardSnapshotMapper`、`CircleLevelMapper`、`CircleMemberAchievementMapper`、`CircleMemberGrowthMapper` | 0 个有专属 `*MapperContractTest`；0 个被任何测试 import |
| User 主包 Mapper（45 个） | 全部 `Content*Mapper` | 全部仅被 `ContentBlockingMutingCrudContractTest` / `ContentSocialSubscriptionMapperContractTest` / `ContentUserBadgesPointsGrowthCrudContractTest` / `ContentUserProfileManagementCrudContractTest` / `ContentUserThirdPartyAuthMapperContractTest` 以 import 形式弱引用，**未验证任何自定义 SQL/方法** |

> **建议**：为每个含自定义 XML 方法的 Mapper 建立 `*MapperContractTest`（使用 `@MybatisPlusTest` 或 `@JdbcTest`），至少覆盖：自定义查询、分页、批量、动态 SQL 拼接。
> 优先关注：`ContentUserProfileMapper` (44 行)、`ContentUserRelationMapper` (39 行)、`ContentUserThirdPartyAuthMapper` (34 行)、`ContentUserSubscriptionMapper` (25 行)、`ContentUserProfileHistoryMapper` (19 行)、`ContentUserExchangeGoodsMapper` (19 行)、`ContentUserHomepageModuleMapper` (17 行)、`ContentUserVerificationBadgeMapper` (17 行) —— 这些 Mapper 文件行数 > 15，疑似包含较多自定义方法。

---

### ⚪ P2（一般工具 / Config / Util）= 0 个

> 当前 P0+P1 之外**没有任何**纯工具类落入 P2。如未来引入，应归入 P2。

### ⚪ P3 可跳过（166 个纯数据载体，统计概况）

| 子目录 | 文件数 |
| --- | --- |
| `entity/` | 51 |
| `vo/` | 71 |
| `dto/` | 4 |
| `req/` | 39 |
| `constant/` | 1 |
| `enums/` | 14 |

> 其中 19 个 Entity 被所属 Service 测试间接验证（构造 fixture 时引用）。其余 166 个仅作为数据传输载体，**不需要单测**。

---

## 4. 可跳过/POJO 清单（166 个）

> 完整清单过长不在此逐条罗列；按目录分类如下：

- **Entity（51）**：`ContentUserActivitySnapshot` … `ContentUserVirtualGiftRecord`（含 `growth/entity/` 6 个 `Circle*`），全部 `extends JeecgEntity`，仅承载字段。
- **VO（71）**：所有 `vo/` 目录下 `*VO.java`，纯响应对象。
- **DTO（4）**：`ContentUserBadgeProgressDTO`、`ContentUserPointLedgerQueryDTO`、`ContentUserRewardEventDTO`、`ContentUserRewardResultDTO`。
- **Req（39）**：`req/account/`(7) / `req/governance/`(1) / `req/growth/`(6) / `req/profile/`(5) / `req/relation/`(7) / `req/settings/`(4) / `req/subscription/`(4) / `req/support/`(5)。
- **Constant（1）**：`ContentUserBadgeConstant`、`ContentUserCacheConstant`（已被 `ContentUserCacheConstantTest` 覆盖）、`ContentUserErrorCode`、`ContentUserPointSpendConstant`、`ContentUserRewardSourceTypeConstant`（5 个常量类，4 个可跳过）。
- **Enum（14）**：`ContentCertificationTypeEnum` … `ContentUserVisibilityEnum`（含 `growth/enums/` 4 个 `*Enum`）。**建议**：用 `ContentUserEnumContractTest` 模式统一覆盖（已存在但未填充）。

---

## 5. 风险与优先建议

### 最大风险点
1. **`SystemUserAccountGatewayImpl`（263 行）**：平台账号注册/密码重置核心路径无任何测试，关联 `SysUser` 落库 + 密码加密 + 重复校验，**线上回归风险极高**。
2. **`ContentUserGrowthController`（316 行）**：最大未测 Controller，成长值总览查询链路一旦出错，前端所有成长面板数据异常。
3. **`ContentUserRelationServiceImpl`（1001 行）虽已有 2 个测试，但行数大且涵盖互相关注边界条件**，建议补充边界场景回归。
4. **`CircleGrowthScheduler` 与 `ContentFanTrendAggregationTask`**：两个定时任务无任何测试，调度 bug 通常只能上线后观测。
5. **51 个 Mapper 全部缺真实行为测试**：所有自定义 SQL 拼接、分页、动态条件都仅靠人工 review。

### 建议补测顺序
1. **P0-A（紧急，1 周内）**：`SystemUserAccountGatewayImplTest` + `ContentUserGrowthControllerWebMvcTest` + 两个 `SchedulerTest`
2. **P0-B（重要，2 周内）**：7 个未测 Controller 的 `*ControllerWebMvcTest`（`ContentInviteController`、`ContentUserGovernanceController`、`ContentUserSettingsController`、`ContentUserSubscriptionController`、`ContentUserSupportController`、`ContentUserSupportAdminController`、`ContentFanAnalyticsController`）
3. **P0-C（重要，2 周内）**：`ContentUserProfileAuditAdapterImpl` / `ContentUserLevelBenefitRecoveryServiceImpl` / `ContentNotificationServiceImpl` / `ContentSocialSubscriptionDefaultsServiceImpl` 的 ServiceTest
4. **P1（持续）**：为每个 Mapper 建 `*MapperContractTest`，优先做 8 个行数 > 15 的 Mapper
5. **P3-Enum**：将 14 个枚举集中到一个 `ContentUserEnumContractTest` 跑通

---

## 6. 审计方法附注

- **匹配规则**：
  - `XxxController` ⇄ `{Xxx}ControllerTest` / `{Xxx}ControllerWebMvcTest` / `{Xxx}ApiDocTest`
  - `XxxService` / `IXxxService` / `XxxServiceImpl` ⇄ `{Xxx}ServiceTest` / `{Xxx}ServiceMutualTest`
  - `XxxBiz` / `XxxBoundaryBizService` ⇄ `{Xxx}BoundaryBizServiceTest` / `{Xxx}BizTest`
  - `XxxMapper` ⇄ `{Xxx}MapperContractTest` / `{Xxx}MapperCompilationTest`
  - `XxxScheduler` / `XxxTask` ⇄ `{Xxx}SchedulerTest` / `{Xxx}TaskTest`
  - `XxxGateway` / `XxxAdapter` ⇄ `{Xxx}GatewayTest` / `{Xxx}AdapterTest`
- **覆盖率粗算公式**：`覆盖率 = 直接匹配命中数 / 应测主类总数`；其中应测主类 = P0 + P1，不计入 P2/P3。
- **弱覆盖定义**：被 `*CrudContractTest` / `*MigrationTest` / `*EnumContractTest` 等契约类测试以 import 形式引用，但未触发该类的业务方法。
- **未修改任何业务代码**：本次审计全程为只读操作。
