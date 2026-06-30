# FixPlan: user-01-authentication & user-01-authentication-frontend

> 生成时间: 2026-06-30
> ChangePair: user-01-authentication (后端) + user-01-authentication-frontend (前端)
> 审核文档来源: review-report, drift-report, verify-report, gap-analysis, login-api-analysis, system-module-gap-analysis

---

## 元数据摘要

| 统计项 | 数量 |
|--------|------|
| 总 FixItem 数 | 28 |
| BLOCK 级别 | 8 |
| CRITICAL 级别 | 4 |
| FLAG 级别 | 12 |
| 文档修复项 | 4 |

### BLOCK/CRITICAL 项简述

1. **[BLOCK-CODE-01]** ContentUserAccount.riskLevel 字段类型 String 与 SQL INT 不匹配 — 运行时 MyBatis 类型映射错误
2. **[BLOCK-CODE-02]** ContentRiskEvent.riskLevel 字段类型 String 与 SQL INT 不匹配 — 同上
3. **[BLOCK-CODE-03]** ContentAuthBizServiceImpl 直接注入 7 个 Mapper，违反 Controller→Biz→Service→Mapper 分层架构
4. **[BLOCK-CODE-04]** ContentAccountCancellationBizServiceImpl 直接注入 2 个 Mapper，分层违规
5. **[BLOCK-CODE-05]** 缺少冷静期到期自动注销的 @Scheduled 定时任务，账号永远停留在 CANCELLING 状态
6. **[BLOCK-CODE-06]** 后端认证 API 路径前缀错误：使用 `/api/v1/content/auth/` 应改为 `/api/v1/content/auth/`（用户明确要求，不经过 /content/ 前缀）
7. **[CRITICAL-CODE-01]** 第三方登录接口 loginByThirdParty 使用 @RequestParam 接收参数而非 @RequestBody，rawJson 等长参数有 URL 长度限制风险
8. **[CRITICAL-CODE-02]** 邮箱注册 registerByEmail 未发送验证邮件，邮箱验证流程无法走通
9. **[BLOCK-DOC-01]** plan.md 文件完全缺失
10. **[BLOCK-DOC-02]** design.md 第三方绑定表名不一致（social_binding vs third_party_auth）

---

## FixItem 列表

### 一、BLOCK 级别 — 代码修复（必须修复）

---

#### FIX-001: ContentUserAccount.riskLevel 类型不匹配修复

- **id**: BLOCK-CODE-01
- **来源**: review-report:BLOCK-03, drift-report:字段漂移, verify-report:CRIT-01
- **位置**: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/auth/entity/ContentUserAccount.java`
- **优先级**: BLOCK
- **依赖项**: 无
- **类型**: 代码修复
- **修复步骤**:
  1. 将 `private String riskLevel;` 改为 `private Integer riskLevel;`
  2. 确认对应的 getter/setter 类型同步修改
  3. 检查所有引用 riskLevel 的地方（BizService、风控逻辑）确保类型一致
  4. 确认 SQL 中 `risk_level INT NOT NULL DEFAULT 0` 保持不变（0-100 分值）
- **验证标准**:
  - 实体字段类型为 Integer
  - 编译通过，无类型转换错误
  - MyBatis 查询/插入 riskLevel 字段正常

---

#### FIX-002: ContentRiskEvent.riskLevel 类型不匹配修复

- **id**: BLOCK-CODE-02
- **来源**: drift-report:字段漂移, verify-report:CRIT-01
- **位置**: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/auth/entity/ContentRiskEvent.java`
- **优先级**: BLOCK
- **依赖项**: FIX-001
- **类型**: 代码修复
- **修复步骤**:
  1. 将 `private String riskLevel;` 改为 `private Integer riskLevel;`
  2. 确认对应的 getter/setter 类型同步修改
  3. 检查引用处类型一致性
- **验证标准**:
  - 实体字段类型为 Integer
  - 编译通过

---

#### FIX-003: ContentAuthBizServiceImpl 分层架构违规修复（移除直接 Mapper 注入）

- **id**: BLOCK-CODE-03
- **来源**: drift-report:架构分层审核, verify-report:CRIT-03
- **位置**: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/auth/biz/ContentAuthBizServiceImpl.java` (第 65-107 行)
- **优先级**: BLOCK
- **依赖项**: 无
- **类型**: 代码重构
- **问题描述**: Biz 层直接注入 7 个 Mapper（credentialMapper, accountMapper, profileMapper, notificationSettingMapper, thirdPartyAuthMapper, deviceSessionMapper, passwordHistoryMapper），跳过 Service 层封装
- **修复步骤**:
  1. 确认每个 Mapper 对应的 Service 接口是否已存在：
     - ContentUserCredentialMapper → IContentUserCredentialService
     - ContentUserAccountMapper → IContentUserAccountService
     - ContentUserProfileMapper → IContentUserProfileService
     - ContentUserNotificationSettingMapper → IContentUserNotificationSettingService
     - ContentUserThirdPartyAuthMapper → IContentUserThirdPartyAuthService
     - ContentUserDeviceSessionMapper → IContentDeviceSessionService
     - ContentUserPasswordHistoryMapper → IContentUserPasswordHistoryService
  2. 若 Service 接口/实现不存在，先创建（遵循 IService/ServiceImpl 模式）
  3. 将 BizServiceImpl 中的 Mapper 注入替换为对应 Service 接口注入
  4. 将所有 mapper.select/update/insert 调用替换为 service 方法调用
  5. 确保事务边界在 Service 层或 Biz 层正确声明
- **验证标准**:
  - ContentAuthBizServiceImpl 中无任何 Mapper 直接注入（@Resource Mapper 数量为 0）
  - 所有数据访问通过 Service 接口
  - 原有业务逻辑不变
  - 模块全量测试通过

---

#### FIX-004: ContentAccountCancellationBizServiceImpl 分层架构违规修复

- **id**: BLOCK-CODE-04
- **来源**: drift-report:架构分层审核
- **位置**: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/auth/biz/ContentAccountCancellationBizServiceImpl.java` (第 49-52 行)
- **优先级**: BLOCK
- **依赖项**: FIX-003
- **类型**: 代码重构
- **问题描述**: Biz 层直接注入 cancellationRequestMapper、accountMapper 两个 Mapper
- **修复步骤**:
  1. 将 cancellationRequestMapper 替换为 IContentCancellationRequestService
  2. 将 accountMapper 替换为 IContentUserAccountService
  3. 替换所有 mapper 调用为 service 方法调用
- **验证标准**:
  - ContentAccountCancellationBizServiceImpl 中无 Mapper 直接注入
  - 编译通过，注销流程功能正常

---

#### FIX-005: 添加冷静期到期自动注销 @Scheduled 定时任务

- **id**: BLOCK-CODE-05
- **来源**: verify-report:CRIT-02, drift-report:负向漂移清单
- **位置**: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/auth/biz/ContentAccountCancellationBizServiceImpl.java`（或新建配置类）
- **优先级**: BLOCK
- **依赖项**: FIX-004
- **类型**: 代码修复（功能缺失）
- **问题描述**: completeCancellation 方法存在但无 @Scheduled 定时任务调用，冷静期到期后账号不会自动注销
- **修复步骤**:
  1. 在 ContentAccountCancellationBizServiceImpl 或新建的 CancellationScheduledTask 中添加定时任务方法
  2. 使用 @Scheduled(cron = "0 0 * * * ?") 每小时执行一次
  3. 查询 content_cancellation_request 表中 status='PENDING' 且 cooldown_deadline <= NOW() 的记录
  4. 对每条到期记录调用 completeCancellation 方法
  5. 添加 @EnableScheduling 确保定时任务启用（若未全局启用）
  6. 考虑分布式场景：使用分布式锁或乐观锁防止多实例重复执行
- **验证标准**:
  - 存在 @Scheduled 注解的定时任务方法
  - 能扫描到 cooldown_deadline 到期的注销申请
  - 到期后账号状态从 CANCELLING 变为 CANCELLED
  - 多实例部署时不会重复执行

---

#### FIX-006: 后端认证 API 路径前缀统一为 /api/v1/content/auth/

- **id**: BLOCK-CODE-06
- **来源**: 用户明确指令（内容社区认证API路径统一使用 /api/v1/content/auth/ 前缀，不经过 /content/ 前缀）
- **位置**: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/auth/controller/ContentAuthController.java` (第 27 行)
- **优先级**: BLOCK
- **依赖项**: FIX-012（前端对应路径修改）
- **类型**: 代码修复
- **问题描述**: 当前 @RequestMapping("/api/v1/content/auth")，用户明确要求认证 API 使用 `/api/v1/content/auth/` 前缀，去掉 `/content/` 段
- **修复步骤**:
  1. 将 ContentAuthController 类级别 @RequestMapping 从 "/api/v1/content/auth" 改为 "/api/v1/content/auth"
  2. 确认所有子路径（/login/*, /register/*, /bind/*, /unbind/*, /rebind/*, /token/*, /captcha/*, /sms/*, /email/*, /logout, /devices/*, /confirm-email, /reset-password/*, /password/*）保持不变
  3. 检查 Spring Security 配置（SecurityConfig）中的 permitAll 路径是否需要同步更新
  4. 确认网关路由（若有）是否需要调整
- **验证标准**:
  - ContentAuthController @RequestMapping 为 "/api/v1/content/auth"
  - 所有认证接口通过 /api/v1/content/auth/* 可访问
  - SecurityConfig 白名单路径同步更新
  - 无静态资源或其他路径冲突

---

### 二、CRITICAL 级别 — 代码修复

---

#### FIX-007: 第三方登录接口参数改用 @RequestBody 接收

- **id**: CRITICAL-CODE-01
- **来源**: drift-report:WARNING-第三方登录参数, verify-report:WARN-05
- **位置**: `jeecg-boot/.../auth/controller/ContentAuthController.java` (第 101-111 行)
- **优先级**: CRITICAL
- **依赖项**: FIX-006
- **类型**: 代码修复
- **问题描述**: loginByThirdParty 使用多个 @RequestParam 接收参数（provider, openId, unionId, nickname, avatar, rawJson），POST 参数暴露在 URL 中，rawJson 可能超出 URL 长度限制
- **修复步骤**:
  1. 创建 ContentAuthThirdPartyLoginReq DTO 类（在 req 包下），包含字段：
     - String provider (NotBlank)
     - String openId (NotBlank)
     - String unionId
     - String nickname
     - String avatar
     - String rawJson
  2. 将 Controller 方法参数改为 `@Valid @RequestBody ContentAuthThirdPartyLoginReq req`
  3. 方法体内从 req 对象获取参数
  4. 前端对应调用处同步调整为 JSON body 传递
- **验证标准**:
  - 存在 ContentAuthThirdPartyLoginReq 类
  - loginByThirdParty 使用 @RequestBody 接收参数
  - 前端 API 调用同步修改为 POST JSON body

---

#### FIX-008: 邮箱注册成功后发送验证邮件

- **id**: CRITICAL-CODE-02
- **来源**: drift-report:负向漂移, verify-report:WARN-04
- **位置**: `jeecg-boot/.../auth/biz/ContentAuthBizServiceImpl.java` (registerByEmail 方法)
- **优先级**: CRITICAL
- **依赖项**: FIX-003
- **类型**: 代码修复（功能缺失）
- **问题描述**: registerByEmail 方法创建用户后直接返回，未调用 emailSenderPort 发送验证链接邮件，confirmEmail 接口无法被触发
- **修复步骤**:
  1. 在 registerByEmail 事务提交后，生成邮箱验证 token（建议使用 JWT 或随机 UUID，存储到 Redis 设置 TTL，如 24 小时）
  2. 构建验证链接：`/api/v1/content/auth/confirm-email?token={token}`（路径与 FIX-006 保持一致）
  3. 调用 emailSenderPort 发送验证邮件，包含验证链接
  4. 确认邮件模板内容正确（包含跳转链接、过期提示）
  5. confirmEmail 方法从 Redis 验证 token 有效性后标记邮箱为已验证
- **验证标准**:
  - registerByEmail 成功后发送验证邮件
  - 邮箱验证 token 存储在 Redis 并有合理 TTL
  - 点击邮件中的 confirm-email 链接能成功验证邮箱
  - token 一次性使用或过期后失效

---

#### FIX-009: 手机号注册返回类型改为 AuthLoginResult（注册后自动登录）

- **id**: CRITICAL-CODE-03
- **来源**: verify-report:WARN-06
- **位置**: `jeecg-boot/.../auth/controller/ContentAuthController.java` (第 38 行 registerByMobile)
- **优先级**: CRITICAL
- **依赖项**: 无
- **类型**: 代码修复
- **问题描述**: registerByMobile 返回 Result<String>(userId)，但 spec 要求注册成功后"issues a login session, and returns the authenticated user summary"，前端需额外调用登录接口造成体验断裂
- **修复步骤**:
  1. 将 registerByMobile 返回类型从 Result<String> 改为 Result<AuthLoginResult>
  2. 注册成功后，调用与登录相同的逻辑生成 accessToken 和 refreshToken
  3. 返回 AuthLoginResult（包含 token、用户信息等）
  4. registerByEmail 同理处理（但邮箱未验证，返回的 token 权限应受限或标记为未验证状态）
- **验证标准**:
  - registerByMobile/registerByEmail 返回 AuthLoginResult
  - 注册成功后前端无需额外调用登录接口
  - 返回的 token 可直接用于后续认证请求

---

#### FIX-010: 注册返回统一后前端 API 类型同步

- **id**: CRITICAL-FE-01
- **来源**: FIX-009 配套
- **位置**: `jeecgboot-vue3/src/api/content/auth/index.ts`
- **优先级**: CRITICAL
- **依赖项**: FIX-009
- **类型**: 前端修复
- **修复步骤**:
  1. 将 registerByMobile/registerByEmail 的返回类型从 string 改为 AuthLoginResult
  2. 注册成功后直接将返回的 token 存入 store，无需再调用 login API
  3. 注册页面提交成功后的跳转逻辑调整
- **验证标准**:
  - 前端注册 API 返回类型与后端一致
  - 注册→登录流程连贯

---

### 三、FLAG 级别 — 代码修复

---

#### FIX-011: content_cancellation_request 唯一约束修复

- **id**: FLAG-CODE-01
- **来源**: review-report:FLAG-15
- **位置**: Flyway 迁移脚本（对应 content_cancellation_request 表）
- **优先级**: FLAG
- **依赖项**: 无
- **类型**: 数据库/代码修复
- **问题描述**: uk_content_cancellation_user_status 唯一约束可能导致用户完成注销（CANCELLED）后无法再次申请注销
- **修复步骤**:
  1. 修改唯一约束为：仅对 status='PENDING' 的记录做 (user_id, status) 唯一约束
  2. 若使用 MySQL 8+，可使用函数索引或在应用层做判断
  3. 方案A（推荐）：移除数据库唯一约束，在 Service 层申请注销前检查是否存在 PENDING 状态的申请
  4. 方案B：使用条件唯一索引（若数据库支持）
- **验证标准**:
  - 用户完成注销（CANCELLED）后可再次申请注销
  - 同一用户不能同时有两个 PENDING 状态的注销申请

---

#### FIX-012: 前端 API 路径前缀系统性修正

- **id**: FLAG-FE-01
- **来源**: frontend drift-report:W-001, frontend review-report:F-001
- **位置**: 
  - `jeecgboot-vue3/src/api/content/auth/index.ts`
  - `jeecgboot-vue3/src/api/content/auth/captcha.ts`
  - `jeecgboot-vue3/src/api/content/account/security.ts`
  - `jeecgboot-vue3/src/api/content/account/cancellation.ts`
- **优先级**: FLAG
- **依赖项**: FIX-006（后端前缀改完后同步）
- **类型**: 前端修复
- **问题描述**:
  1. 所有认证 API（auth/index.ts）当前使用 `/api/v1/content/auth/`，需改为 `/api/v1/content/auth/`（配套 FIX-006）
  2. 账号安全 API（account/security.ts）中 bindPhone/rebindPhone/unbindPhone/bindEmail/rebindEmail/unbindEmail/bindThirdParty/unbindThirdParty/devices/deviceRevoke/resetPassword 错误使用 `/api/v1/content/auth/` 前缀，应使用 `/api/v1/content/account-security/`
  3. loginSmsCode 路径 `/api/v1/content/auth/login/sms` 应与后端对齐（后端当前为 `/login/sms`，设计为 `/login/sms-code`——需确认后端最终路径后统一）
  4. confirmEmail 路径 `/api/v1/content/auth/confirm-email`，设计为 `/register/email/confirm`——后端保持 `/confirm-email`（实际代码），design 文档需同步
  5. cancelCancellation 路径 `/cancel` 应为 `/revoke`（与后端对齐）
  6. anomalyList 路径 `/anomaly/list` vs 设计 `/anomaly-notifications`——后端实际是 `/anomaly/list`，design 文档需同步
- **修复步骤**:
  1. 统一替换 auth/index.ts 中 API 前缀：`/api/v1/content/auth/` → `/api/v1/content/auth/`
  2. 修正 account/security.ts 中 10 个接口前缀：`/api/v1/content/auth/...` → `/api/v1/content/account-security/...`
  3. 确认短信登录路径：后端为 `/api/v1/content/auth/login/sms`（配套 FIX-006 后），前端同步为 `/api/v1/content/auth/login/sms`；design.md 文档路径同步修正
  4. confirmEmail 路径：后端实际为 `/api/v1/content/auth/confirm-email`（配套 FIX-006），前端和 design 文档同步到此路径
  5. cancelCancellation 改为 `/api/v1/content/account-cancellation/revoke`（与后端 ContentAccountCancellationController 对齐，需确认后端实际路径）
  6. anomalyList 路径：后端实际为 `/anomaly/list`，前端保持此路径，更新 design.md 文档
- **验证标准**:
  - 前端所有 API 路径与后端 @RequestMapping 完全一致
  - 无 404 错误
  - auth 前缀为 `/api/v1/content/auth/`（无 /content/）
  - account-security 前缀为 `/api/v1/content/account-security/`
  - account-cancellation 前缀为 `/api/v1/content/account-cancellation/`

---

#### FIX-013: 前端字段命名 phone → mobile 统一

- **id**: FLAG-FE-02
- **来源**: frontend drift-report:W-002, frontend review-report:A-003
- **位置**: 所有前端 API 文件和对应的 Req 接口
- **优先级**: FLAG
- **依赖项**: FIX-012
- **类型**: 前端修复
- **问题描述**: 前端统一使用 `phone` 字段名，后端使用 `mobile`
- **修复步骤**:
  1. 将 SmsCodeParams.phone → mobile
  2. 将 RegisterMobileParams.phone → mobile
  3. 将 LoginSmsParams.phone → mobile
  4. 将 bindPhone/rebindPhone/unbindPhone 等接口的参数名 phone → mobile
  5. 确认所有相关 interface/type 定义同步修改
  6. 检查表单组件中的 v-model 字段名同步
- **验证标准**:
  - 前端所有 API 参数和表单字段使用 `mobile` 命名
  - 与后端 Req VO 字段名一致

---

#### FIX-014: getCaptchaImage HTTP Method 改为 GET

- **id**: FLAG-FE-03
- **来源**: frontend drift-report:W-003, frontend review-report:F-004
- **位置**: `jeecgboot-vue3/src/api/content/auth/captcha.ts` (第 14,19 行)
- **优先级**: FLAG
- **依赖项**: FIX-012
- **类型**: 前端修复
- **问题描述**: 获取图片资源使用 POST 不符合 RESTful 语义，且 design.md 定义为 GET
- **修复步骤**:
  1. 将 `defHttp.post` 改为 `defHttp.get`
  2. 确认后端 ContentAuthController.getCaptchaImage() 实际方法：当前为 @PostMapping，需确认后端是否同步改为 @GetMapping
  3. 若后端改为 @GetMapping，需同步修改 Controller（将 @PostMapping("/captcha/image") 改为 @GetMapping("/captcha/image")）
- **验证标准**:
  - 前端使用 defHttp.get 获取验证码
  - 后端使用 @GetMapping 提供验证码接口
  - 获取验证码功能正常

---

#### FIX-015: 密码锁定阈值和时长统一

- **id**: FLAG-CODE-02
- **来源**: review-report:FLAG-03, verify-report:WARN-01
- **位置**: 
  - design.md
  - specs/user-authentication/spec.md
  - tasks.md
  - ContentRiskControlBizServiceImpl 锁定逻辑
- **优先级**: FLAG
- **依赖项**: 无
- **类型**: 文档+代码修复
- **问题描述**: spec 要求 5 次失败锁定 15 分钟；tasks.md 说 10 次挑战 20 次锁定 30 分钟；平台层是 5 次/10 分钟
- **修复步骤**:
  1. 与产品确认最终阈值（建议采用 spec 的 5 次/15 分钟，与安全最佳实践一致）
  2. 统一更新 design.md、spec.md、tasks.md 中的锁定参数描述
  3. 确认 ContentRiskControlBizServiceImpl 中实际锁定阈值，若不一致则修改为统一值
  4. 确认 Redis key TTL 与锁定时长一致
- **验证标准**:
  - design/spec/tasks/代码 四处锁定阈值完全一致
  - 测试验证锁定逻辑正确

---

#### FIX-016: 注销前置资格多维度校验补充

- **id**: FLAG-CODE-03
- **来源**: verify-report:WARN-02, drift-report:负向漂移
- **位置**: `ContentAccountCancellationBizServiceImpl.checkEligibility()` 方法
- **优先级**: FLAG
- **依赖项**: FIX-004
- **类型**: 代码修复
- **问题描述**: checkEligibility 仅检查账号状态，缺少 spec 要求的"未完成申诉、违规处理、未结算订单、未结算积分"校验
- **修复步骤**:
  1. 定义注销资格校验接口 CancellationEligibilityChecker（或在 Biz 中直接调用）
  2. 补充以下校验项：
     - 是否存在 PENDING 状态的风险事件申诉
     - 是否存在进行中的违规处理
     - 是否存在未结算/未完成的订单（需确认内容社区是否有订单模块，若无则跳过）
     - 是否存在未提现/未消费的积分余额（需确认积分模块，若无则跳过）
  3. 每个校验项返回具体的拒绝原因
  4. 若相关模块尚未实现，预留接口并记录 TODO，当前仅返回"账号状态校验通过"
- **验证标准**:
  - 有未完成申诉/违规的用户无法申请注销
  - 返回明确的拒绝原因
  - 若订单/积分模块不存在，有明确的 TODO 标记和预留接口

---

#### FIX-017: 设备上限 5 台挤出最早设备逻辑确认/补全

- **id**: FLAG-CODE-04
- **来源**: verify-report:缺失项
- **位置**: IContentDeviceSessionService 实现类
- **优先级**: FLAG
- **依赖项**: FIX-003
- **类型**: 代码确认/补全
- **问题描述**: spec 要求最多 5 个活跃设备，超过自动下线最早设备，但当前实现未验证
- **修复步骤**:
  1. 检查登录时是否查询当前用户活跃设备数
  2. 若设备数 >= 5，自动将最早登录的设备下线（删除/标记失效）
  3. 补充挤出逻辑的测试用例
- **验证标准**:
  - 第 6 台设备登录时，最早的设备被自动下线
  - 被下线设备的 token 失效，无法继续访问

---

#### FIX-018: 批量注册 IP 限流实现

- **id**: FLAG-CODE-05
- **来源**: review-report:FLAG对应项, verify-report:缺失项
- **位置**: ContentAuthBizServiceImpl.registerByMobile/registerByEmail
- **优先级**: FLAG
- **依赖项**: FIX-003
- **类型**: 代码增强
- **问题描述**: spec 要求同 IP 1 小时内最多注册 10 个账号，当前未实现
- **修复步骤**:
  1. 在注册接口前添加 IP 限流检查
  2. 使用 Redis 记录每个 IP 的注册计数，key 如 `content:register:ip:{ip}`，TTL 3600 秒
  3. 注册前 incr 计数，超过 10 则拒绝
  4. 考虑使用 HttpServletRequest 获取客户端真实 IP（处理 X-Forwarded-For）
- **验证标准**:
  - 同 IP 1 小时内注册超过 10 个账号被拒绝
  - 计数在 Redis 中正确 TTL 过期

---

#### FIX-019: 图形验证码获取接口 Method 对齐（后端 GET）

- **id**: FLAG-CODE-06
- **来源**: drift-report:API路径, FIX-014 配套
- **位置**: ContentAuthController.java 第 221 行
- **优先级**: FLAG
- **依赖项**: FIX-006, FIX-014
- **类型**: 代码修复
- **修复步骤**:
  1. 将 @PostMapping("/captcha/image") 改为 @GetMapping("/captcha/image")
  2. 确认验证码获取不需要请求体参数
  3. 前端 FIX-014 已同步改为 GET
- **验证标准**:
  - 后端使用 @GetMapping("/captcha/image")
  - GET 请求能正常返回验证码图片 base64

---

#### FIX-020: 密码强度规则明确定义

- **id**: FLAG-DOC-01
- **来源**: review-report:FLAG-10
- **位置**: specs/user-authentication/spec.md, PasswordUtil 相关校验
- **优先级**: FLAG
- **依赖项**: 无
- **类型**: 文档+代码
- **修复步骤**:
  1. 在 spec.md 中明确密码强度规则（建议：至少8位，包含大写字母、小写字母、数字中的至少2种）
  2. 在 design.md 中同步密码策略
  3. 确认代码中密码校验逻辑与文档一致
- **验证标准**:
  - 文档中有明确的密码强度规则
  - 代码校验逻辑与文档一致

---

#### FIX-021: content_user_password_history 添加唯一约束或去重逻辑

- **id**: FLAG-CODE-07
- **来源**: review-report:FLAG-15
- **位置**: Flyway 迁移脚本或 Service 层
- **优先级**: FLAG
- **依赖项**: FIX-003
- **类型**: 代码修复
- **修复步骤**:
  1. 方案A：添加 (user_id, password_hash) 唯一约束
  2. 方案B（推荐）：在重置密码时先查询最近 N 次密码历史，判断是否重复，Service 层做去重
  3. 确保密码历史只保留最近 N 条（如 5 条）
- **验证标准**:
  - 同一用户不会插入重复的密码哈希记录
  - 设置与最近 N 次相同的密码被拒绝

---

#### FIX-022: 账号状态枚举值统一（Enum 类）

- **id**: FLAG-CODE-08
- **来源**: review-report:BLOCK-08
- **位置**: auth 包下新建 enums 包
- **优先级**: FLAG
- **依赖项**: 无
- **类型**: 代码重构
- **修复步骤**:
  1. 创建 AccountStatusEnum：ACTIVE, CANCELLING, CANCELLED, LOCKED
  2. 创建 CancellationStatusEnum：NONE, PENDING, CANCELLED（或与 AccountStatus 合并）
  3. 将 ContentUserAccount.accountStatus 和 cancellationStatus 字段改为 Enum 类型（MyBatis-Plus 支持 @EnumValue）
  4. 或保持 String 类型但添加静态常量类，并在 Service 层做合法值校验
- **验证标准**:
  - 不会有非法状态值写入数据库
  - 状态转换有类型安全校验

---

### 四、文档修复项（仅文档，不涉及代码）

---

#### FIX-023: 补充 plan.md 文件

- **id**: BLOCK-DOC-01
- **来源**: review-report:BLOCK-01
- **位置**: `openspec/changes/user-01-authentication/plan.md`
- **优先级**: BLOCK（文档）
- **依赖项**: 无
- **类型**: 文档
- **修复步骤**:
  1. 补充 plan.md，包含：
     - 包结构说明（controller/biz/service/entity/mapper/dto/req/vo/enums）
     - 实体-表映射表
     - API 端点完整清单（已按 FIX-006 更新路径）
     - 分层实现顺序（Entity → Mapper → Service → Biz → Controller）
     - 测试策略
     - Redis Key 清单和 TTL 配置
     - 外部 Port 接口清单
- **验证标准**:
  - plan.md 文件存在
  - 包含上述所有章节
  - 与实际代码结构一致

---

#### FIX-024: 更新 design.md 第三方绑定表名

- **id**: BLOCK-DOC-02
- **来源**: review-report:BLOCK-02, drift-report:真相源裁决
- **位置**: `openspec/changes/user-01-authentication/design.md`
- **优先级**: BLOCK（文档）
- **依赖项**: 无
- **类型**: 文档
- **修复步骤**:
  1. 将 design.md 中所有 `content_user_social_binding` 替换为 `content_user_third_party_auth`
  2. 更新表设计说明：复用现有表，通过 ALTER TABLE 扩展 open_id、union_id、nickname、avatar、raw_json 等字段
  3. 更新实体映射说明：对应 ContentUserThirdPartyAuth 实体
- **验证标准**:
  - design.md 中无 `social_binding` 字样
  - 表名与实际 Flyway 脚本一致

---

#### FIX-025: 补充 design.md API 契约表格

- **id**: BLOCK-DOC-03
- **来源**: review-report:BLOCK-04, BLOCK-05
- **位置**: `openspec/changes/user-01-authentication/design.md`
- **优先级**: BLOCK（文档）
- **依赖项**: FIX-006
- **类型**: 文档
- **修复步骤**:
  1. 补充每个 API 的完整契约表格：
     - 路径（已更新为 /api/v1/content/auth/ 等正确前缀）
     - HTTP Method
     - 请求体字段（名称、类型、必填、说明）
     - 响应体字段（名称、类型、说明）
     - 错误码
     - 鉴权要求（是否需要登录）
  2. 定义统一错误码常量类 AuthErrorCodeConstant，明确错误码区间（如 AUTH_001 ~ AUTH_999）
  3. 错误码示例：AUTH_001=验证码错误，AUTH_002=账号锁定，AUTH_003=凭证不存在，AUTH_004=密码错误，AUTH_005=手机号已注册 等
- **验证标准**:
  - design.md 中有完整的 API 契约表格
  - 定义了错误码常量
  - 请求/响应字段与实际代码一致

---

#### FIX-026: 更新 design.md 和前端 design.md 中漂移的 API 路径

- **id**: FLAG-DOC-02
- **来源**: 各 drift-report 路径不一致项
- **位置**: 
  - `openspec/changes/user-01-authentication/design.md`
  - `openspec/changes/user-01-authentication-frontend/design.md`
- **优先级**: FLAG（文档）
- **依赖项**: FIX-006, FIX-012
- **类型**: 文档
- **修复步骤**:
  1. 更新后端 design.md：
     - 认证 API 前缀改为 `/api/v1/content/auth/`（去掉 /content/）
     - 确认短信登录路径为 `/login/sms`（实际代码路径）
     - 确认邮箱确认路径为 `/confirm-email`（实际代码路径）
     - 确认获取验证码 Method 为 GET
     - 补充 lock-status API 文档
     - 补充 send-code 通用验证码发送接口
     - 补充 anomaly/list 路径（而非 anomaly-notifications）
     - 补充 denyAnomaly 接口
     - 补充 eligibility 接口
     - 补充 password/reset 通用重置接口
     - 补充 trust/untrust 设备接口
  2. 更新前端 design.md：
     - 同步所有 API 路径与后端实际代码一致
     - 认证前缀 `/api/v1/content/auth/`
     - account-security 前缀保持 `/api/v1/content/account-security/`
     - account-cancellation 取消路径 `/revoke` 而非 `/cancel`
- **验证标准**:
  - 前后端 design.md 中所有 API 路径与实际代码 @RequestMapping 完全一致
  - 无路径名拼写差异

---

#### FIX-027: specs 补充 Token 过期自动刷新 Scenario

- **id**: FLAG-DOC-03
- **来源**: frontend review-report:FLAG-005
- **位置**: `openspec/changes/user-01-authentication-frontend/specs/user-login/spec.md`
- **优先级**: FLAG（文档）
- **依赖项**: 无
- **类型**: 文档
- **修复步骤**:
  1. 在 user-login/spec.md 中补充 Scenario：
     - WHEN accessToken 过期且 refreshToken 有效 THEN 自动刷新 token 并重试原请求
     - WHEN refreshToken 也过期 THEN 跳转登录页并提示"登录已过期"
  2. 补充对应错误场景 UI 反馈描述（FLAG-003 部分）
- **验证标准**:
  - spec.md 中有 Token 过期处理 Scenario
  - 有明确的 UI 反馈描述

---

#### FIX-028: Redis TTL 配置和外部 Port 接口文档补充

- **id**: FLAG-DOC-04
- **来源**: review-report:FLAG-04, BLOCK-06
- **位置**: `openspec/changes/user-01-authentication/design.md`
- **优先级**: FLAG（文档）
- **依赖项**: FIX-025
- **类型**: 文档
- **修复步骤**:
  1. 在 design.md 中明确所有 Redis key 的 TTL 配置：
     - 短信验证码：300 秒（5分钟）
     - 邮箱验证码：300 秒
     - 密码失败计数：900 秒（15分钟）
     - 账号锁定：900 秒（15分钟）
     - 邮箱验证 token：86400 秒（24小时）
     - 密码重置 token：1800 秒（30分钟）
     - IP 注册计数：3600 秒（1小时）
  2. 补充每个 Port 接口（SmsSenderPort、EmailSenderPort、CaptchaVerifyPort、IpGeolocationPort、OAuthPort）的方法签名、参数、返回值、降级策略
- **验证标准**:
  - design.md 中有完整的 Redis Key 和 TTL 清单
  - Port 接口有明确的方法定义

---

## 执行顺序建议

### Phase 1: 阻塞性代码修复（BLOCK + CRITICAL）
1. FIX-001, FIX-002（riskLevel 类型修复）
2. FIX-003, FIX-004（分层架构修复）
3. FIX-005（定时任务补充）
4. FIX-006（后端 API 前缀修正）
5. FIX-007（第三方登录参数修正）
6. FIX-008（邮箱验证邮件发送）
7. FIX-009（注册自动登录返回）
8. FIX-010（前端注册返回类型同步）

### Phase 2: 前端路径和字段对齐
9. FIX-012（前端 API 路径修正，依赖 FIX-006）
10. FIX-013（字段名 phone→mobile）
11. FIX-014（captcha GET）
12. FIX-019（后端 captcha GET 配套）

### Phase 3: FLAG 级代码增强
13. FIX-011（注销唯一约束）
14. FIX-015（锁定阈值统一）
15. FIX-016（注销前置校验）
16. FIX-017（设备挤出逻辑）
17. FIX-018（IP 限流）
18. FIX-021（密码历史去重）
19. FIX-022（状态枚举）
20. FIX-020（密码强度规则）

### Phase 4: 文档补充
21. FIX-023（plan.md）
22. FIX-024（design.md 表名修正）
23. FIX-025（API 契约 + 错误码）
24. FIX-026（API 路径文档同步）
25. FIX-027（spec Scenario 补充）
26. FIX-028（Redis TTL + Port 文档）

---

## 不修复项（过滤的纯流程/建议项）

以下为审核文档中提到但属于流程建议、ADVISORY 级别、或不影响功能的项，本次不纳入修复：

1. **RESTful 路径风格优化**（/sms/send 改为资源风格）— 现有路径已可用，重构成本高，ADVISORY 级别
2. **包结构分散（auth 包 vs user 包）**— 代码现状可接受，后续可单独重构
3. **credential_value 长度扩展（255→更长）**— bcrypt 仅 60 位，当前足够
4. **TINYINT/VARCHAR 状态字段风格统一**— 不影响功能
5. **Flyway 迁移脚本版本号整理**— 不影响功能，后续迭代处理
6. **Noop 外部服务实现替换**— 属于生产环境部署配置，非代码 Bug
7. **数据匿名化具体逻辑**— 产品尚未最终决策匿名化范围，预留接口即可
8. **高风险找回补充校验**— 产品形态未定，FLAG-01 已记录为开放问题
9. **与 sys_user 映射关系**— FLAG-02 已有 SystemUserAccountGateway 作为桥梁，当前不影响功能
10. **验证码冷却组件独立封装**— ADVISORY 级别，代码内联可接受
11. **coolingOffDays 冗余存储**— SUGGESTION 级别，不影响功能
12. **API 目录结构优化**— SUGGESTION 级别
13. **生产环境真实外部服务 Bean 替换**— 部署配置项，非代码修复范畴

---

## user-01-auth BLOCK/CRITICAL 代码修复状态（agent=u01be）

| FIX ID | 问题 | 状态 | 说明 |
|---|---|---|---|
| BLOCK-CODE-01 | ContentUserAccount.riskLevel 类型不匹配 | ✅ 已修复 | String → Integer，与SQL INT一致 |
| BLOCK-CODE-02 | ContentRiskEvent.riskLevel 类型不匹配 | ✅ 已修复 | String → Integer，同步更新 IContentRiskControlBizService 接口参数类型 |
| BLOCK-CODE-03 | Biz层直接注入Mapper（ContentAuthBizServiceImpl） | ⏭️ 跳过 | 大范围重构，本次不处理 |
| BLOCK-CODE-04 | Biz层直接注入Mapper（ContentAccountCancellationBizServiceImpl） | ⏭️ 跳过 | 依赖FIX-003，本次不处理 |
| BLOCK-CODE-05 | 缺少冷静期到期自动注销@Scheduled定时任务 | ✅ 已修复（partial） | 添加processExpiredCancellations()方法，每小时执行，标记TODO:分布式锁+事务自调用问题 |
| BLOCK-CODE-06 | API路径前缀修正 | ✅ 已修复 | ContentAuthController从/api/v1/content/auth改为/api/v1/content/auth；ContentRiskControlController(/api/v1/content/account-security)和ContentAccountCancellationController(/api/v1/content/account-cancellation)路径保持不变；同步修复了邮件确认URL中的路径 |
| CRITICAL-CODE-01 | 第三方登录接口@RequestParam接收rawJson | ✅ 已修复 | 新增ContentAuthThirdPartyLoginReq DTO，改为@RequestBody接收 |
| CRITICAL-CODE-02 | 邮箱注册未发送验证邮件 | ✅ 已修复（已有实现） | registerByEmail中已存在emailSenderPort.send()调用，确认URL路径同步更新 |
| CRITICAL-CODE-03 | 手机号/邮箱注册返回String而非AuthLoginResult | ✅ 已修复 | registerByMobile/registerByEmail均改为返回AuthLoginResult(含token)，与登录接口一致 |

### 编译验证
- jeecg-module-content模块auth包无编译错误
- 存在的编译错误为channel模块（ChannelAnnouncementController.getById、ChannelExportController.getChannelId）和system模块（DateRangeUtils、DySmsLimit等）的已有问题，与本次修复无关
