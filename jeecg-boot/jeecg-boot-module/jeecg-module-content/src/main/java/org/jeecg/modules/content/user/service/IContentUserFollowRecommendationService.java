package org.jeecg.modules.content.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.content.user.entity.ContentUserFollowRecommendation;
import org.jeecg.modules.content.user.vo.ContentFollowRecommendationPageVO;

/**
 * 内容社区关注推荐服务契约。
 */
public interface IContentUserFollowRecommendationService extends IService<ContentUserFollowRecommendation> {

    ContentFollowRecommendationPageVO listRecommendations(String userId, String interestTag, Long pageNo, Long pageSize);
}
