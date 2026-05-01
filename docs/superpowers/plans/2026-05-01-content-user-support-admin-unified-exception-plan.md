# Content User Support Admin Unified Exception Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Migrate support admin controller WebMvc tests to the project's unified exception handling style and add business-error coverage for all admin support endpoints.

**Architecture:** Keep all work inside `ContentUserSupportAdminControllerWebMvcTest`. Replace standalone MockMvc wiring with `@WebMvcTest` plus `JeecgBootExceptionHandler` import so the controller tests validate the real project-level error envelope for `JeecgBootException` without changing production controller or exception-handler code.

**Tech Stack:** Java 21, Spring Boot 3 `@WebMvcTest`, MockMvc, Mockito, JUnit 5, Jakarta Validation

---

## File Structure

### Files to modify

- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/controller/ContentUserSupportAdminControllerWebMvcTest.java`

## Task 1: Switch Support Admin WebMvc Test Infrastructure

**Files:**
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/controller/ContentUserSupportAdminControllerWebMvcTest.java`

- [ ] **Step 1: Replace standalone MockMvc setup with `@WebMvcTest` wiring**

Update the test class annotations and fields to match the project's controller-test pattern:

```java
@AutoConfigureMockMvc(addFilters = false)
@Import(JeecgBootExceptionHandler.class)
@WebMvcTest(
    controllers = ContentUserSupportAdminController.class,
    excludeAutoConfiguration = {
        SecurityAutoConfiguration.class,
        SecurityFilterAutoConfiguration.class
    }
)
class ContentUserSupportAdminControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IContentUserSupportService supportService;

    @MockitoBean
    private BaseCommonService baseCommonService;
}
```

Remove the old standalone-specific pieces:

```java
@ExtendWith(MockitoExtension.class)
private MockMvc mockMvc;

@Mock
private IContentUserSupportService supportService;

@InjectMocks
private ContentUserSupportAdminController supportAdminController;

@BeforeEach
void setUp() {
    LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
    validator.afterPropertiesSet();
    mockMvc = MockMvcBuilders.standaloneSetup(supportAdminController)
        .setValidator(validator)
        .build();
}
```

- [ ] **Step 2: Fix imports after the test infrastructure migration**

Add imports required by the new test style:

```java
import org.jeecg.common.exception.JeecgBootExceptionHandler;
import org.jeecg.modules.base.service.BaseCommonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
```

Remove imports no longer needed by standalone MockMvc:

```java
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
```

## Task 2: Add Unified Business-Error Coverage

**Files:**
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/controller/ContentUserSupportAdminControllerWebMvcTest.java`

- [ ] **Step 3: Add appeal-handle business error test**

Add:

```java
@Test
void shouldReturnBusinessErrorWhenHandleAppealThrowsException() throws Exception {
    when(supportService.handleAppeal(any(ContentAppealHandleReq.class)))
        .thenThrow(new JeecgBootException("申诉不存在"));

    mockMvc.perform(post("/content/user/support/admin/appeal/handle")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"appealId":"appeal-404","operatorUserId":"admin-1","status":"RESOLVED","resultStatus":"REJECTED","resultNote":"驳回","progressNote":"已处理完成"}
                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("申诉不存在"));
}
```

- [ ] **Step 4: Add report-handle business error test**

Add:

```java
@Test
void shouldReturnBusinessErrorWhenHandleReportThrowsException() throws Exception {
    when(supportService.handleReport(any(ContentReportHandleReq.class)))
        .thenThrow(new JeecgBootException("举报不存在"));

    mockMvc.perform(post("/content/user/support/admin/report/handle")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"reportId":"report-404","operatorUserId":"admin-1","status":"RESOLVED","resultStatus":"CONFIRMED","resultNote":"违规成立","progressNote":"已处理完成"}
                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("举报不存在"));
}
```

- [ ] **Step 5: Add report-list business error test**

Add:

```java
@Test
void shouldReturnBusinessErrorWhenListReportsThrowsException() throws Exception {
    when(supportService.listReportsForAdmin(any(ContentUserReportAdminQueryReq.class)))
        .thenThrow(new JeecgBootException("创建时间范围非法"));

    mockMvc.perform(get("/content/user/support/admin/report/list")
            .param("createTimeStart", "2026-05-01 12:00:00")
            .param("createTimeEnd", "2026-05-01 10:00:00"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("创建时间范围非法"));
}
```

- [ ] **Step 6: Replace the old detail exception assertion with unified error assertion**

Change the current detail exception test from exception propagation to unified business-error response:

```java
@Test
void shouldReturnBusinessErrorWhenGetReportDetailThrowsException() throws Exception {
    when(supportService.getReportDetailForAdmin("report-404"))
        .thenThrow(new JeecgBootException("举报不存在"));

    mockMvc.perform(get("/content/user/support/admin/report/detail")
            .param("reportId", "report-404"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("举报不存在"));
}
```

## Task 3: Preserve Existing Success and Validation Coverage

**Files:**
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/controller/ContentUserSupportAdminControllerWebMvcTest.java`

- [ ] **Step 7: Keep existing successful-path tests green after migration**

Ensure these tests remain present and still pass under `@WebMvcTest`:

- `shouldHandleAppeal`
- `shouldHandleReport`
- `shouldListReportsForAdminWithPageQuery`
- `shouldBindCreateTimeRangeForAdminReportList`
- `shouldGetReportDetailForAdmin`

- [ ] **Step 8: Keep existing invalid-request checks stable**

Ensure these tests still validate request rejection behavior after the migration:

- `shouldRejectInvalidHandleRequest`
- `shouldRejectInvalidHandleReportRequest`
- `shouldRejectInvalidPageSizeForAdminReportList`
- `shouldRejectInvalidCreateTimeFormatForAdminReportList`

Note: if Spring exception resolution changes the response envelope for some validation failures under `@WebMvcTest`, keep the assertions aligned with actual framework behavior unless the spec explicitly requires broader normalization.

## Task 4: Verify the Migration

**Files:**
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/controller/ContentUserSupportAdminControllerWebMvcTest.java`

- [ ] **Step 9: Run the controller WebMvc test first**

Run:

```bash
cd /Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/.worktrees/content-user-domain-review/jeecg-boot
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH="$JAVA_HOME/bin:$PATH"
mvn -pl jeecg-boot-module/jeecg-module-content \
  -Dtest=ContentUserSupportAdminControllerWebMvcTest \
  -Dsurefire.failIfNoSpecifiedTests=false test
```

Expected: PASS with the support admin controller test fully green under the new `@WebMvcTest` setup.

- [ ] **Step 10: Run focused regression tests**

Run:

```bash
cd /Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/.worktrees/content-user-domain-review/jeecg-boot
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH="$JAVA_HOME/bin:$PATH"
mvn -pl jeecg-boot-module/jeecg-module-content \
  -Dtest=ContentUserSupportAdminControllerWebMvcTest,ContentUserSupportServiceTest \
  -Dsurefire.failIfNoSpecifiedTests=false test
```

Expected: PASS with controller migration and prior support service regression coverage both green.

- [ ] **Step 11: Check diagnostics**

Inspect diagnostics for:

- `ContentUserSupportAdminControllerWebMvcTest.java`

Fix any introduced import, annotation, or assertion issues if they are straightforward.

- [ ] **Step 12: Commit**

```bash
cd /Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/.worktrees/content-user-domain-review/jeecg-boot
git add jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/controller/ContentUserSupportAdminControllerWebMvcTest.java
git commit -m "test: unify support admin controller error coverage"
```

## Review Checklist

- [ ] `ContentUserSupportAdminControllerWebMvcTest` uses `@WebMvcTest` instead of standalone MockMvc.
- [ ] `JeecgBootExceptionHandler` is imported into the controller test.
- [ ] `BaseCommonService` is mocked for exception logging.
- [ ] All 4 support admin endpoints have business-error response coverage.
- [ ] Business-error assertions verify `status().isOk()`, `success=false`, and the expected `message`.
- [ ] Existing success-path and validation-path tests still pass.
- [ ] No production code changes are introduced unless the migration exposes a real defect.
