## Context

圈子功能已具备基础能力（EPIC-10）和内容互动能力（EPIC-11），但缺乏数据统计和推荐能力。管理员无法基于数据优化运营，用户发现圈子的方式单一（仅搜索）。本设计补齐这两个能力。

**当前状态**:
- 圈子基础 CRUD 已完成
- 内容发布和互动已完成
- 无数据统计能力
- 无推荐和榜单能力

**约束**:
- 依赖 EPIC-10 和 EPIC-11 的数据表结构
- 需兼容现有圈子权限体系
- 数据查询 P95 <1 秒
- 推荐列表 P95 <1 秒

## Goals / Non-Goals

**Goals:**
- 提供圈子运营数据统计面板
- 提供基于规则的圈子推荐能力
- 提供热门和新增圈子榜单
- 支持推荐来源追踪和转化统计

**Non-Goals:**
- 复杂机器学习推荐算法（初期使用规则推荐）
- 成长激励体系（EPIC-13）
- 付费功能

## Decisions

### Decision 1: 数据统计实现方式

**选择**: 定时任务聚合 + 缓存

**理由**:
- 数据面板不需要实时数据，30 分钟延迟可接受
- 避免每次查询实时计算，降低数据库压力
- 核心指标（成员数、发帖数、活跃度）可通过定时任务预聚合

**备选方案**:
- 实时查询：简单但性能差，数据量大时查询慢
- 流式计算：复杂度高，当前规模不需要

### Decision 2: 推荐算法选择

**选择**: 基于规则的推荐（成员数 + 活跃度 + 分类匹配）

**理由**:
- 初期用户行为数据不足，复杂算法效果不明显
- 规则推荐可解释性强，便于调试和优化
- 后续可逐步引入协同过滤等算法

**备选方案**:
- 协同过滤：需要大量用户行为数据，冷启动问题严重
- 内容推荐：需要完善的内容标签体系，当前不具备

### Decision 3: 榜单刷新策略

**选择**: 定时任务每小时刷新，结果缓存到 Redis

**理由**:
- 榜单数据不需要实时更新，每小时刷新可接受
- 缓存到 Redis 可保证查询性能
- 减少数据库查询压力

**备选方案**:
- 实时排名：性能开销大，当前规模不需要
- 每日刷新：时效性不够

### Decision 4: 推荐来源追踪

**选择**: 推荐列表返回时携带来源标识，用户点击和加入时上报

**理由**:
- 轻量级实现，不需要额外存储推荐会话
- 可统计推荐曝光、点击和加入转化
- 便于后续优化推荐算法

**备选方案**:
- 推荐会话存储：增加存储复杂度，当前不需要

## Risks / Trade-offs

- **风险**: 推荐冷启动 → 初期使用热门榜单兜底，逐步积累用户行为数据
- **风险**: 数据统计性能瓶颈 → 定时任务预聚合 + 缓存，控制查询复杂度
- **风险**: 推荐多样性不足 → 单次推荐列表中同一分类占比不超过 60%
- **权衡**: 数据时效性 vs 查询性能 → 选择 30 分钟延迟换取查询性能

## File Structure

```
jeecg-boot-module/jeecg-module-content/
├── src/main/java/org/jeecg/modules/content/
│   ├── controller/
│   │   ├── CircleDataController.java          # 数据统计接口
│   │   ├── CircleRecommendController.java     # 推荐接口
│   │   └── CircleRankingController.java       # 榜单接口
│   ├── service/
│   │   ├── CircleDataService.java             # 数据统计服务
│   │   ├── CircleRecommendService.java        # 推荐服务
│   │   └── CircleRankingService.java          # 榜单服务
│   ├── mapper/
│   │   ├── CircleDataMapper.java              # 数据统计 Mapper
│   │   └── CircleRecommendMapper.java         # 推荐 Mapper
│   └── entity/
│       ├── CircleDataStatistics.java          # 统计数据实体
│       └── CircleRecommendSource.java         # 推荐来源实体
├── src/main/resources/mapper/content/
│   ├── CircleDataMapper.xml                   # 统计 SQL
│   └── CircleRecommendMapper.xml              # 推荐 SQL
└── src/test/java/org/jeecg/modules/content/
    ├── controller/
    │   ├── CircleDataControllerTest.java
    │   ├── CircleRecommendControllerTest.java
    │   └── CircleRankingControllerTest.java
    └── service/
        ├── CircleDataServiceTest.java
        ├── CircleRecommendServiceTest.java
        └── CircleRankingServiceTest.java
```

## Test Strategy

- **CircleDataControllerTest**: 测试数据统计接口的权限控制、时间范围筛选、数据导出
- **CircleRecommendControllerTest**: 测试推荐接口的返回格式、多样性控制、来源标识
- **CircleRankingControllerTest**: 测试榜单接口的排序逻辑、空状态处理
- **CircleDataServiceTest**: 测试数据聚合逻辑、缓存策略
- **CircleRecommendServiceTest**: 测试推荐算法、分类多样性控制
- **CircleRankingServiceTest**: 测试榜单计算逻辑、定时刷新

## Migration Plan

**部署顺序**:
1. 执行数据库迁移脚本（新增推荐来源追踪表）
2. 部署后端服务
3. 部署前端页面
4. 启动定时任务（数据聚合、榜单刷新）

**回滚策略**:
- 禁用定时任务
- 回滚后端服务
- 回滚前端页面

**验收条件**:
- 管理员可查看圈子数据统计
- 用户可看到推荐圈子和榜单
- 推荐来源可追踪

## Open Questions

- 推荐算法的具体权重配置需要根据实际数据调整
- 数据统计的历史数据保留策略需要明确
