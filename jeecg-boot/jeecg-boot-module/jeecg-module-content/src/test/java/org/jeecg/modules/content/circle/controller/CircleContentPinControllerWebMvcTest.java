package org.jeecg.modules.content.circle.controller;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.circle.biz.CircleContentPinBizService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("CircleContentPinController WebMvc")
class CircleContentPinControllerWebMvcTest {

    private static final String TEST_OPERATOR_ID = "u_001";

    @RestControllerAdvice
    static class TestExceptionHandler {
        @ExceptionHandler(JeecgBootException.class)
        Result<?> handleJeecgBootException(JeecgBootException e) {
            return Result.error(e.getErrCode(), e.getMessage());
        }
    }

    private MockMvc mockMvc;

    @Mock
    private CircleContentPinBizService circleContentPinBizService;

    @InjectMocks
    private CircleContentPinController controller;

    private String validJwtToken;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setValidator(validator)
                .setControllerAdvice(new TestExceptionHandler())
                .build();

        validJwtToken = JWT.create()
                .withClaim("username", TEST_OPERATOR_ID)
                .sign(Algorithm.HMAC256("test-secret"));
    }

    // ==================== togglePin ====================

    @Nested
    @DisplayName("togglePin")
    class TogglePin {

        @Test
        @DisplayName("valid path+param - returns success and forwards to biz")
        void validPathAndParam_returnsSuccess() throws Exception {
            mockMvc.perform(put("/api/v1/content/circle/content/ct_001/pin")
                            .param("circleId", "c_001")
                            .header("X-Access-Token", validJwtToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("操作成功"));

            verify(circleContentPinBizService).pin("ct_001", TEST_OPERATOR_ID, "c_001");
        }

        @Test
        @DisplayName("missing circleId query - returns 400")
        void missingCircleId_returns400() throws Exception {
            mockMvc.perform(put("/api/v1/content/circle/content/ct_001/pin")
                            .header("X-Access-Token", validJwtToken))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(circleContentPinBizService);
        }

        @Test
        @DisplayName("biz throws - returns business error")
        void bizThrows_returnsBusinessError() throws Exception {
            doThrow(new JeecgBootException("仅圈主可置顶"))
                    .when(circleContentPinBizService).pin(eq("ct_001"), eq(TEST_OPERATOR_ID), eq("c_001"));

            mockMvc.perform(put("/api/v1/content/circle/content/ct_001/pin")
                            .param("circleId", "c_001")
                            .header("X-Access-Token", validJwtToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("仅圈主可置顶"));
        }
    }

    // ==================== toggleFeature ====================

    @Nested
    @DisplayName("toggleFeature")
    class ToggleFeature {

        @Test
        @DisplayName("valid path+param - returns success and forwards to biz")
        void validPathAndParam_returnsSuccess() throws Exception {
            mockMvc.perform(put("/api/v1/content/circle/content/ct_002/featured")
                            .param("circleId", "c_002")
                            .header("X-Access-Token", validJwtToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("操作成功"));

            verify(circleContentPinBizService).feature("ct_002", TEST_OPERATOR_ID, "c_002");
        }

        @Test
        @DisplayName("biz throws - returns business error")
        void bizThrows_returnsBusinessError() throws Exception {
            doThrow(new JeecgBootException("仅管理员可标记精华"))
                    .when(circleContentPinBizService).feature(eq("ct_002"), eq(TEST_OPERATOR_ID), eq("c_002"));

            mockMvc.perform(put("/api/v1/content/circle/content/ct_002/featured")
                            .param("circleId", "c_002")
                            .header("X-Access-Token", validJwtToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("仅管理员可标记精华"));
        }
    }
}
