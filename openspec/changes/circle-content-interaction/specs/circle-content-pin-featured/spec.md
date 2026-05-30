## ADDED Requirements

### Requirement: 内容置顶

系统 SHALL 支持圈子版主或创建者对圈子内容执行置顶操作。置顶内容 MUST 始终排列在非置顶内容之前。多条置顶内容 MUST 按最近置顶时间优先展示。取消置顶后，该内容 MUST 按普通内容排序规则展示。

#### Scenario: 版主置顶内容
- **WHEN** 圈子版主对某内容执行置顶操作
- **THEN** 该内容的 is_pinned 字段设为 true，pinned_at 设为当前时间，内容在圈子内容列表中排列在非置顶内容之前

#### Scenario: 取消置顶
- **WHEN** 已置顶内容再次被执行置顶操作
- **THEN** 该内容的 is_pinned 字段设为 false，pinned_at 清空，内容按普通排序规则展示

#### Scenario: 多条置顶排序
- **WHEN** 圈子内有多条置顶内容
- **THEN** 所有置顶内容排列在非置顶内容之前，置顶内容之间按 pinned_at 倒序排列（最近置顶的在前）

#### Scenario: 普通成员尝试置顶
- **WHEN** 普通成员尝试对内容执行置顶操作
- **THEN** 系统返回"权限不足"提示，操作被拒绝

---

### Requirement: 内容精华标记

系统 SHALL 支持圈子版主或创建者对圈子内容设置精华标记。精华内容 MUST 在列表和详情页有特殊展示标识。取消精华后，标识 MUST 移除。

#### Scenario: 版主标记精华
- **WHEN** 圈子版主对某内容设置精华
- **THEN** 该内容的 is_featured 字段设为 true，featured_at 设为当前时间，内容获得精华标识

#### Scenario: 取消精华标记
- **WHEN** 已精华内容再次被执行精华操作
- **THEN** 该内容的 is_featured 字段设为 false，featured_at 清空，精华标识移除

#### Scenario: 精华标识一致性
- **WHEN** 内容被标记为精华
- **THEN** 该内容在列表页、详情页和后续浏览中 MUST 一致展示精华标识

#### Scenario: 普通成员尝试精华操作
- **WHEN** 普通成员尝试对内容设置精华
- **THEN** 系统返回"权限不足"提示，操作被拒绝

---

### Requirement: 审核日志记录

系统 MUST 记录所有置顶/精华操作的审核日志，包含操作人、操作时间、操作对象、操作类型和操作结果。记录 MUST 保留不少于 180 天。

#### Scenario: 置顶操作写入日志
- **WHEN** 版主执行置顶或取消置顶操作
- **THEN** 系统在 circle_audit_log 表写入一条记录，包含 operator_id、action(PIN/UNPIN)、target_id、target_type、result、created_at

#### Scenario: 精华操作写入日志
- **WHEN** 版主执行精华或取消精华操作
- **THEN** 系统在 circle_audit_log 表写入一条记录，包含 operator_id、action(FEATURE/UNFEATURE)、target_id、target_type、result、created_at
