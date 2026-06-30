# Design: user-10-auth-api-alignment

**Change**: user-10-auth-api-alignment
**创建时间**: 2026-06-07
**类型**: 基础设施对齐（前后端同时变更）

---

## Decisions

### Decision 1: 统一采用 `/api/v1/` 前缀

**选择**: 内容社区所有 API 统一使用 `/api/v1/content/` 前缀

**备选方案**:
- A) 后端改为 `/api/v1/content/`，前端不改 ✅
- B) 前端改为 `/content/auth/`，后端不改
- C) 双方都改，采用第三种前缀

**选择理由**:
- 前端 PRD/spec 已全部按 `/api/v1/` 设计，改动前端等于推翻已有文档
- 频道模块已使用 `/api/v1/channels` 和 `/api/v1/admin/channels`，保持风格一致
- 系统模块无 `/api/v1/` 路由，无冲突风险
- `/api/v1/` 是 REST API 版本管理的标准实践

### Decision 2: 后端 Controller 拆分策略

**选择**: 保持现有 3 个 Controller 不拆分，仅修改 @RequestMapping 前缀

**映射关系**:

| Controller | 当前前缀 | 目标前缀 |
|---|---|---|
| ContentAuthController | `/content/auth` | `/api/v1/content/auth` |
| ContentAccountCancellationController | `/content/auth/cancellation` | `/api/v1/content/account-cancellation` |
| ContentRiskControlController | `/content/auth/risk` | `/api/v1/content/account-security` |

**说明**: RiskControlController 的风控功能（异常登录通知、确认登录）属于账户安全范畴，合并到 `/api/v1/account-security` 下合理。新增的风控相关端点（deny、appeal）也放在同一 Controller 中。

### Decision 3: 新增端点放置策略

**选择**: 验证码、token 刷新、登出等通用认证端点放入 ContentAuthController；设备信任、密码修改等账户安全端点放入 ContentAuthController（复用已有设备/密码方法）；注销资格检查放入 ContentAccountCancellationController

**理由**: 不为 14 个新端点单独创建 Controller，复用已有 Controller 减少文件数量。

### Decision 4: 频道 Controller context-path 修复

**选择**: 移除 5 个频道 Controller @RequestMapping 中的 `/jeecg-boot/` 前缀

**理由**: Spring Boot 的 `server.servlet.context-path` 配置会自动添加 `/jeecg-boot/`，Controller 不应重复包含。当前能工作是因为 context-path 匹配了，但这属于隐式耦合。

---

## 路径对照表

### 认证模块 (ContentAuthController)

| # | 功能 | HTTP | 后端当前路径 | 目标路径 | 前端当前路径 | 改动 |
|---|------|------|-------------|---------|-------------|------|
| 1 | 手机注册 | POST | `/content/auth/register/mobile` | `/api/v1/content/auth/register/mobile` | `/api/v1/content/auth/register/mobile` | 后端改 |
| 2 | 邮箱注册 | POST | `/content/auth/register/email` | `/api/v1/content/auth/register/email` | `/api/v1/content/auth/register/email` | 后端改 |
| 3 | 邮箱确认 | GET | `/content/auth/confirm-email` | `/api/v1/content/auth/email/confirm` | `/api/v1/content/auth/email/confirm` | 后端改 |
| 4 | 密码登录 | POST | `/content/auth/login/password` | `/api/v1/content/auth/login/password` | `/api/v1/content/auth/login/password` | 后端改 |
| 5 | 短信登录 | POST | `/content/auth/login/sms` | `/api/v1/content/auth/login/sms-code` | `/api/v1/content/auth/login/sms-code` | 后端改 |
| 6 | 第三方登录 | POST | `/content/auth/login/third-party` | `/api/v1/content/auth/login/third-party` | `/api/v1/content/auth/login/third-party` | 后端改 |
| 7 | 绑定手机 | POST | `/content/auth/bind/mobile` | `/api/v1/content/account-security/bind/phone` | `/api/v1/account-security/bind/phone` | 后端改 |
| 8 | 换绑手机 | POST | `/content/auth/rebind/mobile` | `/api/v1/content/account-security/rebind/phone` | `/api/v1/account-security/rebind/phone` | 后端改 |
| 9 | 解绑手机 | POST | `/content/auth/unbind/mobile` | `/api/v1/content/account-security/unbind/phone` | `/api/v1/account-security/unbind/phone` | 后端改 |
| 10 | 绑定邮箱 | POST | `/content/auth/bind/email` | `/api/v1/content/account-security/bind/email` | `/api/v1/account-security/bind/email` | 后端改 |
| 11 | 换绑邮箱 | POST | `/content/auth/rebind/email` | `/api/v1/content/account-security/rebind/email` | `/api/v1/account-security/rebind/email` | 后端改 |
| 12 | 解绑邮箱 | POST | `/content/auth/unbind/email` | `/api/v1/content/account-security/unbind/email` | `/api/v1/account-security/unbind/email` | 后端改 |
| 13 | 绑定第三方 | POST | `/content/auth/bind/third-party` | `/api/v1/content/account-security/bind/third-party` | `/api/v1/account-security/bind/third-party` | 后端改 |
| 14 | 解绑第三方 | POST | `/content/auth/unbind/third-party` | `/api/v1/content/account-security/unbind/third-party` | `/api/v1/account-security/unbind/third-party` | 后端改 |
| 15 | 设备列表 | GET | `/content/auth/devices` | `/api/v1/content/account-security/devices` | `/api/v1/account-security/devices` | 后端改 |
| 16 | 撤销设备 | POST | `/content/auth/devices/revoke` | `/api/v1/content/account-security/devices/revoke` | `/api/v1/account-security/devices/revoke` | 后端改 |
| 17 | 重置密码 | POST | `/content/auth/reset-password` | `/api/v1/content/account-security/password/reset` | `/api/v1/account-security/password/reset` | 后端改 |

### 新增认证端点 (ContentAuthController)

| # | 功能 | HTTP | 目标路径 | 前端当前路径 | 说明 |
|---|------|------|---------|-------------|------|
| N1 | 发送手机验证码 | POST | `/api/v1/content/auth/sms/send` | `/api/v1/content/auth/sms/send` | 复用短信服务 |
| N2 | 发送邮箱验证码 | POST | `/api/v1/content/auth/email/send` | `/api/v1/content/auth/email/send` | 复用邮件服务 |
| N3 | 刷新 token | POST | `/api/v1/content/auth/token/refresh` | `/api/v1/content/auth/token/refresh` | JWT refresh |
| N4 | 登出 | POST | `/api/v1/content/auth/logout` | `/api/v1/content/auth/logout` | 清理 token/会话 |
| N5 | 获取验证码图片 | POST | `/api/v1/content/auth/captcha/image` | `/api/v1/content/auth/captcha/image` | 代理系统模块 |
| N6 | 校验验证码 | POST | `/api/v1/content/auth/captcha/verify` | `/api/v1/content/auth/captcha/verify` | 代理系统模块 |
| N7 | 查询锁定状态 | GET | `/api/v1/content/auth/captcha/lock-status` | `/api/v1/content/auth/captcha/lock-status` | 风控查询 |
| N8 | 账户安全状态 | GET | `/api/v1/content/account-security/status` | `/api/v1/account-security/status` | 聚合查询 |
| N9 | 信任设备 | POST | `/api/v1/content/account-security/devices/trust` | `/api/v1/account-security/devices/trust` | 设备管理扩展 |
| N10 | 取消信任 | POST | `/api/v1/content/account-security/devices/untrust` | `/api/v1/account-security/devices/untrust` | 设备管理扩展 |
| N11 | 修改密码 | POST | `/api/v1/content/account-security/password/change` | `/api/v1/account-security/password/change` | 验证旧密码 |
| N12 | 安全操作验证码 | POST | `/api/v1/content/account-security/send-code` | `/api/v1/account-security/send-code` | 复用短信/邮件 |

### 风控模块 (ContentRiskControlController)

| # | 功能 | HTTP | 后端当前路径 | 目标路径 | 前端当前路径 | 改动 |
|---|------|------|-------------|---------|-------------|------|
| 18 | 异常登录列表 | GET | `/content/auth/risk/notifications` | `/api/v1/content/account-security/anomaly/list` | `/api/v1/account-security/anomaly/list` | 后端改 |
| 19 | 确认登录 | POST | `/content/auth/risk/confirm-login` | `/api/v1/content/account-security/anomaly/confirm` | `/api/v1/account-security/anomaly/confirm` | 后端改 |
| 20 | 申诉 | POST | `/content/auth/risk/appeal` | `/api/v1/content/account-security/anomaly/appeal` | — | 后端改 + 前端新增 |

### 新增风控端点

| # | 功能 | HTTP | 目标路径 | 前端当前路径 | 说明 |
|---|------|------|---------|-------------|------|
| N13 | 否认异常登录 | POST | `/api/v1/content/account-security/anomaly/deny` | `/api/v1/account-security/anomaly/deny` | 触发设备踢出 |

### 注销模块 (ContentAccountCancellationController)

| # | 功能 | HTTP | 后端当前路径 | 目标路径 | 前端当前路径 | 改动 |
|---|------|------|-------------|---------|-------------|------|
| 21 | 申请注销 | POST | `/content/auth/cancellation/apply` | `/api/v1/content/account-cancellation/apply` | `/api/v1/account-cancellation/apply` | 后端改 |
| 22 | 查询状态 | GET | `/content/auth/cancellation/status` | `/api/v1/content/account-cancellation/status` | `/api/v1/account-cancellation/status` | 后端改 |
| 23 | 撤销注销 | POST | `/content/auth/cancellation/revoke` | `/api/v1/content/account-cancellation/cancel` | `/api/v1/account-cancellation/cancel` | 后端改 |

### 新增注销端点

| # | 功能 | HTTP | 目标路径 | 前端当前路径 | 说明 |
|---|------|------|---------|-------------|------|
| N14 | 注销资格检查 | GET | `/api/v1/content/account-cancellation/eligibility` | `/api/v1/account-cancellation/eligibility` | 积分/待办检查 |

### 前端需修正路径

| # | 文件 | 当前值 | 目标值 | 说明 |
|---|------|--------|--------|------|
| F1 | `src/api/content/auth/index.ts` | `Api.smsCode = '/api/v1/content/auth/sms-code'` | 删除此枚举，合并到 `sendSms` | 与 `sms/send` 重复 |
| F2 | `src/api/content/auth/index.ts` | `Api.emailCode = '/api/v1/content/auth/email-code'` | 删除此枚举，合并到 `sendEmail` | 与 `email/send` 重复 |
| F3 | `src/api/content/auth/index.ts` | `Api.resendEmail = '/api/v1/content/auth/email/resend'` | 删除此枚举和对应函数 | 后端无此端点 |
| F4 | `src/api/content/account/security.ts` | `Api.anomalyDeny = '/api/v1/content/account-security/anomaly/deny'` | 保持不变 | 后端新增 N13 对齐 |

### 频道 Controller context-path 修复

| # | Controller | 当前 @RequestMapping | 目标 |
|---|---|---|---|
| C1 | ChannelMergeController | `/jeecg-boot/api/v1/content/channel/merge` | `/api/v1/content/channel/merge` |
| C2 | ChannelStatsController | `/jeecg-boot/api/v1/content/channel/stats` | `/api/v1/content/channel/stats` |
| C3 | ChannelLifecycleController | `/jeecg-boot/api/v1/content/channel/lifecycle` | `/api/v1/content/channel/lifecycle` |
| C4 | ChannelReviewController | `/jeecg-boot/api/v1/content/channel/review` | `/api/v1/content/channel/review` |
| C5 | ChannelExportController | `/jeecg-boot/api/v1/content/channel/export` | `/api/v1/content/channel/export` |

---

## 涉及文件清单

### 后端（jeecg-module-content）

| 文件 | 改动类型 |
|------|---------|
| `ContentAuthController.java` | 修改 @RequestMapping + 新增 12 个方法 |
| `ContentAccountCancellationController.java` | 修改 @RequestMapping + 新增 1 个方法 |
| `ContentRiskControlController.java` | 修改 @RequestMapping + 新增 1 个方法 |
| `ChannelMergeController.java` | 修改 @RequestMapping |
| `ChannelStatsController.java` | 修改 @RequestMapping |
| `ChannelLifecycleController.java` | 修改 @RequestMapping |
| `ChannelReviewController.java` | 修改 @RequestMapping |
| `ChannelExportController.java` | 修改 @RequestMapping |

### 前端（jeecgboot-vue3）

| 文件 | 改动类型 |
|------|---------|
| `src/api/content/auth/index.ts` | 删除 3 个冗余枚举和对应函数 |
| `src/api/content/account/security.ts` | 无改动（路径已对齐） |
| `src/api/content/account/cancellation.ts` | 无改动（路径已对齐） |
| `src/api/content/auth/captcha.ts` | 无改动（路径已对齐） |

---

## 实施顺序

**Phase 1: 后端路径变更（23 处 @RequestMapping 修改）**
- 优先级最高，前端代码已使用目标路径，后端改完即可连通
- 无数据库变更，纯注解修改，风险最低

**Phase 2: 后端新增端点（14 个）**
- 按依赖关系排序：验证码(N1-N2) → 登录增强(N3-N4) → captcha(N5-N7) → 账户安全(N8-N12) → 风控(N13) → 注销(N14)
- 每个端点独立可交付

**Phase 3: 前端路径清理（4 处）**
- 删除冗余枚举和函数，与后端新增端点对齐
- 在 Phase 2 完成后执行

**Phase 4: 频道 context-path 修复（5 处）**
- 独立于其他改动，可并行执行
