## 1. 数据库与实体层

- [x] 1.1 创建 Flyway SQL 迁移脚本：频道内容发布关联表、待审区表、定时发布任务表、发布限额配置表、回收站表、治理日志表、公告表
- [x] 1.2 创建 Entity 类：ChannelContentPublish、ChannelContentReview、ChannelScheduledPublish、ChannelPublishLimit、ChannelRecycleBin、ChannelGovernanceLog、ChannelAnnouncement
- [x] 1.3 创建 Mapper 接口和 XML：对应7个实体的 CRUD 操作

## 2. 频道发布权限与选择

- [x] 2.1 实现 ChannelContentPublishService：发布权限校验（四种模式）、禁言和黑名单校验
- [x] 2.2 实现 ChannelPublishLimitService：每小时/每日发布上限计算、字数下限校验
- [x] 2.3 实现 ChannelPublishBiz：发布完整流程编排（权限校验 → 限额校验 → 写入/入审 → 通知）
- [x] 2.4 创建 req/vo 对象：发布请求、频道选择结果、发布结果反馈
- [x] 2.5 实现 ChannelPublishController：发布 API 接口
- [x] 2.6 编写 ChannelContentPublishServiceTest 单元测试
- [x] 2.7 编写 ChannelPublishLimitServiceTest 单元测试
- [x] 2.8 编写 ChannelPublishBizTest 集成测试

## 3. 定时发布

- [x] 3.1 实现 ChannelScheduledPublishService：定时发布任务创建、修改、查询
- [x] 3.2 实现定时发布调度任务：扫描到期任务、重新校验规则、执行发布或入审
- [x] 3.3 实现定时发布失败通知：权限变更、频道不可用等原因通知用户
- [x] 3.4 编写 ChannelScheduledPublishServiceTest 单元测试

## 4. 待审区审核

- [x] 4.1 实现 ChannelContentReviewService：待审列表查询、审核状态流转、通过/拒绝逻辑
- [x] 4.2 实现 ChannelReviewBiz：审核流程编排（待审列表 → 审核操作 → 结果通知 → 内容状态变更）
- [x] 4.3 创建 req/vo 对象：审核请求、待审列表查询、审核结果
- [x] 4.4 实现 ChannelReviewController：待审区和审核 API 接口
- [x] 4.5 编写 ChannelContentReviewServiceTest 单元测试
- [x] 4.6 编写 ChannelReviewBizTest 集成测试

## 5. 内容治理操作

- [x] 5.1 实现 ChannelContentPinService：置顶、取消置顶、置顶顺序调整
- [x] 5.2 实现 ChannelContentFeatureService：精华标记、取消精华
- [x] 5.3 实现 ChannelRecycleBinService：删除进入回收站、恢复、30天过期处理
- [x] 5.4 实现 ChannelContentMoveService：移出频道、目标频道权限校验
- [x] 5.5 实现 ChannelEditAssistService：有限修订（标题、标签、摘要、错别字）、修订历史记录
- [x] 5.6 实现 ChannelGovernanceLogService：治理操作日志记录
- [x] 5.7 实现 ChannelGovernanceBiz：治理操作流程编排（操作 → 日志 → 通知）
- [x] 5.8 创建 req/vo 对象：治理操作请求、回收站列表、治理日志查询
- [x] 5.9 实现 ChannelGovernanceController：治理操作 API 接口
- [x] 5.10 编写 ChannelRecycleBinServiceTest 单元测试
- [x] 5.11 编写 ChannelGovernanceBizTest 集成测试

## 6. 频道公告

- [x] 6.1 实现 ChannelAnnouncementService：公告 CRUD、富文本安全过滤
- [x] 6.2 实现 ChannelAnnouncementBiz：公告管理流程编排
- [x] 6.3 创建 req/vo 对象：公告请求、公告预览、公告列表
- [x] 6.4 实现 ChannelAnnouncementController：公告 API 接口
- [x] 6.5 编写 ChannelAnnouncementServiceTest 单元测试
- [x] 6.6 编写 ChannelAnnouncementBizTest 集成测试

## 7. 已发布内容添加到频道

- [x] 7.1 实现系统频道添加逻辑：运营身份校验、添加原因记录
- [x] 7.2 实现个人/组织频道添加逻辑：作者身份校验、组织成员校验
- [x] 7.3 实现频道主添加他人作品逻辑：管理权限校验、原作者信息保留
- [x] 7.4 实现添加操作的权限和禁言规则校验复用
- [x] 7.5 创建 req/vo 对象：添加内容请求、添加结果
- [x] 7.6 在 ChannelPublishController 中添加已发布内容添加 API

## 8. 验证与集成

- [x] 8.1 运行所有单元测试，确保核心业务逻辑 >= 80% 行覆盖率
- [x] 8.2 运行所有集成测试，确保流程完整性
- [x] 8.3 验证数据库迁移脚本正确执行
- [x] 8.4 验证 API 接口符合 RESTful 规范和项目约定
