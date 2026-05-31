package org.jeecg.modules.content.channel.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.content.channel.entity.ChannelReview;
import org.jeecg.modules.content.channel.enums.ReviewResult;

import java.util.List;

public interface IChannelReviewService extends IService<ChannelReview> {

    ChannelReview createReview(String channelId, String reviewerId, ReviewResult result, String reason);

    ChannelReview submitReview(String channelId, String reviewType, String applicantId, String reason);

    List<ChannelReview> listReviewsByChannelId(String channelId);
}
