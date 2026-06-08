# JeecgBoot 框架特有规范

本文档补充 `springboot-coding-conventions.md`，覆盖 JeecgBoot 框架特有的模式、基类和约定。

## JeecgEntity 基类

**位置**: `jeecg-boot-base-core/.../org/jeecg/common/system/base/entity/JeecgEntity.java`

所有业务实体继承此类，提供统一的审计字段：

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | `String` | 主键，雪花算法生成（`IdType.ASSIGN_ID`），存储为字符串数字 |
| `createBy` | `String` | 创建人 |
| `createTime` | `Date` | 创建时间，JSON 序列化为 `yyyy-MM-dd HH:mm:ss` |
| `updateBy` | `String` | 更新人 |
| `updateTime` | `Date` | 更新时间，格式同 createTime |

ID 由 MyBatis-Plus `IdWorker.getId()` 自动生成，勿手动设置。

## JeecgController 基类

**位置**: `jeecg-boot-base-core/.../org/jeecg/common/system/base/controller/JeecgController.java`

泛型签名 `JeecgController<T, S extends IService<T>>`：
- `T` — 实体类型
- `S` — Service 接口，Spring 通过 `@Autowired` 自动注入

**提供给子类的内置方法**：

| 方法 | 用途 |
|------|------|
| `exportXls(req, entity, clazz, title)` | 标准 Excel 导出 |
| `exportXls(req, entity, clazz, title, exportFields)` | 自定义字段 Excel 导出 |
| `exportXlsSheet(req, entity, clazz, title, sheet, pageSize)` | 多 Sheet 分页导出 |
| `exportXlsForBigData(req, entity, clazz, title, pageSize)` | 大数据量流式导出 |
| `importExcel(req, resp, clazz)` | Excel 导入（AutoPoi） |

所有导出方法内部调用 `QueryGenerator.initQueryWrapper()` 构建查询条件。

**典型使用**：
```java
@RestController
@RequestMapping("/api/v1/content/channel")
public class ContentChannelController extends JeecgController<ContentChannel, IContentChannelService> {
    // S = IContentChannelService 自动注入到 protected S service
}
```

## JeecgServiceImpl 基类

**位置**: `jeecg-boot-base-core/.../org/jeecg/common/system/base/service/impl/JeecgServiceImpl.java`

```java
public class JeecgServiceImpl<M extends BaseMapper<T>, T extends JeecgEntity>
    extends ServiceImpl<M, T> implements JeecgService<T>
```

薄基类，无额外方法，仅通过泛型约束 `T extends JeecgEntity` 确保实体包含审计字段。日常开发直接使用 MyBatis-Plus `ServiceImpl` 提供的方法即可。

## Result\<T\> 响应封装

**位置**: `jeecg-boot-base-core/.../org/jeecg/common/api/vo/Result.java`

所有接口统一返回 `Result<T>`：

| 工厂方法 | 状态码 | 用途 |
|----------|--------|------|
| `Result.OK()` | 200 | 成功，无数据 |
| `Result.OK(data)` | 200 | 成功，带数据 |
| `Result.OK(msg, data)` | 200 | 成功，带消息和数据 |
| `Result.error(msg)` | 500 | 通用错误 |
| `Result.error(code, msg)` | 自定义 | 指定错误码 |
| `Result.noauth(msg)` | 510 | 无权限 |

状态码常量定义在 `CommonConstant`：`SC_OK_200=200`、`SC_INTERNAL_SERVER_ERROR_500=500`、`SC_JEECG_NO_AUTHZ=510`。

Controller 不要直接返回裸对象或未封装的分页结构。

## QueryGenerator 自动查询

**位置**: `jeecg-boot-base-core/.../org/jeecg/common/system/query/QueryGenerator.java`

将 HTTP 请求参数自动转换为 MyBatis-Plus `QueryWrapper`：

```java
QueryWrapper<ContentChannel> queryWrapper = QueryGenerator.initQueryWrapper(entity, req.getParameterMap());
Page<ContentChannel> page = new Page<>(pageNo, pageSize);
IPage<ContentChannel> result = service.page(page, queryWrapper);
```

**自动查询规则**（根据参数值的特殊前缀/后缀匹配）：

| 参数值格式 | 查询规则 | SQL |
|-----------|----------|-----|
| `abc` | 等于 | `= 'abc'` |
| `*abc*` | 模糊 | `LIKE '%abc%'` |
| `abc*` | 右模糊 | `LIKE 'abc%'` |
| `*abc` | 左模糊 | `LIKE '%abc'` |
| `> 100` | 大于 | `> 100` |
| `!= abc` | 不等于 | `!= 'abc'` |
| `a,b,c` | IN | `IN ('a','b','c')` |

**区间查询**：使用参数后缀 `_begin`/`_end`，如 `createTime_begin=2024-01-01&createTime_end=2024-12-31`。

**高级查询**：`superQueryParams` 参数携带 JSON 数组，支持嵌套 AND/OR 条件组合。

**排序**：`column`+`order` 单字段排序，`sortInfoString` 多字段排序。

## 注入约定：@Resource vs @Autowired

内容社区模块统一使用 `@Resource`（`jakarta.annotation.Resource`）：

```java
@Resource
private IContentChannelService contentChannelService;
```

其他模块（如 airag）使用 `@Autowired`。在内容模块新增代码时遵循 `@Resource` 惯例。

## 多租户隔离

**开关**: `OPEN_SYSTEM_TENANT_CONTROL = false`（默认关闭），配置在 `MybatisPlusSaasConfig`。

**启用时**：
- `TenantContextFilter` 从请求头 `X-Tenant-Id` 提取租户 ID，存入 `TenantContext`（ThreadLocal）
- `TenantLineInnerInterceptor` 自动为白名单表注入 `tenant_id = ?` 条件
- 白名单表在 `MybatisPlusSaasConfig` 的 `TENANT_TABLE` 列表中维护

**数据库字段**: `tenant_id`，对应实体字段 `tenantId`。

当前内容模块表未加入租户白名单，多租户隔离默认不生效。

## Knife4j / SpringDoc 接口文档

**依赖**: knife4j-openapi3-ui 4.5.0 + springdoc-openapi 2.7.0

**配置**: `Swagger3Config`，`knife4j.production=true` 时禁用。

**注解规范**（io.swagger.v3.oas.annotations）：

```java
@Tag(name = "内容社区频道", description = "频道创建、编辑、发布、下线等管理接口")
@RestController
@RequestMapping("/api/v1/content/channel")
public class ContentChannelController {

    @Operation(summary = "分页查询频道列表")
    @GetMapping("/list")
    public Result<IPage<ContentChannel>> list(
            @Parameter(description = "频道名称") @RequestParam(required = false) String name,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNo,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") Integer pageSize) {
        // ...
    }
}
```

- Controller 类加 `@Tag`
- 每个接口方法加 `@Operation`
- 参数加 `@Parameter`
- DTO 字段加 `@Schema(description = "...")`
- 仅带 `@Operation` 的方法会出现在文档中（`GlobalOpenApiMethodFilter`）

## 自动填充

审计字段 `createBy`、`createTime`、`updateBy`、`updateTime` 通过 MyBatis-Plus `MetaObjectHandler` 自动填充，无需手动设置。填充逻辑在 `jeecg-boot-base-core` 中实现。

## 典型 CRUD Controller 模板

```java
@Tag(name = "XXX管理")
@RestController
@RequestMapping("/api/v1/content/xxx")
@Slf4j
public class XxxController extends JeecgController<XxxEntity, IXxxService> {

    @Resource
    private IXxxService xxxService;

    @Operation(summary = "分页查询")
    @GetMapping("/list")
    public Result<IPage<XxxEntity>> list(
            XxxEntity entity,
            @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
            HttpServletRequest req) {
        QueryWrapper<XxxEntity> queryWrapper = QueryGenerator.initQueryWrapper(entity, req.getParameterMap());
        Page<XxxEntity> page = new Page<>(pageNo, pageSize);
        IPage<XxxEntity> pageList = xxxService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    @Operation(summary = "新增")
    @PostMapping("/add")
    public Result<String> add(@RequestBody XxxEntity entity) {
        xxxService.save(entity);
        return Result.OK("添加成功");
    }

    @Operation(summary = "编辑")
    @PostMapping("/edit")
    public Result<String> edit(@RequestBody XxxEntity entity) {
        xxxService.updateById(entity);
        return Result.OK("编辑成功");
    }

    @Operation(summary = "删除")
    @DeleteMapping("/delete")
    public Result<String> delete(@RequestParam String id) {
        xxxService.removeById(id);
        return Result.OK("删除成功");
    }
}
```
