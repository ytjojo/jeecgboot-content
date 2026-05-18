package org.jeecg.modules.content.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.content.user.entity.ContentUserFollowRecommendation;
import org.jeecg.modules.content.user.mapper.ContentUserFollowRecommendationMapper;
import org.jeecg.modules.content.user.service.IContentUserFollowRecommendationService;
import org.springframework.stereotype.Service;

/**
 * 内容社区关注推荐服务实现。
 */
@Service
public class ContentUserFollowRecommendationServiceImpl
    extends ServiceImpl<ContentUserFollowRecommendationMapper, ContentUserFollowRecommendation>
    implements IContentUserFollowRecommendationService {
}
