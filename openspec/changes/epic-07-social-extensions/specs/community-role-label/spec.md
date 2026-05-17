## ADDED Requirements

### Requirement: 角色标签展示
系统 SHALL 在评论区为每用户展示其角色标签。角色标签包含：普通用户、创作者、版主、管理员。

#### Scenario: 普通用户评论
- GIVEN: 普通用户发表评论
- WHEN: 查看评论区
- THEN: 该用户昵称旁显示"用户"标签或无特殊标签

#### Scenario: 创作者评论
- GIVEN: 认证创作者发表评论
- WHEN: 查看评论区
- THEN: 该用户昵称旁显示"创作者"标签及认证标识

#### Scenario: 版主评论
- GIVEN: 版主发表评论
- WHEN: 查看评论区
- THEN: 该用户昵称旁显示"版主"标签

#### Scenario: 管理员评论
- GIVEN: 管理员发表评论
- WHEN: 查看评论区
- THEN: 该用户昵称旁显示"管理员"标签

### Requirement: 角色信息查询
系统 SHALL 允许用户点击角色标签查看该角色的说明信息。

#### Scenario: 点击角色标签
- GIVEN: 用户查看到某"版主"标签
- WHEN: 点击该角色标签
- THEN: 显示"版主负责维护社区秩序"等角色说明

### Requirement: 角色标签批量查询
系统 SHALL 在批量查询评论时，通过一次 JOIN 获取所有评论用户的角色标签，避免 N+1 查询。

#### Scenario: 批量查询评论角色
- GIVEN: 某评论下有 50 条评论，来自 30 个不同用户
- WHEN: 查询评论列表及角色标签
- THEN: 通过一次 JOIN 查询获取所有 30 个用户的角色信息
