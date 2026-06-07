## ADDED Requirements

### Requirement: 冻结用户登录拦截

冻结用户登录时，后端 MUST 不签发 token，返回 403 + 冻结信息。前端 MUST 跳转到安全核验页面，不显示正常登录表单。

#### Scenario: 冻结用户登录
- **WHEN** 冻结用户输入账号密码登录
- **THEN** 后端返回 403 + userStatus=FROZEN + phone（脱敏），前端跳转到 /login/verify

#### Scenario: 拦截页无登录表单
- **WHEN** 用户在拦截页面
- **THEN** 不显示正常的登录表单

#### Scenario: 直接访问拦截页
- **WHEN** 用户直接访问 /login/blocked 或 /login/verify（无拦截数据）
- **THEN** 重定向到登录页

---

### Requirement: 封禁用户登录拦截

封禁用户登录时，后端 MUST 不签发 token，返回 403 + 封禁信息。前端 MUST 跳转到封禁提示页面，展示封禁原因、期限和"申诉"入口。

#### Scenario: 临时封禁用户登录
- **WHEN** 临时封禁用户登录
- **THEN** 显示"账号已被封禁，封禁期限：{endTime}，原因：{reason}"，提供"申诉"按钮（跳转 EPIC-08）

#### Scenario: 永久封禁用户登录
- **WHEN** 永久封禁用户登录（statusEndTime 为 null）
- **THEN** 显示"账号已被永久封禁，原因：{reason}"

---

### Requirement: 安全核验解冻

冻结用户 SHALL 能通过手机验证码完成安全核验，验证通过后账号恢复为"正常"状态。

#### Scenario: 发送验证码
- **WHEN** 用户点击"发送验证码"
- **THEN** 发送验证码，按钮显示 60 秒倒计时，倒计时期间按钮禁用

#### Scenario: 验证码发送频率限制
- **WHEN** 用户在 60 秒内再次点击"发送验证码"
- **THEN** 提示"请稍后再试"，按钮保持倒计时状态

#### Scenario: 验证码正确
- **WHEN** 用户输入正确验证码并提交
- **THEN** 后端恢复账号为"正常"状态，跳转到首页并提示"账号已恢复正常"

#### Scenario: 验证码错误
- **WHEN** 用户输入错误验证码
- **THEN** 提示"验证码错误，请重新输入"

#### Scenario: 验证码已过期
- **WHEN** 用户输入已过期的验证码（超过 5 分钟）
- **THEN** 提示"验证码已过期，请重新获取"

#### Scenario: 手机号格式校验
- **WHEN** 用户输入非 11 位手机号格式
- **THEN** 前端校验提示"请输入正确的手机号"，禁用发送按钮

#### Scenario: 核验接口网络异常
- **WHEN** 安全核验 API 请求网络异常
- **THEN** 提示"网络异常，请检查网络后重试"

---

### Requirement: 登录拦截页响应式布局

登录拦截页和安全核验页 MUST 支持移动端响应式布局。

#### Scenario: 移动端访问
- **WHEN** 在 <768px 宽度设备访问
- **THEN** 居中卡片布局，按钮全宽

---

## 后端 API 依赖

本需求依赖以下后端 API，**全部已实现**（2026-06-07 确认，UserStatusController.java）：

| API | 路径 | 用途 | 状态 |
|-----|------|------|------|
| sendVerifyCode | POST /api/v1/content/user-status/send-verify-code | 发送手机验证码 | ✅ 已实现 |
| verifySecurity | POST /api/v1/content/user-status/verify-security | 安全核验（验证码校验 + 状态恢复） | ✅ 已实现 |
