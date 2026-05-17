## ADDED Requirements

### Requirement: User can set visibility for activity items
The system SHALL allow users to set visibility levels for their activity items (browsing history, like activity, favorites) with options: public, followers-only, and private (self-only).

#### Scenario: Hide browsing history
- GIVEN: 用户想隐藏浏览记录
- WHEN: 用户将"浏览记录"设置为"仅自己可见"
- THEN: 其他用户无法在用户主页看到其浏览历史

#### Scenario: Like activity visible to followers only
- GIVEN: 用户想让关注者看到其点赞动态
- WHEN: 用户将"点赞动态"设置为"仅关注者可见"
- THEN: 只有关注该用户的人能看到其点赞了哪些内容

#### Scenario: Public favorites
- GIVEN: 用户想公开其收藏夹
- WHEN: 用户将"收藏夹"设置为"公开"
- THEN: 任何用户都能查看该用户的收藏夹

#### Scenario: Self-visible after privacy setting
- GIVEN: 用户将某项设置为"仅自己可见"
- WHEN: 用户查看自己的主页
- THEN: 用户仍能看到该内容（对自己始终可见）

### Requirement: Visibility changes propagate within cache TTL
The system SHALL reflect visibility setting changes for new requests within 5 minutes of the change.

#### Scenario: Visibility change propagation
- GIVEN: 用户修改了可见性设置
- WHEN: 其他用户刷新页面
- THEN: 他们在 5 分钟内看到更新后的可见性效果

### Requirement: User can set online status visibility
The system SHALL allow users to control whether their online status is visible to others, with options: public, mutual-followers-only, and hidden.

#### Scenario: Hide online status
- GIVEN: 用户想隐藏在线状态
- WHEN: 用户将"在线状态"设置为"隐藏"
- THEN: 其他用户无法看到该用户是否在线

#### Scenario: Asymmetric visibility for hidden status
- GIVEN: 用户设置为隐藏在线状态
- WHEN: 用户实际在线
- THEN: 系统对该用户显示"在线"，但对其他人显示"离线"或隐藏

#### Scenario: Mutual followers only visibility
- GIVEN: 用户想仅对互关好友显示在线状态
- WHEN: 用户设置为"仅互关可见"
- THEN: 只有与该用户互关的用户能看到其在线状态

#### Scenario: Public online status
- GIVEN: 用户设置为公开在线状态
- WHEN: 用户在线
- THEN: 所有用户都能看到该用户的在线标识

### Requirement: User can control search engine indexing
The system SHALL allow users to enable or disable search engine indexing of their profile page.

#### Scenario: Disable search engine indexing
- GIVEN: 用户不想被搜索引擎收录
- WHEN: 用户将"允许搜索引擎索引"设置为"否"
- THEN: 系统在用户个人主页添加 noindex meta 标签

#### Scenario: Search engine crawler receives noindex
- GIVEN: 用户禁止搜索引擎索引
- WHEN: 搜索引擎爬虫访问用户主页
- THEN: 系统返回 noindex 指令，搜索引擎不收录该页面

#### Scenario: Enable search engine indexing
- GIVEN: 用户想被搜索引擎收录以提升曝光
- WHEN: 用户设置为"允许"
- THEN: 系统移除 noindex 标签，允许搜索引擎抓取

#### Scenario: Indexing change takes effect on next crawl
- GIVEN: 用户修改了索引设置
- WHEN: 保存成功
- THEN: 新设置在下次爬虫访问时生效
