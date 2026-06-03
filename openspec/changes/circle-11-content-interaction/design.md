## Context

圈子基础能力（circle-core）的 PRD 已完成但代码尚未构建，circle 模块目录为空占位符。本 change 在 circle-core 基础上增加内容管理增强、互动功能和审核机制。

现有内容模块采用分层架构：entity → mapper → service → service/impl → biz → controller，实体继承 `JeecgEntity`（提供 id/createBy/createTime/updateBy/updateTime）。审核日志采用领域专用实体（如 `UserStatusAuditLog`），不继承 `JeecgEntity`。

通知系统当前为 stub 实现（`ContentNotificationServiceImpl`），仅写入 `content_notification_audit_log` 表，无真实推送。

## Goals / Non-Goals

**Goals:**
- 内容置顶/精华管理，置顶内容始终排在非置顶内容之前
- 圈子公告发布与展示，同一时间仅一条生效公告
- 圈子内 @成员，异步通知不阻塞主流程
- 加入申请审核，超时自动提醒
- 内容举报处理，操作可追溯
- 所有管理操作权限严格控制

**Non-Goals:**
- 数据统计与推荐
- 成长激励体系
- AI 内容审核
- 通知真实推送（依赖现有 stub 通知服务）

## Decisions

### D1: 分层架构遵循现有 content.user 模式

**选择**: 在 `org.jeecg.modules.content.circle` 包下按 entity/mapper/service/service-impl/biz/controller 分层构建。

**理由**: 项目已有成熟分层模式，保持一致性降低认知成本。circle-core 的 entity/service 已按此结构设计（虽然尚未编码），本 change 在同一包结构上扩展。

**替代方案**: 独立微服务模块 → 过度设计，MVP 阶段不需要。

### D2: 置顶/精华通过 circle_content 表扩展字段实现

**选择**: 在 `circle_content` 表新增 `is_pinned`/`pinned_at`/`is_featured`/`featured_at` 字段，列表查询通过 ORDER BY 排序。

**理由**: 简单直接，无需额外关联表。置顶排序仅依赖 `pinned_at` 时间戳，取消置顶即清除字段。

**替代方案**: 独立 `circle_content_pin` 关联表 → 增加复杂度，单圈子置顶内容量有限，无需额外表。

### D3: 公告采用独立表，同一圈子仅保留一条生效公告

**选择**: 新建 `circle_announcement` 表，发布新公告时将旧公告状态设为失效。

**理由**: 公告需要有效期管理、历史记录追溯。独立表结构清晰，业务逻辑简单。

**替代方案**: 在 circle 表内嵌公告字段 → 无法保留历史公告记录。

### D4: @成员通知采用异步处理

**选择**: 内容/评论发布成功后，异步解析 @提及并发送通知。通知失败记录到日志表，支持后续补偿。

**理由**: PRD 要求通知不阻塞主操作。当前通知服务为 stub，异步设计为后续真实推送预留扩展点。

**实现**: 使用 `@Async` 注解或 Spring Event 机制，提及解析在独立线程执行。

### D5: 审核日志采用领域专用实体

**选择**: 新建 `CircleAuditLog` 实体（不继承 JeecgEntity），记录操作人、时间、类型、对象、结果。遵循 `UserStatusAuditLog` 模式。

**理由**: 审核日志有特殊查询需求（按时间范围、按操作对象查询），字段结构与通用实体不同。项目已有此模式先例。

### D6: 加入申请审核使用独立表

**选择**: 新建 `circle_join_request` 表记录申请状态，与 `circle_member` 表分离。

**理由**: 申请有独立生命周期（待审核→已批准/已拒绝），需要超时提醒等定时任务。与成员表分离避免状态混乱。

## Risks / Trade-offs

- **通知系统为 stub** → @成员通知和审核结果通知仅写 audit log，无真实推送。后续需对接真实通知渠道。Mitigation: 通过 `IContentNotificationService` 接口隔离，实现可替换。
- **circle-core 未构建** → 本 change 依赖的圈子/成员表不存在。Mitigation: 本 change 同时定义所需表结构，与 circle-core 协调部署。
- **超时提醒定时任务** → 3天未处理提醒需要定时扫描。Mitigation: 使用 Spring `@Scheduled`，频率设为每小时一次。

## File Structure

```
jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/
├── entity/
│   ├── CircleContent.java              # 扩展 is_pinned/pinned_at/is_featured/featured_at
│   ├── CircleAnnouncement.java         # 公告实体
│   ├── CircleJoinRequest.java          # 加入申请实体
│   ├── CircleReport.java               # 举报实体
│   └── CircleAuditLog.java             # 审核日志实体（不继承 JeecgEntity）
├── mapper/
│   ├── CircleContentMapper.java
│   ├── CircleAnnouncementMapper.java
│   ├── CircleJoinRequestMapper.java
│   ├── CircleReportMapper.java
│   └── CircleAuditLogMapper.java
├── service/
│   ├── ICircleContentPinService.java
│   ├── ICircleAnnouncementService.java
│   ├── ICircleMentionService.java
│   ├── ICircleJoinReviewService.java
│   ├── ICircleReportService.java
│   └── ICircleAuditLogService.java
├── service/impl/
│   ├── CircleContentPinServiceImpl.java
│   ├── CircleAnnouncementServiceImpl.java
│   ├── CircleMentionServiceImpl.java
│   ├── CircleJoinReviewServiceImpl.java
│   ├── CircleReportServiceImpl.java
│   └── CircleAuditLogServiceImpl.java
├── biz/
│   ├── CircleContentPinBizService.java
│   ├── CircleAnnouncementBizService.java
│   ├── CircleMentionBizService.java
│   ├── CircleJoinReviewBizService.java
│   └── CircleReportBizService.java
├── controller/
│   ├── CircleContentPinController.java
│   ├── CircleAnnouncementController.java
│   ├── CircleJoinReviewController.java
│   └── CircleReportController.java
├── req/
│   ├── CircleAnnouncementReq.java
│   ├── CircleJoinReviewReq.java
│   └── CircleReportReq.java
├── vo/
│   ├── CircleAnnouncementVO.java
│   ├── CircleJoinRequestVO.java
│   └── CircleReportVO.java
└── enums/
    ├── CircleAuditActionEnum.java      # PIN/UNPIN/FEATURE/UNFEATURE/APPROVE/REJECT/DELETE_REPORT/IGNORE_REPORT/MUTE
    ├── CircleReportStatusEnum.java     # PENDING/RESOLVED/IGNORED
    └── CircleJoinRequestStatusEnum.java # PENDING/APPROVED/REJECTED/EXPIRED

jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/resources/
├── mapper/content/circle/
│   ├── CircleContentMapper.xml         # 置顶排序查询
│   ├── CircleAnnouncementMapper.xml
│   ├── CircleJoinRequestMapper.xml     # 超时查询
│   ├── CircleReportMapper.xml
│   └── CircleAuditLogMapper.xml        # 时间范围查询
└── flyway/sql/mysql/
    └── V3.9.1_60__circle_content_interaction.sql

jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/circle/
├── service/
│   ├── CircleContentPinServiceTest.java
│   ├── CircleAnnouncementServiceTest.java
│   ├── CircleMentionServiceTest.java
│   ├── CircleJoinReviewServiceTest.java
│   └── CircleReportServiceTest.java
└── biz/
    ├── CircleContentPinBizServiceTest.java
    ├── CircleAnnouncementBizServiceTest.java
    ├── CircleMentionBizServiceTest.java
    ├── CircleJoinReviewBizServiceTest.java
    └── CircleReportBizServiceTest.java
```

## Test Strategy

每个测试文件的策略：

- **CircleContentPinServiceTest**: 测试置顶/取消置顶/精华/取消精华的 CRUD 操作，验证权限校验（仅版主/创建者），验证排序逻辑（置顶内容在前）
- **CircleAnnouncementServiceTest**: 测试公告发布/替换/过期逻辑，验证同一圈子仅一条生效公告
- **CircleMentionServiceTest**: 测试 @提及解析（仅圈内成员）、通知发送（异步不阻塞）、已退出成员不发送
- **CircleJoinReviewServiceTest**: 测试审核批准/拒绝流程、超时提醒查询、审核日志记录
- **CircleReportServiceTest**: 测试举报提交/处理（删除/忽略/禁言）、状态流转、处理结果通知
- **CircleContentPinBizServiceTest**: 测试 biz 层权限校验 + 操作组合逻辑
- **CircleAnnouncementBizServiceTest**: 测试 biz 层公告替换 + 权限校验
- **CircleMentionBizServiceTest**: 测试 biz 层提及解析 + 异步通知编排
- **CircleJoinReviewBizServiceTest**: 测试 biz 层审核流程 + 超时提醒编排
- **CircleReportBizServiceTest**: 测试 biz 层举报处理 + 操作编排

## Migration Plan

1. 执行 Flyway 迁移脚本 `V3.9.1_60__circle_content_interaction.sql`，创建 5 张新表
2. circle_content 表结构变更（新增置顶/精华字段）需与 circle-core 协调，如 circle-content 表已存在则使用 ALTER TABLE
3. 部署顺序：数据库迁移 → 后端服务 → 验证 API
4. 回滚策略：Flyway 回滚脚本删除新增表和字段

## Open Questions

- circle-core 的 circle_content 表是否已确定最终字段结构？本 change 的置顶/精华字段需要与之对齐
- 通知系统何时升级为真实推送？当前 stub 实现是否需要预留扩展接口
- 审核超时提醒通过什么渠道通知管理员？站内通知还是推送
