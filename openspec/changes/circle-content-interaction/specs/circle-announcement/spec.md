## ADDED Requirements

### Requirement: 发布圈子公告

系统 SHALL 支持圈子版主或创建者发布公告。公告 MUST 展示在圈子顶部，所有成员可见。同一圈子同一时间 MUST 仅展示一条生效公告，新公告发布后替换旧公告展示位置。公告 MUST 支持有效期设置。

#### Scenario: 版主发布公告
- **WHEN** 圈子版主编写公告内容并发布
- **THEN** 系统创建公告记录，status 设为 ACTIVE，公告展示在圈子顶部

#### Scenario: 新公告替换旧公告
- **WHEN** 圈子已有生效公告，版主发布新公告
- **THEN** 旧公告 status 更新为 INACTIVE，新公告成为当前生效公告

#### Scenario: 公告有效期过期
- **WHEN** 公告到达有效期截止时间
- **THEN** 公告 status 自动更新为 INACTIVE，不再展示在圈子顶部

#### Scenario: 普通成员尝试发布公告
- **WHEN** 普通成员尝试发布公告
- **THEN** 系统返回"权限不足"提示，操作被拒绝

#### Scenario: 查询当前公告
- **WHEN** 成员进入圈子页面
- **THEN** 系统返回该圈子 status 为 ACTIVE 且未过期的唯一公告

---

### Requirement: 公告审核日志

系统 MUST 记录公告发布操作的审核日志，包含操作人、操作时间、操作对象、操作类型和操作结果。记录 MUST 保留不少于 180 天。

#### Scenario: 公告发布写入日志
- **WHEN** 版主发布公告
- **THEN** 系统在 circle_audit_log 表写入一条记录，包含 operator_id、action(PUBLISH_ANNOUNCEMENT)、target_id(circle_id)、result、created_at
