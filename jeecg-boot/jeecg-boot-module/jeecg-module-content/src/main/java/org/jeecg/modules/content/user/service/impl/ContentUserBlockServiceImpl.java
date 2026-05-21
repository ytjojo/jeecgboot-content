package org.jeecg.modules.content.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.content.user.entity.ContentUserBlock;
import org.jeecg.modules.content.user.mapper.ContentUserBlockMapper;
import org.jeecg.modules.content.user.service.IContentUserBlockService;
import org.springframework.stereotype.Service;

/**
 * 内容社区用户拉黑关系服务实现。
 */
@Service
public class ContentUserBlockServiceImpl
    extends ServiceImpl<ContentUserBlockMapper, ContentUserBlock>
    implements IContentUserBlockService {
}
