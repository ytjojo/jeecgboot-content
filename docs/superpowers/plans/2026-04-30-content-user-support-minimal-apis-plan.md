# Content User Support Minimal APIs Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build the minimal support-domain backend APIs for report submission, appeal progress query, help-center metadata, and customer-service routing inside `jeecg-module-content`.

**Architecture:** Keep `ContentUserSupportController` thin and place rule logic in `ContentUserSupportServiceImpl`. Reuse `content_user_appeal` for appeal lifecycle, write report submissions as support audit events in `content_user_audit_log`, and return module-local metadata DTOs for help center and customer service entry points.

**Tech Stack:** Java 21, Spring Boot 3, MyBatis-Plus, JUnit 5, Mockito, MockMvc standalone, Jeecg `Result`

---

## File Structure

- Modify: `docs/requirements/prd/内容社区-用户域-PRD.md` (reference only, no changes)
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/controller/ContentUserSupportController.java`
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/IContentUserSupportService.java`
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/impl/ContentUserSupportServiceImpl.java`
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/mapper/ContentUserAppealMapper.java`
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/entity/ContentUserAuditLog.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/req/support/ContentReportCreateReq.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/vo/ContentUserAppealProgressVO.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/vo/ContentHelpCenterVO.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/vo/ContentCustomerServiceVO.java`
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserSupportServiceTest.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/controller/ContentUserSupportControllerWebMvcTest.java`

## Task 1: Expand Service Contract

**Files:**
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/IContentUserSupportService.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/req/support/ContentReportCreateReq.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/vo/ContentUserAppealProgressVO.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/vo/ContentHelpCenterVO.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/vo/ContentCustomerServiceVO.java`

- [ ] **Step 1: Write the new contract signatures**

```java
public interface IContentUserSupportService {
    String createAppeal(ContentAppealCreateReq req);
    ContentUserAppealProgressVO getAppealProgress(String userId, String appealId);
    List<ContentUserAppeal> listAppeals(String userId);
    String createReport(ContentReportCreateReq req);
    ContentHelpCenterVO getHelpCenter();
    ContentCustomerServiceVO getCustomerServiceEntry(String userId);
}
```

- [ ] **Step 2: Add request and response models**

```java
@Data
@Accessors(chain = true)
public class ContentReportCreateReq {
    @NotBlank(message = "举报用户ID不能为空")
    private String userId;
    @NotBlank(message = "举报目标类型不能为空")
    private String targetType;
    @NotBlank(message = "举报目标ID不能为空")
    private String targetId;
    @NotBlank(message = "举报类型不能为空")
    private String reportType;
    @NotBlank(message = "举报原因不能为空")
    private String reason;
    private String evidenceJson;
}
```

- [ ] **Step 3: Commit**

```bash
git add jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/IContentUserSupportService.java jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/req/support/ContentReportCreateReq.java jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/vo
git commit -m "feat: define content user support contracts"
```

## Task 2: Service Tests And Minimal Implementation

**Files:**
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserSupportServiceTest.java`
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/impl/ContentUserSupportServiceImpl.java`
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/mapper/ContentUserAppealMapper.java`
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/entity/ContentUserAuditLog.java`

- [ ] **Step 1: Add failing tests**

```java
@Test
void shouldQueryAppealProgressForOwner() {
    ContentUserAppeal appeal = new ContentUserAppeal()
        .setUserId("u1")
        .setStatus("PROCESSING")
        .setProgressNote("客服已受理");
    appeal.setId("appeal-1");
    when(appealMapper.selectById("appeal-1")).thenReturn(appeal);

    ContentUserAppealProgressVO result = supportService.getAppealProgress("u1", "appeal-1");

    assertThat(result.getStatus()).isEqualTo("PROCESSING");
}

@Test
void shouldCreateReportAndWriteAuditLog() {
    String reportId = supportService.createReport(new ContentReportCreateReq()
        .setUserId("u1")
        .setTargetType("CONTENT")
        .setTargetId("post-1")
        .setReportType("SPAM")
        .setReason("垃圾内容"));

    assertThat(reportId).isNotBlank();
    verify(auditLogMapper).insert(argThat(it -> "USER_REPORT_CREATED".equals(it.getEventType())));
}
```

- [ ] **Step 2: Run focused test**

Run: `cd /Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/.worktrees/content-user-domain-review/jeecg-boot && export JAVA_HOME=$(/usr/libexec/java_home -v 21) && export PATH="$JAVA_HOME/bin:$PATH" && mvn -pl jeecg-boot-module/jeecg-module-content -Dtest=ContentUserSupportServiceTest -Dsurefire.failIfNoSpecifiedTests=false test`
Expected: FAIL

- [ ] **Step 3: Write minimal implementation**

```java
@Override
public ContentUserAppealProgressVO getAppealProgress(String userId, String appealId) {
    ContentUserAppeal appeal = appealMapper.selectById(appealId);
    if (appeal == null || !userId.equals(appeal.getUserId())) {
        throw new JeecgBootException("申诉不存在或无权查看");
    }
    return new ContentUserAppealProgressVO()
        .setAppealId(appeal.getId())
        .setStatus(appeal.getStatus())
        .setProgressNote(appeal.getProgressNote());
}
```

- [ ] **Step 4: Run focused test again**

Run: `cd /Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/.worktrees/content-user-domain-review/jeecg-boot && export JAVA_HOME=$(/usr/libexec/java_home -v 21) && export PATH="$JAVA_HOME/bin:$PATH" && mvn -pl jeecg-boot-module/jeecg-module-content -Dtest=ContentUserSupportServiceTest -Dsurefire.failIfNoSpecifiedTests=false test`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserSupportServiceTest.java jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/impl/ContentUserSupportServiceImpl.java jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/mapper/ContentUserAppealMapper.java jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/entity/ContentUserAuditLog.java
git commit -m "feat: implement content user support service"
```

## Task 3: Controller Tests And API Endpoints

**Files:**
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/controller/ContentUserSupportController.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/controller/ContentUserSupportControllerWebMvcTest.java`

- [ ] **Step 1: Add failing controller tests**

```java
@Test
void shouldReturnAppealProgress() throws Exception {
    when(supportService.getAppealProgress("u1", "appeal-1"))
        .thenReturn(new ContentUserAppealProgressVO()
            .setAppealId("appeal-1")
            .setStatus("PROCESSING")
            .setProgressNote("客服已受理"));

    mockMvc.perform(get("/content/user/support/appeal/progress")
            .param("userId", "u1")
            .param("appealId", "appeal-1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.result.status").value("PROCESSING"));
}
```

- [ ] **Step 2: Run focused controller test**

Run: `cd /Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/.worktrees/content-user-domain-review/jeecg-boot && export JAVA_HOME=$(/usr/libexec/java_home -v 21) && export PATH="$JAVA_HOME/bin:$PATH" && mvn -pl jeecg-boot-module/jeecg-module-content -Dtest=ContentUserSupportControllerWebMvcTest -Dsurefire.failIfNoSpecifiedTests=false test`
Expected: FAIL

- [ ] **Step 3: Implement minimal endpoints**

```java
@GetMapping("/appeal/progress")
public Result<ContentUserAppealProgressVO> getAppealProgress(@RequestParam("userId") String userId,
                                                             @RequestParam("appealId") String appealId) {
    return Result.OK(supportService.getAppealProgress(userId, appealId));
}

@GetMapping("/help-center")
public Result<ContentHelpCenterVO> getHelpCenter() {
    return Result.OK(supportService.getHelpCenter());
}
```

- [ ] **Step 4: Run controller test again**

Run: `cd /Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/.worktrees/content-user-domain-review/jeecg-boot && export JAVA_HOME=$(/usr/libexec/java_home -v 21) && export PATH="$JAVA_HOME/bin:$PATH" && mvn -pl jeecg-boot-module/jeecg-module-content -Dtest=ContentUserSupportControllerWebMvcTest -Dsurefire.failIfNoSpecifiedTests=false test`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/controller/ContentUserSupportController.java jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/controller/ContentUserSupportControllerWebMvcTest.java
git commit -m "feat: expose content user support apis"
```

## Verification

- Run: `cd /Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/.worktrees/content-user-domain-review/jeecg-boot && export JAVA_HOME=$(/usr/libexec/java_home -v 21) && export PATH="$JAVA_HOME/bin:$PATH" && mvn -pl jeecg-boot-module/jeecg-module-content -Dtest=ContentUserSupportServiceTest,ContentUserSupportControllerWebMvcTest -Dsurefire.failIfNoSpecifiedTests=false test`
- Run: `python3 /Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/.trae/skills/ai-coding-java-springboot/scripts/standards-check.py --target /Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/.worktrees/content-user-domain-review/jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user`
