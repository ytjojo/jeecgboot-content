---
name: springboot-api-standards
description: Spring Boot 项目 RESTful 接口设计与文档规范。当用户编写 Controller、设计 API 接口、处理请求参数校验、设计分页接口、编写接口文档、定义统一响应格式时触发。只要涉及到 HTTP 接口的设计和实现，都应该使用此技能。包括但不限于：新增接口、修改接口签名、添加参数校验、设计批量操作接口等。
---

# Spring Boot RESTful 接口设计与文档规范

本技能定义了 Spring Boot 项目中 RESTful API 的设计标准，包括 URL 设计、请求响应规范、参数校验、分页、文档等。目标是让 API 风格统一、语义清晰、易于对接。

## URL 设计规范

### 基本规则

```
# 格式：/api/{version}/{资源名复数}/{动作}
GET    /api/v1/users/list          # 查询用户列表
GET    /api/v1/users/detail        # 查询单个用户
POST   /api/v1/users/save          # 创建用户
POST   /api/v1/users/update        # 更新用户
DELETE /api/v1/users/{ids}         # 删除用户（支持批量，逗号分隔）
```

- **资源名用复数名词**（`users` 不是 `user`），保持整个项目一致
- **URL 全部小写**，多单词用连字符（`/api/v1/user-orders` 不是 `/api/v1/userOrders`）
- **动作路径统一**：增删改查分别用 `save`/`update`/`delete`/`list`/`detail`
- **删除支持批量**：`/api/v1/users/{ids}`，多个 ID 用逗号分隔（如 `/api/v1/users/1,2,3`）
- **查询用 GET，写操作用 POST/DELETE**：save 和 update 统一用 POST
- **嵌套资源最多两层**：`/api/v1/groups/{groupId}/items/list`，再深就拆成独立资源
- **版本号放在 URL 中**：`/api/v1/`，方便未来升级而不破坏旧客户端

### 特殊操作

业务动作直接作为路径：

```
POST   /api/v1/users/disable          # 禁用用户（请求体传 id）
POST   /api/v1/orders/cancel          # 取消订单（请求体传 id）
POST   /api/v1/auth/login             # 登录
POST   /api/v1/auth/logout            # 登出
```

---

## 统一响应格式

JeecgBoot 项目统一返回 `org.jeecg.common.api.vo.Result<T>`，客户端只需要一套解析逻辑。

```java
// 成功
Result<UserVO> ok1 = Result.OK(userVo);
Result<UserVO> ok2 = Result.OK("操作成功", userVo);

// 失败
Result<?> err1 = Result.error("参数验证失败");
Result<?> err2 = Result.error(400, "参数验证失败");
Result<?> err3 = Result.noauth("没有权限");
```

### 响应示例

```json
// 成功
{
  "success": true,
  "message": "操作成功",
  "code": 200,
  "result": { "id": 1, "username": "admin" },
  "timestamp": 1711180800000
}

// 失败
{
  "success": false,
  "message": "参数验证失败",
  "code": 400,
  "result": null,
  "timestamp": 1711180800000
}

// 分页
{
  "success": true,
  "message": "操作成功",
  "code": 200,
  "result": {
    "records": [...],
    "total": 100,
    "size": 10,
    "current": 1,
    "pages": 10
  },
  "timestamp": 1711180800000
}
```

### HTTP 状态码使用

接口应该返回正确的 HTTP 状态码，而不是所有情况都返回 200：

| 状态码 | 场景 |
|--------|------|
| 200 | 成功 |
| 201 | 创建成功 |
| 400 | 参数错误、校验失败 |
| 401 | 未认证（未登录、token 过期） |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 409 | 资源冲突（如用户名已存在） |
| 500 | 服务器内部错误 |

---

## Controller 编写规范

```java
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // GET /api/v1/users/list - 分页查询
    @GetMapping("/list")
    public Result<Page<UserVO>> getUserList(
            @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {
        Page<UserVO> page = userService.getUsersByPage(new Page<>(pageNo, pageSize));
        return Result.OK(page);
    }

    // GET /api/v1/users/detail - 查询单个用户
    @GetMapping("/detail")
    public Result<UserVO> getUserDetail(@RequestParam Long id) {
        UserVO user = userService.getUserById(id);
        return Result.OK(user);
    }

    // POST /api/v1/users/save - 创建用户
    @PostMapping("/save")
    public Result<UserVO> saveUser(@Valid @RequestBody CreateUserDTO dto) {
        UserVO user = userService.createUser(dto);
        return Result.OK(user);
    }

    // POST /api/v1/users/update - 更新用户
    @PostMapping("/update")
    public Result<Void> updateUser(@Valid @RequestBody UpdateUserDTO dto) {
        userService.updateUser(dto);
        return Result.OK();
    }

    // DELETE /api/v1/users/{ids} - 删除用户（支持批量）
    @DeleteMapping("/{ids}")
    public Result<Void> deleteUsers(@PathVariable List<Long> ids) {
        userService.deleteUsers(ids);
        return Result.OK();
    }
}
```

### Controller 规则

- **Controller 只做三件事**：接收参数 → 调用 Service → 返回结果。不写业务逻辑。
- **一个 Controller 对应一个资源**，不要在一个 Controller 里塞多种资源的接口
- **构造器注入**（`@RequiredArgsConstructor`），不用 `@Autowired` 字段注入
- **返回 VO/DTO**，不直接返回 Entity（避免暴露数据库结构和敏感字段）
- **@Valid 校验**放在 Controller 层，Service 层不重复校验

---

## 参数校验规范

### 请求 DTO 校验

```java
@Data
public class CreateUserDTO {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度3-20个字符")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 50, message = "密码长度6-50个字符")
    private String password;

    @Email(message = "邮箱格式不正确")
    private String email;

    @Min(value = 0, message = "年龄不能为负数")
    @Max(value = 150, message = "年龄不合法")
    private Integer age;
}
```

### 校验规则

- 使用 Jakarta Validation 注解（`@NotBlank`, `@Size`, `@Pattern`, `@Email`, `@Min`, `@Max`）
- `@NotBlank` 用于字符串（不为 null 且去掉空格后不为空），`@NotNull` 用于其他类型
- 每个校验注解必须带 `message` 参数，给前端明确的错误提示
- 嵌套对象校验用 `@Valid`

### 分组校验（创建和更新需要不同规则时）

```java
public interface CreateGroup {}
public interface UpdateGroup {}

@Data
public class UserDTO {
    @Null(groups = CreateGroup.class, message = "创建时不需要传ID")
    @NotNull(groups = UpdateGroup.class, message = "更新时必须传ID")
    private Long id;

    @NotBlank(groups = {CreateGroup.class, UpdateGroup.class})
    private String username;
}
```

---

## 分页规范

### 标准分页请求

```
GET /api/v1/users/page?pageNo=1&pageSize=10&keyword=admin&status=1
```

- `pageNo`：当前页码（从 1 开始）
- `pageSize`：每页条数（默认 10，最大限制 100）
- 其他参数为查询条件

### 分页实现

```java
@GetMapping("/page")
public Result<Page<UserVO>> getUsersByPage(
        @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
        @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) Integer status) {

    // 限制单页最大条数，防止一次查太多导致 OOM
    pageSize = Math.min(pageSize, 100);

    Page<UserVO> page = userService.getUsersByPage(
        new Page<>(pageNo, pageSize), keyword, status);
    return Result.OK(page);
}
```

### 深分页处理

当页码很大时（比如第 10000 页），传统的 `OFFSET` 分页会非常慢，因为数据库要扫描前面所有行再丢弃。使用游标分页（Cursor-based Pagination）替代：

```java
// 禁止这样写（深分页时性能急剧下降）：
// SELECT * FROM users ORDER BY id LIMIT 10 OFFSET 100000

// 正确做法：基于 ID 游标分页
@GetMapping("/list")
public Result<CursorPage<UserVO>> getUsersByCursor(
        @RequestParam(required = false) Long lastId,
        @RequestParam(defaultValue = "10") Integer size) {
    // WHERE id > lastId ORDER BY id ASC LIMIT size
    return Result.OK(userService.getUsersByCursor(lastId, size));
}
```

详细的深分页优化方案参见 `springboot-performance` 技能。

---

## DTO/VO 规范

### 分层职责

| 类型 | 用途 | 命名 |
|------|------|------|
| Entity | 映射数据库表 | `User`, `Order` |
| DTO | 接收请求参数 | `CreateUserDTO`, `UpdateUserDTO` |
| VO | 返回前端数据 | `UserVO`, `OrderDetailVO` |
| Query | 复杂查询条件 | `UserQuery`, `OrderSearchQuery` |

- **Entity 不直接暴露给前端**（可能包含密码、盐值等敏感字段）
- **DTO 用于入参**，携带校验注解
- **VO 用于出参**，只包含前端需要的字段
- 如果 Entity 字段和 DTO/VO 差别不大，可以用 Entity 加 `@JsonIgnore` 隐藏敏感字段，不必强制创建 VO

### 对象转换

```java
// 简单场景用 BeanUtils
BeanUtils.copyProperties(dto, entity);

// 复杂场景用 MapStruct（编译期生成，性能好）
@Mapper(componentModel = "spring")
public interface UserConverter {
    UserVO toVO(User user);
    User toEntity(CreateUserDTO dto);
}
```

---

## 接口文档规范

### 接口注释格式

每个 Controller 方法应有清晰的功能描述。如果项目使用 Swagger/SpringDoc，添加注解：

```java
@Operation(summary = "分页查询用户列表", description = "支持按用户名模糊搜索和状态过滤")
@Parameters({
    @Parameter(name = "pageNo", description = "页码", example = "1"),
    @Parameter(name = "pageSize", description = "每页条数", example = "10"),
    @Parameter(name = "keyword", description = "搜索关键词（模糊匹配用户名）")
})
@GetMapping("/page")
public Result<Page<UserVO>> getUsersByPage(...) { ... }
```

### 如果不使用 Swagger

在 Controller 类或方法上添加 Javadoc 注释，说明接口用途、参数含义、返回值结构：

```java
/**
 * 分页查询用户列表
 *
 * @param pageNo 页码（从1开始，默认1）
 * @param pageSize    每页条数（默认10，最大100）
 * @param keyword 搜索关键词（可选，模糊匹配用户名）
 * @return 分页用户列表
 */
@GetMapping("/page")
public Result<Page<UserVO>> getUsersByPage(...) { ... }
```
