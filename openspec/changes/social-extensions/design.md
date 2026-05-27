## Context

内容社区模块 (`jeecg-module-content`) 已有完整的用户关系体系（关注/特别关注/拉黑/静音）、积分体系（双账本+奖励规则+等级+徽章）、治理体系（状态机+审计日志）和个人资料体系（含粉丝/关注计数）。

当前缺失的能力：
- **互关标识**：`ContentUserRelation` 已存储双向关注关系，但无互关判定逻辑和前端标识
- **私密内容可见性**：内容可见性枚举中缺少"仅互关可见"
- **粉丝数据分析**：仅有 `followerCount` 计数，无趋势图表和画像
- **邀请机制**：无邀请码/分享链接相关实体和逻辑
- **社区角色标签**：`ContentUserVerificationBadge` 可用于认证标识，但缺少社区角色（版主/管理员）的展示和管理权限

约束：
- 后端遵循 MyBatis-Plus + Flyway 迁移模式
- 前端使用 Vue3 + TypeScript，通过 `registerSuper.ts` 动态注册模块
- 积分奖励通过 `ContentUserRewardRule` + `ContentUserRewardEvent` 统一管控

## Goals / Non-Goals

**Goals:**
- 复用现有 `ContentUserRelation` 双向查询实现互关标识，零新增表
- 新增"仅互关可见"内容范围，在内容查询层统一过滤
- 新增粉丝趋势聚合定时任务 + 粉丝画像分析（T+1）
- 新增邀请码/分享链接实体，与积分奖励系统对接
- 复用平台 RBAC (`sys_role`) 实现社区角色标签，扩展治理服务支持版主管理操作
- 前端新增互关标识组件、粉丝管理页、邀请分享页、评论区角色标签

**Non-Goals:**
- 不做实时粉丝画像（采用 T+1 离线聚合）
- 不做独立 IM 即时通讯
- 不新建权限体系，复用平台 RBAC

## Decisions

### D1: 互关判定方式 — 双向查询而非冗余字段

**选择**: 查询时 JOIN `content_user_relation` 判定 A→B 和 B→A 均为 `followed=true`
**替代方案**: 在关系表新增 `mutualFollow` 冗余字段
**理由**: 关注/取关频率高，冗余字段维护成本大；双向查询走索引性能可控（<5ms）

### D2: 私密内容可见性 — 内容查询层拦截

**选择**: 在内容查询 SQL 中增加可见性过滤条件，"仅互关可见"内容需判定当前用户与作者的互关关系
**替代方案**: 查询后内存过滤
**理由**: 查询层过滤避免大量无效数据加载，性能更优

### D3: 粉丝趋势 — 定时任务聚合 + 前端图表

**选择**: 每日定时任务聚合新增粉丝数到 `content_fan_trend_daily` 表，前端用 ECharts 渲染
**替代方案**: 实时计算
**理由**: 粉丝趋势不要求实时性，T+1 聚合降低数据库压力

### D4: 粉丝画像 — 复用用户兴趣标签 + 地域信息

**选择**: 基于 `ContentUserProfile` 中的兴趣标签和地域字段聚合，结果缓存到 `content_fan_profile_snapshot` 表
**理由**: 用户资料已有兴趣标签和地域数据，无需额外采集

### D5: 邀请码生成 — 用户ID哈希 + 唯一约束

**选择**: 基于 userId + 时间戳生成 8 位邀请码，数据库唯一索引保证不重复
**替代方案**: UUID 截断
**理由**: 短码便于分享和记忆，哈希保证唯一性

### D6: 邀请奖励 — 复用 `ContentUserRewardRule` + `ContentUserRewardEvent`

**选择**: 新增 `INVITE_REGISTER` 规则到奖励规则表，注册时触发奖励事件
**理由**: 复用现有积分发放框架，保持一致性

### D7: 社区角色 — 复用平台 RBAC + 扩展治理服务

**选择**: 通过 `sys_role` 定义社区角色（版主、创作者），`ContentUserProfile` 冗余存储 `communityRole` 字段加速查询；版主管理操作通过扩展 `IContentUserGovernanceService` 实现
**替代方案**: 在内容模块内建独立角色表
**理由**: 复用平台 RBAC 减少重复建设，冗余字段避免每次查角色表

### D8: 版主管理操作 — 扩展现有治理服务

**选择**: 在 `ContentUserGovernanceController` 新增版主操作端点（删评论、警告用户），通过 `ContentUserAuditLog` 记录操作
**理由**: 治理服务已有状态变更和审计日志框架，扩展而非重建

## Risks / Trade-offs

| 风险 | 缓解措施 |
|------|----------|
| 互关双向查询在高并发下性能 | 为 `(ownerUserId, targetUserId, followed)` 建复合索引；热点用户互关结果缓存 5min |
| 邀请码被刷 | 每日邀请奖励上限（通过 `dailyPointCap` 控制）；同一 IP/设备限制注册数 |
| 粉丝画像隐私泄露 | 画像数据仅展示聚合分布，不暴露个体粉丝信息；导出数据脱敏 |
| 版主权限滥用 | 所有操作写审计日志；管理员可查看版主操作记录并撤销；设置每日操作上限 |
| 私密内容查询性能 | 互关判定结果缓存；分页查询限制每页数量 |

## File Structure

### 后端新增文件

```
jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/
├── entity/
│   ├── ContentInviteCode.java              # 邀请码实体
│   ├── ContentInviteRecord.java            # 邀请记录实体
│   └── ContentFanTrendDaily.java           # 粉丝趋势日聚合实体
├── mapper/
│   ├── ContentInviteCodeMapper.java
│   ├── ContentInviteRecordMapper.java
│   └── ContentFanTrendDailyMapper.java
├── service/
│   ├── IContentInviteService.java          # 邀请服务接口
│   └── IContentFanAnalyticsService.java    # 粉丝分析服务接口
├── service/impl/
│   ├── ContentInviteServiceImpl.java
│   └── ContentFanAnalyticsServiceImpl.java
├── controller/
│   ├── ContentInviteController.java        # 邀请端点
│   └── ContentFanAnalyticsController.java  # 粉丝分析端点
├── req/
│   ├── invite/
│   │   └── ContentInviteGenerateReq.java
│   └── fan/
│       └── ContentFanTrendReq.java
├── vo/
│   ├── ContentInviteCodeVO.java
│   ├── ContentInviteRecordPageVO.java
│   ├── ContentInviteStatsVO.java
│   ├── ContentFanTrendVO.java
│   └── ContentFanProfileVO.java
├── enums/
│   └── ContentCommunityRoleEnum.java       # 社区角色枚举
└── task/
    └── ContentFanTrendAggregationTask.java  # 粉丝趋势聚合定时任务
```

### 后端修改文件

```
├── entity/ContentUserProfile.java          # 新增 communityRole 字段
├── entity/ContentUserRelation.java         # 无变更（复用现有结构）
├── service/IContentUserRelationService.java # 新增 getMutualFollowList 方法
├── service/impl/ContentUserRelationServiceImpl.java # 实现互关查询
├── controller/ContentUserRelationController.java # 新增互关列表端点
├── controller/ContentUserGovernanceController.java # 新增版主操作端点
├── service/IContentUserGovernanceService.java # 新增版主操作方法
├── vo/ContentUserRelationVO.java           # 新增 mutualFollow 标识字段
```

### Flyway 迁移

```
resources/flyway/sql/mysql/
├── V3.9.1_59__social_extensions.sql        # 新表 + 字段变更
└── R3.9.1_59__social_extensions.rollback.sql
```

### 前端新增文件

```
jeecgboot-vue3/src/
├── api/content/
│   ├── invite.ts                           # 邀请 API
│   └── fan-analytics.ts                    # 粉丝分析 API
├── views/content/
│   ├── mutual-follow/
│   │   └── MutualFollowList.vue            # 互关好友列表
│   ├── fan/
│   │   ├── FanList.vue                     # 粉丝列表
│   │   ├── FanTrend.vue                    # 粉丝趋势图表
│   │   └── FanProfile.vue                  # 粉丝画像
│   ├── invite/
│   │   └── InviteShare.vue                 # 邀请分享页
│   └── components/
│       ├── MutualFollowBadge.vue           # 互关标识组件
│       └── CommunityRoleBadge.vue          # 社区角色标签组件
```

## Test Strategy

每个测试文件的测试策略：

| 测试文件 | 测试范围 |
|----------|----------|
| `ContentUserRelationServiceMutualTest` | 互关判定逻辑：双向关注→互关=true，单向→false，取关→移除 |
| `ContentInviteServiceTest` | 邀请码生成唯一性、邀请关系绑定、积分奖励发放、每日上限 |
| `ContentFanAnalyticsServiceTest` | 粉丝趋势聚合准确性、画像分布计算、粉丝数不足提示 |
| `ContentCommunityRoleTest` | 角色标签读取、版主操作权限校验、审计日志写入 |
| `ContentPrivateVisibilityTest` | 仅互关可见内容的查询过滤、非互关用户被拒、取消互关后不可见 |

## Migration Plan

**部署顺序**:
1. 执行 Flyway 迁移 `V3.9.1_59`（新建表 + 字段扩展）
2. 部署后端服务（新端点 + 扩展端点）
3. 部署前端（新页面 + 组件）
4. 初始化邀请奖励规则（`INSERT INTO content_user_reward_rule`）
5. 运行一次粉丝趋势历史数据回填（可选）

**回滚策略**:
- 执行 `R3.9.1_59` 回滚脚本
- 前端路由移除新页面入口

**验收条件**:
- 互关标识在主页和评论区正确显示
- 私密内容对非互关用户不可见
- 粉丝趋势图表和画像数据正确展示
- 邀请码生成、分享、注册绑定、积分发放全流程通畅
- 版主可执行管理操作且审计日志完整

## Open Questions

- 粉丝画像的"兴趣分布"数据源是否需要扩展？当前 `ContentUserProfile` 的兴趣标签是否足够丰富？
- 版主是否需要独立的任命/撤销流程，还是直接通过平台 RBAC 管理？
- 邀请分享到微信/QQ/微博是否需要对接具体 SDK，还是仅复制链接？
