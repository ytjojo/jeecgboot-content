package org.jeecg.modules.content.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.content.user.entity.ContentUserActivitySnapshot;
import org.jeecg.modules.content.user.mapper.ContentUserActivitySnapshotMapper;
import org.jeecg.modules.content.user.service.IContentUserActivitySnapshotService;
import org.springframework.stereotype.Service;

/**
 * 内容社区用户动态快照服务实现。
 */
@Service
public class ContentUserActivitySnapshotServiceImpl
    extends ServiceImpl<ContentUserActivitySnapshotMapper, ContentUserActivitySnapshot>
    implements IContentUserActivitySnapshotService {
}
