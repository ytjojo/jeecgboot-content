package org.jeecg.modules.content.channel.biz;

import org.jeecg.modules.content.channel.req.review.ChannelReviewReq;

public interface ChannelReviewBiz {
    void review(ChannelReviewReq req, String reviewerId);
}
