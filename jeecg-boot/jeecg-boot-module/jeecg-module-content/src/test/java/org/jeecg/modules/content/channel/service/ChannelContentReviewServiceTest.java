package org.jeecg.modules.content.channel.service;

import org.jeecg.modules.content.channel.entity.ChannelContentReview;
import org.jeecg.modules.content.channel.mapper.ChannelContentReviewMapper;
import org.jeecg.modules.content.channel.service.impl.ChannelContentReviewServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChannelContentReviewServiceTest {

    @InjectMocks
    private ChannelContentReviewServiceImpl service;

    @Mock
    private ChannelContentReviewMapper reviewMapper;

    @Test
    void approve_shouldUpdateStatusToApproved() {
        ChannelContentReview review = new ChannelContentReview();
        review.setId("review-1");
        review.setReviewStatus("PENDING");
        when(reviewMapper.selectById("review-1")).thenReturn(review);
        when(reviewMapper.updateById(any(ChannelContentReview.class))).thenReturn(1);

        service.approve("review-1", "admin-1");
        assertEquals("APPROVED", review.getReviewStatus());
        assertEquals("admin-1", review.getReviewerId());
        assertNotNull(review.getReviewTime());
    }

    @Test
    void reject_shouldUpdateStatusAndReason() {
        ChannelContentReview review = new ChannelContentReview();
        review.setId("review-1");
        review.setReviewStatus("PENDING");
        when(reviewMapper.selectById("review-1")).thenReturn(review);
        when(reviewMapper.updateById(any(ChannelContentReview.class))).thenReturn(1);

        service.reject("review-1", "admin-1", "内容不符合主题");
        assertEquals("REJECTED", review.getReviewStatus());
        assertEquals("内容不符合主题", review.getRejectReason());
    }
}
