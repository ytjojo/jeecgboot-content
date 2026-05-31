package org.jeecg.modules.content.circle.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;

import java.util.Date;

/**
 * 圈子内容举报实体。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("circle_report")
@Schema(description = "圈子内容举报")
public class CircleReport extends JeecgEntity {

    @Schema(description = "圈子ID")
    private String circleId;

    @Schema(description = "被举报的内容ID")
    private String contentId;

    @Schema(description = "举报人用户ID")
    private String reporterId;

    @Schema(description = "举报原因")
    private String reason;

    @Schema(description = "状态：PENDING/RESOLVED/IGNORED")
    private String status;

    @Schema(description = "处理人ID")
    private String operatorId;

    @Schema(description = "处理时间")
    private Date operateTime;

    @Schema(description = "处理动作：DELETE/IGNORE/MUTE")
    private String handleAction;
}
