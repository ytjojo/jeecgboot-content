package org.jeecg.modules.content.channel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.jeecg.modules.content.channel.entity.ChannelGovernanceLog;
import org.jeecg.modules.content.channel.enums.GovernanceAction;
import org.jeecg.modules.content.channel.mapper.ChannelGovernanceLogMapper;
import org.jeecg.modules.content.channel.service.ChannelGovernanceLogService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;

@Service
public class ChannelGovernanceLogServiceImpl implements ChannelGovernanceLogService {

    @Resource
    private ChannelGovernanceLogMapper governanceLogMapper;

    @Override
    public void log(GovernanceAction action, String channelId, String operatorId, String targetUserId, String reason, String extraData) {
        ChannelGovernanceLog log = new ChannelGovernanceLog();
        log.setChannelId(channelId);
        log.setAction(action.getCode());
        log.setOperatorId(operatorId);
        log.setTargetUserId(targetUserId);
        log.setReason(reason);
        log.setExtraData(extraData);
        governanceLogMapper.insert(log);
    }

    @Override
    public IPage<ChannelGovernanceLog> listByChannel(String channelId, Integer action, int pageNum, int pageSize) {
        LambdaQueryWrapper<ChannelGovernanceLog> wrapper = new LambdaQueryWrapper<ChannelGovernanceLog>()
            .eq(ChannelGovernanceLog::getChannelId, channelId)
            .orderByDesc(ChannelGovernanceLog::getCreateTime);
        if (action != null) {
            wrapper.eq(ChannelGovernanceLog::getAction, action);
        }
        return governanceLogMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }
}
