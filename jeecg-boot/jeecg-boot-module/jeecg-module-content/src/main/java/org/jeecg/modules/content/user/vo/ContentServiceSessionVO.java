package org.jeecg.modules.content.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 客服会话视图。
 */
@Data
@Accessors(chain = true)
@Schema(description = "客服会话视图")
public class ContentServiceSessionVO {

    @Schema(description = "会话ID")
    private String sessionId;

    @Schema(description = "会话类型")
    private String sessionType;

    @Schema(description = "会话状态")
    private String status;

    @Schema(description = "评分")
    private Integer rating;

    @Schema(description = "评分内容")
    private String ratingComment;

    @Schema(description = "开始时间")
    private Date startTime;

    @Schema(description = "结束时间")
    private Date endTime;

    @Schema(description = "是否超过30天")
    private Boolean expired;
}
