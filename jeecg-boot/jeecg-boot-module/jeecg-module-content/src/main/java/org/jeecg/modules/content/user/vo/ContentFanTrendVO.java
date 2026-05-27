package org.jeecg.modules.content.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDate;

/**
 * 粉丝趋势数据项。
 */
@Data
@Accessors(chain = true)
@Schema(description = "粉丝趋势数据项")
public class ContentFanTrendVO {

    @Schema(description = "日期")
    private LocalDate date;

    @Schema(description = "新增粉丝数")
    private Integer newFollowerCount;
}
