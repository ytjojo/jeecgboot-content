package org.jeecg.modules.content.channel.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;

import java.util.Date;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("content_channel_editorial_pick")
@Schema(description = "频道编辑精选")
public class ContentChannelEditorialPick extends JeecgEntity {

    @Schema(description = "频道ID")
    private String channelId;

    @Schema(description = "推荐语")
    private String recommendationText;

    @Schema(description = "生效开始时间")
    private Date startTime;

    @Schema(description = "生效结束时间，null表示永久")
    private Date endTime;

    @Schema(description = "状态 0=下线 1=上线")
    private Integer status;

    @Schema(description = "操作人ID")
    private String operatorId;
}
