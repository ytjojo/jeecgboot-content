package org.jeecg.modules.content.channel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.content.channel.entity.ChannelReview;
import org.jeecg.modules.content.channel.enums.ReviewResult;
import org.jeecg.modules.content.channel.mapper.ChannelReviewMapper;
import org.jeecg.modules.content.channel.service.IChannelReviewService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ChannelReviewServiceImpl extends ServiceImpl<ChannelReviewMapper, ChannelReview>
    implements IChannelReviewService {

    @Override
    public ChannelReview createReview(String channelId, String reviewerId, ReviewResult result, String reason) {
        ChannelReview review = new ChannelReview()
                .setReviewId(UUID.randomUUID().toString().replace("-", ""))
                .setChannelId(channelId)
                .setReviewerId(reviewerId)
                .setReviewReason(reason)
                .setReviewTime(LocalDateTime.now())
                .setTimeoutFlag(0)
                .setCreatedTime(LocalDateTime.now())
                .setUpdatedTime(LocalDateTime.now());

        // 映射 ReviewResult 枚举到状态字符串
        String status = switch (result) {
            case PASS -> "approved";
            case REJECT -> "rejected";
            case RETURN_FOR_EDIT -> "returned";
        };
        review.setStatus(status);

        save(review);
        return review;
    }

    @Override
    public ChannelReview submitReview(String channelId, String reviewType, String applicantId, String reason) {
        ChannelReview review = new ChannelReview()
                .setReviewId(UUID.randomUUID().toString().replace("-", ""))
                .setChannelId(channelId)
                .setReviewType(reviewType)
                .setStatus("pending")
                .setApplicantId(applicantId)
                .setReviewReason(reason)
                .setSubmitTime(LocalDateTime.now())
                .setTimeoutFlag(0)
                .setCreatedTime(LocalDateTime.now())
                .setUpdatedTime(LocalDateTime.now());

        save(review);
        return review;
    }

    @Override
    public List<ChannelReview> listReviewsByChannelId(String channelId) {
        return list(new LambdaQueryWrapper<ChannelReview>()
                .eq(ChannelReview::getChannelId, channelId)
                .orderByDesc(ChannelReview::getSubmitTime));
    }
}
