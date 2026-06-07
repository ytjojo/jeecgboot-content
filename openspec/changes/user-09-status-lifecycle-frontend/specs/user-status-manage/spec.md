## ADDED Requirements

### Requirement: 管理员查询用户状态列表

管理员 SHALL 能通过用户状态管理页查询用户状态列表，支持按用户ID/用户名、状态下拉筛选。查询响应时间 MUST 小于 100ms。

#### Scenario: 按用户ID查询
- **WHEN** 管理员输入用户ID并点击查询
- **THEN** 列表展示该用户的当前状态、状态开始时间、状态结束时间、状态原因、操作人

#### Scenario: 按状态筛选
- **WHEN** 管理员选择状态下拉选项（如"禁言"）并点击查询
- **THEN** 列表仅展示处于该状态的用户

#### Scenario: 查询不存在的用户
- **WHEN** 管理员输入不存在的用户ID并查询
- **THEN** 提示"用户不存在"

#### Scenario: 重置查询条件
- **WHEN** 管理员点击重置按钮
- **THEN** 清空所有查询条件并重新加载列表

#### Scenario: 查询结果为空
- **WHEN** 查询条件匹配不到任何用户
- **THEN** 展示空状态提示"暂无数据"，不显示分页器

#### Scenario: 查询接口超时
- **WHEN** 查询 API 请求超时（>5s）
- **THEN** 提示"请求超时，请稍后重试"，显示重试按钮

---

### Requirement: 管理员变更用户状态

管理员 SHALL 能通过状态变更弹窗变更用户状态。弹窗 MUST 根据当前状态调用 API 获取可转换状态列表，下拉框仅显示允许转换的目标状态。提交前 MUST 进行二次确认。

#### Scenario: 打开状态变更弹窗
- **WHEN** 管理员点击某用户的"变更状态"按钮
- **THEN** 弹出状态变更弹窗，调用 GET /api/v1/content/user-status/transitions/{currentStatus} 获取可转换状态列表，下拉框仅渲染返回的状态

#### Scenario: 禁言操作
- **WHEN** 管理员选择目标状态为"禁言"，填写原因和期限，点击确定
- **THEN** 弹出二次确认弹窗"确定将用户 XXX 变更为禁言状态？"，确认后提交 API，成功后刷新列表并提示"状态变更成功"

#### Scenario: 封禁操作含永久选项
- **WHEN** 管理员选择目标状态为"封禁"
- **THEN** 表单显示期限选择器和永久封禁复选框

#### Scenario: 非法状态转换
- **WHEN** 后端返回非法状态转换错误
- **THEN** 提示"当前状态不允许执行此操作"

#### Scenario: 并发冲突
- **WHEN** 两个管理员同时变更同一用户状态，后端返回乐观锁冲突
- **THEN** 提示"状态已变更，请刷新后重试"

#### Scenario: 无可转换状态
- **WHEN** 当前状态的可转换状态列表为空
- **THEN** 禁用"变更状态"按钮并提示"当前状态无可执行的转换操作"

---

### Requirement: 管理员解禁用户

管理员 SHALL 能对被处罚用户执行解禁操作。解禁 MUST 填写解禁原因，操作记录写入审计日志，解禁成功后 MUST 发送站内通知给用户。

#### Scenario: 单个解禁
- **WHEN** 管理员点击某用户的"解禁"按钮，填写解禁原因，确认解禁
- **THEN** 用户状态恢复为"正常"，操作记录写入审计日志，发送站内通知给用户

#### Scenario: 批量解禁
- **WHEN** 管理员选中多个用户，点击"批量解禁"，填写解禁原因
- **THEN** 按钮显示 Loading 状态，所有选中用户状态恢复为"正常"，操作记录写入审计日志，成功后提示"批量解禁成功"

#### Scenario: 批量解禁防重复提交
- **WHEN** 管理员快速多次点击"批量解禁"按钮
- **THEN** 按钮在请求期间禁用，防止重复提交

#### Scenario: 批量解禁部分失败
- **WHEN** 批量解禁 API 返回部分失败
- **THEN** 提示"X 个用户解禁成功，Y 个用户解禁失败"，失败用户保持原状态

---

### Requirement: 管理员查看用户状态历史

管理员 SHALL 能通过抽屉形式查看用户状态变更历史，按时间倒序排列。

#### Scenario: 打开状态历史抽屉
- **WHEN** 管理员点击某用户的"状态历史"按钮
- **THEN** 打开抽屉，展示该用户的状态变更历史列表（变更时间、原状态、新状态、操作人、触发原因）

---

### Requirement: 用户状态管理页响应式布局

用户状态管理页 MUST 支持移动端/平板/桌面端响应式布局。

#### Scenario: 移动端访问
- **WHEN** 在 <768px 宽度设备访问
- **THEN** 表格改为卡片列表，操作按钮改为下拉菜单，筛选条件折叠

#### Scenario: 平板访问
- **WHEN** 在 768px-1200px 宽度设备访问
- **THEN** 表格精简列数

---

## 后端 API 依赖

本需求依赖以下后端 API，**全部已实现**（2026-06-07 确认，UserStatusController.java）：

| API | 路径 | 用途 | 状态 |
|-----|------|------|------|
| getUserStatus | GET /api/v1/content/user-status/{userId} | 查询指定用户状态 | ✅ 已实现 |
| changeUserStatus | POST /api/v1/content/user-status/{userId}/change | 变更用户状态 | ✅ 已实现 |
| releaseUser | POST /api/v1/content/user-status/{userId}/release | 解禁用户 | ✅ 已实现 |
| getStatusHistory | GET /api/v1/content/user-status/{userId}/history | 查询状态历史 | ✅ 已实现 |
| getStatusList | GET /api/v1/content/user-status/list | 分页查询用户状态列表 | ✅ 已实现 |
| getTransitions | GET /api/v1/content/user-status/transitions/{currentStatus} | 获取可转换状态列表 | ✅ 已实现 |
| batchReleaseUsers | POST /api/v1/content/user-status/batch-release | 批量解禁 | ✅ 已实现 |
