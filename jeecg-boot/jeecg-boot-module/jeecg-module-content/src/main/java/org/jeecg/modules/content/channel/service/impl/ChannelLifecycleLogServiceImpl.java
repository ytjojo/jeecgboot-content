package org.jeecg.modules.content.channel.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.content.channel.entity.ChannelLifecycleLog;
import org.jeecg.modules.content.channel.mapper.ChannelLifecycleLogMapper;
import org.jeecg.modules.content.channel.service.IChannelLifecycleLogService;
import org.springframework.stereotype.Service;

@Service
public class ChannelLifecycleLogServiceImpl extends ServiceImpl<ChannelLifecycleLogMapper, ChannelLifecycleLog>
    implements IChannelLifecycleLogService {
}
