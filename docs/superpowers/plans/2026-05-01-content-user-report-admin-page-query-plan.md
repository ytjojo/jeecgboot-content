# Content User Report Admin Page Query Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build paged admin-side report query with extended filters for the content user support domain inside `jeecg-module-content`.

**Architecture:** Keep the existing `ContentUserSupportAdminController` and `IContentUserSupportService` as the single admin entrypoint. Replace the current list-returning admin report query with a request-object based paged query, backed by MyBatis-Plus `Page` plus `LambdaQueryWrapper`, and wrap persistence results in module-local request/response models.

**Tech Stack:** Java 21, Spring Boot 3, MyBatis-Plus, Jeecg `Result`, JUnit 5, Mockito, MockMvc, Jakarta Validation

---

## File Structure

### Files to create

- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/req/support/ContentUserReportAdminQueryReq.java`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/vo/ContentUserReportAdminPageVO.java`

### Files to modify

- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/IContentUserSupportService.java`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/impl/ContentUserSupportServiceImpl.java`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/controller/ContentUserSupportAdminController.java`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserSupportServiceTest.java`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/controller/ContentUserSupportAdminControllerWebMvcTest.java`

## Task 1: Add Paged Query Models And Service Support

**Files:**
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/req/support/ContentUserReportAdminQueryReq.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/vo/ContentUserReportAdminPageVO.java`
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/IContentUserSupportService.java`
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/impl/ContentUserSupportServiceImpl.java`
- Test: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserSupportServiceTest.java`

- [ ] **Step 1: Write the failing service tests**

Add these imports to `ContentUserSupportServiceTest.java`:

```java
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.jeecg.modules.content.user.req.support.ContentUserReportAdminQueryReq;
import org.jeecg.modules.content.user.vo.ContentUserReportAdminPageVO;
```

Replace the old admin list tests with these paged-query tests:

```java
    @Test
    void shouldListReportsForAdminWithDefaultPage() {
        Date resolvedAt = new Date();
        ContentUserReport report = new ContentUserReport()
            .setUserId("u1")
            .setTargetType("CONTENT")
            .setTargetId("post-1")
            .setReportType("SPAM")
            .setStatus("PENDING")
            .setResultStatus("INIT")
            .setResolvedBy("admin-1")
            .setResolvedAt(resolvedAt);
        report.setId("report-1");
        report.setCreateTime(new Date(2000L));

        when(reportMapper.selectPage(any(), any())).thenAnswer(invocation -> {
            IPage<ContentUserReport> page = invocation.getArgument(0);
            page.setRecords(List.of(report));
            page.setTotal(1L);
            return page;
        });

        ContentUserReportAdminPageVO result = supportService.listReportsForAdmin(new ContentUserReportAdminQueryReq());

        assertThat(result.getTotal()).isEqualTo(1L);
        assertThat(result.getPageNo()).isEqualTo(1L);
        assertThat(result.getPageSize()).isEqualTo(10L);
        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getRecords().get(0).getReportId()).isEqualTo("report-1");
        verify(reportMapper).selectPage(argThat(page -> page.getCurrent() == 1L && page.getSize() == 10L),
            argThat(wrapper -> wrapper != null));
    }

    @Test
    void shouldListReportsForAdminByExtendedFilters() {
        ContentUserReport report = new ContentUserReport()
            .setUserId("u1")
            .setTargetType("CONTENT")
            .setTargetId("post-1")
            .setReportType("SPAM")
            .setStatus("RESOLVED")
            .setResultStatus("CONFIRMED")
            .setResolvedBy("admin-1");
        report.setId("report-1");

        when(reportMapper.selectPage(any(), any())).thenAnswer(invocation -> {
            IPage<ContentUserReport> page = invocation.getArgument(0);
            page.setRecords(List.of(report));
            page.setTotal(1L);
            return page;
        });

        ContentUserReportAdminQueryReq req = new ContentUserReportAdminQueryReq()
            .setPageNo(2L)
            .setPageSize(20L)
            .setStatus("RESOLVED")
            .setResultStatus("CONFIRMED")
            .setUserId("u1")
            .setTargetType("CONTENT")
            .setTargetId("post-1")
            .setReportType("SPAM")
            .setResolvedBy("admin-1")
            .setCreateTimeStart(new Date(1000L))
            .setCreateTimeEnd(new Date(2000L));

        ContentUserReportAdminPageVO result = supportService.listReportsForAdmin(req);

        assertThat(result.getPageNo()).isEqualTo(2L);
        assertThat(result.getPageSize()).isEqualTo(20L);
        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getRecords().get(0).getStatus()).isEqualTo("RESOLVED");
        verify(reportMapper).selectPage(argThat(page -> page.getCurrent() == 2L && page.getSize() == 20L),
            argThat(wrapper -> wrapper != null));
    }

    @Test
    void shouldReturnEmptyPageWhenNoReportMatched() {
        when(reportMapper.selectPage(any(), any())).thenAnswer(invocation -> {
            IPage<ContentUserReport> page = invocation.getArgument(0);
            page.setRecords(List.of());
            page.setTotal(0L);
            return page;
        });

        ContentUserReportAdminPageVO result = supportService.listReportsForAdmin(
            new ContentUserReportAdminQueryReq().setStatus("PENDING"));

        assertThat(result.getTotal()).isZero();
        assertThat(result.getRecords()).isEmpty();
    }

    @Test
    void shouldRejectListReportsForAdminWhenCreateTimeRangeIsInvalid() {
        ContentUserReportAdminQueryReq req = new ContentUserReportAdminQueryReq()
            .setCreateTimeStart(new Date(2000L))
            .setCreateTimeEnd(new Date(1000L));

        assertThatThrownBy(() -> supportService.listReportsForAdmin(req))
            .isInstanceOf(JeecgBootException.class)
            .hasMessage("创建时间范围非法");
    }
```

- [ ] **Step 2: Run test to verify it fails**

Run:

```bash
cd /Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/.worktrees/content-user-domain-review/jeecg-boot
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH="$JAVA_HOME/bin:$PATH"
mvn -pl jeecg-boot-module/jeecg-module-content \
  -Dtest=ContentUserSupportServiceTest \
  -Dsurefire.failIfNoSpecifiedTests=false test
```

Expected: FAIL with missing types like `ContentUserReportAdminQueryReq`, `ContentUserReportAdminPageVO`, missing `selectPage(...)` stubbing, or outdated service method signature.

- [ ] **Step 3: Write the minimal implementation**

Create `ContentUserReportAdminQueryReq.java`:

```java
package org.jeecg.modules.content.user.req.support;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
@Accessors(chain = true)
@Schema(description = "内容社区举报后台分页查询请求")
public class ContentUserReportAdminQueryReq {

    @Min(value = 1, message = "页码不能小于1")
    @Schema(description = "页码")
    private Long pageNo;

    @Min(value = 1, message = "每页条数不能小于1")
    @Max(value = 100, message = "每页条数不能超过100")
    @Schema(description = "每页条数")
    private Long pageSize;

    @Schema(description = "举报状态")
    private String status;

    @Schema(description = "处理结果状态")
    private String resultStatus;

    @Schema(description = "举报用户ID")
    private String userId;

    @Schema(description = "举报目标类型")
    private String targetType;

    @Schema(description = "举报目标ID")
    private String targetId;

    @Schema(description = "举报类型")
    private String reportType;

    @Schema(description = "处理人")
    private String resolvedBy;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建开始时间")
    private Date createTimeStart;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建结束时间")
    private Date createTimeEnd;
}
```

Create `ContentUserReportAdminPageVO.java`:

```java
package org.jeecg.modules.content.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
@Schema(description = "内容社区举报后台分页结果")
public class ContentUserReportAdminPageVO {

    @Schema(description = "分页记录")
    private List<ContentUserReportAdminListItemVO> records;

    @Schema(description = "总记录数")
    private Long total;

    @Schema(description = "当前页码")
    private Long pageNo;

    @Schema(description = "每页条数")
    private Long pageSize;
}
```

Update `IContentUserSupportService.java` imports and signature:

```java
import org.jeecg.modules.content.user.req.support.ContentUserReportAdminQueryReq;
import org.jeecg.modules.content.user.vo.ContentUserReportAdminPageVO;
```

```java
    ContentUserReportAdminPageVO listReportsForAdmin(ContentUserReportAdminQueryReq req);
```

Update `ContentUserSupportServiceImpl.java` imports:

```java
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.jeecg.modules.content.user.req.support.ContentUserReportAdminQueryReq;
import org.jeecg.modules.content.user.vo.ContentUserReportAdminPageVO;
```

Replace the current admin list service method with:

```java
    @Override
    public ContentUserReportAdminPageVO listReportsForAdmin(ContentUserReportAdminQueryReq req) {
        validateAdminQueryTimeRange(req);
        long pageNo = req.getPageNo() == null ? 1L : req.getPageNo();
        long pageSize = req.getPageSize() == null ? 10L : req.getPageSize();

        LambdaQueryWrapper<ContentUserReport> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StringUtils.hasText(req.getStatus()), ContentUserReport::getStatus, req.getStatus())
            .eq(StringUtils.hasText(req.getResultStatus()), ContentUserReport::getResultStatus, req.getResultStatus())
            .eq(StringUtils.hasText(req.getUserId()), ContentUserReport::getUserId, req.getUserId())
            .eq(StringUtils.hasText(req.getTargetType()), ContentUserReport::getTargetType, req.getTargetType())
            .eq(StringUtils.hasText(req.getTargetId()), ContentUserReport::getTargetId, req.getTargetId())
            .eq(StringUtils.hasText(req.getReportType()), ContentUserReport::getReportType, req.getReportType())
            .eq(StringUtils.hasText(req.getResolvedBy()), ContentUserReport::getResolvedBy, req.getResolvedBy())
            .ge(req.getCreateTimeStart() != null, ContentUserReport::getCreateTime, req.getCreateTimeStart())
            .le(req.getCreateTimeEnd() != null, ContentUserReport::getCreateTime, req.getCreateTimeEnd())
            .orderByDesc(ContentUserReport::getCreateTime);

        IPage<ContentUserReport> page = reportMapper.selectPage(new Page<>(pageNo, pageSize), queryWrapper);
        return new ContentUserReportAdminPageVO()
            .setRecords(page.getRecords().stream().map(this::toAdminListItem).toList())
            .setTotal(page.getTotal())
            .setPageNo(page.getCurrent())
            .setPageSize(page.getSize());
    }
```

Add this helper near the other private helpers in `ContentUserSupportServiceImpl.java`:

```java
    private void validateAdminQueryTimeRange(ContentUserReportAdminQueryReq req) {
        if (req == null) {
            return;
        }
        if (req.getCreateTimeStart() != null
            && req.getCreateTimeEnd() != null
            && req.getCreateTimeStart().after(req.getCreateTimeEnd())) {
            throw new JeecgBootException("创建时间范围非法");
        }
    }
```

If `req` can be null in callers, normalize it before validation:

```java
        if (req == null) {
            req = new ContentUserReportAdminQueryReq();
        }
```

- [ ] **Step 4: Run test to verify it passes**

Run:

```bash
cd /Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/.worktrees/content-user-domain-review/jeecg-boot
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH="$JAVA_HOME/bin:$PATH"
mvn -pl jeecg-boot-module/jeecg-module-content \
  -Dtest=ContentUserSupportServiceTest \
  -Dsurefire.failIfNoSpecifiedTests=false test
```

Expected: PASS with the service test suite green.

- [ ] **Step 5: Commit**

```bash
cd /Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/.worktrees/content-user-domain-review/jeecg-boot
git add jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/req/support/ContentUserReportAdminQueryReq.java \
  jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/vo/ContentUserReportAdminPageVO.java \
  jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/IContentUserSupportService.java \
  jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/impl/ContentUserSupportServiceImpl.java \
  jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserSupportServiceTest.java
git commit -m "feat: add admin report page query service"
```

## Task 2: Expose Paged Query Endpoint

**Files:**
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/controller/ContentUserSupportAdminController.java`
- Test: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/controller/ContentUserSupportAdminControllerWebMvcTest.java`

- [ ] **Step 1: Write the failing controller tests**

Add these imports to `ContentUserSupportAdminControllerWebMvcTest.java`:

```java
import org.jeecg.modules.content.user.req.support.ContentUserReportAdminQueryReq;
import org.jeecg.modules.content.user.vo.ContentUserReportAdminPageVO;
```

Replace the current admin list controller test with:

```java
    @Test
    void shouldListReportsForAdminWithPageQuery() throws Exception {
        Date resolvedAt = new Date(1735689600000L);
        when(supportService.listReportsForAdmin(any(ContentUserReportAdminQueryReq.class)))
            .thenReturn(new ContentUserReportAdminPageVO()
                .setTotal(1L)
                .setPageNo(2L)
                .setPageSize(20L)
                .setRecords(List.of(new ContentUserReportAdminListItemVO()
                    .setReportId("report-1")
                    .setUserId("u1")
                    .setTargetType("CONTENT")
                    .setTargetId("post-1")
                    .setReportType("SPAM")
                    .setStatus("RESOLVED")
                    .setResultStatus("CONFIRMED")
                    .setResolvedBy("admin-1")
                    .setResolvedAt(resolvedAt)
                    .setCreateTime(resolvedAt))));

        mockMvc.perform(get("/content/user/support/admin/report/list")
                .param("pageNo", "2")
                .param("pageSize", "20")
                .param("status", "RESOLVED")
                .param("resultStatus", "CONFIRMED")
                .param("userId", "u1")
                .param("targetType", "CONTENT")
                .param("targetId", "post-1")
                .param("reportType", "SPAM")
                .param("resolvedBy", "admin-1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.result.total").value(1))
            .andExpect(jsonPath("$.result.pageNo").value(2))
            .andExpect(jsonPath("$.result.pageSize").value(20))
            .andExpect(jsonPath("$.result.records[0].reportId").value("report-1"));
    }

    @Test
    void shouldRejectInvalidPageSizeForAdminReportList() throws Exception {
        mockMvc.perform(get("/content/user/support/admin/report/list")
                .param("pageNo", "1")
                .param("pageSize", "101"))
            .andExpect(status().isBadRequest());
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

Expected: FAIL because the controller still binds individual string params and returns list output instead of paged output.

- [ ] **Step 3: Write the minimal implementation**

Update `ContentUserSupportAdminController.java` imports:

```java
import org.jeecg.modules.content.user.req.support.ContentUserReportAdminQueryReq;
import org.jeecg.modules.content.user.vo.ContentUserReportAdminPageVO;
```

Replace the current list method with:

```java
    @Operation(summary = "查询举报列表")
    @GetMapping("/report/list")
    public Result<ContentUserReportAdminPageVO> listReports(@Valid ContentUserReportAdminQueryReq req) {
        return Result.OK(supportService.listReportsForAdmin(req));
    }
```

Keep the detail endpoint unchanged.

- [ ] **Step 4: Run tests to verify it passes**

Run:

```bash
cd /Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/.worktrees/content-user-domain-review/jeecg-boot
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH="$JAVA_HOME/bin:$PATH"
mvn -pl jeecg-boot-module/jeecg-module-content \
  -Dtest=ContentUserSupportAdminControllerWebMvcTest,ContentUserSupportServiceTest \
  -Dsurefire.failIfNoSpecifiedTests=false test
```

Expected: PASS with both service and controller tests green.

- [ ] **Step 5: Commit**

```bash
cd /Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/.worktrees/content-user-domain-review/jeecg-boot
git add jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/controller/ContentUserSupportAdminController.java \
  jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/controller/ContentUserSupportAdminControllerWebMvcTest.java
git commit -m "feat: add admin report page query endpoint"
```

## Review Checklist

- [ ] Admin report list uses `ContentUserReportAdminQueryReq` instead of multiple string parameters.
- [ ] Default paging is `pageNo=1` and `pageSize=10`.
- [ ] Page size is capped at `100`.
- [ ] Filters include `status`, `resultStatus`, `userId`, `targetType`, `targetId`, `reportType`, `resolvedBy`, `createTimeStart`, and `createTimeEnd`.
- [ ] Query sorts by `createTime` descending only.
- [ ] Invalid create time range throws `JeecgBootException("创建时间范围非法")`.
- [ ] Detail endpoint remains unchanged and still returns `ContentUserReportAdminDetailVO`.
- [ ] No mapper XML or SQL migration is introduced.
