## ADDED Requirements

### Requirement: Generate invite code and share link
系统 SHALL 为每个注册用户生成唯一的邀请码和分享链接。邀请码为 8 位字母数字组合，分享链接格式为 `{baseUrl}/invite/{code}`。

#### Scenario: First time generating invite code
- GIVEN: 用户 A 首次进入邀请好友页面
- WHEN: 系统检测到用户 A 无邀请码
- THEN: 系统自动生成唯一邀请码并返回邀请码和分享链接

#### Scenario: Repeatedly viewing invite page
- GIVEN: 用户 A 已有邀请码
- WHEN: 用户 A 再次进入邀请好友页面
- THEN: 系统返回已有的邀请码和分享链接，不重复生成

#### Scenario: Invite code uniqueness
- GIVEN: 系统已有 100 万个邀请码
- WHEN: 生成新邀请码
- THEN: 新邀请码与已有邀请码不重复（数据库唯一索引保证）

### Requirement: Share invite link
系统 SHALL 支持用户复制邀请链接到剪贴板。系统 SHALL 提供分享渠道参数，前端可对接微信/QQ/微博分享 SDK。

#### Scenario: Copy invite link
- GIVEN: 用户 A 在邀请页面
- WHEN: 用户 A 点击"复制链接"
- THEN: 邀请链接复制到剪贴板，页面提示"已复制"

### Requirement: Invite relationship binding and reward
当被邀请用户通过邀请码/链接完成注册时，系统 SHALL 记录邀请关系并自动发放积分奖励。奖励规则通过 `content_user_reward_rule` 表配置，规则代码为 `INVITE_REGISTER`。

#### Scenario: Invitee registers successfully
- GIVEN: 用户 A 的邀请码为 `ABC12345`，用户 B 通过该邀请链接注册
- WHEN: 用户 B 完成注册
- THEN: 系统在邀请记录表中创建记录（inviterUserId=A, inviteeUserId=B, code=ABC12345），并为用户 A 发放积分奖励（通过 RewardEvent）

#### Scenario: Duplicate invite prevention
- GIVEN: 用户 B 已通过用户 A 的邀请码注册
- WHEN: 用户 B 再次使用同一邀请码
- THEN: 系统拒绝并提示"该用户已通过邀请注册"

#### Scenario: Self-invite prevention
- GIVEN: 用户 A 的邀请码为 `ABC12345`
- WHEN: 用户 A 尝试使用自己的邀请码注册新账号
- THEN: 系统拒绝并提示"不能使用自己的邀请码"

#### Scenario: Daily invite reward cap
- GIVEN: 用户 A 今日已通过邀请获得达到上限的积分
- WHEN: 又有新用户通过用户 A 的邀请码注册
- THEN: 记录邀请关系，但积分奖励跳过（dailyBucket 达上限），rewardEvent 标记 `skipReason=DAILY_CAP`

### Requirement: Invite record and statistics
系统 SHALL 提供邀请记录列表和收益统计查询。邀请记录包含被邀请用户信息、注册时间和获得奖励。

#### Scenario: View invite records
- GIVEN: 用户 A 成功邀请了 10 个好友
- WHEN: 用户 A 请求邀请记录列表
- THEN: 系统返回 10 条邀请记录，包含被邀请者昵称、注册时间、获得积分，按注册时间倒序排列

#### Scenario: View invite statistics
- GIVEN: 用户 A 有邀请记录
- WHEN: 用户 A 请求邀请统计
- THEN: 系统返回累计邀请人数、成功注册数、总获得积分
