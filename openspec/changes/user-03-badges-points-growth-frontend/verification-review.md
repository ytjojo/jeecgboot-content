## 验证结果摘要

| 类别 | 状态 | 说明 |
|------|------|------|
| 后端 API 路径 | **不匹配** | specs 中引用的 API 路径与实际后端路径不一致 |
| requestId 幂等 | **缺失** | 后端 `ContentUserExchangeReq` 无 `requestId` 字段 |
| 用户级衰减状态 API | **缺失** | 仅有全局衰减规则接口，无用户级衰减状态查询 |
| authorBadges 字段 | **缺失** | 帖子列表/详情接口未增加 `authorBadges` 字段 |
| levelChanged 全局字段 | **缺失** | 后端响应未包含 `levelChanged` 字段 |
| 前端 API 封装 | **未实现** | 3 个 API 文件、3 个 Store、11 个组件均未创建 |

---

## 后端 API 验证详情

### 实际后端 API 端点（ContentUserGrowthController）

基础路径：`/api/v1/content/user/growth`

| 端点 | 方法 | 功能 | specs 中引用的路径 | 是否匹配 |
|------|------|------|-------------------|----------|
| `/summary` | GET | 成长汇总（积分+成长值+等级） | `getPointBalance` / `getLevelInfo` | 路径不同，功能可用 |
| `/badge/catalog` | GET | 勋章分类目录 | `getBadgeList` | 路径不同，功能可用 |
| `/badge/detail` | GET | 勋章详情 | `getBadgeDetail` | 路径不同，功能可用 |
| `/badge/worn` | GET | 佩戴勋章列表 | `getWornBadges` / `getWornBadgesByUserId` | 路径不同，功能可用 |
| `/badge/wear` | POST | 保存佩戴勋章 | `/api/v1/content/user/growth/badge/wear` | **路径错误** |
| `/badge/recycle` | POST | 回收勋章 | `/content/admin/badge/recycle` | **路径错误** |
| `/point/ledger` | GET | 积分明细查询 | `getPointLedger` | 路径不同，功能可用 |
| `/point/exchange/goods` | GET | 兑换商品列表 | `getExchangeGoods` | 路径不同，功能可用 |
| `/point/exchange` | POST | 积分兑换 | `createExchange` | 路径不同，功能可用 |
| `/point/feature/unlock` | POST | 功能解锁 | `/api/v1/content/user/growth/point/feature/unlock` | **路径错误** |
| `/point/feature/unlock` | GET | 查询功能解锁状态 | (未在 specs 中提及) | 无 |
| `/point/gift/send` | POST | 赠送虚拟礼物 | `/api/v1/content/user/growth/point/gift/send` | **路径错误** |
| `/level/benefit` | GET | 等级权益摘要 | (未在 specs 中提及) | 无 |
| `/level/config` | GET | 等级配置列表 | `getLevelConfig` | 路径不同，功能可用 |
| `/decay/rule` | GET | 衰减规则说明 | `getDecayRule` | 路径不同，功能可用 |
| `/record` | POST | 记录积分与成长行为 | (未在 specs 中提及) | 无 |

### 后端已有但前端文档未引用的 API

| 端点 | 功能 | 建议 |
|------|------|------|
| `GET /point/feature/unlock` | 查询功能解锁状态 | 前端可复用，展示功能解锁状态 |
| `GET /level/benefit` | 等级权益摘要 | LevelBenefitList 组件可直接使用 |
| `POST /record` | 记录积分与成长行为 | 内部 API，前端无需封装 |

### 缺失的后端 API

| 需求 | 状态 | 影响 |
|------|------|------|
| 用户级衰减状态查询 | **缺失** | DecayWarning 组件无法展示用户当前衰减状态 |
| requestId 幂等字段 | **缺失** | ContentUserExchangeReq 无 requestId，前端幂等设计无法落地 |
| authorBadges 字段 | **缺失** | 帖子列表/详情未增加 authorBadges，BadgeDisplay 无数据来源 |
| levelChanged 全局字段 | **缺失** | 后端响应未携带 levelChanged，全局升级事件机制无法触发 |

### API 路径错误汇总

specs 中引用了 4 个错误的 API 路径：

1. `POST /content/user/badge/wear` → 实际：`POST /api/v1/content/user/growth/badge/wear`
2. `POST /content/admin/badge/recycle` → 实际：`POST /api/v1/content/user/growth/badge/recycle`
3. `POST /content/user/feature/unlock` → 实际：`POST /api/v1/content/user/growth/point/feature/unlock`
4. `POST /content/user/gift/send` → 实际：`POST /api/v1/content/user/growth/point/gift/send`

---

## 前端文档问题列表

### design.md 问题

1. **D2 决策中提到的 `authorBadges` 字段** — 后端未实现，需标注为阻塞项或降级处理
2. **D3 决策中提到的 `levelChanged` 字段** — 后端未实现，需标注为阻塞项
3. **D4 决策中提到的 `requestId` 幂等** — 后端 `ContentUserExchangeReq` 无此字段，需后端补充

### specs 问题

1. **badge-system/spec.md**：引用错误 API 路径 `/api/v1/content/user/growth/badge/wear` 和 `/api/v1/content/user/growth/badge/recycle`
2. **point-system/spec.md**：引用错误 API 路径 `/api/v1/content/user/growth/point/feature/unlock` 和 `/api/v1/content/user/growth/point/gift/send`
3. **point-system/spec.md**：ExchangeConcurrencyControl 中 requestId 幂等依赖后端未实现
4. **growth-level/spec.md**：Level up congratulations 依赖 `levelChanged` 字段，后端未实现
5. **decay-notice/spec.md**：用户级衰减状态查询 API 缺失
6. **badge-display/spec.md**：依赖 `authorBadges` 字段，后端未实现

### tasks.md 问题

1. **任务 1.1**：API 函数名（getBadgeList、getBadgeDetail 等）与实际后端端点路径不一致
2. **任务 1.2**：API 函数名（getPointBalance 等）与实际后端端点路径不一致，且 getPointBalance 无独立端点
3. **任务 1.3**：API 函数名（getLevelInfo 等）与实际后端端点路径不一致
4. **任务 4.1**：defHttp 拦截器增加 `levelChanged` 检测依赖后端未实现

---

## 建议修复方案

### 立即修复（前端文档内部可修正）✅ 已完成

1. ✅ **修正所有 specs 中的 API 路径**，统一使用 `/api/v1/content/user/growth/` 前缀（specs 中路径已正确）
2. ✅ **修正 tasks.md 中的 API 封装函数名**，与实际后端端点对齐（已更新为 getBadgeCatalog、getPointLedger、getGrowthSummary 等）
3. ✅ **将 getPointBalance 和 getLevelInfo 合并**，统一使用 `/api/v1/content/user/growth/summary` 接口（tasks.md 1.3 已更新）
4. ✅ **标注阻塞项**：design.md 和 specs 中已标注依赖后端但未实现的功能

### 需后端配合（记录到 backend-issues.md）

1. **ContentUserExchangeReq 增加 requestId 字段** — 支持幂等兑换
2. **帖子列表/详情接口增加 authorBadges 字段** — 支持帖子卡片勋章展示
3. **后端响应增加 levelChanged 字段** — 支持全局升级事件检测
4. **新增用户级衰减状态查询 API** — 支持 DecayWarning 组件
