## Context

内容社区模块（`jeecg-module-content`）当前缺少独立的认证前端体系。现有平台登录页（`/sys/login`）为管理后台设计，无法满足内容社区的注册、第三方登录、设备管理、账号安全等需求。

前端技术栈：Vue 3 + Vite + TypeScript + Ant Design Vue 4，组件自动导入，使用 `defHttp` 封装 HTTP 请求，`useUserStore` 管理用户状态。

后端接口路径前缀为 `/api/v1/content/auth/*`（认证）、`/api/v1/content/account-security/*`（账号安全）、`/api/v1/content/account-cancellation/*`（注销），均已定义在 PRD 中。

## Goals / Non-Goals

**Goals:**
- 构建独立的内容社区认证前端体系（登录、注册、账号安全）
- 支持手机号/邮箱注册、密码/验证码登录、第三方登录（微信/Apple/Google）
- 实现多端会话管理、账号绑定解绑、密码找回、异常登录通知
- 实现风控拦截交互（图形验证码、账号锁定提示）
- 实现账号注销全流程（检查、冷静期、取消）
- 全部页面适配 PC / 平板 / 移动端
- 集成前端埋点体系

**Non-Goals:**
- 不实现生物识别登录（指纹/面容）
- 不实现两步验证（EPIC-08）
- 不实现企业组织通讯录
- 不实现邀请码注册
- 不实现高风险找回的安全问题和身份证明上传
- 不做多语言国际化（仅中文）

## API 依赖清单

### 认证接口（/api/v1/content/auth/*）

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 手机号注册 | POST | `/api/v1/content/auth/register/mobile` | 手机号 + 验证码注册 |
| 邮箱注册 | POST | `/api/v1/content/auth/register/email` | 邮箱 + 密码注册 |
| 邮箱注册确认 | GET | `/api/v1/content/auth/register/email/confirm` | 邮箱验证链接确认 |
| 第三方登录 | POST | `/api/v1/content/auth/login/third-party` | 第三方授权码登录 |
| 验证码登录 | POST | `/api/v1/content/auth/login/sms-code` | 手机号 + 验证码登录 |
| 密码登录 | POST | `/api/v1/content/auth/login/password` | 手机号/邮箱 + 密码登录 |
| 发送短信验证码 | POST | `/api/v1/content/auth/sms/send` | 发送短信验证码 |
| 发送邮箱验证邮件 | POST | `/api/v1/content/auth/email/send` | 发送邮箱验证邮件 |
| 刷新 Token | POST | `/api/v1/content/auth/token/refresh` | 使用 refresh_token 刷新 |
| 退出登录 | POST | `/api/v1/content/auth/logout` | 退出登录 |
| 获取图形验证码 | GET | `/api/v1/content/auth/captcha/image` | 获取图形验证码图片 |
| 校验图形验证码 | POST | `/api/v1/content/auth/captcha/verify` | 校验图形验证码 |

### 账号安全接口（/api/v1/content/account-security/*）

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 获取账号安全状态 | GET | `/api/v1/content/account-security/status` | 绑定状态、设备数等 |
| 绑定手机号 | POST | `/api/v1/content/account-security/bind/mobile` | 绑定手机号 |
| 绑定邮箱 | POST | `/api/v1/content/account-security/bind/email` | 绑定邮箱 |
| 绑定第三方账号 | POST | `/api/v1/content/account-security/bind/third-party` | 绑定第三方账号 |
| 换绑手机号 | POST | `/api/v1/content/account-security/rebind/mobile` | 换绑手机号 |
| 换绑邮箱 | POST | `/api/v1/content/account-security/rebind/email` | 换绑邮箱 |
| 解绑手机号 | POST | `/api/v1/content/account-security/unbind/mobile` | 解绑手机号 |
| 解绑邮箱 | POST | `/api/v1/content/account-security/unbind/email` | 解绑邮箱 |
| 解绑第三方账号 | POST | `/api/v1/content/account-security/unbind/third-party` | 解绑第三方账号 |
| 获取设备列表 | GET | `/api/v1/content/account-security/devices` | 当前用户活跃设备 |
| 下线设备 | POST | `/api/v1/content/account-security/devices/revoke` | 下线指定设备 |
| 信任设备 | POST | `/api/v1/content/account-security/devices/trust` | 标记信任设备 |
| 取消信任设备 | POST | `/api/v1/content/account-security/devices/untrust` | 取消信任状态 |
| 重置密码 | POST | `/api/v1/content/account-security/password/reset` | 手机号/邮箱重置密码 |
| 修改密码 | POST | `/api/v1/content/account-security/password/change` | 已登录状态修改密码 |
| 获取异常登录通知 | GET | `/api/v1/content/account-security/anomaly-notifications` | 异常登录通知列表 |
| 确认异常登录 | POST | `/api/v1/content/account-security/anomaly/confirm` | 确认本人操作 |
| 否认异常登录 | POST | `/api/v1/content/account-security/anomaly/deny` | 否认操作，下线设备 |
| 发送验证码（安全操作） | POST | `/api/v1/content/account-security/sms/send` | 安全操作场景验证码 |

### 账号注销接口（/api/v1/content/account-cancellation/*）

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 查询注销资格 | GET | `/api/v1/content/account-cancellation/eligibility` | 前置条件检查 |
| 申请注销 | POST | `/api/v1/content/account-cancellation/apply` | 申请账号注销 |
| 查询注销状态 | GET | `/api/v1/content/account-cancellation/status` | 注销状态和冷静期 |
| 取消注销 | POST | `/api/v1/content/account-cancellation/revoke` | 冷静期内取消 |

### 其他接口

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 提交兴趣偏好 | POST | `/api/v1/content/user/preferences/topics` | 注册后兴趣标签 |

## Decisions

### 决策 1：独立登录页 vs 复用平台登录页

**选择**: 独立登录页（`/content/login`、`/content/register`）

**理由**: 内容社区有独立认证体系（第三方登录、设备管理等），平台登录页无法满足需求。独立页面可独立迭代，不影响平台层。

**替代方案**: 复用平台登录页并扩展 — 改动范围大，耦合平台层，后续维护困难。

### 决策 2：状态管理方案

**选择**: 扩展 `useUserStore`，在现有 store 中新增认证相关状态字段

**理由**: 登录态（token）已由 `useUserStore` 管理，账号安全状态与用户状态强关联，拆分 store 会增加跨 store 同步复杂度。

**替代方案**: 新建 `useAuthStore` — 增加 store 间依赖，token 刷新逻辑需要跨 store 协调。

### 决策 3：路由方案

**选择**: 新增 `/content/*` 路由组，与现有 `/sys/*` 路由隔离

**理由**: 内容社区有独立的路由前缀和权限体系，隔离路由可独立控制认证守卫。

### 决策 4：表单方案

**选择**: 使用 Ant Design Vue Form 组件的配置式表单

**理由**: 项目已集成 Ant Design Vue 4，Form 组件支持校验、布局、响应式，无需引入额外表单库。

### 决策 5：第三方登录实现方案

**选择**: PC 端新开窗口跳转授权页，移动端跳转第三方 App/WebView，回调携带授权码调用后端

**理由**: 符合第三方 OAuth2 标准流程，前后端解耦。

### 决策 6：Token 刷新方案

**选择**: 复用现有 `defHttp` 拦截器机制，access_token 过期时自动使用 refresh_token 刷新

**理由**: 项目已有 token 刷新机制，只需确保新接口走统一流程。

### 决策 7：埋点方案

**选择**: 使用项目现有的 `useAnalytics` hook 或在 `defHttp` 拦截器中统一上报

**理由**: 复用现有基础设施，避免重复建设。具体实现在编码阶段确定。

### 决策 8：响应式策略

**选择**: PC 端左右分栏登录页 + 居中内容页，移动端全屏表单，弹窗改 Drawer

**理由**: 与现有平台风格一致，Ant Design Vue 的 Grid/Responsive 断点系统支持此方案。

## Risks / Trade-offs

- **[第三方登录兼容性]** 微信/Apple/Google 的 OAuth 流程在不同平台（PC/移动/微信浏览器）存在差异 → 统一使用授权码模式，各平台差异通过环境检测适配
- **[设备指纹采集]** `navigator.userAgent` 信息有限，可能无法精确区分设备 → 后端结合 IP + UA 综合判断，前端仅展示后端返回的设备信息
- **[邮箱验证流程]** 邮箱验证需跳转到邮箱客户端，回调链路长 → 验证链接通过浏览器打开，前端检测验证状态轮询或用户手动刷新
- **[注销冷静期状态同步]** 用户在冷静期内多端登录时状态需实时同步 → 进入首页时主动查询注销状态
- **[并发绑定冲突]** 多标签页同时操作同一绑定项 → 后端加锁，前端提示"操作冲突，请刷新"
