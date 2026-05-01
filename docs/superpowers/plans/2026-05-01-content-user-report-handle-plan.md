# Content User Report Handle Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add the minimum closed-loop report handling flow with an admin handle API and a user progress query API.

**Architecture:** Reuse the existing support domain shape established for appeals. Extend `content_user_report` and `ContentUserReport` with minimal result fields, add one admin-side handle request plus one user-side progress VO, and implement strict `PENDING -> RESOLVED` handling in `ContentUserSupportServiceImpl` with audit logging and focused tests.

**Tech Stack:** Java 21, Spring Boot, JeecgBoot, MyBatis-Plus, Jakarta Validation, Lombok, JUnit 5, Mockito, MockMvc standalone, Maven

---

### Task 1: Add Red Tests For Report Handling

**Files:**
- Modify: `/Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/.worktrees/content-user-domain-review/jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserSupportServiceTest.java`
- Modify: `/Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/.worktrees/content-user-domain-review/jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/controller/ContentUserSupportControllerWebMvcTest.java`
- Modify: `/Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/.worktrees/content-user-domain-review/jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/controller/ContentUserSupportAdminControllerWebMvcTest.java`

- [ ] **Step 1: Write failing service tests**

```java
@Test
void shouldHandlePendingReportToResolved() {
    ContentUserReport report = new ContentUserReport()
        .setUserId("u1")
        .setStatus("PENDING")
        .setReportType("SPAM");
    report.setId("report-1");
    when(reportMapper.selectById("report-1")).thenReturn(report);

    String handledReportId = supportService.handleReport(createHandleReportReq());

    assertThat(handledReportId).isEqualTo("report-1");
    verify(reportMapper).updateById(argThat((ContentUserReport it) ->
        "RESOLVED".equals(it.getStatus())
            && "CONFIRMED".equals(it.getResultStatus())
            && "违规成立".equals(it.getResultNote())
            && "已处理完成".equals(it.getProgressNote())
            && "admin-1".equals(it.getResolvedBy())
            && it.getResolvedAt() != null));
    verify(auditLogMapper).insert(argThat((ContentUserAuditLog it) ->
        "USER_REPORT_HANDLED".equals(it.getEventType())
            && "admin-1".equals(it.getOperatorUserId())));
}

@Test
void shouldQueryReportProgressForOwner() {
    Date resolvedAt = new Date();
    ContentUserReport report = new ContentUserReport()
        .setUserId("u1")
        .setStatus("RESOLVED")
        .setProgressNote("已处理完成")
        .setResultStatus("CONFIRMED")
        .setResultNote("违规成立")
        .setResolvedBy("admin-1")
        .setResolvedAt(resolvedAt);
    report.setId("report-1");
    when(reportMapper.selectById("report-1")).thenReturn(report);

    ContentUserReportProgressVO result = supportService.getReportProgress("u1", "report-1");

    assertThat(result.getReportId()).isEqualTo("report-1");
    assertThat(result.getResolvedAt()).isEqualTo(resolvedAt);
}
```

- [ ] **Step 2: Write failing controller tests**

```java
@Test
void shouldHandleReport() throws Exception {
    when(supportService.handleReport(any(ContentReportHandleReq.class))).thenReturn("report-1");

    mockMvc.perform(post("/content/user/support/admin/report/handle")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"reportId":"report-1","operatorUserId":"admin-1","status":"RESOLVED","resultStatus":"CONFIRMED","resultNote":"违规成立","progressNote":"已处理完成"}
                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.result").value("report-1"));
}

@Test
void shouldReturnReportProgress() throws Exception {
    when(supportService.getReportProgress("u1", "report-1"))
        .thenReturn(new ContentUserReportProgressVO()
            .setReportId("report-1")
            .setStatus("RESOLVED")
            .setResultStatus("CONFIRMED"));

    mockMvc.perform(get("/content/user/support/report/progress")
            .param("userId", "u1")
            .param("reportId", "report-1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.result.reportId").value("report-1"));
}
```

- [ ] **Step 3: Run focused tests to verify RED**

Run:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH="$JAVA_HOME/bin:$PATH"
mvn -pl jeecg-boot-module/jeecg-module-content -Dtest=ContentUserSupportServiceTest,ContentUserSupportControllerWebMvcTest,ContentUserSupportAdminControllerWebMvcTest -Dsurefire.failIfNoSpecifiedTests=false test
```

Expected: FAIL with missing `ContentReportHandleReq`, `ContentUserReportProgressVO`, `handleReport(...)`, and `getReportProgress(...)`.

### Task 2: Implement Request, VO, Entity, Contract, And SQL

**Files:**
- Create: `/Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/.worktrees/content-user-domain-review/jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/req/support/ContentReportHandleReq.java`
- Create: `/Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/.worktrees/content-user-domain-review/jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/vo/ContentUserReportProgressVO.java`
- Modify: `/Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/.worktrees/content-user-domain-review/jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/entity/ContentUserReport.java`
- Modify: `/Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/.worktrees/content-user-domain-review/jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/entity/ContentUserAuditLog.java`
- Modify: `/Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/.worktrees/content-user-domain-review/jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/IContentUserSupportService.java`
- Modify: `/Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/.worktrees/content-user-domain-review/jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/resources/flyway/sql/mysql/V3.9.1_50__content_user_domain_init.sql`

- [ ] **Step 1: Create the handle request and progress VO**

```java
@Data
@Accessors(chain = true)
@Schema(description = "内容社区举报处理请求")
public class ContentReportHandleReq {

    @NotBlank(message = "举报ID不能为空")
    private String reportId;

    @NotBlank(message = "处理人ID不能为空")
    private String operatorUserId;

    @NotBlank(message = "处理后状态不能为空")
    @Pattern(regexp = "^RESOLVED$", message = "处理后状态仅支持RESOLVED")
    private String status;

    @NotBlank(message = "处理结果状态不能为空")
    private String resultStatus;

    @NotBlank(message = "处理结果说明不能为空")
    @Size(max = 500, message = "处理结果说明长度不能超过500位")
    private String resultNote;

    @NotBlank(message = "处理进度说明不能为空")
    @Size(max = 500, message = "处理进度说明长度不能超过500位")
    private String progressNote;
}
```

```java
@Data
@Accessors(chain = true)
@Schema(description = "内容社区用户举报进度视图")
public class ContentUserReportProgressVO {

    private String reportId;
    private String status;
    private String progressNote;
    private String resultStatus;
    private String resultNote;
    private String resolvedBy;
    private Date resolvedAt;
}
```

- [ ] **Step 2: Extend entity, contract, and audit factory**

```java
private String resultStatus;
private String resultNote;
private String progressNote;
private String resolvedBy;
private Date resolvedAt;
```

```java
String handleReport(ContentReportHandleReq req);

ContentUserReportProgressVO getReportProgress(String userId, String reportId);
```

```java
public static ContentUserAuditLog reportHandled(ContentUserReport report, ContentReportHandleReq req) {
    return new ContentUserAuditLog()
        .setUserId(report.getUserId())
        .setOperatorUserId(req.getOperatorUserId())
        .setEventType("USER_REPORT_HANDLED")
        .setEventContent(report.getReportType() + ":" + req.getResultStatus())
        .setExtraDataJson("{\"reportId\":\"" + report.getId() + "\",\"resultStatus\":\""
            + req.getResultStatus() + "\",\"resultNote\":\"" + req.getResultNote() + "\"}")
        .setEventTime(new Date());
}
```

- [ ] **Step 3: Extend SQL init table**

```sql
`status` varchar(32) NOT NULL DEFAULT 'PENDING' COMMENT '举报状态',
`result_status` varchar(32) DEFAULT NULL COMMENT '处理结果状态',
`result_note` varchar(500) DEFAULT NULL COMMENT '处理结果说明',
`progress_note` varchar(500) DEFAULT NULL COMMENT '处理进度说明',
`resolved_by` varchar(32) DEFAULT NULL COMMENT '处理人',
`resolved_at` datetime DEFAULT NULL COMMENT '处理完成时间',
```

- [ ] **Step 4: Run focused tests to confirm compile moves forward**

Run the same Maven command as Task 1.

Expected: FAIL because controllers and service implementation are still missing.

### Task 3: Implement Controllers And Service Logic

**Files:**
- Modify: `/Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/.worktrees/content-user-domain-review/jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/controller/ContentUserSupportController.java`
- Modify: `/Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/.worktrees/content-user-domain-review/jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/controller/ContentUserSupportAdminController.java`
- Modify: `/Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/.worktrees/content-user-domain-review/jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/impl/ContentUserSupportServiceImpl.java`

- [ ] **Step 1: Add the controller endpoints**

```java
@Operation(summary = "查询举报进度")
@GetMapping("/report/progress")
public Result<ContentUserReportProgressVO> getReportProgress(@RequestParam("userId") String userId,
                                                             @RequestParam("reportId") String reportId) {
    return Result.OK(supportService.getReportProgress(userId, reportId));
}
```

```java
@Operation(summary = "处理举报")
@PostMapping("/report/handle")
public Result<String> handleReport(@Valid @RequestBody ContentReportHandleReq req) {
    return Result.OK(supportService.handleReport(req));
}
```

- [ ] **Step 2: Add the strict service flow**

```java
@Override
public ContentUserReportProgressVO getReportProgress(String userId, String reportId) {
    ContentUserReport report = reportMapper.selectById(reportId);
    if (report == null || !userId.equals(report.getUserId())) {
        throw new JeecgBootException("举报不存在或无权查看");
    }
    return toReportProgress(report);
}

@Override
@Transactional(rollbackFor = Exception.class)
public String handleReport(ContentReportHandleReq req) {
    ContentUserReport report = reportMapper.selectById(req.getReportId());
    if (report == null) {
        throw new JeecgBootException("举报不存在");
    }
    if (!"RESOLVED".equals(req.getStatus())) {
        throw new JeecgBootException("举报处理仅支持流转到RESOLVED");
    }
    if ("RESOLVED".equals(report.getStatus())) {
        throw new JeecgBootException("举报已处理完成，请勿重复处理");
    }
    if (!"PENDING".equals(report.getStatus())) {
        throw new JeecgBootException("当前举报状态不允许处理");
    }

    report.setStatus(req.getStatus());
    report.setResultStatus(req.getResultStatus());
    report.setResultNote(req.getResultNote());
    report.setProgressNote(req.getProgressNote());
    report.setResolvedBy(req.getOperatorUserId());
    report.setResolvedAt(new Date());
    reportMapper.updateById(report);
    auditLogMapper.insert(ContentUserAuditLog.reportHandled(report, req));
    return report.getId();
}
```

- [ ] **Step 3: Add the report-progress mapper helper**

```java
private ContentUserReportProgressVO toReportProgress(ContentUserReport report) {
    return new ContentUserReportProgressVO()
        .setReportId(report.getId())
        .setStatus(report.getStatus())
        .setProgressNote(report.getProgressNote())
        .setResultStatus(report.getResultStatus())
        .setResultNote(report.getResultNote())
        .setResolvedBy(report.getResolvedBy())
        .setResolvedAt(report.getResolvedAt());
}
```

- [ ] **Step 4: Run focused tests to verify GREEN**

Run:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH="$JAVA_HOME/bin:$PATH"
mvn -pl jeecg-boot-module/jeecg-module-content -Dtest=ContentUserSupportServiceTest,ContentUserSupportControllerWebMvcTest,ContentUserSupportAdminControllerWebMvcTest -Dsurefire.failIfNoSpecifiedTests=false test
```

Expected: PASS with new report handling and report progress scenarios green.

### Task 4: Diagnostics And Final Verification

**Files:**
- Read: `/Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/.worktrees/content-user-domain-review/jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/entity/ContentUserReport.java`
- Read: `/Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/.worktrees/content-user-domain-review/jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/impl/ContentUserSupportServiceImpl.java`
- Read: `/Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/.worktrees/content-user-domain-review/jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserSupportServiceTest.java`
- Read: `/Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/.worktrees/content-user-domain-review/jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/controller/ContentUserSupportAdminControllerWebMvcTest.java`

- [ ] **Step 1: Check IDE diagnostics**

Use IDE diagnostics on newly added and modified files.

Expected: no new syntax or import errors.

- [ ] **Step 2: Re-run the focused verification command**

Run the same Maven command from Task 3.

Expected: PASS with zero failures and zero errors.
