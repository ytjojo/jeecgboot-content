# profile-management Specification

## Purpose
TBD - created by archiving change complete-profile-management. Update Purpose after archive.
## Requirements
### Requirement: Basic profile editing
The system SHALL allow an authenticated content user to view and update required and optional profile fields through the unified `POST /content/user/profile/update` endpoint with strict field validation, sensitive-word moderation, and review state tracking.

#### Scenario: Required profile fields are saved
- **WHEN** an authenticated user submits a valid `nickname` (≤30 chars) and `avatar` (≤512 chars URL) with optional `bio` (≤500), `gender` (`MALE|FEMALE|OTHER|UNKNOWN`), `birthday` (past), `region` (≤64), `profession` (≤64), and `personalLink` (≤256, http(s)://)
- **THEN** the system saves the profile, updates the completion state, and returns the updated `ContentUserProfileVO`

#### Scenario: Profile update rejects invalid field values
- **WHEN** a user submits null/empty `nickname`, null/empty `avatar`, `nickname` beyond 30 chars, `bio` beyond 500 chars, invalid `gender` value, future `birthday`, `region` beyond 64 chars, `profession` beyond 64 chars, or `personalLink` not matching `^https?://.*$`
- **THEN** the system rejects the update with the matching validation error and preserves the previous effective profile

#### Scenario: Avatar and background are URL-only
- **WHEN** a user submits an `avatar` or `homepageBackground` value
- **THEN** the system only validates it is a non-blank string within the length limit and stores it as a CDN URL; the system does NOT receive multipart uploads

#### Scenario: Suspicious profile content enters review
- **WHEN** a `nickname`, `avatar`, or `bio` matches sensitive-word or AI review risk rules
- **THEN** the system creates a pending review record in `content_user_profile_review` and does not publicly expose the submitted value until `POST /content/user/profile/review/handle` approves it

#### Scenario: Pending review blocks another profile change
- **WHEN** a user has a pending profile review and submits another profile modification
- **THEN** the system rejects the modification with a "profile under review" error

#### Scenario: Review handler publishes or restores
- **WHEN** a moderator calls `POST /content/user/profile/review/handle` with `approve=true`
- **THEN** the system publishes the snapshot to the profile and clears the pending review
- **WHEN** a moderator calls `POST /content/user/profile/review/handle` with `approve=false` and a rejection reason
- **THEN** the system discards the snapshot and records the rejection reason in the review history

### Requirement: Homepage personalization
The system SHALL allow users to personalize their public homepage background, theme color, module visibility, and module order through `POST /content/user/profile/homepage/update` and `GET /content/user/profile/homepage/modules`.

#### Scenario: Homepage background and theme are saved
- **WHEN** a user submits a valid `homepageBackground` (≤512 chars URL) and `themeColor` matching `^#[0-9A-Fa-f]{6}$` via `/homepage/update`
- **THEN** the system stores the homepage configuration and returns the updated `ContentUserProfileVO`

#### Scenario: Theme color rejects invalid values
- **WHEN** a user submits a `themeColor` that is empty, not matching the hex pattern, or beyond 16 chars
- **THEN** the system rejects the update with the matching validation error

#### Scenario: Homepage modules are aggregated for display
- **WHEN** a viewer calls `GET /content/user/profile/homepage/modules?userId=X`
- **THEN** the system returns `List<ContentUserHomepageModuleVO>` with each module's `moduleKey`, `moduleName`, `visible`, and `sortOrder`, filtered by the current privacy visibility rules

#### Scenario: Restore homepage defaults
- **WHEN** a user calls `POST /content/user/profile/homepage/defaults/restore?userId=X`
- **THEN** the system clears custom background, theme color, and module layout, uses platform defaults, and returns the updated `ContentUserProfileVO`

### Requirement: Verification badge display
The system SHALL expose trusted verification badges through `GET /content/user/profile/badge/list` and `GET /content/user/profile/badge/detail` with `visualStyleKey` for frontend icon/color mapping.

#### Scenario: Verification badge list returns visible badges
- **WHEN** a viewer calls `GET /content/user/profile/badge/list?userId=X`
- **THEN** the system returns a `List<ContentUserVerificationBadgeVO>` filtered by `verificationBadgesVisibility` privacy setting, where each badge contains `badgeId`, `badgeType` (`INDIVIDUAL|ENTERPRISE|CREATOR|OFFICIAL|REAL_NAME|MOBILE|EMAIL`), `badgeLabel`, `visualStyleKey`, `verifiedAt`, `expiresAt`, and `description`

#### Scenario: Verification detail is queryable
- **WHEN** a viewer calls `GET /content/user/profile/badge/detail?badgeId=Y` for a visible badge
- **THEN** the system returns the `ContentUserVerificationBadgeVO` for that badge

#### Scenario: Invalid verification records are not displayed
- **WHEN** a verification record has null `badgeType`, empty `badgeType`, unsupported `badgeType`, inactive status, expired `expiresAt`, or missing required enterprise/creator metadata
- **THEN** the system omits that badge from the public list response

#### Scenario: Bound contact badges respect visibility
- **WHEN** the privacy setting `verificationBadgesVisibility` is `PUBLIC` and the user has a `MOBILE` or `EMAIL` badge
- **THEN** the badge list response includes those bound contact badges

#### Scenario: Hidden contact badges are not leaked
- **WHEN** the privacy setting `visibilityBadgesVisibility` is `PRIVATE` or the viewer is not allowed by visibility rules
- **THEN** the system omits the bound contact badges from the badge list response

### Requirement: Field visibility and privacy
The system SHALL apply per-field profile visibility rules for public, followers-only, mutual-only, and private scopes on every profile read through `POST /content/user/profile/privacy/update`.

#### Scenario: 15 visibility fields cover profile, homepage, and presence
- **WHEN** a user calls `POST /content/user/profile/privacy/update` with any subset of `bioVisibility`, `genderVisibility`, `birthdayVisibility`, `regionVisibility`, `professionVisibility`, `personalLinkVisibility`, `homepageBackgroundVisibility`, `themeColorVisibility`, `certificationVisibility`, `verificationBadgesVisibility`, `onlineStatusVisibility`, `homepageModuleVisibility`, `profileCompletionVisibility`, `profileReviewStatusVisibility`, `recentActivityVisibility`
- **THEN** the system accepts valid values (`PUBLIC|FOLLOWERS_ONLY|MUTUAL_ONLY|PRIVATE` for all except `onlineStatusVisibility` which accepts `PUBLIC|HIDDEN|MUTUAL_ONLY`)

#### Scenario: Online status visibility has reduced enum
- **WHEN** a user submits `onlineStatusVisibility` as `PRIVATE`
- **THEN** the system rejects the update because `onlineStatusVisibility` only accepts `PUBLIC|HIDDEN|MUTUAL_ONLY`

#### Scenario: Privacy update rejects invalid field values
- **WHEN** a user submits a visibility value not in the allowed enum, beyond configured length, null `userId` (query), or malformed `userId`
- **THEN** the system rejects the update and preserves the previous setting

#### Scenario: Privacy change is immediately enforced for new requests
- **WHEN** a user changes a field from `PUBLIC` to `PRIVATE` via `/privacy/update`
- **THEN** the system invalidates `content:user:profile:public:{userId}` and the next `GET /detail` MUST perform visibility evaluation against the latest setting and MUST NOT leak the previously public field

#### Scenario: Public fields are visible to any viewer
- **WHEN** a profile field visibility is `PUBLIC`
- **THEN** `ContentUserProfileVO.from(...)` returns that field to anonymous, unrelated, follower, mutual, and owner viewers

#### Scenario: Followers-only fields are visible only to followers
- **WHEN** a profile field visibility is `FOLLOWERS_ONLY`
- **THEN** the system returns that field only to the owner and viewers who follow the owner

#### Scenario: Mutual-only fields are visible only to mutual relations
- **WHEN** a profile field visibility is `MUTUAL_ONLY`
- **THEN** the system returns that field only to the owner and viewers with mutual following relation to the owner

#### Scenario: Private fields are visible only to owner
- **WHEN** a profile field visibility is `PRIVATE`
- **THEN** the system returns that field only to the owner and omits it for every other viewer

### Requirement: Profile cache invalidation
The system SHALL use profile caching only when it cannot violate current privacy settings and SHALL invalidate public caches when privacy settings change.

#### Scenario: Public profile cache is invalidated after privacy change
- **WHEN** a user successfully calls `/privacy/update` and at least one visibility field value changes
- **THEN** the system calls `invalidateProfileCache(userId)` to clear public profile cache entries for that user

#### Scenario: Cached profile is filtered before response
- **WHEN** a viewer requests a cached profile before the cached page expires
- **THEN** `ContentUserProfileVO.from(...)` re-applies current visibility rules before returning the response

#### Scenario: Cache keys reject invalid identifiers
- **WHEN** a cache operation receives null/empty `userId`, over-length `userId`, null viewer scope, or unknown viewer scope
- **THEN** the system avoids writing unsafe cache entries and falls back to direct data loading

### Requirement: Nickname and avatar history
The system SHALL record previous nicknames and avatars, retain them for 180 days, keep at most 20 records per user and history type, and allow users to restore valid history values through `GET /content/user/profile/history/list` and `POST /content/user/profile/history/restore`.

#### Scenario: History list returns per-type ordered records
- **WHEN** a viewer calls `GET /content/user/profile/history/list?userId=X&historyType=NICKNAME`
- **THEN** the system returns a `List<ContentUserProfileHistoryVO>` in reverse chronological order with at most 20 active records, each containing `historyId`, `historyType`, `historyValue`, `changedAt`, `expiresAt`, and `sourceProfileUpdateId`
- The same applies to `historyType=AVATAR`

#### Scenario: History list rejects invalid history type
- **WHEN** a viewer calls `/history/list` with `historyType` other than `NICKNAME` or `AVATAR`
- **THEN** the system rejects the request with a validation error

#### Scenario: Excess history is pruned FIFO
- **WHEN** adding a new nickname or avatar history record would exceed 20 records for that user and type
- **THEN** the system removes or marks the oldest record before exposing the updated history list

#### Scenario: Expired history is cleaned
- **WHEN** the daily cleanup job finds history records with `expiresAt` older than now
- **THEN** the system removes or expires those records so they cannot be restored

#### Scenario: Restore history value succeeds
- **WHEN** a user calls `POST /content/user/profile/history/restore?userId=X&historyId=Y` and the history record is non-expired and the value passes current validation and moderation rules
- **THEN** the system treats the restore as a new profile modification, runs through the same audit pipeline, and returns the updated `ContentUserProfileVO` after approval if needed

#### Scenario: Restore history rejects invalid or unavailable values
- **WHEN** a user restores with null/empty `userId`, null/empty `historyId`, unknown `historyId`, expired history record, over-length `nickname`, unsupported `avatar` URL, or `nickname` already used by another user
- **THEN** the system rejects the restore and keeps the current profile unchanged

