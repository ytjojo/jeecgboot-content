# JeecgBoot-Sass 内容社区模块 · 单元测试覆盖审计总报告

> 审计时间：2026-06-02
> 审计范围：`jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/`
> 审计方式：6 个独立 subagent 并行审查（按子模块拆分），最终由主 agent 汇总
> 项目分支：`springboot3_content`（仅审计，未修改任何业务代码）

---

## 一、总体数据

| 指标 | 数值 |
| --- | --- |
| 主代码 `.java` 文件总数 | **803** |
| 测试代码 `.java` 文件总数 | **176**（其中 `test/` 176 + `main/test/` 3 已废弃） |
| 子模块数 | 5 个核心（auth / channel / circle / user / userstatus）+ 1 个公共（common） |
| 应测主类总数（去 POJO/Enum/Constant/Model/DTO/VO/Req） | **~213** |
| 已测主类（直接覆盖） | **~141** |
| **P0 缺测主类（关键：Controller / Biz / Service / Scheduler / Task / Aspect）** | **🔴 87 个** |
| P1 缺测主类（Mapper 为主） | **🟡 92 个** |
| P2 缺测主类（Enum 含业务方法 / Util / Config） | **🟢 12 个** |
| **整体 P0 粗算覆盖率** | **≈ 56.6 %**（含弱覆盖可至 ~73 %） |

> 覆盖率 = 已覆盖应测主类 / 应测主类。Controller / BizManageService / Service（含 Impl）/ Scheduler / Task / Aspect 视为 P0。

---

## 二、各子模块摘要

| 子模块 | 主文件 | 测试文件 | 应测主类 | 已测 | P0 缺测 | P1 缺测 | P2 缺测 | P0 覆盖率 | 详细报告 |
| --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | --- |
| **content/auth**       |  79 |  27 |  25 |  22 |   3 |   0 |  0 | **88.0 %** | [audit-auth.md](./audit-auth.md) |
| **content/channel**    | 255 |  48 | 110 |  67 |  44 |  28 |  0 | **~60.9 %** | [audit-channel.md](./audit-channel.md) |
| **content/circle**     |  86 |  29 |  32 |  27 |   5 |  10 |  3 | **84.4 %** | [audit-circle.md](./audit-circle.md) |
| **content/user**       | 361 |  66 | 176 |  81 |  44 |  51 |  ~ | **46.0 %**（含弱覆盖 81.8 %） | [audit-user.md](./audit-user.md) |
| **content/userstatus** |  16 |   6 |   9 |   6 |   1 |   3 |  0 | **66.7 %** | [audit-userstatus.md](./audit-userstatus.md) |
| **common + 顶层包**   |   6 |   0 |   5 |   0 |   1 |   4 |  0 | **0 %** | [audit-common-and-top-level.md](./audit-common-and-top-level.md) |
| **合计** | **803** | **176** | **~357** | **~203** | **🔴 98** | **96** | **3** | **~57 %** | — |

> 注：`content/user` 的 P0 数含 32 个完全无测 + 12 个弱覆盖 = 44 个 P0 风险点。

---

## 三、Top 15 高风险未测主类（建议优先补测）

下表按"代码量 × 业务核心度 × 无回归保护后果"综合排序，是首批补测清单的"必选"。

| # | 类名 | 文件路径 | 行数 | 类型 | 风险描述 |
| --- | --- | --- | ---: | --- | --- |
| 1 | `ChannelScheduledTask` | `content/channel/task/` | **328** | Scheduler/Task | 5 个定时入口、6+ Service 编排、定时任务失败无任何 CI 拦截 |
| 2 | `ChannelBizManageService` | `content/channel/biz/` | **256** | BizManageService | 跨 3 领域 `@Transactional` 编排，零测试却承担核心业务流转 |
| 3 | `SystemUserAccountGatewayImpl` | `content/user/gateway/` | **263** | ServiceImpl | 平台账号注册 / 密码重置核心路径无任何测试 |
| 4 | `ContentUserGrowthController` | `content/user/growth/controller/` | **316** | Controller | 最大未测 Controller，多端点 HTTP 协议边界无回归网 |
| 5 | `ContentRiskControlBizService` | `content/auth/biz/` | — | BizManageService | 风控主链路（虽已在测，但建议复核覆盖深度） |
| 6 | `CircleJoinRequestTimeoutTask` | `content/circle/task/` | — | Task | 圈子加入超时处理定时任务，零单测 |
| 7 | `CircleAnnouncementController` | `content/circle/controller/` | — | Controller | 公告发布 HTTP 边界全无 WebMvc 测试 |
| 8 | `CircleContentPinController` | `content/circle/controller/` | — | Controller | 圈子内容置顶 HTTP 边界全无测试 |
| 9 | `CircleJoinReviewController` | `content/circle/controller/` | — | Controller | 加入审核 HTTP 边界全无测试 |
| 10 | `CircleReportController` | `content/circle/controller/` | — | Controller | 举报 HTTP 边界全无测试 |
| 11 | `UserStatusController` | `content/userstatus/controller/` | — | Controller | 5 个端点 + `UserStatusEnum.valueOf` 脏数据抛 `IllegalArgumentException`（应为 `JeecgBootException`），HTTP 契约无回归保护 |
| 12 | `IContentUserPasswordHistoryService` | `content/auth/service/` | — | Service 接口 | 密码历史主链路（Impl 单方法直传，建议补回归底线） |
| 13 | `IContentRiskEventService` | `content/auth/service/` | — | Service 接口 | 风控事件主链路 |
| 14 | `IContentCancellationRequestService` | `content/auth/service/` | — | Service 接口 | 账号注销主链路 |
| 15 | `MybatisEnumTypeHandlar` | `common/converters/` | — | MyBatis Handler | **吞错风险**：`getNullableResult` 在 `SQLException` 路径上 `log.error` 后返回 `null`，会把 DB 异常静默遮蔽为"字段为空"，与全模块 `@EnumValue` 持久化强耦合却无任何测试 |

---

## 四、按优先级分类的整改路线图

### 🔴 P0-A 紧急（1 周内补完，回归网核心）
1. **`ChannelScheduledTask`** + **`ChannelBizManageService`**：Channel 模块最危险的两个零测核心
2. **`SystemUserAccountGatewayImpl`** + **`ContentUserGrowthController`**：user 模块最大零测
3. **`UserStatusController`**：HTTP 契约 + 异常路径未保护
4. **`CircleJoinRequestTimeoutTask`** + 4 个 Circle Controller：补 `*WebMvcTest`
5. **`MybatisEnumTypeHandlar`**：增加 3 个重载 + 吞错路径测试
6. auth 三个 Impl 单方法直传：补"直传回归底线"测试（每个 5 分钟）

### 🟡 P0-B 重要（2 周内补完，HTTP 边界全保护）
- 7 个 `content/user` 未测 Controller：补 `*WebMvcTest`
  - `ContentUserRelationController` 已测，跳过
  - 治理 / 订阅 / 客服 / 邀请 / 分析 / 设置 Controller 全部缺测
- 4 个 `content/user` 未测 ServiceImpl（user 模块中弱覆盖或零覆盖）

### 🟢 P1 持续推进（Mapper Contract Test）
- 92 个 P1 缺测主类，**绝大部分是 Mapper**（51 个 user / 28 个 channel / 10 个 circle / 3 个 userstatus）
- 建议统一引入 `MyBatis-Plus` 的 `BaseMapperTest` 模式或 `@DataJdbcTest` 风格的"Mapper Contract Test"框架
- 优先级：行数 > 15 行的 Mapper 优先（如 `ChannelStatsMapper` 含自定义 XML SQL，必须先测）

### ⚪ P2 / P3 建议（与业务重构合并处理）
- 12 个含业务方法的 Enum 缺测
- 3 个 Util / Config 缺测
- 重构：移除废弃的 `src/main/test/java/`（仅 3 个文件，与标准 `src/test/java/` 重名冲突）

---

## 五、跨子模块共性问题

1. **Controller 覆盖率严重不均**：
   - `content/auth` Controller 全覆盖 ✅
   - `content/channel` 18 个 Controller **全军覆没** ❌
   - `content/circle` 4 个 Controller **全军覆没** ❌
   - `content/user` 7 个 Controller 缺测 ❌
   - **根因**：channel / circle / user 三个子模块的 Controller 普遍未走 `WebMvcTest` 模式

2. **Mapper 整体弱覆盖**：
   - 92 个 Mapper 几乎全部仅靠 `ContentXxxMapperCompilationTest` 验证"能编译"，未验证 SQL 行为
   - 建议补"MyBatis-Plus 契约测试"统一框架

3. **Scheduler / Task 类普遍零测**：
   - `ChannelScheduledTask` / `CircleJoinRequestTimeoutTask` / `CircleGrowthScheduler` / `ContentFanTrendAggregationTask` 均无测试
   - 定时任务失败后只能线上发现，CI 无法拦截

4. **BizManageService 编排类部分零测**：
   - `ChannelBizManageService`（256 行，跨 3 领域）零测
   - `ContentRiskControlBizService` 等已测，可作为模板参考

5. **`MybatisEnumTypeHandlar` 是被忽略的"公共代码炸弹"**：
   - 全模块 `@EnumValue` 持久化强依赖，但零测试
   - 吞错逻辑一旦在生产触发，数据问题将被静默遮蔽

---

## 六、详细报告索引

| 子模块 | 报告文件 | 行数 |
| --- | --- | ---: |
| content/auth       | [audit-auth.md](./audit-auth.md) | ~132 |
| content/channel    | [audit-channel.md](./audit-channel.md) | ~414 |
| content/circle     | [audit-circle.md](./audit-circle.md) | ~226 |
| content/user       | [audit-user.md](./audit-user.md) | ~520 |
| content/userstatus | [audit-userstatus.md](./audit-userstatus.md) | ~70 |
| common + 顶层包   | [audit-common-and-top-level.md](./audit-common-and-top-level.md) | ~80 |

---

## 七、审计方法学说明

- **零侵入**：仅 `find` / `grep` / `read` 操作，未修改任何业务代码
- **匹配规则**：
  - `XxxController` ↔ `XxxControllerTest` / `XxxControllerWebMvcTest`
  - `XxxService` / `XxxServiceImpl` ↔ `XxxServiceTest` / `XxxBizServiceTest` / `XxxCrudContractTest` 等
  - `XxxBiz` / `XxxBizService` ↔ `XxxBizServiceXxxTest` / `XxxBoundaryBizServiceTest` 等
  - `XxxMapper` ↔ `XxxMapperCompilationTest`
  - `XxxScheduler` / `XxxTask` / `XxxAspect` ↔ 同名测试
- **覆盖率口径**：粗算 = 已测主类 / 应测主类；POJO/DTO/VO/Req/Constant 不计入"应测"
- **优先级定义**：
  - P0 = 关键业务路径（Controller / Biz / Service / Scheduler / Task / Aspect）
  - P1 = 数据访问层（Mapper）
  - P2 = 业务规则类（Enum / Util / Config）
  - P3 = 数据载体（Entity / DTO / VO / Req / Model）—— 不计入"缺失"

---

> **审计完成时间**：2026-06-02 22:40
> **总文件审计耗时**：约 13 分钟（6 个 subagent 并行）
> **后续建议**：按 §四 路线图分 4 阶段推进，预计总投入 6-8 人周可达 P0 全覆盖。
