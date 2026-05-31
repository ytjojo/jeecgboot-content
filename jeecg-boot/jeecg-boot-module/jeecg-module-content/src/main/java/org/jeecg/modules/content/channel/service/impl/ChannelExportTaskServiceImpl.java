package org.jeecg.modules.content.channel.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.content.channel.entity.ChannelExportTask;
import org.jeecg.modules.content.channel.mapper.ChannelExportTaskMapper;
import org.jeecg.modules.content.channel.service.IChannelExportTaskService;
import org.springframework.stereotype.Service;

@Service
public class ChannelExportTaskServiceImpl extends ServiceImpl<ChannelExportTaskMapper, ChannelExportTask>
    implements IChannelExportTaskService {
}
