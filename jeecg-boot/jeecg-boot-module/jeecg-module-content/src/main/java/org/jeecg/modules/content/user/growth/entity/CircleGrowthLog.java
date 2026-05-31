package org.jeecg.modules.content.user.growth.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;

import java.time.LocalDate;

/**
 * 成长行为流水实体。
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("circle_growth_log")
@Schema(description = "成长行为流水")
public class CircleGrowthLog extends JeecgEntity {

    @Schema(description = "圈子ID")
    private String circleId;

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "行为类型")
    private String actionType;

    @Schema(description = "获得经验值")
    private Integer expPoints;

    @Schema(description = "获得贡献值")
    private Integer contributionPoints;

    @Schema(description = "关联业务ID")
    private String bizId;

    @Schema(description = "业务日期")
    private LocalDate bizDate;

    @Schema(description = "是否已撤销")
    private Boolean revoked;
}
