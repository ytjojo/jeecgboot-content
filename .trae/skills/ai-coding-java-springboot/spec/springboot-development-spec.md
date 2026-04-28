# Spring Boot 后端开发规范

> 本规范适用于基于 Spring Boot + MyBatis Plus + Redis 技术栈的后端项目。
> 编码时请结合对应的 skills 技能获取详细指导。

## 技术栈

| 组件 | 版本/技术 |
|------|----------|
| 语言 | Java 17 |
| 框架 | Spring Boot 3.x (WebFlux/Reactive) |
| ORM | MyBatis Plus 3.5.x |
| 数据库 | MySQL 8.0 / SQLite |
| 缓存 | Redis 6.0+ (Lettuce) |
| 构建 | Maven |

---

## 一、分层架构

```
Controller（接口层）
    ↓ 调用
BizManageService（业务合并层）
    ↓ 调用一个或多个
Service（数据库业务层，基于 MyBatis Plus 生成）
    ↓ 调用
Mapper/DAO（数据访问层）
    ↓ 操作
Database
```

### 各层职责

| 层 | 职责 | 禁止事项 |
|---|------|---------|
| **Controller** | 接收参数、参数校验（@Valid）、调用 BizManageService、返回 Result | 不写业务逻辑、不直接调用 Service 或 Mapper |
| **BizManageService** | 编排业务流程、组合多个 Service、事务管理（@Transactional）、DTO/VO 转换 | 不处理 HTTP 细节、不直接写 SQL |
| **Service** | 单表 CRUD，继承 MyBatis Plus `ServiceImpl` | 不编排跨表业务逻辑 |
| **Mapper** | 数据库 CRUD、自定义 SQL（XML） | 不写业务逻辑 |

### 依赖方向（只能向下，不能反向或跳层）

- Controller → BizManageService → Service → Mapper

> 详细规范见 skill: `springboot-coding-conventions`

---

## 二、包结构

```
com.{company}.{project}
├── controller/      # REST 控制器
├── biz/             # 业务合并层（BizManageService）
├── service/         # 数据库业务层
│   └── impl/        # 服务实现（extends ServiceImpl）
├── mapper/          # MyBatis Mapper 接口
├── entity/          # 数据库实体（@TableName）
├── dto/             # 请求参数对象（CreateXxxDTO, UpdateXxxDTO）
├── vo/              # 返回前端对象（XxxVO）
├── config/          # Spring 配置类
├── filter/          # 过滤器（认证、鉴权）
├── exception/       # 异常类 + 全局异常处理器
├── util/            # 工具类
├── constant/        # 常量类（RedisKeyConstants 等）
└── scheduled/       # 定时任务
```

---

## 三、接口设计规范

### URL 格式

```
GET    /api/v1/{资源复数}/list      # 列表查询
GET    /api/v1/{资源复数}/detail    # 单条查询
POST   /api/v1/{资源复数}/save      # 新增
POST   /api/v1/{资源复数}/update    # 修改
DELETE /api/v1/{资源复数}/{ids}     # 删除（支持批量，逗号分隔）
```

### 统一响应体

```json
{
  "success": true,
  "message": "操作成功",
  "code": 200,
  "result": { "...": "..." },
  "timestamp": 1711180800000
}
```

### 参数校验

- 请求 DTO 使用 Jakarta Validation 注解：`@NotBlank`, `@NotNull`, `@Size`, `@Pattern`, `@Email`
- Controller 方法参数加 `@Valid`
- 每个校验注解必须带 `message` 参数

### 分页

- 标准分页参数：`current`（页码，从1开始）、`size`（每页条数，最大100）
- 深分页使用游标分页，禁止 OFFSET

> 详细规范见 skill: `springboot-api-standards`

---

## 四、数据库设计规范

### 表设计

- 表名和字段：`snake_case`（小写下划线）
- 字符集：`utf8mb4`，排序规则 `utf8mb4_unicode_ci`
- 引擎：`InnoDB`
- 每个字段必须有 `COMMENT`

### 必备字段

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | `BIGINT UNSIGNED AUTO_INCREMENT` | 主键 |
| `created_at` | `DATETIME DEFAULT CURRENT_TIMESTAMP` | 创建时间 |
| `updated_at` | `DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP` | 更新时间 |

### 关键规则

- **不使用外键约束**，关联关系由 BizManageService 代码逻辑 + 事务保证
- **关联字段必须加索引**（如 `idx_group_id`）
- **金额用 DECIMAL**，不用 FLOAT/DOUBLE
- **索引命名**：普通索引 `idx_字段名`，唯一索引 `uk_字段名`

### MyBatis Mapper

- 简单 CRUD 继承 `BaseMapper`
- 复杂查询用 XML Mapper + `resultMap`
- 条件查询优先用 `LambdaQueryWrapper`（类型安全）
- 参数占位符统一用 `#{}`（防 SQL 注入），禁止 `${}`

> 详细规范见 skill: `springboot-db-design`

---

## 五、依赖注入

统一使用 `@Resource` 注解，不使用 `@Autowired` 和 `@RequiredArgsConstructor`：

```java
@Service
@Slf4j
public class OrderBizManageService {
    @Resource
    private OrderService orderService;
    @Resource
    private OrderItemService orderItemService;
}
```

> 详细规范见 skill: `springboot-coding-conventions`

---

## 六、Redis 使用规范

### Key 命名

所有 Key 必须以业务系统前缀开头：

```
格式：{系统前缀}:{业务模块}:{类型}:{标识}
示例：lumina:user:info:123
```

- 通过 `RedisKeyConstants` 常量类统一管理
- 所有缓存必须设置过期时间
- 过期时间加随机偏移，防止缓存雪崩

> 详细规范见 skill: `springboot-coding-conventions` 和 `springboot-performance`

---

## 七、性能红线（绝对禁止）

| 红线 | 说明 |
|------|------|
| **禁止循环查库** | 不在 for/while/stream 中调用 Mapper/Service，改为批量查询 + 内存关联 |
| **禁止 OFFSET 深分页** | 大数据量用游标分页（WHERE id > lastId）或子查询优化 |
| **禁止循环操作 Redis** | 用 Pipeline 或 multiGet 批量操作 |
| **禁止 SELECT *** | 只查需要的字段，特别是含大文本的表 |
| **禁止索引字段上用函数** | 如 `DATE(created_at)`，改为范围查询 |

> 详细规范见 skill: `springboot-performance`

---

## 八、异常处理

### 异常体系

```
BusinessException（业务异常基类）
├── ResourceNotFoundException（资源不存在，404）
├── DuplicateResourceException（资源冲突，409）
└── 其他业务异常...
```

### 规则

- 全局异常处理器 `@RestControllerAdvice` 统一兜底
- 业务异常用 `warn` 级别日志，系统异常用 `error` + 完整堆栈
- 不暴露系统异常详情给客户端
- `@Transactional` 必须加 `rollbackFor = Exception.class`
- 错误码用 5 位数字枚举：`10xxx` 通用、`20xxx` 用户、`30xxx` 订单...

> 详细规范见 skill: `springboot-exception-handling`

---

## 九、安全规范

| 项目 | 要求 |
|------|------|
| SQL 注入 | MyBatis 统一用 `#{}`，LambdaQueryWrapper 的 `last()`/`apply()` 不拼接用户输入 |
| 密码存储 | BCrypt 加密，禁止明文/MD5/SHA-1 |
| JWT/API Key | 密钥从环境变量读取，不硬编码；Token 存 Redis 支持主动吊销 |
| 日志脱敏 | 禁止打印密码、API Key、Authorization 头 |
| 返回脱敏 | VO 不含敏感字段，手机号/身份证脱敏展示 |
| 文件上传 | 校验类型（MIME）、大小、路径（防穿越），重命名文件 |
| CORS | 生产环境白名单域名，禁止 `*` |
| 输入校验 | 所有外部输入必须校验（长度、格式、范围） |

> 详细规范见 skill: `springboot-security-standards`

---

## 十、日志规范

### 级别使用

| 级别 | 场景 |
|------|------|
| ERROR | 系统错误（数据库连接失败、外部服务不可用），必须带完整堆栈 |
| WARN | 可预期异常（认证失败、资源不存在） |
| INFO | 关键业务节点（登录、订单创建、配置加载） |
| DEBUG | 调试信息（方法入参出参），生产环境关闭 |

### 规则

- 使用 `@Slf4j` + SLF4J
- 日志包含足够上下文（业务ID、操作人、操作内容）
- 敏感信息不打印（密码、密钥、Token）
- 高流量场景使用异步日志
- 日志文件滚动：单文件 100MB，保留 30 天

> 详细规范见 skill: `springboot-logging-monitoring`

---

## 十一、Lombok 使用

```java
@Data           // Entity, DTO, VO
@Builder        // DTO, VO（需要建造者模式时）
@Slf4j          // 日志
@NoArgsConstructor  // Entity（MyBatis Plus 需要）
@AllArgsConstructor // 配合 @Builder 使用
```

- Entity 不用 `@Builder`（与 MyBatis Plus 冲突）
- 不用 `@RequiredArgsConstructor`（统一用 `@Resource` 注入）

---

## 十二、配置管理

- 自定义配置用 `@ConfigurationProperties(prefix = "xxx")`，不到处写 `@Value`
- 敏感配置通过环境变量注入：`${ENV_VAR:default}`
- 所有配置项有默认值，开发环境零配置可启动
- 不同环境用 profile 区分：`application-dev.yaml`, `application-prod.yaml`

---

## 代码质量检查清单

每次提交前逐条自检：

- [ ] 分层正确？Controller 没有业务逻辑？
- [ ] 使用 `@Resource` 注入？
- [ ] 事务注解加了 `rollbackFor = Exception.class`？
- [ ] 没有循环查库（N+1）？
- [ ] 分页没有使用 OFFSET？
- [ ] Redis Key 有系统前缀和过期时间？
- [ ] SQL 用 `#{}` 不用 `${}`？
- [ ] 日志级别正确？ERROR 有完整堆栈？
- [ ] 敏感信息没有出现在日志和返回值中？
- [ ] 数据库表有 COMMENT、有时间戳字段？
- [ ] 没有外键约束？关联字段有索引？
