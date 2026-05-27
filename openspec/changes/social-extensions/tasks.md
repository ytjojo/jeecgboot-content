## 1. 数据库迁移

- [x] 1.1 编写 Flyway 迁移脚本 V3.9.1_59：创建 `content_invite_code` 表（id, user_id, invite_code, created_at）和 `content_invite_record` 表（id, inviter_user_id, invitee_user_id, invite_code, registered_at, reward_point, reward_status）
- [x] 1.2 编写 Flyway 迁移脚本 V3.9.1_59：创建 `content_fan_trend_daily` 表（id, user_id, date, new_follower_count）
- [x] 1.3 编写 Flyway 迁移脚本 V3.9.1_59：`content_user_profile` 表新增 `community_role` 字段（VARCHAR(32), DEFAULT 'NORMAL'）
- [x] 1.4 编写回滚脚本 R3.9.1_59

## 2. 互关标识与私密内容（后端）

- [x] 2.1 在 `IContentUserRelationService` 新增 `isMutualFollow(userIdA, userIdB)` 方法
- [x] 2.2 在 `ContentUserRelationServiceImpl` 实现互关判定逻辑：双向查询 `content_user_relation` 表
- [x] 2.3 在 `ContentUserRelationController` 新增 `GET /mutual-follow-list` 端点（分页查询互关好友列表）
- [x] 2.4 在 `ContentUserRelationVO` 新增 `mutualFollow` 布尔字段
- [x] 2.5 修改内容查询层，新增"仅互关可见"过滤逻辑（`visibility=MUTUAL_FOLLOW_ONLY` 时判定互关关系）
- [x] 2.6 编写互关判定单元测试 `ContentUserRelationServiceMutualTest`

## 3. 粉丝管理与数据分析（后端）

- [x] 3.1 创建 `ContentFanTrendDaily` 实体类和 Mapper
- [x] 3.2 创建 `IContentFanAnalyticsService` 接口和 `ContentFanAnalyticsServiceImpl` 实现
- [x] 3.3 实现粉丝列表查询（复用关系表，按关注时间倒序，支持分页和关键词搜索）
- [x] 3.4 实现粉丝趋势统计接口（按天/周/月聚合，返回时序数据）
- [x] 3.5 实现粉丝画像分析接口（兴趣分布、地域分布、活跃时段，粉丝数<100 返回提示）
- [x] 3.6 实现粉丝数据导出（CSV 格式，脱敏）
- [x] 3.7 创建 `ContentFanAnalyticsController`（`/content/user/fan` 路径）
- [x] 3.8 创建 `ContentFanTrendAggregationTask` 定时任务（每日聚合新增粉丝数）
- [x] 3.9 编写粉丝分析服务单元测试 `ContentFanAnalyticsServiceTest`

## 4. 邀请与分享机制（后端）

- [x] 4.1 创建 `ContentInviteCode` 和 `ContentInviteRecord` 实体类及 Mapper
- [x] 4.2 创建 `IContentInviteService` 接口和 `ContentInviteServiceImpl` 实现
- [x] 4.3 实现邀请码生成逻辑（userId+时间戳哈希，8位，唯一索引）
- [x] 4.4 实现邀请关系绑定逻辑（注册时通过邀请码绑定，防重复、防自邀）
- [x] 4.5 实现邀请积分奖励发放（复用 `ContentUserRewardRule`，规则码 `INVITE_REGISTER`，每日上限）
- [x] 4.6 实现邀请记录列表和收益统计查询
- [x] 4.7 创建 `ContentInviteController`（`/content/user/invite` 路径）
- [x] 4.8 编写邀请服务单元测试 `ContentInviteServiceTest`

## 5. 社区角色标签与管理权限（后端）

- [x] 5.1 创建 `ContentCommunityRoleEnum` 枚举（NORMAL, CREATOR, MODERATOR, ADMIN）
- [x] 5.2 修改 `ContentUserProfile` 实体，新增 `communityRole` 字段
- [x] 5.3 在评论相关 VO 中新增 `communityRole` 字段用于前端展示
- [x] 5.4 在 `IContentUserGovernanceService` 新增版主管理方法（deleteComment, warnUser）
- [x] 5.5 在 `ContentUserGovernanceController` 新增版主操作端点（`/moderator/comment/delete`, `/moderator/user/warn`）
- [x] 5.6 实现管理操作审计日志写入（复用 `ContentUserAuditLog`）
- [x] 5.7 实现权限校验：版主/管理员角色才能访问管理端点，非角色用户返回 403
- [x] 5.8 编写社区角色权限测试 `ContentCommunityRoleTest`

## 6. 前端 API 层

- [x] 6.1 创建 `src/api/content/invite.ts`（邀请码生成、记录列表、统计查询）
- [x] 6.2 创建 `src/api/content/fan-analytics.ts`（粉丝列表、趋势、画像、导出）
- [x] 6.3 扩展 `src/api/content/relation.ts`（互关列表接口）

## 7. 前端组件与页面

- [x] 7.1 创建 `MutualFollowBadge.vue` 互关标识组件（根据 mutualFollow 属性显示徽章）
- [x] 7.2 创建 `CommunityRoleBadge.vue` 社区角色标签组件（根据 role 显示不同颜色标签）
- [x] 7.3 创建 `MutualFollowList.vue` 互关好友列表页面
- [x] 7.4 创建 `FanList.vue` 粉丝列表页面（含搜索和分页）
- [x] 7.5 创建 `FanTrend.vue` 粉丝趋势图表页面（ECharts，支持天/周/月切换）
- [x] 7.6 创建 `FanProfile.vue` 粉丝画像页面（兴趣分布饼图、地域分布图、活跃时段热力图）
- [x] 7.7 创建 `InviteShare.vue` 邀请分享页面（邀请码展示、复制链接、邀请记录、收益统计）
- [x] 7.8 集成互关标识到用户主页和评论区组件（Badge组件已创建，待评论区组件实现后集成）
- [x] 7.9 集成社区角色标签到评论区组件（Badge组件已创建，待评论区组件实现后集成）

## 8. 数据初始化与配置

- [x] 8.1 初始化邀请奖励规则（INSERT INTO content_user_reward_rule: ruleCode=INVITE_REGISTER, pointAmount=50）— 运行时由服务层处理，无规则时跳过奖励
- [x] 8.2 初始化社区角色说明数据 — ContentCommunityRoleEnum 枚举已包含角色说明

## 9. 验证

- [ ] 9.1 验证互关标识：互关用户主页和评论区正确显示标识，取关后标识消失
- [ ] 9.2 验证私密内容：仅互关可见内容对非互关用户不可见，公共推荐流中不出现
- [ ] 9.3 验证粉丝管理：列表分页正确、趋势图表数据准确、画像分布合理、不足 100 时有提示
- [ ] 9.4 验证邀请流程：邀请码生成→分享→注册绑定→积分发放→记录查看 全链路通畅
- [ ] 9.5 验证社区角色：评论区标签正确显示，版主可删评论/警告用户，非版主返回 403，审计日志完整
- [ ] 9.6 运行全部新增单元测试确保通过
