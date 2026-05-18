package org.jeecg.modules.content.user.service.impl;

import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.user.entity.ContentUserVerificationBadge;
import org.jeecg.modules.content.user.mapper.ContentUserVerificationBadgeMapper;
import org.jeecg.modules.content.user.service.IContentUserVerificationBadgeService;
import org.jeecg.modules.content.user.vo.ContentUserVerificationBadgeVO;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * 内容社区认证标识服务实现。
 */
@Service
public class ContentUserVerificationBadgeServiceImpl implements IContentUserVerificationBadgeService {

    private static final Set<String> SUPPORTED_TYPES = Set.of("PERSONAL", "ENTERPRISE", "CREATOR", "OFFICIAL", "REAL_NAME", "MOBILE", "EMAIL");

    @Resource
    private ContentUserVerificationBadgeMapper verificationBadgeMapper;

    @Override
    public List<ContentUserVerificationBadgeVO> listVisibleBadges(String userId) {
        return verificationBadgeMapper.selectActiveByUserId(userId).stream()
            .filter(this::isDisplayable)
            .map(ContentUserVerificationBadgeVO::from)
            .toList();
    }

    @Override
    public ContentUserVerificationBadgeVO getBadgeDetail(String badgeId) {
        ContentUserVerificationBadge badge = verificationBadgeMapper.selectById(badgeId);
        if (badge == null || !isDisplayable(badge)) {
            throw new JeecgBootException("认证标识不存在或不可见");
        }
        return ContentUserVerificationBadgeVO.from(badge);
    }

    private boolean isDisplayable(ContentUserVerificationBadge badge) {
        if (badge.getBadgeType() == null || badge.getBadgeType().trim().isEmpty()) {
            return false;
        }
        if (!SUPPORTED_TYPES.contains(badge.getBadgeType())) {
            return false;
        }
        if (badge.getBadgeLabel() == null || badge.getBadgeLabel().trim().isEmpty()) {
            return false;
        }
        return badge.getExpiresAt() == null || badge.getExpiresAt().after(new Date());
    }
}
