package org.jeecg.modules.content.channel.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Data
@Schema(description = "频道转让记录VO")
public class ChannelTransferVO {

    @Schema(description = "转让ID")
    private String transferId;

    @Schema(description = "频道ID")
    private String channelId;

    @Schema(description = "发起转让用户ID")
    private String fromUserId;

    @Schema(description = "发起转让用户名")
    private String fromUserName;

    @Schema(description = "目标用户ID")
    private String toUserId;

    @Schema(description = "目标用户名")
    private String toUserName;

    @Schema(description = "转让状态: PENDING/ACCEPTED/REJECTED/EXPIRED")
    private String status;

    @Schema(description = "创建时间")
    private Date createdTime;
}
