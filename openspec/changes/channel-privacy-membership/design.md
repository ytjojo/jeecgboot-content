## Context

承接 EPIC-20 channel-infrastructure 的分层架构（Controller → BizManageService → Service → Mapper），在已设计的 `content_channel` 表基础上扩展隐私、订阅和成员管理能力。项目使用 Spring Boot + MyBatis Plus，数据库表命名 snake_case，Entity 使用 `@Data` + `@TableName`，依赖注入统一用 `@Resource`。

现有 channel-infrastructure 设计了三张表（content_channel、content_channel_review、content_channel_transfer），本 change 需要新增订阅关系表、成员关系表、成员角色表、治理操作记录表、邀请表等，并在 content_channel 表上扩展隐私和加入方式字段。

## Goals / Non-Goals

**Goals:**
- 在 content_channel 表扩展隐私状态（公开/私有）和加入方式（自由/审核/邀请）字段
- 实现订阅/取消订阅能力，支持默认关注策略
- 实现订阅列表管理（分组、搜索、提醒控制）
- 实现频道成员关系管理，定义四类角色（频道主/管理员/内容编辑/普通成员）
- 实现加入申请审核流程（待审队列、批量审核、超时提醒）
- 实现成员治理操作（移除 + 7天冷却期、禁言 + 自动解封、黑名单）
- 实现邀请加入机制（邀请码/链接、有效期、可用次数）
- 所有治理操作记录审计日志，支持追溯

**Non-Goals:**
- 频道创建与所有权管理（EPIC-20 已覆盖）
- 内容发布权限详细规则（EPIC-22）
- 频道推荐与发现算法（EPIC-23）
- 完整付费订阅闭环
- 平台级黑名单联动

## Decisions

### D1: 成员关系使用独立表而非在 channel 表内嵌套

**选择**: 新增 `content_channel_member` 表存储成员关系，`content_channel_member_role` 表存储角色

**理由**:
- 成员数量从 0 到百万不等，不适合嵌入 channel 表
- 成员关系有独立的生命周期（加入、退出、移除），与 channel 状态解耦
- 角色变更频繁，独立表便于追踪历史
- 查询模式以"某用户的所有频道"和"某频道的所有成员"为主，独立表索引更高效

**替代方案**: 在 channel 表用 JSON 字段存储成员列表 → 无法高效查询和索引，不采纳

### D2: 订阅与成员分离设计

**选择**: 订阅关系（content_channel_subscription）与成员关系（content_channel_member）独立

**理由**:
- 公开频道用户可直接订阅，无需成为"成员"
- 私有频道需先成为成员才能订阅
- 取消订阅不影响成员关系，移除成员会同时取消订阅
- 订阅关注的是内容推送，成员关注的是社区参与，语义不同

### D3: 治理操作使用统一审计记录表

**选择**: 新增 `content_channel_governance_log` 表，统一记录移除、禁言、黑名单等操作

**理由**:
- 所有治理操作共享字段：操作者、目标用户、操作类型、原因、时间
- 统一表简化审计查询和报表
- 操作类型用枚举区分（REMOVE/MUTE/BLACKLIST_ADD/BLACKLIST_REMOVE/UNMUTE）
- 单表便于实现治理操作追溯率 100% 的验收指标

### D4: 冷却期使用字段标记而非定时任务

**选择**: 在 content_channel_member 表记录 `cooling_end_time`，加入时校验

**理由**:
- 冷却期是被动检查（用户尝试加入时校验），不需要主动扫描
- 避免定时任务的复杂度和延迟
- 冷却期解除由管理员或申诉触发，不需要自动过期处理

### D5: 禁言使用独立表而非 member 表字段

**选择**: 新增 `content_channel_mute` 表记录禁言状态

**理由**:
- 禁言有独立的时间维度（开始、结束、解除方式）
- 支持多次禁言历史查询
- 禁言到期自动解封可用定时任务扫描 `content_channel_mute` 表
- member 表只需一个 `is_muted` 冗余字段用于快速判断

### D6: 邀请机制使用邀请码 + 链接双模式

**选择**: 新增 `content_channel_invite` 表，支持生成邀请码和邀请链接

**理由**:
- 邀请码适合线下传播（口述、海报）
- 邀请链接适合线上分享（消息、二维码）
- 两种模式共享校验逻辑（有效期、可用次数、撤销状态）
- 邀请表记录创建者和使用记录，支持追溯

## Risks / Trade-offs

| 风险 | 影响 | 缓解措施 |
|------|------|----------|
| 通知系统未就绪 | 审核结果、治理处罚无法触达用户 | 先用站内消息兜底，后续对接推送 |
| 私有频道内容被误曝光 | 隐私事故 | 将私有可见性校验作为发布前验收重点，覆盖搜索、推荐、直接访问 |
| 批量操作性能问题 | 大频道成员管理卡顿 | 批量操作异步处理，返回每个目标的成功/失败结果 |
| 组织授权系统未就绪 | 组织频道成员信息展示不合规 | 未授权时仅展示平台公开身份信息 |
| 订阅与成员关系边界不清 | 业务逻辑混乱 | 严格遵循 D2 设计，公开频道只需订阅，私有频道需先成为成员 |

## File Structure

```
jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/
├── controller/
│   ├── ChannelSubscriptionController.java    # 用户端订阅 API
│   ├── ChannelMemberController.java          # 用户端成员 API
│   ├── ChannelGovernanceController.java      # 管理端治理 API
│   └── ChannelInviteController.java          # 邀请 API
├── biz/
│   ├── ChannelSubscriptionBizService.java    # 订阅业务编排
│   ├── ChannelMemberBizService.java          # 成员管理业务编排
│   └── ChannelGovernanceBizService.java      # 治理操作业务编排
├── service/
│   ├── ChannelSubscriptionService.java
│   ├── ChannelSubscriptionGroupService.java
│   ├── ChannelMemberService.java
│   ├── ChannelMemberRoleService.java
│   ├── ChannelJoinApplicationService.java
│   ├── ChannelMuteService.java
│   ├── ChannelBlacklistService.java
│   ├── ChannelGovernanceLogService.java
│   ├── ChannelInviteService.java
│   └── impl/
│       ├── ChannelSubscriptionServiceImpl.java
│       ├── ChannelSubscriptionGroupServiceImpl.java
│       ├── ChannelMemberServiceImpl.java
│       ├── ChannelMemberRoleServiceImpl.java
│       ├── ChannelJoinApplicationServiceImpl.java
│       ├── ChannelMuteServiceImpl.java
│       ├── ChannelBlacklistServiceImpl.java
│       ├── ChannelGovernanceLogServiceImpl.java
│       └── ChannelInviteServiceImpl.java
├── mapper/
│   ├── ChannelSubscriptionMapper.java
│   ├── ChannelSubscriptionGroupMapper.java
│   ├── ChannelMemberMapper.java
│   ├── ChannelMemberRoleMapper.java
│   ├── ChannelJoinApplicationMapper.java
│   ├── ChannelMuteMapper.java
│   ├── ChannelBlacklistMapper.java
│   ├── ChannelGovernanceLogMapper.java
│   └── ChannelInviteMapper.java
├── entity/
│   ├── ChannelSubscription.java
│   ├── ChannelSubscriptionGroup.java
│   ├── ChannelMember.java
│   ├── ChannelMemberRole.java
│   ├── ChannelJoinApplication.java
│   ├── ChannelMute.java
│   ├── ChannelBlacklist.java
│   ├── ChannelGovernanceLog.java
│   └── ChannelInvite.java
├── enums/
│   ├── PrivacyType.java                      # 公开/私有
│   ├── JoinMethod.java                       # 自由/审核/邀请
│   ├── MemberRole.java                       # 频道主/管理员/内容编辑/普通成员
│   ├── ApplicationStatus.java                # 待审/批准/拒绝
│   ├── GovernanceAction.java                 # 移除/禁言/黑名单等
│   └── InviteStatus.java                     # 有效/已用完/已撤销/已过期
├── dto/
│   ├── SubscribeDTO.java
│   ├── JoinApplyDTO.java
│   ├── ReviewApplicationDTO.java
│   ├── UpdatePrivacyDTO.java
│   ├── UpdateJoinMethodDTO.java
│   ├── RemoveMemberDTO.java
│   ├── MuteMemberDTO.java
│   ├── BlacklistDTO.java
│   ├── AssignRoleDTO.java
│   ├── CreateInviteDTO.java
│   └── BatchOperationDTO.java
├── vo/
│   ├── SubscriptionVO.java
│   ├── SubscriptionListVO.java
│   ├── MemberVO.java
│   ├── MemberListVO.java
│   ├── JoinApplicationVO.java
│   ├── BlacklistVO.java
│   ├── GovernanceLogVO.java
│   └── InviteVO.java
└── constant/
    └── ChannelMemberConstants.java

src/main/resources/mapper/content/channel/
├── ChannelSubscriptionMapper.xml
├── ChannelSubscriptionGroupMapper.xml
├── ChannelMemberMapper.xml
├── ChannelMemberRoleMapper.xml
├── ChannelJoinApplicationMapper.xml
├── ChannelMuteMapper.xml
├── ChannelBlacklistMapper.xml
├── ChannelGovernanceLogMapper.xml
└── ChannelInviteMapper.xml

src/main/resources/db/migration/
└── V_channel_privacy_membership.sql          # 新增表结构迁移脚本
```

## Test Strategy

| 测试文件 | 测试策略 |
|---------|---------|
| `ChannelSubscriptionBizServiceTest.java` | 单元测试：订阅/取消订阅编排、默认关注策略、私有频道订阅前置校验 |
| `ChannelMemberBizServiceTest.java` | 单元测试：自由/审核/邀请加入编排、角色分配、冷却期校验 |
| `ChannelGovernanceBizServiceTest.java` | 单元测试：移除/禁言/黑名单操作编排、权限校验、审计记录 |
| `ChannelSubscriptionControllerTest.java` | 集成测试：订阅 API 参数校验、响应格式、权限控制 |
| `ChannelMemberControllerTest.java` | 集成测试：成员管理 API、加入申请审核、成员列表筛选 |
| `ChannelGovernanceControllerTest.java` | 集成测试：治理操作 API、批量操作、二次确认 |
| `ChannelPrivacyTest.java` | 集成测试：公开/私有切换、搜索可见性、推荐可见性 |
| `ChannelJoinMethodTest.java` | 集成测试：自由/审核/邀请加入完整流程 |
| `ChannelInviteFlowTest.java` | 集成测试：邀请创建→使用→过期→撤销完整流程 |
| `ChannelMuteExpiryTest.java` | 集成测试：禁言→到期自动解封→手动解封 |
| `ChannelBlacklistTest.java` | 集成测试：加入黑名单→移除→重新申请 |

## Migration Plan

1. **数据库迁移**: 执行 `V_channel_privacy_membership.sql` 创建 9 张新表，扩展 content_channel 表字段
2. **后端部署**: 部署新增的订阅、成员、治理子模块
3. **验证顺序**:
   - 先验证隐私设置和加入方式配置
   - 再验证订阅和加入流程
   - 最后验证治理操作和审计追溯
4. **回滚策略**: 删除新增的 9 张表，回滚 content_channel 表字段扩展

## Open Questions

1. 默认关注系统频道清单由谁维护？是否按用户类型差异化？（本期假设由平台运营后台配置）
2. 被拒绝的加入申请多久后可再次申请？（本期假设由频道主配置，默认 24 小时）
3. 私有频道是否允许成员分享链接给非成员？（本期假设允许分享，但落地页仅展示基础信息）
4. 频道黑名单是否需要与平台级黑名单联动？（本期不联动，由平台治理规则另行决定）
5. 通知服务的具体接口协议？（假设为异步通知，不阻塞主流程）
