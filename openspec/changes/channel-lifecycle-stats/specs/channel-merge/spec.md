## ADDED Requirements

### Requirement: 频道合并申请

系统 SHALL 支持频道主发起频道合并申请，选择目标频道并提交审核。

#### Scenario: 个人频道主发起合并
- **WHEN** 个人频道主发起合并申请并选择目标频道
- **THEN** 系统展示源频道内容、订阅者、历史链接和可见性影响范围，并进入审核流程

#### Scenario: 组织频道合并需审批
- **WHEN** 组织频道发起合并申请
- **THEN** 需组织最高管理员审批后才能进入平台审核

#### Scenario: 不可合并状态拦截
- **WHEN** 源频道或目标频道处于 Deleted、Closed、Hidden 或 PendingReview 状态
- **THEN** 系统拒绝并提示当前状态不可合并

### Requirement: 频道合并执行

系统 SHALL 在合并审核通过后执行数据迁移，包括内容、订阅关系和历史链接。

#### Scenario: 合并执行
- **WHEN** 合并审核通过且系统执行合并
- **THEN** 源频道的内容和订阅关系迁移至目标频道，并通知相关订阅者

#### Scenario: 合并后源频道状态
- **WHEN** 合并完成
- **THEN** 源频道状态变为 Merged，并展示目标频道入口

#### Scenario: 合并审计日志
- **WHEN** 合并完成
- **THEN** 审计日志记录源频道、目标频道、申请人、审批人、完成时间和影响范围
