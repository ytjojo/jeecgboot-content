## ADDED Requirements

### Requirement: 智能客服入口
系统 SHALL 在用户点击"客服"入口时首先启动智能客服机器人。

#### Scenario: 启动智能客服
- GIVEN: 用户需要帮助
- WHEN: 用户点击"客服"入口
- THEN: 系统启动智能客服机器人对话界面

### Requirement: 转人工客服
系统 SHALL 支持用户在智能客服无法解决问题时转接人工客服。

#### Scenario: 请求转人工
- GIVEN: 智能客服无法解决用户问题
- WHEN: 用户点击"转人工"
- THEN: 系统将用户加入人工客服队列

### Requirement: 人工客服实时对话
系统 SHALL 支持用户与人工客服进行实时对话。

#### Scenario: 客服接入对话
- GIVEN: 人工客服已接入
- WHEN: 客服回复消息
- THEN: 用户能实时收到客服回复

### Requirement: 服务评分
系统 SHALL 在客服会话结束后邀请用户对服务进行评分。

#### Scenario: 会话结束邀请评分
- GIVEN: 客服结束会话
- WHEN: 会话状态变更为已结束
- THEN: 系统邀请用户对本次服务评分
