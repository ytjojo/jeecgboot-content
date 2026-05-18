package org.jeecg.modules.content.user.service;

import org.jeecg.modules.content.user.dto.ContentUserBadgeProgressDTO;
import org.jeecg.modules.content.user.entity.ContentUserBadgeGrant;
import org.jeecg.modules.content.user.vo.ContentUserBadgeVO;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 内容社区用户勋章服务。
 */
public interface IContentUserBadgeService {

    Map<String, List<ContentUserBadgeVO>> listBadgeCatalog(String userId);

    ContentUserBadgeVO getBadgeDetail(String userId, String badgeCode);

    ContentUserBadgeProgressDTO calculateProgress(String userId, String badgeCode);

    ContentUserBadgeGrant autoGrant(String userId, String metric, int currentProgress, String grantSource);

    List<ContentUserBadgeVO> saveWornBadges(String userId, List<String> grantIds);

    List<ContentUserBadgeVO> listWornBadges(String userId);

    int expireBadges(Date now);

    ContentUserBadgeGrant recycleBadge(String grantId, String operatorUserId, String reason);
}
