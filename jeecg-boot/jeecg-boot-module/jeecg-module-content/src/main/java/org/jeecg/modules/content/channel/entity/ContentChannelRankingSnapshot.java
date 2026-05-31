package org.jeecg.modules.content.channel.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("content_channel_ranking_snapshot")
@Schema(description = "频道排行榜快照")
public class ContentChannelRankingSnapshot extends JeecgEntity {

    @Schema(description = "频道ID")
    private String channelId;

    @Schema(description = "榜单类型: HOT/NEW/SYSTEM")
    private String rankingType;

    @Schema(description = "维度: DAILY/WEEKLY/MONTHLY")
    private String dimension;

    @Schema(description = "排名位置")
    private Integer rankPosition;

    @Schema(description = "综合得分")
    private BigDecimal score;

    @Schema(description = "快照日期")
    private Date snapshotDate;
}
