## 1. 数据库迁移与基础实体

- [x] 1.1 创建 Flyway 迁移脚本：频道统计汇总表（channel_stats）、导出任务表（channel_export_task）、审核记录表（channel_review）、生命周期变更日志表（channel_lifecycle_log）、申诉记录表（channel_appeal）
- [x] 1.2 创建实体类：ChannelStats、ChannelExportTask、ChannelReview、ChannelLifecycleLog、ChannelAppeal
- [x] 1.3 创建枚举类：ChannelLifecycleStatus、ChannelReviewStatus、ChannelExportStatus、ChannelAppealStatus、ChannelViolationType
- [x] 1.4 创建 Mapper 接口和 XML：ChannelStatsMapper、ChannelExportTaskMapper、ChannelReviewMapper、ChannelLifecycleLogMapper、ChannelAppealMapper

## 2. 统计看板能力（channel-stats-dashboard）

- [x] 2.1 创建 ChannelStatsService：统计汇总表 CRUD 和查询方法
- [x] 2.2 创建 ChannelStatsBiz：核心指标聚合逻辑（订阅数、内容数、PV、UV）
- [x] 2.3 创建 ChannelStatsController：统计查询 API（核心指标、趋势数据、互动数据）
- [x] 2.4 创建热门内容查询逻辑：按有效互动量排序，排除违规/删除内容
- [x] 2.5 创建用户分析查询逻辑：订阅增量/流失、成员活跃度、贡献排行
- [x] 2.6 创建定时任务：统计汇总表数据刷新（每 5 分钟）

## 3. 数据导出能力（channel-data-export）

- [x] 3.1 创建 ChannelExportTaskService：导出任务表 CRUD
- [x] 3.2 创建 ChannelExportBiz：导出任务创建、权限校验、异步处理编排
- [x] 3.3 创建 ChannelExportController：导出 API（发起导出、查询状态、下载文件）
- [x] 3.4 实现 Excel/CSV 文件生成逻辑
- [x] 3.5 实现导出记录追踪和文件有效期管理

## 4. 审核流程能力（channel-review-flow）

- [x] 4.1 创建 ChannelReviewService：审核记录表 CRUD
- [x] 4.2 创建审核队列查询逻辑：按提交时间、频道类型、状态筛选
- [x] 4.3 创建 ChannelReviewController：审核 API（列表、详情、通过、拒绝、退回）
- [x] 4.4 实现关键字段修改审核流程
- [x] 4.5 实现审核超时标记（24 小时未处理）
- [x] 4.6 实现审核结果通知触发

## 5. 冻结/解冻能力（channel-freeze-unfreeze）

- [x] 5.1 创建 ChannelLifecycleBiz：生命周期状态变更编排（冻结/解冻）
- [x] 5.2 创建 ChannelLifecycleController：冻结/解冻 API
- [x] 5.3 实现冻结后发布拦截逻辑
- [x] 5.4 实现冻结/解冻审计日志记录
- [x] 5.5 实现冻结处罚通知和申诉入口

## 6. 归档能力（channel-archive）

- [x] 6.1 实现不活跃频道识别逻辑（连续 6 个月无活动）
- [x] 6.2 实现自动归档流程（通知后 1 个月无改善）
- [x] 6.3 实现组织频道归档通知逻辑
- [x] 6.4 实现手动归档和申请归档流程
- [x] 6.5 实现归档频道从发现入口排除

## 7. 合并能力（channel-merge）

- [x] 7.1 创建 ChannelMergeBiz：合并流程编排（跨聚合）
- [x] 7.2 实现合并申请逻辑：目标频道校验、影响范围展示
- [x] 7.3 实现组织频道合并审批流程
- [x] 7.4 实现合并执行逻辑：内容和订阅关系迁移
- [x] 7.5 实现合并后源频道状态变更和目标频道入口展示
- [x] 7.6 实现合并审计日志记录

## 8. 违规处理能力（channel-violation-handling）

- [x] 8.1 实现限制推荐逻辑：从公共推荐流排除
- [x] 8.2 实现强制隐藏逻辑：对外不可见
- [x] 8.3 实现永久关闭逻辑：二次确认、不可恢复
- [x] 8.4 实现违规处理通知和申诉入口
- [x] 8.5 实现违规处理审计日志记录

## 9. 不活跃治理能力（channel-inactivity-governance）

- [x] 9.1 创建定时任务：每日扫描不活跃频道
- [x] 9.2 实现不活跃提醒通知逻辑
- [x] 9.3 实现个人频道自动归档逻辑
- [x] 9.4 实现组织频道通知管理员逻辑
- [x] 9.5 实现恢复活跃解除风险状态逻辑
- [x] 9.6 实现不活跃处置审计日志记录

## 10. 审计日志与申诉能力（channel-lifecycle-audit）

- [x] 10.1 创建 ChannelLifecycleLogService：生命周期变更日志 CRUD
- [x] 10.2 创建 ChannelAppealService：申诉记录表 CRUD
- [x] 10.3 实现审计日志查询 API：按频道、操作人、时间筛选
- [x] 10.4 实现申诉提交和处理 API
- [x] 10.5 实现申诉 SLA 监控（首次响应 <= 3 个工作日）

## 11. 验证

- [x] 11.1 编写单元测试：ChannelStatsBizTest（核心指标、趋势、热门内容、用户分析）
- [x] 11.2 编写单元测试：ChannelExportBizTest（导出任务、权限校验、异步处理）
- [x] 11.3 编写单元测试：ChannelLifecycleBizTest（状态机流转、审计日志）
- [x] 11.4 编写单元测试：ChannelMergeBizTest（合并流程、数据迁移）
- [x] 11.5 运行所有测试并验证通过
- [x] 11.6 验证数据库迁移脚本可执行
- [x] 11.7 验证 API 接口符合规范
