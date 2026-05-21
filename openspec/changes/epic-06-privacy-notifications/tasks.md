## 1. Database And DTO Contracts

- [ ] 1.1 RED: Add migration and DTO contract tests for privacy fields, notification delivery log, third-party authorization tables, and rollback SQL.
- [ ] 1.2 GREEN: Implement Flyway SQL, rollback SQL, entities, mappers, DTOs, req and vo classes required by 1.1.
- [ ] 1.3 REFACTOR: Align names, indexes, schema comments, and Chinese code comments for the database and DTO contracts from 1.2.

## 2. Notification Preferences And Compliance

- [ ] 2.1 RED: Add service tests for notification channel decisions, multi-window do-not-disturb, temporary DND disablement, security bypass, and delivery audit logging.
- [ ] 2.2 GREEN: Extend notification setting service, decision DTO, DND parsing, safety bypass, and delivery log persistence to satisfy 2.1.
- [ ] 2.3 REFACTOR: Simplify notification decision helpers and keep JSON default handling backward-compatible after 2.2.

## 3. Privacy Visibility Policy

- [ ] 3.1 RED: Add visibility policy tests for activity visibility, online status visibility, search indexing noindex decision, and cache invalidation.
- [ ] 3.2 GREEN: Extend privacy setting entity, update requests, visibility service, cache invalidation, and profile/search response support to satisfy 3.1.
- [ ] 3.3 REFACTOR: Consolidate visibility enum conversion and owner/follower/mutual-follow checks introduced by 3.2.

## 4. Third-party Authorization Management

- [ ] 4.1 RED: Add authorization service tests for list, detail, owner isolation, revoke, token revocation port invocation, and revoked access rejection.
- [ ] 4.2 GREEN: Implement third-party authorization entity, mapper, service, token revocation port, and revoke behavior to satisfy 4.1.
- [ ] 4.3 REFACTOR: Tighten authorization status constants and mapper query names introduced by 4.2.

## 5. Account Security And Settings APIs

- [ ] 5.1 RED: Add WebMvc and request validation tests for notification settings, privacy settings, third-party authorization APIs, and account security entry responses.
- [ ] 5.2 GREEN: Extend `ContentUserSettingsController` and related services so the HTTP contracts from 5.1 pass.
- [ ] 5.3 REFACTOR: Review controller methods for thin protocol boundaries, `Result<T>` consistency, and Chinese validation messages after 5.2.

## 6. Integration Validation

- [ ] 6.1 RED: Add focused regression tests proving existing subscription notification defaults and profile visibility behavior still work with the new settings.
- [ ] 6.2 GREEN: Fix only the regressions exposed by 6.1 and wire notification-producing services through the new decision method where required.
- [ ] 6.3 REFACTOR: Remove duplicate setup in the new tests and keep implementation changes scoped to EPIC-06.

## 7. Final Verification

- [ ] 7.1 RED: Run the targeted Maven test command and `openspec validate --change epic-06-privacy-notifications --strict`; record any failing evidence.
- [ ] 7.2 GREEN: Resolve validation or targeted-test failures found by 7.1 without broad unrelated refactors.
- [ ] 7.3 REFACTOR: Run the final targeted Maven test command, rerun OpenSpec validation, and update graphify metadata after code changes.
