package org.jeecg.modules.content.auth.controller;

import com.alibaba.fastjson2.JSON;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.modules.content.auth.biz.ContentAuthBizService;
import org.jeecg.modules.content.auth.dto.AuthLoginResult;
import org.jeecg.modules.content.auth.service.IContentDeviceSessionService;
import org.jeecg.modules.content.auth.vo.DeviceSessionVO;
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
 * 内容社区认证控制器 WebMvc 参数校验和响应测试。
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ContentAuthController WebMvc")
class ContentAuthControllerWebMvcTest {

    @RestControllerAdvice
    static class TestExceptionHandler {
        @ExceptionHandler(JeecgBootException.class)
        Result<?> handleJeecgBootException(JeecgBootException e) {
            return Result.error(e.getErrCode(), e.getMessage());
        }
    }

    private MockMvc mockMvc;

    @Mock
    private ContentAuthBizService contentAuthBizService;
    @Mock
    private IContentDeviceSessionService contentDeviceSessionService;

    @InjectMocks
    private ContentAuthController controller;

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

    // ==================== 注册 ====================

    @Nested
    @DisplayName("registerByMobile")
    class RegisterByMobile {

        @Test
        @DisplayName("valid request - returns userId")
        void validRequest_returnsUserId() throws Exception {
            when(contentAuthBizService.registerByMobile(any())).thenReturn(new AuthLoginResult().setUserId("u_001").setAccessToken("token_abc"));

            mockMvc.perform(post("/api/v1/auth/register/mobile")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"mobile":"13800138000","code":"123456","nickname":"test"}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.result.userId").value("u_001"))
                    .andExpect(jsonPath("$.result.accessToken").value("token_abc"));
        }

        @Test
        @DisplayName("blank mobile - returns validation error")
        void blankMobile_returns400() throws Exception {
            mockMvc.perform(post("/api/v1/auth/register/mobile")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"mobile":"","code":"123456","nickname":"test"}
                                    """))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("invalid mobile format - returns validation error")
        void invalidMobile_returns400() throws Exception {
            mockMvc.perform(post("/api/v1/auth/register/mobile")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"mobile":"abc","code":"123456","nickname":"test"}
                                    """))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("blank code - returns validation error")
        void blankCode_returns400() throws Exception {
            mockMvc.perform(post("/api/v1/auth/register/mobile")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"mobile":"13800138000","code":"","nickname":"test"}
                                    """))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("duplicate mobile - returns business error")
        void duplicateMobile_returnsBusinessError() throws Exception {
            when(contentAuthBizService.registerByMobile(any()))
                    .thenThrow(new JeecgBootException("该手机号已被注册"));

            mockMvc.perform(post("/api/v1/auth/register/mobile")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"mobile":"13800138000","code":"123456","nickname":"test"}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("该手机号已被注册"));
        }
    }

    // ==================== 登录 ====================

    @Nested
    @DisplayName("loginByPassword")
    class LoginByPassword {

        @Test
        @DisplayName("valid request - returns login result")
        void validRequest_returnsLoginResult() throws Exception {
            AuthLoginResult result = new AuthLoginResult()
                    .setUserId("u_001").setAccessToken("token_abc").setJti("jti_123");
            when(contentAuthBizService.loginByPassword(any())).thenReturn(result);

            mockMvc.perform(post("/api/v1/auth/login/password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"identifier":"13800138000","password":"Pass@123"}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.result.userId").value("u_001"))
                    .andExpect(jsonPath("$.result.accessToken").value("token_abc"));
        }

        @Test
        @DisplayName("blank identifier - returns 400")
        void blankIdentifier_returns400() throws Exception {
            mockMvc.perform(post("/api/v1/auth/login/password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"identifier":"","password":"Pass@123"}
                                    """))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("wrong password - returns business error")
        void wrongPassword_returnsBusinessError() throws Exception {
            when(contentAuthBizService.loginByPassword(any()))
                    .thenThrow(new JeecgBootException("账号或密码错误"));

            mockMvc.perform(post("/api/v1/auth/login/password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"identifier":"13800138000","password":"wrong"}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("账号或密码错误"));
        }
    }

    @Nested
    @DisplayName("loginBySms")
    class LoginBySms {

        @Test
        @DisplayName("valid request - returns login result")
        void validRequest_returnsLoginResult() throws Exception {
            AuthLoginResult result = new AuthLoginResult()
                    .setUserId("u_001").setAccessToken("token_xyz").setJti("jti_456");
            when(contentAuthBizService.loginBySms(any())).thenReturn(result);

            mockMvc.perform(post("/api/v1/auth/login/sms")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"mobile":"13800138000","code":"123456"}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.result.accessToken").value("token_xyz"));
        }

        @Test
        @DisplayName("invalid mobile format - returns 400")
        void invalidMobile_returns400() throws Exception {
            mockMvc.perform(post("/api/v1/auth/login/sms")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"mobile":"not-a-phone","code":"123456"}
                                    """))
                    .andExpect(status().isBadRequest());
        }
    }

    // ==================== 绑定 ====================

    @Nested
    @DisplayName("bindMobile")
    class BindMobile {

        @Test
        @DisplayName("valid request - returns success")
        void validRequest_returnsSuccess() throws Exception {
            mockMvc.perform(post("/api/v1/auth/bind/phone")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"userId":"u_001","mobile":"13800138001","code":"123456"}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.result").value("手机号绑定成功"));

            verify(contentAuthBizService).bindMobile(any());
        }

        @Test
        @DisplayName("blank mobile - returns 400")
        void blankMobile_returns400() throws Exception {
            mockMvc.perform(post("/api/v1/auth/bind/phone")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"mobile":"","code":"123456"}
                                    """))
                    .andExpect(status().isBadRequest());
        }
    }

    // ==================== 设备管理 ====================

    @Nested
    @DisplayName("devices")
    class Devices {

        @Test
        @DisplayName("valid params - returns device list")
        void validParams_returnsDeviceList() throws Exception {
            when(contentDeviceSessionService.listDevices("testUser", "jti_123"))
                    .thenReturn(List.of());

            mockMvc.perform(get("/api/v1/auth/devices")
                            .param("currentTokenJti", "jti_123"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("missing currentTokenJti - returns 400")
        void missingJti_returns400() throws Exception {
            mockMvc.perform(get("/api/v1/auth/devices")
                            .param("userId", "u_001"))
                    .andExpect(status().isBadRequest());
        }
    }

    // ==================== 换绑/解绑 ====================

    @Nested
    @DisplayName("rebind and unbind")
    class RebindUnbind {

        @Test
        @DisplayName("rebindMobile valid - returns success")
        void rebindMobile_valid_returnsSuccess() throws Exception {
            mockMvc.perform(post("/api/v1/auth/rebind/phone")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"userId":"u_001","oldCode":"111111","newMobile":"13800138002","newCode":"222222"}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.result").value("手机号换绑成功"));
        }

        @Test
        @DisplayName("unbindEmail valid - returns success")
        void unbindEmail_valid_returnsSuccess() throws Exception {
            mockMvc.perform(post("/api/v1/auth/unbind/email")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"userId":"u_001","code":"123456"}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.result").value("邮箱解绑成功"));
        }

        @Test
        @DisplayName("unbind third-party valid - returns success")
        void unbindThirdParty_valid_returnsSuccess() throws Exception {
            mockMvc.perform(post("/api/v1/auth/unbind/third-party")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"userId":"u_001","provider":"WECHAT"}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.result").value("第三方账号解绑成功"));
        }
    }

    // ==================== 密码重置 ====================

    @Nested
    @DisplayName("password reset")
    class PasswordReset {

        @Test
        @DisplayName("resetPasswordByMobile valid - returns success")
        void resetByMobile_valid_returnsSuccess() throws Exception {
            mockMvc.perform(post("/api/v1/auth/reset-password/mobile")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"mobile":"13800138000","code":"123456","newPassword":"NewPass@1"}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.result").value("密码重置成功"));
        }

        @Test
        @DisplayName("resetPasswordByEmail valid - returns success")
        void resetByEmail_valid_returnsSuccess() throws Exception {
            mockMvc.perform(post("/api/v1/auth/reset-password/email")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"token":"reset_token_abc","newPassword":"NewPass@1"}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.result").value("密码重置成功"));
        }

        @Test
        @DisplayName("resetPasswordByMobile weak password - returns business error")
        void resetByMobile_weakPassword_returnsBusinessError() throws Exception {
            org.mockito.Mockito.doThrow(new JeecgBootException("密码不能与最近3次使用的密码相同"))
                    .when(contentAuthBizService).resetPasswordByMobile(any());

            mockMvc.perform(post("/api/v1/auth/reset-password/mobile")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"mobile":"13800138000","code":"123456","newPassword":"NewPass@1"}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("密码不能与最近3次使用的密码相同"));
        }
    }
}
