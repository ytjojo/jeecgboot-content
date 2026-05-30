package org.jeecg.modules.content.circle.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;

import java.util.Date;

/**
 * 圈子内容实体。
 * 支持内容置顶和精华标记。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("circle_content")
@Schema(description = "圈子内容")
public class CircleContent extends JeecgEntity {

    @Schema(description = "圈子ID")
    private String circleId;

    @Schema(description = "发布者ID")
    private String userId;

    @Schema(description = "内容文本")
    private String content;

    @Schema(description = "内容类型（POST/COMMENT）")
    private String contentType;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "是否置顶")
    private Boolean isPinned;

    @Schema(description = "置顶时间")
    private Date pinnedAt;

    @Schema(description = "是否精华")
    private Boolean isFeatured;

    @Schema(description = "精华时间")
    private Date featuredAt;

    @Schema(description = "是否删除")
    private Boolean deleted;
}
