package org.jeecg.modules.content.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;

import java.time.LocalDate;

/**
 * Entity for daily fan trend data.
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("content_fan_trend_daily")
@Schema(description = "内容社区粉丝每日趋势")
public class ContentFanTrendDaily extends JeecgEntity {

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "统计日期")
    private LocalDate date;

    @Schema(description = "当日新增粉丝数")
    private Integer newFollowerCount;
}
