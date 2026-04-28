---
name: springboot-coding-conventions
description: Spring Boot 项目通用编码规范。当用户编写 Java 代码、设计类结构、使用设计模式、管理依赖注入、编写事务逻辑、使用 Redis、配置 Spring Bean、使用 Lombok 注解时触发。这是最基础的编码规范技能，覆盖分层架构、命名规范、设计模式、Redis 使用、配置管理、事务管理等方面。任何 Spring Boot Java 代码编写都应参考此技能。
---

# Spring Boot 通用编码规范

本技能定义了 Spring Boot 项目的基础编码标准。这些规范的目标是让代码库保持一致性——当所有代码看起来像一个人写的，维护成本会大幅降低。

## 分层架构

### 标准分层

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

| 层 | 职责 | 不该做的事 |
|---|------|-----------|
| **Controller** | 接收参数、参数校验、调用 BizManageService、返回响应 | 不写业务逻辑、不直接调用 Service 或 Mapper |
| **BizManageService** | 编排业务流程、组合多个 Service 完成复杂业务、事务管理、数据组装转换 | 不处理 HTTP 细节、不直接写 SQL |
| **Service** | 单表的 CRUD 操作，基于 MyBatis Plus `ServiceImpl` 生成，提供基础数据访问能力 | 不编排跨表业务逻辑（那是 BizManageService 的事） |
| **Mapper** | 数据库 CRUD、自定义 SQL 查询 | 不写业务逻辑 |

### 为什么需要 BizManageService

在实际项目中，一个业务操作往往涉及多张表。比如"创建订单"需要：写订单表 + 写订单明细表 + 扣减库存 + 记录日志。如果把这些逻辑全写在 `OrderService` 里，会导致 Service 之间互相依赖、职责混乱。

引入 BizManageService 后，职责清晰：
- **Service 层**只关心自己那张表的 CRUD（`OrderService` 管订单表，`StockService` 管库存表）
- **BizManageService** 负责把多个 Service 组合起来完成一个完整的业务动作

```java
// ✅ BizManageService 编排业务流程
@Service
@Slf4j
public class OrderBizManageService {

    @Resource
    private OrderService orderService;
    @Resource
    private OrderItemService orderItemService;
    @Resource
    private StockService stockService;
    @Resource
    private LogWriter logWriter;

    @Transactional(rollbackFor = Exception.class)
    public OrderVO createOrder(CreateOrderDTO dto) {
        // 1. 创建订单主表
        Order order = new Order();
        BeanUtils.copyProperties(dto, order);
        order.setCreatedAt(LocalDateTime.now());
        orderService.save(order);

        // 2. 批量创建订单明细
        List<OrderItem> items = dto.getItems().stream()
            .map(itemDto -> {
                OrderItem item = new OrderItem();
                BeanUtils.copyProperties(itemDto, item);
                item.setOrderId(order.getId());
                return item;
            }).collect(Collectors.toList());
        orderItemService.saveBatch(items);

        // 3. 扣减库存
        stockService.deductBatch(dto.getItems());

        // 4. 记录日志
        logWriter.submit(buildLogContext(order));

        return convertToVO(order, items);
    }
}
```

```java
// ✅ Service 层只做单表 CRUD，继承 MyBatis Plus ServiceImpl
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order>
    implements OrderService {
    // 基础 CRUD 由 ServiceImpl 提供
    // 只在需要自定义单表查询时添加方法
}
```

```java
// ✅ Controller 只调 BizManageService
@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    @Resource
    private OrderBizManageService orderBizManageService;

    @PostMapping("/save")
    public Result<OrderVO> createOrder(@Valid @RequestBody CreateOrderDTO dto) {
        return Result.OK(orderBizManageService.createOrder(dto));
    }
}
```

### 依赖方向

只能向下依赖，不能反向、不能跳层：
- Controller → BizManageService ✅
- BizManageService → Service（一个或多个） ✅
- Service → Mapper ✅
- Controller → Service ❌（跳过 BizManageService）
- Controller → Mapper ❌（跳层）
- Service → BizManageService ❌（反向依赖）
- Mapper → Service ❌（反向依赖）

---

## 命名规范

### 包命名

```
com.example.project
├── controller/      # REST 控制器
├── biz/             # 业务合并层（BizManageService）
├── service/         # 数据库业务层（基于 MyBatis Plus）
│   └── impl/        # 服务实现
├── mapper/          # MyBatis Mapper 接口
├── entity/          # 数据库实体
├── dto/             # 数据传输对象
├── vo/              # 视图对象（返回前端）
├── config/          # Spring 配置类
├── filter/          # 过滤器
├── exception/       # 异常类
├── util/            # 工具类
├── constant/        # 常量类
└── scheduled/       # 定时任务
```

### 类命名

| 类型 | 命名规则 | 示例 |
|------|---------|------|
| Controller | `{资源}Controller` | `UserController`, `OrderController` |
| BizManageService | `{业务}BizManageService` | `OrderBizManageService`, `UserBizManageService` |
| Service 接口 | `{资源}Service` | `UserService`, `OrderService` |
| Service 实现 | `{资源}ServiceImpl` | `UserServiceImpl`, `OrderServiceImpl` |
| Mapper | `{资源}Mapper` | `UserMapper`, `OrderMapper` |
| Entity | 与表名对应的驼峰形式 | `User`, `UserOrder`, `RequestLog` |
| DTO | `{动作}{资源}DTO` | `CreateUserDTO`, `UpdateOrderDTO` |
| VO | `{资源}VO` | `UserVO`, `OrderDetailVO` |
| Config | `{模块}Config` | `SecurityConfig`, `RedisConfig` |
| Exception | `{描述}Exception` | `BusinessException`, `ResourceNotFoundException` |
| Util | `{功能}Util` | `JwtUtil`, `DateUtil` |
| 常量 | `{模块}Constants` | `RedisConstants`, `ApiConstants` |

### 方法命名

```java
// Service 层方法命名
getUserById(Long id)           // get + 名词 + By + 条件
listUsersByStatus(Integer status) // list + 名词复数 + By + 条件
createUser(CreateUserDTO dto)  // create + 名词
updateUser(Long id, ...)       // update + 名词
deleteUser(Long id)            // delete + 名词
countUsersByType(Integer type) // count + 名词 + By + 条件
checkUsernameExists(String name) // check/is/has + 描述
```

### 常量命名

```java
// 常量全大写，下划线分隔
public class RedisConstants {
    public static final String USER_KEY_PREFIX = "user:info:";
    public static final long DEFAULT_EXPIRE_SECONDS = 3600L;
    public static final int MAX_RETRY_COUNT = 3;
}

// 不要用魔法数字
// ❌
if (user.getStatus() == 1) { ... }
// ✅
if (user.getStatus().equals(UserStatus.ACTIVE)) { ... }
```

---

## 依赖注入

### 使用 @Resource 注入

统一使用 `@Resource` 注解进行依赖注入。`@Resource` 是 JSR-250 标准注解，按名称匹配注入，语义更明确：

```java
// ✅ 推荐：@Resource 注入
@Service
@Slf4j
public class OrderBizManageService {

    @Resource
    private OrderService orderService;

    @Resource
    private OrderItemService orderItemService;

    @Resource
    private RedisTemplate<String, Object> redisObjectTemplate;
}
```

```java
// ✅ Controller 中同样使用 @Resource
@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    @Resource
    private OrderBizManageService orderBizManageService;
}
```

### 为什么用 @Resource 而不是 @Autowired

- `@Resource` 按名称匹配（`byName`），当存在多个同类型 Bean 时更精确
- `@Autowired` 按类型匹配（`byType`），多个同类型 Bean 时需要配合 `@Qualifier`
- `@Resource` 是 Java 标准注解，不依赖 Spring 框架
- 团队统一用一种方式，避免混用造成混乱

```java
// ❌ 不使用 @Autowired
@Autowired
private UserMapper userMapper;

// ❌ 不使用构造器注入（@RequiredArgsConstructor）
@RequiredArgsConstructor
public class UserServiceImpl {
    private final UserMapper userMapper;
}

// ✅ 统一使用 @Resource
@Resource
private UserMapper userMapper;
```

---

## 设计模式应用

### 策略模式（Strategy）

当有多个同类实现需要根据条件选择时使用。典型场景：多个 LLM 供应商的请求执行器。

```java
// 1. 定义接口
public interface LlmRequestExecutor {
    boolean supports(String type);
    Mono<ObjectNode> executeNormal(ObjectNode request, ModelGroupConfigItem provider, ...);
    Flux<ServerSentEvent<String>> executeStream(ObjectNode request, ModelGroupConfigItem provider, ...);
}

// 2. 多个实现
@Service
public class OpenAiRequestExecutor extends AbstractRequestExecutor {
    @Override
    public boolean supports(String type) {
        return type.startsWith("openai_");
    }
}

@Service
public class AnthropicRequestExecutor extends AbstractRequestExecutor {
    @Override
    public boolean supports(String type) {
        return type.equals("anthropic_messages");
    }
}

// 3. 通过 Spring 自动注入所有实现，运行时动态选择
@Service
@RequiredArgsConstructor
public class RelayServiceImpl implements RelayService {
    private final List<LlmRequestExecutor> executors;

    private LlmRequestExecutor getExecutor(String type) {
        return executors.stream()
            .filter(e -> e.supports(type))
            .findFirst()
            .orElseThrow(() -> new BusinessException("不支持的请求类型: " + type));
    }
}
```

### 模板方法模式（Template Method）

当多个实现有共同的流程骨架时，把公共逻辑提取到抽象基类：

```java
public abstract class AbstractRequestExecutor implements LlmRequestExecutor {

    // 公共方法：创建日志上下文
    protected RequestLogContext createLogContext(ObjectNode request, ...) {
        RequestLogContext ctx = new RequestLogContext();
        ctx.setId(String.valueOf(snowflakeIdGenerator.nextId()));
        ctx.setRequestTime(System.currentTimeMillis() / 1000);
        // ... 公共初始化逻辑
        return ctx;
    }

    // 公共方法：计算费用
    protected void calculateCost(RequestLogContext ctx) { ... }

    // 子类实现具体的请求发送逻辑
    @Override
    public abstract Mono<ObjectNode> executeNormal(ObjectNode request, ...);
}
```

### Builder 模式

构造复杂对象时使用，配合 Lombok `@Builder`：

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String type;
    private Long expiresIn;
    private String username;
}

// 使用
LoginResponse response = LoginResponse.builder()
    .token(jwt)
    .type("Bearer")
    .expiresIn(86400000L)
    .username(username)
    .build();
```

---

## Redis 使用规范

### 键命名

所有 Redis Key 必须以**业务系统前缀**开头，避免多个系统共用同一个 Redis 实例时 Key 冲突：

```java
// 格式：{系统前缀}:{业务模块}:{类型}:{标识}
// 系统前缀从配置读取，不要硬编码

// 例如系统前缀为 "lumina"：
"lumina:user:info:123"                // 用户信息
"lumina:group:config:gpt-4"          // 分组配置
"lumina:jwt:token:admin"              // JWT token
"lumina:lock:order:ORD001"            // 分布式锁
"lumina:rate:limit:api:192.168.1.1"   // 限流计数

// 用常量类统一管理，系统前缀从配置注入
public class RedisKeyConstants {

    /**
     * 系统前缀，从 application.yaml 的 spring.application.name 或自定义配置读取
     * 在 @PostConstruct 或配置类中初始化
     */
    public static String KEY_PREFIX = "lumina";

    public static final String USER_INFO = "%s:user:info:%d";
    public static final String GROUP_CONFIG = "%s:group:config:%s";
    public static final String JWT_TOKEN = "%s:jwt:token:%s";

    // 拼接 Key 的工具方法
    public static String buildKey(String pattern, Object... args) {
        Object[] allArgs = new Object[args.length + 1];
        allArgs[0] = KEY_PREFIX;
        System.arraycopy(args, 0, allArgs, 1, args.length);
        return String.format(pattern, allArgs);
    }
}

// 使用示例
String key = RedisKeyConstants.buildKey(RedisKeyConstants.USER_INFO, userId);
// → "lumina:user:info:123"
```

为什么必须加系统前缀：
- 多个微服务可能共用同一个 Redis 实例，不加前缀会导致 Key 冲突和数据覆盖
- 通过前缀可以快速识别 Key 属于哪个系统，方便运维排查
- 迁移或清理数据时可以按前缀批量操作（`KEYS lumina:*`）

### 序列化配置

```java
// String 类型数据用 StringRedisSerializer
// 对象类型数据用 Jackson2JsonRedisSerializer
// Key 统一用 StringRedisSerializer（可读性好）

@Bean
public RedisTemplate<String, Object> redisObjectTemplate(RedisConnectionFactory factory) {
    RedisTemplate<String, Object> template = new RedisTemplate<>();
    template.setConnectionFactory(factory);

    // Key 用 String 序列化
    template.setKeySerializer(new StringRedisSerializer());
    template.setHashKeySerializer(new StringRedisSerializer());

    // Value 用 Jackson 序列化
    Jackson2JsonRedisSerializer<Object> jackson = new Jackson2JsonRedisSerializer<>(Object.class);
    ObjectMapper om = new ObjectMapper();
    om.registerModule(new JavaTimeModule()); // 支持 LocalDateTime
    om.activateDefaultTyping(
        LaissezFaireSubTypeValidator.instance,
        ObjectMapper.DefaultTyping.NON_FINAL); // 保留类型信息
    jackson.setObjectMapper(om);

    template.setValueSerializer(jackson);
    template.setHashValueSerializer(jackson);

    return template;
}
```

### 过期时间

```java
// 所有缓存必须设置过期时间
redisTemplate.opsForValue().set(key, value, 1, TimeUnit.HOURS);

// 常用过期时间常量
public class RedisTTL {
    public static final long USER_INFO = 3600;    // 1小时
    public static final long CONFIG = 1800;        // 30分钟
    public static final long CAPTCHA = 300;        // 5分钟
    public static final long LOCK = 10;            // 10秒
}
```

---

## 事务管理

```java
// 1. 涉及多表写操作的方法必须加事务
@Transactional(rollbackFor = Exception.class)
public void createOrder(CreateOrderDTO dto) {
    Order order = new Order();
    orderMapper.insert(order);

    List<OrderItem> items = convertItems(dto.getItems(), order.getId());
    orderItemService.saveBatch(items);

    // 扣减库存
    stockService.deduct(dto.getItems());
}

// 2. 只读方法加 readOnly 提示（优化数据库性能）
@Transactional(readOnly = true)
public OrderVO getOrderDetail(Long orderId) {
    // ...
}

// 3. 注意事务传播行为
// 默认 REQUIRED：加入当前事务或创建新事务
// REQUIRES_NEW：挂起当前事务，创建新事务（适用于日志记录等不想被回滚的操作）
@Transactional(propagation = Propagation.REQUIRES_NEW)
public void saveOperationLog(OperationLog log) {
    logMapper.insert(log);
}
```

### 事务陷阱

```java
// ❌ 陷阱1：同一个类内部调用，事务不生效（Spring AOP 代理限制）
@Service
public class OrderServiceImpl {
    public void process() {
        this.createOrder(dto); // 直接调用，@Transactional 不生效！
    }

    @Transactional(rollbackFor = Exception.class)
    public void createOrder(CreateOrderDTO dto) { ... }
}

// ✅ 解决：注入自己 或 拆分到不同的 Service
@Service
@RequiredArgsConstructor
public class OrderServiceImpl {
    private final OrderTransactionService txService; // 拆到独立类

    public void process() {
        txService.createOrder(dto); // 通过代理调用，事务生效
    }
}

// ❌ 陷阱2：catch 了异常但没重新抛出，事务不会回滚
@Transactional(rollbackFor = Exception.class)
public void createOrder(CreateOrderDTO dto) {
    try {
        orderMapper.insert(order);
        stockService.deduct(items); // 这里抛异常
    } catch (Exception e) {
        log.error("error", e);
        // 异常被吃掉了，事务不会回滚！
    }
}
```

---

## Lombok 使用规范

```java
// 推荐使用的注解
@Data           // getter + setter + toString + equals + hashCode
@Builder        // 建造者模式
@Slf4j          // 日志
// 不使用 @RequiredArgsConstructor，统一用 @Resource 注入
@AllArgsConstructor       // 全参构造器
@NoArgsConstructor        // 无参构造器

// 注意事项：
// 1. Entity 用 @Data 就够了
@Data
@TableName("users")
public class User { ... }

// 2. DTO/VO 需要 Builder 时加 @Builder + @NoArgsConstructor + @AllArgsConstructor
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse { ... }

// 3. Service / BizManageService 类用 @Resource 注入
@Service
@Slf4j
public class UserBizManageService {
    @Resource
    private UserService userService;
}

// 4. 不要在 Entity 上用 @Builder（与 MyBatis Plus 冲突）
// MyBatis Plus 需要无参构造器来实例化对象
```

---

## 配置管理

### 自定义配置绑定

```java
@Data
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private Auth auth = new Auth();
    private Cache cache = new Cache();

    @Data
    public static class Auth {
        private Jwt jwt = new Jwt();

        @Data
        public static class Jwt {
            private String secret;
            private long expiration = 86400000; // 默认值
        }
    }

    @Data
    public static class Cache {
        private long defaultTtl = 3600;
        private int maxSize = 1000;
    }
}
```

### 使用原则

- **结构化配置**用 `@ConfigurationProperties`，不要到处写 `@Value`
- **敏感配置**通过环境变量注入：`${ENV_VAR:default}`
- **所有配置项都要有默认值**，确保开发环境零配置可启动
- **不同环境用 profile 区分**：`application-dev.yaml`, `application-prod.yaml`

---

## 代码质量检查清单

每次编写代码时逐条自检：

- [ ] 分层是否正确？Controller 中是否有业务逻辑？
- [ ] 命名是否清晰、符合规范？
- [ ] 是否使用 @Resource 注入？（不用 @Autowired 和 @RequiredArgsConstructor）
- [ ] 事务注解是否加了 `rollbackFor = Exception.class`？
- [ ] Redis 操作是否设置了过期时间？
- [ ] 常量是否提取到了常量类，而不是魔法数字/字符串？
- [ ] 是否有重复代码可以抽取为公共方法？
- [ ] 方法是否过长？（超过 80 行考虑拆分）
- [ ] 类是否过大？（超过 500 行考虑拆分职责）
