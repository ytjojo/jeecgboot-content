# Content User Level Benefit Recovery Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 为 `content/user` 模块补齐等级权益细粒度恢复能力，在不改基础库的前提下让申诉通过与治理到期自动恢复链路可以显式恢复 `PRIORITY_CUSTOMER_SERVICE` 权益，并让 `customer-service` 路由感知恢复结果。

**Architecture:** 保留现有 `ContentUserGrowthPenaltyRecord` 作为成长处罚主表，新增 `content_user_level_benefit_penalty_record` 作为等级权益子表。新增独立的 `IContentUserLevelBenefitRecoveryService` 负责按处罚记录恢复权益并维护子表幂等状态；`ContentUserGrowthPenaltyRecoveryServiceImpl` 继续作为统一恢复入口；`ContentUserSupportServiceImpl` 在现有等级阈值规则之前增加“显式权益优先”判定，兼容旧数据和新恢复数据。

**Tech Stack:** Spring Boot 3, JeecgBoot, MyBatis-Plus, Flyway SQL, JUnit 5, Mockito

---

## 文件边界

### 新增文件

- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/resources/flyway/sql/mysql/V3.9.1_52__content_user_level_benefit_recovery.sql`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/entity/ContentUserLevelBenefitPenaltyRecord.java`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/mapper/ContentUserLevelBenefitPenaltyRecordMapper.java`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/IContentUserLevelBenefitRecoveryService.java`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/impl/ContentUserLevelBenefitRecoveryServiceImpl.java`
- `docs/superpowers/plans/2026-05-02-content-user-level-benefit-recovery-plan.md`

### 修改文件

- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/entity/ContentUserAuditLog.java`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/impl/ContentUserGrowthPenaltyRecoveryServiceImpl.java`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/impl/ContentUserSupportServiceImpl.java`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserGrowthPenaltyRecoveryServiceTest.java`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserSupportServiceTest.java`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserGovernanceServiceTest.java`
- `docs/superpowers/specs/2026-05-02-content-user-prd-backend-coverage-report.md`
- `docs/superpowers/plans/2026-05-02-content-user-support-governance-gap-plan.md`

## Task 1: 锁定失败测试

**Files:**
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserGrowthPenaltyRecoveryServiceTest.java`
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserSupportServiceTest.java`
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserGovernanceServiceTest.java`

- [ ] **Step 1: 在恢复服务测试中增加等级权益恢复红灯**

```java
@Mock
private ContentUserLevelBenefitPenaltyRecordMapper levelBenefitPenaltyRecordMapper;

@InjectMocks
private ContentUserLevelBenefitRecoveryServiceImpl levelBenefitRecoveryService;

@Test
void shouldRecoverLevelBenefitWhenAppealApproved() {
    ContentUserAppeal appeal = new ContentUserAppeal()
        .setUserId("u1")
        .setResultStatus("APPROVED");
    appeal.setId("appeal-1");
    when(growthPenaltyRecordMapper.selectList(any())).thenReturn(List.of(buildPenaltyRecord()));
    when(profileMapper.selectByUserId("u1")).thenReturn(new ContentUserProfile()
        .setUserId("u1")
        .setPointBalance(80)
        .setGrowthValue(190)
        .setLevel(2));
    when(levelBenefitPenaltyRecordMapper.selectList(any())).thenReturn(List.of(
        buildLevelBenefitRecord("penalty-1", "u1", "PRIORITY_CUSTOMER_SERVICE")
    ));

    int recoveredCount = recoveryService.recoverByAppeal(appeal, "admin-1", new Date(), "处罚撤销");

    assertThat(recoveredCount).isEqualTo(1);
    verify(levelBenefitPenaltyRecordMapper).updateById(argThat(it ->
        "RECOVERED".equals(it.getRecoverStatus())
            && "PRIORITY_CUSTOMER_SERVICE".equals(it.getBenefitCode())
            && "admin-1".equals(it.getRecoveredBy())));
    verify(auditLogMapper).insert(argThat(it ->
        "USER_GROWTH_PENALTY_RECOVERED".equals(it.getEventType())
            && it.getExtraDataJson().contains("\"recoveredBenefitCount\":1")));
}
```

- [ ] **Step 2: 在支持域测试中增加显式权益命中人工客服红灯**

```java
@Mock
private IContentUserLevelBenefitRecoveryService levelBenefitRecoveryService;

@Test
void shouldRouteToManualPriorityWhenExplicitBenefitEnabled() {
    when(profileMapper.selectByUserId("u1")).thenReturn(new ContentUserProfile()
        .setUserId("u1")
        .setStatus("NORMAL")
        .setLevel(1)
        .setGrowthValue(0));
    when(levelBenefitRecoveryService.hasEnabledBenefit("u1", "PRIORITY_CUSTOMER_SERVICE")).thenReturn(true);

    ContentCustomerServiceVO result = supportService.getCustomerServiceEntry("u1");

    assertThat(result.getRouteType()).isEqualTo("MANUAL_PRIORITY");
    assertThat(result.getTitle()).isEqualTo("专属客服");
}
```

- [ ] **Step 3: 在治理域测试中增加自动恢复后权益恢复红灯**

```java
@Mock
private IContentUserLevelBenefitRecoveryService levelBenefitRecoveryService;

@Test
void shouldRecoverLevelBenefitAfterAutoRecoverGovernanceStatus() {
    Date currentTime = new Date(1735696800000L);
    ContentUserStatusRecord record = buildRecoverableRecord();
    when(statusRecordMapper.selectPage(any(), any())).thenReturn(new Page<ContentUserStatusRecord>(1L, 50L)
        .setRecords(List.of(record)));
    when(profileMapper.selectByUserId("u1")).thenReturn(new ContentUserProfile()
        .setUserId("u1")
        .setStatus("MUTED")
        .setLevel(3)
        .setGrowthValue(250));

    governanceService.autoRecoverExpiredStatuses(currentTime, 50L);

    verify(growthPenaltyRecoveryService).recoverByGovernanceRecord(
        argThat(it -> "record-1".equals(it.getId())),
        argThat(it -> "system".equals(it)),
        argThat(it -> currentTime.equals(it)),
        argThat(it -> "处罚到期自动恢复".equals(it))
    );
}
```

- [ ] **Step 4: 运行聚焦测试，确认当前失败**

Run:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH="$JAVA_HOME/bin:$PATH"
mvn -pl jeecg-boot-module/jeecg-module-content \
  -Dtest=ContentUserGrowthPenaltyRecoveryServiceTest,ContentUserSupportServiceTest,ContentUserGovernanceServiceTest test
```

Expected:

```text
COMPILATION ERROR
cannot find symbol: class ContentUserLevelBenefitPenaltyRecordMapper
cannot find symbol: class IContentUserLevelBenefitRecoveryService
```

- [ ] **Step 5: 提交失败测试基线**

```bash
git add \
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserGrowthPenaltyRecoveryServiceTest.java \
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserSupportServiceTest.java \
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserGovernanceServiceTest.java
git commit -m "test: cover level benefit recovery flow"
```

## Task 2: 新增等级权益子表与持久化模型

**Files:**
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/resources/flyway/sql/mysql/V3.9.1_52__content_user_level_benefit_recovery.sql`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/entity/ContentUserLevelBenefitPenaltyRecord.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/mapper/ContentUserLevelBenefitPenaltyRecordMapper.java`
- Test: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserGrowthPenaltyRecoveryServiceTest.java`

- [ ] **Step 1: 新增 Flyway SQL**

```sql
CREATE TABLE IF NOT EXISTS `content_user_level_benefit_penalty_record` (
  `id` varchar(32) NOT NULL COMMENT '主键ID',
  `penalty_record_id` varchar(32) NOT NULL COMMENT '成长处罚记录ID',
  `user_id` varchar(32) NOT NULL COMMENT '用户ID',
  `benefit_code` varchar(64) NOT NULL COMMENT '等级权益编码',
  `previous_enabled` tinyint(1) NOT NULL DEFAULT 0 COMMENT '处罚前是否启用',
  `current_enabled` tinyint(1) NOT NULL DEFAULT 0 COMMENT '处罚后是否启用',
  `recover_status` varchar(32) NOT NULL DEFAULT 'PENDING_RECOVER' COMMENT '恢复状态',
  `recover_reason` varchar(255) DEFAULT NULL COMMENT '恢复原因',
  `recovered_by` varchar(32) DEFAULT NULL COMMENT '恢复操作人',
  `recovered_at` datetime DEFAULT NULL COMMENT '恢复时间',
  `create_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_content_user_level_benefit_penalty_record_penalty` (`penalty_record_id`, `recover_status`),
  KEY `idx_content_user_level_benefit_penalty_record_user` (`user_id`, `benefit_code`, `recover_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='内容社区等级权益处罚恢复记录';
```

- [ ] **Step 2: 新增实体**

```java
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("content_user_level_benefit_penalty_record")
public class ContentUserLevelBenefitPenaltyRecord extends JeecgEntity {

    private String penaltyRecordId;
    private String userId;
    private String benefitCode;
    private Boolean previousEnabled;
    private Boolean currentEnabled;
    private String recoverStatus;
    private String recoverReason;
    private String recoveredBy;
    private Date recoveredAt;
}
```

- [ ] **Step 3: 新增 Mapper**

```java
public interface ContentUserLevelBenefitPenaltyRecordMapper extends BaseMapper<ContentUserLevelBenefitPenaltyRecord> {
}
```

- [ ] **Step 4: 运行聚焦测试，确认失败从“缺类”进入“行为未实现”**

Run:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH="$JAVA_HOME/bin:$PATH"
mvn -pl jeecg-boot-module/jeecg-module-content \
  -Dtest=ContentUserGrowthPenaltyRecoveryServiceTest,ContentUserSupportServiceTest,ContentUserGovernanceServiceTest test
```

Expected:

```text
Failures: 2 or more
Wanted but not invoked: levelBenefitPenaltyRecordMapper.updateById(...)
Wanted but not invoked: levelBenefitRecoveryService.hasEnabledBenefit(...)
```

- [ ] **Step 5: 提交持久化模型**

```bash
git add \
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/resources/flyway/sql/mysql/V3.9.1_52__content_user_level_benefit_recovery.sql \
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/entity/ContentUserLevelBenefitPenaltyRecord.java \
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/mapper/ContentUserLevelBenefitPenaltyRecordMapper.java
git commit -m "feat: add level benefit penalty persistence"
```

## Task 3: 实现等级权益恢复服务并接入统一恢复编排

**Files:**
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/IContentUserLevelBenefitRecoveryService.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/impl/ContentUserLevelBenefitRecoveryServiceImpl.java`
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/impl/ContentUserGrowthPenaltyRecoveryServiceImpl.java`
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/entity/ContentUserAuditLog.java`
- Test: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserGrowthPenaltyRecoveryServiceTest.java`

- [ ] **Step 1: 新增服务契约**

```java
public interface IContentUserLevelBenefitRecoveryService {

    int recoverByPenaltyRecord(ContentUserGrowthPenaltyRecord record,
                               String operatorUserId,
                               Date executeTime,
                               String reason);

    boolean hasEnabledBenefit(String userId, String benefitCode);
}
```

- [ ] **Step 2: 实现最小恢复服务**

```java
@Service
public class ContentUserLevelBenefitRecoveryServiceImpl
    extends ServiceImpl<ContentUserLevelBenefitPenaltyRecordMapper, ContentUserLevelBenefitPenaltyRecord>
    implements IContentUserLevelBenefitRecoveryService {

    private static final String STATUS_PENDING_RECOVER = "PENDING_RECOVER";
    private static final String STATUS_RECOVERED = "RECOVERED";

    @Resource
    private ContentUserLevelBenefitPenaltyRecordMapper levelBenefitPenaltyRecordMapper;

    @Override
    public int recoverByPenaltyRecord(ContentUserGrowthPenaltyRecord record,
                                      String operatorUserId,
                                      Date executeTime,
                                      String reason) {
        if (record == null || record.getId() == null) {
            return 0;
        }
        List<ContentUserLevelBenefitPenaltyRecord> records = levelBenefitPenaltyRecordMapper.selectList(
            Wrappers.<ContentUserLevelBenefitPenaltyRecord>lambdaQuery()
                .eq(ContentUserLevelBenefitPenaltyRecord::getPenaltyRecordId, record.getId())
                .eq(ContentUserLevelBenefitPenaltyRecord::getRecoverStatus, STATUS_PENDING_RECOVER)
        );
        int recoveredCount = 0;
        for (ContentUserLevelBenefitPenaltyRecord item : records) {
            item.setCurrentEnabled(Boolean.TRUE.equals(item.getPreviousEnabled()));
            item.setRecoverStatus(STATUS_RECOVERED);
            item.setRecoverReason(reason);
            item.setRecoveredBy(operatorUserId);
            item.setRecoveredAt(executeTime);
            levelBenefitPenaltyRecordMapper.updateById(item);
            recoveredCount++;
        }
        return recoveredCount;
    }

    @Override
    public boolean hasEnabledBenefit(String userId, String benefitCode) {
        return levelBenefitPenaltyRecordMapper.selectCount(
            Wrappers.<ContentUserLevelBenefitPenaltyRecord>lambdaQuery()
                .eq(ContentUserLevelBenefitPenaltyRecord::getUserId, userId)
                .eq(ContentUserLevelBenefitPenaltyRecord::getBenefitCode, benefitCode)
                .eq(ContentUserLevelBenefitPenaltyRecord::getCurrentEnabled, Boolean.TRUE)
                .eq(ContentUserLevelBenefitPenaltyRecord::getRecoverStatus, STATUS_RECOVERED)
                .last("limit 1")
        ) > 0;
    }
}
```

- [ ] **Step 3: 接入统一恢复服务**

```java
@Resource
private IContentUserLevelBenefitRecoveryService levelBenefitRecoveryService;

private int recoverSingleRecord(ContentUserGrowthPenaltyRecord record,
                                String trigger,
                                String operatorUserId,
                                Date executeTime,
                                String reason,
                                String appealId) {
    ContentUserProfile profile = profileMapper.selectByUserId(record.getUserId());
    if (profile == null) {
        return 0;
    }
    GrowthPenaltySnapshot snapshot = parseSnapshot(record.getEffectSnapshotJson());
    int recoveredPoint = restorePoint(profile, record.getId(), trigger, snapshot.getPointDelta());
    int recoveredGrowth = restoreGrowth(profile, record.getId(), trigger, snapshot.getGrowthDelta());
    int recoveredBadgeCount = restoreBadges(snapshot.getBadgeEffects());
    int recoveredBenefitCount = levelBenefitRecoveryService.recoverByPenaltyRecord(record, operatorUserId, executeTime, reason);

    profile.setPointBalance(Math.max(defaultZero(profile.getPointBalance()), 0));
    profile.setGrowthValue(Math.max(defaultZero(profile.getGrowthValue()), 0));
    profile.setLevel(calculateLevel(profile.getGrowthValue()));
    profileMapper.updateById(profile);

    record.setAppealId(appealId == null ? record.getAppealId() : appealId);
    record.setStatus(STATUS_RECOVERED);
    record.setRecoverTrigger(trigger);
    record.setRecoverReason(reason);
    record.setRecoveredBy(operatorUserId);
    record.setRecoveredAt(executeTime);
    growthPenaltyRecordMapper.updateById(record);

    auditLogMapper.insert(ContentUserAuditLog.growthPenaltyRecovered(
        record.getUserId(),
        operatorUserId,
        trigger,
        record.getId(),
        recoveredPoint,
        recoveredGrowth,
        recoveredBadgeCount,
        recoveredBenefitCount
    ));
    return 1;
}
```

- [ ] **Step 4: 扩展审计日志工厂**

```java
public static ContentUserAuditLog growthPenaltyRecovered(String userId,
                                                         String operatorUserId,
                                                         String trigger,
                                                         String penaltyRecordId,
                                                         int pointDelta,
                                                         int growthDelta,
                                                         int badgeCount,
                                                         int recoveredBenefitCount) {
    return new ContentUserAuditLog()
        .setUserId(userId)
        .setOperatorUserId(operatorUserId)
        .setEventType("USER_GROWTH_PENALTY_RECOVERED")
        .setEventContent(trigger)
        .setExtraDataJson("{\"penaltyRecordId\":\"" + penaltyRecordId
            + "\",\"pointDelta\":" + pointDelta
            + ",\"growthDelta\":" + growthDelta
            + ",\"badgeCount\":" + badgeCount
            + ",\"recoveredBenefitCount\":" + recoveredBenefitCount + "}")
        .setEventTime(new Date());
}
```

- [ ] **Step 5: 运行恢复服务测试并转绿**

Run:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH="$JAVA_HOME/bin:$PATH"
mvn -pl jeecg-boot-module/jeecg-module-content -Dtest=ContentUserGrowthPenaltyRecoveryServiceTest test
```

Expected:

```text
Tests run: 4
Failures: 0
Errors: 0
BUILD SUCCESS
```

- [ ] **Step 6: 提交恢复服务实现**

```bash
git add \
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/IContentUserLevelBenefitRecoveryService.java \
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/impl/ContentUserLevelBenefitRecoveryServiceImpl.java \
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/impl/ContentUserGrowthPenaltyRecoveryServiceImpl.java \
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/entity/ContentUserAuditLog.java \
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserGrowthPenaltyRecoveryServiceTest.java
git commit -m "feat: recover level benefits from growth penalties"
```

## Task 4: 接入客服显式权益判定并补联动回归

**Files:**
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/impl/ContentUserSupportServiceImpl.java`
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserSupportServiceTest.java`
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserGovernanceServiceTest.java`

- [ ] **Step 1: 在支持域接入显式权益判定**

```java
private static final String BENEFIT_PRIORITY_CUSTOMER_SERVICE = "PRIORITY_CUSTOMER_SERVICE";

@Resource
private IContentUserLevelBenefitRecoveryService levelBenefitRecoveryService;

private boolean shouldRouteToManualPriority(ContentUserProfile profile) {
    if (profile == null) {
        return false;
    }
    if (StringUtils.hasText(profile.getUserId())
        && levelBenefitRecoveryService.hasEnabledBenefit(profile.getUserId(), BENEFIT_PRIORITY_CUSTOMER_SERVICE)) {
        return true;
    }
    int level = profile.getLevel() == null ? 1 : profile.getLevel();
    int growthValue = profile.getGrowthValue() == null ? 0 : profile.getGrowthValue();
    return level >= 5 || growthValue >= 400;
}
```

- [ ] **Step 2: 补支持域回归测试**

```java
@Test
void shouldStillFallbackToLevelRuleWhenBenefitRecordMissing() {
    when(profileMapper.selectByUserId("u1")).thenReturn(new ContentUserProfile()
        .setUserId("u1")
        .setStatus("NORMAL")
        .setLevel(5)
        .setGrowthValue(300));
    when(levelBenefitRecoveryService.hasEnabledBenefit("u1", "PRIORITY_CUSTOMER_SERVICE")).thenReturn(false);

    ContentCustomerServiceVO result = supportService.getCustomerServiceEntry("u1");

    assertThat(result.getRouteType()).isEqualTo("MANUAL_PRIORITY");
}
```

- [ ] **Step 3: 保持治理域现有联动测试通过**

```java
verify(growthPenaltyRecoveryService).recoverByGovernanceRecord(
    argThat(it -> "record-1".equals(it.getId()) && "u1".equals(it.getUserId())),
    argThat(it -> "system".equals(it)),
    argThat(it -> currentTime.equals(it)),
    argThat(it -> "处罚到期自动恢复".equals(it))
);
```

- [ ] **Step 4: 运行支持/治理聚焦回归**

Run:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH="$JAVA_HOME/bin:$PATH"
mvn -pl jeecg-boot-module/jeecg-module-content \
  -Dtest=ContentUserSupportServiceTest,ContentUserGovernanceServiceTest test
```

Expected:

```text
Tests run: [non-zero]
Failures: 0
Errors: 0
BUILD SUCCESS
```

- [ ] **Step 5: 提交支持域接线**

```bash
git add \
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/impl/ContentUserSupportServiceImpl.java \
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserSupportServiceTest.java \
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserGovernanceServiceTest.java
git commit -m "feat: honor explicit customer service benefit"
```

## Task 5: 文档同步与整体验证

**Files:**
- Modify: `docs/superpowers/specs/2026-05-02-content-user-prd-backend-coverage-report.md`
- Modify: `docs/superpowers/plans/2026-05-02-content-user-support-governance-gap-plan.md`

- [ ] **Step 1: 更新覆盖报告**

```md
- `2026-05-02` 已新增等级权益细粒度恢复：成长处罚恢复链路增加等级权益子表，首批支持 `PRIORITY_CUSTOMER_SERVICE` 显式恢复，并在客服路由中优先判定。
```

- [ ] **Step 2: 更新阶段计划文档**

```md
- 第二阶段已新增：等级权益细粒度恢复，首批闭环 `PRIORITY_CUSTOMER_SERVICE`
- 第二阶段剩余缺口：更多成长处罚来源建模扩展
```

- [ ] **Step 3: 运行模块聚焦回归**

Run:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH="$JAVA_HOME/bin:$PATH"
mvn -pl jeecg-boot-module/jeecg-module-content \
  -Dtest=ContentUserGrowthPenaltyRecoveryServiceTest,ContentUserSupportServiceTest,ContentUserGovernanceServiceTest test
```

Expected:

```text
BUILD SUCCESS
```

- [ ] **Step 4: 运行启动模块编译验证集成未破坏**

Run:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH="$JAVA_HOME/bin:$PATH"
mvn -pl jeecg-module-system/jeecg-system-start -am -DskipTests compile
```

Expected:

```text
BUILD SUCCESS
```

- [ ] **Step 5: 运行规范检查**

Run:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH="$JAVA_HOME/bin:$PATH"
python3 .trae/skills/ai-coding-java-springboot/scripts/standards-check.py \
  --target jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/entity/ContentUserLevelBenefitPenaltyRecord.java \
  --target jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/mapper/ContentUserLevelBenefitPenaltyRecordMapper.java \
  --target jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/IContentUserLevelBenefitRecoveryService.java \
  --target jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/impl/ContentUserLevelBenefitRecoveryServiceImpl.java \
  --target jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/impl/ContentUserGrowthPenaltyRecoveryServiceImpl.java \
  --target jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/impl/ContentUserSupportServiceImpl.java \
  --target jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserGrowthPenaltyRecoveryServiceTest.java \
  --target jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserSupportServiceTest.java \
  --warn-only
```

Expected:

```text
warning count is acceptable for existing historical issues
no new blocking issue for touched files
```

- [ ] **Step 6: 提交文档与验证收口**

```bash
git add \
  docs/superpowers/specs/2026-05-02-content-user-prd-backend-coverage-report.md \
  docs/superpowers/plans/2026-05-02-content-user-support-governance-gap-plan.md \
  docs/superpowers/plans/2026-05-02-content-user-level-benefit-recovery-plan.md
git commit -m "docs: sync level benefit recovery status"
```

## 自检结论

- Spec 覆盖：已覆盖子表建模、恢复服务、统一恢复编排、客服显式权益消费、测试与文档同步。
- 占位词检查：计划中无 `TODO/TBD/implement later`。
- 类型一致性：统一使用 `ContentUserLevelBenefitPenaltyRecord`、`IContentUserLevelBenefitRecoveryService`、`PRIORITY_CUSTOMER_SERVICE`、`recoverByPenaltyRecord(...)`。
