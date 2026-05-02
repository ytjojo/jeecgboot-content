# Content User Level Benefit Consumers Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在 `content/user` 模块内补齐更多等级权益消费方，新增统一等级权益判定服务，为成长汇总提供权益能力摘要，并让 `customer-service` 与 `TOPIC` 订阅额度真实感知权益启用、禁用与恢复。

**Architecture:** 新增 `IContentUserLevelBenefitService` 作为运行时统一权益判定入口，负责汇总用户画像、等级默认规则和等级权益处罚子表状态，生成统一能力摘要。`ContentUserGrowthServiceImpl` 负责对外返回成长与权益汇总，`ContentUserSupportServiceImpl` 改为依赖新服务判断专属客服权益，`ContentUserSubscriptionServiceImpl` 则在 `TOPIC` 类型订阅入口基于权益额度做最小真实消费校验。

**Tech Stack:** Spring Boot 3, JeecgBoot, MyBatis-Plus, JUnit 5, Mockito

---

## 文件边界

### 新增文件

- `docs/superpowers/plans/2026-05-02-content-user-level-benefit-consumers-plan.md`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/IContentUserLevelBenefitService.java`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/impl/ContentUserLevelBenefitServiceImpl.java`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/vo/ContentUserLevelBenefitSummaryVO.java`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserLevelBenefitServiceTest.java`

### 修改文件

- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/vo/ContentUserGrowthVO.java`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/IContentUserGrowthService.java`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/impl/ContentUserGrowthServiceImpl.java`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/impl/ContentUserSupportServiceImpl.java`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/impl/ContentUserSubscriptionServiceImpl.java`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/mapper/ContentUserSubscriptionMapper.java`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserGrowthServiceTest.java`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserSupportServiceTest.java`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserSubscriptionServiceTest.java`
- `docs/superpowers/specs/2026-05-02-content-user-prd-backend-coverage-report.md`
- `docs/superpowers/plans/2026-05-02-content-user-support-governance-gap-plan.md`

## Task 1: 锁定统一权益判定红灯

**Files:**
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserLevelBenefitServiceTest.java`

- [ ] **Step 1: 写“普通用户返回默认能力”的失败测试**

```java
@Test
void shouldReturnDefaultBenefitSummaryForRegularUser() {
    when(profileMapper.selectByUserId("u1")).thenReturn(new ContentUserProfile()
        .setUserId("u1")
        .setLevel(2)
        .setGrowthValue(150)
        .setStatus("NORMAL"));
    when(levelBenefitPenaltyRecordMapper.selectList(any())).thenReturn(List.of());

    ContentUserLevelBenefitSummaryVO result = levelBenefitService.getBenefitSummary("u1");

    assertThat(result.getUploadSizeLimitMb()).isEqualTo(100);
    assertThat(result.getHdVideoEnabled()).isFalse();
    assertThat(result.getTopicQuota()).isEqualTo(10);
    assertThat(result.getEnabledBenefitCodes()).isEmpty();
}
```

- [ ] **Step 2: 写“高等级用户返回增强能力”的失败测试**

```java
@Test
void shouldReturnEnhancedBenefitSummaryForHighLevelUser() {
    when(profileMapper.selectByUserId("u2")).thenReturn(new ContentUserProfile()
        .setUserId("u2")
        .setLevel(5)
        .setGrowthValue(420)
        .setStatus("NORMAL"));
    when(levelBenefitPenaltyRecordMapper.selectList(any())).thenReturn(List.of());

    ContentUserLevelBenefitSummaryVO result = levelBenefitService.getBenefitSummary("u2");

    assertThat(result.getUploadSizeLimitMb()).isEqualTo(500);
    assertThat(result.getHdVideoEnabled()).isTrue();
    assertThat(result.getTopicQuota()).isEqualTo(30);
}
```

- [ ] **Step 3: 写“显式启用覆盖默认规则”和“显式禁用优先于高等级”的失败测试**

```java
@Test
void shouldEnableBenefitByRecoveredExplicitRecord() {
    when(profileMapper.selectByUserId("u3")).thenReturn(new ContentUserProfile()
        .setUserId("u3")
        .setLevel(1)
        .setGrowthValue(0)
        .setStatus("NORMAL"));
    when(levelBenefitPenaltyRecordMapper.selectList(any())).thenReturn(List.of(
        new ContentUserLevelBenefitPenaltyRecord()
            .setUserId("u3")
            .setBenefitCode("HD_VIDEO")
            .setCurrentEnabled(Boolean.TRUE)
            .setRecoverStatus("RECOVERED")
    ));

    ContentUserLevelBenefitSummaryVO result = levelBenefitService.getBenefitSummary("u3");

    assertThat(result.getHdVideoEnabled()).isTrue();
    assertThat(result.getEnabledBenefitCodes()).contains("HD_VIDEO");
}

@Test
void shouldPreferExplicitDisableOverHighLevelRule() {
    when(profileMapper.selectByUserId("u4")).thenReturn(new ContentUserProfile()
        .setUserId("u4")
        .setLevel(6)
        .setGrowthValue(600)
        .setStatus("NORMAL"));
    when(levelBenefitPenaltyRecordMapper.selectList(any())).thenReturn(List.of(
        new ContentUserLevelBenefitPenaltyRecord()
            .setUserId("u4")
            .setBenefitCode("TOPIC_QUOTA_EXPANDED")
            .setCurrentEnabled(Boolean.FALSE)
            .setRecoverStatus("PENDING_RECOVER")
    ));

    ContentUserLevelBenefitSummaryVO result = levelBenefitService.getBenefitSummary("u4");

    assertThat(result.getTopicQuota()).isEqualTo(10);
    assertThat(levelBenefitService.hasEnabledBenefit("u4", "TOPIC_QUOTA_EXPANDED")).isFalse();
}
```

- [ ] **Step 4: 运行新测试，确认当前缺少统一权益服务实现**

Run:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH="$JAVA_HOME/bin:$PATH"
mvn -pl jeecg-boot-module/jeecg-module-content \
  -Dtest=ContentUserLevelBenefitServiceTest test
```

Expected:

```text
Compilation failure
cannot find symbol: class IContentUserLevelBenefitService
cannot find symbol: class ContentUserLevelBenefitSummaryVO
```

- [ ] **Step 5: 记录红灯基线**

```text
红灯必须明确落在“统一权益服务和能力摘要尚不存在”，而不是测试夹具配置错误。
```

## Task 2: 实现统一等级权益判定服务

**Files:**
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/IContentUserLevelBenefitService.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/impl/ContentUserLevelBenefitServiceImpl.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/vo/ContentUserLevelBenefitSummaryVO.java`
- Test: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserLevelBenefitServiceTest.java`

- [ ] **Step 1: 新增统一权益服务契约**

```java
public interface IContentUserLevelBenefitService {

    ContentUserLevelBenefitSummaryVO getBenefitSummary(String userId);

    boolean hasEnabledBenefit(String userId, String benefitCode);

    int resolveTopicQuota(String userId);
}
```

- [ ] **Step 2: 新增权益能力摘要 VO**

```java
@Data
@Accessors(chain = true)
@Schema(description = "内容社区用户等级权益摘要")
public class ContentUserLevelBenefitSummaryVO {

    @Schema(description = "上传大小上限，单位MB")
    private Integer uploadSizeLimitMb;

    @Schema(description = "是否支持高清视频")
    private Boolean hdVideoEnabled;

    @Schema(description = "可订阅话题上限")
    private Integer topicQuota;

    @Schema(description = "当前显式启用的权益编码")
    private List<String> enabledBenefitCodes;
}
```

- [ ] **Step 3: 先写最小实现，固化常量和判定优先级**

```java
private static final int DEFAULT_UPLOAD_LIMIT_MB = 100;
private static final int ENHANCED_UPLOAD_LIMIT_MB = 500;
private static final int DEFAULT_TOPIC_QUOTA = 10;
private static final int ENHANCED_TOPIC_QUOTA = 30;
private static final String BENEFIT_PRIORITY_CUSTOMER_SERVICE = "PRIORITY_CUSTOMER_SERVICE";
private static final String BENEFIT_UPLOAD_EXPANDED = "UPLOAD_EXPANDED";
private static final String BENEFIT_HD_VIDEO = "HD_VIDEO";
private static final String BENEFIT_TOPIC_QUOTA_EXPANDED = "TOPIC_QUOTA_EXPANDED";
private static final String STATUS_PENDING_RECOVER = "PENDING_RECOVER";
private static final String STATUS_RECOVERED = "RECOVERED";

@Override
public ContentUserLevelBenefitSummaryVO getBenefitSummary(String userId) {
    ContentUserProfile profile = profileMapper == null ? null : profileMapper.selectByUserId(userId);
    Set<String> explicitlyEnabledCodes = loadExplicitlyEnabledCodes(userId);
    Set<String> explicitlyDisabledCodes = loadExplicitlyDisabledCodes(userId);
    boolean highLevelUser = isHighLevelUser(profile);

    return new ContentUserLevelBenefitSummaryVO()
        .setUploadSizeLimitMb(resolveUploadLimit(highLevelUser, explicitlyEnabledCodes, explicitlyDisabledCodes))
        .setHdVideoEnabled(resolveHdVideoEnabled(highLevelUser, explicitlyEnabledCodes, explicitlyDisabledCodes))
        .setTopicQuota(resolveTopicQuota(highLevelUser, explicitlyEnabledCodes, explicitlyDisabledCodes))
        .setEnabledBenefitCodes(new ArrayList<>(explicitlyEnabledCodes));
}
```

- [ ] **Step 4: 补足显式启用、显式禁用和高等级默认规则实现**

```java
private boolean isHighLevelUser(ContentUserProfile profile) {
    if (profile == null) {
        return false;
    }
    int level = profile.getLevel() == null ? 1 : profile.getLevel();
    int growthValue = profile.getGrowthValue() == null ? 0 : profile.getGrowthValue();
    return level >= 5 || growthValue >= 400;
}

private int resolveUploadLimit(boolean highLevelUser, Set<String> enabledCodes, Set<String> disabledCodes) {
    if (disabledCodes.contains(BENEFIT_UPLOAD_EXPANDED)) {
        return DEFAULT_UPLOAD_LIMIT_MB;
    }
    if (enabledCodes.contains(BENEFIT_UPLOAD_EXPANDED) || highLevelUser) {
        return ENHANCED_UPLOAD_LIMIT_MB;
    }
    return DEFAULT_UPLOAD_LIMIT_MB;
}
```

- [ ] **Step 5: 运行统一权益服务测试，确认转绿**

Run:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH="$JAVA_HOME/bin:$PATH"
mvn -pl jeecg-boot-module/jeecg-module-content \
  -Dtest=ContentUserLevelBenefitServiceTest test
```

Expected:

```text
Tests run: 4 or more, Failures: 0, Errors: 0
BUILD SUCCESS
```

- [ ] **Step 6: 提交统一权益服务**

```bash
git add \
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/IContentUserLevelBenefitService.java \
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/impl/ContentUserLevelBenefitServiceImpl.java \
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/vo/ContentUserLevelBenefitSummaryVO.java \
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserLevelBenefitServiceTest.java
git commit -m "feat: add runtime level benefit service"
```

## Task 3: 接入成长汇总与客服路由

**Files:**
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/vo/ContentUserGrowthVO.java`
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/impl/ContentUserGrowthServiceImpl.java`
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/impl/ContentUserSupportServiceImpl.java`
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserGrowthServiceTest.java`
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserSupportServiceTest.java`

- [ ] **Step 1: 给成长服务写“summary 返回权益能力摘要”的失败测试**

```java
@Test
void shouldReturnGrowthSummaryWithBenefitSummary() {
    when(profileMapper.selectOne(any())).thenReturn(new ContentUserProfile()
        .setUserId("u1")
        .setPointBalance(20)
        .setGrowthValue(430)
        .setLevel(5));
    when(levelBenefitService.getBenefitSummary("u1")).thenReturn(new ContentUserLevelBenefitSummaryVO()
        .setUploadSizeLimitMb(500)
        .setHdVideoEnabled(Boolean.TRUE)
        .setTopicQuota(30)
        .setEnabledBenefitCodes(List.of("HD_VIDEO")));

    ContentUserGrowthVO result = growthService.getGrowthSummary("u1");

    assertThat(result.getLevelBenefitSummary()).isNotNull();
    assertThat(result.getLevelBenefitSummary().getUploadSizeLimitMb()).isEqualTo(500);
    assertThat(result.getLevelBenefitSummary().getHdVideoEnabled()).isTrue();
}
```

- [ ] **Step 2: 给客服服务写“显式权益判断改走统一服务”的失败测试**

```java
@Test
void shouldRouteToManualPriorityWhenExplicitBenefitEnabledByUnifiedService() {
    when(profileMapper.selectByUserId("u1")).thenReturn(new ContentUserProfile()
        .setUserId("u1")
        .setStatus("NORMAL")
        .setLevel(1)
        .setGrowthValue(0));
    when(levelBenefitService.hasEnabledBenefit("u1", "PRIORITY_CUSTOMER_SERVICE")).thenReturn(true);

    ContentCustomerServiceVO result = supportService.getCustomerServiceEntry("u1");

    assertThat(result.getRouteType()).isEqualTo("MANUAL_PRIORITY");
    verify(levelBenefitService).hasEnabledBenefit("u1", "PRIORITY_CUSTOMER_SERVICE");
}
```

- [ ] **Step 3: 最小修改成长 VO 和成长服务**

```java
@Schema(description = "等级权益摘要", requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true)
private ContentUserLevelBenefitSummaryVO levelBenefitSummary;
```

```java
return new ContentUserGrowthVO()
    .setUserId(userId)
    .setPointBalance(defaultZero(profile.getPointBalance()))
    .setGrowthValue(defaultZero(profile.getGrowthValue()))
    .setLevel(defaultLevel(profile.getLevel(), profile.getGrowthValue()))
    .setLevelBenefitSummary(levelBenefitService == null ? null : levelBenefitService.getBenefitSummary(userId));
```

- [ ] **Step 4: 最小修改客服路由为依赖统一权益服务**

```java
if (levelBenefitService != null
    && StringUtils.hasText(profile.getUserId())
    && levelBenefitService.hasEnabledBenefit(profile.getUserId(), BENEFIT_PRIORITY_CUSTOMER_SERVICE)) {
    return true;
}
```

- [ ] **Step 5: 运行成长与客服测试，确认转绿**

Run:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH="$JAVA_HOME/bin:$PATH"
mvn -pl jeecg-boot-module/jeecg-module-content \
  -Dtest=ContentUserGrowthServiceTest,ContentUserSupportServiceTest test
```

Expected:

```text
Tests run: 10 or more, Failures: 0, Errors: 0
BUILD SUCCESS
```

- [ ] **Step 6: 提交成长汇总和客服接入**

```bash
git add \
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/vo/ContentUserGrowthVO.java \
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/impl/ContentUserGrowthServiceImpl.java \
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/impl/ContentUserSupportServiceImpl.java \
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserGrowthServiceTest.java \
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserSupportServiceTest.java
git commit -m "feat: expose level benefit summary"
```

## Task 4: 为 TOPIC 订阅入口接入额度校验

**Files:**
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/impl/ContentUserSubscriptionServiceImpl.java`
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/mapper/ContentUserSubscriptionMapper.java`
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserSubscriptionServiceTest.java`

- [ ] **Step 1: 给订阅服务写“普通用户 TOPIC 达到上限被拒绝”的失败测试**

```java
@Test
void shouldRejectNewTopicSubscriptionWhenQuotaReached() {
    when(subscriptionMapper.selectByUniqueKey("u1", "TOPIC", "topic-11")).thenReturn(null);
    when(subscriptionMapper.countByUserIdAndSourceType("u1", "TOPIC")).thenReturn(10L);
    when(levelBenefitService.resolveTopicQuota("u1")).thenReturn(10);

    assertThatThrownBy(() -> subscriptionService.subscribe("u1", new ContentSubscriptionReq()
        .setSourceType("TOPIC")
        .setSourceId("topic-11")
        .setSourceName("新话题")))
        .isInstanceOf(JeecgBootException.class)
        .hasMessage("当前等级可订阅话题数已达上限");
}
```

- [ ] **Step 2: 给订阅服务写“增强额度与重复订阅不误判”的失败测试**

```java
@Test
void shouldAllowTopicSubscriptionWhenExpandedQuotaEnabled() {
    when(subscriptionMapper.selectByUniqueKey("u1", "TOPIC", "topic-21")).thenReturn(null);
    when(subscriptionMapper.countByUserIdAndSourceType("u1", "TOPIC")).thenReturn(20L);
    when(levelBenefitService.resolveTopicQuota("u1")).thenReturn(30);

    String subscriptionId = subscriptionService.subscribe("u1", new ContentSubscriptionReq()
        .setSourceType("TOPIC")
        .setSourceId("topic-21")
        .setSourceName("增强话题"));

    assertThat(subscriptionId).isNotBlank();
    verify(subscriptionMapper).insert(argThat(it ->
        "u1".equals(it.getUserId()) && "TOPIC".equals(it.getSourceType())));
}

@Test
void shouldUpdateExistingTopicSubscriptionWithoutQuotaFailure() {
    ContentUserSubscription existing = new ContentUserSubscription()
        .setUserId("u1")
        .setSourceType("TOPIC")
        .setSourceId("topic-1")
        .setPaused(Boolean.TRUE);
    existing.setId("sub-1");
    when(subscriptionMapper.selectByUniqueKey("u1", "TOPIC", "topic-1")).thenReturn(existing);

    String subscriptionId = subscriptionService.subscribe("u1", new ContentSubscriptionReq()
        .setSourceType("TOPIC")
        .setSourceId("topic-1")
        .setSourceName("已存在话题"));

    assertThat(subscriptionId).isEqualTo("sub-1");
    verify(subscriptionMapper, never()).countByUserIdAndSourceType("u1", "TOPIC");
}
```

- [ ] **Step 3: 在 Mapper 中补充话题订阅数量查询方法**

```java
Long countByUserIdAndSourceType(@Param("userId") String userId, @Param("sourceType") String sourceType);
```

- [ ] **Step 4: 在订阅服务中加入 TOPIC 类型额度校验最小实现**

```java
private static final String SOURCE_TYPE_TOPIC = "TOPIC";

private void validateTopicQuotaIfNecessary(String userId, ContentSubscriptionReq req) {
    if (!SOURCE_TYPE_TOPIC.equals(req.getSourceType()) || levelBenefitService == null) {
        return;
    }
    int topicQuota = levelBenefitService.resolveTopicQuota(userId);
    Long currentCount = subscriptionMapper.countByUserIdAndSourceType(userId, SOURCE_TYPE_TOPIC);
    if (currentCount != null && currentCount >= topicQuota) {
        throw new JeecgBootException("当前等级可订阅话题数已达上限");
    }
}
```

- [ ] **Step 5: 在 `subscribe(...)` 中先判断重复订阅，再做额度校验**

```java
ContentUserSubscription existingSubscription =
    subscriptionMapper.selectByUniqueKey(userId, req.getSourceType(), req.getSourceId());
if (existingSubscription != null) {
    existingSubscription.setSourceName(req.getSourceName());
    existingSubscription.setNotificationChannels(req.getNotificationChannels());
    existingSubscription.setNotificationFrequency(req.getNotificationFrequency());
    existingSubscription.setPaused(Boolean.FALSE);
    subscriptionMapper.updateById(existingSubscription);
    return existingSubscription.getId();
}
validateTopicQuotaIfNecessary(userId, req);
```

- [ ] **Step 6: 运行订阅服务测试，确认转绿**

Run:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH="$JAVA_HOME/bin:$PATH"
mvn -pl jeecg-boot-module/jeecg-module-content \
  -Dtest=ContentUserSubscriptionServiceTest test
```

Expected:

```text
Tests run: 6 or more, Failures: 0, Errors: 0
BUILD SUCCESS
```

- [ ] **Step 7: 提交 TOPIC 额度消费方**

```bash
git add \
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/impl/ContentUserSubscriptionServiceImpl.java \
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/mapper/ContentUserSubscriptionMapper.java \
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserSubscriptionServiceTest.java
git commit -m "feat: enforce topic quota by level benefits"
```

## Task 5: 文档同步与整体验证

**Files:**
- Modify: `docs/superpowers/specs/2026-05-02-content-user-prd-backend-coverage-report.md`
- Modify: `docs/superpowers/plans/2026-05-02-content-user-support-governance-gap-plan.md`

- [ ] **Step 1: 更新覆盖报告，记录更多等级权益消费方已落地**

```md
- `2026-05-02` 已新增更多等级权益消费方：统一等级权益判定服务接管运行时权益判断，成长汇总返回上传大小、高清视频与话题额度能力摘要，`TOPIC` 订阅入口可真实感知额度变化。
```

- [ ] **Step 2: 更新阶段 gap 文档，移除剩余缺口**

```md
- 第二阶段已新增：更多等级权益消费方落地，覆盖统一权益判定、成长摘要能力输出与 `TOPIC` 订阅额度消费
- 第二阶段剩余缺口：无
```

- [ ] **Step 3: 运行聚焦回归测试**

Run:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH="$JAVA_HOME/bin:$PATH"
mvn -pl jeecg-boot-module/jeecg-module-content \
  -Dtest=ContentUserLevelBenefitServiceTest,ContentUserGrowthServiceTest,ContentUserSupportServiceTest,ContentUserSubscriptionServiceTest test
```

Expected:

```text
BUILD SUCCESS
```

- [ ] **Step 4: 运行启动模块编译回归**

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
python3 .trae/skills/ai-coding-java-springboot/scripts/standards-check.py \
  --target jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/impl/ContentUserLevelBenefitServiceImpl.java \
  --target jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/IContentUserLevelBenefitService.java \
  --target jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/vo/ContentUserLevelBenefitSummaryVO.java
python3 .trae/skills/ai-coding-java-springboot/scripts/standards-check.py \
  --target jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserLevelBenefitServiceTest.java \
  --target jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserGrowthServiceTest.java \
  --target jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserSupportServiceTest.java \
  --target jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserSubscriptionServiceTest.java
```

Expected:

```text
开发规范检查通过！
```

- [ ] **Step 6: 最终提交**

```bash
git add \
  docs/superpowers/specs/2026-05-02-content-user-prd-backend-coverage-report.md \
  docs/superpowers/plans/2026-05-02-content-user-support-governance-gap-plan.md
git commit -m "docs: close level benefit consumer gap"
```

## 自检结论

- Spec 覆盖：
  - 已覆盖统一等级权益判定服务
  - 已覆盖成长汇总能力摘要输出
  - 已覆盖客服路由改造
  - 已覆盖 `TOPIC` 订阅额度真实消费
  - 已覆盖默认规则、显式启用、显式禁用优先和文档同步
- 占位词扫描：
  - 未使用 `TODO`、`TBD`、`implement later` 之类占位描述
- 类型一致性：
  - 统一使用 `ContentUserLevelBenefitSummaryVO`
  - 统一使用 `IContentUserLevelBenefitService`
  - 统一使用 `resolveTopicQuota(...)` 作为话题额度读取方法
