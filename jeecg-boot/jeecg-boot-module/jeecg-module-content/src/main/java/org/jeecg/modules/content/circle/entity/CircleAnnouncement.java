package org.jeecg.modules.content.circle.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;

import java.util.Date;

/**
 * 圈子公告实体。
 * 支持发布、替换和过期逻辑。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("circle_announcement")
@Schema(description = "圈子公告")
public class CircleAnnouncement extends JeecgEntity {

    @Schema(description = "圈子ID")
    private String circleId;

    @Schema(description = "公告内容")
    private String content;

    @Schema(description = "状态（ACTIVE/INACTIVE）")
    private String status;

    @Schema(description = "过期时间，为空则永不过期")
    private Date expireAt;
}
