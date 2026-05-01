# 内容社区用户域 - 举报后台分页与高级筛选查询设计

## 1. 背景

当前支持域已经具备以下举报能力：

- 用户创建举报
- 管理侧处理举报
- 用户侧查询举报处理进度
- 管理侧查询举报列表与详情

上一阶段已经补齐了管理侧最小查询闭环，但列表接口仍然只支持最小筛选，且不具备分页能力。随着举报数据量增长，后台继续返回全量列表会直接影响运营使用体验，也不利于后续平滑演进为更完整的审核后台。

因此，当前最合适的下一步是在不引入工单工作台、不改动状态机的前提下，为管理侧举报列表补充分页与中等强度筛选能力。

## 2. 目标

在现有 `ContentUserSupportAdminController` 和 `IContentUserSupportService` 基础上，增强举报列表查询能力：

- 为后台举报列表增加分页参数
- 在最小筛选基础上补充更实用的运营筛选字段
- 继续保留举报详情接口不变

实现后，后台查询将从“可查”升级为“可运营地查”。

## 3. 范围

### 3.1 包含

- 升级管理侧举报列表接口为分页查询
- 新增中等强度筛选字段
- 新增分页查询请求模型
- 新增分页结果返回模型
- 调整 service 查询实现与映射逻辑
- 补充 service/controller 测试

### 3.2 不包含

- 举报详情字段扩展
- 模糊搜索
- 多字段排序
- 新增 mapper XML 或自定义 SQL
- 举报处理中间态扩展
- 举报工单评论、分配、流转能力
- 通用后台查询框架改造

## 4. 方案选择

### 4.1 方案 A：升级现有列表接口为分页查询

能力：

- 继续使用 `GET /content/user/support/admin/report/list`
- 为该接口增加分页参数和更多筛选参数

优点：

- 与当前 controller、service、测试结构衔接最好
- 不引入重复接口语义
- 迁移成本最低

缺点：

- 相比返回纯列表，接口返回结构会发生变化

### 4.2 方案 B：新增独立分页接口

能力：

- 保留现有 `list`
- 新增例如 `page` 之类的分页接口

优点：

- 兼容性更强

缺点：

- 会形成两个职责近似的查询接口
- 后续维护和测试成本更高

### 4.3 方案 C：一次性上完整后台检索模型

优点：

- 后台能力最强

缺点：

- 明显超出当前支持域最小演进范围
- 会把简单 support 模块推向完整审核工作台设计

### 4.4 结论

本次采用方案 A：升级现有列表接口为分页查询。

## 5. 接口设计

### 5.1 管理侧举报分页列表

控制器：

- `ContentUserSupportAdminController`

接口：

- `GET /content/user/support/admin/report/list`

请求模型：

- `ContentUserReportAdminQueryReq`

请求参数：

- `pageNo`，可选，默认 `1`
- `pageSize`，可选，默认 `10`
- `status`，可选
- `resultStatus`，可选
- `userId`，可选
- `targetType`，可选
- `targetId`，可选
- `reportType`，可选
- `resolvedBy`，可选
- `createTimeStart`，可选
- `createTimeEnd`，可选

校验规则：

- `pageNo >= 1`
- `pageSize >= 1`
- `pageSize <= 100`
- 时间区间允许为空
- 当同时传 `createTimeStart` 和 `createTimeEnd` 时，要求开始时间不晚于结束时间

查询规则：

- 所有筛选条件均为可选
- 未传筛选条件时，返回分页后的全部举报记录
- 结果按 `createTime desc` 排序
- 时间区间按 `createTime` 过滤

返回模型：

- `ContentUserReportAdminPageVO`

返回字段：

- `records`
- `total`
- `pageNo`
- `pageSize`

其中 `records` 中每一项仍沿用：

- `ContentUserReportAdminListItemVO`

### 5.2 管理侧举报详情

保持不变：

- `GET /content/user/support/admin/report/detail`

本次不调整详情接口签名和返回字段。

## 6. 模型设计

### 6.1 新增请求模型

- `ContentUserReportAdminQueryReq`

字段：

- `pageNo`
- `pageSize`
- `status`
- `resultStatus`
- `userId`
- `targetType`
- `targetId`
- `reportType`
- `resolvedBy`
- `createTimeStart`
- `createTimeEnd`

设计说明：

- 用独立请求对象承载筛选和分页参数，避免 controller 方法签名继续膨胀
- 时间字段使用 `Date`，与当前模块实体和 VO 风格保持一致

### 6.2 新增分页返回模型

- `ContentUserReportAdminPageVO`

字段：

- `List<ContentUserReportAdminListItemVO> records`
- `Long total`
- `Long pageNo`
- `Long pageSize`

设计说明：

- 不直接把 MyBatis-Plus `Page` 暴露到 controller 返回层
- 使用模块内 VO 包装，减少后续切换实现时的耦合

## 7. 服务设计

在 `IContentUserSupportService` 中调整管理侧列表查询签名：

- 原方法：
  - `List<ContentUserReportAdminListItemVO> listReportsForAdmin(String status, String userId, String targetType);`
- 新方法：
  - `ContentUserReportAdminPageVO listReportsForAdmin(ContentUserReportAdminQueryReq req);`

详情查询方法保持不变：

- `ContentUserReportAdminDetailVO getReportDetailForAdmin(String reportId);`

在 `ContentUserSupportServiceImpl` 中实现：

- 创建 `Page<ContentUserReport>` 分页对象
- 使用 `LambdaQueryWrapper<ContentUserReport>` 组合筛选条件
- 使用 `orderByDesc(ContentUserReport::getCreateTime)` 固定排序
- 将分页结果映射为 `ContentUserReportAdminPageVO`
- 对空的 `pageNo/pageSize` 填充默认值
- 对非法时间区间抛出明确业务异常

本次不新增独立 admin query service，继续沿用当前支持域统一 service 风格。

## 8. 持久化设计

复用现有：

- `ContentUserReport`
- `ContentUserReportMapper`

实现方式：

- 直接使用 MyBatis-Plus `selectPage(...)`
- 通过 `LambdaQueryWrapper` 组装筛选

本次不增加 SQL 脚本变更，也不新增 mapper XML。

## 9. 控制器设计

在 `ContentUserSupportAdminController` 中调整列表接口：

- 使用 `@Valid ContentUserReportAdminQueryReq req` 接收查询参数
- 继续通过 `Result.OK(...)` 返回
- controller 只负责接参与出参，不承载分页和筛选业务逻辑

详情接口保持现状，不做额外改动。

## 10. 异常规则

列表查询新增异常规则：

- 分页参数非法时，走参数校验错误
- 时间区间非法时，抛出 `JeecgBootException("创建时间范围非法")`

详情查询异常规则保持不变：

- 举报不存在时抛出 `JeecgBootException("举报不存在")`

## 11. 测试设计

### 11.1 服务层

覆盖：

- 使用默认分页参数查询成功
- 按 `status`、`resultStatus`、`targetType` 等条件分页查询成功
- 按 `resolvedBy` 查询成功
- 按创建时间区间查询成功
- 非法时间区间查询失败
- 空结果分页查询成功

### 11.2 控制器

覆盖：

- 分页查询接口成功
- 分页默认参数生效
- 非法分页参数触发 `400`
- 详情接口回归通过

## 12. 约束

- 仅在 `jeecg-module-content` 范围内修改
- 不改动举报状态机
- 不新增新的 controller 或 service 分层
- 不引入 XML 查询
- 代码与测试继续遵循阿里巴巴规范和当前模块风格

## 13. 验收标准

- 后台举报列表支持分页查询
- 分页默认值为 `pageNo=1`、`pageSize=10`
- 后台举报列表支持以下筛选：
  - `status`
  - `resultStatus`
  - `userId`
  - `targetType`
  - `targetId`
  - `reportType`
  - `resolvedBy`
  - `createTimeStart`
  - `createTimeEnd`
- 列表固定按 `createTime desc` 返回
- 时间区间非法时返回明确业务异常
- 举报详情接口保持可用
- 新增测试覆盖分页主路径、筛选路径、异常路径
