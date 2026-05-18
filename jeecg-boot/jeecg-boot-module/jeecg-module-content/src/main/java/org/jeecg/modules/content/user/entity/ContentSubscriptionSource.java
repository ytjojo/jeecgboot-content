package org.jeecg.modules.content.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 内容社区订阅源目录。
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("content_subscription_source")
@Schema(description = "内容社区订阅源目录")
public class ContentSubscriptionSource extends JeecgEntity {

    @Schema(description = "订阅源类型")
    private String sourceType;

    @Schema(description = "订阅源ID")
    private String sourceId;

    @Schema(description = "订阅源名称")
    private String sourceName;

    @Schema(description = "订阅源介绍")
    private String sourceDescription;

    @Schema(description = "分类")
    private String category;

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
