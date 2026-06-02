## ADDED Requirements

### Requirement: BadgeDisplay prop-driven component
系统 SHALL 提供 BadgeDisplay 组件，通过 `badges` prop 接收数据，内部不发起 API 请求。

#### Scenario: BadgeDisplay receives badge data via props
- **WHEN** 父组件通过 `badges` prop 传入勋章数据数组
- **THEN** BadgeDisplay 渲染勋章图标列表，每个图标 24px（small）或 32px（medium），最多显示 maxDisplay 个

#### Scenario: BadgeDisplay with no badges
- **WHEN** `badges` prop 为空数组或 undefined
- **THEN** BadgeDisplay 不渲染任何内容（不显示空状态，静默处理）

### Requirement: Badge display on personal profile
系统 SHALL 在个人主页展示用户佩戴的勋章。

#### Scenario: View own profile badges
- **WHEN** 用户进入个人主页
- **THEN** 页面调用 `getWornBadges()` API，通过 prop 将佩戴勋章传入 BadgeDisplay 组件

#### Scenario: View other user profile badges
- **WHEN** 用户进入他人主页
- **THEN** 页面调用 `getWornBadges(userId)` API，通过 prop 将佩戴勋章传入 BadgeDisplay 组件

### Requirement: Badge display on post cards
系统 SHALL 在帖子卡片和帖子详情中展示作者佩戴的勋章。

#### Scenario: Post list shows author badges
- **WHEN** 帖子列表 API 响应包含 `authorBadges` 字段
- **THEN** 帖子卡片直接使用 `authorBadges` 数据通过 prop 传入 BadgeDisplay

#### Scenario: Post detail shows author badges
- **WHEN** 帖子详情 API 响应包含 `authorBadges` 字段
- **THEN** 帖子详情页使用 `authorBadges` 数据通过 prop 传入 BadgeDisplay

### Requirement: Badge hover tooltip
系统 SHALL 在勋章图标悬停时显示 Tooltip。

#### Scenario: Desktop hover on badge icon
- **WHEN** 用户在桌面端鼠标悬停在勋章图标上
- **THEN** 显示 Tooltip 包含勋章名称和获得原因

#### Scenario: Mobile tap on badge icon
- **WHEN** 用户在移动端点击勋章图标
- **THEN** 显示 Tooltip 包含勋章名称和获得原因

### Requirement: Badge image fallback
系统 SHALL 在勋章图片加载失败时显示 SVG 占位图标。

#### Scenario: Badge image fails to load
- **WHEN** 勋章图片 URL 加载失败
- **THEN** 显示 SVG 占位图标，按勋章分类区分样式（成就/身份/活动/关系各一个默认图标）

### Requirement: Badge image lazy loading
系统 SHALL 对勋章列表中的图片使用懒加载。

#### Scenario: Badge list lazy loading
- **WHEN** 勋章列表页渲染勋章卡片
- **THEN** 所有勋章图片使用 `loading="lazy"` 懒加载，非可视区域图片不加载
