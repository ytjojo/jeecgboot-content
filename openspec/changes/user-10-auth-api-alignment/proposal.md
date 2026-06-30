## Why

内容社区前端 `src/api/content/auth/` 和 `src/api/content/account/` 使用的 API 路径前缀为 `/api/v1/`，但后端 `ContentAuthController`、`ContentAccountCancellationController`、`ContentRiskControlController` 的实际路径前缀为 `/content/auth/`。**42 个前端路径全部无法命中后端端点**，导致认证、账户安全、注销等功能在运行时全部 404。

此外，14 个前端已定义的端点在后端完全缺失（验证码发送、token 刷新、captcha、设备信任、密码修改等），需要补充实现。

本次对齐统一采用 `/api/v1/content/` 前缀，与频道模块（`ChannelController` 使用 `/api/v1/channels`、`ChannelAdminController` 使用 `/api/v1/admin/channels`）同属 `/api/v1/` 体系，且通过 `content/` 段与基础库路径隔离，防止未来路由重叠。系统模块（`jeecg-module-system`）不使用 `/api/v1/` 前缀，无冲突风险。

## What Changes

**后端路径前缀统一**
- From: `ContentAuthController` 使用 `/content/auth`，`ContentAccountCancellationController` 使用 `/content/auth/cancellation`，`ContentRiskControlController` 使用 `/content/auth/risk`
- To: 统一为 `/api/v1/content/auth`、`/api/v1/content/account-cancellation`、`/api/v1/content/account-security`
- Reason: 前端 PRD/spec 已按 `/api/v1/` 设计，后端应向前端对齐；加 `content/` 段隔离基础库
- Impact: 仅改动 3 个 Controller 的 @RequestMapping，不涉及基础库

**后端缺失端点补充**
- From: 14 个前端已定义但后端未实现的端点
- To: 在内容社区模块中补充实现
- Reason: 前端代码已编写完成，等待后端对接
- Impact: 新增端点，非破坏性变更

**前端路径统一**
- From: 前端使用 `/api/v1/content/auth/`、`/api/v1/account-security/`、`/api/v1/account-cancellation/`
- To: 统一为 `/api/v1/content/auth/`、`/api/v1/content/account-security/`、`/api/v1/content/account-cancellation/`
- Reason: 与后端目标路径对齐，同时与其他内容社区 API（`/api/v1/content/channel/`）风格一致
- Impact: 修改 4 个前端 API 文件的路径字符串

**频道控制器 context-path 修复（附带）**
- From: 5 个频道控制器的 @RequestMapping 错误包含 `/jeecg-boot/` 前缀
- To: 移除 context-path 前缀，由 Spring Boot 自动处理
- Reason: 避免 context-path 变更时路由失效
- Impact: 仅改动 @RequestMapping 注解值

## Success Criteria

- 前端 `src/api/content/auth/index.ts` 中所有 API 函数调用能正确到达后端端点（HTTP 200）
- 前端 `src/api/content/account/security.ts` 中所有 API 函数调用能正确到达后端端点
- 前端 `src/api/content/account/cancellation.ts` 中所有 API 函数调用能正确到达后端端点
- 前端 `src/api/content/auth/captcha.ts` 中所有 API 函数调用能正确到达后端端点
- 内容社区登录流程（密码登录 + 短信登录）端到端可用
- 账户安全页面（绑定/解绑手机/邮箱/第三方）端到端可用
- 账号注销流程（申请/查询/撤销）端到端可用
- 频道管理 API 路径修复后功能不受影响
- 不修改 `jeecg-boot-module-system` 中任何代码
- 所有内容社区 API 路径统一以 `/api/v1/content/` 开头

## Non-Goals

- 不迁移系统模块 `/sys/` 路由
- 不修改 `jeecg-boot-base-core` 或 `jeecg-boot-module-system` 代码
- 不重构前端 API 目录结构（仅修正路径字符串）
- 不修改 `ContentUserSettingsController`、`ContentUserProfileController` 等已使用 `/content/user/` 前缀的控制器（这些路径在前端已正确对齐）
- 不统一 UserStatusController 的 `/api/content/user-status/` 前缀（留待后续独立处理）
- 不实现国际化支持

## Capabilities

### New Capabilities

- `auth-sms-email-verification`: 手机/邮箱验证码发送能力，支持注册、登录、安全操作等场景
- `token-refresh`: JWT token 刷新机制，支持无感续期
- `captcha-service`: 内容社区独立验证码服务（代理系统模块能力）
- `device-trust`: 设备信任管理，支持信任/取消信任操作
- `password-change`: 修改密码能力（验证旧密码 + 设置新密码）
- `anomaly-deny`: 异常登录否认能力，触发设备踢出
- `cancellation-eligibility`: 注销资格预检查能力

### Modified Capabilities

- `auth-login`: 登录路径从 `/content/auth/login/*` 统一为 `/api/v1/content/auth/login/*`
- `account-security`: 账户安全路径从 `/content/auth/bind|unbind|rebind/*` 统一为 `/api/v1/content/account-security/*`
- `account-cancellation`: 注销路径从 `/content/auth/cancellation/*` 统一为 `/api/v1/content/account-cancellation/*`
- `risk-control`: 风控路径从 `/content/auth/risk/*` 统一为 `/api/v1/content/account-security/anomaly/*`

## Impact

**后端代码影响**
- 修改 3 个 Controller 的 class-level @RequestMapping（ContentAuthController、ContentAccountCancellationController、ContentRiskControlController）
- 新增 14 个端点方法（分布在已有 Controller 或新建 Controller 中）
- 修复 5 个频道 Controller 的 context-path 问题
- 涉及文件约 8 个 Java 文件

**前端代码影响**
- 修改 4 个 API 文件的路径字符串（auth/index.ts、auth/captcha.ts、account/security.ts、account/cancellation.ts）
- 无新增文件、无组件改动

**依赖影响**
- 验证码端点依赖系统模块的 `CaptchaVerifyPort` 服务接口（仅调用，不修改系统模块）
- 短信发送依赖现有短信服务
- Token 刷新依赖现有 JWT 基础设施

**系统影响**
- 无数据库变更（路径对齐不涉及数据模型）
- 无缓存变更
