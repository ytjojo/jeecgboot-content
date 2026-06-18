package org.jeecg.modules.content.user.growth.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "等级条件项")
public class LevelConditionVO {

    @Schema(description = "维度类型: MEMBER / CONTENT / INTERACTION")
    private String type;

    @Schema(description = "维度标签")
    private String label;

    @Schema(description = "当前值")
    private Integer current;

    @Schema(description = "上限值")
    private Integer required;

    @Schema(description = "差距值 (required - current)")
    private Integer gap;
}
