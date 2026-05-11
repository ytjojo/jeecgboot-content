---
name: springboot-security-standards
description: Spring Boot 项目安全开发规范。当用户编写认证鉴权、处理用户输入、编写 SQL 查询、处理敏感数据（密码/API密钥/个人信息）、配置 CORS、实现文件上传、设计 API 接口安全时触发。只要涉及安全相关的代码（哪怕只是接收一个用户参数），都应该使用此技能检查是否存在安全隐患。
---

# Spring Boot 安全开发规范

本技能定义了 Spring Boot 项目必须遵守的安全开发规范。安全问题的代价远高于功能 bug——一次 SQL 注入可能泄露整个数据库，一次 XSS 可能劫持用户会话。这些规范不是锦上添花，而是底线。

## SQL 注入防护

### MyBatis 参数绑定

```xml
<!-- ✅ 安全：#{} 使用 PreparedStatement 预编译，自动转义特殊字符 -->
<select id="getUserByName" resultType="User">
    SELECT * FROM users WHERE username = #{username}
</select>

<!-- ❌ 危险：${} 是字符串拼接，直接注入 SQL -->
<select id="getUserByName" resultType="User">
    SELECT * FROM users WHERE username = '${username}'
</select>
<!-- 攻击者输入: admin' OR '1'='1 -->
<!-- 实际执行: SELECT * FROM users WHERE username = 'admin' OR '1'='1' -->
```

**唯一可以用 `${}` 的场景**：动态表名或列名（ORDER BY 字段名），且必须做白名单校验：

```java
// 动态排序字段必须做白名单校验
private static final Set<String> ALLOWED_SORT_FIELDS =
    Set.of("created_time", "updated_time", "username", "id");

public Page<User> getUsers(String sortField) {
    if (!ALLOWED_SORT_FIELDS.contains(sortField)) {
        throw new BusinessException("非法排序字段: " + sortField);
    }
    return userMapper.selectPage(page, sortField);
}
```

### LambdaQueryWrapper

LambdaQueryWrapper 本身是安全的（内部用 PreparedStatement），但要注意 `last()` 和 `apply()` 方法：

```java
// ✅ 安全：LambdaQueryWrapper 自动参数化
wrapper.eq(User::getUsername, userInput);

// ❌ 危险：last() 中拼接了用户输入
wrapper.last("ORDER BY " + userInput);

// ✅ 安全：apply() 中使用参数占位符
wrapper.apply("DATE_FORMAT(created_time, '%Y-%m') = {0}", monthStr);
```

---

## XSS 防护

### 输入过滤

对所有用户输入做 HTML 转义，防止存储型 XSS：

```java
import org.springframework.web.util.HtmlUtils;

// 存储前转义
String safeName = HtmlUtils.htmlEscape(userInput);

// 或使用 OWASP Java HTML Sanitizer
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;

PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);
String safeHtml = policy.sanitize(userInput);
```

### 响应头配置

```java
@Bean
public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http) {
    return http
        .headers(headers -> headers
            .contentTypeOptions(Customizer.withDefaults())  // X-Content-Type-Options: nosniff
            .frameOptions(frame -> frame.deny())             // X-Frame-Options: DENY
            .xssProtection(xss -> xss.headerValue(          // X-XSS-Protection
                XXssProtectionServerHttpHeadersWriter.HeaderValue.ENABLED_MODE_BLOCK))
        )
        .build();
}
```

---

## 认证与鉴权

### 密码安全

```java
// ✅ 正确：使用 BCrypt（自带盐值，每次加密结果不同）
@Bean
public BCryptPasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}

// 注册时加密
user.setPassword(passwordEncoder.encode(rawPassword));

// 登录时验证
boolean matches = passwordEncoder.matches(rawPassword, user.getPassword());

// ❌ 禁止：明文存储、MD5、SHA-1（都不安全）
user.setPassword(rawPassword);                    // 明文
user.setPassword(DigestUtils.md5Hex(rawPassword)); // MD5 彩虹表可破
```

### JWT 安全

```java
// 密钥不能硬编码在代码中，要从配置读取
@Value("${auth.jwt.secret}")
private String jwtSecret;

// 使用 HS512 算法（比 HS256 更安全）
String token = Jwts.builder()
    .setSubject(username)
    .setIssuedAt(new Date())
    .setExpiration(new Date(now + expiration))
    .signWith(signingKey, SignatureAlgorithm.HS512)
    .compact();

// Token 必须可以主动吊销（存 Redis，登出时删除）
public void revokeToken(String token) {
    String username = getUsernameFromToken(token);
    redisTemplate.delete("jwt:token:" + username);
}
```

### API Key 安全

```java
// API Key 在数据库中存储哈希值，不存明文
public void createApiKey(String rawKey) {
    ApiKey entity = new ApiKey();
    entity.setKeyHash(DigestUtils.sha256Hex(rawKey));
    entity.setKeyPrefix(rawKey.substring(0, 8)); // 只存前缀用于展示
    apiKeyMapper.insert(entity);
}

// 验证时对比哈希
public boolean validateApiKey(String rawKey) {
    String hash = DigestUtils.sha256Hex(rawKey);
    return apiKeyMapper.selectByHash(hash) != null;
}
```

---

## 敏感数据处理

### 配置中的密钥

```yaml
# ❌ 禁止：敏感信息硬编码在 yaml 中提交到 Git
spring:
  datasource:
    password: MyRealPassword123!

# ✅ 正确：使用环境变量
spring:
  datasource:
    password: ${SPRING_DATASOURCE_PASSWORD:default_for_dev}
```

- 生产环境密码、API Key、JWT Secret 全部通过环境变量注入
- `.env` 文件加入 `.gitignore`，绝不提交到 Git
- 开发环境可以有默认值（`${VAR:default}`），但生产环境必须显式配置

### 日志脱敏

```java
// ❌ 禁止：日志中打印敏感信息
log.info("用户登录: username={}, password={}", username, password);
log.info("API Key: {}", apiKey);
log.info("请求头: {}", request.getHeaders()); // 可能包含 Authorization

// ✅ 正确：敏感字段脱敏或不打印
log.info("用户登录: username={}", username);
log.info("API Key: {}****", apiKey.substring(0, 8));
```

### 响应数据脱敏

```java
// 返回给前端的 VO 中不包含敏感字段
@Data
public class UserVO {
    private Long id;
    private String username;
    private String email;
    // 没有 password 字段

    // 手机号脱敏
    public String getPhone() {
        if (phone != null && phone.length() == 11) {
            return phone.substring(0, 3) + "****" + phone.substring(7);
        }
        return phone;
    }
}

// 或在 Entity 上用 @JsonIgnore
@Data
@TableName("users")
public class User {
    private Long id;
    private String username;

    @JsonIgnore  // 序列化时忽略此字段
    private String password;
}
```

---

## 输入校验

### 基本原则

**永远不信任客户端输入**。所有来自外部的数据（请求参数、请求头、Cookie）都必须校验：

```java
@Data
public class CreateUserDTO {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度3-20个字符")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 8, max = 50, message = "密码长度8-50个字符")
    private String password;

    @Email(message = "邮箱格式不正确")
    private String email;
}
```

### 文件上传安全

```java
// 文件上传校验
public String uploadFile(MultipartFile file) {
    // 1. 校验文件大小
    if (file.getSize() > 10 * 1024 * 1024) { // 10MB
        throw new BusinessException("文件大小不能超过10MB");
    }

    // 2. 校验文件类型（不能只看后缀名，要检查 MIME 类型）
    String contentType = file.getContentType();
    Set<String> allowedTypes = Set.of("image/jpeg", "image/png", "image/gif");
    if (!allowedTypes.contains(contentType)) {
        throw new BusinessException("只允许上传 JPG/PNG/GIF 图片");
    }

    // 3. 重命名文件（防止路径穿越）
    String newFileName = UUID.randomUUID() + getExtension(file.getOriginalFilename());

    // 4. 存储到安全目录
    Path targetPath = Paths.get(uploadDir).resolve(newFileName).normalize();
    // 确保目标路径在上传目录内（防止 ../../../etc/passwd 攻击）
    if (!targetPath.startsWith(Paths.get(uploadDir))) {
        throw new BusinessException("非法文件路径");
    }

    Files.copy(file.getInputStream(), targetPath);
    return newFileName;
}
```

---

## CORS 配置

```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();

    // ❌ 禁止：生产环境允许所有来源
    // config.addAllowedOrigin("*");

    // ✅ 正确：白名单指定允许的域名
    config.setAllowedOrigins(List.of(
        "https://your-domain.com",
        "https://admin.your-domain.com"
    ));

    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
    config.setAllowCredentials(true);
    config.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/api/**", config);
    return source;
}
```

---

## 安全检查清单

编写或 Review 代码时逐条检查：

- [ ] SQL 查询是否全部使用 `#{}` 或 LambdaQueryWrapper？
- [ ] 用户输入是否经过校验（长度、格式、范围）？
- [ ] 密码是否使用 BCrypt 加密存储？
- [ ] JWT/API Key 等密钥是否从环境变量读取？
- [ ] 日志中是否打印了密码、API Key 等敏感信息？
- [ ] 返回给前端的数据是否去掉了敏感字段？
- [ ] 文件上传是否校验了类型、大小、路径？
- [ ] CORS 是否限制了允许的源？
- [ ] 错误信息是否暴露了系统内部细节？
