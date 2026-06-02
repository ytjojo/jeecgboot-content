package org.jeecg.modules.content.circle.task;

import org.jeecg.modules.content.circle.entity.CircleJoinRequest;
import org.jeecg.modules.content.circle.service.ICircleJoinReviewService;
import org.jeecg.modules.content.user.service.IContentNotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CircleJoinRequestTimeoutTask")
class CircleJoinRequestTimeoutTaskTest {

    @Mock
    private ICircleJoinReviewService joinReviewService;

    @Mock
    private IContentNotificationService notificationService;

    private CircleJoinRequestTimeoutTask task;

    @BeforeEach
    void setUp() {
        task = new CircleJoinRequestTimeoutTask(joinReviewService, notificationService);
    }

    private CircleJoinRequest createTimedOutRequest(String id, String createBy) {
        CircleJoinRequest request = new CircleJoinRequest();
        request.setId(id);
        request.setCreateBy(createBy);
        return request;
    }

    // ==================== empty list ====================

    @Nested
    @DisplayName("remindTimedOutRequests - empty list")
    class EmptyList {

        @Test
        @DisplayName("no timed-out requests - short-circuits and never calls notification")
        void emptyList_shortCircuitsAndNeverNotifies() {
            when(joinReviewService.getTimedOutRequests()).thenReturn(Collections.emptyList());

            task.remindTimedOutRequests();

            verify(joinReviewService, times(1)).getTimedOutRequests();
            verifyNoInteractions(notificationService);
        }
    }

    // ==================== non-empty list ====================

    @Nested
    @DisplayName("remindTimedOutRequests - non-empty list")
    class NonEmptyList {

        @Test
        @DisplayName("two timed-out requests - calls notificationService once per request with 4 args")
        void twoRequests_callsNotificationOncePerRequest() {
            CircleJoinRequest r1 = createTimedOutRequest("req_001", "u_admin_01");
            CircleJoinRequest r2 = createTimedOutRequest("req_002", "u_admin_02");
            when(joinReviewService.getTimedOutRequests()).thenReturn(Arrays.asList(r1, r2));

            task.remindTimedOutRequests();

            verify(notificationService).sendNotification(
                    eq("u_admin_01"),
                    eq("JOIN_REQUEST_TIMEOUT"),
                    eq("加入申请超时提醒"),
                    eq("圈子有加入申请超过 3 天未处理，请及时审核"));
            verify(notificationService).sendNotification(
                    eq("u_admin_02"),
                    eq("JOIN_REQUEST_TIMEOUT"),
                    eq("加入申请超时提醒"),
                    eq("圈子有加入申请超过 3 天未处理，请及时审核"));
            verify(notificationService, times(2))
                    .sendNotification(anyString(), anyString(), anyString(), anyString());
            verify(joinReviewService, times(1)).getTimedOutRequests();
        }

        @Test
        @DisplayName("single request - calls sendNotification exactly once")
        void singleRequest_callsSendNotificationOnce() {
            CircleJoinRequest r1 = createTimedOutRequest("req_001", "u_admin_01");
            when(joinReviewService.getTimedOutRequests()).thenReturn(Collections.singletonList(r1));

            task.remindTimedOutRequests();

            verify(notificationService, times(1))
                    .sendNotification(eq("u_admin_01"), eq("JOIN_REQUEST_TIMEOUT"),
                            eq("加入申请超时提醒"), eq("圈子有加入申请超过 3 天未处理，请及时审核"));
        }
    }

    // ==================== partial failure isolation ====================

    @Nested
    @DisplayName("remindTimedOutRequests - partial failure isolation")
    class PartialFailure {

        @Test
        @DisplayName("first sendNotification throws - loop continues, second still notified")
        void firstFails_secondStillNotified() {
            CircleJoinRequest r1 = createTimedOutRequest("req_001", "u_admin_01");
            CircleJoinRequest r2 = createTimedOutRequest("req_002", "u_admin_02");
            CircleJoinRequest r3 = createTimedOutRequest("req_003", "u_admin_03");
            when(joinReviewService.getTimedOutRequests()).thenReturn(Arrays.asList(r1, r2, r3));

            doThrow(new RuntimeException("notification-channel down"))
                    .when(notificationService)
                    .sendNotification(eq("u_admin_02"), anyString(), anyString(), anyString());

            task.remindTimedOutRequests();

            // first and third still called
            verify(notificationService).sendNotification(
                    eq("u_admin_01"), anyString(), anyString(), anyString());
            verify(notificationService).sendNotification(
                    eq("u_admin_03"), anyString(), anyString(), anyString());
            // total attempts = 3 (one of which threw)
            verify(notificationService, times(3))
                    .sendNotification(anyString(), anyString(), anyString(), anyString());
            // joinReviewService called only once
            verify(joinReviewService, times(1)).getTimedOutRequests();
        }

        @Test
        @DisplayName("all notifications fail - loop completes without propagating exception")
        void allFail_loopCompletesSilently() {
            CircleJoinRequest r1 = createTimedOutRequest("req_001", "u_admin_01");
            CircleJoinRequest r2 = createTimedOutRequest("req_002", "u_admin_02");
            when(joinReviewService.getTimedOutRequests()).thenReturn(Arrays.asList(r1, r2));

            doThrow(new RuntimeException("down"))
                    .when(notificationService)
                    .sendNotification(anyString(), anyString(), anyString(), anyString());

            // Should not throw - task swallows exceptions per item
            task.remindTimedOutRequests();

            verify(notificationService, times(2))
                    .sendNotification(anyString(), anyString(), anyString(), anyString());
        }
    }
}
