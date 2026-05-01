# 内容社区用户域 - 举报后台详情异常路径与时间参数格式化测试补强设计

## 1. 背景

当前举报后台已经具备：

- 分页与高级筛选列表查询
- 后台详情查询

并且主路径测试已经覆盖：

- 列表分页成功
- 分页参数校验失败
- 详情查询成功

但仍缺少两类关键回归测试：

- 举报详情接口在 service 抛出业务异常时的 controller 层传播行为
- 列表接口时间参数字符串到 `Date` 的绑定格式行为

这两类问题都属于接口稳定性问题，最适合通过补充测试来锁定行为，而不是继续扩展生产逻辑。

## 2. 目标

在不修改功能范围的前提下，补齐举报后台接口的关键测试缺口：

- 补齐详情异常路径测试
- 补齐时间参数格式化绑定成功测试
- 补齐非法时间格式请求测试

## 3. 范围

### 3.1 包含

- 补充 `ContentUserSupportAdminControllerWebMvcTest`
- 验证 `report/detail` 的异常传播
- 验证 `createTimeStart/createTimeEnd` 的字符串绑定
- 验证非法时间格式返回 `400`

### 3.2 不包含

- 修改 `ContentUserSupportServiceImpl`
- 修改 `ContentUserSupportAdminController`
- 修改 `ContentUserReportAdminQueryReq` 的日期格式定义
- 新增统一异常处理器
- 变更现有返回结构

## 4. 方案选择

### 4.1 方案 A：只补 controller 测试

能力：

- 用 WebMvc 测试锁定 controller 层行为
- 不改生产代码

优点：

- 改动最小
- 最贴当前诉求
- 能直接验证 Spring 参数绑定和异常传播

缺点：

- 不额外增强 service 边界覆盖

### 4.2 方案 B：controller + service 一起补

优点：

- 覆盖更完整

缺点：

- 超出这次“测试补强”的最小范围

### 4.3 结论

本次采用方案 A：只补 controller 测试。

## 5. 测试设计

测试文件：

- `ContentUserSupportAdminControllerWebMvcTest`

### 5.1 详情异常路径

新增测试：

- `shouldPropagateReportDetailExceptionForAdmin`

覆盖目标：

- 当 `supportService.getReportDetailForAdmin("report-404")` 抛出 `JeecgBootException("举报不存在")` 时
- controller 不吞异常
- standalone MockMvc 下返回 `500`

说明：

- 当前测试类未挂载全局异常处理器，因此这里验证的是异常传播后的 HTTP 状态，不校验统一错误体格式

### 5.2 时间参数绑定成功

新增测试：

- `shouldBindCreateTimeRangeForAdminReportList`

覆盖目标：

- 请求传入：
  - `createTimeStart=2026-05-01 10:00:00`
  - `createTimeEnd=2026-05-01 12:00:00`
- controller 能将参数绑定到 `ContentUserReportAdminQueryReq`
- service 能收到非空时间参数
- 接口返回 `200`

说明：

- 该测试重点验证 `@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")` 的绑定行为
- 通过 `ArgumentCaptor` 或 `argThat` 断言传给 service 的请求对象字段值

### 5.3 时间参数绑定失败

新增测试：

- `shouldRejectInvalidCreateTimeFormatForAdminReportList`

覆盖目标：

- 当传入非法时间格式，如 `2026/05/01 10:00:00`
- Spring 参数绑定失败
- 接口返回 `400`

说明：

- 该测试不要求校验具体错误消息文本
- 只锁定绑定失败时的 HTTP 行为

## 6. 约束

- 本次优先只改测试文件
- 只有在新增测试暴露真实缺陷时，才允许最小修改生产代码
- 不新增新的测试基础设施
- 不引入全局异常处理器改造

## 7. 验收标准

- `report/detail` 异常路径有明确 controller 测试覆盖
- `createTimeStart/createTimeEnd` 正确格式有绑定成功测试
- 非法时间格式有 `400` 测试
- 现有举报后台查询相关测试继续通过
