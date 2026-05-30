package org.jeecg.modules.content.circle.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Date;

/**
 * 圈子公告发布请求。
 */
@Data
@Schema(description = "圈子公告发布请求")
public class CircleAnnouncementReq {

    @NotBlank(message = "圈子ID不能为空")
    @Schema(description = "圈子ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String circleId;

    @NotBlank(message = "公告内容不能为空")
    @Schema(description = "公告内容", requiredMode = Schema.RequiredMode.REQUIRED)
    private String content;

    @Schema(description = "过期时间，为空则永不过期")
    private Date expireAt;
}
