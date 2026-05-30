package org.jeecg.modules.content.circle.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * 圈子公告视图对象。
 */
@Data
@Schema(description = "圈子公告视图")
public class CircleAnnouncementVO {

    @Schema(description = "公告ID")
    private String id;

    @Schema(description = "圈子ID")
    private String circleId;

    @Schema(description = "公告内容")
    private String content;

    @Schema(description = "过期时间")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date expireAt;

    @Schema(description = "创建时间")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
}
