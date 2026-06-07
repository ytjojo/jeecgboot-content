## Context

内容社区已有 `content_user_profile` 中的 `point_balance`、`growth_value`、`level` 汇总字段，也已有 `content_user_point_ledger`、`content_user_growth_ledger`、`content_user_badge_definition`、`content_user_badge_grant`、`ContentUserGrowthServiceImpl` 和等级权益/处罚相关服务。现状能记录简单积分和成长变更，但奖励规则、每日上限、幂等、防刷、兑换、勋章进度、佩戴展示、过期回收、等级阈值配置和经验衰减仍不完整。

EPIC-03 依赖 EPIC-01 的用户注册登录，也会在 EPIC-02 的主页、帖子卡片和评论区域展示勋章与等级。因为 EPIC-01/02 仍是独立 OpenSpec 变更，本设计以现有 `userId`、profile 汇总字段和内容社区用户域为兼容边界，后续通过事件和展示聚合对接其他史诗。

## Goals / Non-Goals

**Goals:**

- 在现有用户域上补齐勋章、积分和成长等级闭环，而不是新建平行用户成长体系。
- 用规则配置驱动积分、成长值、勋章和等级权益，减少硬编码。
- 保证奖励事件幂等、每日上限准确、积分消费原子、台账可追溯。
- 支持勋章分类、进度、佩戴、展示、过期和违规回收。
- 支持积分兑换、功能解锁、虚拟礼物、积分明细查询。
- 支持成长值驱动等级、等级权益、推荐加权输出、经验衰减和降级保护。
- 使用 Flyway 管理新增表和字段，并提供回滚方案。

**Non-Goals:**

- 不实现独立支付结算和创作者收益提现。
- 不实现完整商城履约、物流、库存采购和财务结算。
- 不重构推荐算法主体，只输出受限的等级加权信号。
- 不建设完整任务中心 UI，仅提供任务完成事件与奖励发放承载。

## Decisions

### 1. 采用行为事件驱动的奖励入口

新增统一奖励编排服务，接收登录、浏览、点赞、分享、评论、发布、推荐、加精、转发、关注、邀请注册、任务完成等事件。编排流程：

```text
行为事件
  -> 幂等检查
  -> 规则匹配
  -> Redis 每日上限检查
  -> 积分/成长台账
  -> profile 汇总更新
  -> 勋章进度刷新
  -> 通知/审计
```

理由：现有 `recordBehavior(userId, sourceType, pointDelta, growthDelta)` 太直接，调用方容易绕过上限和幂等规则。统一入口能保护积分与成长体系的一致性。

替代方案：各业务模块直接传 pointDelta/growthDelta。放弃原因是规则散落，难以防刷和追溯。

### 2. 规则配置与台账事实分离

新增或扩展表：

- `content_user_reward_rule`: 行为 sourceType 对应积分、成长值、每日上限、是否启用、规则说明。
- `content_user_reward_event`: 已处理事件幂等表，记录 eventId、sourceType、userId、处理结果。
- `content_user_level_config`: 等级阈值、等级名称、展示样式。
- `content_user_level_benefit_config`: 等级权益配置，如上传大小、视频清晰度、话题额度、客服优先级、推荐加权。
- `content_user_exchange_goods`: 可兑换权益、功能解锁、虚拟礼物配置。
- `content_user_exchange_order`: 兑换订单和权益发放结果。
- `content_user_feature_unlock`: 用户功能解锁状态。
- `content_user_virtual_gift_record`: 虚拟礼物赠送记录。
- `content_user_growth_decay_state`: 经验衰减和降级保护状态。

保留现有 point/growth ledger 和 profile 汇总字段作为事实与读模型。理由是配置可调整，台账不可随意改写。

### 3. Redis 只做短期计数和幂等加速，数据库保存最终事实

Redis key 建议：

- `content:growth:daily_cap:{sourceType}:{userId}:{yyyyMMdd}`: 行为每日奖励上限计数。
- `content:growth:event_lock:{eventId}`: 短期事件处理锁。
- `content:growth:badge_progress:{userId}:{badgeCode}`: 勋章进度缓存。
- `content:growth:benefit:{userId}`: 等级权益摘要缓存。

数据库的 `content_user_reward_event` 负责长期幂等，ledger 负责对账。理由是 Redis 可丢失，不能作为积分余额和奖励事实来源。

### 4. 积分消费使用事务内余额校验和台账写入

兑换、功能解锁、赠礼必须在同一事务内完成：

```text
锁定用户 profile 或余额行
  -> 校验余额/库存/配置
  -> 扣减 point_balance
  -> 写负向 point ledger
  -> 创建订单/解锁/礼物记录
  -> 发通知
```

理由：积分是消耗型资产，必须避免余额透支和权益已发但扣款失败。

替代方案：先扣积分再异步发权益。放弃原因是失败补偿复杂且影响用户信任。

### 5. 勋章授予和佩戴基于 grant 表扩展

继续使用 `content_user_badge_definition` 与 `content_user_badge_grant`，补齐字段或扩展表支持分类、图标、特效、进度规则、排序、佩戴顺序、回收原因、操作人。最多佩戴 5 个通过服务层校验，过期任务将 grant 标记为 `EXPIRED` 并取消展示。

理由：现有表已表达“定义”和“授予”，增量扩展比重建更稳。

### 6. 等级计算从硬编码改为配置阈值

`ContentUserGrowthServiceImpl.calculateLevel` 当前按 `growth / 100 + 1` 计算，后续改为读取 `content_user_level_config`。等级权益也从固定高等级判断改为 `content_user_level_benefit_config`。

理由：EPIC-03 要求等级阈值、特权和加权可配置，硬编码无法满足运营调整。

### 7. 衰减状态独立记录，降级保护显式化

衰减任务根据最后活跃时间筛选连续 30 天未登录用户，写负向 growth ledger，进入或更新 `content_user_growth_decay_state`。如果低于当前等级阈值，先进入 7 天保护期；保护期内活跃并恢复阈值则清除状态，否则保护期结束后降级并通知。

理由：衰减与降级是敏感体验，必须可解释、可审计、可恢复。

## Risks / Trade-offs

- [刷积分或刷经验] → 奖励事件必须带 eventId，使用 Redis 计数和数据库幂等表双重保护，异常事件写审计。
- [Redis 丢失导致上限失效] → 数据库 reward_event 和 ledger 可回查；关键高价值事件使用数据库幂等为准。
- [积分消费并发透支] → 事务内锁定 profile 余额并校验，消费失败整体回滚。
- [规则调整影响历史对账] → 台账记录当时 sourceType、delta、rule snapshot 或 remark，不回写历史 ledger。
- [等级衰减引发用户负反馈] → 规则说明可查询，降级前 7 天保护，活跃后停止衰减。
- [推荐加权破坏公平] → 只输出小幅配置化权重，并设置最大值，推荐系统仍必须结合内容质量评分。

## Migration Plan

1. 新增 Flyway migration，创建奖励规则、奖励事件、等级配置、等级权益配置、兑换商品、兑换订单、功能解锁、礼物记录、衰减状态表。
2. 扩展 `content_user_badge_definition` 和 `content_user_badge_grant` 的展示、进度、回收、佩戴排序相关字段。
3. 为 `content_user_point_ledger`、`content_user_growth_ledger` 补齐 source description、event id、rule snapshot 等可选审计字段。
4. 初始化默认积分规则、成长规则、等级阈值、等级权益和基础勋章定义。
5. 发布统一奖励事件入口，保留旧 `/api/v1/content/user/growth/record` 作为兼容入口但内部委托新编排。
6. 开启 Redis 上限计数和幂等锁，观察奖励发放量、上限命中、重复事件、消费失败率。
7. 最后开启经验衰减定时任务，先 dry-run 记录待衰减用户，再启用真实扣减。

**Rollback strategy:**

- 应用回滚：旧版本继续使用 profile 汇总、point ledger、growth ledger、badge definition/grant，新增表不影响旧流程。
- 数据回滚：先导出新增规则、订单、解锁、礼物和衰减状态表，再按创建顺序反向删除新增索引、字段和表。
- 兼容字段回滚：新增到现有表的字段保持 nullable 或默认值；回滚前停止新逻辑写入，再删除字段。
- Redis 回滚：删除 `content:growth:*` 相关 key。
- 规则回滚：将奖励规则和衰减任务置为 disabled，保留已有 ledger 作为历史事实，不做批量反向冲正，除非单独审批补偿方案。

## Open Questions

- 积分兑换商品是否需要真实库存锁定，还是本期只处理虚拟权益库存？
- 等级推荐加权由当前模块提供权重接口即可，还是需要同步写入内容推荐特征表？
- 勋章动态特效资源由运营上传配置，还是固定枚举样式 key？
- 任务体系是否已有独立 EPIC；若后续独立建设，本期只接收任务完成事件。
