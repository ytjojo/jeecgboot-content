# content/circle 模块 · 缺单元测试审计报告

> 审计范围：`jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/`
> 审计日期：2026-06-02
> 审计员：Java 单元测试审计员（只读分析，未修改任何业务代码）

---

## 1. 摘要

| 指标 | 数值 |
| --- | --- |
| 主代码 `.java` 文件总数 | **86** |
| 测试代码 `.java` 文件总数 | **29** |
| 应测主类数（P0 + 携带业务逻辑的 P1） | **42**（P0=32, P1 Mapper=10） |
| 已覆盖主类数 | **37**（P0 已测 27/32, P1 均 0） |
| **粗算覆盖率** | **37 / 42 ≈ 88.1%** |
| **P0 缺测数** | **5**（4 个 Controller + 1 个 Task） |
| P1 缺测数 | 10（全部 10 个 Mapper 缺 `*MapperCompilationTest`） |
| P2 缺测数 | 3（全部 3 个 Enum 缺 `*EnumTest`，含 `codes()` 工具方法） |
| P3 跳过数 | 39（10 entity + 8 req + 9 vo + 12 service 接口 + 2 biz 接口） |

> 备注：12 个 `ICircle*Service` 接口与 2 个 `ICircleBiz`/`ICircleMemberBiz` 接口的行为由对应 `*ServiceTest` / `*BizTest` 通过 `*ServiceImpl` / `*BizImpl` 直接覆盖（`@InjectMocks`），不计入"应测主类"分母。
> 备注：Entity 层仅 `Circle` 与 `CircleMember` 存在纯枚举值测试（`CircleTest` / `CircleMemberTest`），其余 8 个 Entity 无单测但归入 P3 跳过。

---

## 2. 已测主类清单（含测试类名）

### 2.1 biz 层（7/7 全部覆盖）

| 主类 | 主类路径 | 行数 | 对应测试类 | 备注 |
| --- | --- | --- | --- | --- |
| `CircleAnnouncementBizService` | `biz/CircleAnnouncementBizService.java` | 50 | `biz/CircleAnnouncementBizServiceTest.java` | Mock `ICircleAnnouncementService` + `ICircleAuditLogService` |
| `CircleBizImpl` | `biz/CircleBizImpl.java` | 119 | `biz/CircleBizTest.java` | 跨表编排（create/update/join/leave/transfer） |
| `CircleContentPinBizService` | `biz/CircleContentPinBizService.java` | 91 | `biz/CircleContentPinBizServiceTest.java` | 覆盖 pin / unpin / feature / unfeature |
| `CircleJoinReviewBizService` | `biz/CircleJoinReviewBizService.java` | 65 | `biz/CircleJoinReviewBizServiceTest.java` | 覆盖 approve / reject |
| `CircleMemberBizImpl` | `biz/CircleMemberBizImpl.java` | 197 | `biz/CircleMemberBizTest.java` | **最重 P0 类**（密码加密/状态机/事务回滚） |
| `CircleMentionBizService` | `biz/CircleMentionBizService.java` | 34 | `biz/CircleMentionBizServiceTest.java` | 覆盖 @ 提醒 |
| `CircleReportBizService` | `biz/CircleReportBizService.java` | 90 | `biz/CircleReportBizServiceTest.java` | 覆盖 submitReport / handleDeleteContent / handleIgnore / handleMute |

### 2.2 controller 层（6/10 已覆盖）

| 主类 | 主类路径 | 行数 | 对应测试类 | 备注 |
| --- | --- | --- | --- | --- |
| `CircleController` | `controller/CircleController.java` | 60 | `controller/CircleControllerWebMvcTest.java` | `standaloneSetup` + `@WebMvcTest` 风格 |
| `CircleDataController` | `controller/CircleDataController.java` | 49 | `controller/CircleDataControllerTest.java` | Mock `ICircleDataService` + `ICircleRankingService` |
| `CircleMemberController` | `controller/CircleMemberController.java` | 54 | `controller/CircleMemberControllerWebMvcTest.java` | 覆盖成员增删改查/角色变更 |
| `CircleRankingController` | `controller/CircleRankingController.java` | 33 | `controller/CircleRankingControllerTest.java` | 排行榜查询 |
| `CircleRecommendController` | `controller/CircleRecommendController.java` | 46 | `controller/CircleRecommendControllerTest.java` | 推荐流 |
| `CircleSearchController` | `controller/CircleSearchController.java` | 66 | `controller/CircleSearchControllerWebMvcTest.java` | 全文检索 |

### 2.3 scheduler 层（2/2 全部覆盖）

| 主类 | 主类路径 | 行数 | 对应测试类 | 备注 |
| --- | --- | --- | --- | --- |
| `CircleDataAggregationScheduler` | `scheduler/CircleDataAggregationScheduler.java` | 70 | `scheduler/CircleDataAggregationSchedulerTest.java` | 覆盖 4 种聚合场景 |
| `CircleRankingScheduler` | `scheduler/CircleRankingScheduler.java` | 42 | `scheduler/CircleRankingSchedulerTest.java` | 覆盖 cron 触发与排名刷新 |

### 2.4 service/impl 层（12/12 全部覆盖）

| 主类 | 主类路径 | 行数 | 对应测试类 | 备注 |
| --- | --- | --- | --- | --- |
| `CircleAnnouncementServiceImpl` | `service/impl/CircleAnnouncementServiceImpl.java` | 37 | `service/CircleAnnouncementServiceTest.java` | Mock `CircleAnnouncementMapper` |
| `CircleAuditLogServiceImpl` | `service/impl/CircleAuditLogServiceImpl.java` | 37 | `service/CircleAuditLogServiceTest.java` | |
| `CircleContentPinServiceImpl` | `service/impl/CircleContentPinServiceImpl.java` | 80 | `service/CircleContentPinServiceTest.java` | |
| `CircleDataServiceImpl` | `service/impl/CircleDataServiceImpl.java` | 81 | `service/CircleDataServiceTest.java` | |
| `CircleGovernanceLogServiceImpl` | `service/impl/CircleGovernanceLogServiceImpl.java` | 56 | `service/CircleGovernanceLogServiceTest.java` | |
| `CircleJoinReviewServiceImpl` | `service/impl/CircleJoinReviewServiceImpl.java` | 68 | `service/CircleJoinReviewServiceTest.java` | |
| `CircleMemberServiceImpl` | `service/impl/CircleMemberServiceImpl.java` | 73 | `service/CircleMemberServiceTest.java` | |
| `CircleMentionServiceImpl` | `service/impl/CircleMentionServiceImpl.java` | 79 | `service/CircleMentionServiceTest.java` | |
| `CircleRankingServiceImpl` | `service/impl/CircleRankingServiceImpl.java` | 72 | `service/CircleRankingServiceTest.java` | |
| `CircleRecommendServiceImpl` | `service/impl/CircleRecommendServiceImpl.java` | 112 | `service/CircleRecommendServiceTest.java` | |
| `CircleReportServiceImpl` | `service/impl/CircleReportServiceImpl.java` | 86 | `service/CircleReportServiceTest.java` | |
| `CircleServiceImpl` | `service/impl/CircleServiceImpl.java` | 34 | `service/CircleServiceTest.java` | |

### 2.5 entity 层（2/10 覆盖——仅枚举值校验）

| 主类 | 主类路径 | 行数 | 对应测试类 | 备注 |
| --- | --- | --- | --- | --- |
| `Circle` | `entity/Circle.java` | 63 | `entity/CircleTest.java` | 校验内嵌 `PrivacyType` / `JoinType` / `Status` 枚举值 |
| `CircleMember` | `entity/CircleMember.java` | 44 | `entity/CircleMemberTest.java` | 校验内嵌 `Role` / `Status` 枚举值 |

---

## 3. 缺测试主类清单（按 P0 → P3 排序）

### 🔴 P0 关键（5 项）

| # | 文件:行数 | 类名 | 缺失原因 | 建议测试范围 |
| --- | --- | --- | --- | --- |
| 1 | `controller/CircleAnnouncementController.java:56` | `CircleAnnouncementController` | 2 个端点（`POST /circle-announcement/` 发布、`GET /circle-announcement/active/{circleId}` 查询）均无 WebMvc 覆盖；`JwtUtil.getUserNameByToken` 解析失败、`BeanUtils.copyProperties` 字段映射丢失、`announcement == null` 返回空 `Result` 等关键路径完全未测 | 推荐 `CircleAnnouncementControllerWebMvcTest`（`standaloneSetup` 模式，参考 `CircleControllerWebMvcTest`）：① `publish` 正常路径：mock `circleAnnouncementBizService.publish` + 校验 `Result.OK("发布成功")`；② `publish` 入参 `BeanUtils.copyProperties` 后字段是否完整传透；③ `getActive` 返回非空时 VO 字段映射正确性；④ `getActive` 返回 null 时仍返回 `Result.OK(null)` 而非 500 |
| 2 | `controller/CircleContentPinController.java:46` | `CircleContentPinController` | 2 个端点（`PUT /circle-content/{contentId}/pin`、`PUT /circle-content/{contentId}/featured`）均无 WebMvc 覆盖；`operatorId` 装配、`circleId` `@RequestParam` 透传 Biz 等关键路径未测 | 推荐 `CircleContentPinControllerWebMvcTest`：① `togglePin` 路径变量 + `circleId` query 参数装配正确；② `toggleFeature` 同上；③ mock `circleContentPinBizService` 抛 `JeecgBootException` 时全局异常处理器返回 500 |
| 3 | `controller/CircleJoinReviewController.java:68` | `CircleJoinReviewController` | 3 个端点（`GET /circle-join-review/pending/{circleId}`、`POST /circle-join-review/approve`、`POST /circle-join-review/reject`）均无 WebMvc 覆盖；`List<CircleJoinRequest> → List<CircleJoinRequestVO>` 流式转换、`req.getRequestId()` 与 `req.getRejectReason()` 入参解析完全未测 | 推荐 `CircleJoinReviewControllerWebMvcTest`：① `getPending` 返回列表与 VO 映射（含内嵌枚举的 code/description 透传）；② `approve` 入参到 `circleJoinReviewBizService.approve(requestId, operatorId, circleId)` 的 3 参数装配；③ `reject` 第 4 参 `req.getRejectReason()` 的透传；④ 空列表返回 `Result.OK([])` |
| 4 | `controller/CircleReportController.java:93` | `CircleReportController` | **5 个端点**（`POST /circle-report/` 提交、`GET /circle-report/list/{circleId}` 列表含 `status` 过滤、`POST /{reportId}/delete-content`、`POST /{reportId}/ignore`、`POST /{reportId}/mute`）均无 WebMvc 覆盖；`status` 可选 query 参数处理、`Result<List<CircleReportVO>>` 流式映射、4 类处理动作的方法路由完全未测 | 推荐 `CircleReportControllerWebMvcTest`（5 个 case）：① `submitReport` reporterId 解析与字段拷贝；② `getReports` 不带 status / 带 status 两种分支；③ 三个 `handle*` 端点分别校验调用了 `handleDeleteContent` / `handleIgnore` / `handleMute`，operatorId 正确装配 |
| 5 | `task/CircleJoinRequestTimeoutTask.java:48` | `CircleJoinRequestTimeoutTask` | **唯一未测的定时任务 P0 类**：`@Scheduled(cron = "0 0 * * * ?")` 每小时扫描 `joinReviewService.getTimedOutRequests()`，对超时申请通过 `notificationService.sendNotification` 通知 `createBy`；try-catch 静默吞掉单条失败、空列表短路、`log.info` 计数等关键路径全部未测 | 推荐 `CircleJoinRequestTimeoutTaskTest`：① 空列表短路：不调用 `sendNotification`；② 非空列表：对每条 `CircleJoinRequest` 调用 `sendNotification(createBy, "JOIN_REQUEST_TIMEOUT", ..., ...)` 4 参数严格匹配；③ 单条 `sendNotification` 抛异常时循环继续，剩余申请仍被通知，且 `log.error` 含 `requestId`；④ `joinReviewService.getTimedOutRequests` 仅调用一次 |

### 🟡 P1 重要（10 项——Mapper 全部缺 `*MapperCompilationTest`）

> 全部 10 个 Mapper 均有自定义 SQL（`@Select` / `@Update` 注解或 XML `<foreach>`），无启动期注入与 SQL 解析期校验。优先级建议：`CircleMapper` / `CircleMemberMapper` / `CircleRecommendSourceMapper`（核心业务表 + 含 XML foreach）排在最前。

| # | 文件:行数 | 类名 | 缺失原因 | 建议测试范围 |
| --- | --- | --- | --- | --- |
| 6 | `mapper/CircleMapper.java:38` | `CircleMapper` | 含自定义 SQL，缺启动期注入 / XML 存在性测试 | `CircleMapperCompilationTest`（`@SpringBootTest` + 反射校验方法存在） |
| 7 | `mapper/CircleMemberMapper.java:60` | `CircleMemberMapper` | 含自定义 SQL | 同上 |
| 8 | `mapper/CircleAnnouncementMapper.java:21` | `CircleAnnouncementMapper` | 含自定义方法 | 同上 |
| 9 | `mapper/CircleAuditLogMapper.java:34` | `CircleAuditLogMapper` | 自定义 `selectByTarget` / `selectByTimeRange` | 同上 |
| 10 | `mapper/CircleContentMapper.java:23` | `CircleContentMapper` | 自定义 `selectCircleContentList` | 同上 |
| 11 | `mapper/CircleDataStatisticsMapper.java:19` | `CircleDataStatisticsMapper` | 含自定义 SQL | 同上 |
| 12 | `mapper/CircleGovernanceLogMapper.java:9` | `CircleGovernanceLogMapper` | 含自定义方法 | 同上 |
| 13 | `mapper/CircleJoinRequestMapper.java:30` | `CircleJoinRequestMapper` | 自定义 `selectPendingByCircleId` / `selectTimedOutRequests`（**与 Task 联动**） | 同上 |
| 14 | `mapper/CircleRecommendSourceMapper.java:24` | `CircleRecommendSourceMapper` | **含 XML `<foreach>` 批量插入**（`insertBatch`），风险最高 | 同上 + MyBatis SQL 解析断言 |
| 15 | `mapper/CircleReportMapper.java:26` | `CircleReportMapper` | 自定义 `selectByCircleAndStatus` | 同上 |

### 🟢 P2 一般（3 项——Enum 含 `codes()` 工具方法）

> 3 个 Enum 均为纯枚举 + `public static List<String> codes()` 工具方法。当前 `CircleTest` / `CircleMemberTest` 仅覆盖了 Entity 内嵌枚举，未覆盖 `org.jeecg.modules.content.circle.enums` 包下独立枚举。

| # | 文件:行数 | 类名 | 缺失原因 | 建议测试范围 |
| --- | --- | --- | --- | --- |
| 16 | `enums/CircleAuditActionEnum.java:36` | `CircleAuditActionEnum` | 含 10 个枚举值与 `codes()` 工具方法，被 `CircleAuditLogService` / Biz 层直接消费 | `CircleAuditActionEnumTest`：① 10 个值存在断言（PIN/UNPIN/FEATURE/UNFEATURE/PUBLISH_ANNOUNCEMENT/APPROVE_JOIN/REJECT_JOIN/DELETE_REPORTED/IGNORE_REPORT/MUTE_FROM_REPORT）；② `codes()` 返回 10 个元素且顺序稳定 |
| 17 | `enums/CircleJoinRequestStatusEnum.java:30` | `CircleJoinRequestStatusEnum` | 含 4 个值 + `codes()`，被 `CircleJoinRequest` 状态机消费 | `CircleJoinRequestStatusEnumTest`：① 4 值断言（PENDING/APPROVED/REJECTED/EXPIRED）；② `codes()` 顺序与不可变性 |
| 18 | `enums/CircleReportStatusEnum.java:29` | `CircleReportStatusEnum` | 含枚举值，被举报流程消费 | `CircleReportStatusEnumTest`：值存在性 + `valueOf` 行为 |

### ⚪ P3 跳过（39 项——纯 POJO/接口）

| 类别 | 文件数 | 说明 |
| --- | --- | --- |
| `entity/*` | 10 | 纯 MyBatis-Plus Entity（`@Data + @TableName`），无业务方法 |
| `req/*` | 8 | 请求 DTO（`@Data + @Schema + @Valid`），Bean Validation 由 Spring 触发 |
| `vo/*` | 9 | 响应 VO（`@Data + @Schema`），纯字段 |
| `service/ICircle*` | 12 | Service 接口，行为由 `*ServiceImpl` 测试覆盖 |
| `biz/ICircle*` | 2 | Biz 接口（`ICircleBiz` / `ICircleMemberBiz`），行为由 `*BizTest` 通过 Impl 覆盖 |

---

## 4. 可跳过/POJO 清单

以下文件**不需要**单元测试，按 P3 跳过：

| 类别 | 文件路径 | 行数 | 跳过理由 |
| --- | --- | --- | --- |
| Entity | `entity/Circle.java` | 63 | MyBatis-Plus Entity + 内嵌 3 个枚举（已被 `CircleTest` 部分覆盖） |
| Entity | `entity/CircleAnnouncement.java` | 34 | MyBatis-Plus Entity |
| Entity | `entity/CircleAuditLog.java` | 49 | MyBatis-Plus Entity |
| Entity | `entity/CircleContent.java` | 52 | MyBatis-Plus Entity |
| Entity | `entity/CircleDataStatistics.java` | 38 | MyBatis-Plus Entity |
| Entity | `entity/CircleGovernanceLog.java` | 48 | MyBatis-Plus Entity |
| Entity | `entity/CircleJoinRequest.java` | 39 | MyBatis-Plus Entity |
| Entity | `entity/CircleMember.java` | 44 | MyBatis-Plus Entity + 内嵌 2 个枚举（已被 `CircleMemberTest` 部分覆盖） |
| Entity | `entity/CircleRecommendSource.java` | 41 | MyBatis-Plus Entity |
| Entity | `entity/CircleReport.java` | 45 | MyBatis-Plus Entity |
| Req | `req/CircleAnnouncementReq.java` | 26 | 请求 DTO |
| Req | `req/CircleJoinReviewReq.java` | 20 | 请求 DTO |
| Req | `req/CircleReportReq.java` | 24 | 请求 DTO |
| Req | `req/create/CircleCreateReq.java` | 42 | 请求 DTO |
| Req | `req/create/CircleJoinReq.java` | 17 | 请求 DTO |
| Req | `req/query/CircleSearchReq.java` | 23 | 请求 DTO |
| Req | `req/update/CircleMemberUpdateReq.java` | 27 | 请求 DTO |
| Req | `req/update/CircleUpdateReq.java` | 28 | 请求 DTO |
| VO | `vo/CircleAnnouncementVO.java` | 32 | 响应 VO |
| VO | `vo/CircleDataStatisticsVO.java` | 45 | 响应 VO |
| VO | `vo/CircleJoinRequestVO.java` | 31 | 响应 VO |
| VO | `vo/CircleMemberVO.java` | 29 | 响应 VO |
| VO | `vo/CircleRankingVO.java` | 41 | 响应 VO |
| VO | `vo/CircleRecommendVO.java` | 38 | 响应 VO |
| VO | `vo/CircleReportVO.java` | 40 | 响应 VO |
| VO | `vo/CircleSearchResultVO.java` | 27 | 响应 VO |
| VO | `vo/CircleVO.java` | 56 | 响应 VO |
| Service 接口 | `service/ICircleAnnouncementService.java` | 27 | 行为由 `CircleAnnouncementServiceTest` 覆盖 |
| Service 接口 | `service/ICircleAuditLogService.java` | 38 | 行为由 `CircleAuditLogServiceTest` 覆盖 |
| Service 接口 | `service/ICircleContentPinService.java` | 52 | 行为由 `CircleContentPinServiceTest` 覆盖 |
| Service 接口 | `service/ICircleDataService.java` | 17 | 行为由 `CircleDataServiceTest` 覆盖 |
| Service 接口 | `service/ICircleGovernanceLogService.java` | 15 | 行为由 `CircleGovernanceLogServiceTest` 覆盖 |
| Service 接口 | `service/ICircleJoinReviewService.java` | 44 | 行为由 `CircleJoinReviewServiceTest` 覆盖 |
| Service 接口 | `service/ICircleMemberService.java` | 17 | 行为由 `CircleMemberServiceTest` 覆盖 |
| Service 接口 | `service/ICircleMentionService.java` | 37 | 行为由 `CircleMentionServiceTest` 覆盖 |
| Service 接口 | `service/ICircleRankingService.java` | 15 | 行为由 `CircleRankingServiceTest` 覆盖 |
| Service 接口 | `service/ICircleRecommendService.java` | 20 | 行为由 `CircleRecommendServiceTest` 覆盖 |
| Service 接口 | `service/ICircleReportService.java` | 52 | 行为由 `CircleReportServiceTest` 覆盖 |
| Service 接口 | `service/ICircleService.java` | 13 | 行为由 `CircleServiceTest` 覆盖 |
| Biz 接口 | `biz/ICircleBiz.java` | 12 | 行为由 `CircleBizTest` 通过 `CircleBizImpl` 覆盖 |
| Biz 接口 | `biz/ICircleMemberBiz.java` | 19 | 行为由 `CircleMemberBizTest` 通过 `CircleMemberBizImpl` 覆盖 |

---

## 5. 风险与建议

### 5.1 最大风险点

**4 个 P0 Controller 全部 0 覆盖**，是整个 circle 子模块 HTTP 协议边界最大的回归盲区：

- **`CircleReportController`（93 行 / 5 端点）** —— 涵盖举报提交、列表查询、删除内容、忽略、禁言五类动作，是**内容治理主入口**。其 `getReports` 的 `status` 可选参数处理、3 类 `handle*` 端点的方法路由、`List<CircleReport> → List<CircleReportVO>` 的流式转换，任何路径参数或 Bean 映射错误都会让审核后台静默失败。
- **`CircleJoinReviewController`（68 行 / 3 端点）** —— 申请审核的协议边界，与 `CircleJoinRequestTimeoutTask`（也是未测 P0）形成**完整链路上无单测**：`Task` 调 `joinReviewService.getTimedOutRequests` → 管理员进 `getPending` → `approve` / `reject` 调 `Biz` → 状态机跳转。
- **`CircleAnnouncementController`（56 行 / 2 端点）** —— 公告发布是高频被引用的资源（圈子主页、消息中心），`BeanUtils.copyProperties(req, announcement)` 字段漏拷不会被任何 CI 拦截。
- **`CircleContentPinController`（46 行 / 2 端点）** —— 置顶/精华直接影响信息流排序，与 `CircleRankingService` 联动，未测意味着排序合约无回归保护。

### 5.2 次要风险点

1. **`CircleJoinRequestTimeoutTask`（48 行）** —— 唯一未测的 `@Scheduled` P0 类。其 `try-catch` 静默吞掉单条失败、`log.info` 计数 + `log.error` 含 `requestId` 等可观测性代码无任何断言；prod 告警失效时无法通过单测发现。
2. **10 个 Mapper 全部无 `*MapperCompilationTest`** —— `CircleMapper` / `CircleMemberMapper` / `CircleRecommendSourceMapper` 尤其关键：含自定义 SQL 注解与 XML `<foreach>`（`insertBatch`），启动期无注入校验意味着 Mapper 改名后延迟到运行时才暴露。
3. **3 个独立 Enum 缺 `*EnumTest`** —— `CircleAuditActionEnum`（10 值）/ `CircleJoinRequestStatusEnum`（4 值）/`CircleReportStatusEnum` 的 `codes()` 工具方法被 Biz 层直接消费，值变更不会被单测拦截。

### 5.3 修复优先级（建议按此顺序补齐）

1. **🔴 立即**：补齐 4 个 Controller 的 `*ControllerWebMvcTest`（参考 `CircleControllerWebMvcTest` / `CircleMemberControllerWebMvcTest` 的 `standaloneSetup` 模板）。
2. **🔴 立即**：补齐 `CircleJoinRequestTimeoutTask` 的 4 个 case（空列表短路、批量通知、单条失败隔离、依赖仅调用一次）。
3. **🟡 短期**：补 3 个核心 Mapper 的 `*MapperCompilationTest`（`CircleMapper` / `CircleMemberMapper` / `CircleRecommendSourceMapper`）。
4. **🟢 远期**：补 3 个 `*EnumTest`（`CircleAuditActionEnum` 优先——值最多且被 4 个 Biz 消费）。
5. **🟢 远期**：补剩余 7 个 Mapper 的编译期测试。

---

## 6. 审计元数据

- 主代码目录：`jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/circle/`
- 测试代码目录：`jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/circle/`
- 报告输出：`jeecg-boot/jeecg-boot-module/jeecg-module-content/docs/test-audit/audit-circle.md`
- 审计工具链：Read / Bash（只读），未对任何业务代码做修改
- 报告生成时间：2026-06-02

---

## 7. 总结

审计了 `content/circle` 子模块 **86 个主 Java 文件**，识别出 **5 个 P0 缺测**（4 个 Controller + 1 个 Task）、**10 个 P1 缺测**（Mapper）、**3 个 P2 缺测**（Enum），P0 粗算覆盖率 **27/32 ≈ 84.4%**。最大风险是 4 个完全无 WebMvc 测试的 Controller（涵盖公告/置顶/审核/举报核心 HTTP 端点），其协议边界一旦回归将无法被 CI 拦截；次大风险是 `CircleJoinRequestTimeoutTask` 与 `CircleJoinReviewController` 形成"无单测链路"，定时任务失败不会被发现。
