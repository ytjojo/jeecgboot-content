# Content User Report Admin Query Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build admin-side report list and detail query APIs for the content user support domain inside `jeecg-module-content`.

**Architecture:** Extend the existing `ContentUserSupportAdminController` and `IContentUserSupportService` instead of introducing a new admin query service. Reuse `ContentUserReportMapper` with MyBatis-Plus `LambdaQueryWrapper` for filtering and sorting, and add two focused admin VO types for list and detail responses.

**Tech Stack:** Java 21, Spring Boot 3, MyBatis-Plus, Jeecg `Result`, JUnit 5, Mockito, MockMvc, Jakarta Validation

---

## File Structure

### Files to create

- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/vo/ContentUserReportAdminListItemVO.java`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/vo/ContentUserReportAdminDetailVO.java`

### Files to modify

- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/IContentUserSupportService.java`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/impl/ContentUserSupportServiceImpl.java`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/controller/ContentUserSupportAdminController.java`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserSupportServiceTest.java`
- `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/controller/ContentUserSupportAdminControllerWebMvcTest.java`

## Task 1: Add Admin Query VO And Service Support

**Files:**
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/vo/ContentUserReportAdminListItemVO.java`
- Create: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/vo/ContentUserReportAdminDetailVO.java`
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/IContentUserSupportService.java`
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/impl/ContentUserSupportServiceImpl.java`
- Test: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserSupportServiceTest.java`

- [ ] **Step 1: Write the failing service tests**

Add these tests to `ContentUserSupportServiceTest`:

```java
    @Test
    void shouldListReportsForAdminWithoutFilter() {
        ContentUserReport newerReport = new ContentUserReport()
            .setUserId("u1")
            .setTargetType("CONTENT")
            .setTargetId("post-1")
            .setReportType("SPAM")
            .setStatus("RESOLVED")
            .setResultStatus("CONFIRMED")
            .setResolvedBy("admin-1")
            .setResolvedAt(new Date());
        newerReport.setId("report-1");
        newerReport.setCreateTime(new Date(2000L));
        ContentUserReport olderReport = new ContentUserReport()
            .setUserId("u2")
            .setTargetType("COMMENT")
            .setTargetId("comment-1")
            .setReportType("ABUSE")
            .setStatus("PENDING");
        olderReport.setId("report-2");
        olderReport.setCreateTime(new Date(1000L));
        when(reportMapper.selectList(any())).thenReturn(List.of(newerReport, olderReport));

        List<ContentUserReportAdminListItemVO> result =
            supportService.listReportsForAdmin(null, null, null);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(ContentUserReportAdminListItemVO::getReportId)
            .containsExactly("report-1", "report-2");
        assertThat(result.get(0).getResolvedBy()).isEqualTo("admin-1");
    }

    @Test
    void shouldListReportsForAdminByStatusAndTargetType() {
        ContentUserReport report = new ContentUserReport()
            .setUserId("u1")
            .setTargetType("CONTENT")
            .setTargetId("post-1")
            .setReportType("SPAM")
            .setStatus("PENDING");
        report.setId("report-1");
        when(reportMapper.selectList(any())).thenReturn(List.of(report));

        List<ContentUserReportAdminListItemVO> result =
            supportService.listReportsForAdmin("PENDING", "u1", "CONTENT");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo("PENDING");
        verify(reportMapper).selectList(argThat(wrapper -> wrapper != null));
    }

    @Test
    void shouldGetReportDetailForAdmin() {
        Date resolvedAt = new Date();
        ContentUserReport report = new ContentUserReport()
            .setUserId("u1")
            .setTargetType("CONTENT")
            .setTargetId("post-1")
            .setReportType("SPAM")
            .setReason("垃圾内容")
            .setEvidenceJson("{\"screenshot\":true}")
            .setStatus("RESOLVED")
            .setResultStatus("CONFIRMED")
            .setResultNote("违规成立")
            .setProgressNote("已处理完成")
            .setResolvedBy("admin-1")
            .setResolvedAt(resolvedAt);
        report.setId("report-1");
        report.setCreateTime(new Date(1000L));
        when(reportMapper.selectById("report-1")).thenReturn(report);

        ContentUserReportAdminDetailVO result =
            supportService.getReportDetailForAdmin("report-1");

        assertThat(result.getReportId()).isEqualTo("report-1");
        assertThat(result.getReason()).isEqualTo("垃圾内容");
        assertThat(result.getEvidenceJson()).isEqualTo("{\"screenshot\":true}");
        assertThat(result.getResolvedAt()).isEqualTo(resolvedAt);
    }

    @Test
    void shouldRejectGetReportDetailForAdminWhenReportDoesNotExist() {
        when(reportMapper.selectById("report-404")).thenReturn(null);

        assertThatThrownBy(() -> supportService.getReportDetailForAdmin("report-404"))
            .isInstanceOf(JeecgBootException.class)
            .hasMessage("举报不存在");
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

Expected: FAIL with missing methods such as `listReportsForAdmin`, `getReportDetailForAdmin`, or missing VO classes.

- [ ] **Step 3: Write the minimal implementation**

Create `ContentUserReportAdminListItemVO.java`:

```java
package org.jeecg.modules.content.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

@Data
@Accessors(chain = true)
@Schema(description = "内容社区举报后台列表项视图")
public class ContentUserReportAdminListItemVO {

    @Schema(description = "举报ID")
    private String reportId;

    @Schema(description = "举报用户ID")
    private String userId;

    @Schema(description = "举报目标类型")
    private String targetType;

    @Schema(description = "举报目标ID")
    private String targetId;

    @Schema(description = "举报类型")
    private String reportType;

    @Schema(description = "举报状态")
    private String status;

    @Schema(description = "处理结果状态")
    private String resultStatus;

    @Schema(description = "处理人")
    private String resolvedBy;

    @Schema(description = "处理完成时间")
    private Date resolvedAt;

    @Schema(description = "创建时间")
    private Date createTime;
}
```

Create `ContentUserReportAdminDetailVO.java`:

```java
package org.jeecg.modules.content.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

@Data
@Accessors(chain = true)
@Schema(description = "内容社区举报后台详情视图")
public class ContentUserReportAdminDetailVO {

    @Schema(description = "举报ID")
    private String reportId;

    @Schema(description = "举报用户ID")
    private String userId;

    @Schema(description = "举报目标类型")
    private String targetType;

    @Schema(description = "举报目标ID")
    private String targetId;

    @Schema(description = "举报类型")
    private String reportType;

    @Schema(description = "举报原因")
    private String reason;

    @Schema(description = "举报证据JSON")
    private String evidenceJson;

    @Schema(description = "举报状态")
    private String status;

    @Schema(description = "处理结果状态")
    private String resultStatus;

    @Schema(description = "处理结果说明")
    private String resultNote;

    @Schema(description = "处理进度说明")
    private String progressNote;

    @Schema(description = "处理人")
    private String resolvedBy;

    @Schema(description = "处理完成时间")
    private Date resolvedAt;

    @Schema(description = "创建时间")
    private Date createTime;
}
```

Extend `IContentUserSupportService.java`:

```java
import org.jeecg.modules.content.user.vo.ContentUserReportAdminDetailVO;
import org.jeecg.modules.content.user.vo.ContentUserReportAdminListItemVO;

    List<ContentUserReportAdminListItemVO> listReportsForAdmin(String status,
                                                               String userId,
                                                               String targetType);

    ContentUserReportAdminDetailVO getReportDetailForAdmin(String reportId);
```

Add this code to `ContentUserSupportServiceImpl.java`:

```java
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.jeecg.modules.content.user.vo.ContentUserReportAdminDetailVO;
import org.jeecg.modules.content.user.vo.ContentUserReportAdminListItemVO;
import org.springframework.util.StringUtils;

    @Override
    public List<ContentUserReportAdminListItemVO> listReportsForAdmin(String status,
                                                                      String userId,
                                                                      String targetType) {
        LambdaQueryWrapper<ContentUserReport> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StringUtils.hasText(status), ContentUserReport::getStatus, status)
            .eq(StringUtils.hasText(userId), ContentUserReport::getUserId, userId)
            .eq(StringUtils.hasText(targetType), ContentUserReport::getTargetType, targetType)
            .orderByDesc(ContentUserReport::getCreateTime);
        return reportMapper.selectList(queryWrapper).stream()
            .map(this::toAdminListItem)
            .toList();
    }

    @Override
    public ContentUserReportAdminDetailVO getReportDetailForAdmin(String reportId) {
        ContentUserReport report = reportMapper.selectById(reportId);
        if (report == null) {
            throw new JeecgBootException("举报不存在");
        }
        return toAdminDetail(report);
    }

    private ContentUserReportAdminListItemVO toAdminListItem(ContentUserReport report) {
        return new ContentUserReportAdminListItemVO()
            .setReportId(report.getId())
            .setUserId(report.getUserId())
            .setTargetType(report.getTargetType())
            .setTargetId(report.getTargetId())
            .setReportType(report.getReportType())
            .setStatus(report.getStatus())
            .setResultStatus(report.getResultStatus())
            .setResolvedBy(report.getResolvedBy())
            .setResolvedAt(report.getResolvedAt())
            .setCreateTime(report.getCreateTime());
    }

    private ContentUserReportAdminDetailVO toAdminDetail(ContentUserReport report) {
        return new ContentUserReportAdminDetailVO()
            .setReportId(report.getId())
            .setUserId(report.getUserId())
            .setTargetType(report.getTargetType())
            .setTargetId(report.getTargetId())
            .setReportType(report.getReportType())
            .setReason(report.getReason())
            .setEvidenceJson(report.getEvidenceJson())
            .setStatus(report.getStatus())
            .setResultStatus(report.getResultStatus())
            .setResultNote(report.getResultNote())
            .setProgressNote(report.getProgressNote())
            .setResolvedBy(report.getResolvedBy())
            .setResolvedAt(report.getResolvedAt())
            .setCreateTime(report.getCreateTime());
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
git add jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/IContentUserSupportService.java \
  jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/impl/ContentUserSupportServiceImpl.java \
  jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/vo/ContentUserReportAdminListItemVO.java \
  jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/vo/ContentUserReportAdminDetailVO.java \
  jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserSupportServiceTest.java
git commit -m "feat: add admin report query service"
```

## Task 2: Expose Admin List And Detail Endpoints

**Files:**
- Modify: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/controller/ContentUserSupportAdminController.java`
- Test: `jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/controller/ContentUserSupportAdminControllerWebMvcTest.java`

- [ ] **Step 1: Write the failing controller tests**

Add these tests to `ContentUserSupportAdminControllerWebMvcTest`:

```java
    @Test
    void shouldListReportsForAdmin() throws Exception {
        Date resolvedAt = new Date(1735689600000L);
        when(supportService.listReportsForAdmin("PENDING", "u1", "CONTENT"))
            .thenReturn(List.of(new ContentUserReportAdminListItemVO()
                .setReportId("report-1")
                .setUserId("u1")
                .setTargetType("CONTENT")
                .setTargetId("post-1")
                .setReportType("SPAM")
                .setStatus("PENDING")
                .setResultStatus("INIT")
                .setResolvedBy("admin-1")
                .setResolvedAt(resolvedAt)
                .setCreateTime(resolvedAt)));

        mockMvc.perform(get("/content/user/support/admin/report/list")
                .param("status", "PENDING")
                .param("userId", "u1")
                .param("targetType", "CONTENT"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.result[0].reportId").value("report-1"))
            .andExpect(jsonPath("$.result[0].targetType").value("CONTENT"));
    }

    @Test
    void shouldGetReportDetailForAdmin() throws Exception {
        Date resolvedAt = new Date(1735689600000L);
        when(supportService.getReportDetailForAdmin("report-1"))
            .thenReturn(new ContentUserReportAdminDetailVO()
                .setReportId("report-1")
                .setUserId("u1")
                .setTargetType("CONTENT")
                .setTargetId("post-1")
                .setReportType("SPAM")
                .setReason("垃圾内容")
                .setEvidenceJson("{\"screenshot\":true}")
                .setStatus("RESOLVED")
                .setResultStatus("CONFIRMED")
                .setResultNote("违规成立")
                .setProgressNote("已处理完成")
                .setResolvedBy("admin-1")
                .setResolvedAt(resolvedAt)
                .setCreateTime(resolvedAt));

        mockMvc.perform(get("/content/user/support/admin/report/detail")
                .param("reportId", "report-1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.result.reportId").value("report-1"))
            .andExpect(jsonPath("$.result.reason").value("垃圾内容"))
            .andExpect(jsonPath("$.result.resultStatus").value("CONFIRMED"));
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

Expected: FAIL with missing controller endpoints or missing service methods.

- [ ] **Step 3: Write the minimal implementation**

Update imports in `ContentUserSupportAdminController.java`:

```java
import org.jeecg.modules.content.user.vo.ContentUserReportAdminDetailVO;
import org.jeecg.modules.content.user.vo.ContentUserReportAdminListItemVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
```

Add these methods to `ContentUserSupportAdminController.java`:

```java
    @Operation(summary = "查询举报列表")
    @GetMapping("/report/list")
    public Result<List<ContentUserReportAdminListItemVO>> listReports(@RequestParam(value = "status", required = false) String status,
                                                                      @RequestParam(value = "userId", required = false) String userId,
                                                                      @RequestParam(value = "targetType", required = false) String targetType) {
        return Result.OK(supportService.listReportsForAdmin(status, userId, targetType));
    }

    @Operation(summary = "查询举报详情")
    @GetMapping("/report/detail")
    public Result<ContentUserReportAdminDetailVO> getReportDetail(@RequestParam("reportId") String reportId) {
        return Result.OK(supportService.getReportDetailForAdmin(reportId));
    }
```

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

Expected: PASS with both service and controller regression tests green.

- [ ] **Step 5: Commit**

```bash
cd /Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/.worktrees/content-user-domain-review/jeecg-boot
git add jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/controller/ContentUserSupportAdminController.java \
  jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/controller/ContentUserSupportAdminControllerWebMvcTest.java
git commit -m "feat: add admin report query endpoints"
```

## Review Checklist

- [ ] Admin list endpoint supports optional `status`, `userId`, and `targetType` filters only.
- [ ] Admin detail endpoint returns `reason`, `evidenceJson`, `resultNote`, and `progressNote`.
- [ ] List query sorts by `createTime` descending.
- [ ] Missing report detail throws `JeecgBootException("举报不存在")`.
- [ ] All writable changes stay inside `jeecg-module-content`.
- [ ] Controller remains thin and delegates query logic to `ContentUserSupportServiceImpl`.
- [ ] No SQL migration or mapper XML is introduced for this scope.

