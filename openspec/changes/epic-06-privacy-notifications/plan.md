# EPIC-06 Privacy Notifications Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement content community privacy controls, notification preferences, third-party authorization management, and account security settings for EPIC-06.

**Architecture:** Extend the existing content user settings services instead of introducing a separate module. Keep notification decisions in `ContentUserNotificationSettingServiceImpl`, privacy decisions in `ContentUserVisibilityPolicyServiceImpl`, and third-party authorization behind a content-domain service plus token revocation port.

**Tech Stack:** Spring Boot 3, MyBatis-Plus, Flyway MySQL migrations, JUnit 5, Mockito, Spring MVC test, OpenSpec.

---

## File Map

- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/entity/ContentUserNotificationSetting.java`
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/entity/ContentUserPrivacySetting.java`
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/controller/ContentUserSettingsController.java`
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/IContentUserNotificationSettingService.java`
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/IContentUserVisibilityPolicyService.java`
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/impl/ContentUserNotificationSettingServiceImpl.java`
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/impl/ContentUserVisibilityPolicyServiceImpl.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/entity/ContentUserNotificationDeliveryLog.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/entity/ContentThirdPartyAuthorization.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/mapper/ContentUserNotificationDeliveryLogMapper.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/mapper/ContentThirdPartyAuthorizationMapper.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/IContentThirdPartyAuthorizationService.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/ContentThirdPartyTokenRevocationPort.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/impl/ContentThirdPartyAuthorizationServiceImpl.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/impl/ContentNoopThirdPartyTokenRevocationPort.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/dto/ContentNotificationDecisionDTO.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/req/settings/ContentThirdPartyAuthorizationRevokeReq.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/vo/ContentThirdPartyAuthorizationVO.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/vo/ContentThirdPartyAuthorizationDetailVO.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/vo/ContentAccountSecuritySettingVO.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/resources/flyway/sql/mysql/V3.9.1_58__content_privacy_notifications.sql`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/resources/flyway/sql/mysql/V3.9.1_58__content_privacy_notifications_rollback.sql`

## Steps

### Step 1.1: RED — Database And DTO Contracts
- [ ] Write failing tests in `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentPrivacyNotificationsMigrationTest.java` asserting migration and rollback files exist and contain `content_user_notification_delivery_log`, `content_third_party_authorization`, `browse_history_visibility`, `online_status_visibility`, and matching rollback drops.
- [ ] Extend `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/req/ContentUserReqValidationTest.java` with invalid channel, invalid visibility enum, invalid DND time, and blank revoke reason cases.
- [ ] Verify: `/bin/zsh -lc "JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home mvn -pl jeecg-boot-module/jeecg-module-content -Dtest=ContentPrivacyNotificationsMigrationTest,ContentUserReqValidationTest test"`
- [ ] Expected evidence: tests fail because files/classes/fields are missing.

### Step 1.2: GREEN — Database And DTO Contracts
- [ ] Create migration SQL, rollback SQL, entities, mappers, DTOs, reqs, and VOs listed in the File Map so Step 1.1 compiles and passes.
- [ ] Minimal implementation notes: entities extend `JeecgEntity`; use `@TableName`; req validation messages are Chinese; rollback SQL reverses every added table, index, and column.
- [ ] Verify: same Maven command from Step 1.1.
- [ ] Expected evidence: `ContentPrivacyNotificationsMigrationTest` and `ContentUserReqValidationTest` pass.

### Step 1.3: REFACTOR — Database And DTO Contracts
- [ ] Review new entity and DTO comments, mapper method names, SQL index names, and JSON field names for consistency with existing `ContentUserNotificationSetting` and `ContentUserPrivacySetting`.
- [ ] Verify: same Maven command from Step 1.1.
- [ ] Expected evidence: tests still pass with no unrelated file changes.

### Step 2.1: RED — Notification Preferences And Compliance
- [ ] Add failing tests in `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserNotificationSettingServiceTest.java` for channel decisions, multi-window DND, temporary DND disablement, security bypass, all-channel unsubscribe, and decision log persistence.
- [ ] Verify: `/bin/zsh -lc "JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home mvn -pl jeecg-boot-module/jeecg-module-content -Dtest=ContentUserNotificationSettingServiceTest test"`
- [ ] Expected evidence: tests fail because decision DTO/logging and extended DND behavior are missing.

### Step 2.2: GREEN — Notification Preferences And Compliance
- [ ] Extend `IContentUserNotificationSettingService` with `decideNoticeDelivery(String userId, String noticeType, String requestedChannel, LocalDateTime now)`.
- [ ] Implement `ContentNotificationDecisionDTO` with fields `allowed`, `channels`, `skipReason`, and `securityBypass`.
- [ ] Update `ContentUserNotificationSettingServiceImpl` to parse extended DND JSON, honor temporary disablement, always allow `SECURITY`, and insert `ContentUserNotificationDeliveryLog` for send/skip decisions.
- [ ] Verify: same Maven command from Step 2.1.
- [ ] Expected evidence: notification service tests pass.

### Step 2.3: REFACTOR — Notification Preferences And Compliance
- [ ] Extract small private helpers for `normalizeNoticeType`, `resolveChannels`, `isInAnyDndWindow`, and `writeDecisionLog` inside `ContentUserNotificationSettingServiceImpl`.
- [ ] Verify: same Maven command from Step 2.1.
- [ ] Expected evidence: tests still pass and existing public method `canSendNotice` remains backward-compatible.

### Step 3.1: RED — Privacy Visibility Policy
- [ ] Add failing tests in `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserVisibilityPolicyServiceTest.java` for activity visibility, online status visibility, noindex decision, and privacy cache invalidation.
- [ ] Verify: `/bin/zsh -lc "JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home mvn -pl jeecg-boot-module/jeecg-module-content -Dtest=ContentUserVisibilityPolicyServiceTest test"`
- [ ] Expected evidence: tests fail because new policy methods and fields are missing.

### Step 3.2: GREEN — Privacy Visibility Policy
- [ ] Add fields to `ContentUserPrivacySetting` and methods to `IContentUserVisibilityPolicyService` for `canViewActivity`, `canViewOnlineStatus`, `shouldNoindexProfile`, and cache invalidation after update.
- [ ] Update `ContentUserVisibilityPolicyServiceImpl` to use existing relation checks for `PUBLIC`, `FOLLOWERS_ONLY`, `MUTUAL_ONLY`, and `PRIVATE/HIDDEN`.
- [ ] Verify: same Maven command from Step 3.1.
- [ ] Expected evidence: visibility policy tests pass.

### Step 3.3: REFACTOR — Privacy Visibility Policy
- [ ] Consolidate visibility enum normalization so unknown values fail closed, and keep owner-visible behavior explicit.
- [ ] Verify: same Maven command from Step 3.1.
- [ ] Expected evidence: tests still pass and privacy-deny paths default to hidden.

### Step 4.1: RED — Third-party Authorization Management
- [ ] Add failing tests in `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentThirdPartyAuthorizationServiceTest.java` for list, detail, owner isolation, revoke, token revocation port invocation, idempotent revoked status, and revoked access rejection.
- [ ] Verify: `/bin/zsh -lc "JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home mvn -pl jeecg-boot-module/jeecg-module-content -Dtest=ContentThirdPartyAuthorizationServiceTest test"`
- [ ] Expected evidence: tests fail because service and mapper do not exist.

### Step 4.2: GREEN — Third-party Authorization Management
- [ ] Implement `ContentThirdPartyAuthorization`, mapper methods `selectByUserId`, `selectByUserIdAndId`, service list/detail/revoke methods, and `ContentThirdPartyTokenRevocationPort`.
- [ ] Ensure revoke marks status `REVOKED`, sets revoke metadata, calls the token revocation port, and rejects non-owner access.
- [ ] Verify: same Maven command from Step 4.1.
- [ ] Expected evidence: third-party authorization service tests pass.

### Step 4.3: REFACTOR — Third-party Authorization Management
- [ ] Replace string duplication with private constants or a small status enum in the service implementation, keeping database values stable.
- [ ] Verify: same Maven command from Step 4.1.
- [ ] Expected evidence: tests still pass and mapper names match project conventions.

### Step 5.1: RED — Account Security And Settings APIs
- [ ] Add failing WebMvc tests in `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/controller/ContentUserControllerWebMvcTest.java` for notification settings, privacy settings, authorization list/detail/revoke, and account security entry.
- [ ] Add validation assertions to `ContentUserReqValidationTest` for controller request bodies introduced by this change.
- [ ] Verify: `/bin/zsh -lc "JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home mvn -pl jeecg-boot-module/jeecg-module-content -Dtest=ContentUserControllerWebMvcTest,ContentUserReqValidationTest test"`
- [ ] Expected evidence: tests fail because endpoints or response fields are missing.

### Step 5.2: GREEN — Account Security And Settings APIs
- [ ] Extend `ContentUserSettingsController` with endpoints under existing `/api/v1` style for notification settings, privacy settings, third-party authorization management, and account security settings.
- [ ] Return `Result<T>` only, delegate business logic to services, and reuse existing account/device/password route values inside `ContentAccountSecuritySettingVO`.
- [ ] Verify: same Maven command from Step 5.1.
- [ ] Expected evidence: WebMvc and validation tests pass.

### Step 5.3: REFACTOR — Account Security And Settings APIs
- [ ] Thin controller methods so validation stays in req classes and orchestration stays in services; ensure no raw maps or entity responses are returned.
- [ ] Verify: same Maven command from Step 5.1.
- [ ] Expected evidence: tests still pass and response objects are VOs.

### Step 6.1: RED — Integration Validation
- [ ] Add regression assertions to `ContentSubscriptionNotificationPreferenceServiceTest` and `ContentUserProfileServiceTest` proving existing subscription defaults and profile field visibility still work with EPIC-06 fields.
- [ ] Verify: `/bin/zsh -lc "JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home mvn -pl jeecg-boot-module/jeecg-module-content -Dtest=ContentSubscriptionNotificationPreferenceServiceTest,ContentUserProfileServiceTest test"`
- [ ] Expected evidence: any missing compatibility wiring fails before implementation is adjusted.

### Step 6.2: GREEN — Integration Validation
- [ ] Fix only regressions from Step 6.1; if a content service creates notification records, route its send/skip decision through `decideNoticeDelivery`.
- [ ] Verify: same Maven command from Step 6.1.
- [ ] Expected evidence: regression tests pass without changing unrelated behavior.

### Step 6.3: REFACTOR — Integration Validation
- [ ] Remove duplicate fixture builders in new tests and keep reusable helpers private to their test class.
- [ ] Verify: same Maven command from Step 6.1.
- [ ] Expected evidence: tests still pass and no unrelated modules are edited.

### Step 7.1: RED — Final Verification
- [ ] Run targeted full suite and OpenSpec validation.
- [ ] Verify: `/bin/zsh -lc "JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home mvn -pl jeecg-boot-module/jeecg-module-content -Dtest=ContentUserNotificationSettingServiceTest,ContentUserVisibilityPolicyServiceTest,ContentThirdPartyAuthorizationServiceTest,ContentUserControllerWebMvcTest,ContentUserReqValidationTest,ContentPrivacyNotificationsMigrationTest,ContentSubscriptionNotificationPreferenceServiceTest,ContentUserProfileServiceTest test"`
- [ ] Verify: `openspec validate --change epic-06-privacy-notifications --strict`
- [ ] Expected evidence: failures, if any, are recorded with exact test or validation names.

### Step 7.2: GREEN — Final Verification
- [ ] Resolve only failures observed in Step 7.1, preferring minimal service or test fixture corrections over broad refactors.
- [ ] Verify: rerun both commands from Step 7.1.
- [ ] Expected evidence: targeted Maven tests and OpenSpec validation pass.

### Step 7.3: REFACTOR — Final Verification
- [ ] Run final verification and update graph metadata because code files changed.
- [ ] Verify: rerun both commands from Step 7.1.
- [ ] Verify: `graphify update .`
- [ ] Expected evidence: final tests pass, OpenSpec validation passes, and graphify update completes.

## Self-Review

- Spec coverage: notification preferences, DND, unsubscribe compliance, privacy visibility, search indexing, third-party authorization, and account security entry each map to at least one RED/GREEN/REFACTOR sequence above.
- Placeholder scan: no deferred placeholders are present; all paths and commands are concrete.
- Type consistency: DTO, service, mapper, and VO names match the design file and task list.
