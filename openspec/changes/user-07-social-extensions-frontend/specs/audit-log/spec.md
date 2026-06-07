## ADDED Requirements

### Requirement: Audit log page for administrators
The system SHALL provide an audit log page at `/system/audit-log` accessible only to users with admin role, displaying governance operation logs.

#### Scenario: Admin views audit log page
- **WHEN** an admin user navigates to `/system/audit-log`
- **THEN** the system SHALL display a query filter area and a JVxeTable with columns: operator, operation time, operation type, target user, reason, IP address

#### Scenario: Non-admin access denied
- **WHEN** a non-admin user attempts to access `/system/audit-log`
- **THEN** the system SHALL display a 403 forbidden page

### Requirement: Audit log filtering
The system SHALL support filtering audit logs by operator, operation type, and time range.

#### Scenario: Filter by operation type
- **WHEN** user selects an operation type from the dropdown (删除评论/警告/封禁/禁言/撤销)
- **THEN** the system SHALL filter the log list to show only matching entries

#### Scenario: Filter by time range
- **WHEN** user selects a time range using RangePicker
- **THEN** the system SHALL filter the log list to show only entries within the selected time range

#### Scenario: Filter by operator
- **WHEN** user types an operator name in the search field
- **THEN** the system SHALL filter the log list by operator

#### Scenario: Reset filters
- **WHEN** user clicks the "重置" (reset) button
- **THEN** the system SHALL clear all filters and show the full log list

### Requirement: Audit log immutability
Audit log entries SHALL be read-only and cannot be deleted or edited.

#### Scenario: No edit or delete actions on audit logs
- **WHEN** admin views the audit log list
- **THEN** there SHALL be no edit or delete buttons or actions available for log entries

### Requirement: Audit log API integration
The system SHALL integrate the audit log API using `defHttp` encapsulation.

#### Scenario: Fetch audit log list
- **WHEN** the audit log page loads or user applies filters
- **THEN** the system SHALL call the audit log query API with filter params and pagination
- **NOTE**: 后端需补充治理操作审计日志查询接口，详见 backend-issues.md

## API 封装

API 文件: `src/api/content/governance.ts`（复用已有 governance 封装）

| 端点 | 方法 | 参数 | 响应关键字段 | 状态 |
|------|------|------|------------|------|
| `/content/user/governance/audit-log` | GET | @RequestParam: page, pageSize, operatorName?, operationType?, startTime?, endTime? | records[{operator, operationTime, operationType, targetUser, reason, ipAddress}], total | ❌ 后端待补充 |
