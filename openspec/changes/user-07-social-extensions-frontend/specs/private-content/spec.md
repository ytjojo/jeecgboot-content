## ADDED Requirements

### Requirement: Private content publishing
The system SHALL add a visibility option to the content publishing form, allowing users to publish content as "仅互关可见" (mutual follow only).

#### Scenario: Publish mutual-follow-only content
- **WHEN** user selects "仅互关可见" from the visibility dropdown in the content publishing form and submits
- **THEN** the system SHALL save the content with `visibility=MUTUAL_FOLLOW_ONLY` and display a success message

#### Scenario: Visibility dropdown options
- **WHEN** user opens the visibility dropdown in the content publishing form
- **THEN** the system SHALL display two options: "公开" (public) and "仅互关可见" (mutual follow only)

### Requirement: Private content display in feed
The system SHALL control private content visibility in the feed stream based on mutual follow relationship.

#### Scenario: Mutual follow user sees private content in feed
- **WHEN** user A is in the feed stream and user B (who mutually follows A) has published private content
- **THEN** the private content SHALL appear in the feed with a "仅互关可见" lock icon and label

#### Scenario: Non-mutual follow user does not see private content in feed
- **WHEN** user A is in the feed stream and user B (who does not mutually follow A) has published private content
- **THEN** the private content SHALL NOT appear in user A's feed

### Requirement: Private content display on user profile
The system SHALL control private content visibility on user profile pages based on mutual follow relationship.

#### Scenario: Mutual follow user sees private content on profile
- **WHEN** user A visits user B's profile, and A and B mutually follow each other
- **THEN** the content list SHALL include private content items with "仅互关可见" label

#### Scenario: Non-mutual follow user sees only public content on profile
- **WHEN** user A visits user B's profile, and A and B do not mutually follow each other
- **THEN** the content list SHALL only display public content, with no placeholder or hint about private content existence

### Requirement: Private content search visibility
The system SHALL exclude private content from public search results and only show them to mutual follow users.

#### Scenario: Mutual follow user searches and finds private content
- **WHEN** user A searches for content and user B (who mutually follows A) has matching private content
- **THEN** the search results SHALL include the private content with "仅互关可见" label

#### Scenario: Non-mutual follow user cannot find private content
- **WHEN** user A searches for content and user B (who does not mutually follow A) has matching private content
- **THEN** the search results SHALL NOT include user B's private content

### Requirement: Private content interaction restrictions
The system SHALL restrict forwarding and handle collection (favorites) for private content.

#### Scenario: Forward button hidden for private content
- **WHEN** user views a private content detail page
- **THEN** the forward/share button SHALL be hidden

#### Scenario: Favorite private content then lose mutual follow
- **WHEN** user A has favorited user B's private content, then A and B lose mutual follow relationship
- **THEN** the favorite list SHALL display "内容已不可见" for that item, and the detail page SHALL show a permission prompt

### Requirement: Private content access after unfollow
The system SHALL handle private content access when mutual follow relationship is broken.

#### Scenario: Unfollow removes private content from feed
- **WHEN** user A unfollows user B (breaking mutual follow), and B has published private content
- **THEN** the private content SHALL no longer appear in A's feed

#### Scenario: Unfollow removes private content from profile
- **WHEN** user A unfollows user B, and A is viewing B's profile
- **THEN** the content list SHALL switch to public-only view

#### Scenario: Opened private content detail after unfollow
- **WHEN** user A has already opened user B's private content detail page, then unfollows B and refreshes
- **THEN** the page SHALL display "该内容仅对互关好友可见" permission prompt
