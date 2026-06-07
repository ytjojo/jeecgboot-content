# channel-subscription Specification

## Purpose
TBD - created by archiving change channel-21-privacy-membership-frontend. Update Purpose after archive.
## Requirements
### Requirement: 订阅/取消订阅按钮状态机

系统 SHALL 在频道主页根据用户与频道的关系展示不同的操作按钮，支持订阅、取消订阅、申请加入等操作。

#### Scenario: 未订阅公开频道
- **WHEN** 用户进入未订阅的公开频道主页
- **THEN** 展示"订阅"主按钮（Primary 样式）

#### Scenario: 已订阅频道
- **WHEN** 用户进入已订阅的频道主页
- **THEN** 展示"已订阅"按钮（默认样式），hover 展示下拉菜单含"取消订阅"选项

#### Scenario: 未订阅私有频道非成员
- **WHEN** 用户进入未订阅的私有频道主页且非成员
- **THEN** 展示"申请加入"主按钮

#### Scenario: 私有频道已加入未订阅
- **WHEN** 用户进入私有频道主页且已加入但未订阅
- **THEN** 展示"订阅"按钮

#### Scenario: 被禁言用户
- **WHEN** 用户被频道禁言
- **THEN** 展示"已禁言"标签 + 订阅状态

#### Scenario: 被黑名单用户
- **WHEN** 用户被频道加入黑名单
- **THEN** 不展示订阅/加入按钮，展示"您无法加入此频道"

### Requirement: 乐观更新策略

系统 SHALL 对订阅和取消订阅操作采用乐观更新策略，点击后立即更新 UI 状态，失败时回滚。

#### Scenario: 订阅乐观更新成功
- **WHEN** 用户点击"订阅"按钮
- **THEN** 按钮立即变为"已订阅"状态（不等待接口返回），接口成功后保持该状态

#### Scenario: 订阅乐观更新失败回滚
- **WHEN** 用户点击"订阅"按钮后接口返回失败
- **THEN** 按钮状态回滚为"订阅"，展示错误提示

#### Scenario: 取消订阅乐观更新成功
- **WHEN** 用户确认取消订阅
- **THEN** 按钮立即变为"订阅"状态，接口成功后保持该状态

#### Scenario: 取消订阅二次确认
- **WHEN** 用户点击下拉菜单中的"取消订阅"
- **THEN** 弹出确认 Modal："确认取消订阅？取消后您将不再收到该频道的更新推送。"

#### Scenario: 缓存失效
- **WHEN** 订阅或取消订阅操作成功
- **THEN** 同步更新 useChannelContext 中的 isSubscribed 字段，使订阅列表和订阅状态缓存失效

### Requirement: 订阅列表管理页面

系统 SHALL 提供订阅列表管理页面，支持分组管理、搜索、提醒控制和取消订阅。

#### Scenario: 展示订阅列表
- **WHEN** 用户进入订阅列表页
- **THEN** 展示已订阅频道的卡片列表，每项包含频道头像、名称、最新内容摘要、订阅来源标签、提醒开关、取消订阅

#### Scenario: 搜索过滤
- **WHEN** 用户在搜索框输入关键词
- **THEN** 实时过滤频道列表，按频道名称模糊匹配

#### Scenario: 分组切换
- **WHEN** 用户点击标签页切换分组
- **THEN** 展示对应分组的频道列表

#### Scenario: 新建分组
- **WHEN** 用户点击"新建分组"按钮
- **THEN** 弹出 Modal 输入分组名称，确认后创建分组

#### Scenario: 提醒开关切换
- **WHEN** 用户切换频道的提醒开关
- **THEN** 无需确认，切换成功后展示成功消息

#### Scenario: 取消订阅
- **WHEN** 用户点击频道卡片上的取消订阅
- **THEN** 二次确认 Modal，确认后卡片移除

#### Scenario: 空订阅列表
- **WHEN** 用户没有订阅任何频道
- **THEN** 展示空状态"暂无订阅频道" + "去发现频道"按钮

#### Scenario: 系统推荐标签
- **WHEN** 频道为默认关注的系统频道
- **THEN** 展示"系统推荐"蓝色 Tag

