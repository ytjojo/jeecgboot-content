package org.jeecg.modules.content.channel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.channel.entity.ChannelJoinApplication;
import org.jeecg.modules.content.channel.enums.ApplicationStatus;
import org.jeecg.modules.content.channel.mapper.ChannelJoinApplicationMapper;
import org.jeecg.modules.content.channel.service.ChannelJoinApplicationService;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class ChannelJoinApplicationServiceImpl extends ServiceImpl<ChannelJoinApplicationMapper, ChannelJoinApplication>
    implements ChannelJoinApplicationService {

    @Override
    public void apply(String channelId, String userId, String reason) {
        Long count = count(new LambdaQueryWrapper<ChannelJoinApplication>()
            .eq(ChannelJoinApplication::getChannelId, channelId)
            .eq(ChannelJoinApplication::getUserId, userId)
            .eq(ChannelJoinApplication::getStatus, ApplicationStatus.PENDING.getCode()));
        if (count > 0) {
            throw new JeecgBootException("已有待审核的申请");
        }
        ChannelJoinApplication app = new ChannelJoinApplication();
        app.setChannelId(channelId);
        app.setUserId(userId);
        app.setReason(reason);
        app.setStatus(ApplicationStatus.PENDING.getCode());
        baseMapper.insert(app);
    }

    @Override
    public void approve(String applicationId, String reviewerId, String reviewReason) {
        ChannelJoinApplication app = getById(applicationId);
        if (app == null) {
            throw new JeecgBootException("申请不存在");
        }
        if (app.getStatus() != ApplicationStatus.PENDING.getCode()) {
            throw new JeecgBootException("申请已处理");
        }
        app.setStatus(ApplicationStatus.APPROVED.getCode());
        app.setReviewerId(reviewerId);
        app.setReviewReason(reviewReason);
        app.setReviewTime(new Date());
        updateById(app);
    }

    @Override
    public void reject(String applicationId, String reviewerId, String reviewReason) {
        ChannelJoinApplication app = getById(applicationId);
        if (app == null) {
            throw new JeecgBootException("申请不存在");
        }
        if (app.getStatus() != ApplicationStatus.PENDING.getCode()) {
            throw new JeecgBootException("申请已处理");
        }
        app.setStatus(ApplicationStatus.REJECTED.getCode());
        app.setReviewerId(reviewerId);
        app.setReviewReason(reviewReason);
        app.setReviewTime(new Date());
        updateById(app);
    }

    @Override
    public List<ChannelJoinApplication> listPending(String channelId) {
        return list(new LambdaQueryWrapper<ChannelJoinApplication>()
            .eq(ChannelJoinApplication::getChannelId, channelId)
            .eq(ChannelJoinApplication::getStatus, ApplicationStatus.PENDING.getCode())
            .orderByAsc(ChannelJoinApplication::getCreateTime));
    }
}
