# Execution Plan
> **For agentic workers:** Use superpowers:subagent-driven-development to implement this plan task-by-task.

## Phase 1: 数据库迁移

### Step 1: RED — 编写迁移脚本并验证回滚
- 迁移文件: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/resources/flyway/sql/mysql/V3.9.1_59__social_extensions.sql`
- 回滚文件: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/resources/flyway/sql/mysql/R3.9.1_59__social_extensions.rollback.sql`
- 内容: 创建 `content_invite_code`、`content_invite_record`、`content_fan_trend_daily` 表；`content_user_profile` 新增 `community_role` 字段
- Verify: 迁移脚本语法正确，回滚脚本可逆

### Step 2: GREEN — 执行迁移验证表结构
- 验证新表和字段在数据库中正确创建
- Verify: `SHOW CREATE TABLE content_invite_code` 等

---

## Phase 2: 互关标识与私密内容

### Step 3: RED — 编写互关判定测试
- 测试文件: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserRelationServiceMutualTest.java`
- 断言: 双向关注→`isMutualFollow=true`，单向→`false`，取关后→`false`
- Expected failure: `isMutualFollow` 方法不存在
- Verify: `mvn test -pl jeecg-boot-module/jeecg-module-content -Dtest=ContentUserRelationServiceMutualTest`

### Step 4: GREEN — 实现互关判定逻辑
- 最小代码: `IContentUserRelationService` 新增 `isMutualFollow`，`ContentUserRelationServiceImpl` 实现双向查询
- 新增复合索引 `(owner_user_id, target_user_id, followed)`
- Verify: `mvn test -pl jeecg-boot-module/jeecg-module-content -Dtest=ContentUserRelationServiceMutualTest`

### Step 5: RED — 编写互关列表接口测试
- 测试: `GET /api/v1/content/user/relation/mutual-follow-list` 返回分页互关列表
- Expected failure: 端点不存在
- Verify: `mvn test -pl jeecg-boot-module/jeecg-module-content -Dtest=ContentUserRelationControllerTest`

### Step 6: GREEN — 实现互关列表端点
- 最小代码: Controller 新增端点，Service 新增 `getMutualFollowList` 方法，VO 新增 `mutualFollow` 字段
- Verify: 测试通过

### Step 7: RED — 编写私密内容可见性测试
- 测试: `ContentPrivateVisibilityTest` — 互关用户可见、非互关不可见、推荐流不包含
- Expected failure: 可见性过滤逻辑未实现
- Verify: `mvn test -pl jeecg-boot-module/jeecg-module-content -Dtest=ContentPrivateVisibilityTest`

### Step 8: GREEN — 实现私密内容过滤
- 最小代码: 内容查询 SQL 增加 `visibility` 过滤条件，`MUTUAL_FOLLOW_ONLY` 时判定互关
- Verify: 测试通过

### Commit: `feat(social): add mutual follow display and private content visibility`

---

## Phase 3: 粉丝管理与数据分析

### Step 9: RED — 编写粉丝列表查询测试
- 测试文件: `ContentFanAnalyticsServiceTest.java`
- 断言: 粉丝列表分页正确、关键词搜索有效
- Expected failure: Service 不存在
- Verify: `mvn test -pl jeecg-boot-module/jeecg-module-content -Dtest=ContentFanAnalyticsServiceTest`

### Step 10: GREEN — 实现粉丝列表查询
- 最小代码: `ContentFanTrendDaily` 实体/Mapper，`IContentFanAnalyticsService` 接口和实现，`ContentFanAnalyticsController`
- Verify: 测试通过

### Step 11: RED — 编写粉丝趋势统计测试
- 断言: 按天/周/月聚合返回正确时序数据
- Expected failure: 趋势方法未实现
- Verify: `mvn test -pl jeecg-boot-module/jeecg-module-content -Dtest=ContentFanAnalyticsServiceTest`

### Step 12: GREEN — 实现粉丝趋势统计
- 最小代码: 趋势查询 SQL，定时任务 `ContentFanTrendAggregationTask`
- Verify: 测试通过

### Step 13: RED — 编写粉丝画像测试
- 断言: 兴趣/地域/活跃时段分布正确，粉丝<100 返回提示
- Expected failure: 画像方法未实现
- Verify: `mvn test -pl jeecg-boot-module/jeecg-module-content -Dtest=ContentFanAnalyticsServiceTest`

### Step 14: GREEN — 实现粉丝画像分析
- 最小代码: 基于 `ContentUserProfile` 聚合兴趣标签和地域，生成分布数据
- Verify: 测试通过

### Commit: `feat(social): add fan list, trend, and profile analytics`

---

## Phase 4: 邀请与分享机制

### Step 15: RED — 编写邀请码生成测试
- 测试文件: `ContentInviteServiceTest.java`
- 断言: 邀请码唯一、8位长度、重复查询返回已有码
- Expected failure: Service 不存在
- Verify: `mvn test -pl jeecg-boot-module/jeecg-module-content -Dtest=ContentInviteServiceTest`

### Step 16: GREEN — 实现邀请码生成
- 最小代码: `ContentInviteCode`/`ContentInviteRecord` 实体/Mapper，`IContentInviteService` 接口和实现
- Verify: 测试通过

### Step 17: RED — 编写邀请绑定与奖励测试
- 断言: 注册绑定关系正确、积分发放成功、防重复、防自邀、每日上限
- Expected failure: 绑定逻辑未实现
- Verify: `mvn test -pl jeecg-boot-module/jeecg-module-content -Dtest=ContentInviteServiceTest`

### Step 18: GREEN — 实现邀请绑定与奖励
- 最小代码: 注册时调用绑定逻辑，复用 `ContentUserRewardRule` 发放积分
- 初始化奖励规则: `INSERT INTO content_user_reward_rule (rule_code, source_type, point_amount, daily_point_cap, enabled) VALUES ('INVITE_REGISTER', 'INVITE', 50, 500, 1)`
- Verify: 测试通过

### Step 19: RED — 编写邀请记录和统计测试
- 断言: 记录列表分页正确、统计数据准确
- Expected failure: 查询方法未实现
- Verify: `mvn test -pl jeecg-boot-module/jeecg-module-content -Dtest=ContentInviteServiceTest`

### Step 20: GREEN — 实现邀请记录和统计
- 最小代码: `ContentInviteController`，记录列表和统计端点
- Verify: 测试通过

### Commit: `feat(social): add invite code, sharing, and reward system`

---

## Phase 5: 社区角色标签与管理权限

### Step 21: RED — 编写社区角色标签测试
- 测试文件: `ContentCommunityRoleTest.java`
- 断言: 角色枚举正确、Profile 字段读写正常、评论 VO 包含角色信息
- Expected failure: 枚举和字段不存在
- Verify: `mvn test -pl jeecg-boot-module/jeecg-module-content -Dtest=ContentCommunityRoleTest`

### Step 22: GREEN — 实现社区角色标签
- 最小代码: `ContentCommunityRoleEnum`，`ContentUserProfile` 新增 `communityRole` 字段，评论 VO 扩展
- Verify: 测试通过

### Step 23: RED — 编写版主管理权限测试
- 断言: 版主可删评论/警告、管理员可封禁/禁言、非角色用户 403、审计日志写入
- Expected failure: 管理端点不存在
- Verify: `mvn test -pl jeecg-boot-module/jeecg-module-content -Dtest=ContentCommunityRoleTest`

### Step 24: GREEN — 实现版主管理权限
- 最小代码: `ContentUserGovernanceController` 新增版主端点，`IContentUserGovernanceService` 扩展方法，权限校验，审计日志
- Verify: 测试通过

### Commit: `feat(social): add community role badges and moderator permissions`

---

## Phase 6: 前端实现

### Step 25: RED — 创建前端 API 层
- 文件: `src/api/content/invite.ts`、`src/api/content/fan-analytics.ts`
- 扩展: `src/api/content/relation.ts` 新增互关列表接口
- Verify: TypeScript 编译通过

### Step 26: GREEN — 创建互关标识和角色标签组件
- 文件: `MutualFollowBadge.vue`、`CommunityRoleBadge.vue`
- Verify: 组件渲染正确

### Step 27: GREEN — 创建粉丝管理页面
- 文件: `FanList.vue`、`FanTrend.vue`、`FanProfile.vue`
- Verify: 页面功能完整

### Step 28: GREEN — 创建邀请分享页面
- 文件: `InviteShare.vue`
- Verify: 邀请码展示、复制链接、记录列表、统计展示正确

### Step 29: GREEN — 集成组件到现有页面
- 集成互关标识到用户主页和评论区
- 集成社区角色标签到评论区
- Verify: 页面交互正常

### Commit: `feat(social): add frontend pages for mutual follow, fan analytics, invite, and role badges`

---

## Phase 7: 数据初始化与端到端验证

### Step 30: 初始化数据
- 初始化邀请奖励规则
- 初始化社区角色说明数据

### Step 31: 端到端验证
- 互关标识: 主页和评论区正确显示，取关后消失
- 私密内容: 非互关不可见，推荐流不包含
- 粉丝管理: 列表/趋势/画像功能完整
- 邀请流程: 生成→分享→注册→绑定→奖励 全链路
- 社区角色: 标签显示、管理操作、审计日志、权限控制

### Commit: `feat(social): finalize social extensions with data initialization`
