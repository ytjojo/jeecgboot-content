# Content User Growth Penalty Recovery Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 为 `content/user` 模块补齐成长处罚恢复编排，支持申诉通过和治理到期自动恢复两条链路在不改基础库的前提下联动恢复积分、成长值、等级和勋章状态。

**Architecture:** 采用“单表快照式强一致模型”。新增 `content_user_growth_penalty_record` 保存处罚影响快照与恢复幂等状态；新增独立恢复服务负责写反向积分/成长账本、恢复勋章状态、更新 `profile` 汇总并记录审计日志；`ContentUserSupportServiceImpl` 和 `ContentUserGovernanceServiceImpl` 仅作为触发入口，不在原有 service 内堆叠跨域细节。

**Tech Stack:** Spring Boot 3, JeecgBoot, MyBatis-Plus, Flyway SQL, JUnit 5, Mockito

---

## 文件边界

### 新增文件

- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/resources/flyway/sql/mysql/V3.9.1_51__content_user_growth_penalty_recovery.sql`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/entity/ContentUserGrowthPenaltyRecord.java`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/mapper/ContentUserGrowthPenaltyRecordMapper.java`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/IContentUserGrowthPenaltyRecoveryService.java`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/impl/ContentUserGrowthPenaltyRecoveryServiceImpl.java`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserGrowthPenaltyRecoveryServiceTest.java`

### 修改文件

- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/entity/ContentUserAuditLog.java`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/impl/ContentUserSupportServiceImpl.java`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/impl/ContentUserGovernanceServiceImpl.java`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserSupportServiceTest.java`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserGovernanceServiceTest.java`
- `docs/superpowers/specs/2026-05-02-content-user-prd-backend-coverage-report.md`
- `docs/superpowers/plans/2026-05-02-content-user-support-governance-gap-plan.md`

## Task 1: 锁定恢复服务失败测试

**Files:**
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserGrowthPenaltyRecoveryServiceTest.java`
- Read: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserGrowthServiceTest.java`

- [ ] **Step 1: 写恢复服务失败测试骨架**

```java
@ExtendWith(MockitoExtension.class)
class ContentUserGrowthPenaltyRecoveryServiceTest {

    @Mock
    private ContentUserGrowthPenaltyRecordMapper growthPenaltyRecordMapper;

    @Mock
    private ContentUserPointLedgerMapper pointLedgerMapper;

    @Mock
    private ContentUserGrowthLedgerMapper growthLedgerMapper;

    @Mock
    private ContentUserBadgeGrantMapper badgeGrantMapper;

    @Mock
    private ContentUserProfileMapper profileMapper;

    @Mock
    private ContentUserAuditLogMapper auditLogMapper;

    @InjectMocks
    private ContentUserGrowthPenaltyRecoveryServiceImpl recoveryService;

    @Test
    void shouldRecoverPenaltyEffectsByAppealApproval() {
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
        when(badgeGrantMapper.selectById("badge-grant-1")).thenReturn(new ContentUserBadgeGrant()
            .setId("badge-grant-1")
            .setUserId("u1")
            .setStatus("RECYCLED")
            .setDisplaying(Boolean.FALSE));

        int recoveredCount = recoveryService.recoverByAppeal(appeal, "admin-1", new Date(), "处罚撤销");

        assertThat(recoveredCount).isEqualTo(1);
        verify(pointLedgerMapper).insert(argThat(it ->
            "u1".equals(it.getUserId()) && it.getPointDelta() == 20 && "PENALTY_RECOVER".equals(it.getSourceType())));
        verify(growthLedgerMapper).insert(argThat(it ->
            "u1".equals(it.getUserId()) && it.getGrowthDelta() == 10 && "PENALTY_RECOVER".equals(it.getSourceType())));
        verify(profileMapper).updateById(argThat(it ->
            "u1".equals(it.getUserId()) && it.getPointBalance() == 100 && it.getGrowthValue() == 200 && it.getLevel() == 3));
        verify(badgeGrantMapper).updateById(argThat(it ->
            "badge-grant-1".equals(it.getId()) && "ACTIVE".equals(it.getStatus()) && Boolean.TRUE.equals(it.getDisplaying())));
        verify(growthPenaltyRecordMapper).updateById(argThat(it ->
            "RECOVERED".equals(it.getStatus()) && "APPEAL_APPROVED".equals(it.getRecoverTrigger())));
        verify(auditLogMapper).insert(argThat(it ->
            "USER_GROWTH_PENALTY_RECOVERED".equals(it.getEventType()) && "u1".equals(it.getUserId())));
    }
}
```

- [ ] **Step 2: 追加幂等与缺失勋章测试**

```java
@Test
void shouldSkipRecoveredPenaltyRecord() {
    when(growthPenaltyRecordMapper.selectList(any())).thenReturn(List.of(buildRecoveredPenaltyRecord()));

    int recoveredCount = recoveryService.recoverByAppeal(new ContentUserAppeal().setUserId("u1"), "admin-1", new Date(), "重复触发");

    assertThat(recoveredCount).isEqualTo(0);
    verifyNoInteractions(pointLedgerMapper, growthLedgerMapper, badgeGrantMapper, auditLogMapper);
}

@Test
void shouldContinueWhenBadgeGrantIsMissing() {
    when(growthPenaltyRecordMapper.selectList(any())).thenReturn(List.of(buildPenaltyRecord()));
    when(profileMapper.selectByUserId("u1")).thenReturn(new ContentUserProfile()
        .setUserId("u1")
        .setPointBalance(0)
        .setGrowthValue(0)
        .setLevel(1));
    when(badgeGrantMapper.selectById("badge-grant-1")).thenReturn(null);

    int recoveredCount = recoveryService.recoverByAppeal(new ContentUserAppeal().setUserId("u1"), "admin-1", new Date(), "处罚撤销");

    assertThat(recoveredCount).isEqualTo(1);
    verify(pointLedgerMapper).insert(any(ContentUserPointLedger.class));
    verify(growthLedgerMapper).insert(any(ContentUserGrowthLedger.class));
}
```

- [ ] **Step 3: 运行测试确认当前失败**

Run:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH="$JAVA_HOME/bin:$PATH"
mvn -pl jeecg-boot-module/jeecg-module-content -Dtest=ContentUserGrowthPenaltyRecoveryServiceTest test
```

Expected:

```text
COMPILATION ERROR
cannot find symbol: class ContentUserGrowthPenaltyRecoveryServiceImpl
cannot find symbol: class ContentUserGrowthPenaltyRecordMapper
```

- [ ] **Step 4: 提交测试脚手架**

```bash
git add jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserGrowthPenaltyRecoveryServiceTest.java
git commit -m "test: add growth penalty recovery service coverage"
```

## Task 2: 建表与持久化模型

**Files:**
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/resources/flyway/sql/mysql/V3.9.1_51__content_user_growth_penalty_recovery.sql`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/entity/ContentUserGrowthPenaltyRecord.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/mapper/ContentUserGrowthPenaltyRecordMapper.java`
- Test: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserGrowthPenaltyRecoveryServiceTest.java`

- [ ] **Step 1: 新增 Flyway SQL**

```sql
CREATE TABLE IF NOT EXISTS `content_user_growth_penalty_record` (
  `id` varchar(32) NOT NULL COMMENT '主键ID',
  `user_id` varchar(32) NOT NULL COMMENT '用户ID',
  `governance_record_id` varchar(32) DEFAULT NULL COMMENT '治理状态记录ID',
  `appeal_id` varchar(32) DEFAULT NULL COMMENT '关联申诉ID',
  `penalty_type` varchar(32) NOT NULL COMMENT '处罚类型',
  `effect_snapshot_json` text NOT NULL COMMENT '处罚影响快照JSON',
  `status` varchar(32) NOT NULL DEFAULT 'PENDING_RECOVER' COMMENT '恢复状态',
  `recover_trigger` varchar(64) DEFAULT NULL COMMENT '恢复触发来源',
  `recover_reason` varchar(255) DEFAULT NULL COMMENT '恢复原因',
  `recovered_by` varchar(32) DEFAULT NULL COMMENT '恢复操作人',
  `recovered_at` datetime DEFAULT NULL COMMENT '恢复时间',
  `create_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_content_user_growth_penalty_user` (`user_id`,`status`,`create_time`),
  KEY `idx_content_user_growth_penalty_governance` (`governance_record_id`,`status`),
  KEY `idx_content_user_growth_penalty_appeal` (`appeal_id`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='内容社区成长处罚恢复记录';
```

- [ ] **Step 2: 新增实体与 mapper**

```java
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("content_user_growth_penalty_record")
public class ContentUserGrowthPenaltyRecord extends JeecgEntity {

    private String userId;
    private String governanceRecordId;
    private String appealId;
    private String penaltyType;
    private String effectSnapshotJson;
    private String status;
    private String recoverTrigger;
    private String recoverReason;
    private String recoveredBy;
    private Date recoveredAt;
}
```

```java
public interface ContentUserGrowthPenaltyRecordMapper extends BaseMapper<ContentUserGrowthPenaltyRecord> {
}
```

- [ ] **Step 3: 运行失败测试，确认开始进入实现逻辑层错误而不是缺类**

Run:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH="$JAVA_HOME/bin:$PATH"
mvn -pl jeecg-boot-module/jeecg-module-content -Dtest=ContentUserGrowthPenaltyRecoveryServiceTest test
```

Expected:

```text
Tests run: 3
Failures: 3
Wanted but not invoked: pointLedgerMapper.insert(...)
```

- [ ] **Step 4: 提交持久化模型**

```bash
git add \
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/resources/flyway/sql/mysql/V3.9.1_51__content_user_growth_penalty_recovery.sql \
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/entity/ContentUserGrowthPenaltyRecord.java \
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/mapper/ContentUserGrowthPenaltyRecordMapper.java
git commit -m "feat: add growth penalty recovery persistence"
```

## Task 3: 实现成长处罚恢复服务

**Files:**
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/IContentUserGrowthPenaltyRecoveryService.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/impl/ContentUserGrowthPenaltyRecoveryServiceImpl.java`
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/entity/ContentUserAuditLog.java`
- Test: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserGrowthPenaltyRecoveryServiceTest.java`

- [ ] **Step 1: 新增服务契约**

```java
public interface IContentUserGrowthPenaltyRecoveryService {

    int recoverByAppeal(ContentUserAppeal appeal, String operatorUserId, Date executeTime, String reason);

    int recoverByGovernanceRecord(ContentUserStatusRecord record, String operatorUserId, Date executeTime, String reason);
}
```

- [ ] **Step 2: 写最小实现并解析快照**

```java
@Service
public class ContentUserGrowthPenaltyRecoveryServiceImpl implements IContentUserGrowthPenaltyRecoveryService {

    private static final String RECOVER_SOURCE_TYPE = "PENALTY_RECOVER";
    private static final String STATUS_PENDING_RECOVER = "PENDING_RECOVER";
    private static final String STATUS_RECOVERED = "RECOVERED";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int recoverByAppeal(ContentUserAppeal appeal, String operatorUserId, Date executeTime, String reason) {
        if (appeal == null || !"APPROVED".equalsIgnoreCase(appeal.getResultStatus())) {
            return 0;
        }
        return recoverPendingRecords(
            Wrappers.<ContentUserGrowthPenaltyRecord>lambdaQuery()
                .eq(ContentUserGrowthPenaltyRecord::getUserId, appeal.getUserId())
                .eq(ContentUserGrowthPenaltyRecord::getStatus, STATUS_PENDING_RECOVER),
            "APPEAL_APPROVED",
            operatorUserId,
            executeTime,
            reason,
            appeal.getId()
        );
    }
}
```

```java
private int recoverPendingRecords(LambdaQueryWrapper<ContentUserGrowthPenaltyRecord> queryWrapper,
                                  String trigger,
                                  String operatorUserId,
                                  Date executeTime,
                                  String reason,
                                  String appealId) {
    List<ContentUserGrowthPenaltyRecord> records = growthPenaltyRecordMapper.selectList(queryWrapper);
    if (records == null || records.isEmpty()) {
        return 0;
    }
    int recoveredCount = 0;
    for (ContentUserGrowthPenaltyRecord record : records) {
        if (STATUS_RECOVERED.equals(record.getStatus())) {
            continue;
        }
        recoveredCount += recoverSingleRecord(record, trigger, operatorUserId, executeTime, reason, appealId);
    }
    return recoveredCount;
}
```

- [ ] **Step 3: 实现积分、成长值、等级、勋章恢复**

```java
private int recoverSingleRecord(ContentUserGrowthPenaltyRecord record,
                                String trigger,
                                String operatorUserId,
                                Date executeTime,
                                String reason,
                                String appealId) {
    GrowthPenaltyEffectSnapshot snapshot = parseSnapshot(record.getEffectSnapshotJson());
    ContentUserProfile profile = profileMapper.selectByUserId(record.getUserId());
    if (profile == null) {
        return 0;
    }
    int recoveredPoint = restorePoint(profile, record.getId(), trigger, snapshot.getPointDelta());
    int recoveredGrowth = restoreGrowth(profile, record.getId(), trigger, snapshot.getGrowthDelta());
    restoreBadges(snapshot.getBadgeEffects());
    profile.setLevel(calculateLevel(profile.getGrowthValue()));
    profileMapper.updateById(profile);

    record.setAppealId(appealId);
    record.setStatus(STATUS_RECOVERED);
    record.setRecoverTrigger(trigger);
    record.setRecoverReason(reason);
    record.setRecoveredBy(operatorUserId);
    record.setRecoveredAt(executeTime);
    growthPenaltyRecordMapper.updateById(record);
    auditLogMapper.insert(ContentUserAuditLog.growthPenaltyRecovered(
        record.getUserId(), operatorUserId, trigger, record.getId(), recoveredPoint, recoveredGrowth, snapshot.getBadgeEffects().size()));
    return 1;
}
```

- [ ] **Step 4: 扩展审计日志工厂方法**

```java
public static ContentUserAuditLog growthPenaltyRecovered(String userId,
                                                         String operatorUserId,
                                                         String trigger,
                                                         String penaltyRecordId,
                                                         int pointDelta,
                                                         int growthDelta,
                                                         int badgeCount) {
    return new ContentUserAuditLog()
        .setUserId(userId)
        .setOperatorUserId(operatorUserId)
        .setEventType("USER_GROWTH_PENALTY_RECOVERED")
        .setEventContent(trigger)
        .setExtraDataJson("{\"penaltyRecordId\":\"" + penaltyRecordId
            + "\",\"pointDelta\":" + pointDelta
            + ",\"growthDelta\":" + growthDelta
            + ",\"badgeCount\":" + badgeCount + "}")
        .setEventTime(new Date());
}
```

- [ ] **Step 5: 运行恢复服务测试确认通过**

Run:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH="$JAVA_HOME/bin:$PATH"
mvn -pl jeecg-boot-module/jeecg-module-content -Dtest=ContentUserGrowthPenaltyRecoveryServiceTest test
```

Expected:

```text
Tests run: 3
Failures: 0
Errors: 0
BUILD SUCCESS
```

- [ ] **Step 6: 提交恢复服务**

```bash
git add \
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/IContentUserGrowthPenaltyRecoveryService.java \
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/impl/ContentUserGrowthPenaltyRecoveryServiceImpl.java \
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/entity/ContentUserAuditLog.java \
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserGrowthPenaltyRecoveryServiceTest.java
git commit -m "feat: add growth penalty recovery service"
```

## Task 4: 接入申诉通过链路

**Files:**
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/impl/ContentUserSupportServiceImpl.java`
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserSupportServiceTest.java`
- Test: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserGrowthPenaltyRecoveryServiceTest.java`

- [ ] **Step 1: 先补支持域失败测试**

```java
@Mock
private IContentUserGrowthPenaltyRecoveryService growthPenaltyRecoveryService;

@Test
void shouldRecoverGrowthPenaltyWhenAppealApproved() {
    ContentUserAppeal appeal = new ContentUserAppeal()
        .setUserId("u1")
        .setStatus("PENDING")
        .setAppealType("PENALTY");
    appeal.setId("appeal-1");
    when(appealMapper.selectById("appeal-1")).thenReturn(appeal);
    when(profileMapper.selectByUserId("u1")).thenReturn(new ContentUserProfile().setUserId("u1").setStatus("FROZEN"));
    when(statusRecordMapper.selectLatestByUserId("u1")).thenReturn(new ContentUserStatusRecord()
        .setUserId("u1")
        .setCurrentStatus("NORMAL")
        .setTargetStatus("FROZEN")
        .setRecoverable(Boolean.TRUE));

    supportService.handleAppeal(createHandleReq());

    verify(growthPenaltyRecoveryService).recoverByAppeal(
        argThat(it -> "appeal-1".equals(it.getId()) && "APPROVED".equals(it.getResultStatus())),
        eq("admin-1"),
        any(Date.class),
        eq("处罚撤销"));
}
```

- [ ] **Step 2: 运行测试确认当前失败**

Run:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH="$JAVA_HOME/bin:$PATH"
mvn -pl jeecg-boot-module/jeecg-module-content -Dtest=ContentUserSupportServiceTest#shouldRecoverGrowthPenaltyWhenAppealApproved test
```

Expected:

```text
Wanted but not invoked:
growthPenaltyRecoveryService.recoverByAppeal(...)
```

- [ ] **Step 3: 在 `handleAppeal` 中接入恢复服务**

```java
Date resolvedAt = new Date();
appeal.setStatus(req.getStatus());
appeal.setResultStatus(req.getResultStatus());
appeal.setResultNote(req.getResultNote());
appeal.setProgressNote(req.getProgressNote());
appeal.setResolvedBy(req.getOperatorUserId());
appeal.setResolvedAt(resolvedAt);
appealMapper.updateById(appeal);
restoreGovernanceStatusIfNecessary(appeal, req);
growthPenaltyRecoveryService.recoverByAppeal(appeal, req.getOperatorUserId(), resolvedAt, req.getResultNote());
auditLogMapper.insert(ContentUserAuditLog.appealHandled(appeal, req));
```

- [ ] **Step 4: 运行支持域测试**

Run:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH="$JAVA_HOME/bin:$PATH"
mvn -pl jeecg-boot-module/jeecg-module-content -Dtest=ContentUserSupportServiceTest test
```

Expected:

```text
Tests run:
Failures: 0
Errors: 0
BUILD SUCCESS
```

- [ ] **Step 5: 提交支持域接线**

```bash
git add \
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/impl/ContentUserSupportServiceImpl.java \
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserSupportServiceTest.java
git commit -m "feat: recover growth penalties after appeal approval"
```

## Task 5: 接入自动恢复链路

**Files:**
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/impl/ContentUserGovernanceServiceImpl.java`
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserGovernanceServiceTest.java`

- [ ] **Step 1: 先补治理域失败测试**

```java
@Mock
private IContentUserGrowthPenaltyRecoveryService growthPenaltyRecoveryService;

@Test
void shouldRecoverGrowthPenaltyAfterAutoRecoverGovernanceStatus() {
    Date currentTime = new Date(1735696800000L);
    ContentUserStatusRecord expiredRecord = new ContentUserStatusRecord()
        .setUserId("u1")
        .setCurrentStatus("NORMAL")
        .setTargetStatus("FROZEN")
        .setEffectiveEndTime(new Date(1735693200000L))
        .setRecoverable(Boolean.TRUE);
    expiredRecord.setId("record-1");
    when(statusRecordMapper.selectPage(any(), any())).thenAnswer(invocation -> {
        IPage<ContentUserStatusRecord> page = invocation.getArgument(0);
        page.setRecords(List.of(expiredRecord));
        page.setTotal(1L);
        return page;
    });
    when(profileMapper.selectList(any())).thenReturn(List.of(new ContentUserProfile().setUserId("u1").setStatus("FROZEN")));

    governanceService.autoRecoverExpiredStatuses(currentTime, 50L);

    verify(growthPenaltyRecoveryService).recoverByGovernanceRecord(
        argThat(it -> "record-1".equals(it.getId()) && "u1".equals(it.getUserId())),
        eq("system"),
        eq(currentTime),
        eq("处罚到期自动恢复"));
}
```

- [ ] **Step 2: 运行测试确认当前失败**

Run:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH="$JAVA_HOME/bin:$PATH"
mvn -pl jeecg-boot-module/jeecg-module-content -Dtest=ContentUserGovernanceServiceTest#shouldRecoverGrowthPenaltyAfterAutoRecoverGovernanceStatus test
```

Expected:

```text
Wanted but not invoked:
growthPenaltyRecoveryService.recoverByGovernanceRecord(...)
```

- [ ] **Step 3: 在自动恢复逻辑中接入成长恢复**

```java
String restoredStatus = restoreProfileStatus(profile, expiredRecord, executionTime);
if (restoredStatus == null) {
    continue;
}
auditLogMapper.insert(buildAutoRecoverAuditLog(expiredRecord, restoredStatus));
growthPenaltyRecoveryService.recoverByGovernanceRecord(
    expiredRecord,
    AUTO_RECOVER_OPERATOR,
    executionTime,
    "处罚到期自动恢复"
);
recoveredCount++;
```

- [ ] **Step 4: 运行治理域测试**

Run:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH="$JAVA_HOME/bin:$PATH"
mvn -pl jeecg-boot-module/jeecg-module-content -Dtest=ContentUserGovernanceServiceTest test
```

Expected:

```text
Tests run:
Failures: 0
Errors: 0
BUILD SUCCESS
```

- [ ] **Step 5: 提交治理域接线**

```bash
git add \
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/impl/ContentUserGovernanceServiceImpl.java \
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserGovernanceServiceTest.java
git commit -m "feat: recover growth penalties after governance expiry"
```

## Task 6: 跑全量回归并同步文档

**Files:**
- Modify: `docs/superpowers/specs/2026-05-02-content-user-prd-backend-coverage-report.md`
- Modify: `docs/superpowers/plans/2026-05-02-content-user-support-governance-gap-plan.md`

- [ ] **Step 1: 更新覆盖报告中的实现状态**

```md
| 96、106 | 申诉恢复后保留历史并驱动处罚恢复 | 已实现 | `ContentUserSupportServiceImpl.java`、`ContentUserGovernanceServiceImpl.java`、`ContentUserGrowthPenaltyRecoveryServiceImpl.java`、`ContentUserGrowthPenaltyRecord.java` | `ContentUserSupportServiceTest.java`、`ContentUserGovernanceServiceTest.java`、`ContentUserGrowthPenaltyRecoveryServiceTest.java` | 已补齐治理状态、积分、成长值、勋章恢复编排 | 后续继续扩等级特权细粒度恢复 | P0 |
```

```md
- `2026-05-02` 已新增成长处罚恢复编排：申诉通过和到期自动恢复会联动恢复积分、成长值、等级与勋章状态。
```

- [ ] **Step 2: 更新阶段计划中的剩余缺口**

```md
- 第二阶段已新增：成长处罚恢复编排，覆盖积分、成长值、等级和勋章状态恢复
- 第二阶段剩余缺口：等级权益细粒度恢复与更多成长处罚建模扩展
```

- [ ] **Step 3: 运行聚焦回归**

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

- [ ] **Step 4: 运行启动模块编译验证**

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

- [ ] **Step 5: 运行诊断与规范检查**

Run:

```bash
python3 scripts/check_java_style.py --paths \
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user \
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service
```

Expected:

```text
仅保留仓库级历史 warning；本轮新增文件不出现新的阻断问题
```

- [ ] **Step 6: 提交文档与验证结果**

```bash
git add \
  docs/superpowers/specs/2026-05-02-content-user-prd-backend-coverage-report.md \
  docs/superpowers/plans/2026-05-02-content-user-support-governance-gap-plan.md \
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/resources/flyway/sql/mysql/V3.9.1_51__content_user_growth_penalty_recovery.sql
git commit -m "docs: sync growth penalty recovery coverage"
```

## 自检结论

- 设计要求“强一致建模”已映射到 Task 2 的新表和 Task 3 的统一恢复服务
- 设计要求“申诉恢复 + 自动恢复双入口”已分别映射到 Task 4 和 Task 5
- 设计要求“积分、成长值、等级、勋章恢复”和“幂等”已映射到 Task 1 与 Task 3 的测试及实现步骤
- 文档同步、回归验证、规范检查已映射到 Task 6
