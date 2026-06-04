# 内容模块付费与预览（Paywall）设计文档

## 背景与目标
- 背景：在内容详情页引入付费阅读机制（支付墙）。未购买/未授权用户仅可查看部分预览内容，已授权用户可查看全文。
- 目标：
  - 清晰划分列表摘要 `summary` 与详情页预览的职责。
  - 以最小改动扩展 `contents` 表，支持付费与预览策略配置。
  - 引入访问授权记录表，支撑购买/订阅/会员等授权判断，确保并发安全。
  - 定义接口、DTO/VO、服务层流程与缓存策略，保障性能与可维护性。

## 术语约定
- `summary`：列表页摘要，用于快速浏览与搜索结果展示；与权限无关。
- 预览内容：详情页在未授权时展示的付费前内容片段；与权限强关联。
- 授权类型：购买（单篇）、订阅（专栏/栏目）、会员（站点级）等。

## 数据库设计
设计文件：`jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/resources/content_module_init.sql`

### 1. contents 表扩展字段（已落库）
- `paywall_enabled INTEGER`：是否开启付费阅读（0-否 1-是）。
- `pay_type INTEGER`：付费类型（`1-免费 2-单篇付费 3-专栏/订阅 4-会员专享`）。
- `price DECIMAL(10,2)`：单篇价格，单位元。
- `currency VARCHAR(10)`：币种，默认 `CNY`。
- `preview_strategy INTEGER`：预览策略（`1-按字数 2-按百分比 3-使用摘要 4-自定义预览`）。
- `preview_length INTEGER`：预览字数，用于策略1。
- `preview_percent INTEGER`：预览百分比，用于策略2。
- `preview_content LONGTEXT`：自定义预览内容，用于策略4。
- `preview_hint TEXT`：预览提示文案，如“购买后可阅读全文”。

### 2. 索引（已落库）
- `idx_contents_paywall_enabled(paywall_enabled)`：筛选是否付费内容。
- `idx_contents_pay_type(pay_type)`：付费类型筛选。
- `idx_contents_price(price)`：价格排序与筛选。

### 3. 内容访问授权表 `content_access_rights`（已落库）
- 作用：记录用户对内容的访问授权（购买/订阅/会员），用于详情页判断是否可阅读全文。
- 字段：
  - `content_id VARCHAR(32)`：内容ID（关联 `contents`）。
  - `user_id VARCHAR(32)`：用户ID（关联 `sys_user`）。
  - `access_type INTEGER`：授权类型（`1-免费 2-购买 3-订阅 4-会员`）。
  - `status INTEGER`：授权状态（`1-有效 2-过期 3-撤销`）。
  - `granted_time TIMESTAMP`：授权生效时间；`expired_time TIMESTAMP`：过期时间（购买通常为空）。
  - `source_order_id VARCHAR(64)`：来源订单号/账单号（幂等与审计）。
  - `version BIGINT`：乐观锁版本号，保障并发安全。
  - 审计与逻辑删除：`del_flag`、`create_by`、`create_time`、`update_by`、`update_time`。
- 约束与索引：
  - 唯一约束：`uk_user_content_access(user_id, content_id, access_type, del_flag)`。
  - 索引：`idx_access_user_content(user_id, content_id)`、`idx_access_content(content_id, status)`、`idx_access_user_type_status(user_id, access_type, status)`、`idx_access_granted_time(granted_time DESC)`。

## 业务规则
### 1. summary 与预览的关系
- `summary` 用途：列表/搜索页摘要展示，不参与详情页权限控制。
- 预览内容：仅详情页在未授权时展示的部分内容。
- 关系：当预览策略为 `3-使用摘要` 时，详情页预览直接取 `summary`；否则走字数/百分比/自定义预览。

### 2. 详情页授权判断
- 流程：
  1. 查询 `contents` 获取 `paywall_enabled`、`pay_type`、`preview_strategy` 等配置。
  2. 若 `paywall_enabled=0` 或 `pay_type=1(免费)`：直接返回全文。
  3. 若开启付费：
     - 已登录：查询 `content_access_rights` 是否存在有效授权（`status=1` 且未过期）。
     - 未登录/未授权：根据 `preview_strategy` 返回预览内容，并附带购买提示与价格信息。

### 3. 授权来源
- 购买：单篇；订单支付成功后写入授权记录。
- 订阅：栏目/专栏范围；可按内容所属栏目判断授权覆盖。
- 会员：站点级；会员有效期内对 `pay_type=4` 内容视为授权。

## 接口设计（REST）
统一前缀：`/api/v1/content`

- `GET /{id}/detail`：内容详情（自动根据授权返回全文或预览）。
  - 入参：`id`, `currentUserId`（Header/Token）。
  - 出参：`isAuthorized`，`isPaywalled`，`contentFull | contentPreview`，`payInfo{price, payType, hint}`。

- `GET /{id}/access`：查询当前用户对内容的授权状态。
  - 入参：`id`。
  - 出参：`hasAccess`，`accessType`，`expiredTime`。

- `POST /{id}/purchase`：创建购买订单（对接支付模块）。
  - 入参：`id`，`payChannel`，`clientInfo`。
  - 出参：`orderId`，`payUrl` 或支付参数。

说明：实际购买流程由交易/订单模块负责；支付回调成功后写入 `content_access_rights` 并失效缓存。

## VO/DTO 设计
- 请求：
  - `ContentDetailQueryVO`：`contentId`，`userId`（从鉴权上下文获取）。
- 响应：
  - `ContentDetailResultVO`：`isAuthorized`，`isPaywalled`，`contentFull | contentPreview`，`payInfo{price, payType, hint}`，`meta{viewCount, likeCount}`。
- DTO：
  - `ContentDetailDto`：服务层内部传输对象，承载业务拼装数据。

## Service 流程与示例（伪代码）
```java
/**
 * 获取内容详情（按授权返回预览或全文）
 * 职责：统一封装付费判断、授权校验与预览内容生成逻辑
 * 并发安全：授权依赖 content_access_rights 的唯一约束与乐观锁
 * 参数：
 *  - contentId: 内容ID
 *  - currentUserId: 当前用户ID（未登录可为空）
 * 返回：
 *  - isAuthorized: 是否已授权
 *  - isPaywalled: 是否开启付费
 *  - contentPreview / contentFull: 根据授权状态返回
 *  - payInfo: 价格、类型、提示文案
 */
public ContentDetailDto getContentDetail(String contentId, @Nullable String currentUserId) {
    // 1. 查询内容基础信息与付费配置
    Content content = contentRepo.findById(contentId);
    // 2. 未付费或免费内容直接返回全文
    if (content.getPaywallEnabled() == 0 || content.getPayType() == 1) {
        return buildFullResponse(content);
    }
    // 3. 判断授权（已登录才可校验，未登录则视为未授权）
    boolean authorized = false;
    if (StringUtils.isNotBlank(currentUserId)) {
        authorized = accessRepo.hasValidAccess(currentUserId, contentId);
    }
    // 4. 授权用户返回全文；未授权返回预览
    if (authorized) {
        return buildFullResponse(content);
    }
    // 5. 生成预览内容（按策略生成）
    String preview = buildPreview(content);
    return buildPreviewResponse(content, preview);
}
```

## 并发与事务
- 授权写入：订单回调处理使用事务，写入 `content_access_rights` 时携带幂等键 `source_order_id`。
- 唯一约束 + 乐观锁：`uk_user_content_access` 防重复；`version` 字段防并发覆盖。
- 读取授权：只读查询结合索引，避免锁表；需要强一致时走事务读取并加行锁（谨慎使用）。

## 缓存策略
- Key：`access:{userId}:{contentId}`，TTL 5–15 分钟（视支付回调时延确定）。
- 失效：订单支付成功或授权状态变更后，主动删除缓存或发布失效消息。
- 防穿透：未登录或未授权场景缓存短 TTL 的负结果，降低 DB 压力。

## 安全与权限
- 鉴权：接口需携带有效 Token；未登录仅允许访问预览。
- 授权类型匹配：`pay_type=4(会员专享)` 需校验站点会员有效期；`pay_type=3(订阅)` 需校验用户订阅所覆盖栏目。
- 数据安全：避免在响应中返回敏感内部字段（如订单内部标识）。

## 性能与基准（计划与目标）
- 详情授权判定（缓存命中率≥80%）：TP95 < 5ms，TP99 < 10ms。
- 纯 DB 授权判定（未命中）：TP95 < 15ms，TP99 < 30ms。
- 压测方案：JMeter/wrk/Gatling 预生产环境压测；指标采集包括 QPS、TP99、错误率、DB 连接池使用率。
- 索引与热点：`idx_access_user_content`、`idx_access_user_type_status` 保证授权判定为 B+Tree 近似 O(logN)。

## 埋点与日志
- 关键事件：详情页授权判定、购买发起、支付成功回调、授权写入成功/失败。
- 日志内容：用户ID、内容ID、授权类型、耗时、异常堆栈；敏感信息脱敏。

## 配置化
- 预览策略与默认参数可通过配置中心下发（如 `preview_length` 默认值、`preview_hint` 通用文案）。
- 币种与价格精度为可配置项，默认 `CNY` 与两位小数。

## 待确认问题清单（请与我讨论）
- 订阅范围：是否已有“栏目/专栏”模型？`contents` 的订阅覆盖如何判定（按 `channel_id`、社区或话题）？
- 会员体系：站点会员是否已有独立模块与有效期管理？授权判定是否统一走会员服务？
- 价格体系：是否需要支持促销价/活动价与生效时段？是否需要价格生效版本与审计？
- 预览来源：预览截取基于 `plain_content` 还是 `rendered_content`？不同内容类型（视频/图片）是否有专用预览策略？
- 退款与撤销：订单退款后授权是否需要撤销？撤销策略与审计字段是否需要扩展？

## 迁移与回滚
- 初次上线：为 `paywall_enabled` 默认 0，避免影响既有内容。
- 回滚策略：移除新增字段不建议；如需回滚，停用功能入口并忽略授权判断。

## 验收标准
- 未授权访问详情页返回预览与购买提示；已授权返回全文。
- 授权写入并发下无重复与覆盖错误；缓存命中与失效生效。
- 重点接口压测达到性能目标；关键日志完整可追踪。