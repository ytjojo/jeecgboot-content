package org.jeecg.modules.content.channel.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.content.channel.entity.ContentChannelEditorialPick;
import org.jeecg.modules.content.channel.req.create.ChannelEditorialPickCreateReq;
import org.jeecg.modules.content.channel.req.update.ChannelEditorialPickUpdateReq;
import org.jeecg.modules.content.channel.vo.ChannelEditorialPickVO;

import java.util.List;

public interface IContentChannelEditorialPickService extends IService<ContentChannelEditorialPick> {

    ContentChannelEditorialPick createPick(ChannelEditorialPickCreateReq req);

    void updatePick(ChannelEditorialPickUpdateReq req);

    void removePick(String pickId);

    List<ChannelEditorialPickVO> listActivePicks();
}
