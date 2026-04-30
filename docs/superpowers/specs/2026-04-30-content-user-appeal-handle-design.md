# 内容社区用户域 - 申诉处理回写设计

## 1. 背景

当前支持域已经具备用户侧能力：

- 创建申诉
- 查询申诉进度
- 查询申诉列表

现阶段缺少处理侧回写能力，导致以下字段虽然已经存在，但没有正式入口完成业务闭环：

- `status`
- `result_status`
- `result_note`
- `resolved_by`
- `resolved_at`
- `progress_note`

本次设计目标是在不扩展完整工单后台的前提下，补齐一个最小可交付的申诉处理回写接口，并增加必要状态机约束与自动化测试。

## 2. 范围

### 2.1 包含

- 新增处理侧控制器 `ContentUserSupportAdminController`
- 新增申诉处理请求对象
- 新增 service 处理方法
- 回写申诉处理结果字段
- 写入申诉处理审计日志
- 增加服务层与控制器测试

### 2.2 不包含

- 完整后台工单流转
- 多次处理中备注追加
- 处理人权限体系
- 举报处理回写

## 3. 接口设计

### 3.1 控制器

新增独立处理侧控制器：

- 类名：`ContentUserSupportAdminController`
- 路径前缀：`/content/user/support/admin`

### 3.2 处理接口

- 方法：`POST`
- 路径：`/appeal/handle`

请求体字段：

- `appealId`：申诉 ID，必填
- `operatorUserId`：处理人用户 ID，必填
- `status`：处理后状态，必填，本次仅允许传 `RESOLVED`
- `resultStatus`：处理结果状态，必填，例如 `APPROVED`、`REJECTED`
- `resultNote`：处理结果说明，必填
- `progressNote`：处理进度说明，必填

返回值：

- 返回处理后的申诉 ID，保持与现有支持域风格一致

## 4. 状态机规则

本次采用最小严格状态机：

- 仅允许当前状态为 `PENDING` 或 `PROCESSING` 的记录进入处理动作
- 处理后的目标状态仅允许为 `RESOLVED`
- 当前状态为 `RESOLVED` 的记录禁止重复处理
- 申诉不存在时返回业务异常

错误语义遵循当前模块做法，使用 `JeecgBootException` 返回明确中文提示。

## 5. 数据回写

处理成功后统一更新以下字段：

- `status` = 请求中的 `status`
- `resultStatus` = 请求中的 `resultStatus`
- `resultNote` = 请求中的 `resultNote`
- `progressNote` = 请求中的 `progressNote`
- `resolvedBy` = 请求中的 `operatorUserId`
- `resolvedAt` = 当前时间

不新增表结构，本次直接复用既有 `content_user_appeal` 字段。

## 6. 审计设计

处理成功后新增一条审计日志，记录：

- 事件类型：`USER_APPEAL_HANDLED`
- 关联用户：申诉所属用户
- 操作人：处理人用户 ID
- 事件内容：申诉类型与处理结果摘要
- 扩展字段：申诉 ID、结果状态、结果说明

该日志用于后续追踪处理结果，不扩展独立审批流水表。

## 7. 测试设计

### 7.1 服务层测试

覆盖以下场景：

- 申诉从 `PENDING` 处理为 `RESOLVED` 成功
- 申诉从 `PROCESSING` 处理为 `RESOLVED` 成功
- 申诉不存在时报错
- 已为 `RESOLVED` 的申诉重复处理时报错
- 非法目标状态时报错
- 处理成功后写入审计日志

### 7.2 控制器测试

覆盖以下场景：

- 合法请求返回成功
- 缺少必填参数触发 `400`
- 请求体字段校验生效

控制器测试继续沿用 standalone `MockMvc` 风格，避免引入额外 Spring 上下文依赖。

## 8. 兼容性与规范

- 仅在 `jeecg-module-content` 范围内改动
- 延续 `@Resource` 注入风格
- 请求对象使用 `jakarta.validation` 注解完成参数校验
- 方法与类增加简洁 Javadoc
- 控制器、service、测试命名遵循现有模块风格

## 9. 实施顺序

1. 先补服务层红灯测试
2. 再补控制器红灯测试
3. 增加请求对象、接口契约与控制器
4. 完成 service 实现与审计日志工厂
5. 运行聚焦测试
6. 做诊断与规范检查
