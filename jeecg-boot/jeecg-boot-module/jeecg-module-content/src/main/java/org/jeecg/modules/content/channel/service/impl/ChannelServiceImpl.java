package org.jeecg.modules.content.channel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.jeecg.common.system.base.service.impl.JeecgServiceImpl;
import org.jeecg.modules.content.channel.entity.Channel;
import org.jeecg.modules.content.channel.enums.ChannelStatus;
import org.jeecg.modules.content.channel.enums.ChannelType;
import org.jeecg.modules.content.channel.mapper.ChannelMapper;
import org.jeecg.modules.content.channel.service.ChannelService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChannelServiceImpl extends JeecgServiceImpl<ChannelMapper, Channel>
    implements ChannelService {

    private static final List<ChannelStatus> NAME_OCCUPIED_STATUSES = List.of(
        ChannelStatus.PENDING_REVIEW,
        ChannelStatus.ACTIVE,
        ChannelStatus.DELETE_COOLING
    );

    @Override
    public boolean checkNameUnique(String name, String excludeId) {
        LambdaQueryWrapper<Channel> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Channel::getName, name)
               .in(Channel::getStatus, NAME_OCCUPIED_STATUSES)
               .ne(Channel::getChannelType, ChannelType.SYSTEM);

        if (excludeId != null) {
            wrapper.ne(Channel::getId, excludeId);
        }

        return baseMapper.selectCount(wrapper) == 0;
    }
}
