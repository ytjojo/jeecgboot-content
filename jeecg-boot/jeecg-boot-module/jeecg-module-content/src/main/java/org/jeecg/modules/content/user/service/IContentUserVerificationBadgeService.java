package org.jeecg.modules.content.user.service;

import org.jeecg.modules.content.user.vo.ContentUserVerificationBadgeVO;

import java.util.List;

/**
 * 内容社区认证标识服务契约。
 */
public interface IContentUserVerificationBadgeService {

    List<ContentUserVerificationBadgeVO> listVisibleBadges(String userId);

    ContentUserVerificationBadgeVO getBadgeDetail(String badgeId);
}
