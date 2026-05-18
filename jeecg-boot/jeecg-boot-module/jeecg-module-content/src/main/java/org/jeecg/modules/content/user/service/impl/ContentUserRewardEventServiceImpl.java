package org.jeecg.modules.content.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.content.user.entity.ContentUserRewardEvent;
import org.jeecg.modules.content.user.mapper.ContentUserRewardEventMapper;
import org.jeecg.modules.content.user.service.IContentUserRewardEventService;
import org.springframework.stereotype.Service;

/**
 * 内容社区用户奖励事件服务实现。
 */
@Service
public class ContentUserRewardEventServiceImpl
    extends ServiceImpl<ContentUserRewardEventMapper, ContentUserRewardEvent>
    implements IContentUserRewardEventService {
}
