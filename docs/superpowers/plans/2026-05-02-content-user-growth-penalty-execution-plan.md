# Content User Growth Penalty Execution Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在 `content/user` 模块内把成长处罚链路从“来源建档 + 恢复回放”升级为“真实处罚执行 + 可恢复回放”的完整闭环，覆盖积分、成长值、等级、勋章和首批等级权益。

**Architecture:** 保留现有 `ContentUserGrowthPenaltyRecord` 作为唯一处罚主记录，升级 `IContentUserGrowthPenaltyRecordService` 为“建档 + 真实处罚执行编排服务”，由治理入口与举报入口统一委托。恢复侧继续沿用 `IContentUserGrowthPenaltyRecoveryService`，但补兼容新的真实执行快照结构，确保处罚与恢复前后对称、事务一致、来源幂等。

**Tech Stack:** Spring Boot 3, JeecgBoot, MyBatis-Plus, Flyway SQL, Jackson, JUnit 5, Mockito

---

## 文件边界

### 新增文件

- `docs/superpowers/plans/2026-05-02-content-user-growth-penalty-execution-plan.md`

### 修改文件

- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/impl/ContentUserGrowthPenaltyRecordServiceImpl.java`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/impl/ContentUserGrowthPenaltyRecoveryServiceImpl.java`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/entity/ContentUserAuditLog.java`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/impl/ContentUserGovernanceServiceImpl.java`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/impl/ContentUserSupportServiceImpl.java`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserGrowthPenaltyRecordServiceTest.java`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserGrowthPenaltyRecoveryServiceTest.java`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserGovernanceServiceTest.java`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserSupportServiceTest.java`
- `docs/superpowers/specs/2026-05-02-content-user-prd-backend-coverage-report.md`
- `docs/superpowers/plans/2026-05-02-content-user-support-governance-gap-plan.md`

## Task 1: 锁定真实处罚执行红灯

**Files:**
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserGrowthPenaltyRecordServiceTest.java`

- [ ] **Step 1: 增加治理来源首次执行会真实扣积分与成长值的失败测试**

```java
@Test
void shouldExecutePenaltyFromGovernanceRecord() {
    ContentUserStatusRecord record = new ContentUserStatusRecord()
        .setUserId("u1")
        .setCurrentStatus("NORMAL")
        .setTargetStatus("MUTED")
        .setReason("违规处理")
        .setRuleCode("RULE-1");
    record.setId("status-1");
    ContentUserStatusChangeReq req = new ContentUserStatusChangeReq()
        .setUserId("u1")
        .setCurrentStatus("NORMAL")
        .setTargetStatus("MUTED")
        .setOperatorUserId("admin-1")
        .setReason("违规处理")
        .setRuleCode("RULE-1");
    when(growthPenaltyRecordMapper.selectOne(any())).thenReturn(null);
    when(profileMapper.selectByUserId("u1")).thenReturn(new ContentUserProfile()
        .setUserId("u1")
        .setPointBalance(100)
        .setGrowthValue(260)
        .setLevel(3));

    growthPenaltyRecordService.createFromGovernanceRecord(record, req, new Date(1735696800000L));

    verify(pointLedgerMapper).insert(argThat((ContentUserPointLedger it) ->
        "u1".equals(it.getUserId())
            && it.getPointDelta() == -20
            && "GROWTH_PENALTY".equals(it.getSourceType())));
    verify(growthLedgerMapper).insert(argThat((ContentUserGrowthLedger it) ->
        "u1".equals(it.getUserId())
            && it.getGrowthDelta() == -30
            && "GROWTH_PENALTY".equals(it.getSourceType())));
    verify(profileMapper).updateById(argThat((ContentUserProfile it) ->
        "u1".equals(it.getUserId())
            && it.getPointBalance() == 80
            && it.getGrowthValue() == 230
            && it.getLevel() == 3));
    verify(growthPenaltyRecordMapper).insert(argThat((ContentUserGrowthPenaltyRecord it) ->
        "COMPOSITE".equals(it.getPenaltyType())
            && it.getEffectSnapshotJson().contains("\"pointEffect\"")
            && it.getEffectSnapshotJson().contains("\"growthEffect\"")));
}
```

- [ ] **Step 2: 增加勋章和等级权益真实处罚失败测试**

```java
@Test
void shouldDisableBadgeAndLevelBenefitWhenPenaltyExecuted() {
    ContentUserStatusRecord record = new ContentUserStatusRecord()
        .setUserId("u1")
        .setCurrentStatus("NORMAL")
        .setTargetStatus("FROZEN");
    record.setId("status-2");
    when(growthPenaltyRecordMapper.selectOne(any())).thenReturn(null);
    when(profileMapper.selectByUserId("u1")).thenReturn(new ContentUserProfile()
        .setUserId("u1")
        .setPointBalance(50)
        .setGrowthValue(120)
        .setLevel(2));
    when(badgeGrantMapper.selectList(any())).thenReturn(List.of(new ContentUserBadgeGrant()
        .setUserId("u1")
        .setBadgeCode("CREATOR_STAR")
        .setStatus("ACTIVE")
        .setDisplaying(Boolean.TRUE)));
    when(levelBenefitPenaltyRecordMapper.selectList(any())).thenReturn(List.of());

    growthPenaltyRecordService.createFromGovernanceRecord(
        record,
        new ContentUserStatusChangeReq().setUserId("u1").setOperatorUserId("admin-1").setReason("违规处理"),
        new Date()
    );

    verify(badgeGrantMapper).updateById(argThat((ContentUserBadgeGrant it) ->
        "RECYCLED".equals(it.getStatus()) && Boolean.FALSE.equals(it.getDisplaying())));
    verify(levelBenefitPenaltyRecordMapper).insert(argThat((ContentUserLevelBenefitPenaltyRecord it) ->
        "u1".equals(it.getUserId())
            && "PRIORITY_CUSTOMER_SERVICE".equals(it.getBenefitCode())
            && Boolean.TRUE.equals(it.getPreviousEnabled())
            && Boolean.FALSE.equals(it.getCurrentEnabled())));
}
```

- [ ] **Step 3: 增加重复来源不重复执行处罚失败测试**

```java
@Test
void shouldSkipPenaltyExecutionWhenGovernanceSourceAlreadyExists() {
    ContentUserStatusRecord record = new ContentUserStatusRecord()
        .setUserId("u1")
        .setTargetStatus("MUTED");
    record.setId("status-1");
    when(growthPenaltyRecordMapper.selectOne(any())).thenReturn(new ContentUserGrowthPenaltyRecord()
        .setGovernanceRecordId("status-1")
        .setStatus("PENDING_RECOVER"));

    growthPenaltyRecordService.createFromGovernanceRecord(
        record,
        new ContentUserStatusChangeReq().setUserId("u1").setOperatorUserId("admin-1"),
        new Date()
    );

    verifyNoInteractions(pointLedgerMapper, growthLedgerMapper, badgeGrantMapper, levelBenefitPenaltyRecordMapper);
}
```

- [ ] **Step 4: 运行建档服务测试，确认红灯失败在真实执行行为**

Run:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH="$JAVA_HOME/bin:$PATH"
mvn -pl jeecg-boot-module/jeecg-module-content \
  -Dtest=ContentUserGrowthPenaltyRecordServiceTest test
```

Expected:

```text
Failures: 2 or more
Wanted but not invoked: pointLedgerMapper.insert(...)
Wanted but not invoked: badgeGrantMapper.updateById(...)
Wanted but not invoked: levelBenefitPenaltyRecordMapper.insert(...)
```

- [ ] **Step 5: 记录红灯基线**

```text
此时失败必须反映“真实处罚未执行”，而不是编译错误或测试夹具错误。
如出现编译错误，优先补最小测试夹具依赖。
```

## Task 2: 升级建档服务为真实处罚编排器

**Files:**
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/impl/ContentUserGrowthPenaltyRecordServiceImpl.java`
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserGrowthPenaltyRecordServiceTest.java`

- [ ] **Step 1: 在测试夹具中补齐新的 mapper mock**

```java
@Mock
private ContentUserPointLedgerMapper pointLedgerMapper;

@Mock
private ContentUserGrowthLedgerMapper growthLedgerMapper;

@Mock
private ContentUserProfileMapper profileMapper;

@Mock
private ContentUserBadgeGrantMapper badgeGrantMapper;

@Mock
private ContentUserLevelBenefitPenaltyRecordMapper levelBenefitPenaltyRecordMapper;
```

- [ ] **Step 2: 给执行服务补依赖与常量**

```java
@Resource
private ContentUserPointLedgerMapper pointLedgerMapper;

@Resource
private ContentUserGrowthLedgerMapper growthLedgerMapper;

@Resource
private ContentUserProfileMapper profileMapper;

@Resource
private ContentUserBadgeGrantMapper badgeGrantMapper;

@Resource
private ContentUserLevelBenefitPenaltyRecordMapper levelBenefitPenaltyRecordMapper;

private static final String SOURCE_TYPE_GROWTH_PENALTY = "GROWTH_PENALTY";
private static final String PENALTY_TYPE_COMPOSITE = "COMPOSITE";
private static final String BENEFIT_PRIORITY_CUSTOMER_SERVICE = "PRIORITY_CUSTOMER_SERVICE";
private static final String BENEFIT_RECOVER_STATUS_PENDING = "PENDING_RECOVER";
```

- [ ] **Step 3: 在 `createFromGovernanceRecord(...)` 中执行真实处罚**

```java
ContentUserProfile profile = profileMapper.selectByUserId(record.getUserId());
if (profile == null) {
    return;
}
PenaltyExecutionResult result = executePenalty(profile, req == null ? null : req.getOperatorUserId(),
    req == null ? null : req.getReason(), req == null ? null : req.getRuleCode(), record.getTargetStatus());
ContentUserGrowthPenaltyRecord item = new ContentUserGrowthPenaltyRecord();
item.setId(UUIDGenerator.generate());
item.setUserId(record.getUserId());
item.setGovernanceRecordId(record.getId());
item.setSourceType(SOURCE_GOVERNANCE);
item.setSourceId(record.getId());
item.setSourceStatus(record.getTargetStatus());
item.setPenaltyType(PENALTY_TYPE_COMPOSITE);
item.setEffectSnapshotJson(buildExecutionSnapshotJson(result));
item.setStatus(STATUS_PENDING_RECOVER);
item.setCreateTime(resolveExecuteTime(executeTime));
growthPenaltyRecordMapper.insert(item);
persistBenefitPenaltyRecords(item.getId(), profile.getUserId(), result, resolveExecuteTime(executeTime));
```

- [ ] **Step 4: 在 `createFromReportHandle(...)` 中复用同一套执行逻辑**

```java
ContentUserProfile profile = profileMapper.selectByUserId(report.getUserId());
if (profile == null) {
    return;
}
PenaltyExecutionResult result = executePenalty(profile, req == null ? null : req.getOperatorUserId(),
    req == null ? null : req.getResultNote(), null, report.getResultStatus());
ContentUserGrowthPenaltyRecord item = new ContentUserGrowthPenaltyRecord();
item.setId(UUIDGenerator.generate());
item.setUserId(report.getUserId());
item.setGovernanceRecordId(governanceRecordId);
item.setSourceType(SOURCE_REPORT);
item.setSourceId(report.getId());
item.setSourceStatus(report.getResultStatus());
item.setPenaltyType(PENALTY_TYPE_COMPOSITE);
item.setEffectSnapshotJson(buildExecutionSnapshotJson(result));
item.setStatus(STATUS_PENDING_RECOVER);
item.setCreateTime(resolveExecuteTime(executeTime));
growthPenaltyRecordMapper.insert(item);
persistBenefitPenaltyRecords(item.getId(), profile.getUserId(), result, resolveExecuteTime(executeTime));
```

- [ ] **Step 5: 实现最小执行逻辑**

```java
private PenaltyExecutionResult executePenalty(ContentUserProfile profile,
                                              String operatorUserId,
                                              String reason,
                                              String ruleCode,
                                              String sourceStatus) {
    PenaltyExecutionResult result = new PenaltyExecutionResult()
        .setOperatorUserId(operatorUserId)
        .setReason(reason)
        .setRuleCode(ruleCode)
        .setSourceStatus(sourceStatus);
    applyPointPenalty(profile, result);
    applyGrowthPenalty(profile, result);
    applyBadgePenalty(profile.getUserId(), result);
    applyLevelBenefitPenalty(profile.getUserId(), result);
    profileMapper.updateById(profile);
    return result;
}
```

- [ ] **Step 6: 实现积分与成长值处罚**

```java
private void applyPointPenalty(ContentUserProfile profile, PenaltyExecutionResult result) {
    int before = defaultZero(profile.getPointBalance());
    int actualDelta = Math.min(before, 20);
    if (actualDelta <= 0) {
        return;
    }
    int after = before - actualDelta;
    profile.setPointBalance(after);
    pointLedgerMapper.insert(new ContentUserPointLedger()
        .setUserId(profile.getUserId())
        .setSourceType(SOURCE_TYPE_GROWTH_PENALTY)
        .setPointDelta(-actualDelta)
        .setBalanceAfter(after)
        .setRemark(result.getSourceStatus()));
    result.setPointEffect(new PointEffect().setDelta(-actualDelta).setBalanceBefore(before).setBalanceAfter(after));
}

private void applyGrowthPenalty(ContentUserProfile profile, PenaltyExecutionResult result) {
    int beforeGrowth = defaultZero(profile.getGrowthValue());
    int beforeLevel = calculateLevel(beforeGrowth);
    int actualDelta = Math.min(beforeGrowth, 30);
    if (actualDelta <= 0) {
        return;
    }
    int afterGrowth = beforeGrowth - actualDelta;
    profile.setGrowthValue(afterGrowth);
    profile.setLevel(calculateLevel(afterGrowth));
    growthLedgerMapper.insert(new ContentUserGrowthLedger()
        .setUserId(profile.getUserId())
        .setSourceType(SOURCE_TYPE_GROWTH_PENALTY)
        .setGrowthDelta(-actualDelta)
        .setGrowthAfter(afterGrowth)
        .setRemark(result.getSourceStatus()));
    result.setGrowthEffect(new GrowthEffect()
        .setDelta(-actualDelta)
        .setGrowthBefore(beforeGrowth)
        .setGrowthAfter(afterGrowth)
        .setLevelBefore(beforeLevel)
        .setLevelAfter(profile.getLevel()));
}
```

- [ ] **Step 7: 实现勋章与等级权益处罚**

```java
private void applyBadgePenalty(String userId, PenaltyExecutionResult result) {
    List<ContentUserBadgeGrant> badgeGrants = badgeGrantMapper.selectList(
        Wrappers.<ContentUserBadgeGrant>lambdaQuery().eq(ContentUserBadgeGrant::getUserId, userId)
    );
    for (ContentUserBadgeGrant item : badgeGrants) {
        if (!"ACTIVE".equals(item.getStatus()) || !Boolean.TRUE.equals(item.getDisplaying())) {
            continue;
        }
        BadgeEffect effect = new BadgeEffect()
            .setBadgeGrantId(item.getId())
            .setBadgeCode(item.getBadgeCode())
            .setPreviousStatus(item.getStatus())
            .setPreviousDisplaying(item.getDisplaying())
            .setCurrentStatus("RECYCLED")
            .setCurrentDisplaying(Boolean.FALSE);
        item.setStatus("RECYCLED");
        item.setDisplaying(Boolean.FALSE);
        item.setRecycledAt(new Date());
        badgeGrantMapper.updateById(item);
        result.getBadgeEffects().add(effect);
    }
}

private void applyLevelBenefitPenalty(String userId, PenaltyExecutionResult result) {
    BenefitEffect effect = new BenefitEffect()
        .setBenefitCode(BENEFIT_PRIORITY_CUSTOMER_SERVICE)
        .setPreviousEnabled(Boolean.TRUE)
        .setCurrentEnabled(Boolean.FALSE);
    result.getBenefitEffects().add(effect);
}
```

- [ ] **Step 8: 运行建档服务测试并转绿**

Run:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH="$JAVA_HOME/bin:$PATH"
mvn -pl jeecg-boot-module/jeecg-module-content \
  -Dtest=ContentUserGrowthPenaltyRecordServiceTest test
```

Expected:

```text
BUILD SUCCESS
```

- [ ] **Step 9: 记录执行编排检查点**

```text
已完成真实处罚执行最小闭环。
当前恢复服务仍只兼容旧快照结构，下一任务补恢复兼容。
```

## Task 3: 补齐快照结构与等级权益子表落库

**Files:**
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/impl/ContentUserGrowthPenaltyRecordServiceImpl.java`
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserGrowthPenaltyRecordServiceTest.java`

- [ ] **Step 1: 实现真实结果快照 JSON 构造**

```java
private String buildExecutionSnapshotJson(PenaltyExecutionResult result) {
    return "{"
        + "\"operatorUserId\":\"" + escapeJson(result.getOperatorUserId()) + "\","
        + "\"reason\":\"" + escapeJson(result.getReason()) + "\","
        + "\"ruleCode\":\"" + escapeJson(result.getRuleCode()) + "\","
        + "\"sourceStatus\":\"" + escapeJson(result.getSourceStatus()) + "\","
        + "\"plannedEffects\":" + buildPlannedEffectsJson(result) + ","
        + "\"pointEffect\":" + buildPointEffectJson(result.getPointEffect()) + ","
        + "\"growthEffect\":" + buildGrowthEffectJson(result.getGrowthEffect()) + ","
        + "\"badgeEffects\":" + buildBadgeEffectsJson(result.getBadgeEffects()) + ","
        + "\"benefitEffects\":" + buildBenefitEffectsJson(result.getBenefitEffects())
        + "}";
}
```

- [ ] **Step 2: 持久化等级权益处罚子记录**

```java
private void persistBenefitPenaltyRecords(String penaltyRecordId,
                                          String userId,
                                          PenaltyExecutionResult result,
                                          Date executeTime) {
    for (BenefitEffect effect : result.getBenefitEffects()) {
        ContentUserLevelBenefitPenaltyRecord item = new ContentUserLevelBenefitPenaltyRecord();
        item.setId(UUIDGenerator.generate());
        item.setPenaltyRecordId(penaltyRecordId);
        item.setUserId(userId);
        item.setBenefitCode(effect.getBenefitCode());
        item.setPreviousEnabled(effect.getPreviousEnabled());
        item.setCurrentEnabled(effect.getCurrentEnabled());
        item.setRecoverStatus(BENEFIT_RECOVER_STATUS_PENDING);
        item.setCreateTime(executeTime);
        levelBenefitPenaltyRecordMapper.insert(item);
    }
}
```

- [ ] **Step 3: 增加“已禁用权益不重复写子表”的测试**

```java
@Test
void shouldNotInsertBenefitPenaltyRecordWhenBenefitAlreadyDisabled() {
    ContentUserStatusRecord record = new ContentUserStatusRecord()
        .setUserId("u1")
        .setTargetStatus("MUTED");
    record.setId("status-3");
    when(growthPenaltyRecordMapper.selectOne(any())).thenReturn(null);
    when(profileMapper.selectByUserId("u1")).thenReturn(new ContentUserProfile()
        .setUserId("u1")
        .setPointBalance(30)
        .setGrowthValue(80)
        .setLevel(1));
    when(badgeGrantMapper.selectList(any())).thenReturn(List.of());
    when(levelBenefitRecoveryService.hasEnabledBenefit("u1", "PRIORITY_CUSTOMER_SERVICE")).thenReturn(false);

    growthPenaltyRecordService.createFromGovernanceRecord(
        record,
        new ContentUserStatusChangeReq().setUserId("u1").setOperatorUserId("admin-1"),
        new Date()
    );

    verify(levelBenefitPenaltyRecordMapper, never()).insert(any(ContentUserLevelBenefitPenaltyRecord.class));
}
```

- [ ] **Step 4: 运行建档服务测试并保持绿色**

Run:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH="$JAVA_HOME/bin:$PATH"
mvn -pl jeecg-boot-module/jeecg-module-content \
  -Dtest=ContentUserGrowthPenaltyRecordServiceTest test
```

Expected:

```text
BUILD SUCCESS
```

- [ ] **Step 5: 记录快照结构检查点**

```text
主记录已不再写空的 plannedEffects，占位快照已升级为真实执行结果快照。
```

## Task 4: 让恢复服务兼容真实执行快照

**Files:**
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/impl/ContentUserGrowthPenaltyRecoveryServiceImpl.java`
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserGrowthPenaltyRecoveryServiceTest.java`

- [ ] **Step 1: 先写恢复服务失败测试，锁定新快照结构兼容**

```java
@Test
void shouldRecoverPenaltyEffectsFromExecutionSnapshot() {
    ContentUserAppeal appeal = new ContentUserAppeal()
        .setUserId("u1")
        .setResultStatus("APPROVED");
    appeal.setId("appeal-1");
    when(growthPenaltyRecordMapper.selectList(any())).thenReturn(List.of(new ContentUserGrowthPenaltyRecord()
        .setUserId("u1")
        .setPenaltyType("COMPOSITE")
        .setStatus("PENDING_RECOVER")
        .setEffectSnapshotJson("{\"pointEffect\":{\"delta\":-20,\"balanceBefore\":100,\"balanceAfter\":80},"
            + "\"growthEffect\":{\"delta\":-30,\"growthBefore\":260,\"growthAfter\":230,\"levelBefore\":3,\"levelAfter\":3},"
            + "\"badgeEffects\":[{\"badgeGrantId\":\"badge-grant-1\",\"previousStatus\":\"ACTIVE\",\"previousDisplaying\":true}],"
            + "\"benefitEffects\":[{\"benefitCode\":\"PRIORITY_CUSTOMER_SERVICE\",\"previousEnabled\":true,\"currentEnabled\":false}]}")));
    when(profileMapper.selectByUserId("u1")).thenReturn(new ContentUserProfile()
        .setUserId("u1")
        .setPointBalance(80)
        .setGrowthValue(230)
        .setLevel(3));
    when(badgeGrantMapper.selectById("badge-grant-1")).thenReturn(new ContentUserBadgeGrant()
        .setUserId("u1")
        .setStatus("RECYCLED")
        .setDisplaying(Boolean.FALSE));
    when(levelBenefitRecoveryService.recoverByPenaltyRecord(any(), any(), any(), any())).thenReturn(1);

    int recoveredCount = recoveryService.recoverByAppeal(appeal, "admin-1", new Date(), "处罚撤销");

    assertThat(recoveredCount).isEqualTo(1);
    verify(pointLedgerMapper).insert(argThat((ContentUserPointLedger it) -> it.getPointDelta() == 20));
    verify(growthLedgerMapper).insert(argThat((ContentUserGrowthLedger it) -> it.getGrowthDelta() == 30));
}
```

- [ ] **Step 2: 调整快照解析逻辑，优先读取新结构**

```java
private GrowthPenaltySnapshot parseSnapshot(String effectSnapshotJson) {
    if (effectSnapshotJson == null || effectSnapshotJson.isBlank()) {
        return new GrowthPenaltySnapshot();
    }
    try {
        JsonNode root = OBJECT_MAPPER.readTree(effectSnapshotJson);
        GrowthPenaltySnapshot snapshot = new GrowthPenaltySnapshot();
        JsonNode pointEffectNode = root.path("pointEffect");
        JsonNode growthEffectNode = root.path("growthEffect");
        snapshot.setPointDelta(pointEffectNode.isMissingNode() ? root.path("pointDelta").asInt(0)
            : pointEffectNode.path("delta").asInt(0));
        snapshot.setGrowthDelta(growthEffectNode.isMissingNode() ? root.path("growthDelta").asInt(0)
            : growthEffectNode.path("delta").asInt(0));
        snapshot.setBadgeEffects(parseBadgeEffects(root.path("badgeEffects")));
        snapshot.setBenefitEffects(parseBenefitEffects(root.path("benefitEffects")));
        return snapshot;
    } catch (IOException ex) {
        throw new JeecgBootException("成长处罚快照解析失败");
    }
}
```

- [ ] **Step 3: 调整恢复服务测试夹具，覆盖旧快照兼容**

```java
@Test
void shouldStillRecoverLegacySnapshot() {
    when(growthPenaltyRecordMapper.selectList(any())).thenReturn(List.of(buildPenaltyRecord()));
    when(profileMapper.selectByUserId("u1")).thenReturn(new ContentUserProfile()
        .setUserId("u1")
        .setPointBalance(80)
        .setGrowthValue(190)
        .setLevel(2));

    int recoveredCount = recoveryService.recoverByAppeal(
        new ContentUserAppeal().setUserId("u1").setResultStatus("APPROVED"),
        "admin-1",
        new Date(),
        "处罚撤销"
    );

    assertThat(recoveredCount).isEqualTo(1);
    verify(pointLedgerMapper).insert(any(ContentUserPointLedger.class));
    verify(growthLedgerMapper).insert(any(ContentUserGrowthLedger.class));
}
```

- [ ] **Step 4: 运行恢复服务测试并转绿**

Run:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH="$JAVA_HOME/bin:$PATH"
mvn -pl jeecg-boot-module/jeecg-module-content \
  -Dtest=ContentUserGrowthPenaltyRecoveryServiceTest test
```

Expected:

```text
BUILD SUCCESS
```

- [ ] **Step 5: 记录恢复兼容检查点**

```text
恢复服务已能兼容旧快照与真实执行新快照。
```

## Task 5: 补处罚执行审计与入口回归

**Files:**
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/entity/ContentUserAuditLog.java`
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/impl/ContentUserGrowthPenaltyRecordServiceImpl.java`
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserGovernanceServiceTest.java`
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserSupportServiceTest.java`

- [ ] **Step 1: 增加处罚执行审计工厂方法**

```java
public static ContentUserAuditLog growthPenaltyExecuted(String userId,
                                                        String operatorUserId,
                                                        String sourceType,
                                                        String penaltyRecordId,
                                                        int pointDelta,
                                                        int growthDelta,
                                                        int badgeCount,
                                                        int benefitCount) {
    return new ContentUserAuditLog()
        .setUserId(userId)
        .setOperatorUserId(operatorUserId)
        .setEventType("USER_GROWTH_PENALTY_EXECUTED")
        .setEventContent(sourceType)
        .setExtraDataJson("{\"penaltyRecordId\":\"" + penaltyRecordId
            + "\",\"pointDelta\":" + pointDelta
            + ",\"growthDelta\":" + growthDelta
            + ",\"badgeCount\":" + badgeCount
            + ",\"benefitCount\":" + benefitCount + "}")
        .setEventTime(new Date());
}
```

- [ ] **Step 2: 在执行服务中写处罚执行审计**

```java
if (auditLogMapper != null) {
    auditLogMapper.insert(ContentUserAuditLog.growthPenaltyExecuted(
        item.getUserId(),
        result.getOperatorUserId(),
        item.getSourceType(),
        item.getId(),
        result.getPointEffect() == null ? 0 : result.getPointEffect().getDelta(),
        result.getGrowthEffect() == null ? 0 : result.getGrowthEffect().getDelta(),
        result.getBadgeEffects().size(),
        result.getBenefitEffects().size()
    ));
}
```

- [ ] **Step 3: 增加治理入口与举报入口回归断言**

```java
verify(growthPenaltyRecordService).createFromGovernanceRecord(any(), any(), any(Date.class));
verify(growthPenaltyRecordService).createFromReportHandle(any(), any(), isNull(), any(Date.class));
```

- [ ] **Step 4: 运行入口与执行测试回归**

Run:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH="$JAVA_HOME/bin:$PATH"
mvn -pl jeecg-boot-module/jeecg-module-content \
  -Dtest=ContentUserGrowthPenaltyRecordServiceTest,ContentUserGovernanceServiceTest,ContentUserSupportServiceTest test
```

Expected:

```text
BUILD SUCCESS
```

- [ ] **Step 5: 记录审计检查点**

```text
处罚执行和处罚恢复已形成成对审计事件。
```

## Task 6: 文档同步与整体验证

**Files:**
- Modify: `docs/superpowers/specs/2026-05-02-content-user-prd-backend-coverage-report.md`
- Modify: `docs/superpowers/plans/2026-05-02-content-user-support-governance-gap-plan.md`

- [ ] **Step 1: 更新覆盖报告**

```md
- `2026-05-02` 已新增成长处罚真实执行引擎：治理处罚入口与举报处理入口可真实执行积分、成长值、等级、勋章和首批等级权益处罚，并生成可恢复回放的执行快照。
```

- [ ] **Step 2: 更新阶段计划文档**

```md
- 第二阶段已新增：成长处罚真实执行引擎，覆盖积分、成长值、等级、勋章和首批等级权益处罚闭环
- 第二阶段剩余缺口：更多等级权益消费方落地
```

- [ ] **Step 3: 运行模块聚焦回归**

Run:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH="$JAVA_HOME/bin:$PATH"
mvn -pl jeecg-boot-module/jeecg-module-content \
  -Dtest=ContentUserGrowthPenaltyRecordServiceTest,ContentUserGrowthPenaltyRecoveryServiceTest,ContentUserGovernanceServiceTest,ContentUserSupportServiceTest test
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

- [ ] **Step 5: 运行规范检查**

Run:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH="$JAVA_HOME/bin:$PATH"
python3 .trae/skills/ai-coding-java-springboot/scripts/standards-check.py \
  --target jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/impl/ContentUserGrowthPenaltyRecordServiceImpl.java \
  --target jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/impl/ContentUserGrowthPenaltyRecoveryServiceImpl.java \
  --target jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/entity/ContentUserAuditLog.java \
  --target jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserGrowthPenaltyRecordServiceTest.java \
  --target jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserGrowthPenaltyRecoveryServiceTest.java \
  --target jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserGovernanceServiceTest.java \
  --target jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserSupportServiceTest.java
```

Expected:

```text
开发规范检查通过！
```

- [ ] **Step 6: 做本轮收口说明，不在当前脏工作树直接提交**

```text
当前 worktree 仍然包含前面几轮的未提交改动。
本任务完成后只交付代码、测试结果和文档更新，不自动执行 git commit。
如需单独提交，再根据最新 worktree 状态决定提交策略。
```

## 自检结论

- Spec 覆盖：已覆盖真实处罚执行边界、快照升级、积分/成长/等级/勋章/首批等级权益处罚、恢复兼容、审计与文档更新。
- 占位词检查：计划中无 `TODO`、`TBD`、`implement later`。
- 类型一致性：统一使用 `IContentUserGrowthPenaltyRecordService`、`createFromGovernanceRecord(...)`、`createFromReportHandle(...)`、`ContentUserLevelBenefitPenaltyRecord`、`USER_GROWTH_PENALTY_EXECUTED`、`COMPOSITE`。
