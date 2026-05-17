# Tasks

<!-- 每个 task 只能是一个 TDD 阶段 -->
<!-- 必须使用 checkbox 格式 -->
<!-- 任务严格交替 RED → GREEN →（可选 REFACTOR） -->
<!-- GREEN 任务必须引用对应的 RED 任务 -->

## 1. 互关标识 (mutual-follow-indicator)

- [ ] 1.1 RED: 编写 `isMutualFollow` 判定逻辑的单元测试 — 覆盖双向/单向/无关系三种场景，测试文件 `ContentUserRelationMutualFollowTest.java`
- [ ] 1.2 GREEN: 实现 `IContentUserRelationService.isMutualFollow()` 方法 — 在 `ContentUserRelationServiceImpl` 中查询双向关注关系
- [ ] 1.3 RED: 编写 `getRelation` 返回 VO 中 `mutualFollow` 字段的 WebMvc 测试 — `ContentUserRelationController` 的 detail 接口验证 mutualFollow 返回值
- [ ] 1.4 GREEN: 在 `ContentUserRelationVO` 中增加 `mutualFollow` 字段，在 `getRelation` 实现中设置该值
- [ ] 1.5 RED: 编写 `getMutualFollowList` 分页查询的单元测试 — 覆盖分页、排序、空列表边界
- [ ] 1.6 GREEN: 实现 `IContentUserRelationService.getMutualFollowList()` 方法及 `ContentUserRelationController` 的 `/mutual-friends` 接口
- [ ] 1.7 REFACTOR: 将互关判定逻辑抽取为 `ContentUserRelationBiz` 编排方法，消除 Service 中重复的双向查询代码

## 2. 私密内容可见性 (private-content-mutual)

- [ ] 2.1 RED: 编写 `ContentVisibilityPolicy` 中互关可见性判定的单元测试 — 覆盖互关/非互关/取消互关三种场景
- [ ] 2.2 GREEN: 在 `ContentVisibilityPolicy` 中增加 `checkMutualVisibility(contentOwnerId, viewerUserId)` 方法，利用 `isMutualFollow` 判定
- [ ] 2.3 RED: 编写私密内容过滤的集成测试 — 验证 MUTUAL_ONLY 内容在 Mapper 层 JOIN 过滤后被非互关用户不可见
- [ ] 2.4 GREEN: 在内容查询 Mapper XML 中增加 MUTUAL_ONLY 可见性的 LEFT JOIN 过滤逻辑
- [ ] 2.5 RED: 编写内容可见性修改（公开↔互关）的 WebMvc 测试
- [ ] 2.6 GREEN: 在内容更新接口中实现可见性变更逻辑，包含权限校验和审计

## 3. 粉丝管理 (follower-management)

- [ ] 3.1 RED: 编写粉丝列表分页查询的单元测试 — 覆盖分页、倒序、搜索边界，测试文件 `ContentFollowerStatServiceTest.java`
- [ ] 3.2 GREEN: 创建 `ContentFollowerDailyStat` 实体、Mapper 和 `IContentFollowerStatService.getFollowerList()` 分页查询方法
- [ ] 3.3 RED: 编写粉丝趋势统计的 WebMvc 测试 — `ContentFollowerController` 的趋势查询接口
- [ ] 3.4 GREEN: 实现 `ContentFollowerController` 的趋势查询接口（按天/周/月），返回 `ContentFollowerStatVO`
- [ ] 3.5 RED: 编写粉丝 CSV 导出的单元测试 — 验证脱敏逻辑（不包含邮箱/手机号）
- [ ] 3.6 GREEN: 实现粉丝 CSV 导出功能，包含脱敏处理和文件下载响应

## 4. 粉丝画像 (follower-profile)

- [ ] 4.1 RED: 编写粉丝兴趣分布聚合的单元测试 — 验证按标签聚合逻辑，测试文件 `ContentFollowerStatServiceTest.java`
- [ ] 4.2 GREEN: 创建 `ContentFollowerProfile` 实体、Mapper 和兴趣/地域/活跃时段聚合方法
- [ ] 4.3 RED: 编写定时任务 `ContentFollowerStatScheduler` 的单元测试 — 验证 T+1 聚合和 <100 粉丝边界
- [ ] 4.4 GREEN: 实现 `ContentFollowerStatScheduler` 定时任务，每日聚合粉丝趋势和画像数据
- [ ] 4.5 RED: 编写粉丝画像查询接口的 WebMvc 测试 — `ContentFollowerController` 的画像查询接口
- [ ] 4.6 GREEN: 实现 `ContentFollowerController` 的画像查询接口，返回 `ContentFollowerProfileVO`

## 5. 邀请分享 (invite-referral)

- [ ] 5.1 RED: 编写邀请码生成与获取的单元测试 — 覆盖首次生成、重复获取幂等性、邀请码唯一性，测试文件 `ContentInviteServiceTest.java`
- [ ] 5.2 GREEN: 创建 `ContentInviteCode` 实体、Mapper 和 `IContentInviteService.getOrCreateInviteCode()` 方法
- [ ] 5.3 RED: 编写邀请关系记录的单元测试 — 覆盖正常邀请、重复注册防刷场景
- [ ] 5.4 GREEN: 创建 `ContentInviteRecord` 实体、Mapper 和 `ContentInviteService.recordInvite()` 方法
- [ ] 5.5 RED: 编写邀请奖励自动发放的单元测试 — 覆盖正常发放和防刷暂缓场景
- [ ] 5.6 GREEN: 实现邀请奖励发放逻辑，与积分体系（EPIC-03）集成
- [ ] 5.7 RED: 编写邀请记录与统计查询的 WebMvc 测试 — `ContentInviteController` 的接口
- [ ] 5.8 GREEN: 创建 `ContentInviteController`，实现邀请码获取、记录查询、统计查询接口
- [ ] 5.9 REFACTOR: 将邀请码生成逻辑统一为 `ContentInviteBiz` 编排方法，解耦 Service 与 Controller

## 6. 社区角色标签 (community-role-label)

- [ ] 6.1 RED: 编写角色标签批量查询的单元测试 — 验证通过 JOIN 避免 N+1，测试文件 `ContentUserRoleLabelVO` 相关测试
- [ ] 6.2 GREEN: 创建 `ContentUserRoleLabelVO`，实现从 `sys_user_role` 批量查询角色标签的方法
- [ ] 6.3 RED: 编写评论区角色标签展示的 WebMvc 测试 — 验证评论列表中包含角色标签
- [ ] 6.4 GREEN: 在评论查询 Mapper 中增加角色信息 JOIN，返回评论 VO 时附加角色标签

## 7. 版主权限 (moderator-permission)

- [ ] 7.1 RED: 编写版主删除评论的单元测试 — 覆盖权限校验、删除操作、审计日志生成，测试文件 `ContentModerationServiceTest.java`
- [ ] 7.2 GREEN: 创建 `ContentModerationAuditLog` 实体、Mapper 和 `IContentModerationService.deleteComment()` 方法
- [ ] 7.3 RED: 编写版主警告用户的单元测试 — 覆盖通知发送和审计日志
- [ ] 7.4 GREEN: 实现 `IContentModerationService.warnUser()` 方法
- [ ] 7.5 RED: 编写管理员封禁/禁言操作的单元测试 — 覆盖 RBAC 权限校验和操作记录
- [ ] 7.6 GREEN: 实现 `IContentModerationService.banUser()` 和 `muteUser()` 方法
- [ ] 7.7 RED: 编写撤销处罚的单元测试 — 覆盖撤销删除、撤销封禁、撤销日志生成
- [ ] 7.8 GREEN: 实现 `IContentModerationService.revokeAction()` 方法，恢复状态并记录撤销日志
- [ ] 7.9 RED: 编写版主管理操作的 WebMvc 测试 — `ContentModerationController` 的所有接口
- [ ] 7.10 GREEN: 创建 `ContentModerationController`，实现版主操作接口
- [ ] 7.11 REFACTOR: 将版主操作公共逻辑（权限校验、审计日志写入）抽取为 `ContentModerationBiz` 编排

## 8. 错误码与常量

- [ ] 8.1 RED: 编写新增错误码的编译验证测试 — 验证所有新错误码在 `ContentUserErrorCode` 中定义正确
- [ ] 8.2 GREEN: 在 `ContentUserErrorCode` 中新增邀请码不存在、非互关无权查看、非版主无权操作等错误码

## 9. Flyway 数据库迁移

- [ ] 9.1 RED: 编写迁移脚本语法验证 — 确保所有 CREATE TABLE 语句可在测试数据库执行
- [ ] 9.2 GREEN: 创建 Flyway 迁移脚本：`content_invite_code`、`content_invite_record`、`content_follower_daily_stat`、`content_follower_profile`、`content_moderation_audit_log` 表

## 10. 集成验证

- [ ] 10.1 RED: 编写端到端集成测试 — 覆盖互关→发布私密内容→互关可见→取消互关→不可见的完整流程
- [ ] 10.2 GREEN: 创建集成测试类，串联所有新增模块验证数据一致性
- [ ] 10.3 REFACTOR: 清理重复测试数据准备代码，抽取公共 Fixture
