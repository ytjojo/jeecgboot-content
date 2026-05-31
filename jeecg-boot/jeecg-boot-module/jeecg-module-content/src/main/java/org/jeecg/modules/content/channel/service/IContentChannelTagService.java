package org.jeecg.modules.content.channel.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.content.channel.entity.ContentChannelTag;
import org.jeecg.modules.content.channel.req.create.ChannelTagCreateReq;
import org.jeecg.modules.content.channel.vo.ChannelTagVO;

import java.util.List;

public interface IContentChannelTagService extends IService<ContentChannelTag> {

    ContentChannelTag createTag(ChannelTagCreateReq req);

    void deleteTag(String tagId);

    List<ChannelTagVO> listByChannel(String channelId);
}
