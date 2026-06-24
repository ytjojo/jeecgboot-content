package org.jeecg.modules.content.user.growth.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@Schema(description = "圈子等级权益项")
public class CircleBenefitVO {

    @Schema(description = "权益名称")
    private String name;

    @Schema(description = "是否已解锁")
    private Boolean unlocked;
}
