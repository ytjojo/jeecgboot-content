## ADDED Requirements

### Requirement: 状态检查注解
系统 SHALL 提供 @CheckUserStatus 注解，用于声明接口所需用户状态或禁止的用户状态。

#### Scenario: 注解允许状态
- GIVEN: Controller 方法标注 @CheckUserStatus(allow = {NORMAL, MUTED})
- WHEN: 用户状态为 NORMAL 时调用
- THEN: 请求正常通过

#### Scenario: 注解拒绝状态
- GIVEN: Controller 方法标注 @CheckUserStatus(allow = {NORMAL, MUTED})
- WHEN: 用户状态为 BANNED 时调用
- THEN: 系统返回 403 Forbidden 并提示用户当前状态及限制原因

### Requirement: 禁言状态功能限制
系统 SHALL 对禁言状态用户禁止所有主动互动功能，但允许被动浏览。

#### Scenario: 禁言用户发表评论
- GIVEN: 用户状态为 MUTED
- WHEN: 调用发表评论接口
- THEN: 系统返回 403 并提示"您已被禁言，禁言期限：{endTime}，原因：{reason}"

#### Scenario: 禁言用户发送私信
- GIVEN: 用户状态为 MUTED
- WHEN: 调用发送私信接口
- THEN: 系统返回 403 并提示禁言信息

#### Scenario: 禁言用户浏览内容
- GIVEN: 用户状态为 MUTED
- WHEN: 调用浏览内容接口
- THEN: 请求正常通过，用户可浏览和点赞收藏

### Requirement: 限制推荐状态内容分发限制
系统 SHALL 对限制推荐状态用户的内容在推荐流和搜索中降权或屏蔽。

#### Scenario: 限制推荐用户发布内容
- GIVEN: 用户状态为 RESTRICTED_RECOMMEND
- WHEN: 发布新内容
- THEN: 内容对粉丝可见但不进入公共推荐流

#### Scenario: 限制推荐用户内容搜索
- GIVEN: 用户状态为 RESTRICTED_RECOMMEND
- WHEN: 其他用户搜索相关关键词
- THEN: 该用户内容在搜索结果中降权排序

### Requirement: 冻结状态登录限制
系统 SHALL 对冻结状态用户禁止登录，引导安全核验。

#### Scenario: 冻结用户尝试登录
- GIVEN: 用户状态为 FROZEN
- WHEN: 调用登录接口
- THEN: 系统返回登录失败并提示"账号已冻结，请进行安全核验"

#### Scenario: 冻结用户完成核验
- GIVEN: 用户状态为 FROZEN
- WHEN: 完成安全核验（手机验证码）
- THEN: 系统将状态恢复为 NORMAL

### Requirement: 封禁状态严格限制
系统 SHALL 对封禁状态用户禁止登录和所有 API 访问。

#### Scenario: 封禁用户尝试登录
- GIVEN: 用户状态为 BANNED
- WHEN: 调用登录接口
- THEN: 系统返回登录失败并提示"账号已被封禁，封禁期限：{endTime}，原因：{reason}"

#### Scenario: 封禁用户 API 访问
- GIVEN: 用户状态为 BANNED 且持有有效 Token
- WHEN: 调用任意业务 API
- THEN: 系统返回 403 Forbidden
