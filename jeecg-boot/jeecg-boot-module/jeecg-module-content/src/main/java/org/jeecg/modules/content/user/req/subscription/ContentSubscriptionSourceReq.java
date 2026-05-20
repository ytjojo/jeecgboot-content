package org.jeecg.modules.content.user.req.subscription;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 订阅源目录保存请求。
 */
@Data
@Accessors(chain = true)
@Schema(description = "订阅源目录保存请求")
public class ContentSubscriptionSourceReq {

    @Schema(description = "订阅源类型", requiredMode = Schema.RequiredMode.REQUIRED, nullable = false)
    private String sourceType;

    @Schema(description = "订阅源ID", requiredMode = Schema.RequiredMode.REQUIRED, nullable = false)
    private String sourceId;

    @Schema(description = "订阅源名称", requiredMode = Schema.RequiredMode.REQUIRED, nullable = false)
    private String sourceName;

    @Size(max = 512, message = "订阅源介绍长度不能超过512位")
    @Schema(description = "订阅源介绍")
    private String sourceDescription;

    @Size(max = 64, message = "订阅源分类长度不能超过64位")
    @Schema(description = "订阅源分类")
    private String category;

    @Size(max = 255, message = "封面地址长度不能超过255位")
    @Schema(description = "封面地址")
    private String coverUrl;

    @Schema(description = "订阅人数")
    private Integer subscriberCount;

    @Schema(description = "热度分")
    private BigDecimal heatScore;

    @Schema(description = "最近更新时间")
    private Date latestUpdateTime;

    @Schema(description = "是否启用")
    private Boolean enabled;
}
