## ADDED Requirements

### Requirement: Mutual follow badge display
The system SHALL display a "互关" (mutual follow) badge next to user names when both users follow each other. The badge SHALL appear in the user profile page and comment sections.

#### Scenario: Mutual follow users see badge in comments
- **WHEN** user A views a comment written by user B, and A and B mutually follow each other
- **THEN** a green "互关" Tag badge SHALL appear next to user B's nickname in the comment

#### Scenario: Non-mutual follow users see no badge
- **WHEN** user A views a comment written by user B, and A follows B but B does not follow A
- **THEN** no mutual follow badge SHALL appear next to user B's nickname

#### Scenario: Badge in user profile page
- **WHEN** user A visits user B's profile page, and A and B mutually follow each other
- **THEN** a "互关" badge SHALL appear next to user B's nickname on the profile page

### Requirement: Mutual follow friend list page
The system SHALL provide a mutual follow friend list page at `/content/mutual-follow` with pagination, search, and unfollow capabilities.

#### Scenario: View mutual follow list
- **WHEN** user navigates to the mutual follow friend list page
- **THEN** the system SHALL display a paginated list of mutual follow friends sorted by recent interaction time (descending), showing avatar, nickname, mutual follow badge, mutual follow time, and action buttons

#### Scenario: Search mutual follow friends
- **WHEN** user types a keyword in the search input
- **THEN** the system SHALL filter the list by nickname or username with 300ms debounce, and restore the full list when input is cleared
  - **验收标准**: 防抖延迟 300ms ± 50ms；列表刷新响应 < 1s

#### Scenario: Unfollow a mutual friend
- **WHEN** user clicks "取消关注" button on a mutual friend and confirms in the confirmation dialog
- **THEN** the system SHALL remove the friend from the list and display a success message "已取消互关"

#### Scenario: Empty state
- **WHEN** user has no mutual follow friends
- **THEN** the system SHALL display an empty state illustration with text "还没有互关好友，去发现更多有趣的人吧" and a guided button

### Requirement: Mutual follow status caching
The system SHALL cache mutual follow status in a Pinia store (`useMutualFollowStore`) to avoid redundant API requests for incremental comment loading scenarios.

#### Scenario: Comment list with built-in mutual follow field
- **WHEN** the comment list API returns data with `mutualFollow` field
- **THEN** the system SHALL use the field directly without additional API requests

#### Scenario: Incremental comment loading cache
- **WHEN** comments are loaded incrementally (dynamically) and mutual status is fetched via batch API
- **THEN** the system SHALL cache results in `useMutualFollowStore` and reuse them within the session

#### Scenario: Cache invalidation on follow/unfollow
- **WHEN** user follows or unfollows another user
- **THEN** the system SHALL clear the corresponding cache entry in `useMutualFollowStore`

### Requirement: Mutual follow status API
The system SHALL provide API endpoints for mutual follow list and mutual status query using `defHttp` encapsulation.

#### Scenario: Fetch mutual follow list
- **WHEN** the mutual follow list page loads
- **THEN** the system SHALL call `GET /api/v1/content/user/relation/mutual-follow-list` with pagination params (page, pageSize, keyword)

#### Scenario: Batch query mutual status
- **WHEN** incremental comment loading requires mutual status for a list of userIds
- **THEN** the system SHALL call the mutual status batch query API with userId list and cache results in `useMutualFollowStore`
- **NOTE**: 后端需补充 `GET /api/v1/content/user/relation/mutual-status` 端点，详见 backend-issues.md

## API 封装

API 文件: `src/api/content/mutual-follow.ts`（如已有 relation 相关封装则复用）

| 端点 | 方法 | 参数 | 响应关键字段 | 状态 |
|------|------|------|------------|------|
| `/api/v1/content/user/relation/mutual-follow-list` | GET | @RequestParam: page, pageSize, keyword? | records[{userId, nickname, avatar, mutualFollowTime}], total | ✅ 后端已实现 |
| `/api/v1/content/user/relation/mutual-status` | GET | @RequestParam: userIds (逗号分隔) | Map<userId, boolean> | ❌ 后端待补充 |
