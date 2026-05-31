package org.jeecg.modules.content.circle.biz;

import org.jeecg.modules.content.circle.entity.CircleAnnouncement;
import org.jeecg.modules.content.circle.entity.CircleAuditLog;
import org.jeecg.modules.content.circle.enums.CircleAuditActionEnum;
import org.jeecg.modules.content.circle.service.ICircleAnnouncementService;
import org.jeecg.modules.content.circle.service.ICircleAuditLogService;
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
 * 圈子公告业务编排服务测试。
 */
@ExtendWith(MockitoExtension.class)
class CircleAnnouncementBizServiceTest {

    @Mock
    private ICircleAnnouncementService circleAnnouncementService;

    @Mock
    private ICircleAuditLogService circleAuditLogService;

    @InjectMocks
    private CircleAnnouncementBizService circleAnnouncementBizService;

    private static final String TEST_CIRCLE_ID = "circle001";
    private static final String TEST_OPERATOR_ID = "admin001";
    private static final String TEST_CONTENT = "测试公告内容";

    private CircleAnnouncement createTestAnnouncement() {
        CircleAnnouncement announcement = new CircleAnnouncement();
        announcement.setCircleId(TEST_CIRCLE_ID);
        announcement.setContent(TEST_CONTENT);
        return announcement;
    }

    // ==================== publish ====================

    @Nested
    @DisplayName("publish - 发布公告")
    class Publish {

        @Test
        @DisplayName("发布公告 - 调用服务并写入审计日志")
        void publish_callsServiceAndWritesAuditLog() {
            CircleAnnouncement announcement = createTestAnnouncement();

            circleAnnouncementBizService.publish(announcement, TEST_OPERATOR_ID);

            verify(circleAnnouncementService).publish(announcement);

            ArgumentCaptor<CircleAuditLog> logCaptor = ArgumentCaptor.forClass(CircleAuditLog.class);
            verify(circleAuditLogService).writeAuditLog(logCaptor.capture());
            CircleAuditLog capturedLog = logCaptor.getValue();
            assertThat(capturedLog.getCircleId()).isEqualTo(TEST_CIRCLE_ID);
            assertThat(capturedLog.getOperatorId()).isEqualTo(TEST_OPERATOR_ID);
            assertThat(capturedLog.getAction()).isEqualTo(CircleAuditActionEnum.PUBLISH_ANNOUNCEMENT.getCode());
            assertThat(capturedLog.getTargetType()).isEqualTo("ANNOUNCEMENT");
            assertThat(capturedLog.getResult()).isEqualTo("SUCCESS");
        }
    }
}
