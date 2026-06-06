package org.jeecg.modules.content.channel.biz;

import org.jeecg.modules.content.channel.req.review.ChannelReviewReq;
import org.jeecg.modules.content.channel.vo.review.ReviewStatsVO;

public interface ChannelReviewBiz {
    void review(ChannelReviewReq req, String reviewerId);
    ReviewStatsVO getReviewStats(String channelId);
}
