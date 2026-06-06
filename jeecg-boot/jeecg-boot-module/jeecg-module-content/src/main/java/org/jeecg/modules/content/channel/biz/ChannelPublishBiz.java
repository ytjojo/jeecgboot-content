package org.jeecg.modules.content.channel.biz;

import org.jeecg.modules.content.channel.req.publish.ChannelAddExistingContentReq;
import org.jeecg.modules.content.channel.req.publish.ChannelPublishReq;
import org.jeecg.modules.content.channel.vo.publish.AvailableChannelVO;
import org.jeecg.modules.content.channel.vo.publish.ChannelPublishResultVO;
import java.util.List;

public interface ChannelPublishBiz {
    List<ChannelPublishResultVO> publish(ChannelPublishReq req, String userId);
    List<ChannelPublishResultVO> addExistingContent(ChannelAddExistingContentReq req, String userId);
    List<AvailableChannelVO> getAvailableChannels(String userId);
}
