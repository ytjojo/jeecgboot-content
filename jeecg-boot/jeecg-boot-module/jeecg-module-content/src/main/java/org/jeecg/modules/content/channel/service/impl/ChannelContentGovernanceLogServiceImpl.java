package org.jeecg.modules.content.channel.service.impl;

import org.jeecg.modules.content.channel.entity.ChannelContentGovernanceLog;
import org.jeecg.modules.content.channel.mapper.ChannelContentGovernanceLogMapper;
import org.jeecg.modules.content.channel.service.ChannelContentGovernanceLogService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class ChannelContentGovernanceLogServiceImpl implements ChannelContentGovernanceLogService {

    @Resource
    private ChannelContentGovernanceLogMapper governanceLogMapper;

    @Override
    public void log(String channelId, String contentId, String operatorId, String action, String detail, String reason, String result) {
        ChannelContentGovernanceLog log = new ChannelContentGovernanceLog();
        log.setChannelId(channelId);
        log.setContentId(contentId);
        log.setOperatorId(operatorId);
        log.setAction(action);
        log.setActionDetail(detail);
        log.setReason(reason);
        log.setResult(result);
        governanceLogMapper.insert(log);
    }
}
