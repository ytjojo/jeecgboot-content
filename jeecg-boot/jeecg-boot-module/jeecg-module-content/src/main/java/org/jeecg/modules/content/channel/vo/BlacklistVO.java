package org.jeecg.modules.content.channel.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "黑名单详情")
public class BlacklistVO {

    @Schema(description = "黑名单ID")
    private String id;

    @Schema(description = "频道ID")
    private String channelId;

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "用户昵称")
    private String nickname;

    @Schema(description = "操作原因")
    private String reason;

    @Schema(description = "操作者ID")
    private String operatorId;

    @Schema(description = "加入时间")
    private LocalDateTime createTime;
}
