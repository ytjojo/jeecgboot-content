## ADDED Requirements

### Requirement: 动态可见性三级控制
系统 SHALL 允许用户将"我的动态"（浏览记录、点赞动态、收藏夹）的可见性分别设置为"公开（PUBLIC）"、"仅关注者可见（FOLLOWERS_ONLY）"、"仅自己可见（PRIVATE）"。

#### Scenario: 隐藏浏览记录
- **WHEN** 用户将"浏览记录"可见性设置为"仅自己可见"
- **THEN** 其他用户无法在该用户主页看到浏览历史

#### Scenario: 点赞动态仅关注者可见
- **WHEN** 用户将"点赞动态"可见性设置为"仅关注者可见"
- **THEN** 只有关注该用户的用户能看到点赞动态

#### Scenario: 收藏夹公开
- **WHEN** 用户将"收藏夹"可见性设置为"公开"
- **THEN** 任何用户都能查看该用户的收藏夹

#### Scenario: 动态可见性异常——null 值
- **WHEN** 某项动态可见性字段为 null
- **THEN** 系统默认为"公开"级别

#### Scenario: 对自己始终可见
- **WHEN** 用户将某项可见性设置为"仅自己可见"
- **THEN** 该用户自己仍能看到该内容

### Requirement: 动态可见性缓存失效
系统 SHALL 在用户修改可见性设置后，确保缓存页面在 5 分钟内失效。可见性判定在查询接口层统一执行。

#### Scenario: 修改可见性后 5 分钟内生效
- **WHEN** 用户修改可见性设置
- **THEN** 其他用户在 5 分钟内刷新后看到更新后的可见性效果

#### Scenario: 主动删除缓存
- **WHEN** 用户更新可见性设置
- **THEN** 系统主动删除 Redis 中的可见性缓存键

### Requirement: 在线状态三级可见性
系统 SHALL 允许用户设置在线状态可见性为"公开（PUBLIC）"、"隐藏（HIDDEN）"、"仅互关可见（MUTUAL_ONLY）"三种模式。

#### Scenario: 隐藏在线状态
- **WHEN** 用户将在线状态可见性设置为"隐藏"
- **THEN** 系统对该用户显示"在线"，但对其他所有用户显示"离线"

#### Scenario: 仅互关可见
- **WHEN** 用户将在线状态可见性设置为"仅互关可见"
- **THEN** 只有与该用户互关的用户能看到其在线状态

#### Scenario: 公开在线状态
- **WHEN** 用户将在线状态可见性设置为"公开"
- **THEN** 所有用户都能看到该用户的在线标识

#### Scenario: 在线状态可见性异常——null 值
- **WHEN** 在线状态可见性字段为 null
- **THEN** 系统默认为"公开"

### Requirement: 搜索引擎索引控制
系统 SHALL 允许用户设置是否允许搜索引擎索引个人主页，通过 noindex meta 标签或 HTTP header 控制。

#### Scenario: 禁止搜索引擎索引
- **WHEN** 用户将"允许搜索引擎索引"设置为"否"
- **THEN** 系统在个人主页返回 noindex meta 标签和 X-Robots-Tag: noindex header

#### Scenario: 允许搜索引擎索引
- **WHEN** 用户将"允许搜索引擎索引"设置为"是"
- **THEN** 系统移除 noindex 标签，允许搜索引擎抓取

#### Scenario: 搜索引擎索引设置异常——空值
- **WHEN** 搜索引擎索引设置为 null
- **THEN** 系统默认不允许索引（保护隐私优先）

### Requirement: 可见性查询统一拦截
系统 SHALL 在所有涉及可见性检查的查询接口中统一执行可见性判定逻辑，不得在各处分散实现。

#### Scenario: 非关注者查看受限动态
- **WHEN** 非关注者尝试查看设置"仅关注者可见"的点赞动态
- **THEN** 系统返回空列表，不泄露任何动态信息

#### Scenario: 可见性策略查询
- **WHEN** 查询接口收到内容可见性检查请求
- **THEN** 系统通过 IContentUserVisibilityPolicyService 统一执行判定
