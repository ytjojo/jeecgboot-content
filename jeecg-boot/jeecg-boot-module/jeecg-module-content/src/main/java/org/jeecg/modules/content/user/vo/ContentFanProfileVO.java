package org.jeecg.modules.content.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

/**
 * 粉丝画像分析结果。
 */
@Data
@Accessors(chain = true)
@Schema(description = "粉丝画像分析结果")
public class ContentFanProfileVO {

    @Schema(description = "粉丝总数")
    private Integer fanCount;

    @Schema(description = "地区分布，key为地区，value为人数")
    private Map<String, Integer> regionDistribution;

    @Schema(description = "提示信息（粉丝数不足时返回）")
    private String hint;
}
