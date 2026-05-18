package org.jeecg.modules.content.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.content.user.entity.ContentUserLevelConfig;
import org.jeecg.modules.content.user.mapper.ContentUserLevelConfigMapper;
import org.jeecg.modules.content.user.service.IContentUserLevelConfigService;
import org.springframework.stereotype.Service;

/**
 * 内容社区用户等级配置服务实现。
 */
@Service
public class ContentUserLevelConfigServiceImpl
    extends ServiceImpl<ContentUserLevelConfigMapper, ContentUserLevelConfig>
    implements IContentUserLevelConfigService {
}
