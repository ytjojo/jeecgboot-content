# Content User Growth Penalty Source Modeling Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 为 `content/user` 模块补齐成长处罚主记录的来源建模能力，让治理处罚入口和举报处理入口都能稳定建档，并为后续处罚执行引擎预留统一扩展边界。

**Architecture:** 保留现有 `ContentUserGrowthPenaltyRecord` 作为唯一主记录，不新增来源子表。通过扩展主表字段 `sourceType/sourceId/sourceStatus`，新增独立的 `IContentUserGrowthPenaltyRecordService` 统一承接治理入口和举报入口的建档逻辑；`ContentUserGovernanceServiceImpl` 与 `ContentUserSupportServiceImpl` 只负责传入上下文并维持现有编排顺序。

**Tech Stack:** Spring Boot 3, JeecgBoot, MyBatis-Plus, Flyway SQL, JUnit 5, Mockito

---

## 文件边界

### 新增文件

- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/IContentUserGrowthPenaltyRecordService.java`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/impl/ContentUserGrowthPenaltyRecordServiceImpl.java`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserGrowthPenaltyRecordServiceTest.java`
- `docs/superpowers/plans/2026-05-02-content-user-growth-penalty-source-modeling-plan.md`

### 修改文件

- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/resources/flyway/sql/mysql/V3.9.1_51__content_user_growth_penalty_recovery.sql`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/entity/ContentUserGrowthPenaltyRecord.java`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/impl/ContentUserGovernanceServiceImpl.java`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/impl/ContentUserSupportServiceImpl.java`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserGovernanceServiceTest.java`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserSupportServiceTest.java`
- `docs/superpowers/specs/2026-05-02-content-user-prd-backend-coverage-report.md`
- `docs/superpowers/plans/2026-05-02-content-user-support-governance-gap-plan.md`

## Task 1: 锁定入口建档红灯

**Files:**
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserGovernanceServiceTest.java`
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserSupportServiceTest.java`

- [ ] **Step 1: 在治理服务测试中增加“处罚态建档”红灯**

```java
@Mock
private IContentUserGrowthPenaltyRecordService growthPenaltyRecordService;

@Test
void shouldCreateGrowthPenaltyRecordWhenStatusChangesToMuted() {
    ContentUserStatusChangeReq req = changeReq("u1", ContentUserStatusEnum.MUTED.getCode());

    governanceService.changeStatus(req);

    verify(statusRecordMapper).insert(any(ContentUserStatusRecord.class));
    verify(profileMapper).updateById(argThat((ContentUserProfile it) ->
        "u1".equals(it.getUserId()) && "MUTED".equals(it.getStatus())));
    verify(growthPenaltyRecordService).createFromGovernanceRecord(
        argThat((ContentUserStatusRecord it) ->
            "u1".equals(it.getUserId()) && "MUTED".equals(it.getTargetStatus())),
        argThat((ContentUserStatusChangeReq it) ->
            "u1".equals(it.getUserId()) && "admin".equals(it.getOperatorUserId())),
        any(Date.class)
    );
}
```

- [ ] **Step 2: 在治理服务测试中增加“非处罚态不建档”红灯**

```java
@Test
void shouldNotCreateGrowthPenaltyRecordWhenStatusChangesToNormal() {
    ContentUserStatusChangeReq req = new ContentUserStatusChangeReq()
        .setUserId("u1")
        .setCurrentStatus(ContentUserStatusEnum.REGISTERED_INCOMPLETE.getCode())
        .setTargetStatus(ContentUserStatusEnum.NORMAL.getCode())
        .setOperatorUserId("admin")
        .setReason("资料补全");

    governanceService.changeStatus(req);

    verifyNoInteractions(growthPenaltyRecordService);
}
```

- [ ] **Step 3: 在支持服务测试中增加“处罚性举报结论建档”红灯**

```java
@Mock
private IContentUserGrowthPenaltyRecordService growthPenaltyRecordService;

@Test
void shouldCreateGrowthPenaltyRecordWhenReportIsConfirmed() {
    ContentUserReport report = new ContentUserReport()
        .setUserId("u1")
        .setStatus("PENDING")
        .setReportType("SPAM")
        .setTargetType("CONTENT")
        .setTargetId("post-1");
    report.setId("report-1");
    when(reportMapper.selectById("report-1")).thenReturn(report);

    supportService.handleReport(createHandleReportReq());

    verify(growthPenaltyRecordService).createFromReportHandle(
        argThat((ContentUserReport it) ->
            "report-1".equals(it.getId()) && "CONFIRMED".equals(it.getResultStatus())),
        argThat((ContentReportHandleReq it) ->
            "report-1".equals(it.getReportId()) && "CONFIRMED".equals(it.getResultStatus())),
        isNull(),
        any(Date.class)
    );
}
```

- [ ] **Step 4: 在支持服务测试中增加“非处罚性举报结论不建档”红灯**

```java
@Test
void shouldNotCreateGrowthPenaltyRecordWhenReportResultIsRejected() {
    ContentUserReport report = new ContentUserReport()
        .setUserId("u1")
        .setStatus("PENDING")
        .setReportType("SPAM");
    report.setId("report-1");
    when(reportMapper.selectById("report-1")).thenReturn(report);

    ContentReportHandleReq req = createHandleReportReq()
        .setResultStatus("REJECTED")
        .setResultNote("证据不足");

    supportService.handleReport(req);

    verifyNoInteractions(growthPenaltyRecordService);
}
```

- [ ] **Step 5: 运行聚焦测试，确认当前失败**

Run:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH="$JAVA_HOME/bin:$PATH"
mvn -pl jeecg-boot-module/jeecg-module-content \
  -Dtest=ContentUserGovernanceServiceTest,ContentUserSupportServiceTest test
```

Expected:

```text
Failures: 2 or more
Wanted but not invoked: growthPenaltyRecordService.createFromGovernanceRecord(...)
Wanted but not invoked: growthPenaltyRecordService.createFromReportHandle(...)
```

- [ ] **Step 6: 记录测试基线检查点**

```text
当前 worktree 已存在较多未提交改动。
本任务不做 git commit，只保留红灯基线并继续下一任务。
```

## Task 2: 扩展成长处罚主表来源字段

**Files:**
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/resources/flyway/sql/mysql/V3.9.1_51__content_user_growth_penalty_recovery.sql`
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/entity/ContentUserGrowthPenaltyRecord.java`

- [ ] **Step 1: 扩展 Flyway SQL，新增来源字段与索引**

```sql
CREATE TABLE IF NOT EXISTS `content_user_growth_penalty_record` (
  `id` varchar(32) NOT NULL COMMENT '主键ID',
  `user_id` varchar(32) NOT NULL COMMENT '用户ID',
  `governance_record_id` varchar(32) DEFAULT NULL COMMENT '治理状态记录ID',
  `appeal_id` varchar(32) DEFAULT NULL COMMENT '关联申诉ID',
  `source_type` varchar(64) DEFAULT NULL COMMENT '处罚来源类型',
  `source_id` varchar(64) DEFAULT NULL COMMENT '处罚来源业务主键',
  `source_status` varchar(64) DEFAULT NULL COMMENT '处罚来源状态快照',
  `penalty_type` varchar(32) NOT NULL COMMENT '处罚类型',
  `effect_snapshot_json` text NOT NULL COMMENT '处罚影响快照JSON',
  `status` varchar(32) NOT NULL DEFAULT 'PENDING_RECOVER' COMMENT '恢复状态',
  `recover_trigger` varchar(64) DEFAULT NULL COMMENT '恢复触发来源',
  `recover_reason` varchar(255) DEFAULT NULL COMMENT '恢复原因',
  `recovered_by` varchar(32) DEFAULT NULL COMMENT '恢复操作人',
  `recovered_at` datetime DEFAULT NULL COMMENT '恢复时间',
  `create_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_content_user_growth_penalty_user` (`user_id`,`status`,`create_time`),
  KEY `idx_content_user_growth_penalty_governance` (`governance_record_id`,`status`),
  KEY `idx_content_user_growth_penalty_appeal` (`appeal_id`,`status`),
  KEY `idx_content_user_growth_penalty_source` (`source_type`,`source_id`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='内容社区成长处罚恢复记录';
```

- [ ] **Step 2: 扩展实体字段**

```java
@Schema(description = "处罚来源类型")
private String sourceType;

@Schema(description = "处罚来源业务主键")
private String sourceId;

@Schema(description = "处罚来源状态快照")
private String sourceStatus;
```

- [ ] **Step 3: 运行聚焦测试，确认失败仍停留在行为层**

Run:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH="$JAVA_HOME/bin:$PATH"
mvn -pl jeecg-boot-module/jeecg-module-content \
  -Dtest=ContentUserGovernanceServiceTest,ContentUserSupportServiceTest test
```

Expected:

```text
Failures: 2 or more
Wanted but not invoked: growthPenaltyRecordService.createFromGovernanceRecord(...)
Wanted but not invoked: growthPenaltyRecordService.createFromReportHandle(...)
```

- [ ] **Step 4: 记录数据模型检查点**

```text
已完成主表来源字段扩展。
当前仍未接通建档服务，继续下一任务。
```

## Task 3: 新增统一成长处罚建档服务

**Files:**
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/IContentUserGrowthPenaltyRecordService.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/impl/ContentUserGrowthPenaltyRecordServiceImpl.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserGrowthPenaltyRecordServiceTest.java`

- [ ] **Step 1: 先写建档服务失败测试，锁定治理来源幂等**

```java
@Test
void shouldCreateRecordFromGovernanceRecord() {
    ContentUserStatusRecord record = new ContentUserStatusRecord()
        .setUserId("u1")
        .setCurrentStatus("NORMAL")
        .setTargetStatus("MUTED")
        .setReason("违规处理")
        .setRuleCode("RULE-1");
    record.setId("status-1");
    ContentUserStatusChangeReq req = new ContentUserStatusChangeReq()
        .setUserId("u1")
        .setCurrentStatus("NORMAL")
        .setTargetStatus("MUTED")
        .setOperatorUserId("admin-1")
        .setReason("违规处理")
        .setRuleCode("RULE-1");
    when(growthPenaltyRecordMapper.selectOne(any())).thenReturn(null);

    growthPenaltyRecordService.createFromGovernanceRecord(record, req, new Date(1735696800000L));

    verify(growthPenaltyRecordMapper).insert(argThat((ContentUserGrowthPenaltyRecord it) ->
        "u1".equals(it.getUserId())
            && "status-1".equals(it.getGovernanceRecordId())
            && "GOVERNANCE_STATUS_CHANGE".equals(it.getSourceType())
            && "status-1".equals(it.getSourceId())
            && "MUTED".equals(it.getSourceStatus())
            && "GOVERNANCE_PENALTY".equals(it.getPenaltyType())
            && "PENDING_RECOVER".equals(it.getStatus())));
}
```

- [ ] **Step 2: 增加建档服务失败测试，锁定举报来源幂等**

```java
@Test
void shouldNotCreateRecordTwiceFromReportHandle() {
    ContentUserReport report = new ContentUserReport()
        .setUserId("u1")
        .setStatus("RESOLVED")
        .setResultStatus("CONFIRMED")
        .setReportType("SPAM");
    report.setId("report-1");
    when(growthPenaltyRecordMapper.selectOne(any())).thenReturn(new ContentUserGrowthPenaltyRecord()
        .setSourceType("REPORT_HANDLE")
        .setSourceId("report-1")
        .setStatus("PENDING_RECOVER"));

    growthPenaltyRecordService.createFromReportHandle(report, createHandleReportReq(), null, new Date());

    verify(growthPenaltyRecordMapper, never()).insert(any(ContentUserGrowthPenaltyRecord.class));
}
```

- [ ] **Step 3: 新增服务契约**

```java
public interface IContentUserGrowthPenaltyRecordService {

    void createFromGovernanceRecord(ContentUserStatusRecord record,
                                    ContentUserStatusChangeReq req,
                                    Date executeTime);

    void createFromReportHandle(ContentUserReport report,
                                ContentReportHandleReq req,
                                String governanceRecordId,
                                Date executeTime);
}
```

- [ ] **Step 4: 实现最小建档服务**

```java
@Service
public class ContentUserGrowthPenaltyRecordServiceImpl implements IContentUserGrowthPenaltyRecordService {

    private static final String STATUS_PENDING_RECOVER = "PENDING_RECOVER";
    private static final String SOURCE_GOVERNANCE = "GOVERNANCE_STATUS_CHANGE";
    private static final String SOURCE_REPORT = "REPORT_HANDLE";
    private static final String PENALTY_GOVERNANCE = "GOVERNANCE_PENALTY";
    private static final String PENALTY_REPORT = "REPORT_PENALTY";
    private static final Set<String> PUNISHING_REPORT_RESULTS = Set.of("CONFIRMED");

    @Resource
    private ContentUserGrowthPenaltyRecordMapper growthPenaltyRecordMapper;

    @Override
    public void createFromGovernanceRecord(ContentUserStatusRecord record,
                                           ContentUserStatusChangeReq req,
                                           Date executeTime) {
        if (record == null || record.getId() == null || !isPunishmentStatus(record.getTargetStatus())) {
            return;
        }
        ContentUserGrowthPenaltyRecord existing = growthPenaltyRecordMapper.selectOne(
            Wrappers.<ContentUserGrowthPenaltyRecord>lambdaQuery()
                .eq(ContentUserGrowthPenaltyRecord::getGovernanceRecordId, record.getId())
                .ne(ContentUserGrowthPenaltyRecord::getStatus, "CANCELLED")
                .last("limit 1")
        );
        if (existing != null) {
            return;
        }
        ContentUserGrowthPenaltyRecord item = new ContentUserGrowthPenaltyRecord()
            .setId(UUIDGenerator.generate())
            .setUserId(record.getUserId())
            .setGovernanceRecordId(record.getId())
            .setSourceType(SOURCE_GOVERNANCE)
            .setSourceId(record.getId())
            .setSourceStatus(record.getTargetStatus())
            .setPenaltyType(PENALTY_GOVERNANCE)
            .setEffectSnapshotJson(buildSnapshotJson(req.getOperatorUserId(), req.getReason(), req.getRuleCode(), record.getTargetStatus()))
            .setStatus(STATUS_PENDING_RECOVER);
        item.setCreateTime(executeTime);
        growthPenaltyRecordMapper.insert(item);
    }

    @Override
    public void createFromReportHandle(ContentUserReport report,
                                       ContentReportHandleReq req,
                                       String governanceRecordId,
                                       Date executeTime) {
        if (report == null || report.getId() == null || !PUNISHING_REPORT_RESULTS.contains(report.getResultStatus())) {
            return;
        }
        ContentUserGrowthPenaltyRecord existing = growthPenaltyRecordMapper.selectOne(
            Wrappers.<ContentUserGrowthPenaltyRecord>lambdaQuery()
                .eq(ContentUserGrowthPenaltyRecord::getSourceType, SOURCE_REPORT)
                .eq(ContentUserGrowthPenaltyRecord::getSourceId, report.getId())
                .ne(ContentUserGrowthPenaltyRecord::getStatus, "CANCELLED")
                .last("limit 1")
        );
        if (existing != null) {
            return;
        }
        ContentUserGrowthPenaltyRecord item = new ContentUserGrowthPenaltyRecord()
            .setId(UUIDGenerator.generate())
            .setUserId(report.getUserId())
            .setGovernanceRecordId(governanceRecordId)
            .setSourceType(SOURCE_REPORT)
            .setSourceId(report.getId())
            .setSourceStatus(report.getResultStatus())
            .setPenaltyType(PENALTY_REPORT)
            .setEffectSnapshotJson(buildSnapshotJson(req.getOperatorUserId(), req.getResultNote(), null, report.getResultStatus()))
            .setStatus(STATUS_PENDING_RECOVER);
        item.setCreateTime(executeTime);
        growthPenaltyRecordMapper.insert(item);
    }
}
```

- [ ] **Step 5: 给快照构造加最小实现**

```java
private String buildSnapshotJson(String operatorUserId, String reason, String ruleCode, String sourceStatus) {
    return "{\"operatorUserId\":\"" + defaultText(operatorUserId)
        + "\",\"reason\":\"" + defaultText(reason)
        + "\",\"ruleCode\":\"" + defaultText(ruleCode)
        + "\",\"sourceStatus\":\"" + defaultText(sourceStatus)
        + "\",\"plannedEffects\":[]}";
}

private boolean isPunishmentStatus(String targetStatus) {
    return "MUTED".equals(targetStatus)
        || "RECOMMENDATION_LIMITED".equals(targetStatus)
        || "FROZEN".equals(targetStatus)
        || "BANNED".equals(targetStatus);
}

private String defaultText(String value) {
    return value == null ? "" : value.replace("\"", "\\\"");
}
```

- [ ] **Step 6: 运行建档服务测试并转绿**

Run:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH="$JAVA_HOME/bin:$PATH"
mvn -pl jeecg-boot-module/jeecg-module-content \
  -Dtest=ContentUserGrowthPenaltyRecordServiceTest test
```

Expected:

```text
Tests run: [non-zero]
Failures: 0
Errors: 0
BUILD SUCCESS
```

- [ ] **Step 7: 记录服务实现检查点**

```text
已完成统一建档服务与幂等基线。
当前尚未接通两个业务入口。
```

## Task 4: 接入治理状态变更入口

**Files:**
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/impl/ContentUserGovernanceServiceImpl.java`
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserGovernanceServiceTest.java`

- [ ] **Step 1: 注入建档服务**

```java
@Resource
private IContentUserGrowthPenaltyRecordService growthPenaltyRecordService;
```

- [ ] **Step 2: 在 `changeStatus(...)` 中接入处罚态建档**

```java
@Override
@Transactional(rollbackFor = Exception.class)
public void changeStatus(ContentUserStatusChangeReq req) {
    validateTransition(req);
    ContentUserStatusRecord record = ContentUserStatusRecord.from(req);
    statusRecordMapper.insert(record);
    updateProfileStatus(req.getUserId(), req.getTargetStatus());
    if (growthPenaltyRecordService != null) {
        growthPenaltyRecordService.createFromGovernanceRecord(record, req, new Date());
    }
    auditLogMapper.insert(ContentUserAuditLog.statusChange(req));
}
```

- [ ] **Step 3: 运行治理服务测试并转绿**

Run:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH="$JAVA_HOME/bin:$PATH"
mvn -pl jeecg-boot-module/jeecg-module-content \
  -Dtest=ContentUserGovernanceServiceTest test
```

Expected:

```text
BUILD SUCCESS
```

- [ ] **Step 4: 记录治理入口检查点**

```text
治理处罚入口已可稳定建档。
自动恢复链路不需要在本任务中改动。
```

## Task 5: 接入举报处理入口

**Files:**
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/impl/ContentUserSupportServiceImpl.java`
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserSupportServiceTest.java`

- [ ] **Step 1: 注入建档服务**

```java
@Resource
private IContentUserGrowthPenaltyRecordService growthPenaltyRecordService;
```

- [ ] **Step 2: 在 `handleReport(...)` 中接入处罚性结果建档**

```java
@Override
@Transactional(rollbackFor = Exception.class)
public String handleReport(ContentReportHandleReq req) {
    ContentUserReport report = reportMapper.selectById(req.getReportId());
    if (report == null) {
        throw new JeecgBootException("举报不存在");
    }
    validateReportHandle(req, report);
    Date resolvedAt = new Date();
    report.setStatus(req.getStatus());
    report.setResultStatus(req.getResultStatus());
    report.setResultNote(req.getResultNote());
    report.setProgressNote(req.getProgressNote());
    report.setResolvedBy(req.getOperatorUserId());
    report.setResolvedAt(resolvedAt);
    reportMapper.updateById(report);
    if (growthPenaltyRecordService != null) {
        growthPenaltyRecordService.createFromReportHandle(report, req, null, resolvedAt);
    }
    auditLogMapper.insert(ContentUserAuditLog.reportHandled(report, req));
    return report.getId();
}
```

- [ ] **Step 3: 运行支持服务测试并转绿**

Run:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH="$JAVA_HOME/bin:$PATH"
mvn -pl jeecg-boot-module/jeecg-module-content \
  -Dtest=ContentUserSupportServiceTest test
```

Expected:

```text
BUILD SUCCESS
```

- [ ] **Step 4: 记录举报入口检查点**

```text
举报处理入口已支持基于 `CONFIRMED` 的处罚性建档。
当前仍未要求举报入口自行执行真实处罚动作。
```

## Task 6: 文档同步与整体验证

**Files:**
- Modify: `docs/superpowers/specs/2026-05-02-content-user-prd-backend-coverage-report.md`
- Modify: `docs/superpowers/plans/2026-05-02-content-user-support-governance-gap-plan.md`

- [ ] **Step 1: 更新覆盖报告**

```md
- `2026-05-02` 已新增成长处罚来源建模扩展：`governance/status/change` 与 `support/admin/report/handle` 可对成长处罚主表进行统一建档，并补齐来源字段、幂等规则和后续执行预留快照。
```

- [ ] **Step 2: 更新阶段计划文档**

```md
- 第二阶段已新增：更多成长处罚来源建模扩展，覆盖治理处罚入口与举报处理入口统一建档
- 第二阶段剩余缺口：更多等级权益消费方落地，以及成长处罚真实执行引擎补齐
```

- [ ] **Step 3: 运行模块聚焦回归**

Run:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH="$JAVA_HOME/bin:$PATH"
mvn -pl jeecg-boot-module/jeecg-module-content \
  -Dtest=ContentUserGrowthPenaltyRecordServiceTest,ContentUserGovernanceServiceTest,ContentUserSupportServiceTest,ContentUserGrowthPenaltyRecoveryServiceTest test
```

Expected:

```text
BUILD SUCCESS
```

- [ ] **Step 4: 运行启动模块编译验证**

Run:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH="$JAVA_HOME/bin:$PATH"
mvn -pl jeecg-module-system/jeecg-system-start -am -DskipTests compile
```

Expected:

```text
BUILD SUCCESS
```

- [ ] **Step 5: 运行规范检查**

Run:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH="$JAVA_HOME/bin:$PATH"
python3 .trae/skills/ai-coding-java-springboot/scripts/standards-check.py \
  --target jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/entity/ContentUserGrowthPenaltyRecord.java \
  --target jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/IContentUserGrowthPenaltyRecordService.java \
  --target jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/impl/ContentUserGrowthPenaltyRecordServiceImpl.java \
  --target jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/impl/ContentUserGovernanceServiceImpl.java \
  --target jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/impl/ContentUserSupportServiceImpl.java \
  --target jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserGrowthPenaltyRecordServiceTest.java \
  --target jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserGovernanceServiceTest.java \
  --target jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserSupportServiceTest.java
```

Expected:

```text
开发规范检查通过！
```

- [ ] **Step 6: 做本轮收口说明，不在当前脏工作树直接提交**

```text
当前 worktree 已包含多批未提交改动。
本任务完成后只交付代码、测试结果和文档更新，不自动执行 git commit。
如用户后续要求单独提交，再基于最新工作树状态决定提交策略。
```

## 自检结论

- Spec 覆盖：已覆盖主表来源字段、统一建档服务、治理入口、举报入口、幂等规则、测试与文档同步。
- 占位词检查：计划中无 `TODO`、`TBD`、`implement later`。
- 类型一致性：统一使用 `IContentUserGrowthPenaltyRecordService`、`createFromGovernanceRecord(...)`、`createFromReportHandle(...)`、`sourceType/sourceId/sourceStatus`、`GOVERNANCE_PENALTY`、`REPORT_PENALTY`。
