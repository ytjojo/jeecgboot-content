# 单元测试审计报告 — `org.jeecg.modules.common.*` 及顶层非 `content/` 包

> 审计范围：`jeecg-module-content` 模块下 `org.jeecg.modules.common.*`（含 `converters/`、`enums/`），以及 `org.jeecg.modules` 下其余 11 个顶层目录（`Interaction` / `article` / `channel` / `circle` / `note` / `notifcation` / `permission` / `post` / `question` / `userprofile` / `video`）。
>
> 审计方式：只读扫描，不修改任何业务代码。

---

## 1. 摘要

| 指标 | 数量 |
| --- | --- |
| 待审计主文件总数 | **6** |
| 顶层非 `content/` 目录中额外主文件 | 0（11 个目录均为空，仅 `channel/` 残留 `.DS_Store`） |
| 与审计范围匹配的单测文件数 | **0** |
| `src/test/.../common/**` 目录 | **不存在** |
| 应测主类数 | **5** |
| 可跳过主类数 | **1**（`BaseEnum` 接口） |
| **缺失测试数** | **5 / 5**（覆盖率 0%） |

> 整体结论：审计范围内主代码 100% 缺测试，其中包含 1 个 **P0**（MyBatis 枚举类型转换器），4 个 **P1**（含业务方法的枚举）。

---

## 2. 顶层非 `content/` 目录扫描结果

下列 11 个目录经 `find -type f -name "*.java"` 扫描均为 0 个 java 文件，无需审计：

| 目录 | java 文件数 | 备注 |
| --- | --- | --- |
| `org.jeecg.modules.Interaction` | 0 | 空目录 |
| `org.jeecg.modules.article` | 0 | 空目录 |
| `org.jeecg.modules.channel` | 0 | 仅含 macOS `.DS_Store` |
| `org.jeecg.modules.circle` | 0 | 空目录 |
| `org.jeecg.modules.note` | 0 | 空目录 |
| `org.jeecg.modules.notifcation` | 0 | 空目录（拼写错误，建议后续清理） |
| `org.jeecg.modules.permission` | 0 | 空目录 |
| `org.jeecg.modules.post` | 0 | 空目录 |
| `org.jeecg.modules.question` | 0 | 空目录 |
| `org.jeecg.modules.userprofile` | 0 | 空目录 |
| `org.jeecg.modules.video` | 0 | 空目录 |

> 这些是预留包名占位，不在本次测试审计对象范围内。建议在仓库层面统一清理（与本任务无关，仅提示）。

---

## 3. 主文件清单

主代码目录：`src/main/java/org/jeecg/modules/common/`

| # | 文件路径（相对 `src/main/java/`） | 行数 | 类型 | 含业务方法 |
| --- | --- | ---: | --- | --- |
| 1 | `org/jeecg/modules/common/converters/MybatisEnumTypeHandlar.java` | 156 | Converter (MyBatis `BaseTypeHandler`) | **是** |
| 2 | `org/jeecg/modules/common/enums/BaseEnum.java` | 29 | Interface | — |
| 3 | `org/jeecg/modules/common/enums/EnableStatusEnum.java` | 119 | Enum | **是** |
| 4 | `org/jeecg/modules/common/enums/MemberJoinStatusEnum.java` | 150 | Enum | **是** |
| 5 | `org/jeecg/modules/common/enums/VerifyStatus.java` | 119 | Enum | **是** |
| 6 | `org/jeecg/modules/common/enums/VerifyType.java` | 128 | Enum | **是** |

---

## 4. 已测主类清单

**无**。

`grep -r "MybatisEnumTypeHandlar\|EnableStatusEnum\|MemberJoinStatusEnum\|VerifyStatus\|VerifyType" src/test --include="*.java"` 返回 0 条匹配。整个 `src/test/.../common/**` 目录不存在。所有 6 个主类均无任何形式（直接 / 间接 / EnumCompilation 风格）的测试覆盖。

> 旁证：模块内已有 `ContentUserEnumContractTest`、`UserStatusEnumTest`、`AuthEnumCompilationTest` 等枚举契约测试模式可参考，但都未覆盖本批枚举。

---

## 5. 缺测试主类清单（按优先级）

### 5.1 P0 — `MybatisEnumTypeHandlar`（必须补）

| 字段 | 内容 |
| --- | --- |
| 文件 | `src/main/java/org/jeecg/modules/common/converters/MybatisEnumTypeHandlar.java:25` |
| 行数 | 156 |
| 类名 | `MybatisEnumTypeHandlar<T extends BaseEnum>` |
| 缺失原因 | 继承 MyBatis `BaseTypeHandler`，是 ORM 层枚举读写唯一通道；含私有 `loadEnum()` 解析逻辑（支持 `getValue().toString()`、`Enum.name()`、`getName()` 三种匹配方式，未匹配时抛 `IllegalArgumentException`）。所有 `@EnumValue` 字段的持久化都依赖此 Handler，**与数据库枚举值映射直接耦合**。 |
| 风险点 | 1) DB 中存的旧值（如 `Enum.name()` 形式）与代码枚举 `getValue()` 形式混淆时，`loadEnum` 三段匹配顺序错就可能误命中；2) `setNonNullParameter` 直接用 `jdbcType.TYPE_CODE` 而非 `Types.VARCHAR/INTEGER`，数据库驱动差异下可能写入错误类型；3) `getNullableResult` 三个重载均 `try/catch SQLException` 后吞掉异常返回 `null`，会**静默丢失数据**，无日志外暴露。 |
| 建议测试范围 | （mock `PreparedStatement` / `ResultSet` / `CallableStatement`）① 三个 `getNullableResult` 重载：DB 返回 `null` → 返回 `null`；② DB 返回匹配 `getValue().toString()` → 返回正确枚举；③ DB 返回匹配 `Enum.name()` → 返回正确枚举；④ DB 返回匹配 `getName()` → 返回正确枚举；⑤ 不匹配值 → 抛 `IllegalArgumentException`；⑥ `setNonNullParameter`：parameter 为 `null` 调用 `setNull`；非 null 时 `setObject` 携带 `getValue()`；⑦ SQL 异常被捕获并返回 `null`（验证静默吞错行为是否符合预期）。 |

### 5.2 P1 — `EnableStatusEnum`

| 字段 | 内容 |
| --- | --- |
| 文件 | `src/main/java/org/jeecg/modules/common/enums/EnableStatusEnum.java:16` |
| 行数 | 119 |
| 类名 | `EnableStatusEnum` |
| 含方法 | `getByValue(Integer)` / `getByName(String)` / `isNormal()` / `isDisabled()` / `isReviewing()` |
| 缺失原因 | 含 5 个业务方法；状态判定（`isNormal` / `isDisabled` / `isReviewing`）用于多模块启用/审核逻辑，是 channel/circle/auth 多处判断的依据。 |
| 建议测试范围 | ① `getByValue(0/1/2/3/-1)` 返回对应枚举；② `getByValue(null)` 返回 `null`；③ `getByValue(99)` 返回 `null`；④ `getByName("enabled"/...)` 返回正确枚举；⑤ `getByName(null/""/"  ")` 返回 `null`；⑥ `getByName("unknown")` 返回 `null`；⑦ 5 个 `is*` 方法覆盖每个枚举常量。 |

### 5.3 P1 — `MemberJoinStatusEnum`

| 字段 | 内容 |
| --- | --- |
| 文件 | `src/main/java/org/jeecg/modules/common/enums/MemberJoinStatusEnum.java:17` |
| 行数 | 150 |
| 类名 | `MemberJoinStatusEnum` |
| 含方法 | `getByValue(Integer)` / `getByName(String)` / `isActive()` / `isInactive()` / `isPending()` |
| 缺失原因 | 成员加圈状态机核心，6 个状态被 `isActive/isInactive/isPending` 三段划分（`EXITED/KICKED/REJECTED` 才算非活跃）。`isInactive` / `isPending` 漏状态或状态变更会直接影响圈子成员权限判定。 |
| 建议测试范围 | ① `getByValue(0..5)` 命中；② 非法值 / `null` 返回 `null`；③ `getByName` 同上；④ `isActive` 仅 `NORMAL` 为 true；⑤ `isInactive` 仅 `EXITED/KICKED/REJECTED` 为 true（**特别注意 `PENDING/INVITING` 不应被算作非活跃**）；⑥ `isPending` 仅 `PENDING/INVITING` 为 true；⑦ 三个状态集合覆盖所有 6 个枚举常量（无遗漏、无重叠）。 |

### 5.4 P1 — `VerifyStatus`

| 字段 | 内容 |
| --- | --- |
| 文件 | `src/main/java/org/jeecg/modules/common/enums/VerifyStatus.java:19` |
| 行数 | 119 |
| 类名 | `VerifyStatus` |
| 含方法 | `getByCode(Integer)` / `isVerified()` / `isVerifying()` / `canVerify()` |
| 缺失原因 | 认证状态机，`canVerify` 决定能否发起认证（仅 `NOT_VERIFIED / VERIFY_FAILED` 可发起）。改名后下层 Service 全部静默失效。 |
| 建议测试范围 | ① `getByCode(0..3)` 命中；② 非法值 / `null` 返回 `null`；③ `isVerified` 仅 `VERIFIED` 为 true；④ `isVerifying` 仅 `VERIFYING` 为 true；⑤ `canVerify` 仅 `NOT_VERIFIED / VERIFY_FAILED` 为 true（`VERIFYING/VERIFIED` 不可重复发起）。 |

### 5.5 P1 — `VerifyType`

| 字段 | 内容 |
| --- | --- |
| 文件 | `src/main/java/org/jeecg/modules/common/enums/VerifyType.java:19` |
| 行数 | 128 |
| 类名 | `VerifyType` |
| 含方法 | `getByValue(Integer)` / `isPersonal()` / `isEnterprise()` / `isInstitution()` / `isOfficialType()` / `requiresVerificationMaterial()` |
| 缺失原因 | 6 种认证类型，`isOfficialType` 涵盖 4 种（`BIG_V/OFFICIAL/ENTERPRISE/INSTITUTION`），`requiresVerificationMaterial` 排除 `NONE` —— 是认证提交表单是否要求材料的硬规则。 |
| 建议测试范围 | ① `getByValue(0..5)` 命中；② 非法值 / `null` 返回 `null`；③ `isPersonal/Enterprise/Institution` 各自单值判断；④ `isOfficialType` 仅在 `BIG_V/OFFICIAL/ENTERPRISE/INSTITUTION` 为 true（`PERSONAL` 不是官方类，必须测）；⑤ `requiresVerificationMaterial` 仅 `NONE` 为 false，其他 5 个全部为 true。 |

---

## 6. 可跳过清单

| 文件 | 行数 | 跳过理由 |
| --- | ---: | --- |
| `org/jeecg/modules/common/enums/BaseEnum.java` | 29 | **纯接口契约**，仅声明 `getValue()` / `getName()` / `getDescription()` 三个抽象方法，无任何实现逻辑。覆盖该接口的契约测试应放在 4 个实现枚举的测试里（参见上节 P1 项），无需独立测试文件。 |

---

## 7. 风险摘要（Top 3）

1. **MyBatis 枚举 Handler 静默吞错** — `MybatisEnumTypeHandlar.getNullableResult` 在 `SQLException` 路径上 `log.error` 后返回 `null`，DB 端异常会被遮蔽为"该字段为空"，下游业务可能因此误判"未认证/未启用"。
2. **`MemberJoinStatusEnum` 状态集合可能错位** — `isInactive` 与 `isPending` 共同覆盖 5 个非 `NORMAL` 状态，缺少互斥测试时若有人改判定条件，可能出现"既是 inactive 又是 pending"或两者都不算的孤儿状态。
3. **`VerifyType.isOfficialType` 与 `requiresVerificationMaterial` 互推** — `isOfficialType` 排除 `PERSONAL`，而 `requiresVerificationMaterial` 排除 `NONE`，两者集合不同。规则调整时若不同步两处，会出现"官方类不要求材料"或"个人认证要机构材料"之类的错配。

---

## 8. 建议落地路径

按 P0 → P1 顺序补齐 5 个测试类，建议目录：

```
src/test/java/org/jeecg/modules/common/
├── converters/MybatisEnumTypeHandlarTest.java        (P0, mock 三个 Statement/ResultSet)
└── enums/
    ├── EnableStatusEnumTest.java
    ├── MemberJoinStatusEnumTest.java
    ├── VerifyStatusTest.java
    └── VerifyTypeTest.java
```

> 模板可参考同模块已存在的 `ContentUserEnumContractTest.java`、`UserStatusEnumTest.java`、`AuthEnumCompilationTest.java`，保持风格一致。

