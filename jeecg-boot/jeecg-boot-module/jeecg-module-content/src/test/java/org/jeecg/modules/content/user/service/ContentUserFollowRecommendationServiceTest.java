package org.jeecg.modules.content.user.service;

import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.user.entity.ContentUserBlock;
import org.jeecg.modules.content.user.entity.ContentUserFollowRecommendation;
import org.jeecg.modules.content.user.entity.ContentUserProfile;
import org.jeecg.modules.content.user.entity.ContentUserRelation;
import org.jeecg.modules.content.user.mapper.ContentUserBlockMapper;
import org.jeecg.modules.content.user.mapper.ContentUserFollowRecommendationMapper;
import org.jeecg.modules.content.user.mapper.ContentUserProfileMapper;
import org.jeecg.modules.content.user.mapper.ContentUserRelationMapper;
import org.jeecg.modules.content.user.service.impl.ContentUserFollowRecommendationServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.any;

/**
 * 关注推荐服务测试。
 */
@ExtendWith(MockitoExtension.class)
class ContentUserFollowRecommendationServiceTest {

    @Mock
    private ContentUserFollowRecommendationMapper recommendationMapper;

    @Mock
    private ContentUserProfileMapper profileMapper;

    @Mock
    private ContentUserRelationMapper relationMapper;

    @Mock
    private ContentUserBlockMapper blockMapper;

    @InjectMocks
    private ContentUserFollowRecommendationServiceImpl recommendationService;

    @Test
    void shouldReturnCachedRecommendationsWithReasonsAndScores() {
        ContentUserFollowRecommendation commonFollow = recommendation("u1", "u2", "COMMON_FOLLOW", "你们有共同关注", 88);
        ContentUserFollowRecommendation interest = recommendation("u1", "u3", "INTEREST_TAG", "科技兴趣相近", 90);
        when(recommendationMapper.selectList(any())).thenReturn(List.of(commonFollow, interest));
        when(profileMapper.selectByUserId("u2")).thenReturn(profile("u2", "NORMAL", 20));
        when(profileMapper.selectByUserId("u3")).thenReturn(profile("u3", "NORMAL", 30));
        when(relationMapper.selectByPair("u1", "u2")).thenReturn(null);
        when(relationMapper.selectByPair("u1", "u3")).thenReturn(null);
        when(blockMapper.selectByPair("u2", "u1")).thenReturn(null);
        when(blockMapper.selectByPair("u3", "u1")).thenReturn(null);

        var result = recommendationService.listRecommendations("u1", "科技", 1L, 10L);

        assertThat(result.getTotal()).isEqualTo(2L);
        assertThat(result.getRecords()).extracting("targetUserId")
            .containsExactly("u3", "u2");
        assertThat(result.getRecords().get(0).getRecommendationReason()).contains("科技");
    }

    @Test
    void shouldFallbackToPopularCreatorsWhenCacheIsEmpty() {
        ContentUserProfile creator = profile("u2", "NORMAL", 120).setNickname("创作者");
        when(recommendationMapper.selectList(any())).thenReturn(List.of());
        when(profileMapper.selectList(any())).thenReturn(List.of(creator));
        when(profileMapper.selectByUserId("u2")).thenReturn(creator);
        when(relationMapper.selectByPair("u1", "u2")).thenReturn(null);
        when(blockMapper.selectByPair("u2", "u1")).thenReturn(null);

        var result = recommendationService.listRecommendations("u1", null, 1L, 10L);

        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getRecords().get(0).getRecommendationRule()).isEqualTo("POPULAR_CREATOR");
        assertThat(result.getRecords().get(0).getRecommendationReason()).isEqualTo("热门创作者");
        assertThat(result.getRecords().get(0).getNickname()).isEqualTo("创作者");
    }

    @Test
    void shouldExcludeSelfFollowedBlockedMutedAndInactiveCandidates() {
        when(recommendationMapper.selectList(any())).thenReturn(List.of(
            recommendation("u1", "u1", "POPULAR_CREATOR", "自己", 99),
            recommendation("u1", "u2", "POPULAR_CREATOR", "已关注", 98),
            recommendation("u1", "u3", "POPULAR_CREATOR", "拉黑", 97),
            recommendation("u1", "u4", "POPULAR_CREATOR", "冻结", 96),
            recommendation("u1", "u5", "POPULAR_CREATOR", "可推荐", 95)
        ));
        when(profileMapper.selectByUserId("u2")).thenReturn(profile("u2", "NORMAL", 10));
        when(profileMapper.selectByUserId("u3")).thenReturn(profile("u3", "NORMAL", 10));
        when(profileMapper.selectByUserId("u4")).thenReturn(profile("u4", "FROZEN", 10));
        when(profileMapper.selectByUserId("u5")).thenReturn(profile("u5", "NORMAL", 10));
        when(relationMapper.selectByPair("u1", "u2")).thenReturn(new ContentUserRelation().setFollowed(true));
        when(relationMapper.selectByPair("u1", "u3")).thenReturn(new ContentUserRelation().setBlacklisted(true));
        when(relationMapper.selectByPair("u1", "u5")).thenReturn(null);
        when(blockMapper.selectByPair("u2", "u1")).thenReturn(null);
        when(blockMapper.selectByPair("u5", "u1")).thenReturn(null);

        var result = recommendationService.listRecommendations("u1", null, 1L, 10L);

        assertThat(result.getRecords()).extracting("targetUserId")
            .containsExactly("u5");
    }

    @Test
    void shouldExcludeCandidateWhoBlockedCurrentUser() {
        when(recommendationMapper.selectList(any())).thenReturn(List.of(
            recommendation("u1", "u2", "POPULAR_CREATOR", "被反向拉黑", 98),
            recommendation("u1", "u3", "POPULAR_CREATOR", "正常候选", 95)
        ));
        when(profileMapper.selectByUserId("u2")).thenReturn(profile("u2", "NORMAL", 10));
        when(profileMapper.selectByUserId("u3")).thenReturn(profile("u3", "NORMAL", 10));
        when(relationMapper.selectByPair("u1", "u2")).thenReturn(null);
        when(relationMapper.selectByPair("u1", "u3")).thenReturn(null);
        // u2 拉黑了 u1，应该被过滤
        ContentUserBlock reverseBlock = new ContentUserBlock()
            .setUserId("u2")
            .setBlockedUserId("u1")
            .setStatus("ACTIVE");
        when(blockMapper.selectByPair("u2", "u1")).thenReturn(reverseBlock);
        when(blockMapper.selectByPair("u3", "u1")).thenReturn(null);

        var result = recommendationService.listRecommendations("u1", null, 1L, 10L);

        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getRecords().get(0).getTargetUserId()).isEqualTo("u3");
    }

    @Test
    void shouldReturnEmptyRecommendationWhenAllCandidatesBlockedCurrentUser() {
        when(recommendationMapper.selectList(any())).thenReturn(List.of(
            recommendation("u1", "u2", "POPULAR_CREATOR", "被拉黑", 98)
        ));
        when(profileMapper.selectByUserId("u2")).thenReturn(profile("u2", "NORMAL", 10));
        when(relationMapper.selectByPair("u1", "u2")).thenReturn(null);
        ContentUserBlock reverseBlock = new ContentUserBlock()
            .setUserId("u2")
            .setBlockedUserId("u1")
            .setStatus("ACTIVE");
        when(blockMapper.selectByPair("u2", "u1")).thenReturn(reverseBlock);

        var result = recommendationService.listRecommendations("u1", null, 1L, 10L);

        assertThat(result.getRecords()).isEmpty();
        assertThat(result.getTotal()).isZero();
        assertThat(result.getHasMore()).isFalse();
    }

    @Test
    void shouldRejectInvalidRecommendationQuery() {
        assertThatThrownBy(() -> recommendationService.listRecommendations(" ", null, 1L, 10L))
            .isInstanceOf(JeecgBootException.class)
            .hasMessage("用户ID不能为空");
        assertThatThrownBy(() -> recommendationService.listRecommendations("u1", "标".repeat(65), 1L, 10L))
            .isInstanceOf(JeecgBootException.class)
            .hasMessage("兴趣标签长度不能超过64位");
        assertThatThrownBy(() -> recommendationService.listRecommendations("u1", null, 1L, 101L))
            .isInstanceOf(JeecgBootException.class)
            .hasMessage("分页大小不能超过100");

        verify(recommendationMapper, never()).selectList(any());
    }

    private ContentUserFollowRecommendation recommendation(String userId, String targetUserId, String rule, String reason, int score) {
        return new ContentUserFollowRecommendation()
            .setUserId(userId)
            .setTargetUserId(targetUserId)
            .setRecommendationRule(rule)
            .setRecommendationReason(reason)
            .setRankingScore(BigDecimal.valueOf(score))
            .setRecommendationStatus("ACTIVE");
    }

    private ContentUserProfile profile(String userId, String status, int followerCount) {
        return new ContentUserProfile()
            .setUserId(userId)
            .setStatus(status)
            .setFollowerCount(followerCount);
    }
}
