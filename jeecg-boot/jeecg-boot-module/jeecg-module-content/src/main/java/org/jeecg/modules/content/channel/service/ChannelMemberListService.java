package org.jeecg.modules.content.channel.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.jeecg.modules.content.channel.entity.ChannelMember;

public interface ChannelMemberListService {

    IPage<ChannelMember> listMembers(String channelId, Integer role, int pageNum, int pageSize);

    IPage<ChannelMember> searchMembers(String channelId, String keyword, int pageNum, int pageSize);
}
