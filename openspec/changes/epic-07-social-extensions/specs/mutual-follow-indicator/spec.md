## ADDED Requirements

### Requirement: 互关状态判定
系统 SHALL 提供判定两个用户是否互相关注的能力。互关的定义是：A 关注了 B 且 B 也关注了 A。

#### Scenario: 双方互相关注
- GIVEN: 用户 A 关注了用户 B，且用户 B 关注了用户 A
- WHEN: 调用互关判定方法检查 A 与 B 的关系
- THEN: 返回互关状态为 true

#### Scenario: 仅单方关注
- GIVEN: 用户 A 关注了用户 B，但用户 B 未关注用户 A
- WHEN: 调用互关判定方法检查 A 与 B 的关系
- THEN: 返回互关状态为 false

#### Scenario: 双方均未关注
- GIVEN: 用户 A 和用户 B 之间不存在任何关注关系
- WHEN: 调用互关判定方法检查 A 与 B 的关系
- THEN: 返回互关状态为 false

### Requirement: 关系查询返回互关标识
系统 SHALL 在查询用户关系时，返回的 VO 中 MUST 包含 mutualFollow 布尔字段。

#### Scenario: 查询互关关系
- GIVEN: 用户 A 与用户 B 互关
- WHEN: 用户 A 调用 /content/user/relation/detail 查询与 B 的关系
- THEN: 返回的 VO 中 mutualFollow 字段为 true

#### Scenario: 查询单方关注
- GIVEN: 用户 A 关注了用户 B，但 B 未回关
- WHEN: 用户 A 调用 /content/user/relation/detail 查询与 B 的关系
- THEN: 返回的 VO 中 mutualFollow 字段为 false

### Requirement: 查询互关好友列表
系统 SHALL 提供查询某用户所有互关好友列表的能力，按最近互关时间倒序排列，支持分页。

#### Scenario: 查询互关好友列表
- GIVEN: 用户 A 与 5 个用户互关
- WHEN: 调用查询互关好友列表接口
- THEN: 返回 5 个互关用户的信息，按最近互关时间倒序排列

#### Scenario: 分页查询互关好友
- GIVEN: 用户 A 与 50 个用户互关
- WHEN: 调用查询接口，分页参数 pageSize=20, pageNo=1
- THEN: 返回第一页 20 个互关好友，pageNo=2 返回剩余的 30 个

#### Scenario: 无互关好友
- GIVEN: 用户 A 没有任何互关好友
- WHEN: 调用查询互关好友列表接口
- THEN: 返回空列表，totalCount 为 0
