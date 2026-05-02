package org.jeecg.modules.content.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * View object for runtime level benefit summary.
 */
@Data
@Accessors(chain = true)
@Schema(description = "内容社区用户等级权益摘要")
public class ContentUserLevelBenefitSummaryVO {

    @Schema(description = "上传大小上限，单位MB")
    private Integer uploadSizeLimitMb;

    @Schema(description = "是否支持高清视频")
    private Boolean hdVideoEnabled;

    @Schema(description = "可订阅话题上限")
    private Integer topicQuota;

    @Schema(description = "当前显式启用的权益编码")
    private List<String> enabledBenefitCodes;
}
