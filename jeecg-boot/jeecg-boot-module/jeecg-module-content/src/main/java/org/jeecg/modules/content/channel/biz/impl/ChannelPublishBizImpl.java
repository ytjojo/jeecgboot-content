package org.jeecg.modules.content.channel.biz.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.jeecg.modules.content.channel.biz.ChannelPublishBiz;
import org.jeecg.modules.content.channel.entity.ChannelContentPublish;
import org.jeecg.modules.content.channel.entity.ChannelContentReview;
import org.jeecg.modules.content.channel.mapper.ChannelContentPublishMapper;
import org.jeecg.modules.content.channel.mapper.ChannelContentReviewMapper;
import org.jeecg.modules.content.channel.req.publish.ChannelAddExistingContentReq;
import org.jeecg.modules.content.channel.req.publish.ChannelPublishReq;
import org.jeecg.modules.content.channel.service.ChannelContentPublishService;
import org.jeecg.modules.content.channel.service.ChannelPublishLimitService;
import org.jeecg.modules.content.channel.vo.publish.ChannelPublishResultVO;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
public class ChannelPublishBizImpl implements ChannelPublishBiz {

    private static final Logger log = LoggerFactory.getLogger(ChannelPublishBizImpl.class);

    @Resource
    private ChannelContentPublishService publishService;

    @Resource
    private ChannelPublishLimitService limitService;

    @Resource
    private ChannelContentPublishMapper publishMapper;

    @Resource
    private ChannelContentReviewMapper reviewMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<ChannelPublishResultVO> publish(ChannelPublishReq req, String userId) {
        List<ChannelPublishResultVO> results = new ArrayList<>();
        for (String channelId : req.getChannelIds()) {
            ChannelPublishResultVO result = new ChannelPublishResultVO();
            result.setChannelId(channelId);
            try {
                // TODO: 查询频道配置获取 publishPermission, 查询用户角色和禁言/黑名单状态
                String userRole = "MEMBER";
                String publishPermission = "ALL_MEMBERS";
                boolean isMuted = false;
                boolean isBlacklisted = false;

                String permissionResult = publishService.checkPublishPermission(userRole, publishPermission, isMuted, isBlacklisted);

                if ("REJECT".equals(permissionResult)) {
                    result.setStatus("FAILED");
                    result.setFailReason("权限不足");
                } else if ("REVIEW".equals(permissionResult)) {
                    ChannelContentReview review = new ChannelContentReview();
                    review.setChannelId(channelId);
                    review.setContentId(req.getContentId());
                    review.setContentType(req.getContentType());
                    review.setSubmitterId(userId);
                    review.setReviewStatus("PENDING");
                    reviewMapper.insert(review);
                    result.setStatus("PENDING");
                } else {
                    // TODO: 查询发布限额配置和今日/本小时已发布数量
                    String limitResult = limitService.checkLimit(0, 0, 0, 0, 0, 0);
                    if (!"PASS".equals(limitResult)) {
                        result.setStatus("FAILED");
                        result.setFailReason("发布限额: " + limitResult);
                    } else {
                        ChannelContentPublish publish = new ChannelContentPublish();
                        publish.setChannelId(channelId);
                        publish.setContentId(req.getContentId());
                        publish.setContentType(req.getContentType());
                        publish.setPublisherId(userId);
                        publish.setPublishStatus("PUBLISHED");
                        publishMapper.insert(publish);
                        result.setStatus("PUBLISHED");
                    }
                }
            } catch (Exception e) {
                log.error("发布内容到频道失败, channelId={}, contentId={}", channelId, req.getContentId(), e);
                result.setStatus("FAILED");
                result.setFailReason("发布失败，请稍后重试");
            }
            results.add(result);
        }
        return results;
    }

    @Override
    public List<ChannelPublishResultVO> addExistingContent(ChannelAddExistingContentReq req, String userId) {
        List<ChannelPublishResultVO> results = new ArrayList<>();

        // 批量查询已存在的发布记录，避免 N+1 查询
        LambdaQueryWrapper<ChannelContentPublish> existQuery = new LambdaQueryWrapper<>();
        existQuery.in(ChannelContentPublish::getChannelId, req.getChannelIds())
                  .eq(ChannelContentPublish::getContentId, req.getContentId());
        java.util.Set<String> existingChannels = publishMapper.selectList(existQuery).stream()
                .map(ChannelContentPublish::getChannelId)
                .collect(java.util.stream.Collectors.toSet());

        // 权限校验（循环外计算一次）
        // TODO: 查询频道配置获取 publishPermission, 查询用户角色和禁言/黑名单状态
        String userRole = "MEMBER";
        String publishPermission = "ALL_MEMBERS";
        boolean isMuted = false;
        boolean isBlacklisted = false;
        String permissionResult = publishService.checkPublishPermission(userRole, publishPermission, isMuted, isBlacklisted);

        for (String channelId : req.getChannelIds()) {
            ChannelPublishResultVO result = new ChannelPublishResultVO();
            result.setChannelId(channelId);
            try {
                if (existingChannels.contains(channelId)) {
                    result.setStatus("FAILED");
                    result.setFailReason("该内容已存在于此频道");
                } else if ("REJECT".equals(permissionResult)) {
                    result.setStatus("FAILED");
                    result.setFailReason("权限不足");
                } else if ("REVIEW".equals(permissionResult)) {
                    ChannelContentReview review = new ChannelContentReview();
                    review.setChannelId(channelId);
                    review.setContentId(req.getContentId());
                    review.setContentType(req.getContentType());
                    review.setSubmitterId(userId);
                    review.setReviewStatus("PENDING");
                    review.setSourceScene("ADD_EXISTING");
                    reviewMapper.insert(review);
                    result.setStatus("PENDING");
                } else {
                    ChannelContentPublish publish = new ChannelContentPublish();
                    publish.setChannelId(channelId);
                    publish.setContentId(req.getContentId());
                    publish.setContentType(req.getContentType());
                    publish.setPublisherId(userId);
                    publish.setPublishStatus("PUBLISHED");
                    publish.setSourceType("ADD_EXISTING");
                    publishMapper.insert(publish);
                    result.setStatus("PUBLISHED");
                }
            } catch (Exception e) {
                log.error("添加已发布内容到频道失败, channelId={}, contentId={}", channelId, req.getContentId(), e);
                result.setStatus("FAILED");
                result.setFailReason("添加失败，请稍后重试");
            }
            results.add(result);
        }
        return results;
    }
}
