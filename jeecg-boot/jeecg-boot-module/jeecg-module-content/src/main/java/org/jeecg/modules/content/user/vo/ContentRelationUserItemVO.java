package org.jeecg.modules.content.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.jeecg.modules.content.user.entity.ContentUserActivitySnapshot;
import org.jeecg.modules.content.user.entity.ContentUserProfile;
import org.jeecg.modules.content.user.entity.ContentUserRelation;

import java.util.Date;

/**
 * 内容社区关注用户列表项。
 */
@Data
@Accessors(chain = true)
@Schema(description = "内容社区关注用户列表项")
public class ContentRelationUserItemVO {

    @Schema(description = "目标用户ID")
    private String targetUserId;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "个人简介")
    private String bio;

    @Schema(description = "认证展示文案")
    private String certificationLabel;

    @Schema(description = "关系分组ID")
    private String relationGroupId;

    @Schema(description = "是否已关注")
    private Boolean followed;

    @Schema(description = "是否特别关注")
    private Boolean specialFollow;

    @Schema(description = "关注时间")
    private Date followedAt;

    @Schema(description = "最新动态提示")
    private String latestActivityHint;

    @Schema(description = "最新动态时间")
    private Date latestActivityTime;

    /**
     * 组装关系与资料摘要。
     */
    public static ContentRelationUserItemVO from(ContentUserRelation relation, ContentUserProfile profile, ContentUserActivitySnapshot activitySnapshot) {
        ContentRelationUserItemVO item = new ContentRelationUserItemVO()
            .setTargetUserId(relation.getTargetUserId())
            .setRelationGroupId(relation.getRelationGroupId())
            .setFollowed(relation.getFollowed())
            .setSpecialFollow(relation.getSpecialFollow())
            .setFollowedAt(relation.getFollowedAt());
        if (profile != null) {
            item.setNickname(profile.getNickname())
                .setAvatar(profile.getAvatar())
                .setBio(profile.getBio())
                .setCertificationLabel(profile.getCertificationLabel());
        }
        if (activitySnapshot != null) {
            item.setLatestActivityHint(activitySnapshot.getSummary())
                .setLatestActivityTime(activitySnapshot.getActivityTime());
        }
        return item;
    }
}
