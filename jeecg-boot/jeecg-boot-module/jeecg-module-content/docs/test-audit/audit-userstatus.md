# content/userstatus 模块 · 缺单元测试审计报告

> 审计范围：`jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/userstatus/`
> 审计日期：2026-06-02
> 审计员：Java 单元测试审计员（只读分析，未修改任何业务代码）

---

## 1. 摘要

| 指标 | 数值 |
| --- | --- |
| 主代码 `.java` 文件总数 | **16**（含 `config/` 空目录，实际 15 个类文件） |
| 测试代码 `.java` 文件总数 | **6** |
| 应测主类数（P0 + 携带业务逻辑的 P1） | **9** |
| 已覆盖主类数 | **6** |
| **粗算覆盖率** | **6 / 9 ≈ 66.7%** |
| **P0 缺测数** | **1**（`UserStatusController`） |
| P1 缺测数 | 3（`UserStatusAuditLogMapper`、`UserStatusTransition`、`UserRestriction`） |

> 备注：`UserStatusEnumTest` 覆盖了 P2 层级的 `UserStatusEnum`，未计入"应测主类"分母。
> 备注：`UserStatusService` 与 `UserStatusAuditLogService` 均为接口，被对应 `*ServiceTest` 通过 `*ServiceImpl` 直接覆盖（`@InjectMocks UserStatusServiceImpl`）。

---

## 2. 已测主类清单（含测试类名）

| 主类 | 主类路径 | 行数 | 对应测试类 | 备注 |
| --- | --- | --- | --- | --- |
| `UserStatusServiceImpl` | `service/impl/UserStatusServiceImpl.java` | 66 | `service/UserStatusServiceTest.java` | `@InjectMocks` 注入实现类，覆盖 5 个方法 |
| `UserStatusAuditLogServiceImpl` | `service/impl/UserStatusAuditLogServiceImpl.java` | 39 | `service/UserStatusAuditLogServiceTest.java` | Mock `UserStatusAuditLogMapper` |
| `UserStatusBizManageService` | `biz/UserStatusBizManageService.java` | 169 | `biz/UserStatusBizManageServiceTest.java` | 覆盖 `changeStatus` / `forceChangeStatus` / `batchChangeStatus` |
| `UserStatusCheckAspect` | `aspect/UserStatusCheckAspect.java` | 119 | `aspect/UserStatusCheckAspectTest.java` | 覆盖 AOP 拦截各分支 |
| `UserStatusAutoReleaseScheduler` | `scheduler/UserStatusAutoReleaseScheduler.java` | 80 | `scheduler/UserStatusAutoReleaseSchedulerTest.java` | 覆盖 4 种到期解禁场景 |
| `UserStatusEnum` | `entity/UserStatusEnum.java` | 58 | `entity/UserStatusEnumTest.java` | P2，验证枚举 9 值及 `fromCode` |

---

## 3. 缺测试主类清单（按 P0 → P3 排序）

### 🔴 P0 关键（1 项）

| # | 文件:行数 | 类名 | 缺失原因 | 建议测试范围 |
| --- | --- | --- | --- | --- |
| 1 | `controller/UserStatusController.java:132` | `UserStatusController` | 5 个端点（含 `/current`、`/{userId}`、`/{userId}/change`、`/{userId}/history`、`/{userId}/release`）均无 WebMvc 测试覆盖；`buildUserStatusVO` 私有方法、`UserStatusEnum.valueOf` 解析失败、用户资料为空异常、`bizManageService.changeStatus` 调用参数装配等关键路径完全未测 | 推荐 `UserStatusControllerWebMvcTest`（`@WebMvcTest` + `MockMvc`），至少覆盖：① `getCurrentUserStatus` / `getUserStatus` 正常路径；② 用户不存在抛 `JeecgBootException`；③ `changeUserStatus` 入参到 `bizManageService` 的参数映射；④ `getUserStatusHistory` 返回列表；⑤ `releaseUserStatus` 强制 NORMAL 的目标态装配 |

### 🟡 P1 重要（3 项）

| # | 文件:行数 | 类名 | 缺失原因 | 建议测试范围 |
| --- | --- | --- | --- | --- |
| 2 | `mapper/UserStatusAuditLogMapper.java:38` | `UserStatusAuditLogMapper` | 含 2 个自定义方法 `selectByUserId`、`selectByUserIdAndTimeRange`，无 `@Mapper` 编译期或 SQL 解析期校验 | 推荐 `UserStatusAuditLogMapperCompilationTest`（`@SpringBootTest` 启动上下文 + `MybatisSqlSessionFactoryBean`），至少：① 启动期 `Mapper` 注入校验；② XML 中两个自定义方法存在性反射校验 |
| 3 | `model/UserStatusTransition.java:83` | `UserStatusTransition` | **状态机核心规则表**：9 种状态间 11 条转换规则，每条规则都直接影响 `UserStatusServiceImpl.validateStatusChange` 的合法性判断；目前仅 `UserStatusServiceTest` 黑盒覆盖了若干断言，规则表本身的"全部转换矩阵"未被穷尽验证，存在规则漂移风险 | 推荐 `UserStatusTransitionTest`：① 9×9 全转换矩阵断言（含 `DEACTIVATED` 终态空集）；② `getAllowedTransitions(null)` 边界 |
| 4 | `model/UserRestriction.java:124` | `UserRestriction` | **功能限制规则表**：8 种状态对应不同功能黑名单（`publish/comment/like/...`），直接被 `UserStatusCheckAspect` 通过 `forbid` 列表消费，规则错配会引发越权；`canLogin / canPublish / canComment / canSendMessage` 4 个公开谓词方法均无独立单测 | 推荐 `UserRestrictionTest`：① 每种状态对 8 个功能点的 `isRestricted` 矩阵断言；② 4 个 `can*` 谓词对 NORMAL/DEACTIVATED 的对偶验证；③ `getRestrictions(null)` 空集 |

### 🟢 P2 一般（0 项）

无。所有 P2 类别（Enum/Annotation/Config）要么已测，要么为空目录（`config/`）。

### ⚪ P3 跳过（4 项）

| # | 文件:行数 | 类名 | 类型 |
| --- | --- | --- | --- |
| - | `entity/UserStatusAuditLog.java:61` | `UserStatusAuditLog` | 纯 Entity（`@Data + @TableName`，无业务方法） |
| - | `req/UserStatusChangeReq.java:31` | `UserStatusChangeReq` | 纯 Req（`@Data + @Schema` + Bean Validation 注解） |
| - | `vo/UserStatusVO.java:35` | `UserStatusVO` | 纯 VO（`@Data + @Schema`） |
| - | `config/` (空目录) | - | 无类文件 |

> 备注：`UserStatusService` 与 `UserStatusAuditLogService` 两个 Service 接口本身不计入独立测试对象（其行为由对应 `*ServiceImpl` 测试覆盖）。

---

## 4. 可跳过/POJO 清单

以下文件**不需要**单元测试，按 P3 跳过：

| 文件路径 | 行数 | 跳过理由 |
| --- | --- | --- |
| `entity/UserStatusAuditLog.java` | 61 | MyBatis-Plus Entity，无业务方法 |
| `req/UserStatusChangeReq.java` | 31 | 请求 DTO，Bean Validation 由 Spring 自动触发 |
| `vo/UserStatusVO.java` | 35 | 响应 VO，纯字段 |
| `config/` | - | 空目录，无任何配置类 |

---

## 5. 风险与建议

### 5.1 最大风险点

**`UserStatusController`** 是整个 userstatus 子模块**唯一**面向 HTTP 端点的入口，包含 5 个 `@RestController` 方法。当前 0 覆盖意味着：
- 路径 `/api/content/user-status/**` 的全部契约变更（参数名、响应结构、异常码）无法被回归保护。
- `UserStatusEnum.valueOf(profile.getStatus())` 在 `status` 字段出现非枚举值时会抛 `IllegalArgumentException`（**不是** `JeecgBootException`），全模块无任何测试拦截该异常分支，prod 风险高。
- `buildUserStatusVO` 与 `changeUserStatus` / `releaseUserStatus` 重复样板代码（3 次"先查 profile，再 null 校验，再调用 biz"），无测试约束，重构极易引入回归。

### 5.2 次要风险点

`UserStatusTransition` 与 `UserRestriction` 是**两条核心规则表**，被 4 个上层类（`Service` / `BizManage` / `Aspect` / `Scheduler`）共同消费。当前仅通过 `UserStatusServiceTest` 与 `UserStatusCheckAspectTest` 的少量用例间接覆盖，规则漂移后端到端断言失效的风险较高。**强烈建议**补两份独立规则表单测，与消费者测试解耦。

### 5.3 修复优先级（建议按此顺序补齐）

1. **🔴 立即**：`UserStatusController` 端到端 MockMvc 测试。
2. **🟡 短期**：`UserStatusTransition` + `UserRestriction` 规则表全矩阵单测。
3. **🟡 短期**：`UserStatusAuditLogMapper` 启动期注入 / XML 存在性测试。
4. **🟢 远期**：`UserStatusEnumTest` 已存在且充分，无需扩展。

---

## 6. 审计元数据

- 主代码目录：`jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/userstatus/`
- 测试代码目录：`jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/userstatus/`
- 报告输出：`jeecg-boot/jeecg-boot-module/jeecg-module-content/docs/test-audit/audit-userstatus.md`
- 审计工具链：Read / Glob / Bash（只读），未对任何业务代码做修改
- 报告生成时间：2026-06-02
