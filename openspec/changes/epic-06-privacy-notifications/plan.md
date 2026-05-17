# Execution Plan

## Steps

### Step 1: RED 1.1 — Notification Preference CRUD test
- Test file: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/privacy/service/NotificationPreferenceServiceTest.java`
- Assertion: Create preference for userId, read back, update channels, verify update, delete and confirm not found
- Expected failure: Classes not yet defined
- Verify: `cd jeecg-boot/jeecg-boot-module/jeecg-module-content && mvn test -Dtest=NotificationPreferenceServiceTest` — tests fail with ClassNotFoundException

### Step 2: GREEN 1.2 — Notification Preference CRUD implementation
- Pass test from: Step 1
- Minimal code: Create `NotificationPreference` entity (id, userId, notificationType, channels JSON, enabled, createTime, updateTime), `NotificationPreferenceMapper`, `NotificationPreferenceService` with CRUD methods
- Verify: `cd jeecg-boot/jeecg-boot-module/jeecg-module-content && mvn test -Dtest=NotificationPreferenceServiceTest` — all tests pass

### Step 3: RED 1.3 — Channel-specific filtering test
- Test file: `NotificationPreferenceServiceTest.java` (append)
- Assertion: Given a notification type and userId, service returns list of enabled channels; disabled type returns empty list
- Expected failure: `getEnabledChannels(userId, type)` method not implemented
- Verify: `mvn test -Dtest=NotificationPreferenceServiceTest` — compilation error or assertion failure

### Step 4: GREEN 1.4 — Channel filtering implementation
- Pass test from: Step 3
- Minimal code: Add `getEnabledChannels(userId, type)` method to `NotificationPreferenceService` that queries DB and parses channels JSON
- Verify: `mvn test -Dtest=NotificationPreferenceServiceTest` — channel filtering tests pass

### Step 5: REFACTOR 1.5 — Extract enums
- Create `NotificationChannelEnum` (APP=1, SMS=2, EMAIL=3) and `NotificationTypeEnum` (LIKE, COMMENT, FOLLOW, FAVORITE, MENTION, MESSAGE) in `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/privacy/enums/`
- Verify: `mvn test -Dtest=NotificationPreferenceServiceTest` — all tests still pass, no string constants in service

### Step 6: RED 2.1 — DND period check test
- Test file: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/privacy/service/DndConfigServiceTest.java`
- Assertion: Given current time within DND period → isInDnd returns true; outside period → false
- Expected failure: `DndConfigService` class not found
- Verify: `mvn test -Dtest=DndConfigServiceTest` — tests fail with ClassNotFoundException

### Step 7: GREEN 2.2 — DND period check implementation
- Pass test from: Step 6
- Minimal code: Create `DndConfig` entity, `DndConfigMapper`, `DndConfigService` with `isInDnd(userId, currentTime)` method using timezone-aware comparison
- Verify: `mvn test -Dtest=DndConfigServiceTest` — DND period tests pass

### Step 8: RED 2.3 — Security whitelist bypass test
- Test file: `DndConfigServiceTest.java` (append)
- Assertion: Security notification type returns isInDnd=false even during DND period
- Expected failure: Whitelist check not implemented
- Verify: `mvn test -Dtest=DndConfigServiceTest` — whitelist test fails

### Step 9: GREEN 2.4 — Security whitelist implementation
- Pass test from: Step 8
- Minimal code: Add `SECURITY_NOTIFICATION_TYPES` constant set; `isInDnd` returns false for security types
- Verify: `mvn test -Dtest=DndConfigServiceTest` — whitelist tests pass

### Step 10: RED 2.5 — Temporary DND disable test
- Test file: `DndConfigServiceTest.java` (append)
- Assertion: After calling `disableTemporarily(userId, 3600)`, `isInDnd` returns false for 1 hour
- Expected failure: `disableTemporarily` method not found
- Verify: `mvn test -Dtest=DndConfigServiceTest` — test fails

### Step 11: GREEN 2.6 — Temporary DND disable implementation
- Pass test from: Step 10
- Minimal code: Add `tempDisableUntil` timestamp field to config; `isInDnd` checks if current time < tempDisableUntil
- Verify: `mvn test -Dtest=DndConfigServiceTest` — temporary disable tests pass

### Step 12: REFACTOR 2.7 — Extract DND utility method
- Extract period overlap detection into utility method; add edge case tests for midnight-crossing periods
- Verify: `mvn test -Dtest=DndConfigServiceTest` — all tests pass, utility method tested

### Step 13: RED 3.1 — Activity visibility check test
- Test file: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/privacy/service/VisibilitySettingServiceTest.java`
- Assertion: Given userId/viewerId/activityType, returns correct visibility level (public/followers-only/private)
- Expected failure: `VisibilitySettingService` class not found
- Verify: `mvn test -Dtest=VisibilitySettingServiceTest` — tests fail

### Step 14: GREEN 3.2 — Visibility setting implementation
- Pass test from: Step 13
- Minimal code: Create `VisibilitySetting` entity, `VisibilitySettingMapper`, `VisibilitySettingService` with `getVisibilityLevel(userId, viewerId, activityType)` method
- Verify: `mvn test -Dtest=VisibilitySettingServiceTest` — visibility tests pass

### Step 15: RED 3.3 — Self-visibility always true test
- Test file: `VisibilitySettingServiceTest.java` (append)
- Assertion: When viewerId == ownerUserId, visibility always returns true regardless of setting
- Expected failure: Self-check not implemented
- Verify: `mvn test -Dtest=VisibilitySettingServiceTest` — self-visibility test fails

### Step 16: GREEN 3.4 — Self-visibility implementation
- Pass test from: Step 15
- Minimal code: Add `if (viewerId.equals(ownerUserId)) return true` at start of `isContentVisible`
- Verify: `mvn test -Dtest=VisibilitySettingServiceTest` — self-visibility tests pass

### Step 17: RED 3.5 — Redis cache invalidation test
- Test file: `VisibilitySettingServiceTest.java` (append)
- Assertion: Set cache value, change visibility setting, verify cache key deleted or stale within 5 min
- Expected failure: Cache invalidation not implemented
- Verify: `mvn test -Dtest=VisibilitySettingServiceTest` — cache test fails

### Step 18: GREEN 3.6 — Redis caching implementation
- Pass test from: Step 17
- Minimal code: Add Redis cache layer with 5-min TTL; on setting change, delete corresponding cache key
- Verify: `mvn test -Dtest=VisibilitySettingServiceTest` — cache tests pass

### Step 19: RED 3.7 — Online status asymmetric visibility test
- Test file: `VisibilitySettingServiceTest.java` (append)
- Assertion: Hidden user's online status returns true to self, false to others
- Expected failure: Asymmetric logic not implemented
- Verify: `mvn test -Dtest=VisibilitySettingServiceTest` — asymmetric test fails

### Step 20: GREEN 3.8 — Online status asymmetric implementation
- Pass test from: Step 19
- Minimal code: Modify online status query: if viewer == owner return actual status; if hidden return false; else check mutual follow
- Verify: `mvn test -Dtest=VisibilitySettingServiceTest` — asymmetric visibility tests pass

### Step 21: RED 3.9 — Noindex meta tag test
- Test file: `VisibilitySettingServiceTest.java` (append)
- Assertion: User with searchIndex=false → service returns shouldOutputNoindex=true
- Expected failure: Noindex check not implemented
- Verify: `mvn test -Dtest=VisibilitySettingServiceTest` — noindex test fails

### Step 22: GREEN 3.10 — Noindex implementation
- Pass test from: Step 21
- Minimal code: Add `shouldOutputNoindex(userId)` method returning true when searchIndex setting is disabled
- Verify: `mvn test -Dtest=VisibilitySettingServiceTest` — noindex tests pass

### Step 23: RED 4.1 — List authorized applications test
- Test file: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/privacy/service/ThirdPartyAuthServiceTest.java`
- Assertion: Given userId, returns list of authorized apps with name, date, scope
- Expected failure: `ThirdPartyAuthService` class not found
- Verify: `mvn test -Dtest=ThirdPartyAuthServiceTest` — tests fail

### Step 24: GREEN 4.2 — Authorized applications query implementation
- Pass test from: Step 23
- Minimal code: Implement `ThirdPartyAuthService.getAuthorizedApps(userId)` querying OAuth authorization table
- Verify: `mvn test -Dtest=ThirdPartyAuthServiceTest` — list tests pass

### Step 25: RED 4.3 — Authorization revocation test
- Test file: `ThirdPartyAuthServiceTest.java` (append)
- Assertion: After revoke(accessToken), subsequent access attempt is denied
- Expected failure: Revoke method not found
- Verify: `mvn test -Dtest=ThirdPartyAuthServiceTest` — revocation test fails

### Step 26: GREEN 4.4 — Token invalidation implementation
- Pass test from: Step 25
- Minimal code: Implement `revoke(accessToken)` marking token as revoked in DB; add `isValid(accessToken)` check
- Verify: `mvn test -Dtest=ThirdPartyAuthServiceTest` — revocation tests pass

### Step 27: REFACTOR 4.5 — Consistent enum usage
- Ensure `VisibilityLevelEnum` exists and is used consistently; verify no string constants for visibility levels
- Verify: `mvn test` — all privacy module tests pass

### Step 28: RED 5.1 — Preference-based dispatch check test
- Test file: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/privacy/service/NotificationDispatchServiceTest.java`
- Assertion: Given notification type + userId, dispatch service returns allowed channels or empty
- Expected failure: `NotificationDispatchService` class not found
- Verify: `mvn test -Dtest=NotificationDispatchServiceTest` — tests fail

### Step 29: GREEN 5.2 — Preference-based dispatch implementation
- Pass test from: Step 28
- Minimal code: Implement `NotificationDispatchService.shouldSend(userId, type)` delegating to `NotificationPreferenceService`
- Verify: `mvn test -Dtest=NotificationDispatchServiceTest` — dispatch tests pass

### Step 30: RED 5.3 — DND-aware dispatch test
- Test file: `NotificationDispatchServiceTest.java` (append)
- Assertion: During DND period, non-urgent notification is queued (not sent immediately)
- Expected failure: DND integration not implemented
- Verify: `mvn test -Dtest=NotificationDispatchServiceTest` — DND test fails

### Step 31: GREEN 5.4 — DND integration implementation
- Pass test from: Step 30
- Minimal code: Inject `DndConfigService` into dispatch service; if `isInDnd`, queue notification instead of sending
- Verify: `mvn test -Dtest=NotificationDispatchServiceTest` — DND dispatch tests pass

### Step 32: RED 5.5 — Security whitelist dispatch test
- Test file: `NotificationDispatchServiceTest.java` (append)
- Assertion: Security notification type always dispatched regardless of user preferences
- Expected failure: Whiteline bypass not implemented
- Verify: `mvn test -Dtest=NotificationDispatchServiceTest` — whitelist test fails

### Step 33: GREEN 5.6 — Whitelist bypass implementation
- Pass test from: Step 32
- Minimal code: Check notification type against security whitelist before preference check; if in whitelist, skip preference/DND checks
- Verify: `mvn test -Dtest=NotificationDispatchServiceTest` — whitelist tests pass

### Step 34: RED 5.7 — All channels blocked test
- Test file: `NotificationDispatchServiceTest.java` (append)
- Assertion: When all channels disabled for a notification type, no notification is sent
- Expected failure: Final dispatch gate not implemented
- Verify: `mvn test -Dtest=NotificationDispatchServiceTest` — all-blocked test fails

### Step 35: GREEN 5.8 — Final dispatch gate implementation
- Pass test from: Step 34
- Minimal code: After preference + DND + whitelist checks, if allowed channels is empty, do not send
- Verify: `mvn test -Dtest=NotificationDispatchServiceTest` — all tests pass

### Step 36: REFACTOR 5.9 — Wire dispatch service into existing pipeline
- Integrate `NotificationDispatchService` into existing notification sending code; verify all notification paths go through it
- Verify: `mvn test` — all module tests pass; grep confirms no direct send calls bypassing dispatch service

### Step 37: RED 6.1 — Notification preferences API test
- Test file: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/privacy/controller/PrivacyControllerTest.java`
- Assertion: GET `/privacy/notifications` returns preferences; PUT updates and returns 200
- Expected failure: `PrivacyController` endpoints not found
- Verify: `mvn test -Dtest=PrivacyControllerTest` — tests fail

### Step 38: GREEN 6.2 — Notification preferences API implementation
- Pass test from: Step 37
- Minimal code: Add `PrivacyController` with GET/PUT `/privacy/notifications` endpoints
- Verify: `mvn test -Dtest=PrivacyControllerTest` — preference API tests pass

### Step 39: RED 6.3 — DND config API test
- Test file: `PrivacyControllerTest.java` (append)
- Assertion: GET `/privacy/dnd` returns DND config; PUT updates and returns 200
- Expected failure: DND endpoints not found
- Verify: `mvn test -Dtest=PrivacyControllerTest` — DND API tests fail

### Step 40: GREEN 6.4 — DND config API implementation
- Pass test from: Step 39
- Minimal code: Add GET/PUT `/privacy/dnd` endpoints to `PrivacyController`
- Verify: `mvn test -Dtest=PrivacyControllerTest` — DND API tests pass

### Step 41: RED 6.5 — Visibility settings API test
- Test file: `PrivacyControllerTest.java` (append)
- Assertion: GET `/privacy/visibility` returns visibility settings; PUT updates and returns 200
- Expected failure: Visibility endpoints not found
- Verify: `mvn test -Dtest=PrivacyControllerTest` — visibility API tests fail

### Step 42: GREEN 6.6 — Visibility settings API implementation
- Pass test from: Step 41
- Minimal code: Add GET/PUT `/privacy/visibility` endpoints to `PrivacyController`
- Verify: `mvn test -Dtest=PrivacyControllerTest` — visibility API tests pass

### Step 43: RED 6.7 — Third-party auth API test
- Test file: `PrivacyControllerTest.java` (append)
- Assertion: GET `/privacy/third-party-auth` returns authorized apps; POST `/privacy/third-party-auth/revoke` revokes and returns 200
- Expected failure: Auth endpoints not found
- Verify: `mvn test -Dtest=PrivacyControllerTest` — auth API tests fail

### Step 44: GREEN 6.8 — Third-party auth API implementation
- Pass test from: Step 43
- Minimal code: Add GET `/privacy/third-party-auth` and POST `/privacy/third-party-auth/revoke` endpoints
- Verify: `mvn test -Dtest=PrivacyControllerTest` — auth API tests pass

### Step 45: REFACTOR 6.9 — Add DTOs and validation
- Create request/response DTOs for all endpoints; add `@Valid` parameter validation; standardize error responses
- Verify: `mvn test -Dtest=PrivacyControllerTest` — all tests pass; validation errors return 400

### Step 46: RED 7.1 — Account security entry API test
- Test file: `PrivacyControllerTest.java` (append)
- Assertion: GET `/privacy/account-security` returns list of security feature URLs
- Expected failure: Endpoint not found
- Verify: `mvn test -Dtest=PrivacyControllerTest` — security entry test fails

### Step 47: GREEN 7.2 — Account security entry API implementation
- Pass test from: Step 46
- Minimal code: Add GET `/privacy/account-security` endpoint returning static feature URLs
- Verify: `mvn test -Dtest=PrivacyControllerTest` — security entry tests pass

### Step 48: RED 8.1 — Database migration DDL
- File: `jeecg-boot/db/migration/V6_1__create_privacy_tables.sql`
- DDL for `content_notification_preference`, `content_dnd_config`, `content_visibility_setting`, `content_third_party_auth` with indexes and constraints
- Validation query: `SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name IN (...)` returns 4
- Verify: Manual DB query confirms 4 tables created

### Step 49: GREEN 8.2 — Execute migration
- Run migration via Flyway or manual SQL execution
- Verify: `SHOW TABLES LIKE 'content_%'` — all 4 tables present with correct columns

### Step 50: RED 8.3 — Rollback SQL
- File: `jeecg-boot/db/migration/V6_1__rollback.sql`
- DROP TABLE IF EXISTS for all 4 tables
- Verify: Execute rollback, `SHOW TABLES LIKE 'content_%'` — 4 tables removed

### Step 51: GREEN 8.4 — Re-apply migration
- Re-run `V6_1__create_privacy_tables.sql` to restore tables
- Verify: Tables recreated successfully; application starts without error

### Step 52: RED 9.1 — API client mock test
- File: `jeecgboot-vue3/src/api/content/__tests__/privacy.test.ts`
- Assertion: API client methods (getNotificationPrefs, updateNotificationPrefs, getDndConfig, etc.) return correct mock responses
- Expected failure: API client module not found
- Verify: `cd jeecgboot-vue3 && pnpm test src/api/content/__tests__/privacy.test.ts` — tests fail

### Step 53: GREEN 9.2 — API client implementation
- Pass test from: Step 52
- Minimal code: Implement `jeecgboot-vue3/src/api/content/privacy.ts` with typed methods for all endpoints
- Verify: `pnpm test src/api/content/__tests__/privacy.test.ts` — API client tests pass

### Step 54: RED 9.3 — NotificationSetting.vue component test
- File: `jeecgboot-vue3/src/views/content/privacy/__tests__/NotificationSetting.test.ts`
- Assertion: Component renders notification type list with toggle switches and channel selectors
- Expected failure: Component not found
- Verify: `pnpm vitest run src/views/content/privacy/__tests__/NotificationSetting.test.ts` — test fails

### Step 55: GREEN 9.4 — NotificationSetting.vue implementation
- Pass test from: Step 54
- Minimal code: Create `jeecgboot-vue3/src/views/content/privacy/NotificationSetting.vue` with toggle UI bound to API client
- Verify: `pnpm vitest run` — component tests pass

### Step 56: RED 9.5 — PrivacySetting.vue component test
- File: `jeecgboot-vue3/src/views/content/privacy/__tests__/PrivacySetting.test.ts`
- Assertion: Component renders visibility controls, online status selector, search engine indexing toggle
- Expected failure: Component not found
- Verify: `pnpm vitest run` — test fails

### Step 57: GREEN 9.6 — PrivacySetting.vue implementation
- Pass test from: Step 56
- Minimal code: Create `jeecgboot-vue3/src/views/content/privacy/PrivacySetting.vue` with visibility + online status + search index controls
- Verify: `pnpm vitest run` — component tests pass

### Step 58: RED 9.7 — ThirdPartyAuthSetting.vue component test
- File: `jeecgboot-vue3/src/views/content/privacy/__tests__/ThirdPartyAuthSetting.test.ts`
- Assertion: Component renders authorized app list with revoke buttons
- Expected failure: Component not found
- Verify: `pnpm vitest run` — test fails

### Step 59: GREEN 9.8 — ThirdPartyAuthSetting.vue implementation
- Pass test from: Step 58
- Minimal code: Create `jeecgboot-vue3/src/views/content/privacy/ThirdPartyAuthSetting.vue` with app list and revoke flow
- Verify: `pnpm vitest run` — component tests pass

### Step 60: RED 9.9 — AccountSecurityEntry.vue component test
- File: `jeecgboot-vue3/src/views/content/privacy/__tests__/AccountSecurityEntry.test.ts`
- Assertion: Component renders security feature navigation links (device management, password, 2FA, login alert)
- Expected failure: Component not found
- Verify: `pnpm vitest run` — test fails

### Step 61: GREEN 9.10 — AccountSecurityEntry.vue implementation
- Pass test from: Step 60
- Minimal code: Create `jeecgboot-vue3/src/views/content/privacy/AccountSecurityEntry.vue` with navigation links
- Verify: `pnpm vitest run` — component tests pass

---

## Execution Mode Selection
REQUIRED: Use superpowers:subagent-driven-development skill.
DO NOT use executing-plans or inline execution.
