## Context

当前 `channel/` 包为空壳，EPIC-20/21/22 的频道基础架构、隐私订阅和内容治理尚未在代码中落地。EPIC-23 需要从零建立频道发现能力，包括分类、标签、推荐、榜单、精选、浏览和搜索。

已有可参考的模式：
- `ContentUserFollowRecommendation` 实体/服务：推荐评分、推荐规则、推荐理由、状态管理
- `ContentUserNotInterested` 实体：不感兴趣反馈
- `ContentFanTrendDaily` + 定时任务：每日聚合统计

项目遵循分层架构：`controller / biz / service / mapper / entity / req / vo / dto`，实体继承 `JeecgEntity`，服务继承 `ServiceImpl`，控制器返回 `Result<T>`。

## Goals / Non-Goals

**Goals:**
- 建立完整的平台分类树体系（最多 4 级），支持频道主/副分类选择
- 实现频道内标签的 CRUD 和内容关联筛选
- 提供个性化频道推荐，含冷启动、不感兴趣反馈和推荐理由
- 建立热门/新晋/系统频道排行榜，每日定时更新
- 支持运营标记编辑精选频道，配置推荐语和有效期
- 实现分类浏览页面，支持排序和分页
- 实现频道搜索，覆盖名称/描述/标签，支持多条件筛选，P99 <= 200ms
- 所有公开发现入口严格遵守频道可见性边界

**Non-Goals:**
- 推荐模型训练方案（使用规则+评分的简单策略，后续迭代）
- 搜索基础设施选型（使用数据库 LIKE + 全文索引，后续可迁移到 ES）
- 平台级违规内容识别
- EPIC-20/21/22 的功能实现

## Decisions

### D1: 分类树存储方案

**选择**: 邻接表（parent_id 自引用）

**理由**: 分类最多 4 级，树深度有限，邻接表实现简单、查询效率可接受。路径枚举（path 字段）冗余存储祖先路径，加速祖先查询但增加写入复杂度。

**替代方案**:
- 闭包表：查询灵活但表膨胀快，4 级树用闭包表过度设计
- 嵌套集合：插入/移动代价高，分类树变更频繁时不适合

**决策**: 采用邻接表 + `path` 字段（如 `/001/002/003`），兼顾查询效率和写入简洁。

## Decisions

### D1: 分类树实现方式

采用邻接表（parent_id）+ 冗余 path 字段。parent_id 维护树结构，path 加速祖先查询和分类浏览。

```
content_channel_category
├── id (varchar 32)
├── parent_id (varchar 32, null for root)
├── name (varchar 50)
├── path (varchar 255, e.g. "/001/002")
├── level (tinyint, 1-4)
├── sort_order (int)
├── status (tinyint, 0=停用 1=启用)
├── is_system (tinyint, 0=普通 1=特殊分类)
├── create_by, create_time, update_by, update_time
```

### D2: 推荐策略

第一阶段采用规则+评分策略，不引入 ML 模型：
- 冷启动：热门频道 + 编辑精选 + 系统频道
- 已有行为：基于订阅相似度（订阅了 A 的人也订阅了 B）+ 分类偏好 + 活跃度评分
- 不感兴趣反馈：30 天内屏蔽同频道，降低同分类权重

推荐结果缓存到 `content_channel_recommendation_cache` 表，5 分钟刷新。

### D3: 搜索实现

第一阶段使用数据库能力：
- MySQL FULLTEXT 索引覆盖 name + description 字段
- 标签搜索使用 JOIN 查询
- 分类筛选使用 path LIKE 查询

后续可迁移到 Elasticsearch，当前设计预留搜索接口抽象层。

### D4: 榜单更新策略

使用定时任务每日凌晨计算：
- 热门榜：订阅数 × 0.4 + 近 7 日活跃度 × 0.3 + 近 7 日互动量 × 0.3
- 新晋榜：创建 30 天内，按订阅增长率 + 活跃度排序
- 系统榜：运营配置排序

结果写入 `content_channel_ranking_snapshot` 表，按日/周/月维度存储。

### D5: 可见性边界

所有发现入口（搜索、推荐、榜单、分类浏览、精选）统一调用 `ChannelVisibilityService` 过滤：
- 排除：私有频道、隐藏频道、冻结频道、限制公开曝光频道、未通过审核频道
- 仅对有权用户返回受限频道

## Risks / Trade-offs

| 风险 | 影响 | 缓解措施 |
|------|------|----------|
| 搜索性能不达标 | P99 > 200ms 影响用户体验 | FULLTEXT 索引 + 分页限制 20 条 + 结果缓存 |
| 推荐冷启动效果差 | 新用户推荐点击率低 | 热门+精选+系统频道兜底，观察数据后迭代 |
| 分类树过深 | 用户选择困难 | 限制 4 级，新增需运营审批 |
| 私有频道误曝光 | 隐私风险 | 统一可见性过滤服务，发布前验收重点 |
| 榜单数据延迟 | 用户看到过时数据 | 定时任务 + 增量更新，监控延迟指标 |

## File Structure

### 后端 Java 文件（`jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/`）

```
channel/
├── entity/
│   ├── ContentChannelCategory.java          # 分类实体
│   ├── ContentChannelTag.java               # 标签实体
│   ├── ContentChannelTagRelation.java       # 标签-内容关联
│   ├── ContentChannelRecommendationCache.java # 推荐缓存
│   ├── ContentChannelNotInterested.java     # 不感兴趣反馈
│   ├── ContentChannelRankingSnapshot.java   # 排行榜快照
│   └── ContentChannelEditorialPick.java     # 编辑精选
├── mapper/
│   ├── ContentChannelCategoryMapper.java
│   ├── ContentChannelTagMapper.java
│   ├── ContentChannelTagRelationMapper.java
│   ├── ContentChannelRecommendationCacheMapper.java
│   ├── ContentChannelNotInterestedMapper.java
│   ├── ContentChannelRankingSnapshotMapper.java
│   └── ContentChannelEditorialPickMapper.java
├── service/
│   ├── IContentChannelCategoryService.java
│   ├── ContentChannelCategoryServiceImpl.java
│   ├── IContentChannelTagService.java
│   ├── ContentChannelTagServiceImpl.java
│   ├── IContentChannelRecommendationService.java
│   ├── ContentChannelRecommendationServiceImpl.java
│   ├── IContentChannelRankingService.java
│   ├── ContentChannelRankingServiceImpl.java
│   ├── IContentChannelEditorialPickService.java
│   ├── ContentChannelEditorialPickServiceImpl.java
│   ├── IContentChannelSearchService.java
│   ├── ContentChannelSearchServiceImpl.java
│   ├── IContentChannelVisibilityService.java
│   └── ContentChannelVisibilityServiceImpl.java
├── biz/
│   ├── ContentChannelDiscoveryBiz.java      # 发现页聚合编排
│   └── ContentChannelCategoryBiz.java       # 分类树编排
├── controller/
│   ├── ContentChannelCategoryController.java # 分类管理 API
│   ├── ContentChannelTagController.java      # 标签管理 API
│   ├── ContentChannelRecommendationController.java # 推荐 API
│   ├── ContentChannelRankingController.java  # 排行榜 API
│   ├── ContentChannelEditorialPickController.java # 精选 API
│   ├── ContentChannelSearchController.java   # 搜索 API
│   └── ContentChannelBrowseController.java   # 分类浏览 API
├── req/
│   ├── query/
│   │   ├── ChannelCategoryQueryReq.java
│   │   ├── ChannelSearchQueryReq.java
│   │   ├── ChannelRankingQueryReq.java
│   │   └── ChannelBrowseQueryReq.java
│   ├── create/
│   │   ├── ChannelCategoryCreateReq.java
│   │   ├── ChannelTagCreateReq.java
│   │   └── ChannelEditorialPickCreateReq.java
│   └── update/
│       ├── ChannelCategoryUpdateReq.java
│       └── ChannelEditorialPickUpdateReq.java
├── vo/
│   ├── ChannelCategoryTreeVO.java
│   ├── ChannelTagVO.java
│   ├── ChannelRecommendationVO.java
│   ├── ChannelRankingItemVO.java
│   ├── ChannelEditorialPickVO.java
│   ├── ChannelSearchResultVO.java
│   └── ChannelBrowseItemVO.java
├── dto/
│   └── ChannelVisibilityDTO.java            # 可见性判断 DTO
├── task/
│   ├── ChannelRankingDailyTask.java         # 榜单每日更新
│   └── ChannelRecommendationRefreshTask.java # 推荐缓存刷新
└── constant/
    └── ChannelDiscoveryConstant.java        # 发现模块常量
```

### 测试文件（`jeecg-module-content/src/test/java/org/jeecg/modules/content/channel/`）

```
channel/
├── service/
│   ├── ContentChannelCategoryServiceTest.java
│   ├── ContentChannelTagServiceTest.java
│   ├── ContentChannelRecommendationServiceTest.java
│   ├── ContentChannelRankingServiceTest.java
│   ├── ContentChannelEditorialPickServiceTest.java
│   ├── ContentChannelSearchServiceTest.java
│   └── ContentChannelVisibilityServiceTest.java
├── biz/
│   ├── ContentChannelDiscoveryBizTest.java
│   └── ContentChannelCategoryBizTest.java
└── task/
    ├── ChannelRankingDailyTaskTest.java
    └── ChannelRecommendationRefreshTaskTest.java
```

### SQL 迁移

```
V3.9.1_63__channel_discovery_tables.sql
R3.9.1_63__channel_discovery_tables_rollback.sql
```

## Test Strategy

### 单元测试

| 测试文件 | 测试策略 |
|---------|---------|
| `ContentChannelCategoryServiceTest` | 分类 CRUD、层级校验（不超过 4 级）、path 生成、启停状态、特殊分类权限 |
| `ContentChannelTagServiceTest` | 标签 CRUD、名称校验（空/重复/超长）、删除后历史保留 |
| `ContentChannelRecommendationServiceTest` | 推荐生成、冷启动兜底、不感兴趣反馈过滤、可见性过滤、推荐理由 |
| `ContentChannelRankingServiceTest` | 榜单计算、排序正确性、更新延迟、可见性过滤 |
| `ContentChannelEditorialPickServiceTest` | 精选标记/取消、有效期过期、状态联动（频道变私有后精选失效） |
| `ContentChannelSearchServiceTest` | 关键词匹配、多条件筛选、排序、分页、可见性过滤、空状态 |
| `ContentChannelVisibilityServiceTest` | 各种不可见状态的过滤、权限判断 |

### 集成测试

| 测试文件 | 测试策略 |
|---------|---------|
| `ContentChannelDiscoveryBizTest` | 发现页聚合数据、推荐+榜单+精选的组合展示 |
| `ContentChannelCategoryBizTest` | 分类树构建、分类浏览的端到端流程 |

### 定时任务测试

| 测试文件 | 测试策略 |
|---------|---------|
| `ChannelRankingDailyTaskTest` | 榜单计算准确性、增量更新、异常处理 |
| `ChannelRecommendationRefreshTaskTest` | 推荐缓存刷新、过期清理 |

## Migration Plan

1. **数据库迁移**: 执行 `V3.9.1_63__channel_discovery_tables.sql` 创建 7 张新表
2. **后端部署**: 按依赖顺序部署：entity → mapper → service → biz → controller → task
3. **前端部署**: 分类管理 → 标签管理 → 推荐 → 榜单 → 精选 → 搜索 → 分类浏览
4. **验收重点**: 可见性边界测试、搜索性能测试、榜单更新延迟监控
5. **回滚策略**: 执行 rollback SQL，删除新增表和代码

## Open Questions

| 问题 | 影响 | 处理建议 |
|------|------|----------|
| 推荐模块是否需要区分首页/发现页/详情页不同口径？ | 推荐点击率、埋点口径 | 先统一口径，后续按场景拆分 |
| 编辑精选是否需要按地区/用户身份差异化展示？ | 运营配置复杂度 | 先全站统一，差异化进入后续版本 |
| 活跃度评分是否包含负反馈/举报扣分？ | 榜单公平性 | 与治理规则对齐后确认 |
| 分类停用后历史频道是否强制迁移？ | 分类管理、搜索筛选 | 默认保留历史归属，下次编辑时重新选择 |
