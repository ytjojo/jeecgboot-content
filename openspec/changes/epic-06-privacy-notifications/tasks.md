# Tasks

## Atomic TDD Task List

### 1. Notification Preferences

- [ ] RED 1.1: Create `NotificationPreferenceTest.java` with test for CRUD operations (create preference, read by userId, update channels, delete)
- [ ] GREEN 1.2: Implement `NotificationPreference` entity, `NotificationPreferenceMapper`, `NotificationPreferenceService` to pass RED 1.1
- [ ] RED 1.3: Add test in `NotificationPreferenceServiceTest.java` for channel-specific notification filtering (given a notification type, return enabled channels)
- [ ] GREEN 1.4: Implement channel filtering logic in `NotificationPreferenceService` to pass RED 1.3
- [ ] REFACTOR 1.5: Extract `NotificationChannelEnum` and `NotificationTypeEnum` enums, ensure service uses them instead of string constants

### 2. Do-Not-Disturb

- [ ] RED 2.1: Create `DndConfigServiceTest.java` with test for DND period check (given current time and user DND config, return whether in DND)
- [ ] GREEN 2.2: Implement `DndConfig` entity, `DndConfigMapper`, `DndConfigService` with timezone-aware period calculation to pass RED 2.1
- [ ] RED 2.3: Add test for security notification whitelist bypass in DND (security notification type always returns NOT in DND)
- [ ] GREEN 2.4: Implement whitelist check in `DndConfigService` to pass RED 2.3
- [ ] RED 2.5: Add test for temporary DND disable (1-hour override window)
- [ ] GREEN 2.6: Implement temporary disable logic in `DndConfigService` to pass RED 2.5
- [ ] REFACTOR 2.7: Extract DND period overlap detection into utility method, add test for edge cases (period crossing midnight)

### 3. Visibility Control

- [ ] RED 3.1: Create `VisibilitySettingServiceTest.java` with test for activity visibility check (given userId, viewerId, activityType, return visibility level)
- [ ] GREEN 3.2: Implement `VisibilitySetting` entity, `VisibilitySettingMapper`, `VisibilitySettingService` to pass RED 3.1
- [ ] RED 3.3: Add test for self-visibility always true (user can always see own content regardless of setting)
- [ ] GREEN 3.4: Implement self-check bypass in `VisibilitySettingService` to pass RED 3.3
- [ ] RED 3.5: Add test for Redis cache invalidation on visibility change (set value, change setting, verify cache miss or stale value within 5 min)
- [ ] GREEN 3.6: Implement Redis caching with active invalidation in `VisibilitySettingService` to pass RED 3.5
- [ ] RED 3.7: Add test for online status asymmetric visibility (hidden user appears online to self but offline to others)
- [ ] GREEN 3.8: Implement asymmetric visibility in online status query to pass RED 3.7
- [ ] RED 3.9: Add test for noindex meta tag output based on user search engine indexing setting
- [ ] GREEN 3.10: Implement noindex meta tag and X-Robots-Tag header in user profile page rendering to pass RED 3.9

### 4. Third-Party Auth

- [ ] RED 4.1: Create `ThirdPartyAuthServiceTest.java` with test for listing authorized applications (given userId, return list with name, date, scope)
- [ ] GREEN 4.2: Implement `ThirdPartyAuthService` with authorized application query to pass RED 4.1
- [ ] RED 4.3: Add test for authorization revocation (revoke token, verify subsequent access is denied)
- [ ] GREEN 4.4: Implement token invalidation logic in `ThirdPartyAuthService` to pass RED 4.3
- [ ] REFACTOR 4.5: Add `VisibilityLevelEnum` for visibility levels if not already created, ensure consistent usage across visibility and auth modules

### 5. Notification Dispatch Service (Unsubscribe Enforcement)

- [ ] RED 5.1: Create `NotificationDispatchServiceTest.java` with test for preference-based dispatch check (given notification type + userId, verify channels returned or empty)
- [ ] GREEN 5.2: Implement `NotificationDispatchService` that checks `NotificationPreferenceService` before dispatch to pass RED 5.1
- [ ] RED 5.3: Add test for DND-aware dispatch (notification during DND period is queued, not sent immediately)
- [ ] GREEN 5.4: Integrate `DndConfigService` into `NotificationDispatchService` to pass RED 5.3
- [ ] RED 5.5: Add test for security notification whitelist in dispatch service (security types always dispatched regardless of user settings)
- [ ] GREEN 5.6: Implement whitelist bypass in `NotificationDispatchService` to pass RED 5.5
- [ ] RED 5.7: Add test for all-channels-blocked scenario (no notification sent when all channels disabled)
- [ ] GREEN 5.8: Implement final dispatch gate in `NotificationDispatchService` to pass RED 5.7
- [ ] REFACTOR 5.9: Wire `NotificationDispatchService` into existing notification sending pipeline, verify all paths go through dispatch service

### 6. Privacy Controller & API

- [ ] RED 6.1: Create `PrivacyControllerTest.java` with test for GET/PUT notification preferences endpoint
- [ ] GREEN 6.2: Implement `PrivacyController` notification preference endpoints to pass RED 6.1
- [ ] RED 6.3: Add test for GET/PUT DND config endpoint
- [ ] GREEN 6.4: Implement DND endpoints in `PrivacyController` to pass RED 6.3
- [ ] RED 6.5: Add test for GET/PUT visibility settings endpoint
- [ ] GREEN 6.6: Implement visibility endpoints in `PrivacyController` to pass RED 6.5
- [ ] RED 6.7: Add test for GET authorized applications and POST revoke endpoint
- [ ] GREEN 6.8: Implement third-party auth endpoints in `PrivacyController` to pass RED 6.7
- [ ] REFACTOR 6.9: Add request/response DTOs, parameter validation, and consistent error responses across all endpoints

### 7. Account Security Entry

- [ ] RED 7.1: Create test in `PrivacyControllerTest.java` for account security entry endpoint (returns list of security feature URLs)
- [ ] GREEN 7.2: Implement account security entry endpoint in `PrivacyController` to pass RED 7.1

### 8. Database Migration

- [ ] RED 8.1: Create `V6_1__create_privacy_tables.sql` with DDL for `content_notification_preference`, `content_dnd_config`, `content_visibility_setting`, `content_third_party_auth` tables; write validation query to verify tables exist
- [ ] GREEN 8.2: Execute migration and validate tables created with correct columns, constraints, and indexes
- [ ] RED 8.3: Write rollback SQL (`V6_1__rollback.sql`) that drops tables safely; test rollback in local environment
- [ ] GREEN 8.4: Apply rollback and verify tables are removed cleanly

### 9. Frontend

- [ ] RED 9.1: Create `privacy.ts` API client with typed methods for all privacy endpoints; write mock test for API calls
- [ ] GREEN 9.2: Implement `PrivacyController` API methods in `jeecgboot-vue3/src/api/content/privacy.ts` to pass RED 9.1
- [ ] RED 9.3: Create `NotificationSetting.vue` component test (renders notification type list, toggle switches, channel selectors)
- [ ] GREEN 9.4: Implement `NotificationSetting.vue` to pass RED 9.3
- [ ] RED 9.5: Create `PrivacySetting.vue` component test (renders visibility controls, online status, search engine indexing)
- [ ] GREEN 9.6: Implement `PrivacySetting.vue` to pass RED 9.5
- [ ] RED 9.7: Create `ThirdPartyAuthSetting.vue` component test (renders authorized app list, revoke buttons)
- [ ] GREEN 9.8: Implement `ThirdPartyAuthSetting.vue` to pass RED 9.7
- [ ] RED 9.9: Create `AccountSecurityEntry.vue` component test (renders security feature navigation links)
- [ ] GREEN 9.10: Implement `AccountSecurityEntry.vue` to pass RED 9.9
