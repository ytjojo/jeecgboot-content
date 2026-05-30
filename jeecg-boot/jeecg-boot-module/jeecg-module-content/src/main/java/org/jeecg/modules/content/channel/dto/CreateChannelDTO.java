package org.jeecg.modules.content.channel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.jeecg.modules.content.channel.enums.ChannelType;

@Data
@Schema(description = "创建频道请求")
public class CreateChannelDTO {

    @NotBlank(message = "频道名称不能为空")
    @Schema(description = "频道名称")
    private String name;

    @Schema(description = "频道简介")
    private String description;

    @Schema(description = "频道图标URL")
    private String iconUrl;

    @Schema(description = "频道封面URL")
    private String coverUrl;

    @NotNull(message = "频道类型不能为空")
    @Schema(description = "频道类型")
    private ChannelType channelType;

    @Schema(description = "隐私设置: 1=公开, 2=私有")
    private Integer privacy;

    @Schema(description = "归属分类ID")
    private String categoryId;

    @Schema(description = "组织ID(组织频道必填)")
    private String organizationId;

    @Schema(description = "置顶权重(系统频道)")
    private Integer pinWeight;
}
