## 1. 数据库迁移与实体定义

- [x] 1.1 编写 `V_channel_privacy_membership.sql` 迁移脚本：扩展 content_channel 表（privacy_type、join_method 字段），创建 content_channel_subscription、content_channel_subscription_group、content_channel_member、content_channel_member_role、content_channel_join_application、content_channel_mute、content_channel_blacklist、content_channel_governance_log、content_channel_invite 共 9 张新表
- [x] 1.2 创建枚举类：PrivacyType、JoinMethod、MemberRole、ApplicationStatus、GovernanceAction、InviteStatus
- [x] 1.3 创建实体类：ChannelSubscription、ChannelSubscriptionGroup、ChannelMember、ChannelMemberRole、ChannelJoinApplication、ChannelMute、ChannelBlacklist、ChannelGovernanceLog、ChannelInvite
- [x] 1.4 创建 Mapper 接口和 XML 映射文件（9 个 Mapper）

## 2. 频道隐私与加入方式

- [x] 2.1 实现 ChannelPrivacyService：隐私状态设置、系统频道公开校验、隐私变更影响提示
- [x] 2.2 实现 ChannelJoinMethodService：加入方式配置（自由/审核/邀请）、可用选项校验
- [x] 2.3 实现 ChannelInviteService：邀请创建（码/链接）、有效性校验、撤销、过期处理
- [x] 2.4 实现 ChannelInviteController：创建邀请、查看邀请列表、撤销邀请 API
- [x] 2.5 编写 ChannelPrivacyTest：公开/私有切换、系统频道校验、搜索可见性
- [x] 2.6 编写 ChannelJoinMethodTest：自由/审核/邀请加入配置和校验
- [x] 2.7 编写 ChannelInviteFlowTest：邀请创建→使用→过期→撤销完整流程

## 3. 加入申请审核

- [x] 3.1 实现 ChannelJoinApplicationService：申请提交、重复申请校验、待审队列查询
- [x] 3.2 实现 ChannelJoinApplicationBizService：审核编排（批准→成为成员、拒绝→通知）、批量审核、超时突出展示
- [x] 3.3 实现审核相关 API（在 ChannelMemberController 中）：提交申请、查看待审列表、批准/拒绝、批量审核
- [x] 3.4 编写 ChannelJoinReviewTest：申请提交、重复申请拒绝、审核批准/拒绝、批量审核、超时展示

## 4. 订阅机制

- [x] 4.1 实现 ChannelSubscriptionService：订阅/取消订阅、订阅状态查询、私有频道订阅前置校验
- [x] 4.2 实现 ChannelSubscriptionBizService：订阅编排（含默认关注策略）、信息流加权标记
- [x] 4.3 实现 ChannelSubscriptionGroupService：分组 CRUD、频道分组关联、按分组筛选
- [x] 4.4 实现 ChannelSubscriptionController：订阅/取消订阅、订阅列表、分组管理、搜索、提醒控制 API
- [x] 4.5 编写 ChannelSubscriptionBizServiceTest：订阅/取消订阅、默认关注、私有频道前置校验
- [x] 4.6 编写 ChannelSubscriptionControllerTest：订阅 API 参数校验、响应格式、权限控制

## 5. 成员角色与权限

- [x] 5.1 实现 ChannelMemberService：成员关系管理（加入、退出、状态查询）、冷却期校验
- [x] 5.2 实现 ChannelMemberRoleService：角色分配（频道主/管理员/内容编辑/普通成员）、角色变更记录
- [x] 5.3 实现 ChannelMemberBizService：自由加入编排、审核加入编排、邀请加入编排、组织授权校验
- [x] 5.4 实现 ChannelMemberController：加入频道、退出频道、角色分配 API
- [x] 5.5 实现 ChannelMemberListService：成员列表展示、按角色筛选、按时间排序、昵称搜索
- [x] 5.6 实现成员列表 API（在 ChannelMemberController 中）：成员列表查询、筛选、搜索
- [x] 5.7 编写 ChannelMemberBizServiceTest：加入编排、角色分配、冷却期校验
- [x] 5.8 编写 ChannelMemberControllerTest：成员管理 API、权限校验

## 6. 成员治理操作

- [x] 6.1 实现 ChannelMemberRemovalService：移除成员、冷却期记录、权限校验（不可移除频道主/高权限成员）
- [x] 6.2 实现 ChannelMuteService：禁言设置（1天/7天/30天/永久）、到期自动解封、手动解除
- [x] 6.3 实现 ChannelBlacklistService：加入/移出黑名单、黑名单校验（优先级高于邀请和自由加入）
- [x] 6.4 实现 ChannelGovernanceLogService：统一审计记录（移除/禁言/黑名单操作）
- [x] 6.5 实现 ChannelGovernanceBizService：治理操作编排（权限校验→操作执行→通知→审计记录）
- [x] 6.6 实现 ChannelGovernanceController：移除成员、禁言、解除禁言、黑名单管理、批量操作 API
- [x] 6.7 编写 ChannelGovernanceBizServiceTest：移除/禁言/黑名单操作编排、权限校验、审计记录
- [x] 6.8 编写 ChannelGovernanceControllerTest：治理操作 API、批量操作、二次确认
- [x] 6.9 编写 ChannelMuteExpiryTest：禁言→到期自动解封→手动解封
- [x] 6.10 编写 ChannelBlacklistTest：加入黑名单→移除→重新申请

## 7. Validation

- [x] 7.1 运行所有单元测试和集成测试，确保全部通过
- [x] 7.2 验证私有内容不可见性：搜索、推荐、直接访问、分享入口
- [x] 7.3 验证核心操作 P95 <= 500ms
- [x] 7.4 验证治理操作记录完整率 100%
