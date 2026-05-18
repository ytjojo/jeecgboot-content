package org.jeecg.modules.content.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.content.user.entity.ContentUserGrowthDecayState;
import org.jeecg.modules.content.user.mapper.ContentUserGrowthDecayStateMapper;
import org.jeecg.modules.content.user.service.IContentUserGrowthDecayStateService;
import org.springframework.stereotype.Service;

/**
 * 内容社区成长值衰减状态服务实现。
 */
@Service
public class ContentUserGrowthDecayStateServiceImpl
    extends ServiceImpl<ContentUserGrowthDecayStateMapper, ContentUserGrowthDecayState>
    implements IContentUserGrowthDecayStateService {
}
