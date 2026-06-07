# 平台系统模块 (jeecg-module-system) 认证能力差距分析

> 分析日期: 2026-05-28
> 分析范围: LoginController + SysUserController + ThirdLoginController + SysPermissionController + CasClientController + SecurityConfig + 相关 Service/Util
> 对比基准: tasks.md 8 大模块 64 个子任务

---

## 总览

| 模块 | 平台已有能力 | 与 tasks.md 差距 | 适配难度 |
|------|-------------|-----------------|---------|
| 1. 基础结构与数据迁移 | SysUser 实体、RBAC 表结构 | 无 content.auth 包、无凭证/密码历史/风险事件表 | 中 |
| 2. 验证码与外部通道 | 图片验证码 + SMS（阿里云） | 无邮件验证、无适配接口抽象、无冷却机制 | 低-中 |
| 3. 注册与登录 | 完整的密码/手机/第三方/扫码/CAS 登录 | 内容模块需独立登录体系，非复用平台 | 高 |
| 4. 会话与设备管理 | Redis token + 单点登录控制 | 无设备上限、无 jti、无设备列表、无黑名单 | 高 |
| 5. 账号绑定与密码找回 | 手机号变更、密码重置 | 无密码历史校验、无邮箱绑定、无换绑流程 | 中 |
| 6. 风控与异常登录 | 5次/10分钟锁定 + SMS IP 限流 | 无 IP 登录限流、无风险事件、无异常检测 | 高 |
| 7. 账号注销 | `userLogOff()`（平台级） | 无冷静期、无数据匿名化（平台级直接删除） | 低（内容模块已有） |
| 8. API 收口 | `/sys/*` 路径 + 基本 Knife4j 注解 | 无 `/api/v1/` 版本化、无统一错误码 | 中 |

**关键结论: 平台系统模块有成熟的认证基础设施，但 tasks.md 要求内容社区建立独立认证体系，不能直接复用平台登录，需在内容模块层重新编排。**

---

## 逐项对比分析

### 1. 基础结构与数据迁移

| 子任务 | 平台现状 | 差距 | 说明 |
|--------|---------|------|------|
| 1.1 content.auth 包和枚举 | 无（认证代码在 `org.jeecg.modules.system`） | **需新建** | 内容模块需独立的 auth 领域包 |
| 1.2 编译测试 | 无 | **需新建** | — |
| 1.3 用户账号/凭证/第三方绑定/密码历史表 | `sys_user`（账号+密码+salt）、`sys_third_account`（第三方绑定） | **需新建** 内容模块专用表 | 平台表结构不可直接用于内容社区 |
| 1.4 migration 约束测试 | 无 | **需新建** | — |
| 1.5 设备会话/风险事件/注销申请表 | 无设备会话表、无风险事件表 | **需新建** | 内容模块已有 `content_user_device_session` 基础 |
| 1.6 回滚脚本 | 无 | **需新建** | — |
| 1.7 entity/mapper/service | `SysUser` entity + MyBatis-Plus mapper + `ISysUserService` | **需在内容模块新建** | 可参考平台模式 |
| 1.8 mapper 测试 | 无 | **需新建** | — |

**平台可参考模式:**
- `SysUser` → `@TableId(ASSIGN_ID)` + `@TableLogic` 软删除
- `ISysUserService` / `SysUserServiceImpl` → IService/ServiceImpl 模式
- MyBatis-Plus BaseMapper + 自定义 XML mapper

---

### 2. 验证码与外部通道适配

| 子任务 | 平台现状 | 差距 | 说明 |
|--------|---------|------|------|
| 2.1 短信/邮件/人机验证/地理位置适配接口 | SMS 已有（`DySmsHelper` + 阿里云）、图片验证码已有（`RandImageUtil`） | **缺邮件、缺人机验证、缺适配接口抽象** | 平台直接调用，无适配层 |
| 2.2 mock 测试 | 无 | **需新建** | — |
| 2.3 验证码生成/Redis/TTL/冷却/失败计数 | 图片验证码: Redis 60s TTL；SMS: Redis 600s TTL + IP 限流 | **缺冷却机制、缺统一失败计数** | 平台的 SMS 限流是内存级别 |
| 2.4 验证码边界测试 | 无 | **需新建** | — |
| 2.5 邮件验证链接和密码重置 token | 无邮件验证；密码重置通过 SMS | **需新建邮件通道** | — |
| 2.6 邮件 token 测试 | 无 | **需新建** | — |

**平台已有能力详情:**

| 能力 | 实现 | 文件 |
|------|------|------|
| 图片验证码生成 | 4 位随机字符 + base64 图片 | `LoginController.randomImage()` line 633 |
| 验证码存储 | Redis key = `MD5(checkKey + secret) + code`, TTL 60s | `LoginController.validateCaptcha()` line 950 |
| SMS 发送 | 阿里云 SMS + 模板编码 | `DySmsHelper.sendSms()` |
| SMS 存储 | Redis key = `phone_msg{mobile}`, TTL 600s | `LoginController.sms()` line 423 |
| SMS IP 限流 | 内存 ConcurrentHashMap, 5次/分钟 | `DySmsLimit` |
| 验证码可关闭 | `Firewall.enableLoginCaptcha` 配置项 | `JeecgBaseConfig` |

---

### 3. 注册与登录

| 子任务 | 平台现状 | 差距 | 说明 |
|--------|---------|------|------|
| 3.1 手机号验证码注册 | `SysUserController.userRegister()` — 手机+SMS 验证码注册 | **内容模块需独立实现** | 平台注册逻辑不可直接复用 |
| 3.2 手机号注册测试 | 无 | **需新建** | — |
| 3.3 邮箱密码注册 | 无独立邮箱注册（仅 `getUserByEmail` 查询） | **需新建** | — |
| 3.4 邮箱注册测试 | 无 | **需新建** | — |
| 3.5 第三方登录 provider | `ThirdLoginController` — JustAuth 动态 provider + 钉钉/企微 OAuth2 | **内容模块需编排接入** | 平台有完整 OAuth2 基础设施 |
| 3.6 第三方登录测试 | 无 | **需新建** | — |
| 3.7 验证码登录和密码登录 | `LoginController.login()` 密码登录 + `phoneLogin()` 验证码登录 | **内容模块需独立登录编排** | 见下方登录流程详情 |
| 3.8 登录测试 | 无 | **需新建** | — |

**平台登录流程详情（7 步）:**

```
前端 AES 加密密码
  → AesEncryptUtil.resolvePassword() 解密
  → isLoginFailOvertimes() 检查锁定（5次/10分钟）
  → validateCaptcha() 图片验证码校验
  → getUserByName() + checkUserIsEffective() 用户校验
  → PasswordUtil.encrypt() 密码比对
  → userInfo() → JwtUtil.sign() 生成 OAuth2 token → Redis 缓存 → handleSingleSignOn()
  → baseCommonService.addLog() 记录日志
```

**平台支持的登录方式:**

| 方式 | 端点 | 认证机制 |
|------|------|----------|
| PC 密码登录 | `POST /sys/login` | AES 密码 + 图片验证码 + JWT |
| 手机验证码登录 | `POST /sys/phoneLogin` | SMS 验证码 + JWT |
| APP 密码登录 | `POST /sys/mLogin` | AES 密码（无验证码）+ JWT |
| 第三方 OAuth2 | `GET /sys/thirdLogin/oauth2/{source}/callback` | JustAuth + JWT |
| CAS SSO | `GET /sys/cas/client/validateLogin` | CAS ticket + JWT |
| 扫码登录 | `GET/POST /sys/getLoginQrcode/scanLoginQrcode/getQrcodeToken` | QR + 已登录 token 共享 |
| OAuth2 Password Grant | `POST /oauth2/token` (grant_type=password) | Spring Authorization Server |
| OAuth2 Phone Grant | `POST /oauth2/token` (grant_type=phone) | Spring Authorization Server |

---

### 4. 会话与设备管理

| 子任务 | 平台现状 | 差距 | 说明 |
|--------|---------|------|------|
| 4.1 设备会话 + token jti | Redis 存储 token，无 jti 记录 | **需新建** jti 字段和设备会话记录 | 平台 token 存 Redis 但无设备维度 |
| 4.2 设备会话测试 | 无 | **需新建** | — |
| 4.3 最多 5 设备 + 自动下线 | 仅有单点登录（per client type: PC/APP/PHONE） | **需新建** 设备上限逻辑 | 平台只支持"全部挤出"或"允许并发" |
| 4.4 设备挤出测试 | 无 | **需新建** | — |
| 4.5 设备列表和下线接口 | `SysUserOnlineController` 可查看在线用户 | **需新建** 设备维度的列表和下线 | 平台无设备指纹、无设备管理 |
| 4.6 设备列表测试 | 无 | **需新建** | — |
| 4.7 token 黑名单 + 失效拦截 | Redis 白名单模式（删除=失效）+ `RedisTokenValidationFilter` 拦截 | **需适配** 白名单模式可复用 | 平台无显式黑名单，但白名单等效 |
| 4.8 被下线 token 拒绝测试 | 无 | **需新建** | — |

**平台会话管理详情:**

| 能力 | 实现 |
|------|------|
| Token 存储 | Redis `token::jeecg-client::{token}`, TTL = 2 × EXPIRE_TIME |
| Token 有效期 | PC/Phone: 84 小时, APP: 360 小时 |
| 单点登录 | `handleSingleSignOn()` — per client type, 删除旧 token + OAuth2 授权 |
| 强制下线 | `SysUserOnlineController.forceLogout()` — 删除 Redis token |
| Token 失效检测 | `RedisTokenValidationFilter` — 检查 OAuth2Authorization 是否存在 |
| 在线用户列表 | `SysUserOnlineController` — 基于 Redis key 扫描 |

---

### 5. 账号绑定与密码找回

| 子任务 | 平台现状 | 差距 | 说明 |
|--------|---------|------|------|
| 5.1 手机号和邮箱绑定 | `changePhone()` + SMS 验证 | **缺邮箱绑定** | 平台有手机变更，无邮箱绑定 |
| 5.2 绑定测试 | 无 | **需新建** | — |
| 5.3 换绑、解绑 | `changePhone()` 换绑手机 | **缺解绑、缺双向验证** | — |
| 5.4 换绑测试 | 无 | **需新建** | — |
| 5.5 第三方账号绑定/解绑 | `ThirdLoginController.bindingThirdPhone()` | **缺独立绑定/解绑管理** | 平台绑定是登录流程一部分 |
| 5.6 第三方绑定测试 | 无 | **需新建** | — |
| 5.7 密码重置 + 密码历史 | `passwordChange()` SMS 验证码重置 + `resetPassword()` 旧密码验证 | **无密码历史校验** | `SysUser` 有 `lastPwdUpdateTime` 但无历史表 |
| 5.8 密码重置测试 | 无 | **需新建** | — |

**平台密码管理详情:**

| 方法 | 用途 | 验证方式 |
|------|------|----------|
| `resetPassword(username, old, new, confirm)` | 用户自改密码 | 旧密码验证 |
| `changePassword(SysUser)` | 系统改密码 | 生成新 salt |
| `passwordChange()` | 忘记密码 | SMS 验证码 + 用户名 + 手机号匹配 |
| `resetToSysPassword(usernames)` | 管理员重置 | admin 角色权限 |
| `updatePasswordNotBindPhone()` | 未绑手机改密 | 旧密码验证 |

---

### 6. 风控与异常登录

| 子任务 | 平台现状 | 差距 | 说明 |
|--------|---------|------|------|
| 6.1 登录失败风控/验证码挑战/30分钟锁定 | 5 次/10 分钟锁定（Redis `LOGIN_FAIL_{username}`） | **参数不同**（tasks 要 10 次挑战、20 次锁定、30 分钟） | 平台机制可复用，参数需调整 |
| 6.2 风控测试 | 无 | **需新建** | — |
| 6.3 批量注册 IP 限流 | 无注册限流 | **需新建** | SMS 有 IP 限流但注册没有 |
| 6.4 IP 限流测试 | 无 | **需新建** | — |
| 6.5 风险事件落库 | 无风险事件表 | **需新建** | 平台仅记操作日志 |
| 6.6 风险事件测试 | 无 | **需新建** | — |
| 6.7 新设备和异地登录检测 | 无 | **需新建** | 平台无 GeoIP、无设备指纹 |
| 6.8 异常登录通知测试 | 无 | **需新建** | — |

**平台风控详情:**

| 机制 | 实现 | 限制 |
|------|------|------|
| 登录失败锁定 | Redis 计数 `LOGIN_FAIL_{username}`, 5 次 → 锁 10 分钟 | 仅按用户名，不按 IP |
| SMS 限流 | 内存 ConcurrentHashMap, 5 次/分钟/IP | 内存级别，不持久化 |
| 验证码 | 图片验证码 4 字符，60s TTL | 可配置关闭 |
| OAuth2 锁定 | `PasswordGrantAuthenticationProvider` 中有锁定逻辑 | **BUG: TTL 设为 10 秒而非 600 秒** |
| 操作日志 | `baseCommonService.addLog()` 记录登录/密码变更等 | 非结构化，无风险评估 |

---

### 7. 账号注销

| 子任务 | 平台现状 | 差距 | 说明 |
|--------|---------|------|------|
| 7.1 注销申请前置校验和冷静期 | `userLogOff()` SMS 验证后直接删除 | **无冷静期** | 平台直接删除，内容模块已有冷静期 |
| 7.2 注销申请测试 | 无 | **需新建** | — |
| 7.3 冷静期登录提示和取消 | 无 | **内容模块已有** `revokeCancel()` | — |
| 7.4 冷静期测试 | 无 | **需新建** | — |
| 7.5 最终注销 + 数据匿名化 | `userLogOff()` 设置 `delFlag=1` | **无数据匿名化** | 平台仅标记删除 |
| 7.6 最终注销测试 | 无 | **需新建** | — |
| 7.7 冷静期配置 7-30 天 | 无冷静期 | **内容模块已有** 固定 7 天 | 需改为可配置 |
| 7.8 冷静期边界测试 | 无 | **需新建** | — |

**平台注销:** `ISysUserService.userLogOff()` — SMS 验证 → 设置 `delFlag=1` → 清除缓存。无冷静期、无数据匿名化。

**内容模块已有:** `ContentAccountServiceImpl` — `initiateCancel()` 冷静期 7 天 + `completeCancel()` 前置校验 + `revokeCancel()` 取消。覆盖率约 75%。

---

### 8. API 收口与验收

| 子任务 | 平台现状 | 差距 | 说明 |
|--------|---------|------|------|
| 8.1 `/api/v1/content/auth/*` 路径 | 当前 `/sys/*` 路径 | **需新建** 版本化路径 | — |
| 8.2 参数校验 + Result<T> 测试 | 有基本校验 | **需新建** 测试 | — |
| 8.3 Knife4j 注解和错误码 | 部分 `@Schema` 注解，无统一错误码 | **需补齐** | — |
| 8.4 接口文档测试 | 无 | **需新建** | — |
| 8.5 运行测试 | 无认证相关测试 | **需新建** | — |
| 8.6 修复测试 | 无 | **需新建** | — |

---

## 平台可复用资产清单

| 资产 | 文件 | 复用方式 |
|------|------|----------|
| `JwtUtil` | `jeecg-boot-base-core/.../JwtUtil.java` | 直接调用 — token 生成/验证/解析 |
| `PasswordUtil` | `jeecg-boot-base-core/.../PasswordUtil.java` | 直接调用 — 密码加密/验证 |
| `AesEncryptUtil` | `jeecg-boot-base-core/.../AesEncryptUtil.java` | 直接调用 — 前端密码解密 |
| `SecurityConfig` | `jeecg-boot-base-core/.../SecurityConfig.java` | 扩展 — 添加内容模块路径到 permitAll |
| `RedisTokenValidationFilter` | `jeecg-boot-base-core/.../filter/` | 直接复用 — token 失效拦截 |
| `DySmsHelper` | `jeecg-boot-base-core/.../DySmsHelper.java` | 直接调用 — SMS 发送 |
| `DySmsLimit` | `jeecg-boot-base-core/.../DySmsLimit.java` | 参考 — 需改为 Redis 级别 |
| `RandImageUtil` | `jeecg-boot-base-core/.../RandImageUtil.java` | 直接调用 — 图片验证码生成 |
| `SystemUserAccountGateway` | 内容模块已有 | 扩展 — 添加登录能力 |
| `ISysUserService` | `jeecg-system-biz/.../ISysUserService.java` | 通过 Gateway 间接调用 |
| OAuth2 Authorization Server | `SecurityConfig` + 4 种 Grant Type | 直接复用 — token 签发基础设施 |
| `JeecgRedisOAuth2AuthorizationService` | `jeecg-boot-base-core/.../` | 直接复用 — OAuth2 授权存储 |
| `CommonConstant` | Redis key 前缀常量 | 直接引用 |

---

## tasks.md 覆盖率评估（基于平台系统模块）

| 模块 | 子任务数 | 平台已有 | 内容模块已有 | 需新建 | 覆盖率 |
|------|---------|---------|-------------|--------|--------|
| 1. 基础结构 | 8 | 2 | 1 | 5 | 37% |
| 2. 验证码适配 | 6 | 2 | 0 | 4 | 33% |
| 3. 注册与登录 | 8 | 3 | 1 | 4 | 50% |
| 4. 会话与设备 | 8 | 1 | 1 | 6 | 25% |
| 5. 绑定与密码 | 8 | 3 | 2 | 3 | 62% |
| 6. 风控 | 8 | 1 | 0 | 7 | 12% |
| 7. 账号注销 | 8 | 1 | 5 | 2 | 75% |
| 8. API 收口 | 6 | 1 | 0 | 5 | 16% |
| **合计** | **60** | **14** | **10** | **36** | **40%** |

---

## 关键发现与风险

### 已发现 BUG

1. **OAuth2 锁定 TTL 错误**: `PasswordGrantAuthenticationProvider` line 293 将锁定 TTL 设为 `10` 秒而非 `600` 秒，导致 OAuth2 密码登录的失败锁定几乎无效。

### 安全缺陷

1. **登录失败仅按用户名计数**: 可通过切换用户名绕过，无 IP 级别限流
2. **APP 登录 (`mLogin`) 无验证码**: 仅检查失败计数 + 密码，安全弱于 PC 登录
3. **手机/APP 登录成功不清除失败计数**: 仅 PC 登录在 line 149 清除
4. **SMS 限流为内存级别**: 不持久化、不跨实例共享
5. **扫码登录无用户确认步骤**: 扫码即登录，无二次确认
6. **静态 AES 密钥**: `EncryptedString.key/iv` 编译时确定，安全性有限

### 架构约束

1. **平台登录不可直接用于内容社区**: `LoginController` 绑定 `SysUser` 实体和平台 RBAC，内容社区需独立用户体系
2. **OAuth2 基础设施可复用**: Spring Authorization Server 的 4 种 Grant Type 可扩展
3. **Redis 模式可复用**: token 存储、验证码存储、锁定计数的 Redis 模式可直接借鉴
4. **`SystemUserAccountGateway` 是关键桥梁**: 内容模块已有此 Gateway 连接平台系统，需扩展其登录能力

---

## 建议实施策略

### 策略 A: 内容模块独立认证体系（推荐）

在内容模块内建立完整的 auth 领域，通过 Gateway 调用平台能力：

```
ContentAuthController (/api/v1/content/auth/*)
  → ContentAuthService (登录编排)
    → SystemUserAccountGateway (调用平台用户服务)
    → JwtUtil (token 生成，复用平台)
    → Redis (验证码/锁定/会话，复用平台 Redis)
```

**优势:** 独立演进、不影响平台、可定制风控参数
**劣势:** 需新建较多代码

### 策略 B: 扩展平台登录 + 内容模块适配层

在平台 LoginController 上扩展，内容模块仅做适配：

**优势:** 代码复用率高
**劣势:** 耦合平台、受平台约束、风控参数难以定制

**推荐策略 A**，与内容模块已有的 Gateway 模式一致。

决议使用方案A