## ADDED Requirements

### Requirement: 客服会话历史列表
系统 SHALL 提供"客服记录"页面，按时间倒序展示历史会话列表。

#### Scenario: 查看会话历史
- GIVEN: 用户有历史客服会话
- WHEN: 用户进入"客服记录"
- THEN: 系统显示按时间倒序排列的会话列表

### Requirement: 会话详情查看
系统 SHALL 支持用户查看单次会话的完整对话记录。

#### Scenario: 查看会话详情
- GIVEN: 用户点击某次会话记录
- WHEN: 页面加载完成
- THEN: 系统显示完整的对话记录

### Requirement: 继续咨询
系统 SHALL 支持用户基于历史会话创建新会话。

#### Scenario: 继续之前的咨询
- GIVEN: 用户查看历史会话
- WHEN: 用户点击"继续咨询"
- THEN: 系统创建新会话并关联历史记录

### Requirement: 会话保留期限
系统 SHALL 保留 30 天内的会话记录，超过期限提示用户。

#### Scenario: 查看超过 30 天的会话
- GIVEN: 会话记录超过 30 天
- WHEN: 用户尝试查看
- THEN: 系统提示"历史记录仅保留 30 天"
