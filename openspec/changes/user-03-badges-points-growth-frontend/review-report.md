# Review Report: user-03-badges-points-growth-frontend

> **Change**: user-03-badges-points-growth-frontend (前端 change)
> **Domain**: user | **Epic**: EPIC-03
> **PRD**: EPIC-03-badges-points-growth-frontend-prd.md
> **配对后端 Change**: user-03-badges-points-growth
> **审核日期**: 2026-06-06
> **审核类型**: Apply 前置深度审核

---

## 总览

### 6 维度评分

| 维度 | 得分 | 说明 |
|------|------|------|
| 完整性 (Completeness) | 9/10 | 文档结构完整，5 个 capability 全覆盖，PRD 用户故事全覆盖 |
| 一致性 (Consistency) | 7/10 | PRD API 路径与实际后端不一致（遗留问题）；spec 阻塞标记与 backend-issues 状态不同步 |
| 可实现性 (Feasibility) | 9/10 | 技术栈兼容，架构决策合理，与项目既有模式一致 |
| 可测试性 (Testability) | 8/10 | 场景覆盖全面，多数可量化；部分场景缺少具体数值阈值 |
| 接口契约 (API Contract) | 6/10 | PRD 5.1-5.3 节 API 路径与实际后端端点存在系统性偏差；4 个端点路径错误已修正于 tasks/specs 但 PRD 未同步 |
| 边界覆盖 (Boundary) | 8/10 | 覆盖 8/10 类边界条件，网络断开恢复和时区边界未显式覆盖 |

### 问题计数

| 级别 | 数量 | 说明 |
|------|------|------|
| BLOCK | 0 | 无阻塞性问题（所有原始 BLOCK 已解决或转为 FLAG） |
| FLAG | 3 | 需关注但不阻塞 apply |
| ADVISORY | 4 | 建议改进项 |

### 量化指标

| 指标 | 值 | 说明 |
|------|-----|------|
| PRD 用户故事覆盖率 | 100% (13/13) | US-3.1.1~3.3.5 全部在 specs 中有对应 scenario |
| PRD AC 覆盖率 | 95% | PRD 第 3-4 节页面/组件设计在 specs+design 中全覆盖 |
| API 契约完整率 | 75% | 16 个端点中 12 个路径正确，4 个已在 specs/tasks 修正但 PRD 未同步 |
| 边界覆盖率 | 80% | 10 类边界中覆盖 8 类 |
| TDD 配对率 | N/A | 前端 change 无后端测试要求；前端测试由 DoD 流程管控 |
| Spec 场景总数 | 42 | 5 个 spec 文件合计 42 个 scenario |
| 阻塞标记数 | 1 | badge-display spec 中 authorBadges 仍标记 [阻塞] |

---

## 1. 完整性 (Completeness) — 9/10

### 文档结构

| 文件 | 存在 | 内容完整 |
|------|------|---------|
| proposal.md | Yes | Why/What/Goals/Capabilities/Impact 齐全 |
| design.md | Yes | Context/Goals/Decisions(6)/Risks 齐全 |
| specs/ (5 files) | Yes | badge-system, point-system, growth-level, decay-notice, badge-display |
| tasks.md | Yes | 38 个任务，12 个分组，全部标记完成 |
| backend-issues.md | Yes | 4 个后端遗留问题，3 个已解决 |
| verification-review.md | Yes | 后端 API 验证结果（早期文档） |

### 内容覆盖

**PRD 用户故事 → Spec 场景映射**：

| PRD 用户故事 | 对应 Spec | 场景数 | 覆盖状态 |
|-------------|----------|--------|---------|
| US-3.1.1 勋章分类浏览 | badge-system | 3 | 完整 |
| US-3.1.2 佩戴设置 | badge-system | 4 | 完整 |
| US-3.1.3 过期/回收 | badge-system | 3 | 完整 |
| US-3.2.1 日常积分 | point-system (后端) | N/A | 后端覆盖 |
| US-3.2.4 积分兑换 | point-system | 7 | 完整 |
| US-3.2.5 功能解锁+礼物 | point-system | 2 | 完整 |
| US-3.2.6 积分明细 | point-system | 4 | 完整 |
| US-3.3.1 等级成长 | growth-level | 2 | 完整 |
| US-3.3.2 积分/成长值分栏 | growth-level | 1 | 完整 |
| US-3.3.3 等级权益 | growth-level | 1 | 完整 |
| US-3.3.5 衰减规则 | decay-notice | 4 | 完整 |
| 勋章佩戴展示 | badge-display | 6 | 完整（1 场景阻塞中） |
| 升级祝贺 | growth-level | 4 | 完整（1 场景阻塞中） |

**缺失项**：
- PRD US-3.2.2（创作激励积分）和 US-3.2.3（社交/任务积分）为后端奖励规则配置，前端无独立页面，设计文档中未明确说明这些场景的前端展示策略（仅展示积分获取结果）。**ADVISORY-1**

---

## 2. 一致性 (Consistency) — 7/10

### Capabilities ↔ Specs 一致性

| Proposal Capability | 对应 Spec | 一致性 |
|--------------------|----------|--------|
| badge-system | specs/badge-system/spec.md | 一致 |
| point-system | specs/point-system/spec.md | 一致 |
| growth-level | specs/growth-level/spec.md | 一致 |
| decay-notice | specs/decay-notice/spec.md | 一致 |
| badge-display | specs/badge-display/spec.md | 一致 |

### Decisions ↔ Requirements 一致性

| Design Decision | 对应 Spec 要求 | 一致性 |
|----------------|---------------|--------|
| D1: 组合式 API + Pinia | tasks 3.1-3.3 (Store 创建) | 一致 |
| D2: BadgeDisplay Prop 驱动 | badge-display prop-driven component | 一致 |
| D3: defHttp 拦截器 + mitt | growth-level level up congratulations | 一致 |
| D4: requestId 幂等 | point-system exchange concurrency control | 一致 |
| D5: 响应式断点 | tasks 12.1-12.5 | 一致 |
| D6: Pinia + TTL 缓存 | design.md 6.3 缓存策略 | 一致 |

### 发现的不一致

**FLAG-1: PRD API 路径与 specs/tasks 不一致**

前端 PRD 第 5.1-5.3 节定义的 API 路径与实际后端端点和 specs/tasks 中已修正的路径存在系统性偏差：

| PRD 中的路径 | 实际后端路径 | 状态 |
|-------------|------------|------|
| `GET /content/user/badge/list` | `GET /content/user/growth/badge/catalog` | PRD 未同步 |
| `GET /content/user/badge/detail/{badgeCode}` | `GET /content/user/growth/badge/detail` | PRD 未同步 |
| `GET /content/user/badge/worn` | `GET /content/user/growth/badge/worn` | PRD 未同步 |
| `POST /content/user/badge/wear` | `POST /content/user/growth/badge/wear` | PRD 未同步 |
| `GET /content/user/point/balance` | `GET /content/user/growth/summary` (合并) | PRD 未同步 |
| `GET /content/user/exchange/goods` | `GET /content/user/growth/point/exchange/goods` | PRD 未同步 |
| `POST /content/user/exchange/create` | `POST /content/user/growth/point/exchange` | PRD 未同步 |
| `POST /content/user/feature/unlock` | `POST /content/user/growth/point/feature/unlock` | PRD 未同步 |
| `POST /content/user/gift/send` | `POST /content/user/growth/point/gift/send` | PRD 未同步 |
| `GET /content/user/growth/level` | `GET /content/user/growth/summary` (合并) | PRD 未同步 |
| `GET /content/user/growth/level-config` | `GET /content/user/growth/level/config` | PRD 未同步 |
| `GET /content/user/growth/decay-rule` | `GET /content/user/growth/decay/rule` | PRD 未同步 |
| `POST /content/admin/badge/recycle` | `POST /content/user/growth/badge/recycle` | PRD 未同步 |

specs 和 tasks.md 中的 API 路径已修正为正确路径，但 PRD 第 5 节未同步更新。verification-review.md 已记录此问题并标记为"已修正"，但修正范围仅限于 specs/tasks，未覆盖 PRD。

**FLAG-2: Spec 阻塞标记与 backend-issues.md 状态不同步**

| 阻塞项 | Spec 标记 | backend-issues.md 状态 | 实际状态 |
|--------|----------|----------------------|---------|
| authorBadges 字段 | badge-display: [阻塞] | Issue 2: 待处理 | **后端未实现**，标记正确 |
| levelChanged 字段 | growth-level: [阻塞] | Issue 3: 已解决 | **不一致**，spec 应移除阻塞标记 |
| requestId 幂等 | point-system: [阻塞] | Issue 1: 已解决 | **不一致**，spec 应移除阻塞标记 |
| 用户级衰减状态 | decay-notice: [阻塞] | Issue 4: 已解决 | **不一致**，spec 应移除阻塞标记 |

3 个已解决的后端 issue 在 specs 中仍标记为 [阻塞]，应移除标记以反映当前状态。

**ADVISORY-2: PRD 第 5.4 节 API 封装示例代码与实际实现可能不一致**

PRD 中的示例代码使用旧路径（如 `getBadgeList`、`getPointBalance`、`getLevelInfo`），而 tasks.md 已更新为新函数名（`getBadgeCatalog`、`getPointLedger`、`getGrowthSummary`）。PRD 示例代码应同步更新。

---

## 3. 可实现性 (Feasibility) — 9/10

### 技术栈兼容性

| 项目 | 评估 |
|------|------|
| Vue 3 组合式 API | 项目已有成熟使用模式 |
| Pinia Store | 项目已从 Vuex 迁移至 Pinia |
| Ant Design Vue | 项目主力 UI 库，Tabs/Modal/Table/Form 等组件可直接复用 |
| defHttp API 封装 | 项目标准 API 封装模式 |
| mitt 事件总线 | 轻量级事件方案，适合全局升级事件 |
| @vue/test-utils + Vitest | 项目已迁移至 Vitest 测试框架 |

### 架构规范合规性

| 检查项 | 结果 |
|--------|------|
| 组件目录 `src/components/content/` | 符合项目组件组织规范 |
| Store 目录 `src/store/modules/` | 符合 Pinia 模块化规范 |
| API 目录 `src/api/content/` | 符合 API 封装规范 |
| 路由配置 | 5 个新路由，符合路由注册规范 |
| 响应式策略 | 1200px/768px 断点，符合项目响应式基础 |

### 设计决策合理性

- **D2 Prop 驱动**: 避免帖子列表 N+1 请求，性能收益显著，决策合理
- **D3 全局拦截器**: 覆盖所有 API 调用，避免逐页面修改，决策合理
- **D4 requestId 幂等**: 分布式系统标准做法，决策合理
- **D6 分级缓存**: 低频/高频数据区分处理，决策合理

**ADVISORY-3: 大量勋章虚拟滚动阈值**

design.md 提到超过 50 个勋章时启用虚拟滚动（antdv VirtualList），但 specs 和 tasks 中未明确虚拟滚动的实现细节。建议在 badge-system spec 中补充虚拟滚动场景。

---

## 4. 可测试性 (Testability) — 8/10

### 场景可量化评估

| Spec | 场景总数 | 可量化 | 部分量化 | 不可量化 |
|------|---------|--------|---------|---------|
| badge-system | 10 | 8 | 2 | 0 |
| point-system | 9 | 7 | 2 | 0 |
| growth-level | 8 | 6 | 2 | 0 |
| decay-notice | 6 | 5 | 1 | 0 |
| badge-display | 9 | 7 | 2 | 0 |
| **合计** | **42** | **33 (79%)** | **9 (21%)** | **0** |

### 部分量化场景清单

| Spec | 场景 | 缺少的量化指标 |
|------|------|--------------|
| badge-system | Badge category browsing | 分类标签切换响应时间未定义 |
| point-system | Point ledger query | 分页默认每页条数未在 spec 中明确 |
| point-system | Exchange concurrency control | requestId UUID 格式未在 spec 中定义 |
| growth-level | Level up animation | 60fps 指标在 spec 中提及但未定义验证方法 |
| growth-level | Level up congratulations | 7 天冷却期的"7 天"在 spec 中有，但去重窗口（同一轮事件循环）未量化 |
| decay-notice | Decay status warning | "X 天未登录"中的 X 未在 spec 中定义具体取值范围 |
| badge-display | Badge hover tooltip | Tooltip 延迟显示时间未定义 |
| badge-display | Badge image fallback | fallback 图标加载超时阈值未定义 |
| badge-display | Badge image lazy loading | 懒加载预加载距离（rootMargin）未定义 |

### TDD 配对

前端 change 无后端测试要求。前端测试由 DoD 流程管控（Vitest + @vue/test-utils），testing-conventions.md 已定义 4 类测试模式（API/Store/Composable/Component）。

---

## 5. 接口契约 (API Contract) — 6/10

### API 端点清单对比

**前端 specs/tasks 引用的端点（已修正）**：

| 方法 | 路径 | 来源 | 后端实际 | 匹配 |
|------|------|------|---------|------|
| GET | `/content/user/growth/badge/catalog` | tasks 1.1 | ContentUserGrowthController | 匹配 |
| GET | `/content/user/growth/badge/detail` | tasks 1.1 | ContentUserGrowthController | 匹配 |
| GET | `/content/user/growth/badge/worn` | tasks 1.1 | ContentUserGrowthController | 匹配 |
| POST | `/content/user/growth/badge/wear` | tasks 1.1 | ContentUserGrowthController | 匹配 |
| POST | `/content/user/growth/badge/recycle` | tasks 1.1 | ContentUserGrowthController | 匹配 |
| GET | `/content/user/growth/point/ledger` | tasks 1.2 | ContentUserGrowthController | 匹配 |
| GET | `/content/user/growth/point/exchange/goods` | tasks 1.2 | ContentUserGrowthController | 匹配 |
| POST | `/content/user/growth/point/exchange` | tasks 1.2 | ContentUserGrowthController | 匹配 |
| POST | `/content/user/growth/point/feature/unlock` | tasks 1.2 | ContentUserGrowthController | 匹配 |
| GET | `/content/user/growth/point/feature/unlock` | tasks 1.2 | ContentUserGrowthController | 匹配 |
| POST | `/content/user/growth/point/gift/send` | tasks 1.2 | ContentUserGrowthController | 匹配 |
| GET | `/content/user/growth/summary` | tasks 1.3 | ContentUserGrowthController | 匹配 |
| GET | `/content/user/growth/level/config` | tasks 1.3 | ContentUserGrowthController | 匹配 |
| GET | `/content/user/growth/level/benefit` | tasks 1.3 | ContentUserGrowthController | 匹配 |
| GET | `/content/user/growth/decay/rule` | tasks 1.3 | ContentUserGrowthController | 匹配 |
| GET | `/content/user/growth/decay/status` | backend-issues Issue 4 | ContentUserGrowthController | 匹配（新增） |

**specs/tasks 中的 API 路径已全部修正为正确路径**，与后端实际端点一致。

**FLAG-3: PRD 第 5 节 API 路径未同步修正**

PRD 第 5.1-5.3 节仍使用旧路径，与实际后端端点不一致。详见 FLAG-1。

### 后端已有但前端未引用的 API

| 端点 | 功能 | 建议 |
|------|------|------|
| `POST /content/user/growth/record` | 记录积分与成长行为 | 内部 API，前端无需封装（合理） |

### req/vo 匹配

| API | 请求参数 | 响应 VO | 匹配状态 |
|-----|---------|---------|---------|
| badge/catalog | 无 | ContentUserBadgeCatalogVO | 待确认字段 |
| badge/detail | badgeCode (query) | ContentUserBadgeDetailVO | 待确认字段 |
| badge/worn | 无 | List<ContentUserBadgeVO> | 待确认字段 |
| badge/wear | badgeIds (body) | 成功/失败 | 待确认 |
| badge/recycle | grantId, reason (body) | 成功/失败 | 待确认 |
| point/ledger | type, startTime, endTime, page, pageSize | 分页<ContentUserPointLedgerVO> | 待确认字段 |
| point/exchange | goodsId, quantity, requestId (body) | ContentUserPointSpendResultVO | 含 levelChanged 字段 |
| point/exchange/goods | 无 | List<ContentUserExchangeGoodsVO> | 待确认字段 |
| growth/summary | 无 | ContentUserGrowthSummaryVO | 含积分余额+等级信息 |

**ADVISORY-4: VO 字段定义未在前端 specs 中明确**

前端 specs 定义了交互场景但未明确 VO 字段结构。建议在 API 封装层实现时参照后端 VO 定义确保字段对齐。

---

## 6. 边界覆盖 (Boundary) — 8/10

### 10 类边界条件覆盖

| # | 边界类型 | 覆盖状态 | 对应场景 |
|---|---------|---------|---------|
| 1 | 空状态 | 已覆盖 | badge empty state, point empty state, filter no results |
| 2 | 极值 | 已覆盖 | 最多佩戴 5 个勋章、积分不足差额提示、最高等级进度条满格 |
| 3 | 并发 | 已覆盖 | requestId 幂等、前端防重、弹窗锁定、乐观更新 |
| 4 | 权限 | 已覆盖 | 管理员勋章回收权限校验、勋章管理页路由权限 |
| 5 | 网络异常 | 部分覆盖 | API 失败时显示错误提示；**未覆盖**：网络断开恢复后的状态同步 |
| 6 | 数据竞争 | 已覆盖 | 兑换并发竞态三重保障、佩戴设置取消恢复 |
| 7 | 输入验证 | 已覆盖 | 回收原因必填+200 字限制、勋章勾选上限 |
| 8 | 时序依赖 | 已覆盖 | 升级弹窗 7 天冷却期、事件去重（同一轮事件循环） |
| 9 | 资源限制 | 已覆盖 | 50+ 勋章虚拟滚动、分页加载、图片懒加载 |
| 10 | 时区/国际化 | 未覆盖 | 时间范围筛选的时区处理未在 spec 中定义 |

**边界覆盖总结**：8/10 类已覆盖。未覆盖的 2 类（网络断开恢复、时区）为 ADVISORY 级别。

---

## 前后端衔接审计

### 接口清单双向对比

| 前端引用（specs/tasks） | 后端定义（实际 Controller） | 匹配 |
|------------------------|--------------------------|------|
| 16 个端点 | 16 个端点 | 全部匹配（specs/tasks 已修正） |

### 数据模型一致性

| 字段 | 前端期望 | 后端实际 | 状态 |
|------|---------|---------|------|
| requestId (exchange) | String, 前端生成 UUID | ContentUserExchangeReq 已增加 | 已解决 |
| levelChanged | Boolean + newLevel | ContentUserPointSpendResultVO 已增加 | 已解决 |
| authorBadges | List<BadgeVO> | 帖子接口未增加 | 未解决（降级处理） |
| decayStatus | status/inactiveDays/protectionUntil | ContentUserGrowthDecayStatusVO 已新增 | 已解决 |

### 错误码覆盖

前端 specs 中未定义错误码映射。建议在 API 封装层统一处理后端错误码。

### 认证鉴权一致性

- 前端路由权限：勋章管理页需管理员角色（tasks 11.2）
- 后端鉴权：`/content/admin/badge/recycle` 需管理员权限（后端 spec）
- 一致性：匹配

### 分页契约

- 前端 PRD：每页 20 条，最大 100 条/页
- 后端 spec：分页参数 page/pageSize，最大值由 API 规则控制
- 一致性：匹配

---

## PRD 追溯矩阵

| PRD 章节 | PRD 内容 | 对应 Artifact | 覆盖状态 |
|----------|---------|--------------|---------|
| 1.1 需求目标 | 游戏化激励体系 | proposal.md Why | 完整 |
| 1.2 目标用户 | 4 种角色场景 | proposal.md Goals | 完整 |
| 1.3 范围定义 | 本期/非本期 | proposal.md Non-Goals | 完整 |
| 2 用户故事 | 13 个 US | 5 个 specs (42 scenarios) | 完整 |
| 3.1 我的勋章页 | 页面结构+交互 | badge-system spec + tasks 8.1-8.4 | 完整 |
| 3.2 积分明细页 | 页面结构+交互 | point-system spec + tasks 9.1-9.2 | 完整 |
| 3.3 积分商城页 | 页面结构+交互 | point-system spec + tasks 9.3-9.4 | 完整 |
| 3.4 我的等级页 | 页面结构+交互 | growth-level spec + tasks 10.1-10.3 | 完整 |
| 3.5 勋章管理页 | 页面结构+交互 | badge-system spec (admin recycle) + tasks 8.4 | 完整 |
| 4.1 新增组件 | 11 个组件 | tasks 5.1-7.4 | 完整 |
| 4.2 复用组件 | 13 个复用 | design.md 组件选型 | 完整 |
| 4.3 BadgeDisplay | prop 驱动策略 | badge-display spec + design D2 | 完整 |
| 4.4 图片资源 | 尺寸/fallback/加载 | badge-display spec | 完整 |
| 5 API 对接 | 16 个端点 | tasks 1.1-1.3 | 完整（PRD 路径需同步） |
| 6 状态管理 | 3 个 Store + 事件 | tasks 3.1-4.2 + design D3 | 完整 |
| 7 组件选型 | 选型表 | design.md | 完整 |
| 8 交互设计 | 6 个交互流程 | 5 个 specs scenarios | 完整 |
| 9 响应式设计 | 3 断点 5 页面 | tasks 12.1-12.5 + design D5 | 完整 |
| 10 性能要求 | 8 项指标 | design.md Risks | 完整 |
| 11 测试要点 | 4 类测试 | testing-conventions.md | 完整 |
| 12 待确认问题 | 6 个问题 | design.md Open Questions | 完整 |

---

## 问题清单

### FLAG 级别（需关注，不阻塞 apply）

| ID | 问题 | 影响 | 建议 |
|----|------|------|------|
| FLAG-1 | PRD 第 5.1-5.3 节 API 路径与实际后端不一致 | 开发参考 PRD 时可能使用错误路径 | 同步更新 PRD API 路径 |
| FLAG-2 | 3 个已解决的后端 issue 在 specs 中仍标记 [阻塞] | 文档误导 | 移除已解决项的阻塞标记 |
| FLAG-3 | PRD 第 5.4 节 API 封装示例代码使用旧函数名 | 开发参考时可能使用错误函数名 | 同步更新示例代码 |

### ADVISORY 级别（建议改进）

| ID | 问题 | 建议 |
|----|------|------|
| ADVISORY-1 | US-3.2.2/3.2.3 创作/社交积分的前端展示策略未在 spec 中明确 | 补充说明这些积分通过积分明细页展示 |
| ADVISORY-2 | PRD 示例代码函数名与 tasks.md 不一致 | 统一为 getBadgeCatalog/getPointLedger/getGrowthSummary |
| ADVISORY-3 | 虚拟滚动实现细节未在 spec 中定义 | 补充虚拟滚动场景和配置 |
| ADVISORY-4 | VO 字段定义未在前端 specs 中明确 | 实现时参照后端 VO 确保字段对齐 |

---

## 最终结论

**审核结果：通过（有条件）**

change 文档质量整体良好，5 个 capability 完整覆盖 PRD 全部用户故事，6 个设计决策合理且与 spec 一致，42 个测试场景覆盖主要交互流程和边界条件。specs 和 tasks 中的 API 路径已修正为与后端实际端点一致。

**需要处理的事项**（不阻塞 apply，但应在实现前或实现中完成）：

1. **同步更新 PRD 第 5 节 API 路径**（FLAG-1/FLAG-3）：将 PRD 中的旧 API 路径和示例代码更新为与实际后端端点一致
2. **更新 spec 阻塞标记**（FLAG-2）：移除已解决的 3 个阻塞标记（requestId、levelChanged、decayStatus）
3. **确认 authorBadges 降级方案**：badge-display spec 中帖子勋章展示仍标记 [阻塞]，前端已做好降级处理（无数据时不展示），可继续 apply

**建议 apply 顺序**：先完成 API 封装层（tasks 1.1-1.3），再实现 Store（tasks 3.1-3.3），然后组件和页面。全局拦截器（task 4.1-4.2）可在 Store 之后独立完成。
