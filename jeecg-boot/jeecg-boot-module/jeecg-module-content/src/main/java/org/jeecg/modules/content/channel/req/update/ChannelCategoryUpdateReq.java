package org.jeecg.modules.content.channel.req.update;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "更新分类请求")
public class ChannelCategoryUpdateReq {

    @NotBlank(message = "分类ID不能为空")
    @Schema(description = "分类ID")
    private String id;

    @Size(max = 50, message = "分类名称不能超过50个字符")
    @Schema(description = "分类名称")
    private String name;

    @Schema(description = "排序序号")
    private Integer sortOrder;
}
