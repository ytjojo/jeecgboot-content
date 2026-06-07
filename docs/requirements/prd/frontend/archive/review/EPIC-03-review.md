# EPIC-03 勋章、积分与成长体系 — 前端 PRD 审核报告

> **审核人**: 🏗️ Winston (System Architect)
> **审核日期**: 2026-06-02
> **审核视角**: 系统架构 — 技术可行性、可扩展性、组件设计

## 总体评价

PRD 结构完整，页面清单、组件设计、API 对接、状态管理、交互流程均有覆盖，具备直接进入开发阶段的基础。从架构角度看，三个新增 Store 模块的划分合理，API 封装遵循项目既有 `defHttp` 规范，组件复用策略与 `frontend-standards.md` 一致。但存在若干需要在开发前解决的架构隐患，主要集中在跨域状态同步、实时性保障和组件粒度方面。

## 优点

- **Store 模块划分清晰**：`useBadgeStore`、`usePointStore`、`useGrowthStore` 按业务域拆分，职责单一，符合 Pinia 最佳实践
- **缓存策略分层合理**：区分了低频变更数据（5 分钟 TTL）和高频变更数据（每次刷新），避免了"一刀切"的缓存策略
- **组件复用意识强**：明确列出了 13 个复用组件（Table、Modal、Form、CardList、CountTo 等），减少重复造轮子
- **API 封装规范统一**：所有接口放在 `src/api/content/` 目录下，使用 `defHttp`，响应格式与项目一致
- **响应式策略细致**：每个页面都有独立的断点适配方案，移动端表格转卡片列表、弹窗转 Drawer 等策略务实
- **性能指标量化**：每个关键交互都有明确的时间/帧率指标（如勋章列表 <500ms、动画 60fps），便于验收
- **边界情况考虑充分**：空状态、加载状态、图片加载失败、积分不足、库存不足等场景均有覆盖

## 问题与建议

### 🔴 高优先级

**1. 跨 Store 状态同步缺失**

PRD 定义了三个独立 Store，但未说明跨 Store 数据一致性如何保障。典型场景：
- 用户兑换商品后，`usePointStore.balance` 需要更新，同时 `useGrowthStore.levelInfo` 中的成长值也可能变化
- 勋章回收后，`useBadgeStore.badgeList` 和 `useBadgeStore.wornBadges` 需要同步刷新
- 升级事件可能在任何页面触发（非仅"我的等级页"），需要全局监听

**建议**：增加跨 Store 联动机制设计。可选方案：
- 方案 A：在 Store action 中调用其他 Store 的刷新方法（简单直接）
- 方案 B：使用 Pinia 的 `$onAction` 拦截器实现事件驱动同步（更解耦）
- 方案 C：定义一个轻量级 `useGrowthEventStore` 作为事件总线，广播升级/降级/勋章变更事件

**2. 升级祝贺弹窗的全局触发机制未定义**

PRD 8.5 节描述了升级祝贺交互，但仅说明"用户进入等级页面 / 等级发生变化时检测"。问题是：用户可能在帖子列表页、个人主页等任何位置触发升级，等级信息是在"进入等级页面"时才拉取的。

**建议**：
- 方案 A：在应用入口（如 `App.vue` 或路由守卫）定期轮询等级信息（每 5 分钟），检测到升级时触发全局弹窗
- 方案 B：依赖后端 WebSocket/SSE 推送升级事件，前端监听后触发（实时性最好，但依赖后端能力）
- 方案 C：每次 API 响应中携带 `levelChanged` 标记，前端检测到后触发（无需额外请求）

**3. 勋章佩戴展示的位置扩展性问题**

PRD 要求在主页、帖子卡片、评论区三处展示佩戴勋章（`BadgeDisplay` 组件），但这些位置分布在不同模块（个人中心、内容社区）。如果 `BadgeDisplay` 组件内部直接调用 `useBadgeStore.fetchWornBadges()`，会导致：
- 每个帖子卡片都触发 API 请求（帖子列表页可能有 20+ 个帖子）
- 查看他人主页时需要额外调用 `/api/v1/content/user/growth/badge/worn/{userId}`

**建议**：
- `BadgeDisplay` 组件应接受 `badges` 作为 prop，而非内部请求数据
- 帖子列表页应在列表数据中直接返回作者佩戴勋章信息（后端 join 查询），避免 N+1 请求
- 查看他人主页的佩戴勋章请求应在页面级别发起，通过 prop 传递给 `BadgeDisplay`

### 🟡 中优先级

**4. 组件粒度过细，建议合并**

PRD 定义了 11 个新增业务组件，其中部分组件功能高度相关，可合并以降低维护成本：
- `BadgeCard` + `BadgeGrid` + `BadgeDetail` + `BadgeDisplay` — 四个勋章相关组件，建议合并为 `BadgeModule` 下的子组件（BadgeCard、BadgeDetailView、BadgeWearDisplay），通过命名空间组织
- `LevelCard` + `LevelBenefitList` + `GrowthProgress` — 三个等级相关组件，建议合并为 `LevelModule` 下的子组件

**建议**：采用模块化目录结构：
```
src/components/content/badge/
  ├── BadgeCard.vue
  ├── BadgeDetailModal.vue
  ├── BadgeWearDisplay.vue
  └── BadgeGrid.vue
src/components/content/level/
  ├── LevelCard.vue
  ├── LevelBenefitList.vue
  └── GrowthProgressBar.vue
src/components/content/point/
  ├── PointBalanceCard.vue
  └── ExchangeConfirmModal.vue
```

**5. 积分明细页的筛选策略需优化**

PRD 描述"类型筛选切换后自动刷新列表"，但"时间范围选择后点击查询刷新列表"。两种筛选行为不一致会增加用户认知负担。同时，300ms 防抖仅适用于筛选操作，但 PRD 未明确哪些操作触发防抖。

**建议**：
- 统一筛选策略：所有筛选条件变更后均自动刷新（防抖 300ms），移除"查询"按钮
- 或统一为：所有筛选条件变更后均需点击"查询"才刷新（减少无效请求）
- 明确防抖的适用范围：仅限 Select 切换和 DatePicker 变更

**6. 勋章图片资源管理策略缺失**

PRD 提到"勋章图片使用懒加载 + WebP 格式 + CDN"，但未说明：
- 图片资源如何管理（运营上传？代码内置？）
- 不同尺寸（24px 小图、48px 卡片图、96px 详情大图）是否需要多套图片
- 图片 fallback 策略的具体实现（默认占位图是 SVG 还是 PNG）

**建议**：补充图片资源管理方案，建议：
- 后端返回统一的图片 URL，前端通过 CSS `background-size` 控制显示尺寸
- 提供 3 套尺寸：small(24px)、medium(48px)、large(96px)
- fallback 使用 SVG 占位图标（体积小、可缩放）

**7. 兑换操作的并发控制未考虑**

PRD 8.3 节提到兑换失败场景包括"库存不足/并发等"，但前端未设计并发控制机制。如果用户快速连续点击"确认兑换"，可能导致重复兑换。

**建议**：
- 前端：按钮 loading 期间完全禁用点击（PRD 已提及防重复提交，需确认实现方式）
- 前端：兑换成功后立即更新本地余额，避免用户在余额未刷新时再次兑换
- 后端幂等性：建议在 API 设计中加入幂等 token（如 `requestId`）

### 🟢 低优先级

**8. 路由建议缺少嵌套关系**

PRD 建议路由为 `/content/my-badges`、`/content/point-detail`、`/content/my-level` 等，但未说明这些路由是否需要嵌套在某个父路由下（如 `/content/growth/`）。如果后续扩展成长任务、排行榜等功能，平铺路由会变得混乱。

**建议**：考虑路由分组：
- `/content/growth/badges` — 勋章相关
- `/content/growth/points` — 积分相关
- `/content/growth/level` — 等级相关
- `/content/growth/mall` — 积分商城

**9. 等级配置接口缺少版本控制**

`/api/v1/content/user/growth/level-config` 返回等级阈值配置，但 PRD 未说明配置变更时前端如何感知。如果运营调整了等级阈值，用户看到的等级进度条可能与实际不符。

**建议**：在等级配置响应中增加 `version` 或 `updatedAt` 字段，前端对比本地缓存版本，有变化时自动刷新。

**10. 管理员勋章管理页缺少批量操作**

PRD 仅定义了单个勋章回收操作，如果需要批量回收违规用户的多个勋章，需逐个操作，效率低下。

**建议**：在表格中增加多选功能（`Table` 组件支持 rowSelection），增加批量回收按钮。

## 架构建议

### 1. 数据流架构建议

```
┌─────────────────────────────────────────────────┐
│                    页面层                         │
│  我的勋章页 / 积分商城页 / 我的等级页              │
└─────────┬───────────────────────────┬───────────┘
          │                           │
          ▼                           ▼
┌─────────────────┐         ┌─────────────────┐
│  useBadgeStore  │◄───────►│  usePointStore  │
│  (勋章状态)      │  事件    │  (积分状态)      │
└────────┬────────┘  同步    └────────┬────────┘
         │                           │
         ▼                           ▼
┌─────────────────────────────────────────────────┐
│              useGrowthStore                      │
│  (等级/经验值/衰减状态)                            │
│  → 升级事件广播（$onAction / mitt）               │
└─────────────────────────────────────────────────┘
          │
          ▼
┌─────────────────────────────────────────────────┐
│              API 层 (src/api/content/)            │
│  badge.ts / point.ts / growth.ts                 │
└─────────────────────────────────────────────────┘
```

### 2. 组件通信建议

- **父子组件**：通过 props 传递数据，events 回调操作结果
- **跨组件**：通过 Store 共享状态，避免 eventBus
- **全局事件**（升级/降级）：使用 mitt 或 Pinia `$onAction` 广播

### 3. 性能优化建议

- **勋章列表虚拟滚动**：PRD 提到 50+ 勋章时启用，建议使用 `@vueuse/core` 的 `useVirtualList` 或 Ant Design Vue 的虚拟列表
- **图片预加载**：勋章详情弹窗打开时预加载大图，避免弹窗内图片闪烁
- **API 请求合并**：进入"我的等级页"时，`getLevelInfo`、`getLevelConfig`、`getDecayRule` 三个接口可合并为一个（后端聚合）

### 4. 错误处理建议

- **全局错误拦截**：在 `defHttp` 层统一处理 401（跳转登录）、403（权限不足）、500（服务器错误）
- **业务错误**：兑换失败、佩戴失败等业务错误在组件内处理，显示具体错误信息
- **网络异常**：增加网络断开检测（`navigator.onLine`），断网时显示离线提示

## 总结

PRD 整体质量较高，架构设计与项目现有技术栈（Vue 3 + Pinia + Ant Design Vue）一致，组件复用策略合理。主要风险点在于跨 Store 状态同步、升级事件全局触发机制、以及勋章展示的 N+1 请求问题。建议在开发前优先解决上述 3 个高优先级问题，其余中低优先级问题可在开发过程中逐步优化。
