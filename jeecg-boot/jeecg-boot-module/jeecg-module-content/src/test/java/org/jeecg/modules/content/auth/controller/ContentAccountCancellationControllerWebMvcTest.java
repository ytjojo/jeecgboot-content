package org.jeecg.modules.content.auth.controller;

import org.jeecg.common.api.vo.Result;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.auth.biz.IContentAccountCancellationBizService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 账号注销控制器 WebMvc 参数校验和响应测试。
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ContentAccountCancellationController WebMvc")
class ContentAccountCancellationControllerWebMvcTest {

    @RestControllerAdvice
    static class TestExceptionHandler {
        @ExceptionHandler(JeecgBootException.class)
        Result<?> handleJeecgBootException(JeecgBootException e) {
            return Result.error(e.getErrCode(), e.getMessage());
        }
    }

    private MockMvc mockMvc;

    @Mock
    private IContentAccountCancellationBizService cancellationBizService;

    @InjectMocks
    private ContentAccountCancellationController controller;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setValidator(validator)
                .setControllerAdvice(new TestExceptionHandler())
                .build();
    }

    @Nested
    @DisplayName("apply")
    class Apply {

        @Test
        @DisplayName("valid request - returns success")
        void validRequest_returnsSuccess() throws Exception {
            mockMvc.perform(post("/content/auth/cancellation/apply")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"userId":"u_001","reason":"不想用了","cooldownDays":14}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));

            verify(cancellationBizService).applyCancellation(any());
        }

        @Test
        @DisplayName("blank userId - returns 400")
        void blankUserId_returns400() throws Exception {
            mockMvc.perform(post("/content/auth/cancellation/apply")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"userId":"","cooldownDays":14}
                                    """))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("cooldownDays below 7 - returns 400")
        void cooldownDaysBelow7_returns400() throws Exception {
            mockMvc.perform(post("/content/auth/cancellation/apply")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"userId":"u_001","cooldownDays":3}
                                    """))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("cooldownDays above 30 - returns 400")
        void cooldownDaysAbove30_returns400() throws Exception {
            mockMvc.perform(post("/content/auth/cancellation/apply")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"userId":"u_001","cooldownDays":60}
                                    """))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("already in cooldown - returns business error")
        void alreadyInCooldown_returnsBusinessError() throws Exception {
            org.mockito.Mockito.doThrow(new JeecgBootException("已存在注销申请"))
                    .when(cancellationBizService).applyCancellation(any());

            mockMvc.perform(post("/content/auth/cancellation/apply")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"userId":"u_001","cooldownDays":14}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("已存在注销申请"));
        }
    }

    @Nested
    @DisplayName("status")
    class Status {

        @Test
        @DisplayName("valid userId - returns status")
        void validUserId_returnsStatus() throws Exception {
            when(cancellationBizService.checkCooldownStatus("u_001")).thenReturn("PENDING");

            mockMvc.perform(get("/content/auth/cancellation/status")
                            .param("userId", "u_001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.result").value("PENDING"));
        }

        @Test
        @DisplayName("missing userId - returns 400")
        void missingUserId_returns400() throws Exception {
            mockMvc.perform(get("/content/auth/cancellation/status"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("revoke")
    class Revoke {

        @Test
        @DisplayName("valid userId - returns success")
        void validUserId_returnsSuccess() throws Exception {
            mockMvc.perform(post("/content/auth/cancellation/revoke")
                            .param("userId", "u_001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));

            verify(cancellationBizService).revokeCancellation("u_001");
        }

        @Test
        @DisplayName("missing userId - returns 400")
        void missingUserId_returns400() throws Exception {
            mockMvc.perform(post("/content/auth/cancellation/revoke"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("no pending cancellation - returns business error")
        void noPending_returnsBusinessError() throws Exception {
            org.mockito.Mockito.doThrow(new JeecgBootException("无待取消的注销申请"))
                    .when(cancellationBizService).revokeCancellation(eq("u_001"));

            mockMvc.perform(post("/content/auth/cancellation/revoke")
                            .param("userId", "u_001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("无待取消的注销申请"));
        }
    }
}
