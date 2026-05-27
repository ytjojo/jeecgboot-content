## ADDED Requirements

### Requirement: 申诉次数限制
系统 SHALL 限制同一用户对同一事件最多申诉 3 次。

#### Scenario: 第 1 次申诉
- GIVEN: 用户首次对某处罚发起申诉
- WHEN: 用户提交申诉
- THEN: 系统创建申诉记录

#### Scenario: 第 2 次申诉
- GIVEN: 用户已对某处罚申诉过 1 次
- WHEN: 用户再次提交申诉
- THEN: 系统创建申诉记录

#### Scenario: 第 3 次申诉
- GIVEN: 用户已对某处罚申诉过 2 次
- WHEN: 用户再次提交申诉
- THEN: 系统创建申诉记录

#### Scenario: 超过次数限制
- GIVEN: 用户已对某处罚申诉过 3 次
- WHEN: 用户再次尝试申诉
- THEN: 系统提示"您已申诉多次，请耐心等待最终结果"

### Requirement: 申诉审批与处罚撤销
系统 SHALL 在申诉审批通过后自动撤销处罚并恢复用户正常状态。

#### Scenario: 申诉通过恢复状态
- GIVEN: 审核团队批准申诉
- WHEN: 系统执行处罚撤销
- THEN: 系统恢复用户正常状态并通知用户

### Requirement: 申诉审计日志
系统 SHALL 为所有申诉操作生成不可篡改的审计日志。

#### Scenario: 记录申诉创建
- GIVEN: 用户提交申诉
- WHEN: 申诉记录创建成功
- THEN: 系统生成审计日志（申诉人、时间、操作类型）

#### Scenario: 记录申诉处理
- GIVEN: 审核团队处理申诉
- WHEN: 申诉状态变更
- THEN: 系统生成审计日志（审核人、时间、结果、原因）
