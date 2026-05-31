package org.jeecg.modules.content.channel.biz;

import org.jeecg.modules.content.channel.req.publish.ChannelPublishReq;
import org.jeecg.modules.content.channel.vo.publish.ChannelPublishResultVO;
import java.util.List;

public interface ChannelPublishBiz {
    List<ChannelPublishResultVO> publish(ChannelPublishReq req);
}
