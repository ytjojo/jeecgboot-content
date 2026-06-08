# 内容社区模块架构概览

内容社区模块（`jeecg-module-content`）是一个大型业务模块，包含 6 个子域，约 618 个 Java 文件、100 个实体和 58 个 Controller。

## 子域概览

```
jeecg-module-content/src/main/java/org/jeecg/modules/content/
├── auth/          # 认证与安全 — 注册、登录、设备管理、风险控制
├── channel/       # 频道管理 — 频道 CRUD、发布、治理、发现、搜索
├── circle/        # 圈子管理 — 圈子 CRUD、内容、排行、推荐
├── user/          # 用户中心 — 个人资料、关系、订阅、积分、徽章、成长
│   └── growth/    # 成长体系 — 成就、等级、排行榜（user 子域内的独立功能组）
└── userstatus/    # 用户状态管理 — 封禁、限制、状态流转
```

| 子域 | 文件数 | Controller | Entity | Service | Biz |
|------|--------|-----------|--------|---------|-----|
| auth | 79 | 3 | 5 | 9+5 ports | 6 |
| channel | 149 | 24 | 31 | 36 | 14 |
| circle | 69 | 12 | 10 | 12 | 10 |
| user | ~280 | 14 | 46 | 43 | 1 |
| user/growth | 23 | 4 | 6 | 4 | 0 |
| userstatus | 18 | 1 | 2 | 2 | 1 |

## 子域职责与边界

### auth — 认证与安全

**Controller**:
- `ContentAuthController` — `/api/v1/content/auth` — 注册、登录、绑定、解绑、密码重置、验证码、登出
- `ContentAccountCancellationController` — `/api/v1/content/account-cancellation` — 账号注销申请与撤销
- `ContentRiskControlController` — `/api/v1/content/account-security` — 风险事件申诉、设备信任、安全状态查询

**核心 Entity**: `ContentUserAccount`、`ContentUserCredential`、`ContentUserPasswordHistory`、`ContentRiskEvent`、`ContentCancellationRequest`

**Port 接口**（外部依赖抽象层）: `SmsSenderPort`、`EmailSenderPort`、`CaptchaVerifyPort`、`IpGeolocationPort`、`LoginTokenGeneratorPort`，默认提供 Noop 实现，实际接入时替换。

**依赖方向**: auth → user（注册时需创建 user 子域的 Profile、NotificationSetting、DeviceSession）

### channel — 频道管理

**Controller**（19 个）:
| Controller | 路径前缀 | 职责 |
|---|---|---|
| ChannelController | `/api/v1/content/channels` | 频道 CRUD、转让 |
| ChannelAdminController | `/api/v1/content/admin/channels` | 管理员频道操作 |
| ChannelPublishController | `/api/v1/content/channel/publish` | 内容发布 |
| ChannelMemberController | `/api/v1/content/channel/member` | 成员加入/退出/角色 |
| ChannelGovernanceController | `/api/v1/content/channel/governance` | 成员治理（禁言、黑名单） |
| ChannelLifecycleController | `/api/v1/content/channel/lifecycle` | 生命周期（冻结、隐藏、关闭、归档） |
| ChannelReviewController | `/api/v1/content/channel/review` | 审核 |
| ChannelSubscriptionController | `/api/v1/content/channel/subscription` | 订阅 |
| ChannelInviteController | `/api/v1/content/channel/invite` | 邀请 |
| ChannelMergeController | `/api/v1/content/channel/merge` | 合并频道 |
| ChannelAnnouncementController | `/api/v1/content/channel/announcement` | 公告 |
| ChannelStatsController | `/api/v1/content/channel/stats` | 数据统计 |
| ChannelExportController | `/api/v1/content/channel/export` | 数据导出 |
| ContentChannelCategoryController | `/api/v1/content/channel/category` | 频道分类 |
| ContentChannelDiscoveryController | `/api/v1/content/channel/discovery` | 发现页 |
| ContentChannelRankingController | `/api/v1/content/channel/ranking` | 排行榜 |
| ContentChannelRecommendationController | `/api/v1/content/channel/recommendation` | 推荐 |
| ContentChannelSearchController | `/api/v1/content/channel/search` | 搜索 |
| ContentChannelBrowseController | `/api/v1/content/channel/browse` | 浏览 |

**核心 Entity**: `Channel`、`ChannelMember`、`ChannelSubscription`、`ChannelContentPublish`、`ChannelReview`、`ContentChannelCategory`

**依赖方向**: channel → user（通过 `IContentNotificationService` 发送通知）

### circle — 圈子管理

**Controller**（12 个）:
- `CircleController` — `/api/v1/content/circle` — 圈子 CRUD、加入/退出
- `CircleContentController` — 圈子帖子列表
- `CircleAnnouncementController` — 公告
- `CircleMemberController` — 成员管理
- `CircleJoinReviewController` — 加入审批
- `CircleReportController` — 举报处理
- `CircleRankingController` — 排行
- `CircleRecommendController` — 推荐
- `CircleSearchController` — 搜索
- `CircleDataController` — 数据统计
- `CircleContentPinController` — 帖子置顶/精选
- `CircleGovernanceLogController` — 治理日志

**核心 Entity**: `Circle`、`CircleMember`、`CircleContent`、`CircleReport`、`CircleJoinRequest`、`CircleDataStatistics`

**依赖方向**: circle → user（通过 `IContentNotificationService`、`IContentUserBadgeService`）

### user — 用户中心

**Controller**（14 个，统一 `/api/v1/content/user/` 前缀）:

| Controller | 路径 | 职责 |
|---|---|---|
| ContentAccountController | `/account` | 账号注册、绑定 |
| ContentUserProfileController | `/profile` | 个人资料 |
| ContentUserRelationController | `/relation` | 关注、拉黑、静音 |
| ContentUserSettingsController | `/settings` | 隐私、通知设置 |
| ContentUserSubscriptionController | `/subscription` | 订阅管理 |
| ContentUserGrowthController | `/growth` | 积分、徽章、等级 |
| ContentUserGovernanceController | `/governance` | 用户治理 |
| ContentUserSupportController | `/support` | 申诉、举报、客服 |
| ContentFanAnalyticsController | `/fan` | 粉丝分析 |
| ContentUserFilterRuleController | `/filter-rule` | 过滤规则 |
| ContentUserNotInterestedController | `/user` | 不感兴趣 |
| ContentInviteController | `/invite` | 邀请码 |
| ContentUserThirdPartyAuthController | `/auth/third-party` | 第三方账号绑定 |
| ContentUserSupportAdminController | `/support/admin` | 管理员客服处理 |

**核心 Entity**: `ContentUserProfile`、`ContentUserRelation`、`ContentUserBlock`、`ContentUserMute`、`ContentUserSubscription`、`ContentUserBadgeGrant`、`ContentUserPointLedger`、`ContentUserPrivacySetting`、`ContentUserNotificationSetting`、`ContentUserDeviceSession` 等 46 个

**Gateway**: `SystemUserAccountGateway` — 与系统模块 `SysUser` 的桥接接口，实现位于 `user/gateway/impl/`

**依赖方向**: user 是最底层的子域，不依赖任何其他子域（仅依赖外部 `jeecg-system-biz` 的 `SysUser`）

### user/growth — 成长体系

**Controller**（4 个）: `AchievementController`、`CircleLevelController`、`LeaderboardController`、`MemberGrowthController`，前缀 `/api/v1/content/user/growth`

**核心 Entity**: `CircleMemberGrowth`、`CircleGrowthLog`、`CircleAchievement`、`CircleMemberAchievement`、`CircleLevel`、`CircleLeaderboardSnapshot`

### userstatus — 用户状态管理

**Controller**: `UserStatusController` — `/api/v1/content/user-status` — 状态查询、变更、解除、审计日志

**核心 Entity**: `UserStatusAuditLog` + `UserStatusEnum`（枚举）

**AOP**: `@CheckUserStatus` 注解 + `UserStatusCheckAspect` 切面，用于拦截被限制用户的操作

**依赖方向**: userstatus → user（读取 `ContentUserProfile`）、userstatus → auth（调用 `IContentVerificationCodeService`）

## 子域间依赖关系

```
                    ┌──────────────────┐
                    │   jeecg-system   │
                    │   (SysUser)      │
                    └────────┬─────────┘
                             │
                    ┌────────▼─────────┐
                    │      user        │  ← 最底层，被所有子域依赖
                    │  (Profile,       │
                    │   Relation,      │
                    │   Notification,  │
                    │   Badge...)      │
                    └───┬────┬────┬────┘
                        │    │    │
          ┌─────────────┘    │    └─────────────┐
          │                  │                  │
   ┌──────▼──────┐   ┌──────▼──────┐   ┌───────▼──────┐
   │    auth     │   │   channel   │   │    circle    │
   │ (注册时创建  │   │ (通知用户    │   │ (通知、徽章)  │
   │  Profile等) │   │  状态变更)   │   │              │
   └─────────────┘   └─────────────┘   └──────────────┘
          │
   ┌──────▼──────┐
   │ userstatus  │  ← 也依赖 auth（验证码服务）
   └─────────────┘
```

- **无循环依赖**：依赖关系是单向的
- channel、circle、user/growth 之间相互独立，无直接引用
- user 子域是整个内容模块的"基础设施层"

## 分层架构

每个子域遵循统一的分层结构：

```
{子域}/
├── controller/    # @RestController，接收参数、调用 biz/service，返回 Result
├── biz/           # 业务编排层，跨多个 service 的复杂逻辑放在这里
├── service/       # 服务接口 + impl/
├── entity/        # 数据库实体，@TableName 映射
├── mapper/        # MyBatis-Plus BaseMapper
├── dto/           # 内部传输对象（区别于入参 req 和出参 vo）
├── req/           # 请求入参 DTO（含 @Valid 校验）
├── vo/            # 响应出参 VO
├── enums/         # 枚举
├── constant/      # 常量
├── scheduler/     # 定时任务
└── task/          # 异步任务
```

**关键规则**：
- Controller 只做三件事：接收参数、参数校验、调用下层并返回 `Result`
- 单表操作放在 Service 层，跨 Service 编排放在 Biz 层
- 跨子域编排逻辑放在发起方子域的 Biz 层（如 auth 注册时调用 user 子域创建 Profile，由 `ContentAuthBizServiceImpl` 编排）

## Flyway 迁移脚本

存在两种迁移路径：

| 路径 | 命名格式 | 用途 |
|------|----------|------|
| `db/migration/` | `V{N}__{desc}.sql` | 早期迁移（channel 基础设施） |
| `flyway/sql/mysql/` | `V3.9.1_{N}__{desc}.sql` | 当前主力路径，按功能域组织 |

**迁移脚本按功能域分布**（`flyway/sql/mysql/`）：

| 编号范围 | 功能域 |
|----------|--------|
| V50-V53 | user 领域初始化、成长惩罚、等级权益、个人资料 |
| V54-V59 | 社交订阅、徽章积分、关系、拉黑静音、隐私通知 |
| V60-V62 | 客服会话、认证基础、会话/风险/注销 |
| V63-V67 | circle 内容互动、表结构、数据统计、推荐、成长体系 |
| V68-V69 | channel 发现页、生命周期统计 |
| V70-V71 | 用户状态审计日志、内容菜单 |

回滚脚本对应命名：`R{编号}__{desc}_rollback.sql`。

## 与系统模块的交互点

内容模块通过以下方式与 `jeecg-system-biz` 等系统模块交互：

| 交互点 | 方式 | 位置 |
|--------|------|------|
| 系统用户桥接 | `SystemUserAccountGateway` 接口 | `user/gateway/` |
| 当前用户获取 | `SecureUtil.getCurrentUser()` | 各 Controller |
| 通用响应封装 | `Result<T>` | 所有 Controller |
| 实体基类 | `JeecgEntity` | 所有 Entity |
| 查询构建 | `QueryGenerator` | 列表查询 Controller |
| JWT 工具 | `JwtUtil` | auth、circle |
| 密码工具 | `PasswordUtil` | auth biz、user gateway |

在内容模块中新增功能时，如需访问系统用户信息，应通过 `SystemUserAccountGateway` 接口而非直接调用 `SysUserMapper`。
