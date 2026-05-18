package org.jeecg.modules.content.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.content.user.entity.ContentUserFeatureUnlock;
import org.jeecg.modules.content.user.mapper.ContentUserFeatureUnlockMapper;
import org.jeecg.modules.content.user.service.IContentUserFeatureUnlockService;
import org.springframework.stereotype.Service;

/**
 * 内容社区用户功能解锁服务实现。
 */
@Service
public class ContentUserFeatureUnlockServiceImpl
    extends ServiceImpl<ContentUserFeatureUnlockMapper, ContentUserFeatureUnlock>
    implements IContentUserFeatureUnlockService {
}
