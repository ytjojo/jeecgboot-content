## ADDED Requirements

### Requirement: 用户状态枚举定义
系统 SHALL 定义 9 种用户状态枚举值，每个状态具有唯一标识、显示名称和描述。

#### Scenario: 状态枚举完整性
- GIVEN: 用户状态枚举类 UserStatusEnum
- WHEN: 系统初始化
- THEN: 枚举包含 GUEST（游客）、REGISTERED_INCOMPLETE（已注册未完善资料）、NORMAL（正常）、MUTED（禁言）、RESTRICTED_RECOMMEND（限制推荐）、FROZEN（冻结）、BANNED（封禁）、DEACTIVATING（注销中）、DEACTIVATED（已注销）共 9 个值

#### Scenario: 状态元数据
- GIVEN: 任意用户状态枚举值
- WHEN: 获取状态元数据
- THEN: 返回状态码（整数）、状态名称（英文）、显示名称（中文）、状态描述

### Requirement: 用户状态字段扩展
系统 SHALL 在用户表中增加用户状态相关字段，用于存储当前状态和状态元数据。

#### Scenario: 用户表字段
- GIVEN: 用户表 user 或内容社区用户表
- WHEN: 表结构定义
- THEN: 包含 user_status（状态枚举值）、status_start_time（状态开始时间）、status_end_time（状态结束时间，可为空表示永久）、status_reason（状态原因）、status_operator_id（操作人 ID）字段

#### Scenario: 默认状态
- GIVEN: 新注册用户
- WHEN: 用户完成注册
- THEN: 用户状态默认为 NORMAL（正常）

### Requirement: 状态查询接口
系统 SHALL 提供用户状态查询接口，返回用户当前状态及状态详情。

#### Scenario: 查询当前状态
- GIVEN: 已登录用户
- WHEN: 调用 GET /api/content/user-status/current
- THEN: 返回当前用户的状态枚举值、状态开始时间、状态结束时间、状态原因

#### Scenario: 查询指定用户状态（管理员）
- GIVEN: 管理员用户
- WHEN: 调用 GET /api/content/user-status/{userId}
- THEN: 返回指定用户的状态详情
