## ADDED Requirements

### Requirement: 仅互关可见的内容权限判定
系统 SHALL 在内容查询时，对可见性为 MUTUAL_ONLY 的内容执行互关关系校验。非互关用户查询此类内容时，系统 MUST 返回权限拒绝。

#### Scenario: 互关好友查看私密内容
- GIVEN: 用户 A 发布了可见性为 MUTUAL_ONLY 的内容，且用户 B 与 A 互关
- WHEN: 用户 B 查询该内容
- THEN: 正常返回内容详情

#### Scenario: 非互关用户查看私密内容
- GIVEN: 用户 A 发布了可见性为 MUTUAL_ONLY 的内容
- WHEN: 与 A 非互关的用户 C 查询该内容
- THEN: 返回权限拒绝，提示信息为"该内容仅对互关好友可见"

#### Scenario: 取消互关后查看私密内容
- GIVEN: 用户 A 发布了 MUTUAL_ONLY 内容，用户 B 原与 A 互关
- WHEN: A 取消对 B 的关注后，B 查询该内容
- THEN: 返回权限拒绝，B 不再能看到该私密内容

### Requirement: 私密内容不进入公共推荐流
系统 SHALL 确保可见性为 MUTUAL_ONLY 的内容不进入公共推荐流和公开搜索结果。

#### Scenario: 私密内容不在推荐流
- GIVEN: 用户 A 发布了 MUTUAL_ONLY 的内容
- WHEN: 非互关用户浏览公共推荐流
- THEN: 推荐流中不包含该私密内容

#### Scenario: 私密内容不在公开搜索结果
- GIVEN: 用户 A 发布了 MUTUAL_ONLY 的内容
- WHEN: 非互关用户通过关键词搜索
- THEN: 搜索结果中不包含该私密内容

### Requirement: 修改私密内容可见性
系统 SHALL 允许内容发布者修改已发布内容的可见性。

#### Scenario: 私密内容改为公开
- GIVEN: 用户 A 已发布 MUTUAL_ONLY 的内容
- WHEN: A 将该内容的可见性修改为 PUBLIC 并保存
- THEN: 系统更新可见性，所有用户均可查看该内容

#### Scenario: 公开内容改为仅互关可见
- GIVEN: 用户 A 已发布 PUBLIC 的内容
- WHEN: A 将该内容的可见性修改为 MUTUAL_ONLY 并保存
- THEN: 系统更新可见性，仅互关用户可查看该内容
