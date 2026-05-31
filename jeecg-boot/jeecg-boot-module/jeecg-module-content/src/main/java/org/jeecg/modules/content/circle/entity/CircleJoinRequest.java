package org.jeecg.modules.content.circle.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;

import java.util.Date;

/**
 * 圈子加入申请实体。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("circle_join_request")
@Schema(description = "圈子加入申请")
public class CircleJoinRequest extends JeecgEntity {

    @Schema(description = "圈子ID")
    private String circleId;

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "状态：PENDING/APPROVED/REJECTED/EXPIRED")
    private String status;

    @Schema(description = "拒绝原因")
    private String rejectReason;

    @Schema(description = "操作人ID")
    private String operatorId;

    @Schema(description = "操作时间")
    private Date operateTime;
}
