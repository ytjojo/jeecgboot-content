## Context

频道模块（jeecg-module-content）当前仅有空的 channel 包占位，无任何实现代码。EPIC-20 至 EPIC-23 定义了频道基础架构、成员权限、内容治理和发现能力，本 EPIC-24 在其之上建立数据统计看板和完整生命周期管理。

**现有约束**:
- 分层架构: controller / biz / service / mapper / entity / req / vo / dto
- biz 层仅用于跨聚合、跨域编排；单表逻辑留在 service
- 数据库: snake_case 命名，无外键，Flyway 迁移，必须有 id/created_time/updated_time
- API: `/api/v1/{resources}`，Result<T> 返回
- DI: 必须用 @Resource，不用 @Autowired
- Lombok: @Data 用于 entity/DTO，@Builder 用于 DTO/VO（不用于 entity）

**前置依赖**:
- EPIC-20: 频道基础实体（channel 表、频道类型、关键字段）
- EPIC-21: 成员管理、订阅关系、组织管理员权限
- EPIC-22: 内容发布、内容状态、互动事件
- EPIC-23: 公开发现入口和可见性规则

## Goals / Non-Goals

**Goals:**
- 建立频道数据统计看板（核心指标、互动数据、热门内容、用户分析）
- 实现 Excel/CSV 数据导出能力
- 实现频道审核流程（创建审核、关键字段修改审核）
- 实现频道冻结/解冻只读治理状态
- 实现频道归档（不活跃识别、自动归档、手动归档）
- 实现频道合并（源频道内容和订阅关系迁移）
- 实现违规处理（限制推荐、强制隐藏、永久关闭）
- 实现不活跃频道自动识别与处置
- 实现治理审计日志与申诉入口

**Non-Goals:**
- 频道创建基础流程（EPIC-20 范围）
- 成员和加入规则（EPIC-21 范围）
- 内容发布与内容级审核（EPIC-22 范围）
- 推荐发现与搜索排序（EPIC-23 范围）
- 底层数据采集方案实现
- 审核系统和通知系统基础设施建设

## Decisions

### D1: 统计数据存储方案

**选择**: 预聚合汇总表 + 定时任务刷新

**理由**:
- 频道看板查询 P95 <= 1 秒，实时计算不现实
- 数据新鲜度 P99 <= 5 分钟，允许分钟级延迟
- 汇总表可按时间维度（日/周/月）预计算，减少查询时计算量

**替代方案**:
- 实时流计算（Flink/Spark）: 过重，当前规模不需要
- 物化视图: MySQL 不原生支持，需额外中间件

### D2: 生命周期状态机实现

**选择**: 状态枚举 + 状态转换规则表 + 状态机引擎

**理由**:
- 8 种状态（PendingReview/Active/ReadonlyFrozen/Hidden/Archived/Merged/Closed/Deleted），流转规则明确
- 状态转换规则表可配置化，便于后续扩展
- 每次状态变更记录审计日志

**替代方案**:
- 硬编码 if-else: 不可维护，规则变更需改代码
- 第三方状态机框架（Spring StateMachine）: 引入额外依赖，当前复杂度不需要

### D3: 数据导出架构

**选择**: 异步导出 + 文件存储 + 下载通知

**理由**:
- 超过 10,000 行需异步处理，避免请求超时
- 导出文件需限时下载（建议 7 天有效期）
- 导出记录需可追踪

**替代方案**:
- 同步导出: 超过 10,000 行会超时
- 流式下载: 用户体验差，无法支持 Excel 格式

### D4: 频道合并实现

**选择**: 批量数据迁移 + 源频道状态标记

**理由**:
- 合并涉及内容、订阅关系、历史链接迁移
- 需要在事务内完成，保证数据一致性
- 合并后源频道进入 Merged 状态，展示目标频道入口

**替代方案**:
- 逻辑关联（不迁移数据）: 查询复杂，历史数据不一致
- 定时任务批量迁移: 无法保证原子性

### D5: 不活跃频道识别

**选择**: 定时任务扫描 + 阈值配置

**理由**:
- 每日扫描一次，识别连续 6 个月无活动的频道
- 阈值可配置（6 个月），便于调整策略
- 个人频道自动归档，组织频道通知管理员

**替代方案**:
- 事件驱动实时检测: 增加系统复杂度
- 人工巡查: 效率低，无法规模化

## Risks / Trade-offs

| 风险 | 影响 | 缓解方式 |
|------|------|----------|
| 统计口径不一致导致频道主误判 | 看板可信度下降 | 展示统计周期、更新时间和指标说明；导出文件保留筛选条件 |
| 频道合并对内容、订阅和历史链接影响复杂 | 用户找不到原频道 | 合并前展示影响范围；合并后源频道展示目标频道入口并通知订阅者 |
| 高风险治理动作缺少二次确认或审计 | 误操作难以追责 | 冻结、隐藏、关闭、归档、合并、删除必须二次确认并记录审计日志 |
| 私有或受限频道在统计、导出或治理通知中越权暴露 | 数据安全风险 | 看板、导出和通知按操作者权限过滤字段和对象 |
| 不活跃频道误判 | 正常频道被误归档 | 保留提醒期（1 个月），支持频道主恢复活跃状态解除风险 |

## File Structure

```
jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/
├── controller/
│   ├── ChannelStatsController.java          # 统计看板 API
│   ├── ChannelExportController.java         # 数据导出 API
│   ├── ChannelReviewController.java         # 审核队列 API
│   └── ChannelLifecycleController.java      # 生命周期操作 API（冻结/解冻/归档/合并/关闭）
├── biz/
│   ├── ChannelStatsBiz.java                 # 统计数据聚合编排
│   ├── ChannelExportBiz.java                # 导出任务编排
│   ├── ChannelLifecycleBiz.java             # 生命周期状态变更编排（跨聚合）
│   └── ChannelMergeBiz.java                 # 合并流程编排（跨聚合）
├── service/
│   ├── ChannelStatsService.java             # 统计汇总表 CRUD
│   ├── ChannelStatsServiceImpl.java
│   ├── ChannelExportTaskService.java        # 导出任务表 CRUD
│   ├── ChannelExportTaskServiceImpl.java
│   ├── ChannelReviewService.java            # 审核记录表 CRUD
│   ├── ChannelReviewServiceImpl.java
│   ├── ChannelLifecycleLogService.java      # 生命周期变更日志 CRUD
│   ├── ChannelLifecycleLogServiceImpl.java
│   ├── ChannelAppealService.java            # 申诉记录表 CRUD
│   └── ChannelAppealServiceImpl.java
├── mapper/
│   ├── ChannelStatsMapper.java
│   ├── ChannelStatsMapper.xml
│   ├── ChannelExportTaskMapper.java
│   ├── ChannelReviewMapper.java
│   ├── ChannelLifecycleLogMapper.java
│   └── ChannelAppealMapper.java
├── entity/
│   ├── ChannelStats.java                    # 统计汇总实体
│   ├── ChannelExportTask.java               # 导出任务实体
│   ├── ChannelReview.java                   # 审核记录实体
│   ├── ChannelLifecycleLog.java             # 生命周期变更日志实体
│   └── ChannelAppeal.java                   # 申诉记录实体
├── enums/
│   ├── ChannelLifecycleStatus.java          # 生命周期状态枚举
│   ├── ChannelReviewStatus.java             # 审核状态枚举
│   ├── ChannelExportStatus.java             # 导出状态枚举
│   ├── ChannelAppealStatus.java             # 申诉状态枚举
│   └── ChannelViolationType.java            # 违规处理类型枚举
├── req/
│   ├── ChannelStatsQueryReq.java            # 统计查询请求
│   ├── ChannelExportReq.java                # 导出请求
│   ├── ChannelReviewActionReq.java          # 审核操作请求
│   ├── ChannelLifecycleActionReq.java       # 生命周期操作请求
│   └── ChannelAppealReq.java                # 申诉请求
├── vo/
│   ├── ChannelStatsVO.java                  # 统计数据响应
│   ├── ChannelTrendVO.java                  # 趋势数据响应
│   ├── ChannelHotContentVO.java             # 热门内容响应
│   ├── ChannelUserAnalysisVO.java           # 用户分析响应
│   ├── ChannelExportTaskVO.java             # 导出任务响应
│   ├── ChannelReviewVO.java                 # 审核记录响应
│   └── ChannelLifecycleLogVO.java           # 生命周期日志响应
└── constant/
    └── ChannelStatsConstant.java            # 统计相关常量

src/main/resources/flyway/sql/mysql/
└── V{version}__channel_lifecycle_stats.sql  # 迁移脚本

src/test/java/org/jeecg/modules/content/channel/
├── biz/
│   ├── ChannelStatsBizTest.java
│   ├── ChannelExportBizTest.java
│   ├── ChannelLifecycleBizTest.java
│   └── ChannelMergeBizTest.java
└── service/
    ├── ChannelStatsServiceTest.java
    ├── ChannelLifecycleLogServiceTest.java
    └── ChannelAppealServiceTest.java
```

## Test Strategy

| 测试文件 | 测试策略 |
|----------|----------|
| ChannelStatsBizTest | 验证统计数据聚合逻辑：核心指标计算、趋势数据生成、热门内容排序、用户分析计算 |
| ChannelExportBizTest | 验证导出任务创建、异步处理、文件生成、权限校验、超时处理 |
| ChannelLifecycleBizTest | 验证状态机流转：合法流转通过、非法流转拦截、审计日志记录、通知触发 |
| ChannelMergeBizTest | 验证合并流程：目标频道校验、内容迁移、订阅关系迁移、源频道状态变更、审计记录 |
| ChannelStatsServiceTest | 验证单表 CRUD 和查询条件构建 |
| ChannelLifecycleLogServiceTest | 验证日志记录完整性 |
| ChannelAppealServiceTest | 验证申诉记录 CRUD 和状态流转 |

## Migration Plan

**部署顺序**:
1. 执行 Flyway 迁移脚本，创建统计汇总表、导出任务表、审核记录表、生命周期变更日志表、申诉记录表
2. 部署后端服务
3. 部署前端页面
4. 配置定时任务（统计刷新、不活跃扫描）

**回滚策略**:
- 后端: 回滚代码版本
- 数据库: Flyway 回滚脚本（删除新增表）
- 前端: 回滚前端版本

**验收条件**:
- 核心指标看板可用，数据延迟 P99 <= 5 分钟
- 常规查询 P95 <= 1 秒
- Excel/CSV 导出功能可用
- 生命周期状态流转正确
- 审计日志完整

## Open Questions

| 问题 | 影响范围 | 处理建议 |
|------|----------|----------|
| 归档频道是否允许频道主申请恢复？ | 归档流程、用户体验 | 需产品/运营确认恢复策略 |
| 数据导出文件下载有效期和保留期限？ | 数据安全、存储 | 需产品/安全/数据确认保留策略 |
| 频道合并后历史统计是否合并？ | 数据看板、运营复盘 | 需产品/数据确认统计口径 |
| 永久关闭是否有特权恢复流程？ | 合规治理、申诉 | 需产品/法务/运营确认例外权限 |
