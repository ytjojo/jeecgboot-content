package org.jeecg.modules.content.circle.controller;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.circle.biz.CircleJoinReviewBizService;
import org.jeecg.modules.content.circle.entity.CircleJoinRequest;
import org.jeecg.modules.content.circle.service.ICircleJoinReviewService;
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

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("CircleJoinReviewController WebMvc")
class CircleJoinReviewControllerWebMvcTest {

    private static final String TEST_OPERATOR_ID = "admin001";

    @RestControllerAdvice
    static class TestExceptionHandler {
        @ExceptionHandler(JeecgBootException.class)
        Result<?> handleJeecgBootException(JeecgBootException e) {
            return Result.error(e.getErrCode(), e.getMessage());
        }
    }

    private MockMvc mockMvc;

    @Mock
    private CircleJoinReviewBizService circleJoinReviewBizService;

    @Mock
    private ICircleJoinReviewService circleJoinReviewService;

    @InjectMocks
    private CircleJoinReviewController controller;

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

    // ==================== getPending ====================

    @Nested
    @DisplayName("getPending")
    class GetPending {

        @Test
        @DisplayName("non-empty - returns mapped VOs")
        void nonEmpty_returnsMappedVOs() throws Exception {
            CircleJoinRequest req1 = new CircleJoinRequest();
            req1.setId("req_001");
            req1.setCircleId("c_001");
            req1.setUserId("u_010");
            req1.setStatus("PENDING");
            req1.setCreateTime(new Date(1735000000000L));

            CircleJoinRequest req2 = new CircleJoinRequest();
            req2.setId("req_002");
            req2.setCircleId("c_001");
            req2.setUserId("u_011");
            req2.setStatus("PENDING");
            req2.setCreateTime(new Date(1735100000000L));

            when(circleJoinReviewService.getPendingRequests("c_001"))
                    .thenReturn(Arrays.asList(req1, req2));

            mockMvc.perform(get("/circle-join-review/pending/c_001")
                            .header("X-Access-Token", validJwtToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.result[0].id").value("req_001"))
                    .andExpect(jsonPath("$.result[0].circleId").value("c_001"))
                    .andExpect(jsonPath("$.result[0].userId").value("u_010"))
                    .andExpect(jsonPath("$.result[0].status").value("PENDING"))
                    .andExpect(jsonPath("$.result[1].id").value("req_002"));
        }

        @Test
        @DisplayName("empty - returns OK with empty array")
        void empty_returnsEmptyArray() throws Exception {
            when(circleJoinReviewService.getPendingRequests("c_empty"))
                    .thenReturn(Collections.emptyList());

            mockMvc.perform(get("/circle-join-review/pending/c_empty")
                            .header("X-Access-Token", validJwtToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.result").isArray())
                    .andExpect(jsonPath("$.result.length()").value(0));
        }
    }

    // ==================== approve ====================

    @Nested
    @DisplayName("approve")
    class Approve {

        @Test
        @DisplayName("valid request - returns success and forwards 3 args to biz")
        void validRequest_returnsSuccess() throws Exception {
            mockMvc.perform(post("/circle-join-review/approve")
                            .param("circleId", "c_001")
                            .header("X-Access-Token", validJwtToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"requestId":"req_001"}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("已批准"));

            verify(circleJoinReviewBizService).approve("req_001", TEST_OPERATOR_ID, "c_001");
        }

        @Test
        @DisplayName("blank requestId - returns 400")
        void blankRequestId_returns400() throws Exception {
            mockMvc.perform(post("/circle-join-review/approve")
                            .param("circleId", "c_001")
                            .header("X-Access-Token", validJwtToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"requestId":""}
                                    """))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("biz throws - returns business error")
        void bizThrows_returnsBusinessError() throws Exception {
            doThrow(new JeecgBootException("申请已被处理"))
                    .when(circleJoinReviewBizService).approve(anyString(), eq(TEST_OPERATOR_ID), eq("c_001"));

            mockMvc.perform(post("/circle-join-review/approve")
                            .param("circleId", "c_001")
                            .header("X-Access-Token", validJwtToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"requestId":"req_dup"}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("申请已被处理"));
        }
    }

    // ==================== reject ====================

    @Nested
    @DisplayName("reject")
    class Reject {

        @Test
        @DisplayName("valid request with reason - returns success and forwards 4 args including reason")
        void validRequestWithReason_returnsSuccess() throws Exception {
            mockMvc.perform(post("/circle-join-review/reject")
                            .param("circleId", "c_001")
                            .header("X-Access-Token", validJwtToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"requestId":"req_002","rejectReason":"信息不完整"}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("已拒绝"));

            verify(circleJoinReviewBizService).reject("req_002", TEST_OPERATOR_ID, "c_001", "信息不完整");
        }

        @Test
        @DisplayName("missing reason - still calls biz with null reason")
        void missingReason_callsBizWithNullReason() throws Exception {
            mockMvc.perform(post("/circle-join-review/reject")
                            .param("circleId", "c_001")
                            .header("X-Access-Token", validJwtToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"requestId":"req_002"}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));

            verify(circleJoinReviewBizService).reject("req_002", TEST_OPERATOR_ID, "c_001", null);
        }

        @Test
        @DisplayName("biz throws - returns business error")
        void bizThrows_returnsBusinessError() throws Exception {
            doThrow(new JeecgBootException("权限不足"))
                    .when(circleJoinReviewBizService).reject(any(), any(), any(), any());

            mockMvc.perform(post("/circle-join-review/reject")
                            .param("circleId", "c_001")
                            .header("X-Access-Token", validJwtToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"requestId":"req_002","rejectReason":"信息不完整"}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("权限不足"));
        }
    }
}
