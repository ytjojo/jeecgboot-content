package org.jeecg.modules.content.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.content.user.entity.ContentUserMute;
import org.jeecg.modules.content.user.mapper.ContentUserMuteMapper;
import org.jeecg.modules.content.user.service.IContentUserMuteService;
import org.springframework.stereotype.Service;

/**
 * 内容社区用户屏蔽关系服务实现。
 */
@Service
public class ContentUserMuteServiceImpl
    extends ServiceImpl<ContentUserMuteMapper, ContentUserMute>
    implements IContentUserMuteService {
}
