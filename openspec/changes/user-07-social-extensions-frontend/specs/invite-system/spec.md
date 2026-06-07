## ADDED Requirements

### Requirement: Invite code generation and display
The system SHALL provide an invite share page at `/content/invite` that displays the user's invite code, shareable link, and QR code.

#### Scenario: First-time invite code generation
- **WHEN** user visits the invite page for the first time
- **THEN** the system SHALL auto-generate an invite code via `POST /content/user/invite/generate` and display it prominently with a copyable link and QR code

#### Scenario: Reuse existing invite code
- **WHEN** user visits the invite page and already has an invite code
- **THEN** the system SHALL display the existing invite code without regenerating

#### Scenario: Copy invite link
- **WHEN** user clicks the "复制链接" button
- **THEN** the system SHALL copy the invite link to the clipboard and change button text to "已复制" for 2 seconds

#### Scenario: Copy failure fallback
- **WHEN** clipboard copy fails
- **THEN** the system SHALL display "复制失败，请手动复制" and show the link as selectable text

### Requirement: Invite statistics display
The system SHALL display invite statistics on the invite page including total invites, successful registrations, and total reward points.

#### Scenario: View invite statistics
- **WHEN** user visits the invite page
- **THEN** the system SHALL display CountTo animated numbers for total invite count, successful registration count, and total reward points via `GET /content/user/invite/stats`

### Requirement: Invite records list
The system SHALL display a paginated table of invite records on the invite page.

#### Scenario: View invite records
- **WHEN** user visits the invite page
- **THEN** the system SHALL display a Table with columns: invitee nickname, registration time, reward points, sorted by registration time descending, with pagination

#### Scenario: No invite records
- **WHEN** user has no invite records
- **THEN** the system SHALL display "还没有邀请记录，快分享给好友吧"

### Requirement: Invite landing page
The system SHALL provide an invite landing page at `/invite/:inviteCode` for invited users, with inviter info, platform highlights, and registration guidance.

#### Scenario: Valid invite code landing page
- **WHEN** a new user visits `/invite/:inviteCode` with a valid, non-expired invite code
- **THEN** the system SHALL display the inviter's avatar and nickname, platform highlights, a "立即注册" button, and registration reward info
- **NOTE**: 需后端补充邀请码校验接口，详见 backend-issues.md

#### Scenario: Expired invite code
- **WHEN** a user visits `/invite/:inviteCode` with an expired invite code
- **THEN** the system SHALL display "邀请链接已过期，请联系邀请人获取新链接" with a direct registration option

#### Scenario: Max reached invite code
- **WHEN** a user visits `/invite/:inviteCode` where the inviter has reached the invite limit
- **THEN** the system SHALL display "该邀请人邀请名额已满" with a direct registration option

#### Scenario: Invalid invite code
- **WHEN** a user visits `/invite/:inviteCode` with an invalid invite code
- **THEN** the system SHALL display "邀请链接已失效"

#### Scenario: Already logged-in user visits invite link
- **WHEN** a logged-in user visits `/invite/:inviteCode`
- **THEN** the system SHALL redirect to the home page without showing the landing page

#### Scenario: Register from landing page
- **WHEN** a new user clicks "立即注册" on the landing page
- **THEN** the system SHALL navigate to the registration page with `inviteCode` parameter automatically carried in the URL

### Requirement: Invite landing page responsive design
The landing page SHALL support mobile-first responsive design with fixed bottom registration button on mobile.

#### Scenario: Mobile landing page layout
- **WHEN** the landing page is viewed on a mobile device (viewport < 576px)
- **THEN** the layout SHALL stack vertically with the registration button fixed at the bottom of the screen

### Requirement: Invite API integration
The system SHALL integrate invite APIs using `defHttp` encapsulation in `src/api/content/invite.ts`.

#### Scenario: Fetch invite code
- **WHEN** the invite page loads
- **THEN** the system SHALL call `POST /content/user/invite/generate` to get or generate the invite code

#### Scenario: Fetch invite info for landing page
- **WHEN** the landing page loads with an invite code
- **THEN** the system SHALL call the invite info API to get inviter info and code validity
- **NOTE**: 后端需补充邀请码校验接口，详见 backend-issues.md

#### Scenario: Fetch invite records
- **WHEN** the invite page loads
- **THEN** the system SHALL call `GET /content/user/invite/records` with pagination params

#### Scenario: Fetch invite statistics
- **WHEN** the invite page loads
- **THEN** the system SHALL call `GET /content/user/invite/stats` to get aggregate statistics

### Requirement: Invite store for caching
The system SHALL create a Pinia store (`useInviteStore`) to cache invite code and statistics within the session.

#### Scenario: Cache invite code
- **WHEN** the invite code is fetched for the first time
- **THEN** the system SHALL store it in `useInviteStore` and reuse it on subsequent visits within the session

## API 封装

API 文件: `src/api/content/invite.ts`

| 端点 | 方法 | 参数 | 响应关键字段 | 状态 |
|------|------|------|------------|------|
| `/content/user/invite/generate` | POST | 无参数 | inviteCode, inviteLink | ✅ 后端已实现 |
| `/content/user/invite/info/{inviteCode}` | GET | @PathVariable: inviteCode | inviterNickname, inviterAvatar, inviteCode, expired, maxReached, registerRewardPoints | ❌ 后端待补充 |
| `/content/user/invite/records` | GET | @RequestParam: page, pageSize | records[{inviteeNickname, registerTime, rewardPoints}], total | ✅ 后端已实现 |
| `/content/user/invite/stats` | GET | 无参数 | totalInviteCount, successRegisterCount, totalRewardPoints | ✅ 后端已实现 |
