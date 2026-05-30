package org.jeecg.modules.content.channel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.channel.entity.ChannelMute;
import org.jeecg.modules.content.channel.mapper.ChannelMuteMapper;
import org.jeecg.modules.content.channel.service.ChannelMuteService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Service
public class ChannelMuteServiceImpl implements ChannelMuteService {

    @Resource
    private ChannelMuteMapper muteMapper;

    @Override
    public void mute(String channelId, String userId, String operatorId, String reason, int days) {
        ChannelMute mute = new ChannelMute();
        mute.setChannelId(channelId);
        mute.setUserId(userId);
        mute.setOperatorId(operatorId);
        mute.setReason(reason);
        mute.setStartTime(new Date());
        if (days > 0) {
            mute.setEndTime(Date.from(LocalDateTime.now().plusDays(days)
                .atZone(ZoneId.systemDefault()).toInstant()));
        }
        muteMapper.insert(mute);
    }

    @Override
    public void unmute(String channelId, String userId, String operatorId) {
        ChannelMute mute = muteMapper.selectOne(new LambdaQueryWrapper<ChannelMute>()
            .eq(ChannelMute::getChannelId, channelId)
            .eq(ChannelMute::getUserId, userId)
            .isNull(ChannelMute::getUnmuteType));
        if (mute == null) {
            throw new JeecgBootException("未找到有效禁言记录");
        }
        mute.setUnmuteType(2);
        mute.setUnmuteTime(new Date());
        muteMapper.updateById(mute);
    }

    @Override
    public boolean isMuted(String channelId, String userId) {
        ChannelMute mute = muteMapper.selectOne(new LambdaQueryWrapper<ChannelMute>()
            .eq(ChannelMute::getChannelId, channelId)
            .eq(ChannelMute::getUserId, userId)
            .isNull(ChannelMute::getUnmuteType));
        if (mute == null) {
            return false;
        }
        if (mute.getEndTime() == null) {
            return true;
        }
        return mute.getEndTime().after(new Date());
    }
}
