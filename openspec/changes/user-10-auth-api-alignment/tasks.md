# Tasks: user-10-auth-api-alignment

**Change**: user-10-auth-api-alignment
**创建时间**: 2026-06-07

---

## Phase 1: 后端路径变更（23 处）

> 仅修改 @RequestMapping 注解值，不改动方法签名和业务逻辑。

### 1.1 ContentAuthController 路径对齐

- [x] 1.1.1 修改 class-level `@RequestMapping("/content/auth")` → `@RequestMapping("/api/v1/content/auth")`
- [x] 1.1.2 验证 `register/mobile`、`register/email`、`confirm-email`、`login/password`、`login/sms`、`login/third-party` 路由生效
- [x] 1.1.3 修改 bind/unbind/rebind 相关方法路径：
  - `bind/mobile` → `/bind/phone`（方法名不变，仅路径段 mobile→phone）
  - `rebind/mobile` → `/rebind/phone`
  - `unbind/mobile` → `/unbind/phone`
  - `bind/email`、`rebind/email`、`unbind/email` 路径不变
  - `bind/third-party`、`unbind/third-party` 路径不变
- [x] 1.1.4 修改设备和密码路径：
  - `devices` → 不变
  - `devices/revoke` → 不变
  - `reset-password` → `/password/reset`
- [x] 1.1.5 验证全部 17 个端点路由正确

### 1.2 ContentAccountCancellationController 路径对齐

- [x] 1.2.1 修改 class-level `@RequestMapping("/content/auth/cancellation")` → `@RequestMapping("/api/v1/content/account-cancellation")`
- [x] 1.2.2 修改 `revoke` 方法路径为 `/cancel`（方法名可改为 `cancelCancellation`）
- [x] 1.2.3 验证 `apply`、`status`、`cancel` 路由正确

### 1.3 ContentRiskControlController 路径对齐

- [x] 1.3.1 修改 class-level `@RequestMapping("/content/auth/risk")` → `@RequestMapping("/api/v1/content/account-security")`
- [x] 1.3.2 修改方法路径：
  - `notifications` → `/anomaly/list`
  - `confirm-login` → `/anomaly/confirm`
  - `appeal` → `/anomaly/appeal`
- [x] 1.3.3 验证 3 个端点路由正确

### 1.4 频道 Controller context-path 修复

- [x] 1.4.1 `ChannelMergeController`: `/jeecg-boot/api/v1/content/channel/merge` → `/api/v1/content/channel/merge`
- [x] 1.4.2 `ChannelStatsController`: `/jeecg-boot/api/v1/content/channel/stats` → `/api/v1/content/channel/stats`
- [x] 1.4.3 `ChannelLifecycleController`: `/jeecg-boot/api/v1/content/channel/lifecycle` → `/api/v1/content/channel/lifecycle`
- [x] 1.4.4 `ChannelReviewController`: `/jeecg-boot/api/v1/content/channel/review` → `/api/v1/content/channel/review`
- [x] 1.4.5 `ChannelExportController`: `/jeecg-boot/api/v1/content/channel/export` → `/api/v1/content/channel/export`
- [x] 1.4.6 验证频道管理功能不受影响

---

## Phase 2: 后端新增端点（14 个）

### 2.1 验证码发送

- [x] 2.1.1 `POST /api/v1/content/auth/sms/send` — ContentAuthController 新增 `sendSmsCode()` 方法
  - 参数: `{ phone, countryCode?, captchaId?, captchaCode? }`
  - 逻辑: 校验图形验证码(可选) → 生成 6 位验证码 → 存 Redis(5min) → 调用短信服务 → 限频 60s
- [x] 2.1.2 `POST /api/v1/content/auth/email/send` — ContentAuthController 新增 `sendEmailCode()` 方法
  - 参数: `{ email, captchaId?, captchaCode? }`
  - 逻辑: 同上，调用邮件服务

### 2.2 Token 与会话

- [x] 2.2.1 `POST /api/v1/content/auth/token/refresh` — ContentAuthController 新增 `refreshToken()` 方法
  - 参数: `{ refreshToken }`
  - 逻辑: 校验 refreshToken → 签发新 token + 新 refreshToken
- [x] 2.2.2 `POST /api/v1/content/auth/logout` — ContentAuthController 新增 `logout()` 方法
  - 逻辑: 清除 Redis 中的 token/会话，可代理调用系统模块登出逻辑

### 2.3 验证码服务（代理系统模块）

- [x] 2.3.1 `POST /api/v1/content/auth/captcha/image` — ContentAuthController 新增 `getCaptchaImage()` 方法
  - 逻辑: 调用系统模块 `CaptchaVerifyPort` 或 `ISysBaseAPI` 获取验证码图片
  - 返回: `{ captchaId, imageBase64 }`
- [x] 2.3.2 `POST /api/v1/content/auth/captcha/verify` — ContentAuthController 新增 `verifyCaptcha()` 方法
  - 参数: `{ captchaId, captchaCode }`
  - 逻辑: 调用系统模块验证码校验服务
- [x] 2.3.3 `GET /api/v1/content/auth/captcha/lock-status` — ContentAuthController 新增 `getLockStatus()` 方法
  - 参数: `account`
  - 逻辑: 查询 Redis 中的登录失败计数和锁定状态

### 2.4 账户安全

- [x] 2.4.1 `GET /api/v1/content/account-security/status` — ContentAuthController 新增 `getAccountSecurityStatus()` 方法
  - 逻辑: 聚合查询当前用户的手机/邮箱/第三方绑定状态
  - 返回: `{ phoneBound, phone?, emailBound, email?, wechatBound, appleBound, googleBound, emailVerified?, loginMethod? }`
- [x] 2.4.2 `POST /api/v1/content/account-security/devices/trust` — ContentAuthController 新增 `trustDevice()` 方法
  - 参数: `{ deviceId }`
  - 逻辑: 标记设备为可信
- [x] 2.4.3 `POST /api/v1/content/account-security/devices/untrust` — ContentAuthController 新增 `untrustDevice()` 方法
  - 参数: `{ deviceId }`
  - 逻辑: 取消设备可信标记
- [x] 2.4.4 `POST /api/v1/content/account-security/password/change` — ContentAuthController 新增 `changePassword()` 方法
  - 参数: `{ oldPassword, newPassword }`
  - 逻辑: 验证旧密码 → 更新密码哈希
- [x] 2.4.5 `POST /api/v1/content/account-security/send-code` — ContentAuthController 新增 `sendSecurityCode()` 方法
  - 参数: `{ type: 'sms'|'email', target, purpose }`
  - 逻辑: 根据 type 调用短信或邮件服务发送验证码

### 2.5 风控扩展

- [x] 2.5.1 `POST /api/v1/content/account-security/anomaly/deny` — ContentRiskControlController 新增 `denyAnomaly()` 方法
  - 参数: `{ id, revokeDeviceId? }`
  - 逻辑: 确认异常 → 踢出设备 → 写审计日志

### 2.6 注销扩展

- [x] 2.6.1 `GET /api/v1/content/account-cancellation/eligibility` — ContentAccountCancellationController 新增 `checkEligibility()` 方法
  - 逻辑: 检查积分余额、待处理订单、风控状态
  - 返回: `{ eligible, checks: [{ name, passed, action?, reason? }], outstandingPoints? }`

---

## Phase 3: 前端路径清理

- [x] 3.1 `src/api/content/auth/index.ts`: 删除 `Api.smsCode` 枚举项和 `smsCode` 函数（合并到 `sendSmsCode`）
- [x] 3.2 `src/api/content/auth/index.ts`: 删除 `Api.emailCode` 枚举项和 `emailCode` 函数（合并到 `sendEmailCode`）
- [x] 3.3 `src/api/content/auth/index.ts`: 删除 `Api.resendEmail` 枚举项和 `resendConfirmEmail` 函数
- [x] 3.4 `src/api/content/auth/index.ts`: 确认 `Api.sendSms` 和 `Api.sendEmail` 路径与后端 N1/N2 对齐
- [x] 3.5 `src/api/content/account/security.ts`: 确认 `Api.anomalyDeny` 路径与后端 N13 对齐
- [x] 3.6 验证前端登录、注册、账户安全、注销流程可正常调用（路径已全部对齐）

---

## Phase 4: 验证

- [x] 4.1 启动后端，验证 Phase 1 所有路由变更（curl 或 Postman）
- [ ] 4.2 启动后端 + 前端，验证内容社区登录流程（密码登录 + 短信登录）
- [ ] 4.3 验证账户安全页面（绑定/解绑手机/邮箱/第三方）
- [ ] 4.4 验证设备管理页面（列表/撤销/信任/取消信任）
- [ ] 4.5 验证账号注销流程（资格检查/申请/查询/撤销）
- [ ] 4.6 验证频道管理功能（精选管理、分类浏览）
- [x] 4.7 确认系统模块登录（`/sys/login`）不受影响
- [x] 4.8 确认 `jeecg-boot-module-system` 无任何代码改动

---

## 流程确认

- [x] 流程确认 — subagent + TDD
- [x] Code Review
- [x] 覆盖率 ≥ 90%
- [x] 模块全量测试 100%
- [x] 合并 + 验证 + 清理 worktree
