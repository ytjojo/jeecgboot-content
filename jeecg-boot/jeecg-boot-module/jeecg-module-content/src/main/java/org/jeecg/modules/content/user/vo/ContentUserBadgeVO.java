package org.jeecg.modules.content.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.jeecg.modules.content.user.dto.ContentUserBadgeProgressDTO;

import java.util.Date;

/**
 * 内容社区用户勋章展示 VO。
 */
@Data
@Accessors(chain = true)
@Schema(description = "内容社区用户勋章展示")
public class ContentUserBadgeVO {

    @Schema(description = "勋章定义ID")
    private String badgeDefinitionId;

    @Schema(description = "勋章授予ID")
    private String badgeGrantId;

    @Schema(description = "勋章编码")
    private String badgeCode;

    @Schema(description = "勋章名称")
    private String badgeName;

    @Schema(description = "勋章类型")
    private String badgeType;

    @Schema(description = "勋章分类")
    private String category;

    @Schema(description = "勋章图标URL")
    private String iconUrl;

    @Schema(description = "展示特效KEY")
    private String effectKey;

    @Schema(description = "获得条件说明")
    private String conditionDescription;

    @Schema(description = "是否已获得")
    private Boolean granted;

    @Schema(description = "是否佩戴展示")
    private Boolean displaying;

    @Schema(description = "发放原因")
    private String grantReason;

    @Schema(description = "授予状态")
    private String status;

    @Schema(description = "授予时间")
    private Date grantTime;

    @Schema(description = "过期时间")
    private Date expiresAt;

    @Schema(description = "勋章进度")
    private ContentUserBadgeProgressDTO progress;
}
