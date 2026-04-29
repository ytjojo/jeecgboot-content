# Content Community User Domain Backend Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build the Java backend for the content community user domain entirely inside `jeecg-module-content`, reusing platform capabilities only through dependency composition and read-only references.

**Architecture:** Treat `jeecg-module-content` as the only writable module for this feature. Existing `SysUser`, login/auth, and common Jeecg infrastructure are consumed as stable platform dependencies, but `jeecg-system-biz`, `jeecg-system-start`, and other upstream/open-source modules are read-only and must not be changed. Use module-local SQL resources, MyBatis-Plus persistence, focused application services, and gateway/adaptor classes inside `jeecg-module-content` to integrate with platform identity.

**Tech Stack:** Java 17, Spring Boot 3.5.x, MyBatis-Plus 3.5.x, Flyway, Redis, JUnit 5, Mockito, Spring Boot Test, Jeecg common `Result`/`QueryGenerator`/security utilities.

---

## Scope Check

The PRD covers multiple independent subsystems: account orchestration, profile/privacy, relation graph, subscriptions, points/growth/badges, governance/state machine, notification preferences, and support/appeal. Do not implement this as one giant merge request. Deliver it in the six chunks below so each chunk is testable and deployable on its own.

## File Structure

### Read-only references

- Read-only reference: `jeecg-boot/jeecg-boot-module/jeecg-module-demo/src/main/java/org/jeecg/modules/demo/test/controller/JeecgDemoController.java`
- Read-only reference: `jeecg-boot/jeecg-boot-module/jeecg-module-demo/src/main/java/org/jeecg/modules/demo/test/service/impl/JeecgDemoServiceImpl.java`
- Read-only reference: `jeecg-boot/jeecg-module-system/jeecg-system-biz/src/main/java/org/jeecg/modules/system/controller/SysUserController.java`
- Read-only reference: `jeecg-boot/jeecg-module-system/jeecg-system-biz/src/main/java/org/jeecg/modules/system/entity/SysUser.java`
- Read-only reference: `jeecg-boot/jeecg-module-system/jeecg-system-biz/src/main/java/org/jeecg/modules/system/mapper/SysUserMapper.java`
- Read-only reference: `jeecg-boot/jeecg-module-system/jeecg-system-start/src/main/resources/application-dev.yml`

### Hard Constraint

- Only create or modify files under `jeecg-boot/jeecg-boot-module/jeecg-module-content/`
- Do not modify `jeecg-system-biz`, `jeecg-system-start`, `jeecg-boot-base-core`, or other upstream modules
- Platform auth endpoints such as `/sys/login` are treated as existing capabilities to consume, not extension points to edit

### New package root

- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/resources/flyway/sql/mysql/`

### New persistence layer files

- Create: `entity/ContentUserProfile.java`
- Create: `entity/ContentUserPrivacySetting.java`
- Create: `entity/ContentUserRelation.java`
- Create: `entity/ContentUserRelationGroup.java`
- Create: `entity/ContentUserSubscription.java`
- Create: `entity/ContentUserNotificationSetting.java`
- Create: `entity/ContentUserPointLedger.java`
- Create: `entity/ContentUserGrowthLedger.java`
- Create: `entity/ContentUserBadgeDefinition.java`
- Create: `entity/ContentUserBadgeGrant.java`
- Create: `entity/ContentUserStatusRecord.java`
- Create: `entity/ContentUserAppeal.java`
- Create: `entity/ContentUserAuditLog.java`
- Create: `entity/ContentUserDeviceSession.java`
- Create: `entity/ContentUserThirdAuthBinding.java`
- Create: `mapper/ContentUserProfileMapper.java`
- Create: `mapper/ContentUserPrivacySettingMapper.java`
- Create: `mapper/ContentUserRelationMapper.java`
- Create: `mapper/ContentUserRelationGroupMapper.java`
- Create: `mapper/ContentUserSubscriptionMapper.java`
- Create: `mapper/ContentUserNotificationSettingMapper.java`
- Create: `mapper/ContentUserPointLedgerMapper.java`
- Create: `mapper/ContentUserGrowthLedgerMapper.java`
- Create: `mapper/ContentUserBadgeDefinitionMapper.java`
- Create: `mapper/ContentUserBadgeGrantMapper.java`
- Create: `mapper/ContentUserStatusRecordMapper.java`
- Create: `mapper/ContentUserAppealMapper.java`
- Create: `mapper/ContentUserAuditLogMapper.java`
- Create: `mapper/ContentUserDeviceSessionMapper.java`
- Create: `mapper/ContentUserThirdAuthBindingMapper.java`

### New application/service layer files

- Create: `service/IContentAccountService.java`
- Create: `service/IContentUserProfileService.java`
- Create: `service/IContentUserRelationService.java`
- Create: `service/IContentUserSubscriptionService.java`
- Create: `service/IContentUserGrowthService.java`
- Create: `service/IContentUserGovernanceService.java`
- Create: `service/IContentUserSupportService.java`
- Create: `service/IContentUserVisibilityPolicyService.java`
- Create: `service/impl/ContentAccountServiceImpl.java`
- Create: `service/impl/ContentUserProfileServiceImpl.java`
- Create: `service/impl/ContentUserRelationServiceImpl.java`
- Create: `service/impl/ContentUserSubscriptionServiceImpl.java`
- Create: `service/impl/ContentUserGrowthServiceImpl.java`
- Create: `service/impl/ContentUserGovernanceServiceImpl.java`
- Create: `service/impl/ContentUserSupportServiceImpl.java`
- Create: `service/impl/ContentUserVisibilityPolicyServiceImpl.java`

### New gateway/adaptor files

- Create: `gateway/SystemUserAccountGateway.java`
- Create: `gateway/SystemUserSessionGateway.java`
- Create: `gateway/impl/SystemUserAccountGatewayImpl.java`
- Create: `gateway/impl/SystemUserSessionGatewayImpl.java`

### New API layer files

- Create: `controller/ContentAccountController.java`
- Create: `controller/ContentUserProfileController.java`
- Create: `controller/ContentUserRelationController.java`
- Create: `controller/ContentUserSettingsController.java`
- Create: `controller/ContentUserGrowthController.java`
- Create: `controller/ContentUserGovernanceController.java`
- Create: `controller/ContentUserSupportController.java`

### New DTO/VO/enum/constant files

- Create: `req/account/ContentRegisterReq.java`
- Create: `req/account/ContentPasswordResetReq.java`
- Create: `req/profile/ContentUserProfileUpdateReq.java`
- Create: `req/profile/ContentUserPrivacyUpdateReq.java`
- Create: `req/relation/ContentFollowReq.java`
- Create: `req/relation/ContentBatchRelationReq.java`
- Create: `req/subscription/ContentSubscriptionReq.java`
- Create: `req/growth/ContentPointAdjustReq.java`
- Create: `req/governance/ContentUserStatusChangeReq.java`
- Create: `req/support/ContentAppealCreateReq.java`
- Create: `vo/ContentUserProfileVO.java`
- Create: `vo/ContentUserRelationVO.java`
- Create: `vo/ContentUserGrowthVO.java`
- Create: `vo/ContentUserStatusVO.java`
- Create: `vo/ContentUserAuditLogVO.java`
- Create: `enums/ContentUserStatusEnum.java`
- Create: `enums/ContentUserRelationTypeEnum.java`
- Create: `enums/ContentUserVisibilityEnum.java`
- Create: `enums/ContentNotificationChannelEnum.java`
- Create: `enums/ContentCertificationTypeEnum.java`
- Create: `constant/ContentUserCacheConstant.java`
- Create: `constant/ContentUserErrorCode.java`

### New tests

- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserProfileServiceTest.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserRelationServiceTest.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserVisibilityPolicyServiceTest.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserGrowthServiceTest.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserGovernanceServiceTest.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/controller/ContentUserControllerWebMvcTest.java`

## Chunk 1: Foundation And Account Orchestration

### Task 1: Add Schema, Enums, And Base Persistence Models

**Files:**
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/resources/flyway/sql/mysql/V3.9.1_50__content_user_domain_init.sql`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/enums/ContentUserStatusEnum.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/enums/ContentUserRelationTypeEnum.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/enums/ContentUserVisibilityEnum.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/entity/ContentUserProfile.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/entity/ContentUserRelation.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/entity/ContentUserNotificationSetting.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/entity/ContentUserStatusRecord.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/entity/ContentUserAuditLog.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/mapper/ContentUserProfileMapper.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/mapper/ContentUserRelationMapper.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/mapper/ContentUserStatusRecordMapper.java`
- Test: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserEnumContractTest.java`

- [ ] **Step 1: Write the failing enum and table contract test**

```java
class ContentUserEnumContractTest {
    @Test
    void shouldExposeExpectedLifecycleStatuses() {
        assertThat(ContentUserStatusEnum.codes())
            .containsExactly("GUEST", "REGISTERED_INCOMPLETE", "NORMAL", "MUTED", "RECOMMENDATION_LIMITED", "FROZEN", "BANNED", "CANCEL_PENDING", "CANCELLED");
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `cd /Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/jeecg-boot && mvn -pl jeecg-boot-module/jeecg-module-content -am -Dtest=ContentUserEnumContractTest test`
Expected: FAIL with `cannot find symbol ContentUserStatusEnum`

- [ ] **Step 3: Write the minimal implementation**

```java
@Getter
@RequiredArgsConstructor
public enum ContentUserStatusEnum {
    GUEST("GUEST"),
    REGISTERED_INCOMPLETE("REGISTERED_INCOMPLETE"),
    NORMAL("NORMAL"),
    MUTED("MUTED"),
    RECOMMENDATION_LIMITED("RECOMMENDATION_LIMITED"),
    FROZEN("FROZEN"),
    BANNED("BANNED"),
    CANCEL_PENDING("CANCEL_PENDING"),
    CANCELLED("CANCELLED");

    private final String code;
}
```

Also add module-local Flyway tables for `content_user_profile`, `content_user_relation`, `content_user_notification_setting`, `content_user_status_record`, `content_user_audit_log`, `content_user_point_ledger`, `content_user_growth_ledger`, `content_user_badge_definition`, `content_user_badge_grant`, `content_user_appeal`, and `content_user_device_session`. Keep the SQL under `jeecg-module-content/src/main/resources/flyway/sql/mysql/` so it is discovered from the dependency classpath without changing `jeecg-system-start`.

- [ ] **Step 4: Run tests to verify they pass**

Run: `cd /Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/jeecg-boot && mvn -pl jeecg-boot-module/jeecg-module-content -am -Dtest=ContentUserEnumContractTest test`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add jeecg-boot-module/jeecg-module-content/src/main/resources/flyway/sql/mysql/V3.9.1_50__content_user_domain_init.sql jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserEnumContractTest.java
git commit -m "feat: add content user domain schema and base models"
```

### Task 2: Build Account Extension Service Around Existing `SysUser`

**Files:**
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/IContentAccountService.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/impl/ContentAccountServiceImpl.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/controller/ContentAccountController.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/gateway/SystemUserAccountGateway.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/gateway/impl/SystemUserAccountGatewayImpl.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/req/account/ContentRegisterReq.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/req/account/ContentPasswordResetReq.java`
- Test: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentAccountServiceTest.java`

- [ ] **Step 1: Write the failing account orchestration test**

```java
@Test
void shouldCreateSysUserAndBootstrapCommunityProfile() {
    when(systemUserAccountGateway.createUser(any())).thenReturn("u_1001");
    String userId = accountService.registerByMobile(registerReq());
    assertThat(userId).isEqualTo("u_1001");
    verify(profileMapper).insert(any(ContentUserProfile.class));
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `cd /Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/jeecg-boot && mvn -pl jeecg-boot-module/jeecg-module-content -am -Dtest=ContentAccountServiceTest test`
Expected: FAIL with `accountService` or `SystemUserAccountGateway` not found

- [ ] **Step 3: Write the minimal implementation**

```java
@Transactional(rollbackFor = Exception.class)
public String registerByMobile(ContentRegisterReq req) {
    String userId = systemUserAccountGateway.createUser(req);
    ContentUserProfile profile = new ContentUserProfile().setUserId(userId).setNickname(req.getNickname());
    profileMapper.insert(profile);
    notificationSettingMapper.insert(ContentUserNotificationSetting.defaults(userId));
    return userId;
}
```

Implement this through composition only:
- `SystemUserAccountGatewayImpl` reads/writes `SysUser` via mapper-level dependency from `jeecg-system-biz`
- `ContentAccountController` provides community-facing bootstrap, bind/unbind record, cancel workflow, and device/session query APIs
- Existing `/sys/login` and `/sys/logout` remain untouched; this plan only consumes them as platform capabilities
- If password reset and token issuance cannot be completed without touching base auth code, downgrade them in this phase to workflow records plus manual/admin completion hooks inside `jeecg-module-content`

- [ ] **Step 4: Run tests to verify they pass**

Run: `cd /Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/jeecg-boot && mvn -pl jeecg-boot-module/jeecg-module-content -am -Dtest=ContentAccountServiceTest test`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/controller/ContentAccountController.java jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/gateway jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentAccountServiceTest.java
git commit -m "feat: add content account orchestration service"
```

## Chunk 2: Profile, Homepage, And Privacy

### Task 3: Implement Profile Aggregate And Field Visibility

**Files:**
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/IContentUserProfileService.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/impl/ContentUserProfileServiceImpl.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/controller/ContentUserProfileController.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/req/profile/ContentUserProfileUpdateReq.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/req/profile/ContentUserPrivacyUpdateReq.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/vo/ContentUserProfileVO.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/entity/ContentUserPrivacySetting.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/mapper/ContentUserPrivacySettingMapper.java`
- Test: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserProfileServiceTest.java`

- [ ] **Step 1: Write the failing profile privacy test**

```java
@Test
void shouldHideBirthdayWhenViewerIsNotFollower() {
    ContentUserProfileVO profile = profileService.getProfile("author_1", "viewer_2");
    assertThat(profile.getBirthday()).isNull();
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `cd /Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/jeecg-boot && mvn -pl jeecg-boot-module/jeecg-module-content -am -Dtest=ContentUserProfileServiceTest test`
Expected: FAIL with `profileService` or visibility policy not implemented

- [ ] **Step 3: Write the minimal implementation**

```java
public ContentUserProfileVO getProfile(String ownerUserId, String viewerUserId) {
    ContentUserProfile profile = requireProfile(ownerUserId);
    ContentUserPrivacySetting privacy = privacyMapper.selectByUserId(ownerUserId);
    boolean birthdayVisible = visibilityPolicyService.canViewField(ownerUserId, viewerUserId, privacy.getBirthdayVisibility());
    return ContentUserProfileVO.from(profile, birthdayVisible);
}
```

Support nickname/avatar history, homepage background/theme, module ordering, certification display, field-level visibility, and cache eviction on update.

- [ ] **Step 4: Run tests to verify they pass**

Run: `cd /Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/jeecg-boot && mvn -pl jeecg-boot-module/jeecg-module-content -am -Dtest=ContentUserProfileServiceTest test`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/controller/ContentUserProfileController.java jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/impl/ContentUserProfileServiceImpl.java jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/entity/ContentUserPrivacySetting.java jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserProfileServiceTest.java
git commit -m "feat: add content user profile and privacy service"
```

## Chunk 3: Social Graph, Subscription, And Visibility Policies

### Task 4: Implement Follow, Grouping, Special Follow, And Subscription

**Files:**
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/IContentUserRelationService.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/IContentUserSubscriptionService.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/impl/ContentUserRelationServiceImpl.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/impl/ContentUserSubscriptionServiceImpl.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/controller/ContentUserRelationController.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/req/relation/ContentFollowReq.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/req/relation/ContentBatchRelationReq.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/entity/ContentUserRelationGroup.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/entity/ContentUserSubscription.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/vo/ContentUserRelationVO.java`
- Test: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserRelationServiceTest.java`

- [ ] **Step 1: Write the failing relation test**

```java
@Test
void shouldUnfollowAutomaticallyWhenRequesterBlacklistsTarget() {
    relationService.follow("u1", "u2", null);
    relationService.blacklist("u1", "u2");
    ContentUserRelation relation = relationMapper.selectByPair("u1", "u2");
    assertThat(relation.getFollowed()).isFalse();
    assertThat(relation.getBlacklisted()).isTrue();
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `cd /Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/jeecg-boot && mvn -pl jeecg-boot-module/jeecg-module-content -am -Dtest=ContentUserRelationServiceTest test`
Expected: FAIL because follow/blacklist logic is missing

- [ ] **Step 3: Write the minimal implementation**

```java
@Transactional(rollbackFor = Exception.class)
public void blacklist(String operatorUserId, String targetUserId) {
    ContentUserRelation relation = relationRepository.getOrCreate(operatorUserId, targetUserId);
    relation.setBlacklisted(true);
    relation.setMuted(true);
    relation.setBlockedByOwner(true);
    relation.setFollowed(false);
    relation.setSpecialFollow(false);
    relationMapper.upsert(relation);
}
```

Also implement follow groups, special follow notifications, batch unfollow, recommendation-reason placeholders, topic/channel/collection subscriptions, and unified subscription management.

- [ ] **Step 4: Run tests to verify they pass**

Run: `cd /Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/jeecg-boot && mvn -pl jeecg-boot-module/jeecg-module-content -am -Dtest=ContentUserRelationServiceTest test`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/controller/ContentUserRelationController.java jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/impl/ContentUserRelationServiceImpl.java jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/impl/ContentUserSubscriptionServiceImpl.java jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserRelationServiceTest.java
git commit -m "feat: add content user relation and subscription services"
```

### Task 5: Implement Visibility, Blacklist, Mute, And Search/Message Guards

**Files:**
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/IContentUserVisibilityPolicyService.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/impl/ContentUserVisibilityPolicyServiceImpl.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/controller/ContentUserSettingsController.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/constant/ContentUserCacheConstant.java`
- Test: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserVisibilityPolicyServiceTest.java`

- [ ] **Step 1: Write the failing visibility policy test**

```java
@Test
void shouldRejectContentVisibilityWhenOwnerBlocksViewer() {
    when(relationMapper.selectByPair("owner", "viewer")).thenReturn(blockedByOwner());
    assertThat(policyService.canViewContent("owner", "viewer")).isFalse();
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `cd /Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/jeecg-boot && mvn -pl jeecg-boot-module/jeecg-module-content -am -Dtest=ContentUserVisibilityPolicyServiceTest test`
Expected: FAIL with `policyService` not implemented

- [ ] **Step 3: Write the minimal implementation**

```java
public boolean canViewContent(String ownerUserId, String viewerUserId) {
    if (Objects.equals(ownerUserId, viewerUserId)) {
        return true;
    }
    ContentUserRelation ownerToViewer = relationMapper.selectByPair(ownerUserId, viewerUserId);
    if (ownerToViewer != null && ownerToViewer.getBlockedByOwner()) {
        return false;
    }
    ContentUserRelation viewerToOwner = relationMapper.selectByPair(viewerUserId, ownerUserId);
    return viewerToOwner == null || !viewerToOwner.getMuted();
}
```

Implement `canSearchUser`, `canSendPrivateMessage`, `canMention`, and cache invalidation after privacy/relation changes.

- [ ] **Step 4: Run tests to verify they pass**

Run: `cd /Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/jeecg-boot && mvn -pl jeecg-boot-module/jeecg-module-content -am -Dtest=ContentUserVisibilityPolicyServiceTest test`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/impl/ContentUserVisibilityPolicyServiceImpl.java jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/controller/ContentUserSettingsController.java jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserVisibilityPolicyServiceTest.java
git commit -m "feat: add content user visibility and guard policies"
```

## Chunk 4: Growth, Points, Level, And Badge Systems

### Task 6: Implement Points, Growth, Level, And Badge Ledgers

**Files:**
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/IContentUserGrowthService.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/impl/ContentUserGrowthServiceImpl.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/controller/ContentUserGrowthController.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/entity/ContentUserPointLedger.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/entity/ContentUserGrowthLedger.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/entity/ContentUserBadgeDefinition.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/entity/ContentUserBadgeGrant.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/vo/ContentUserGrowthVO.java`
- Test: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserGrowthServiceTest.java`

- [ ] **Step 1: Write the failing growth accounting test**

```java
@Test
void shouldKeepPointsAndGrowthInSeparateLedgers() {
    growthService.recordBehavior("u1", "CONTENT_PUBLISH", 20, 15);
    verify(pointLedgerMapper).insert(argThat(it -> it.getPointDelta() == 20));
    verify(growthLedgerMapper).insert(argThat(it -> it.getGrowthDelta() == 15));
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `cd /Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/jeecg-boot && mvn -pl jeecg-boot-module/jeecg-module-content -am -Dtest=ContentUserGrowthServiceTest test`
Expected: FAIL because the service and ledger entities do not exist

- [ ] **Step 3: Write the minimal implementation**

```java
@Transactional(rollbackFor = Exception.class)
public void recordBehavior(String userId, String sourceType, int pointDelta, int growthDelta) {
    pointLedgerMapper.insert(ContentUserPointLedger.of(userId, sourceType, pointDelta));
    growthLedgerMapper.insert(ContentUserGrowthLedger.of(userId, sourceType, growthDelta));
    badgeGrantService.tryGrantAutoBadges(userId, sourceType);
}
```

Add daily caps, redemption deductions, upgrade notifications, expiration/recovery for badges, and downgrade protection rules.

- [ ] **Step 4: Run tests to verify they pass**

Run: `cd /Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/jeecg-boot && mvn -pl jeecg-boot-module/jeecg-module-content -am -Dtest=ContentUserGrowthServiceTest test`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/impl/ContentUserGrowthServiceImpl.java jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/entity/ContentUserPointLedger.java jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/entity/ContentUserGrowthLedger.java jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/entity/ContentUserBadgeDefinition.java jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/entity/ContentUserBadgeGrant.java jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserGrowthServiceTest.java
git commit -m "feat: add content user growth point and badge ledgers"
```

## Chunk 5: Governance, Lifecycle, Notification, And Support

### Task 7: Implement Status Machine, Device Sessions, Notification Preferences, And Audit

**Files:**
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/IContentUserGovernanceService.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/impl/ContentUserGovernanceServiceImpl.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/controller/ContentUserGovernanceController.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/entity/ContentUserDeviceSession.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/req/governance/ContentUserStatusChangeReq.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/vo/ContentUserStatusVO.java`
- Test: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserGovernanceServiceTest.java`

- [ ] **Step 1: Write the failing status transition test**

```java
@Test
void shouldRecordAuditWhenUserIsMuted() {
    governanceService.changeStatus(changeReq("u1", ContentUserStatusEnum.MUTED));
    verify(statusRecordMapper).insert(any(ContentUserStatusRecord.class));
    verify(auditLogMapper).insert(argThat(it -> "USER_STATUS_CHANGE".equals(it.getEventType())));
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `cd /Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/jeecg-boot && mvn -pl jeecg-boot-module/jeecg-module-content -am -Dtest=ContentUserGovernanceServiceTest test`
Expected: FAIL because status machine and audit service are missing

- [ ] **Step 3: Write the minimal implementation**

```java
@Transactional(rollbackFor = Exception.class)
public void changeStatus(ContentUserStatusChangeReq req) {
    validateTransition(req.getCurrentStatus(), req.getTargetStatus());
    statusRecordMapper.insert(ContentUserStatusRecord.from(req));
    auditLogMapper.insert(ContentUserAuditLog.statusChange(req));
}
```

Include frozen/banned/cancel-pending flows, automatic unmute scheduling, login restriction checks, device session list/offline APIs, notification matrix (`type x channel x frequency x dnd`), and security-notification whitelist bypass.

- [ ] **Step 4: Run tests to verify they pass**

Run: `cd /Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/jeecg-boot && mvn -pl jeecg-boot-module/jeecg-module-content -am -Dtest=ContentUserGovernanceServiceTest test`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/impl/ContentUserGovernanceServiceImpl.java jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/controller/ContentUserGovernanceController.java jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/entity/ContentUserDeviceSession.java jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserGovernanceServiceTest.java
git commit -m "feat: add content user governance state machine and audit"
```

### Task 8: Implement Appeal, Report, Help, And Customer Support Entry Points

**Files:**
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/IContentUserSupportService.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/impl/ContentUserSupportServiceImpl.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/controller/ContentUserSupportController.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/entity/ContentUserAppeal.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/req/support/ContentAppealCreateReq.java`
- Test: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserSupportServiceTest.java`

- [ ] **Step 1: Write the failing appeal creation test**

```java
@Test
void shouldCreateAppealAgainstPenaltyAndRecordProgress() {
    String appealId = supportService.createAppeal(createAppealReq());
    assertThat(appealId).isNotBlank();
    verify(auditLogMapper).insert(argThat(it -> "USER_APPEAL_CREATED".equals(it.getEventType())));
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `cd /Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/jeecg-boot && mvn -pl jeecg-boot-module/jeecg-module-content -am -Dtest=ContentUserSupportServiceTest test`
Expected: FAIL because support service and appeal entity do not exist

- [ ] **Step 3: Write the minimal implementation**

```java
@Transactional(rollbackFor = Exception.class)
public String createAppeal(ContentAppealCreateReq req) {
    ContentUserAppeal appeal = ContentUserAppeal.from(req);
    appealMapper.insert(appeal);
    auditLogMapper.insert(ContentUserAuditLog.appealCreated(appeal));
    return appeal.getId();
}
```

Expose endpoints for report submission, appeal progress, help-center metadata, and smart/manual customer-support routing.

- [ ] **Step 4: Run tests to verify they pass**

Run: `cd /Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/jeecg-boot && mvn -pl jeecg-boot-module/jeecg-module-content -am -Dtest=ContentUserSupportServiceTest test`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/impl/ContentUserSupportServiceImpl.java jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/controller/ContentUserSupportController.java jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/entity/ContentUserAppeal.java jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserSupportServiceTest.java
git commit -m "feat: add content user appeal and support APIs"
```

## Chunk 6: Integration, Regression, And Handoff

### Task 9: Add Module-Local Web And Service Regression Tests

**Files:**
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/controller/ContentUserControllerWebMvcTest.java`
- Modify: module-local test fixtures only under `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/`
- Test target: all controllers under `org.jeecg.modules.content.user.controller`

- [ ] **Step 1: Write the failing API regression test**

```java
@WebMvcTest({
    ContentUserGovernanceController.class,
    ContentUserProfileController.class,
    ContentUserRelationController.class
})
class ContentUserControllerWebMvcTest {
    @Test
    void shouldRejectMutedUserCommentPermission() {
        // mock governanceService.canExecuteAction(...)
        // call GET /content/user/governance/permission/check
        // assert response result is false
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `cd /Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/jeecg-boot && mvn -pl jeecg-boot-module/jeecg-module-content -am -Dtest=ContentUserControllerWebMvcTest test`
Expected: FAIL because controller wiring or endpoints are incomplete

- [ ] **Step 3: Write the minimal implementation**

```java
@GetMapping("/permission/check")
public Result<Boolean> checkPermission(@RequestParam String actionType) {
    return Result.OK(governanceService.canExecuteAction(SecureUtil.currentUser().getId(), actionType));
}
```

Cover these regression scenarios:
- community bootstrap creates profile and default notification settings
- profile privacy hides restricted fields
- blacklist removes follow/special-follow behavior
- muted/frozen/banned/cancel-pending statuses restrict expected actions
- points and growth ledgers stay separated
- disabled notification channels stop non-security delivery
- sensitive actions write audit logs

- [ ] **Step 4: Run tests to verify they pass**

Run: `cd /Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/jeecg-boot && mvn -pl jeecg-boot-module/jeecg-module-content -am -Dtest=ContentUserControllerWebMvcTest test`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/controller/ContentUserControllerWebMvcTest.java jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user
git commit -m "test: add content user domain api regression coverage"
```

## Review Checklist

- [ ] `SysUser` remains the source of truth for account identity; no duplicate user master table is introduced.
- [ ] All writable changes stay inside `jeecg-module-content`; upstream/base modules remain untouched.
- [ ] Community user tables keep `user_id` as the join key and use Jeecg audit fields.
- [ ] Blacklist has higher priority than follow, subscription, notification, and visibility.
- [ ] Points and growth are ledgered separately and can share the same behavior event source.
- [ ] Privacy changes take effect immediately and evict relevant Redis keys.
- [ ] Sensitive actions always write `content_user_audit_log`.
- [ ] Controllers stay thin; rule-heavy logic lives in services/policies.
- [ ] Platform login/auth is integrated through gateway/adaptor composition, not by editing `LoginController` or `SysUserServiceImpl`.
- [ ] Each chunk can be released independently behind menu/API permission control if needed.

## Suggested Execution Order

1. Chunk 1 first, because every later service depends on schema and account bootstrap.
2. Chunk 2 and Chunk 3 next, because profile/privacy and relation policy drive most user-visible behavior.
3. Chunk 5 governance before exposing public interaction endpoints broadly.
4. Chunk 4 growth after the core lifecycle and relation rules are stable.
5. Chunk 6 last to lock regression coverage before frontend integration.

## Commands Summary

- Content module unit tests:
  `cd /Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/jeecg-boot && mvn -pl jeecg-boot-module/jeecg-module-content -am test`
- Focused API regression:
  `cd /Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/jeecg-boot && mvn -pl jeecg-boot-module/jeecg-module-content -am -Dtest=ContentUserControllerWebMvcTest test`

Plan complete and saved to `docs/superpowers/plans/2026-04-29-content-community-user-domain-backend-plan.md`. Ready to execute?
