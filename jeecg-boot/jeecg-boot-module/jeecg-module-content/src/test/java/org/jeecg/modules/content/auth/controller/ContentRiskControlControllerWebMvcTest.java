package org.jeecg.modules.content.auth.controller;

import com.alibaba.fastjson2.JSON;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.modules.content.auth.biz.IContentRiskControlBizService;
import org.jeecg.modules.content.auth.entity.ContentRiskEvent;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Collections;
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

        LoginUser loginUser = new LoginUser().setId("testUser");
        String userJson = JSON.toJSONString(loginUser);
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(
                new UsernamePasswordAuthenticationToken(userJson, null, Collections.emptyList())
        );
        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("appeal")
    class Appeal {

        @Test
        @DisplayName("valid request - returns success")
        void validRequest_returnsSuccess() throws Exception {
            mockMvc.perform(post("/api/v1/content/account-security/anomaly/appeal")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"eventId":"evt_001","note":"误操作"}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.result").value("申诉成功"));

            verify(riskControlBizService).appealRiskEvent("evt_001", "testUser", "误操作");
        }

        @Test
        @DisplayName("blank eventId - returns 400")
        void blankEventId_returns400() throws Exception {
            mockMvc.perform(post("/api/v1/content/account-security/anomaly/appeal")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"eventId":""}
                                    """))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("event not found - returns business error")
        void eventNotFound_returnsBusinessError() throws Exception {
            org.mockito.Mockito.doThrow(new JeecgBootException("风险事件不存在"))
                    .when(riskControlBizService).appealRiskEvent(eq("evt_not_exist"), eq("testUser"), any());

            mockMvc.perform(post("/api/v1/content/account-security/anomaly/appeal")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"eventId":"evt_not_exist","note":"test"}
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
            mockMvc.perform(post("/api/v1/content/account-security/anomaly/confirm")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"eventId":"evt_002","isSelf":true}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.result").value("确认成功"));
        }

        @Test
        @DisplayName("blank eventId - returns 400")
        void blankEventId_returns400() throws Exception {
            mockMvc.perform(post("/api/v1/content/account-security/anomaly/confirm")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"eventId":"","isSelf":true}
                                    """))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("null isSelf - returns 400")
        void nullIsSelf_returns400() throws Exception {
            mockMvc.perform(post("/api/v1/content/account-security/anomaly/confirm")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"eventId":"evt_002"}
                                    """))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("notifications")
    class Notifications {

        @Test
        @DisplayName("valid request - returns list")
        void validRequest_returnsList() throws Exception {
            ContentRiskEvent event = new ContentRiskEvent();
            event.setId("evt_001");
            event.setUserId("testUser");
            when(riskControlBizService.getPendingNotifications("testUser")).thenReturn(List.of(event));

            mockMvc.perform(get("/api/v1/content/account-security/anomaly/list"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.result[0].id").value("evt_001"));
        }
    }
}
