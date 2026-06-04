## ADDED Requirements

### Requirement: AddContentDialog 添加已发布内容到频道

系统 SHALL 提供 AddContentDialog 组件，支持将已发布内容添加到频道，覆盖三种场景入口。

#### Scenario: 搜索可添加的已发布内容
- **WHEN** 用户打开 AddContentDialog
- **THEN** 展示内容搜索框，调用 `GET /content/channel/publish/add-existing/search` 搜索可添加的已发布内容（**后端待实现**），展示搜索结果列表

#### Scenario: 选择内容并预览
- **WHEN** 用户选择一条已发布内容
- **THEN** 展示内容预览（标题、类型、作者、发布时间）

#### Scenario: 选择目标频道
- **WHEN** 用户选择内容后
- **THEN** 展示简化版 ChannelSelector，用户选择目标频道，展示每个频道的预期结果（直接展示/进入待审）

#### Scenario: 系统频道添加场景
- **WHEN** 平台运营通过系统频道入口添加内容
- **THEN** 额外展示运营身份和添加原因输入框（必填）

#### Scenario: 添加他人作品场景
- **WHEN** 频道主/管理员添加他人作品到频道
- **THEN** 展示原作者信息，不改变署名和发布时间

#### Scenario: 作者添加到个人/组织频道
- **WHEN** 内容作者将已发布内容添加到自己的频道
- **THEN** 直接展示可选频道列表，无需额外身份验证

#### Scenario: 不可添加内容提示
- **WHEN** 搜索结果中包含已删除或不可见内容
- **THEN** 该内容不可选，提示"内容不可添加"

#### Scenario: 提交后展示结果
- **WHEN** 用户点击"添加到频道"
- **THEN** 调用 `POST /content/channel/publish/add-existing` API，提交后逐频道展示结果反馈

#### Scenario: 三种场景入口
- **WHEN** 用户从不同入口触发添加
- **THEN** 内容详情页的"添加到频道"操作、频道管理页的"添加内容"按钮、系统频道管理入口都复用同一 AddContentDialog 组件，根据入口类型展示不同的额外字段
