## 1. Database And Domain Contracts

- [ ] 1.1 Add Flyway migration and rollback SQL for privacy setting fields, notification delivery decisions, and third-party authorization records.
- [ ] 1.2 Add or extend entities, mappers, DTOs, request objects, and response objects for notification decisions, privacy visibility, third-party authorizations, and account security settings.
- [ ] 1.3 Add migration and mapper contract tests that verify new tables, fields, indexes, rollback SQL, and basic persistence behavior.

## 2. Notification Preferences

- [ ] 2.1 Extend notification settings APIs and service contracts to list and save supported interaction notification types, enabled state, and delivery channels.
- [ ] 2.2 Implement notification send decision logic for disabled types, disabled channels, do-not-disturb windows, temporary DND disablement, and security-notification bypass.
- [ ] 2.3 Persist notification delivery decision logs with enough detail to audit sent and skipped notification outcomes.
- [ ] 2.4 Add service tests for notification type preferences, channel filtering, DND behavior, security bypass, unsubscribe compliance, and decision logging.

## 3. Privacy Visibility

- [ ] 3.1 Extend privacy settings storage and update contracts for browsing history, like activity, favorites, online status visibility, and search engine indexing.
- [ ] 3.2 Extend visibility policy logic so owner, follower, mutual-follow, public, and self-only access rules are applied consistently for profile-sensitive data.
- [ ] 3.3 Add noindex decision support for profile responses when search engine indexing is disabled.
- [ ] 3.4 Invalidate affected privacy/profile visibility cache entries after privacy settings change and ensure cached responses expire within five minutes.
- [ ] 3.5 Add service tests for activity visibility, online status visibility, noindex decisions, owner access, follower access, and cache invalidation.

## 4. Third-party Authorization Management

- [ ] 4.1 Add third-party authorization list, detail, and revoke service methods with owner isolation and idempotent revoke behavior.
- [ ] 4.2 Add token revocation port wiring so revoked authorizations make related access tokens unusable immediately or explicitly mark them pending revocation through the adapter.
- [ ] 4.3 Add API contracts for listing authorizations, viewing scope details, revoking active authorizations, and returning an empty list when none exist.
- [ ] 4.4 Add service and controller tests for list, detail, owner isolation, revoke, token revocation invocation, revoked access rejection, and reauthorization requirement.

## 5. Account Security Settings

- [ ] 5.1 Add account security settings response model that exposes device management, password change, two-step verification, and login reminder entries.
- [ ] 5.2 Wire account security settings to existing device management and password change flows without reimplementing those flows in this change.
- [ ] 5.3 Implement login reminder preference save/load behavior and ensure disabled non-mandatory login reminders are skipped for later new-device events.
- [ ] 5.4 Add controller and validation tests for the account security entry response and login reminder preference updates.

## 6. Settings Controller Integration

- [ ] 6.1 Extend `ContentUserSettingsController` with notification preference, privacy setting, third-party authorization, and account security endpoints returning `Result<T>`.
- [ ] 6.2 Add request validation for notification channels, DND time ranges, temporary disable duration, privacy visibility enums, and third-party revoke input.
- [ ] 6.3 Add WebMvc tests covering HTTP contracts, validation messages, owner isolation, and response shapes for all new settings endpoints.

## 7. Regression And Final Validation

- [ ] 7.1 Add focused regression tests to prove existing subscription notification defaults and profile visibility behavior still work with the new settings.
- [ ] 7.2 Run targeted Maven tests for notification settings, visibility policy, third-party authorization, settings controller, request validation, and migration coverage.
- [ ] 7.3 Run `openspec validate epic-06-privacy-notifications --type change --strict --no-interactive` and fix any validation failures within this change.
- [ ] 7.4 After implementation changes, run `graphify update .` to refresh the project knowledge graph.
