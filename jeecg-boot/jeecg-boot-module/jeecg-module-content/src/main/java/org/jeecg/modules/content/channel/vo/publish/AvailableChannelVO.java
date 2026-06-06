package org.jeecg.modules.content.channel.vo.publish;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "可发布频道VO")
public class AvailableChannelVO {

    @Schema(description = "频道ID")
    private String channelId;

    @Schema(description = "频道名称")
    private String channelName;

    @Schema(description = "频道图标URL")
    private String iconUrl;

    @Schema(description = "用户在该频道的角色")
    private String userRole;

    @Schema(description = "发布权限配置")
    private String publishPermission;

    @Schema(description = "是否可发布")
    private Boolean canPublish;

    @Schema(description = "不可发布原因")
    private String blockedReason;
}
