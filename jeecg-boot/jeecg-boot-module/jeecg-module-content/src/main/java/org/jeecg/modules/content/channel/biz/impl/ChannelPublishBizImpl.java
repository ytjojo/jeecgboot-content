package org.jeecg.modules.content.channel.biz.impl;

import org.jeecg.modules.content.channel.biz.ChannelPublishBiz;
import org.jeecg.modules.content.channel.entity.ChannelContentPublish;
import org.jeecg.modules.content.channel.entity.ChannelContentReview;
import org.jeecg.modules.content.channel.mapper.ChannelContentPublishMapper;
import org.jeecg.modules.content.channel.mapper.ChannelContentReviewMapper;
import org.jeecg.modules.content.channel.req.publish.ChannelPublishReq;
import org.jeecg.modules.content.channel.service.ChannelContentPublishService;
import org.jeecg.modules.content.channel.service.ChannelPublishLimitService;
import org.jeecg.modules.content.channel.vo.publish.ChannelPublishResultVO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class ChannelPublishBizImpl implements ChannelPublishBiz {

    @Resource
    private ChannelContentPublishService publishService;

    @Resource
    private ChannelPublishLimitService limitService;

    @Resource
    private ChannelContentPublishMapper publishMapper;

    @Resource
    private ChannelContentReviewMapper reviewMapper;

    @Override
    public List<ChannelPublishResultVO> publish(ChannelPublishReq req) {
        List<ChannelPublishResultVO> results = new ArrayList<>();
        for (String channelId : req.getChannelIds()) {
            ChannelPublishResultVO result = new ChannelPublishResultVO();
            result.setChannelId(channelId);
            try {
                // TODO: 查询用户角色、禁言状态、黑名单状态、发布权限、限额配置
                String permissionResult = "ALLOW"; // placeholder
                if ("REJECT".equals(permissionResult)) {
                    result.setStatus("FAILED");
                    result.setFailReason("权限不足");
                } else if ("REVIEW".equals(permissionResult)) {
                    ChannelContentReview review = new ChannelContentReview();
                    review.setChannelId(channelId);
                    review.setContentId(req.getContentId());
                    review.setContentType(req.getContentType());
                    review.setReviewStatus("PENDING");
                    reviewMapper.insert(review);
                    result.setStatus("PENDING");
                } else {
                    ChannelContentPublish publish = new ChannelContentPublish();
                    publish.setChannelId(channelId);
                    publish.setContentId(req.getContentId());
                    publish.setContentType(req.getContentType());
                    publish.setPublishStatus("PUBLISHED");
                    publishMapper.insert(publish);
                    result.setStatus("PUBLISHED");
                }
            } catch (Exception e) {
                result.setStatus("FAILED");
                result.setFailReason(e.getMessage());
            }
            results.add(result);
        }
        return results;
    }
}
