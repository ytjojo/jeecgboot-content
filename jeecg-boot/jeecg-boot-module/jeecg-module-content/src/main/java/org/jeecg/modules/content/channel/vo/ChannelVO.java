package org.jeecg.modules.content.channel.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Data
@Schema(description = "频道详情VO")
public class ChannelVO {

    @Schema(description = "频道ID")
    private String id;

    @Schema(description = "频道名称")
    private String name;

    @Schema(description = "频道简介")
    private String description;

    @Schema(description = "频道图标URL")
    private String iconUrl;

    @Schema(description = "频道封面URL")
    private String coverUrl;

    @Schema(description = "频道类型")
    private Integer channelType;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "隐私设置")
    private Integer privacy;

    @Schema(description = "归属分类ID")
    private String categoryId;

    @Schema(description = "频道主用户ID")
    private String ownerId;

    @Schema(description = "组织ID")
    private String organizationId;

    @Schema(description = "置顶权重")
    private Integer pinWeight;

    @Schema(description = "创建时间")
    private Date createTime;
}
