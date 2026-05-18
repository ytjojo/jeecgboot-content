package org.jeecg.modules.content.user.req.relation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 内容社区关注分组请求。
 */
@Data
@Accessors(chain = true)
@Schema(description = "内容社区关注分组请求")
public class ContentRelationGroupReq {

    @NotBlank(message = "分组名称不能为空")
    @Size(max = 64, message = "分组名称长度不能超过64位")
    @Schema(description = "分组名称", requiredMode = Schema.RequiredMode.REQUIRED, nullable = false)
    private String groupName;

    @NotNull(message = "排序值不能为空")
    @Min(value = 0, message = "排序值不能小于0")
    @Schema(description = "排序值", requiredMode = Schema.RequiredMode.REQUIRED, nullable = false)
    private Integer sortOrder;
}
