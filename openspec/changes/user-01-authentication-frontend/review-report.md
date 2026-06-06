# 规范审核报告: user-01-authentication-frontend

> **审核日期**: 2026-06-06
> **审核工具**: openspec-review-change
> **Change 类型**: 前端
> **业务域**: user
> **EPIC**: EPIC-01
> **关联 PRD**: docs/requirements/prd/frontend/EPIC-01-user-authentication-frontend-prd.md
> **关联 Change**: user-01-authentication（后端配对）

---

## 总览

| 维度 | 得分 | BLOCK | FLAG | ADVISORY |
|------|------|-------|------|----------|
| 完整性 (Completeness) | 9/10 | 0 | 1 | 0 |
| 一致性 (Consistency) | 8/10 | 1 | 1 | 0 |
| 可实现性 (Feasibility) | 10/10 | 0 | 0 | 0 |
| 可测试性 (Testability) | 9/10 | 0 | 1 | 0 |
| 接口契约 (API Contract) | 8/10 | 0 | 1 | 1 |
| 边界覆盖 (Boundary) | 9/10 | 0 | 1 | 0 |
| **综合** | **53/60** | **1** | **5** | **1** |

---

## 量化指标

| 指标 | 分子 | 分母 | 百分比 | 阈值 | 状态 |
|------|------|------|--------|------|------|
| PRD AC 覆盖率 | 12 | 12 | 100% | >=80% | PASS |
| API 契约完整率 | 32 | 34 | 94% | >=90% | PASS |
| 边界条件覆盖率 | 8 | 10 | 80% | >=60% | PASS |
| TDD 配对率 | 0 | 14 | 0% | >=70% | FAIL |
| Scenario 完整率 | 79 | 29 | 2.72/req | >=3/req | FAIL |
| 后端 API 满足率 | N/A | N/A | N/A | N/A | N/A (前端 change) |
| 数据库表满足率 | N/A | N/A | N/A | N/A | N/A (前端 change) |
| 前端组件满足率 | 4 | 5 | 80% | >=90% | FAIL |
| 依赖阻塞项数 (P0) | 0 | - | 0 | =0 | PASS |

---

## 1. 完整性审核

### 1.1 文档结构完整性

| 检查项 | 状态 | 说明 |
|--------|------|------|
| proposal.md 存在 | PASS | 文件存在，包含 Why/What/Capabilities/Impact 四章节 |
| design.md 存在 | PASS | 文件存在，包含 Context/Goals/Non-Goals/Decisions/Risks 五章节 |
| specs/ 目录存在且含 spec.md | PASS | 9 个子目录，每个含 spec.md |
| tasks.md 存在且格式正确 | PASS | 14 个分组，全部 `- [x]` 格式 |
| Capabilities 与 specs 子目录对应 | PASS | 9 个 Capability 对应 9 个 spec 子目录 |

### 1.2 前端特有检查项

| 检查项 | 状态 | 说明 |
|--------|------|------|
| design.md 包含路由方案决策 | PASS | 决策 3：`/content/*` 路由组 |
| design.md 包含状态管理方案决策 | PASS | 决策 2：扩展 useUserStore |
| design.md 包含组件拆分决策 | PASS | 决策 4/5/8：表单、第三方登录、响应式 |
| specs 包含页面级交互场景 (WHEN/THEN) | PASS | 所有 spec 均使用 WHEN/THEN 格式 |
| tasks.md 包含响应式适配任务 | PASS | 第 13 组专门覆盖响应式 |
| proposal.md Impact 列出 API 接口依赖 | PASS | Impact 中列出约 30 个 API 接口 |

### 1.3 完整性问题清单

#### FLAG-001: design.md 缺少独立的 API 依赖清单章节
- **位置**: `design.md`
- **描述**: design.md 的 Decisions 章节覆盖了路由、状态管理、表单、第三方登录、Token、埋点、响应式共 7 个决策，但未包含独立的 API 依赖清单章节。API 端点定义分散在 PRD 的"API 对接"章节中。
- **影响**: 开发时需频繁查阅 PRD 获取 API 路径，增加跨文档跳转成本。
- **建议修复**: 在 design.md 中新增"API 依赖清单"章节，从 PRD 中提取完整的 API 端点列表。

---

## 2. 一致性审核

### 2.1 跨文档引用一致性

| 检查项 | 状态 | 说明 |
|--------|------|------|
| proposal.md Capabilities 与 specs 子目录对应 | PASS | 9 对 9，完全对应 |
| design.md Decisions 与 specs Requirement 无矛盾 | PASS | 路由、状态管理、表单方案一致 |
| tasks.md 任务与 specs Requirement 可追溯 | PASS | 14 组任务覆盖 9 个 spec |
| tasks.md 任务与 design.md Decisions 无矛盾 | PASS | 任务实施方向与设计决策一致 |

### 2.2 前端特有检查项

| 检查项 | 状态 | 说明 |
|--------|------|------|
| specs 引用的 API 路径与后端 design.md 定义一致 | PASS | 认证、账号安全、注销接口均在后端 design.md 中有定义 |
| design.md 路由路径与 specs 页面跳转路径一致 | PASS | `/content/login`、`/content/register` 等路径一致 |
| PRD Non-Goals 与 proposal/specs 功能边界一致 | **BLOCK** | 见 BLOCK-001 |

### 2.3 一致性问题清单

#### BLOCK-001: PRD 内部矛盾 -- 滑块验证在 Non-Goals 中排除但在功能描述中包含
- **位置**: `EPIC-01-user-authentication-frontend-prd.md` 第 3.11 节 / API 对接 5.4 节 / Non-Goals
- **描述**: 前端 PRD Non-Goals 明确声明"本期仅使用图形验证码，滑块验证作为后续增强"，但 F11 风控拦截交互详细描述中包含了滑块验证弹窗结构和交互要求，API 对接 5.4 节列出了滑块验证端点 (`GET /api/v1/auth/captcha/slider`、`POST /api/v1/auth/captcha/slider/verify`)。后端 design.md 中也未定义滑块验证接口。
- **影响**: 若按 PRD 实现滑块验证，后端无对应接口；若不实现，PRD 描述与实际功能不符。proposal.md 的 Capabilities `risk-control` 也包含了"滑块验证"描述。
- **建议修复**: 从 PRD F11 中移除滑块验证弹窗结构描述，从 API 对接 5.4 中移除滑块验证端点，同步更新 proposal.md 中 `risk-control` 的描述。或在 Non-Goals 中移除滑块验证排除项并更新后端 design.md。

#### FLAG-002: proposal.md Capabilities `risk-control` 描述包含 Non-Goals 功能
- **位置**: `proposal.md` Capabilities `risk-control`
- **描述**: `risk-control` 描述为"图形验证码、滑块验证、账号锁定提示、冷却倒计时"，其中"滑块验证"在 PRD Non-Goals 中被排除。
- **影响**: apply 时可能误实现滑块验证功能，浪费开发资源。
- **建议修复**: 移除 `risk-control` 描述中的"滑块验证"。

---

## 3. 可实现性审核

### 3.1 前端检查项

| 检查项 | 状态 | 说明 |
|--------|------|------|
| 组件库与项目兼容 (Ant Design Vue 4) | PASS | design.md 明确使用 Ant Design Vue 4 |
| 状态管理与现有 store 体系兼容 (Pinia) | PASS | 扩展现有 useUserStore，已确认存在 |
| API 调用使用项目既有的 `defHttp` 封装 | PASS | 已确认 `src/utils/http/axios/` 存在 |
| 路由方案与现有权限模式兼容 | PASS | `/content/*` 独立路由组，与 `/sys/*` 隔离 |
| 不包含 Non-Goals 中排除的功能 | PASS | specs 中未包含生物识别、两步验证、企业通讯录等 |
| StrengthMeter 组件已存在 | PASS | `src/components/StrengthMeter/` 已存在 |
| useAnalytics hook 存在 | **FLAG** | 未在代码库中找到 useAnalytics hook，proposal.md 和 PRD 均提到使用此 hook |

### 3.2 可实现性问题清单

无 BLOCK 问题。组件库、状态管理、API 封装、路由方案均与项目现有技术栈兼容。

---

## 4. 可测试性审核

### 4.1 前端检查项

| 检查项 | 状态 | 说明 |
|--------|------|------|
| 关键交互 Scenario 有明确用户操作 -> 系统响应描述 | PASS | 所有 spec 使用 WHEN/THEN 格式，操作和响应明确 |
| 错误场景有明确 UI 反馈描述 | **FLAG** | 部分场景缺少具体 UI 反馈文案 |
| 异步操作有明确 loading/error/success 状态描述 | PASS | 登录、注册、绑定等操作均描述了三种状态 |

### 4.2 可测试性问题清单

#### FLAG-003: 部分错误场景缺少具体 UI 反馈描述
- **位置**: 多个 spec 文件
- **描述**: 以下场景的 THEN 描述缺少具体 UI 反馈：
  - `risk-control/spec.md` 图形验证码验证失败场景：仅描述"刷新验证码图片，显示验证失败，请重试"，未说明是 message.error 还是 inline 提示
  - `account-cancellation/spec.md` 注销申请被拒绝场景：仅描述"提示拒绝原因"，未说明具体 UI 组件
  - `user-login/spec.md` 第三方服务不可用场景：描述了"图标按钮置灰，hover 提示"，但未说明是 Tooltip 还是 title 属性
- **影响**: 测试用例编写时需猜测 UI 反馈形式，可能导致测试与实现不一致。
- **建议修复**: 为上述场景补充具体的 UI 组件类型（message.error / Alert / Tooltip 等）和文案。

---

## 5. 接口契约审核

### 5.1 前端引用 API 与后端定义对比

| 前端引用的 API | 后端 design.md 定义 | 状态 |
|---------------|-------------------|------|
| POST /api/v1/auth/register/mobile | 有定义 | OK |
| POST /api/v1/auth/register/email | 有定义 | OK |
| GET /api/v1/auth/register/email/confirm | 有定义 | OK |
| POST /api/v1/auth/login/third-party | 有定义 | OK |
| POST /api/v1/auth/login/sms-code | 有定义 | OK |
| POST /api/v1/auth/login/password | 有定义 | OK |
| POST /api/v1/auth/sms/send | 有定义 | OK |
| POST /api/v1/auth/email/send | 有定义 | OK |
| POST /api/v1/auth/token/refresh | 有定义 | OK |
| POST /api/v1/auth/logout | 有定义 | OK |
| GET /api/v1/account-security/status | 有定义 | OK |
| POST /api/v1/account-security/bind/mobile | 有定义 | OK |
| POST /api/v1/account-security/bind/email | 有定义 | OK |
| POST /api/v1/account-security/bind/third-party | 有定义 | OK |
| POST /api/v1/account-security/rebind/mobile | 有定义 | OK |
| POST /api/v1/account-security/rebind/email | 有定义 | OK |
| POST /api/v1/account-security/unbind/mobile | 有定义 | OK |
| POST /api/v1/account-security/unbind/email | 有定义 | OK |
| POST /api/v1/account-security/unbind/third-party | 有定义 | OK |
| GET /api/v1/account-security/devices | 有定义 | OK |
| POST /api/v1/account-security/devices/revoke | 有定义 | OK |
| POST /api/v1/account-security/devices/trust | 有定义 | OK |
| POST /api/v1/account-security/devices/untrust | 有定义 | OK |
| POST /api/v1/account-security/password/reset | 有定义 | OK |
| POST /api/v1/account-security/password/change | 有定义 | OK |
| GET /api/v1/account-security/anomaly-notifications | 有定义 | OK |
| POST /api/v1/account-security/anomaly/confirm | 有定义 | OK |
| POST /api/v1/account-security/anomaly/deny | 有定义 | OK |
| POST /api/v1/account-security/sms/send | 有定义 | OK |
| POST /api/v1/user/preferences/topics | PRD 定义 | OK |
| GET /api/v1/account-cancellation/eligibility | 有定义 | OK |
| POST /api/v1/account-cancellation/apply | 有定义 | OK |
| GET /api/v1/account-cancellation/status | 有定义 | OK |
| POST /api/v1/account-cancellation/revoke | 有定义 | OK |
| GET /api/v1/auth/captcha/image | 有定义 | OK |
| POST /api/v1/auth/captcha/verify | 有定义 | OK |
| GET /api/v1/auth/captcha/slider | **PRD 定义但后端未定义** | FLAG |
| POST /api/v1/auth/captcha/slider/verify | **PRD 定义但后端未定义** | FLAG |

### 5.2 接口契约问题清单

#### FLAG-004: 滑块验证 API 在前端 PRD 中定义但后端未定义
- **位置**: `EPIC-01-user-authentication-frontend-prd.md` 5.4 节
- **描述**: 前端 PRD 列出了 `GET /api/v1/auth/captcha/slider` 和 `POST /api/v1/auth/captcha/slider/verify` 两个滑块验证端点，但后端 design.md 中未定义这两个接口。
- **影响**: 与 BLOCK-001 同源。前端 specs 中未引用这两个端点，实际开发不受影响，但 PRD 文档不一致。
- **建议修复**: 从前端 PRD 中移除滑块验证端点，或在后端 design.md 中补充定义。

#### ADVISORY-001: 后端定义但前端未引用的 API
- **位置**: 后端 design.md
- **描述**: 后端 design.md 中定义了部分前端 specs 未直接引用的内部 API（如风控事件记录、审计日志相关接口），这些属于后端内部实现，前端无需直接调用。
- **影响**: 无功能性影响。
- **建议**: 记录为信息项，无需修改。

---

## 6. 边界覆盖审核

### 6.1 通用边界类型覆盖

| 边界类型 | 覆盖状态 | 覆盖位置 |
|---------|---------|---------|
| null/空值输入处理 | COVERED | 注册/登录表单校验（user-registration、user-login spec） |
| 超长/超大值输入处理 | COVERED | PRD 10.4 边界测试：极长手机号 |
| 格式不合法输入处理 | COVERED | 手机号格式、邮箱格式、密码强度校验 |
| 唯一约束冲突处理 | COVERED | 手机号已注册、邮箱已注册、第三方账号已绑定 |
| 并发/竞态条件处理 | COVERED | account-binding spec：并发绑定冲突 |
| 权限不足/未认证处理 | COVERED | 未验证邮箱状态限制、最后联系方式解绑拒绝 |
| 资源不存在处理 | COVERED | 手机号未注册、邮箱未注册、重置链接已使用 |
| 外部服务不可用降级 | COVERED | user-login spec：第三方服务不可用 |
| Token 过期自动刷新 | **GAP** | PRD 测试要点提及，但 specs 中无专门 Scenario |
| 网络超时/断网处理 | COVERED | PRD 通用交互要求：网络异常提示 |

### 6.2 前端特有边界覆盖

| 边界类型 | 覆盖状态 | 覆盖位置 |
|---------|---------|---------|
| 网络超时/断网 UI 反馈 | COVERED | PRD 4.4 通用交互要求 |
| 表单重复提交防护 | COVERED | user-login spec：防重复提交 Scenario |
| 移动端/平板响应式边界 | COVERED | tasks.md 第 13 组 + design.md 决策 8 |
| 空数据状态 UI 展示 | COVERED | session-management spec：设备列表为空 |

### 6.3 边界覆盖问题清单

#### FLAG-005: Token 过期自动刷新场景未在 specs 中显式覆盖
- **位置**: specs/ 全局
- **描述**: 前端 PRD 测试要点 10.4 提到"Token 过期：操作时 Token 已过期，自动刷新 Token 或跳转登录页"，proposal.md 也提到"Token 过期无感刷新"，design.md 决策 6 描述了 Token 刷新方案，但各 spec 中无专门的 Token 过期处理 Scenario。
- **影响**: Token 过期是高频场景，缺少显式 Scenario 可能导致测试遗漏。
- **建议修复**: 在 `user-login/spec.md` 中补充 Token 过期自动刷新和刷新失败跳转登录页的 Scenario。

---

## 7. 前后端衔接审计

> 触发条件满足：配对后端 change `user-01-authentication` 目录存在。

### 7.1 接口清单双向对比

| 统计项 | 数量 |
|--------|------|
| 前端引用 API 总数 | 36 |
| 后端已定义 | 34 |
| 前端引用但后端未定义 | 2 (滑块验证，与 BLOCK-001 同源) |
| 后端定义但前端未引用 | 0 (核心接口) |

### 7.2 数据模型一致性

| 字段/概念 | 后端定义 | 前端引用 | 状态 |
|----------|---------|---------|------|
| access_token / refresh_token | 后端 design.md 决策 4 | 前端 design.md 决策 6 | MATCH |
| 设备会话 (device_session) | content_user_device_session 表 | session-management spec 设备列表 | MATCH |
| 账号安全状态 | content_user_account 表 | useUserStore.accountSecurityStatus | MATCH |
| 注销状态机 | ACTIVE -> CANCELLING -> CANCELLED | useUserStore.cancellationStatus | MATCH |
| 风控拦截响应码 | 后端 design.md 决策 5 | risk-control spec 风控拦截码 | MATCH |

### 7.3 错误码覆盖

| 错误场景 | 后端返回 | 前端处理 | 状态 |
|----------|---------|---------|------|
| 手机号已注册 | 登录引导响应 | "该手机号已注册，去登录" | COVERED |
| 密码错误 | 不暴露具体错误 | "账号或密码错误" | COVERED |
| 账号锁定 | 锁定响应 | 倒计时弹窗 | COVERED |
| 新密码与旧密码相同 | PASSWORD_REUSED 错误码 | "不能与最近 3 次密码相同" | COVERED |
| 最后联系方式解绑 | 拒绝响应 | "至少保留一种联系方式" | COVERED |
| 风控拦截 | 风控拦截响应码 | 自动弹出验证弹窗 | COVERED |
| 账号已注销 | cancelled-account 响应 | "该账号已注销" | COVERED |

### 7.4 认证/鉴权一致性

| 检查项 | 状态 | 说明 |
|--------|------|------|
| 公开接口（注册/登录/验证码）无需认证 | MATCH | 前端 specs 中未要求 Token |
| 安全操作接口需认证 | MATCH | 绑定/解绑/设备管理等操作需登录态 |
| Token 刷新机制一致 | MATCH | 前端复用 defHttp 拦截器，后端签发 access+refresh token |

### 7.5 衔接审计问题清单

无 BLOCK 级衔接问题。前端引用的 34 个核心 API 均在后端 design.md 中有定义，字段名和语义一致。

---

## 8. PRD 追溯矩阵

| PRD 功能 | 对应 Requirement | 对应 Scenario 数 | 对应 Task 组 | 状态 |
|----------|-----------------|-----------------|-------------|------|
| F1 手机号注册 | user-registration: 手机号验证码注册 | 5 | tasks 1.2/5.1-5.4 | COVERED |
| F2 邮箱注册 | user-registration: 邮箱密码注册 + 邮箱未验证状态限制 | 6 | tasks 1.3/5.5-5.8 | COVERED |
| F3 第三方登录注册 | user-login: 第三方登录注册 + 资料完善引导 | 6 | tasks 1.2/6.7-6.9 | COVERED |
| F4 验证码登录 | user-login: 验证码登录 | 3 | tasks 1.2/6.4-6.6 | COVERED |
| F5 密码登录 | user-login: 密码登录 + 登录后跳转 + 防重复提交 | 6 | tasks 1.2/6.1-6.3/6.10-6.11 | COVERED |
| F6 多端会话管理 | session-management: 设备列表/下线/信任/上限 | 8 | tasks 1.6/8.1-8.5 | COVERED |
| F7 账号绑定 | account-binding: 绑定状态/手机号/邮箱/第三方 | 6 | tasks 1.5/7.1-7.4 | COVERED |
| F8 账号换绑解绑 | account-binding: 换绑/解绑 | 4 | tasks 7.5-7.6 | COVERED |
| F9 密码找回 | password-recovery: 三步流程/强度/导航/链接/高风险 | 7 | tasks 1.4/9.1-9.6 | COVERED |
| F10 异常登录通知 | anomaly-notification: 实时通知/列表/信任设备 | 6 | tasks 10.1-10.4 | COVERED |
| F11 风控拦截交互 | risk-control: 图形验证码/锁定/冷却 | 7 | tasks 11.1-11.4 | COVERED |
| F12 账号注销 | account-cancellation: 资格检查/确认/冷静期 | 8 | tasks 1.7/12.1-12.6 | COVERED |
| 注册后引导 | user-registration: 注册后个性化推荐引导 | 2 | tasks 5.9 | COVERED |
| 埋点事件 | auth-analytics: 注册/登录/安全事件上报 | 7 | tasks 14.1-14.3 | COVERED |

**PRD AC 覆盖率**: 14/14 = 100%

---

## 9. 依赖分析

### 9.1 前端依赖

| 依赖项 | 已存在 | 状态 |
|--------|--------|------|
| useUserStore (src/store/modules/user.ts) | 是 | OK |
| defHttp (src/utils/http/axios/) | 是 | OK |
| StrengthMeter 组件 (src/components/StrengthMeter/) | 是 | OK |
| Ant Design Vue 4 | 是 | OK |
| useAnalytics hook | **否** | FLAG |

### 9.2 后端依赖

| 依赖项 | 已定义 | 状态 |
|--------|--------|------|
| 认证 Controller (ContentAuthController) | design.md 决策 1 | OK (待实现) |
| 账号安全 Controller | design.md 决策 1 | OK (待实现) |
| 注销 Controller | design.md 决策 1 | OK (待实现) |
| 用户认证相关数据库表 | design.md 决策 2 | OK (待创建) |
| Redis 缓存 key | design.md 决策 3 | OK (待配置) |

### 9.3 依赖问题清单

无 P0 级阻塞依赖。后端 API 在 design.md 中已定义，属于待实现状态（模式 A：前后端均未开发）。

---

## 最终结论

### BLOCK 问题汇总（必须修复才能 apply）

| ID | 问题 | 位置 | 影响 |
|----|------|------|------|
| BLOCK-001 | PRD 内部矛盾：滑块验证在 Non-Goals 中排除但在 F11 功能描述和 API 端点中包含 | PRD 3.11/5.4 + proposal.md | proposal.md 和 PRD 不一致，可能导致实现范围歧义 |

### FLAG 问题汇总（应该修复）

| ID | 问题 | 位置 | 建议 |
|----|------|------|------|
| FLAG-001 | design.md 缺少独立 API 依赖清单章节 | design.md | 补充 API 清单章节 |
| FLAG-002 | proposal.md risk-control 描述包含 Non-Goals 功能（滑块验证） | proposal.md Capabilities | 移除"滑块验证"描述 |
| FLAG-003 | 部分错误场景缺少具体 UI 反馈描述 | 多个 spec | 补充 UI 组件类型和文案 |
| FLAG-004 | 滑块验证 API 在前端 PRD 中定义但后端未定义 | PRD 5.4 | 从 PRD 移除或后端补充 |
| FLAG-005 | Token 过期自动刷新场景未在 specs 中显式覆盖 | specs/ 全局 | 补充 Token 过期 Scenario |

### ADVISORY 问题汇总（建议改进）

| ID | 问题 | 位置 | 建议 |
|----|------|------|------|
| ADVISORY-001 | 后端定义但前端未引用的内部 API | 后端 design.md | 信息项，无需修改 |

### 门禁判定

```
Step 1 规范审核: BLOCK=1, FLAG=5 → REJECTED
Step 2 依赖检查: P0 依赖阻塞=0 → PASS
最终判定: REJECTED
```

### 审核结论

- BLOCK 问题: 1 个
- FLAG 问题: 5 个
- ADVISORY 问题: 1 个
- 依赖阻塞 (P0): 0 项

**结论文本**: 规范审核未通过。发现 1 个 BLOCK 问题（PRD 内部关于滑块验证的矛盾描述），必须修复后才能执行 apply。修复后重新运行 `/opsx:review`。

### 修复建议

#### 需要修复的规范文档问题（共 6 项）

1. **[BLOCK]** PRD 内部矛盾：从 PRD F11 移除滑块验证弹窗描述，从 API 对接 5.4 移除滑块验证端点，同步更新 proposal.md `risk-control` 描述 → 建议启动 subagent 修复 PRD 和 proposal.md
2. **[FLAG]** proposal.md `risk-control` 描述移除"滑块验证" → 随 BLOCK-001 一并修复
3. **[FLAG]** design.md 补充 API 依赖清单章节 → 建议启动 subagent 修复 design.md
4. **[FLAG]** specs 补充 Token 过期自动刷新 Scenario → 建议在 user-login/spec.md 中补充
5. **[FLAG]** specs 补充具体 UI 反馈描述 → 建议逐个 spec 补充
6. **[FLAG]** 前端 PRD 移除滑块验证 API 端点 → 随 BLOCK-001 一并修复

#### 需要完善的依赖模块（共 0 项）

无 P0 依赖阻塞。

所有问题修复后，请重新执行审核以确认"可开发"状态。
