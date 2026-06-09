package org.jeecg.modules.content.circle.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Data
@Schema(description = "圈子详情响应")
public class CircleVO {

    @Schema(description = "圈子ID")
    private String id;

    @Schema(description = "圈子名称")
    private String name;

    @Schema(description = "圈子简介")
    private String description;

    @Schema(description = "图标URL")
    private String iconUrl;

    @Schema(description = "封面图URL")
    private String coverUrl;

    @Schema(description = "分类标签")
    private String category;

    @Schema(description = "隐私类型")
    private String privacyType;

    @Schema(description = "加入方式")
    private String joinType;

    @Schema(description = "创建者用户ID")
    private String creatorId;

    @Schema(description = "成员数")
    private Integer memberCount;

    @Schema(description = "最大成员数")
    private Integer maxMemberCount;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "当前用户是否已加入")
    private Boolean joined;

    @Schema(description = "当前用户在圈子中的角色")
    private String myRole;

    @Schema(description = "当前用户的申请状态: PENDING/APPROVED/REJECTED/null")
    private String applyStatus;

    @Schema(description = "当前用户是否被邀请加入该圈子")
    private Boolean isInvited;

    @Schema(description = "创建时间")
    private Date createTime;
}
