package org.jeecg.modules.content.circle.controller;

import org.jeecg.common.api.vo.Result;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.circle.biz.ICircleMemberBiz;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("CircleMemberController WebMvc")
class CircleMemberControllerWebMvcTest {

    @RestControllerAdvice
    static class TestExceptionHandler {
        @ExceptionHandler(JeecgBootException.class)
        Result<?> handleJeecgBootException(JeecgBootException e) {
            return Result.error(e.getErrCode(), e.getMessage());
        }
    }

    private MockMvc mockMvc;

    @Mock
    private ICircleMemberBiz circleMemberBiz;

    @InjectMocks
    private CircleMemberController controller;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setValidator(validator)
                .setControllerAdvice(new TestExceptionHandler())
                .build();

        // SecureUtil.currentUser() reads JSON from SecurityContextHolder
        Authentication auth = mock(Authentication.class);
        lenient().when(auth.getName()).thenReturn("{\"id\":\"u_001\"}");
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("changeRole")
    class ChangeRole {

        @Test
        @DisplayName("valid request - returns success")
        void validRequest_returnsSuccess() throws Exception {
            mockMvc.perform(post("/api/v1/content/circle/member/change-role")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"circleId":"c_001","targetUserId":"u_002","targetRole":"MODERATOR"}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));

            verify(circleMemberBiz).changeRole(any(), eq("u_001"));
        }

        @Test
        @DisplayName("blank targetUserId - returns 400")
        void blankTargetUserId_returns400() throws Exception {
            mockMvc.perform(post("/api/v1/content/circle/member/change-role")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"circleId":"c_001","targetUserId":"","targetRole":"MODERATOR"}
                                    """))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("muteMember")
    class MuteMember {

        @Test
        @DisplayName("valid request - returns success")
        void validRequest_returnsSuccess() throws Exception {
            mockMvc.perform(post("/api/v1/content/circle/member/mute")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"circleId":"c_001","targetUserId":"u_002","muteDuration":"24h","reason":"违规发言"}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("removeMember")
    class RemoveMember {

        @Test
        @DisplayName("valid request - returns success")
        void validRequest_returnsSuccess() throws Exception {
            mockMvc.perform(post("/api/v1/content/circle/member/remove")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"circleId":"c_001","targetUserId":"u_002","reason":"严重违规"}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("not member - returns business error")
        void notMember_returnsBusinessError() throws Exception {
            doThrow(new JeecgBootException("目标用户不是圈子成员"))
                    .when(circleMemberBiz).removeMember(any(), eq("u_001"));

            mockMvc.perform(post("/api/v1/content/circle/member/remove")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"circleId":"c_001","targetUserId":"u_999"}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("目标用户不是圈子成员"));
        }
    }
}
