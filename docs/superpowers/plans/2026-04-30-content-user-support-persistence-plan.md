# Content User Support Persistence Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add persistent report records and minimal appeal result fields for the content user support domain inside `jeecg-module-content`.

**Architecture:** Keep all writable changes inside `jeecg-module-content`. Introduce a dedicated `content_user_report` entity and mapper for report persistence, and extend the existing appeal aggregate with minimal result fields so support queries can expose final outcomes without expanding into a full admin workflow.

**Tech Stack:** Java 21, Spring Boot 3, MyBatis-Plus, Flyway SQL, JUnit 5, Mockito, MockMvc standalone, Jeecg `Result`

---

## File Structure

- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/resources/flyway/sql/mysql/V3.9.1_50__content_user_domain_init.sql`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/entity/ContentUserReport.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/mapper/ContentUserReportMapper.java`
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/entity/ContentUserAppeal.java`
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/vo/ContentUserAppealProgressVO.java`
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/IContentUserSupportService.java`
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/impl/ContentUserSupportServiceImpl.java`
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/controller/ContentUserSupportController.java`
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserSupportServiceTest.java`
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/controller/ContentUserSupportControllerWebMvcTest.java`

## Task 1: Add Schema And Persistence Model

**Files:**
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/resources/flyway/sql/mysql/V3.9.1_50__content_user_domain_init.sql`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/entity/ContentUserReport.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/mapper/ContentUserReportMapper.java`
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/entity/ContentUserAppeal.java`

- [ ] **Step 1: Write the failing persistence test**

```java
@Test
void shouldPersistReportRecordBeforeWritingAuditLog() {
    String reportId = supportService.createReport(createReportReq());
    assertThat(reportId).isNotBlank();
    verify(reportMapper).insert(argThat(it -> "u1".equals(it.getUserId())));
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `cd /Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/.worktrees/content-user-domain-review/jeecg-boot && export JAVA_HOME=$(/usr/libexec/java_home -v 21) && export PATH="$JAVA_HOME/bin:$PATH" && mvn -pl jeecg-boot-module/jeecg-module-content -Dtest=ContentUserSupportServiceTest -Dsurefire.failIfNoSpecifiedTests=false test`
Expected: FAIL because `reportMapper` and report entity do not exist

- [ ] **Step 3: Write minimal schema and entity**

```sql
CREATE TABLE IF NOT EXISTS `content_user_report` (
  `id` varchar(32) NOT NULL COMMENT '主键ID',
  `user_id` varchar(32) NOT NULL COMMENT '举报用户ID',
  `target_type` varchar(32) NOT NULL COMMENT '举报目标类型',
  `target_id` varchar(64) NOT NULL COMMENT '举报目标ID',
  `report_type` varchar(32) NOT NULL COMMENT '举报类型',
  `reason` varchar(500) DEFAULT NULL COMMENT '举报原因',
  `evidence_json` text COMMENT '举报证据JSON',
  `status` varchar(32) NOT NULL DEFAULT 'PENDING' COMMENT '举报状态',
  `create_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_content_user_report_user` (`user_id`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='内容社区用户举报';
```

- [ ] **Step 4: Run test to verify it passes**

Run: `cd /Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/.worktrees/content-user-domain-review/jeecg-boot && export JAVA_HOME=$(/usr/libexec/java_home -v 21) && export PATH="$JAVA_HOME/bin:$PATH" && mvn -pl jeecg-boot-module/jeecg-module-content -Dtest=ContentUserSupportServiceTest -Dsurefire.failIfNoSpecifiedTests=false test`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/resources/flyway/sql/mysql/V3.9.1_50__content_user_domain_init.sql jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/entity/ContentUserReport.java jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/mapper/ContentUserReportMapper.java jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/entity/ContentUserAppeal.java
git commit -m "feat: add content user report persistence"
```

## Task 2: Extend Appeal Result Query

**Files:**
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/vo/ContentUserAppealProgressVO.java`
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/impl/ContentUserSupportServiceImpl.java`
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserSupportServiceTest.java`

- [ ] **Step 1: Write the failing appeal result test**

```java
@Test
void shouldExposeAppealResultFieldsWhenAppealIsResolved() {
    ContentUserAppeal appeal = new ContentUserAppeal()
        .setUserId("u1")
        .setStatus("RESOLVED")
        .setResultStatus("APPROVED")
        .setResultNote("处罚已撤销")
        .setResolvedBy("admin-1");
    appeal.setId("appeal-1");
    when(appealMapper.selectById("appeal-1")).thenReturn(appeal);

    ContentUserAppealProgressVO result = supportService.getAppealProgress("u1", "appeal-1");

    assertThat(result.getResultStatus()).isEqualTo("APPROVED");
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `cd /Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/.worktrees/content-user-domain-review/jeecg-boot && export JAVA_HOME=$(/usr/libexec/java_home -v 21) && export PATH="$JAVA_HOME/bin:$PATH" && mvn -pl jeecg-boot-module/jeecg-module-content -Dtest=ContentUserSupportServiceTest -Dsurefire.failIfNoSpecifiedTests=false test`
Expected: FAIL because result fields are missing from entity or VO mapping

- [ ] **Step 3: Write minimal implementation**

```java
private ContentUserAppealProgressVO toAppealProgress(ContentUserAppeal appeal) {
    return new ContentUserAppealProgressVO()
        .setAppealId(appeal.getId())
        .setStatus(appeal.getStatus())
        .setProgressNote(appeal.getProgressNote())
        .setResultStatus(appeal.getResultStatus())
        .setResultNote(appeal.getResultNote())
        .setResolvedBy(appeal.getResolvedBy());
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `cd /Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/.worktrees/content-user-domain-review/jeecg-boot && export JAVA_HOME=$(/usr/libexec/java_home -v 21) && export PATH="$JAVA_HOME/bin:$PATH" && mvn -pl jeecg-boot-module/jeecg-module-content -Dtest=ContentUserSupportServiceTest -Dsurefire.failIfNoSpecifiedTests=false test`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/vo/ContentUserAppealProgressVO.java jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/impl/ContentUserSupportServiceImpl.java jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserSupportServiceTest.java
git commit -m "feat: expose content user appeal result fields"
```

## Task 3: Verify Controller Output

**Files:**
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/controller/ContentUserSupportControllerWebMvcTest.java`
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/controller/ContentUserSupportController.java`

- [ ] **Step 1: Write the failing controller assertion**

```java
@Test
void shouldReturnAppealResultFields() throws Exception {
    when(supportService.getAppealProgress("u1", "appeal-1"))
        .thenReturn(new ContentUserAppealProgressVO()
            .setAppealId("appeal-1")
            .setStatus("RESOLVED")
            .setResultStatus("APPROVED")
            .setResultNote("处罚已撤销"));

    mockMvc.perform(get("/content/user/support/appeal/progress")
            .param("userId", "u1")
            .param("appealId", "appeal-1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.result.resultStatus").value("APPROVED"));
}
```

- [ ] **Step 2: Run controller test to verify it fails**

Run: `cd /Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/.worktrees/content-user-domain-review/jeecg-boot && export JAVA_HOME=$(/usr/libexec/java_home -v 21) && export PATH="$JAVA_HOME/bin:$PATH" && mvn -pl jeecg-boot-module/jeecg-module-content -Dtest=ContentUserSupportControllerWebMvcTest -Dsurefire.failIfNoSpecifiedTests=false test`
Expected: FAIL because the response model lacks the new result fields

- [ ] **Step 3: Keep controller thin and reuse updated VO**

```java
@GetMapping("/appeal/progress")
public Result<ContentUserAppealProgressVO> getAppealProgress(@RequestParam("userId") String userId,
                                                             @RequestParam("appealId") String appealId) {
    return Result.OK(supportService.getAppealProgress(userId, appealId));
}
```

- [ ] **Step 4: Run controller test to verify it passes**

Run: `cd /Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/.worktrees/content-user-domain-review/jeecg-boot && export JAVA_HOME=$(/usr/libexec/java_home -v 21) && export PATH="$JAVA_HOME/bin:$PATH" && mvn -pl jeecg-boot-module/jeecg-module-content -Dtest=ContentUserSupportControllerWebMvcTest -Dsurefire.failIfNoSpecifiedTests=false test`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/controller/ContentUserSupportControllerWebMvcTest.java jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/controller/ContentUserSupportController.java
git commit -m "test: cover content user support persistence outputs"
```

## Verification

- Run: `cd /Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/.worktrees/content-user-domain-review/jeecg-boot && export JAVA_HOME=$(/usr/libexec/java_home -v 21) && export PATH="$JAVA_HOME/bin:$PATH" && mvn -pl jeecg-boot-module/jeecg-module-content -Dtest=ContentUserSupportServiceTest,ContentUserSupportControllerWebMvcTest -Dsurefire.failIfNoSpecifiedTests=false test`
- Run: `python3 /Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/.trae/skills/ai-coding-java-springboot/scripts/standards-check.py --target /Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/.worktrees/content-user-domain-review/jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user`
