## Why

内容社区已经具备关注、特别关注、关系分组、基础订阅和通知设置雏形，但还缺少关注流、关注列表管理、推荐、订阅广场、订阅流和细粒度通知频率等 EPIC-04 要求的完整闭环。
现在需要在现有 `content/user` 用户域上补齐社交关系与多源订阅能力，帮助用户建立内容来源、发现创作者，并持续接收感兴趣内容更新。

## What Changes

- 补齐关注/取消关注的产品化规则：禁止自关注、拉黑互斥、默认分组、幂等关注状态、关注数量统计和关系详情展示。
- 补齐关注分组管理：默认分组、自定义分组创建/重命名/删除、关注对象移入/移出分组、按分组筛选关注列表和关注流。
- 补齐特别关注能力：特别关注列表、取消特别关注、强提醒事件、关注流置顶优先展示和批量管理。
- 补齐关注流：展示关注对象发布、点赞、收藏动态，支持动态类型配置、分页加载、刷新空状态和特别关注优先排序。
- 补齐关注推荐与批量管理：基于用户兴趣、热门创作者和共同关注生成推荐理由；支持批量取消关注、批量移动分组并返回成功/失败明细。
- 补齐订阅源能力：支持专题/合集、话题/Tag、栏目/频道等内容源的订阅、取消、暂停、恢复和统一管理。
- 补齐订阅通知配置：站内通知、推送、邮件摘要、实时/每日频率和免打扰时段，复用现有通知设置并扩展到订阅更新。
- 补齐订阅流与订阅广场：展示订阅源新内容，按热度、分类、搜索发现订阅源，并提供订阅源详情。
- 不包含独立支付结算和付费订阅；若平台后续启用付费能力，本期仅保留状态扩展点。

## Capabilities

### New Capabilities

- `social-subscription`: 内容社区社交关注与内容订阅能力，覆盖关注关系、分组、特别关注、关注流、关注推荐、批量管理、多类型内容源订阅、通知配置、订阅流和订阅广场。

### Modified Capabilities

- 无。

## Impact

- 影响内容社区后端模块 `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user` 下的 relation、subscription、settings、notification 相关 controller、service、mapper、entity、req、vo。
- 影响现有表 `content_user_relation`、`content_user_relation_group`、`content_user_subscription`、`content_user_notification_setting`，并新增或扩展关注流配置、用户动态、关注推荐、订阅源广场、订阅通知偏好等表。
- 影响内容源读取：需要从文章、笔记、视频、问答、栏目、话题等内容域聚合发布、点赞、收藏和订阅源更新事件。
- 影响通知链路：特别关注强提醒、订阅更新站内通知、推送和邮件摘要需要接入异步通知或定时任务。
- 影响 API：补齐 `/api/v1/content/user/relation/*`、`/api/v1/content/user/subscription/*`、`/api/v1/content/user/settings/*` 相关列表、分组、feed、recommendation、discovery、notification 配置接口；后续可统一迁移到 `/api/v1` 风格。
- 依赖 EPIC-01 的注册登录和用户主体；当前实现继续兼容现有 `userId` 参数，待账号主体稳定后切换到登录态上下文。
