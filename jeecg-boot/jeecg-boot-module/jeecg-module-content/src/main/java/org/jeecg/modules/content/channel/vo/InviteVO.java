package org.jeecg.modules.content.channel.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "邀请详情")
public class InviteVO {

    @Schema(description = "邀请ID")
    private String id;

    @Schema(description = "频道ID")
    private String channelId;

    @Schema(description = "邀请码/链接")
    private String code;

    @Schema(description = "类型: 1=邀请码, 2=邀请链接")
    private Integer type;

    @Schema(description = "最大使用次数")
    private Integer maxUses;

    @Schema(description = "已使用次数")
    private Integer usedCount;

    @Schema(description = "过期时间")
    private LocalDateTime expireTime;

    @Schema(description = "状态: 1=有效, 2=已用完, 3=已撤销, 4=已过期")
    private Integer status;

    @Schema(description = "状态描述")
    private String statusDesc;

    @Schema(description = "创建者ID")
    private String creatorId;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
