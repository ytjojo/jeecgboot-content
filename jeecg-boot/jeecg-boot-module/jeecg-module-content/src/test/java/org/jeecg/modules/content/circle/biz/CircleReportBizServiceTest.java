package org.jeecg.modules.content.circle.biz;

import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.circle.entity.CircleAuditLog;
import org.jeecg.modules.content.circle.entity.CircleMember;
import org.jeecg.modules.content.circle.entity.CircleReport;
import org.jeecg.modules.content.circle.enums.CircleAuditActionEnum;
import org.jeecg.modules.content.circle.service.ICircleAuditLogService;
import org.jeecg.modules.content.circle.service.ICircleMemberService;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * 圈子内容举报业务编排服务测试。
 */
@ExtendWith(MockitoExtension.class)
class CircleReportBizServiceTest {

    @Mock
    private ICircleReportService circleReportService;

    @Mock
    private ICircleAuditLogService circleAuditLogService;

    @Mock
    private ICircleMemberService circleMemberService;

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

        private void mockManagePermission() {
            CircleMember manager = new CircleMember();
            manager.setRole(CircleMember.Role.CREATOR);
            when(circleMemberService.findByCircleAndUser(TEST_CIRCLE_ID, TEST_OPERATOR_ID))
                    .thenReturn(manager);
        }

        @Test
        @DisplayName("删除内容 - 调用服务并写入DELETE_REPORTED审计日志")
        void handleDeleteContent_callsServiceAndWritesAuditLog() {
            mockManagePermission();

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

        @Test
        @DisplayName("操作人是普通成员 - 抛出权限不足异常")
        void operatorIsMember_throwsPermissionError() {
            CircleMember member = new CircleMember();
            member.setRole(CircleMember.Role.MEMBER);
            when(circleMemberService.findByCircleAndUser(TEST_CIRCLE_ID, TEST_OPERATOR_ID))
                    .thenReturn(member);

            assertThatThrownBy(() ->
                    circleReportBizService.handleDeleteContent(TEST_REPORT_ID, TEST_OPERATOR_ID, TEST_CIRCLE_ID))
                    .isInstanceOf(JeecgBootException.class)
                    .hasMessage("权限不足，仅创建者和版主可管理举报");

            verifyNoInteractions(circleReportService, circleAuditLogService);
        }
    }

    // ==================== handleIgnore ====================

    @Nested
    @DisplayName("handleIgnore - 忽略举报")
    class HandleIgnore {

        private void mockManagePermission() {
            CircleMember manager = new CircleMember();
            manager.setRole(CircleMember.Role.MODERATOR);
            when(circleMemberService.findByCircleAndUser(TEST_CIRCLE_ID, TEST_OPERATOR_ID))
                    .thenReturn(manager);
        }

        @Test
        @DisplayName("忽略举报 - 调用服务并写入IGNORE_REPORT审计日志")
        void handleIgnore_callsServiceAndWritesAuditLog() {
            mockManagePermission();

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

        @Test
        @DisplayName("操作人是普通成员 - 抛出权限不足异常")
        void operatorIsMember_throwsPermissionError() {
            CircleMember member = new CircleMember();
            member.setRole(CircleMember.Role.MEMBER);
            when(circleMemberService.findByCircleAndUser(TEST_CIRCLE_ID, TEST_OPERATOR_ID))
                    .thenReturn(member);

            assertThatThrownBy(() ->
                    circleReportBizService.handleIgnore(TEST_REPORT_ID, TEST_OPERATOR_ID, TEST_CIRCLE_ID))
                    .isInstanceOf(JeecgBootException.class)
                    .hasMessage("权限不足，仅创建者和版主可管理举报");

            verifyNoInteractions(circleReportService, circleAuditLogService);
        }
    }

    // ==================== handleMute ====================

    @Nested
    @DisplayName("handleMute - 禁言用户")
    class HandleMute {

        private void mockManagePermission() {
            CircleMember manager = new CircleMember();
            manager.setRole(CircleMember.Role.CREATOR);
            when(circleMemberService.findByCircleAndUser(TEST_CIRCLE_ID, TEST_OPERATOR_ID))
                    .thenReturn(manager);
        }

        @Test
        @DisplayName("禁言用户 - 调用服务并写入MUTE_FROM_REPORT审计日志")
        void handleMute_callsServiceAndWritesAuditLog() {
            mockManagePermission();

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

        @Test
        @DisplayName("操作人是普通成员 - 抛出权限不足异常")
        void operatorIsMember_throwsPermissionError() {
            CircleMember member = new CircleMember();
            member.setRole(CircleMember.Role.MEMBER);
            when(circleMemberService.findByCircleAndUser(TEST_CIRCLE_ID, TEST_OPERATOR_ID))
                    .thenReturn(member);

            assertThatThrownBy(() ->
                    circleReportBizService.handleMute(TEST_REPORT_ID, TEST_OPERATOR_ID, TEST_CIRCLE_ID))
                    .isInstanceOf(JeecgBootException.class)
                    .hasMessage("权限不足，仅创建者和版主可管理举报");

            verifyNoInteractions(circleReportService, circleAuditLogService);
        }
    }
}
