## ADDED Requirements

### Requirement: 客服入口 SHALL 在所有页面可见

系统 SHALL 在页面右下角添加悬浮客服按钮，所有页面可见。点击后 SHALL 进入客服对话页。

#### Scenario: 点击客服入口
- **WHEN** 用户点击页面右下角的悬浮客服按钮
- **THEN** 进入客服对话页，自动启动智能客服机器人

---

### Requirement: 客服对话 SHALL 首先启动智能客服机器人

进入客服页面后 SHALL 自动启动智能客服机器人对话界面。智能客服阶段 SHALL 显示欢迎消息和快捷回复按钮（常见问题分类）。

#### Scenario: 进入客服对话
- **WHEN** 用户进入客服对话页
- **THEN** 自动启动智能客服，显示欢迎消息和快捷回复按钮

#### Scenario: 使用快捷回复
- **WHEN** 用户点击快捷回复按钮
- **THEN** 发送对应问题，智能客服回复相关内容

---

### Requirement: 客服 SHALL 支持转人工客服排队

点击"转人工"后 SHALL 加入人工客服队列，显示排队等待提示和预计等待时间。高等级用户（LV.15+） SHALL 排在优先位置，预计等待 <2 分钟；普通用户 <5 分钟。高等级用户 SHALL 显示"优先排队中"标识。

#### Scenario: 普通用户转人工
- **WHEN** 普通用户点击"转人工"
- **THEN** 显示排队等待提示和预计等待时间

#### Scenario: 高等级用户转人工
- **WHEN** LV.15+ 用户点击"转人工"
- **THEN** 显示"优先排队中"标识和预计等待时间（<2分钟）

#### Scenario: 人工客服接入
- **WHEN** 人工客服接入会话
- **THEN** 系统消息显示"人工客服已接入"，"转人工"按钮隐藏

---

### Requirement: 排队状态 SHALL 持久化并支持离开提醒

排队状态 SHALL 持久化到 sessionStorage，刷新页面后 SHALL 自动恢复排队状态并重新连接 WebSocket。客服入口悬浮按钮在排队期间 SHALL 显示排队状态徽标（如"排队中 #3"）。排队期间用户点击页面内导航时 SHALL 弹出确认弹窗"您正在排队中，离开将取消排队，确认离开？"。

#### Scenario: 刷新页面恢复排队
- **WHEN** 用户在排队中刷新页面
- **THEN** 自动恢复排队状态，重新连接 WebSocket

#### Scenario: 排队期间离开页面
- **WHEN** 用户在排队中点击页面内导航
- **THEN** 弹出确认弹窗"您正在排队中，离开将取消排队，确认离开？"

#### Scenario: 排队状态徽标
- **WHEN** 用户正在排队
- **THEN** 客服入口悬浮按钮显示"排队中 #N"徽标

---

### Requirement: 客服对话 SHALL 支持实时消息收发

人工客服接入后 SHALL 支持实时对话。消息类型 SHALL 支持文本、图片、链接。新消息 SHALL 自动滚动到底部。消息 SHALL 使用气泡样式：智能客服消息左侧、用户消息右侧、系统消息居中。

#### Scenario: 发送文本消息
- **WHEN** 用户输入文本并点击发送
- **THEN** 消息以气泡形式显示在右侧，自动滚动到底部

#### Scenario: 接收客服消息
- **WHEN** 客服发送消息
- **THEN** 消息以气泡形式显示在左侧，自动滚动到底部

---

### Requirement: 消息发送失败 SHALL 支持重试

消息发送失败时 SHALL 在气泡旁显示红色感叹号图标 + "重试"文字按钮。消息发送中 SHALL 显示 loading 转圈图标，气泡半透明。消息状态流转：发送中（灰色气泡+loading） -> 已发送（正常气泡） -> 发送失败（红色感叹号+重试）。

#### Scenario: 消息发送失败
- **WHEN** 消息发送失败
- **THEN** 气泡旁显示红色感叹号和"重试"按钮

#### Scenario: 重试发送失败消息
- **WHEN** 用户点击"重试"按钮
- **THEN** 重新发送消息，状态变为发送中

---

### Requirement: WebSocket 断连 SHALL 有明确的状态反馈

WebSocket 断连时 SHALL 在顶部显示"连接已断开，正在重连..."黄色横幅，输入框禁用。重连成功后横幅 SHALL 自动消失，自动重新发送所有失败消息，输入框恢复可用。超过 30 秒无法重连时横幅 SHALL 变为红色，提示"连接失败，请刷新页面重试"。

#### Scenario: WebSocket 断连
- **WHEN** WebSocket 连接断开
- **THEN** 顶部显示黄色横幅"连接已断开，正在重连..."，输入框禁用

#### Scenario: WebSocket 重连成功
- **WHEN** WebSocket 重新连接成功
- **THEN** 横幅消失，自动重发失败消息，输入框恢复可用

#### Scenario: 长时间无法重连
- **WHEN** 断连超过 30 秒
- **THEN** 横幅变为红色，提示"连接失败，请刷新页面重试"，附带"刷新页面"按钮

#### Scenario: 排队期间断连
- **WHEN** 排队期间 WebSocket 断连
- **THEN** 自动重新加入队列并恢复排队位置，顶部提示"连接已恢复，您仍在排队中"

---

### Requirement: 会话结束后 SHALL 弹出评分弹窗

客服结束会话后系统消息显示"会话已结束"，SHALL 弹出评分 Modal。评分 SHALL 包含星级评分（1-5星）和评价输入框。评分后不可再次评分。

#### Scenario: 会话结束评分
- **WHEN** 客服结束会话
- **THEN** 弹出评分 Modal，包含星级评分和评价输入框

#### Scenario: 提交评分
- **WHEN** 用户选择星级并提交评分
- **THEN** 评分提交成功，弹窗关闭

#### Scenario: 重复评分
- **WHEN** 用户已评分后再次查看会话
- **THEN** 不再弹出评分弹窗

---

### Requirement: 客服历史记录页 SHALL 展示历史会话列表

"客服记录"页面 SHALL 按时间倒序展示历史会话列表，每条记录显示：会话时间、客服类型（智能/人工）、问题摘要、状态（进行中/已结束）、操作（查看详情/继续咨询）。超过 30 天的会话 SHALL 提示"历史记录仅保留 30 天"。

#### Scenario: 查看客服历史
- **WHEN** 用户进入"客服记录"页面
- **THEN** 页面展示历史会话列表，按时间倒序排列

#### Scenario: 查看会话详情
- **WHEN** 用户点击某条会话的"查看详情"
- **THEN** 打开 Drawer 展示完整对话记录

#### Scenario: 继续咨询
- **WHEN** 用户点击"继续咨询"
- **THEN** 创建新会话并关联历史记录

#### Scenario: 查看过期会话
- **WHEN** 用户查看超过 30 天的会话
- **THEN** 提示"历史记录仅保留 30 天"

---

### Requirement: 移动端客服对话 SHALL 全屏展示

移动端（<768px）客服对话页 SHALL 全屏展示，顶部显示返回按钮。点击返回 SHALL 最小化会话（不结束），客服入口悬浮按钮 SHALL 显示绿色圆点表示进行中。

#### Scenario: 移动端进入客服
- **WHEN** 用户在移动端进入客服对话页
- **THEN** 页面全屏展示，顶部显示返回按钮

#### Scenario: 移动端点击返回
- **WHEN** 用户在移动端点击返回按钮
- **THEN** 会话最小化（不结束），返回上一页，客服入口显示绿色圆点

#### Scenario: 重新进入最小化会话
- **WHEN** 用户点击带绿色圆点的客服入口
- **THEN** 重新进入未结束的会话

---

## 后端依赖

| API | 后端状态 | 说明 |
|-----|---------|------|
| `GET /content/user/support/customer-service` | 已存在 | 返回客服入口信息（routeType/title/description/manualSupported），非会话创建 |
| `POST /content/user/support/customer-service/session` | 服务层已实现 | `createServiceSession(userId, sessionType)` 已实现但未暴露 HTTP 端点 |
| `POST .../session/{id}/transfer` | 完全缺失 | 转人工功能后端未实现 |
| `POST .../session/{id}/message` | 完全缺失 | 消息发送需 WebSocket 实现，后端无此端点 |
| `POST .../session/{id}/close` | 完全缺失 | 结束会话后端未实现 |
| `POST .../session/{id}/rating` | 服务层已实现 | `rateService(userId, sessionId, rating, comment)` 已实现但未暴露 HTTP 端点 |
| `GET .../customer-service/sessions` | 服务层已实现 | `listServiceSessions(req)` 已实现但未暴露 HTTP 端点 |
| `GET .../session/{id}` | 完全缺失 | 会话详情（含消息）后端未实现 |

**数据结构差异**:
- 前端期望 `ServiceSession` 含 `type, status, agentName, queuePosition, estimatedWaitTime`
- 后端 `ContentServiceSessionVO` 实际含 `sessionType, status, rating, ratingComment, startTime, endTime, expired`（无 agentName/queuePosition/estimatedWaitTime）

**WebSocket**: 后端未定义 WebSocket 端点和消息协议，需要后端新增实现
