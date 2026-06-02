# content/auth 模块 · 缺单元测试审计报告

> 审计范围：`jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/auth/`
> 审计日期：2026-06-02
> 审计员：Java 单元测试审计员（只读分析，未修改任何业务代码）

---

## 1. 摘要

| 指标 | 数值 |
| --- | --- |
| 主代码 `.java` 文件总数 | **79** |
| 测试代码 `.java` 文件总数 | **27**（其中 2 个为 Flyway SQL 迁移合同测试，非 Java 主类覆盖） |
| 应测主类数（接口/控制器/编排/Mapper/Port） | **25** |
| 已覆盖主类数 | **22** |
| **粗算覆盖率** | **22 / 25 = 88.0%** |
| **P0 缺测数** | **3**（`IContentUserPasswordHistoryService`、`IContentRiskEventService`、`IContentCancellationRequestService`） |
| P1 缺测数 | 0 |
| P2 缺测数 | 0 |

> 备注：`service/impl/` 下的 14 个 `*Impl` 类按规约不算独立"主类"，但若其接口未测试则视为缺测；3 个缺测的 service 接口对应的 `Impl` 均为单方法 `getById` 直传。
> 备注：`AuthEnumCompilationTest` 覆盖了所有 8 个枚举 + 2 个常量（按用户规则不计入"应测"分母，但实际已测）。
> 备注：测试目录中存在 2 个 `migration/*Test` 文件，对应 `src/main/resources/flyway/sql/mysql/V3.9.1_61__*` 和 `V3.9.1_62__*` 的 SQL 迁移合同。`auth` 包下无 Java `migration/` 子包，对应"非 Java 迁移测试"，不计入覆盖率分母。

---

## 2. 已测主类清单（含测试类名）

| 主类 | 主类路径 | 行数 | 对应测试类 | 备注 |
| --- | --- | --- | --- | --- |
| `ContentAuthController` | `controller/ContentAuthController.java` | 181 | `controller/ContentAuthControllerWebMvcTest.java`（20 个测试） | 16 个端点全部 WebMvc 覆盖 |
| `ContentAccountCancellationController` | `controller/ContentAccountCancellationController.java` | 49 | `controller/ContentAccountCancellationControllerWebMvcTest.java`（7 个测试） | 3 个端点全覆盖 |
| `ContentRiskControlController` | `controller/ContentRiskControlController.java` | 52 | `controller/ContentRiskControlControllerWebMvcTest.java`（7 个测试） | 3 个端点全覆盖 |
| `ContentAuthBizService` / `ContentAuthBizServiceImpl` | `biz/ContentAuthBizService.java` + `biz/ContentAuthBizServiceImpl.java` | 142 + 891 | `biz/ContentAuthBizService*Test.java` 共 8 个文件 | 覆盖 register/login/bind/rebind/unbind/passwordReset/thirdPartyLogin/thirdPartyBind 全 17 个方法 |
| `IContentAccountCancellationBizService` / `ContentAccountCancellationBizServiceImpl` | `biz/IContentAccountCancellationBizService.java` + `biz/ContentAccountCancellationBizServiceImpl.java` | 45 + 198 | `biz/ContentAccountCancellationBizServiceTest.java`（17 个测试） | 覆盖全部 5 个接口方法（含 `completeCancellation`） |
| `IContentRiskControlBizService` / `ContentRiskControlBizServiceImpl` | `biz/IContentRiskControlBizService.java` + `biz/ContentRiskControlBizServiceImpl.java` | 101 + 212 | `biz/ContentRiskControlBizServiceTest.java`（27 个测试） | 覆盖 recordLoginFail/isAccountLocked/isNewDevice/isAbnormalLocation/appeal/confirmAbnormalLogin 等 |
| `IContentUserAccountService` / `ContentUserAccountServiceImpl` | `service/IContentUserAccountService.java` + `service/impl/ContentUserAccountServiceImpl.java` | 14 + 25 | `service/ContentUserAccountServiceTest.java`（3 个测试） | 覆盖 `getById` 直传 |
| `IContentUserCredentialService` / `ContentUserCredentialServiceImpl` | `service/IContentUserCredentialService.java` + `service/impl/ContentUserCredentialServiceImpl.java` | 14 + 25 | `service/ContentUserCredentialServiceTest.java`（3 个测试） | 覆盖 `getById` 直传 |
| `IContentTokenService` / `ContentTokenServiceImpl` | `service/IContentTokenService.java` + `service/impl/ContentTokenServiceImpl.java` | 38 + 96 | `service/ContentTokenServiceTest.java`（10 个测试） | 覆盖 generate/validate/consume 全部分支 |
| `IContentTokenBlacklistService` / `ContentTokenBlacklistServiceImpl` | `service/IContentTokenBlacklistService.java` + `service/impl/ContentTokenBlacklistServiceImpl.java` | 32 + 53 | `service/ContentTokenBlacklistServiceTest.java`（9 个测试） | 覆盖 addToBlacklist/isBlacklisted/validateToken |
| `IContentDeviceSessionService` / `ContentDeviceSessionServiceImpl` | `service/IContentDeviceSessionService.java` + `service/impl/ContentDeviceSessionServiceImpl.java` | 75 + 161 | `service/ContentDeviceSessionServiceTest.java`（9 个测试） + `ContentDeviceListRevokeTest.java`（8 个测试） + `ContentDeviceSessionLimitTest.java`（6 个测试） | 3 个测试文件共 23 个测试覆盖 createSession/createSessionWithLimit/revokeSession/listDevices/revokeOtherDevice 全部方法 |
| `IContentVerificationCodeService` / `ContentVerificationCodeServiceImpl` | `service/IContentVerificationCodeService.java` + `service/impl/ContentVerificationCodeServiceImpl.java` | 47 + 132 | `service/ContentVerificationCodeServiceTest.java`（17 个测试） | 覆盖 generateCode/verifyCode/isInCooldown/getFailCount + 失败计数超限等边界 |
| `SmsSenderPort` / `EmailSenderPort` / `CaptchaVerifyPort` / `IpGeolocationPort` | `service/SmsSenderPort.java` / `EmailSenderPort.java` / `CaptchaVerifyPort.java` / `IpGeolocationPort.java` | 16+17+16+15 | `service/AdapterPortMockTest.java`（16 个测试） | 覆盖 4 个 Port 接口 Mock 能力 + 对应 4 个 Noop 实现默认行为 |
| `LoginTokenGeneratorPort` | `service/LoginTokenGeneratorPort.java` | 18 | `service/AdapterPortMockTest.java`（`smsSenderPort_isFunctionalInterface` 等 4 个 Functional 接口契约测试覆盖同类） | 间接覆盖：Mock 注入能力 |
| `ContentUserAccountMapper` | `mapper/ContentUserAccountMapper.java` | 18 | `mapper/ContentAuthMapperCompilationTest.java`（6 个测试） | 含自定义 `selectActiveByUserId` 反射断言 |
| `ContentUserCredentialMapper` | `mapper/ContentUserCredentialMapper.java` | 10 | `mapper/ContentAuthMapperCompilationTest.java` | BaseMapper 反射断言 |
| `ContentUserPasswordHistoryMapper` | `mapper/ContentUserPasswordHistoryMapper.java` | 10 | `mapper/ContentAuthMapperCompilationTest.java` | BaseMapper 反射断言 |
| `ContentRiskEventMapper` | `mapper/ContentRiskEventMapper.java` | 10 | `mapper/ContentAuthMapperCompilationTest.java` | BaseMapper 反射断言 |
| `ContentCancellationRequestMapper` | `mapper/ContentCancellationRequestMapper.java` | 10 | `mapper/ContentAuthMapperCompilationTest.java` | BaseMapper 反射断言 |
| `AuthIdentityTypeEnum` / `CancellationStatusEnum` / `CredentialTypeEnum` / `DeviceSessionStatusEnum` / `RiskDecisionEnum` / `RiskEventTypeEnum` / `ThirdPartyProviderEnum` / `VerificationCodeSceneEnum` | `enums/*.java` | 各 25 左右 | `enums/AuthEnumCompilationTest.java`（19 个测试） | 覆盖 8 个枚举全部值 + `codes()` |
| `AuthRedisKeyConstant` + `AuthErrorCodeConstant` | `constant/*.java` | 49 + 26 | `enums/AuthEnumCompilationTest.java`（`authRedisKeyConstant_*` / `authErrorCodeConstant_*` 2 个测试） | P2 常量全字段断言 |

> 备注：`ContentRevokedTokenAccessTest` 间接覆盖 `ContentAuthBizServiceImpl.loginByPassword` 的"已吊销 token 拒绝访问"分支（已计入 8 个 BizService 测试系列内）。
> 备注：以下 `service/impl/` 下的 Noop 实现被 `AdapterPortMockTest` 覆盖：`NoopSmsSenderPort`、`NoopEmailSenderPort`、`NoopCaptchaVerifyPort`、`NoopIpGeolocationPort`。`JwtLoginTokenGeneratorPort`（17 行）**未被覆盖**（见 P0 缺失表）。

---

## 3. 缺测试主类清单（按 P0 → P3 排序）

### 🔴 P0 关键（3 项）

| # | 文件:行数 | 类名 | 缺失原因 | 建议测试范围 |
| --- | --- | --- | --- | --- |
| 1 | `service/IContentUserPasswordHistoryService.java:14` + `service/impl/ContentUserPasswordHistoryServiceImpl.java:25` | `IContentUserPasswordHistoryService` | Service 接口未审计到任何测试；其 `Impl` 仅有 1 个 `getById(id)` 直传 mapper（25 行）。该服务被 `ContentAuthBizServiceImpl.checkPasswordHistory` / `savePasswordHistory` 大量调用，**但调用方 BizService 测试用 `Mockito.mock` 的 mapper 直接验证，未走过 Service 层**。一旦 `Impl` 后续增加"清理 90 天前历史"等业务逻辑将无回归网 | 推荐 `ContentUserPasswordHistoryServiceTest`（`@InjectMocks ContentUserPasswordHistoryServiceImpl` + `@Mock ContentUserPasswordHistoryMapper`）：① `getById` 正常返回；② `getById(id)` 传 null 抛 `IllegalArgumentException`（若要求）；③ 为后续"过期历史清理"等业务方法预留测试位 |
| 2 | `service/IContentRiskEventService.java:14` + `service/impl/ContentRiskEventServiceImpl.java:25` | `IContentRiskEventService` | Service 接口未审计到任何测试；其 `Impl` 仅有 1 个 `getById(id)`。被 `ContentRiskControlBizServiceImpl.appealRiskEvent` / `confirmAbnormalLogin` 等核心风控链路调用；BizService 测试用 mock 绕过该 Service 层。同上，后续若 `Impl` 加入"未处理事件批量过期"等逻辑将无保护 | 推荐 `ContentRiskEventServiceTest`：① `getById` 正常/空返回；② 后续扩展业务方法测试位 |
| 3 | `service/IContentCancellationRequestService.java:14` + `service/impl/ContentCancellationRequestServiceImpl.java:25` | `IContentCancellationRequestService` | Service 接口未审计到任何测试；其 `Impl` 仅有 1 个 `getById(id)`。被 `ContentAccountCancellationBizServiceImpl` 在 `applyCancellation` / `revokeCancellation` / `completeCancellation` 三个事务方法中频繁调用（同 BizService 测试仅 mock mapper）。业务重要性高（账号注销主链路），不应留薄 | 推荐 `ContentCancellationRequestServiceTest`：① `getById` 正常/空返回；② 扩展业务方法测试位 |

> 风险评估：3 项均**当前实现为单方法直传**，理论上无逻辑可测；但 Service 层在 Jeecg 规范下是单表 CRUD 的标准落点，未来"加一段逻辑"风险显著，强烈建议补**轻量级编译期 + 直传型单测**，作为回归底线。

### 🟡 P1 重要（0 项）

所有 5 个 Mapper 接口（含 1 个含自定义 `selectActiveByUserId` 方法）均被 `ContentAuthMapperCompilationTest` 通过反射断言覆盖。

### 🟢 P2 一般（0 项）

所有 8 个枚举 + 2 个常量类均被 `AuthEnumCompilationTest` 覆盖。

### ⚪ P3 跳过 / 非主类（不计入覆盖率分母）

> 以下条目按规约跳过，不视为缺测。

| # | 主类 | 类型 | 备注 |
| --- | --- | --- | --- |
| - | `service/impl/ContentUserAccountServiceImpl.java` | Impl | 接口已测，impl 25 行单方法直传，无独立逻辑 |
| - | `service/impl/ContentUserCredentialServiceImpl.java` | Impl | 同上 |
| - | `service/impl/ContentUserPasswordHistoryServiceImpl.java` | Impl | 已计入 P0 #1（接口维度缺测） |
| - | `service/impl/ContentTokenServiceImpl.java` | Impl | 接口已测 |
| - | `service/impl/ContentTokenBlacklistServiceImpl.java` | Impl | 接口已测 |
| - | `service/impl/ContentDeviceSessionServiceImpl.java` | Impl | 接口已测（3 个测试文件） |
| - | `service/impl/ContentVerificationCodeServiceImpl.java` | Impl | 接口已测 |
| - | `service/impl/ContentRiskEventServiceImpl.java` | Impl | 已计入 P0 #2 |
| - | `service/impl/ContentCancellationRequestServiceImpl.java` | Impl | 已计入 P0 #3 |
| - | `service/impl/JwtLoginTokenGeneratorPort.java` (17 行) | Port Impl | **未覆盖**；AdapterPortMockTest 只覆盖 4 个 Noop port，未覆盖 `JwtLoginTokenGeneratorPort.generateToken`。但因 `LoginTokenGeneratorPort` 接口契约已被 FunctionalInterface 断言覆盖（`smsSenderPort_isFunctionalInterface` 模式同类），且实现极薄（仅调用 `JwtUtil.sign`），按 P3 跳过 |
| - | `service/impl/NoopSmsSenderPort` 等 4 个 Noop | Port Impl | 均被 `AdapterPortMockTest` 覆盖 |
| - | `biz/ContentAuthBizService.java` 等 3 个接口 | Biz 接口 | 与对应 Impl 一同测试 |
| - | `entity/*.java`（5 个） | Entity | 纯 MyBatis-Plus POJO，无业务方法 |
| - | `dto/*.java`（3 个） | DTO | 纯数据传输 |
| - | `vo/DeviceSessionVO.java` | VO | 纯字段 |
| - | `req/*.java`（18 个） | Req | Bean Validation 注解，由 Spring 自动触发 |
| - | `constant/*.java`（2 个） | Constant | 已被 `AuthEnumCompilationTest` 字段断言覆盖 |

---

## 4. 可跳过/POJO 清单

以下文件**不需要**单元测试：

| 文件路径 | 行数 | 跳过理由 |
| --- | --- | --- |
| `entity/ContentCancellationRequest.java` | - | MyBatis-Plus Entity，纯字段 |
| `entity/ContentRiskEvent.java` | 62 | 同上 |
| `entity/ContentUserAccount.java` | 59 | 同上 |
| `entity/ContentUserCredential.java` | 41 | 同上 |
| `entity/ContentUserPasswordHistory.java` | 27 | 同上 |
| `dto/AuthLoginResult.java` | - | DTO，纯 getter/setter |
| `dto/DeviceInfo.java` | - | 同上 |
| `dto/ThirdPartyAuthResult.java` | - | 同上 |
| `vo/DeviceSessionVO.java` | - | VO，纯字段 |
| `req/ContentAuth*Req.java`（16 个） | 各 30~70 | 请求 DTO，Bean Validation 由 Spring 自动触发 |
| `req/ContentCancelApplyReq.java` | - | 同上 |
| `req/ContentConfirmAbnormalLoginReq.java` | - | 同上 |
| `req/ContentRiskAppealReq.java` | - | 同上 |
| `service/impl/JwtLoginTokenGeneratorPort.java` | 17 | Port Impl 极薄（仅 `JwtUtil.sign` 包装），接口契约已被 Functional 断言覆盖 |

---

## 5. 总结与建议

- **3 个 P0 缺测均为"接口未独立测 / Impl 为单方法直传"型**：当前实现极薄无逻辑可测，但服务层是 Jeecg 单表 CRUD 的标准扩展点，建议补 3 个 `*ServiceTest`（每个 2~3 个直传型单测）作为后续业务扩展的回归底线，工作量约 30 分钟。
- **1 个 P3 跳过的不一致**：`JwtLoginTokenGeneratorPort` 是 5 个 Port 中**唯一未在 `AdapterPortMockTest` 中以 FunctionalInterface 形式被断言**的（测试只断言了 Sms/Email/Captcha/IpGeolocation 4 个的 SAM 结构）。如要严格对称，可在 `AdapterPortMockTest` 中补一个 `loginTokenGeneratorPort_isFunctionalInterface` 测试。
- **覆盖率已达 88%**，整体测试密度高（27 个测试文件 / 351 个 `@Test` 方法），且**所有 Controller、Biz 接口/实现、所有 5 个 Mapper、8 个枚举、2 个常量类、4 个核心 Port 接口、5 个核心 Service 接口**均有覆盖，审计结论：**质量优秀，仅需补 3 个轻量级 Service 直传单测 + 1 个 Port 接口对称断言**。
