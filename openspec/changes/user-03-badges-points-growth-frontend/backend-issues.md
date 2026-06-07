## 后端遗留代码问题

本文档记录前端实现依赖但后端尚未完成的功能，需后端配合补充。

---

### Issue 1：ContentUserExchangeReq 缺少 requestId 字段 ✅ 已解决

**优先级**：高

**现状**：`ContentUserExchangeReq`（`jeecg-module-content/.../req/growth/ContentUserExchangeReq.java`）当前字段为 `userId`、`goodsId`、`quantity`，无 `requestId` 字段。

**需要修改**：
1. `ContentUserExchangeReq` 增加 `requestId` 字段（String，非必填，最大 64 位）
2. `ContentUserPointSpendServiceImpl.exchangeGoods()` 方法中基于 `requestId` 做幂等校验：相同 `requestId` 的重复请求返回已有结果而非重复扣积分

**影响**：前端 D4 决策（积分兑换并发控制）中的幂等校验无法落地，当前仅能依赖前端防重。

**已完成**：`fix/growth-backend-blockers` 分支已合并到 `springboot3_content`，包含 4 个文件修改和 2 个新增测试。

---

### Issue 2：帖子列表/详情接口缺少 authorBadges 字段

**优先级**：中

**现状**：帖子列表和详情 API 响应中无 `authorBadges` 字段。

**需要修改**：
1. 帖子列表/详情的 VO 增加 `authorBadges` 字段（`List<ContentUserBadgeVO>` 类型）
2. 查询时 join 用户佩戴勋章数据（`content_user_badge_grant` 表，`worn = true`）

**影响**：BadgeDisplay 组件在帖子卡片中无数据来源。前端已做降级处理：无 badges 数据时不展示。

**涉及文件**：帖子相关 VO 和 Controller（需确认具体文件路径）。

---

### Issue 3：后端响应缺少 levelChanged 全局字段 ✅ 已解决

**优先级**：中

**现状**：后端 API 响应格式为 `{ code, result, message, success }`，无 `levelChanged` 字段。

**需要修改**：
1. 在成长值变更相关的 API 响应中增加 `levelChanged` 字段（boolean 或包含新等级信息的对象）
2. 建议在响应包装层（Result 类扩展或响应拦截器）统一处理，避免逐个接口修改

**影响**：全局升级祝贺弹窗机制（defHttp 拦截器 + mitt 事件）无法触发。前端先完成代码实现，待后端就绪后联调。

**已完成**：`ContentUserPointSpendResultVO` 增加 `levelChanged`（Boolean）和 `newLevel`（Integer）字段，按业务场景逐步扩展，避免全局 Result 改动。

---

### Issue 4：缺少用户级衰减状态查询 API ✅ 已解决

**优先级**：中

**现状**：仅有 `GET /api/v1/content/user/growth/decay/rule` 返回全局衰减规则（`ContentUserGrowthDecayRuleVO`），无查询特定用户当前衰减状态的接口。

**需要新增**：
1. `GET /api/v1/content/user/growth/decay/status?userId=xxx` 端点
2. 返回用户当前衰减状态：`status`（NORMAL/DECAYING/PROTECTION/DOWNGRADED）、`inactiveDays`、`protectionUntil`、`currentLevel` 等
3. 数据来源：`content_user_growth_decay_state` 表 + `content_user_profile` 表

**影响**：DecayWarning 组件无法展示用户当前衰减状态，只能展示全局规则。

**已完成**：新增 `GET /api/v1/content/user/growth/decay/status` 端点，返回 `ContentUserGrowthDecayStatusVO`，包含 status、inactiveDays、protectionUntil、currentLevel、currentGrowthValue、lastActiveTime、decayCount 字段。

---

## 后端修改优先级建议

| 优先级 | Issue | 建议排期 | 状态 |
|--------|-------|---------|------|
| P0 | Issue 1 - requestId 幂等 | 积分兑换功能开发前 | ✅ 已完成 |
| P1 | Issue 3 - levelChanged 字段 | 升级弹窗联调前 | ✅ 已完成 |
| P1 | Issue 4 - 用户级衰减状态 | 衰减警告组件开发前 | ✅ 已完成 |
| P2 | Issue 2 - authorBadges | 帖子勋章展示功能开发前 | ⏳ 待处理（需先创建 CircleContentVO） |
