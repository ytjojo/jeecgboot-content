package org.jeecg.modules.content.user.req.profile;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 主页模块配置请求项。
 */
@Data
@Accessors(chain = true)
@Schema(description = "内容社区主页模块配置项")
public class ContentUserHomepageModuleReq {

    @NotBlank(message = "模块编码不能为空")
    @Schema(description = "模块编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String moduleKey;

    @NotNull(message = "模块显隐不能为空")
    @Schema(description = "是否展示", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean visible;

    @NotNull(message = "模块排序不能为空")
    @Schema(description = "排序号", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer sortOrder;
}
