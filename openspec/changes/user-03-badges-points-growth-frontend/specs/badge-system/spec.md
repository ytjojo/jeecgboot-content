## ADDED Requirements

### Requirement: Badge category browsing
系统 SHALL 提供勋章分类浏览功能，支持全部、成就类、身份类、活动类、关系类、已过期六个分类标签页切换。每个分类下展示该类别勋章卡片网格。

#### Scenario: User views badge categories
- **WHEN** 用户进入 `/content/my-badges` 页面
- **THEN** 默认展示"全部"标签页，显示所有勋章卡片，顶部统计区显示已获得勋章数/佩戴中勋章数/总勋章数

#### Scenario: User switches category tab
- **WHEN** 用户点击"成就类"标签页
- **THEN** 仅展示成就类勋章卡片，其他分类隐藏

#### Scenario: User views expired badges
- **WHEN** 用户点击"已过期"标签页
- **THEN** 仅展示已过期勋章，卡片显示灰色图标和"已过期"标签

### Requirement: Badge progress display
系统 SHALL 展示未获得勋章的获取进度，包括进度条（当前值/目标值）和剩余要求文字说明。

#### Scenario: User views unearned badge progress
- **WHEN** 用户查看未获得的勋章卡片
- **THEN** 显示灰色勋章图标、勋章名称、进度条（如 7/10）

#### Scenario: User opens unearned badge detail
- **WHEN** 用户点击未获得勋章卡片
- **THEN** 弹窗展示勋章大图、名称、分类标签、获取条件说明、进度条（当前值/目标值）、剩余要求文字

### Requirement: Badge wearing configuration
系统 SHALL 支持用户佩戴最多 5 个勋章，佩戴的勋章在主页/帖子/评论区展示。

#### Scenario: User enters wear edit mode
- **WHEN** 用户点击"佩戴设置"按钮
- **THEN** 进入编辑模式，勋章卡片出现勾选框，已获得勋章可勾选

#### Scenario: User exceeds max wear limit
- **WHEN** 用户在编辑模式下勾选第 6 个勋章
- **THEN** 提示"最多佩戴 5 个勋章"并阻止勾选

#### Scenario: User saves wear configuration
- **WHEN** 用户点击"保存"按钮
- **THEN** 调用 POST `/api/v1/content/user/growth/badge/wear` API（实际端点），成功后提示"佩戴设置已更新"并退出编辑模式

#### Scenario: User cancels wear edit
- **WHEN** 用户点击"取消"按钮
- **THEN** 恢复之前的选择状态并退出编辑模式

### Requirement: Badge detail popup
系统 SHALL 提供勋章详情弹窗，展示勋章的完整信息。

#### Scenario: User views earned badge detail
- **WHEN** 用户点击已获得的勋章卡片
- **THEN** 弹窗展示勋章大图、名称、分类标签、获得时间、有效期（如有）、佩戴状态

#### Scenario: User views expired badge detail
- **WHEN** 用户点击已过期的勋章卡片
- **THEN** 弹窗展示勋章大图、名称、"已过期"状态标签、过期时间

### Requirement: Badge empty state
系统 SHALL 在无勋章数据时展示空状态引导。

#### Scenario: User has no badges
- **WHEN** 用户进入勋章页且无任何勋章
- **THEN** 显示空状态插图和"暂无勋章，完成任务可获得勋章"引导文案

### Requirement: Admin badge recycling
管理员 SHALL 能够回收违规勋章，回收操作需记录原因且不可撤销。

#### Scenario: Admin recycles a badge
- **WHEN** 管理员在勋章管理页点击"回收"按钮
- **THEN** 打开回收确认弹窗，显示用户信息和勋章信息，回收原因输入框必填（最多 200 字）

#### Scenario: Admin confirms badge recycling
- **WHEN** 管理员输入回收原因并点击"确认回收"
- **THEN** 二次确认"确认回收该勋章？此操作不可撤销"，确认后调用 POST `/api/v1/content/user/growth/badge/recycle` API（实际端点）

#### Scenario: Admin badge management page
- **WHEN** 管理员访问 `/content/badge-manage`
- **THEN** 展示查询表单（用户ID/用户名、勋章名称）和数据表格（用户、勋章、获得时间、状态、操作）
