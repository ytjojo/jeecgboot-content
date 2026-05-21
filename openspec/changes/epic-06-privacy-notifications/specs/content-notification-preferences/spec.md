## ADDED Requirements

### Requirement: Manage interaction notification preferences
The system SHALL allow a registered user to configure notification enablement and delivery channels for each supported interaction type, including likes, comments, follows, favorites, mentions, and direct messages.

#### Scenario: List configurable interaction notification types
- GIVEN: a registered user opens notification settings
- WHEN: the settings are loaded
- THEN: the system returns all supported interaction notification types and their current enabled state and channels

#### Scenario: Disable one notification type
- GIVEN: a registered user has like notifications enabled
- WHEN: the user disables like notifications and saves
- THEN: the system persists the setting and skips subsequent non-security like notifications for that user

#### Scenario: Restrict one notification type to one channel
- GIVEN: a registered user wants comments only in the in-app channel
- WHEN: the user enables only the in-app channel for comment notifications and saves
- THEN: the system sends subsequent comment notifications only through the in-app channel

### Requirement: Manage do-not-disturb rules
The system SHALL allow a registered user to configure do-not-disturb windows for non-urgent notifications, while security notifications MUST bypass do-not-disturb restrictions.

#### Scenario: Suppress non-urgent notifications during do-not-disturb
- GIVEN: a registered user has a do-not-disturb window from 23:00 to 07:00
- WHEN: a non-urgent notification is triggered at 23:30 in the user's timezone
- THEN: the system does not send the notification immediately

#### Scenario: Preserve security notification delivery
- GIVEN: a registered user has an active do-not-disturb window
- WHEN: a security notification such as abnormal login is triggered
- THEN: the system sends the security notification despite the active do-not-disturb rule

#### Scenario: Temporarily disable do-not-disturb
- GIVEN: a registered user has an active do-not-disturb rule
- WHEN: the user temporarily disables do-not-disturb for one hour
- THEN: the system sends non-urgent notifications normally until the temporary disablement expires

### Requirement: Enforce unsubscribe compliance
The system MUST check user notification preferences before sending non-security notifications and MUST not send through a disabled type or channel.

#### Scenario: Skip disabled marketing notification
- GIVEN: a registered user has disabled marketing notifications
- WHEN: the system prepares a marketing notification for that user
- THEN: the system records a skipped decision and sends no marketing notification

#### Scenario: Skip all disabled channels
- GIVEN: a registered user has disabled all channels for a non-security notification type
- WHEN: the system prepares that notification type
- THEN: the system sends no notification through any channel

#### Scenario: Audit delivery decision
- GIVEN: a notification is evaluated against user preferences
- WHEN: the system sends or skips the notification
- THEN: the system records enough decision detail to verify whether the preference was honored
