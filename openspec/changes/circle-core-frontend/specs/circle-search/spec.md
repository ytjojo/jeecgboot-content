## ADDED Requirements

### Requirement: 圈子搜索
系统 SHALL 提供圈子搜索结果页（`/circle/search?q={keyword}`），按关键词模糊匹配公开圈子，展示搜索结果列表。

#### Scenario: 正常搜索
- **WHEN** 用户输入关键词并触发搜索
- **THEN** 跳转搜索结果页，展示匹配的公开圈子列表，关键词高亮

#### Scenario: 搜索结果展示
- **WHEN** 搜索返回结果
- **THEN** 每个结果展示图标、名称（关键词高亮）、简介（1行截断）、成员数、分类、加入状态按钮

#### Scenario: 私有圈子过滤
- **WHEN** 执行搜索
- **THEN** 搜索结果不包含私有圈子和密码保护圈子

#### Scenario: 搜索无结果
- **WHEN** 搜索无匹配结果
- **THEN** 展示空状态插图 + "未找到相关圈子" + 「浏览公开圈子」引导按钮

#### Scenario: 搜索服务异常
- **WHEN** 搜索接口请求失败
- **THEN** 展示 "搜索暂时不可用" + 「浏览公开圈子」入口

#### Scenario: 关键词为空
- **WHEN** 搜索关键词为空
- **THEN** 展示公开圈子浏览列表

#### Scenario: 加载中
- **WHEN** 搜索请求进行中
- **THEN** 展示列表骨架屏占位

### Requirement: 搜索防抖
系统 SHALL 对搜索输入执行 300ms 防抖，Enter 键立即触发搜索。

#### Scenario: 输入防抖
- **WHEN** 用户连续输入关键词
- **THEN** 停止输入 300ms 后自动触发搜索请求

#### Scenario: Enter 键立即搜索
- **WHEN** 用户在搜索框按 Enter 键
- **THEN** 立即触发搜索，不等待防抖

### Requirement: 搜索结果加入操作
系统 SHALL 在搜索结果中支持加入操作，行为与详情页加入逻辑一致。

#### Scenario: 直接加入公开圈子
- **WHEN** 用户在搜索结果中点击公开+直接加入圈子的「加入」按钮
- **THEN** 直接加入成功，按钮变为「已加入」

#### Scenario: 已加入标识
- **WHEN** 搜索结果中包含用户已加入的圈子
- **THEN** 显示灰色 "已加入" 文字

### Requirement: 搜索入口
系统 SHALL 在圈子列表页顶部提供搜索框，输入关键词后按 Enter 或点击搜索图标触发搜索，跳转搜索结果页。

#### Scenario: 从列表页搜索
- **WHEN** 用户在圈子列表页搜索框输入关键词并按 Enter
- **THEN** 跳转到搜索结果页 `/circle/search?q={keyword}`

#### Scenario: 搜索结果页重新搜索
- **WHEN** 用户在搜索结果页修改关键词并搜索
- **THEN** 更新 URL 参数并展示新的搜索结果
