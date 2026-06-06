package org.jeecg.modules.content.channel.vo.governance;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "回收站列表项VO")
public class RecycleBinItemVO {

    @Schema(description = "回收站记录ID")
    private String id;

    @Schema(description = "频道ID")
    private String channelId;

    @Schema(description = "内容ID")
    private String contentId;

    @Schema(description = "内容类型")
    private String contentType;

    @Schema(description = "原作者ID")
    private String originalAuthorId;

    @Schema(description = "删除人ID")
    private String deletedBy;

    @Schema(description = "删除时间")
    private java.util.Date deleteTime;

    @Schema(description = "删除原因")
    private String deleteReason;

    @Schema(description = "过期时间")
    private java.util.Date expireTime;

    @Schema(description = "是否已恢复")
    private Boolean isRestored;

    @Schema(description = "剩余天数")
    private Long remainingDays;
}
