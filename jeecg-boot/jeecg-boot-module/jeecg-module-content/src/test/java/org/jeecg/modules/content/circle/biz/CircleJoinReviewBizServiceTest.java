package org.jeecg.modules.content.circle.biz;

import org.jeecg.modules.content.circle.entity.CircleAuditLog;
import org.jeecg.modules.content.circle.enums.CircleAuditActionEnum;
import org.jeecg.modules.content.circle.service.ICircleAuditLogService;
import org.jeecg.modules.content.circle.service.ICircleJoinReviewService;
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
 * 圈子加入申请审核业务编排服务测试。
 */
@ExtendWith(MockitoExtension.class)
class CircleJoinReviewBizServiceTest {

    @Mock
    private ICircleJoinReviewService circleJoinReviewService;

    @Mock
    private ICircleAuditLogService circleAuditLogService;

    @InjectMocks
    private CircleJoinReviewBizService circleJoinReviewBizService;

    private static final String TEST_REQUEST_ID = "req001";
    private static final String TEST_OPERATOR_ID = "admin001";
    private static final String TEST_CIRCLE_ID = "circle001";

    // ==================== approve ====================

    @Nested
    @DisplayName("approve - 批准加入申请")
    class Approve {

        @Test
        @DisplayName("批准申请 - 调用服务并写入APPROVE_JOIN审计日志")
        void approve_callsServiceAndWritesAuditLog() {
            // when
            circleJoinReviewBizService.approve(TEST_REQUEST_ID, TEST_OPERATOR_ID, TEST_CIRCLE_ID);

            // then
            verify(circleJoinReviewService).approve(TEST_REQUEST_ID, TEST_OPERATOR_ID);

            ArgumentCaptor<CircleAuditLog> logCaptor = ArgumentCaptor.forClass(CircleAuditLog.class);
            verify(circleAuditLogService).writeAuditLog(logCaptor.capture());
            CircleAuditLog capturedLog = logCaptor.getValue();
            assertThat(capturedLog.getCircleId()).isEqualTo(TEST_CIRCLE_ID);
            assertThat(capturedLog.getOperatorId()).isEqualTo(TEST_OPERATOR_ID);
            assertThat(capturedLog.getAction()).isEqualTo(CircleAuditActionEnum.APPROVE_JOIN.getCode());
            assertThat(capturedLog.getTargetId()).isEqualTo(TEST_REQUEST_ID);
            assertThat(capturedLog.getTargetType()).isEqualTo("JOIN_REQUEST");
            assertThat(capturedLog.getResult()).isEqualTo("SUCCESS");
        }
    }

    // ==================== reject ====================

    @Nested
    @DisplayName("reject - 拒绝加入申请")
    class Reject {

        @Test
        @DisplayName("拒绝申请 - 调用服务并写入REJECT_JOIN审计日志（含拒绝原因）")
        void reject_callsServiceAndWritesAuditLogWithReason() {
            // given
            String reason = "不符合加入条件";

            // when
            circleJoinReviewBizService.reject(TEST_REQUEST_ID, TEST_OPERATOR_ID, TEST_CIRCLE_ID, reason);

            // then
            verify(circleJoinReviewService).reject(TEST_REQUEST_ID, TEST_OPERATOR_ID, reason);

            ArgumentCaptor<CircleAuditLog> logCaptor = ArgumentCaptor.forClass(CircleAuditLog.class);
            verify(circleAuditLogService).writeAuditLog(logCaptor.capture());
            CircleAuditLog capturedLog = logCaptor.getValue();
            assertThat(capturedLog.getCircleId()).isEqualTo(TEST_CIRCLE_ID);
            assertThat(capturedLog.getOperatorId()).isEqualTo(TEST_OPERATOR_ID);
            assertThat(capturedLog.getAction()).isEqualTo(CircleAuditActionEnum.REJECT_JOIN.getCode());
            assertThat(capturedLog.getTargetId()).isEqualTo(TEST_REQUEST_ID);
            assertThat(capturedLog.getTargetType()).isEqualTo("JOIN_REQUEST");
            assertThat(capturedLog.getResult()).isEqualTo("SUCCESS");
            assertThat(capturedLog.getReason()).isEqualTo(reason);
        }
    }
}
