package org.jeecg.modules.content.channel.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.jeecg.modules.content.channel.entity.ChannelGovernanceLog;
import org.jeecg.modules.content.channel.enums.GovernanceAction;

public interface ChannelGovernanceLogService {

    void log(GovernanceAction action, String channelId, String operatorId, String targetUserId, String reason, String extraData);

    IPage<ChannelGovernanceLog> listByChannel(String channelId, Integer action, int pageNum, int pageSize);
}
