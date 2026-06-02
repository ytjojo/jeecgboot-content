## ADDED Requirements

### Requirement: Community role badge display in comments
The system SHALL display community role badges next to user nicknames in the comment section based on the user's role type.

#### Scenario: Creator role badge
- **WHEN** a comment is written by a user with `communityRole=CREATOR`
- **THEN** the system SHALL display a blue "创作者" Tag with a verification icon next to the nickname

#### Scenario: Moderator role badge
- **WHEN** a comment is written by a user with `communityRole=MODERATOR`
- **THEN** the system SHALL display a green "版主" Tag next to the nickname

#### Scenario: Admin role badge
- **WHEN** a comment is written by a user with `communityRole=ADMIN`
- **THEN** the system SHALL display an orange "管理员" Tag next to the nickname

#### Scenario: Normal user has no badge
- **WHEN** a comment is written by a user with `communityRole=NORMAL`
- **THEN** no role badge SHALL be displayed

### Requirement: Role badge tooltip with role description
The system SHALL display a Popover with role description when user hovers over a community role badge.

#### Scenario: Hover over creator badge
- **WHEN** user hovers over a "创作者" role badge
- **THEN** the system SHALL display a Popover with the role description

#### Scenario: Hover over moderator badge
- **WHEN** user hovers over a "版主" role badge
- **THEN** the system SHALL display a Popover with text explaining moderator responsibilities

### Requirement: CommunityRoleBadge component
The system SHALL create a reusable `CommunityRoleBadge.vue` component at `src/views/content/components/CommunityRoleBadge.vue` that accepts `role` and `verified` props.

#### Scenario: Component renders based on role prop
- **WHEN** the component receives `role='CREATOR'` and `verified=true`
- **THEN** it SHALL render a blue Tag with "创作者" text and verification icon

#### Scenario: Component does not render for normal users
- **WHEN** the component receives `role='NORMAL'`
- **THEN** it SHALL render nothing

### Requirement: Role data from comment API
The system SHALL use the `communityRole` field from the comment list API response to render role badges without additional API requests.

#### Scenario: Comment list includes role data
- **WHEN** the comment list API returns comment data
- **THEN** each comment object SHALL include a `communityRole` field that the frontend uses directly for badge rendering
