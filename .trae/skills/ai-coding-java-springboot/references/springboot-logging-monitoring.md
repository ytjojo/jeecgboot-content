---
name: springboot-logging-monitoring
description: Spring Boot 项目日志与监控规范。当用户添加日志输出、配置日志级别、实现链路追踪、添加 Metrics 埋点、设计异步日志、配置日志文件滚动时触发。也适用于讨论 log.info/warn/error 的使用场景、结构化日志、日志脱敏、Prometheus 指标等话题。只要涉及日志和监控相关代码，都应使用此技能。
---

# Spring Boot 日志与监控规范

本技能定义了 Spring Boot 项目中日志记录和监控的标准。好的日志是线上排查问题的唯一救命稻草——出了问题没有日志等于盲人摸象。但日志太多也是灾难（磁盘撑爆、性能下降、关键信息淹没在噪音中）。

## 日志框架

统一使用 **SLF4J + Logback**（Spring Boot 默认），通过 Lombok `@Slf4j` 注解简化使用：

```java
@Slf4j
@Service
public class UserServiceImpl implements UserService {

    public UserVO getUserById(Long id) {
        log.debug("查询用户: id={}", id);
        User user = userMapper.selectById(id);
        if (user == null) {
            log.warn("用户不存在: id={}", id);
            throw new ResourceNotFoundException("用户", id);
        }
        log.info("查询用户成功: id={}, username={}", id, user.getUsername());
        return convertToVO(user);
    }
}
```

---

## 日志级别规范

每个级别有明确的使用场景，不能混用：

| 级别 | 场景 | 示例 |
|------|------|------|
| **ERROR** | 系统错误，需要立即关注。只用于**真正影响系统功能**的异常 | 数据库连接失败、外部服务不可用、数据损坏 |
| **WARN** | 可预期的异常情况，不影响核心功能但需要关注 | 用户认证失败、资源不存在、配置降级使用默认值 |
| **INFO** | 关键业务节点，记录系统正常运行的里程碑事件 | 服务启动完成、用户登录、订单创建、配置加载 |
| **DEBUG** | 开发调试信息，生产环境关闭 | 方法入参出参、SQL 参数、中间计算结果 |
| **TRACE** | 非常详细的跟踪信息，几乎不在生产使用 | 循环内的每步操作、字节级数据 |

### 常见误用

```java
// ❌ 错误：业务校验失败用 ERROR（这是可预期的，不是系统错误）
log.error("用户名已存在: {}", username);

// ✅ 正确：业务校验失败用 WARN
log.warn("用户名已存在: {}", username);

// ❌ 错误：正常流程用 WARN
log.warn("查询到 {} 条记录", count);

// ✅ 正确：正常流程用 INFO 或 DEBUG
log.info("查询到 {} 条记录", count);

// ❌ 错误：ERROR 级别不记录异常堆栈（丢失了最重要的排查信息）
log.error("处理失败: {}", e.getMessage());

// ✅ 正确：ERROR 必须记录完整异常堆栈
log.error("处理失败: {}", e.getMessage(), e);
```

---

## 日志内容规范

### 结构化日志

每条日志应包含足够的上下文信息，让人在不看代码的情况下理解发生了什么：

```java
// ❌ 不好：信息不足，无法排查
log.info("处理成功");
log.error("处理失败");

// ✅ 好：包含关键上下文
log.info("订单创建成功: orderId={}, userId={}, amount={}", orderId, userId, amount);
log.error("订单创建失败: orderId={}, userId={}, 原因: {}", orderId, userId, e.getMessage(), e);
```

### 日志格式

```yaml
logging:
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
```

格式说明：
- `%d{...}` — 精确到毫秒的时间戳
- `[%thread]` — 线程名（排查并发问题必备）
- `%-5level` — 日志级别（左对齐5字符）
- `%logger{36}` — 类名（最多36字符）

### 日志脱敏

敏感信息绝不能出现在日志中（参见 `springboot-security-standards` 技能）：

```java
// ❌ 禁止
log.info("用户登录: username={}, password={}", username, password);
log.info("API请求: Authorization={}", request.getHeader("Authorization"));

// ✅ 正确
log.info("用户登录: username={}", username);
log.info("API请求: apiKey={}****", apiKey.substring(0, Math.min(8, apiKey.length())));
```

---

## 日志文件配置

```yaml
logging:
  file:
    name: logs/application.log
  logback:
    rollingpolicy:
      max-file-size: 100MB        # 单个文件最大 100MB
      max-history: 30              # 保留 30 天
      total-size-cap: 3GB          # 总大小上限 3GB
      clean-history-on-start: false
  level:
    root: INFO
    com.example: INFO              # 应用代码
    org.springframework.web: WARN  # 框架日志调低
    com.baomidou.mybatisplus: WARN # ORM 日志调低
    org.springframework.security: WARN
```

### 生产 vs 开发环境

```yaml
# application-dev.yaml（开发环境）
logging:
  level:
    com.example: DEBUG
    org.springframework.web: DEBUG

# application-prod.yaml（生产环境）
logging:
  level:
    com.example: INFO
    org.springframework.web: WARN
```

---

## 异步日志

高流量场景下，同步写日志会阻塞业务线程。使用异步日志降低影响：

### 方案一：Logback AsyncAppender

```xml
<!-- logback-spring.xml -->
<configuration>
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>logs/application.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
            <fileNamePattern>logs/application.%d{yyyy-MM-dd}.%i.log</fileNamePattern>
            <maxFileSize>100MB</maxFileSize>
            <maxHistory>30</maxHistory>
            <totalSizeCap>3GB</totalSizeCap>
        </rollingPolicy>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>

    <appender name="ASYNC_FILE" class="ch.qos.logback.classic.AsyncAppender">
        <queueSize>1024</queueSize>
        <discardingThreshold>0</discardingThreshold>
        <appender-ref ref="FILE"/>
    </appender>

    <root level="INFO">
        <appender-ref ref="ASYNC_FILE"/>
    </root>
</configuration>
```

### 方案二：业务日志异步写入数据库

对于需要结构化存储的业务日志（如请求日志），使用独立线程池异步写入：

```java
@Slf4j
@Component
@RequiredArgsConstructor
public class LogWriter {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final RequestLogService requestLogService;

    public void submit(RequestLogContext ctx) {
        executor.submit(() -> {
            try {
                requestLogService.save(convert(ctx));
            } catch (Exception e) {
                log.error("日志写入失败: ", e);
                // 日志写入失败不能影响业务流程
            }
        });
    }

    @PreDestroy
    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
```

---

## 链路追踪

为每个请求分配唯一的 requestId，贯穿整个调用链路，方便从海量日志中捞出一次请求的所有日志：

```java
// WebFilter 中注入 requestId
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestIdFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String requestId = exchange.getRequest().getHeaders()
            .getFirst("X-Request-Id");
        if (requestId == null) {
            requestId = UUID.randomUUID().toString().replace("-", "");
        }

        exchange.getResponse().getHeaders().add("X-Request-Id", requestId);

        // 放入 Reactor Context，后续链路可以取到
        String finalRequestId = requestId;
        return chain.filter(exchange)
            .contextWrite(ctx -> ctx.put("requestId", finalRequestId));
    }
}
```

日志中输出 requestId：

```java
log.info("[{}] 处理请求: method={}, path={}",
    requestId, request.getMethod(), request.getPath());
```

---

## Metrics 监控埋点

使用 Micrometer + Prometheus 收集关键指标：

```java
@Component
@RequiredArgsConstructor
public class RequestMetrics {

    private final MeterRegistry meterRegistry;

    // 计数器：记录请求总数
    public void recordRequest(String provider, String model, String status) {
        meterRegistry.counter("llm_requests_total",
            "provider", provider,
            "model", model,
            "status", status
        ).increment();
    }

    // 直方图：记录响应时间分布
    public void recordLatency(String provider, long durationMs) {
        meterRegistry.timer("llm_request_duration",
            "provider", provider
        ).record(Duration.ofMillis(durationMs));
    }

    // 仪表盘：记录当前活跃连接数
    public void recordActiveConnections(String provider, int count) {
        meterRegistry.gauge("llm_active_connections",
            Tags.of("provider", provider),
            count
        );
    }
}
```

### 必须监控的关键指标

| 指标 | 类型 | 说明 |
|------|------|------|
| 请求总数 | Counter | 按接口/状态码分组 |
| 响应时间 | Timer/Histogram | P50/P95/P99 |
| 错误率 | Counter | 按错误类型分组 |
| 活跃连接数 | Gauge | 数据库/Redis/HTTP 连接池 |
| JVM 内存 | Gauge | 堆内存使用率 |
| 线程池状态 | Gauge | 活跃线程数、队列长度 |

---

## 日志检查清单

- [ ] 是否使用了正确的日志级别？
- [ ] ERROR 日志是否包含了完整的异常堆栈？
- [ ] 日志内容是否包含足够的上下文（关键业务ID、操作人、操作内容）？
- [ ] 是否有敏感信息泄露？（密码、API Key、token）
- [ ] 生产环境日志级别是否设置为 INFO？
- [ ] 日志文件是否配置了滚动策略和大小上限？
- [ ] 高流量接口是否使用了异步日志？
