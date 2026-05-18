package org.jeecg.modules.content.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.jeecg.modules.content.user.entity.ContentUserVerificationBadge;

import java.util.Date;

/**
 * 内容社区认证标识视图。
 */
@Data
@Accessors(chain = true)
@Schema(description = "内容社区认证标识视图")
public class ContentUserVerificationBadgeVO {

    @Schema(description = "认证ID")
    private String id;

    @Schema(description = "认证类型")
    private String badgeType;

    @Schema(description = "认证文案")
    private String badgeLabel;

    @Schema(description = "视觉样式编码")
    private String visualStyleKey;

    @Schema(description = "认证描述")
    private String description;

    @Schema(description = "认证时间")
    private Date verifiedAt;

    public static ContentUserVerificationBadgeVO from(ContentUserVerificationBadge badge) {
        return new ContentUserVerificationBadgeVO()
            .setId(badge.getId())
            .setBadgeType(badge.getBadgeType())
            .setBadgeLabel(badge.getBadgeLabel())
            .setVisualStyleKey(badge.getVisualStyleKey())
            .setDescription(badge.getDescription())
            .setVerifiedAt(badge.getVerifiedAt());
    }
}
