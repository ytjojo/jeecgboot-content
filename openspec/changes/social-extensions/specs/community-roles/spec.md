## ADDED Requirements

### Requirement: Community role badge display in comments
系统 SHALL 在评论区展示用户的社区角色标签。角色类型包括：普通用户（无标签或显示"用户"）、创作者（带认证标识）、版主、管理员。角色信息从 `content_user_profile.community_role` 字段读取。

#### Scenario: Normal user comment
- GIVEN: 用户 A 的 communityRole 为 NORMAL
- WHEN: 用户 A 发表评论
- THEN: 评论中用户 A 昵称旁无特殊标签

#### Scenario: Creator comment with badge
- GIVEN: 用户 B 的 communityRole 为 CREATOR，已通过认证
- WHEN: 用户 B 发表评论
- THEN: 评论中用户 B 昵称旁显示"创作者"标签及认证标识

#### Scenario: Moderator comment with badge
- GIVEN: 用户 C 的 communityRole 为 MODERATOR
- WHEN: 用户 C 发表评论
- THEN: 评论中用户 C 昵称旁显示"版主"标签

#### Scenario: Admin comment with badge
- GIVEN: 用户 D 的 communityRole 为 ADMIN
- WHEN: 用户 D 发表评论
- THEN: 评论中用户 D 昵称旁显示"管理员"标签

#### Scenario: Click role badge shows description
- GIVEN: 评论区显示了用户的角色标签
- WHEN: 其他用户点击该角色标签
- THEN: 系统显示该角色的说明信息（如"版主负责维护社区秩序"）

### Requirement: Moderator management permissions
系统 SHALL 为版主角色用户提供评论管理权限，包括删除评论和警告用户。所有管理操作 SHALL 记录审计日志。

#### Scenario: Moderator deletes comment
- GIVEN: 用户 C 是版主，看到违规评论
- WHEN: 用户 C 点击"删除评论"并确认
- THEN: 系统删除该评论，记录审计日志（操作人、时间、原因、操作类型=DELETE_COMMENT），通知被处罚用户

#### Scenario: Moderator warns user
- GIVEN: 用户 C 是版主
- WHEN: 用户 C 对违规用户发出警告
- THEN: 系统记录警告日志，通知被警告用户

#### Scenario: Admin bans user
- GIVEN: 用户 D 是管理员
- WHEN: 用户 D 执行封禁操作（选择封禁原因和时长）
- THEN: 系统调用治理服务 `changeStatus(BANNED)`，生成审计日志

#### Scenario: Admin mutes user
- GIVEN: 用户 D 是管理员
- WHEN: 用户 D 执行禁言操作
- THEN: 系统调用治理服务 `changeStatus(MUTED)`，生成审计日志

#### Scenario: Revoke punishment
- GIVEN: 用户 D 是管理员，某用户处于被封禁状态
- WHEN: 用户 D 选择"撤销"并填写原因
- THEN: 系统恢复用户为 NORMAL 状态，记录撤销日志

#### Scenario: Non-moderator cannot access management actions
- GIVEN: 用户 A 是普通用户
- WHEN: 用户 A 尝试调用版主管理接口
- THEN: 系统返回 HTTP 403，拒绝访问

### Requirement: Audit log for all management operations
系统 SHALL 为所有版主/管理员管理操作生成审计日志，包含操作人、操作时间、操作类型、目标用户、原因。审计日志 SHALL 不可删除，仅管理员可查看。

#### Scenario: View audit log
- GIVEN: 管理员用户 D
- WHEN: 用户 D 请求审计日志列表
- THEN: 系统返回所有管理操作的分页日志，按时间倒序排列

#### Scenario: Non-admin cannot view audit log
- GIVEN: 用户 A 是普通用户
- WHEN: 用户 A 尝试访问审计日志接口
- THEN: 系统返回 HTTP 403
