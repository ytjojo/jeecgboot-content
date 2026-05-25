package org.jeecg.modules.content.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.user.entity.ContentUserBlock;
import org.jeecg.modules.content.user.entity.ContentUserProfile;
import org.jeecg.modules.content.user.entity.ContentUserFollowRecommendation;
import org.jeecg.modules.content.user.mapper.ContentUserBlockMapper;
import org.jeecg.modules.content.user.mapper.ContentUserFollowRecommendationMapper;
import org.jeecg.modules.content.user.mapper.ContentUserProfileMapper;
import org.jeecg.modules.content.user.mapper.ContentUserRelationMapper;
import org.jeecg.modules.content.user.service.IContentUserFollowRecommendationService;
import org.jeecg.modules.content.user.vo.ContentFollowRecommendationItemVO;
import org.jeecg.modules.content.user.vo.ContentFollowRecommendationPageVO;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * 内容社区关注推荐服务实现。
 */
@Service
public class ContentUserFollowRecommendationServiceImpl
    extends ServiceImpl<ContentUserFollowRecommendationMapper, ContentUserFollowRecommendation>
    implements IContentUserFollowRecommendationService {

    private static final int USER_ID_MAX_LENGTH = 64;
    private static final long DEFAULT_PAGE_SIZE = 10L;
    private static final long MAX_PAGE_SIZE = 100L;
    private static final String ACTIVE_STATUS = "ACTIVE";
    private static final Set<String> INACTIVE_PROFILE_STATUS = Set.of("FROZEN", "BANNED", "CANCEL_PENDING", "CANCELLED");

    @Resource
    private ContentUserFollowRecommendationMapper recommendationMapper;

    @Resource
    private ContentUserProfileMapper profileMapper;

    @Resource
    private ContentUserRelationMapper relationMapper;

    @Resource
    private ContentUserBlockMapper blockMapper;

    @Override
    public ContentFollowRecommendationPageVO listRecommendations(String userId, String interestTag, Long pageNo, Long pageSize) {
        requireValidUserId(userId);
        validateInterestTag(interestTag);
        long currentPage = normalizePageNo(pageNo);
        long currentSize = normalizePageSize(pageSize);
        List<ContentFollowRecommendationItemVO> candidates = cachedRecommendations(userId, interestTag);
        if (candidates.isEmpty()) {
            candidates = fallbackPopularCreators(userId, interestTag);
        }
        List<ContentFollowRecommendationItemVO> eligible = candidates.stream()
            .filter(item -> isEligibleCandidate(userId, item.getTargetUserId()))
            .sorted(Comparator.comparing(ContentFollowRecommendationItemVO::getRankingScore,
                    Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(ContentFollowRecommendationItemVO::getTargetUserId))
            .toList();
        int fromIndex = (int) Math.min((currentPage - 1L) * currentSize, eligible.size());
        int toIndex = (int) Math.min(fromIndex + currentSize, eligible.size());
        return new ContentFollowRecommendationPageVO()
            .setRecords(eligible.subList(fromIndex, toIndex))
            .setTotal((long) eligible.size())
            .setPageNo(currentPage)
            .setPageSize(currentSize)
            .setHasMore(toIndex < eligible.size());
    }

    private List<ContentFollowRecommendationItemVO> cachedRecommendations(String userId, String interestTag) {
        String normalizedInterestTag = interestTag == null ? null : interestTag.trim();
        return recommendationMapper.selectList(Wrappers.<ContentUserFollowRecommendation>lambdaQuery()
                .eq(ContentUserFollowRecommendation::getUserId, userId)
                .eq(ContentUserFollowRecommendation::getRecommendationStatus, ACTIVE_STATUS)
                .like(normalizedInterestTag != null && !normalizedInterestTag.isEmpty(),
                    ContentUserFollowRecommendation::getRecommendationReason, normalizedInterestTag))
            .stream()
            .map(ContentFollowRecommendationItemVO::fromRecommendation)
            .toList();
    }

    private List<ContentFollowRecommendationItemVO> fallbackPopularCreators(String userId, String interestTag) {
        return profileMapper.selectList(Wrappers.<ContentUserProfile>lambdaQuery()
                .orderByDesc(ContentUserProfile::getFollowerCount)
                .orderByDesc(ContentUserProfile::getGrowthValue))
            .stream()
            .map(profile -> ContentFollowRecommendationItemVO.fromProfile(profile, fallbackRule(interestTag), fallbackReason(interestTag)))
            .toList();
    }

    private String fallbackRule(String interestTag) {
        return interestTag == null || interestTag.trim().isEmpty() ? "POPULAR_CREATOR" : "INTEREST_TAG";
    }

    private String fallbackReason(String interestTag) {
        return interestTag == null || interestTag.trim().isEmpty() ? "热门创作者" : "兴趣标签匹配：" + interestTag.trim();
    }

    private boolean isEligibleCandidate(String userId, String targetUserId) {
        if (targetUserId == null || userId.equals(targetUserId)) {
            return false;
        }
        ContentUserProfile profile = profileMapper.selectByUserId(targetUserId);
        if (profile == null || INACTIVE_PROFILE_STATUS.contains(profile.getStatus())) {
            return false;
        }
        // 正向拉黑：当前用户拉黑了候选用户
        var relation = relationMapper.selectByPair(userId, targetUserId);
        if (relation != null
            && (Boolean.TRUE.equals(relation.getBlacklisted())
            || Boolean.TRUE.equals(relation.getBlockedByOwner())
            || Boolean.TRUE.equals(relation.getMuted()))) {
            return false;
        }
        // 反向拉黑：候选用户拉黑了当前用户
        ContentUserBlock reverseBlock = blockMapper.selectByPair(targetUserId, userId);
        if (reverseBlock != null && ACTIVE_STATUS.equals(reverseBlock.getStatus())) {
            return false;
        }
        return relation == null || !Boolean.TRUE.equals(relation.getFollowed());
    }

    private void requireValidUserId(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new JeecgBootException("用户ID不能为空");
        }
        if (userId.length() > USER_ID_MAX_LENGTH) {
            throw new JeecgBootException("用户ID长度不能超过64位");
        }
    }

    private void validateInterestTag(String interestTag) {
        if (interestTag != null && interestTag.length() > USER_ID_MAX_LENGTH) {
            throw new JeecgBootException("兴趣标签长度不能超过64位");
        }
    }

    private long normalizePageNo(Long pageNo) {
        return pageNo == null || pageNo < 1L ? 1L : pageNo;
    }

    private long normalizePageSize(Long pageSize) {
        long currentSize = pageSize == null || pageSize < 1L ? DEFAULT_PAGE_SIZE : pageSize;
        if (currentSize > MAX_PAGE_SIZE) {
            throw new JeecgBootException("分页大小不能超过100");
        }
        return currentSize;
    }
}
