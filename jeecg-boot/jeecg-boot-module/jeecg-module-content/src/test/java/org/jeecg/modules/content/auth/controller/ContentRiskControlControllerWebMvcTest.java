package org.jeecg.modules.content.auth.controller;

import org.jeecg.common.api.vo.Result;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.auth.biz.IContentRiskControlBizService;
import org.jeecg.modules.content.auth.entity.ContentRiskEvent;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 风控控制器 WebMvc 参数校验和响应测试。
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ContentRiskControlController WebMvc")
class ContentRiskControlControllerWebMvcTest {

    @RestControllerAdvice
    static class TestExceptionHandler {
        @ExceptionHandler(JeecgBootException.class)
        Result<?> handleJeecgBootException(JeecgBootException e) {
            return Result.error(e.getErrCode(), e.getMessage());
        }
    }

    private MockMvc mockMvc;

    @Mock
    private IContentRiskControlBizService riskControlBizService;

    @InjectMocks
    private ContentRiskControlController controller;

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
    @DisplayName("appeal")
    class Appeal {

        @Test
        @DisplayName("valid request - returns success")
        void validRequest_returnsSuccess() throws Exception {
            mockMvc.perform(post("/content/auth/risk/appeal")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"eventId":"evt_001","resolvedBy":"u_001","note":"误操作"}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.result").value("申诉成功"));

            verify(riskControlBizService).appealRiskEvent("evt_001", "u_001", "误操作");
        }

        @Test
        @DisplayName("blank eventId - returns 400")
        void blankEventId_returns400() throws Exception {
            mockMvc.perform(post("/content/auth/risk/appeal")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"eventId":"","resolvedBy":"u_001"}
                                    """))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("blank resolvedBy - returns 400")
        void blankResolvedBy_returns400() throws Exception {
            mockMvc.perform(post("/content/auth/risk/appeal")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"eventId":"evt_001","resolvedBy":""}
                                    """))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("event not found - returns business error")
        void eventNotFound_returnsBusinessError() throws Exception {
            org.mockito.Mockito.doThrow(new JeecgBootException("风险事件不存在"))
                    .when(riskControlBizService).appealRiskEvent(eq("evt_not_exist"), eq("u_001"), any());

            mockMvc.perform(post("/content/auth/risk/appeal")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"eventId":"evt_not_exist","resolvedBy":"u_001","note":"test"}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("风险事件不存在"));
        }
    }

    @Nested
    @DisplayName("confirmLogin")
    class ConfirmLogin {

        @Test
        @DisplayName("valid request self - returns success")
        void validRequestSelf_returnsSuccess() throws Exception {
            mockMvc.perform(post("/content/auth/risk/confirm-login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"userId":"u_001","eventId":"evt_002","isSelf":true}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.result").value("确认成功"));
        }

        @Test
        @DisplayName("blank userId - returns 400")
        void blankUserId_returns400() throws Exception {
            mockMvc.perform(post("/content/auth/risk/confirm-login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"userId":"","eventId":"evt_002","isSelf":true}
                                    """))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("null isSelf - returns 400")
        void nullIsSelf_returns400() throws Exception {
            mockMvc.perform(post("/content/auth/risk/confirm-login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"userId":"u_001","eventId":"evt_002"}
                                    """))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("notifications")
    class Notifications {

        @Test
        @DisplayName("valid userId - returns list")
        void validUserId_returnsList() throws Exception {
            ContentRiskEvent event = new ContentRiskEvent();
            event.setId("evt_001");
            event.setUserId("u_001");
            when(riskControlBizService.getPendingNotifications("u_001")).thenReturn(List.of(event));

            mockMvc.perform(get("/content/auth/risk/notifications")
                            .param("userId", "u_001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.result[0].id").value("evt_001"));
        }

        @Test
        @DisplayName("missing userId - returns 400")
        void missingUserId_returns400() throws Exception {
            mockMvc.perform(get("/content/auth/risk/notifications"))
                    .andExpect(status().isBadRequest());
        }
    }
}
