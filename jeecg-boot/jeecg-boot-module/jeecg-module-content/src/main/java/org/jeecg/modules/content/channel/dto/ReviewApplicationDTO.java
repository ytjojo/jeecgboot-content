package org.jeecg.modules.content.channel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "审核申请请求")
public class ReviewApplicationDTO {

    @NotBlank(message = "申请ID不能为空")
    @Schema(description = "申请ID")
    private String applicationId;

    @NotNull(message = "审核结果不能为空")
    @Schema(description = "是否批准")
    private Boolean approved;

    @Schema(description = "审核理由")
    private String reason;
}
