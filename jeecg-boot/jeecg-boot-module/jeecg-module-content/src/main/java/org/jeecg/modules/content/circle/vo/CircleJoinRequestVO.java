package org.jeecg.modules.content.circle.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * 圈子加入申请视图对象。
 */
@Data
@Schema(description = "圈子加入申请视图")
public class CircleJoinRequestVO {

    @Schema(description = "申请ID")
    private String id;

    @Schema(description = "圈子ID")
    private String circleId;

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "创建时间")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
}
