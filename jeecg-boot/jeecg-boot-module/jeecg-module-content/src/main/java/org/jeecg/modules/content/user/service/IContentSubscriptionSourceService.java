package org.jeecg.modules.content.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.content.user.entity.ContentSubscriptionSource;
import org.jeecg.modules.content.user.req.subscription.ContentSubscriptionSourceReq;
import org.jeecg.modules.content.user.vo.ContentSubscriptionSourceDetailVO;
import org.jeecg.modules.content.user.vo.ContentSubscriptionSourcePageVO;
import org.jeecg.modules.content.user.vo.ContentSubscriptionSourceVO;
import org.jeecg.modules.content.user.vo.ContentUserSubscriptionVO;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 内容社区订阅源目录服务契约。
 */
public interface IContentSubscriptionSourceService extends IService<ContentSubscriptionSource> {

    ContentSubscriptionSourceVO saveSource(ContentSubscriptionSourceReq req);

    ContentSubscriptionSourceVO refreshSource(String sourceType, String sourceId, Integer subscriberCount,
                                              BigDecimal heatScore, Date latestUpdateTime);

    ContentSubscriptionSourcePageVO listPlaza(String userId, String category, String keyword, String sourceType,
                                              Long pageNo, Long pageSize);

    ContentSubscriptionSourceDetailVO getSourceDetail(String userId, String sourceType, String sourceId);

    ContentUserSubscriptionVO subscribeFromPlaza(String userId, String sourceType, String sourceId);
}
