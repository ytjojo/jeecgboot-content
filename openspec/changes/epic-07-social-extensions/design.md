## Context

内容社区模块已具备基础关注体系（`ContentUserRelation` 支持关注/取关/特别关注/屏蔽/拉黑），但缺少：
- 互关关系判定与前端标识展示
- 内容可见性中 MUTUAL_ONLY（仅互关可见）的实际判定逻辑
- 粉丝管理的数据统计与展示
- 邀请码/邀请链接生成与追踪
- 社区角色标签在评论区的展示
- 版主/管理员对评论的管理操作

已有基础：
- `ContentUserRelation` 已存储关注关系（`followed`、`specialFollow`）
- `ContentUserVisibilityEnum` 已有 `MUTUAL_ONLY` 枚举值
- 现有架构遵循 `controller → biz → service → mapper → entity` 分层

## Goals / Non-Goals

**Goals:**
- 在查询关系时返回 `mutualFollow` 字段（双向判定）
- 内容查询层根据可见性（含 MUTUAL_ONLY）执行权限过滤
- 新增邀请码表与邀请关系追踪
- 新增粉丝趋势统计表与粉丝画像表（T+1 聚合）
- 新增社区角色标签 VO，前端评论区渲染
- 版主/管理员评论管理与操作审计日志

**Non-Goals:**
- 不涉及 IM 即时通讯
- 不涉及独立的用户组织/通讯录
- 粉丝画像不涉及实时大数据分析，仅基于已有用户标签聚合
- 不涉及大规模推荐算法改造

## Decisions

### 1. 互关判定逻辑：Service 层双向查询
**决策**：在 `IContentUserRelationService` 新增 `getMutualFollowList(userId)` 方法，以及 `isMutualFollow(userIdA, userIdB)` 方法。在 `getRelation` 返回的 VO 中增加 `mutualFollow` 字段。
**理由**：互关判定本质是两次单表查询，无需引入复杂图数据库。利用现有 `content_user_relation` 表即可。
**替代方案**：增加单独的互关关系表 — 否，冗余存储增加一致性维护成本。

### 2. 私密内容可见性判定：Mapper 层 JOIN 过滤
**决策**：在内容查询的 SQL 中，当 visible = `MUTUAL_ONLY` 时，通过 LEFT JOIN `content_user_relation` 表确认双向关注关系，非互关用户的查询自动过滤。
**理由**：权限判定在数据层执行最安全，避免 Service 层遗漏。
**替代方案**：在 Service 层过滤 — 不安全，分页和排序会受影响。

### 3. 邀请码存储：独立表 + 唯一索引
**决策**：新增 `content_invite_code` 表，每个用户一条记录，`invite_code` 字段加唯一索引，`share_link` 为冗余字段。被邀请人注册时通过 `content_invite_record` 表记录关系和奖励发放状态。
**理由**：邀请关系和邀请码分开存储，便于追踪和分析。
**替代方案**：在用户表直接存邀请码 — 不便于扩展邀请记录追踪。

### 4. 粉丝趋势与画像：定时任务聚合
**决策**：新增 `content_follower_daily_stat` 表（按天聚合新增粉丝数）和 `content_follower_profile` 表（兴趣/地域/活跃时段分布）。通过 `@Scheduled` 定时任务 T+1 聚合。
**理由**：粉丝画像非实时需求，定时聚合减轻查询压力。

### 5. 社区角色标签：从系统 RBAC 读取
**决策**：角色标签（普通用户/创作者/版主/管理员）直接从系统已有的角色表（`sys_user_role`）读取，不新建角色表。在评论查询时批量 JOIN 角色信息，避免 N+1 查询。
**理由**：系统已有 RBAC 模型，复用避免数据不一致。

### 6. 版主管理操作审计：独立审计表
**决策**：新增 `content_moderation_audit_log` 表，记录所有版主/管理员操作（操作人、被操作人、操作类型、原因、时间），支持撤销。
**理由**：审计留痕是治理刚需，独立表便于查询和导出。

## Risks / Trade-offs

- **[互关判定性能]** 双向查询在大量数据下可能慢 → 通过缓存 Redis 互关状态（TTL 5 分钟）+ 数据库索引优化
- **[邀请码滥用]** 用户批量注册刷奖励 → 增加 IP/设备指纹限制、奖励发放延迟校验、风控策略
- **[版主权限滥用]** 版主误删/误封 → 审计日志 + 撤销机制 + 管理员复核
- **[粉丝画像隐私]** 画像可能泄露个人信息 → 导出 CSV 前脱敏，不存储精确个人信息

## Migration Plan

1. 创建新表 Flyway 迁移 → 2. 部署后端新代码 → 3. 启动定时任务首次聚合历史数据 → 4. 前端发布新功能

**回滚**：Flyway 回滚迁移（删除新表/字段），回退代码版本，定时任务不执行即可。

## File Structure

### 后端新增/修改文件
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/vo/ContentUserRelationVO.java` — 修改，增加 mutualFollow 字段
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/IContentUserRelationService.java` — 修改，增加互关相关方法
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/impl/ContentUserRelationServiceImpl.java` — 修改，实现互关逻辑
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/controller/ContentUserRelationController.java` — 修改，增加互关好友列表接口
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/entity/ContentInviteCode.java` — 新增，邀请码实体
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/entity/ContentInviteRecord.java` — 新增，邀请记录实体
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/entity/ContentFollowerDailyStat.java` — 新增，粉丝趋势统计实体
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/entity/ContentFollowerProfile.java` — 新增，粉丝画像实体
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/entity/ContentModerationAuditLog.java` — 新增，审计日志实体
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/vo/ContentInviteRecordVO.java` — 新增，邀请记录视图
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/vo/ContentFollowerStatVO.java` — 新增，粉丝统计视图
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/vo/ContentFollowerProfileVO.java` — 新增，粉丝画像视图
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/vo/ContentUserRoleLabelVO.java` — 新增，角色标签视图
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/mapper/ContentInviteCodeMapper.java` — 新增
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/mapper/ContentInviteRecordMapper.java` — 新增
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/mapper/ContentFollowerDailyStatMapper.java` — 新增
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/mapper/ContentFollowerProfileMapper.java` — 新增
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/mapper/ContentModerationAuditLogMapper.java` — 新增
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/IContentInviteService.java` — 新增
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/impl/ContentInviteServiceImpl.java` — 新增
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/IContentFollowerStatService.java` — 新增
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/impl/ContentFollowerStatServiceImpl.java` — 新增
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/IContentModerationService.java` — 新增
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/impl/ContentModerationServiceImpl.java` — 新增
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/controller/ContentInviteController.java` — 新增
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/controller/ContentFollowerController.java` — 新增
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/controller/ContentModerationController.java` — 新增
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/scheduler/ContentFollowerStatScheduler.java` — 新增，定时任务
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/constant/ContentUserErrorCode.java` — 修改，新增错误码
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/common/ContentVisibilityPolicy.java` — 修改，增加互关可见性判定逻辑

### 测试文件
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserRelationMutualFollowTest.java` — 互关判定单元测试
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentInviteServiceTest.java` — 邀请服务单元测试
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentFollowerStatServiceTest.java` — 粉丝统计单元测试
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentModerationServiceTest.java` — 版主审计单元测试
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/controller/ContentInviteControllerTest.java` — 邀请控制器测试
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/controller/ContentFollowerControllerTest.java` — 粉丝控制器测试
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/controller/ContentModerationControllerTest.java` — 版主控制器测试

### Flyway 迁移
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/resources/db/flyway/` — 新增迁移脚本

## Test Strategy

| 测试文件 | 策略 |
|---------|------|
| `ContentUserRelationMutualFollowTest` | 单元测试，Mock Mapper，覆盖单向/双向/取消关注的互关状态判定 |
| `ContentInviteServiceTest` | 单元测试，覆盖邀请码生成（幂等）、邀请关系绑定、重复注册防刷、奖励发放 |
| `ContentFollowerStatServiceTest` | 单元测试，覆盖分页查询、趋势聚合、画像数据生成（<100 粉丝边界） |
| `ContentModerationServiceTest` | 单元测试，覆盖版主权限校验、删除/警告操作、审计日志、撤销处罚 |
| `ContentInviteControllerTest` | WebMvc 测试，覆盖邀请码获取、记录查询、统计接口 |
| `ContentFollowerControllerTest` | WebMvc 测试，覆盖粉丝列表、趋势、画像导出接口 |
| `ContentModerationControllerTest` | WebMvc 测试，覆盖版主操作、权限拦截、审计日志查询 |
