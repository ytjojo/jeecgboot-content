package org.jeecg.modules.content.channel.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.content.channel.entity.ChannelTransfer;

public interface ChannelTransferService extends IService<ChannelTransfer> {

    ChannelTransfer createTransfer(String channelId, String fromUserId, String toUserId);

    ChannelTransfer confirmTransfer(String transferId, String userId);

    boolean rejectTransfer(String transferId, String userId);
}
