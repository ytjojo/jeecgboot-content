package org.jeecg.modules.content.user.controller;

import org.jeecg.common.api.vo.Result;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.user.service.IContentAccountService;
import org.junit.jupiter.api.BeforeEach;
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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ContentAccountControllerWebMvcTest {

    @RestControllerAdvice
    static class TestJeecgBootExceptionHandler {

        @ExceptionHandler(JeecgBootException.class)
        Result<?> handleJeecgBootException(JeecgBootException e) {
            return Result.error(e.getErrCode(), e.getMessage());
        }
    }

    private MockMvc mockMvc;

    @Mock
    private IContentAccountService contentAccountService;

    @InjectMocks
    private ContentAccountController contentAccountController;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(contentAccountController)
            .setValidator(validator)
            .setControllerAdvice(new TestJeecgBootExceptionHandler())
            .build();
    }

    @Test
    void applyCancel_validRequest_returnsSuccess() throws Exception {
        mockMvc.perform(post("/api/v1/content/user/account/cancel/apply")
                .param("userId", "u1")
                .param("operatorUserId", "operator1")
                .param("reason", "user requested"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.result").value("注销流程已发起"));

        verify(contentAccountService).initiateCancel("u1", "operator1", "user requested");
    }

    @Test
    void completeCancel_validRequest_returnsSuccess() throws Exception {
        mockMvc.perform(post("/api/v1/content/user/account/cancel/complete")
                .param("userId", "u1")
                .param("operatorUserId", "system"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.result").value("账号已完成注销"));

        verify(contentAccountService).completeCancel("u1", "system");
    }

    @Test
    void revokeCancel_validRequest_returnsSuccess() throws Exception {
        mockMvc.perform(post("/api/v1/content/user/account/cancel/revoke")
                .param("userId", "u1")
                .param("operatorUserId", "u1")
                .param("reason", "keep account"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.result").value("注销申请已撤销"));

        verify(contentAccountService).revokeCancel("u1", "u1", "keep account");
    }

    @Test
    void completeCancel_coolingPeriodNotEnded_returnsBusinessError() throws Exception {
        doThrow(new JeecgBootException("注销冷静期未结束"))
            .when(contentAccountService)
            .completeCancel("u1", "system");

        mockMvc.perform(post("/api/v1/content/user/account/cancel/complete")
                .param("userId", "u1")
                .param("operatorUserId", "system"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("注销冷静期未结束"));
    }

    @Test
    void registerByEmail_validRequest_returnsSuccess() throws Exception {
        when(contentAccountService.registerByEmail(any())).thenReturn("u_mail_1001");

        mockMvc.perform(post("/api/v1/content/user/account/register/email")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "email":"user@example.com",
                      "password":"Pass@123",
                      "nickname":"邮箱用户"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.result").value("u_mail_1001"));
    }

    @Test
    void bindMobile_secondaryVerifyFailed_returnsBusinessError() throws Exception {
        doThrow(new JeecgBootException("绑定手机号需先完成二次校验"))
            .when(contentAccountService)
            .bindMobile(any());

        mockMvc.perform(post("/api/v1/content/user/account/bind/mobile")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "userId":"u1",
                      "mobile":"13800000002",
                      "secondaryVerified":false
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("绑定手机号需先完成二次校验"));
    }

    @Test
    void bindEmail_validRequest_returnsSuccess() throws Exception {
        mockMvc.perform(post("/api/v1/content/user/account/bind/email")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "userId":"u1",
                      "email":"bind@example.com",
                      "operatorUserId":"u1",
                      "secondaryVerified":true
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.result").value("邮箱绑定成功"));

        verify(contentAccountService).bindEmail(any());
    }

    @Test
    void unbindMobile_validRequest_returnsSuccess() throws Exception {
        mockMvc.perform(post("/api/v1/content/user/account/unbind/mobile")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "userId":"u1",
                      "operatorUserId":"u1",
                      "secondaryVerified":true
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.result").value("手机号解绑成功"));

        verify(contentAccountService).unbindMobile(any());
    }

    @Test
    void unbindEmail_validRequest_returnsSuccess() throws Exception {
        mockMvc.perform(post("/api/v1/content/user/account/unbind/email")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "userId":"u1",
                      "operatorUserId":"u1",
                      "secondaryVerified":true
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.result").value("邮箱解绑成功"));

        verify(contentAccountService).unbindEmail(any());
    }
}
