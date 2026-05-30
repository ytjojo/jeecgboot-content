package org.jeecg.modules.content.channel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.content.channel.entity.ChannelReview;
import org.jeecg.modules.content.channel.enums.ReviewResult;
import org.jeecg.modules.content.channel.mapper.ChannelReviewMapper;
import org.jeecg.modules.content.channel.service.ChannelReviewService;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class ChannelReviewServiceImpl extends ServiceImpl<ChannelReviewMapper, ChannelReview>
    implements ChannelReviewService {

    @Override
    public ChannelReview createReview(String channelId, String reviewerId, ReviewResult result, String reason) {
        ChannelReview review = new ChannelReview();
        review.setChannelId(channelId);
        review.setReviewerId(reviewerId);
        review.setResult(result);
        review.setReason(reason);
        review.setCreateTime(new Date());
        baseMapper.insert(review);
        return review;
    }

    @Override
    public List<ChannelReview> listReviewsByChannelId(String channelId) {
        return baseMapper.selectList(
            new LambdaQueryWrapper<ChannelReview>()
                .eq(ChannelReview::getChannelId, channelId)
                .orderByDesc(ChannelReview::getCreateTime)
        );
    }
}
