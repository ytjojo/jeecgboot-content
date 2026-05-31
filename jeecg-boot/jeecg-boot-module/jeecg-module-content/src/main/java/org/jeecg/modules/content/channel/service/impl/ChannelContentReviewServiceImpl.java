package org.jeecg.modules.content.channel.service.impl;

import org.jeecg.modules.content.channel.entity.ChannelContentReview;
import org.jeecg.modules.content.channel.mapper.ChannelContentReviewMapper;
import org.jeecg.modules.content.channel.service.ChannelContentReviewService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import java.util.Date;

@Service
public class ChannelContentReviewServiceImpl implements ChannelContentReviewService {

    @Resource
    private ChannelContentReviewMapper reviewMapper;

    @Override
    public void approve(String reviewId, String reviewerId) {
        ChannelContentReview review = reviewMapper.selectById(reviewId);
        if (review == null) {
            throw new IllegalArgumentException("审核记录不存在: " + reviewId);
        }
        review.setReviewStatus("APPROVED");
        review.setReviewerId(reviewerId);
        review.setReviewTime(new Date());
        reviewMapper.updateById(review);
    }

    @Override
    public void reject(String reviewId, String reviewerId, String reason) {
        ChannelContentReview review = reviewMapper.selectById(reviewId);
        if (review == null) {
            throw new IllegalArgumentException("审核记录不存在: " + reviewId);
        }
        review.setReviewStatus("REJECTED");
        review.setReviewerId(reviewerId);
        review.setReviewTime(new Date());
        review.setRejectReason(reason);
        reviewMapper.updateById(review);
    }
}
