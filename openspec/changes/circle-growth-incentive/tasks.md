## 1. 数据库迁移与基础实体

- [x] 1.1 创建 Flyway 迁移脚本 V3.9.1_66__circle_growth_system.sql，包含 circle_level、circle_member_growth、circle_growth_log、circle_achievement、circle_member_achievement、circle_leaderboard_snapshot 6 张表
- [x] 1.2 创建 Flyway 回滚脚本 R3.9.1_66__circle_growth_system_rollback.sql
- [x] 1.3 创建枚举类：CircleLevelEnum（L1-L5）、GrowthActionEnum（POST/COMMENT/LIKE/FEATURED）、AchievementTypeEnum、LeaderboardDimensionEnum
- [x] 1.4 创建常量类 GrowthConstant，定义经验值规则（发帖10/评论3/加精30+50）、等级门槛（0/100/300/600/850）、每日上限100

## 2. 成员经验与贡献值

- [x] 2.1 创建 CircleMemberGrowth 实体和 Mapper
- [x] 2.2 创建 CircleGrowthLog 实体和 Mapper
- [x] 2.3 实现 MemberGrowthService.addExperience()，包含每日上限校验（应用层+数据库唯一索引防并发）
- [x] 2.4 实现 MemberGrowthService.revokeExperience()，支持内容删除/违规时回退
- [x] 2.5 编写 MemberGrowthServiceTest：经验值增加、每日上限、回退、流水写入
- [x] 2.6 创建 MemberGrowthController，提供个人成长信息查询 API

## 3. 连续参与进度

- [x] 3.1 实现 MemberGrowthService.recordParticipation()，标记当日有效参与
- [x] 3.2 实现 MemberGrowthService.getParticipationProgress()，返回近 7 天参与天数
- [x] 3.3 编写连续参与进度相关测试：正常记录、空状态、跨圈子独立统计
- [x] 3.4 在 MemberGrowthController 中增加连续参与进度查询接口

## 4. 圈子等级系统

- [x] 4.1 创建 CircleLevel 实体和 Mapper
- [x] 4.2 实现 CircleLevelService.calculateGrowthScore()，聚合成员规模、内容贡献、活跃互动三类指标
- [x] 4.3 实现 CircleLevelService.updateLevel()，根据成长分判定等级升降
- [x] 4.4 实现等级提升通知：调用 IContentNotificationService 通知创建者和活跃成员
- [x] 4.5 编写 CircleLevelServiceTest：成长分计算、等级升降判定、权益映射
- [x] 4.6 创建 CircleLevelController，提供等级信息和进度查询 API

## 5. 成就徽章系统

- [x] 5.1 创建 CircleAchievement 和 CircleMemberAchievement 实体和 Mapper
- [x] 5.2 实现 AchievementService.checkAndAward()，异步检查各徽章条件并发放
- [x] 5.3 实现 4 种徽章判定逻辑：持续创作者（10篇）、优质贡献者（5篇精华）、活跃参与者（3天参与）、圈内新星（前10）
- [x] 5.4 实现徽章撤销逻辑：内容违规/删除回退、成员退出圈子
- [x] 5.5 实现徽章获得通知
- [x] 5.6 编写 AchievementServiceTest：各徽章条件判定、重复发放幂等、撤销逻辑
- [x] 5.7 创建 AchievementController，提供徽章列表和进度查询 API

## 6. 排行榜系统

- [x] 6.1 创建 CircleLeaderboardSnapshot 实体和 Mapper
- [x] 6.2 实现 LeaderboardService.refreshSnapshot()，每小时从 growth_log 聚合生成快照
- [x] 6.3 实现 LeaderboardService.getLeaderboard()，支持多维度（经验值/贡献值/发帖数）和多周期（本周/本月/累计）查询
- [x] 6.4 实现当前用户定位逻辑：Top 50 内高亮、Top 50 外展示排名和差距
- [x] 6.5 编写 LeaderboardServiceTest：多维度排序、周期筛选、Top 50 截断、当前用户定位
- [x] 6.6 创建 LeaderboardController，提供排行榜查询 API

## 7. 定时任务与集成

- [x] 7.1 配置等级计算定时任务（每 30 分钟）
- [x] 7.2 配置排行榜刷新定时任务（每小时）
- [x] 7.3 集成测试：MemberGrowthController 和 LeaderboardController 的权限和响应格式

## 8. 验证

- [x] 8.1 运行全部单元测试，确认通过
- [x] 8.2 运行 Flyway 迁移，确认表结构正确
- [x] 8.3 验证等级计算、经验值上限、徽章发放、排行榜刷新的端到端流程
