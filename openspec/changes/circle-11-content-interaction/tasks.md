## 1. 数据库与枚举

- [ ] 1.1 编写 Flyway 迁移脚本 V3.9.1_60__circle_content_interaction.sql，创建 circle_announcement、circle_join_request、circle_report、circle_audit_log 表，以及 circle_content 表的 is_pinned/pinned_at/is_featured/featured_at 字段扩展
- [ ] 1.2 创建枚举类 CircleAuditActionEnum、CircleReportStatusEnum、CircleJoinRequestStatusEnum

## 2. 内容置顶与精华

- [ ] 2.1 编写 CircleContentPinServiceTest：置顶/取消置顶/精华/取消精华的 CRUD 测试
- [ ] 2.2 创建 CircleContent 实体扩展字段（is_pinned/pinned_at/is_featured/featured_at）
- [ ] 2.3 创建 CircleContentMapper 和 CircleContentMapper.xml（置顶排序查询）
- [ ] 2.4 实现 ICircleContentPinService 和 CircleContentPinServiceImpl
- [ ] 2.5 编写 CircleContentPinBizServiceTest：权限校验 + 操作组合逻辑测试
- [ ] 2.6 实现 CircleContentPinBizService（权限校验 + 操作编排）
- [ ] 2.7 实现 CircleContentPinController（PUT /circle-content/{id}/pin、/featured）

## 3. 圈子公告

- [ ] 3.1 编写 CircleAnnouncementServiceTest：公告发布/替换/过期逻辑测试
- [ ] 3.2 创建 CircleAnnouncement 实体和 CircleAnnouncementMapper
- [ ] 3.3 实现 ICircleAnnouncementService 和 CircleAnnouncementServiceImpl
- [ ] 3.4 编写 CircleAnnouncementBizServiceTest：公告替换 + 权限校验测试
- [ ] 3.5 实现 CircleAnnouncementBizService（权限校验 + 公告替换编排）
- [ ] 3.6 实现 CircleAnnouncementController 和相关 Req/VO

## 4. @成员功能

- [ ] 4.1 编写 CircleMentionServiceTest：@提及解析、通知发送、已退出成员过滤测试
- [ ] 4.2 创建 CircleMentionMapper（圈子成员查询）
- [ ] 4.3 实现 ICircleMentionService 和 CircleMentionServiceImpl（提及解析 + 异步通知）
- [ ] 4.4 编写 CircleMentionBizServiceTest：提及解析 + 异步通知编排测试
- [ ] 4.5 实现 CircleMentionBizService（业务编排）

## 5. 加入申请审核

- [ ] 5.1 编写 CircleJoinReviewServiceTest：审核批准/拒绝流程、超时提醒查询、审核日志记录测试
- [ ] 5.2 创建 CircleJoinRequest 实体和 CircleJoinRequestMapper（含超时查询 SQL）
- [ ] 5.3 实现 ICircleJoinReviewService 和 CircleJoinReviewServiceImpl
- [ ] 5.4 编写 CircleJoinReviewBizServiceTest：审核流程 + 超时提醒编排测试
- [ ] 5.5 实现 CircleJoinReviewBizService（权限校验 + 审核编排）
- [ ] 5.6 实现 CircleJoinReviewController 和相关 Req/VO

## 6. 内容举报处理

- [ ] 6.1 编写 CircleReportServiceTest：举报提交/处理、状态流转、处理结果通知测试
- [ ] 6.2 创建 CircleReport 实体和 CircleReportMapper
- [ ] 6.3 实现 ICircleReportService 和 CircleReportServiceImpl
- [ ] 6.4 编写 CircleReportBizServiceTest：举报处理 + 操作编排测试
- [ ] 6.5 实现 CircleReportBizService（权限校验 + 处理编排）
- [ ] 6.6 实现 CircleReportController 和相关 Req/VO

## 7. 审核日志与定时任务

- [ ] 7.1 创建 CircleAuditLog 实体（不继承 JeecgEntity）和 CircleAuditLogMapper（含时间范围查询 SQL）
- [ ] 7.2 实现 ICircleAuditLogService 和 CircleAuditLogServiceImpl（writeAuditLog、queryByTarget、queryByTimeRange）
- [ ] 7.3 实现加入申请超时提醒定时任务（@Scheduled 每小时扫描 PENDING 超过 3 天的申请）

## 8. Validation

- [ ] 8.1 执行 Flyway 迁移，验证数据库表结构正确
- [ ] 8.2 运行所有单元测试，确保全部通过
- [ ] 8.3 验证置顶内容排序逻辑（置顶在前，按 pinned_at 倒序）
- [ ] 8.4 验证权限控制（普通成员操作返回"权限不足"）
- [ ] 8.5 验证公告同一时间仅一条生效
- [ ] 8.6 验证审核日志完整记录（操作人、时间、类型、对象、结果）
