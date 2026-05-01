package org.jeecg.modules.content.user.controller;

import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.exception.JeecgBootExceptionHandler;
import org.jeecg.modules.base.service.BaseCommonService;
import org.jeecg.modules.content.user.service.IContentAccountService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc(addFilters = false)
@Import(JeecgBootExceptionHandler.class)
@WebMvcTest(
    controllers = ContentAccountController.class,
    excludeAutoConfiguration = {
        SecurityAutoConfiguration.class,
        SecurityFilterAutoConfiguration.class
    }
)
class ContentAccountControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IContentAccountService contentAccountService;

    @MockitoBean
    private BaseCommonService baseCommonService;

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
