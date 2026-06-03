## ADDED Requirements

### Requirement: Follow relationship lifecycle
The system SHALL allow authenticated content users to follow, unfollow, special-follow, and inspect other users while enforcing relationship constraints.

#### Scenario: Follow creates or refreshes relationship
- **WHEN** a user follows another active user with a valid target user id and optional valid relation group id
- **THEN** the system marks the relationship as followed, assigns the default group when no group is provided, records follow time, and returns the followed state

#### Scenario: Follow rejects invalid target values
- **WHEN** a follow request has null target user id, empty target user id, over-length target user id, self target user id, unknown target user id, blacklisted target, or a target that has blocked the requester
- **THEN** the system rejects the follow request and preserves the previous relationship state

#### Scenario: Unfollow removes follow and special-follow state
- **WHEN** a user confirms unfollow for a currently followed target user
- **THEN** the system marks the relationship as not followed, clears special-follow state, and removes the target user's future content from new follow-feed queries

#### Scenario: Special follow promotes an existing or new follow
- **WHEN** a user special-follows another valid user
- **THEN** the system marks the relationship as followed and special-followed, records special-follow time, and makes future eligible activity available for strong reminder and priority feed display

#### Scenario: Cancel special follow retains normal follow
- **WHEN** a user cancels special-follow for a target user that is still followed
- **THEN** the system clears only the special-follow state and keeps the normal follow relationship

#### Scenario: Relationship detail is queryable
- **WHEN** a user queries relation detail for a valid target user
- **THEN** the system returns followed, special-followed, muted, blacklisted, group, follow time, and special-follow time fields without exposing entity-only fields

### Requirement: Follow group management
The system SHALL allow users to organize followed users with default and custom relation groups.

#### Scenario: Default group is created and used
- **WHEN** a user follows another user without choosing a group
- **THEN** the system places the relationship into the user's default group and ensures the default group cannot be deleted

#### Scenario: Custom group is created or renamed
- **WHEN** a user submits a valid group name and sort order for creation or rename
- **THEN** the system saves the group and enforces user-scoped unique group names

#### Scenario: Group request rejects invalid values
- **WHEN** a group request has null group name, empty group name, over-length group name, duplicate group name, null owner user id, empty owner user id, over-length owner user id, negative sort order, or over-length group id
- **THEN** the system rejects the request and preserves existing group data

#### Scenario: Followed users move between groups
- **WHEN** a user moves one or more followed target users into a valid owned group
- **THEN** the system updates their relation group and returns success and failure counts for the move operation

#### Scenario: Removing a user from a group keeps follow
- **WHEN** a user removes a followed target user from a custom group
- **THEN** the system moves the relationship back to the default group and keeps the follow state

### Requirement: Follow list and special-follow list
The system SHALL provide paged management lists for followed users and special-followed users.

#### Scenario: Follow list supports search and group filter
- **WHEN** a user queries the follow list with valid pagination, optional group id, and optional nickname keyword
- **THEN** the system returns followed users ordered by follow time descending with total count, group info, relation state, and profile summary

#### Scenario: Follow list rejects invalid filters
- **WHEN** a follow list query has null user id, empty user id, over-length user id, unknown group id, page below 1, size below 1, size above the configured maximum, or over-length keyword
- **THEN** the system rejects or normalizes the query according to API pagination rules without returning unrelated users

#### Scenario: Special-follow list shows latest activity hint
- **WHEN** a user queries the special-follow list
- **THEN** the system returns only special-followed users with total count, profile summary, relation state, and latest eligible activity hint when available

#### Scenario: Empty special-follow list returns guidance state
- **WHEN** a user has no special-followed users
- **THEN** the system returns an empty list with total count zero and an empty-state code suitable for guiding the user to set special follows

### Requirement: Follow feed
The system SHALL provide a configurable follow feed containing eligible activities from followed users.

#### Scenario: Follow feed shows followed users' activities
- **WHEN** a user opens the follow feed with default settings
- **THEN** the system returns eligible publish, like, and favorite activities from followed users ordered by special-follow priority and activity time descending

#### Scenario: Feed type settings filter activities
- **WHEN** a user disables like and favorite activity types and keeps publish enabled
- **THEN** the next follow-feed query returns only publish activities from followed users

#### Scenario: Feed settings reject invalid values
- **WHEN** a feed setting request has null user id, empty user id, over-length user id, null activity type list, empty activity type list, unsupported activity type, duplicate activity type, or a configuration that disables every activity type
- **THEN** the system rejects the setting and keeps the previous feed configuration

#### Scenario: Follow feed paginates historical activity
- **WHEN** a user scrolls the follow feed with valid cursor or page parameters
- **THEN** the system returns the next page of eligible activities and stable pagination metadata

#### Scenario: Follow feed hides unfollowed users after refresh
- **WHEN** a user unfollows a target user and refreshes the follow feed
- **THEN** the system excludes new activities from that target user and does not leak activities hidden by block, mute, or content visibility rules

### Requirement: Follow recommendation and batch management
The system SHALL recommend follow targets with explainable reasons and support batch relation management.

#### Scenario: Recommended users include reasons
- **WHEN** a user opens the recommendation list
- **THEN** the system returns recommended active users with profile summary, relation state, ranking score, and a human-readable reason such as common follows, matching interests, or popular creator

#### Scenario: Recommendation excludes ineligible users
- **WHEN** recommendation candidates include the requester, already followed users, blacklisted users, blocked users, muted users, or inactive users
- **THEN** the system excludes those candidates from the recommendation list

#### Scenario: Recommendation query rejects invalid filters
- **WHEN** a recommendation query has null user id, empty user id, over-length user id, page below 1, size below 1, size above the configured maximum, or over-length interest tag
- **THEN** the system rejects or normalizes the query according to API pagination rules

#### Scenario: Batch relation operation returns result detail
- **WHEN** a user submits a batch unfollow, batch cancel special-follow, or batch move group request with valid target user ids
- **THEN** the system processes owned relationships and returns success count, failure count, and per-target failure reasons

#### Scenario: Batch relation operation rejects invalid values
- **WHEN** a batch relation request has null target id list, empty target id list, duplicate target ids, over-limit target count, over-length target id, unknown target id, null operation type, unsupported operation type, or invalid target group id
- **THEN** the system rejects invalid items or the whole request according to the operation contract and preserves unrelated relationships

### Requirement: Content source subscription lifecycle
The system SHALL allow users to subscribe to, pause, resume, and cancel subscriptions for supported content source types.

#### Scenario: Subscribe creates or resumes subscription
- **WHEN** a user subscribes to a valid topic, tag, collection, special, column, or channel source
- **THEN** the system creates or resumes the subscription, records source type, source id, source name, notification defaults, and subscription time

#### Scenario: Subscribe rejects invalid source values
- **WHEN** a subscription request has null source type, empty source type, unsupported source type, over-length source type, null source id, empty source id, over-length source id, null source name, empty source name, over-length source name, unknown source, or duplicate active subscription that violates uniqueness
- **THEN** the system rejects the request or returns the existing subscription id without creating duplicate active subscriptions

#### Scenario: Pause and resume retain subscription relationship
- **WHEN** a user pauses and later resumes an owned subscription
- **THEN** the system toggles update delivery while preserving subscription id, source metadata, and subscription history

#### Scenario: Cancel subscription removes future updates
- **WHEN** a user cancels an owned subscription
- **THEN** the system deactivates or deletes the subscription according to retention policy and excludes the source from new subscription-feed and notification queries

#### Scenario: Subscription ownership is enforced
- **WHEN** a user attempts to pause, resume, cancel, or configure a subscription that does not exist or belongs to another user
- **THEN** the system rejects the operation and preserves the subscription state

### Requirement: Subscription notification configuration
The system SHALL allow users to configure notification channels, frequency, and do-not-disturb rules for subscriptions.

#### Scenario: Subscription notification settings are saved
- **WHEN** a user configures an owned subscription with valid in-app, push, and email channels, valid realtime or daily frequency, and optional valid do-not-disturb window
- **THEN** the system saves the subscription-level notification configuration and uses it for future source updates

#### Scenario: Notification settings inherit global defaults
- **WHEN** a subscription has no explicit channel or frequency configuration
- **THEN** the system applies the user's global notification defaults before sending subscription update notifications

#### Scenario: Notification setting request rejects invalid values
- **WHEN** a notification setting request has null subscription id, empty subscription id, over-length subscription id, null channel list, empty channel list, unsupported channel, duplicate channel, null frequency, unsupported frequency, malformed do-not-disturb start time, malformed do-not-disturb end time, or start time equal to end time
- **THEN** the system rejects the setting and keeps the previous notification configuration

#### Scenario: Do-not-disturb delays update notifications
- **WHEN** a subscribed source updates during the user's configured do-not-disturb window
- **THEN** the system suppresses realtime push delivery and schedules the update for summary delivery after the do-not-disturb window

#### Scenario: Daily email summary groups updates
- **WHEN** a user chooses daily email summary for one or more subscriptions
- **THEN** the system groups eligible source updates into the configured daily summary and avoids sending duplicate realtime email notifications for the same update

### Requirement: Subscription feed and management list
The system SHALL provide a unified subscription feed and a paged management list for all user subscriptions.

#### Scenario: Subscription feed shows source updates
- **WHEN** a user opens the subscription feed
- **THEN** the system returns new eligible content from active, unpaused subscribed sources ordered by update time descending

#### Scenario: Subscription feed respects source type filter
- **WHEN** a user filters the subscription feed by topic, tag, collection, special, column, or channel
- **THEN** the system returns only updates from matching active subscribed source types

#### Scenario: Subscription feed query rejects invalid filters
- **WHEN** a subscription feed query has null user id, empty user id, over-length user id, unsupported source type, page below 1, size below 1, size above the configured maximum, or malformed cursor
- **THEN** the system rejects or normalizes the query according to API pagination rules

#### Scenario: Subscription management list returns all subscriptions
- **WHEN** a user opens the subscription management page
- **THEN** the system returns paged subscription items with source type, source name, subscription time, last update time, paused state, notification summary, and source profile summary

#### Scenario: Batch subscription operation returns result detail
- **WHEN** a user submits a batch pause, batch resume, or batch cancel request with valid owned subscription ids
- **THEN** the system processes the subscriptions and returns success count, failure count, and per-subscription failure reasons

### Requirement: Subscription discovery plaza
The system SHALL expose a subscription plaza for users to discover, search, inspect, and subscribe to content sources.

#### Scenario: Plaza lists hot subscription sources
- **WHEN** a user opens the subscription plaza without filters
- **THEN** the system returns enabled subscription sources ordered by heat score with source type, category, subscriber count, latest update time, and subscribed state

#### Scenario: Plaza supports category browsing
- **WHEN** a user selects a valid category
- **THEN** the system returns enabled subscription sources within that category ordered by heat score or latest update time

#### Scenario: Plaza search returns matching sources
- **WHEN** a user searches with a valid keyword
- **THEN** the system returns matching topics, tags, collections, specials, columns, and channels with pagination metadata

#### Scenario: Plaza query rejects invalid filters
- **WHEN** a plaza query has null category where category is required, empty category, over-length category, over-length keyword, unsupported source type, page below 1, size below 1, or size above the configured maximum
- **THEN** the system rejects or normalizes the query without exposing disabled or hidden sources

#### Scenario: Subscription source detail is queryable
- **WHEN** a user opens a valid enabled subscription source detail
- **THEN** the system returns source description, type, category, subscriber count, latest update, recent content summary, and current user's subscribed state

#### Scenario: Source detail rejects invalid source identity
- **WHEN** a source detail request has null source type, empty source type, unsupported source type, over-length source type, null source id, empty source id, over-length source id, unknown source id, or disabled source
- **THEN** the system rejects the request and does not create a subscription side effect
