package org.jeecg.modules.content.user.vo;

import lombok.Data;
import lombok.experimental.Accessors;
import org.jeecg.modules.content.user.entity.ContentUserRelation;

@Data
@Accessors(chain = true)
public class ContentUserRelationVO {

    private String ownerUserId;
    private String targetUserId;
    private Boolean followed;
    private Boolean specialFollow;
    private Boolean muted;
    private Boolean blacklisted;
    private Boolean blockedByOwner;
    private String relationGroupId;

    public static ContentUserRelationVO from(ContentUserRelation relation) {
        return new ContentUserRelationVO()
            .setOwnerUserId(relation.getOwnerUserId())
            .setTargetUserId(relation.getTargetUserId())
            .setFollowed(relation.getFollowed())
            .setSpecialFollow(relation.getSpecialFollow())
            .setMuted(relation.getMuted())
            .setBlacklisted(relation.getBlacklisted())
            .setBlockedByOwner(relation.getBlockedByOwner())
            .setRelationGroupId(relation.getRelationGroupId());
    }
}
