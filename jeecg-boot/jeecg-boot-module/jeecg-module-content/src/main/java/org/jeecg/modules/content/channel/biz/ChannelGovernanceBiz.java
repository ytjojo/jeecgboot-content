package org.jeecg.modules.content.channel.biz;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.jeecg.modules.content.channel.req.governance.ChannelGovernanceReq;
import org.jeecg.modules.content.channel.req.governance.GovernanceContentListReq;
import org.jeecg.modules.content.channel.req.governance.RecycleBinListReq;
import org.jeecg.modules.content.channel.vo.governance.GovernanceContentItemVO;
import org.jeecg.modules.content.channel.vo.governance.RecycleBinItemVO;

public interface ChannelGovernanceBiz {
    void executeGovernance(ChannelGovernanceReq req, String operatorId);
    Page<GovernanceContentItemVO> getContentList(GovernanceContentListReq req);
    Page<RecycleBinItemVO> getRecycleBinList(RecycleBinListReq req);
}
