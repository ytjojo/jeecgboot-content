package org.jeecg.modules.content.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.jeecg.modules.content.user.entity.ContentUserFollowRecommendation;
import org.jeecg.modules.content.user.entity.ContentUserProfile;

import java.math.BigDecimal;

/**
 * 关注推荐用户条目。
 */
@Data
@Accessors(chain = true)
@Schema(description = "关注推荐用户条目")
public class ContentFollowRecommendationItemVO {

    @Schema(description = "推荐关注用户ID")
    private String targetUserId;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "个人简介")
    private String bio;

    @Schema(description = "推荐规则")
    private String recommendationRule;

    @Schema(description = "推荐理由")
    private String recommendationReason;

    @Schema(description = "排序分数")
    private BigDecimal rankingScore;

    public static ContentFollowRecommendationItemVO fromRecommendation(ContentUserFollowRecommendation recommendation) {
        return new ContentFollowRecommendationItemVO()
            .setTargetUserId(recommendation.getTargetUserId())
            .setRecommendationRule(recommendation.getRecommendationRule())
            .setRecommendationReason(recommendation.getRecommendationReason())
            .setRankingScore(recommendation.getRankingScore());
    }

    public static ContentFollowRecommendationItemVO fromProfile(ContentUserProfile profile, String rule, String reason) {
        BigDecimal score = BigDecimal.valueOf(profile.getFollowerCount() == null ? 0 : profile.getFollowerCount());
        return new ContentFollowRecommendationItemVO()
            .setTargetUserId(profile.getUserId())
            .setNickname(profile.getNickname())
            .setAvatar(profile.getAvatar())
            .setBio(profile.getBio())
            .setRecommendationRule(rule)
            .setRecommendationReason(reason)
            .setRankingScore(score);
    }
}
