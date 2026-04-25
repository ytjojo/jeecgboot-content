---
name: springboot-exception-handling
description: Spring Boot 项目统一异常处理规范。当用户编写异常处理逻辑、定义错误码、创建自定义异常类、编写全局异常处理器、处理业务错误返回时触发。也适用于用户讨论错误响应格式、异常分类、try-catch 策略等场景。只要涉及到异常和错误处理，都应该使用此技能。
---

# Spring Boot 统一异常处理规范

本技能定义了 Spring Boot 项目中异常处理的完整方案。核心思想是：**业务代码只管抛异常，全局异常处理器统一兜底**，让 Controller 和 Service 代码保持干净。

## 异常体系设计

### 异常分类

| 类型 | 说明 | HTTP 状态码 | 处理方式 |
|------|------|-------------|----------|
| 参数校验异常 | 请求参数不合法 | 400 | 框架自动触发，全局处理器捕获 |
| 认证异常 | 未登录、token 过期 | 401 | Security 过滤器或全局处理器 |
| 权限异常 | 无操作权限 | 403 | Security 过滤器或全局处理器 |
| 业务异常 | 业务规则不满足 | 200（业务码非200） 或 400 | Service 层主动抛出 |
| 资源不存在 | 数据查不到 | 404 | Service 层主动抛出 |
| 系统异常 | 意料之外的错误 | 500 | 全局处理器兜底 |

### 自定义异常类

```java
/**
 * 业务异常基类
 * 所有可预期的业务错误都应继承此类
 */
@Getter
public class BusinessException extends RuntimeException {

    private final Integer code;

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }
}

/**
 * 资源不存在异常
 */
public class ResourceNotFoundException extends BusinessException {
    public ResourceNotFoundException(String resource, Object id) {
        super(404, resource + "不存在: " + id);
    }
}

/**
 * 重复资源异常
 */
public class DuplicateResourceException extends BusinessException {
    public DuplicateResourceException(String message) {
        super(409, message);
    }
}
```

为什么用 RuntimeException 而不是 checked Exception：Spring 的 `@Transactional` 默认只回滚 RuntimeException，而且 checked Exception 会污染方法签名，让调用链上的每个方法都要声明 throws。

### 错误码设计

```java
@Getter
@AllArgsConstructor
public enum ErrorCode {

    // 通用错误 1xxxx
    SUCCESS(200, "操作成功"),
    BAD_REQUEST(10001, "请求参数错误"),
    UNAUTHORIZED(10002, "未登录或登录已过期"),
    FORBIDDEN(10003, "没有操作权限"),
    NOT_FOUND(10004, "资源不存在"),
    INTERNAL_ERROR(10005, "服务器内部错误"),

    // 用户模块 2xxxx
    USER_NOT_FOUND(20001, "用户不存在"),
    USERNAME_DUPLICATE(20002, "用户名已被使用"),
    PASSWORD_WRONG(20003, "密码错误"),
    USER_DISABLED(20004, "用户已被禁用"),

    // 订单模块 3xxxx
    ORDER_NOT_FOUND(30001, "订单不存在"),
    ORDER_STATUS_INVALID(30002, "订单状态不允许此操作"),
    STOCK_INSUFFICIENT(30003, "库存不足");

    private final Integer code;
    private final String message;
}
```

错误码编码规则：
- **5位数字**：前2位是模块编号，后3位是具体错误
- **10xxx** 通用错误、**20xxx** 用户模块、**30xxx** 订单模块...
- 新增模块时在此枚举中添加对应段

---

## 全局异常处理器

```java
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 业务异常 —— Service 层主动抛出的可预期错误
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException ex) {
        log.warn("业务异常: code={}, message={}", ex.getCode(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getCode(), ex.getMessage()));
    }

    /**
     * 资源不存在
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFound(ResourceNotFoundException ex) {
        log.warn("资源不存在: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getCode(), ex.getMessage()));
    }

    /**
     * 参数校验异常（@Valid 校验失败）
     * WebFlux 环境下是 WebExchangeBindException
     * Spring MVC 环境下是 MethodArgumentNotValidException
     */
    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidation(
            WebExchangeBindException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
            errors.put(error.getField(), error.getDefaultMessage())
        );
        log.warn("参数校验失败: {}", errors);
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(400, "参数验证失败"));
    }

    /**
     * 认证异常
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentials(BadCredentialsException ex) {
        log.warn("认证失败: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(401, "用户名或密码错误"));
    }

    /**
     * 兜底异常 —— 所有未被上面捕获的异常
     * 这一层是防线，确保客户端永远收到结构化的 JSON 响应
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpected(Exception ex) {
        // 系统异常要记录完整堆栈，方便排查
        log.error("系统异常: ", ex);
        // 不要把异常详情暴露给客户端（安全原因）
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(500, "服务器内部错误，请稍后重试"));
    }
}
```

### 处理器规则

- `@RestControllerAdvice` 而不是 `@ControllerAdvice`，自动序列化为 JSON
- 异常处理方法**从具体到宽泛排列**：先处理特定异常，最后兜底 `Exception`
- **业务异常用 warn 级别日志**（可预期的，不需要报警）
- **系统异常用 error 级别日志 + 完整堆栈**（需要排查的未知错误）
- **永远不暴露系统异常详情给客户端**（如数据库连接信息、SQL 语句、堆栈信息）

---

## Service 层异常使用

### 基本原则

Service 层的职责是检查业务规则，不满足就抛异常。不要返回 null 或者特殊状态码让 Controller 去判断——那会让 Controller 充满 if-else。

```java
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    @Override
    public UserVO getUserById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new ResourceNotFoundException("用户", id);
        }
        return convertToVO(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createUser(CreateUserDTO dto) {
        // 检查用户名是否重复
        long count = this.count(new LambdaQueryWrapper<User>()
            .eq(User::getUsername, dto.getUsername()));
        if (count > 0) {
            throw new DuplicateResourceException("用户名已被使用: " + dto.getUsername());
        }

        User user = new User();
        BeanUtils.copyProperties(dto, user);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setCreatedAt(LocalDateTime.now());
        userMapper.insert(user);
    }
}
```

### try-catch 使用原则

- **不要在 Service 层 catch BusinessException**——让它一路抛到全局处理器
- **只 catch 你确实能处理的异常**，比如外部 API 调用失败需要降级
- **catch 后必须做有意义的事**（日志 + 降级 / 日志 + 转换为业务异常），不要空 catch

```java
// ❌ 错误：空 catch，异常被吃掉了
try {
    callExternalApi();
} catch (Exception e) {
    // 什么都不做
}

// ❌ 错误：catch 了又原样抛出，没有意义
try {
    userMapper.insert(user);
} catch (Exception e) {
    throw e;
}

// ✅ 正确：catch 外部异常，转为业务异常 + 记录日志
try {
    externalApiClient.sendNotification(userId);
} catch (Exception e) {
    log.error("发送通知失败, userId={}: {}", userId, e);
    // 通知失败不影响主流程，降级处理
}

// ✅ 正确：catch 后转为业务异常
try {
    smsService.sendCode(phone);
} catch (SmsException e) {
    log.error("短信发送失败: {}", e);
    throw new BusinessException(ErrorCode.SMS_SEND_FAILED);
}
```

---

## 事务中的异常

```java
// @Transactional 默认只回滚 RuntimeException
// 如果要回滚所有异常（包括 checked exception），指定 rollbackFor
@Transactional(rollbackFor = Exception.class)
public void transferMoney(Long fromId, Long toId, BigDecimal amount) {
    deductBalance(fromId, amount);
    addBalance(toId, amount);
    // 如果 addBalance 抛异常，deductBalance 也会回滚
}
```

关键规则：
- `@Transactional` 必须加 `rollbackFor = Exception.class`
- 不要在事务方法内部 catch 异常又不重新抛出（会导致事务不回滚）
- 事务方法必须是 public 的（Spring AOP 限制）
