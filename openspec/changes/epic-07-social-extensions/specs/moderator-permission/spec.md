## ADDED Requirements

### Requirement: 版主评论管理操作
系统 SHALL 允许版主查看评论区时执行删除评论、警告用户等操作。版主权限 MUST 受 RBAC 校验。

#### Scenario: 版主删除评论
- GIVEN: 用户是版主，且评论属于其管辖版块
- WHEN: 版主执行"删除评论"操作
- THEN: 评论被标记删除，生成审计日志，通知被处罚用户

#### Scenario: 版主警告用户
- GIVEN: 用户是版主
- WHEN: 版主执行"警告用户"操作并填写原因
- THEN: 生成审计日志，警告通知发送给被警告用户

#### Scenario: 非版主执行管理操作
- GIVEN: 用户不是版主或管理员
- WHEN: 尝试调用管理操作接口
- THEN: 系统返回权限拒绝

### Requirement: 管理员用户管理操作
系统 SHALL 允许管理员执行封禁、禁言、限制推荐等用户管理操作。

#### Scenario: 管理员封禁用户
- GIVEN: 用户是管理员
- WHEN: 管理员执行封禁操作并填写原因
- THEN: 用户被封禁，生成审计日志

#### Scenario: 管理员禁言用户
- GIVEN: 用户是管理员
- WHEN: 管理员执行禁言操作
- THEN: 用户在指定时间内无法发表评论，生成审计日志

### Requirement: 管理操作审计日志
系统 SHALL 为所有版主/管理员管理操作记录审计日志，包含操作人、被操作人、操作类型、原因、时间戳。

#### Scenario: 记录删除操作
- GIVEN: 版主删除了一条评论
- WHEN: 操作完成
- THEN: 审计日志记录操作人 ID、被操作人 ID、操作类型=DELETE_COMMENT、原因、时间戳

#### Scenario: 查询审计日志
- GIVEN: 存在多条审计日志
- WHEN: 管理员查询审计日志列表
- THEN: 按时间倒序返回操作记录

### Requirement: 撤销处罚
系统 SHALL 允许版主/管理员撤销已执行的处罚操作，并记录撤销日志。

#### Scenario: 版主撤销删除操作
- GIVEN: 版主之前删除了某评论
- WHEN: 版主选择"撤销"操作并填写原因
- THEN: 评论恢复，生成撤销审计日志

#### Scenario: 管理员撤销封禁
- GIVEN: 管理员之前封禁了某用户
- WHEN: 管理员执行撤销封禁操作
- THEN: 用户恢复正常状态，生成撤销审计日志
