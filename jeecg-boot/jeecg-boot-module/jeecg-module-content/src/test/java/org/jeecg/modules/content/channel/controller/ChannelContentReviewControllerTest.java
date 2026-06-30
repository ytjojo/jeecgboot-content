package org.jeecg.modules.content.channel.controller;

import com.alibaba.fastjson2.JSON;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.modules.content.channel.biz.ChannelReviewBiz;
import org.jeecg.modules.content.channel.req.review.ChannelReviewReq;
import org.jeecg.modules.content.channel.vo.review.ReviewStatsVO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * 频道内容审核控制器测试
 */
@ExtendWith(MockitoExtension.class)
class ChannelContentReviewControllerTest {

    private static final String TEST_USER_ID = "test-user-id";
    private static final String TEST_USERNAME = "testuser";

    @Mock
    private ChannelReviewBiz channelReviewBiz;

    @InjectMocks
    private ChannelContentReviewController controller;

    @BeforeEach
    void setUp() {
        LoginUser loginUser = new LoginUser();
        loginUser.setId(TEST_USER_ID);
        loginUser.setUsername(TEST_USERNAME);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                JSON.toJSONString(loginUser), null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void should_review_content() {
        ChannelReviewReq req = new ChannelReviewReq();
        req.setReviewId("r1");
        req.setAction("approved");

        Result<Void> result = controller.review(req);

        assertThat(result.isSuccess()).isTrue();
        verify(channelReviewBiz).review(req, TEST_USER_ID);
    }

    @Test
    void should_pass_reject_reason_through() {
        ChannelReviewReq req = new ChannelReviewReq();
        req.setReviewId("r2");
        req.setAction("rejected");
        req.setRejectReason("violation");

        Result<Void> result = controller.review(req);

        assertThat(result.isSuccess()).isTrue();
        verify(channelReviewBiz).review(req, TEST_USER_ID);
    }

    @Test
    void should_get_review_stats() {
        ReviewStatsVO stats = ReviewStatsVO.builder()
                .pendingCount(5L)
                .timeoutCount(2L)
                .todayApprovedCount(3L)
                .todayRejectedCount(1L)
                .build();
        when(channelReviewBiz.getReviewStats("ch1")).thenReturn(stats);

        Result<ReviewStatsVO> result = controller.getReviewStats("ch1");

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getResult().getPendingCount()).isEqualTo(5L);
        verify(channelReviewBiz).getReviewStats("ch1");
    }
}
