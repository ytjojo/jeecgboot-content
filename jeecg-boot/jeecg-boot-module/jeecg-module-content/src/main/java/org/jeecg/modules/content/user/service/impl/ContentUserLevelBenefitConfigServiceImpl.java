package org.jeecg.modules.content.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.content.user.entity.ContentUserLevelBenefitConfig;
import org.jeecg.modules.content.user.mapper.ContentUserLevelBenefitConfigMapper;
import org.jeecg.modules.content.user.service.IContentUserLevelBenefitConfigService;
import org.springframework.stereotype.Service;

/**
 * 内容社区用户等级权益配置服务实现。
 */
@Service
public class ContentUserLevelBenefitConfigServiceImpl
    extends ServiceImpl<ContentUserLevelBenefitConfigMapper, ContentUserLevelBenefitConfig>
    implements IContentUserLevelBenefitConfigService {
}
