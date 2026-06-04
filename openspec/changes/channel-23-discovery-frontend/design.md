## Context

JeecgBoot_sass 内容社区模块已有频道基础功能（EPIC-20 创建、EPIC-21 成员管理），但频道发现能力缺失。前端基于 Vue 3 + Vite + Ant Design Vue + Pinia，使用 `defHttp` 封装 API 调用，已有 `useTable`/`useForm`/`useModal` 等通用 hooks。

后端频道可见性服务（ChannelVisibilityService）已上线，负责过滤私有/隐藏/冻结/限制公开曝光/未通过审核频道，前端无需额外过滤。

## Goals / Non-Goals

**Goals:**
- 建立完整的频道发现前端页面体系（发现页、分类浏览、搜索、排行榜）
- 提供运营后台分类管理和编辑精选管理工具
- 提供频道管理后台标签管理模块
- 搜索 P99 <= 200ms，推荐 P95 <= 500ms
- 完整的移动端响应式适配

**Non-Goals:**
- 推荐模型训练与算法迭代（服务端负责）
- 搜索基础设施迁移到 Elasticsearch
- 数据统计看板（EPIC-24）
- 独立移动端页面（通过响应式适配）

## Decisions

### 1. 路由组织：嵌套路由 vs 独立页面

**选择**: 独立页面路由，统一放在 `/channel/` 前缀下

**理由**: 发现页、分类浏览、搜索结果、排行榜功能独立，各自有完整的页面生命周期和数据加载逻辑，嵌套路由会增加复杂度但收益有限。

**路由规划**:
- `/channel/discovery` - 频道发现页
- `/channel/category` - 分类浏览页（`:id?` 可选分类 ID）
- `/channel/search` - 搜索结果页
- `/channel/ranking` - 排行榜页
- `/channel/category-manage` - 运营后台分类管理
- `/channel/editorial-pick-manage` - 运营后台精选管理

### 2. 状态管理：集中式 Store vs 页面内 composable

**选择**: 3 个集中式 Pinia Store + 页面级 composable

**理由**: 分类树数据跨页面共享（发现页、分类浏览、搜索筛选、分类选择组件都需要），集中缓存避免重复请求。页面特有的交互逻辑使用 composable 封装。

**Store 划分**:
- `useChannelDiscoveryStore`: 发现页聚合数据（推荐+榜单+精选），5 分钟缓存
- `useChannelCategoryStore`: 分类树数据，会话级缓存，写操作后刷新
- `useChannelSearchStore`: 搜索状态，不缓存结果，持久化搜索历史到 localStorage

### 3. 发现页数据加载：并行请求 vs 聚合 API

**选择**: 聚合 API `GET /content/channel/discovery/home` 一次性获取推荐+榜单+精选

**理由**: 减少 HTTP 请求数，后端可做聚合优化，前端只需一次 loading 状态管理。聚合失败时降级为并行请求各子接口。

**降级策略**: 聚合接口超时或失败时，前端 fallback 为并行调用以下独立接口：
- `GET /content/channel/recommendation/list`（推荐频道）
- `GET /content/channel/ranking/hot`（热门排行榜）
- `GET /content/channel/editorial-pick/list`（编辑精选）

**后端状态**: `ContentChannelDiscoveryBiz` 已实现聚合逻辑，但尚缺 Controller 端点暴露（见 backend-issues.md）。

### 4. 频道卡片组件：单一组件多模式 vs 多个独立组件

**选择**: 单一 `ChannelCard` 组件，通过 `mode` prop 控制展示模式

**理由**: 推荐、搜索、分类浏览、排行榜的卡片信息层级高度重叠（图标+名称+分类+订阅数），仅推荐理由/匹配原因/排名序号有差异。单一组件减少维护成本，通过 mode 控制信息展示。

### 5. 分类浏览：左侧树 + 右侧列表 vs 顶部 Tab 切换

**选择**: PC 端左侧分类树（240px 固定宽度）+ 右侧卡片网格；移动端顶部分类 Tab 或下拉选择器

**理由**: 分类层级最多 4 级，左侧树形导航是 PC 端最自然的多级分类浏览方式。移动端屏幕宽度有限，收起为 Tab 或下拉。

### 6. 搜索防抖与缓存策略

**选择**: 输入防抖 300ms，搜索结果不缓存，搜索历史持久化到 localStorage

**理由**: 搜索结果实时性要求高，缓存可能导致过期数据。搜索历史对用户体验有价值，持久化到 localStorage 跨会话保留。

## Risks / Trade-offs

- **[聚合 API 耦合]** → 聚合接口 `/content/channel/discovery/home` 将推荐、榜单、精选绑定在一起，任一模块接口变更可能影响聚合接口。缓解：实现降级逻辑，聚合失败时 fallback 到独立接口。
- **[分类树数据量]** → 假设分类总数 < 500，一次性加载到 Store。若分类数增长超出预期，需改为懒加载子分类。缓解：前端校验层级不超过 4 级，监控分类树加载性能。
- **[搜索性能依赖后端]** → 搜索 P99 <= 200ms 目标依赖后端 MySQL FULLTEXT 性能。缓解：前端实现骨架屏减少感知延迟，搜索降级时展示热门频道。
- **[可见性过滤一致性]** → 前端依赖后端 ChannelVisibilityService 过滤，前端不做二次过滤。若后端过滤遗漏，前端会展示不应显示的频道。缓解：后端已覆盖所有可见性场景，前端仅做展示。
