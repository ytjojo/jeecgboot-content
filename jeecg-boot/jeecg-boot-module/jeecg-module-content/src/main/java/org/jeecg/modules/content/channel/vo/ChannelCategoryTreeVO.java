package org.jeecg.modules.content.channel.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "分类树节点")
public class ChannelCategoryTreeVO {

    @Schema(description = "分类ID")
    private String id;

    @Schema(description = "分类名称")
    private String name;

    @Schema(description = "分类层级")
    private Integer level;

    @Schema(description = "排序序号")
    private Integer sortOrder;

    @Schema(description = "是否特殊分类")
    private Integer isSystem;

    @Schema(description = "子分类")
    private List<ChannelCategoryTreeVO> children;
}
