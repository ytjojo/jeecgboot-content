package org.jeecg.modules.content.circle.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "圈子搜索结果项")
public class CircleSearchResultVO {

    @Schema(description = "圈子ID")
    private String id;

    @Schema(description = "圈子名称")
    private String name;

    @Schema(description = "图标URL")
    private String iconUrl;

    @Schema(description = "圈子简介")
    private String description;

    @Schema(description = "成员数")
    private Integer memberCount;

    @Schema(description = "当前用户是否已加入")
    private Boolean joined;
}
