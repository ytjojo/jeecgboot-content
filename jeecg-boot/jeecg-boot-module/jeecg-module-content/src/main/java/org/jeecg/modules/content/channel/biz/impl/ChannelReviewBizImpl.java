package org.jeecg.modules.content.channel.biz.impl;

import org.jeecg.modules.content.channel.biz.ChannelReviewBiz;
import org.jeecg.modules.content.channel.req.review.ChannelReviewReq;
import org.jeecg.modules.content.channel.service.ChannelContentReviewService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class ChannelReviewBizImpl implements ChannelReviewBiz {

    @Resource
    private ChannelContentReviewService reviewService;

    @Override
    public void review(ChannelReviewReq req, String reviewerId) {
        if ("APPROVE".equals(req.getAction())) {
            reviewService.approve(req.getReviewId(), reviewerId);
        } else {
            reviewService.reject(req.getReviewId(), reviewerId, req.getRejectReason());
        }
    }
}
