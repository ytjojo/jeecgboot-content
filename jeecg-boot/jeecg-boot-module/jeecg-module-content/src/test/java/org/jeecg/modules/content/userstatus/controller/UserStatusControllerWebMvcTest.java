package org.jeecg.modules.content.userstatus.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.exception.JeecgBootException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.jeecg.modules.content.auth.enums.VerificationCodeSceneEnum;
import org.jeecg.modules.content.auth.service.IContentVerificationCodeService;
import org.jeecg.modules.content.user.entity.ContentUserProfile;
import org.jeecg.modules.content.user.mapper.ContentUserProfileMapper;
import org.jeecg.modules.content.userstatus.biz.UserStatusBizManageService;
import org.jeecg.modules.content.userstatus.entity.UserStatusAuditLog;
import org.jeecg.modules.content.userstatus.entity.UserStatusEnum;
import org.jeecg.modules.content.userstatus.mapper.UserStatusAuditLogMapper;
import org.jeecg.modules.content.userstatus.req.SendVerifyCodeReq;
import org.jeecg.modules.content.userstatus.req.UserStatusChangeReq;
import org.jeecg.modules.content.userstatus.req.VerifySecurityReq;
import org.jeecg.modules.content.userstatus.service.UserStatusAuditLogService;
import org.jeecg.modules.content.userstatus.service.UserStatusService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * UserStatusController WebMvc 测试。
 * 覆盖 5 个端点 + 异常路径（用户不存在、脏数据、参数校验、权限异常）。
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserStatusController WebMvc")
class UserStatusControllerWebMvcTest {

    @RestControllerAdvice
    static class TestExceptionHandler {
        @ExceptionHandler(JeecgBootException.class)
        Result<?> handleJeecgBootException(JeecgBootException e) {
            return Result.error(e.getErrCode(), e.getMessage());
        }
    }

    private final ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc mockMvc;

    @Mock
    private UserStatusService userStatusService;

    @Mock
    private UserStatusBizManageService bizManageService;

    @Mock
    private UserStatusAuditLogService auditLogService;

    @Mock
    private ContentUserProfileMapper userProfileMapper;

    @Mock
    private UserStatusAuditLogMapper auditLogMapper;

    @Mock
    private IContentVerificationCodeService verificationCodeService;

    @InjectMocks
    private UserStatusController controller;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setValidator(validator)
            .setControllerAdvice(new TestExceptionHandler())
            .build();

        // Mock SecurityContext，使 SecureUtil.currentUser() 返回 id="admin001" 的 LoginUser
        // 使用 lenient() 因为部分测试（如 400 校验拦截）不会走到 SecureUtil
        SecurityContext securityContext = org.mockito.Mockito.mock(SecurityContext.class);
        Authentication authentication = org.mockito.Mockito.mock(Authentication.class);
        org.mockito.Mockito.lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        org.mockito.Mockito.lenient().when(authentication.getName()).thenReturn("{\"id\":\"admin001\"}");
        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private ContentUserProfile profile(String userId, String statusName) {
        ContentUserProfile p = new ContentUserProfile();
        p.setUserId(userId);
        p.setStatus(statusName);
        return p;
    }

    @Nested
    @DisplayName("GET /api/content/user-status/current")
    class GetCurrent {

        @Test
        @DisplayName("有效用户+合法状态 → 返回 UserStatusVO")
        void valid_returnsVO() throws Exception {
            when(userProfileMapper.selectByUserId("u1")).thenReturn(profile("u1", "NORMAL"));

            mockMvc.perform(get("/api/v1/content/user-status/current").param("userId", "u1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.result.userId").value("u1"))
                .andExpect(jsonPath("$.result.status").value("NORMAL"))
                .andExpect(jsonPath("$.result.statusDisplayName").value("正常"));
        }

        @Test
        @DisplayName("用户资料不存在 → 业务错误")
        void userNotFound_returnsBusinessError() throws Exception {
            when(userProfileMapper.selectByUserId("ghost")).thenReturn(null);

            mockMvc.perform(get("/api/v1/content/user-status/current").param("userId", "ghost"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("用户资料不存在")));
        }

        @Test
        @DisplayName("脏数据：profile.status 是非枚举名 → 应抛 JeecgBootException（当前实现会抛 IllegalArgumentException，这是 P0 待修 bug）")
        void dirtyStatus_shouldThrowJeecgBootException() throws Exception {
            when(userProfileMapper.selectByUserId("u1")).thenReturn(profile("u1", "INVALID_STATUS"));

            mockMvc.perform(get("/api/v1/content/user-status/current").param("userId", "u1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("用户状态值不合法")));
        }
    }

    @Nested
    @DisplayName("GET /api/content/user-status/{userId}")
    class GetById {

        @Test
        @DisplayName("管理员查询指定用户 → 返回 UserStatusVO")
        void adminQuery_returnsVO() throws Exception {
            when(userProfileMapper.selectByUserId("u2")).thenReturn(profile("u2", "MUTED"));

            mockMvc.perform(get("/api/v1/content/user-status/u2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.result.userId").value("u2"))
                .andExpect(jsonPath("$.result.status").value("MUTED"))
                .andExpect(jsonPath("$.result.statusDisplayName").value("禁言"));
        }

        @Test
        @DisplayName("用户不存在 → 业务错误")
        void userNotFound_returnsBusinessError() throws Exception {
            when(userProfileMapper.selectByUserId("ghost")).thenReturn(null);

            mockMvc.perform(get("/api/v1/content/user-status/ghost"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("POST /api/content/user-status/{userId}/change")
    class ChangeStatus {

        @Test
        @DisplayName("合法请求 → 调用 biz.changeStatus 并返回成功")
        void validRequest_callsBiz() throws Exception {
            when(userProfileMapper.selectByUserId("u1")).thenReturn(profile("u1", "NORMAL"));

            UserStatusChangeReq req = new UserStatusChangeReq();
            req.setToStatus(UserStatusEnum.MUTED);
            req.setReason("违规发言");
            req.setEndTime(new Date());
            req.setRemark("首次警告");
            String body = objectMapper.writeValueAsString(req);

            mockMvc.perform(post("/api/v1/content/user-status/u1/change")
                    .param("operatorId", "admin001")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

            verify(bizManageService).changeStatus(
                eq("u1"),
                eq(UserStatusEnum.NORMAL),
                eq(UserStatusEnum.MUTED),
                eq("违规发言"),
                eq("admin001"),
                eq("ADMIN"),
                eq(null)
            );
        }

        @Test
        @DisplayName("reason 为空：@Valid 校验拦截，返回 400")
        void blankReason_returnsValidationError() throws Exception {
            UserStatusChangeReq req = new UserStatusChangeReq();
            req.setToStatus(UserStatusEnum.MUTED);
            req.setReason("");
            String body = objectMapper.writeValueAsString(req);

            // @Valid + @NotBlank 校验：reason 为空直接返回 400
            mockMvc.perform(post("/api/v1/content/user-status/u1/change")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("toStatus 为空：@Valid 校验拦截，返回 400")
        void nullToStatus_returnsValidationError() throws Exception {
            UserStatusChangeReq req = new UserStatusChangeReq();
            req.setReason("只有原因没有目标");
            String body = objectMapper.writeValueAsString(req);

            // @Valid + @NotNull 校验：toStatus 为空直接返回 400
            mockMvc.perform(post("/api/v1/content/user-status/u1/change")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("用户不存在 → 业务错误，不调 biz")
        void userNotFound_doesNotCallBiz() throws Exception {
            when(userProfileMapper.selectByUserId("ghost")).thenReturn(null);

            UserStatusChangeReq req = new UserStatusChangeReq();
            req.setToStatus(UserStatusEnum.MUTED);
            req.setReason("违规");
            String body = objectMapper.writeValueAsString(req);

            mockMvc.perform(post("/api/v1/content/user-status/ghost/change")
                    .param("operatorId", "admin001")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));

            verify(bizManageService, never()).changeStatus(
                any(), any(), any(), any(), any(), any(), any()
            );
        }

        @Test
        @DisplayName("脏数据：profile.status 非法 → 应抛 JeecgBootException（当前实现会抛 IllegalArgumentException）")
        void dirtyStatus_shouldThrowJeecgBootException() throws Exception {
            when(userProfileMapper.selectByUserId("u1")).thenReturn(profile("u1", "GARBAGE"));

            UserStatusChangeReq req = new UserStatusChangeReq();
            req.setToStatus(UserStatusEnum.MUTED);
            req.setReason("违规");
            String body = objectMapper.writeValueAsString(req);

            mockMvc.perform(post("/api/v1/content/user-status/u1/change")
                    .param("operatorId", "admin001")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("用户状态值不合法")));

            verify(bizManageService, never()).changeStatus(
                any(), any(), any(), any(), any(), any(), any()
            );
        }

        @Test
        @DisplayName("biz 抛出 JeecgBootException → 透传给客户端")
        void bizThrows_propagates() throws Exception {
            when(userProfileMapper.selectByUserId("u1")).thenReturn(profile("u1", "NORMAL"));
            doThrow(new JeecgBootException("非法的状态转换"))
                .when(bizManageService).changeStatus(
                    eq("u1"), eq(UserStatusEnum.NORMAL), eq(UserStatusEnum.MUTED),
                    eq("违规"), eq("admin001"), eq("ADMIN"), eq(null));

            UserStatusChangeReq req = new UserStatusChangeReq();
            req.setToStatus(UserStatusEnum.MUTED);
            req.setReason("违规");
            String body = objectMapper.writeValueAsString(req);

            mockMvc.perform(post("/api/v1/content/user-status/u1/change")
                    .param("operatorId", "admin001")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("非法的状态转换"));
        }
    }

    @Nested
    @DisplayName("GET /api/content/user-status/{userId}/history")
    class GetHistory {

        @Test
        @DisplayName("返回审计日志列表")
        void returnsHistoryList() throws Exception {
            UserStatusAuditLog log = new UserStatusAuditLog()
                .setLogId("log-1")
                .setUserId("u1")
                .setFromStatus("NORMAL")
                .setToStatus("MUTED")
                .setOperatorId("admin001")
                .setOperatorType("ADMIN")
                .setTriggerReason("违规")
                .setCreatedAt(new Date());
            when(auditLogService.queryByUserId("u1")).thenReturn(List.of(log));

            mockMvc.perform(get("/api/v1/content/user-status/u1/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.result[0].logId").value("log-1"))
                .andExpect(jsonPath("$.result[0].fromStatus").value("NORMAL"))
                .andExpect(jsonPath("$.result[0].toStatus").value("MUTED"));

            verify(auditLogService).queryByUserId("u1");
        }

        @Test
        @DisplayName("无历史记录 → 返回空数组")
        void noHistory_returnsEmptyList() throws Exception {
            when(auditLogService.queryByUserId("u1")).thenReturn(List.of());

            mockMvc.perform(get("/api/v1/content/user-status/u1/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.result").isArray())
                .andExpect(jsonPath("$.result").isEmpty());
        }
    }

    @Nested
    @DisplayName("POST /api/content/user-status/{userId}/release")
    class ReleaseStatus {

        @Test
        @DisplayName("合法请求 → biz.changeStatus 强制转为 NORMAL")
        void validRequest_callsBizWithNormal() throws Exception {
            when(userProfileMapper.selectByUserId("u1")).thenReturn(profile("u1", "MUTED"));

            UserStatusChangeReq req = new UserStatusChangeReq();
            req.setToStatus(UserStatusEnum.NORMAL); // @Valid 要求非空，release 端点会忽略此值
            req.setReason("管理员人工解禁");
            String body = objectMapper.writeValueAsString(req);

            mockMvc.perform(post("/api/v1/content/user-status/u1/release")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

            verify(bizManageService).changeStatus(
                eq("u1"),
                eq(UserStatusEnum.MUTED),
                eq(UserStatusEnum.NORMAL),
                eq("管理员人工解禁"),
                eq("admin001"),
                eq("ADMIN"),
                eq(null)
            );
        }

        @Test
        @DisplayName("reason 为空：@Valid 校验拦截，返回 400")
        void blankReason_returnsValidationError() throws Exception {
            UserStatusChangeReq req = new UserStatusChangeReq();
            req.setToStatus(UserStatusEnum.NORMAL);
            req.setReason("");
            String body = objectMapper.writeValueAsString(req);

            // @Valid + @NotBlank 校验：reason 为空直接返回 400
            mockMvc.perform(post("/api/v1/content/user-status/u1/release")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("用户不存在 → 业务错误")
        void userNotFound_returnsBusinessError() throws Exception {
            when(userProfileMapper.selectByUserId("ghost")).thenReturn(null);

            UserStatusChangeReq req = new UserStatusChangeReq();
            req.setToStatus(UserStatusEnum.NORMAL); // @Valid 要求非空
            req.setReason("解禁");
            String body = objectMapper.writeValueAsString(req);

            mockMvc.perform(post("/api/v1/content/user-status/ghost/release")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));

            verify(bizManageService, never()).changeStatus(
                any(), any(), any(), any(), any(), any(), any()
            );
        }
    }

    // ==================== P0: getTransitions ====================

    @Nested
    @DisplayName("GET /api/content/user-status/transitions/{currentStatus}")
    class GetTransitions {

        @Test
        @DisplayName("合法状态 → 返回允许转换的状态集合")
        void validStatus_returnsAllowedTransitions() throws Exception {
            mockMvc.perform(get("/api/v1/content/user-status/transitions/NORMAL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.result").isArray())
                .andExpect(jsonPath("$.result", hasSize(5)));
        }

        @Test
        @DisplayName("FROZEN 状态 → 返回 NORMAL 和 BANNED")
        void frozenStatus_returnsNormalAndBanned() throws Exception {
            mockMvc.perform(get("/api/v1/content/user-status/transitions/FROZEN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.result").isArray())
                .andExpect(jsonPath("$.result", hasSize(2)));
        }

        @Test
        @DisplayName("DEACTIVATED 状态 → 返回空集合（终态）")
        void deactivatedStatus_returnsEmptySet() throws Exception {
            mockMvc.perform(get("/api/v1/content/user-status/transitions/DEACTIVATED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.result").isArray())
                .andExpect(jsonPath("$.result", hasSize(0)));
        }

        @Test
        @DisplayName("非法状态名 → 业务错误")
        void invalidStatus_returnsBusinessError() throws Exception {
            mockMvc.perform(get("/api/v1/content/user-status/transitions/INVALID"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("用户状态值不合法")));
        }
    }

    // ==================== P0: getStatusList ====================

    @Nested
    @DisplayName("GET /api/content/user-status/list")
    class GetStatusList {

        @Test
        @DisplayName("无筛选条件 → 返回分页数据")
        void noFilters_returnsPaginatedData() throws Exception {
            when(userProfileMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenAnswer(invocation -> {
                    Page<ContentUserProfile> page = invocation.getArgument(0);
                    page.setTotal(0);
                    page.setRecords(List.of());
                    return page;
                });

            mockMvc.perform(get("/api/v1/content/user-status/list")
                    .param("page", "1")
                    .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("按状态筛选 → wrapper 包含状态条件")
        void withStatusFilter_queriesWithStatus() throws Exception {
            when(userProfileMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenAnswer(invocation -> {
                    Page<ContentUserProfile> page = invocation.getArgument(0);
                    page.setTotal(0);
                    page.setRecords(List.of());
                    return page;
                });

            mockMvc.perform(get("/api/v1/content/user-status/list")
                    .param("status", "MUTED")
                    .param("page", "1")
                    .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

            verify(userProfileMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
        }

        @Test
        @DisplayName("按 userId 筛选 → wrapper 包含 userId 条件")
        void withUserIdFilter_queriesWithUserId() throws Exception {
            when(userProfileMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenAnswer(invocation -> {
                    Page<ContentUserProfile> page = invocation.getArgument(0);
                    page.setTotal(1);
                    ContentUserProfile p = new ContentUserProfile();
                    p.setUserId("u1");
                    p.setStatus("NORMAL");
                    page.setRecords(List.of(p));
                    return page;
                });

            mockMvc.perform(get("/api/v1/content/user-status/list")
                    .param("userId", "u1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
        }
    }

    // ==================== P0: sendVerifyCode ====================

    @Nested
    @DisplayName("POST /api/content/user-status/send-verify-code")
    class SendVerifyCode {

        @Test
        @DisplayName("正常发送 → 返回成功")
        void normalSend_returnsSuccess() throws Exception {
            when(verificationCodeService.isInCooldown(VerificationCodeSceneEnum.SECURITY_VERIFY, "13800138000"))
                .thenReturn(false);

            SendVerifyCodeReq req = new SendVerifyCodeReq();
            req.setPhone("13800138000");
            String body = objectMapper.writeValueAsString(req);

            mockMvc.perform(post("/api/v1/content/user-status/send-verify-code")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

            verify(verificationCodeService).generateCode(VerificationCodeSceneEnum.SECURITY_VERIFY, "13800138000");
        }

        @Test
        @DisplayName("冷却期内 → 业务错误")
        void inCooldown_returnsBusinessError() throws Exception {
            when(verificationCodeService.isInCooldown(VerificationCodeSceneEnum.SECURITY_VERIFY, "13800138000"))
                .thenReturn(true);

            SendVerifyCodeReq req = new SendVerifyCodeReq();
            req.setPhone("13800138000");
            String body = objectMapper.writeValueAsString(req);

            mockMvc.perform(post("/api/v1/content/user-status/send-verify-code")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("验证码发送过于频繁")));

            verify(verificationCodeService, never()).generateCode(any(), any());
        }

        @Test
        @DisplayName("手机号为空 → 参数校验失败，返回 400")
        void blankPhone_returnsValidationError() throws Exception {
            SendVerifyCodeReq req = new SendVerifyCodeReq();
            req.setPhone("");
            String body = objectMapper.writeValueAsString(req);

            // @Valid + @NotBlank 校验：phone 为空直接返回 400
            mockMvc.perform(post("/api/v1/content/user-status/send-verify-code")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
                .andExpect(status().isBadRequest());
        }
    }

    // ==================== P0: verifySecurity ====================

    @Nested
    @DisplayName("POST /api/content/user-status/verify-security")
    class VerifySecurity {

        @Test
        @DisplayName("验证码正确 → 返回成功")
        void validCode_returnsSuccess() throws Exception {
            when(verificationCodeService.verifyCode(
                VerificationCodeSceneEnum.SECURITY_VERIFY, "13800138000", "123456"))
                .thenReturn(true);
            when(userProfileMapper.selectByUserId("u1")).thenReturn(profile("u1", "FROZEN"));

            VerifySecurityReq req = new VerifySecurityReq();
            req.setUserId("u1");
            req.setPhone("13800138000");
            req.setVerifyCode("123456");
            String body = objectMapper.writeValueAsString(req);

            mockMvc.perform(post("/api/v1/content/user-status/verify-security")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

            // 验证安全核验通过后自动恢复为 NORMAL
            verify(bizManageService).changeStatus(
                eq("u1"),
                eq(UserStatusEnum.FROZEN),
                eq(UserStatusEnum.NORMAL),
                eq("安全核验通过，自动恢复"),
                eq("SYSTEM"),
                eq("SYSTEM"),
                eq(null)
            );
        }

        @Test
        @DisplayName("验证码错误 → 业务错误")
        void invalidCode_returnsBusinessError() throws Exception {
            when(verificationCodeService.verifyCode(
                VerificationCodeSceneEnum.SECURITY_VERIFY, "13800138000", "999999"))
                .thenReturn(false);

            VerifySecurityReq req = new VerifySecurityReq();
            req.setPhone("13800138000");
            req.setVerifyCode("999999");
            String body = objectMapper.writeValueAsString(req);

            mockMvc.perform(post("/api/v1/content/user-status/verify-security")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("验证码错误或已过期")));
        }

        @Test
        @DisplayName("验证码过期 → 业务错误")
        void expiredCode_returnsBusinessError() throws Exception {
            when(verificationCodeService.verifyCode(
                VerificationCodeSceneEnum.SECURITY_VERIFY, "13800138000", "123456"))
                .thenReturn(false);

            VerifySecurityReq req = new VerifySecurityReq();
            req.setPhone("13800138000");
            req.setVerifyCode("123456");
            String body = objectMapper.writeValueAsString(req);

            mockMvc.perform(post("/api/v1/content/user-status/verify-security")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));
        }
    }

    // ==================== P1: getAuditLogList ====================

    @Nested
    @DisplayName("GET /api/content/user-status/audit-logs")
    class GetAuditLogList {

        @Test
        @DisplayName("无筛选条件 → 返回分页数据")
        void noFilters_returnsPaginatedData() throws Exception {
            when(auditLogMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenAnswer(invocation -> {
                    Page<UserStatusAuditLog> page = invocation.getArgument(0);
                    page.setTotal(0);
                    page.setRecords(List.of());
                    return page;
                });

            mockMvc.perform(get("/api/v1/content/user-status/audit-logs")
                    .param("page", "1")
                    .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("按 userId 和 operatorType 筛选 → 调用 mapper 查询")
        void withFilters_queriesWithFilters() throws Exception {
            when(auditLogMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenAnswer(invocation -> {
                    Page<UserStatusAuditLog> page = invocation.getArgument(0);
                    UserStatusAuditLog log = new UserStatusAuditLog()
                        .setLogId("log-1")
                        .setUserId("u1")
                        .setOperatorType("ADMIN");
                    page.setTotal(1);
                    page.setRecords(List.of(log));
                    return page;
                });

            mockMvc.perform(get("/api/v1/content/user-status/audit-logs")
                    .param("userId", "u1")
                    .param("operatorType", "ADMIN")
                    .param("page", "1")
                    .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

            verify(auditLogMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
        }
    }

    // ==================== P1: getAuditLogDetail ====================

    @Nested
    @DisplayName("GET /api/content/user-status/audit-logs/{logId}")
    class GetAuditLogDetail {

        @Test
        @DisplayName("日志存在 → 返回审计日志详情")
        void logExists_returnsDetail() throws Exception {
            UserStatusAuditLog log = new UserStatusAuditLog()
                .setLogId("log-1")
                .setUserId("u1")
                .setFromStatus("NORMAL")
                .setToStatus("MUTED")
                .setOperatorId("admin001")
                .setOperatorType("ADMIN")
                .setTriggerReason("违规")
                .setCreatedAt(new Date());
            when(auditLogMapper.selectById("log-1")).thenReturn(log);

            mockMvc.perform(get("/api/v1/content/user-status/audit-logs/log-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.result.logId").value("log-1"))
                .andExpect(jsonPath("$.result.userId").value("u1"))
                .andExpect(jsonPath("$.result.fromStatus").value("NORMAL"))
                .andExpect(jsonPath("$.result.toStatus").value("MUTED"));
        }

        @Test
        @DisplayName("日志不存在 → 业务错误")
        void logNotFound_returnsBusinessError() throws Exception {
            when(auditLogMapper.selectById("ghost")).thenReturn(null);

            mockMvc.perform(get("/api/v1/content/user-status/audit-logs/ghost"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("审计日志不存在")));
        }
    }

    // ==================== P1: batchReleaseUsers ====================

    @Nested
    @DisplayName("POST /api/content/user-status/batch-release")
    class BatchReleaseUsers {

        @Test
        @DisplayName("正常批量解禁 → 返回成功")
        void validBatch_returnsSuccess() throws Exception {
            when(userProfileMapper.selectByUserId("u1")).thenReturn(profile("u1", "MUTED"));
            when(userProfileMapper.selectByUserId("u2")).thenReturn(profile("u2", "FROZEN"));

            mockMvc.perform(post("/api/v1/content/user-status/batch-release")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(Arrays.asList("u1", "u2")))
                    .param("reason", "批量解禁")
                    .param("operatorId", "admin001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

            verify(bizManageService).changeStatus(
                eq("u1"), eq(UserStatusEnum.MUTED), eq(UserStatusEnum.NORMAL),
                eq("批量解禁"), eq("admin001"), eq("ADMIN"), eq(null));
            verify(bizManageService).changeStatus(
                eq("u2"), eq(UserStatusEnum.FROZEN), eq(UserStatusEnum.NORMAL),
                eq("批量解禁"), eq("admin001"), eq("ADMIN"), eq(null));
        }

        @Test
        @DisplayName("部分用户不存在 → 仍返回成功（跳过不存在的用户）")
        void partialNotFound_stillReturnsSuccess() throws Exception {
            when(userProfileMapper.selectByUserId("u1")).thenReturn(profile("u1", "MUTED"));
            when(userProfileMapper.selectByUserId("ghost")).thenReturn(null);

            mockMvc.perform(post("/api/v1/content/user-status/batch-release")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(Arrays.asList("u1", "ghost")))
                    .param("reason", "批量解禁")
                    .param("operatorId", "admin001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

            verify(bizManageService, times(1)).changeStatus(
                eq("u1"), any(), any(), any(), any(), any(), any());
        }

        @Test
        @DisplayName("空列表 → 业务错误")
        void emptyList_returnsBusinessError() throws Exception {
            mockMvc.perform(post("/api/v1/content/user-status/batch-release")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("[]")
                    .param("reason", "批量解禁")
                    .param("operatorId", "admin001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("用户ID列表不能为空")));
        }

        @Test
        @DisplayName("biz 抛异常 → 跳过该用户继续处理其他用户")
        void bizThrows_skipsAndContinues() throws Exception {
            when(userProfileMapper.selectByUserId("u1")).thenReturn(profile("u1", "MUTED"));
            when(userProfileMapper.selectByUserId("u2")).thenReturn(profile("u2", "FROZEN"));
            doThrow(new JeecgBootException("非法转换"))
                .when(bizManageService).changeStatus(
                    eq("u1"), any(), any(), any(), any(), any(), any());

            mockMvc.perform(post("/api/v1/content/user-status/batch-release")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(Arrays.asList("u1", "u2")))
                    .param("reason", "批量解禁")
                    .param("operatorId", "admin001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

            verify(bizManageService).changeStatus(
                eq("u2"), eq(UserStatusEnum.FROZEN), eq(UserStatusEnum.NORMAL),
                eq("批量解禁"), eq("admin001"), eq("ADMIN"), eq(null));
        }
    }

    // ==================== P1: exportAuditLogs ====================

    @Nested
    @DisplayName("GET /api/content/user-status/audit-logs/export")
    class ExportAuditLogs {

        @Test
        @DisplayName("无筛选条件 → 返回日志列表")
        void noFilters_returnsLogList() throws Exception {
            when(auditLogMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());

            mockMvc.perform(get("/api/v1/content/user-status/audit-logs/export"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.result").isArray());
        }

        @Test
        @DisplayName("按 userId 筛选 → 返回筛选后的日志")
        void withUserIdFilter_returnsFilteredLogs() throws Exception {
            UserStatusAuditLog log = new UserStatusAuditLog()
                .setLogId("log-1")
                .setUserId("u1")
                .setFromStatus("NORMAL")
                .setToStatus("MUTED");
            when(auditLogMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(log));

            mockMvc.perform(get("/api/v1/content/user-status/audit-logs/export")
                    .param("userId", "u1")
                    .param("format", "csv"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.result", hasSize(1)))
                .andExpect(jsonPath("$.result[0].logId").value("log-1"));

            verify(auditLogMapper).selectList(any(LambdaQueryWrapper.class));
        }
    }

    // ==================== P1: getUserAuditLogs ====================

    @Nested
    @DisplayName("GET /api/content/user-status/users/{userId}/audit-logs")
    class GetUserAuditLogs {

        @Test
        @DisplayName("正常查询 → 返回分页审计日志")
        void validUser_returnsPaginatedLogs() throws Exception {
            when(auditLogMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenAnswer(invocation -> {
                    Page<UserStatusAuditLog> page = invocation.getArgument(0);
                    UserStatusAuditLog log = new UserStatusAuditLog()
                        .setLogId("log-1")
                        .setUserId("u1")
                        .setFromStatus("NORMAL")
                        .setToStatus("MUTED");
                    page.setTotal(1);
                    page.setRecords(List.of(log));
                    return page;
                });

            mockMvc.perform(get("/api/v1/content/user-status/users/u1/audit-logs")
                    .param("page", "1")
                    .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

            verify(auditLogMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
        }

        @Test
        @DisplayName("无历史记录 → 返回空分页")
        void noHistory_returnsEmptyPage() throws Exception {
            when(auditLogMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenAnswer(invocation -> {
                    Page<UserStatusAuditLog> page = invocation.getArgument(0);
                    page.setTotal(0);
                    page.setRecords(List.of());
                    return page;
                });

            mockMvc.perform(get("/api/v1/content/user-status/users/u1/audit-logs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
        }
    }
}
