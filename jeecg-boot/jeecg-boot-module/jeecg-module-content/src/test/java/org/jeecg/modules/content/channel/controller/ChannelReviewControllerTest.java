package org.jeecg.modules.content.channel.controller;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.modules.content.channel.biz.ChannelMergeBiz;
import org.jeecg.modules.content.channel.entity.ChannelReview;
import org.jeecg.modules.content.channel.req.ChannelReviewActionReq;
import org.jeecg.modules.content.channel.service.IChannelReviewService;
import org.jeecg.modules.content.channel.vo.ChannelReviewVO;
import org.jeecg.modules.content.user.service.IContentNotificationService;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChannelReviewControllerTest {

    private static final String TEST_USER_ID = "test-admin-user";
    private static final String TEST_USERNAME = "admin";

    @Mock
    private IChannelReviewService reviewService;
    @Mock
    private IContentNotificationService notificationService;
    @Mock
    private ChannelMergeBiz mergeBiz;

    @InjectMocks
    private ChannelReviewController controller;

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
    void should_list_reviews() {
        Page<ChannelReview> page = new Page<>(1, 10);
        page.setRecords(Collections.emptyList());
        when(reviewService.page(any(Page.class), any())).thenReturn(page);

        Result<Page<ChannelReviewVO>> result = controller.listReviews(1, 10, "pending", "merge");

        assertThat(result.isSuccess()).isTrue();
        verify(reviewService).page(any(Page.class), any());
    }

    @Test
    void should_get_review_detail() {
        ChannelReview review = new ChannelReview();
        review.setReviewId("r1");
        review.setChannelId("ch1");
        when(reviewService.getById("r1")).thenReturn(review);

        Result<ChannelReviewVO> result = controller.getReviewDetail("r1");

        assertThat(result.isSuccess()).isTrue();
    }

    @Test
    void should_reject_invalid_action() {
        ChannelReviewActionReq req = new ChannelReviewActionReq();
        req.setReviewId("r1");
        req.setAction("invalid");

        Result<Void> result = controller.reviewAction(req);

        assertThat(result.getCode()).isEqualTo(500);
        assertThat(result.getMessage()).contains("无效的审核操作");
        verifyNoInteractions(reviewService);
    }

    @Test
    void should_error_when_review_not_found() {
        ChannelReviewActionReq req = new ChannelReviewActionReq();
        req.setReviewId("r1");
        req.setAction("approved");
        when(reviewService.getById("r1")).thenReturn(null);

        Result<Void> result = controller.reviewAction(req);

        assertThat(result.getCode()).isEqualTo(500);
        assertThat(result.getMessage()).contains("审核记录不存在");
    }

    @Test
    void should_approve_review_and_notify() {
        ChannelReviewActionReq req = new ChannelReviewActionReq();
        req.setReviewId("r1");
        req.setAction("approved");
        req.setReason("ok");

        ChannelReview review = new ChannelReview();
        review.setReviewId("r1");
        review.setChannelId("ch1");
        review.setApplicantId("user1");
        review.setReviewType("normal");
        when(reviewService.getById("r1")).thenReturn(review);

        Result<Void> result = controller.reviewAction(req);

        assertThat(result.isSuccess()).isTrue();
        verify(reviewService).updateById(review);
        verify(notificationService).sendNotification(eq("user1"), eq("channel_review"), any(), any());
        verify(mergeBiz, never()).executeMerge(any(), any(), any());
    }

    @Test
    void should_execute_merge_after_merge_review_approved() {
        ChannelReviewActionReq req = new ChannelReviewActionReq();
        req.setReviewId("r1");
        req.setAction("approved");

        ChannelReview review = new ChannelReview();
        review.setReviewId("r1");
        review.setChannelId("src");
        review.setTargetChannelId("tgt");
        review.setReviewType("merge");
        review.setApplicantId("user1");
        when(reviewService.getById("r1")).thenReturn(review);

        Result<Void> result = controller.reviewAction(req);

        assertThat(result.isSuccess()).isTrue();
        verify(mergeBiz).executeMerge("src", "tgt", TEST_USER_ID);
    }

    @Test
    void should_return_error_when_merge_execution_fails() {
        ChannelReviewActionReq req = new ChannelReviewActionReq();
        req.setReviewId("r1");
        req.setAction("approved");

        ChannelReview review = new ChannelReview();
        review.setReviewId("r1");
        review.setChannelId("src");
        review.setTargetChannelId("tgt");
        review.setReviewType("merge");
        when(reviewService.getById("r1")).thenReturn(review);
        doThrow(new RuntimeException("conflict")).when(mergeBiz).executeMerge("src", "tgt", TEST_USER_ID);

        Result<Void> result = controller.reviewAction(req);

        assertThat(result.getCode()).isEqualTo(500);
        assertThat(result.getMessage()).contains("合并执行失败");
    }

    @Test
    void should_reject_review() {
        ChannelReviewActionReq req = new ChannelReviewActionReq();
        req.setReviewId("r1");
        req.setAction("rejected");
        req.setReason("violation");

        ChannelReview review = new ChannelReview();
        review.setReviewId("r1");
        review.setChannelId("ch1");
        review.setApplicantId("user1");
        when(reviewService.getById("r1")).thenReturn(review);

        Result<Void> result = controller.reviewAction(req);

        assertThat(result.isSuccess()).isTrue();
        verify(notificationService).sendNotification(eq("user1"), eq("channel_review"), any(), any());
    }
}
