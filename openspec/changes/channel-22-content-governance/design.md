## Context

频道基础架构（EPIC-20）已建立频道创建、所有权和基础数据模型。频道隐私与成员管理（EPIC-21）已建立成员角色、禁言、黑名单和隐私规则。内容发布系统已有基础的内容创建、状态管理和作者身份能力。

当前缺少频道级的内容发布权限控制、审核流程、治理操作和公告机制。本设计在此基础上建立完整的内容发布与治理链路。

**约束**:
- 遵循 jeecg-module-content 模块分层规范：controller / biz / service / mapper / entity / req / vo / dto
- biz 层仅用于多表、跨聚合编排；单表逻辑留在 service
- 表结构变更需同步更新 entity、mapper、Flyway SQL 和测试
- 定时发布依赖调度框架（XXL-Job 或 Spring Scheduled）

## Goals / Non-Goals

**Goals:**
- 建立频道发布权限模型，支持四种模式差异化控制
- 实现待审区审核流程，支持逐条/批量审核和结果通知
- 支持定时发布，到达时间重新校验所有规则
- 实现发布限额与防灌水策略
- 建立频道内容治理能力（置顶、精华、删除、回收站、移出、编辑协助）
- 实现频道公告管理
- 支持已发布内容添加到频道

**Non-Goals:**
- 不涉及频道创建和所有权管理
- 不涉及频道隐私和成员管理
- 不涉及平台级内容安全审核
- 不涉及推荐发现和数据统计

## Decisions

### Decision 1: 发布权限模型存储方式

**选择**: 在频道配置表中新增 `publish_permission` 字段枚举值，配合独立的发布限额配置表。

**理由**: 发布权限是频道级配置，与频道基础配置同表存储减少查询开销。限额规则字段较多（每小时上限、每日上限、字数下限），独立表更灵活。

**备选方案**: 全部放入频道配置表 → 字段膨胀，限额扩展性差。

### Decision 2: 待审区数据模型

**选择**: 新建 `channel_content_review` 表，记录待审内容关联、提交者、目标频道、提交时间、审核状态、审核人、审核时间和拒绝原因。

**理由**: 待审区是独立的业务域，与频道内容关联表分离，审核流程的字段（审核人、时间、原因）不应污染内容关联表。

**备选方案**: 在频道内容关联表中增加审核状态字段 → 会导致关联表承载过多职责，查询复杂度上升。

### Decision 3: 定时发布实现机制

**选择**: 新建 `channel_scheduled_publish` 表记录定时发布任务，使用调度框架定时扫描到期任务，到达时重新校验权限、禁言、限额后执行发布或入审。

**理由**: 定时发布需要持久化，调度框架负责触发。到达时必须重新校验（EPIC-22 明确要求），不能简单延迟写入。

**备选方案**: 使用消息队列延迟消息 → 延迟精度受限，重新校验时机不明确。

### Decision 4: 内容治理操作记录

**选择**: 新建 `channel_governance_log` 表，记录所有治理操作（置顶、精华、删除、恢复、移出、编辑协助、公告变更），字段包含操作者、对象、动作、时间、原因和结果。

**理由**: 治理日志是审计和追溯的核心需求，独立表便于查询和保留策略管理（180天保留）。

**备选方案**: 使用通用操作日志表 → 字段通用化导致查询和分析困难。

### Decision 5: 回收站实现

**选择**: 新建 `channel_recycle_bin` 表，记录被删除内容的关联信息、删除人、删除时间、删除原因。30天保留期后不再支持频道级恢复。

**理由**: 回收站是频道级能力，仅影响内容在该频道的展示，不影响内容在其他频道或作者主页。独立表便于保留期管理和恢复操作。

**备选方案**: 软删除标记在关联表上 → 混合正常关联和已删除关联，查询逻辑复杂。

### Decision 6: 多频道发布事务策略

**选择**: 多频道发布按目标频道逐一校验和写入，使用数据库事务保证单个频道的操作原子性，整体操作不使用大事务包裹。

**理由**: 多频道发布允许部分成功、部分失败。大事务包裹会导致一个频道失败全部回滚，不符合业务要求。

**备选方案**: 全部成功或全部回滚 → 不符合业务要求，用户期望逐频道结果反馈。

## Risks / Trade-offs

| 风险 | 影响 | 缓解措施 |
|------|------|----------|
| 多频道发布部分失败增加用户理解成本 | 用户误判发布结果 | 逐频道返回明确结果状态，提供重试入口 |
| 定时发布到达时规则已变 | 发布结果与用户预期不符 | 到达时重新校验并通知用户最终结果 |
| 编辑协助边界不清 | 作者权益争议 | 限定可修改字段（标题、标签、摘要、错别字），记录修订历史 |
| 回收站与平台级删除边界 | 恢复失败争议 | 明确频道级删除仅影响频道展示，平台级删除不由频道回收站恢复 |
| 大量定时发布任务同时到期 | 调度压力 | 分批处理、限流、监控调度队列深度 |

## File Structure

```
jeecg-boot/jeecg-boot-module/jeecg-module-content/
├── src/main/java/.../content/
│   ├── controller/channel/
│   │   ├── ChannelPublishController.java
│   │   ├── ChannelReviewController.java
│   │   ├── ChannelGovernanceController.java
│   │   └── ChannelAnnouncementController.java
│   ├── biz/channel/
│   │   ├── ChannelPublishBiz.java
│   │   ├── ChannelReviewBiz.java
│   │   ├── ChannelGovernanceBiz.java
│   │   └── ChannelAnnouncementBiz.java
│   ├── service/channel/
│   │   ├── ChannelContentPublishService.java
│   │   ├── ChannelContentReviewService.java
│   │   ├── ChannelScheduledPublishService.java
│   │   ├── ChannelPublishLimitService.java
│   │   ├── ChannelContentPinService.java
│   │   ├── ChannelContentFeatureService.java
│   │   ├── ChannelRecycleBinService.java
│   │   ├── ChannelContentMoveService.java
│   │   ├── ChannelEditAssistService.java
│   │   ├── ChannelGovernanceLogService.java
│   │   └── ChannelAnnouncementService.java
│   ├── mapper/channel/
│   │   ├── ChannelContentPublishMapper.java
│   │   ├── ChannelContentReviewMapper.java
│   │   ├── ChannelScheduledPublishMapper.java
│   │   ├── ChannelPublishLimitMapper.java
│   │   ├── ChannelRecycleBinMapper.java
│   │   ├── ChannelGovernanceLogMapper.java
│   │   └── ChannelAnnouncementMapper.java
│   ├── entity/channel/
│   │   ├── ChannelContentPublish.java
│   │   ├── ChannelContentReview.java
│   │   ├── ChannelScheduledPublish.java
│   │   ├── ChannelPublishLimit.java
│   │   ├── ChannelRecycleBin.java
│   │   ├── ChannelGovernanceLog.java
│   │   └── ChannelAnnouncement.java
│   ├── req/channel/
│   │   ├── publish/
│   │   ├── review/
│   │   ├── governance/
│   │   └── announcement/
│   └── vo/channel/
│       ├── publish/
│       ├── review/
│       ├── governance/
│       └── announcement/
├── src/test/java/.../content/
│   ├── biz/channel/
│   │   ├── ChannelPublishBizTest.java
│   │   ├── ChannelReviewBizTest.java
│   │   ├── ChannelGovernanceBizTest.java
│   │   └── ChannelAnnouncementBizTest.java
│   └── service/channel/
│       ├── ChannelContentPublishServiceTest.java
│       ├── ChannelContentReviewServiceTest.java
│       ├── ChannelScheduledPublishServiceTest.java
│       ├── ChannelPublishLimitServiceTest.java
│       ├── ChannelRecycleBinServiceTest.java
│       └── ChannelAnnouncementServiceTest.java
└── src/main/resources/db/migration/
    └── V{version}__channel_content_governance.sql
```

## Test Strategy

**Service 层单元测试**:
- `ChannelContentPublishServiceTest`: 发布权限校验逻辑（四种模式）、多频道发布逐频道校验
- `ChannelContentReviewServiceTest`: 审核状态流转、通过/拒绝逻辑、批量审核
- `ChannelScheduledPublishServiceTest`: 定时发布到达校验、规则变更处理
- `ChannelPublishLimitServiceTest`: 每小时/每日限额计算、字数下限校验
- `ChannelRecycleBinServiceTest`: 删除进入回收站、30天过期、恢复逻辑
- `ChannelAnnouncementServiceTest`: 公告 CRUD、富文本安全过滤

**Biz 层集成测试**:
- `ChannelPublishBizTest`: 发布完整流程（权限校验 → 限额校验 → 写入/入审 → 通知）
- `ChannelReviewBizTest`: 审核完整流程（待审列表 → 审核操作 → 结果通知 → 内容状态变更）
- `ChannelGovernanceBizTest`: 治理操作完整流程（置顶/精华/删除/恢复/移出/编辑协助 → 日志记录）
- `ChannelAnnouncementBizTest`: 公告管理完整流程

**测试覆盖目标**: 核心业务逻辑 >= 80% 行覆盖率

## Migration Plan

1. **数据库迁移**: 执行 Flyway SQL 创建新表（频道内容发布关联、待审区、定时发布、限额配置、回收站、治理日志、公告）
2. **后端部署**: 按依赖顺序部署 Service → Biz → Controller
3. **前端部署**: 部署频道选择组件、待审区、治理页面、公告组件
4. **数据初始化**: 为已有频道设置默认发布权限（所有成员可发布）
5. **回滚策略**: 新表独立，回滚不影响已有功能；前端可通过功能开关隐藏入口

## Open Questions

| 问题 | 影响范围 | 建议负责人 |
|------|----------|-----------|
| 单篇内容最多可同步到多少个频道，是否按内容类型或用户等级区分？ | 发布限额策略 | 产品/运营 |
| 待审区内容超过多长时间未处理需要提醒管理员？ | 审核体验 | 产品/社区治理 |
| 编辑协助是否需要作者确认后才生效？ | 作者权益 | 产品/法务 |
| 定时发布使用哪个调度框架？ | 技术实现 | 后端架构 |
