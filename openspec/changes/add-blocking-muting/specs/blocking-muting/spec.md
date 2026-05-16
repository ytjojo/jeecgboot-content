## ADDED Requirements

### Requirement: User blocking
The system SHALL allow an authenticated user to block another user and SHALL treat the block as a bidirectional visibility and interaction cut.

#### Scenario: Block user succeeds
- **WHEN** an authenticated user confirms blocking a valid target user from a profile page or comment area
- **THEN** the system records the target user in the actor's blacklist with block time and returns success within the configured operation budget

#### Scenario: Block user rejects invalid targets
- **WHEN** the block request contains null target user id, empty target user id, over-length target user id, current user's own id, or an unknown target user id
- **THEN** the system rejects the request and does not create a blacklist record

#### Scenario: Actor cannot view blocked user's profile
- **WHEN** a user who blocked another user attempts to access the blocked user's profile
- **THEN** the system returns a blocked-view response such as "您已拉黑该用户，无法查看其内容"

#### Scenario: Blocked user cannot view actor profile
- **WHEN** a blocked user attempts to access the actor's profile
- **THEN** the system returns a non-revealing response such as user-not-found or empty profile

#### Scenario: Blocked authors are filtered from actor feed
- **WHEN** a user browses recommendation feed, following feed, content list, comment list, or activity feed after blocking another user
- **THEN** the system excludes all content, comments, and activities authored by the blocked user

#### Scenario: Actor content is filtered from blocked user's feed
- **WHEN** a blocked user browses recommendation feed, following feed, content list, comment list, or activity feed
- **THEN** the system excludes all content, comments, and activities authored by the actor who blocked them

### Requirement: Blocking interaction boundaries
The system SHALL make blocking higher priority than following, liking, commenting, messaging, and mentioning.

#### Scenario: Blocking removes mutual following
- **WHEN** a user blocks another user and the two users currently follow each other
- **THEN** the system removes both following relationships in the same business operation

#### Scenario: Blocked user cannot comment
- **WHEN** a blocked user attempts to comment on the actor's content
- **THEN** the system rejects the comment with a generic operation failure and creates no comment record

#### Scenario: Blocked user cannot like
- **WHEN** a blocked user attempts to like the actor's content
- **THEN** the system rejects the like with a generic operation failure and creates no like record

#### Scenario: Blocked user cannot send message
- **WHEN** a blocked user attempts to send a private message to the actor
- **THEN** the system rejects the message with a generic send failure and creates no message record

#### Scenario: Mention from blocked user is suppressed
- **WHEN** a blocked user mentions the actor in content or comments
- **THEN** the system does not send a notification to the actor and does not expose an active mention link to the actor

#### Scenario: Actor cannot mention blocked user
- **WHEN** the actor attempts to mention a user they have blocked
- **THEN** the system rejects the mention and tells the actor that blocked users cannot be mentioned

#### Scenario: Blocking and unblocking are silent
- **WHEN** a user blocks or unblocks another user
- **THEN** the system MUST NOT send any notification to the target user

### Requirement: Blacklist management
The system SHALL let users view and manage their blacklist without restoring removed social relationships automatically.

#### Scenario: Blacklist is ordered by block time
- **WHEN** a user opens the blacklist page
- **THEN** the system returns blocked users ordered by block time descending

#### Scenario: Blacklist item includes display fields
- **WHEN** the system returns a blacklist item
- **THEN** the item includes blocked user id, nickname, avatar, and block time

#### Scenario: Unblock user succeeds
- **WHEN** a user confirms unblocking a user currently in their blacklist
- **THEN** the system removes the blacklist relationship and the user no longer appears in the refreshed blacklist

#### Scenario: Unblock user rejects invalid targets
- **WHEN** the unblock request contains null target user id, empty target user id, over-length target user id, or a target not present in the user's blacklist
- **THEN** the system rejects the request or treats the missing relationship idempotently according to API contract without restoring following relationships

#### Scenario: Unblocked profile becomes visible
- **WHEN** a user unblocks another user and no opposite-direction block exists
- **THEN** the user can access the target profile normally but previous following relationships remain removed

### Requirement: Muting users
The system SHALL allow users to mute another user's feed content as a one-way noise-reduction action without cutting social relationship or profile access.

#### Scenario: Mute user succeeds
- **WHEN** a user chooses "屏蔽该用户" on a valid content item or profile
- **THEN** the system records the target user in the actor's mute list

#### Scenario: Mute user rejects invalid targets
- **WHEN** the mute request contains null target user id, empty target user id, over-length target user id, current user's own id, or an unknown target user id
- **THEN** the system rejects the request and does not create a mute record

#### Scenario: Muted user's content is hidden from feeds
- **WHEN** a user browses recommendation feed or following feed after muting another user
- **THEN** the system excludes content authored by the muted user

#### Scenario: Muted user's profile remains accessible
- **WHEN** a user who muted another user actively opens the muted user's profile
- **THEN** the system returns that user's profile and content unless a blocking relationship also exists

#### Scenario: Following relationship remains after mute
- **WHEN** a user mutes another user they follow
- **THEN** the system keeps the following relationship unchanged

#### Scenario: Mute is one-way
- **WHEN** a muted user opens the actor's profile or browses feed
- **THEN** the muted user can still see the actor's content unless another visibility rule blocks it

### Requirement: Not interested feedback and content filters
The system SHALL record not-interested feedback and allow users to reduce or filter content by content type, topic, Tag, and temporary topic rules.

#### Scenario: Not interested feedback is recorded
- **WHEN** a user clicks "不感兴趣" on a valid content item
- **THEN** the system records the feedback and reduces similar content in subsequent feed responses

#### Scenario: Not interested rejects invalid content
- **WHEN** the feedback request contains null content id, empty content id, over-length content id, unknown content id, null content type, empty content type, or unsupported content type
- **THEN** the system rejects the feedback and does not create a feedback record

#### Scenario: User blocks content type
- **WHEN** a user chooses to block a valid content type after clicking "不感兴趣"
- **THEN** the system stores a content-type filter rule and excludes that type from feed responses

#### Scenario: Content type filter rejects invalid values
- **WHEN** the content-type filter request contains null type, empty type, over-length type, or unsupported type
- **THEN** the system rejects the rule and keeps existing filters unchanged

#### Scenario: User blocks topic or Tag
- **WHEN** a user chooses to block a valid topic or Tag
- **THEN** the system stores the topic filter rule and filters feed content containing that topic or Tag

#### Scenario: Topic filter rejects invalid values
- **WHEN** the topic filter request contains null topic, empty topic, topic beyond the configured length, or unsupported topic format
- **THEN** the system rejects the rule and keeps existing topic filters unchanged

#### Scenario: Temporary topic filter expires
- **WHEN** a user creates a temporary topic filter for 7 days and the expiration time passes
- **THEN** the system automatically stops applying that filter and restores normal recommendation eligibility

#### Scenario: Temporary topic filter can be cancelled early
- **WHEN** a user cancels a valid unexpired temporary topic filter
- **THEN** the system immediately stops applying that filter

### Requirement: Keyword filtering
The system SHALL allow users to configure keyword and regular-expression filters for feed content titles and bodies.

#### Scenario: Keyword filter is saved
- **WHEN** a user adds a valid keyword filter
- **THEN** the system saves the keyword and applies it to subsequent feed content title and body matching

#### Scenario: Regex filter is saved
- **WHEN** a user adds a valid regular-expression filter within configured length and complexity limits
- **THEN** the system saves the regex rule and applies it to subsequent feed content title and body matching

#### Scenario: Keyword or regex filter rejects invalid values
- **WHEN** a filter request contains null rule text, empty rule text, rule text beyond the configured length, unsupported rule type, or an invalid regular expression
- **THEN** the system rejects the rule and keeps existing filters unchanged

#### Scenario: Matching content is folded or hidden
- **WHEN** feed content title or body matches a user's active keyword or regex filter
- **THEN** the system returns the content as hidden or folded with a prompt such as "该内容包含屏蔽词，已折叠"

#### Scenario: Filtered content can be expanded
- **WHEN** a user chooses to expand a folded item filtered by keyword or regex
- **THEN** the system returns the content detail according to normal visibility rules

#### Scenario: Keyword filter can be removed
- **WHEN** a user deletes an existing keyword or regex filter
- **THEN** the system stops using that rule for later feed filtering

### Requirement: Mute and filter list management
The system SHALL manage muted users, blocked topics, blocked content types, temporary filters, and keyword filters independently from the blacklist.

#### Scenario: Privacy settings expose separate entries
- **WHEN** a user opens privacy settings
- **THEN** the system exposes separate entries for blacklist and mute/filter list management

#### Scenario: Mute list includes all filter categories
- **WHEN** a user opens the mute/filter list
- **THEN** the system returns muted users, blocked topics, blocked content types, temporary topic filters, and keyword or regex filters

#### Scenario: Single mute or filter can be cancelled
- **WHEN** a user cancels one valid muted user, topic, type, temporary rule, keyword, or regex filter
- **THEN** the system removes that rule and immediately restores eligibility for matching content sources

#### Scenario: Batch cancel filters succeeds
- **WHEN** a user selects multiple valid mute or filter items and confirms batch cancellation
- **THEN** the system removes all selected items atomically or reports failed items without removing unrelated rules

#### Scenario: Batch cancel rejects invalid identifiers
- **WHEN** the batch cancel request contains null ids, empty ids, duplicate ids, over-length ids, unknown ids, or ids belonging to another user
- **THEN** the system rejects invalid items and never removes another user's rules

### Requirement: Boundary education
The system SHALL explain the difference between muting and blocking before destructive or easily confused actions.

#### Scenario: Block confirmation explains consequences
- **WHEN** a user clicks the block action
- **THEN** the system shows confirmation text explaining that both users will be unable to view each other's content and existing following relationships will be removed

#### Scenario: Mute confirmation explains one-way behavior
- **WHEN** a user clicks the mute action
- **THEN** the system shows confirmation text explaining that the actor will no longer see the target user's feed content, profile access remains available, and the target user is unaffected

#### Scenario: Unblock confirmation explains following is not restored
- **WHEN** a user unblocks another user
- **THEN** the system tells the user that the block is removed but following relationship will not be restored automatically

#### Scenario: Help text compares block and mute
- **WHEN** a user searches help or opens guidance for "拉黑" or "屏蔽"
- **THEN** the system returns a comparison that states "屏蔽是单向降噪、拉黑是双向切断"
