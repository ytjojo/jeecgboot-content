package org.jeecg.modules.content.channel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "更新加入方式请求")
public class UpdateJoinMethodDTO {

    @NotBlank(message = "频道ID不能为空")
    @Schema(description = "频道ID")
    private String channelId;

    @NotNull(message = "加入方式不能为空")
    @Schema(description = "加入方式: 1=自由加入, 2=审核加入, 3=邀请加入")
    private Integer joinMethod;
}
