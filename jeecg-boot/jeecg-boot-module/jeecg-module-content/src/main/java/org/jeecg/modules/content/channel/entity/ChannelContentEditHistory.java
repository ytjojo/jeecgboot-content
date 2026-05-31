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
@TableName("channel_content_edit_history")
@Schema(description = "频道内容编辑历史")
public class ChannelContentEditHistory extends JeecgEntity {

    @Schema(description = "频道ID")
    private String channelId;

    @Schema(description = "内容ID")
    private String contentId;

    @Schema(description = "编辑者ID")
    private String editorId;

    @Schema(description = "修改字段：title/tags/summary")
    private String fieldName;

    @Schema(description = "修改前的值")
    private String oldValue;

    @Schema(description = "修改后的值")
    private String newValue;
}
