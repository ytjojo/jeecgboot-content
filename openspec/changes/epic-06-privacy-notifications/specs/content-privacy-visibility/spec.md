## ADDED Requirements

### Requirement: Manage activity visibility
The system SHALL allow a registered user to configure visibility for browsing history, like activity, and favorites as public, followers-only, or self-only.

#### Scenario: Hide browsing history from other users
- GIVEN: a registered user sets browsing history visibility to self-only
- WHEN: another user views the registered user's profile
- THEN: the system does not expose the registered user's browsing history

#### Scenario: Show like activity only to followers
- GIVEN: a registered user sets like activity visibility to followers-only
- WHEN: a follower views the registered user's profile
- THEN: the system exposes like activity to that follower

#### Scenario: Always show private activity to owner
- GIVEN: a registered user sets favorites visibility to self-only
- WHEN: the registered user views their own profile
- THEN: the system exposes the favorites to the owner

### Requirement: Manage online status visibility
The system SHALL allow a registered user to configure online status visibility as public, mutual-follow-only, or hidden.

#### Scenario: Hide online status from others
- GIVEN: a registered user sets online status visibility to hidden
- WHEN: another user checks the registered user's status
- THEN: the system does not reveal that the registered user is online

#### Scenario: Show online status only to mutual follows
- GIVEN: a registered user sets online status visibility to mutual-follow-only
- WHEN: a mutual-follow user checks the registered user's status
- THEN: the system reveals the registered user's online status

#### Scenario: Owner sees own online status
- GIVEN: a registered user has hidden online status from others
- WHEN: the registered user checks their own status
- THEN: the system returns the actual online status to the owner

### Requirement: Manage search engine indexing
The system SHALL allow a registered user to control whether their public profile can be indexed by search engines.

#### Scenario: Return noindex when indexing is disabled
- GIVEN: a registered user disables search engine indexing
- WHEN: a crawler or public profile request loads the registered user's profile
- THEN: the system includes a noindex directive for that profile response

#### Scenario: Permit indexing when enabled
- GIVEN: a registered user enables search engine indexing
- WHEN: a crawler or public profile request loads the registered user's profile
- THEN: the system does not include a noindex directive for that profile response

### Requirement: Invalidate privacy cache
The system MUST ensure changed privacy settings affect new requests immediately and any cached profile or visibility response expires within five minutes.

#### Scenario: Updated privacy controls affect new requests
- GIVEN: a registered user changes a privacy visibility setting
- WHEN: another user makes a new visibility-sensitive request
- THEN: the system evaluates the request with the latest saved privacy setting

#### Scenario: Cached visibility response expires promptly
- GIVEN: a registered user changes a privacy visibility setting
- WHEN: a previously cached profile or visibility response exists
- THEN: the cached response becomes invalid no later than five minutes after the change
