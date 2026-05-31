package org.jeecg.modules.content.channel.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.jeecg.modules.content.channel.req.query.ChannelSearchQueryReq;
import org.jeecg.modules.content.channel.vo.ChannelSearchResultVO;

public interface IContentChannelSearchService {

    IPage<ChannelSearchResultVO> search(String userId, ChannelSearchQueryReq req);
}
