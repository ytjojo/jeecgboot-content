# 内容社区用户域 - 申诉处理时间透出设计

## 1. 背景

当前申诉处理接口已经在处理完成时回写以下字段：

- `status`
- `result_status`
- `result_note`
- `resolved_by`
- `resolved_at`
- `progress_note`

其中 `resolved_at` 已经成功落库，但用户侧查询返回对象 `ContentUserAppealProgressVO` 还未透出该字段，导致处理时间无法在进度查询和列表查询中被前端消费。

## 2. 目标

本次仅补齐 `resolvedAt` 的只读透出能力，使用户侧现有查询接口能返回处理完成时间。

## 3. 范围

### 3.1 包含

- `ContentUserAppealProgressVO` 新增 `resolvedAt`
- `ContentUserSupportServiceImpl.toAppealProgress()` 补字段映射
- `ContentUserSupportServiceTest` 增加 `resolvedAt` 断言
- `ContentUserSupportControllerWebMvcTest` 增加 `resolvedAt` JSON 断言

### 3.2 不包含

- 新增接口
- 新增数据库字段
- 新增管理员字段透出
- 修改处理接口

## 4. 接口影响

不新增接口，继续复用：

- `GET /content/user/support/appeal/progress`
- `GET /content/user/support/appeal/list`

仅在响应体中增加：

- `resolvedAt`

字段语义：

- 表示申诉被处理完成的时间
- 未处理完成时允许为空

## 5. 实现设计

### 5.1 VO 设计

在 `ContentUserAppealProgressVO` 中新增：

- `Date resolvedAt`

并补充对应 Swagger 注解说明。

### 5.2 Service 映射

在 `ContentUserSupportServiceImpl.toAppealProgress()` 中增加：

- `setResolvedAt(appeal.getResolvedAt())`

不增加额外转换逻辑，直接沿用当前模块对 `Date` 的返回方式。

## 6. 测试设计

### 6.1 服务层测试

验证：

- `getAppealProgress()` 返回 `resolvedAt`
- `listAppeals()` 透出后的对象结构不受影响

### 6.2 控制器测试

验证：

- `/appeal/progress` 返回 `resolvedAt`
- `/appeal/list` 返回 `resolvedAt`

本次仍使用 standalone `MockMvc`。

## 7. 约束

- 仅做最小透出，不扩展更多字段
- 不改 SQL 和持久化结构
- 代码风格继续遵循现有支持域实现与阿里巴巴规范要求
