## ADDED Requirements

### Requirement: useFeedbackStore SHALL 管理举报列表状态

useFeedbackStore SHALL 维护举报列表数据（reportList）、总数（reportTotal）和查询参数（reportQuery）。SHALL 支持分页查询、状态筛选、类型筛选。

#### Scenario: 加载举报列表
- **WHEN** 用户进入"我的举报"页面
- **THEN** Store 调用 API 加载举报列表，更新 reportList 和 reportTotal

#### Scenario: 筛选举报列表
- **WHEN** 用户修改筛选条件
- **THEN** Store 更新 reportQuery 并重新查询

---

### Requirement: useFeedbackStore SHALL 管理申诉列表状态

useFeedbackStore SHALL 维护申诉列表数据（appealList）、总数（appealTotal）和查询参数（appealQuery）。SHALL 支持分页查询、状态筛选。

#### Scenario: 加载申诉列表
- **WHEN** 用户进入"我的申诉"页面
- **THEN** Store 调用 API 加载申诉列表，更新 appealList 和 appealTotal

---

### Requirement: useFeedbackStore SHALL 管理客服会话状态

useFeedbackStore SHALL 维护当前会话（currentSession）、消息列表（chatMessages）、排队位置（queuePosition）、WebSocket 连接状态（wsConnected）和重连状态（reconnecting）。

#### Scenario: 建立客服会话
- **WHEN** 用户进入客服对话页
- **THEN** Store 创建会话，更新 currentSession，初始化 chatMessages

#### Scenario: 排队状态更新
- **WHEN** 用户加入人工客服队列
- **THEN** Store 更新 queuePosition

#### Scenario: WebSocket 状态变更
- **WHEN** WebSocket 连接状态变化
- **THEN** Store 更新 wsConnected 和 reconnecting 状态

---

### Requirement: useFeedbackStore SHALL 管理统计数据

useFeedbackStore SHALL 维护待处理举报数（pendingReportCount）和待处理申诉数（pendingAppealCount），用于个人中心菜单角标显示。

#### Scenario: 更新统计数据
- **WHEN** 举报或申诉状态变更
- **THEN** Store 重新获取统计数据，更新 pendingReportCount 和 pendingAppealCount

#### Scenario: 菜单角标显示
- **WHEN** 个人中心菜单渲染
- **THEN** 读取 Store 中的 pendingReportCount 和 pendingAppealCount 显示角标
