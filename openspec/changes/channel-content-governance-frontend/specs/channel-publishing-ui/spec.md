## ADDED Requirements

### Requirement: ChannelSelector 频道选择组件

系统 SHALL 提供 ChannelSelector 组件，用于内容发布时选择目标频道。组件 SHALL 支持搜索、多选、权限状态预览，并按用户角色分组展示频道列表。

#### Scenario: 加载可发布频道列表
- **WHEN** 用户打开 ChannelSelector
- **THEN** 调用 `/api/channel/publish/available` 获取频道列表，按"推荐频道"、"我管理的频道"、"我加入的频道"三组展示，每个频道卡片展示频道名称、类型标签、用户角色标签和发布结果预期

#### Scenario: 搜索频道
- **WHEN** 用户在搜索框输入频道名称
- **THEN** 使用 300ms 防抖，模糊匹配频道名称，展示搜索结果列表；无结果时展示"未找到匹配的频道"

#### Scenario: 选择频道并达到上限
- **WHEN** 用户选择频道，已选频道数达到接口返回的 `maxChannelCount` 上限
- **THEN** 不可选频道展示"已达上限"提示，阻止继续添加

#### Scenario: 不可发布频道展示
- **WHEN** 频道列表中包含用户不可发布的频道（如仅管理员可发布但用户是普通成员）
- **THEN** 该频道卡片置灰不可选，hover 展示具体不可发布原因

#### Scenario: 重复发布检测
- **WHEN** 内容已在某频道发布
- **THEN** 该频道卡片展示"已在此频道"标识并置灰不可选

#### Scenario: 空状态
- **WHEN** 用户无可发布的频道
- **THEN** 展示引导文案"暂无可发布的频道，去加入或创建频道"

#### Scenario: 频道数超过 50 时启用虚拟滚动
- **WHEN** 可发布频道数超过 50
- **THEN** 列表启用虚拟滚动优化渲染性能

---

### Requirement: PublishResult 发布结果反馈组件

系统 SHALL 提供 PublishResult 组件，在内容发布提交后逐频道展示发布结果状态。

#### Scenario: 展示逐频道发布结果
- **WHEN** 用户提交多频道发布后
- **THEN** 逐频道展示结果状态（成功/待审/失败），失败项展示具体原因和建议操作

#### Scenario: 重试失败项
- **WHEN** 用户点击失败项的"重试"按钮
- **THEN** 仅重试原来的失败频道，不可更换目标频道；重试中展示 loading 状态

#### Scenario: 定时发布结果展示
- **WHEN** 用户设置了定时发布
- **THEN** 展示"已设定发布时间：YYYY-MM-DD HH:mm"，不展示即时发布结果

---

### Requirement: 定时发布管理

系统 SHALL 支持定时发布设置和"我的定时发布"任务管理。

#### Scenario: 设置定时发布时间
- **WHEN** 用户在发布页开启定时发布并选择未来时间
- **THEN** 展示日期时间选择器，时间范围限制为当前时间到 30 天内

#### Scenario: 查看我的定时发布列表
- **WHEN** 用户进入"我的定时发布"入口
- **THEN** 展示当前用户所有待执行的定时发布任务列表（标题、目标频道、计划发布时间、状态），每条支持"编辑时间"和"取消发布"操作

#### Scenario: 编辑定时发布时间
- **WHEN** 用户点击"编辑时间"
- **THEN** 展示时间选择器，用户选择新时间后调用 `/api/channel/publish/scheduled/{id}` PUT 接口更新

#### Scenario: 取消定时发布
- **WHEN** 用户点击"取消发布"
- **THEN** 展示二次确认弹窗，确认后调用 DELETE 接口取消任务

#### Scenario: 定时发布期间用户被禁言
- **WHEN** 到达发布时间时用户已被禁言
- **THEN** 前端通过站内消息通知用户"定时发布已取消：您在频道 {channelName} 的定时发布因禁言被取消"

---

### Requirement: 发布权限模型配置

系统 SHALL 提供发布权限配置页面，支持四种权限模型切换和影响说明。

#### Scenario: 展示四种权限模型
- **WHEN** 频道主进入发布权限配置页
- **THEN** 以 RadioGroup 形式展示四种模式：仅管理员可发布、所有成员可发布、公开投稿、先审后发，每种模式附带说明文案

#### Scenario: 切换权限模型时展示影响说明
- **WHEN** 用户切换权限模型
- **THEN** 弹出影响说明弹窗，说明对普通成员、非成员和管理员的影响

#### Scenario: 保存权限配置
- **WHEN** 用户点击"保存"
- **THEN** 展示配置变更摘要供确认，确认后调用 API 保存

---

### Requirement: 发布限额配置

系统 SHALL 提供发布限额配置，支持每小时/每日发布上限和内容字数下限。

#### Scenario: 配置发布限额
- **WHEN** 频道主在发布权限配置页设置限额
- **THEN** 可配置每小时发布上限、每日发布上限、内容字数下限，值为 0 表示不限制

#### Scenario: 发布前预校验限额
- **WHEN** 用户提交发布前
- **THEN** 调用 `/api/channel/publish/limit/check` 预校验，超限时展示具体限制信息并阻止发布

---

### Requirement: useChannelPublishStore 发布状态管理

系统 SHALL 提供 `useChannelPublishStore` Pinia Store，管理频道发布相关的前端状态。

#### Scenario: 管理已选频道列表
- **WHEN** 用户选择或移除频道
- **THEN** Store 维护 `selectedChannels` 数组，提供 `addChannel` 和 `removeChannel` action

#### Scenario: 管理发布结果
- **WHEN** 发布提交后收到结果
- **THEN** Store 维护 `publishResult` Record（key 为 channelId），提供 `clearResult` action

#### Scenario: 管理定时发布任务
- **WHEN** 用户查看或操作定时发布任务
- **THEN** Store 提供 `fetchScheduledTasks`、`editScheduledTime`、`cancelScheduledTask` action
