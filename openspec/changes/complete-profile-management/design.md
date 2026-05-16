## Context

内容社区已有 `org.jeecg.modules.content.user` 用户域，包含 `ContentUserProfileController`、`IContentUserProfileService`、`ContentUserProfileServiceImpl`、`content_user_profile`、`content_user_privacy_setting` 和关系可见性策略。现状已经能保存基础资料、主页背景、主题色、模块 JSON、认证字段和部分隐私设置，但资料更新缺少频率限制、审核流、素材校验、全字段隐私裁剪、缓存失效、独立认证标识和可恢复历史记录。

EPIC-02 依赖 EPIC-01 的注册登录、账号绑定和认证状态。由于 EPIC-01 仍是 OpenSpec 进行中变更，本设计保持与现有 `userId` 参数和 `SystemUserAccountGateway` 兼容，后续再把登录态和手机号/邮箱绑定状态接入 EPIC-01 的账号主体。

## Goals / Non-Goals

**Goals:**

- 在现有 `content/user` 用户域内补齐资料管理、主页个性化、认证标识、隐私控制、缓存失效和历史记录能力。
- 保留现有 `content_user_profile` 作为当前有效资料主表，避免创建平行资料体系。
- 将审核、历史、认证标识、主页模块配置从主表中过重的 JSON/展示字段中拆出，便于约束、查询和清理。
- 对资料展示接口统一执行字段可见性裁剪，确保隐私设置对新请求即时生效。
- 使用 Redis 处理频率限制和缓存，数据库保存审核、认证、历史等长期事实。
- 使用 Flyway 管理新增表和字段，并提供回滚方案。

**Non-Goals:**

- 不实现独立支付结算和企业组织通讯录。
- 不实现完整实名认证供应商接入和认证材料审批后台，只提供认证标识记录、状态展示与详情查询的后端承载。
- 不重构内容社区关注关系、粉丝统计、勋章体系和 EPIC-01 认证实现。
- 不一次性迁移所有 `/content/user/*` 接口到 `/api/v1/*`，仅在设计中保持后续兼容方向。

## Decisions

### 1. 保留主表，新增专用扩展表

`content_user_profile` 继续作为当前有效资料主表，保存展示时最常用的字段。新增表承载独立生命周期：

- `content_user_profile_review`: 待审核资料快照、审核状态、审核原因、原始值和目标值。
- `content_user_homepage_module`: 用户主页模块配置，替代主表中难以约束的模块排序 JSON。
- `content_user_verification_badge`: 个人、企业、达人、官方、实名、手机号/邮箱验证等认证标识记录。
- `content_user_profile_history`: 曾用昵称、头像历史记录，支持 180 天保留、每类最多 20 条和恢复。

理由：主表继续满足高频读取，扩展表满足审核、历史、认证等不同生命周期，避免把全部规则塞进 `ContentUserProfileServiceImpl` 的直接字段覆盖。

替代方案：继续使用 `nickname_history_json`、`avatar_history_json` 和 `certification_type` 字段。放弃原因是 JSON 难以做上限、过期、恢复、审核和并发控制。

### 2. 资料更新走编排服务，字段写入仍落在 profile service

新增或扩展资料编排服务，负责：

- 校验必填字段、长度、生日、链接、性别、图片元数据。
- 调用敏感词和 AI 审核适配接口。
- 判断每日 5 次资料修改限制。
- 创建审核记录或直接更新 profile。
- 记录昵称/头像历史。
- 清理资料缓存。

`ContentUserProfileServiceImpl` 可保留对外接口，但内部不再简单全字段覆盖，而是委托编排逻辑。理由是资料更新现在跨 profile、review、history、cache、多媒体适配，属于跨聚合编排。

替代方案：在 controller 中直接加校验和审核。放弃原因是 controller 会承担业务拼装，不符合模块规则。

### 3. 素材处理通过适配接口接入

定义头像和背景图素材适配接口，输入文件元数据或上传结果，输出 CDN URL、资源格式、大小、宽高和多分辨率资源信息。业务层只关心是否满足 JPG/PNG/WebP、5MB、最大分辨率和处理状态。

理由：真实上传/CDN/压缩实现易变，适配接口便于单元测试和替换供应商。

替代方案：直接在资料服务里处理文件上传。放弃原因是与业务规则耦合，测试成本高。

### 4. 认证标识独立于 profile 字段

认证标识以 `content_user_verification_badge` 为准，profile 响应通过聚合查询返回可展示 badge。手机号/邮箱验证状态优先从 EPIC-01 账号层读取，在账号层未完成前使用适配接口兜底。

理由：认证有类型、状态、时间、描述、过期、展示规则，不应只是 profile 的两个字符串字段。

替代方案：继续使用 `certification_type` 和 `certification_label`。放弃原因是无法表达多认证、多状态、实名和绑定标识可见性。

### 5. 隐私裁剪在 VO 构建前统一执行

资料详情查询加载 profile、privacy、relation 后，由可见性策略对生日、性别、地区、职业、个人链接、认证绑定状态、主页模块等字段统一裁剪。`ContentUserProfileVO.from` 不再只接收 `birthdayVisible`，而是接收裁剪后的资料视图或可见性上下文。

理由：当前只隐藏生日，无法满足 EPIC-02 的多字段隐私承诺。把裁剪前置可以避免 VO 转换遗漏。

替代方案：前端按隐私字段隐藏。放弃原因是后端仍会泄露数据。

### 6. 缓存采用 Cache-Aside，但响应前仍做实时可见性判定

Redis 缓存只缓存可复用的资料基础数据或 owner 视角数据，最终响应必须按 viewer 当前关系和最新隐私设置过滤。隐私设置变更时删除用户资料公共缓存，并设置相关页面缓存最长 5 分钟失效。

建议 Redis key：

- `content:user:profile:{userId}`: 当前资料基础数据。
- `content:user:privacy:{userId}`: 当前隐私设置。
- `content:user:profile:update_count:{userId}:{yyyyMMdd}`: 每日资料修改次数。
- `content:user:privacy:update_count:{userId}:{yyyyMMddHH}`: 每小时隐私修改次数。
- `content:user:profile:public:{userId}`: 可选公共资料缓存，隐私变更时必须删除。

理由：隐私正确性优先于缓存命中率，缓存不能成为泄漏旧字段的来源。

### 7. 历史恢复复用资料更新流程

恢复曾用昵称或头像不直接写 profile，而是转化为一次新的资料修改请求，重新执行频率限制、唯一性校验、素材校验和审核规则。恢复成功后同样记录当前值进入历史。

理由：恢复也是对当前身份的改变，必须与普通修改遵守同样约束。

## Risks / Trade-offs

- [现有字段与新扩展表重复] → 短期保留主表字段兼容读取，新增能力以扩展表为准，后续归档时再清理冗余字段。
- [隐私裁剪遗漏导致字段泄露] → 集中在 profile query service 做裁剪，测试覆盖每个字段 PUBLIC、FOLLOWERS_ONLY、MUTUAL_ONLY、PRIVATE。
- [审核流程增加资料更新延迟] → 明确区分低风险直接生效和高风险待审核，待审核时保留旧值公开展示。
- [缓存提升性能但增加隐私风险] → 响应前始终按最新隐私和关系过滤；隐私变更主动删除缓存。
- [历史表增长] → 每用户每类型保留 20 条，并每日清理 180 天前数据。
- [EPIC-01 尚未完成导致绑定状态不可用] → 通过账号状态适配接口读取手机号/邮箱验证状态，后续切换到 EPIC-01 账号表。

## Migration Plan

1. 新增 Flyway migration，创建 `content_user_profile_review`、`content_user_homepage_module`、`content_user_verification_badge`、`content_user_profile_history`。
2. 为 `content_user_profile` 补充必要审核状态或资料版本字段，保留现有昵称、头像、背景、主题色等字段。
3. 为 `content_user_privacy_setting` 补齐个人链接、认证绑定状态等需要独立控制的字段可见性。
4. 初始化已有用户的默认主页模块配置、默认隐私设置和已有认证字段的 badge 兼容数据。
5. 发布新服务逻辑，保持旧接口路径可用。
6. 开启缓存和频率限制配置，观察资料更新失败率、审核队列量、隐私裁剪命中和缓存删除指标。

**Rollback strategy:**

- 应用回滚：回滚到旧版本后，旧代码继续读取 `content_user_profile` 和 `content_user_privacy_setting`，新增表不影响旧流程。
- 数据回滚：若需要撤销 migration，先导出新增审核、认证、历史、模块配置表数据，再按创建顺序反向删除新增索引、字段和表。
- 兼容字段回滚：新增到现有表的字段必须允许为空或有默认值；回滚前停止写入新字段，再执行字段删除脚本。
- Redis 回滚：删除 `content:user:profile:*`、`content:user:privacy:*`、`content:user:profile:update_count:*`、`content:user:privacy:update_count:*` 键。
- 外部服务回滚：关闭素材处理、AI 审核、实名状态适配配置，资料更新退回仅基础字段保存。

## Open Questions

- 昵称是否需要全站唯一？EPIC-02 只在恢复历史昵称时提到“已被其他用户使用”，设计暂按需要唯一校验处理。
- 头像和背景图上传接口由内容社区提供，还是复用 JeecgBoot 现有文件上传能力后仅保存 URL？
- 手机号/邮箱验证标识是否默认公开，还是默认仅自己可见后由用户选择公开？
- 认证标识申请和审核后台是否归入后续 EPIC，还是本期需要最小管理端录入能力？
