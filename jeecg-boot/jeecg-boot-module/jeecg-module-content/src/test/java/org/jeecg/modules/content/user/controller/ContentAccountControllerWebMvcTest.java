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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
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
        mockMvc.perform(post("/content/user/account/cancel/apply")
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
        mockMvc.perform(post("/content/user/account/cancel/complete")
                .param("userId", "u1")
                .param("operatorUserId", "system"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.result").value("账号已完成注销"));

        verify(contentAccountService).completeCancel("u1", "system");
    }

    @Test
    void revokeCancel_validRequest_returnsSuccess() throws Exception {
        mockMvc.perform(post("/content/user/account/cancel/revoke")
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

        mockMvc.perform(post("/content/user/account/cancel/complete")
                .param("userId", "u1")
                .param("operatorUserId", "system"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("注销冷静期未结束"));
    }
}
