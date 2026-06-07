# Auth API Alignment Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 统一内容社区认证 API 路径为 `/api/v1/content/` 前缀，补充 14 个缺失端点，修复频道 Controller context-path 问题。

**Architecture:** 修改 3 个已有 Controller 的 @RequestMapping + 新增 14 个 Controller 方法 + 修复 5 个频道 Controller + 清理前端冗余 API 枚举。后端使用 BizService 模式（Controller → BizService → Mapper），新增端点复用已有服务。

**Tech Stack:** Spring Boot 3, MyBatis-Plus, Vue3, TypeScript, JUnit 5, Mockito

---

## File Structure

### 后端改动文件

| 文件 | 改动 |
|------|------|
| `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/auth/controller/ContentAuthController.java` | 修改 @RequestMapping + 新增 7 个方法 (N1-N7) |
| `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/auth/controller/ContentAccountCancellationController.java` | 修改 @RequestMapping + 修改 revoke→cancel + 新增 1 个方法 (N14) |
| `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/auth/controller/ContentRiskControlController.java` | 修改 @RequestMapping + 修改 3 个方法路径 + 新增 6 个方法 (N8-N13) |
| `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/auth/biz/ContentAuthBizService.java` | 新增 7 个接口方法 (sendSmsCode, sendEmailCode, refreshToken, logout, getCaptchaImage, verifyCaptcha, getLockStatus) |
| `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/auth/biz/ContentAuthBizServiceImpl.java` | 实现 7 个新方法 |
| `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/auth/biz/IContentRiskControlBizService.java` | 新增 6 个接口方法 (getAccountSecurityStatus, trustDevice, untrustDevice, changePassword, sendSecurityCode, denyAnomaly) |
| `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/auth/biz/IContentAccountCancellationBizService.java` | 新增 1 个接口方法 (checkEligibility) |
| `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/controller/ChannelMergeController.java` | 移除 `/jeecg-boot/` 前缀 |
| `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/controller/ChannelStatsController.java` | 移除 `/jeecg-boot/` 前缀 |
| `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/controller/ChannelLifecycleController.java` | 移除 `/jeecg-boot/` 前缀 |
| `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/controller/ChannelReviewController.java` | 移除 `/jeecg-boot/` 前缀 |
| `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/controller/ChannelExportController.java` | 移除 `/jeecg-boot/` 前缀 |

### 后端新增测试文件

| 文件 | 说明 |
|------|------|
| `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/auth/controller/ContentAuthControllerPathTest.java` | 路径对齐后的路由验证测试 |
| `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/auth/controller/ContentAuthControllerNewEndpointsTest.java` | 12 个新增端点的单元测试 |
| `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/auth/controller/ContentAccountCancellationControllerTest.java` | 注销控制器测试（含新增 eligibility） |
| `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/auth/controller/ContentRiskControlControllerNewEndpointTest.java` | denyAnomaly 端点测试 |

### 前端改动文件

| 文件 | 改动 |
|------|------|
| `jeecgboot-vue3/src/api/content/auth/index.ts` | 删除 3 个冗余枚举和函数 |
| `jeecgboot-vue3/src/api/content/auth/captcha.ts` | 确认路径对齐（已正确） |
| `jeecgboot-vue3/src/api/content/account/security.ts` | 确认路径对齐（已正确） |
| `jeecgboot-vue3/src/api/content/account/cancellation.ts` | 确认路径对齐（已正确） |

---

## Task 1: ContentAuthController 路径对齐

**Files:**
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/auth/controller/ContentAuthController.java`

- [ ] **Step 1: 修改 class-level @RequestMapping**

```java
// Before:
@RequestMapping("/content/auth")
// After:
@RequestMapping("/api/v1/content/auth")
```

- [ ] **Step 2: 修改 bind/unbind 方法路径中的 mobile→phone**

```java
// Before:
@PostMapping("/bind/mobile")
// After:
@PostMapping("/bind/phone")

// Before:
@PostMapping("/rebind/mobile")
// After:
@PostMapping("/rebind/phone")

// Before:
@PostMapping("/unbind/mobile")
// After:
@PostMapping("/unbind/phone")
```

- [ ] **Step 3: 修改 reset-password 路径**

```java
// Before:
@PostMapping("/reset-password")
// After:
@PostMapping("/password/reset")
```

- [ ] **Step 4: 验证编译通过**

Run: `mvn compile -pl jeecg-boot-module/jeecg-module-content -am -q`
Expected: BUILD SUCCESS

- [ ] **Step 5: Commit**

```bash
git add jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/auth/controller/ContentAuthController.java
git commit -m "refactor(auth): 对齐 ContentAuthController 路径到 /api/v1/content/auth"
```

---

## Task 2: ContentAccountCancellationController 路径对齐

**Files:**
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/auth/controller/ContentAccountCancellationController.java`

- [ ] **Step 1: 修改 class-level @RequestMapping**

```java
// Before:
@RequestMapping("/content/auth/cancellation")
// After:
@RequestMapping("/api/v1/content/account-cancellation")
```

- [ ] **Step 2: 修改 revoke 方法路径和方法名**

```java
// Before:
@PostMapping("/revoke")
public Result<?> revoke(...) {
// After:
@PostMapping("/cancel")
public Result<?> cancelCancellation(...) {
```

- [ ] **Step 3: 验证编译通过**

Run: `mvn compile -pl jeecg-boot-module/jeecg-module-content -am -q`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/auth/controller/ContentAccountCancellationController.java
git commit -m "refactor(auth): 对齐 ContentAccountCancellationController 路径到 /api/v1/content/account-cancellation"
```

---

## Task 3: ContentRiskControlController 路径对齐

**Files:**
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/auth/controller/ContentRiskControlController.java`

- [ ] **Step 1: 修改 class-level @RequestMapping**

```java
// Before:
@RequestMapping("/content/auth/risk")
// After:
@RequestMapping("/api/v1/content/account-security")
```

- [ ] **Step 2: 修改方法路径**

```java
// Before: @GetMapping("/notifications")  → After: @GetMapping("/anomaly/list")
// Before: @PostMapping("/confirm-login") → After: @PostMapping("/anomaly/confirm")
// Before: @PostMapping("/appeal")        → After: @PostMapping("/anomaly/appeal")
```

- [ ] **Step 3: 验证编译通过**

Run: `mvn compile -pl jeecg-boot-module/jeecg-module-content -am -q`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/auth/controller/ContentRiskControlController.java
git commit -m "refactor(auth): 对齐 ContentRiskControlController 路径到 /api/v1/content/account-security"
```

---

## Task 4: 频道 Controller context-path 修复

**Files:**
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/controller/ChannelMergeController.java`
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/controller/ChannelStatsController.java`
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/controller/ChannelLifecycleController.java`
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/controller/ChannelReviewController.java`
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/controller/ChannelExportController.java`

- [ ] **Step 1: 移除 5 个 Controller 的 `/jeecg-boot/` 前缀**

```java
// ChannelMergeController:    "/jeecg-boot/api/v1/content/channel/merge"    → "/api/v1/content/channel/merge"
// ChannelStatsController:    "/jeecg-boot/api/v1/content/channel/stats"    → "/api/v1/content/channel/stats"
// ChannelLifecycleController: "/jeecg-boot/api/v1/content/channel/lifecycle" → "/api/v1/content/channel/lifecycle"
// ChannelReviewController:   "/jeecg-boot/api/v1/content/channel/review"   → "/api/v1/content/channel/review"
// ChannelExportController:   "/jeecg-boot/api/v1/content/channel/export"   → "/api/v1/content/channel/export"
```

- [ ] **Step 2: 验证编译通过**

Run: `mvn compile -pl jeecg-boot-module/jeecg-module-content -am -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/channel/controller/
git commit -m "fix(channel): 移除频道 Controller 中多余的 /jeecg-boot/ context-path 前缀"
```

---

## Task 5: BizService 接口扩展（前置依赖）

> 新增端点需要 BizService 接口先定义方法签名。本 Task 仅添加接口方法声明和空实现，不包含业务逻辑。

**Files:**
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/auth/biz/ContentAuthBizService.java`
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/auth/biz/ContentAuthBizServiceImpl.java`
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/auth/biz/IContentRiskControlBizService.java`
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/auth/biz/IContentRiskControlBizServiceImpl.java` (或对应实现类)
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/auth/biz/IContentAccountCancellationBizService.java`

- [ ] **Step 1: ContentAuthBizService 接口新增 7 个方法**

```java
/** 发送手机验证码 */
void sendSmsCode(String phone, String countryCode, String captchaId, String captchaCode);

/** 发送邮箱验证码 */
void sendEmailCode(String email, String captchaId, String captchaCode);

/** 刷新 token */
AuthLoginResult refreshToken(String refreshToken);

/** 登出，清除会话 */
void logout(String userId);

/** 获取验证码图片 */
Map<String, String> getCaptchaImage();

/** 校验验证码 */
boolean verifyCaptcha(String captchaId, String captchaCode);

/** 查询锁定状态 */
Map<String, Object> getLockStatus(String account);
```

- [ ] **Step 2: ContentAuthBizServiceImpl 添加空实现（stub）**

```java
@Override
public void sendSmsCode(String phone, String countryCode, String captchaId, String captchaCode) {
    // TODO: 实现验证码发送逻辑
    throw new JeecgBootException("功能待实现");
}
// ... 其余 6 个方法类似
```

- [ ] **Step 3: IContentRiskControlBizService 接口新增 6 个方法**

```java
/** 获取账户安全状态聚合 */
Map<String, Object> getAccountSecurityStatus(String userId);

/** 信任设备 */
void trustDevice(String userId, String deviceId);

/** 取消信任设备 */
void untrustDevice(String userId, String deviceId);

/** 修改密码 */
void changePassword(String userId, String oldPassword, String newPassword);

/** 发送安全操作验证码 */
void sendSecurityCode(String type, String target, String purpose);

/** 否认异常登录 */
void denyAnomaly(String notificationId, String revokeDeviceId);
```

- [ ] **Step 4: IContentRiskControlBizServiceImpl 添加空实现**

- [ ] **Step 5: IContentAccountCancellationBizService 接口新增 1 个方法**

```java
/** 检查注销资格 */
Map<String, Object> checkEligibility(String userId);
```

- [ ] **Step 6: 验证编译通过**

Run: `mvn compile -pl jeecg-boot-module/jeecg-module-content -am -q`
Expected: BUILD SUCCESS

- [ ] **Step 7: Commit**

```bash
git add jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/auth/biz/
git commit -m "feat(auth): 扩展 BizService 接口，新增 14 个方法签名（stub 实现）"
```

---

## Task 6: ContentAuthController 新增端点 — 验证码发送 (N1, N2) + BizService 实现

**Files:**
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/auth/biz/ContentAuthBizServiceImpl.java`
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/auth/controller/ContentAuthController.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/auth/controller/ContentAuthControllerNewEndpointsTest.java`

- [ ] **Step 1: 实现 ContentAuthBizServiceImpl.sendSmsCode**

替换 Task 5 中的 stub，实现：校验图形验证码(可选) → 生成 6 位验证码 → 存 Redis(5min) → 调用短信服务 → 限频 60s

- [ ] **Step 2: 实现 ContentAuthBizServiceImpl.sendEmailCode**

同上，调用邮件服务

- [ ] **Step 3: Write failing test for sendSmsCode**

```java
@Test
@DisplayName("sendSmsCode - 委托给 bizService 并返回成功")
void sendSmsCode_delegatesToBizService() {
    Map<String, String> params = new HashMap<>();
    params.put("phone", "13800138000");
    params.put("countryCode", "+86");
    Result<?> result = controller.sendSmsCode(params);
    assertThat(result.getCode()).isEqualTo(200);
    verify(bizService).sendSmsCode("13800138000", "+86", null, null);
}
```

- [ ] **Step 4: Implement sendSmsCode in ContentAuthController**

```java
@PostMapping("/sms/send")
public Result<?> sendSmsCode(@RequestBody Map<String, String> params) {
    String phone = params.get("phone");
    String countryCode = params.getOrDefault("countryCode", "+86");
    String captchaId = params.get("captchaId");
    String captchaCode = params.get("captchaCode");
    bizService.sendSmsCode(phone, countryCode, captchaId, captchaCode);
    return Result.ok("验证码已发送");
}
```

- [ ] **Step 5: Write failing test for sendEmailCode + Implement**

```java
// Controller:
@PostMapping("/email/send")
public Result<?> sendEmailCode(@RequestBody Map<String, String> params) {
    String email = params.get("email");
    String captchaId = params.get("captchaId");
    String captchaCode = params.get("captchaCode");
    bizService.sendEmailCode(email, captchaId, captchaCode);
    return Result.ok("验证码已发送");
}
```

- [ ] **Step 6: Run tests**

Run: `mvn test -pl jeecg-boot-module/jeecg-module-content -Dtest=ContentAuthControllerNewEndpointsTest -am`
Expected: PASS

- [ ] **Step 7: Commit**

```bash
git add jeecg-boot/jeecg-boot-module/jeecg-module-content/
git commit -m "feat(auth): 新增 sendSmsCode 和 sendEmailCode 端点 + BizService 实现 (N1, N2)"
```

---

## Task 7: ContentAuthController 新增端点 — Token 刷新与登出 (N3, N4) + BizService 实现

**Files:**
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/auth/biz/ContentAuthBizServiceImpl.java`
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/auth/controller/ContentAuthController.java`
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/auth/controller/ContentAuthControllerNewEndpointsTest.java`

- [ ] **Step 1: 实现 ContentAuthBizServiceImpl.refreshToken**

替换 stub：校验 refreshToken → 签发新 token + 新 refreshToken

- [ ] **Step 2: 实现 ContentAuthBizServiceImpl.logout**

替换 stub：清除 Redis 中的 token/会话

- [ ] **Step 3: Write failing test + Implement refreshToken Controller**

```java
// Test:
@Test
@DisplayName("refreshToken - 有效 refreshToken 返回新 token")
void refreshToken_validToken() {
    Map<String, String> params = new HashMap<>();
    params.put("refreshToken", "valid-refresh-token");
    when(bizService.refreshToken("valid-refresh-token")).thenReturn(new AuthLoginResult().setToken("new-token").setRefreshToken("new-refresh"));
    Result<?> result = controller.refreshToken(params);
    assertThat(result.getCode()).isEqualTo(200);
}

// Controller:
@PostMapping("/token/refresh")
public Result<?> refreshToken(@RequestBody Map<String, String> params) {
    String refreshToken = params.get("refreshToken");
    AuthLoginResult result = bizService.refreshToken(refreshToken);
    return Result.ok(result);
}
```

- [ ] **Step 4: Write failing test + Implement logout Controller**

```java
// Test:
@Test
@DisplayName("logout - 清除会话并返回成功")
void logout_clearsSession() {
    Result<?> result = controller.logout();
    assertThat(result.getCode()).isEqualTo(200);
    verify(bizService).logout(anyString());
}

// Controller:
@PostMapping("/logout")
public Result<?> logout() {
    String userId = SecureUtil.currentUser().getId();
    bizService.logout(userId);
    return Result.ok("已登出");
}
```

- [ ] **Step 5: Run tests**

Run: `mvn test -pl jeecg-boot-module/jeecg-module-content -Dtest=ContentAuthControllerNewEndpointsTest -am`
Expected: PASS

- [ ] **Step 6: Commit**

```bash
git add jeecg-boot/jeecg-boot-module/jeecg-module-content/
git commit -m "feat(auth): 新增 refreshToken 和 logout 端点 + BizService 实现 (N3, N4)"
```

---

## Task 8: ContentAuthController 新增端点 — Captcha 代理 (N5, N6, N7) + BizService 实现

**Files:**
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/auth/biz/ContentAuthBizServiceImpl.java`
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/auth/controller/ContentAuthController.java`
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/auth/controller/ContentAuthControllerNewEndpointsTest.java`

- [ ] **Step 1: 实现 ContentAuthBizServiceImpl 中 3 个 captcha 方法**

- `getCaptchaImage()`: 调用 `CaptchaVerifyPort` 获取验证码图片
- `verifyCaptcha(captchaId, captchaCode)`: 调用 `CaptchaVerifyPort` 校验
- `getLockStatus(account)`: 查询 Redis 中的登录失败计数

- [ ] **Step 2: Write failing test for getCaptchaImage**

```java
@Test
@DisplayName("getCaptchaImage - 返回验证码图片 base64")
void getCaptchaImage_returnsBase64() {
    when(bizService.getCaptchaImage()).thenReturn(Map.of("captchaId", "id-123", "imageBase64", "base64data"));
    Result<?> result = controller.getCaptchaImage();
    assertThat(result.getCode()).isEqualTo(200);
    verify(bizService).getCaptchaImage();
}
```

- [ ] **Step 2: Implement getCaptchaImage**

```java
@PostMapping("/captcha/image")
public Result<?> getCaptchaImage() {
    Map<String, String> captcha = bizService.getCaptchaImage();
    return Result.ok(captcha);
}
```

- [ ] **Step 3: Write failing test for verifyCaptcha**

```java
@Test
@DisplayName("verifyCaptcha - 校验成功返回通过")
void verifyCaptcha_success() {
    Map<String, String> params = new HashMap<>();
    params.put("captchaId", "id-123");
    params.put("captchaCode", "abcd");
    when(bizService.verifyCaptcha("id-123", "abcd")).thenReturn(true);
    Result<?> result = controller.verifyCaptcha(params);
    assertThat(result.getCode()).isEqualTo(200);
}
```

- [ ] **Step 4: Implement verifyCaptcha**

```java
@PostMapping("/captcha/verify")
public Result<?> verifyCaptcha(@RequestBody Map<String, String> params) {
    String captchaId = params.get("captchaId");
    String captchaCode = params.get("captchaCode");
    boolean passed = bizService.verifyCaptcha(captchaId, captchaCode);
    return passed ? Result.ok("验证通过") : Result.error("验证码错误");
}
```

- [ ] **Step 5: Write failing test for getLockStatus**

```java
@Test
@DisplayName("getLockStatus - 返回账户锁定状态")
void getLockStatus_returnsStatus() {
    when(bizService.getLockStatus("test@example.com")).thenReturn(Map.of("locked", false, "attempts", 0));
    Result<?> result = controller.getLockStatus("test@example.com");
    assertThat(result.getCode()).isEqualTo(200);
}
```

- [ ] **Step 6: Implement getLockStatus**

```java
@GetMapping("/captcha/lock-status")
public Result<?> getLockStatus(@RequestParam String account) {
    Map<String, Object> status = bizService.getLockStatus(account);
    return Result.ok(status);
}
```

- [ ] **Step 7: Run tests**

Run: `mvn test -pl jeecg-boot-module/jeecg-module-content -Dtest=ContentAuthControllerNewEndpointsTest -am`
Expected: PASS

- [ ] **Step 8: Commit**

```bash
git add jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/auth/controller/ContentAuthController.java
git add jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/auth/controller/ContentAuthControllerNewEndpointsTest.java
git commit -m "feat(auth): 新增 captcha 图片/校验/锁定状态端点 (N5, N6, N7)"
```

---

## Task 9: ContentRiskControlController 新增端点 — 账户安全 (N8-N12) + BizService 实现

**Files:**
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/auth/biz/IContentRiskControlBizServiceImpl.java` (或对应实现类)
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/auth/controller/ContentRiskControlController.java`
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/auth/controller/ContentRiskControlControllerNewEndpointTest.java`

> **设计决策**: N8-N12 路径在 `/api/v1/content/account-security/` 下，与 ContentRiskControlController 的 class prefix `/api/v1/content/account-security` 一致，因此 N8-N12 放入 ContentRiskControlController。

- [ ] **Step 1: 实现 IContentRiskControlBizServiceImpl 中 5 个方法**

- `getAccountSecurityStatus(userId)`: 聚合查询手机/邮箱/第三方绑定状态
- `trustDevice(userId, deviceId)`: 标记设备为可信
- `untrustDevice(userId, deviceId)`: 取消设备可信标记
- `changePassword(userId, oldPassword, newPassword)`: 验证旧密码 → 更新密码哈希
- `sendSecurityCode(type, target, purpose)`: 根据 type 调用短信或邮件服务

- [ ] **Step 2: Write failing tests + Implement Controller**

```java
// 5 个 Controller 方法 + 对应测试
@GetMapping("/status")
@PostMapping("/devices/trust")
@PostMapping("/devices/untrust")
@PostMapping("/password/change")
@PostMapping("/send-code")
```

- [ ] **Step 3: Run tests**

Run: `mvn test -pl jeecg-boot-module/jeecg-module-content -Dtest=ContentRiskControlControllerNewEndpointTest -am`
Expected: PASS

- [ ] **Step 4: Commit**

```bash
git add jeecg-boot/jeecg-boot-module/jeecg-module-content/
git commit -m "feat(auth): 新增账户安全端点 N8-N12 + BizService 实现"
```

---

## Task 10: ContentRiskControlController 新增端点 — denyAnomaly (N13) + BizService 实现

**Files:**
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/auth/biz/IContentRiskControlBizServiceImpl.java`
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/auth/controller/ContentRiskControlController.java`
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/auth/controller/ContentRiskControlControllerNewEndpointTest.java`

- [ ] **Step 1: 实现 IContentRiskControlBizServiceImpl.denyAnomaly**

确认异常 → 踢出设备 → 写审计日志

- [ ] **Step 2: Write failing test + Implement Controller**

```java
// Test:
@Test
@DisplayName("denyAnomaly - 确认异常并踢出设备")
void denyAnomaly_confirmsAndKicksDevice() {
    Map<String, String> params = new HashMap<>();
    params.put("id", "notification-123");
    params.put("revokeDeviceId", "device-456");
    Result<?> result = controller.denyAnomaly(params);
    assertThat(result.getCode()).isEqualTo(200);
}

// Controller:
@PostMapping("/anomaly/deny")
public Result<?> denyAnomaly(@RequestBody Map<String, String> params) {
    String id = params.get("id");
    String revokeDeviceId = params.get("revokeDeviceId");
    riskControlBizService.denyAnomaly(id, revokeDeviceId);
    return Result.ok("已否认异常登录");
}
```

- [ ] **Step 3: Run tests**

Run: `mvn test -pl jeecg-boot-module/jeecg-module-content -Dtest=ContentRiskControlControllerNewEndpointTest -am`
Expected: PASS

- [ ] **Step 4: Commit**

```bash
git add jeecg-boot/jeecg-boot-module/jeecg-module-content/
git commit -m "feat(auth): 新增 denyAnomaly 端点 + BizService 实现 (N13)"
```

---

## Task 11: ContentAccountCancellationController 新增端点 — eligibility (N14) + BizService 实现

**Files:**
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/auth/biz/IContentAccountCancellationBizServiceImpl.java` (或对应实现类)
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/auth/controller/ContentAccountCancellationController.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/auth/controller/ContentAccountCancellationControllerTest.java`

- [ ] **Step 1: 实现 IContentAccountCancellationBizServiceImpl.checkEligibility**

检查积分余额、待处理订单、风控状态

- [ ] **Step 2: Write failing test + Implement Controller**

```java
// Test:
@Test
@DisplayName("checkEligibility - 返回资格检查结果")
void checkEligibility_returnsChecks() {
    when(cancellationBizService.checkEligibility(anyString())).thenReturn(Map.of("eligible", true, "checks", List.of()));
    Result<?> result = controller.checkEligibility();
    assertThat(result.getCode()).isEqualTo(200);
}

// Controller:
@GetMapping("/eligibility")
public Result<?> checkEligibility() {
    String userId = SecureUtil.currentUser().getId();
    Map<String, Object> eligibility = cancellationBizService.checkEligibility(userId);
    return Result.ok(eligibility);
}
```

- [ ] **Step 3: Run tests**

Run: `mvn test -pl jeecg-boot-module/jeecg-module-content -Dtest=ContentAccountCancellationControllerTest -am`
Expected: PASS

- [ ] **Step 4: Commit**

```bash
git add jeecg-boot/jeecg-boot-module/jeecg-module-content/
git commit -m "feat(auth): 新增 checkEligibility 端点 + BizService 实现 (N14)"
```

---

## Task 12: 前端路径清理

**Files:**
- Modify: `jeecgboot-vue3/src/api/content/auth/index.ts`

- [ ] **Step 1: 删除 `Api.smsCode` 枚举项和 `smsCode` 函数**

删除枚举：
```typescript
smsCode = '/api/v1/content/auth/sms-code',
```

删除函数：
```typescript
export const smsCode = (params: SmsCodeParams) => {
  return defHttp.post({ url: Api.smsCode, params });
};
```

- [ ] **Step 2: 删除 `Api.emailCode` 枚举项和 `emailCode` 函数**

删除枚举：
```typescript
emailCode = '/api/v1/content/auth/email-code',
```

删除函数：
```typescript
export const emailCode = (params: EmailCodeParams) => {
  return defHttp.post({ url: Api.emailCode, params });
};
```

- [ ] **Step 3: 删除 `Api.resendEmail` 枚举项和 `resendConfirmEmail` 函数**

删除枚举：
```typescript
resendEmail = '/api/v1/content/auth/email/resend',
```

删除函数：
```typescript
export const resendConfirmEmail = (params: ResendEmailParams) => {
  return defHttp.post({ url: Api.resendEmail, params });
};
```

- [ ] **Step 4: 确认剩余路径与后端对齐**

检查 `Api.sendSms` = `/api/v1/content/auth/sms/send` 与后端 N1 对齐
检查 `Api.sendEmail` = `/api/v1/content/auth/email/send` 与后端 N2 对齐

- [ ] **Step 5: Commit**

```bash
git add jeecgboot-vue3/src/api/content/auth/index.ts
git commit -m "refactor(frontend): 删除 auth API 中 3 个冗余枚举和函数"
```

---

## Task 13: 前端路径验证

**Files:**
- Read: `jeecgboot-vue3/src/api/content/auth/captcha.ts`
- Read: `jeecgboot-vue3/src/api/content/account/security.ts`
- Read: `jeecgboot-vue3/src/api/content/account/cancellation.ts`

- [ ] **Step 1: 确认 captcha.ts 路径正确**

检查枚举值：
- `getCaptchaImage` = `/api/v1/content/auth/captcha/image` ✓
- `verifyCaptcha` = `/api/v1/content/auth/captcha/verify` ✓
- `getLockStatus` = `/api/v1/content/auth/captcha/lock-status` ✓

- [ ] **Step 2: 确认 security.ts 路径正确**

检查关键枚举值：
- `bindPhone` = `/api/v1/content/account-security/bind/phone` ✓
- `anomalyDeny` = `/api/v1/content/account-security/anomaly/deny` ✓
- `accountSecurityStatus` = `/api/v1/content/account-security/status` ✓

- [ ] **Step 3: 确认 cancellation.ts 路径正确**

检查枚举值：
- `eligibility` = `/api/v1/content/account-cancellation/eligibility` ✓
- `apply` = `/api/v1/content/account-cancellation/apply` ✓
- `cancel` = `/api/v1/content/account-cancellation/cancel` ✓

- [ ] **Step 4: 无需改动，记录验证结果**

---

## Task 14: 模块全量测试

- [ ] **Step 1: 运行内容社区模块全量测试**

Run: `mvn test -pl jeecg-boot-module/jeecg-module-content -am`
Expected: 100% tests pass, BUILD SUCCESS

- [ ] **Step 2: 修复任何失败的测试**

---

## Task 15: 集成验证

- [ ] **Step 1: 启动后端，验证 Phase 1 路由变更**

```bash
# 验证 ContentAuthController 路由
curl -s http://localhost:8080/jeecg-boot/api/v1/content/auth/login/password -X POST -H "Content-Type: application/json" -d '{}' | head -c 200
# 预期: 非 404（应为 400 参数错误）

# 验证 ContentRiskControlController 路由
curl -s http://localhost:8080/jeecg-boot/api/v1/content/account-security/anomaly/list | head -c 200
# 预期: 非 404

# 验证 ContentAccountCancellationController 路由
curl -s http://localhost:8080/jeecg-boot/api/v1/content/account-cancellation/status | head -c 200
# 预期: 非 404

# 验证频道 Controller 路由
curl -s http://localhost:8080/jeecg-boot/api/v1/content/channel/merge | head -c 200
# 预期: 非 404

# 验证系统模块登录不受影响
curl -s http://localhost:8080/jeecg-boot/sys/login -X POST -H "Content-Type: application/json" -d '{}' | head -c 200
# 预期: 非 404
```

- [ ] **Step 2: 验证前端登录流程可正常调用**

启动前端 dev server，测试密码登录和短信登录流程。

---

## 流程确认

- [ ] 流程确认 — subagent + TDD
- [ ] Code Review
- [ ] 覆盖率 ≥ 90%
- [ ] 模块全量测试 100%
- [ ] 合并 + 验证 + 清理 worktree
