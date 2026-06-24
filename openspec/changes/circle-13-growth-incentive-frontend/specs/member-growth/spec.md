## ADDED Requirements

### Requirement: 成长概览卡片展示

系统 SHALL 在个人成长信息页顶部展示成长概览卡片，包含经验值、贡献值、圈内排名三个指标，数值使用 CountTo 数字动画组件。后端 MemberGrowthVO 字段映射：`expPoints` → 经验值、`contributionPoints` → 贡献值、`rank` → 圈内排名。

#### Scenario: 展示成长概览数据
- **WHEN** 用户进入个人成长信息页
- **THEN** 顶部展示三列等宽卡片，分别显示经验值（`expPoints`）、贡献值（`contributionPoints`）、圈内排名（`rank`），数值有 CountTo 动画过渡

#### Scenario: 数值变化动画
- **WHEN** 经验值或贡献值发生变化
- **THEN** 数值使用 CountTo 组件进行动画过渡展示

### Requirement: 成员等级进度展示

系统 SHALL 在个人成长信息页展示成员当前等级和下一等级进度。

#### Scenario: 展示等级进度
- **WHEN** 用户进入个人成长信息页
- **THEN** 展示当前等级标识、下一等级标识、进度条和「距下一等级还需 X 经验值」文案

#### Scenario: 最高等级展示
- **WHEN** 成员达到最高等级
- **THEN** 进度条满格，文案变为「已达最高等级」

### Requirement: 连续参与进度展示

系统 SHALL 在个人成长信息页展示近 7 天连续参与进度，已完成天数用实心圆标记（主题色），当日未完成用空心圆，非统计日用横线。后端 MemberGrowthVO 提供 `participationDays` 字段（连续参与天数），也可通过 `GET /api/v1/content/circle/growth/participation?circleId=&userId=` 接口单独查询。

#### Scenario: 展示连续参与进度
- **WHEN** 用户进入个人成长信息页
- **THEN** 展示 7 天时间轴，已完成天数用实心圆标记，当日未完成用空心圆，并展示「已连续参与 X 天」文案。连续参与天数从 `MemberGrowthVO.participationDays` 字段获取。

#### Scenario: 近 7 天有参与行为
- **WHEN** 近 7 天内至少 3 天完成有效参与行为
- **THEN** 展示连续参与进度和已完成天数

#### Scenario: 近 7 天无参与行为
- **WHEN** 近 7 天没有任何有效参与行为
- **THEN** 展示空状态，引导文案「发帖、评论或点赞即可开始记录」，附带操作按钮

#### Scenario: 连续参与里程碑达成
- **WHEN** 连续参与进度达到 3 天、7 天或 14 天
- **THEN** 展示对应成就进度或已获得徽章

### Requirement: 每日经验上限展示

系统 SHALL 在个人成长信息页展示今日已获经验值和每日上限。后端 MemberGrowthVO 未提供 `todayExp` 和 `dailyExpLimit` 字段，每日上限 100 点为前端硬编码（PRD 定义），今日经验值暂不展示（后续后端补充 `todayExp` 字段后启用）。

#### Scenario: 展示今日经验进度
- **WHEN** 用户进入个人成长信息页
- **THEN** 展示每日经验上限提示「每日上限 100 点」（注：后端未提供 `todayExp` 字段，今日已获经验值暂不展示，待后端补充后启用进度条）

#### Scenario: 达到每日上限
- **WHEN** 今日经验值达到 100 点上限
- **THEN** 进度条变为满格，显示「已达今日上限」（注：后端未提供 `todayExp` 字段，此场景待后端补充后启用）

### Requirement: 徽章摘要展示

系统 SHALL 在个人成长信息页展示最近获得的徽章摘要，并提供「查看全部徽章」入口。后端 MemberGrowthVO 未提供 `recentBadges` 字段，需单独调用 `GET /api/v1/content/circle/growth/achievement/list` 接口获取徽章列表并筛选已获得的徽章。

#### Scenario: 展示徽章摘要
- **WHEN** 用户进入个人成长信息页
- **THEN** 调用成就徽章列表接口获取已获得徽章，展示最近获得的 3 枚徽章卡片，点击「查看全部徽章」跳转徽章墙页

#### Scenario: 无已获得徽章
- **WHEN** 用户未获得任何徽章
- **THEN** 展示空状态引导文案和可参与行为入口

### Requirement: 成员成长信息 API 对接

系统 SHALL 通过 GET `/api/v1/content/circle/growth/info?circleId={circleId}&userId={userId}` 接口获取成员成长信息。

#### Scenario: 接口请求成功
- **WHEN** 个人成长信息页加载
- **THEN** 调用成员成长接口，解析 MemberGrowthVO 响应数据并渲染到页面

#### Scenario: 接口请求失败
- **WHEN** 成员成长接口请求失败
- **THEN** 展示错误提示和「重试」按钮

#### Scenario: 加载中状态
- **WHEN** 成员成长接口请求中
- **THEN** 展示骨架屏占位

### Requirement: 经验值扣除与等级回退展示

系统 SHALL 正确展示经验值扣除后的数据变化，包括等级下降情况。

#### Scenario: 经验值扣减后数据更新
- **WHEN** 内容删除/撤回/违规导致经验值扣减
- **THEN** 个人成长页数据实时更新，排行榜排名相应调整

#### Scenario: 等级下降展示
- **WHEN** 经验值扣减导致低于当前等级门槛
- **THEN** 等级下降一级，页面展示更新后的等级标识
