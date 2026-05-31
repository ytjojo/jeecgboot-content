package org.jeecg.modules.content.circle.req.update;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "更新圈子请求")
public class CircleUpdateReq {

    @NotBlank(message = "圈子ID不能为空")
    @Schema(description = "圈子ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String circleId;

    @Size(max = 500, message = "圈子简介最多500个字符")
    @Schema(description = "圈子简介")
    private String description;

    @Schema(description = "图标URL")
    private String iconUrl;

    @Schema(description = "封面图URL")
    private String coverUrl;

    @Schema(description = "分类标签")
    private String category;
}
