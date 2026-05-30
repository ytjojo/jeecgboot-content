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
@TableName("content_channel_subscription")
@Schema(description = "频道订阅表")
public class ChannelSubscription extends JeecgEntity {

    @Schema(description = "频道ID")
    private String channelId;

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "来源: 1=主动订阅 2=默认关注")
    private Integer source;

    @Schema(description = "提醒开关: 0=关闭 1=开启")
    private Integer remindEnabled;
}
