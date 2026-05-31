package org.jeecg.modules.content.circle.biz;

import org.jeecg.modules.content.circle.entity.CircleAuditLog;
import org.jeecg.modules.content.circle.entity.CircleReport;
import org.jeecg.modules.content.circle.enums.CircleAuditActionEnum;
import org.jeecg.modules.content.circle.service.ICircleAuditLogService;
import org.jeecg.modules.content.circle.service.ICircleReportService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

/**
 * 圈子内容举报业务编排服务测试。
 */
@ExtendWith(MockitoExtension.class)
class CircleReportBizServiceTest {

    @Mock
    private ICircleReportService circleReportService;

    @Mock
    private ICircleAuditLogService circleAuditLogService;

    @InjectMocks
    private CircleReportBizService circleReportBizService;

    private static final String TEST_REPORT_ID = "report001";
    private static final String TEST_REPORTER_ID = "user001";
    private static final String TEST_OPERATOR_ID = "admin001";
    private static final String TEST_CIRCLE_ID = "circle001";
    private static final String TEST_CONTENT_ID = "content001";

    // ==================== submitReport ====================

    @Nested
    @DisplayName("submitReport - 提交举报")
    class SubmitReport {

        @Test
        @DisplayName("提交举报 - 设置reporterId并调用服务")
        void submitReport_setsReporterIdAndCallsService() {
            // given
            CircleReport report = new CircleReport();
            report.setCircleId(TEST_CIRCLE_ID);
            report.setContentId(TEST_CONTENT_ID);

            // when
            circleReportBizService.submitReport(report, TEST_REPORTER_ID);

            // then
            assertThat(report.getReporterId()).isEqualTo(TEST_REPORTER_ID);
            verify(circleReportService).submitReport(report);
        }
    }

    // ==================== handleDeleteContent ====================

    @Nested
    @DisplayName("handleDeleteContent - 删除被举报内容")
    class HandleDeleteContent {

        @Test
        @DisplayName("删除内容 - 调用服务并写入DELETE_REPORTED审计日志")
        void handleDeleteContent_callsServiceAndWritesAuditLog() {
            // when
            circleReportBizService.handleDeleteContent(TEST_REPORT_ID, TEST_OPERATOR_ID, TEST_CIRCLE_ID);

            // then
            verify(circleReportService).handleDeleteContent(TEST_REPORT_ID, TEST_OPERATOR_ID);

            ArgumentCaptor<CircleAuditLog> logCaptor = ArgumentCaptor.forClass(CircleAuditLog.class);
            verify(circleAuditLogService).writeAuditLog(logCaptor.capture());
            CircleAuditLog capturedLog = logCaptor.getValue();
            assertThat(capturedLog.getCircleId()).isEqualTo(TEST_CIRCLE_ID);
            assertThat(capturedLog.getOperatorId()).isEqualTo(TEST_OPERATOR_ID);
            assertThat(capturedLog.getAction()).isEqualTo(CircleAuditActionEnum.DELETE_REPORTED.getCode());
            assertThat(capturedLog.getTargetId()).isEqualTo(TEST_REPORT_ID);
            assertThat(capturedLog.getTargetType()).isEqualTo("REPORT");
            assertThat(capturedLog.getResult()).isEqualTo("SUCCESS");
        }
    }

    // ==================== handleIgnore ====================

    @Nested
    @DisplayName("handleIgnore - 忽略举报")
    class HandleIgnore {

        @Test
        @DisplayName("忽略举报 - 调用服务并写入IGNORE_REPORT审计日志")
        void handleIgnore_callsServiceAndWritesAuditLog() {
            // when
            circleReportBizService.handleIgnore(TEST_REPORT_ID, TEST_OPERATOR_ID, TEST_CIRCLE_ID);

            // then
            verify(circleReportService).handleIgnore(TEST_REPORT_ID, TEST_OPERATOR_ID);

            ArgumentCaptor<CircleAuditLog> logCaptor = ArgumentCaptor.forClass(CircleAuditLog.class);
            verify(circleAuditLogService).writeAuditLog(logCaptor.capture());
            CircleAuditLog capturedLog = logCaptor.getValue();
            assertThat(capturedLog.getAction()).isEqualTo(CircleAuditActionEnum.IGNORE_REPORT.getCode());
            assertThat(capturedLog.getTargetType()).isEqualTo("REPORT");
        }
    }

    // ==================== handleMute ====================

    @Nested
    @DisplayName("handleMute - 禁言用户")
    class HandleMute {

        @Test
        @DisplayName("禁言用户 - 调用服务并写入MUTE_FROM_REPORT审计日志")
        void handleMute_callsServiceAndWritesAuditLog() {
            // when
            circleReportBizService.handleMute(TEST_REPORT_ID, TEST_OPERATOR_ID, TEST_CIRCLE_ID);

            // then
            verify(circleReportService).handleMute(TEST_REPORT_ID, TEST_OPERATOR_ID);

            ArgumentCaptor<CircleAuditLog> logCaptor = ArgumentCaptor.forClass(CircleAuditLog.class);
            verify(circleAuditLogService).writeAuditLog(logCaptor.capture());
            CircleAuditLog capturedLog = logCaptor.getValue();
            assertThat(capturedLog.getAction()).isEqualTo(CircleAuditActionEnum.MUTE_FROM_REPORT.getCode());
            assertThat(capturedLog.getTargetType()).isEqualTo("REPORT");
        }
    }
}
