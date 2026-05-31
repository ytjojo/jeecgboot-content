package org.jeecg.modules.content.channel.biz;

import org.jeecg.modules.content.channel.biz.impl.ChannelReviewBizImpl;
import org.jeecg.modules.content.channel.req.review.ChannelReviewReq;
import org.jeecg.modules.content.channel.service.ChannelContentReviewService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChannelReviewBizTest {

    @InjectMocks
    private ChannelReviewBizImpl biz;

    @Mock
    private ChannelContentReviewService reviewService;

    @Test
    void shouldCallApproveWhenActionIsApprove() {
        ChannelReviewReq req = new ChannelReviewReq();
        req.setReviewId("review-1");
        req.setAction("APPROVE");

        biz.review(req, "admin-1");

        verify(reviewService).approve("review-1", "admin-1");
        verify(reviewService, never()).reject(anyString(), anyString(), anyString());
    }

    @Test
    void shouldCallRejectWhenActionIsReject() {
        ChannelReviewReq req = new ChannelReviewReq();
        req.setReviewId("review-1");
        req.setAction("REJECT");
        req.setRejectReason("内容不符合主题");

        biz.review(req, "admin-1");

        verify(reviewService).reject("review-1", "admin-1", "内容不符合主题");
        verify(reviewService, never()).approve(anyString(), anyString());
    }
}
