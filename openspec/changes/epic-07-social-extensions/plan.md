# Execution Plan

## Steps

### Step 1.1: RED — 互关判定逻辑单元测试
- Test file: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserRelationMutualFollowTest.java`
- Assertion: `isMutualFollow(A,B)` returns true when both follow each other; false for one-way; false for no relation
- Expected failure: `isMutualFollow` method does not exist in `IContentUserRelationService`
- Verify: `cd jeecg-boot/jeecg-boot-module/jeecg-module-content && mvn test -Dtest=ContentUserRelationMutualFollowTest -q` — expected compilation failure

### Step 1.2: GREEN — 实现 isMutualFollow 方法
- Pass test from: Step 1.1
- Minimal code: Add `isMutualFollow(String userIdA, String userIdB)` to `IContentUserRelationService` and implement in `ContentUserRelationServiceImpl` by querying two rows from `content_user_relation`
- Verify: `cd jeecg-boot/jeecg-boot-module/jeecg-module-content && mvn test -Dtest=ContentUserRelationMutualFollowTest -q` — all tests pass

### Step 1.3: RED — WebMvc 测试 mutualFollow 字段
- Test file: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/controller/ContentUserControllerWebMvcTest.java` (modify existing)
- Assertion: `GET /content/user/relation/detail?userId=A&targetUserId=B` returns VO with `mutualFollow=true` for mutual followers
- Expected failure: `ContentUserRelationVO` does not have `mutualFollow` field
- Verify: `cd jeecg-boot/jeecg-boot-module/jeecg-module-content && mvn test -Dtest=ContentUserControllerWebMvcTest -q` — test fails on missing field

### Step 1.4: GREEN — 增加 mutualFollow 字段到 VO 并设置值
- Pass test from: Step 1.3
- Minimal code: Add `mutualFollow` field to `ContentUserRelationVO`, set value in `from()` method using `isMutualFollow`
- Verify: `cd jeecg-boot/jeecg-boot-module/jeecg-module-content && mvn test -Dtest=ContentUserControllerWebMvcTest -q` — test passes

### Step 1.5: RED — 互关好友列表分页查询单元测试
- Test file: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserRelationMutualFollowTest.java` (add methods)
- Assertion: `getMutualFollowList` returns paginated results sorted by follow time, empty list for no mutual friends
- Expected failure: `getMutualFollowList` method does not exist
- Verify: `cd jeecg-boot/jeecg-boot-module/jeecg-module-content && mvn test -Dtest=ContentUserRelationMutualFollowTest -q` — tests fail

### Step 1.6: GREEN — 实现 getMutualFollowList 和 /mutual-friends 接口
- Pass test from: Step 1.5
- Minimal code: Implement `getMutualFollowList` in service; add `GET /content/user/relation/mutual-friends` endpoint to `ContentUserRelationController`
- Verify: `cd jeecg-boot/jeecg-boot-module/jeecg-module-content && mvn test -Dtest=ContentUserRelationMutualFollowTest -q` — all tests pass

### Step 1.7: REFACTOR — 抽取互关判定到 ContentUserRelationBiz
- Pass test from: Step 1.6 (all related tests still pass)
- Minimal code: Create `ContentUserRelationBiz` with `checkMutualFollow` method; refactor service to delegate
- Verify: `cd jeecg-boot/jeecg-boot-module/jeecg-module-content && mvn test -Dtest=ContentUserRelationMutualFollowTest,ContentUserControllerWebMvcTest -q` — all tests pass, no regression

### Step 2.1: RED — 私密内容可见性判定单元测试
- Test file: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserVisibilityPolicyServiceTest.java` (modify existing)
- Assertion: `checkMutualVisibility` returns true for mutual followers, false for non-mutual and cancelled mutual
- Expected failure: `checkMutualVisibility` method does not exist in `ContentVisibilityPolicy`
- Verify: `cd jeecg-boot/jeecg-boot-module/jeecg-module-content && mvn test -Dtest=ContentUserVisibilityPolicyServiceTest -q` — compilation failure

### Step 2.2: GREEN — 实现 checkMutualVisibility 方法
- Pass test from: Step 2.1
- Minimal code: Add `checkMutualVisibility(contentOwnerId, viewerUserId)` to `ContentVisibilityPolicy` using `isMutualFollow` from biz layer
- Verify: `cd jeecg-boot/jeecg-boot-module/jeecg-module-content && mvn test -Dtest=ContentUserVisibilityPolicyServiceTest -q` — all tests pass

### Step 2.3: RED — 私密内容过滤集成测试
- Test file: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserVisibilityPolicyServiceTest.java` (add integration methods)
- Assertion: MUTUAL_ONLY content is invisible to non-mutual users when queried through mapper JOIN
- Expected failure: Mapper XML does not have MUTUAL_ONLY JOIN logic
- Verify: `cd jeecg-boot/jeecg-boot-module/jeecg-module-content && mvn test -Dtest=ContentUserVisibilityPolicyServiceTest -q` — test fails on missing data

### Step 2.4: GREEN — Mapper XML 增加 MUTUAL_ONLY JOIN 过滤
- Pass test from: Step 2.3
- Minimal code: Add LEFT JOIN `content_user_relation` in content query mapper XML to filter MUTUAL_ONLY visibility
- Verify: `cd jeecg-boot/jeecg-boot-module/jeecg-module-content && mvn test -Dtest=ContentUserVisibilityPolicyServiceTest -q` — all tests pass

### Step 2.5: RED — 内容可见性修改 WebMvc 测试
- Test file: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/controller/ContentUserControllerWebMvcTest.java` (add methods)
- Assertion: Updating visibility from MUTUAL_ONLY to PUBLIC makes content visible to all
- Expected failure: Update visibility endpoint may not handle MUTUAL_ONLY transitions correctly
- Verify: `cd jeecg-boot/jeecg-boot-module/jeecg-module-content && mvn test -Dtest=ContentUserControllerWebMvcTest -q` — test fails

### Step 2.6: GREEN — 实现可见性变更逻辑
- Pass test from: Step 2.5
- Minimal code: In content update endpoint, validate visibility transition, update `content_user_relation` if needed, verify owner permissions
- Verify: `cd jeecg-boot/jeecg-boot-module/jeecg-module-content && mvn test -Dtest=ContentUserControllerWebMvcTest -q` — all tests pass

### Step 3.1: RED — 粉丝列表分页查询单元测试
- Test file: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentFollowerStatServiceTest.java`
- Assertion: `getFollowerList` returns paginated results sorted by follow time desc, supports keyword search
- Expected failure: `ContentFollowerStatService` and `getFollowerList` do not exist
- Verify: `cd jeecg-boot/jeecg-boot-module/jeecg-module-content && mvn test -Dtest=ContentFollowerStatServiceTest -q` — compilation failure

### Step 3.2: GREEN — 创建粉丝统计实体、Mapper 和分页查询
- Pass test from: Step 3.1
- Minimal code: Create `ContentFollowerDailyStat` entity, `ContentFollowerDailyStatMapper`, and `IContentFollowerStatService.getFollowerList()` method
- Verify: `cd jeecg-boot/jeecg-boot-module/jeecg-module-content && mvn test -Dtest=ContentFollowerStatServiceTest -q` — all tests pass

### Step 3.3: RED — 粉丝趋势查询 WebMvc 测试
- Test file: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/controller/ContentFollowerControllerTest.java`
- Assertion: Trend endpoint returns daily/weekly/monthly aggregated follower counts
- Expected failure: `ContentFollowerController` does not exist
- Verify: `cd jeecg-boot/jeecg-boot-module/jeecg-module-content && mvn test -Dtest=ContentFollowerControllerTest -q` — compilation failure

### Step 3.4: GREEN — 实现粉丝趋势查询接口
- Pass test from: Step 3.3
- Minimal code: Create `ContentFollowerController` with trend endpoint (groupBy: day/week/month), return `ContentFollowerStatVO`
- Verify: `cd jeecg-boot/jeecg-boot-module/jeecg-module-content && mvn test -Dtest=ContentFollowerControllerTest -q` — all tests pass

### Step 3.5: RED — 粉丝 CSV 导出脱敏单元测试
- Test file: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentFollowerStatServiceTest.java` (add methods)
- Assertion: CSV export excludes email/phone, includes nickname/follow-time only
- Expected failure: Export method does not exist or includes sensitive fields
- Verify: `cd jeecg-boot/jeecg-boot-module/jeecg-module-content && mvn test -Dtest=ContentFollowerStatServiceTest -q` — test fails

### Step 3.6: GREEN — 实现粉丝 CSV 导出
- Pass test from: Step 3.5
- Minimal code: Add `exportFollowerCsv` method in service, filter out sensitive fields, return CSV response with proper headers
- Verify: `cd jeecg-boot/jeecg-boot-module/jeecg-module-content && mvn test -Dtest=ContentFollowerStatServiceTest -q` — all tests pass

### Step 4.1: RED — 粉丝兴趣分布聚合单元测试
- Test file: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentFollowerStatServiceTest.java` (add methods)
- Assertion: Aggregation by follower interest tags returns correct percentages
- Expected failure: Aggregation method does not exist
- Verify: `cd jeecg-boot/jeecg-boot-module/jeecg-module-content && mvn test -Dtest=ContentFollowerStatServiceTest -q` — test fails

### Step 4.2: GREEN — 创建粉丝画像实体和聚合方法
- Pass test from: Step 4.1
- Minimal code: Create `ContentFollowerProfile` entity, `ContentFollowerProfileMapper`, implement interest/region/active-hour aggregation
- Verify: `cd jeecg-boot/jeecg-boot-module/jeecg-module-content && mvn test -Dtest=ContentFollowerStatServiceTest -q` — all tests pass

### Step 4.3: RED — 定时任务单元测试
- Test file: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentFollowerStatServiceTest.java` (add scheduler test methods)
- Assertion: Scheduler aggregates T+1 data, skips profile generation when followers < 100
- Expected failure: `ContentFollowerStatScheduler` does not exist
- Verify: `cd jeecg-boot/jeecg-boot-module/jeecg-module-content && mvn test -Dtest=ContentFollowerStatServiceTest -q` — test fails

### Step 4.4: GREEN — 实现粉丝统计定时任务
- Pass test from: Step 4.3
- Minimal code: Create `ContentFollowerStatScheduler` with `@Scheduled` method to aggregate daily follower stats and profile data
- Verify: `cd jeecg-boot/jeecg-boot-module/jeecg-module-content && mvn test -Dtest=ContentFollowerStatServiceTest -q` — all tests pass

### Step 4.5: RED — 粉丝画像查询 WebMvc 测试
- Test file: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/controller/ContentFollowerControllerTest.java` (add methods)
- Assertion: Profile endpoint returns interest/region/active-hour distributions; returns insufficient-follower message when < 100
- Expected failure: Profile endpoint does not exist
- Verify: `cd jeecg-boot/jeecg-boot-module/jeecg-module-content && mvn test -Dtest=ContentFollowerControllerTest -q` — test fails

### Step 4.6: GREEN — 实现粉丝画像查询接口
- Pass test from: Step 4.5
- Minimal code: Add profile endpoint in `ContentFollowerController`, return `ContentFollowerProfileVO`
- Verify: `cd jeecg-boot/jeecg-boot-module/jeecg-module-content && mvn test -Dtest=ContentFollowerControllerTest -q` — all tests pass

### Step 5.1: RED — 邀请码生成单元测试
- Test file: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentInviteServiceTest.java`
- Assertion: `getOrCreateInviteCode` generates unique code on first call; returns same code on subsequent calls (idempotent)
- Expected failure: `ContentInviteService` does not exist
- Verify: `cd jeecg-boot/jeecg-boot-module/jeecg-module-content && mvn test -Dtest=ContentInviteServiceTest -q` — compilation failure

### Step 5.2: GREEN — 创建邀请码实体和服务
- Pass test from: Step 5.1
- Minimal code: Create `ContentInviteCode` entity, `ContentInviteCodeMapper`, `IContentInviteService.getOrCreateInviteCode()` method
- Verify: `cd jeecg-boot/jeecg-boot-module/jeecg-module-content && mvn test -Dtest=ContentInviteServiceTest -q` — all tests pass

### Step 5.3: RED — 邀请关系记录单元测试
- Test file: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentInviteServiceTest.java` (add methods)
- Assertion: `recordInvite` creates relationship; duplicate registration does not create second record
- Expected failure: `recordInvite` method does not exist
- Verify: `cd jeecg-boot/jeecg-boot-module/jeecg-module-content && mvn test -Dtest=ContentInviteServiceTest -q` — test fails

### Step 5.4: GREEN — 创建邀请记录实体和 recordInvite 方法
- Pass test from: Step 5.3
- Minimal code: Create `ContentInviteRecord` entity, `ContentInviteRecordMapper`, implement `recordInvite` with duplicate check
- Verify: `cd jeecg-boot/jeecg-boot-module/jeecg-module-content && mvn test -Dtest=ContentInviteServiceTest -q` — all tests pass

### Step 5.5: RED — 邀请奖励发放单元测试
- Test file: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentInviteServiceTest.java` (add methods)
- Assertion: Normal invite grants 50 points; same IP multiple registrations triggers fraud review
- Expected failure: Reward distribution logic does not exist
- Verify: `cd jeecg-boot/jeecg-boot-module/jeecg-module-content && mvn test -Dtest=ContentInviteServiceTest -q` — test fails

### Step 5.6: GREEN — 实现邀请奖励发放逻辑
- Pass test from: Step 5.5
- Minimal code: Implement reward distribution in `ContentInviteServiceImpl`, call points service (EPIC-03 integration), add IP-based fraud check
- Verify: `cd jeecg-boot/jeecg-boot-module/jeecg-module-content && mvn test -Dtest=ContentInviteServiceTest -q` — all tests pass

### Step 5.7: RED — 邀请记录查询 WebMvc 测试
- Test file: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/controller/ContentInviteControllerTest.java`
- Assertion: Invite record and stats endpoints return correct data
- Expected failure: `ContentInviteController` does not exist
- Verify: `cd jeecg-boot/jeecg-boot-module/jeecg-module-content && mvn test -Dtest=ContentInviteControllerTest -q` — compilation failure

### Step 5.8: GREEN — 创建邀请控制器
- Pass test from: Step 5.7
- Minimal code: Create `ContentInviteController` with endpoints for invite code, records, and stats
- Verify: `cd jeecg-boot/jeecg-boot-module/jeecg-module-content && mvn test -Dtest=ContentInviteControllerTest -q` — all tests pass

### Step 5.9: REFACTOR — 抽取邀请逻辑到 ContentInviteBiz
- Pass test from: Step 5.8 (all related tests still pass)
- Minimal code: Create `ContentInviteBiz`, move invite code generation and reward logic into biz layer
- Verify: `cd jeecg-boot/jeecg-boot-module/jeecg-module-content && mvn test -Dtest=ContentInviteServiceTest,ContentInviteControllerTest -q` — all tests pass

### Step 6.1: RED — 角色标签批量查询单元测试
- Test file: Create test for `ContentUserRoleLabelVO` batch query
- Assertion: Batch query via JOIN returns role labels for multiple users in single query, no N+1
- Expected failure: `ContentUserRoleLabelVO` and batch query method do not exist
- Verify: `cd jeecg-boot/jeecg-boot-module/jeecg-module-content && mvn test -Dtest=*RoleLabel* -q` — compilation failure

### Step 6.2: GREEN — 创建角色标签 VO 和批量查询方法
- Pass test from: Step 6.1
- Minimal code: Create `ContentUserRoleLabelVO`, implement batch query method that JOINs `sys_user_role`
- Verify: `cd jeecg-boot/jeecg-boot-module/jeecg-module-content && mvn test -Dtest=*RoleLabel* -q` — all tests pass

### Step 6.3: RED — 评论区角色标签 WebMvc 测试
- Test file: Add to `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/controller/ContentUserControllerWebMvcTest.java` (add comment role methods)
- Assertion: Comment list response includes role labels for each commenter
- Expected failure: Comment query does not return role labels
- Verify: `cd jeecg-boot/jeecg-boot-module/jeecg-module-content && mvn test -Dtest=ContentUserControllerWebMvcTest -q` — test fails

### Step 6.4: GREEN — 评论查询增加角色标签 JOIN
- Pass test from: Step 6.3
- Minimal code: Modify comment query mapper to JOIN role info, attach role labels to comment VO
- Verify: `cd jeecg-boot/jeecg-boot-module/jeecg-module-content && mvn test -Dtest=ContentUserControllerWebMvcTest -q` — all tests pass

### Step 7.1: RED — 版主删除评论单元测试
- Test file: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentModerationServiceTest.java`
- Assertion: Moderator can delete comments in their section; permission denied for non-moderators; audit log created
- Expected failure: `ContentModerationService` does not exist
- Verify: `cd jeecg-boot/jeecg-boot-module/jeecg-module-content && mvn test -Dtest=ContentModerationServiceTest -q` — compilation failure

### Step 7.2: GREEN — 创建审计日志实体和删除评论方法
- Pass test from: Step 7.1
- Minimal code: Create `ContentModerationAuditLog` entity/mapper, implement `IContentModerationService.deleteComment()` with RBAC check
- Verify: `cd jeecg-boot/jeecg-boot-module/jeecg-module-content && mvn test -Dtest=ContentModerationServiceTest -q` — all tests pass

### Step 7.3: RED — 版主警告用户单元测试
- Test file: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentModerationServiceTest.java` (add methods)
- Assertion: `warnUser` creates audit log and sends warning notification
- Expected failure: `warnUser` method does not exist
- Verify: `cd jeecg-boot/jeecg-boot-module/jeecg-module-content && mvn test -Dtest=ContentModerationServiceTest -q` — test fails

### Step 7.4: GREEN — 实现 warnUser 方法
- Pass test from: Step 7.3
- Minimal code: Implement `warnUser` in `ContentModerationServiceImpl` with audit log and notification
- Verify: `cd jeecg-boot/jeecg-boot-module/jeecg-module-content && mvn test -Dtest=ContentModerationServiceTest -q` — all tests pass

### Step 7.5: RED — 管理员封禁/禁言单元测试
- Test file: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentModerationServiceTest.java` (add methods)
- Assertion: Admin can ban/mute users; RBAC check denies non-admins; audit logs created
- Expected failure: `banUser` and `muteUser` methods do not exist
- Verify: `cd jeecg-boot/jeecg-boot-module/jeecg-module-content && mvn test -Dtest=ContentModerationServiceTest -q` — test fails

### Step 7.6: GREEN — 实现 banUser 和 muteUser 方法
- Pass test from: Step 7.5
- Minimal code: Implement `banUser` and `muteUser` in `ContentModerationServiceImpl` with RBAC check
- Verify: `cd jeecg-boot/jeecg-boot-module/jeecg-module-content && mvn test -Dtest=ContentModerationServiceTest -q` — all tests pass

### Step 7.7: RED — 撤销处罚单元测试
- Test file: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentModerationServiceTest.java` (add methods)
- Assertion: `revokeAction` restores previous state and creates revocation audit log
- Expected failure: `revokeAction` method does not exist
- Verify: `cd jeecg-boot/jeecg-boot-module/jeecg-module-content && mvn test -Dtest=ContentModerationServiceTest -q` — test fails

### Step 7.8: GREEN — 实现撤销处罚方法
- Pass test from: Step 7.7
- Minimal code: Implement `revokeAction` in `ContentModerationServiceImpl`, restore state based on audit log type
- Verify: `cd jeecg-boot/jeecg-boot-module/jeecg-module-content && mvn test -Dtest=ContentModerationServiceTest -q` — all tests pass

### Step 7.9: RED — 版主管理操作 WebMvc 测试
- Test file: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/controller/ContentModerationControllerTest.java`
- Assertion: All moderation endpoints (delete, warn, ban, mute, revoke, audit log query) return correct responses
- Expected failure: `ContentModerationController` does not exist
- Verify: `cd jeecg-boot/jeecg-boot-module/jeecg-module-content && mvn test -Dtest=ContentModerationControllerTest -q` — compilation failure

### Step 7.10: GREEN — 创建版主控制器
- Pass test from: Step 7.9
- Minimal code: Create `ContentModerationController` with all moderation endpoints
- Verify: `cd jeecg-boot/jeecg-boot-module/jeecg-module-content && mvn test -Dtest=ContentModerationControllerTest -q` — all tests pass

### Step 7.11: REFACTOR — 抽取版主操作公共逻辑到 ContentModerationBiz
- Pass test from: Step 7.10 (all related tests still pass)
- Minimal code: Create `ContentModerationBiz`, move permission check and audit log writing into biz layer
- Verify: `cd jeecg-boot/jeecg-boot-module/jeecg-module-content && mvn test -Dtest=ContentModerationServiceTest,ContentModerationControllerTest -q` — all tests pass

### Step 8.1: RED — 错误码编译验证
- Test file: Create a simple test in existing test suite that references all new error codes
- Assertion: All new error codes in `ContentUserErrorCode` compile and are accessible
- Expected failure: New error codes not yet defined
- Verify: `cd jeecg-boot/jeecg-boot-module/jeecg-module-content && mvn compile -q` — compilation fails

### Step 8.2: GREEN — 新增错误码
- Pass test from: Step 8.1
- Minimal code: Add error codes to `ContentUserErrorCode`: INVITE_CODE_NOT_FOUND, MUTUAL_ONLY_ACCESS_DENIED, NOT_MODERATOR_ACCESS_DENIED, INSUFFICIENT_FOLLOWERS
- Verify: `cd jeecg-boot/jeecg-boot-module/jeecg-module-content && mvn compile -q` — compilation succeeds

### Step 9.1: RED — Flyway 迁移脚本语法验证
- Test file: SQL validation script
- Assertion: All CREATE TABLE statements are syntactically valid for MySQL/PostgreSQL
- Expected failure: Tables not yet created
- Verify: `cd jeecg-boot/jeecg-boot-module/jeecg-module-content && mvn flyway:validate -q` — validation fails

### Step 9.2: GREEN — 创建 Flyway 迁移脚本
- Pass test from: Step 9.1
- Minimal code: Create Flyway migration with tables: `content_invite_code`, `content_invite_record`, `content_follower_daily_stat`, `content_follower_profile`, `content_moderation_audit_log`; add proper indexes
- Verify: `cd jeecg-boot/jeecg-boot-module/jeecg-module-content && mvn flyway:validate -q` — validation passes

### Step 10.1: RED — 端到端集成测试
- Test file: Create integration test class in test directory
- Assertion: Full flow: mutual follow → publish MUTUAL_ONLY content → mutual friend sees it → cancel mutual → can no longer see
- Expected failure: Integration test not yet written
- Verify: `cd jeecg-boot/jeecg-boot-module/jeecg-module-content && mvn test -Dtest=*IntegrationTest -q` — compilation failure

### Step 10.2: GREEN — 创建集成测试类验证数据一致性
- Pass test from: Step 10.1
- Minimal code: Create integration test class that chains all new modules: mutual follow, private content, invite, moderation
- Verify: `cd jeecg-boot/jeecg-boot-module/jeecg-module-content && mvn test -Dtest=*IntegrationTest -q` — all tests pass

### Step 10.3: REFACTOR — 清理重复测试数据准备代码
- Pass test from: Step 10.2 (all tests still pass)
- Minimal code: Extract common test fixtures (test user creation, test data setup) into shared test utility class
- Verify: `cd jeecg-boot/jeecg-boot-module/jeecg-module-content && mvn test -q` — all tests pass, no regression

---
## Execution Mode Selection
REQUIRED: Use superpowers:subagent-driven-development skill.
DO NOT use executing-plans or inline execution.
