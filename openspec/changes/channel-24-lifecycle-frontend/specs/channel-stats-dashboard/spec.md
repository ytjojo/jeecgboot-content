## ADDED Requirements

### Requirement: 核心指标展示
系统 SHALL 在数据看板页面展示频道核心指标，包括订阅数、内容数、PV、UV。

#### Scenario: 正常展示核心指标
- **WHEN** 频道主进入数据看板页面
- **THEN** 页面展示 4 个指标卡片，分别显示订阅数、内容数、PV、UV 的当前值

#### Scenario: 指标数值动画
- **WHEN** 指标数据加载完成
- **THEN** 指标数值使用 CountTo 数字动画组件展示

#### Scenario: 趋势变化展示
- **WHEN** 指标数据包含趋势变化值
- **THEN** 指标卡片展示上升/下降箭头和百分比

#### Scenario: 数据更新时间展示
- **WHEN** 指标数据加载完成
- **THEN** 页面展示数据更新时间

### Requirement: 时间范围筛选
系统 SHALL 支持切换时间范围查看指标变化趋势，支持日/周/月/自定义日期区间。

#### Scenario: 切换时间范围
- **WHEN** 用户选择时间范围（日/周/月/自定义）
- **THEN** 全页面数据联动刷新

#### Scenario: 自定义日期区间
- **WHEN** 用户选择自定义日期区间
- **THEN** 使用 DatePicker Range 选择起止日期

### Requirement: 趋势图展示
系统 SHALL 使用折线图展示核心指标趋势，支持多指标叠加展示。

#### Scenario: 趋势图正常展示
- **WHEN** 趋势数据加载完成
- **THEN** 折线图展示订阅数/内容数/PV/UV 趋势

#### Scenario: 趋势图交互
- **WHEN** 用户 hover 趋势图
- **THEN** 展示具体数值 tooltip

#### Scenario: 图例切换
- **WHEN** 用户点击图例
- **THEN** 切换显示/隐藏对应指标

### Requirement: 互动数据展示
系统 SHALL 展示互动数据，包括点赞、评论、收藏、分享、有效访问。

#### Scenario: 互动数据正常展示
- **WHEN** 互动数据加载完成
- **THEN** 展示 5 项互动指标卡片

#### Scenario: 新增内容统计
- **WHEN** 互动数据加载完成
- **THEN** 展示新增内容数量和类型分布

### Requirement: 热门内容排行
系统 SHALL 展示热门内容排行，支持按周期切换。

#### Scenario: 热门内容列表展示
- **WHEN** 热门内容数据加载完成
- **THEN** 展示排名、标题、类型、发布时间、有效互动量

#### Scenario: 周期切换
- **WHEN** 用户切换周期（近 7 天/近 30 天/近 90 天）
- **THEN** 列表数据刷新

#### Scenario: 内容跳转
- **WHEN** 用户点击内容标题
- **THEN** 跳转到内容详情页

### Requirement: 用户分析展示
系统 SHALL 展示用户分析数据，包括订阅增量/流失、成员活跃度、贡献排行。

#### Scenario: 订阅趋势展示
- **WHEN** 用户分析数据加载完成
- **THEN** 展示订阅增量/流失趋势图

#### Scenario: 活跃度分布展示
- **WHEN** 用户分析数据加载完成
- **THEN** 展示成员活跃度占比饼图

#### Scenario: 贡献排行展示
- **WHEN** 用户分析数据加载完成
- **THEN** 展示贡献排行列表

#### Scenario: 组织频道部门统计
- **WHEN** 操作者有组织数据查看权限
- **THEN** 展示按部门/职务统计维度

#### Scenario: 无权限提示
- **WHEN** 操作者无组织数据查看权限
- **THEN** 展示"权限不足"提示

### Requirement: 数据加载状态
系统 SHALL 在数据加载过程中展示骨架屏。

#### Scenario: 骨架屏展示
- **WHEN** 数据正在加载
- **THEN** 展示骨架屏占位

#### Scenario: 独立加载状态
- **WHEN** 多个模块同时加载
- **THEN** 每个模块独立 loading 状态，互不阻塞

#### Scenario: 错误状态处理
- **WHEN** 某个模块加载失败
- **THEN** 展示错误提示，其他模块正常展示
