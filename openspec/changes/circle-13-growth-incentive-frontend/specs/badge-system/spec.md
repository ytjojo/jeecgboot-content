## ADDED Requirements

### Requirement: 徽章墙页面展示

系统 SHALL 提供徽章墙页面，分组展示已获得和未获得徽章，已获得徽章图标点亮，未获得徽章图标灰色展示达成条件描述。后端 AchievementVO 提供完整字段：`achievementType`、`name`、`description`、`iconUrl`、`earned`、`earnedDate`、`conditionDesc`、`currentProgress`、`targetProgress`、`status`。前端 TypeScript 类型直接使用后端 VO 字段名，不做字段重命名映射。徽章图标直接使用后端返回的 `iconUrl` URL，无需本地兜底。

#### Scenario: 展示已获得徽章
- **WHEN** 用户进入徽章墙页
- **THEN** 「已获得」区域展示已点亮的徽章卡片，包含徽章图标（`iconUrl` 字段）、名称（`name` 字段）、获得日期（`earnedDate` 字段）

#### Scenario: 展示未获得徽章
- **WHEN** 用户进入徽章墙页
- **THEN** 「未获得」区域展示灰色徽章卡片，包含徽章图标（`iconUrl` 字段）、名称（`name` 字段）、达成条件描述（`conditionDesc` 字段）、进度（`currentProgress` / `targetProgress`）

#### Scenario: 即将达成徽章高亮
- **WHEN** 未获得徽章的 `conditionDesc` 包含进度信息且接近达成
- **THEN** 该徽章卡片加橙色边框高亮显示（`status === 'CLOSE'` 表示即将达成，进度 >= 80%）

#### Scenario: 徽章按圈子展示
- **WHEN** 用户进入徽章墙页
- **THEN** 页面顶部显示当前圈子名称，徽章仅展示当前圈子内的徽章

### Requirement: 徽章详情弹窗

系统 SHALL 支持点击单个徽章弹出详情弹窗（Modal），展示完整条件说明。后端 AchievementVO 提供完整字段：`earnedDate`（获得时间）、`currentProgress`（当前进度值）、`targetProgress`（目标值）、`status`（`EARNED` / `CLOSE` / `UNEARNED`）。已获得徽章展示获得时间和完整条件说明；未获得徽章展示结构化的进度条（`currentProgress` / `targetProgress`）。

#### Scenario: 查看已获得徽章详情
- **WHEN** 用户点击已获得的徽章卡片
- **THEN** 弹出 Modal 展示徽章名称（`name`）、图标（`iconUrl`）、获得时间（`earnedDate`）、完整条件说明（`description`）

#### Scenario: 查看未获得徽章详情
- **WHEN** 用户点击未获得的徽章卡片
- **THEN** 弹出 Modal 展示徽章名称（`name`）、图标（`iconUrl`）、达成条件描述（`conditionDesc`）、进度条（`currentProgress` / `targetProgress`）、达成状态（`status === 'CLOSE'` 显示「即将达成」）

### Requirement: 徽章种类定义

系统 SHALL 支持展示后端已定义的徽章种类。PRD 定义 6 种徽章（持续创作者、优质贡献者、活跃参与者、圈内新星、内容里程碑、社交达人），实际展示种类由后端 `GET /api/v1/content/circle/growth/achievement/list` 接口返回的数据决定。注意：数据库当前仅初始化 4 种徽章（ach_001-004），「内容里程碑」和「社交达人」待后端补充。

#### Scenario: 展示全部徽章
- **WHEN** 用户进入徽章墙页
- **THEN** 调用成就徽章列表接口，展示该圈子定义的全部徽章种类，已获得和未获得分组展示

### Requirement: 徽章自动获得通知

系统 SHALL 在获得新徽章后通过站内通知推送，并在页面右上角弹出 Toast 提示。

#### Scenario: 获得新徽章 Toast 提示
- **WHEN** 用户通过 WebSocket 收到新徽章获得通知
- **THEN** 页面右上角弹出 Toast 提示展示徽章名称，3 秒后自动消失

#### Scenario: 获得新徽章后刷新徽章数据
- **WHEN** 用户收到新徽章通知
- **THEN** 自动刷新当前页面的徽章列表数据

### Requirement: 徽章撤销状态展示

系统 SHALL 正确展示徽章撤销状态，撤销徽章在徽章墙中展示为灰色并标注「已撤销」。

#### Scenario: 内容违规导致徽章撤销
- **WHEN** 内容删除/违规导致徽章获得条件不再满足
- **THEN** 徽章标记为「已撤销」，在徽章墙中展示为灰色，不再计入已获得徽章总数

#### Scenario: 退出圈子导致徽章撤销
- **WHEN** 用户退出圈子
- **THEN** 该圈子内获得的所有徽章标记为「已撤销」

#### Scenario: 重新加入圈子后徽章状态
- **WHEN** 用户重新加入圈子
- **THEN** 已撤销徽章需重新满足条件才能再次获得

### Requirement: 徽章列表 API 对接

系统 SHALL 通过 GET `/api/v1/content/circle/growth/achievement/list?circleId={circleId}&userId={userId}` 接口获取成就徽章列表数据。

#### Scenario: 接口请求成功
- **WHEN** 徽章墙页加载
- **THEN** 调用成就徽章列表接口 `GET /api/v1/content/circle/growth/achievement/list`，解析 AchievementVO 数组并渲染到页面

#### Scenario: 接口请求失败
- **WHEN** 徽章列表接口请求失败
- **THEN** 展示错误提示和「重试」按钮

#### Scenario: 加载中状态
- **WHEN** 徽章列表接口请求中
- **THEN** 展示骨架屏占位
