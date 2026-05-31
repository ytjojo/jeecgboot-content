package org.jeecg.modules.content.circle.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "圈子成员响应")
public class CircleMemberVO {

    @Schema(description = "成员记录ID")
    private String id;

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "角色")
    private String role;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "禁言结束时间")
    private LocalDateTime muteEndTime;

    @Schema(description = "加入时间")
    private LocalDateTime createTime;
}
