package org.jeecg.modules.content.circle.service;

import org.jeecg.modules.content.circle.entity.CircleReport;
import org.jeecg.modules.content.circle.enums.CircleReportStatusEnum;
import org.jeecg.modules.content.circle.mapper.CircleReportMapper;
import org.jeecg.modules.content.circle.service.impl.CircleReportServiceImpl;
import org.jeecg.modules.content.user.service.IContentNotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 圈子内容举报服务测试。
 */
@ExtendWith(MockitoExtension.class)
class CircleReportServiceTest {

    @Mock
    private CircleReportMapper circleReportMapper;

    @Mock
    private IContentNotificationService contentNotificationService;

    @InjectMocks
    private CircleReportServiceImpl circleReportService;

    private static final String TEST_REPORT_ID = "report001";
    private static final String TEST_CIRCLE_ID = "circle001";
    private static final String TEST_CONTENT_ID = "content001";
    private static final String TEST_REPORTER_ID = "user001";
    private static final String TEST_OPERATOR_ID = "admin001";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(circleReportService, "baseMapper", circleReportMapper);
    }

    private CircleReport createPendingReport() {
        CircleReport report = new CircleReport();
        report.setId(TEST_REPORT_ID);
        report.setCircleId(TEST_CIRCLE_ID);
        report.setContentId(TEST_CONTENT_ID);
        report.setReporterId(TEST_REPORTER_ID);
        report.setStatus(CircleReportStatusEnum.PENDING.getCode());
        return report;
    }

    // ==================== submitReport ====================

    @Nested
    @DisplayName("submitReport - 提交举报")
    class SubmitReport {

        @Test
        @DisplayName("提交举报 - 设置PENDING状态并保存")
        void submitReport_createsPendingReport() {
            // given
            CircleReport report = new CircleReport();
            report.setCircleId(TEST_CIRCLE_ID);
            report.setContentId(TEST_CONTENT_ID);
            report.setReporterId(TEST_REPORTER_ID);
            when(circleReportMapper.selectCount(any())).thenReturn(0L);
            when(circleReportMapper.insert(any(CircleReport.class))).thenReturn(1);

            // when
            circleReportService.submitReport(report);

            // then
            assertThat(report.getStatus()).isEqualTo(CircleReportStatusEnum.PENDING.getCode());
            verify(circleReportMapper).insert(report);
        }

        @Test
        @DisplayName("重复举报 - 抛出IllegalArgumentException")
        void submitReport_duplicate_throwsException() {
            // given
            CircleReport report = new CircleReport();
            report.setCircleId(TEST_CIRCLE_ID);
            report.setContentId(TEST_CONTENT_ID);
            report.setReporterId(TEST_REPORTER_ID);
            when(circleReportMapper.selectCount(any())).thenReturn(1L);

            // when & then
            assertThatThrownBy(() -> circleReportService.submitReport(report))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("重复举报");
        }
    }

    // ==================== handleDeleteContent ====================

    @Nested
    @DisplayName("handleDeleteContent - 删除被举报内容")
    class HandleDeleteContent {

        @Test
        @DisplayName("删除内容 - 设置RESOLVED状态和DELETE动作，并发送通知")
        void handleDeleteContent_setsResolvedAndDelete() {
            // given
            CircleReport report = createPendingReport();
            when(circleReportMapper.selectById(TEST_REPORT_ID)).thenReturn(report);
            when(circleReportMapper.updateById(any(CircleReport.class))).thenReturn(1);

            // when
            circleReportService.handleDeleteContent(TEST_REPORT_ID, TEST_OPERATOR_ID);

            // then
            ArgumentCaptor<CircleReport> captor = ArgumentCaptor.forClass(CircleReport.class);
            verify(circleReportMapper).updateById(captor.capture());
            CircleReport updated = captor.getValue();
            assertThat(updated.getStatus()).isEqualTo(CircleReportStatusEnum.RESOLVED.getCode());
            assertThat(updated.getHandleAction()).isEqualTo("DELETE");
            assertThat(updated.getOperatorId()).isEqualTo(TEST_OPERATOR_ID);
            assertThat(updated.getOperateTime()).isNotNull();

            verify(contentNotificationService).sendNotification(
                    eq(TEST_REPORTER_ID), eq("REPORT_RESOLVED"), anyString(), anyString());
        }
    }

    // ==================== handleIgnore ====================

    @Nested
    @DisplayName("handleIgnore - 忽略举报")
    class HandleIgnore {

        @Test
        @DisplayName("忽略举报 - 设置IGNORED状态和IGNORE动作，并发送通知")
        void handleIgnore_setsIgnored() {
            // given
            CircleReport report = createPendingReport();
            when(circleReportMapper.selectById(TEST_REPORT_ID)).thenReturn(report);
            when(circleReportMapper.updateById(any(CircleReport.class))).thenReturn(1);

            // when
            circleReportService.handleIgnore(TEST_REPORT_ID, TEST_OPERATOR_ID);

            // then
            ArgumentCaptor<CircleReport> captor = ArgumentCaptor.forClass(CircleReport.class);
            verify(circleReportMapper).updateById(captor.capture());
            CircleReport updated = captor.getValue();
            assertThat(updated.getStatus()).isEqualTo(CircleReportStatusEnum.IGNORED.getCode());
            assertThat(updated.getHandleAction()).isEqualTo("IGNORE");
            assertThat(updated.getOperatorId()).isEqualTo(TEST_OPERATOR_ID);

            verify(contentNotificationService).sendNotification(
                    eq(TEST_REPORTER_ID), eq("REPORT_IGNORED"), anyString(), anyString());
        }
    }

    // ==================== getReports ====================

    @Nested
    @DisplayName("getReports - 查询举报列表")
    class GetReports {

        @Test
        @DisplayName("按状态过滤 - 返回指定状态的举报列表")
        void getReports_filtersByStatus() {
            // given
            List<CircleReport> expected = Arrays.asList(createPendingReport());
            when(circleReportMapper.selectByCircleAndStatus(TEST_CIRCLE_ID, "PENDING"))
                    .thenReturn(expected);

            // when
            List<CircleReport> result = circleReportService.getReports(TEST_CIRCLE_ID, "PENDING");

            // then
            assertThat(result).hasSize(1);
            assertThat(result).isEqualTo(expected);
            verify(circleReportMapper).selectByCircleAndStatus(TEST_CIRCLE_ID, "PENDING");
        }

        @Test
        @DisplayName("状态为null - 返回该圈子所有举报")
        void getReports_nullStatus_returnsAll() {
            // given
            List<CircleReport> expected = Arrays.asList(createPendingReport());
            when(circleReportMapper.selectByCircleAndStatus(TEST_CIRCLE_ID, null))
                    .thenReturn(expected);

            // when
            List<CircleReport> result = circleReportService.getReports(TEST_CIRCLE_ID, null);

            // then
            assertThat(result).hasSize(1);
            verify(circleReportMapper).selectByCircleAndStatus(TEST_CIRCLE_ID, null);
        }
    }
}
