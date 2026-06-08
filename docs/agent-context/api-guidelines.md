# API 与后端约定

## 基础路径
- 对外访问前缀通常为 `/jeecg-boot`
- Controller 路径统一按 `/api/v1/{resources}` 组织，版本号放在 URL 中
- 最终对外地址通常形如 `/jeecg-boot/api/v1/users/list`

## URL 约定
- 资源路径优先使用语义清晰的复数名词，例如 `users`、`user-orders`
- URL 全部小写，多单词使用连字符，不使用驼峰
- 查询和写操作路径统一使用 `list`、`detail`、`save`、`update`、`delete`
- 删除接口优先使用 `DELETE /api/v1/{resources}/{ids}`，支持逗号分隔批量删除
- 嵌套资源最多两层，例如 `/api/v1/groups/{groupId}/items/list`
- 业务型动作可直接挂在资源后，例如 `/api/v1/orders/cancel`、`/api/v1/auth/login`

## HTTP 方法约定
- 查询使用 `GET`
- 新增和更新在本项目内统一使用 `POST`，分别对应 `/save`、`/update`
- 删除使用 `DELETE`
- 不再额外引入风格不统一的 `PUT /{id}`、`PATCH /{id}` 写法，除非模块已有历史兼容要求

## 推荐示例

```text
GET    /jeecg-boot/api/v1/users/list
GET    /jeecg-boot/api/v1/users/detail?id=1
POST   /jeecg-boot/api/v1/users/save
POST   /jeecg-boot/api/v1/users/update
DELETE /jeecg-boot/api/v1/users/1,2,3
```

## 返回格式
- 后端接口统一返回 `org.jeecg.common.api.vo.Result<T>`
- 成功优先使用 `Result.OK()`、`Result.OK(data)`、`Result.OK(message, data)`
- 失败优先使用 `Result.error(message)`、`Result.error(code, message)`
- 接口层不要直接返回裸对象、`Map` 或未封装的分页结构

## HTTP 状态码
- `200`：查询、更新、删除成功
- `201`：创建成功，项目内如仍统一返回 `Result.OK()`，至少保证业务语义与文档一致
- `400`：参数错误、校验失败
- `401`：未认证或登录态失效
- `403`：无权限
- `404`：资源不存在
- `409`：资源冲突，例如唯一键重复
- `500`：服务端内部错误

## Controller 与分层约定
- Controller 使用 `@RestController`
- Controller 只做三件事：接收参数、参数校验、调用下层并返回 `Result`
- 不在 Controller 中编写业务编排、事务处理和复杂判断
- 跨聚合编排逻辑进入 `biz` 层，单表逻辑保留在 `service` 层
- Service 优先复用 MyBatis-Plus 能力，Mapper 使用 MyBatis-Plus
- 请求参数优先使用 `req`、`dto`、`query` 对象，不直接暴露大而杂的 `Map`
- 返回值优先使用 `vo`，不要直接把实体对象暴露给接口层

## 参数校验约定
- 在 Controller 入参处使用 `@Valid` 或 `@Validated`
- DTO/req 请求入参字段优先使用 `@NotBlank`、`@NotNull`、`@Size`、`@Pattern`、`@Email`、`@Min`、`@Max` 等校验注解
- 每个校验注解都应写清晰的 `message`，直接给前端可读提示
- 嵌套对象校验使用 `@Valid`
- 创建与更新规则不一致时，使用分组校验

## 分页约定
- 分页参数统一使用 `pageNo`、`pageSize`
- `pageNo` 从 `1` 开始，`pageSize` 默认 `10`
- 单页条数需要限制上限，建议最大不超过 `100`
- 查询条件作为普通请求参数或查询对象传入，不使用无结构 `Map`
- 深分页场景避免高 `OFFSET`，必要时改为基于主键或时间游标的分页

## DTO / VO / Query 约定
- `entity`：数据库实体，仅用于持久化映射
- `dto` 或 `req`：接口入参，承载校验规则
- `vo`：接口出参，只暴露前端需要的字段
- `query`：复杂查询条件对象
- 默认不要直接返回实体对象，避免暴露敏感字段和数据库结构

## 接口文档约定
- 每个 Controller 方法都应提供清晰的功能说明
- 使用 Swagger 或 SpringDoc 时，补充接口摘要、参数说明和示例值
- 不使用 Swagger 时，至少补充 Javadoc，说明用途、参数和返回值
- 文档中的路径、方法、参数名、返回结构必须和实际代码保持一致

## 数据库变更约定
- 修改表结构时，同步更新 `entity`、`mapper`、SQL 脚本和相关请求/响应对象
- 常见审计字段包括 `create_by`、`create_time`、`update_by`、`update_time`
- 常见逻辑删除字段为 `del_flag`
- 多租户场景关注 `tenant_id`

## 测试建议
- 单元测试优先使用 JUnit 5 + Mockito
- 集成测试优先使用 Spring Boot Test
- 针对接口签名、参数校验、分页逻辑和返回模型变更，优先补充贴近改动范围的测试
- 不追求低价值覆盖率，重点覆盖容易回归的接口行为

## 前端 API 层约定

### HTTP 客户端

- 使用自定义 Axios 封装：`src/utils/http/axios/`，导出为 `defHttp`
- 所有请求通过 `signMd5Utils` 进行 MD5 签名
- 租户模式启用时（`VITE_GLOB_TENANT_MODE`），自动注入 Tenant ID 到请求头

### 响应格式

```typescript
interface Result<T> {
  code: number;       // 200 表示成功
  result: T;          // 业务数据
  message: string;    // 提示信息
  success: boolean;   // 业务是否成功
}
```

### 使用示例

```typescript
import { defHttp } from '/@/utils/http/axios';

// GET 请求
export const getUserStatus = (userId: string) =>
  defHttp.get({ url: `/api/v1/content/user-status/${userId}` });

// POST 请求
export const updateUserStatus = (data: UpdateStatusDTO) =>
  defHttp.post({ url: '/api/v1/content/user-status/update' }, { data });
```
