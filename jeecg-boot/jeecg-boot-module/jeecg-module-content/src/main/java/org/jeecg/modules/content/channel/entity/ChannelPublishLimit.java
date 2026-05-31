package org.jeecg.modules.content.channel.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("channel_publish_limit")
@Schema(description = "频道发布限额配置")
public class ChannelPublishLimit extends JeecgEntity {

    @Schema(description = "频道ID")
    private String channelId;

    @Schema(description = "每小时发布上限，0表示不限")
    private Integer hourlyLimit;

    @Schema(description = "每日发布上限，0表示不限")
    private Integer dailyLimit;

    @Schema(description = "内容字数下限，0表示不限")
    private Integer minWordCount;
}
