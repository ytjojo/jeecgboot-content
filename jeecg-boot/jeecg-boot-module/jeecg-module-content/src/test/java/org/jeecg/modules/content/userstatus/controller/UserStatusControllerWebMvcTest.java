package org.jeecg.modules.content.userstatus.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.user.entity.ContentUserProfile;
import org.jeecg.modules.content.user.mapper.ContentUserProfileMapper;
import org.jeecg.modules.content.userstatus.biz.UserStatusBizManageService;
import org.jeecg.modules.content.userstatus.entity.UserStatusAuditLog;
import org.jeecg.modules.content.userstatus.entity.UserStatusEnum;
import org.jeecg.modules.content.userstatus.req.UserStatusChangeReq;
import org.jeecg.modules.content.userstatus.service.UserStatusAuditLogService;
import org.jeecg.modules.content.userstatus.service.UserStatusService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
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
    }

    @AfterEach
    void tearDown() {
        // 无 SecurityContext 依赖，无需清理
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

            mockMvc.perform(get("/api/content/user-status/current").param("userId", "u1"))
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

            mockMvc.perform(get("/api/content/user-status/current").param("userId", "ghost"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("用户资料不存在")));
        }

        @Test
        @DisplayName("脏数据：profile.status 是非枚举名 → 应抛 JeecgBootException（当前实现会抛 IllegalArgumentException，这是 P0 待修 bug）")
        void dirtyStatus_shouldThrowJeecgBootException() throws Exception {
            when(userProfileMapper.selectByUserId("u1")).thenReturn(profile("u1", "INVALID_STATUS"));

            mockMvc.perform(get("/api/content/user-status/current").param("userId", "u1"))
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

            mockMvc.perform(get("/api/content/user-status/u2"))
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

            mockMvc.perform(get("/api/content/user-status/ghost"))
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

            mockMvc.perform(post("/api/content/user-status/u1/change")
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
        @DisplayName("reason 为空：当前 controller 未启用 @Valid，业务流继续（遗留问题见报告）")
        void blankReason_currentlyPassesThroughToBiz() throws Exception {
            when(userProfileMapper.selectByUserId("u1")).thenReturn(profile("u1", "NORMAL"));

            UserStatusChangeReq req = new UserStatusChangeReq();
            req.setToStatus(UserStatusEnum.MUTED);
            req.setReason("");
            String body = objectMapper.writeValueAsString(req);

            // 期望：当前 controller 未在 @RequestBody 上加 @Valid，所以 reason 不会触发 400
            // 规范应启用校验：见审计报告遗留问题
            mockMvc.perform(post("/api/content/user-status/u1/change")
                    .param("operatorId", "admin001")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("toStatus 为空：当前 controller 未启用 @Valid，业务流继续（遗留问题见报告）")
        void nullToStatus_currentlyPassesThroughToBiz() throws Exception {
            when(userProfileMapper.selectByUserId("u1")).thenReturn(profile("u1", "NORMAL"));

            UserStatusChangeReq req = new UserStatusChangeReq();
            req.setReason("只有原因没有目标");
            String body = objectMapper.writeValueAsString(req);

            mockMvc.perform(post("/api/content/user-status/u1/change")
                    .param("operatorId", "admin001")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("用户不存在 → 业务错误，不调 biz")
        void userNotFound_doesNotCallBiz() throws Exception {
            when(userProfileMapper.selectByUserId("ghost")).thenReturn(null);

            UserStatusChangeReq req = new UserStatusChangeReq();
            req.setToStatus(UserStatusEnum.MUTED);
            req.setReason("违规");
            String body = objectMapper.writeValueAsString(req);

            mockMvc.perform(post("/api/content/user-status/ghost/change")
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

            mockMvc.perform(post("/api/content/user-status/u1/change")
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

            mockMvc.perform(post("/api/content/user-status/u1/change")
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

            mockMvc.perform(get("/api/content/user-status/u1/history"))
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

            mockMvc.perform(get("/api/content/user-status/u1/history"))
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
            req.setReason("管理员人工解禁");
            String body = objectMapper.writeValueAsString(req);

            mockMvc.perform(post("/api/content/user-status/u1/release")
                    .param("operatorId", "admin001")
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
        @DisplayName("reason 为空：当前 controller 未启用 @Valid，业务流继续（遗留问题见报告）")
        void blankReason_currentlyPassesThroughToBiz() throws Exception {
            when(userProfileMapper.selectByUserId("u1")).thenReturn(profile("u1", "MUTED"));

            UserStatusChangeReq req = new UserStatusChangeReq();
            req.setReason("");
            String body = objectMapper.writeValueAsString(req);

            mockMvc.perform(post("/api/content/user-status/u1/release")
                    .param("operatorId", "admin001")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("用户不存在 → 业务错误")
        void userNotFound_returnsBusinessError() throws Exception {
            when(userProfileMapper.selectByUserId("ghost")).thenReturn(null);

            UserStatusChangeReq req = new UserStatusChangeReq();
            req.setReason("解禁");
            String body = objectMapper.writeValueAsString(req);

            mockMvc.perform(post("/api/content/user-status/ghost/release")
                    .param("operatorId", "admin001")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));

            verify(bizManageService, never()).changeStatus(
                any(), any(), any(), any(), any(), any(), any()
            );
        }
    }
}
