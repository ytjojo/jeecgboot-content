package org.jeecg.modules.content.channel.biz;

import org.jeecg.modules.content.channel.req.governance.ChannelGovernanceReq;

public interface ChannelGovernanceBiz {
    void executeGovernance(ChannelGovernanceReq req, String operatorId);
}
