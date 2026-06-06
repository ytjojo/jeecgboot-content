package org.jeecg.modules.content.channel.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.jeecg.modules.content.channel.req.query.ChannelBrowseQueryReq;
import org.jeecg.modules.content.channel.vo.ChannelBrowseItemVO;

public interface IContentChannelBrowseService {

    IPage<ChannelBrowseItemVO> browseByCategory(ChannelBrowseQueryReq req);
}
