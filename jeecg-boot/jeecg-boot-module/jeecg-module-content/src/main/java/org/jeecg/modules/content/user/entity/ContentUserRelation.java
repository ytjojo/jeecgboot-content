package org.jeecg.modules.content.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;

import java.util.Date;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("content_user_relation")
@Schema(description = "内容社区用户关系")
public class ContentUserRelation extends JeecgEntity {

    @Schema(description = "关系拥有者用户ID")
    private String ownerUserId;

    @Schema(description = "目标用户ID")
    private String targetUserId;

    @Schema(description = "关系分组ID")
    private String relationGroupId;

    @Schema(description = "是否关注")
    private Boolean followed;

    @Schema(description = "是否特别关注")
    private Boolean specialFollow;

    @Schema(description = "是否屏蔽")
    private Boolean muted;

    @Schema(description = "是否拉黑")
    private Boolean blacklisted;

    @Schema(description = "是否由拥有者阻断")
    private Boolean blockedByOwner;

    @Schema(description = "推荐理由")
    private String recommendationReason;

    @Schema(description = "关注时间")
    private Date followedAt;

    @Schema(description = "特别关注时间")
    private Date specialFollowAt;

    @Schema(description = "屏蔽时间")
    private Date mutedAt;

    @Schema(description = "拉黑时间")
    private Date blacklistedAt;
}
