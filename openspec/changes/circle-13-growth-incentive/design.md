## Context

圈子基础功能（EPIC-10）和内容互动能力（EPIC-11）为设计前置依赖。当前代码库中尚无圈子相关 Java 代码，圈子功能以 spec 和 PRD 形式存在。模块代码将放在 `jeecg-module-content` 的 `content/user/` 包下，遵循已有分层架构：entity → mapper → service/impl → controller，辅以 dto/req/vo。

数据库迁移使用 Flyway，最新版本号 V3.9.1_62，新表使用 V3.9.1_63+。通知系统已有完整基础设施（`IContentNotificationService`、`ContentUserNotificationSetting`），等级提升和徽章发放直接复用。

## Goals / Non-Goals

**Goals:**
- 建立圈子等级 5 级体系（L1-L5），基于成长分（0-1000）自动计算和升级
- 实现成员经验值和贡献值系统，支持发帖、评论、加精等行为触发
- 实现连续参与进度统计（7 天窗口），驱动留存目标
- 实现成就徽章自动发放和展示
- 实现圈子内排行榜（多维度、多周期、Top 50）

**Non-Goals:**
- 付费/商业化功能
- 跨圈子积分兑换或成长数据合并
- 自定义徽章
- 实物奖励

## Decisions

### D1: 成长值存储方案 — 独立表 vs 冗余字段

**选择**: 独立表 `circle_member_growth` 存储经验值、贡献值、等级，辅以 `circle_growth_log` 行为流水

**理由**: 成长值需要支持回退（内容删除/违规）、每日上限校验、排行统计，独立表+流水比冗余字段更易维护和审计。流水表同时支撑排行榜定时聚合和成功指标统计。

**替代方案**: 在圈子成员表加冗余字段 — 简单但无法支撑回退和审计需求。

### D2: 圈子等级计算 — 实时 vs 定时

**选择**: 定时任务（30 分钟）批量计算圈子成长分并更新等级

**理由**: 成长分依赖成员规模、内容贡献、活跃互动 3 类聚合指标，实时计算成本高。PRD 允许 30 分钟延迟，定时任务更简单可靠。

**替代方案**: 事件驱动实时计算 — 延迟低但实现复杂，需要监听多种事件源并维护聚合状态。

### D3: 排行榜实现 — 实时聚合 vs 快照

**选择**: 定时任务（每小时）从 `circle_growth_log` 聚合生成 `circle_leaderboard_snapshot` 快照表

**理由**: PRD 要求每小时更新，Top 50 查询频繁但数据量可控。快照表避免每次查询扫描全量流水，查询性能稳定。

**替代方案**: 实时查询流水表聚合 — 无快照开销但每次查询需聚合，数据量增长后性能不可控。

### D4: 每日经验上限 — 应用层校验 vs 数据库约束

**选择**: 应用层在 `MemberGrowthService.addExperience()` 中校验当日累计，辅以数据库唯一索引防并发

**理由**: 业务规则明确（每日 100 点上限），应用层校验可提供友好提示。数据库唯一索引 `(circle_id, user_id, biz_date)` 作为最后防线防并发写入。

### D5: 徽章触发方式 — 同步 vs 异步

**选择**: 成长行为完成后异步检查徽章条件（通过消息队列或异步方法）

**理由**: 徽章检查涉及聚合查询（如累计发帖数），同步执行会拖慢主流程。异步方式解耦，主流程不受徽章逻辑影响。

### D6: 包结构 — 新建 growth 子包

**选择**: 在 `content/user/` 下新建 `growth/` 子包，包含 entity/mapper/service/controller/vo

**理由**: 已有 `content/user/req/growth/` 子包存在，说明项目已预留成长相关扩展位。新增 `growth/` 包与现有架构一致，避免污染其他子域。

## Risks / Trade-offs

| 风险 | 影响 | 缓解措施 |
|------|------|----------|
| 刷分行为 | 经验值失真，排行榜公信力下降 | 每日上限 100 点 + 违规内容回退 + 同一行为幂等校验 |
| 聚合查询性能 | 排行榜和等级计算在成员规模大时变慢 | 快照表 + 定时批量 + 必要时加索引 |
| 成长值回退遗漏 | 内容删除后经验值未清理 | 内容删除事件触发回退流水写入，定时任务兜底校验 |
| 等级频繁波动 | 接近阈值时反复升降 | 仅在成长分明确超过门槛时升级，降级需低于当前等级门槛一定比例 |

## File Structure

```
jeecg-boot-module/jeecg-module-content/src/main/
├── java/org/jeecg/modules/content/user/growth/
│   ├── constant/
│   │   └── GrowthConstant.java              # 经验值规则、等级门槛等常量
│   ├── entity/
│   │   ├── CircleLevel.java                 # 圈子等级配置表实体
│   │   ├── CircleMemberGrowth.java          # 成员成长记录实体
│   │   ├── CircleGrowthLog.java             # 成长行为流水实体
│   │   ├── CircleAchievement.java           # 成就徽章配置实体
│   │   ├── CircleMemberAchievement.java     # 成员已获得徽章实体
│   │   └── CircleLeaderboardSnapshot.java   # 排行榜快照实体
│   ├── enums/
│   │   ├── CircleLevelEnum.java             # L1-L5 等级枚举
│   │   ├── GrowthActionEnum.java            # 成长行为类型枚举
│   │   ├── AchievementTypeEnum.java         # 徽章类型枚举
│   │   └── LeaderboardDimensionEnum.java    # 排行榜维度枚举
│   ├── mapper/
│   │   ├── CircleLevelMapper.java
│   │   ├── CircleMemberGrowthMapper.java
│   │   ├── CircleGrowthLogMapper.java
│   │   ├── CircleAchievementMapper.java
│   │   ├── CircleMemberAchievementMapper.java
│   │   └── CircleLeaderboardSnapshotMapper.java
│   ├── service/
│   │   ├── ICircleLevelService.java
│   │   ├── IMemberGrowthService.java
│   │   ├── IAchievementService.java
│   │   ├── ILeaderboardService.java
│   │   └── impl/
│   │       ├── CircleLevelServiceImpl.java
│   │       ├── MemberGrowthServiceImpl.java
│   │       ├── AchievementServiceImpl.java
│   │       └── LeaderboardServiceImpl.java
│   ├── vo/
│   │   ├── CircleLevelVO.java
│   │   ├── MemberGrowthVO.java
│   │   ├── AchievementVO.java
│   │   └── LeaderboardEntryVO.java
│   └── controller/
│       ├── CircleLevelController.java
│       ├── MemberGrowthController.java
│       ├── AchievementController.java
│       └── LeaderboardController.java
├── resources/flyway/sql/mysql/
│   ├── V3.9.1_63__circle_growth_system.sql
│   └── R3.9.1_63__circle_growth_system_rollback.sql

jeecg-boot-module/jeecg-module-content/src/test/
├── java/org/jeecg/modules/content/user/growth/
│   ├── service/
│   │   ├── MemberGrowthServiceTest.java
│   │   ├── CircleLevelServiceTest.java
│   │   ├── AchievementServiceTest.java
│   │   └── LeaderboardServiceTest.java
│   └── controller/
│       ├── MemberGrowthControllerTest.java
│       └── LeaderboardControllerTest.java
```

## Test Strategy

| 测试文件 | 策略 |
|----------|------|
| `MemberGrowthServiceTest` | 单元测试：经验值增加/回退、每日上限校验、贡献值计算、流水写入 |
| `CircleLevelServiceTest` | 单元测试：成长分计算、等级升降判定、权益映射 |
| `AchievementServiceTest` | 单元测试：各徽章条件判定、重复发放幂等、撤销逻辑 |
| `LeaderboardServiceTest` | 单元测试：多维度排序、周期筛选、Top 50 截断、当前用户定位 |
| `MemberGrowthControllerTest` | 集成测试：成长信息查询 API、排行榜 API 的权限和响应格式 |
| `LeaderboardControllerTest` | 集成测试：排行榜维度切换、周期切换、空状态处理 |

## Migration Plan

1. 执行 Flyway 迁移 `V3.9.1_63__circle_growth_system.sql` 创建 6 张表
2. 部署后端服务，验证等级计算和经验值服务可用
3. 运行一次性历史数据初始化脚本（如有已有圈子和成员数据）
4. 启用定时任务（等级 30 分钟、排行榜 1 小时）
5. 部署前端页面
6. Rollback: 禁用定时任务 → 回滚 Flyway → 回滚后端服务

## Open Questions

- 圈子基础表（EPIC-10）的具体表名和字段需确认，本设计假设存在 `circle` 表和 `circle_member` 表
- 内容加精事件的通知方式需确认：是通过消息队列还是直接服务调用
- 成长行为流水是否需要保留历史归档策略（当前设计为永久保留）
