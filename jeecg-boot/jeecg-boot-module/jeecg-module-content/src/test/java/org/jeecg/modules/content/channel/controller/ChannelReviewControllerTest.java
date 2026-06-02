package org.jeecg.modules.content.channel.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.content.channel.biz.ChannelMergeBiz;
import org.jeecg.modules.content.channel.entity.ChannelReview;
import org.jeecg.modules.content.channel.req.ChannelReviewActionReq;
import org.jeecg.modules.content.channel.service.IChannelReviewService;
import org.jeecg.modules.content.channel.vo.ChannelReviewVO;
import org.jeecg.modules.content.user.service.IContentNotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * 频道审核控制器测试
 * 验证审核列表 + 审核操作（含合并后置动作 + 通知）的行为
 */
@ExtendWith(MockitoExtension.class)
class ChannelReviewControllerTest {

    @Mock
    private IChannelReviewService reviewService;
    @Mock
    private IContentNotificationService notificationService;
    @Mock
    private ChannelMergeBiz mergeBiz;

    @InjectMocks
    private ChannelReviewController controller;

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
        verify(mergeBiz).executeMerge("src", "tgt", "current-user-id");
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
        doThrow(new RuntimeException("conflict")).when(mergeBiz).executeMerge("src", "tgt", "current-user-id");

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
