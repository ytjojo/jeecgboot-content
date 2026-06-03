## ADDED Requirements

### Requirement: Moderator delete comment action
The system SHALL provide a "删除评论" (delete comment) action in the comment operation menu for users with moderator or admin role.

#### Scenario: Moderator sees delete comment option
- **WHEN** a user with moderator role opens the comment operation menu
- **THEN** the system SHALL display "删除评论" option in addition to normal user actions (like, reply, report)

#### Scenario: Delete comment confirmation
- **WHEN** moderator clicks "删除评论"
- **THEN** the system SHALL display a Modal with a textarea for deletion reason (required) and a confirm button

#### Scenario: Successful comment deletion
- **WHEN** moderator submits the deletion form with a valid reason
- **THEN** the system SHALL call `POST /content/user-governance/delete-comment` with commentId and reason, display success message, and refresh the comment list

#### Scenario: Delete comment failure
- **WHEN** the delete comment API returns an error
- **THEN** the system SHALL display an error message and keep the Modal open for retry

### Requirement: Moderator warn user action
The system SHALL provide a "警告用户" (warn user) action in the comment operation menu for users with moderator or admin role.

#### Scenario: Moderator sees warn user option
- **WHEN** a user with moderator role opens the comment operation menu
- **THEN** the system SHALL display "警告用户" option

#### Scenario: Warn user confirmation
- **WHEN** moderator clicks "警告用户"
- **THEN** the system SHALL display a Modal with a textarea for warning reason (required) and a confirm button

#### Scenario: Successful user warning
- **WHEN** moderator submits the warning form with a valid reason
- **THEN** the system SHALL call `POST /content/user-governance/warn-user` with userId and reason, and display success message

### Requirement: Admin quick link to user management
The system SHALL provide a "前往用户管理" (go to user management) action for admin users that links to the EPIC-09 user governance page.

#### Scenario: Admin sees user management link
- **WHEN** a user with admin role opens the comment operation menu
- **THEN** the system SHALL display "前往用户管理" option in addition to moderator actions

#### Scenario: Navigate to user management page
- **WHEN** admin clicks "前往用户管理"
- **THEN** the system SHALL navigate to `/system/user-governance/:userId` (EPIC-09 user detail page) for the comment's author

### Requirement: ModeratorActionModal component
The system SHALL create a reusable `ModeratorActionModal.vue` component at `src/views/content/components/ModeratorActionModal.vue` that handles delete comment and warn user actions.

#### Scenario: Modal renders for delete comment action
- **WHEN** the component receives `action='deleteComment'`
- **THEN** it SHALL display a Modal with a textarea for deletion reason

#### Scenario: Modal renders for warn user action
- **WHEN** the component receives `action='warnUser'`
- **THEN** it SHALL display a Modal with a textarea for warning reason

#### Scenario: Success event emission
- **WHEN** the action is submitted successfully
- **THEN** the component SHALL emit a `success` event to trigger parent refresh

### Requirement: Permission-based button visibility
The system SHALL use `usePermission` Hook to control visibility of moderation action buttons based on user role.

#### Scenario: Normal user sees only standard actions
- **WHEN** a normal user views a comment
- **THEN** only standard actions (like, reply, report) SHALL be visible; moderation actions SHALL be hidden

#### Scenario: Moderator sees moderation actions
- **WHEN** a moderator views a comment
- **THEN** moderation actions (delete comment, warn user) SHALL be visible in the operation menu

### Requirement: Governance API integration
The system SHALL integrate governance APIs using `defHttp` encapsulation in `src/api/content/governance.ts`.

#### Scenario: Call delete comment API
- **WHEN** moderator confirms comment deletion
- **THEN** the system SHALL call `POST /content/user-governance/delete-comment` with `{ commentId, reason }`

#### Scenario: Call warn user API
- **WHEN** moderator confirms user warning
- **THEN** the system SHALL call `POST /content/user-governance/warn-user` with `{ userId, reason }`
