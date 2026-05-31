package org.jeecg.modules.content.channel.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "治理操作日志")
public class GovernanceLogVO {

    @Schema(description = "日志ID")
    private String id;

    @Schema(description = "频道ID")
    private String channelId;

    @Schema(description = "操作类型: 1=移除, 2=禁言, 3=解除禁言, 4=加入黑名单, 5=移出黑名单")
    private Integer action;

    @Schema(description = "操作类型描述")
    private String actionDesc;

    @Schema(description = "操作者ID")
    private String operatorId;

    @Schema(description = "目标用户ID")
    private String targetUserId;

    @Schema(description = "目标用户昵称")
    private String targetNickname;

    @Schema(description = "操作原因")
    private String reason;

    @Schema(description = "操作时间")
    private LocalDateTime createTime;
}
