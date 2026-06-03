## Context

内容社区已有 `org.jeecg.modules.content.user` 用户域，包含 `ContentUserProfileController`、`IContentUserProfileService`、`ContentUserProfileServiceImpl`、`content_user_profile`、`content_user_privacy_setting` 和关系可见性策略。
> **实施更新（2026-06-03）**: 本变更的实现通过统一 `ContentUserProfileController` 端点完成，舍弃了原设计中"上传适配器/CDN 元数据校验"链路，转而由前端 OSS 直传 + 单一 `/profile/update` 端点保存 URL 的更轻量方案。

EPIC-02 依赖 EPIC-01 的注册登录、账号绑定和认证状态。由于 EPIC-01 仍是 OpenSpec 进行中变更，本设计保持与现有 `userId` 参数和 `SystemUserAccountGateway` 兼容。

## Goals / Non-Goals

**Goals:**

- 在现有 `content/user` 用户域内补齐资料管理、主页个性化、认证标识、隐私控制、缓存失效和历史记录能力。
- 保留现有 `content_user_profile` 作为当前有效资料主表，避免创建平行资料体系。
- 将审核、历史、认证标识、主页模块配置从主表中过重的 JSON/展示字段中拆出，便于约束、查询和清理。
- 对资料展示接口统一执行字段可见性裁剪（`ContentUserProfileVO.from(...)` 内聚裁剪），确保隐私设置对新请求即时生效。
- 使用 Redis 处理频率限制和缓存，数据库保存审核、认证、历史等长期事实。
- 使用 Flyway 管理新增表和字段，并提供回滚方案。

**Non-Goals:**

- 不实现独立支付结算和企业组织通讯录。
- 不实现完整实名认证供应商接入和认证材料审批后台，只提供认证标识记录、状态展示与详情查询的后端承载。
- 不实现独立的 OSS/文件上传端点（前端直传 + URL 持久化方案）。
- 不重构内容社区关注关系、粉丝统计、勋章体系和 EPIC-01 认证实现。
- 不一次性迁移所有 `/content/user/*` 接口到 `/api/v1/*`，仅在设计中保持后续兼容方向。

## Decisions

### 1. 保留主表，新增专用扩展表

`content_user_profile` 继续作为当前有效资料主表，保存展示时最常用的字段。新增表承载独立生命周期：

- `content_user_profile_review`: 待审核资料快照、审核状态、审核原因、原始值和目标值。
- `content_user_homepage_module`: 用户主页模块配置，替代主表中难以约束的模块排序 JSON。
- `content_user_verification_badge`: 个人、企业、达人、官方、实名、手机号/邮箱验证等认证标识记录。
- `content_user_profile_history`: 曾用昵称、头像历史记录，支持 180 天保留（`expires_at`）、每类最多 20 条和恢复。

理由：主表继续满足高频读取，扩展表满足审核、历史、认证等不同生命周期，避免把全部规则塞进 `ContentUserProfileServiceImpl` 的直接字段覆盖。

替代方案：继续使用 `nickname_history_json`、`avatar_history_json` 和 `certification_type` 字段。放弃原因是 JSON 难以做上限、过期、恢复、审核和并发控制。

### 2. 资料更新走编排服务，统一端点提交

新增或扩展资料编排服务（`ContentUserProfileServiceImpl` 内部），负责：

- 校验必填字段、长度、生日、链接、性别。
- 调用敏感词和 AI 审核适配接口（`ContentUserProfileAuditAdapter`）。
- 创建审核记录或直接更新 profile。
- 记录昵称/头像历史。
- 清理资料缓存。

**`POST /content/user/profile/update` 是统一更新端点**，入参为 `ContentUserProfileUpdateReq`（详见 [Delta 端点设计](#delta-端点设计)）。controller 内部委托 `IContentUserProfileService.updateProfile(...)`，service 内部按字段差异化处理：
- 基础资料字段（昵称/头像/简介/性别/生日/地区/职业/个人链接）→ 落 `content_user_profile`
- 主页配置（背景图/主题色/模块排序/认证文案）→ 落 `content_user_profile` + `content_user_homepage_module`

理由：基础资料与主页配置在同一事务内更新，避免出现"基础资料已生效但主页配置还在审核中"的不一致状态；同时减少前端提交次数。

替代方案：拆为 `/profile/update`、`/homepage/update` 两个独立端点。**最终选择**：保留 `/homepage/update` 作为单独端点（不影响主表的可选更新），但 `/profile/update` 同步支持主页相关字段以兼容历史接口。

### 3. 素材处理不再走后端上传端点

> **实施调整**: 原设计假设"头像/背景图通过后端上传端点处理 CDN 资源"；实际采用前端 OSS 客户端直传后回填 URL 的方案。

- 前端使用 OSS SDK 直接上传图片到对象存储；
- 上传完成后调用 `/content/user/profile/update` 时把 CDN URL 作为 `avatar` / `homepageBackground` 字段提交；
- 后端只校验 URL 格式与长度（`@Pattern` 校验、URL 长度上限），不参与文件接收与 CDN 处理；
- JPG/PNG/WebP、≤5MB 的素材约束由 OSS 客户端 + 前端校验保证。

理由：避免在后端引入 OSS SDK 依赖，降低服务复杂度；让前端可以直接走 CDN 边缘节点上传，节省一次回源。

替代方案：保留后端上传适配接口。放弃原因是增加后端依赖面、测试成本与上线复杂度，且在 5MB 小文件场景下优化收益不明显。

### 4. 认证标识独立于 profile 字段

认证标识以 `content_user_verification_badge` 为准，profile 响应通过聚合查询返回可展示 badge。VO 字段定义：

```java
// ContentUserVerificationBadgeVO
String badgeId;         // 标识 ID
String badgeType;       // INDIVIDUAL / ENTERPRISE / CREATOR / OFFICIAL / REAL_NAME / MOBILE / EMAIL
String badgeLabel;      // 展示标签
String visualStyleKey;  // 前端图标+颜色字典 key
LocalDateTime verifiedAt;
LocalDateTime expiresAt;
String description;
```

profile 响应 `verificationBadges` 字段为 `List<ContentUserVerificationBadgeVO>`，前端根据 `visualStyleKey` 映射图标和颜色。手机号/邮箱验证状态优先从 EPIC-01 账号层读取，在账号层未完成前使用适配接口兜底。

理由：认证有类型、状态、时间、描述、过期、展示规则，不应只是 profile 的两个字符串字段。

替代方案：继续使用 `certification_type` 和 `certification_label`。放弃原因是无法表达多认证、多状态、实名和绑定标识可见性。

### 5. 隐私裁剪在 VO 构建前统一执行

`ContentUserProfileVO.from(profile, privacy, relation, viewer)` 接收 4 个输入：
- `profile`: 原始资料
- `privacy`: 隐私设置
- `relation`: viewer 与 owner 的关系（`FOLLOWER` / `MUTUAL` / `STRANGER` / `SELF`）
- `viewer`: 实际查看者 userId（用于关系查询）

裁剪规则：
| 字段 | PUBLIC | FOLLOWERS_ONLY | MUTUAL_ONLY | PRIVATE |
|------|--------|----------------|-------------|---------|
| `bio`/`gender`/`birthday`/`region`/`profession`/`personalLink` | 所有人 | follower+ | mutual+ | 仅 owner |
| `homepageBackground`/`themeColor` | 所有人 | follower+ | mutual+ | 仅 owner |
| `certificationType`/`certificationLabel` | 所有人 | follower+ | mutual+ | 仅 owner |
| `verificationBadges`(手机/邮箱绑定) | 所有人 | follower+ | mutual+ | 仅 owner |
| `onlineStatus` | 所有人 | 仅自己 | 互关 | 仅自己（HIDDEN 等价 PRIVATE） |

特殊枚举 `onlineStatusVisibility` 仅取值 `PUBLIC` / `HIDDEN` / `MUTUAL_ONLY`，不提供 `PRIVATE`（语义上 `HIDDEN` 已覆盖此场景）。

理由：当前只隐藏生日，无法满足 EPIC-02 的多字段隐私承诺。把裁剪前置到 VO 转换可避免遗漏。

替代方案：前端按隐私字段隐藏。放弃原因是后端仍会泄露数据。

### 6. 缓存采用 Cache-Aside，但响应前仍做实时可见性判定

Redis 缓存只缓存可复用的资料基础数据或 owner 视角数据，最终响应必须按 viewer 当前关系和最新隐私设置过滤。隐私设置变更时调用 `ContentUserProfileServiceImpl.invalidateProfileCache(userId)` 清理用户公共缓存。

建议 Redis key（实现可调整命名，但必须覆盖以下场景）：
- `content:user:profile:{userId}`: 当前资料基础数据
- `content:user:privacy:{userId}`: 当前隐私设置
- `content:user:profile:public:{userId}`: 公共资料缓存，隐私变更时必须删除

**频率限制本期不在 controller 层实现**——`ContentUserProfileController` 不暴露 `update-count` 类端点；如果需要频率限制，应在 service 层引入（详见 Open Questions）。

理由：隐私正确性优先于缓存命中率，缓存不能成为泄漏旧字段的来源。

### 7. 历史恢复复用资料更新流程

`POST /content/user/profile/history/restore?userId=X&historyId=Y` 把历史值作为一次新的 `ContentUserProfileUpdateReq` 提交，重新执行必填校验、`@Pattern` 校验、敏感词审核、唯一性校验和审核规则。恢复成功后同样记录当前值进入历史。

理由：恢复也是对当前身份的改变，必须与普通修改遵守同样约束。

## Delta 端点设计

> 本节为实现回顾（2026-06-03），记录实际 controller 端点与原设计的差异。

### 端点清单（`ContentUserProfileController`）

| HTTP | 路径 | 入参 | 出参 | 说明 |
|------|------|------|------|------|
| GET | `/content/user/profile/detail` | `ownerUserId`, `viewerUserId` | `ContentUserProfileVO` | 资料详情（已裁剪） |
| POST | `/content/user/profile/update` | `userId` (query) + `ContentUserProfileUpdateReq` (body) | `ContentUserProfileVO` | 统一资料更新 |
| POST | `/content/user/profile/review/handle` | `ContentUserReviewHandleReq` (body) | `Result<?>` | 审核处理（后台） |
| POST | `/content/user/profile/privacy/update` | `userId` (query) + `ContentUserPrivacyUpdateReq` (body) | `ContentUserPrivacySettingVO` | 隐私配置更新 |
| POST | `/content/user/profile/homepage/update` | `userId` (query) + `ContentUserHomepageUpdateReq` (body) | `Result<ContentUserProfileVO>` | 主页配置更新 |
| POST | `/content/user/profile/homepage/defaults/restore` | `userId` (query) | `Result<ContentUserProfileVO>` | 恢复主页默认 |
| GET | `/content/user/profile/homepage/modules` | `userId` (query) | `List<ContentUserHomepageModuleVO>` | 主页模块列表 |
| GET | `/content/user/profile/badge/list` | `userId` (query) | `List<ContentUserVerificationBadgeVO>` | 认证标识列表 |
| GET | `/content/user/profile/badge/detail` | `badgeId` (query) | `ContentUserVerificationBadgeVO` | 认证标识详情 |
| GET | `/content/user/profile/history/list` | `userId`, `historyType` (query) | `List<ContentUserProfileHistoryVO>` | 历史记录列表 |
| POST | `/content/user/profile/history/restore` | `userId`, `historyId` (query) | `Result<ContentUserProfileVO>` | 恢复历史 |

### 统一更新入参 `ContentUserProfileUpdateReq`

```java
class ContentUserProfileUpdateReq {
    @NotBlank @Size(max=30) String nickname;
    @NotBlank @Size(max=512) String avatar;
    @Size(max=500) String bio;
    @Pattern(regexp="^(MALE|FEMALE|OTHER|UNKNOWN)$") String gender;
    @Past LocalDate birthday;
    @Size(max=64) String region;
    @Size(max=64) String profession;          // 注意：max=64 而非 PRD 原写 30
    @Size(max=256) @Pattern(regexp="^https?://.*$") String personalLink;
    @Size(max=512) String homepageBackground;
    @Size(max=16) @Pattern(regexp="^#[0-9A-Fa-f]{6}$") String themeColor;
    String moduleOrderJson;                    // JSON 字符串，由 service 层解析
    @Size(max=32) String certificationType;
    @Size(max=64) String certificationLabel;
    @Size(max=512) String certificationDescription;
}
```

### 隐私入参 `ContentUserPrivacyUpdateReq`

15 个 `*Visibility` 字段 + 2 个 Boolean：

| 字段 | 校验 |
|------|------|
| `bioVisibility` | `@Pattern(PUBLIC|FOLLOWERS_ONLY|MUTUAL_ONLY|PRIVATE)` |
| `genderVisibility` | 同上 |
| `birthdayVisibility` | 同上 |
| `regionVisibility` | 同上 |
| `professionVisibility` | 同上 |
| `personalLinkVisibility` | 同上 |
| `homepageBackgroundVisibility` | 同上 |
| `themeColorVisibility` | 同上 |
| `certificationVisibility` | 同上 |
| `verificationBadgesVisibility` | 同上 |
| `onlineStatusVisibility` | `@Pattern(PUBLIC|HIDDEN|MUTUAL_ONLY)`（特殊枚举） |
| `homepageModuleVisibility` | 同 visibility 模式 |
| `profileCompletionVisibility` | 同上 |
| `profileReviewStatusVisibility` | 同上 |
| `recentActivityVisibility` | 同上 |
| `showMutualFollowersCount` | `Boolean` |
| `showRecentActivityHighlight` | `Boolean` |

> **前端对接提示**（同时反映在 `complete-profile-management-frontend` change 中）：`onlineStatusVisibility` 的可选值比其他 visibility 字段少一个 `PRIVATE`，前端表单必须动态切换枚举源。

## Risks / Trade-offs

- [现有字段与新扩展表重复] → 短期保留主表字段兼容读取，新增能力以扩展表为准，后续归档时再清理冗余字段。
- [隐私裁剪遗漏导致字段泄露] → 集中在 `ContentUserProfileVO.from(...)` 做裁剪，测试覆盖每个字段 PUBLIC、FOLLOWERS_ONLY、MUTUAL_ONLY、PRIVATE。
- [审核流程增加资料更新延迟] → 明确区分低风险直接生效和高风险待审核，待审核时保留旧值公开展示。
- [缓存提升性能但增加隐私风险] → 响应前始终按最新隐私和关系过滤；隐私变更主动删除缓存。
- [历史表增长] → 每用户每类型保留 20 条，并按 `expires_at`（默认 180 天）TTL 清理。
- [EPIC-01 尚未完成导致绑定状态不可用] → 通过账号状态适配接口读取手机号/邮箱验证状态，后续切换到 EPIC-01 账号表。
- [本期缺失独立上传端点] → 头像/背景图约束（5MB、JPG/PNG/WebP）由前端 + OSS 联合保证；如果未来需要服务端二次校验，可通过新增 `POST /content/user/profile/asset/validate` 端点补齐。
- [本期缺失频率限制] → 资料更新/隐私更新无频控，需在 service 层补齐；详见 Open Questions Q1。

## Migration Plan

1. 新增 Flyway migration，创建 `content_user_profile_review`、`content_user_homepage_module`、`content_user_verification_badge`、`content_user_profile_history`。
2. 为 `content_user_profile` 补充必要审核状态或资料版本字段，保留现有昵称、头像、背景、主题色等字段。
3. 为 `content_user_privacy_setting` 补齐个人链接、认证绑定状态等需要独立控制的字段可见性。
4. 初始化已有用户的默认主页模块配置、默认隐私设置和已有认证字段的 badge 兼容数据。
5. 发布新服务逻辑，保持旧接口路径可用。
6. 开启缓存和（若补齐的）频率限制配置，观察资料更新失败率、审核队列量、隐私裁剪命中和缓存删除指标。

**Rollback strategy:**

- 应用回滚：回滚到旧版本后，旧代码继续读取 `content_user_profile` 和 `content_user_privacy_setting`，新增表不影响旧流程。
- 数据回滚：若需要撤销 migration，先导出新增审核、认证、历史、模块配置表数据，再按创建顺序反向删除新增索引、字段和表。
- 兼容字段回滚：新增到现有表的字段必须允许为空或有默认值；回滚前停止写入新字段，再执行字段删除脚本。
- Redis 回滚：删除 `content:user:profile:*`、`content:user:privacy:*` 键。
- 外部服务回滚：关闭素材处理、AI 审核、实名状态适配配置，资料更新退回仅基础字段保存。

## Open Questions

- **Q1（本期）**: 资料更新与隐私更新的频控策略未在 controller 层实现，是否需要在 service 层补齐"每日 5 次"与"每小时 10 次"限制？
- Q2: 昵称是否需要全站唯一？EPIC-02 只在恢复历史昵称时提到"已被其他用户使用"，当前按需要唯一校验处理。
- Q3: 手机号/邮箱验证标识是否默认公开，还是默认仅自己可见后由用户选择公开？
- Q4: 认证标识申请和审核后台是否归入后续 EPIC，还是本期需要最小管理端录入能力？
