# EPIC-06 Privacy Notifications Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement EPIC-06 content community privacy controls, notification preferences, third-party authorization management, and account security settings.

**Architecture:** Extend the existing content user settings boundary instead of creating a new module. Keep notification decisions in `ContentUserNotificationSettingServiceImpl`, privacy decisions in `ContentUserVisibilityPolicyServiceImpl`, third-party authorization in a content-domain service plus token revocation port, and HTTP contracts in `ContentUserSettingsController`.

**Tech Stack:** Spring Boot 3, MyBatis-Plus, Flyway MySQL migrations, Bean Validation, JUnit 5, Mockito, Spring MVC Test, OpenSpec.

---

## Source Artifacts

- Tasks: `openspec/changes/epic-06-privacy-notifications/tasks.md`
- Design: `openspec/changes/epic-06-privacy-notifications/design.md`
- Specs:
  - `openspec/changes/epic-06-privacy-notifications/specs/content-notification-preferences/spec.md`
  - `openspec/changes/epic-06-privacy-notifications/specs/content-privacy-visibility/spec.md`
  - `openspec/changes/epic-06-privacy-notifications/specs/third-party-authorization-management/spec.md`
  - `openspec/changes/epic-06-privacy-notifications/specs/account-security-settings/spec.md`

## File Map

- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/entity/ContentUserNotificationSetting.java`
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/entity/ContentUserPrivacySetting.java`
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/controller/ContentUserSettingsController.java`
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/IContentUserNotificationSettingService.java`
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/IContentUserVisibilityPolicyService.java`
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/impl/ContentUserNotificationSettingServiceImpl.java`
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/impl/ContentUserVisibilityPolicyServiceImpl.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/entity/ContentNotificationAuditLog.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/entity/ContentUserThirdPartyAuth.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/mapper/ContentNotificationAuditLogMapper.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/mapper/ContentUserThirdPartyAuthMapper.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/IContentUserThirdPartyAuthService.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/ContentThirdPartyTokenRevocationPort.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/impl/ContentUserThirdPartyAuthServiceImpl.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/impl/ContentNoopThirdPartyTokenRevocationPort.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/vo/ContentSubscriptionNotificationDecisionVO.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/req/settings/ContentThirdPartyAuthorizationRevokeReq.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/vo/ContentThirdPartyAuthVO.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/vo/ContentThirdPartyAuthorizationDetailVO.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/vo/ContentUserSecuritySettingVO.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/resources/flyway/sql/mysql/V3.9.1_58__content_privacy_notifications.sql`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/resources/flyway/sql/mysql/V3.9.1_58__content_privacy_notifications_rollback.sql`
- Test: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentPrivacyNotificationsMigrationTest.java`
- Test: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserNotificationSettingServiceTest.java`
- Test: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserVisibilityPolicyServiceTest.java`
- Test: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserThirdPartyAuthServiceTest.java`
- Test: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/controller/ContentUserSettingsControllerWebMvcTest.java`
- Test: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/controller/ContentUserThirdPartyAuthControllerWebMvcTest.java`
- Test: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/req/ContentUserReqValidationTest.java`

## Steps

### Step 1.1: Add Flyway Migration And Rollback SQL

**Maps to task:** `1.1 Add Flyway migration and rollback SQL for privacy setting fields, notification delivery decisions, and third-party authorization records.`

- [ ] Read existing content-module Flyway naming and rollback patterns before editing.
- [ ] Create `V3.9.1_58__content_privacy_notifications.sql` with `content_user_privacy_setting` visibility columns, `content_user_notification_delivery_log`, `content_third_party_authorization`, and required indexes.
- [ ] Create `V3.9.1_58__content_privacy_notifications_rollback.sql` that drops the added indexes, tables, and columns in reverse order.
- [ ] Verify migration file contains `browse_history_visibility`, `like_activity_visibility`, `favorite_visibility`, `online_status_visibility`, `content_user_notification_delivery_log`, and `content_third_party_authorization`.
- [ ] Commit point: `feat(content): add privacy notification migration`.

### Step 1.2: Add Domain Entities, Mappers, DTOs, Reqs, And VOs

**Maps to task:** `1.2 Add or extend entities, mappers, DTOs, request objects, and response objects for notification decisions, privacy visibility, third-party authorizations, and account security settings.`

- [ ] Extend `ContentUserPrivacySetting` with new visibility fields and Chinese comments.
- [ ] Create `ContentNotificationAuditLog` and `ContentUserThirdPartyAuth` entities with `@TableName` and project-consistent base fields.
- [ ] Create mappers for notification delivery logs and third-party authorizations.
- [ ] Create `ContentSubscriptionNotificationDecisionVO`, third-party authorization VOs, revoke request, and account security VO.
- [ ] Add validation annotations and Chinese validation messages to request objects.
- [ ] Commit point: `feat(content): add privacy notification contracts`.

### Step 1.3: Add Migration And Mapper Contract Tests

**Maps to task:** `1.3 Add migration and mapper contract tests that verify new tables, fields, indexes, rollback SQL, and basic persistence behavior.`

- [ ] Add `ContentPrivacyNotificationsMigrationTest` assertions for migration file existence, rollback file existence, expected DDL fragments, and reverse DDL fragments.
- [ ] Add mapper-focused tests or existing mapper-contract assertions for insert/select behavior of notification delivery logs and third-party authorizations.
- [ ] Run `/bin/zsh -lc "JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home mvn -pl jeecg-boot-module/jeecg-module-content -Dtest=ContentPrivacyNotificationsMigrationTest test"`.
- [ ] Expected evidence: migration contract tests pass.
- [ ] Commit point: `test(content): cover privacy notification migration contracts`.

### Step 2.1: Extend Notification Settings APIs And Service Contracts

**Maps to task:** `2.1 Extend notification settings APIs and service contracts to list and save supported interaction notification types, enabled state, and delivery channels.`

- [ ] Extend notification setting request/response objects to expose likes, comments, follows, favorites, mentions, direct messages, marketing, and security notification categories.
- [ ] Extend `IContentUserNotificationSettingService` with methods for listing supported notification types and saving per-type channel preferences.
- [ ] Preserve existing `getSetting`, `updateSetting`, and `canSendNotice` behavior for backward compatibility.
- [ ] Add service tests in `ContentUserNotificationSettingServiceTest` for list/save behavior and default values.
- [ ] Commit point: `feat(content): expose notification preference contracts`.

### Step 2.2: Implement Notification Send Decision Logic

**Maps to task:** `2.2 Implement notification send decision logic for disabled types, disabled channels, do-not-disturb windows, temporary DND disablement, and security-notification bypass.`

- [ ] Add `decideNoticeDelivery(String userId, String noticeType, String requestedChannel, LocalDateTime now)` to the notification service contract.
- [ ] Implement disabled type checks, disabled channel checks, all-channel unsubscribe checks, DND window checks, cross-midnight DND handling, and temporary DND disablement.
- [ ] Treat security notifications as mandatory delivery candidates that bypass DND and non-security unsubscribe logic.
- [ ] Add service tests for disabled type, disabled channel, cross-midnight DND, temporary DND disablement, and security bypass.
- [ ] Run `/bin/zsh -lc "JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home mvn -pl jeecg-boot-module/jeecg-module-content -Dtest=ContentUserNotificationSettingServiceTest test"`.
- [ ] Commit point: `feat(content): decide notification delivery`.

### Step 2.3: Persist Notification Delivery Decision Logs

**Maps to task:** `2.3 Persist notification delivery decision logs with enough detail to audit sent and skipped notification outcomes.`

- [ ] Inject `ContentNotificationAuditLogMapper` into `ContentUserNotificationSettingServiceImpl`.
- [ ] Write one decision log for each delivery decision with user id, notice type, requested channel, final channels, allowed flag, skip reason, and security bypass flag.
- [ ] Add tests proving both sent and skipped decisions create audit rows.
- [ ] Run the same `ContentUserNotificationSettingServiceTest` Maven command.
- [ ] Commit point: `feat(content): audit notification delivery decisions`.

### Step 2.4: Add Notification Preference Service Tests

**Maps to task:** `2.4 Add service tests for notification type preferences, channel filtering, DND behavior, security bypass, unsubscribe compliance, and decision logging.`

- [ ] Ensure `ContentUserNotificationSettingServiceTest` covers notification type preferences, channel filtering, DND behavior, security bypass, unsubscribe compliance, and decision logging.
- [ ] Confirm each test name explains the business reason, such as honoring unsubscribe or preserving security alerts.
- [ ] Run `/bin/zsh -lc "JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home mvn -pl jeecg-boot-module/jeecg-module-content -Dtest=ContentUserNotificationSettingServiceTest test"`.
- [ ] Commit point: `test(content): cover notification preference decisions`.

### Step 3.1: Extend Privacy Settings Storage And Update Contracts

**Maps to task:** `3.1 Extend privacy settings storage and update contracts for browsing history, like activity, favorites, online status visibility, and search engine indexing.`

- [ ] Add request and response fields for browsing history, like activity, favorite visibility, online status visibility, and search engine indexing.
- [ ] Extend privacy setting update service behavior so missing new fields use safe defaults and existing persisted values remain compatible.
- [ ] Add validation tests for invalid visibility enum values.
- [ ] Commit point: `feat(content): extend privacy setting contracts`.

### Step 3.2: Extend Visibility Policy Logic

**Maps to task:** `3.2 Extend visibility policy logic so owner, follower, mutual-follow, public, and self-only access rules are applied consistently for profile-sensitive data.`

- [ ] Extend `IContentUserVisibilityPolicyService` with methods for activity and online-status visibility checks.
- [ ] Implement owner-always-visible behavior, public access, followers-only access, mutual-follow access, self-only access, and hidden online status.
- [ ] Reuse existing relation checks in `ContentUserVisibilityPolicyServiceImpl` instead of duplicating relation queries.
- [ ] Add `ContentUserVisibilityPolicyServiceTest` coverage for owner, follower, mutual-follow, public, self-only, and hidden paths.
- [ ] Run `/bin/zsh -lc "JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home mvn -pl jeecg-boot-module/jeecg-module-content -Dtest=ContentUserVisibilityPolicyServiceTest test"`.
- [ ] Commit point: `feat(content): enforce privacy visibility policy`.

### Step 3.3: Add Noindex Decision Support

**Maps to task:** `3.3 Add noindex decision support for profile responses when search engine indexing is disabled.`

- [ ] Add `shouldNoindexProfile(String profileUserId)` or equivalent service method to the visibility policy contract.
- [ ] Return `true` when search engine indexing is disabled for the profile owner and `false` when enabled.
- [ ] Surface the noindex decision in the profile/settings response path chosen by the existing controller design.
- [ ] Add tests for disabled and enabled indexing.
- [ ] Commit point: `feat(content): expose profile noindex decision`.

### Step 3.4: Invalidate Privacy And Profile Visibility Cache

**Maps to task:** `3.4 Invalidate affected privacy/profile visibility cache entries after privacy settings change and ensure cached responses expire within five minutes.`

- [ ] Identify existing cache key conventions used by profile and privacy settings code.
- [ ] Delete affected cache entries immediately after privacy settings update.
- [ ] Ensure any remaining profile/visibility cache for these decisions has a TTL no longer than five minutes.
- [ ] Add tests proving cache invalidation is invoked on privacy update.
- [ ] Commit point: `feat(content): invalidate privacy visibility cache`.

### Step 3.5: Add Privacy Visibility Service Tests

**Maps to task:** `3.5 Add service tests for activity visibility, online status visibility, noindex decisions, owner access, follower access, and cache invalidation.`

- [ ] Consolidate `ContentUserVisibilityPolicyServiceTest` coverage for all privacy visibility scenarios from the spec.
- [ ] Verify tests fail closed for unknown visibility values and preserve owner access.
- [ ] Run `/bin/zsh -lc "JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home mvn -pl jeecg-boot-module/jeecg-module-content -Dtest=ContentUserVisibilityPolicyServiceTest test"`.
- [ ] Commit point: `test(content): cover privacy visibility policy`.

### Step 4.1: Add Third-party Authorization Services

**Maps to task:** `4.1 Add third-party authorization list, detail, and revoke service methods with owner isolation and idempotent revoke behavior.`

- [ ] Create `IContentUserThirdPartyAuthService` with list, detail, revoke, and revoked-access check methods.
- [ ] Implement `ContentUserThirdPartyAuthServiceImpl` using `ContentUserThirdPartyAuthMapper`.
- [ ] Ensure list/detail queries are scoped by current user id and revoke is idempotent for already revoked records.
- [ ] Add `ContentUserThirdPartyAuthServiceTest` for list, detail, owner isolation, revoke, and idempotent revoke.
- [ ] Run `/bin/zsh -lc "JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home mvn -pl jeecg-boot-module/jeecg-module-content -Dtest=ContentUserThirdPartyAuthServiceTest test"`.
- [ ] Commit point: `feat(content): manage third-party authorizations`.

### Step 4.2: Wire Token Revocation Port

**Maps to task:** `4.2 Add token revocation port wiring so revoked authorizations make related access tokens unusable immediately or explicitly mark them pending revocation through the adapter.`

- [ ] Create `ContentThirdPartyTokenRevocationPort` with a revoke method accepting authorization token identifiers.
- [ ] Create `ContentNoopThirdPartyTokenRevocationPort` that records a pending-revocation outcome until the real auth-module adapter is connected.
- [ ] Call the token revocation port from authorization revoke flow after status changes are persisted.
- [ ] Add tests proving the port is invoked and revoked authorization access is rejected.
- [ ] Commit point: `feat(content): wire third-party token revocation`.

### Step 4.3: Add Third-party Authorization API Contracts

**Maps to task:** `4.3 Add API contracts for listing authorizations, viewing scope details, revoking active authorizations, and returning an empty list when none exist.`

- [ ] Add controller endpoints in `ContentUserSettingsController` for authorization list, detail, and revoke.
- [ ] Return authorization VOs rather than entities.
- [ ] Ensure empty authorization lists return an empty array inside `Result<T>`.
- [ ] Add WebMvc tests for list, detail, revoke, and empty list responses.
- [ ] Commit point: `feat(content): expose authorization settings APIs`.

### Step 4.4: Add Third-party Authorization Tests

**Maps to task:** `4.4 Add service and controller tests for list, detail, owner isolation, revoke, token revocation invocation, revoked access rejection, and reauthorization requirement.`

- [ ] Complete `ContentUserThirdPartyAuthServiceTest` coverage for revoked access rejection and reauthorization requirement.
- [ ] Complete `ContentUserSettingsControllerWebMvcTest` coverage for HTTP response shapes and owner isolation.
- [ ] Run `/bin/zsh -lc "JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home mvn -pl jeecg-boot-module/jeecg-module-content -Dtest=ContentUserThirdPartyAuthServiceTest,ContentUserSettingsControllerWebMvcTest test"`.
- [ ] Commit point: `test(content): cover third-party authorization management`.

### Step 5.1: Add Account Security Settings Response Model

**Maps to task:** `5.1 Add account security settings response model that exposes device management, password change, two-step verification, and login reminder entries.`

- [ ] Implement `ContentUserSecuritySettingVO` with entries for device management, password change, two-step verification, and login reminders.
- [ ] Include route keys or route paths matching the existing device management and password change flows.
- [ ] Add WebMvc expectations for all four entries.
- [ ] Commit point: `feat(content): add account security settings model`.

### Step 5.2: Wire Existing Device And Password Flows

**Maps to task:** `5.2 Wire account security settings to existing device management and password change flows without reimplementing those flows in this change.`

- [ ] Locate existing device management and password change controller route constants or documented paths.
- [ ] Return those existing route targets from the account security settings endpoint.
- [ ] Avoid duplicating password modification, old-password verification, or device-session business logic in content settings code.
- [ ] Add tests proving the account security endpoint points to the existing flows.
- [ ] Commit point: `feat(content): link account security existing flows`.

### Step 5.3: Implement Login Reminder Preference

**Maps to task:** `5.3 Implement login reminder preference save/load behavior and ensure disabled non-mandatory login reminders are skipped for later new-device events.`

- [ ] Add login reminder preference to the appropriate notification or security setting contract.
- [ ] Persist the preference through the settings service.
- [ ] Ensure non-mandatory new-device reminders are skipped when disabled while mandatory security alerts remain deliverable.
- [ ] Add service tests for enable, disable, and mandatory-security behavior.
- [ ] Commit point: `feat(content): manage login reminder preference`.

### Step 5.4: Add Account Security Controller And Validation Tests

**Maps to task:** `5.4 Add controller and validation tests for the account security entry response and login reminder preference updates.`

- [ ] Add `ContentUserSettingsControllerWebMvcTest` coverage for the account security settings endpoint.
- [ ] Add `ContentUserReqValidationTest` coverage for login reminder update request validation.
- [ ] Run `/bin/zsh -lc "JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home mvn -pl jeecg-boot-module/jeecg-module-content -Dtest=ContentUserSettingsControllerWebMvcTest,ContentUserReqValidationTest test"`.
- [ ] Commit point: `test(content): cover account security settings`.

### Step 6.1: Extend Settings Controller Endpoints

**Maps to task:** `6.1 Extend ContentUserSettingsController with notification preference, privacy setting, third-party authorization, and account security endpoints returning Result<T>.`

- [ ] Add or extend controller endpoints for notification preferences, privacy settings, third-party authorizations, and account security settings.
- [ ] Keep controller methods thin: validate request, resolve current user, delegate to services, return `Result<T>`.
- [ ] Do not return entities or raw maps from these endpoints.
- [ ] Add WebMvc tests for success responses of each endpoint group.
- [ ] Commit point: `feat(content): integrate settings controller endpoints`.

### Step 6.2: Add Request Validation

**Maps to task:** `6.2 Add request validation for notification channels, DND time ranges, temporary disable duration, privacy visibility enums, and third-party revoke input.`

- [ ] Add Bean Validation annotations or custom validators for notification channels and DND time ranges.
- [ ] Validate temporary DND disable duration bounds.
- [ ] Validate privacy visibility enum inputs and third-party revoke request input.
- [ ] Ensure validation messages are Chinese and match existing project style.
- [ ] Run `/bin/zsh -lc "JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home mvn -pl jeecg-boot-module/jeecg-module-content -Dtest=ContentUserReqValidationTest test"`.
- [ ] Commit point: `feat(content): validate privacy notification requests`.

### Step 6.3: Add Settings WebMvc Tests

**Maps to task:** `6.3 Add WebMvc tests covering HTTP contracts, validation messages, owner isolation, and response shapes for all new settings endpoints.`

- [ ] Complete `ContentUserSettingsControllerWebMvcTest` cases for notification preferences, privacy settings, third-party authorizations, account security, validation errors, and owner isolation.
- [ ] Confirm all responses use `Result<T>` and expected VO fields.
- [ ] Run `/bin/zsh -lc "JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home mvn -pl jeecg-boot-module/jeecg-module-content -Dtest=ContentUserSettingsControllerWebMvcTest test"`.
- [ ] Commit point: `test(content): cover settings controller contracts`.

### Step 7.1: Add Regression Tests

**Maps to task:** `7.1 Add focused regression tests to prove existing subscription notification defaults and profile visibility behavior still work with the new settings.`

- [ ] Add regression coverage to `ContentSubscriptionNotificationPreferenceServiceTest` for existing subscription notification defaults.
- [ ] Add regression coverage to `ContentUserProfileServiceTest` or existing visibility tests for pre-existing profile visibility behavior.
- [ ] Run `/bin/zsh -lc "JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home mvn -pl jeecg-boot-module/jeecg-module-content -Dtest=ContentSubscriptionNotificationPreferenceServiceTest,ContentUserProfileServiceTest test"`.
- [ ] Commit point: `test(content): preserve existing notification and profile behavior`.

### Step 7.2: Run Targeted Maven Tests

**Maps to task:** `7.2 Run targeted Maven tests for notification settings, visibility policy, third-party authorization, settings controller, request validation, and migration coverage.`

- [ ] Run `/bin/zsh -lc "JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home mvn -pl jeecg-boot-module/jeecg-module-content -Dtest=ContentUserNotificationSettingServiceTest,ContentUserVisibilityPolicyServiceTest,ContentUserThirdPartyAuthServiceTest,ContentUserSettingsControllerWebMvcTest,ContentUserReqValidationTest,ContentPrivacyNotificationsMigrationTest,ContentSubscriptionNotificationPreferenceServiceTest,ContentUserProfileServiceTest test"`.
- [ ] Fix only failures directly caused by EPIC-06 changes.
- [ ] Rerun the same targeted Maven command until it passes.
- [ ] Commit point: `test(content): pass epic 06 targeted tests`.

### Step 7.3: Run OpenSpec Validation

**Maps to task:** `7.3 Run openspec validate epic-06-privacy-notifications --type change --strict --no-interactive and fix any validation failures within this change.`

- [ ] Run `openspec validate epic-06-privacy-notifications --type change --strict --no-interactive`.
- [ ] Fix only validation failures inside `openspec/changes/epic-06-privacy-notifications`.
- [ ] Rerun validation until it prints `Change 'epic-06-privacy-notifications' is valid`.
- [ ] Commit point: `docs(openspec): validate epic 06 change`.

### Step 7.4: Refresh Graphify Metadata

**Maps to task:** `7.4 After implementation changes, run graphify update . to refresh the project knowledge graph.`

- [ ] After code changes are complete, run `graphify update .`.
- [ ] If graphify changes metadata files, review the diff and keep only metadata generated by this implementation pass.
- [ ] Run `git status --short` and confirm no unrelated user changes were reverted.
- [ ] Commit point: `chore(graphify): refresh graph after epic 06`.

## Coverage Check

- Notification preferences: Steps 2.1 through 2.4 and 6.1 through 6.3.
- Do-not-disturb and unsubscribe compliance: Steps 2.2 through 2.4.
- Privacy visibility and search indexing: Steps 3.1 through 3.5.
- Third-party authorization management: Steps 4.1 through 4.4.
- Account security settings: Steps 5.1 through 5.4.
- Regression and final validation: Steps 7.1 through 7.4.

## Execution Notes

- Execute tasks in numeric order because database contracts unlock services, services unlock controller contracts, and controller contracts unlock final validation.
- Keep comments in Java code in Chinese to match repository rules.
- Keep changes scoped to the content module and this OpenSpec change.
- Do not run `graphify update .` until implementation changes have touched code files.
