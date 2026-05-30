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
@TableName("content_channel_subscription_group")
@Schema(description = "订阅分组表")
public class ChannelSubscriptionGroup extends JeecgEntity {

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "分组名称")
    private String groupName;

    @Schema(description = "排序顺序")
    private Integer sortOrder;
}
