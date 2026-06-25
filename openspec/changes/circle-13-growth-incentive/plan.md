# 圈子成长激励 Implementation Plan

> **Verification Fixes (2026-06-19)**: 验证报告 W-2/W-5/W-6/W-7/S-1/S-2/CRITICAL#2 已修复，VO 字段已补充，WebSocket 实时推送已集成。

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 建立圈子等级、成员经验值/贡献值、成就徽章和排行榜体系，形成可感知、可持续的正向激励循环。

**Architecture:** 在 `jeecg-module-content` 的 `content/user/growth/` 子包下新增成长激励模块。圈子等级通过定时任务（30分钟）聚合计算，成员经验值通过行为事件实时写入并校验每日上限，排行榜通过定时任务（每小时）生成快照。徽章通过异步方法在成长行为完成后检查条件。

**Tech Stack:** Spring Boot, MyBatis-Plus, Flyway, JUnit 5 + Mockito + AssertJ

---

## Task 1: Flyway 迁移脚本与枚举

**Files:**
- Create: `jeecg-boot-module/jeecg-module-content/src/main/resources/flyway/sql/mysql/V3.9.1_63__circle_growth_system.sql`
- Create: `jeecg-boot-module/jeecg-module-content/src/main/resources/flyway/sql/mysql/R3.9.1_63__circle_growth_system_rollback.sql`
- Create: `src/main/java/org/jeecg/modules/content/user/growth/enums/CircleLevelEnum.java`
- Create: `src/main/java/org/jeecg/modules/content/user/growth/enums/GrowthActionEnum.java`
- Create: `src/main/java/org/jeecg/modules/content/user/growth/enums/AchievementTypeEnum.java`
- Create: `src/main/java/org/jeecg/modules/content/user/growth/enums/LeaderboardDimensionEnum.java`
- Create: `src/main/java/org/jeecg/modules/content/user/growth/constant/GrowthConstant.java`

- [ ] **Step 1: Write the Flyway migration script**

```sql
-- V3.9.1_63__circle_growth_system.sql

-- 圈子等级配置表
CREATE TABLE IF NOT EXISTS `circle_level` (
    `id` varchar(32) NOT NULL COMMENT '主键ID',
    `circle_id` varchar(32) NOT NULL COMMENT '圈子ID',
    `level` int NOT NULL DEFAULT 1 COMMENT '等级 1-5',
    `growth_score` int NOT NULL DEFAULT 0 COMMENT '成长分 0-1000',
    `member_score` int NOT NULL DEFAULT 0 COMMENT '成员规模得分',
    `content_score` int NOT NULL DEFAULT 0 COMMENT '内容贡献得分',
    `activity_score` int NOT NULL DEFAULT 0 COMMENT '活跃互动得分',
    `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
    `create_time` datetime DEFAULT NULL COMMENT '创建日期',
    `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
    `update_time` datetime DEFAULT NULL COMMENT '更新日期',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_circle_id` (`circle_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='圈子等级';

-- 成员成长记录表
CREATE TABLE IF NOT EXISTS `circle_member_growth` (
    `id` varchar(32) NOT NULL COMMENT '主键ID',
    `circle_id` varchar(32) NOT NULL COMMENT '圈子ID',
    `user_id` varchar(32) NOT NULL COMMENT '用户ID',
    `exp_points` int NOT NULL DEFAULT 0 COMMENT '经验值',
    `contribution_points` int NOT NULL DEFAULT 0 COMMENT '贡献值',
    `level` int NOT NULL DEFAULT 1 COMMENT '成员等级',
    `post_count` int NOT NULL DEFAULT 0 COMMENT '发帖数',
    `comment_count` int NOT NULL DEFAULT 0 COMMENT '评论数',
    `featured_count` int NOT NULL DEFAULT 0 COMMENT '精华数',
    `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
    `create_time` datetime DEFAULT NULL COMMENT '创建日期',
    `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
    `update_time` datetime DEFAULT NULL COMMENT '更新日期',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_circle_user` (`circle_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='圈子成员成长记录';

-- 成长行为流水表
CREATE TABLE IF NOT EXISTS `circle_growth_log` (
    `id` varchar(32) NOT NULL COMMENT '主键ID',
    `circle_id` varchar(32) NOT NULL COMMENT '圈子ID',
    `user_id` varchar(32) NOT NULL COMMENT '用户ID',
    `action_type` varchar(32) NOT NULL COMMENT '行为类型: POST/COMMENT/LIKE/FEATURED',
    `exp_points` int NOT NULL DEFAULT 0 COMMENT '获得经验值',
    `contribution_points` int NOT NULL DEFAULT 0 COMMENT '获得贡献值',
    `biz_id` varchar(32) DEFAULT NULL COMMENT '关联业务ID',
    `biz_date` date NOT NULL COMMENT '业务日期',
    `revoked` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否已撤销',
    `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
    `create_time` datetime DEFAULT NULL COMMENT '创建日期',
    `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
    `update_time` datetime DEFAULT NULL COMMENT '更新日期',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_circle_user_date_action_biz` (`circle_id`, `user_id`, `biz_date`, `action_type`, `biz_id`),
    KEY `idx_circle_user_date` (`circle_id`, `user_id`, `biz_date`),
    KEY `idx_circle_date` (`circle_id`, `biz_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='成长行为流水';

-- 成就徽章配置表
CREATE TABLE IF NOT EXISTS `circle_achievement` (
    `id` varchar(32) NOT NULL COMMENT '主键ID',
    `achievement_type` varchar(32) NOT NULL COMMENT '徽章类型',
    `name` varchar(64) NOT NULL COMMENT '徽章名称',
    `description` varchar(256) DEFAULT NULL COMMENT '徽章描述',
    `icon_url` varchar(256) DEFAULT NULL COMMENT '徽章图标',
    `condition_desc` varchar(256) DEFAULT NULL COMMENT '达成条件描述',
    `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
    `create_time` datetime DEFAULT NULL COMMENT '创建日期',
    `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
    `update_time` datetime DEFAULT NULL COMMENT '更新日期',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_achievement_type` (`achievement_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='成就徽章配置';

-- 成员已获得徽章表
CREATE TABLE IF NOT EXISTS `circle_member_achievement` (
    `id` varchar(32) NOT NULL COMMENT '主键ID',
    `circle_id` varchar(32) NOT NULL COMMENT '圈子ID',
    `user_id` varchar(32) NOT NULL COMMENT '用户ID',
    `achievement_type` varchar(32) NOT NULL COMMENT '徽章类型',
    `revoked` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否已撤销',
    `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
    `create_time` datetime DEFAULT NULL COMMENT '创建日期',
    `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
    `update_time` datetime DEFAULT NULL COMMENT '更新日期',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_circle_user_achievement` (`circle_id`, `user_id`, `achievement_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='成员已获得徽章';

-- 排行榜快照表
CREATE TABLE IF NOT EXISTS `circle_leaderboard_snapshot` (
    `id` varchar(32) NOT NULL COMMENT '主键ID',
    `circle_id` varchar(32) NOT NULL COMMENT '圈子ID',
    `user_id` varchar(32) NOT NULL COMMENT '用户ID',
    `dimension` varchar(32) NOT NULL COMMENT '维度: EXP/CONTRIBUTION/POST',
    `period` varchar(16) NOT NULL COMMENT '周期: WEEK/MONTH/ALL',
    `score` int NOT NULL DEFAULT 0 COMMENT '得分',
    `rank_num` int NOT NULL DEFAULT 0 COMMENT '排名',
    `snapshot_time` datetime NOT NULL COMMENT '快照时间',
    `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
    `create_time` datetime DEFAULT NULL COMMENT '创建日期',
    `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
    `update_time` datetime DEFAULT NULL COMMENT '更新日期',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_circle_dimension_period_user` (`circle_id`, `dimension`, `period`, `user_id`),
    KEY `idx_circle_dimension_period_rank` (`circle_id`, `dimension`, `period`, `rank_num`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='排行榜快照';
```

- [ ] **Step 2: Write the rollback script**

```sql
-- R3.9.1_63__circle_growth_system_rollback.sql
DROP TABLE IF EXISTS `circle_leaderboard_snapshot`;
DROP TABLE IF EXISTS `circle_member_achievement`;
DROP TABLE IF EXISTS `circle_achievement`;
DROP TABLE IF EXISTS `circle_growth_log`;
DROP TABLE IF EXISTS `circle_member_growth`;
DROP TABLE IF EXISTS `circle_level`;
```

- [ ] **Step 3: Create the enum classes**

```java
// CircleLevelEnum.java
@Getter
@RequiredArgsConstructor
public enum CircleLevelEnum {
    L1(1, "新芽圈", 0),
    L2(2, "活跃圈", 100),
    L3(3, "优质圈", 300),
    L4(4, "热门圈", 600),
    L5(5, "标杆圈", 850);

    private final int level;
    private final String name;
    private final int threshold;

    public static CircleLevelEnum ofScore(int score) {
        CircleLevelEnum result = L1;
        for (CircleLevelEnum e : values()) {
            if (score >= e.threshold) {
                result = e;
            }
        }
        return result;
    }
}
```

```java
// GrowthActionEnum.java
@Getter
@RequiredArgsConstructor
public enum GrowthActionEnum {
    POST("POST", "发帖", 10, 10),
    COMMENT("COMMENT", "评论", 3, 3),
    LIKE("LIKE", "点赞", 0, 0),
    FEATURED("FEATURED", "加精", 30, 50);

    private final String code;
    private final String description;
    private final int expPoints;
    private final int contributionPoints;
}
```

```java
// AchievementTypeEnum.java
@Getter
@RequiredArgsConstructor
public enum AchievementTypeEnum {
    CONTINUOUS_CREATOR("CONTINUOUS_CREATOR", "持续创作者"),
    QUALITY_CONTRIBUTOR("QUALITY_CONTRIBUTOR", "优质贡献者"),
    ACTIVE_PARTICIPANT("ACTIVE_PARTICIPANT", "活跃参与者"),
    RISING_STAR("RISING_STAR", "圈内新星");

    private final String code;
    private final String description;
}
```

```java
// LeaderboardDimensionEnum.java
@Getter
@RequiredArgsConstructor
public enum LeaderboardDimensionEnum {
    EXP("EXP", "经验值"),
    CONTRIBUTION("CONTRIBUTION", "贡献值"),
    POST("POST", "发帖数");

    private final String code;
    private final String description;
}
```

- [ ] **Step 4: Create the constant class**

```java
// GrowthConstant.java
public final class GrowthConstant {
    private GrowthConstant() {}

    public static final int DAILY_EXP_CAP = 100;
    public static final int[] LEVEL_THRESHOLDS = {0, 100, 300, 600, 850};
    public static final int MAX_GROWTH_SCORE = 1000;
    public static final int LEADERBOARD_TOP_N = 50;
}
```

- [ ] **Step 5: Verify compilation**

Run: `mvn compile -pl jeecg-boot-module/jeecg-module-content -am -q`
Expected: BUILD SUCCESS

- [ ] **Step 6: Commit**

```bash
git add jeecg-boot-module/jeecg-module-content/src/main/resources/flyway/sql/mysql/V3.9.1_63__circle_growth_system.sql \
        jeecg-boot-module/jeecg-module-content/src/main/resources/flyway/sql/mysql/R3.9.1_63__circle_growth_system_rollback.sql \
        jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/growth/enums/ \
        jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/growth/constant/
git commit -m "feat(growth): add Flyway migration and enums for circle growth system"
```

---

## Task 2: 成员经验与贡献值 — 实体与 Mapper

**Files:**
- Create: `src/main/java/org/jeecg/modules/content/user/growth/entity/CircleMemberGrowth.java`
- Create: `src/main/java/org/jeecg/modules/content/user/growth/entity/CircleGrowthLog.java`
- Create: `src/main/java/org/jeecg/modules/content/user/growth/mapper/CircleMemberGrowthMapper.java`
- Create: `src/main/java/org/jeecg/modules/content/user/growth/mapper/CircleGrowthLogMapper.java`

- [ ] **Step 1: Create CircleMemberGrowth entity**

```java
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("circle_member_growth")
@Schema(description = "圈子成员成长记录")
public class CircleMemberGrowth extends JeecgEntity {
    @Schema(description = "圈子ID")
    private String circleId;
    @Schema(description = "用户ID")
    private String userId;
    @Schema(description = "经验值")
    private Integer expPoints;
    @Schema(description = "贡献值")
    private Integer contributionPoints;
    @Schema(description = "成员等级")
    private Integer level;
    @Schema(description = "发帖数")
    private Integer postCount;
    @Schema(description = "评论数")
    private Integer commentCount;
    @Schema(description = "精华数")
    private Integer featuredCount;
}
```

- [ ] **Step 2: Create CircleGrowthLog entity**

```java
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("circle_growth_log")
@Schema(description = "成长行为流水")
public class CircleGrowthLog extends JeecgEntity {
    @Schema(description = "圈子ID")
    private String circleId;
    @Schema(description = "用户ID")
    private String userId;
    @Schema(description = "行为类型")
    private String actionType;
    @Schema(description = "获得经验值")
    private Integer expPoints;
    @Schema(description = "获得贡献值")
    private Integer contributionPoints;
    @Schema(description = "关联业务ID")
    private String bizId;
    @Schema(description = "业务日期")
    private LocalDate bizDate;
    @Schema(description = "是否已撤销")
    private Boolean revoked;
}
```

- [ ] **Step 3: Create Mapper interfaces**

```java
public interface CircleMemberGrowthMapper extends BaseMapper<CircleMemberGrowth> {
}
```

```java
public interface CircleGrowthLogMapper extends BaseMapper<CircleGrowthLog> {
}
```

- [ ] **Step 4: Verify compilation**

Run: `mvn compile -pl jeecg-boot-module/jeecg-module-content -am -q`
Expected: BUILD SUCCESS

- [ ] **Step 5: Commit**

```bash
git add jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/growth/entity/ \
        jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/growth/mapper/
git commit -m "feat(growth): add CircleMemberGrowth and CircleGrowthLog entities with mappers"
```

---

## Task 3: 成员经验与贡献值 — Service 实现与测试

**Files:**
- Create: `src/main/java/org/jeecg/modules/content/user/growth/service/IMemberGrowthService.java`
- Create: `src/main/java/org/jeecg/modules/content/user/growth/service/impl/MemberGrowthServiceImpl.java`
- Create: `src/test/java/org/jeecg/modules/content/user/growth/service/MemberGrowthServiceTest.java`

- [ ] **Step 1: Write the failing test for addExperience**

```java
@ExtendWith(MockitoExtension.class)
class MemberGrowthServiceTest {
    @Mock private CircleMemberGrowthMapper growthMapper;
    @Mock private CircleGrowthLogMapper growthLogMapper;
    @InjectMocks private MemberGrowthServiceImpl service;

    private static final String CIRCLE_ID = "circle1";
    private static final String USER_ID = "user1";

    @Test
    @DisplayName("发帖成功后获得10点经验值和10点贡献值")
    void addExperience_post_success() {
        when(growthMapper.selectOne(any())).thenReturn(null);
        when(growthLogMapper.selectCount(any())).thenReturn(0L);

        service.addExperience(CIRCLE_ID, USER_ID, GrowthActionEnum.POST, "post1");

        ArgumentCaptor<CircleMemberGrowth> growthCaptor = ArgumentCaptor.forClass(CircleMemberGrowth.class);
        verify(growthMapper).insert(growthCaptor.capture());
        assertThat(growthCaptor.getValue().getExpPoints()).isEqualTo(10);
        assertThat(growthCaptor.getValue().getContributionPoints()).isEqualTo(10);

        ArgumentCaptor<CircleGrowthLog> logCaptor = ArgumentCaptor.forClass(CircleGrowthLog.class);
        verify(growthLogMapper).insert(logCaptor.capture());
        assertThat(logCaptor.getValue().getExpPoints()).isEqualTo(10);
        assertThat(logCaptor.getValue().getRevoked()).isFalse();
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn test -pl jeecg-boot-module/jeecg-module-content -Dtest=MemberGrowthServiceTest#addExperience_post_success -q`
Expected: FAIL — `MemberGrowthServiceImpl` does not exist

- [ ] **Step 3: Create the service interface**

```java
public interface IMemberGrowthService extends IService<CircleMemberGrowth> {
    void addExperience(String circleId, String userId, GrowthActionEnum action, String bizId);
    void revokeExperience(String circleId, String userId, GrowthActionEnum action, String bizId);
    MemberGrowthVO getGrowthInfo(String circleId, String userId);
}
```

- [ ] **Step 4: Implement MemberGrowthServiceImpl.addExperience**

```java
@Slf4j
@Service
public class MemberGrowthServiceImpl extends ServiceImpl<CircleMemberGrowthMapper, CircleMemberGrowth>
        implements IMemberGrowthService {

    @Resource
    private CircleGrowthLogMapper growthLogMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addExperience(String circleId, String userId, GrowthActionEnum action, String bizId) {
        LocalDate today = LocalDate.now();

        // 每日上限校验
        if (isDailyCapReached(circleId, userId, today)) {
            log.info("用户{}在圈子{}今日经验值已达上限", userId, circleId);
            return;
        }

        // 写入流水
        CircleGrowthLog growthLog = new CircleGrowthLog()
                .setCircleId(circleId)
                .setUserId(userId)
                .setActionType(action.getCode())
                .setExpPoints(action.getExpPoints())
                .setContributionPoints(action.getContributionPoints())
                .setBizId(bizId)
                .setBizDate(today)
                .setRevoked(false);
        growthLogMapper.insert(growthLog);

        // 更新成长记录
        CircleMemberGrowth growth = getOrCreateGrowth(circleId, userId);
        growth.setExpPoints(growth.getExpPoints() + action.getExpPoints());
        growth.setContributionPoints(growth.getContributionPoints() + action.getContributionPoints());
        if (action == GrowthActionEnum.POST) {
            growth.setPostCount(growth.getPostCount() + 1);
        } else if (action == GrowthActionEnum.COMMENT) {
            growth.setCommentCount(growth.getCommentCount() + 1);
        } else if (action == GrowthActionEnum.FEATURED) {
            growth.setFeaturedCount(growth.getFeaturedCount() + 1);
        }
        this.saveOrUpdate(growth);
    }

    private boolean isDailyCapReached(String circleId, String userId, LocalDate date) {
        QueryWrapper<CircleGrowthLog> qw = new QueryWrapper<>();
        qw.eq("circle_id", circleId)
          .eq("user_id", userId)
          .eq("biz_date", date)
          .eq("revoked", false);
        Long dailyTotal = growthLogMapper.selectSumExp(qw);
        return dailyTotal != null && dailyTotal >= GrowthConstant.DAILY_EXP_CAP;
    }

    private CircleMemberGrowth getOrCreateGrowth(String circleId, String userId) {
        QueryWrapper<CircleMemberGrowth> qw = new QueryWrapper<>();
        qw.eq("circle_id", circleId).eq("user_id", userId);
        CircleMemberGrowth growth = this.getOne(qw);
        if (growth == null) {
            growth = new CircleMemberGrowth()
                    .setCircleId(circleId)
                    .setUserId(userId)
                    .setExpPoints(0)
                    .setContributionPoints(0)
                    .setLevel(1)
                    .setPostCount(0)
                    .setCommentCount(0)
                    .setFeaturedCount(0);
        }
        return growth;
    }
}
```

- [ ] **Step 5: Run test to verify it passes**

Run: `mvn test -pl jeecg-boot-module/jeecg-module-content -Dtest=MemberGrowthServiceTest#addExperience_post_success -q`
Expected: PASS

- [ ] **Step 6: Write test for daily cap**

```java
@Test
@DisplayName("每日经验值达到100点上限后不再增加")
void addExperience_dailyCapReached_noExpAdded() {
    when(growthLogMapper.selectSumExp(any())).thenReturn(100L);

    service.addExperience(CIRCLE_ID, USER_ID, GrowthActionEnum.POST, "post2");

    verify(growthLogMapper, never()).insert(any());
}
```

- [ ] **Step 7: Run test for daily cap**

Run: `mvn test -pl jeecg-boot-module/jeecg-module-content -Dtest=MemberGrowthServiceTest#addExperience_dailyCapReached_noExpAdded -q`
Expected: PASS

- [ ] **Step 8: Write test for revokeExperience**

```java
@Test
@DisplayName("内容删除后回退对应经验值和贡献值")
void revokeExperience_success() {
    CircleGrowthLog existingLog = new CircleGrowthLog()
            .setCircleId(CIRCLE_ID).setUserId(USER_ID)
            .setActionType(GrowthActionEnum.POST.getCode())
            .setExpPoints(10).setContributionPoints(10)
            .setBizId("post1").setBizDate(LocalDate.now()).setRevoked(false);
    when(growthLogMapper.selectOne(any())).thenReturn(existingLog);

    CircleMemberGrowth growth = new CircleMemberGrowth()
            .setCircleId(CIRCLE_ID).setUserId(USER_ID)
            .setExpPoints(20).setContributionPoints(20).setLevel(1)
            .setPostCount(2).setCommentCount(0).setFeaturedCount(0);
    when(growthMapper.selectOne(any())).thenReturn(growth);

    service.revokeExperience(CIRCLE_ID, USER_ID, GrowthActionEnum.POST, "post1");

    assertThat(growth.getExpPoints()).isEqualTo(10);
    assertThat(growth.getContributionPoints()).isEqualTo(10);
    assertThat(existingLog.getRevoked()).isTrue();
}
```

- [ ] **Step 9: Run all MemberGrowthServiceTest tests**

Run: `mvn test -pl jeecg-boot-module/jeecg-module-content -Dtest=MemberGrowthServiceTest -q`
Expected: ALL PASS

- [ ] **Step 10: Commit**

```bash
git add jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/growth/service/ \
        jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/growth/service/MemberGrowthServiceTest.java
git commit -m "feat(growth): implement MemberGrowthService with exp add/revoke and daily cap"
```

---

## Task 4: 连续参与进度

**Files:**
- Modify: `src/main/java/org/jeecg/modules/content/user/growth/service/IMemberGrowthService.java`
- Modify: `src/main/java/org/jeecg/modules/content/user/growth/service/impl/MemberGrowthServiceImpl.java`
- Modify: `src/test/java/org/jeecg/modules/content/user/growth/service/MemberGrowthServiceTest.java`

- [ ] **Step 1: Write the failing test for recordParticipation**

```java
@Test
@DisplayName("成员完成有效参与行为后当日标记为已参与")
void recordParticipation_marksToday() {
    when(growthLogMapper.selectCount(any())).thenReturn(1L);

    int days = service.getParticipationDays(CIRCLE_ID, USER_ID);

    assertThat(days).isGreaterThanOrEqualTo(1);
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn test -pl jeecg-boot-module/jeecg-module-content -Dtest=MemberGrowthServiceTest#recordParticipation_marksToday -q`
Expected: FAIL — `getParticipationDays` not found

- [ ] **Step 3: Add methods to IMemberGrowthService**

```java
int getParticipationDays(String circleId, String userId);
```

- [ ] **Step 4: Implement getParticipationDays**

```java
@Override
public int getParticipationDays(String circleId, String userId) {
    LocalDate today = LocalDate.now();
    LocalDate weekAgo = today.minusDays(6);
    QueryWrapper<CircleGrowthLog> qw = new QueryWrapper<>();
    qw.eq("circle_id", circleId)
      .eq("user_id", userId)
      .eq("revoked", false)
      .between("biz_date", weekAgo, today)
      .select("COUNT(DISTINCT biz_date)");
    Long count = growthLogMapper.selectCount(qw);
    return count == null ? 0 : count.intValue();
}
```

- [ ] **Step 5: Run test to verify it passes**

Run: `mvn test -pl jeecg-boot-module/jeecg-module-content -Dtest=MemberGrowthServiceTest#recordParticipation_marksToday -q`
Expected: PASS

- [ ] **Step 6: Write test for empty state (no participation)**

```java
@Test
@DisplayName("近7天无参与行为时返回0天")
void getParticipationDays_noActivity_returnsZero() {
    when(growthLogMapper.selectCount(any())).thenReturn(0L);

    int days = service.getParticipationDays(CIRCLE_ID, USER_ID);

    assertThat(days).isEqualTo(0);
}
```

- [ ] **Step 7: Run all MemberGrowthServiceTest**

Run: `mvn test -pl jeecg-boot-module/jeecg-module-content -Dtest=MemberGrowthServiceTest -q`
Expected: ALL PASS

- [ ] **Step 8: Commit**

```bash
git add jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/growth/service/ \
        jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/growth/service/MemberGrowthServiceTest.java
git commit -m "feat(growth): add continuous participation tracking"
```

---

## Task 5: MemberGrowthController 与 VO

**Files:**
- Create: `src/main/java/org/jeecg/modules/content/user/growth/vo/MemberGrowthVO.java`
- Create: `src/main/java/org/jeecg/modules/content/user/growth/controller/MemberGrowthController.java`

- [x] **Step 1: Create MemberGrowthVO**

```java
@Data
@Schema(description = "成员成长信息")
public class MemberGrowthVO {
    @Schema(description = "圈子ID")
    private String circleId;
    @Schema(description = "经验值")
    private Integer expPoints;
    @Schema(description = "贡献值")
    private Integer contributionPoints;
    @Schema(description = "成员等级")
    private Integer level;
    @Schema(description = "发帖数")
    private Integer postCount;
    @Schema(description = "连续参与天数")
    private Integer participationDays;
    @Schema(description = "圈内排名")
    private Integer rank;
    @Schema(description = "下一等级门槛")
    private Integer nextLevelThreshold;
    @Schema(description = "等级进度百分比")
    private Integer progressPercent;
    @Schema(description = "今日已获经验值")
    private Integer todayExp;
    @Schema(description = "每日经验值上限")
    private Integer dailyExpLimit;
    @Schema(description = "最近获得的徽章（最多3枚）")
    private List<AchievementVO> recentBadges;
}
```

- [ ] **Step 2: Create MemberGrowthController**

```java
@Tag(name = "成员成长信息")
@RestController
@RequestMapping("/api/v1/content/user/growth")
public class MemberGrowthController {
    @Resource
    private IMemberGrowthService memberGrowthService;

    @Operation(summary = "获取成员在圈子的成长信息")
    @GetMapping("/info")
    public Result<MemberGrowthVO> getGrowthInfo(
            @RequestParam String circleId,
            @RequestParam String userId) {
        return Result.OK(memberGrowthService.getGrowthInfo(circleId, userId));
    }

    @Operation(summary = "获取连续参与进度")
    @GetMapping("/participation")
    public Result<Integer> getParticipationDays(
            @RequestParam String circleId,
            @RequestParam String userId) {
        return Result.OK(memberGrowthService.getParticipationDays(circleId, userId));
    }
}
```

- [ ] **Step 3: Verify compilation**

Run: `mvn compile -pl jeecg-boot-module/jeecg-module-content -am -q`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/growth/vo/ \
        jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/growth/controller/
git commit -m "feat(growth): add MemberGrowthController and VO"
```

---

## Task 6: 圈子等级系统

**Files:**
- Create: `src/main/java/org/jeecg/modules/content/user/growth/entity/CircleLevel.java`
- Create: `src/main/java/org/jeecg/modules/content/user/growth/mapper/CircleLevelMapper.java`
- Create: `src/main/java/org/jeecg/modules/content/user/growth/service/ICircleLevelService.java`
- Create: `src/main/java/org/jeecg/modules/content/user/growth/service/impl/CircleLevelServiceImpl.java`
- Create: `src/main/java/org/jeecg/modules/content/user/growth/vo/CircleLevelVO.java`
- Create: `src/main/java/org/jeecg/modules/content/user/growth/controller/CircleLevelController.java`
- Create: `src/test/java/org/jeecg/modules/content/user/growth/service/CircleLevelServiceTest.java`

- [ ] **Step 1: Write the failing test for level calculation**

```java
@ExtendWith(MockitoExtension.class)
class CircleLevelServiceTest {
    @Mock private CircleLevelMapper levelMapper;
    @Mock private CircleMemberGrowthMapper growthMapper;
    @Mock private IContentNotificationService notificationService;
    @InjectMocks private CircleLevelServiceImpl service;

    @Test
    @DisplayName("成长分达到L2门槛时等级提升为L2")
    void updateLevel_score100_promotesToL2() {
        CircleLevel level = new CircleLevel()
                .setCircleId("circle1").setLevel(1).setGrowthScore(100)
                .setMemberScore(40).setContentScore(30).setActivityScore(30);
        when(levelMapper.selectOne(any())).thenReturn(level);

        service.updateLevel("circle1");

        assertThat(level.getLevel()).isEqualTo(2);
        verify(levelMapper).updateById(level);
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn test -pl jeecg-boot-module/jeecg-module-content -Dtest=CircleLevelServiceTest#updateLevel_score100_promotesToL2 -q`
Expected: FAIL — `CircleLevelServiceImpl` does not exist

- [ ] **Step 3: Create CircleLevel entity and mapper**

```java
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("circle_level")
@Schema(description = "圈子等级")
public class CircleLevel extends JeecgEntity {
    @Schema(description = "圈子ID")
    private String circleId;
    @Schema(description = "等级 1-5")
    private Integer level;
    @Schema(description = "成长分 0-1000")
    private Integer growthScore;
    @Schema(description = "成员规模得分")
    private Integer memberScore;
    @Schema(description = "内容贡献得分")
    private Integer contentScore;
    @Schema(description = "活跃互动得分")
    private Integer activityScore;
}
```

```java
public interface CircleLevelMapper extends BaseMapper<CircleLevel> {
}
```

- [ ] **Step 4: Create ICircleLevelService and CircleLevelServiceImpl**

```java
public interface ICircleLevelService extends IService<CircleLevel> {
    void calculateGrowthScore(String circleId);
    void updateLevel(String circleId);
    CircleLevelVO getLevelInfo(String circleId);
}
```

```java
@Slf4j
@Service
public class CircleLevelServiceImpl extends ServiceImpl<CircleLevelMapper, CircleLevel>
        implements ICircleLevelService {

    @Resource
    private CircleMemberGrowthMapper growthMapper;
    @Resource
    private IContentNotificationService notificationService;

    @Override
    public void calculateGrowthScore(String circleId) {
        // 聚合成员规模、内容贡献、活跃互动得分
        int memberScore = calculateMemberScore(circleId);
        int contentScore = calculateContentScore(circleId);
        int activityScore = calculateActivityScore(circleId);
        int total = Math.min(memberScore + contentScore + activityScore, GrowthConstant.MAX_GROWTH_SCORE);

        CircleLevel level = getOrCreateLevel(circleId);
        int oldLevel = level.getLevel();
        level.setMemberScore(memberScore)
             .setContentScore(contentScore)
             .setActivityScore(activityScore)
             .setGrowthScore(total);
        this.saveOrUpdate(level);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateLevel(String circleId) {
        CircleLevel level = getOrCreateLevel(circleId);
        CircleLevelEnum newLevelEnum = CircleLevelEnum.ofScore(level.getGrowthScore());
        int oldLevel = level.getLevel();

        if (newLevelEnum.getLevel() > oldLevel) {
            level.setLevel(newLevelEnum.getLevel());
            this.updateById(level);
            notifyLevelUpgrade(circleId, newLevelEnum);
            log.info("圈子{}等级从L{}提升为L{}", circleId, oldLevel, newLevelEnum.getLevel());
        }
    }

    private void notifyLevelUpgrade(String circleId, CircleLevelEnum newLevel) {
        // 调用已有通知服务
        // notificationService.send(...)
    }

    private CircleLevel getOrCreateLevel(String circleId) {
        QueryWrapper<CircleLevel> qw = new QueryWrapper<>();
        qw.eq("circle_id", circleId);
        CircleLevel level = this.getOne(qw);
        if (level == null) {
            level = new CircleLevel()
                    .setCircleId(circleId)
                    .setLevel(1)
                    .setGrowthScore(0)
                    .setMemberScore(0)
                    .setContentScore(0)
                    .setActivityScore(0);
        }
        return level;
    }
}
```

- [ ] **Step 5: Run test to verify it passes**

Run: `mvn test -pl jeecg-boot-module/jeecg-module-content -Dtest=CircleLevelServiceTest#updateLevel_score100_promotesToL2 -q`
Expected: PASS

- [x] **Step 6: Create CircleLevelVO, LevelConditionVO, and CircleLevelController**

```java
@Data
@Schema(description = "圈子等级信息")
public class CircleLevelVO {
    @Schema(description = "当前等级")
    private Integer level;
    @Schema(description = "等级名称")
    private String levelName;
    @Schema(description = "成长分")
    private Integer growthScore;
    @Schema(description = "下一等级门槛")
    private Integer nextLevelThreshold;
    @Schema(description = "进度百分比")
    private Integer progressPercent;
    @Schema(description = "已解锁权益列表")
    private List<CircleBenefitVO> benefits;  // {name, unlocked}
    @Schema(description = "成员规模得分")
    private Integer memberScore;
    @Schema(description = "内容贡献得分")
    private Integer contentScore;
    @Schema(description = "活跃互动得分")
    private Integer activityScore;
    @Schema(description = "下一等级各项条件")
    private List<LevelConditionVO> nextLevelConditions;
}
```

```java
@Data
@Schema(description = "等级条件项")
public class LevelConditionVO {
    private String type;      // MEMBER / CONTENT / INTERACTION
    private String label;
    private Integer current;
    private Integer required;  // 维度上限
    private Integer gap;       // required - current
}
```

```java
@Tag(name = "圈子等级信息")
@RestController
@RequestMapping("/api/v1/content/user/growth/level")
public class CircleLevelController {
    @Resource
    private ICircleLevelService circleLevelService;

    @Operation(summary = "获取圈子等级信息")
    @GetMapping("/info")
    public Result<CircleLevelVO> getLevelInfo(@RequestParam String circleId) {
        return Result.OK(circleLevelService.getLevelInfo(circleId));
    }
}
```

- [ ] **Step 7: Run all CircleLevelServiceTest**

Run: `mvn test -pl jeecg-boot-module/jeecg-module-content -Dtest=CircleLevelServiceTest -q`
Expected: ALL PASS

- [ ] **Step 8: Commit**

```bash
git add jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/growth/entity/CircleLevel.java \
        jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/growth/mapper/CircleLevelMapper.java \
        jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/growth/service/ \
        jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/growth/vo/CircleLevelVO.java \
        jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/growth/controller/CircleLevelController.java \
        jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/growth/service/CircleLevelServiceTest.java
git commit -m "feat(growth): implement circle level system with calculation and upgrade"
```

---

## Task 7: 成就徽章系统

**Files:**
- Create: `src/main/java/org/jeecg/modules/content/user/growth/entity/CircleAchievement.java`
- Create: `src/main/java/org/jeecg/modules/content/user/growth/entity/CircleMemberAchievement.java`
- Create: `src/main/java/org/jeecg/modules/content/user/growth/mapper/CircleAchievementMapper.java`
- Create: `src/main/java/org/jeecg/modules/content/user/growth/mapper/CircleMemberAchievementMapper.java`
- Create: `src/main/java/org/jeecg/modules/content/user/growth/service/IAchievementService.java`
- Create: `src/main/java/org/jeecg/modules/content/user/growth/service/impl/AchievementServiceImpl.java`
- Create: `src/main/java/org/jeecg/modules/content/user/growth/vo/AchievementVO.java`
- Create: `src/main/java/org/jeecg/modules/content/user/growth/controller/AchievementController.java`
- Create: `src/test/java/org/jeecg/modules/content/user/growth/service/AchievementServiceTest.java`

- [ ] **Step 1: Write the failing test for badge award**

```java
@ExtendWith(MockitoExtension.class)
class AchievementServiceTest {
    @Mock private CircleAchievementMapper achievementMapper;
    @Mock private CircleMemberAchievementMapper memberAchievementMapper;
    @Mock private CircleMemberGrowthMapper growthMapper;
    @Mock private IContentNotificationService notificationService;
    @InjectMocks private AchievementServiceImpl service;

    @Test
    @DisplayName("累计发布10篇可见内容后获得持续创作者徽章")
    void checkAndAward_continuousCreator_awardsBadge() {
        CircleMemberGrowth growth = new CircleMemberGrowth()
                .setCircleId("c1").setUserId("u1").setPostCount(10);
        when(growthMapper.selectOne(any())).thenReturn(growth);
        when(memberAchievementMapper.selectOne(any())).thenReturn(null);

        service.checkAndAward("c1", "u1");

        ArgumentCaptor<CircleMemberAchievement> captor = ArgumentCaptor.forClass(CircleMemberAchievement.class);
        verify(memberAchievementMapper).insert(captor.capture());
        assertThat(captor.getValue().getAchievementType())
                .isEqualTo(AchievementTypeEnum.CONTINUOUS_CREATOR.getCode());
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn test -pl jeecg-boot-module/jeecg-module-content -Dtest=AchievementServiceTest#checkAndAward_continuousCreator_awardsBadge -q`
Expected: FAIL

- [ ] **Step 3: Create entities, mappers, service interface**

```java
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("circle_achievement")
@Schema(description = "成就徽章配置")
public class CircleAchievement extends JeecgEntity {
    @Schema(description = "徽章类型")
    private String achievementType;
    @Schema(description = "徽章名称")
    private String name;
    @Schema(description = "徽章描述")
    private String description;
    @Schema(description = "徽章图标")
    private String iconUrl;
    @Schema(description = "达成条件描述")
    private String conditionDesc;
}
```

```java
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("circle_member_achievement")
@Schema(description = "成员已获得徽章")
public class CircleMemberAchievement extends JeecgEntity {
    @Schema(description = "圈子ID")
    private String circleId;
    @Schema(description = "用户ID")
    private String userId;
    @Schema(description = "徽章类型")
    private String achievementType;
    @Schema(description = "是否已撤销")
    private Boolean revoked;
}
```

```java
public interface CircleAchievementMapper extends BaseMapper<CircleAchievement> {}
public interface CircleMemberAchievementMapper extends BaseMapper<CircleMemberAchievement> {}
```

```java
public interface IAchievementService extends IService<CircleMemberAchievement> {
    void checkAndAward(String circleId, String userId);
    void revoke(String circleId, String userId, AchievementTypeEnum type);
    List<AchievementVO> getMemberAchievements(String circleId, String userId);
}
```

- [ ] **Step 4: Implement AchievementServiceImpl.checkAndAward**

```java
@Slf4j
@Service
public class AchievementServiceImpl extends ServiceImpl<CircleMemberAchievementMapper, CircleMemberAchievement>
        implements IAchievementService {

    @Resource
    private CircleMemberGrowthMapper growthMapper;
    @Resource
    private IContentNotificationService notificationService;

    @Override
    @Async
    @Transactional(rollbackFor = Exception.class)
    public void checkAndAward(String circleId, String userId) {
        CircleMemberGrowth growth = getGrowth(circleId, userId);
        if (growth == null) return;

        // 持续创作者：10篇
        if (growth.getPostCount() >= 10) {
            tryAward(circleId, userId, AchievementTypeEnum.CONTINUOUS_CREATOR);
        }
        // 优质贡献者：5篇精华
        if (growth.getFeaturedCount() >= 5) {
            tryAward(circleId, userId, AchievementTypeEnum.QUALITY_CONTRIBUTOR);
        }
        // 活跃参与者：近7天3天参与
        // (需要调用 participationDays)
    }

    private void tryAward(String circleId, String userId, AchievementTypeEnum type) {
        QueryWrapper<CircleMemberAchievement> qw = new QueryWrapper<>();
        qw.eq("circle_id", circleId)
          .eq("user_id", userId)
          .eq("achievement_type", type.getCode())
          .eq("revoked", false);
        if (this.count(qw) > 0) return; // 已获得

        CircleMemberAchievement achievement = new CircleMemberAchievement()
                .setCircleId(circleId)
                .setUserId(userId)
                .setAchievementType(type.getCode())
                .setRevoked(false);
        this.save(achievement);
        // 发送通知
    }
}
```

- [ ] **Step 5: Run test to verify it passes**

Run: `mvn test -pl jeecg-boot-module/jeecg-module-content -Dtest=AchievementServiceTest#checkAndAward_continuousCreator_awardsBadge -q`
Expected: PASS

- [ ] **Step 6: Write test for duplicate award (idempotent)**

```java
@Test
@DisplayName("已获得的徽章不会重复发放")
void checkAndAward_alreadyAwarded_noDuplicate() {
    CircleMemberGrowth growth = new CircleMemberGrowth()
            .setCircleId("c1").setUserId("u1").setPostCount(10);
    when(growthMapper.selectOne(any())).thenReturn(growth);
    when(memberAchievementMapper.selectCount(any())).thenReturn(1L);

    service.checkAndAward("c1", "u1");

    verify(memberAchievementMapper, never()).insert(any());
}
```

- [ ] **Step 7: Write test for badge revocation**

```java
@Test
@DisplayName("内容违规后撤销对应徽章")
void revoke_success() {
    CircleMemberAchievement achievement = new CircleMemberAchievement()
            .setCircleId("c1").setUserId("u1")
            .setAchievementType(AchievementTypeEnum.CONTINUOUS_CREATOR.getCode())
            .setRevoked(false);
    when(memberAchievementMapper.selectOne(any())).thenReturn(achievement);

    service.revoke("c1", "u1", AchievementTypeEnum.CONTINUOUS_CREATOR);

    assertThat(achievement.getRevoked()).isTrue();
    verify(memberAchievementMapper).updateById(achievement);
}
```

- [ ] **Step 8: Run all AchievementServiceTest**

Run: `mvn test -pl jeecg-boot-module/jeecg-module-content -Dtest=AchievementServiceTest -q`
Expected: ALL PASS

- [x] **Step 9: Create AchievementVO and AchievementController**

```java
@Data
@Schema(description = "成就徽章信息")
public class AchievementVO {
    @Schema(description = "徽章类型")
    private String achievementType;
    @Schema(description = "徽章名称")
    private String name;
    @Schema(description = "徽章描述")
    private String description;
    @Schema(description = "徽章图标URL")
    private String iconUrl;
    @Schema(description = "是否已获得")
    private Boolean earned;
    @Schema(description = "获得时间")
    private Date earnedDate;
    @Schema(description = "达成条件描述")
    private String conditionDesc;
    @Schema(description = "当前进度数值")
    private Integer currentProgress;
    @Schema(description = "目标数值")
    private Integer targetProgress;
    @Schema(description = "状态: EARNED/CLOSE/UNEARNED")
    private String status;
}
```

```java
@Tag(name = "成就徽章")
@RestController
@RequestMapping("/api/v1/content/user/growth/achievement")
public class AchievementController {
    @Resource
    private IAchievementService achievementService;

    @Operation(summary = "获取成员在圈子的徽章列表")
    @GetMapping("/list")
    public Result<List<AchievementVO>> getAchievements(
            @RequestParam String circleId,
            @RequestParam String userId) {
        return Result.OK(achievementService.getMemberAchievements(circleId, userId));
    }
}
```

- [ ] **Step 10: Commit**

```bash
git add jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/growth/ \
        jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/growth/service/AchievementServiceTest.java
git commit -m "feat(growth): implement achievement badge system with auto-award and revoke"
```

---

## Task 8: 排行榜系统

**Files:**
- Create: `src/main/java/org/jeecg/modules/content/user/growth/entity/CircleLeaderboardSnapshot.java`
- Create: `src/main/java/org/jeecg/modules/content/user/growth/mapper/CircleLeaderboardSnapshotMapper.java`
- Create: `src/main/java/org/jeecg/modules/content/user/growth/service/ILeaderboardService.java`
- Create: `src/main/java/org/jeecg/modules/content/user/growth/service/impl/LeaderboardServiceImpl.java`
- Create: `src/main/java/org/jeecg/modules/content/user/growth/vo/LeaderboardEntryVO.java`
- Create: `src/main/java/org/jeecg/modules/content/user/growth/controller/LeaderboardController.java`
- Create: `src/test/java/org/jeecg/modules/content/user/growth/service/LeaderboardServiceTest.java`

- [ ] **Step 1: Write the failing test for getLeaderboard**

```java
@ExtendWith(MockitoExtension.class)
class LeaderboardServiceTest {
    @Mock private CircleLeaderboardSnapshotMapper snapshotMapper;
    @InjectMocks private LeaderboardServiceImpl service;

    @Test
    @DisplayName("排行榜返回Top50成员并高亮当前用户")
    void getLeaderboard_top50_highlightsCurrentUser() {
        List<CircleLeaderboardSnapshot> snapshots = new ArrayList<>();
        for (int i = 1; i <= 50; i++) {
            CircleLeaderboardSnapshot s = new CircleLeaderboardSnapshot()
                    .setCircleId("c1").setUserId("user" + i)
                    .setDimension("EXP").setPeriod("WEEK")
                    .setScore(100 - i).setRankNum(i);
            snapshots.add(s);
        }
        when(snapshotMapper.selectList(any())).thenReturn(snapshots);

        List<LeaderboardEntryVO> result = service.getLeaderboard("c1", "EXP", "WEEK", "user5");

        assertThat(result).hasSize(50);
        assertThat(result.get(4).getHighlighted()).isTrue();
        assertThat(result.get(4).getUserId()).isEqualTo("user5");
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn test -pl jeecg-boot-module/jeecg-module-content -Dtest=LeaderboardServiceTest#getLeaderboard_top50_highlightsCurrentUser -q`
Expected: FAIL

- [ ] **Step 3: Create entity and mapper**

```java
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("circle_leaderboard_snapshot")
@Schema(description = "排行榜快照")
public class CircleLeaderboardSnapshot extends JeecgEntity {
    @Schema(description = "圈子ID")
    private String circleId;
    @Schema(description = "用户ID")
    private String userId;
    @Schema(description = "维度: EXP/CONTRIBUTION/POST")
    private String dimension;
    @Schema(description = "周期: WEEK/MONTH/ALL")
    private String period;
    @Schema(description = "得分")
    private Integer score;
    @Schema(description = "排名")
    private Integer rankNum;
    @Schema(description = "快照时间")
    private LocalDateTime snapshotTime;
}
```

```java
public interface CircleLeaderboardSnapshotMapper extends BaseMapper<CircleLeaderboardSnapshot> {
}
```

- [ ] **Step 4: Create ILeaderboardService and LeaderboardServiceImpl**

```java
public interface ILeaderboardService extends IService<CircleLeaderboardSnapshot> {
    List<LeaderboardEntryVO> getLeaderboard(String circleId, String dimension, String period, String currentUserId);
    void refreshSnapshot(String circleId);
}
```

```java
@Slf4j
@Service
public class LeaderboardServiceImpl extends ServiceImpl<CircleLeaderboardSnapshotMapper, CircleLeaderboardSnapshot>
        implements ILeaderaderboardService {

    @Resource
    private CircleGrowthLogMapper growthLogMapper;

    @Override
    public List<LeaderboardEntryVO> getLeaderboard(String circleId, String dimension, String period, String currentUserId) {
        QueryWrapper<CircleLeaderboardSnapshot> qw = new QueryWrapper<>();
        qw.eq("circle_id", circleId)
          .eq("dimension", dimension)
          .eq("period", period)
          .orderByAsc("rank_num")
          .last("LIMIT " + GrowthConstant.LEADERBOARD_TOP_N);
        List<CircleLeaderboardSnapshot> snapshots = this.list(qw);

        List<LeaderboardEntryVO> entries = new ArrayList<>();
        for (CircleLeaderboardSnapshot s : snapshots) {
            LeaderboardEntryVO vo = new LeaderboardEntryVO();
            vo.setUserId(s.getUserId());
            vo.setScore(s.getScore());
            vo.setRankNum(s.getRankNum());
            vo.setHighlighted(s.getUserId().equals(currentUserId));
            entries.add(vo);
        }
        return entries;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refreshSnapshot(String circleId) {
        // 删除旧快照
        QueryWrapper<CircleLeaderboardSnapshot> deleteQw = new QueryWrapper<>();
        deleteQw.eq("circle_id", circleId);
        this.remove(deleteQw);

        // 聚合新快照
        for (LeaderboardDimensionEnum dim : LeaderboardDimensionEnum.values()) {
            for (String period : new String[]{"WEEK", "MONTH", "ALL"}) {
                refreshDimension(circleId, dim.getCode(), period);
            }
        }
    }
}
```

- [ ] **Step 5: Run test to verify it passes**

Run: `mvn test -pl jeecg-boot-module/jeecg-module-content -Dtest=LeaderboardServiceTest#getLeaderboard_top50_highlightsCurrentUser -q`
Expected: PASS

- [ ] **Step 6: Write test for user not in top 50**

```java
@Test
@DisplayName("当前用户未进入Top50时返回空高亮")
void getLeaderboard_userNotInTop50_noHighlight() {
    List<CircleLeaderboardSnapshot> snapshots = new ArrayList<>();
    for (int i = 1; i <= 50; i++) {
        CircleLeaderboardSnapshot s = new CircleLeaderboardSnapshot()
                .setCircleId("c1").setUserId("user" + i)
                .setDimension("EXP").setPeriod("WEEK")
                .setScore(100 - i).setRankNum(i);
        snapshots.add(s);
    }
    when(snapshotMapper.selectList(any())).thenReturn(snapshots);

    List<LeaderboardEntryVO> result = service.getLeaderboard("c1", "EXP", "WEEK", "outsider");

    assertThat(result).allMatch(e -> !e.getHighlighted());
}
```

- [x] **Step 7: Create LeaderboardEntryVO and LeaderboardController**

```java
@Data
@Schema(description = "排行榜条目")
public class LeaderboardEntryVO {
    @Schema(description = "用户ID")
    private String userId;
    @Schema(description = "得分")
    private Integer score;
    @Schema(description = "排名")
    private Integer rankNum;
    @Schema(description = "是否高亮当前用户")
    private Boolean highlighted;
    @Schema(description = "与上一名得分差值")
    private Integer gap;
    @Schema(description = "用户名")
    private String username;
    @Schema(description = "用户头像URL")
    private String avatar;
}
```

```java
@Tag(name = "圈子排行榜")
@RestController
@RequestMapping("/api/v1/content/user/growth/leaderboard")
public class LeaderboardController {
    @Resource
    private ILeaderboardService leaderboardService;

    @Operation(summary = "获取圈子排行榜")
    @GetMapping
    public Result<List<LeaderboardEntryVO>> getLeaderboard(
            @RequestParam String circleId,
            @RequestParam String dimension,
            @RequestParam(defaultValue = "WEEK") String period,
            @RequestParam String currentUserId) {
        return Result.OK(leaderboardService.getLeaderboard(circleId, dimension, period, currentUserId));
    }
}
```

- [ ] **Step 8: Run all LeaderboardServiceTest**

Run: `mvn test -pl jeecg-boot-module/jeecg-module-content -Dtest=LeaderboardServiceTest -q`
Expected: ALL PASS

- [ ] **Step 9: Commit**

```bash
git add jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/growth/ \
        jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/growth/service/LeaderboardServiceTest.java
git commit -m "feat(growth): implement leaderboard with snapshot, multi-dimension and period support"
```

---

## Task 9: 定时任务

**Files:**
- Create: `src/main/java/org/jeecg/modules/content/user/growth/task/CircleGrowthScheduler.java`

- [ ] **Step 1: Create the scheduler**

```java
@Slf4j
@Component
public class CircleGrowthScheduler {
    @Resource
    private ICircleLevelService circleLevelService;
    @Resource
    private ILeaderboardService leaderboardService;
    @Resource
    private CircleLevelMapper levelMapper;

    @Scheduled(fixedDelayString = "${content.circle.growth.level-update.fixed-delay-ms:1800000}")
    public void updateCircleLevels() {
        log.info("开始更新圈子等级");
        List<CircleLevel> levels = levelMapper.selectList(null);
        for (CircleLevel level : levels) {
            try {
                circleLevelService.calculateGrowthScore(level.getCircleId());
                circleLevelService.updateLevel(level.getCircleId());
            } catch (Exception e) {
                log.error("更新圈子{}等级失败", level.getCircleId(), e);
            }
        }
        log.info("圈子等级更新完成，共{}个圈子", levels.size());
    }

    @Scheduled(fixedDelayString = "${content.circle.growth.leaderboard.fixed-delay-ms:3600000}")
    public void refreshLeaderboards() {
        log.info("开始刷新排行榜");
        List<CircleLevel> levels = levelMapper.selectList(null);
        for (CircleLevel level : levels) {
            try {
                leaderboardService.refreshSnapshot(level.getCircleId());
            } catch (Exception e) {
                log.error("刷新圈子{}排行榜失败", level.getCircleId(), e);
            }
        }
        log.info("排行榜刷新完成，共{}个圈子", levels.size());
    }
}
```

- [ ] **Step 2: Verify compilation**

Run: `mvn compile -pl jeecg-boot-module/jeecg-module-content -am -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/growth/task/
git commit -m "feat(growth): add scheduled tasks for level update and leaderboard refresh"
```

---

## Task 10: 全量验证

**Files:**
- Modify: 所有已创建的测试文件

- [ ] **Step 1: Run all growth module tests**

Run: `mvn test -pl jeecg-boot-module/jeecg-module-content -Dtest="org.jeecg.modules.content.user.growth.**" -q`
Expected: ALL PASS

- [ ] **Step 2: Run Flyway migration locally**

Run: `mvn spring-boot:run -pl jeecg-boot-module/jeecg-module-content -Dspring-boot.run.arguments="--spring.flyway.enabled=true"`
Expected: Migration V3.9.1_63 applied successfully

- [ ] **Step 3: Verify table structure**

```sql
DESC circle_level;
DESC circle_member_growth;
DESC circle_growth_log;
DESC circle_achievement;
DESC circle_member_achievement;
DESC circle_leaderboard_snapshot;
```

Expected: All 6 tables exist with correct columns

- [ ] **Step 4: Commit final state**

```bash
git add -A
git commit -m "feat(growth): complete circle growth incentive system"
```
