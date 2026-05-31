package org.jeecg.modules.content.channel.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.content.channel.entity.ChannelAppeal;
import org.jeecg.modules.content.channel.mapper.ChannelAppealMapper;
import org.jeecg.modules.content.channel.service.IChannelAppealService;
import org.springframework.stereotype.Service;

@Service
public class ChannelAppealServiceImpl extends ServiceImpl<ChannelAppealMapper, ChannelAppeal>
    implements IChannelAppealService {
}
