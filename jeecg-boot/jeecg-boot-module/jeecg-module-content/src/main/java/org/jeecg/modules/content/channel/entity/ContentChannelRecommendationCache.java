package org.jeecg.modules.content.channel.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;

import java.math.BigDecimal;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("content_channel_recommendation_cache")
@Schema(description = "频道推荐缓存")
public class ContentChannelRecommendationCache extends JeecgEntity {

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "频道ID")
    private String channelId;

    @Schema(description = "推荐评分")
    private BigDecimal rankingScore;

    @Schema(description = "推荐规则: SIMILARITY/PREFERENCE/POPULAR/COLD_START")
    private String recommendationRule;

    @Schema(description = "推荐理由")
    private String recommendationReason;

    @Schema(description = "状态 0=已消费 1=有效")
    private Integer recommendationStatus;
}
