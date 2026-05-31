package org.jeecg.modules.content.channel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "批量操作请求")
public class BatchOperationDTO {

    @NotEmpty(message = "目标ID列表不能为空")
    @Schema(description = "目标用户/成员ID列表")
    private List<String> targetIds;

    @Schema(description = "操作原因")
    private String reason;
}
