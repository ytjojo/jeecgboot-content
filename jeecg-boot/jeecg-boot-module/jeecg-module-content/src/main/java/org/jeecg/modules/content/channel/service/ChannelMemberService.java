package org.jeecg.modules.content.channel.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.content.channel.entity.ChannelMember;
import org.jeecg.modules.content.channel.enums.MemberRole;

import java.util.List;

public interface ChannelMemberService extends IService<ChannelMember> {

    void addMember(String channelId, String userId, MemberRole role);

    void removeMember(String memberId);

    void assignRole(String memberId, MemberRole role, String operatorId);

    boolean isMember(String channelId, String userId);

    ChannelMember getByChannelAndUser(String channelId, String userId);

    List<ChannelMember> listByChannel(String channelId);
}
