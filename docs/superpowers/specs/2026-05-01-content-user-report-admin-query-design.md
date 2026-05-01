# 内容社区用户域 - 举报后台列表/详情查询设计

## 1. 背景

当前支持域已经具备以下举报闭环能力：

- 用户创建举报
- 管理侧处理举报
- 用户侧查询举报处理进度

但管理侧仍缺少查询入口，导致运营或审核人员无法在后台查看举报列表，也无法按举报 ID 查询完整内容。现阶段若继续推进支持域，最直接且风险最低的增量能力就是补齐管理侧举报列表和详情查询。

## 2. 目标

在不引入完整工单后台和复杂筛选体系的前提下，补齐最小后台查询能力：

- 管理侧查询举报列表
- 管理侧查询举报详情

实现后，举报流将形成“提交 -> 处理 -> 用户回查 -> 后台查询”的最小可运营闭环。

## 3. 范围

### 3.1 包含

- 新增 admin 举报列表查询接口
- 新增 admin 举报详情查询接口
- 新增管理侧 VO
- 在 service 中补查询方法和实体映射
- 补充 service/controller 测试

### 3.2 不包含

- 高级筛选
- 分页能力
- 后台举报处理工作台
- 举报处理中间态扩展
- 举报工单评论、分配、流转能力

## 4. 方案选择

### 4.1 方案 A：最小列表 + 最小详情

能力：

- 列表接口支持最小筛选
- 详情接口返回完整举报信息

优点：

- 与当前处理流直接衔接
- 改动集中在支持域现有 service/controller
- 测试面清晰，最适合当前阶段

缺点：

- 暂不支持更细粒度后台检索

### 4.2 方案 B：只做列表

优点：

- 改动更小

缺点：

- 无法支撑详情页或后台复核查看

### 4.3 方案 C：列表 + 详情 + 高级筛选

优点：

- 后台可用性更强

缺点：

- 筛选模型、测试范围和后续维护成本明显增大

### 4.4 结论

本次采用方案 A：最小列表 + 最小详情。

## 5. 接口设计

### 5.1 管理侧举报列表

控制器：

- `ContentUserSupportAdminController`

接口：

- `GET /content/user/support/admin/report/list`

请求参数：

- `status`，可选
- `userId`，可选
- `targetType`，可选

查询规则：

- 三个条件全部可选
- 未传筛选条件时返回全部举报记录
- 结果按 `createTime desc` 排序

返回内容：

- `reportId`
- `userId`
- `targetType`
- `targetId`
- `reportType`
- `status`
- `resultStatus`
- `resolvedBy`
- `resolvedAt`
- `createTime`

### 5.2 管理侧举报详情

控制器：

- `ContentUserSupportAdminController`

接口：

- `GET /content/user/support/admin/report/detail`

请求参数：

- `reportId`

返回内容：

- `reportId`
- `userId`
- `targetType`
- `targetId`
- `reportType`
- `reason`
- `evidenceJson`
- `status`
- `resultStatus`
- `resultNote`
- `progressNote`
- `resolvedBy`
- `resolvedAt`
- `createTime`

异常规则：

- 举报不存在时抛出 `JeecgBootException("举报不存在")`

## 6. 模型设计

新增管理侧查询返回对象：

- `ContentUserReportAdminListItemVO`
- `ContentUserReportAdminDetailVO`

说明：

- 列表 VO 只保留后台列表必要字段，避免把大字段如 `reason`、`evidenceJson` 放入列表接口
- 详情 VO 返回完整查询字段，便于后台查看举报上下文和处理结果

## 7. 服务设计

在 `IContentUserSupportService` 中新增：

- `List<ContentUserReportAdminListItemVO> listReportsForAdmin(String status, String userId, String targetType);`
- `ContentUserReportAdminDetailVO getReportDetailForAdmin(String reportId);`

在 `ContentUserSupportServiceImpl` 中实现：

- 使用 `LambdaQueryWrapper<ContentUserReport>` 组装最小筛选条件
- 使用 `orderByDesc(ContentUserReport::getCreateTime)` 保持最近创建优先
- 使用私有映射方法把实体转换成管理侧列表/详情 VO

本次不新增独立查询 service，不拆分 admin service，继续沿用当前支持域的最小实现风格。

## 8. 持久化设计

复用现有：

- `ContentUserReport`
- `ContentUserReportMapper`

本次不增加 SQL 脚本变更，也不新增 mapper XML。

原因：

- 当前查询能力可由 MyBatis-Plus `BaseMapper` + `LambdaQueryWrapper` 完成
- 现有表结构已经包含后台查询所需字段

## 9. 控制器设计

继续在 `ContentUserSupportAdminController` 中扩展两个 GET 接口：

- `report/list`
- `report/detail`

约束：

- 继续使用 `Result.OK(...)`
- 继续保持控制器薄、业务逻辑下沉到 service
- 继续沿用现有注解和命名风格

## 10. 测试设计

### 10.1 服务层

覆盖：

- 无筛选条件查询列表成功
- 按 `status` 查询列表成功
- 按 `userId` 和 `targetType` 查询列表成功
- 详情查询成功
- 举报不存在时详情查询失败

### 10.2 控制器

覆盖：

- admin 举报列表接口成功
- admin 举报详情接口成功
- admin 举报详情接口异常传播

## 11. 约束

- 仅在 `jeecg-module-content` 范围内修改
- 不改动现有举报处理状态机
- 不引入分页对象或统一查询框架改造
- 代码与测试继续遵循阿里巴巴规范和当前模块风格

## 12. 验收标准

- 后台可查询举报列表
- 后台可按举报 ID 查询详情
- 列表支持最小筛选：`status`、`userId`、`targetType`
- 列表按最近创建时间倒序返回
- 举报不存在时返回明确业务异常
- 新增测试覆盖查询主路径和异常路径
