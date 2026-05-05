# 内容社区用户域 - 邮箱注册与手机号邮箱绑定解绑设计

## 1. 背景

当前 `content/user` 模块在账号安全域已经具备以下基础能力：

- `POST /content/user/account/register/mobile` 可完成手机号注册与用户资料初始化
- `POST /content/user/account/password/reset` 可通过用户 ID / 手机号 / 邮箱重置密码，并要求二次校验
- 注销申请、冷静期完成注销、撤销注销已经具备基础闭环
- `SystemUserAccountGateway` 已经承担平台账号创建、查询、密码重置与注销标记能力

但覆盖报告中仍有两个明确缺口：

- `PRD-7`：邮箱注册仅停留在字段可落库，没有独立后端入口
- `PRD-17`：手机号/邮箱绑定解绑与敏感操作二次校验尚未实现

这两个缺口都落在当前 `content/user` 模块可以独立闭环的边界内，不需要立刻扩展到验证码登录、第三方登录或复杂风控编排，因此适合作为本轮最小增量实现。

## 2. 目标

在 `jeecg-module-content` 模块内补齐以下能力：

- 新增邮箱注册接口，形成与手机号注册对称的独立能力
- 新增手机号绑定、邮箱绑定、手机号解绑、邮箱解绑接口
- 对绑定解绑敏感操作强制执行二次校验
- 绑定时校验手机号/邮箱唯一性，避免同一联系方式绑定到多个平台账号
- 解绑时保证账号至少保留一种找回方式，避免手机号与邮箱被同时清空
- 为绑定解绑操作补齐审计留痕
- 为新增接口与关键规则补齐 service 与 WebMvc 测试

本轮目标不是建设完整登录中心，而是在现有用户域边界内，把“邮箱注册 + 绑定解绑”补成真实可验证的后端闭环。

## 3. 范围

### 3.1 包含

- 新增邮箱注册请求对象与接口
- 新增绑定手机号、绑定邮箱、解绑手机号、解绑邮箱请求对象与接口
- 扩展 `IContentAccountService` 与 `SystemUserAccountGateway`
- 在 `SysUser.phone`、`SysUser.email` 上完成绑定解绑真实写入
- 新增账号敏感操作审计日志事件
- 补齐对应单元测试与 WebMvc 测试

### 3.2 不包含

- 不实现验证码登录、密码登录、第三方登录
- 不实现邀请码校验
- 不实现异常登录识别、限流、临时冻结等风控能力
- 不新增第三方账号绑定表
- 不改动 `content/user` 目录外的鉴权或平台登录主链路
- 不修改数据库表结构或新增 Flyway SQL

## 4. 约束

- 保持当前 Spring Boot 3、JeecgBoot、MyBatis-Plus 与 `Result<T>` 风格不变
- Controller 只负责协议边界，业务编排仍落在 `service`
- 请求对象放 `req/account`
- 真实账号凭证写入继续复用平台 `SysUser`，不新造绑定副本
- 绑定解绑必须走二次校验，不能绕过安全门槛
- 审计日志必须记录为敏感操作，满足后续合规追踪
- 只做当前最小闭环，不顺手扩成更大的账号中心重构

## 5. 方案选择

### 5.1 方案 A：新增独立请求模型与独立接口

做法：

- 为邮箱注册新增独立 `req`
- 为手机号绑定、邮箱绑定、手机号解绑、邮箱解绑分别新增请求模型
- `ContentAccountController` 新增 5 个明确接口
- `SystemUserAccountGateway` 新增针对手机号/邮箱的绑定解绑方法

优点：

- 接口语义清晰，和 PRD 映射关系最直接
- 每个请求对象的校验规则单一，不会互相污染
- 后续扩验证码、绑定历史、第三方绑定时更易演进

缺点：

- 新增文件数量略多

### 5.2 方案 B：复用现有注册请求和一个大而全的绑定请求

做法：

- `/register/email` 继续复用 `ContentRegisterReq`
- 绑定解绑共用一个请求对象，通过动作字段区分操作

优点：

- 新增文件更少

缺点：

- `ContentRegisterReq.mobile` 当前是必填，和邮箱注册语义冲突
- 绑定解绑请求容易堆成万能对象，后续校验会越来越绕

### 5.3 结论

本次采用 **方案 A：新增独立请求模型与独立接口**。

原因：

- 能保持接口和校验边界稳定
- 能避免继续扭曲现有 `ContentRegisterReq` 的手机号必填语义
- 能让 service 和测试都保持最小歧义

## 6. 接口设计

### 6.1 邮箱注册

新增接口：

- `POST /content/user/account/register/email`

新增请求对象建议：

- `ContentEmailRegisterReq`

建议字段：

- `username`
- `email`
- `password`
- `nickname`
- `inviteCode`

规则：

- `email` 必填且格式合法
- `password`、`nickname` 必填
- `username` 可选；若未传则默认使用邮箱前缀或邮箱全量作为用户名候选，再走唯一性校验
- 初始化社区资料、通知设置的流程与手机号注册保持一致

### 6.2 绑定手机号

新增接口：

- `POST /content/user/account/bind/mobile`

新增请求对象建议：

- `ContentAccountBindMobileReq`

建议字段：

- `userId`
- `mobile`
- `operatorUserId`
- `secondaryVerified`

规则：

- `userId`、`mobile` 必填
- `secondaryVerified` 必须为 `true`
- 目标手机号未被其他账号占用
- 如果用户已绑定相同手机号，则视为幂等成功

### 6.3 绑定邮箱

新增接口：

- `POST /content/user/account/bind/email`

新增请求对象建议：

- `ContentAccountBindEmailReq`

建议字段：

- `userId`
- `email`
- `operatorUserId`
- `secondaryVerified`

规则：

- `userId`、`email` 必填
- `secondaryVerified` 必须为 `true`
- 目标邮箱未被其他账号占用
- 如果用户已绑定相同邮箱，则视为幂等成功

### 6.4 解绑手机号

新增接口：

- `POST /content/user/account/unbind/mobile`

新增请求对象建议：

- `ContentAccountUnbindMobileReq`

建议字段：

- `userId`
- `operatorUserId`
- `secondaryVerified`

规则：

- `userId` 必填
- `secondaryVerified` 必须为 `true`
- 当前账号已绑定手机号，否则报错
- 若当前账号未绑定邮箱，则禁止解绑手机号，避免失去全部找回方式

### 6.5 解绑邮箱

新增接口：

- `POST /content/user/account/unbind/email`

新增请求对象建议：

- `ContentAccountUnbindEmailReq`

建议字段：

- `userId`
- `operatorUserId`
- `secondaryVerified`

规则：

- `userId` 必填
- `secondaryVerified` 必须为 `true`
- 当前账号已绑定邮箱，否则报错
- 若当前账号未绑定手机号，则禁止解绑邮箱，避免失去全部找回方式

## 7. 服务边界设计

### 7.1 `IContentAccountService`

新增方法：

- `registerByEmail(ContentEmailRegisterReq req)`
- `bindMobile(ContentAccountBindMobileReq req)`
- `bindEmail(ContentAccountBindEmailReq req)`
- `unbindMobile(ContentAccountUnbindMobileReq req)`
- `unbindEmail(ContentAccountUnbindEmailReq req)`

职责：

- 负责参数之外的业务校验
- 负责用户资料初始化复用
- 负责二次校验门槛与解绑保护规则
- 负责调用 gateway 修改平台账号
- 负责写入敏感操作审计日志

### 7.2 `SystemUserAccountGateway`

新增方法：

- `createUserByEmail(ContentEmailRegisterReq req)`
- `bindMobile(String userId, String mobile)`
- `bindEmail(String userId, String email)`
- `unbindMobile(String userId)`
- `unbindEmail(String userId)`

职责：

- 操作平台 `SysUser`
- 校验手机号/邮箱唯一性
- 在账号不存在时抛业务异常
- 返回更新后的平台账号信息供 service 做后续规则判断

说明：

- 邮箱注册也可以继续复用通用创建逻辑，但接口层建议拆出独立方法，避免继续让 `ContentRegisterReq` 承担双重语义

## 8. 规则设计

### 8.1 二次校验

绑定解绑均属于敏感操作，统一规则如下：

- `secondaryVerified != true` 时直接拒绝
- 返回业务异常，错误文案保持和密码重置一致风格

### 8.2 唯一性

- 绑定手机号前，若该手机号已被其他 `SysUser` 占用，则拒绝
- 绑定邮箱前，若该邮箱已被其他 `SysUser` 占用，则拒绝
- 若当前用户已经绑定相同值，则直接返回成功，不重复写库

### 8.3 至少保留一种联系方式

- 解绑手机号前，若当前账号邮箱为空，则拒绝解绑
- 解绑邮箱前，若当前账号手机号为空，则拒绝解绑

该规则用于满足找回能力最小兜底，不让账号进入“无手机号且无邮箱”的不可恢复状态。

### 8.4 用户名生成

邮箱注册时：

- 若请求显式传入 `username`，则按传入值校验唯一性
- 若未传 `username`，优先使用邮箱地址作为用户名候选
- 若候选用户名已存在，则按当前仓库风格追加随机后缀生成新候选，直到唯一

本轮优先保证注册成功，不把用户名策略扩展成复杂命名中心。

## 9. 审计设计

本轮为绑定解绑补充新的账号敏感操作审计事件：

- `USER_ACCOUNT_MOBILE_BOUND`
- `USER_ACCOUNT_EMAIL_BOUND`
- `USER_ACCOUNT_MOBILE_UNBOUND`
- `USER_ACCOUNT_EMAIL_UNBOUND`

审计日志字段建议：

- `userId`
- `operatorUserId`
- `eventType`
- `eventContent`
- `extraDataJson`
- `eventTime`

其中：

- `eventContent` 记录操作摘要，例如 `bind_mobile`、`unbind_email`
- `extraDataJson` 仅记录脱敏后的目标值，避免明文暴露完整手机号和邮箱

## 10. 测试设计

### 10.1 Service 单测

补充 `ContentAccountServiceTest`：

- 邮箱注册成功，创建平台账号、资料与通知设置
- 未二次校验时绑定手机号失败
- 绑定邮箱成功并写审计日志
- 绑定已被占用的手机号或邮箱时失败
- 解绑手机号时若邮箱为空则失败
- 解绑邮箱时若手机号为空则失败
- 已绑定相同手机号或邮箱时按幂等成功处理

### 10.2 WebMvc 测试

补充 `ContentAccountControllerWebMvcTest`：

- `register/email` 成功路径
- `bind/mobile` 成功路径
- `bind/email` 成功路径
- `unbind/mobile` 成功路径
- `unbind/email` 成功路径
- 业务异常路径至少覆盖一种二次校验失败或解绑保护失败

## 11. 实施顺序

1. 新增请求对象
2. 扩展 `IContentAccountService` 与 `ContentAccountController`
3. 扩展 `SystemUserAccountGateway` 与实现类
4. 在 `ContentAccountServiceImpl` 中补邮箱注册与绑定解绑规则
5. 补账号敏感操作审计日志
6. 补 service 与 WebMvc 测试
7. 更新覆盖报告中的增量实现记录与账号安全域结论

## 12. 风险与边界

- 本轮直接复用 `SysUser.phone`、`SysUser.email`，因此不具备绑定历史与换绑过程留痕明细，只保留最终值和审计事件
- 邮箱注册的用户名自动生成策略若处理不当，可能引入重复用户名冲突，需要在 gateway 内统一兜底
- 绑定解绑与资料展示仍是两条链路，本轮不顺手修改 `profile` 出参中的绑定状态字段
- 若未来要接验证码校验，当前 `secondaryVerified` 仍只是布尔门闩，需要后续升级为真实校验凭证

## 13. 验收标准

- 后端存在独立邮箱注册接口，且可真实创建平台账号与社区资料
- 后端存在手机号/邮箱绑定解绑接口，且能真实更新平台账号联系方式
- 绑定解绑在未完成二次校验时会被拒绝
- 解绑不会让账号同时失去手机号和邮箱
- 绑定解绑会产生可追踪的审计日志
- 新增能力具备对应 service 单测和 WebMvc 测试
