package org.jeecg.modules.content.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.jeecg.modules.content.user.entity.ContentUserRelation;

import java.util.Date;

/**
 * View object for content user relation.
 */
@Data
@Accessors(chain = true)
@Schema(description = "内容社区用户关系视图")
public class ContentUserRelationVO {

    @Schema(description = "关系拥有者用户ID", requiredMode = Schema.RequiredMode.REQUIRED, nullable = false)
    private String ownerUserId;

    @Schema(description = "目标用户ID", requiredMode = Schema.RequiredMode.REQUIRED, nullable = false)
    private String targetUserId;

    @Schema(description = "是否已关注", requiredMode = Schema.RequiredMode.REQUIRED, nullable = false)
    private Boolean followed;

    @Schema(description = "是否特别关注", requiredMode = Schema.RequiredMode.REQUIRED, nullable = false)
    private Boolean specialFollow;

    @Schema(description = "是否屏蔽", requiredMode = Schema.RequiredMode.REQUIRED, nullable = false)
    private Boolean muted;

    @Schema(description = "是否拉黑", requiredMode = Schema.RequiredMode.REQUIRED, nullable = false)
    private Boolean blacklisted;

    @Schema(description = "是否由拥有者阻断", requiredMode = Schema.RequiredMode.REQUIRED, nullable = false)
    private Boolean blockedByOwner;

    @Schema(description = "关系分组ID", requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true)
    private String relationGroupId;

    @Schema(description = "关注时间", requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true)
    private Date followedAt;

    @Schema(description = "特别关注时间", requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true)
    private Date specialFollowAt;

    @Schema(description = "是否互相关注", requiredMode = Schema.RequiredMode.NOT_REQUIRED, nullable = true)
    private Boolean mutualFollow;

    /**
     * Builds the current object from the given request or entity.
     */
    public static ContentUserRelationVO from(ContentUserRelation relation) {
        return new ContentUserRelationVO()
            .setOwnerUserId(relation.getOwnerUserId())
            .setTargetUserId(relation.getTargetUserId())
            .setFollowed(relation.getFollowed())
            .setSpecialFollow(relation.getSpecialFollow())
            .setMuted(relation.getMuted())
            .setBlacklisted(relation.getBlacklisted())
            .setBlockedByOwner(relation.getBlockedByOwner())
            .setRelationGroupId(relation.getRelationGroupId())
            .setFollowedAt(relation.getFollowedAt())
            .setSpecialFollowAt(relation.getSpecialFollowAt());
    }
}
