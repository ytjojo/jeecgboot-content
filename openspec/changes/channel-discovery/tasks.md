## 1. 数据库迁移与基础实体

- [ ] 1.1 创建 Flyway 迁移脚本 `V3.9.1_63__channel_discovery_tables.sql`，包含 7 张表：content_channel_category、content_channel_tag、content_channel_tag_relation、content_channel_recommendation_cache、content_channel_not_interested、content_channel_ranking_snapshot、content_channel_editorial_pick
- [ ] 1.2 创建回滚脚本 `R3.9.1_63__channel_discovery_tables_rollback.sql`
- [ ] 1.3 创建实体类 `ContentChannelCategory`，继承 JeecgEntity，包含 parent_id、name、path、level、sort_order、status、is_system 字段
- [ ] 1.4 创建实体类 `ContentChannelTag`，包含 channel_id、name、status 字段
- [ ] 1.5 创建实体类 `ContentChannelTagRelation`，包含 tag_id、content_id、content_type 字段
- [ ] 1.6 创建实体类 `ContentChannelRecommendationCache`，包含 user_id、channel_id、ranking_score、recommendation_rule、recommendation_reason、recommendation_status 字段
- [ ] 1.7 创建实体类 `ContentChannelNotInterested`，包含 user_id、channel_id、expire_time 字段
- [ ] 1.8 创建实体类 `ContentChannelRankingSnapshot`，包含 channel_id、ranking_type、dimension、rank_position、score、snapshot_date 字段
- [ ] 1.9 创建实体类 `ContentChannelEditorialPick`，包含 channel_id、recommendation_text、start_time、end_time、status、operator_id 字段

## 2. Mapper 层

- [ ] 2.1 创建 `ContentChannelCategoryMapper`，包含树查询、路径查询方法
- [ ] 2.2 创建 `ContentChannelTagMapper`，包含按频道查询标签方法
- [ ] 2.3 创建 `ContentChannelTagRelationMapper`，包含按标签查询内容、按内容查询标签方法
- [ ] 2.4 创建 `ContentChannelRecommendationCacheMapper`，包含按用户查询推荐、批量更新状态方法
- [ ] 2.5 创建 `ContentChannelNotInterestedMapper`，包含按用户查询屏蔽列表、过期清理方法
- [ ] 2.6 创建 `ContentChannelRankingSnapshotMapper`，包含按类型和维度查询榜单方法
- [ ] 2.7 创建 `ContentChannelEditorialPickMapper`，包含查询有效精选方法

## 3. 分类体系（channel-taxonomy）

- [ ] 3.1 创建 `IContentChannelCategoryService` 接口和 `ContentChannelCategoryServiceImpl` 实现，包含 CRUD、层级校验、path 生成、启停逻辑
- [ ] 3.2 创建 `ContentChannelCategoryBiz`，实现分类树构建、停用影响评估、分类迁移逻辑
- [ ] 3.3 创建请求对象：`ChannelCategoryCreateReq`、`ChannelCategoryUpdateReq`、`ChannelCategoryQueryReq`
- [ ] 3.4 创建响应对象：`ChannelCategoryTreeVO`
- [ ] 3.5 创建 `ContentChannelCategoryController`，实现分类树查询、创建、编辑、停用 API
- [ ] 3.6 编写 `ContentChannelCategoryServiceTest`，覆盖层级校验、path 生成、启停逻辑、特殊分类权限
- [ ] 3.7 编写 `ContentChannelCategoryBizTest`，覆盖分类树构建和停用影响评估

## 4. 频道标签（channel-tags）

- [ ] 4.1 创建 `IContentChannelTagService` 接口和 `ContentChannelTagServiceImpl` 实现，包含 CRUD、名称校验、删除后历史保留逻辑
- [ ] 4.2 创建请求对象：`ChannelTagCreateReq`
- [ ] 4.3 创建响应对象：`ChannelTagVO`
- [ ] 4.4 创建 `ContentChannelTagController`，实现标签 CRUD 和频道内标签查询 API
- [ ] 4.5 编写 `ContentChannelTagServiceTest`，覆盖名称校验（空/重复/超长）、删除后历史保留

## 5. 可见性服务（公共依赖）

- [ ] 5.1 创建 `IContentChannelVisibilityService` 接口和 `ContentChannelVisibilityServiceImpl` 实现，统一过滤私有/隐藏/冻结/限制公开曝光/未审核频道
- [ ] 5.2 创建 `ChannelVisibilityDTO`，封装频道可见性判断参数
- [ ] 5.3 编写 `ContentChannelVisibilityServiceTest`，覆盖各种不可见状态的过滤

## 6. 频道推荐（channel-recommendations）

- [ ] 6.1 创建 `IContentChannelRecommendationService` 接口和 `ContentChannelRecommendationServiceImpl` 实现，包含推荐生成、冷启动、不感兴趣反馈逻辑
- [ ] 6.2 创建请求对象：`ChannelRecommendationQueryReq`（在 query 目录下）
- [ ] 6.3 创建响应对象：`ChannelRecommendationVO`，包含推荐理由和频道信息
- [ ] 6.4 创建 `ContentChannelRecommendationController`，实现推荐列表查询和不感兴趣反馈 API
- [ ] 6.5 创建 `ChannelRecommendationRefreshTask` 定时任务，刷新推荐缓存
- [ ] 6.6 编写 `ContentChannelRecommendationServiceTest`，覆盖推荐生成、冷启动、不感兴趣反馈、可见性过滤

## 7. 频道排行榜（channel-rankings）

- [ ] 7.1 创建 `IContentChannelRankingService` 接口和 `ContentChannelRankingServiceImpl` 实现，包含榜单计算、排序、更新逻辑
- [ ] 7.2 创建请求对象：`ChannelRankingQueryReq`
- [ ] 7.3 创建响应对象：`ChannelRankingItemVO`
- [ ] 7.4 创建 `ContentChannelRankingController`，实现热门/新晋/系统榜单查询 API
- [ ] 7.5 创建 `ChannelRankingDailyTask` 定时任务，每日计算榜单快照
- [ ] 7.6 编写 `ContentChannelRankingServiceTest`，覆盖榜单计算准确性、排序规则、可见性过滤
- [ ] 7.7 编写 `ChannelRankingDailyTaskTest`，覆盖每日更新逻辑

## 8. 编辑精选（channel-editorial-picks）

- [ ] 8.1 创建 `IContentChannelEditorialPickService` 接口和 `ContentChannelEditorialPickServiceImpl` 实现，包含精选标记、有效期管理、状态联动逻辑
- [ ] 8.2 创建请求对象：`ChannelEditorialPickCreateReq`、`ChannelEditorialPickUpdateReq`
- [ ] 8.3 创建响应对象：`ChannelEditorialPickVO`
- [ ] 8.4 创建 `ContentChannelEditorialPickController`，实现精选 CRUD 和查询 API
- [ ] 8.5 编写 `ContentChannelEditorialPickServiceTest`，覆盖精选标记/取消、有效期过期、状态联动

## 9. 分类浏览（channel-category-browse）

- [ ] 9.1 创建请求对象：`ChannelBrowseQueryReq`，包含分类 ID、排序方式、分页参数
- [ ] 9.2 创建响应对象：`ChannelBrowseItemVO`，包含频道卡片信息
- [ ] 9.3 创建 `ContentChannelBrowseController`，实现分类浏览 API，调用可见性服务过滤
- [ ] 9.4 编写分类浏览集成测试，覆盖分类筛选、排序、分页、可见性过滤

## 10. 频道搜索（channel-search）

- [ ] 10.1 创建 `IContentChannelSearchService` 接口和 `ContentChannelSearchServiceImpl` 实现，包含关键词搜索、多条件筛选、排序逻辑
- [ ] 10.2 创建请求对象：`ChannelSearchQueryReq`，包含关键词、类型、分类、排序参数
- [ ] 10.3 创建响应对象：`ChannelSearchResultVO`，包含匹配原因和频道信息
- [ ] 10.4 创建 `ContentChannelSearchController`，实现搜索 API
- [ ] 10.5 编写 `ContentChannelSearchServiceTest`，覆盖关键词匹配、多条件筛选、排序、分页、可见性过滤、空状态

## 11. 发现页聚合编排

- [ ] 11.1 创建 `ContentChannelDiscoveryBiz`，聚合推荐、榜单、精选数据，供发现页使用
- [ ] 11.2 编写 `ContentChannelDiscoveryBizTest`，覆盖发现页聚合数据的正确性

## 12. 验证

- [ ] 12.1 执行所有单元测试，确保通过率 100%
- [ ] 12.2 执行 Flyway 迁移，验证表结构正确
- [ ] 12.3 验证搜索 P99 响应时间 <= 200ms
- [ ] 12.4 验证可见性边界：私有/隐藏/冻结/限制公开曝光频道不进入任何公开发现入口
- [ ] 12.5 验证分类层级不超过 4 级、副分类不超过 3 个
- [ ] 12.6 验证标签名称校验（空/重复/超长）
