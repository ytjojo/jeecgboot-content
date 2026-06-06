## ADDED Requirements

### Requirement: useChannelContext composable 状态隔离

useChannelContext composable SHALL 按 channelId 隔离管理频道上下文，避免多频道切换时数据串台。

#### Scenario: 不同 channelId 独立状态
- **WHEN** 两个不同 channelId 的组件分别调用 useChannelContext
- **THEN** 各自拥有独立的 channelInfo、userRelation、isSubscribed 等状态，互不影响

#### Scenario: channelId 变化时状态更新
- **WHEN** 同一组件的 channelId ref 发生变化
- **THEN** composable 自动重新加载新频道的上下文数据

### Requirement: 频道上下文数据加载

useChannelContext SHALL 提供 loadContext 方法，并行加载频道信息和用户关系数据。

#### Scenario: 并行加载频道信息和用户关系
- **WHEN** 调用 loadContext()
- **THEN** 并行请求 getChannelInfo 和 getUserChannelRelation，完成后更新 channelInfo、userRelation、privacyType、joinMethod、isSubscribed、memberRole、isMuted、isBlacklisted

#### Scenario: 加载失败处理
- **WHEN** loadContext() 中任一请求失败
- **THEN** 保留当前状态，不更新已加载的数据

#### Scenario: 网络超时处理
- **WHEN** loadContext() 中 API 请求超过 10 秒未响应
- **THEN** 中断请求，显示"网络请求超时，请稍后重试"错误提示，并展示重试按钮

#### Scenario: 离线状态检测
- **WHEN** 浏览器检测到网络离线（navigator.onLine === false 或请求因网络不可用失败）
- **THEN** 显示离线状态横幅提示，禁用所有需要网络的交互元素（如订阅、取消订阅、加入频道等按钮）

#### Scenario: Token 过期处理
- **WHEN** loadContext() 中 API 返回 401 状态码
- **THEN** 清除本地认证信息，重定向至登录页面，并在 URL 参数中保留当前频道页面地址以便登录后回跳

#### Scenario: API 服务器错误处理
- **WHEN** loadContext() 中 API 返回 500 状态码
- **THEN** 显示"服务器繁忙，请稍后重试"通用错误提示，并展示重试按钮

### Requirement: 权限判断 computed 属性

useChannelContext SHALL 提供 canManageMembers 和 canPublish 等 computed 属性用于权限判断。

#### Scenario: canManageMembers 判断
- **WHEN** userRelation.role 为 OWNER 或 ADMIN
- **THEN** canManageMembers 返回 true，否则返回 false

#### Scenario: canPublish 判断
- **WHEN** memberRole 存在且 isMuted 为 false 且 isBlacklisted 为 false
- **THEN** canPublish 返回 true，否则返回 false

### Requirement: 上下文重置

useChannelContext SHALL 提供 resetContext 方法，用于频道切换时重置所有状态。

#### Scenario: 频道切换时重置
- **WHEN** 调用 resetContext()
- **THEN** channelInfo、userRelation 设为 null，isSubscribed、isMuted、isBlacklisted 设为 false，memberRole 设为 null

#### Scenario: 路由守卫配合
- **WHEN** 频道路由 beforeRouteUpdate 触发
- **THEN** 调用 resetContext() + loadContext()，确保切换频道时数据正确刷新

### Requirement: provide/inject 集成

useChannelContext SHALL 通过 provide/inject 向子组件传递频道上下文。

#### Scenario: 父组件 provide 频道上下文
- **WHEN** 频道页面根组件调用 useChannelContext
- **THEN** 通过 provide 向子组件暴露频道上下文

#### Scenario: 子组件 inject 频道上下文
- **WHEN** 子组件通过 inject 获取频道上下文
- **THEN** 可访问 channelInfo、userRelation、isSubscribed 等响应式状态
