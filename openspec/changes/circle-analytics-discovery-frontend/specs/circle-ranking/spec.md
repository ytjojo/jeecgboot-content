## ADDED Requirements

### Requirement: 热门榜单展示

系统 SHALL 在圈子列表页提供"热门榜"Tab，展示按成员数和活跃度排名的 Top 20 圈子列表。

#### Scenario: 热门榜单正常展示
- **WHEN** 用户切换到"热门榜"Tab
- **THEN** 加载并展示 Top 20 圈子列表，每行展示排名序号、圈子图标、名称、成员数、分类标签

#### Scenario: 热门榜排名标识
- **WHEN** 热门榜单加载完成
- **THEN** 排名 1-3 使用金银铜色序号标识

#### Scenario: 候选不足 20 个
- **WHEN** 符合条件的圈子不足 20 个
- **THEN** 展示全部符合条件的公开圈子

#### Scenario: 热门榜单为空
- **WHEN** 无符合条件的圈子
- **THEN** 展示空状态

---

### Requirement: 新锐榜单展示

系统 SHALL 在圈子列表页提供"新锐榜"Tab，展示按创建时间倒序的新增圈子列表。

#### Scenario: 新锐榜单正常展示
- **WHEN** 用户切换到"新锐榜"Tab
- **THEN** 加载并展示按创建时间倒序的圈子列表，展示圈子图标、名称、成员数、分类标签、创建时间

#### Scenario: 新锐榜单为空
- **WHEN** 无符合条件的圈子
- **THEN** 展示空状态

---

### Requirement: 榜单来源追踪

系统 SHALL 在用户点击榜单圈子时，跳转详情页 URL 携带对应的 source 参数。

#### Scenario: 点击热门榜圈子跳转
- **WHEN** 用户点击热门榜中的圈子
- **THEN** 跳转至圈子详情页，URL 携带 `source=hot_rank` 参数

#### Scenario: 点击新锐榜圈子跳转
- **WHEN** 用户点击新锐榜中的圈子
- **THEN** 跳转至圈子详情页，URL 携带 `source=new_rank` 参数

---

### Requirement: 未登录用户默认展示热门榜

系统 SHALL 在未登录用户进入圈子列表页时默认展示"热门榜"Tab。

#### Scenario: 未登录用户进入圈子列表页
- **WHEN** 未登录用户进入 `/circle` 页面
- **THEN** 默认选中"热门榜"Tab，加载热门榜单数据

---

### Requirement: Tab 切换加载

系统 SHALL 在用户切换 Tab 时加载对应数据，使用骨架屏占位。已缓存数据 SHALL 直接展示。

#### Scenario: Tab 切换加载数据
- **WHEN** 用户切换到未加载过的 Tab
- **THEN** 展示骨架屏，加载对应数据后渲染

#### Scenario: Tab 切换使用缓存
- **WHEN** 用户切换到已加载过的 Tab
- **THEN** 直接展示缓存数据，不重新请求
