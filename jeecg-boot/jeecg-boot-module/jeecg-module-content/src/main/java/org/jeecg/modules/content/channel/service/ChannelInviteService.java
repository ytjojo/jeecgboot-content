package org.jeecg.modules.content.channel.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.content.channel.entity.ChannelInvite;

import java.util.List;

public interface ChannelInviteService extends IService<ChannelInvite> {

    ChannelInvite createInvite(String channelId, Integer type, Integer maxUses, Integer expireDays, String creatorId);

    boolean validateInvite(String code);

    void useInvite(String code);

    void revokeInvite(String inviteId, String operatorId);

    List<ChannelInvite> listByChannel(String channelId);
}
