# 圈子成长激励 — 未完成事项代码实现计划

> **生成日期**: 2026-06-26
> **来源**: verify.md + PENDING-ISSUES.md + review-report.md + circle-growth-api-conventions-audit-2026-06-25.md
> **范围**: 后端 + 前端，涵盖代码变更、数据库变更、文档修正

---

## 一、当前状态总览

| 类别 | 已完成 | 待完成 |
|------|--------|--------|
| 后端 tasks (tasks.md) | 39/39 ✅ | 0 |
| 后端 CRITICAL 修复 | 2/2 ✅ | 0 |
| 后端 WARNING 修复 | 2/2 ✅ | 0 |
| 后端 PENDING 事项 | — | 7 项 |
| 后端 SUGGESTION | — | 2 项 |
| 前端 tasks (tasks.md) | 0/64 | 64 |
| 前端 BLOCK 问题 | 0/2 | 2 |
| 前端 FLAG 问题 | 0/12 | 12 |

---

## 二、后端待实现 (P0 → P2)

### Phase 1: 测试验证 (P0)

#### T-1 运行全量单元测试

**当前状态**: CRITICAL #1/#2 修复已完成，但全量测试未跑。
**影响**: 无法确认修复是否引入回归。

```bash
# 执行命令
cd jeecg-boot/jeecg-boot-module/jeecg-module-content
mvn test -pl . -am
```

**验收标准**:
- [ ] 28+ tests pass (含 `MemberGrowthServiceTest`、`AchievementServiceTest`、`CircleLevelServiceTest`、`LeaderboardServiceTest`)
- [ ] 无编译错误
- [ ] BUILD SUCCESS

**涉及文件**: 无需修改，仅验证。

---

### Phase 2: 数据库变更 (P1)

#### DB-1 补充徽章初始化数据

**问题**: PRD 定义 6 种徽章，`circle_achievement` 只 INSERT 了 4 条 (`ach_001`~`ach_004`)。缺少：
- `ach_005`: 内容里程碑 (CONTENT_MILESTONE) — 累计 50 篇可见内容
- `ach_006`: 社交达人 (SOCIAL_BUTTERFLY) — 邀请 5 人加入圈子

**实现**:

**Step 1**: 新建 Flyway 迁移脚本 `V3.9.1_68__circle_achievement_supplement.sql`

```sql
-- V3.9.1_68__circle_achievement_supplement.sql
-- 补充 2 种徽章配置：内容里程碑、社交达人

INSERT INTO `circle_achievement` (`id`, `achievement_type`, `name`, `description`, `icon_url`, `condition_desc`, `create_time`)
VALUES
(REPLACE(UUID(), '-', ''), 'CONTENT_MILESTONE', '内容里程碑', '累计发布 50 篇可见内容', '/icons/achievement/content_milestone.png', '累计发布 50 篇可见内容', NOW()),
(REPLACE(UUID(), '-', ''), 'SOCIAL_BUTTERFLY', '社交达人', '邀请 5 人加入圈子', '/icons/achievement/social_butterfly.png', '邀请 5 人加入圈子', NOW());
```

**Step 2**: 新建回滚脚本 `R3.9.1_68__circle_achievement_supplement_rollback.sql`

```sql
-- R3.9.1_68__circle_achievement_supplement_rollback.sql
DELETE FROM `circle_member_achievement` WHERE `achievement_type` IN ('CONTENT_MILESTONE', 'SOCIAL_BUTTERFLY');
DELETE FROM `circle_achievement` WHERE `achievement_type` IN ('CONTENT_MILESTONE', 'SOCIAL_BUTTERFLY');
```

**Step 3**: 更新 `AchievementTypeEnum`，新增 2 个枚举值

```java
// AchievementTypeEnum.java — 新增枚举值
CONTINUOUS_CREATOR("ach_001", "持续创作者", "累计发布 10 篇可见内容"),
QUALITY_CONTRIBUTOR("ach_002", "优质贡献者", "累计 5 篇精华内容"),
ACTIVE_PARTICIPANT("ach_003", "活跃参与者", "近 7 天 3 天有效参与"),
RISING_STAR("ach_004", "圈内新星", "近 7 天经验增长前 10"),
CONTENT_MILESTONE("ach_005", "内容里程碑", "累计发布 50 篇可见内容"),  // 新增
SOCIAL_BUTTERFLY("ach_006", "社交达人", "邀请 5 人加入圈子");       // 新增
```

**Step 4**: 在 `AchievementServiceImpl` 中实现 2 种新徽章的判定逻辑

```java
// AchievementServiceImpl.java — 新增判定方法

/**
 * 检查内容里程碑徽章：累计发布 50 篇可见内容
 */
private void checkContentMilestone(String circleId, String userId) {
    long postCount = circleContentMapper.selectCount(
        new LambdaQueryWrapper<CircleContent>()
            .eq(CircleContent::getCircleId, circleId)
            .eq(CircleContent::getCreateBy, userId)
            .eq(CircleContent::getStatus, ContentStatusEnum.PUBLISHED.getCode())
    );
    if (postCount >= 50) {
        awardAchievement(circleId, userId, AchievementTypeEnum.CONTENT_MILESTONE);
    }
}

/**
 * 检查社交达人徽章：邀请 5 人加入圈子
 * 依赖 circle_invite_record 表（见 DB-2）
 */
private void checkSocialButterfly(String circleId, String userId) {
    long inviteCount = circleInviteRecordMapper.selectCount(
        new LambdaQueryWrapper<CircleInviteRecord>()
            .eq(CircleInviteRecord::getCircleId, circleId)
            .eq(CircleInviteRecord::getInviterId, userId)
            .eq(CircleInviteRecord::getStatus, InviteStatusEnum.JOINED.getCode())
    );
    if (inviteCount >= 5) {
        awardAchievement(circleId, userId, AchievementTypeEnum.SOCIAL_BUTTERFLY);
    }
}
```

**验收标准**:
- [ ] Flyway 迁移成功，`circle_achievement` 表有 6 条记录
- [ ] `AchievementTypeEnum` 含 6 个枚举值
- [ ] 内容里程碑/社交达人判定逻辑单元测试通过
- [ ] 徽章发放后 `circle_member_achievement` 记录正确

---

#### DB-2 社交达人徽章邀请追踪表

**问题**: `content_invite_record` 无 `circle_id` 字段，无法统计"邀请用户加入**特定圈子**"的次数。

**方案**: 选项 B — 新建 `circle_invite_record` 表（与全局邀请表解耦，圈子邀请独立追踪）。

**实现**:

**Step 1**: 在 `V3.9.1_68__circle_achievement_supplement.sql` 中追加 DDL

```sql
-- 圈子邀请记录表
CREATE TABLE IF NOT EXISTS `circle_invite_record` (
    `id` varchar(32) NOT NULL COMMENT '主键ID',
    `circle_id` varchar(32) NOT NULL COMMENT '圈子ID',
    `inviter_id` varchar(32) NOT NULL COMMENT '邀请人用户ID',
    `invitee_id` varchar(32) DEFAULT NULL COMMENT '被邀请人用户ID',
    `status` varchar(16) NOT NULL DEFAULT 'PENDING' COMMENT '邀请状态: PENDING/JOINED/EXPIRED',
    `create_time` datetime DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_circle_inviter` (`circle_id`, `inviter_id`),
    KEY `idx_circle_invitee` (`circle_id`, `invitee_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='圈子邀请记录';
```

**Step 2**: 创建 Entity + Mapper

```
新增文件:
- jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/growth/entity/CircleInviteRecord.java
- jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/growth/mapper/CircleInviteRecordMapper.java
```

**Step 3**: 创建 `InviteStatusEnum`

```java
public enum InviteStatusEnum {
    PENDING("PENDING", "待接受"),
    JOINED("JOINED", "已加入"),
    EXPIRED("EXPIRED", "已过期");
}
```

**验收标准**:
- [ ] 表创建成功
- [ ] Mapper CRUD 操作正常
- [ ] 加入圈子时写入邀请记录（需在圈子加入逻辑中集成）

---

### Phase 3: API 扩展 (P1)

#### API-1 `/participation` 接口扩展返回 7 天每日状态

**问题**: 当前 `/api/v1/content/circle/member_growth/participation` 仅返回 `Integer`（连续天数），前端需要 `boolean[7]` 7 天每日状态来渲染时间轴组件。

**方案**: 选项 C — 返回 `ParticipationVO { days, dailyStatus: List<DayStatus> }`

**实现**:

**Step 1**: 新建 `ParticipationVO`

```java
// ParticipationVO.java
@Data
@ApiModel("连续参与进度")
public class ParticipationVO {
    @ApiModelProperty("连续参与天数")
    private Integer days;

    @ApiModelProperty("近 7 天每日状态（下标 0 = 今天，6 = 7 天前）")
    private List<DayStatus> dailyStatus;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DayStatus {
        @ApiModelProperty("日期 yyyy-MM-dd")
        private String date;
        @ApiModelProperty("是否参与")
        private Boolean participated;
    }
}
```

**Step 2**: 修改 `MemberGrowthServiceImpl.getParticipationProgress()`

```java
// MemberGrowthServiceImpl.java — 扩展实现
public ParticipationVO getParticipationProgress(String circleId, String userId) {
    LocalDate today = LocalDate.now();
    List<ParticipationVO.DayStatus> dailyStatus = new ArrayList<>();
    int consecutiveDays = 0;
    boolean inConsecutive = true;

    for (int i = 0; i < 7; i++) {
        LocalDate date = today.minusDays(i);
        boolean participated = growthLogMapper.exists(
            new LambdaQueryWrapper<CircleGrowthLog>()
                .eq(CircleGrowthLog::getCircleId, circleId)
                .eq(CircleGrowthLog::getUserId, userId)
                .eq(CircleGrowthLog::getBizDate, date)
        );
        dailyStatus.add(new ParticipationVO.DayStatus(date.toString(), participated));

        if (participated && inConsecutive) {
            consecutiveDays++;
        } else {
            inConsecutive = false;
        }
    }

    ParticipationVO vo = new ParticipationVO();
    vo.setDays(consecutiveDays);
    vo.setDailyStatus(dailyStatus);
    return vo;
}
```

**Step 3**: 修改 `MemberGrowthController`

```java
// MemberGrowthController.java — 返回类型改为 ParticipationVO
@GetMapping("/participation")
public Result<ParticipationVO> getParticipation(
    @RequestParam String circleId,
    @RequestParam String userId) {
    return Result.ok(memberGrowthService.getParticipationProgress(circleId, userId));
}
```

**验收标准**:
- [ ] 接口返回 `{ days: 3, dailyStatus: [{date, participated}, ...] }` 格式
- [ ] 7 天窗口为滚动 7 天（含今天）
- [ ] 无参与记录时返回 `days=0, dailyStatus` 全为 `false`
- [ ] 单元测试：连续参与、断断续续、空状态

---

### Phase 4: VO 字段补充 (P2)

#### VO-1/VO-2 MemberGrowthVO 补充徽章总数字段

**问题**: `MemberGrowthVO` 缺少 `totalBadges`（已获得徽章数）和 `totalBadgeCount`（徽章总数）。

**实现**:

```java
// MemberGrowthVO.java — 新增字段
@ApiModelProperty("已获得徽章数")
private Integer totalBadges;

@ApiModelProperty("徽章总数（含未获得）")
private Integer totalBadgeCount;
```

```java
// MemberGrowthServiceImpl.getGrowthInfo() — 补充查询
// 查询已获得徽章数
long earnedBadges = circleMemberAchievementMapper.selectCount(
    new LambdaQueryWrapper<CircleMemberAchievement>()
        .eq(CircleMemberAchievement::getCircleId, circleId)
        .eq(CircleMemberAchievement::getUserId, userId)
        .eq(CircleMemberAchievement::getRevoked, false)
);
// 查询徽章总数
long totalBadgeCount = circleAchievementMapper.selectCount(null);

vo.setTotalBadges((int) earnedBadges);
vo.setTotalBadgeCount((int) totalBadgeCount);
```

---

#### VO-3 CircleLevelVO 补充 circleId 字段

**实现**:

```java
// CircleLevelVO.java — 新增字段（低优先级）
@ApiModelProperty("圈子ID")
private String circleId;
```

```java
// CircleLevelServiceImpl.getLevelInfo() — 填充
vo.setCircleId(circleId);
```

---

### Phase 5: 代码整洁 (P2)

#### T-2 同步 plan.md Flyway 版本号

**文件**: `openspec/changes/circle-13-growth-incentive/plan.md`
**修改**: 全文搜索 `V3.9.1_63`，替换为 `V3.9.1_67`

---

#### T-3 排行榜周期常量提取

**文件**: `LeaderboardServiceImpl.java`
**修改**: 将硬编码 `{"WEEK", "MONTH", "ALL"}` 提取到 `LeaderboardPeriodEnum` 或 `GrowthConstant`

```java
// LeaderboardPeriodEnum.java 或 GrowthConstant.java
public static final List<String> LEADERBOARD_PERIODS = List.of("WEEK", "MONTH", "ALL");
```

---

## 三、前端待实现

### Phase 6: 规范修复 (P0 — 必须先于编码)

#### FE-2 修正 proposal.md API 路径 (BLOCK-001)

**文件**: `openspec/changes/circle-13-growth-incentive-frontend/proposal.md`

| 当前 (错误) | 修正后 |
|------------|--------|
| `GET /api/v1/content/circle/growth/info` | `GET /api/v1/content/circle/member_growth/info?circleId=&userId=` |
| `GET /api/v1/content/circle/growth/achievement/list` | `GET /api/v1/content/circle/growth/achievement/list?circleId=&userId=` |
| `GET /api/v1/content/circle/growth/leaderboard` | `GET /api/v1/content/circle/growth/leaderboard?circleId=&dimension=&period=&currentUserId=` |
| (缺失) | **补充** `GET /api/v1/content/circle/member_growth/participation?circleId=&userId=` |

---

#### FE-3 修正等级降级规则 (BLOCK-002)

**文件**: `openspec/changes/circle-13-growth-incentive-frontend/specs/member-growth/spec.md`

**修改**: 删除"等级下降展示" Scenario（第 95-97 行），替换为：

```
#### Scenario: 经验值扣减但等级不下降
- **WHEN** 经验值因内容删除/撤回/违规被扣减至低于当前等级门槛
- **THEN** 成员等级保持不变，仅经验值数值和进度条更新
- **AND** 页面提示「经验值已调整」
```

---

#### FE-4 12 个 FLAG 问题修复

| FLAG | 问题 | 修改位置 | 变更类型 |
|------|------|---------|---------|
| FLAG-001/013 | tasks.md 缺少 DoD 收尾项 | tasks.md | 补充第 9 分组 |
| FLAG-002 | proposal.md 成员成长接口路径不完整 | proposal.md | 已在 FE-2 一并修复 |
| FLAG-003 | WebSocket 消息体容错逻辑不明确 | design.md D4 + badge-system spec | 补充容错 Scenario |
| FLAG-004 | streakDetail 降级方案未定义 | design.md D9 + member-growth spec | 添加降级策略 |
| FLAG-005 | totalBadges 降级方案未定义 | design.md D9 | 添加降级策略 |
| FLAG-006 | 徽章撤销 UI 细节不明确 | badge-system spec | 补充样式细节 |
| FLAG-007 | 等级提升通知 Scenario 缺失 | member-growth spec + circle-level spec | 补充 Scenario |
| FLAG-008 | 排行榜更新时间提示缺失 | leaderboard spec | 补充提示 Scenario |
| FLAG-009 | 圈子已解散场景缺失 | 所有 spec | 补充错误场景 |
| FLAG-010 | 网络超时/断网差异化处理 | 所有 spec | 补充错误提示 |
| FLAG-011 | WebSocket 降级方案缺失 | design.md D4 | 补充轮询降级 |
| FLAG-012 | XSS 防护策略未明确 | design.md | 补充防护声明 |
| FLAG-014 | 排行榜参数名不一致 (type/range→dimension/period) | leaderboard spec | 统一参数名 |

---

### Phase 7: 前端编码实现 (P0 — 41 个 tasks)

> 详见 `openspec/changes/circle-13-growth-incentive-frontend/tasks.md`，共 8 个分组 64 个任务（含新增的第 9 分组质量门禁）。

**核心对接要点** (来自 PENDING-ISSUES §4.1):

| 对接项 | 后端字段 | 前端注意 |
|--------|---------|---------|
| 徽章状态 | `status`: `EARNED` / `CLOSE` / `UNEARNED` | `CLOSE` = 进度 >= 80% |
| 今日经验 | `MemberGrowthVO.todayExp` / `dailyExpLimit` | 直接使用 |
| 排行榜参数 | `dimension`: `EXP`/`CONTRIBUTION`/`POST`, `period`: `WEEK`/`MONTH`/`ALL` | 大写枚举值 |
| 权益列表 | `CircleLevelVO.benefits` = `CircleBenefitVO[]` | `{name, unlocked}` |
| 7天时间轴 | `ParticipationVO.dailyStatus` | 依赖 API-1 后端扩展 |

**文件组织**:
- API: `src/api/content/growth/circle.ts`
- Store: `src/store/modules/circleGrowth.ts`
- 组件: `src/components/circle/growth/`

---

## 四、执行顺序

```
Phase 1: T-1 (测试验证)
    ↓
Phase 2: DB-1 + DB-2 (数据库变更，可并行)
    ↓
Phase 3: API-1 (participation 扩展)
    ↓
Phase 4: VO-1 + VO-2 + VO-3 (VO 字段补充，可并行)
    ↓
Phase 5: T-2 + T-3 (代码整洁，可并行)
    ↓
Phase 6: FE-2 + FE-3 + FE-4 (前端规范修复，FE-2/FE-3 优先)
    ↓
Phase 7: 前端编码实现 (41 tasks 按 tasks.md 顺序)
```

---

## 五、验收标准 (总)

### 后端
- [ ] 全量单元测试 100% 通过 (28+ tests)
- [ ] `circle_achievement` 表含 6 条徽章配置
- [ ] `circle_invite_record` 表创建并集成
- [ ] `/participation` 返回 7 天每日状态
- [ ] `MemberGrowthVO` 含 `totalBadges`/`totalBadgeCount`
- [ ] Flyway 迁移脚本有对应的 rollback 脚本
- [ ] plan.md Flyway 版本号一致

### 前端
- [ ] proposal.md API 路径与 design.md/specs 一致
- [ ] 等级降级规则与后端一致（不降级）
- [ ] 12 个 FLAG 问题已修复
- [ ] tasks.md 含 DoD 收尾项
- [ ] 64 个 tasks 全部完成
- [ ] 响应式三断点验证通过

---

## 六、相关文档索引

| 文档 | 路径 |
|------|------|
| 后端验证报告 | `openspec/changes/circle-13-growth-incentive/verify.md` |
| 后端实现计划 | `openspec/changes/circle-13-growth-incentive/plan.md` |
| 后端任务清单 | `openspec/changes/circle-13-growth-incentive/tasks.md` |
| 前端综合待办 | `openspec/changes/circle-13-growth-incentive-frontend/PENDING-ISSUES.md` |
| 前端规范审核 | `openspec/changes/circle-13-growth-incentive-frontend/review-report.md` |
| API 规范审核 | `openspec/changes/circle-13-growth-incentive-frontend/circle-growth-api-conventions-audit-2026-06-25.md` |
| 前端 PRD | `docs/requirements/prd/frontend/EPIC-13-circle-growth-incentive-frontend-prd.md` |
| 后端 PRD | `docs/requirements/prd/decomposition/circle/EPIC-13-circle-growth-incentive.md` |
| 数据库 DDL | `V3.9.1_67__circle_growth_system.sql` |
