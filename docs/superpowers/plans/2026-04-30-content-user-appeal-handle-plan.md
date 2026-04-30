# Content User Appeal Handle Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add an admin-side appeal handling API that writes back appeal resolution fields, enforces a minimal strict state machine, records an audit log, and is covered by focused tests.

**Architecture:** Keep the existing user-side support controller unchanged and add a dedicated admin controller for handling appeals. Extend the support service contract with a single handle method, implement strict `PENDING/PROCESSING -> RESOLVED` validation in the service layer, and reuse the existing appeal table plus audit log table for persistence and traceability.

**Tech Stack:** Java 21, Spring Boot, JeecgBoot, MyBatis-Plus, Jakarta Validation, JUnit 5, Mockito, MockMvc standalone, Maven

---

### Task 1: Add Service-Level Red Tests For Appeal Handling

**Files:**
- Modify: `/Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/.worktrees/content-user-domain-review/jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserSupportServiceTest.java`
- Read for context: `/Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/docs/superpowers/specs/2026-04-30-content-user-appeal-handle-design.md`

- [ ] **Step 1: Write the failing tests**

```java
@Test
void shouldHandlePendingAppealToResolved() {
    ContentUserAppeal appeal = new ContentUserAppeal()
        .setUserId("u1")
        .setStatus("PENDING")
        .setAppealType("PENALTY");
    appeal.setId("appeal-1");
    when(appealMapper.selectById("appeal-1")).thenReturn(appeal);

    String handledAppealId = supportService.handleAppeal(createHandleReq());

    assertThat(handledAppealId).isEqualTo("appeal-1");
    verify(appealMapper).updateById(argThat((ContentUserAppeal it) ->
        "appeal-1".equals(it.getId())
            && "RESOLVED".equals(it.getStatus())
            && "APPROVED".equals(it.getResultStatus())
            && "处罚撤销".equals(it.getResultNote())
            && "admin-1".equals(it.getResolvedBy())
            && "已处理完成".equals(it.getProgressNote())
            && it.getResolvedAt() != null));
    verify(auditLogMapper).insert(argThat((ContentUserAuditLog it) ->
        "USER_APPEAL_HANDLED".equals(it.getEventType())
            && "admin-1".equals(it.getOperatorUserId())
            && "u1".equals(it.getUserId())));
}

@Test
void shouldRejectHandledAppealWhenAppealAlreadyResolved() {
    ContentUserAppeal appeal = new ContentUserAppeal()
        .setUserId("u1")
        .setStatus("RESOLVED");
    appeal.setId("appeal-1");
    when(appealMapper.selectById("appeal-1")).thenReturn(appeal);

    assertThatThrownBy(() -> supportService.handleAppeal(createHandleReq()))
        .isInstanceOf(JeecgBootException.class)
        .hasMessage("申诉已处理完成，请勿重复处理");
}

@Test
void shouldRejectHandledAppealWhenTargetStatusIsInvalid() {
    ContentUserAppeal appeal = new ContentUserAppeal()
        .setUserId("u1")
        .setStatus("PENDING");
    appeal.setId("appeal-1");
    when(appealMapper.selectById("appeal-1")).thenReturn(appeal);

    ContentAppealHandleReq req = createHandleReq().setStatus("PROCESSING");

    assertThatThrownBy(() -> supportService.handleAppeal(req))
        .isInstanceOf(JeecgBootException.class)
        .hasMessage("申诉处理仅支持流转到RESOLVED");
}

@Test
void shouldRejectHandledAppealWhenAppealDoesNotExist() {
    when(appealMapper.selectById("appeal-1")).thenReturn(null);

    assertThatThrownBy(() -> supportService.handleAppeal(createHandleReq()))
        .isInstanceOf(JeecgBootException.class)
        .hasMessage("申诉不存在");
}

private ContentAppealHandleReq createHandleReq() {
    return new ContentAppealHandleReq()
        .setAppealId("appeal-1")
        .setOperatorUserId("admin-1")
        .setStatus("RESOLVED")
        .setResultStatus("APPROVED")
        .setResultNote("处罚撤销")
        .setProgressNote("已处理完成");
}
```

- [ ] **Step 2: Run the service test to verify it fails**

Run:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH="$JAVA_HOME/bin:$PATH"
mvn -pl jeecg-boot-module/jeecg-module-content -Dtest=ContentUserSupportServiceTest -Dsurefire.failIfNoSpecifiedTests=false test
```

Expected: FAIL with compilation errors for missing `ContentAppealHandleReq`, `handleAppeal(...)`, and `resolvedAt` accessors.

- [ ] **Step 3: Commit the failing test**

```bash
git add /Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/.worktrees/content-user-domain-review/jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserSupportServiceTest.java
git commit -m "test: add appeal handle service coverage"
```

### Task 2: Add Controller Red Tests For Admin Appeal Handle API

**Files:**
- Create: `/Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/.worktrees/content-user-domain-review/jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/controller/ContentUserSupportAdminControllerWebMvcTest.java`
- Read for style: `/Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/.worktrees/content-user-domain-review/jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/controller/ContentUserSupportControllerWebMvcTest.java`

- [ ] **Step 1: Write the failing controller tests**

```java
@ExtendWith(MockitoExtension.class)
class ContentUserSupportAdminControllerWebMvcTest {

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

    @Test
    void shouldHandleAppeal() throws Exception {
        when(supportService.handleAppeal(any(ContentAppealHandleReq.class))).thenReturn("appeal-1");

        mockMvc.perform(post("/content/user/support/admin/appeal/handle")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"appealId":"appeal-1","operatorUserId":"admin-1","status":"RESOLVED","resultStatus":"APPROVED","resultNote":"处罚撤销","progressNote":"已处理完成"}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.result").value("appeal-1"));
    }

    @Test
    void shouldRejectInvalidHandleRequest() throws Exception {
        mockMvc.perform(post("/content/user/support/admin/appeal/handle")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"appealId":"","operatorUserId":"admin-1","status":"RESOLVED","resultStatus":"","resultNote":"","progressNote":""}
                    """))
            .andExpect(status().isBadRequest());
    }
}
```

- [ ] **Step 2: Run the controller test to verify it fails**

Run:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH="$JAVA_HOME/bin:$PATH"
mvn -pl jeecg-boot-module/jeecg-module-content -Dtest=ContentUserSupportAdminControllerWebMvcTest -Dsurefire.failIfNoSpecifiedTests=false test
```

Expected: FAIL with missing controller and request type compilation errors.

- [ ] **Step 3: Commit the failing controller test**

```bash
git add /Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/.worktrees/content-user-domain-review/jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/controller/ContentUserSupportAdminControllerWebMvcTest.java
git commit -m "test: add appeal handle admin controller coverage"
```

### Task 3: Implement Request Model, Contract, Entity Fields, And Audit Factory

**Files:**
- Create: `/Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/.worktrees/content-user-domain-review/jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/req/support/ContentAppealHandleReq.java`
- Modify: `/Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/.worktrees/content-user-domain-review/jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/IContentUserSupportService.java`
- Modify: `/Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/.worktrees/content-user-domain-review/jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/entity/ContentUserAppeal.java`
- Modify: `/Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/.worktrees/content-user-domain-review/jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/entity/ContentUserAuditLog.java`

- [ ] **Step 1: Create the request model**

```java
@Data
@Accessors(chain = true)
@Schema(description = "内容社区申诉处理请求")
public class ContentAppealHandleReq {

    @NotBlank(message = "申诉ID不能为空")
    private String appealId;

    @NotBlank(message = "处理人ID不能为空")
    private String operatorUserId;

    @NotBlank(message = "处理后状态不能为空")
    private String status;

    @NotBlank(message = "处理结果状态不能为空")
    private String resultStatus;

    @NotBlank(message = "处理结果说明不能为空")
    @Size(max = 500, message = "处理结果说明长度不能超过500")
    private String resultNote;

    @NotBlank(message = "处理进度说明不能为空")
    @Size(max = 500, message = "处理进度说明长度不能超过500")
    private String progressNote;
}
```

- [ ] **Step 2: Extend the service contract and appeal entity**

```java
public interface IContentUserSupportService {

    String createAppeal(ContentAppealCreateReq req);

    ContentUserAppealProgressVO getAppealProgress(String userId, String appealId);

    List<ContentUserAppealProgressVO> listAppeals(String userId);

    String createReport(ContentReportCreateReq req);

    ContentHelpCenterVO getHelpCenter();

    ContentCustomerServiceVO getCustomerServiceEntry(String userId);

    String handleAppeal(ContentAppealHandleReq req);
}
```

```java
@Schema(description = "处理完成时间")
private Date resolvedAt;
```

- [ ] **Step 3: Add an audit-log factory for handled appeals**

```java
public static ContentUserAuditLog appealHandled(ContentUserAppeal appeal, ContentAppealHandleReq req) {
    return new ContentUserAuditLog()
        .setUserId(appeal.getUserId())
        .setOperatorUserId(req.getOperatorUserId())
        .setEventType("USER_APPEAL_HANDLED")
        .setEventContent(appeal.getAppealType() + ":" + req.getResultStatus())
        .setExtraDataJson("{\"appealId\":\"" + appeal.getId() + "\",\"resultStatus\":\""
            + req.getResultStatus() + "\",\"resultNote\":\"" + req.getResultNote() + "\"}")
        .setEventTime(new Date());
}
```

- [ ] **Step 4: Run compilation-oriented tests**

Run:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH="$JAVA_HOME/bin:$PATH"
mvn -pl jeecg-boot-module/jeecg-module-content -Dtest=ContentUserSupportServiceTest,ContentUserSupportAdminControllerWebMvcTest -Dsurefire.failIfNoSpecifiedTests=false test
```

Expected: FAIL because the controller and service implementation are still missing.

- [ ] **Step 5: Commit the contract and model work**

```bash
git add /Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/.worktrees/content-user-domain-review/jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/req/support/ContentAppealHandleReq.java
git add /Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/.worktrees/content-user-domain-review/jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/IContentUserSupportService.java
git add /Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/.worktrees/content-user-domain-review/jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/entity/ContentUserAppeal.java
git add /Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/.worktrees/content-user-domain-review/jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/entity/ContentUserAuditLog.java
git commit -m "feat: add appeal handle request contract"
```

### Task 4: Implement Admin Controller And Service Logic

**Files:**
- Create: `/Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/.worktrees/content-user-domain-review/jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/controller/ContentUserSupportAdminController.java`
- Modify: `/Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/.worktrees/content-user-domain-review/jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/impl/ContentUserSupportServiceImpl.java`

- [ ] **Step 1: Create the admin controller**

```java
@Tag(name = "内容社区用户支持管理")
@RestController
@RequestMapping("/content/user/support/admin")
public class ContentUserSupportAdminController {

    @Resource
    private IContentUserSupportService supportService;

    @Operation(summary = "处理申诉")
    @PostMapping("/appeal/handle")
    public Result<String> handleAppeal(@Valid @RequestBody ContentAppealHandleReq req) {
        return Result.OK(supportService.handleAppeal(req));
    }
}
```

- [ ] **Step 2: Implement the strict state machine in the service**

```java
@Override
@Transactional(rollbackFor = Exception.class)
public String handleAppeal(ContentAppealHandleReq req) {
    ContentUserAppeal appeal = appealMapper.selectById(req.getAppealId());
    if (appeal == null) {
        throw new JeecgBootException("申诉不存在");
    }
    if (!"RESOLVED".equals(req.getStatus())) {
        throw new JeecgBootException("申诉处理仅支持流转到RESOLVED");
    }
    if ("RESOLVED".equals(appeal.getStatus())) {
        throw new JeecgBootException("申诉已处理完成，请勿重复处理");
    }
    if (!"PENDING".equals(appeal.getStatus()) && !"PROCESSING".equals(appeal.getStatus())) {
        throw new JeecgBootException("当前申诉状态不允许处理");
    }

    appeal.setStatus(req.getStatus());
    appeal.setResultStatus(req.getResultStatus());
    appeal.setResultNote(req.getResultNote());
    appeal.setProgressNote(req.getProgressNote());
    appeal.setResolvedBy(req.getOperatorUserId());
    appeal.setResolvedAt(new Date());
    appealMapper.updateById(appeal);
    auditLogMapper.insert(ContentUserAuditLog.appealHandled(appeal, req));
    return appeal.getId();
}
```

- [ ] **Step 3: Run focused tests**

Run:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH="$JAVA_HOME/bin:$PATH"
mvn -pl jeecg-boot-module/jeecg-module-content -Dtest=ContentUserSupportServiceTest,ContentUserSupportAdminControllerWebMvcTest,ContentUserSupportControllerWebMvcTest -Dsurefire.failIfNoSpecifiedTests=false test
```

Expected: PASS for the new appeal-handle tests and no regression in the existing support controller tests.

- [ ] **Step 4: Commit the implementation**

```bash
git add /Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/.worktrees/content-user-domain-review/jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/controller/ContentUserSupportAdminController.java
git add /Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/.worktrees/content-user-domain-review/jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/impl/ContentUserSupportServiceImpl.java
git commit -m "feat: add appeal handle admin api"
```

### Task 5: Verify Diagnostics And Final Quality Gate

**Files:**
- Read: `/Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/.worktrees/content-user-domain-review/jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/controller/ContentUserSupportAdminController.java`
- Read: `/Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/.worktrees/content-user-domain-review/jeecg-boot/jeecg-boot-module/jeecg-module-content/src/main/java/org/jeecg/modules/content/user/service/impl/ContentUserSupportServiceImpl.java`
- Read: `/Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/.worktrees/content-user-domain-review/jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/service/ContentUserSupportServiceTest.java`
- Read: `/Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/.worktrees/content-user-domain-review/jeecg-boot/jeecg-boot-module/jeecg-module-content/src/test/java/org/jeecg/modules/content/user/controller/ContentUserSupportAdminControllerWebMvcTest.java`

- [ ] **Step 1: Check editor diagnostics**

Run:

```bash
# Use IDE diagnostics for the four touched files.
```

Expected: no new syntax or import errors in the edited files.

- [ ] **Step 2: Run the module standards check if needed**

Run:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH="$JAVA_HOME/bin:$PATH"
python3 /Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/.trae/skills/ai-coding-java-springboot/scripts/standards-check.py /Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/.worktrees/content-user-domain-review/jeecg-boot/jeecg-boot-module/jeecg-module-content
```

Expected: no new appeal-handle-specific rule regressions; any remaining findings should match existing module baseline warnings.

- [ ] **Step 3: Commit the quality-gate confirmation**

```bash
git add /Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/docs/superpowers/plans/2026-04-30-content-user-appeal-handle-plan.md
git commit -m "docs: add appeal handle implementation plan"
```
