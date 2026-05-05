# Content User Account Email Register Bind Unbind Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在 `content/user` 模块内补齐邮箱注册，以及手机号/邮箱绑定解绑的最小后端闭环，并补齐审计与测试。

**Architecture:** 继续复用 `ContentAccountController -> IContentAccountService -> SystemUserAccountGateway` 的现有账号编排边界，不新增数据库表，也不扩到平台登录主链路。平台联系方式的真实写入继续落在 `SysUser.phone` 和 `SysUser.email`，社区侧只负责资料初始化、敏感操作校验、解绑保护和审计日志落库。

**Tech Stack:** Spring Boot 3, JeecgBoot, MyBatis-Plus, JUnit 5, Mockito, MockMvc

---

## 文件边界

### 新增文件

- `docs/superpowers/plans/2026-05-05-content-user-account-email-register-bind-unbind-plan.md`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/req/account/ContentEmailRegisterReq.java`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/req/account/ContentAccountBindMobileReq.java`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/req/account/ContentAccountBindEmailReq.java`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/req/account/ContentAccountUnbindMobileReq.java`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/req/account/ContentAccountUnbindEmailReq.java`

### 修改文件

- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/controller/ContentAccountController.java`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/IContentAccountService.java`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/impl/ContentAccountServiceImpl.java`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/gateway/SystemUserAccountGateway.java`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/gateway/impl/SystemUserAccountGatewayImpl.java`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/entity/ContentUserAuditLog.java`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/req/ContentUserReqValidationTest.java`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentAccountServiceTest.java`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/controller/ContentAccountControllerWebMvcTest.java`
- `docs/superpowers/specs/2026-05-02-content-user-prd-backend-coverage-report.md`

## Task 1: 锁定请求模型与校验红灯

**Files:**
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/req/account/ContentEmailRegisterReq.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/req/account/ContentAccountBindMobileReq.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/req/account/ContentAccountBindEmailReq.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/req/account/ContentAccountUnbindMobileReq.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/req/account/ContentAccountUnbindEmailReq.java`
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/req/ContentUserReqValidationTest.java`

- [ ] **Step 1: 在请求校验测试里先写邮箱注册与绑定解绑的失败测试**

```java
@Test
void shouldRejectInvalidEmailRegisterRequest() {
    ContentEmailRegisterReq req = new ContentEmailRegisterReq()
        .setEmail("bad")
        .setPassword("123")
        .setNickname("");

    Set<String> fields = validate(req);

    assertTrue(fields.contains("email"));
    assertTrue(fields.contains("password"));
    assertTrue(fields.contains("nickname"));
}

@Test
void shouldRejectInvalidBindMobileRequest() {
    ContentAccountBindMobileReq req = new ContentAccountBindMobileReq()
        .setUserId("")
        .setMobile("123")
        .setSecondaryVerified(null);

    Set<String> fields = validate(req);

    assertTrue(fields.contains("userId"));
    assertTrue(fields.contains("mobile"));
}

@Test
void shouldRejectInvalidBindEmailRequest() {
    ContentAccountBindEmailReq req = new ContentAccountBindEmailReq()
        .setUserId("")
        .setEmail("bad");

    Set<String> fields = validate(req);

    assertTrue(fields.contains("userId"));
    assertTrue(fields.contains("email"));
}
```

- [ ] **Step 2: 为解绑请求补失败测试，确认必须传 `userId`**

```java
@Test
void shouldRejectInvalidUnbindRequests() {
    ContentAccountUnbindMobileReq unbindMobileReq = new ContentAccountUnbindMobileReq()
        .setUserId("");
    ContentAccountUnbindEmailReq unbindEmailReq = new ContentAccountUnbindEmailReq()
        .setUserId("");

    Set<String> unbindMobileFields = validate(unbindMobileReq);
    Set<String> unbindEmailFields = validate(unbindEmailReq);

    assertTrue(unbindMobileFields.contains("userId"));
    assertTrue(unbindEmailFields.contains("userId"));
}
```

- [ ] **Step 3: 运行请求校验测试，确认当前缺少请求模型**

Run:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH="$JAVA_HOME/bin:$PATH"
mvn -pl jeecg-boot/jeecg-boot-module/jeecg-module-content \
  -Dtest=ContentUserReqValidationTest test
```

Expected:

```text
Compilation failure
cannot find symbol: class ContentEmailRegisterReq
cannot find symbol: class ContentAccountBindMobileReq
```

- [ ] **Step 4: 新增邮箱注册请求对象**

```java
@Data
@Accessors(chain = true)
@Schema(description = "内容社区邮箱注册请求")
public class ContentEmailRegisterReq {

    @Size(max = 64, message = "用户名长度不能超过64位")
    @Schema(description = "用户名", requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true)
    private String username;

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过100位")
    @Schema(description = "邮箱", requiredMode = Schema.RequiredMode.REQUIRED, nullable = false)
    private String email;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 32, message = "密码长度需在6到32位之间")
    @Schema(description = "密码", requiredMode = Schema.RequiredMode.REQUIRED, nullable = false)
    private String password;

    @NotBlank(message = "昵称不能为空")
    @Size(min = 1, max = 20, message = "昵称长度不能超过20位")
    @Schema(description = "昵称", requiredMode = Schema.RequiredMode.REQUIRED, nullable = false)
    private String nickname;

    @Size(max = 32, message = "邀请码长度不能超过32位")
    @Schema(description = "邀请码", requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true)
    private String inviteCode;
}
```

- [ ] **Step 5: 新增绑定解绑请求对象，保持和密码重置相同的 `secondaryVerified` 语义**

```java
@Data
@Accessors(chain = true)
@Schema(description = "内容社区绑定手机号请求")
public class ContentAccountBindMobileReq {

    @NotBlank(message = "用户ID不能为空")
    @Size(max = 64, message = "用户ID长度不能超过64位")
    private String userId;

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1\\d{10}$", message = "手机号格式不正确")
    private String mobile;

    @Size(max = 64, message = "操作人ID长度不能超过64位")
    private String operatorUserId;

    @Schema(description = "是否已完成二次校验")
    private Boolean secondaryVerified;
}
```

```java
@Data
@Accessors(chain = true)
@Schema(description = "内容社区解绑邮箱请求")
public class ContentAccountUnbindEmailReq {

    @NotBlank(message = "用户ID不能为空")
    @Size(max = 64, message = "用户ID长度不能超过64位")
    private String userId;

    @Size(max = 64, message = "操作人ID长度不能超过64位")
    private String operatorUserId;

    @Schema(description = "是否已完成二次校验")
    private Boolean secondaryVerified;
}
```

- [ ] **Step 6: 重新运行请求校验测试，确认转绿**

Run:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH="$JAVA_HOME/bin:$PATH"
mvn -pl jeecg-boot/jeecg-boot-module/jeecg-module-content \
  -Dtest=ContentUserReqValidationTest test
```

Expected:

```text
BUILD SUCCESS
Tests run: ... , Failures: 0, Errors: 0
```

- [ ] **Step 7: 提交请求模型与校验基线**

```bash
git add \
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/req/account/ContentEmailRegisterReq.java \
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/req/account/ContentAccountBindMobileReq.java \
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/req/account/ContentAccountBindEmailReq.java \
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/req/account/ContentAccountUnbindMobileReq.java \
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/req/account/ContentAccountUnbindEmailReq.java \
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/req/ContentUserReqValidationTest.java
git commit -m "feat: add account bind-unbind request models"
```

## Task 2: 锁定服务与网关行为红灯

**Files:**
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentAccountServiceTest.java`
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/IContentAccountService.java`
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/gateway/SystemUserAccountGateway.java`
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/impl/ContentAccountServiceImpl.java`
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/gateway/impl/SystemUserAccountGatewayImpl.java`
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/entity/ContentUserAuditLog.java`

- [ ] **Step 1: 先写邮箱注册成功测试**

```java
@Test
void shouldCreateSysUserAndBootstrapCommunityProfileByEmail() {
    when(systemUserAccountGateway.createUserByEmail(any())).thenReturn("u_mail_1001");

    String userId = accountService.registerByEmail(new ContentEmailRegisterReq()
        .setEmail("user@example.com")
        .setPassword("Pass@123")
        .setNickname("邮箱用户"));

    assertThat(userId).isEqualTo("u_mail_1001");
    verify(profileMapper).insert(any(ContentUserProfile.class));
    verify(notificationSettingMapper).insert(any(ContentUserNotificationSetting.class));
}
```

- [ ] **Step 2: 写绑定解绑关键规则失败测试**

```java
@Test
void shouldRejectBindMobileWithoutSecondaryVerification() {
    ContentAccountBindMobileReq req = new ContentAccountBindMobileReq()
        .setUserId("u1")
        .setMobile("13800000002")
        .setSecondaryVerified(Boolean.FALSE);

    assertThatThrownBy(() -> accountService.bindMobile(req))
        .isInstanceOf(JeecgBootException.class)
        .hasMessage("绑定手机号需先完成二次校验");
}

@Test
void shouldRejectUnbindMobileWhenEmailNotBound() {
    SysUser user = new SysUser().setId("u1").setPhone("13800000001").setEmail(null);
    when(systemUserAccountGateway.getById("u1")).thenReturn(user);

    assertThatThrownBy(() -> accountService.unbindMobile(new ContentAccountUnbindMobileReq()
        .setUserId("u1")
        .setSecondaryVerified(Boolean.TRUE)))
        .isInstanceOf(JeecgBootException.class)
        .hasMessage("解绑手机号后至少保留一种找回方式");
}
```

- [ ] **Step 3: 写绑定成功与幂等成功测试**

```java
@Test
void shouldBindEmailAndWriteAuditLog() {
    SysUser user = new SysUser().setId("u1").setPhone("13800000001").setEmail(null);
    when(systemUserAccountGateway.getById("u1")).thenReturn(user);
    when(systemUserAccountGateway.bindEmail("u1", "bind@example.com"))
        .thenReturn(new SysUser().setId("u1").setPhone("13800000001").setEmail("bind@example.com"));

    accountService.bindEmail(new ContentAccountBindEmailReq()
        .setUserId("u1")
        .setEmail("bind@example.com")
        .setOperatorUserId("u1")
        .setSecondaryVerified(Boolean.TRUE));

    verify(systemUserAccountGateway).bindEmail("u1", "bind@example.com");
    verify(auditLogMapper).insert(any(ContentUserAuditLog.class));
}

@Test
void shouldTreatSameMobileAsIdempotentSuccess() {
    SysUser user = new SysUser().setId("u1").setPhone("13800000001").setEmail("a@example.com");
    when(systemUserAccountGateway.getById("u1")).thenReturn(user);

    accountService.bindMobile(new ContentAccountBindMobileReq()
        .setUserId("u1")
        .setMobile("13800000001")
        .setOperatorUserId("u1")
        .setSecondaryVerified(Boolean.TRUE));

    verify(systemUserAccountGateway, never()).bindMobile(any(), any());
}
```

- [ ] **Step 4: 运行服务测试，确认当前缺少接口与实现**

Run:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH="$JAVA_HOME/bin:$PATH"
mvn -pl jeecg-boot/jeecg-boot-module/jeecg-module-content \
  -Dtest=ContentAccountServiceTest test
```

Expected:

```text
Compilation failure
cannot find symbol: method registerByEmail(...)
cannot find symbol: method bindMobile(...)
cannot find symbol: variable auditLogMapper
```

- [ ] **Step 5: 扩展账号服务契约**

```java
public interface IContentAccountService {

    String registerByMobile(ContentRegisterReq req);

    String registerByEmail(ContentEmailRegisterReq req);

    void bindMobile(ContentAccountBindMobileReq req);

    void bindEmail(ContentAccountBindEmailReq req);

    void unbindMobile(ContentAccountUnbindMobileReq req);

    void unbindEmail(ContentAccountUnbindEmailReq req);

    void resetPassword(ContentPasswordResetReq req);
}
```

- [ ] **Step 6: 扩展网关契约**

```java
public interface SystemUserAccountGateway {

    String createUser(ContentRegisterReq req);

    String createUserByEmail(ContentEmailRegisterReq req);

    void resetPassword(ContentPasswordResetReq req);

    SysUser getById(String userId);

    SysUser bindMobile(String userId, String mobile);

    SysUser bindEmail(String userId, String email);

    SysUser unbindMobile(String userId);

    SysUser unbindEmail(String userId);

    void markCancelled(String userId);
}
```

- [ ] **Step 7: 在 `ContentUserAuditLog` 中补账户敏感操作工厂方法**

```java
public static ContentUserAuditLog accountMobileBound(String userId, String operatorUserId, String maskedMobile) {
    return new ContentUserAuditLog()
        .setUserId(userId)
        .setOperatorUserId(operatorUserId)
        .setEventType("USER_ACCOUNT_MOBILE_BOUND")
        .setEventContent("bind_mobile")
        .setExtraDataJson("{\"mobile\":\"" + maskedMobile + "\"}")
        .setEventTime(new Date());
}
```

```java
public static ContentUserAuditLog accountEmailUnbound(String userId, String operatorUserId) {
    return new ContentUserAuditLog()
        .setUserId(userId)
        .setOperatorUserId(operatorUserId)
        .setEventType("USER_ACCOUNT_EMAIL_UNBOUND")
        .setEventContent("unbind_email")
        .setEventTime(new Date());
}
```

- [ ] **Step 8: 在账号服务实现中补邮箱注册与绑定解绑规则**

```java
@Override
@Transactional(rollbackFor = Exception.class)
public String registerByEmail(ContentEmailRegisterReq req) {
    String userId = systemUserAccountGateway.createUserByEmail(req);
    bootstrapProfile(userId, req.getNickname());
    return userId;
}

@Override
@Transactional(rollbackFor = Exception.class)
public void bindMobile(ContentAccountBindMobileReq req) {
    requireSecondaryVerified(req.getSecondaryVerified(), "绑定手机号需先完成二次校验");
    SysUser user = requireSysUser(req.getUserId());
    if (req.getMobile().equals(user.getPhone())) {
        return;
    }
    SysUser updatedUser = systemUserAccountGateway.bindMobile(req.getUserId(), req.getMobile());
    auditLogMapper.insert(ContentUserAuditLog.accountMobileBound(
        req.getUserId(),
        req.getOperatorUserId(),
        maskMobile(updatedUser.getPhone())
    ));
}
```

```java
private void requireSecondaryVerified(Boolean secondaryVerified, String message) {
    if (!Boolean.TRUE.equals(secondaryVerified)) {
        throw new JeecgBootException(message);
    }
}

private void ensureUnbindAllowed(SysUser user, boolean unbindMobile) {
    boolean hasPhone = user != null && oConvertUtils.isNotEmpty(user.getPhone());
    boolean hasEmail = user != null && oConvertUtils.isNotEmpty(user.getEmail());
    if (unbindMobile && !hasEmail) {
        throw new JeecgBootException("解绑手机号后至少保留一种找回方式");
    }
    if (!unbindMobile && !hasPhone) {
        throw new JeecgBootException("解绑邮箱后至少保留一种找回方式");
    }
}
```

- [ ] **Step 9: 在网关实现中复用 `SysUserMapper` 完成真实更新**

```java
@Override
public String createUserByEmail(ContentEmailRegisterReq req) {
    String username = resolveUsernameForEmailRegister(req);
    SysUser user = buildSysUser(username, null, req.getEmail(), req.getPassword(), req.getNickname());
    sysUserMapper.insert(user);
    return user.getId();
}

@Override
public SysUser bindEmail(String userId, String email) {
    SysUser existing = sysUserMapper.getUserByEmail(email);
    if (existing != null && !userId.equals(existing.getId())) {
        throw new JeecgBootException("邮箱已绑定其他账号");
    }
    SysUser user = requireUser(userId);
    user.setEmail(email);
    user.setUpdateTime(new Date());
    sysUserMapper.updateById(user);
    return user;
}
```

```java
private SysUser requireUser(String userId) {
    SysUser user = sysUserMapper.selectById(userId);
    if (user == null) {
        throw new JeecgBootException("未找到对应平台账号");
    }
    return user;
}
```

- [ ] **Step 10: 运行服务测试，确认转绿**

Run:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH="$JAVA_HOME/bin:$PATH"
mvn -pl jeecg-boot/jeecg-boot-module/jeecg-module-content \
  -Dtest=ContentAccountServiceTest test
```

Expected:

```text
BUILD SUCCESS
Tests run: ... , Failures: 0, Errors: 0
```

- [ ] **Step 11: 提交服务与网关实现**

```bash
git add \
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/IContentAccountService.java \
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/impl/ContentAccountServiceImpl.java \
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/gateway/SystemUserAccountGateway.java \
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/gateway/impl/SystemUserAccountGatewayImpl.java \
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/entity/ContentUserAuditLog.java \
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentAccountServiceTest.java
git commit -m "feat: add account email register and bind flow"
```

## Task 3: 锁定控制器与接口红灯

**Files:**
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/controller/ContentAccountController.java`
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/controller/ContentAccountControllerWebMvcTest.java`

- [ ] **Step 1: 先写邮箱注册与绑定解绑接口的 WebMvc 测试**

```java
@Test
void registerByEmail_validRequest_returnsSuccess() throws Exception {
    when(contentAccountService.registerByEmail(any())).thenReturn("u_mail_1001");

    mockMvc.perform(post("/content/user/account/register/email")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "email":"user@example.com",
                  "password":"Pass@123",
                  "nickname":"邮箱用户"
                }
                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.result").value("u_mail_1001"));
}
```

```java
@Test
void bindMobile_secondaryVerifyFailed_returnsBusinessError() throws Exception {
    doThrow(new JeecgBootException("绑定手机号需先完成二次校验"))
        .when(contentAccountService)
        .bindMobile(any());

    mockMvc.perform(post("/content/user/account/bind/mobile")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "userId":"u1",
                  "mobile":"13800000002",
                  "secondaryVerified":false
                }
                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("绑定手机号需先完成二次校验"));
}
```

- [ ] **Step 2: 运行 WebMvc 测试，确认当前缺少接口映射**

Run:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH="$JAVA_HOME/bin:$PATH"
mvn -pl jeecg-boot/jeecg-boot-module/jeecg-module-content \
  -Dtest=ContentAccountControllerWebMvcTest test
```

Expected:

```text
java.lang.AssertionError: Status expected:<200> but was:<404>
```

- [ ] **Step 3: 在控制器中新增 5 个明确接口**

```java
@Operation(summary = "邮箱注册并初始化社区资料")
@PostMapping("/register/email")
public Result<String> registerByEmail(@Valid @RequestBody ContentEmailRegisterReq req) {
    return Result.OK(contentAccountService.registerByEmail(req));
}

@Operation(summary = "绑定手机号")
@PostMapping("/bind/mobile")
public Result<String> bindMobile(@Valid @RequestBody ContentAccountBindMobileReq req) {
    contentAccountService.bindMobile(req);
    return Result.OK("手机号绑定成功");
}
```

```java
@Operation(summary = "解绑邮箱")
@PostMapping("/unbind/email")
public Result<String> unbindEmail(@Valid @RequestBody ContentAccountUnbindEmailReq req) {
    contentAccountService.unbindEmail(req);
    return Result.OK("邮箱解绑成功");
}
```

- [ ] **Step 4: 补齐其余成功断言与 service 交互校验**

```java
verify(contentAccountService).bindEmail(any(ContentAccountBindEmailReq.class));
verify(contentAccountService).unbindMobile(any(ContentAccountUnbindMobileReq.class));
verify(contentAccountService).unbindEmail(any(ContentAccountUnbindEmailReq.class));
```

- [ ] **Step 5: 重新运行 WebMvc 测试，确认转绿**

Run:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH="$JAVA_HOME/bin:$PATH"
mvn -pl jeecg-boot/jeecg-boot-module/jeecg-module-content \
  -Dtest=ContentAccountControllerWebMvcTest test
```

Expected:

```text
BUILD SUCCESS
Tests run: ... , Failures: 0, Errors: 0
```

- [ ] **Step 6: 提交控制器接口层**

```bash
git add \
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/controller/ContentAccountController.java \
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/controller/ContentAccountControllerWebMvcTest.java
git commit -m "feat: add account email register endpoints"
```

## Task 4: 回归验证与覆盖报告同步

**Files:**
- Modify: `docs/superpowers/specs/2026-05-02-content-user-prd-backend-coverage-report.md`

- [ ] **Step 1: 运行账号相关测试集**

Run:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH="$JAVA_HOME/bin:$PATH"
mvn -pl jeecg-boot/jeecg-boot-module/jeecg-module-content \
  -Dtest=ContentUserReqValidationTest,ContentAccountServiceTest,ContentAccountControllerWebMvcTest test
```

Expected:

```text
BUILD SUCCESS
Tests run: ... , Failures: 0, Errors: 0
```

- [ ] **Step 2: 运行最近修改文件的 IDE 诊断检查**

Run:

```text
对新增 req、controller、service、gateway、test 文件执行诊断，确保无新增编译错误。
```

- [ ] **Step 3: 更新覆盖报告的增量实现记录与账号安全域矩阵**

```markdown
- `2026-05-05` 已新增账号安全域闭环：`register/email` 独立邮箱注册、手机号/邮箱绑定解绑、敏感操作二次校验与账号审计留痕。
```

```markdown
| 7 | 邮箱注册 | 已实现 | `controller/ContentAccountController.java`、`service/impl/ContentAccountServiceImpl.java`、`gateway/impl/SystemUserAccountGatewayImpl.java`、`req/account/ContentEmailRegisterReq.java` | `service/ContentAccountServiceTest.java`、`controller/ContentAccountControllerWebMvcTest.java` | 当前仅补邮箱注册，不含验证码注册编排 | 后续补邀请码与登录链路 | P0 |
```

- [ ] **Step 4: 再次运行账号相关测试，确认文档更新未影响代码**

Run:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH="$JAVA_HOME/bin:$PATH"
mvn -pl jeecg-boot/jeecg-boot-module/jeecg-module-content \
  -Dtest=ContentUserReqValidationTest,ContentAccountServiceTest,ContentAccountControllerWebMvcTest test
```

Expected:

```text
BUILD SUCCESS
```

- [ ] **Step 5: 提交最终实现**

```bash
git add \
  docs/superpowers/specs/2026-05-02-content-user-prd-backend-coverage-report.md \
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/controller/ContentAccountController.java \
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/IContentAccountService.java \
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/impl/ContentAccountServiceImpl.java \
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/gateway/SystemUserAccountGateway.java \
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/gateway/impl/SystemUserAccountGatewayImpl.java \
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/entity/ContentUserAuditLog.java \
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/req/account/ContentEmailRegisterReq.java \
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/req/account/ContentAccountBindMobileReq.java \
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/req/account/ContentAccountBindEmailReq.java \
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/req/account/ContentAccountUnbindMobileReq.java \
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/req/account/ContentAccountUnbindEmailReq.java \
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/req/ContentUserReqValidationTest.java \
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentAccountServiceTest.java \
  jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/controller/ContentAccountControllerWebMvcTest.java
git commit -m "feat: add account email register and bind unbind"
```

## 自检结论

- `spec` 覆盖：邮箱注册、绑定解绑、二次校验、解绑保护、审计、测试、覆盖报告同步均已映射到任务。
- `placeholder` 扫描：无 `TODO/TBD/implement later` 占位项。
- `type` 一致性：请求类型、service 方法名、gateway 方法名在各任务中保持一致。
