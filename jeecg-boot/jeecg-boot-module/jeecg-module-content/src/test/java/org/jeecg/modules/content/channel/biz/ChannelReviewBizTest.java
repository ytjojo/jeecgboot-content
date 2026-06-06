package org.jeecg.modules.content.channel.biz;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.jeecg.modules.content.channel.biz.impl.ChannelReviewBizImpl;
import org.jeecg.modules.content.channel.entity.ChannelContentReview;
import org.jeecg.modules.content.channel.mapper.ChannelContentReviewMapper;
import org.jeecg.modules.content.channel.req.review.ChannelReviewReq;
import org.jeecg.modules.content.channel.service.ChannelContentReviewService;
import org.jeecg.modules.content.channel.vo.review.ReviewStatsVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChannelReviewBizTest {

    @InjectMocks
    private ChannelReviewBizImpl biz;

    @Mock
    private ChannelContentReviewService reviewService;

    @Mock
    private ChannelContentReviewMapper reviewMapper;

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

    @Test
    void getReviewStats_shouldReturnStats() {
        when(reviewMapper.selectCount(any(LambdaQueryWrapper.class)))
                .thenReturn(5L)  // pending
                .thenReturn(2L)  // timeout
                .thenReturn(3L)  // today approved
                .thenReturn(1L); // today rejected

        ReviewStatsVO stats = biz.getReviewStats(null);

        assertEquals(5L, stats.getPendingCount());
        assertEquals(2L, stats.getTimeoutCount());
        assertEquals(3L, stats.getTodayApprovedCount());
        assertEquals(1L, stats.getTodayRejectedCount());
    }

    @Test
    void getReviewStats_shouldFilterByChannel() {
        when(reviewMapper.selectCount(any(LambdaQueryWrapper.class)))
                .thenReturn(2L)
                .thenReturn(0L)
                .thenReturn(1L)
                .thenReturn(0L);

        ReviewStatsVO stats = biz.getReviewStats("ch-1");

        assertEquals(2L, stats.getPendingCount());
    }
}
