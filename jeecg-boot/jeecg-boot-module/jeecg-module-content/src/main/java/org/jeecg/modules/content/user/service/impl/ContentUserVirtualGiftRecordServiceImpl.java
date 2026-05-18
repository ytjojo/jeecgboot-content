package org.jeecg.modules.content.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.content.user.entity.ContentUserVirtualGiftRecord;
import org.jeecg.modules.content.user.mapper.ContentUserVirtualGiftRecordMapper;
import org.jeecg.modules.content.user.service.IContentUserVirtualGiftRecordService;
import org.springframework.stereotype.Service;

/**
 * 内容社区虚拟礼物记录服务实现。
 */
@Service
public class ContentUserVirtualGiftRecordServiceImpl
    extends ServiceImpl<ContentUserVirtualGiftRecordMapper, ContentUserVirtualGiftRecord>
    implements IContentUserVirtualGiftRecordService {
}
