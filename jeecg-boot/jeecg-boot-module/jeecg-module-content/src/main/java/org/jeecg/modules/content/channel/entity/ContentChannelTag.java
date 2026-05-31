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
@TableName("content_channel_tag")
@Schema(description = "频道内标签")
public class ContentChannelTag extends JeecgEntity {

    @Schema(description = "频道ID")
    private String channelId;

    @Schema(description = "标签名称")
    private String name;

    @Schema(description = "状态 0=已删除 1=正常")
    private Integer status;
}
