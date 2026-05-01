# 内容社区用户域 - 举报处理流最小闭环设计

## 1. 背景

当前支持域已经具备：

- 用户创建举报
- 举报独立落库到 `content_user_report`
- 举报创建审计日志

但还缺少处理侧回写能力和用户侧结果查询能力，因此举报目前只能“提交”，无法形成“处理完成并回查”的闭环。

## 2. 目标

在不扩展完整后台工单流的前提下，补齐举报处理的最小闭环：

- 管理侧处理举报
- 用户侧查询举报处理进度与结果

## 3. 范围

### 3.1 包含

- 举报实体补最小处理结果字段
- 新增 admin 举报处理接口
- 新增用户侧举报进度查询接口
- 新增举报处理审计日志
- 增加 service 与 controller 测试

### 3.2 不包含

- 举报后台列表与筛选
- 举报处理中多阶段流转
- 举报详情页
- 完整客服工单能力

## 4. 数据设计

在 `content_user_report` / `ContentUserReport` 上补以下字段：

- `result_status`
- `result_note`
- `progress_note`
- `resolved_by`
- `resolved_at`

保留现有 `status` 字段，并沿用当前最小状态机。

## 5. 状态机设计

本次采用最小严格状态机：

- 当前仅允许 `PENDING -> RESOLVED`
- 已为 `RESOLVED` 的举报禁止重复处理
- 举报不存在时报业务异常

本次不增加 `PROCESSING`。

## 6. 接口设计

### 6.1 管理侧处理接口

新增控制器：

- `ContentUserSupportAdminController`

新增接口：

- `POST /content/user/support/admin/report/handle`

请求字段：

- `reportId`
- `operatorUserId`
- `status`，本次仅允许 `RESOLVED`
- `resultStatus`
- `resultNote`
- `progressNote`

返回值：

- 处理后的举报 ID

### 6.2 用户侧进度查询接口

复用现有用户支持控制器：

- `GET /content/user/support/report/progress`

请求参数：

- `userId`
- `reportId`

返回内容：

- 举报 ID
- 状态
- 处理进度说明
- 处理结果状态
- 处理结果说明
- 处理人
- 处理完成时间

## 7. 审计设计

处理成功后新增审计日志：

- 事件类型：`USER_REPORT_HANDLED`
- 关联用户：举报所属用户
- 操作人：处理人用户 ID
- 扩展数据：举报 ID、结果状态、结果说明

## 8. 测试设计

### 8.1 服务层

覆盖：

- `PENDING` 举报处理成功
- 已处理举报重复处理失败
- 举报不存在失败
- 非法目标状态失败
- 用户查询本人举报进度成功
- 用户查询他人举报进度失败
- 处理成功写审计日志

### 8.2 控制器

覆盖：

- admin 处理接口成功
- admin 处理接口参数校验
- 用户侧举报进度查询成功

## 9. 约束

- 延续当前支持域实现风格
- 继续使用 `@Resource`
- 继续使用 `JeecgBootException`
- 代码与测试遵循阿里巴巴规范和现有模块命名风格
