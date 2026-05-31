package org.jeecg.modules.content.channel.service;

public interface ChannelContentReviewService {
    void approve(String reviewId, String reviewerId);
    void reject(String reviewId, String reviewerId, String reason);
}
