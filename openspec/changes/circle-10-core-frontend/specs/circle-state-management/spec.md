## ADDED Requirements

### Requirement: useCircleStore 状态管理
系统 SHALL 提供 `useCircleStore`（Pinia）管理圈子模块的共享状态，包括当前圈子详情（currentCircle）、用户角色（currentRole）、成员状态（currentMemberStatus）、搜索关键词（searchKeyword）。

#### Scenario: 进入详情页加载状态
> **后端接口**: `GET /api/v1/content/circle/detail?id={id}` 已实现。当前 CircleVO 提供 `joined` 和 `myRole` 字段，可用于 `currentMemberStatus` 和 `currentRole` 状态填充。`applyStatus` 和 `isInvited` 字段待后端补充。

- **WHEN** 用户进入圈子详情页
- **THEN** 请求详情接口，将 `currentCircle`、`currentRole`、`currentMemberStatus` 写入 Store

#### Scenario: 离开详情页清理状态
- **WHEN** 用户离开圈子详情页（路由变更）
- **THEN** 清空 `currentCircle`、`currentRole`、`currentMemberStatus`

#### Scenario: 退出圈子后清理状态
- **WHEN** 用户执行退出操作成功
- **THEN** 清空 Store 中的 `currentRole` 和 `currentMemberStatus` 为 `null`

#### Scenario: 角色变更后刷新状态
- **WHEN** 执行设置/取消版主操作成功
- **THEN** 重新加载圈子详情，更新 `currentRole`

#### Scenario: 禁言/解除禁言后刷新状态
- **WHEN** 执行禁言/解除禁言操作成功
- **THEN** 重新加载圈子详情，更新 `currentMemberStatus`

### Requirement: 搜索关键词管理
系统 SHALL 通过 `searchKeyword` 管理搜索关键词，不持久化，从 URL 参数 `q` 读取。

#### Scenario: 搜索时写入关键词
- **WHEN** 用户执行搜索
- **THEN** 将关键词写入 Store 的 `searchKeyword`

#### Scenario: 从详情页返回搜索页
- **WHEN** 用户从详情页返回搜索结果页
- **THEN** 从 URL 参数 `q` 重新读取关键词，不依赖 Store 缓存

### Requirement: 列表缓存策略
系统 SHALL 对「已加入」Tab 和「发现」Tab 的数据独立缓存，Tab 切换时保留已加载数据，超过 5 分钟自动刷新。加入/退出操作后同时刷新两个 Tab。

#### Scenario: Tab 切换保留数据
- **WHEN** 用户在「已加入」和「发现」Tab 之间切换
- **THEN** 保留已加载数据，不重新请求

#### Scenario: 缓存过期刷新
- **WHEN** Tab 数据超过 5 分钟未刷新
- **THEN** 切换到该 Tab 时自动重新请求数据

#### Scenario: 操作后双 Tab 刷新
- **WHEN** 用户执行加入或退出操作
- **THEN** 同时刷新「已加入」和「发现」两个 Tab 的数据

### Requirement: 权限判断逻辑
系统 SHALL 提供前端权限判断逻辑，配合后端校验。包括 `canManageMember`（创建者或版主）、`canManageRole`（仅创建者）、`canMute`（创建者可禁言所有，版主仅可禁言普通成员）。

#### Scenario: 创建者权限
- **WHEN** `currentRole === 'CREATOR'`
- **THEN** `canManageMember`、`canManageRole`、`canMute` 均为 true

#### Scenario: 版主权限
- **WHEN** `currentRole === 'MODERATOR'`
- **THEN** `canManageMember` 为 true，`canManageRole` 为 false，`canMute` 仅对普通成员为 true

#### Scenario: 普通成员权限
- **WHEN** `currentRole === 'MEMBER'`
- **THEN** `canManageMember`、`canManageRole`、`canMute` 均为 false

### Requirement: 并发竞态处理
系统 SHALL 在并发操作竞态时进行降级处理。操作失败时 Toast 提示 "操作失败，该成员状态已变更" 并自动刷新成员列表。

#### Scenario: 管理员操作冲突
- **WHEN** 两个管理员同时对同一成员执行禁言/移除
- **THEN** 一方操作失败，Toast 提示 "操作失败，该成员状态已变更"，自动刷新成员列表

#### Scenario: 圈子隐私类型变更
- **WHEN** 用户在详情页停留期间圈子隐私类型被修改
- **THEN** 切换 Tab 或执行加入操作时重新拉取圈子详情，刷新页面状态

#### Scenario: 并发加入满员
- **WHEN** 多人同时点击加入且圈子即将满员
- **THEN** 后端以实际成员数为准，超限返回满员错误码，前端 Toast 提示 "圈子已满员，无法加入"

### Requirement: 国际化文案管理
系统 SHALL 将所有用户可见文案统一管理在 `src/locales/lang/zh-CN/circle.ts` 常量文件中，不散落在模板中。预留 `t()` 翻译函数接口。

#### Scenario: 文案引用
- **WHEN** 组件需要展示用户可见文案
- **THEN** 从常量文件导入文案，不硬编码在模板中

#### Scenario: 预留国际化接口
- **WHEN** 后续需要国际化支持
- **THEN** 常量文件可直接替换为 `t()` 调用，无需大规模改造
