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
@TableName("content_channel_tag_relation")
@Schema(description = "标签-内容关联")
public class ContentChannelTagRelation extends JeecgEntity {

    @Schema(description = "标签ID")
    private String tagId;

    @Schema(description = "内容ID")
    private String contentId;

    @Schema(description = "内容类型: article/post/video/note/question")
    private String contentType;
}
