## ADDED Requirements

### Requirement: 数据统计页路由与权限控制

系统 SHALL 提供 `/circle/:id/analytics` 路由，仅允许圈子创建者和版主访问。普通成员访问时 SHALL 展示 403 权限不足页面并提供返回按钮。

#### Scenario: 创建者访问数据统计页
- **WHEN** 圈子创建者进入 `/circle/:id/analytics`
- **THEN** 页面正常加载，展示数据统计面板

#### Scenario: 版主访问数据统计页
- **WHEN** 圈子版主进入 `/circle/:id/analytics`
- **THEN** 页面正常加载，展示数据统计面板

#### Scenario: 普通成员访问数据统计页
- **WHEN** 普通成员进入 `/circle/:id/analytics`
- **THEN** 展示 403 页面，文案为"权限不足，仅创建者和版主可查看数据统计"，并提供返回按钮

#### Scenario: 未登录用户访问数据统计页
- **WHEN** 未登录用户进入 `/circle/:id/analytics`
- **THEN** 跳转登录页

---

### Requirement: 核心指标卡片展示

系统 SHALL 在数据统计页顶部展示 4 个核心指标卡片（成员总数、新增成员数、发帖总数、活跃用户数），每个卡片 SHALL 显示当前值和与上期对比的变化百分比。

#### Scenario: 指标卡片正常展示
- **WHEN** 统计数据加载完成
- **THEN** 展示 4 个指标卡片，每个卡片包含指标名称、当前数值（带数字动画）、变化百分比

#### Scenario: 变化百分比方向标识
- **WHEN** 变化百分比为正数
- **THEN** 展示绿色上箭头和百分比数值

#### Scenario: 变化百分比为负数
- **WHEN** 变化百分比为负数
- **THEN** 展示红色下箭头和百分比数值

#### Scenario: 变化百分比为零
- **WHEN** 变化百分比为零
- **THEN** 展示灰色横线

#### Scenario: 上期值为零且本期值大于零
- **WHEN** 上期值为 0 且本期值大于 0
- **THEN** 不展示百分比，改为展示"新增"文案

#### Scenario: 上期值和本期值均为零
- **WHEN** 上期值和本期值均为 0
- **THEN** 展示"--"（无变化）

#### Scenario: 指标卡片加载状态
- **WHEN** 统计数据正在加载
- **THEN** 展示骨架块占位

---

### Requirement: 趋势图表展示

系统 SHALL 展示成员增长、内容发布、活跃度三条趋势折线图，支持 Hover 查看具体数值 Tooltip。

#### Scenario: 趋势图表正常渲染
- **WHEN** 统计数据加载完成
- **THEN** 展示 3 个折线图，X 轴为日期，Y 轴为对应指标数值

#### Scenario: Hover 数据点展示 Tooltip
- **WHEN** 用户 Hover 图表数据点
- **THEN** 展示 Tooltip，显示日期和具体数值

#### Scenario: 图表数据为空
- **WHEN** 时间范围内无数据
- **THEN** 展示空状态插图 + "暂无数据"文案，保留时间筛选入口

---

### Requirement: 时间范围筛选

系统 SHALL 提供时间范围选择器，支持快捷选项（近 7 天/近 30 天）和自定义日期区间，最大可选跨度 90 天。切换时间范围后 SHALL 自动刷新所有数据和图表。

#### Scenario: 默认加载近 7 天数据
- **WHEN** 用户进入数据统计页
- **THEN** 默认选中"近 7 天"，加载近 7 天数据

#### Scenario: 切换快捷时间范围
- **WHEN** 用户点击"近 30 天"快捷选项
- **THEN** 数据区域展示加载态，刷新所有数据和图表

#### Scenario: 选择自定义时间范围
- **WHEN** 用户通过 DatePicker 选择自定义日期区间
- **THEN** 数据区域展示加载态，刷新所有数据和图表

#### Scenario: 自定义时间范围超过 90 天
- **WHEN** 用户选择的日期区间超过 90 天
- **THEN** DatePicker 中超出范围的日期变为不可选状态（灰色），前端校验优先

---

### Requirement: 数据导出 CSV

系统 SHALL 支持将当前筛选时间范围内的统计数据导出为 CSV 文件。

#### Scenario: 正常导出 CSV
- **WHEN** 用户点击"导出数据"按钮
- **THEN** 按钮变为 loading 状态，浏览器触发 CSV 下载，文件名格式为 `{圈子名称}_{startDate}_{endDate}.csv`

#### Scenario: 导出按钮加载前禁用
- **WHEN** 统计数据正在加载
- **THEN** 导出按钮为禁用状态

#### Scenario: 导出失败
- **WHEN** 导出请求失败
- **THEN** 展示 Toast 错误提示"导出失败，请重试"，保留当前筛选状态

---

### Requirement: 数据统计页错误处理

系统 SHALL 在数据加载失败时展示错误状态和重试按钮，点击重试 SHALL 重新请求数据。

#### Scenario: 数据加载失败
- **WHEN** 统计数据接口请求失败
- **THEN** 展示错误状态 + "重试"按钮，文案为"数据加载失败，请重试"

#### Scenario: 点击重试
- **WHEN** 用户点击重试按钮
- **THEN** 重新请求统计数据，展示加载态

#### Scenario: 请求频率过高（429）
- **WHEN** 接口返回 429 错误
- **THEN** 展示 Toast 提示"操作过于频繁，请稍后重试"

#### Scenario: 圈子不存在（404）
- **WHEN** 接口返回 404 错误
- **THEN** 展示 404 页面，文案为"圈子不存在或已被删除"
