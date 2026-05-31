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
@TableName("content_channel_not_interested")
@Schema(description = "频道不感兴趣反馈")
public class ContentChannelNotInterested extends JeecgEntity {

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "频道ID")
    private String channelId;

    @Schema(description = "频道主分类ID，用于降低同分类权重")
    private String categoryId;

    @Schema(description = "过期时间，30天后")
    private Date expireTime;
}
