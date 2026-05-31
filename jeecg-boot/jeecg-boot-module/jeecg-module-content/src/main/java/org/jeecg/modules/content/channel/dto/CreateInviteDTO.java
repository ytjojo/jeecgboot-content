package org.jeecg.modules.content.channel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "创建邀请请求")
public class CreateInviteDTO {

    @NotBlank(message = "频道ID不能为空")
    @Schema(description = "频道ID")
    private String channelId;

    @NotNull(message = "邀请类型不能为空")
    @Schema(description = "邀请类型: 1=邀请码, 2=邀请链接")
    private Integer type;

    @Schema(description = "最大使用次数, null表示不限")
    private Integer maxUses;

    @Schema(description = "过期时间(小时), null表示不过期")
    private Integer expireHours;
}
