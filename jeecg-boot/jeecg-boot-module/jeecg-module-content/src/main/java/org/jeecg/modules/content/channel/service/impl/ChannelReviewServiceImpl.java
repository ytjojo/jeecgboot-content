package org.jeecg.modules.content.channel.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.content.channel.entity.ChannelReview;
import org.jeecg.modules.content.channel.mapper.ChannelReviewMapper;
import org.jeecg.modules.content.channel.service.IChannelReviewService;
import org.springframework.stereotype.Service;

@Service
public class ChannelReviewServiceImpl extends ServiceImpl<ChannelReviewMapper, ChannelReview>
    implements IChannelReviewService {
}
