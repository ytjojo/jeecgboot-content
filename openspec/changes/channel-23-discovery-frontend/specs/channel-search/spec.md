## ADDED Requirements

### Requirement: 频道搜索功能
系统 SHALL 提供频道搜索结果页（路由 `/channel/search`），支持关键词搜索频道。

#### Scenario: 执行关键词搜索
- **WHEN** 用户在搜索框输入关键词并按回车或点击搜索按钮
- **THEN** 调用 `GET /content/channel/search/query` 接口，展示搜索结果列表，结果统计区显示"共找到 N 个频道"

#### Scenario: 搜索输入防抖
- **WHEN** 用户连续输入关键词
- **THEN** 300ms 防抖后才触发搜索请求，避免频繁调用

### Requirement: 搜索结果展示
搜索结果 SHALL 展示频道卡片，包含图标、名称（关键词高亮）、类型标签、主分类、简介、订阅数和匹配原因。

#### Scenario: 搜索结果卡片信息
- **WHEN** 搜索返回结果
- **THEN** 每张卡片展示：频道图标 + 频道名称（关键词高亮）+ 频道类型标签、主分类 + 简介限 2 行、订阅数 + 匹配原因（名称匹配/标签匹配/描述匹配）

### Requirement: 搜索筛选条件
搜索结果页 SHALL 支持频道类型筛选、分类筛选和排序方式切换。

#### Scenario: 切换筛选条件
- **WHEN** 用户切换频道类型或分类筛选条件
- **THEN** 搜索结果实时刷新，展示符合筛选条件的结果

#### Scenario: 切换排序方式
- **WHEN** 用户切换排序方式（相关性/活跃度/订阅数/创建时间）
- **THEN** 搜索结果按新排序方式重新加载

### Requirement: 搜索分页
搜索结果 SHALL 支持分页加载，每页 20 条。

#### Scenario: 加载更多结果
- **WHEN** 用户滚动到结果列表底部且还有更多数据
- **THEN** 自动加载下一页搜索结果

### Requirement: 搜索空状态
搜索结果页 SHALL 在无结果时展示空状态引导。

#### Scenario: 无搜索结果
- **WHEN** 搜索返回 0 条结果
- **THEN** 展示空状态：清除筛选入口、改写关键词建议、浏览分类入口

### Requirement: 搜索结果反馈
搜索结果页 SHALL 提供"结果有帮助"反馈按钮。

#### Scenario: 用户反馈结果有帮助
- **WHEN** 用户点击"结果有帮助"按钮
- **THEN** 调用 `POST /content/channel/search/feedback` 接口记录反馈，按钮变为已反馈状态
- **NOTE**: 该后端接口已实现（`ContentChannelSearchController.java:35`，2026-06-07 更新）

### Requirement: 搜索历史管理
搜索 SHALL 支持搜索历史记录，持久化到 localStorage。

#### Scenario: 展示搜索历史
- **WHEN** 用户点击搜索框且搜索框为空
- **THEN** 展示最近 10 条搜索历史，支持清除单条或全部

#### Scenario: 清除搜索历史
- **WHEN** 用户点击清除搜索历史
- **THEN** 清空 localStorage 中的搜索历史记录

### Requirement: 搜索降级处理
搜索 SHALL 在接口异常时提供降级体验。

#### Scenario: 搜索接口降级
- **WHEN** 搜索接口请求失败
- **THEN** 展示"搜索服务繁忙，为您展示热门频道"并切换到热门频道列表

### Requirement: 搜索响应式适配
搜索结果页 SHALL 在移动端自适应布局。

#### Scenario: PC 端筛选布局
- **WHEN** 屏幕宽度 >= 992px
- **THEN** 筛选条件水平排列在搜索框下方

#### Scenario: 移动端筛选布局
- **WHEN** 屏幕宽度 < 768px
- **THEN** 筛选条件收纳为"筛选"按钮，点击展开筛选面板（Drawer 或 BottomSheet）
