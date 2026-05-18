package org.jeecg.modules.content.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.content.user.entity.ContentUserFeedSetting;
import org.jeecg.modules.content.user.mapper.ContentUserFeedSettingMapper;
import org.jeecg.modules.content.user.service.IContentUserFeedSettingService;
import org.springframework.stereotype.Service;

/**
 * 内容社区关注流设置服务实现。
 */
@Service
public class ContentUserFeedSettingServiceImpl
    extends ServiceImpl<ContentUserFeedSettingMapper, ContentUserFeedSetting>
    implements IContentUserFeedSettingService {
}
