package org.jeecg.modules.content.channel.vo.governance;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "频道治理内容列表项VO")
public class GovernanceContentItemVO {

    @Schema(description = "发布记录ID")
    private String id;

    @Schema(description = "频道ID")
    private String channelId;

    @Schema(description = "内容ID")
    private String contentId;

    @Schema(description = "内容类型")
    private String contentType;

    @Schema(description = "发布状态")
    private String publishStatus;

    @Schema(description = "是否置顶")
    private Boolean isPinned;

    @Schema(description = "置顶排序")
    private Integer pinOrder;

    @Schema(description = "是否精华")
    private Boolean isFeatured;

    @Schema(description = "发布者ID")
    private String publisherId;

    @Schema(description = "来源类型")
    private String sourceType;

    @Schema(description = "创建时间")
    private java.util.Date createTime;
}
