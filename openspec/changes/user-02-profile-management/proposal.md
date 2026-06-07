## Why

内容社区已有 `content_user_profile`、隐私设置和资料接口雏形，但还缺少 EPIC-02 要求的资料审核、主页个性化、认证标识、完整字段可见性、缓存失效和昵称/头像历史能力。
现在需要在现有 `content/user` 用户域上补齐产品级资料管理，承接 EPIC-01 认证后的用户身份展示与隐私边界。

## What Changes

> **实施更新（2026-06-03）**: 本变更已通过 `ContentUserProfileController`（`/api/v1/content/user/profile` 路径前缀）实现并完成单测覆盖。统一端点 `/profile/update` 承载基础资料 + 主页配置 + 模块排序 + 认证文案的合并提交；隐私接口覆盖 15 个 `*Visibility` 字段。

- 补齐基础资料维护：昵称、头像、简介、性别、生日、地区、职业、个人链接等字段的保存、字段校验（`@NotBlank`/`@Size`/`@Pattern`）和审核状态。
- 主页背景图与头像：本期 **仅持久化 CDN URL**（不提供独立上传端点），由前端 OSS 客户端直传后回填 URL；JPG/PNG/WebP、≤5MB 的素材约束由前端 + OSS 联合保证。
- 补齐主页个性化：背景图、主题色、模块显隐、模块排序、恢复默认配置；`ContentUserHomepageUpdateReq` 与 `/profile/homepage/update` 端点支持单独更新主页配置。
- 补齐认证标识展示：以 `content_user_verification_badge` 表为权威；`ContentUserProfileVO.verificationBadges` 内嵌聚合结果，`visualStyleKey` 字段映射前端图标/颜色。
- 补齐资料字段可见性：15 个 `*Visibility` 字段，枚举为 `PUBLIC` / `FOLLOWERS_ONLY` / `MUTUAL_ONLY` / `PRIVATE`；`onlineStatusVisibility` 特殊枚举 `PUBLIC` / `HIDDEN` / `MUTUAL_ONLY`。
- 补齐隐私缓存失效：资料详情查询统一由 `ContentUserProfileVO.from(...)` 在 viewer 视角下裁剪不可见字段，隐私设置变更后由后端清理用户公共缓存。
- 补齐曾用昵称和头像历史：`/profile/history/list?historyType=NICKNAME|AVATAR` 统一端点返回 `ContentUserProfileHistoryVO`，保留 180 天（`expiresAt`）、每类最多 20 条、倒序查询、恢复历史值。
- 不包含独立支付结算、企业组织通讯录、生物识别、两步验证和完整认证申请工作流的材料审批后台；`/profile/review/handle` 端点保留供后台管理系统调用。

## Capabilities

### New Capabilities

- `profile-management`: 内容社区个人资料与主页个性化能力，覆盖基础资料、审核、主页配置、认证标识展示、字段可见性、缓存失效和昵称/头像历史。

### Modified Capabilities

- 无。

## Impact

- **新增端点**（`/api/v1/content/user/profile` 前缀下 12 个接口）:
  - `GET /detail` 资料详情（owner + viewer 视角裁剪）
  - `POST /update` 统一资料更新
  - `POST /review/handle` 审核处理
  - `POST /privacy/update` 隐私配置更新
  - `POST /homepage/update` 主页配置更新
  - `POST /homepage/defaults/restore` 恢复主页默认
  - `GET /homepage/modules` 主页模块列表
  - `GET /badge/list` 认证标识列表
  - `GET /badge/detail` 认证标识详情
  - `GET /history/list` 历史记录列表（按 historyType 区分）
  - `POST /history/restore` 恢复历史
- **新增/扩展表**:`content_user_profile`、`content_user_privacy_setting`；扩展表 `content_user_profile_review`、`content_user_homepage_module`、`content_user_verification_badge`、`content_user_profile_history`。
- **新增 Redis 键**: 资料公共缓存、隐私变更失效触发；具体 key 由实现定义，遵循"隐私正确性优先"原则（详见 design.md Decision 6）。
- **新增 VO/Req**:`ContentUserProfileVO`、`ContentUserProfileUpdateReq`、`ContentUserProfileHistoryVO`、`ContentUserHomepageUpdateReq`、`ContentUserHomepageModuleReq`、`ContentUserPrivacyUpdateReq`、`ContentUserVerificationBadgeVO`、`ContentUserReviewHandleReq`（`req/profile/` 与 `vo/` 包下）。
- **依赖 EPIC-01**: 账号主体、登录态、手机号/邮箱绑定状态；在 EPIC-01 尚未完成时通过现有 `userId` 参数保持兼容。
- **外部依赖**: 头像/背景图 OSS 上传由前端负责（不引入内容社区模块的上传端点）；敏感词/AI 审核由适配器接口 `ContentUserProfileAuditAdapter` 接入。
