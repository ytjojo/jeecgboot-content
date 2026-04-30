# Content User Appeal ResolvedAt Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Expose `resolvedAt` in user-side appeal progress and appeal list responses.

**Architecture:** Reuse the existing appeal query endpoints and extend only the response VO plus the service mapping method. Keep the change read-only by flowing the already-persisted `resolvedAt` field from `ContentUserAppeal` into `ContentUserAppealProgressVO`, then verify the field through focused service and controller tests.

**Tech Stack:** Java 21, Spring Boot, JeecgBoot, Lombok, JUnit 5, Mockito, MockMvc standalone, Maven

---

### Task 1: Add Red Tests For `resolvedAt`

**Files:**
- Modify: `/Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/.worktrees/content-user-domain-review/jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserSupportServiceTest.java`
- Modify: `/Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/.worktrees/content-user-domain-review/jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/controller/ContentUserSupportControllerWebMvcTest.java`

- [ ] **Step 1: Write the failing service assertions**

```java
Date resolvedAt = new Date();
ContentUserAppeal appeal = new ContentUserAppeal()
    .setUserId("u1")
    .setStatus("PROCESSING")
    .setProgressNote("客服已受理")
    .setResultStatus("APPROVED")
    .setResultNote("处罚已撤销")
    .setResolvedBy("admin-1")
    .setResolvedAt(resolvedAt);

assertThat(result.getResolvedAt()).isEqualTo(resolvedAt);
```

- [ ] **Step 2: Write the failing controller assertions**

```java
Date resolvedAt = new Date(1735689600000L);
when(supportService.getAppealProgress("u1", "appeal-1"))
    .thenReturn(new ContentUserAppealProgressVO()
        .setAppealId("appeal-1")
        .setStatus("RESOLVED")
        .setProgressNote("客服已受理")
        .setResultStatus("APPROVED")
        .setResultNote("处罚已撤销")
        .setResolvedBy("admin-1")
        .setResolvedAt(resolvedAt));

.andExpect(jsonPath("$.result.resolvedAt").exists())
```

```java
Date resolvedAt = new Date(1735689600000L);
when(supportService.listAppeals("u1"))
    .thenReturn(List.of(new ContentUserAppealProgressVO()
        .setAppealId("appeal-1")
        .setStatus("RESOLVED")
        .setProgressNote("已处理")
        .setResolvedAt(resolvedAt)));

.andExpect(jsonPath("$.result[0].resolvedAt").exists())
```

- [ ] **Step 3: Run the focused tests to verify RED**

Run:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH="$JAVA_HOME/bin:$PATH"
mvn -pl jeecg-boot-module/jeecg-module-content -Dtest=ContentUserSupportServiceTest,ContentUserSupportControllerWebMvcTest -Dsurefire.failIfNoSpecifiedTests=false test
```

Expected: FAIL with missing `getResolvedAt()` / `setResolvedAt(...)` on `ContentUserAppealProgressVO`.

### Task 2: Implement Minimal `resolvedAt` Exposure

**Files:**
- Modify: `/Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/.worktrees/content-user-domain-review/jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/vo/ContentUserAppealProgressVO.java`
- Modify: `/Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/.worktrees/content-user-domain-review/jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/impl/ContentUserSupportServiceImpl.java`

- [ ] **Step 1: Extend the VO**

```java
import java.util.Date;

@Schema(description = "处理完成时间", requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true)
private Date resolvedAt;
```

- [ ] **Step 2: Map the field in service**

```java
private ContentUserAppealProgressVO toAppealProgress(ContentUserAppeal appeal) {
    return new ContentUserAppealProgressVO()
        .setAppealId(appeal.getId())
        .setStatus(appeal.getStatus())
        .setProgressNote(appeal.getProgressNote())
        .setResultStatus(appeal.getResultStatus())
        .setResultNote(appeal.getResultNote())
        .setResolvedBy(appeal.getResolvedBy())
        .setResolvedAt(appeal.getResolvedAt());
}
```

- [ ] **Step 3: Run the same focused tests to verify GREEN**

Run:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH="$JAVA_HOME/bin:$PATH"
mvn -pl jeecg-boot-module/jeecg-module-content -Dtest=ContentUserSupportServiceTest,ContentUserSupportControllerWebMvcTest -Dsurefire.failIfNoSpecifiedTests=false test
```

Expected: PASS with all specified tests green.

### Task 3: Verification And Diagnostics

**Files:**
- Read: `/Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/.worktrees/content-user-domain-review/jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/vo/ContentUserAppealProgressVO.java`
- Read: `/Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/.worktrees/content-user-domain-review/jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/impl/ContentUserSupportServiceImpl.java`
- Read: `/Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/.worktrees/content-user-domain-review/jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/controller/ContentUserSupportControllerWebMvcTest.java`
- Read: `/Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/.worktrees/content-user-domain-review/jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserSupportServiceTest.java`

- [ ] **Step 1: Check diagnostics for touched files**

Run:

```bash
# Use IDE diagnostics on the four touched files.
```

Expected: no new syntax or import errors.

- [ ] **Step 2: Re-run the focused verification command**

Run:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH="$JAVA_HOME/bin:$PATH"
mvn -pl jeecg-boot-module/jeecg-module-content -Dtest=ContentUserSupportServiceTest,ContentUserSupportControllerWebMvcTest -Dsurefire.failIfNoSpecifiedTests=false test
```

Expected: PASS with the `resolvedAt` assertions included.
