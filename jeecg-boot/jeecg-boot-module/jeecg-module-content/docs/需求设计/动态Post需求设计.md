# 动态（Post）需求设计文档

> 参考源：
> - `/jeecg-boot/jeecg-boot-module/jeecg-module-content/docs/需求设计/帖子系统需求设计.md`
> - `/原始需求/内容社区系统需求文档.md`
> - `/ .lingma/需求设计大师提示词.md`（结构化与可测试输出规范）

## 1. 概述与目标

- 范围：仅针对“动态（Post）”内容类型的需求设计与接口契约，覆盖发布、存储、浏览、互动、管理、审核与安全合规。
- 架构：遵循 JeecgBoot 分层架构（Controller → Service → Mapper），严格 VO/DTO/Entity 分离；统一异常、响应、权限、缓存与事务管理。
- 目标（SMART）：
  - 发布动态 P95 < 150ms（不含 OSS 上传）；成功率 ≥ 99.9%。
  - 最新信息流（仅 Post 类型）分页 20 条，查询 P95 < 120ms；错误率 < 0.1%。
  - 点赞/收藏/评论写入 P95 < 80ms；幂等成功率 100%。
  - 缓存命中率 ≥ 85%；热点逻辑过期重建延迟 < 3s。
  - 安全与合规：XSS 过滤覆盖率 100%；速率限制生效且可观测。
  - 输入与存储约束：仅支持 TipTap JSON 作为输入内容格式；不支持 Markdown/HTML 作为输入；系统可生成 HTML 缓存（用于展示）与纯文本摘要（用于搜索）。

## 2. 角色与权限（RBAC）

- 普通用户：发布/编辑/删除自己的 Post；点赞、收藏、评论、分享；@提及；举报；拉黑；关注。
- 频道管理员：管理频道范围内 Post；置顶/设精华/移除不符合主题内容。
- 系统管理员/审核员：内容审核、屏蔽、封禁；处理举报与风控。
- 权限控制：
  - URL 级与方法级鉴权，统一基于 Spring Security/Shiro；JWT 无状态认证。
  - 资源级能力校验：仅作者可编辑/删除；频道操作需频道管理员角色。

## 3. 用户故事与验收标准

### 3.1 发布动态（文字在上，媒体在下；支持纯文本与图片/视频九宫格混排）

- 用户故事：作为注册用户，我希望发布一条动态，支持纯文本或图文/视频混排，媒体展示在正文下方并支持九宫格布局。
- 验收标准：
  - 仅支持 TipTap JSON 格式输入；支持添加图片/视频附件（最多 9 图九宫格，视频最多 1 条）。
  - 媒体统一走 OSS/CDN；数据库仅存 URL 与元数据（类型、大小、分辨率、时长等）。
  - 支持标签/话题、可见性设置（公开、仅自己、好友可见）、位置信息、@提及、Emoji。
  - 发布前支持预览；发布后生成 HTML 缓存（可选）与纯文本摘要。
  - 发布写入事务化：主内容与媒体关联一致；失败回滚；幂等性保证（客户端重复提交不产生多条）。

### 3.2 浏览动态列表（最新）

- 用户故事：作为用户，我希望按时间倒序浏览最新的动态列表，可分页与筛选类型，仅展示 Post 类型内容。
- 验收标准：
  - 列表分页、时间倒序；支持刷新与自动刷新；支持内容摘要与互动数据（点赞、评论、收藏）。
  - 支持类型筛选（仅 Post）；支持频道筛选与标签筛选。
  - 列表缓存 30–120s 可配置；缓存失效策略统一；热点逻辑过期与后台重建。

### 3.3 互动功能（点赞/收藏/评论/分享）

- 用户故事：作为用户，我希望能够对动态点赞、收藏、评论并分享至外部平台。
- 验收标准：
  - 点赞与收藏为幂等操作；同一用户对同一内容的唯一性约束；事务写入成功后更新计数（Redis 原子计数 + 异步落库）。
  - 评论支持楼层与回复、@提及、匿名、仅楼主可见；发布后自动关注该动态；保留编辑历史版本。
  - 互动写入 P95 < 80ms；接口有速率限制与告警；通知事件触达（点赞、评论、@）。

### 3.4 草稿与定时发布

- 用户故事：作为用户，我希望将编辑中的动态保存为草稿，或设置定时发布确保唯一触发与并发安全。
- 验收标准：
  - 草稿自动/手动保存；支持继续编辑并发布；编辑器状态可恢复（返回原始 TipTap JSON/纯文本）。
  - 定时发布使用分布式任务（如 XXL-JOB）；唯一触发；幂等控制（唯一键 + 分布式锁）。
  - 草稿与发布均仅支持 TipTap JSON 内容格式。

### 3.5 频道发布与管理

- 用户故事：作为内容创作者，我希望将 Post 发布到一个或多个频道，并由频道管理员管理频道内容。
- 验收标准：
  - 发布时可选择频道（受频道规则限制）；支持多频道发布。
  - 频道管理员可置顶、设精华、移除不符合主题内容。

### 3.6 转发动态

- 用户故事：作为用户，我希望转发任意类型的内容，生成 Post 类型的转发内容并附加评论，保留原始引用关系。
- 验收标准：
  - 转发支持附加评论；保留原始内容引用 ID 与类型；显示转发标识。

## 4. 模块与分层架构

- 包结构：`org.jeecg.modules.content` 下新增/扩展 Post 相关模块，遵循：
  - `controller`：仅接收 VO(req)，返回 VO(resp)。
  - `service`：编排业务流程、权限与并发控制；DTO 与 Entity 转换在此层完成。
  - `mapper`：MyBatis-Plus 持久层操作；索引与并发唯一约束由数据库保证。
  - `entity` / `dto` / `vo`：严格分离；禁止 Controller 直接使用 Entity；禁止 BeanUtils.copyProperties 进行转换。
- 统一：异常处理（`@ControllerAdvice`）、统一响应（`Result<T>`）、统一日志、统一事务、统一缓存策略、统一权限控制。

## 5. 数据模型设计（Entity/DTO/VO 分离）

### 5.1 实体（示意字段）

- `content`（Post 主内容）
  - `id`(String)、`content_type`(POST)、`title`(可空)、`source_type`(TIPTAP/TEXT)、`source_payload`(JSON/Text)、
    `html_cache`(Text, 可空)、`text_cache`(Text)、`cover_url`(可空)、`visibility`(PUBLIC/PRIVATE/FRIENDS_ONLY)、
    `status`(DRAFT/PUBLISHED/REVIEW_PENDING/BLOCKED/DELETED)、`author_id`、`channel_ids`(JSON)、
    `location`(lng/lat/address JSON，可空)、`mention_user_ids`(JSON)、`created_at`、`updated_at`、`deleted_flag`。

- `content_media`
  - `id`、`content_id`、`media_type`(IMAGE/VIDEO)、`url`、`meta`(JSON，如时长、分辨率)、`order_index`、`created_at`。

- `content_tag_rel`
  - `id`、`content_id`、`tag_id`、`created_at`。

- `interaction_like` / `favorite` / `comment`
  - 互动表按类型拆分；唯一约束（`user_id + content_id + type`）；评论支持层级与@提及。

### 5.2 索引与并发约束

- 索引：`content.created_at`、`content.author_id`、`content.status`、`content_type`；`media.content_id`；`tag_rel.content_id`；互动表的 `user_id + content_id` 组合索引。
- 并发唯一：
  - 点赞/收藏：`UNIQUE (user_id, content_id, type)`；插入冲突即视为幂等成功。
  - 定时发布：基于内容 ID 唯一任务键，分布式锁保证唯一触发。

### 5.3 DTO 与 VO（示意）

- DTO：`ContentBaseDTO`、`ContentCreateDTO`、`ContentUpdateDTO`、`ContentQueryDTO`、`ContentResultDTO`（包含 `toEntity/fromRequest/toResponse`）。
- VO(req)：`ContentCreateVO`、`ContentUpdateVO`、`ContentQueryVO`（参数优先用枚举）。
- VO(resp)：`ContentListVO`、`ContentDetailVO`、`ContentResultVO`。
- 约束：禁止在 Controller 使用 Entity；VO 中允许包含 DTO 字段；避免在 Service 层暴露 Request/Response 对象。

## 6. API 设计（RESTful）

- 统一前缀：`/api/v1/content`；资源使用小写与连字符；版本同时支持请求头 `API-Version: v1`。

- 发布 Post：`POST /api/v1/content/posts`
  - 请求：`ContentCreateVO`
  - 响应：`Result<ContentResultVO>`

- 编辑 Post：`PUT /api/v1/content/posts/{id}`
  - 请求：`ContentUpdateVO`
  - 响应：`Result<ContentResultVO>`

- 删除 Post：`DELETE /api/v1/content/posts/{id}`
  - 响应：`Result<Void>`

- 获取 Post 详情：`GET /api/v1/content/posts/{id}`
  - 响应：`Result<ContentDetailVO>`

- 最新 Post 列表：`GET /api/v1/content/feeds/latest`
  - 查询参数：`ContentQueryVO`（类型字段固定为 POST）
  - 响应：`Result<Page<ContentListVO>>`

- 点赞：`POST /api/v1/content/interactions/{id}/like`
- 收藏：`POST /api/v1/content/interactions/{id}/favorite`
- 评论：`POST /api/v1/content/comments`
- 转发：`POST /api/v1/content/posts/{id}/repost`

## 7. 编辑器与存储策略

- 源内容：仅存储 TipTap JSON；不支持 Markdown/HTML 作为输入内容格式。
- 渲染：可选缓存 HTML 版本用于展示；统一净化（XSS 防护）。
- 纯文本：生成摘要用于搜索与快速预览。
- 媒体：统一上传 OSS/CDN；仅存 URL 与元数据；最多 9 图九宫格、视频最多 1 条。
- 编辑历史：记录版本与差异；允许回滚。

## 8. 并发与事务

- 发布写入：主内容 + 媒体 + 标签关系同事务；失败回滚；客户端去重 Token 保证幂等。
- 点赞/收藏：唯一约束 + 事务；冲突视为幂等成功；计数采用 Redis 原子自增，消息队列异步落库与对账任务。
- 评论发布：事务写入评论与通知事件；编辑历史独立记录。
- 定时发布：分布式锁与唯一任务键；失败重试与观测。

## 9. 缓存策略

- 键前缀：`content:`；如 `content:detail:{id}`、`content:feed:latest:post:{page}`、`content:count:{id}`。
- 过期：详情 60–300s；列表 30–120s；热门/精选 5–15m；统一可配置。
- 失效：内容更新/删除精确失效；计数写入后延时双删；热点使用逻辑过期 + 后台重建，保证可用性。

## 10. 性能目标与 Benchmark

- 指标：见第 1 节目标；每次迭代输出压测与 JMH 基准数据。
- 基准方法：
  - 离线（JMH）：
    - TipTap JSON → HTML 渲染净化性能。
    - DTO/VO/Entity 转换开销。
    - 计数聚合与缓存命中评估。
  - 在线（Gatling/JMeter）：
    - 发布、编辑、列表查询、互动写入在 500–2000 req/s 并发下的 p95/p99、错误率、资源占用。
  - 数据规模：模拟 1e6 Post、1e7 互动；冷热分布 80/20。

## 11. 日志与监控

- 日志：统一格式；发布、编辑、删除、互动、审核、举报关键打点；包含时间戳、类名、方法名、用户与内容 ID；异常日志包含堆栈。
- 监控：接口耗时、错误率、DB 慢 SQL、缓存命中率、队列堆积、任务执行；告警与自愈策略。

## 12. 安全与合规

- 输入安全：统一 XSS 净化；SQL 注入防护；文件类型白名单与大小限制；外链安全校验。
- 鉴权与限流：JWT 无状态；接口级速率限制（Sentinel/网关）。
- 审核：敏感词与违规检测（可接入外部服务）；人工审核流程与记录；屏蔽策略。

## 13. 国际化（i18n）

- Spring `MessageSource`；消息文件按语言分类：`messages.properties`、`messages_zh_CN.properties`、`messages_en_US.properties`；支持动态切换语言。

## 14. 状态机与枚举

- 状态：`DRAFT`、`PUBLISHED`、`REVIEW_PENDING`、`BLOCKED`、`DELETED`。
- 可见性：`PUBLIC`、`PRIVATE`、`FRIENDS_ONLY`。
- 内容类型：`POST`（此文档仅 Post）。
- 互动类型：`LIKE`、`FAVORITE`、`COMMENT`（回答场景另有 `DOWNVOTE`，Post 不涉及）。

## 15. 验收标准（汇总）

- 发布与编辑：Post 支持基于 TipTap JSON 的图文/视频混排（不支持 Markdown/HTML 作为输入）；媒体 OSS；@、标签、位置、Emoji；预览与版本历史；事务一致。
- 浏览：最新列表（仅 Post）支持筛选、排序、分页、刷新；展示摘要与互动数据；缓存与离线能力。
- 互动：点赞、收藏、评论（回复、@、匿名、仅楼主可见、历史版本）；通知触达。
- 草稿与定时：草稿保存与恢复；定时发布并发安全与幂等唯一触发。
- 审核与版本：审核记录、屏蔽策略；版本回溯。
- 并发与缓存：唯一约束与事务；Redis 计数 + 异步落库；统一缓存键与失效策略；性能指标达标并有基准数据。
- 权限与安全：JWT 鉴权；速率限制；XSS 与注入防护；风控与合规。

## 16. 需求追踪与优先级（MoSCoW）

- Must：发布/编辑/删除 Post；最新列表；点赞/收藏/评论；草稿；频道发布；@、标签、位置；媒体 OSS；缓存与并发安全。
- Should：转发；精选与热门列表（Post 维度）；通知与拉黑；审核与版本。
- Could：离线浏览；夜间模式；推荐解释与反馈。
- Won’t（MVP 外）：复杂推荐策略的深度学习融合；跨内容类型的高级聚合可视化。

## 17. 风险与边界

- 富媒体渲染复杂度与性能；需分层渲染与缓存。
- 大规模计数一致性；需离线归档与在线近实时折中。
- 定时发布在分布式环境的一致性；需全链路幂等与任务观测。
- OSS 依赖与失败恢复；需重试与降级策略。

## 18. 版本与变更记录

- v1.0：首版 Post 动态需求设计（按上述内容交付）。

---

附：开发实现约束（与 JeecgBoot 规则对齐）

- Controller 层仅接收 VO(req)/返回 VO(resp)，统一响应 `Result<T>`；方法级权限注解，严格校验作者或角色。
- Service 层：DTO ↔ Entity 转换；事务编排与并发控制；缓存与计数一致性；避免直接使用 Request/Response。
- Mapper 层：MyBatis-Plus；索引优化与唯一约束；避免 N+1 查询；分页与筛选合理使用。
- 工程：统一日志、异常、权限、缓存、事务；遵守命名与包结构规范；单元测试覆盖率 ≥ 80%。