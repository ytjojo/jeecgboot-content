## ADDED Requirements

### Requirement: Basic profile editing
The system SHALL allow an authenticated content user to view and update required and optional profile fields while enforcing validation, media constraints, frequency limits, and moderation status.

#### Scenario: Required profile fields are saved
- **WHEN** an authenticated user submits a valid nickname and avatar with optional bio, gender, birthday, region, profession, and personal link
- **THEN** the system saves the profile, updates the completion state, and returns the updated profile summary

#### Scenario: Profile update rejects invalid field values
- **WHEN** a user submits null required nickname or avatar, empty required nickname or avatar, nickname beyond the configured length, bio beyond 500 characters, invalid gender value, future birthday, region beyond the configured length, profession beyond the configured length, or malformed personal link
- **THEN** the system rejects the update and preserves the previous effective profile

#### Scenario: Avatar media constraints are enforced
- **WHEN** a user uploads an avatar with null file metadata, empty file content, unsupported format, file size greater than 5MB, or dimensions outside the supported processing range
- **THEN** the system rejects the avatar and returns the matching validation message

#### Scenario: Profile update frequency is limited
- **WHEN** a user attempts the 6th profile modification in one day
- **THEN** the system rejects the update and keeps the previous effective profile unchanged

#### Scenario: Suspicious profile content enters review
- **WHEN** a nickname, avatar, or bio matches sensitive-word or AI review risk rules
- **THEN** the system creates a pending review record and does not publicly expose the submitted value until the review is approved

#### Scenario: Pending review blocks another profile change
- **WHEN** a user has a pending profile review and submits another profile modification
- **THEN** the system rejects the modification and tells the user the profile is under review

#### Scenario: Review result applies or restores profile
- **WHEN** a pending profile review is approved or rejected
- **THEN** the system either publishes the new profile values and notifies the user, or restores the previous values and records the rejection reason

### Requirement: Homepage personalization
The system SHALL allow users to personalize their public homepage background, theme color, module visibility, and module order.

#### Scenario: Homepage background and theme are saved
- **WHEN** a user submits a valid background image and a supported theme color
- **THEN** the system stores the homepage configuration and returns resource URLs suitable for homepage rendering

#### Scenario: Homepage background rejects invalid media
- **WHEN** a user uploads a background image with null metadata, empty content, unsupported format, file size greater than 5MB, or dimensions outside the supported processing range
- **THEN** the system rejects the background update and keeps the previous background

#### Scenario: Theme color rejects invalid values
- **WHEN** a user submits a null theme color to clear customization, an empty theme color, an unsupported named color, or a color string beyond the configured length
- **THEN** the system either restores the platform default for null clear requests or rejects invalid non-null values

#### Scenario: Homepage modules can be hidden and sorted
- **WHEN** a user submits a valid module configuration with visible modules and unique sort orders
- **THEN** the system saves the module visibility and order and renders the homepage using that configuration

#### Scenario: Homepage modules reject invalid configuration
- **WHEN** a user submits null module list, empty module list, unknown module key, duplicate module key, duplicate sort order, negative sort order, or a configuration that hides all modules
- **THEN** the system rejects the configuration and keeps the previous module layout

#### Scenario: Restore homepage defaults
- **WHEN** a user requests to restore default homepage settings
- **THEN** the system clears custom background, theme color, and module layout and uses platform defaults

### Requirement: Verification badge display
The system SHALL expose trusted verification badges for profile, content, comment, and homepage display without coupling verification records to mutable profile text fields.

#### Scenario: Verification badge appears near nickname
- **WHEN** a user has an active personal, enterprise, creator, official, or real-name verification record
- **THEN** the system returns the badge type, badge label, visual style key, verified time, and verification description for display near the nickname

#### Scenario: Verification detail is queryable
- **WHEN** a viewer requests details for a visible verification badge
- **THEN** the system returns certification type, certification time, certification label, and certification description

#### Scenario: Invalid verification records are not displayed
- **WHEN** a verification record has null type, empty type, unsupported type, inactive status, expired time, or missing required enterprise or creator metadata
- **THEN** the system does not expose that badge in public profile responses

#### Scenario: Bound contact badges respect visibility
- **WHEN** a user has verified mobile or email binding from the account layer and chooses to expose that verification state
- **THEN** the profile response includes the visible mobile or email verified badge

#### Scenario: Hidden contact badges are not leaked
- **WHEN** a user hides mobile or email verification badges or the viewer is not allowed by visibility rules
- **THEN** the system omits the binding-state badge from public profile responses

### Requirement: Field visibility and privacy
The system SHALL apply per-field profile visibility rules for public, followers-only, mutual-only, and private scopes on every profile read.

#### Scenario: Public fields are visible to any viewer
- **WHEN** a profile field visibility is PUBLIC
- **THEN** the system returns that field to anonymous, unrelated, follower, mutual, and owner viewers

#### Scenario: Followers-only fields are visible only to followers
- **WHEN** a profile field visibility is FOLLOWERS_ONLY
- **THEN** the system returns that field only to the owner and viewers who follow the owner

#### Scenario: Mutual-only fields are visible only to mutual relations
- **WHEN** a profile field visibility is MUTUAL_ONLY
- **THEN** the system returns that field only to the owner and viewers with mutual following relation to the owner

#### Scenario: Private fields are visible only to owner
- **WHEN** a profile field visibility is PRIVATE
- **THEN** the system returns that field only to the owner and omits it for every other viewer

#### Scenario: Privacy update rejects invalid field values
- **WHEN** a user submits null visibility to keep default, empty visibility, unsupported visibility, visibility string beyond the configured length, null field key, empty field key, or unknown field key
- **THEN** the system preserves the previous setting or default for null keep-default requests and rejects invalid non-null values

#### Scenario: Privacy change is immediately enforced for new requests
- **WHEN** a user changes a field from PUBLIC to PRIVATE
- **THEN** the next profile read MUST perform visibility evaluation against the latest setting and MUST NOT leak the previously public field

#### Scenario: Privacy update frequency is limited
- **WHEN** a user changes privacy settings more than 10 times within 1 hour
- **THEN** the system rejects additional privacy changes until the configured window resets

### Requirement: Profile cache invalidation
The system SHALL use profile caching only when it cannot violate current privacy settings and SHALL invalidate public caches when privacy or public profile fields change.

#### Scenario: Public profile cache is invalidated after privacy change
- **WHEN** a user successfully changes field visibility
- **THEN** the system clears public profile cache entries for that user and marks any cached page data to expire within 5 minutes

#### Scenario: Cached profile is filtered before response
- **WHEN** a viewer requests a cached profile before the cached page expires
- **THEN** the system applies current visibility rules before returning the response

#### Scenario: Cache keys reject invalid identifiers
- **WHEN** a cache operation receives null user id, empty user id, over-length user id, null viewer scope, or unknown viewer scope
- **THEN** the system avoids writing unsafe cache entries and falls back to direct data loading when possible

### Requirement: Nickname and avatar history
The system SHALL record previous nicknames and avatars, retain them for 180 days, keep at most 20 records per user and history type, and allow users to restore valid history values.

#### Scenario: Nickname and avatar changes create history
- **WHEN** a profile nickname or avatar update is approved and becomes effective
- **THEN** the system records the previous value, change time, history type, and source profile update

#### Scenario: History list is bounded and ordered
- **WHEN** a user views profile history
- **THEN** the system returns nickname and avatar history in reverse chronological order with at most 20 active records per type

#### Scenario: Excess history is pruned FIFO
- **WHEN** adding a new nickname or avatar history record would exceed 20 records for that user and type
- **THEN** the system removes or marks the oldest record before exposing the updated history list

#### Scenario: Expired history is cleaned
- **WHEN** the daily cleanup job finds nickname or avatar history older than 180 days
- **THEN** the system removes or expires those records so they cannot be restored

#### Scenario: Restore history value succeeds
- **WHEN** a user restores a non-expired nickname or avatar history value and the value passes current validation and moderation rules
- **THEN** the system treats the restore as a new profile modification, applies frequency limits, and updates the current profile after approval if needed

#### Scenario: Restore history rejects invalid or unavailable values
- **WHEN** a user restores a null history id, empty history id, unknown history id, expired history record, over-length nickname, unsupported avatar URL, nickname already used by another user, or restores after reaching the daily modification limit
- **THEN** the system rejects the restore and keeps the current profile unchanged
