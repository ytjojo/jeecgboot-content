## 1. 数据库设计与迁移

- [ ] 1.1 创建推荐来源追踪表 circle_recommend_source（字段：id, circle_id, user_id, source_type, source_id, click_time, join_time）
- [ ] 1.2 创建统计数据缓存表 circle_data_statistics（字段：id, circle_id, stat_date, member_count, post_count, active_count, new_member_count, new_post_count）
- [ ] 1.3 编写数据库迁移脚本并验证表结构

## 2. 数据统计后端实现

- [ ] 2.1 创建 CircleDataStatistics 实体类
- [ ] 2.2 创建 CircleDataMapper 和 XML（成员统计、发帖统计、活跃度统计查询）
- [ ] 2.3 实现 CircleDataService（数据聚合、时间范围筛选、缓存策略）
- [ ] 2.4 实现 CircleDataController（GET /api/circle/{circleId}/data/statistics, GET /api/circle/{circleId}/data/export）
- [ ] 2.5 编写数据统计单元测试（CircleDataServiceTest, CircleDataControllerTest）
- [ ] 2.6 实现定时任务（每 30 分钟聚合统计数据）

## 3. 推荐后端实现

- [ ] 3.1 创建 CircleRecommendSource 实体类
- [ ] 3.2 创建 CircleRecommendMapper 和 XML（基于规则的推荐查询）
- [ ] 3.3 实现 CircleRecommendService（推荐算法、分类多样性控制、来源追踪）
- [ ] 3.4 实现 CircleRecommendController（GET /api/circle/recommend）
- [ ] 3.5 编写推荐单元测试（CircleRecommendServiceTest, CircleRecommendControllerTest）

## 4. 榜单后端实现

- [ ] 4.1 实现 CircleRankingService（热门榜单计算、新增榜单计算、Redis 缓存）
- [ ] 4.2 实现 CircleRankingController（GET /api/circle/ranking/hot, GET /api/circle/ranking/new）
- [ ] 4.3 实现定时任务（每小时刷新榜单）
- [ ] 4.4 编写榜单单元测试（CircleRankingServiceTest, CircleRankingControllerTest）

## 5. 前端实现

- [ ] 5.1 实现圈子数据统计页面（时间范围选择、图表展示、导出按钮）
- [ ] 5.2 实现推荐列表展示（推荐圈子卡片、来源标识）
- [ ] 5.3 实现榜单展示（热门榜单、新增榜单）
- [ ] 5.4 实现空状态、错误状态和权限不足状态展示

## 6. 集成测试与验证

- [ ] 6.1 端到端测试：管理员查看数据统计流程
- [ ] 6.2 端到端测试：用户通过推荐加入圈子流程
- [ ] 6.3 端到端测试：榜单展示和刷新流程
- [ ] 6.4 性能测试：数据查询和推荐接口响应时间
