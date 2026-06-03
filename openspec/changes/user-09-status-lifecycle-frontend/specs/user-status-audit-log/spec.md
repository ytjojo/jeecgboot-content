## ADDED Requirements

### Requirement: 管理员查看审计日志列表

管理员 SHALL 能通过审计日志页查看用户状态变更的审计日志列表，按时间倒序排列，支持按用户ID、时间范围、操作类型筛选。

#### Scenario: 查看审计日志列表
- **WHEN** 管理员访问审计日志页
- **THEN** 展示审计日志列表，每行包含：日志ID、用户ID、原状态（Tag）、新状态（Tag）、操作人、操作类型、触发原因、操作时间

#### Scenario: 按用户ID筛选
- **WHEN** 管理员输入用户ID并点击查询
- **THEN** 列表仅展示该用户的审计日志

#### Scenario: 按时间范围筛选
- **WHEN** 管理员选择时间范围并点击查询
- **THEN** 列表仅展示该时间范围内的审计日志

#### Scenario: 按操作类型筛选
- **WHEN** 管理员选择操作类型（系统自动/管理员手动）并查询
- **THEN** 列表仅展示该操作类型的审计日志

---

### Requirement: 管理员查看审计日志详情

管理员 SHALL 能通过点击日志行查看审计日志完整字段信息。

#### Scenario: 查看日志详情
- **WHEN** 管理员点击某条日志的"查看详情"按钮
- **THEN** 弹出详情弹窗，展示完整字段：log_id、user_id、from_status、to_status、operator_id、operator_type、trigger_reason、rule_id、start_time、end_time、remark、ip_address、created_at

---

### Requirement: 管理员导出审计日志

管理员 SHALL 能导出审计日志为 Excel/CSV 文件。导出 MUST 由后端生成文件流，前端通过 blob 下载。

#### Scenario: 导出审计日志
- **WHEN** 管理员点击"导出"按钮
- **THEN** 调用导出 API，后端生成文件流，前端展示 Loading 状态，下载完成后提示导出成功

---

### Requirement: 审计日志只读

审计日志 MUST 为只读，不可修改或删除。

#### Scenario: 审计日志无编辑/删除入口
- **WHEN** 管理员查看审计日志列表
- **THEN** 不显示编辑或删除按钮
