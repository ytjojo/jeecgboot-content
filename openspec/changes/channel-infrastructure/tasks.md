## 1. 数据库与实体层

- [ ] 1.1 编写数据库迁移脚本 `V_channel_infrastructure.sql`，创建 `content_channel`、`content_channel_review`、`content_channel_transfer` 三张表
- [ ] 1.2 创建枚举类 `ChannelType`（system/personal/organization）、`ChannelStatus`（Draft/PendingReview/Active/Rejected/DeleteCooling/Deleted）、`ReviewResult`（Pass/Reject/ReturnForEdit）
- [ ] 1.3 创建实体类 `Channel`（@TableName、@TableId、@Data），包含所有字段和验证注解
- [ ] 1.4 创建实体类 `ChannelReview`，包含 channel_id、reviewer_id、result、reason、created_time
- [ ] 1.5 创建实体类 `ChannelTransfer`，包含 channel_id、from_user_id、to_user_id、status、expire_time
- [ ] 1.6 创建 Mapper 接口 `ChannelMapper`、`ChannelReviewMapper`、`ChannelTransferMapper`，继承 BaseMapper
- [ ] 1.7 编写 Mapper 单元测试，验证基本 CRUD 和自定义查询

## 2. 频道数据模型 Service 层

- [ ] 2.1 创建 `ChannelService` 接口和 `ChannelServiceImpl` 实现，提供频道 CRUD 基础能力
- [ ] 2.2 实现名称唯一性校验方法 `checkNameUnique(name, excludeId)`，查询用户频道范围内是否存在同名频道
- [ ] 2.3 实现按类型和状态查询频道列表的方法
- [ ] 2.4 编写 ChannelService 单元测试（名称唯一性校验、状态查询、按类型筛选）

## 3. 审核 Service 层

- [ ] 3.1 创建 `ChannelReviewService` 接口和 `ChannelReviewServiceImpl` 实现
- [ ] 3.2 实现审核记录创建方法 `createReview(channelId, reviewerId, result, reason)`
- [ ] 3.3 实现查询频道审核历史方法 `listReviewsByChannelId(channelId)`
- [ ] 3.4 编写 ChannelReviewService 单元测试

## 4. 转让 Service 层

- [ ] 4.1 创建 `ChannelTransferService` 接口和 `ChannelTransferServiceImpl` 实现
- [ ] 4.2 实现转让请求创建方法 `createTransfer(channelId, fromUserId, toUserId)`
- [ ] 4.3 实现转让确认方法 `confirmTransfer(transferId, userId)`，更新状态并变更频道所有权
- [ ] 4.4 实现转让超时处理（定时任务扫描 Expired 状态）
- [ ] 4.5 编写 ChannelTransferService 单元测试

## 5. 频道业务编排层（BizManageService）

- [ ] 5.1 创建 `ChannelBizManageService`，注入 ChannelService、ChannelReviewService、ChannelTransferService
- [ ] 5.2 实现系统频道创建方法 `createSystemChannel(dto)`：校验管理员权限 → 创建频道（Active） → 记录审计
- [ ] 5.3 实现个人频道创建方法 `createPersonalChannel(dto)`：校验账号要求 → 校验数量上限 → 校验名称唯一性 → 调用内容安全审核 → 创建频道（PendingReview） → 创建审核记录
- [ ] 5.4 实现组织频道创建方法 `createOrganizationChannel(dto)`：校验组织认证 → 校验管理员权限 → 校验数量上限 → 校验名称唯一性 → 调用内容安全审核 → 创建频道（PendingReview） → 创建审核记录
- [ ] 5.5 实现频道信息编辑方法 `updateChannel(channelId, dto)`：区分关键/非关键字段 → 关键字段触发审核 → 非关键字段即时生效 → 记录审计
- [ ] 5.6 实现频道转让方法 `transferChannel(dto)`：校验频道类型规则 → 创建转让请求 → 发送通知
- [ ] 5.7 实现转让确认方法 `confirmTransfer(transferId, userId)`：校验目标用户 → 更新频道所有权 → 记录审计
- [ ] 5.8 实现频道删除方法 `deleteChannel(channelId, userId)`：校验前置条件 → 二次确认 → 进入冷静期
- [ ] 5.9 实现删除撤销方法 `cancelDelete(channelId, userId)`：校验冷静期 → 回退状态到 Active
- [ ] 5.10 实现冷静期到期处理（定时任务扫描 DeleteCooling 状态到期记录，批量更新为 Deleted）
- [ ] 5.11 编写 ChannelBizManageService 单元测试（Mock Service 层，覆盖所有业务流程）

## 6. Controller 层

- [ ] 6.1 创建 `ChannelController`，实现用户端 API：创建频道、查询频道详情、查询频道列表、编辑频道、发起转让、确认转让、申请删除、撤销删除
- [ ] 6.2 创建 `ChannelAdminController`，实现后台 API：创建系统频道、审核频道、查询审核队列、管理系统频道
- [ ] 6.3 创建 DTO 类：CreateChannelDTO、UpdateChannelDTO、TransferChannelDTO、DeleteChannelDTO
- [ ] 6.4 创建 VO 类：ChannelVO、ChannelListVO
- [ ] 6.5 创建查询请求类：ChannelQueryReq、ChannelCreateReq、ChannelUpdateReq
- [ ] 6.6 编写 Controller 集成测试（参数校验、权限校验、响应格式）

## 7. 定时任务

- [ ] 7.1 创建 `ChannelScheduledTask`，实现冷静期到期扫描任务（每小时执行）
- [ ] 7.2 实现转让超时扫描任务（每小时执行）
- [ ] 7.3 编写定时任务单元测试

## 8. Validation

- [ ] 8.1 运行所有单元测试，确保通过率 100%
- [ ] 8.2 运行所有集成测试，确保通过率 100%
- [ ] 8.3 执行数据库迁移脚本，验证表结构正确
- [ ] 8.4 通过后台管理页面创建系统频道，验证完整流程
- [ ] 8.5 通过用户端创建个人频道，验证审核流程
- [ ] 8.6 验证频道编辑、转让、删除的完整流程
