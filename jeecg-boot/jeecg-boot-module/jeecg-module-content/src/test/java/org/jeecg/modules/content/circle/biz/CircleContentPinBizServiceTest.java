package org.jeecg.modules.content.circle.biz;

import org.jeecg.modules.content.circle.entity.CircleAuditLog;
import org.jeecg.modules.content.circle.enums.CircleAuditActionEnum;
import org.jeecg.modules.content.circle.service.ICircleAuditLogService;
import org.jeecg.modules.content.circle.service.ICircleContentPinService;
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
 * 圈子内容置顶/精华业务编排服务测试。
 */
@ExtendWith(MockitoExtension.class)
class CircleContentPinBizServiceTest {

    @Mock
    private ICircleContentPinService circleContentPinService;

    @Mock
    private ICircleAuditLogService circleAuditLogService;

    @InjectMocks
    private CircleContentPinBizService circleContentPinBizService;

    private static final String TEST_CONTENT_ID = "content001";
    private static final String TEST_OPERATOR_ID = "admin001";
    private static final String TEST_CIRCLE_ID = "circle001";

    // ==================== pin ====================

    @Nested
    @DisplayName("pin - 置顶内容")
    class Pin {

        @Test
        @DisplayName("置顶 - 调用服务并写入审计日志")
        void pin_callsServiceAndWritesAuditLog() {
            circleContentPinBizService.pin(TEST_CONTENT_ID, TEST_OPERATOR_ID, TEST_CIRCLE_ID);

            verify(circleContentPinService).pinContent(TEST_CONTENT_ID);

            ArgumentCaptor<CircleAuditLog> logCaptor = ArgumentCaptor.forClass(CircleAuditLog.class);
            verify(circleAuditLogService).writeAuditLog(logCaptor.capture());
            CircleAuditLog capturedLog = logCaptor.getValue();
            assertThat(capturedLog.getCircleId()).isEqualTo(TEST_CIRCLE_ID);
            assertThat(capturedLog.getOperatorId()).isEqualTo(TEST_OPERATOR_ID);
            assertThat(capturedLog.getAction()).isEqualTo(CircleAuditActionEnum.PIN.getCode());
            assertThat(capturedLog.getTargetId()).isEqualTo(TEST_CONTENT_ID);
            assertThat(capturedLog.getTargetType()).isEqualTo("CONTENT");
            assertThat(capturedLog.getResult()).isEqualTo("SUCCESS");
        }
    }

    // ==================== unpin ====================

    @Nested
    @DisplayName("unpin - 取消置顶")
    class Unpin {

        @Test
        @DisplayName("取消置顶 - 调用服务并写入审计日志")
        void unpin_callsServiceAndWritesAuditLog() {
            circleContentPinBizService.unpin(TEST_CONTENT_ID, TEST_OPERATOR_ID, TEST_CIRCLE_ID);

            verify(circleContentPinService).unpinContent(TEST_CONTENT_ID);

            ArgumentCaptor<CircleAuditLog> logCaptor = ArgumentCaptor.forClass(CircleAuditLog.class);
            verify(circleAuditLogService).writeAuditLog(logCaptor.capture());
            CircleAuditLog capturedLog = logCaptor.getValue();
            assertThat(capturedLog.getAction()).isEqualTo(CircleAuditActionEnum.UNPIN.getCode());
            assertThat(capturedLog.getTargetId()).isEqualTo(TEST_CONTENT_ID);
        }
    }

    // ==================== feature ====================

    @Nested
    @DisplayName("feature - 设为精华")
    class Feature {

        @Test
        @DisplayName("设精华 - 调用服务并写入审计日志")
        void feature_callsServiceAndWritesAuditLog() {
            circleContentPinBizService.feature(TEST_CONTENT_ID, TEST_OPERATOR_ID, TEST_CIRCLE_ID);

            verify(circleContentPinService).featureContent(TEST_CONTENT_ID);

            ArgumentCaptor<CircleAuditLog> logCaptor = ArgumentCaptor.forClass(CircleAuditLog.class);
            verify(circleAuditLogService).writeAuditLog(logCaptor.capture());
            CircleAuditLog capturedLog = logCaptor.getValue();
            assertThat(capturedLog.getAction()).isEqualTo(CircleAuditActionEnum.FEATURE.getCode());
            assertThat(capturedLog.getTargetId()).isEqualTo(TEST_CONTENT_ID);
        }
    }

    // ==================== unfeature ====================

    @Nested
    @DisplayName("unfeature - 取消精华")
    class Unfeature {

        @Test
        @DisplayName("取消精华 - 调用服务并写入审计日志")
        void unfeature_callsServiceAndWritesAuditLog() {
            circleContentPinBizService.unfeature(TEST_CONTENT_ID, TEST_OPERATOR_ID, TEST_CIRCLE_ID);

            verify(circleContentPinService).unfeatureContent(TEST_CONTENT_ID);

            ArgumentCaptor<CircleAuditLog> logCaptor = ArgumentCaptor.forClass(CircleAuditLog.class);
            verify(circleAuditLogService).writeAuditLog(logCaptor.capture());
            CircleAuditLog capturedLog = logCaptor.getValue();
            assertThat(capturedLog.getAction()).isEqualTo(CircleAuditActionEnum.UNFEATURE.getCode());
            assertThat(capturedLog.getTargetId()).isEqualTo(TEST_CONTENT_ID);
        }
    }
}
