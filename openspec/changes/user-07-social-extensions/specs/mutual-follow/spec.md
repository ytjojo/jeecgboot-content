## ADDED Requirements

### Requirement: Mutual follow status display
系统 SHALL 在用户主页和个人资料接口中返回互关状态标识。当两个用户互相均为 `followed=true` 时，系统 SHALL 将关系标记为"互关"。

#### Scenario: Mutual follow badge on user profile
- GIVEN: 用户 A 关注了用户 B，用户 B 也关注了用户 A
- WHEN: 用户 A 查看用户 B 的主页
- THEN: 系统返回 `mutualFollow=true` 标识，前端显示"互关"或"好友"徽章

#### Scenario: One-way follow shows no mutual badge
- GIVEN: 用户 A 关注了用户 B，但用户 B 未关注用户 A
- WHEN: 用户 A 查看用户 B 的主页
- THEN: 系统返回 `mutualFollow=false`，前端仅显示"已关注"按钮

#### Scenario: Mutual follow badge in comment area
- GIVEN: 用户 A 与评论者用户 B 互相关注
- WHEN: 用户 A 浏览评论区看到用户 B 的评论
- THEN: 评论中用户 B 的昵称旁显示互关标识

#### Scenario: Mutual follow removed after unfollow
- GIVEN: 用户 A 与用户 B 互相关注
- WHEN: 用户 B 取消关注用户 A
- THEN: 用户 A 刷新页面后，用户 B 主页不再显示互关标识，仅显示"关注"按钮

### Requirement: Mutual follow friends list
系统 SHALL 提供互关好友列表查询接口，返回所有与当前用户互相关注的用户列表，支持分页。

#### Scenario: View mutual follow list
- GIVEN: 用户 A 有多个互关好友
- WHEN: 用户 A 请求互关好友列表
- THEN: 系统返回所有与用户 A 互相关注的用户列表，按最近互动时间倒序排列，支持分页

#### Scenario: Empty mutual follow list
- GIVEN: 用户 A 没有互关好友
- WHEN: 用户 A 请求互关好友列表
- THEN: 系统返回空列表

### Requirement: Private content visibility for mutual follows only
系统 SHALL 支持"仅互关可见"的内容可见性范围。当内容设置为仅互关可见时，仅互关用户可查看该内容。

#### Scenario: Publish content with mutual-follow-only visibility
- GIVEN: 用户 A 想发布私密内容
- WHEN: 用户 A 选择可见性为"仅互关可见"并发布
- THEN: 系统保存内容并标记 `visibility=MUTUAL_FOLLOW_ONLY`

#### Scenario: Mutual follow user can see private content
- GIVEN: 用户 A 发布了仅互关可见的内容，用户 B 与用户 A 互相关注
- WHEN: 用户 B 浏览用户 A 的内容列表
- THEN: 用户 B 能看到该私密内容

#### Scenario: Non-mutual follow user cannot see private content
- GIVEN: 用户 A 发布了仅互关可见的内容，用户 C 未与用户 A 互关
- WHEN: 用户 C 尝试查看该内容
- THEN: 系统返回"该内容仅对互关好友可见"提示，不返回内容详情

#### Scenario: Private content excluded from public feed
- GIVEN: 用户 A 发布了仅互关可见的内容
- WHEN: 非互关用户浏览公共推荐流
- THEN: 该内容不出现在推荐结果中

#### Scenario: Visibility change after unfollow
- GIVEN: 用户 A 发布了仅互关可见的内容，用户 B 原本与用户 A 互关
- WHEN: 用户 B 取消关注用户 A（不再互关）
- THEN: 用户 B 刷新后不再看到该私密内容
