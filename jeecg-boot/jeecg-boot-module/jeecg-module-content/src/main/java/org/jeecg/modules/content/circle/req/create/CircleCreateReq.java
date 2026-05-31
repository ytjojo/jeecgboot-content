package org.jeecg.modules.content.circle.req.create;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "创建圈子请求")
public class CircleCreateReq {

    @NotBlank(message = "圈子名称不能为空")
    @Size(max = 100, message = "圈子名称最多100个字符")
    @Schema(description = "圈子名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @NotBlank(message = "圈子简介不能为空")
    @Size(max = 500, message = "圈子简介最多500个字符")
    @Schema(description = "圈子简介", requiredMode = Schema.RequiredMode.REQUIRED)
    private String description;

    @Schema(description = "图标URL")
    private String iconUrl;

    @Schema(description = "封面图URL")
    private String coverUrl;

    @Schema(description = "分类标签")
    private String category;

    @NotNull(message = "隐私类型不能为空")
    @Schema(description = "隐私类型: PUBLIC/PRIVATE/PASSWORD", requiredMode = Schema.RequiredMode.REQUIRED)
    private String privacyType;

    @NotNull(message = "加入方式不能为空")
    @Schema(description = "加入方式: DIRECT/APPROVAL/INVITE/PASSWORD", requiredMode = Schema.RequiredMode.REQUIRED)
    private String joinType;

    @Schema(description = "密码保护密码(当privacyType=PASSWORD时必填)")
    private String password;
}
