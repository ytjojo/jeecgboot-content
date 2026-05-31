package org.jeecg.modules.content.circle.service;

import org.jeecg.modules.content.circle.entity.CircleJoinRequest;
import org.jeecg.modules.content.circle.enums.CircleJoinRequestStatusEnum;
import org.jeecg.modules.content.circle.mapper.CircleJoinRequestMapper;
import org.jeecg.modules.content.circle.service.impl.CircleJoinReviewServiceImpl;
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
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 圈子加入申请审核服务测试。
 */
@ExtendWith(MockitoExtension.class)
class CircleJoinReviewServiceTest {

    @Mock
    private CircleJoinRequestMapper circleJoinRequestMapper;

    @Mock
    private IContentNotificationService contentNotificationService;

    @InjectMocks
    private CircleJoinReviewServiceImpl circleJoinReviewService;

    private static final String TEST_REQUEST_ID = "req001";
    private static final String TEST_CIRCLE_ID = "circle001";
    private static final String TEST_USER_ID = "user001";
    private static final String TEST_OPERATOR_ID = "admin001";

    @BeforeEach
    void setUp() {
        // ServiceImpl 的 baseMapper 需要手动注入，Mockito 不会自动设置
        ReflectionTestUtils.setField(circleJoinReviewService, "baseMapper", circleJoinRequestMapper);
    }

    private CircleJoinRequest createPendingRequest() {
        CircleJoinRequest request = new CircleJoinRequest();
        request.setId(TEST_REQUEST_ID);
        request.setCircleId(TEST_CIRCLE_ID);
        request.setUserId(TEST_USER_ID);
        request.setStatus(CircleJoinRequestStatusEnum.PENDING.getCode());
        return request;
    }

    // ==================== approve ====================

    @Nested
    @DisplayName("approve - 批准加入申请")
    class Approve {

        @Test
        @DisplayName("批准申请 - 状态设为APPROVED并发送通知")
        void approve_setsStatusToApproved() {
            // given
            CircleJoinRequest request = createPendingRequest();
            when(circleJoinRequestMapper.selectById(TEST_REQUEST_ID)).thenReturn(request);
            when(circleJoinRequestMapper.updateById(any(CircleJoinRequest.class))).thenReturn(1);

            // when
            circleJoinReviewService.approve(TEST_REQUEST_ID, TEST_OPERATOR_ID);

            // then
            ArgumentCaptor<CircleJoinRequest> captor = ArgumentCaptor.forClass(CircleJoinRequest.class);
            verify(circleJoinRequestMapper).updateById(captor.capture());
            CircleJoinRequest updated = captor.getValue();
            assertThat(updated.getStatus()).isEqualTo(CircleJoinRequestStatusEnum.APPROVED.getCode());
            assertThat(updated.getOperatorId()).isEqualTo(TEST_OPERATOR_ID);
            assertThat(updated.getOperateTime()).isNotNull();

            verify(contentNotificationService).sendNotification(
                    eq(TEST_USER_ID), eq("JOIN_APPROVED"), anyString(), anyString());
        }
    }

    // ==================== reject ====================

    @Nested
    @DisplayName("reject - 拒绝加入申请")
    class Reject {

        @Test
        @DisplayName("拒绝申请 - 状态设为REJECTED并附带拒绝原因")
        void reject_setsStatusToRejectedWithReason() {
            // given
            CircleJoinRequest request = createPendingRequest();
            when(circleJoinRequestMapper.selectById(TEST_REQUEST_ID)).thenReturn(request);
            when(circleJoinRequestMapper.updateById(any(CircleJoinRequest.class))).thenReturn(1);
            String reason = "不符合加入条件";

            // when
            circleJoinReviewService.reject(TEST_REQUEST_ID, TEST_OPERATOR_ID, reason);

            // then
            ArgumentCaptor<CircleJoinRequest> captor = ArgumentCaptor.forClass(CircleJoinRequest.class);
            verify(circleJoinRequestMapper).updateById(captor.capture());
            CircleJoinRequest updated = captor.getValue();
            assertThat(updated.getStatus()).isEqualTo(CircleJoinRequestStatusEnum.REJECTED.getCode());
            assertThat(updated.getOperatorId()).isEqualTo(TEST_OPERATOR_ID);
            assertThat(updated.getOperateTime()).isNotNull();
            assertThat(updated.getRejectReason()).isEqualTo(reason);

            verify(contentNotificationService).sendNotification(
                    eq(TEST_USER_ID), eq("JOIN_REJECTED"), anyString(), contains(reason));
        }
    }

    // ==================== getPendingRequests ====================

    @Nested
    @DisplayName("getPendingRequests - 查询待审核申请")
    class GetPendingRequests {

        @Test
        @DisplayName("查询待审核申请 - 返回PENDING状态的申请列表")
        void getPendingRequests_returnsOnlyPending() {
            // given
            List<CircleJoinRequest> expected = Arrays.asList(createPendingRequest());
            when(circleJoinRequestMapper.selectPendingByCircleId(TEST_CIRCLE_ID)).thenReturn(expected);

            // when
            List<CircleJoinRequest> result = circleJoinReviewService.getPendingRequests(TEST_CIRCLE_ID);

            // then
            assertThat(result).hasSize(1);
            assertThat(result).isEqualTo(expected);
            verify(circleJoinRequestMapper).selectPendingByCircleId(TEST_CIRCLE_ID);
        }
    }

    // ==================== getTimedOutRequests ====================

    @Nested
    @DisplayName("getTimedOutRequests - 查询超时申请")
    class GetTimedOutRequests {

        @Test
        @DisplayName("查询超时申请 - 返回超过3天未处理的申请")
        void getTimedOutRequests_returnsOverdueRequests() {
            // given
            CircleJoinRequest timedOut = createPendingRequest();
            timedOut.setCreateTime(new Date(System.currentTimeMillis() - 4 * 86400000L));
            List<CircleJoinRequest> expected = Arrays.asList(timedOut);
            when(circleJoinRequestMapper.selectTimedOutRequests()).thenReturn(expected);

            // when
            List<CircleJoinRequest> result = circleJoinReviewService.getTimedOutRequests();

            // then
            assertThat(result).hasSize(1);
            assertThat(result).isEqualTo(expected);
            verify(circleJoinRequestMapper).selectTimedOutRequests();
        }
    }
}
