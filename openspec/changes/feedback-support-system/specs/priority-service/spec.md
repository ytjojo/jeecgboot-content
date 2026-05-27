## MODIFIED Requirements

### Requirement: 高等级用户优先客服
系统 SHALL 为高等级用户（LV.15+）在人工客服队列中提供优先排队权。

#### Scenario: 高等级用户优先排队
- GIVEN: 用户等级 >= 15
- WHEN: 用户进入人工客服队列
- THEN: 系统将用户排在优先位置，预计等待时间 <2 分钟

#### Scenario: 普通用户正常排队
- GIVEN: 用户等级 < 15
- WHEN: 用户进入人工客服队列
- THEN: 系统按正常顺序排队，预计等待时间 <5 分钟

### Requirement: 治理状态用户优先通道
系统 SHALL 为被冻结/封禁的用户提供治理申诉专线通道。

#### Scenario: 冻结用户进入优先通道
- GIVEN: 用户状态为冻结或封禁
- WHEN: 用户进入客服入口
- THEN: 系统将用户路由到治理申诉专线通道
