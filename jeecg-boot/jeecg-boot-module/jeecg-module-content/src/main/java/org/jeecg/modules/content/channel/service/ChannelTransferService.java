package org.jeecg.modules.content.channel.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.content.channel.entity.ChannelTransfer;

import java.util.List;

public interface ChannelTransferService extends IService<ChannelTransfer> {

    ChannelTransfer createTransfer(String channelId, String fromUserId, String toUserId);

    ChannelTransfer confirmTransfer(String transferId, String userId);

    boolean rejectTransfer(String transferId, String userId);

    /**
     * 查询频道的转让历史
     * @param channelId 频道ID
     * @return 转让记录列表（按创建时间倒序）
     */
    List<ChannelTransfer> getTransferHistory(String channelId);

    /**
     * 查询频道待确认的转让请求
     * @param channelId 频道ID
     * @return 待确认的转让请求，无则返回null
     */
    ChannelTransfer getPendingTransfer(String channelId);
}
