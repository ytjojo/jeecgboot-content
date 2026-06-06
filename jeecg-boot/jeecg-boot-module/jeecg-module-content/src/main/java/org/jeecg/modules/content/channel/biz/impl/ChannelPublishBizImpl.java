package org.jeecg.modules.content.channel.biz.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.jeecg.modules.content.channel.biz.ChannelPublishBiz;
import org.jeecg.modules.content.channel.entity.*;
import org.jeecg.modules.content.channel.enums.ChannelLifecycleStatus;
import org.jeecg.modules.content.channel.enums.MemberRole;
import org.jeecg.modules.content.channel.enums.PublishStatusEnum;
import org.jeecg.modules.content.channel.mapper.*;
import org.jeecg.modules.content.channel.req.publish.ChannelAddExistingContentReq;
import org.jeecg.modules.content.channel.req.publish.ChannelPublishReq;
import org.jeecg.modules.content.channel.service.ChannelContentPublishService;
import org.jeecg.modules.content.channel.service.ChannelPublishLimitService;
import org.jeecg.modules.content.channel.service.IChannelLifecycleLogService;
import org.jeecg.modules.content.channel.vo.publish.AvailableChannelVO;
import org.jeecg.modules.content.channel.vo.publish.ChannelPublishResultVO;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
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

    @Resource
    private IChannelLifecycleLogService lifecycleLogService;

    @Resource
    private ChannelMapper channelMapper;

    @Resource
    private ChannelMemberMapper channelMemberMapper;

    @Resource
    private ChannelBlacklistMapper channelBlacklistMapper;

    @Resource
    private ChannelMuteMapper channelMuteMapper;

    @Resource
    private ChannelLifecycleLogMapper channelLifecycleLogMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<ChannelPublishResultVO> publish(ChannelPublishReq req, String userId) {
        List<ChannelPublishResultVO> results = new ArrayList<>();
        for (String channelId : req.getChannelIds()) {
            ChannelPublishResultVO result = new ChannelPublishResultVO();
            result.setChannelId(channelId);
            try {
                // 冻结/隐藏/关闭状态拦截发布
                ChannelLifecycleLog latestLog = lifecycleLogService.getOne(
                        new LambdaQueryWrapper<ChannelLifecycleLog>()
                                .eq(ChannelLifecycleLog::getChannelId, channelId)
                                .orderByDesc(ChannelLifecycleLog::getCreatedTime)
                                .last("LIMIT 1"));
                if (latestLog != null) {
                    ChannelLifecycleStatus lifecycleStatus = ChannelLifecycleStatus.fromCode(latestLog.getToStatus());
                    if (lifecycleStatus == ChannelLifecycleStatus.READONLY_FROZEN
                            || lifecycleStatus == ChannelLifecycleStatus.HIDDEN
                            || lifecycleStatus == ChannelLifecycleStatus.CLOSED) {
                        result.setStatus("FAILED");
                        result.setFailReason("频道当前状态为" + lifecycleStatus.getDesc() + "，不允许发布");
                        results.add(result);
                        continue;
                    }
                }

                // TODO: 查询频道配置获取 publishPermission, 查询用户角色和禁言/黑名单状态
                String userRole = "MEMBER";
                String publishPermission = "ALL_MEMBERS";
                boolean isMuted = false;
                boolean isBlacklisted = false;

                String permissionResult = publishService.checkPublishPermission(userRole, publishPermission, isMuted, isBlacklisted);

                if ("REJECT".equals(permissionResult)) {
                    result.setStatus(PublishStatusEnum.FAILED.getCode());
                    result.setFailReason("权限不足");
                } else if ("REVIEW".equals(permissionResult)) {
                    ChannelContentReview review = new ChannelContentReview();
                    review.setChannelId(channelId);
                    review.setContentId(req.getContentId());
                    review.setContentType(req.getContentType());
                    review.setSubmitterId(userId);
                    review.setReviewStatus(PublishStatusEnum.PENDING.getCode());
                    reviewMapper.insert(review);
                    result.setStatus(PublishStatusEnum.PENDING.getCode());
                } else {
                    // TODO: 查询发布限额配置和今日/本小时已发布数量
                    String limitResult = limitService.checkLimit(0, 0, 0, 0, 0, 0);
                    if (!"PASS".equals(limitResult)) {
                        result.setStatus(PublishStatusEnum.FAILED.getCode());
                        result.setFailReason("发布限额: " + limitResult);
                    } else {
                        ChannelContentPublish publish = new ChannelContentPublish();
                        publish.setChannelId(channelId);
                        publish.setContentId(req.getContentId());
                        publish.setContentType(req.getContentType());
                        publish.setPublisherId(userId);
                        publish.setPublishStatus(PublishStatusEnum.PUBLISHED.getCode());
                        publishMapper.insert(publish);
                        result.setStatus(PublishStatusEnum.PUBLISHED.getCode());
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
    public List<AvailableChannelVO> getAvailableChannels(String userId) {
        // 1. 查询用户加入的所有频道
        List<ChannelMember> memberships = channelMemberMapper.selectList(
                new LambdaQueryWrapper<ChannelMember>()
                        .eq(ChannelMember::getUserId, userId));
        if (memberships.isEmpty()) {
            return new ArrayList<>();
        }

        List<AvailableChannelVO> result = new ArrayList<>();
        for (ChannelMember membership : memberships) {
            String channelId = membership.getChannelId();
            Channel channel = channelMapper.selectById(channelId);
            if (channel == null) {
                continue;
            }

            AvailableChannelVO vo = new AvailableChannelVO();
            vo.setChannelId(channelId);
            vo.setChannelName(channel.getName());
            vo.setIconUrl(channel.getIconUrl());
            vo.setUserRole(String.valueOf(membership.getRole()));
            vo.setPublishPermission("ALL_MEMBERS");

            // 2. 检查频道生命周期状态
            ChannelLifecycleLog latestLog = channelLifecycleLogMapper.selectOne(
                    new LambdaQueryWrapper<ChannelLifecycleLog>()
                            .eq(ChannelLifecycleLog::getChannelId, channelId)
                            .orderByDesc(ChannelLifecycleLog::getCreatedTime)
                            .last("LIMIT 1"));
            if (latestLog != null) {
                ChannelLifecycleStatus lifecycleStatus = ChannelLifecycleStatus.fromCode(latestLog.getToStatus());
                if (lifecycleStatus == ChannelLifecycleStatus.READONLY_FROZEN
                        || lifecycleStatus == ChannelLifecycleStatus.HIDDEN
                        || lifecycleStatus == ChannelLifecycleStatus.CLOSED
                        || lifecycleStatus == ChannelLifecycleStatus.DELETED) {
                    vo.setCanPublish(false);
                    vo.setBlockedReason("频道当前状态为" + lifecycleStatus.getDesc() + "，不允许发布");
                    result.add(vo);
                    continue;
                }
            }

            // 3. 检查用户是否被禁言或拉黑
            Long blacklistCount = channelBlacklistMapper.selectCount(
                    new LambdaQueryWrapper<ChannelBlacklist>()
                            .eq(ChannelBlacklist::getChannelId, channelId)
                            .eq(ChannelBlacklist::getUserId, userId));
            if (blacklistCount > 0) {
                vo.setCanPublish(false);
                vo.setBlockedReason("您已被加入该频道黑名单");
                result.add(vo);
                continue;
            }

            Long muteCount = channelMuteMapper.selectCount(
                    new LambdaQueryWrapper<ChannelMute>()
                            .eq(ChannelMute::getChannelId, channelId)
                            .eq(ChannelMute::getUserId, userId));
            if (muteCount > 0) {
                vo.setCanPublish(false);
                vo.setBlockedReason("您在该频道已被禁言");
                result.add(vo);
                continue;
            }

            vo.setCanPublish(true);
            result.add(vo);
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<ChannelPublishResultVO> addExistingContent(ChannelAddExistingContentReq req, String userId) {
        List<ChannelPublishResultVO> results = new ArrayList<>();

        // 批量查询已存在的发布记录，避免 N+1 查询
        LambdaQueryWrapper<ChannelContentPublish> existQuery = new LambdaQueryWrapper<>();
        existQuery.in(ChannelContentPublish::getChannelId, req.getChannelIds())
                  .eq(ChannelContentPublish::getContentId, req.getContentId());
        java.util.Set<String> existingChannels = publishMapper.selectList(existQuery).stream()
                .map(ChannelContentPublish::getChannelId)
                .collect(java.util.stream.Collectors.toUnmodifiableSet());

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
                    result.setStatus(PublishStatusEnum.FAILED.getCode());
                    result.setFailReason("该内容已存在于此频道");
                } else if ("REJECT".equals(permissionResult)) {
                    result.setStatus(PublishStatusEnum.FAILED.getCode());
                    result.setFailReason("权限不足");
                } else if ("REVIEW".equals(permissionResult)) {
                    ChannelContentReview review = new ChannelContentReview();
                    review.setChannelId(channelId);
                    review.setContentId(req.getContentId());
                    review.setContentType(req.getContentType());
                    review.setSubmitterId(userId);
                    review.setReviewStatus(PublishStatusEnum.PENDING.getCode());
                    review.setSourceScene("ADD_EXISTING");
                    reviewMapper.insert(review);
                    result.setStatus(PublishStatusEnum.PENDING.getCode());
                } else {
                    ChannelContentPublish publish = new ChannelContentPublish();
                    publish.setChannelId(channelId);
                    publish.setContentId(req.getContentId());
                    publish.setContentType(req.getContentType());
                    publish.setPublisherId(userId);
                    publish.setPublishStatus(PublishStatusEnum.PUBLISHED.getCode());
                    publish.setSourceType("ADD_EXISTING");
                    publishMapper.insert(publish);
                    result.setStatus(PublishStatusEnum.PUBLISHED.getCode());
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

    @Override
    public List<AvailableChannelVO> getAvailableChannels(String userId) {
        // 查询用户加入的所有频道
        List<ChannelMember> memberships = channelMemberMapper.selectList(
                new LambdaQueryWrapper<ChannelMember>().eq(ChannelMember::getUserId, userId));
        if (memberships.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> channelIds = memberships.stream()
                .map(ChannelMember::getChannelId).collect(java.util.stream.Collectors.toList());

        // 批量查询频道
        java.util.Map<String, Channel> channelMap = channelMapper.selectBatchIds(channelIds).stream()
                .collect(java.util.stream.Collectors.toMap(Channel::getId, c -> c));

        // 批量查询禁封状态(取每个频道最新一条)
        java.util.Map<String, ChannelLifecycleLog> lifecycleMap = new java.util.HashMap<>();
        channelLifecycleLogMapper.selectList(
                new LambdaQueryWrapper<ChannelLifecycleLog>()
                        .in(ChannelLifecycleLog::getChannelId, channelIds)
                        .orderByDesc(ChannelLifecycleLog::getCreatedTime))
                .forEach(log -> lifecycleMap.putIfAbsent(log.getChannelId(), log));

        // 批量查询黑名单
        java.util.Set<String> blacklisted = channelBlacklistMapper.selectList(
                new LambdaQueryWrapper<ChannelBlacklist>()
                        .in(ChannelBlacklist::getChannelId, channelIds)
                        .eq(ChannelBlacklist::getUserId, userId))
                .stream().map(ChannelBlacklist::getChannelId).collect(java.util.stream.Collectors.toSet());

        // 批量查询禁言
        Date now = new Date();
        java.util.Set<String> muted = channelMuteMapper.selectList(
                new LambdaQueryWrapper<ChannelMute>()
                        .in(ChannelMute::getChannelId, channelIds)
                        .eq(ChannelMute::getUserId, userId)
                        .and(w -> w.isNull(ChannelMute::getEndTime)
                                .or().gt(ChannelMute::getEndTime, now)))
                .stream().map(ChannelMute::getChannelId).collect(java.util.stream.Collectors.toSet());

        List<AvailableChannelVO> result = new ArrayList<>();
        for (ChannelMember member : memberships) {
            Channel channel = channelMap.get(member.getChannelId());
            if (channel == null) {
                continue;
            }

            AvailableChannelVO vo = new AvailableChannelVO();
            vo.setChannelId(channel.getId());
            vo.setChannelName(channel.getName());
            vo.setIconUrl(channel.getIconUrl());
            vo.setUserRole(MemberRole.fromCode(member.getRole()).getDesc());
            vo.setPublishPermission("ALL_MEMBERS");

            // 检查频道生命周期状态
            ChannelLifecycleLog latestLog = lifecycleMap.get(channel.getId());
            if (latestLog != null) {
                ChannelLifecycleStatus status = ChannelLifecycleStatus.fromCode(latestLog.getToStatus());
                if (status == ChannelLifecycleStatus.READONLY_FROZEN
                        || status == ChannelLifecycleStatus.HIDDEN
                        || status == ChannelLifecycleStatus.CLOSED
                        || status == ChannelLifecycleStatus.DELETED) {
                    vo.setCanPublish(false);
                    vo.setBlockedReason("频道当前状态为" + status.getDesc());
                    result.add(vo);
                    continue;
                }
            }

            // 检查黑名单
            if (blacklisted.contains(channel.getId())) {
                vo.setCanPublish(false);
                vo.setBlockedReason("您已被加入该频道黑名单");
                result.add(vo);
                continue;
            }

            // 检查禁言
            if (muted.contains(channel.getId())) {
                vo.setCanPublish(false);
                vo.setBlockedReason("您在该频道已被禁言");
                result.add(vo);
                continue;
            }

            vo.setCanPublish(true);
            result.add(vo);
        }
        return result;
    }
}
