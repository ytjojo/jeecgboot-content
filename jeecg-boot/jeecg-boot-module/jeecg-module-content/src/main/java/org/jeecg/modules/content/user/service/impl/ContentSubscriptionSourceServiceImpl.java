package org.jeecg.modules.content.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.content.user.entity.ContentSubscriptionSource;
import org.jeecg.modules.content.user.mapper.ContentSubscriptionSourceMapper;
import org.jeecg.modules.content.user.service.IContentSubscriptionSourceService;
import org.springframework.stereotype.Service;

/**
 * 内容社区订阅源目录服务实现。
 */
@Service
public class ContentSubscriptionSourceServiceImpl
    extends ServiceImpl<ContentSubscriptionSourceMapper, ContentSubscriptionSource>
    implements IContentSubscriptionSourceService {
}
