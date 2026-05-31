package org.jeecg.modules.content.channel.req.create;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "创建分类请求")
public class ChannelCategoryCreateReq {

    @NotBlank(message = "分类名称不能为空")
    @Size(max = 50, message = "分类名称不能超过50个字符")
    @Schema(description = "分类名称")
    private String name;

    @Schema(description = "父级分类ID，null表示根分类")
    private String parentId;

    @Schema(description = "排序序号")
    private Integer sortOrder;

    @Schema(description = "是否特殊分类 0=普通 1=特殊")
    @Max(value = 1, message = "is_system 只能是 0 或 1")
    private Integer isSystem;
}
