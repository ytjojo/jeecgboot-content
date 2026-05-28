# 前端登录接口与后端 Java 类映射分析

## 概述

前端项目 `jeecgboot-vue3` 使用的认证相关接口，对应的后端 Controller、Service、Entity 完整映射。

---

## 1. 核心登录接口（POST /sys/login）

### 前端调用链

```
LoginForm.vue → store/modules/user.ts (loginApi) → POST /sys/login
```

### 后端 Java 类

| 层级 | 类名 | 文件路径 | 关键方法 |
|------|------|----------|----------|
| **Controller** | `LoginController` | `jeecg-module-system/jeecg-system-biz/src/main/java/org/jeecg/modules/system/controller/LoginController.java` | `login()` (line 106) |
| **Service** | `ISysUserService` | `jeecg-module-system/jeecg-system-biz/src/main/java/org/jeecg/modules/system/service/ISysUserService.java` | `checkUserIsEffective()`, `getUserByName()`, `setLoginTenant()` |
| **Service Impl** | `SysUserServiceImpl` | `jeecg-module-system/jeecg-system-biz/src/main/java/org/jeecg/modules/system/service/impl/SysUserServiceImpl.java` | 上述方法的具体实现 |
| **Entity** | `SysLoginModel` | `jeecg-module-system/jeecg-system-biz/src/main/java/org/jeecg/modules/system/model/SysLoginModel.java` | 登录请求体：username, password, captcha, checkKey, loginOrgCode |
| **Entity** | `SysUser` | `jeecg-module-system/jeecg-system-biz/src/main/java/org/jeecg/modules/system/entity/SysUser.java` | 用户实体 |
| **工具类** | `JwtUtil` | `jeecg-boot-base-core/src/main/java/org/jeecg/common/system/util/JwtUtil.java` | `sign()` 生成 token |
| **工具类** | `PasswordUtil` | `jeecg-boot-base-core/src/main/java/org/jeecg/common/util/PasswordUtil.java` | `encrypt()` 密码加密比对 |
| **工具类** | `AesEncryptUtil` | `jeecg-boot-base-core/src/main/java/org/jeecg/common/util/encryption/AesEncryptUtil.java` | `resolvePassword()` AES 密码解密 |

### 登录流程（7步）

1. **AES 解密密码** — `AesEncryptUtil.resolvePassword()`
2. **登录失败次数检查** — Redis 记录，超过5次锁定10分钟
3. **验证码校验** — `validateCaptcha()` 从 Redis 比对
4. **用户有效性校验** — `sysUserService.checkUserIsEffective()`
5. **密码比对** — `PasswordUtil.encrypt()` + 字符串比较
6. **生成 Token** — `JwtUtil.sign()` + 存入 Redis
7. **记录登录日志** — `baseCommonService.addLog()`

---

## 2. 手机号登录接口（POST /sys/phoneLogin）

### 后端 Java 类

| 层级 | 类名 | 关键方法 | 行号 |
|------|------|----------|------|
| **Controller** | `LoginController` | `phoneLogin()` | line 442 |
| **Service** | `ISysUserService` | `getUserByPhone()`, `checkUserIsEffective()` | — |

### 流程
1. 登录失败次数检查
2. `sysUserService.getUserByPhone(phone)` 查询用户
3. 从 Redis 比对手机验证码
4. 调用 `userInfo()` 生成 token 并返回用户信息

---

## 3. 退出登录接口（GET /sys/logout）

### 后端 Java 类

| 层级 | 类名 | 关键方法 | 行号 |
|------|------|----------|------|
| **Controller** | `LoginController` | `logout()` | line 203 |
| **Service** | `SysBaseApiImpl` | `getUserByName()` | — |
| **依赖** | `OAuth2AuthorizationService` | `findByToken()`, `remove()` | Spring Authorization Server |

### 流程
1. 从 Header 取 token → `JwtUtil.getUsername()` 获取用户名
2. 异步清理 Redis 缓存（token、shiro 权限、用户信息、单点登录缓存）
3. 清除 Spring Authorization Server 的 OAuth2 授权信息

---

## 4. 获取用户信息接口（GET /sys/user/getUserInfo）

### 后端 Java 类

| 层级 | 类名 | 关键方法 | 行号 |
|------|------|----------|------|
| **Controller** | `LoginController` | `getUserInfo()` | line 162 |
| **Service** | `ISysUserService` | `getUserByName()`, `getDynamicIndexByUserRole()` |
| **Service** | `ISysDictService` | `queryAllDictItems()` |

### 返回内容
- `userInfo` — SysUser 对象
- `sysAllDictItems` — 所有字典数据（vue3 不加载，vue2 加载）

---

## 5. 验证码接口（GET /sys/randomImage/{key}）

### 后端 Java 类

| 层级 | 类名 | 关键方法 | 行号 |
|------|------|----------|------|
| **Controller** | `LoginController` | `randomImage()` | line 632 |
| **工具类** | `RandImageUtil` | `generate()` | 生成图片 base64 |
| **工具类** | `Md5Util` | `md5Encode()` | Redis key 加密 |

---

## 6. 短信验证码接口（POST /sys/sms）

### 后端 Java 类

| 层级 | 类名 | 关键方法 | 行号 |
|------|------|----------|------|
| **Controller** | `LoginController` | `sms()` | line 332 |
| **Service** | `ISysUserService` | `getUserByPhone()`, `checkUserIsEffective()` |
| **工具类** | `DySmsHelper` | `sendSms()` | 短信发送 |
| **限流** | `DySmsLimit` | `canSendSms()` | IP 级别防刷 |

---

## 7. 权限编码接口（GET /sys/permission/getPermCode）

### 后端 Java 类

| 层级 | 类名 | 文件路径 |
|------|------|----------|
| **Controller** | `SysPermissionController` | `jeecg-module-system/jeecg-system-biz/src/main/java/org/jeecg/modules/system/controller/SysPermissionController.java` |
| **Service** | `ISysPermissionService` | `jeecg-module-system/jeecg-system-biz/src/main/java/org/jeecg/modules/system/service/ISysPermissionService.java` |

---

## 8. 第三方登录接口（/sys/thirdLogin/**）

### 后端 Java 类

| 层级 | 类名 | 文件路径 |
|------|------|----------|
| **Controller** | `ThirdLoginController` | `jeecg-module-system/jeecg-system-biz/src/main/java/org/jeecg/modules/system/controller/ThirdLoginController.java` |
| **Service** | `ISysThirdAccountService` | `jeecg-module-system/jeecg-system-biz/src/main/java/org/jeecg/modules/system/service/ISysThirdAccountService.java` |
| **Service** | `ThirdAppDingtalkServiceImpl` | 钉钉 OAuth2 |
| **Service** | `ThirdAppWechatEnterpriseServiceImpl` | 企业微信 OAuth2 |
| **Service** | `ISysThirdAppConfigService` | 第三方应用配置 |
| **依赖** | `AuthRequestFactory` (JustAuth) | 统一第三方登录 |

### 接口清单

| 接口路径 | 方法 | 行号 | 说明 |
|----------|------|------|------|
| `/getLoginUser/{token}/{thirdType}/{tenantId}` | GET | line 225 | 第三方登录获取用户信息 |
| `/oauth2/{source}/login` | GET | line 318 | OAuth2 授权跳转（企业微信/钉钉） |
| `/oauth2/{source}/callback` | GET | line 394 | OAuth2 回调 |
| `/oauth2/dingding/login` | GET | line 564 | 新版钉钉登录 |
| `/get/corpId/clientId` | GET | line 605 | 获取钉钉企业 ID |
| `/bindingThirdPhone` | POST | line 279 | 绑定手机号返回 token |
| `/user/create` | POST | line 144 | 创建第三方账号 |
| `/user/checkPassword` | POST | line 174 | 验证密码绑定账号 |

---

## 9. CAS 单点登录接口（GET /sys/cas/client/validateLogin）

### 后端 Java 类

| 层级 | 类名 | 文件路径 |
|------|------|----------|
| **Controller** | `CasClientController` | `jeecg-module-system/jeecg-system-biz/src/main/java/org/jeecg/modules/cas/controller/CasClientController.java` |
| **工具类** | `CasServiceUtil` | CAS 服务端验证工具 |

---

## 10. 扫码登录接口

### 后端 Java 类

| 接口路径 | Controller 方法 | 行号 | 说明 |
|----------|-----------------|------|------|
| `GET /sys/getLoginQrcode` | `LoginController.getLoginQrcode()` | line 775 | 生成登录二维码 |
| `POST /sys/scanLoginQrcode` | `LoginController.scanLoginQrcode()` | line 789 | APP 扫码确认 |
| `GET /sys/getQrcodeToken` | `LoginController.getQrcodeToken()` | line 806 | 轮询获取扫码后的 token |

---

## 11. 安全配置

### 后端 Java 类

| 类名 | 文件路径 | 说明 |
|------|----------|------|
| `SecurityConfig` | `jeecg-boot-base-core/src/main/java/org/jeecg/config/security/SecurityConfig.java` | Spring Security 配置 |
| `JeecgPermissionService` | `jeecg-boot-base-core/src/main/java/org/jeecg/config/security/JeecgPermissionService.java` | 权限校验服务 |
| `SecureUtil` | `jeecg-boot-base-core/src/main/java/org/jeecg/config/security/utils/SecureUtil.java` | 获取当前登录用户 |
| `IgnoreAuth` | `jeecg-boot-base-core/src/main/java/org/jeecg/config/shiro/IgnoreAuth.java` | 免认证注解 |

---

## 12. 核心依赖 Service 汇总

| Service 接口 | 文件路径 | 登录中的作用 |
|-------------|----------|-------------|
| `ISysUserService` | `jeecg-module-system/jeecg-system-biz/.../service/ISysUserService.java` | 用户查询、校验、租户设置 |
| `ISysPermissionService` | `jeecg-module-system/jeecg-system-biz/.../service/ISysPermissionService.java` | 菜单权限 |
| `ISysDepartService` | `jeecg-module-system/jeecg-system-biz/.../service/ISysDepartService.java` | 部门查询 |
| `ISysDictService` | `jeecg-module-system/jeecg-system-biz/.../service/ISysDictService.java` | 字典数据 |
| `ISysLogService` | `jeecg-module-system/jeecg-system-biz/.../service/ISysLogService.java` | 登录日志 |
| `BaseCommonService` | `jeecg-boot-base-core/.../service/BaseCommonService.java` | 通用日志记录 |
| `ISysThirdAccountService` | `jeecg-module-system/jeecg-system-biz/.../service/ISysThirdAccountService.java` | 第三方账号管理 |

---

## 完整 API 路径 → Java 类速查表

| 前端 API 路径 | HTTP 方法 | Controller | 方法 |
|--------------|-----------|------------|------|
| `/sys/login` | POST | `LoginController` | `login()` |
| `/sys/phoneLogin` | POST | `LoginController` | `phoneLogin()` |
| `/sys/logout` | GET | `LoginController` | `logout()` |
| `/sys/user/getUserInfo` | GET | `LoginController` | `getUserInfo()` |
| `/sys/permission/getPermCode` | GET | `SysPermissionController` | `getPermCode()` |
| `/sys/randomImage/{key}` | GET | `LoginController` | `randomImage()` |
| `/sys/sms` | POST | `LoginController` | `sms()` |
| `/sys/user/register` | POST | `SysUserController` | 用户注册 |
| `/sys/user/checkOnlyUser` | GET | `SysUserController` | 校验用户名唯一 |
| `/sys/user/phoneVerification` | POST | `SysUserController` | 手机验证 |
| `/sys/user/passwordChange` | GET | `SysUserController` | 修改密码 |
| `/sys/thirdLogin/getLoginUser/{token}/{thirdType}/{tenantId}` | GET | `ThirdLoginController` | `getThirdLoginUser()` |
| `/sys/thirdSms` | POST | `ThirdLoginController` | 第三方短信 |
| `/sys/getLoginQrcode` | GET | `LoginController` | `getLoginQrcode()` |
| `/sys/getQrcodeToken` | GET | `LoginController` | `getQrcodeToken()` |
| `/sys/cas/client/validateLogin` | GET | `CasClientController` | `validateLogin()` |
| `/sys/thirdLogin/oauth2/{source}/login` | GET | `ThirdLoginController` | `oauth2LoginCallback()` |
| `/sys/thirdLogin/oauth2/dingding/login` | GET | `ThirdLoginController` | `OauthDingDingLogin()` |
| `/sys/thirdLogin/get/corpId/clientId` | GET | `ThirdLoginController` | `getCorpIdClientId()` |
