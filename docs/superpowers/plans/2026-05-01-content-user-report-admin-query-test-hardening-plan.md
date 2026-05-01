# Content User Report Admin Query Test Hardening Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add focused WebMvc regression tests for admin report detail exception propagation and create-time parameter binding behavior.

**Architecture:** Keep the scope inside the existing `ContentUserSupportAdminControllerWebMvcTest` so the work stays at the controller contract layer. Use standalone MockMvc plus Mockito stubbing to verify exception propagation, successful date binding, and invalid date format rejection without changing production code unless a real bug is exposed.

**Tech Stack:** Java 21, Spring Boot 3 MockMvc standalone, Mockito, JUnit 5, Jakarta Validation

---

## File Structure

### Files to modify

- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/controller/ContentUserSupportAdminControllerWebMvcTest.java`

## Task 1: Harden Admin Query WebMvc Coverage

**Files:**
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/controller/ContentUserSupportAdminControllerWebMvcTest.java`

- [ ] **Step 1: Write the failing controller tests**

Add these imports to `ContentUserSupportAdminControllerWebMvcTest.java`:

```java
import org.jeecg.common.exception.JeecgBootException;

import static org.mockito.ArgumentMatchers.argThat;
```

Add these tests near the existing admin report list and detail tests:

```java
    @Test
    void shouldBindCreateTimeRangeForAdminReportList() throws Exception {
        Date resolvedAt = new Date(1735689600000L);
        when(supportService.listReportsForAdmin(argThat(req ->
            req.getCreateTimeStart() != null
                && req.getCreateTimeEnd() != null
                && req.getPageNo() == 1L
                && req.getPageSize() == 10L)))
            .thenReturn(new ContentUserReportAdminPageVO()
                .setTotal(1L)
                .setPageNo(1L)
                .setPageSize(10L)
                .setRecords(List.of(new ContentUserReportAdminListItemVO()
                    .setReportId("report-1")
                    .setCreateTime(resolvedAt))));

        mockMvc.perform(get("/content/user/support/admin/report/list")
                .param("pageNo", "1")
                .param("pageSize", "10")
                .param("createTimeStart", "2026-05-01 10:00:00")
                .param("createTimeEnd", "2026-05-01 12:00:00"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.result.total").value(1))
            .andExpect(jsonPath("$.result.records[0].reportId").value("report-1"));
    }

    @Test
    void shouldRejectInvalidCreateTimeFormatForAdminReportList() throws Exception {
        mockMvc.perform(get("/content/user/support/admin/report/list")
                .param("createTimeStart", "2026/05/01 10:00:00")
                .param("createTimeEnd", "2026-05-01 12:00:00"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldPropagateReportDetailExceptionForAdmin() throws Exception {
        when(supportService.getReportDetailForAdmin("report-404"))
            .thenThrow(new JeecgBootException("举报不存在"));

        mockMvc.perform(get("/content/user/support/admin/report/detail")
                .param("reportId", "report-404"))
            .andExpect(status().isInternalServerError());
    }
```

- [ ] **Step 2: Run test to verify it fails**

Run:

```bash
cd /Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/.worktrees/content-user-domain-review/jeecg-boot
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH="$JAVA_HOME/bin:$PATH"
mvn -pl jeecg-boot-module/jeecg-module-content \
  -Dtest=ContentUserSupportAdminControllerWebMvcTest \
  -Dsurefire.failIfNoSpecifiedTests=false test
```

Expected: FAIL if current standalone MockMvc setup does not yet assert the desired exception propagation behavior or if time parameter binding behaves differently than expected.

- [ ] **Step 3: Write the minimal implementation**

If the tests fail only because the test class is missing the new imports and methods, no production code changes are needed. Keep all implementation inside `ContentUserSupportAdminControllerWebMvcTest.java`.

If a real defect is exposed, apply the smallest possible fix and keep it scoped to:

```java
// Allowed only if tests prove it is necessary:
// - ContentUserSupportAdminController.java
// - ContentUserReportAdminQueryReq.java
```

Do not add a global exception handler or change response structures for this task.

- [ ] **Step 4: Run test to verify it passes**

Run:

```bash
cd /Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/.worktrees/content-user-domain-review/jeecg-boot
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH="$JAVA_HOME/bin:$PATH"
mvn -pl jeecg-boot-module/jeecg-module-content \
  -Dtest=ContentUserSupportAdminControllerWebMvcTest,ContentUserSupportServiceTest \
  -Dsurefire.failIfNoSpecifiedTests=false test
```

Expected: PASS with both admin controller and support service regression tests green.

- [ ] **Step 5: Commit**

```bash
cd /Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/.worktrees/content-user-domain-review/jeecg-boot
git add jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/controller/ContentUserSupportAdminControllerWebMvcTest.java
git commit -m "test: harden admin report query webmvc coverage"
```

## Review Checklist

- [ ] `report/detail` has an exception propagation test.
- [ ] `createTimeStart` and `createTimeEnd` have a successful binding test.
- [ ] Invalid create-time format returns `400`.
- [ ] Existing admin query tests continue to pass.
- [ ] No production code changes are introduced unless a real defect is proven by the new tests.
