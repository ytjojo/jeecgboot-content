package org.jeecg.modules.content.user.controller;

import org.jeecg.common.api.vo.Result;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.user.service.IContentUserThirdPartyAuthService;
import org.jeecg.modules.content.user.vo.ContentThirdPartyAuthVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 内容社区用户第三方授权 Controller WebMvc 测试。
 */
@ExtendWith(MockitoExtension.class)
class ContentUserThirdPartyAuthControllerWebMvcTest {

    @RestControllerAdvice
    static class TestJeecgBootExceptionHandler {

        @ExceptionHandler(JeecgBootException.class)
        Result<?> handleJeecgBootException(JeecgBootException e) {
            return Result.error(e.getErrCode(), e.getMessage());
        }
    }

    private MockMvc mockMvc;

    @Mock
    private IContentUserThirdPartyAuthService thirdPartyAuthService;

    @InjectMocks
    private ContentUserThirdPartyAuthController controller;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(new TestJeecgBootExceptionHandler())
            .build();
    }

    @Test
    void listActiveAuths_validUserId_returns200WithList() throws Exception {
        ContentThirdPartyAuthVO vo = new ContentThirdPartyAuthVO()
            .setAuthId("a1")
            .setAppName("微信")
            .setScopes(List.of("openid"))
            .setStatus("ACTIVE");
        when(thirdPartyAuthService.listActiveAuths("u1")).thenReturn(List.of(vo));

        mockMvc.perform(get("/content/user/auth/third-party/")
                .param("userId", "u1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.result[0].authId").value("a1"))
            .andExpect(jsonPath("$.result[0].appName").value("微信"));
    }

    @Test
    void revokeAuth_validAuthId_returns200() throws Exception {
        when(thirdPartyAuthService.revokeAuth("u1", "a1")).thenReturn(true);

        mockMvc.perform(delete("/content/user/auth/third-party/a1")
                .param("userId", "u1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.result").value("授权已撤销"));

        verify(thirdPartyAuthService).revokeAuth("u1", "a1");
    }

    @Test
    void revokeAuth_nonExistentAuthId_returnsBusinessError() throws Exception {
        when(thirdPartyAuthService.revokeAuth("u1", "nonexistent"))
            .thenThrow(new JeecgBootException("授权记录不存在"));

        mockMvc.perform(delete("/content/user/auth/third-party/nonexistent")
                .param("userId", "u1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("授权记录不存在"));
    }
}
