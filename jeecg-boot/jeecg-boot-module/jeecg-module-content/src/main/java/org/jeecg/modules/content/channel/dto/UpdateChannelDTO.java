package org.jeecg.modules.content.channel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "更新频道请求")
public class UpdateChannelDTO {

    @Schema(description = "频道名称")
    private String name;

    @Schema(description = "频道简介")
    private String description;

    @Schema(description = "频道图标URL")
    private String iconUrl;

    @Schema(description = "频道封面URL")
    private String coverUrl;

    @Schema(description = "归属分类ID")
    private String categoryId;

    @Schema(description = "标签")
    private String tags;
}
