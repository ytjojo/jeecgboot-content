## ADDED Requirements

### Requirement: 频道发现页聚合展示
系统 SHALL 提供频道发现聚合页（路由 `/channel/discovery`），集中展示推荐频道、排行榜入口、编辑精选和分类入口。

#### Scenario: 登录用户查看发现页
- **WHEN** 登录用户访问 `/channel/discovery`
- **THEN** 页面从上到下依次展示搜索入口区、个性化推荐频道区、排行榜入口区（Top 5）、编辑精选区、分类入口区

#### Scenario: 未登录用户查看发现页
- **WHEN** 未登录用户访问 `/channel/discovery`
- **THEN** 页面展示冷启动推荐结果（热门频道），其余模块与登录用户一致

#### Scenario: 发现页数据加载失败
- **WHEN** 聚合接口 `GET /content/channel/discovery/home` 请求失败
- **THEN** 前端降级为并行调用 `GET /content/channel/recommendation/list`、`GET /content/channel/ranking/hot`、`GET /content/channel/editorial-pick/list` 三个独立接口，任一模块加载失败时展示该模块的错误状态和重试按钮

### Requirement: 推荐频道卡片展示
推荐频道区 SHALL 展示推荐频道卡片列表，包含频道图标、名称、类型标签、主分类、简介、订阅数、推荐理由和订阅操作。

#### Scenario: 推荐卡片信息展示
- **WHEN** 推荐频道数据加载完成
- **THEN** 每张卡片展示：频道图标 + 频道名称 + 频道类型标签（第一层）、主分类 + 简介限 2 行（第二层）、订阅数 + 推荐理由（第三层）、订阅/已订阅按钮 + 不感兴趣菜单（操作区）

#### Scenario: 用户反馈不感兴趣
- **WHEN** 用户点击推荐卡片的"不感兴趣"按钮并确认
- **THEN** 调用 `/content/channel/recommendation/not-interested` 接口，卡片从列表中移除，展示"已反馈"提示

### Requirement: 排行榜入口区展示
发现页 SHALL 展示排行榜入口区，支持热门榜/新晋榜/系统榜 Tab 切换，每榜展示 Top 5。

#### Scenario: 切换排行榜 Tab
- **WHEN** 用户点击热门榜/新晋榜/系统榜 Tab
- **THEN** 展示对应榜单的 Top 5 频道，包含排名序号、频道图标、名称、订阅数

#### Scenario: 查看完整榜单
- **WHEN** 用户点击"查看完整榜单"
- **THEN** 跳转到排行榜页 `/channel/ranking`

### Requirement: 编辑精选区展示
发现页 SHALL 展示编辑精选区，包含精选频道卡片和运营推荐语。

#### Scenario: 有精选数据时展示
- **WHEN** 存在有效的编辑精选频道
- **THEN** 展示精选频道卡片，包含频道信息和运营推荐语

#### Scenario: 无精选数据时隐藏
- **WHEN** 没有有效的编辑精选频道
- **THEN** 编辑精选区不展示

### Requirement: 分类入口区展示
发现页 SHALL 展示一级分类卡片网格，点击进入分类浏览页。

#### Scenario: 点击分类卡片
- **WHEN** 用户点击某个一级分类卡片
- **THEN** 跳转到分类浏览页 `/channel/category`，并自动选中该分类

### Requirement: 发现页响应式适配
发现页 SHALL 在不同断点下自适应布局。

#### Scenario: 桌面端布局
- **WHEN** 屏幕宽度 >= 992px
- **THEN** 推荐区横向 4 列网格，分类区 6 列网格

#### Scenario: 移动端布局
- **WHEN** 屏幕宽度 < 576px
- **THEN** 推荐区横向滚动单卡，分类区 2 列，搜索框全宽
