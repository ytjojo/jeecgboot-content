package org.jeecg.modules.content.channel.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "加入申请详情")
public class JoinApplicationVO {

    @Schema(description = "申请ID")
    private String id;

    @Schema(description = "频道ID")
    private String channelId;

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "用户昵称")
    private String nickname;

    @Schema(description = "申请理由")
    private String reason;

    @Schema(description = "状态: 1=待审核, 2=已批准, 3=已拒绝")
    private Integer status;

    @Schema(description = "审核理由")
    private String reviewReason;

    @Schema(description = "申请时间")
    private LocalDateTime createTime;

    @Schema(description = "是否超时(超过48小时未处理)")
    private Boolean overdue;
}
