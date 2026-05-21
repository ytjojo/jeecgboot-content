## ADDED Requirements

### Requirement: Badge catalog and progress
The system SHALL expose badge definitions grouped by category and SHALL show user-specific acquisition progress and grant state.

#### Scenario: Badge catalog is grouped by category
- **WHEN** an authenticated user opens the badge page
- **THEN** the system returns enabled badges grouped by achievement, identity, activity, and relationship categories

#### Scenario: Badge detail shows acquisition condition and progress
- **WHEN** a user views a not-yet-granted badge
- **THEN** the system returns acquisition condition, current progress, target progress, and remaining requirement

#### Scenario: Granted badge detail shows grant metadata
- **WHEN** a user views a granted badge
- **THEN** the system returns grant time, grant reason, status, and expiration time when applicable

#### Scenario: Invalid badge definition is not exposed
- **WHEN** a badge definition has null code, empty code, duplicate code, unsupported category, disabled status, malformed rule config, negative valid days, or over-length display fields
- **THEN** the system excludes or rejects the definition and does not expose it in the user badge catalog

### Requirement: Badge wearing and display
The system SHALL allow users to wear granted active badges and expose worn badges for homepage, post card, and comment display.

#### Scenario: User wears up to five badges
- **WHEN** a user selects between 1 and 5 active granted badges and saves the wearing configuration
- **THEN** the system marks those badges as displaying and returns the updated worn badge list

#### Scenario: Wearing configuration rejects invalid values
- **WHEN** a user submits null badge id list, empty badge id list, more than 5 badge ids, duplicate badge ids, unknown badge id, expired badge, recycled badge, or badge owned by another user
- **THEN** the system rejects the configuration and preserves the previous worn badge list

#### Scenario: Worn badges are visible in profile and content surfaces
- **WHEN** another user views the profile, post card, or comment of a badge wearer
- **THEN** the system returns worn badge icon, name, category, effect key, and grant reason for display

#### Scenario: Badge wearing updates immediately
- **WHEN** a user changes worn badges and saves successfully
- **THEN** subsequent profile, post card, and comment reads return the new badge list

### Requirement: Badge expiration and recycling
The system SHALL expire time-limited badges automatically and SHALL allow authorized administrators to recycle badges with audit trail and notification.

#### Scenario: Expired badge is removed from wearing list
- **WHEN** a badge grant reaches its expiration time
- **THEN** the system marks the grant as expired, removes it from the worn badge list, and shows it in the expired category

#### Scenario: Administrator recycles badge
- **WHEN** an authorized administrator recycles a user badge with a non-empty reason
- **THEN** the system marks the grant as recycled, removes it from the worn badge list, records operator and reason, creates audit log, and sends user notification

#### Scenario: Badge recycle rejects invalid values
- **WHEN** recycle request has null grant id, empty grant id, unknown grant id, missing operator, empty reason, over-length reason, or operator lacks permission
- **THEN** the system rejects the request and preserves the badge grant state

### Requirement: Point earning
The system SHALL award points for configured daily, creative, social, invitation, and task events while enforcing daily caps and idempotency.

#### Scenario: Daily behavior awards points within cap
- **WHEN** a user completes first login, 10-minute browsing, like, share, or comment behavior and the configured daily cap is not reached
- **THEN** the system adds configured points, updates point balance, and writes a point ledger

#### Scenario: Creative behavior awards points
- **WHEN** a user publishes content, receives editor recommendation, receives featured status, or receives counted repost events
- **THEN** the system adds configured creative points and writes source-specific point ledger entries

#### Scenario: Social and task behavior awards points
- **WHEN** a user is followed, invites a friend who registers, completes newcomer task, daily task, or activity task
- **THEN** the system adds configured points according to the event and writes a point ledger

#### Scenario: Daily cap stops additional points without blocking behavior
- **WHEN** a behavior event would exceed its daily point cap
- **THEN** the system does not add points, does not write a positive point ledger for the excess, and allows the original behavior to remain successful

#### Scenario: Point event rejects invalid values
- **WHEN** a point award event has null user id, empty user id, unsupported source type, null event id, empty event id, negative award amount for earn rule, over-limit amount, or malformed daily bucket
- **THEN** the system rejects or ignores the reward according to safe defaults and does not corrupt point balance

#### Scenario: Duplicate event is idempotent
- **WHEN** the same reward event id is processed more than once
- **THEN** the system awards points at most once and returns the existing result for duplicate attempts

### Requirement: Point spending and exchange
The system SHALL allow users to spend points on exchange goods, feature unlocks, and virtual gifts with atomic balance updates and benefit delivery.

#### Scenario: Exchange succeeds with enough points
- **WHEN** a user confirms exchange for a configured good and has sufficient point balance and stock
- **THEN** the system deducts points, writes a spending ledger, creates an exchange order, and grants the configured benefit atomically

#### Scenario: Exchange rejects insufficient balance or invalid goods
- **WHEN** a user exchanges with null good id, empty good id, unknown good id, disabled good, over-length quantity, quantity below 1, insufficient points, or insufficient stock
- **THEN** the system rejects the exchange and does not deduct points

#### Scenario: Feature unlock does not double charge
- **WHEN** a user spends points to unlock a configured feature and later uses the same unlocked feature again within its validity period
- **THEN** the system allows use without deducting points again

#### Scenario: Gift sending succeeds atomically
- **WHEN** a user sends a configured virtual gift to another user with sufficient points
- **THEN** the system deducts sender points, creates gift record, sends receiver notification, and writes spending ledger atomically

#### Scenario: Gift sending rejects invalid values
- **WHEN** gift request has null receiver id, empty receiver id, receiver equals sender, unknown gift id, disabled gift id, non-positive quantity, insufficient points, or over-length message
- **THEN** the system rejects the gift and does not deduct points

### Requirement: Point ledger query
The system SHALL provide paged point ledger query with type and time filters for user reconciliation and appeals.

#### Scenario: User queries point ledger
- **WHEN** a user opens point detail page
- **THEN** the system returns point ledger records ordered by create time descending with delta, balance after, source type, source description, and related business id

#### Scenario: User filters point ledger
- **WHEN** a user filters by earn or spend type and time range
- **THEN** the system returns only matching records with pagination metadata

#### Scenario: Ledger query rejects invalid filters
- **WHEN** query has null user id, empty user id, invalid type, start time after end time, page below 1, size below 1, or size above the configured maximum
- **THEN** the system rejects the query or normalizes pagination according to API rules

### Requirement: Growth value and level
The system SHALL calculate growth value separately from points, update levels by configurable thresholds, and notify users when level changes.

#### Scenario: Behavior awards growth and updates level
- **WHEN** a configured behavior grants growth and the user's growth value crosses a level threshold
- **THEN** the system writes growth ledger, updates profile growth and level, and creates level-up notification

#### Scenario: Points and growth are independent
- **WHEN** a user earns points, earns growth, or spends points
- **THEN** the system updates only the configured dimensions and spending points MUST NOT reduce growth value or level

#### Scenario: Level config rejects invalid values
- **WHEN** a level config has null level, duplicate level, level below 1, negative threshold, non-increasing threshold, null benefit config, malformed benefit config, or over-length display fields
- **THEN** the system rejects the config and does not use it for level calculation

#### Scenario: Growth event rejects invalid values
- **WHEN** a growth event has null user id, empty user id, unsupported source type, null event id, negative growth for earn rule, or amount beyond configured limit
- **THEN** the system rejects or ignores the growth update and preserves current growth summary

### Requirement: Level benefits and distribution weight
The system SHALL expose configurable level benefits and a small configurable content distribution weight without bypassing content quality controls.

#### Scenario: Level benefits are exposed
- **WHEN** a user reaches a configured level
- **THEN** the system returns level badge, comment visual effect, upload file limit, video quality limit, topic creation quota, and support priority benefit values

#### Scenario: Benefit check uses current level
- **WHEN** a user attempts an action controlled by level benefits
- **THEN** the system evaluates the user's current level and returns allow or deny with required level

#### Scenario: Distribution weight is bounded
- **WHEN** a high-level user publishes content
- **THEN** the system exposes a configured distribution weight no higher than the allowed maximum and quality scoring remains required

#### Scenario: Benefit config rejects invalid values
- **WHEN** benefit config has null benefit key, empty benefit key, unsupported benefit key, negative quota, upload limit below default, distribution weight above maximum, or malformed JSON
- **THEN** the system rejects the config and keeps previous valid benefit behavior

### Requirement: Growth decay and downgrade protection
The system SHALL decay growth for inactive users after 30 days, apply a 7-day downgrade protection period, and stop decay when the user becomes active.

#### Scenario: Inactive user starts growth decay
- **WHEN** a user has not logged in for 30 consecutive days and the decay job runs on day 31
- **THEN** the system reduces growth by the configured decay rate, writes growth ledger, and records decay state

#### Scenario: Downgrade protection starts before level drops
- **WHEN** decay reduces growth below the threshold of the current level
- **THEN** the system enters downgrade protection for 7 days instead of immediately lowering the level

#### Scenario: Activity stops decay during protection
- **WHEN** a user logs in and earns growth during the protection period
- **THEN** the system stops decay, clears protection state, and keeps the current level if threshold is recovered

#### Scenario: Protection expiry downgrades level
- **WHEN** the protection period ends and the user remains below the current level threshold
- **THEN** the system lowers the level to the level matching current growth and creates downgrade notification

#### Scenario: Decay config rejects invalid values
- **WHEN** decay config has null inactive days, inactive days below 1, negative decay rate, decay rate above the configured maximum, null protection days, protection days below 0, or over-length rule description
- **THEN** the system rejects the config and keeps the previous valid decay rule
