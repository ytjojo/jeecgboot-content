package org.jeecg.modules.content.circle.controller;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.circle.biz.CircleReportBizService;
import org.jeecg.modules.content.circle.entity.CircleMember;
import org.jeecg.modules.content.circle.entity.CircleReport;
import org.jeecg.modules.content.circle.service.ICircleMemberService;
import org.jeecg.modules.content.circle.service.ICircleReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("CircleReportController WebMvc")
class CircleReportControllerWebMvcTest {

    private static final String TEST_REPORTER_ID = "u_reporter_001";
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
    private CircleReportBizService circleReportBizService;

    @Mock
    private ICircleReportService circleReportService;

    @Mock
    private ICircleMemberService circleMemberService;

    @InjectMocks
    private CircleReportController controller;

    private String validReporterToken;
    private String validOperatorToken;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setValidator(validator)
                .setControllerAdvice(new TestExceptionHandler())
                .build();

        validReporterToken = JWT.create()
                .withClaim("username", TEST_REPORTER_ID)
                .sign(Algorithm.HMAC256("test-secret"));
        validOperatorToken = JWT.create()
                .withClaim("username", TEST_OPERATOR_ID)
                .sign(Algorithm.HMAC256("test-secret"));
    }

    // ==================== submitReport ====================

    @Nested
    @DisplayName("submitReport")
    class SubmitReport {

        @Test
        @DisplayName("valid request - returns success and forwards fields to biz")
        void validRequest_returnsSuccess() throws Exception {
            mockMvc.perform(post("/api/v1/content/circle/report/")
                            .header("X-Access-Token", validReporterToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"circleId":"c_001","contentId":"ct_001","reason":"违规内容"}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("举报已提交"));

            ArgumentCaptor<CircleReport> captor = ArgumentCaptor.forClass(CircleReport.class);
            verify(circleReportBizService).submitReport(captor.capture(), eq(TEST_REPORTER_ID));
            assertThat(captor.getValue().getCircleId()).isEqualTo("c_001");
            assertThat(captor.getValue().getContentId()).isEqualTo("ct_001");
            assertThat(captor.getValue().getReason()).isEqualTo("违规内容");
        }

        @Test
        @DisplayName("blank contentId - returns 400")
        void blankContentId_returns400() throws Exception {
            mockMvc.perform(post("/api/v1/content/circle/report/")
                            .header("X-Access-Token", validReporterToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"circleId":"c_001","contentId":""}
                                    """))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(circleReportBizService);
        }

        @Test
        @DisplayName("biz throws - returns business error")
        void bizThrows_returnsBusinessError() throws Exception {
            doThrow(new JeecgBootException("已存在举报记录"))
                    .when(circleReportBizService).submitReport(any(), eq(TEST_REPORTER_ID));

            mockMvc.perform(post("/api/v1/content/circle/report/")
                            .header("X-Access-Token", validReporterToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"circleId":"c_001","contentId":"ct_dup","reason":"违规"}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("已存在举报记录"));
        }
    }

    // ==================== getReports ====================

    @Nested
    @DisplayName("getReports")
    class GetReports {

        private void mockManagePermission() {
            CircleMember manager = new CircleMember();
            manager.setRole(CircleMember.Role.CREATOR);
            when(circleMemberService.findByCircleAndUser("c_001", TEST_OPERATOR_ID))
                    .thenReturn(manager);
        }

        @Test
        @DisplayName("without status - returns all reports")
        void withoutStatus_returnsAll() throws Exception {
            mockManagePermission();
            CircleReport r1 = new CircleReport();
            r1.setId("r_001");
            r1.setCircleId("c_001");
            r1.setContentId("ct_001");
            r1.setReporterId("u_010");
            r1.setReason("reason-1");
            r1.setStatus("PENDING");
            r1.setCreateTime(new Date(1735000000000L));

            when(circleReportService.getReports("c_001", null))
                    .thenReturn(Arrays.asList(r1));

            mockMvc.perform(get("/api/v1/content/circle/report/list/c_001")
                            .header("X-Access-Token", validOperatorToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.result[0].id").value("r_001"))
                    .andExpect(jsonPath("$.result[0].circleId").value("c_001"))
                    .andExpect(jsonPath("$.result[0].contentId").value("ct_001"))
                    .andExpect(jsonPath("$.result[0].status").value("PENDING"));
        }

        @Test
        @DisplayName("with status filter - forwards status to service")
        void withStatusFilter_forwardsStatus() throws Exception {
            mockManagePermission();
            when(circleReportService.getReports("c_001", "PENDING"))
                    .thenReturn(Collections.emptyList());

            mockMvc.perform(get("/api/v1/content/circle/report/list/c_001")
                            .param("status", "PENDING")
                            .header("X-Access-Token", validOperatorToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.result.length()").value(0));

            verify(circleReportService).getReports("c_001", "PENDING");
        }

        @Test
        @DisplayName("operator is MEMBER - returns permission error")
        void operatorIsMember_returnsPermissionError() throws Exception {
            CircleMember member = new CircleMember();
            member.setRole(CircleMember.Role.MEMBER);
            when(circleMemberService.findByCircleAndUser("c_001", TEST_OPERATOR_ID))
                    .thenReturn(member);

            mockMvc.perform(get("/api/v1/content/circle/report/list/c_001")
                            .header("X-Access-Token", validOperatorToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("权限不足，仅创建者和版主可查看举报列表"));

            verifyNoInteractions(circleReportService);
        }
    }

    // ==================== handleDeleteContent ====================

    @Nested
    @DisplayName("handleDeleteContent")
    class HandleDeleteContent {

        @Test
        @DisplayName("valid path+param - calls biz.handleDeleteContent with operatorId")
        void validPathAndParam_callsBiz() throws Exception {
            mockMvc.perform(post("/api/v1/content/circle/report/r_001/delete-content")
                            .param("circleId", "c_001")
                            .header("X-Access-Token", validOperatorToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("已删除举报内容"));

            verify(circleReportBizService).handleDeleteContent("r_001", TEST_OPERATOR_ID, "c_001");
        }

        @Test
        @DisplayName("biz throws - returns business error")
        void bizThrows_returnsBusinessError() throws Exception {
            doThrow(new JeecgBootException("举报已处理"))
                    .when(circleReportBizService).handleDeleteContent("r_done", TEST_OPERATOR_ID, "c_001");

            mockMvc.perform(post("/api/v1/content/circle/report/r_done/delete-content")
                            .param("circleId", "c_001")
                            .header("X-Access-Token", validOperatorToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("举报已处理"));
        }
    }

    // ==================== handleIgnore ====================

    @Nested
    @DisplayName("handleIgnore")
    class HandleIgnore {

        @Test
        @DisplayName("valid path+param - calls biz.handleIgnore with operatorId")
        void validPathAndParam_callsBiz() throws Exception {
            mockMvc.perform(post("/api/v1/content/circle/report/r_002/ignore")
                            .param("circleId", "c_001")
                            .header("X-Access-Token", validOperatorToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("已忽略举报"));

            verify(circleReportBizService).handleIgnore("r_002", TEST_OPERATOR_ID, "c_001");
        }
    }

    // ==================== handleMute ====================

    @Nested
    @DisplayName("handleMute")
    class HandleMute {

        @Test
        @DisplayName("valid path+param - calls biz.handleMute with operatorId")
        void validPathAndParam_callsBiz() throws Exception {
            mockMvc.perform(post("/api/v1/content/circle/report/r_003/mute")
                            .param("circleId", "c_001")
                            .header("X-Access-Token", validOperatorToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("已禁言用户"));

            verify(circleReportBizService).handleMute("r_003", TEST_OPERATOR_ID, "c_001");
        }
    }
}
