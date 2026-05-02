# Content User Review Fixes Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 分批修复本轮代码审查发现的恢复范围错误、权益禁用失效、历史记录聚合错误以及订阅并发与接口兼容风险。

**Architecture:** 先收敛会错误恢复数据状态的链路，再修正运行时等级权益语义，随后补齐 `TOPIC` 额度并发保护与对外接口兼容性。每一批都用先写失败测试、最小实现、聚焦回归的方式推进，避免在脏 worktree 上一次性混改过多行为。

**Tech Stack:** Spring Boot 3, JeecgBoot, MyBatis-Plus, Flyway, JUnit 5, Mockito, MockMvc

---

## 文件边界

### 主要修改文件

- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/impl/ContentUserSupportServiceImpl.java`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/impl/ContentUserGrowthPenaltyRecoveryServiceImpl.java`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/impl/ContentUserGrowthPenaltyRecordServiceImpl.java`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/impl/ContentUserLevelBenefitServiceImpl.java`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/impl/ContentUserSupportServiceImpl.java`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/impl/ContentUserSubscriptionServiceImpl.java`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/mapper/ContentUserSubscriptionMapper.java`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/controller/ContentUserSupportController.java`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/IContentUserSupportService.java`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/vo/ContentHelpCenterVO.java`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/resources/flyway/sql/mysql/`

### 主要测试文件

- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserSupportServiceTest.java`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserLevelBenefitServiceTest.java`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserSubscriptionServiceTest.java`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/controller/ContentUserSupportControllerWebMvcTest.java`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserGovernanceServiceTest.java`

## 分批策略

### Batch 1: 申诉恢复范围与治理状态恢复绑定

**目标:** 先修“一个申诉批准会恢复全部待恢复处罚”的 blocker，并让治理状态恢复只针对申诉目标，不再拿“最新记录”猜测。

**Files:**
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserSupportServiceTest.java`
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/impl/ContentUserSupportServiceImpl.java`
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/impl/ContentUserGrowthPenaltyRecoveryServiceImpl.java`
- Test: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserSupportServiceTest.java`

- [ ] **Step 1: 写“批准单个申诉不能恢复其他处罚”的失败测试**

```java
@Test
void shouldRecoverOnlyPenaltyBoundToApprovedAppeal() {
    ContentUserAppeal appeal = new ContentUserAppeal()
        .setUserId("u1")
        .setTargetType("GROWTH_PENALTY")
        .setTargetId("penalty-1")
        .setStatus("PENDING");
    appeal.setId("appeal-1");
    when(appealMapper.selectById("appeal-1")).thenReturn(appeal);

    ContentAppealHandleReq req = new ContentAppealHandleReq()
        .setAppealId("appeal-1")
        .setStatus("RESOLVED")
        .setResultStatus("APPROVED")
        .setOperatorUserId("admin")
        .setResultNote("申诉成立");

    supportService.handleAppeal(req);

    verify(growthPenaltyRecoveryService).recoverByAppeal(
        argThat(it -> "appeal-1".equals(it.getId()) && "penalty-1".equals(it.getTargetId())),
        eq("admin"),
        any(Date.class),
        eq("申诉成立")
    );
}
```

- [ ] **Step 2: 写“治理状态恢复只恢复申诉绑定治理记录”的失败测试**

```java
@Test
void shouldRestoreGovernanceStatusFromAppealTargetInsteadOfLatestRecord() {
    ContentUserAppeal appeal = new ContentUserAppeal()
        .setUserId("u1")
        .setTargetType("GOVERNANCE_STATUS")
        .setTargetId("record-1")
        .setStatus("PENDING");
    appeal.setId("appeal-2");
    when(appealMapper.selectById("appeal-2")).thenReturn(appeal);
    when(profileMapper.selectByUserId("u1")).thenReturn(new ContentUserProfile()
        .setUserId("u1")
        .setStatus("FROZEN"));
    when(statusRecordMapper.selectById("record-1")).thenReturn(new ContentUserStatusRecord()
        .setId("record-1")
        .setUserId("u1")
        .setCurrentStatus("NORMAL")
        .setTargetStatus("FROZEN")
        .setRecoverable(Boolean.TRUE));

    supportService.handleAppeal(new ContentAppealHandleReq()
        .setAppealId("appeal-2")
        .setStatus("RESOLVED")
        .setResultStatus("APPROVED")
        .setOperatorUserId("admin")
        .setResultNote("恢复状态"));

    verify(statusRecordMapper, never()).selectLatestByUserId("u1");
    verify(profileMapper).updateById(argThat(it -> "NORMAL".equals(it.getStatus())));
}
```

- [ ] **Step 3: 最小实现为“按申诉 targetType/targetId 精确恢复”**

```java
private void restoreGovernanceStatusIfNecessary(ContentUserAppeal appeal, ContentAppealHandleReq req) {
    if (!"APPROVED".equalsIgnoreCase(req.getResultStatus())
        || !"GOVERNANCE_STATUS".equals(appeal.getTargetType())
        || !StringUtils.hasText(appeal.getTargetId())) {
        return;
    }
    ContentUserStatusRecord targetRecord = statusRecordMapper.selectById(appeal.getTargetId());
    if (targetRecord == null || !Boolean.TRUE.equals(targetRecord.getRecoverable())) {
        return;
    }
    applyGovernanceRestore(targetRecord, req.getOperatorUserId(), req.getResultNote());
}

@Override
public int recoverByAppeal(ContentUserAppeal appeal, String operatorUserId, Date executeTime, String reason) {
    if (appeal == null || !APPEAL_RESULT_APPROVED.equalsIgnoreCase(appeal.getResultStatus())) {
        return 0;
    }
    if (!"GROWTH_PENALTY".equals(appeal.getTargetType()) || !StringUtils.hasText(appeal.getTargetId())) {
        return 0;
    }
    return recoverPendingRecords(
        Wrappers.<ContentUserGrowthPenaltyRecord>lambdaQuery()
            .eq(ContentUserGrowthPenaltyRecord::getId, appeal.getTargetId())
            .eq(ContentUserGrowthPenaltyRecord::getStatus, STATUS_PENDING_RECOVER),
        TRIGGER_APPEAL_APPROVED,
        operatorUserId,
        executeTime,
        reason,
        appeal.getId()
    );
}
```

- [ ] **Step 4: 把恢复写回记录改成非处罚记录**

```java
restoreRecord.setRecoverable(Boolean.FALSE);
restoreRecord.setEffectiveEndTime(null);
```

- [ ] **Step 5: 运行聚焦测试确认修复**

Run:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH="$JAVA_HOME/bin:$PATH"
cd /Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/.worktrees/audit-user-prd-coverage/jeecg-boot
mvn -pl jeecg-boot-module/jeecg-module-content \
  -Dtest=ContentUserSupportServiceTest test
```

Expected:

```text
BUILD SUCCESS
```

### Batch 2: 权益禁用/恢复语义修正

**目标:** 修正高等级兜底绕过显式禁用、`previousEnabled` 硬编码以及运行时状态聚合顺序敏感问题。

**Files:**
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserLevelBenefitServiceTest.java`
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserSupportServiceTest.java`
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/impl/ContentUserLevelBenefitServiceImpl.java`
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/impl/ContentUserGrowthPenaltyRecordServiceImpl.java`
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/impl/ContentUserSupportServiceImpl.java`

- [ ] **Step 1: 写“恢复后的最新权益记录应覆盖旧禁用记录”的失败测试**

```java
@Test
void shouldUseLatestBenefitRecordStateForSameBenefitCode() {
    when(profileMapper.selectByUserId("u1")).thenReturn(new ContentUserProfile()
        .setUserId("u1")
        .setLevel(1)
        .setGrowthValue(0));
    when(levelBenefitPenaltyRecordMapper.selectList(any())).thenReturn(List.of(
        new ContentUserLevelBenefitPenaltyRecord()
            .setBenefitCode("HD_VIDEO")
            .setCurrentEnabled(Boolean.FALSE)
            .setRecoverStatus("PENDING_RECOVER")
            .setCreateTime(new Date(1000L)),
        new ContentUserLevelBenefitPenaltyRecord()
            .setBenefitCode("HD_VIDEO")
            .setCurrentEnabled(Boolean.TRUE)
            .setRecoverStatus("RECOVERED")
            .setCreateTime(new Date(2000L))
    ));

    assertThat(levelBenefitService.hasEnabledBenefit("u1", "HD_VIDEO")).isTrue();
}
```

- [ ] **Step 2: 写“显式禁用优先于高等级兜底”的失败测试**

```java
@Test
void shouldNotRouteManualPriorityWhenBenefitExplicitlyDisabled() {
    when(profileMapper.selectByUserId("u1")).thenReturn(new ContentUserProfile()
        .setUserId("u1")
        .setStatus("NORMAL")
        .setLevel(6)
        .setGrowthValue(600));
    when(levelBenefitService.getBenefitSummary("u1")).thenReturn(new ContentUserLevelBenefitSummaryVO()
        .setEnabledBenefitCodes(List.of()));
    when(levelBenefitService.hasEnabledBenefit("u1", "PRIORITY_CUSTOMER_SERVICE")).thenReturn(false);

    ContentCustomerServiceVO result = supportService.getCustomerServiceEntry("u1");

    assertThat(result.getRouteType()).isEqualTo("SMART_FIRST");
}
```

- [ ] **Step 3: 写“处罚前未启用的权益恢复后不能被凭空启用”的失败测试**

```java
@Test
void shouldPersistRealPreviousEnabledStateWhenCreatingBenefitPenaltyRecord() {
    ContentUserProfile profile = new ContentUserProfile()
        .setUserId("u1")
        .setLevel(1)
        .setGrowthValue(0);
    when(profileMapper.selectByUserId("u1")).thenReturn(profile);

    growthPenaltyRecordService.createFromGovernanceRecord(
        new ContentUserStatusRecord().setId("record-1").setUserId("u1").setTargetStatus("MUTED"),
        new ContentUserStatusChangeReq().setUserId("u1").setTargetStatus("MUTED").setCurrentStatus("NORMAL").setOperatorUserId("admin"),
        new Date()
    );

    verify(levelBenefitPenaltyRecordMapper).insert(argThat(it -> Boolean.FALSE.equals(it.getPreviousEnabled())));
}
```

- [ ] **Step 4: 最小实现为“按 benefitCode 选最新状态 + 客服路由显式识别禁用”**

```java
private BenefitState loadBenefitState(String userId) {
    Map<String, ContentUserLevelBenefitPenaltyRecord> latestRecordMap = new LinkedHashMap<>();
    for (ContentUserLevelBenefitPenaltyRecord item : records) {
        if (!StringUtils.hasText(item.getBenefitCode())) {
            continue;
        }
        ContentUserLevelBenefitPenaltyRecord existing = latestRecordMap.get(item.getBenefitCode());
        if (existing == null || compareRecordTime(item, existing) >= 0) {
            latestRecordMap.put(item.getBenefitCode(), item);
        }
    }
    for (ContentUserLevelBenefitPenaltyRecord item : latestRecordMap.values()) {
        if (STATUS_PENDING_RECOVER.equals(item.getRecoverStatus()) && Boolean.FALSE.equals(item.getCurrentEnabled())) {
            state.disabledCodes.add(item.getBenefitCode());
        } else if (STATUS_RECOVERED.equals(item.getRecoverStatus()) && Boolean.TRUE.equals(item.getCurrentEnabled())) {
            state.enabledCodes.add(item.getBenefitCode());
        }
    }
    return state;
}

private boolean shouldRouteToManualPriority(ContentUserProfile profile) {
    if (profile == null) {
        return false;
    }
    if (levelBenefitService == null || !StringUtils.hasText(profile.getUserId())) {
        return isHighLevelUser(profile);
    }
    ContentUserLevelBenefitSummaryVO summary = levelBenefitService.getBenefitSummary(profile.getUserId());
    if (summary != null && !summary.getEnabledBenefitCodes().contains(BENEFIT_PRIORITY_CUSTOMER_SERVICE)
        && !isHighLevelUser(profile)) {
        return false;
    }
    return levelBenefitService.hasEnabledBenefit(profile.getUserId(), BENEFIT_PRIORITY_CUSTOMER_SERVICE)
        || isHighLevelUser(profile);
}
```

- [ ] **Step 5: 将处罚前状态改为运行时真实快照**

```java
boolean benefitPreviouslyEnabled = levelBenefitService != null
    && levelBenefitService.hasEnabledBenefit(profile.getUserId(), BENEFIT_PRIORITY_CUSTOMER_SERVICE);
benefitRecord.setPreviousEnabled(benefitPreviouslyEnabled);
benefitRecord.setCurrentEnabled(Boolean.FALSE);
```

- [ ] **Step 6: 运行权益与客服聚焦测试**

Run:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH="$JAVA_HOME/bin:$PATH"
cd /Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/.worktrees/audit-user-prd-coverage/jeecg-boot
mvn -pl jeecg-boot-module/jeecg-module-content \
  -Dtest=ContentUserLevelBenefitServiceTest,ContentUserSupportServiceTest test
```

Expected:

```text
BUILD SUCCESS
```

### Batch 3: TOPIC 额度并发保护

**目标:** 让 `TOPIC` 订阅不再依赖“先查再插”的乐观路径，至少具备数据库唯一约束和可测试的异常映射。

**Files:**
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserSubscriptionServiceTest.java`
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/impl/ContentUserSubscriptionServiceImpl.java`
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/mapper/ContentUserSubscriptionMapper.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/resources/flyway/sql/mysql/V3.9.1_53__content_user_subscription_unique_constraint.sql`

- [ ] **Step 1: 写“重复插入要转成幂等或业务错误”的失败测试**

```java
@Test
void shouldTranslateDuplicateTopicSubscriptionToBusinessError() {
    when(subscriptionMapper.selectByUniqueKey("u1", "TOPIC", "topic-1")).thenReturn(null);
    when(subscriptionMapper.countByUserIdAndSourceType("u1", "TOPIC")).thenReturn(1L);
    when(levelBenefitService.resolveTopicQuota("u1")).thenReturn(10);
    doThrow(new DuplicateKeyException("uk_user_source")).when(subscriptionMapper)
        .insert(any(ContentUserSubscription.class));

    assertThatThrownBy(() -> subscriptionService.subscribe("u1", new ContentSubscriptionReq()
        .setSourceType("TOPIC")
        .setSourceId("topic-1")
        .setSourceName("并发话题")))
        .isInstanceOf(JeecgBootException.class)
        .hasMessageContaining("请勿重复订阅");
}
```

- [ ] **Step 2: 补唯一索引脚本**

```sql
alter table content_user_subscription
    add unique key uk_content_user_subscription_user_source (
        user_id,
        source_type,
        source_id
    );
```

- [ ] **Step 3: 在服务层兜底翻译数据库并发异常**

```java
try {
    subscriptionMapper.insert(subscription);
} catch (DuplicateKeyException ex) {
    throw new JeecgBootException("请勿重复订阅同一话题");
}
```

- [ ] **Step 4: 运行订阅聚焦测试**

Run:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH="$JAVA_HOME/bin:$PATH"
cd /Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/.worktrees/audit-user-prd-coverage/jeecg-boot
mvn -pl jeecg-boot-module/jeecg-module-content \
  -Dtest=ContentUserSubscriptionServiceTest test
```

Expected:

```text
BUILD SUCCESS
```

### Batch 4: 帮助中心接口兼容性收口

**目标:** 收敛 `/help-center` 从无参+字符串列表到强依赖 `userId` + 复杂对象的兼容性破坏，至少保留旧调用可用。

**Files:**
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/controller/ContentUserSupportControllerWebMvcTest.java`
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/controller/ContentUserSupportController.java`
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/IContentUserSupportService.java`
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/impl/ContentUserSupportServiceImpl.java`

- [ ] **Step 1: 写“旧调用不传 userId 也能返回默认帮助中心”的失败测试**

```java
@Test
void shouldReturnHelpCenterWithoutUserIdForBackwardCompatibility() throws Exception {
    when(supportService.getHelpCenter(null)).thenReturn(new ContentHelpCenterVO()
        .setFaqCategories(List.of())
        .setGuideEntries(List.of())
        .setReleaseNotes(List.of()));

    mockMvc.perform(get("/content/user/support/help-center"))
        .andExpect(status().isOk());
}
```

- [ ] **Step 2: 最小实现为“userId 改为可选”**

```java
@GetMapping("/help-center")
public Result<ContentHelpCenterVO> getHelpCenter(
        @RequestParam(value = "userId", required = false) String userId) {
    return Result.OK(supportService.getHelpCenter(userId));
}
```

- [ ] **Step 3: 运行 Controller 聚焦测试**

Run:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH="$JAVA_HOME/bin:$PATH"
cd /Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/.worktrees/audit-user-prd-coverage/jeecg-boot
mvn -pl jeecg-boot-module/jeecg-module-content \
  -Dtest=ContentUserSupportControllerWebMvcTest test
```

Expected:

```text
BUILD SUCCESS
```

### Batch 5: 整体验证与回归

**目标:** 用最小但覆盖关键风险的测试矩阵确认四批修复没有引入回归。

**Files:**
- Test: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserSupportServiceTest.java`
- Test: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserLevelBenefitServiceTest.java`
- Test: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserSubscriptionServiceTest.java`
- Test: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/controller/ContentUserSupportControllerWebMvcTest.java`
- Test: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserGovernanceServiceTest.java`

- [ ] **Step 1: 运行关键测试矩阵**

Run:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH="$JAVA_HOME/bin:$PATH"
cd /Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/.worktrees/audit-user-prd-coverage/jeecg-boot
mvn -pl jeecg-boot-module/jeecg-module-content \
  -Dtest=ContentUserSupportServiceTest,ContentUserLevelBenefitServiceTest,ContentUserSubscriptionServiceTest,ContentUserSupportControllerWebMvcTest,ContentUserGovernanceServiceTest test
```

Expected:

```text
BUILD SUCCESS
```

- [ ] **Step 2: 运行启动模块编译**

Run:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH="$JAVA_HOME/bin:$PATH"
cd /Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/.worktrees/audit-user-prd-coverage/jeecg-boot
mvn -pl jeecg-module-system/jeecg-system-start -am -DskipTests compile
```

Expected:

```text
BUILD SUCCESS
```

- [ ] **Step 3: 运行规范检查**

Run:

```bash
cd /Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/.worktrees/audit-user-prd-coverage
python3 .trae/skills/ai-coding-java-springboot/scripts/standards-check.py \
  --target jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user \
  --target jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user
```

Expected:

```text
开发规范检查通过！
```

## 自检

- Spec coverage:
  - 已覆盖申诉误恢复全部处罚、治理状态恢复绑定错误、权益禁用失效、历史聚合错误、并发订阅风险、帮助中心接口兼容问题。
  - 未把“帮助中心返回结构回退成旧 `List<String>`”纳入本计划，因为那会扩大变更面；本计划只保证旧入口不因缺少 `userId` 直接 400。

- Placeholder scan:
  - 已避免使用 `TODO`、`TBD`、`similar to above`。
  - 每批都有明确测试、实现方向与运行命令。

- Type consistency:
  - 计划统一使用 `GROWTH_PENALTY` 和 `GOVERNANCE_STATUS` 作为申诉绑定目标类型；实现前需要先确认现有 `appealType/targetType` 实际值是否一致，不一致则在 Batch 1 先统一常量。
