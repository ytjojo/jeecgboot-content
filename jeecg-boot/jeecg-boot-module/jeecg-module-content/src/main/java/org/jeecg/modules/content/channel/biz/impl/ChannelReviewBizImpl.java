package org.jeecg.modules.content.channel.biz.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.jeecg.modules.content.channel.biz.ChannelReviewBiz;
import org.jeecg.modules.content.channel.entity.ChannelContentReview;
import org.jeecg.modules.content.channel.mapper.ChannelContentReviewMapper;
import org.jeecg.modules.content.channel.req.review.ChannelReviewReq;
import org.jeecg.modules.content.channel.service.ChannelContentReviewService;
import org.jeecg.modules.content.channel.vo.review.ReviewStatsVO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;

@Service
public class ChannelReviewBizImpl implements ChannelReviewBiz {

    @Resource
    private ChannelContentReviewService reviewService;

    @Resource
    private ChannelContentReviewMapper reviewMapper;

    @Override
    public void review(ChannelReviewReq req, String reviewerId) {
        if ("APPROVE".equals(req.getAction())) {
            reviewService.approve(req.getReviewId(), reviewerId);
        } else {
            reviewService.reject(req.getReviewId(), reviewerId, req.getRejectReason());
        }
    }

    @Override
    public ReviewStatsVO getReviewStats(String channelId) {
        // 待审核数量
        Long pendingCount = reviewMapper.selectCount(buildBaseQuery(channelId, "PENDING"));

        // 超时未审核(超过24小时)
        Date timeoutThreshold = new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000);
        LambdaQueryWrapper<ChannelContentReview> timeoutQuery = buildBaseQuery(channelId, "PENDING");
        timeoutQuery.lt(ChannelContentReview::getCreateTime, timeoutThreshold);
        Long timeoutCount = reviewMapper.selectCount(timeoutQuery);

        // 今日开始时间
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date todayStart = cal.getTime();

        // 今日已通过
        LambdaQueryWrapper<ChannelContentReview> approvedQuery = buildBaseQuery(channelId, "APPROVED");
        approvedQuery.ge(ChannelContentReview::getReviewTime, todayStart);
        Long todayApprovedCount = reviewMapper.selectCount(approvedQuery);

        // 今日已拒绝
        LambdaQueryWrapper<ChannelContentReview> rejectedQuery = buildBaseQuery(channelId, "REJECTED");
        rejectedQuery.ge(ChannelContentReview::getReviewTime, todayStart);
        Long todayRejectedCount = reviewMapper.selectCount(rejectedQuery);

        return ReviewStatsVO.builder()
                .pendingCount(pendingCount)
                .timeoutCount(timeoutCount)
                .todayApprovedCount(todayApprovedCount)
                .todayRejectedCount(todayRejectedCount)
                .build();
    }

    private LambdaQueryWrapper<ChannelContentReview> buildBaseQuery(String channelId, String status) {
        LambdaQueryWrapper<ChannelContentReview> query = new LambdaQueryWrapper<>();
        if (channelId != null && !channelId.isEmpty()) {
            query.eq(ChannelContentReview::getChannelId, channelId);
        }
        query.eq(ChannelContentReview::getReviewStatus, status);
        return query;
    }
}
