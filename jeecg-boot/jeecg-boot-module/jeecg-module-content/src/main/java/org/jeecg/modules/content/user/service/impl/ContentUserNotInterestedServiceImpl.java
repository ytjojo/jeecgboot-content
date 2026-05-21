package org.jeecg.modules.content.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.content.user.entity.ContentUserNotInterested;
import org.jeecg.modules.content.user.mapper.ContentUserNotInterestedMapper;
import org.jeecg.modules.content.user.service.IContentUserNotInterestedService;
import org.springframework.stereotype.Service;

/**
 * 内容社区用户不感兴趣反馈服务实现。
 */
@Service
public class ContentUserNotInterestedServiceImpl
    extends ServiceImpl<ContentUserNotInterestedMapper, ContentUserNotInterested>
    implements IContentUserNotInterestedService {
}
