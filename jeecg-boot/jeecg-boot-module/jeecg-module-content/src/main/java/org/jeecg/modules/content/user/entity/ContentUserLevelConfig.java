package org.jeecg.modules.content.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;

/**
 * 内容社区用户等级配置实体。
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("content_user_level_config")
@Schema(description = "内容社区用户等级配置")
public class ContentUserLevelConfig extends JeecgEntity {

    @Schema(description = "等级")
    private Integer level;

    @Schema(description = "等级名称")
    private String levelName;

    @Schema(description = "成长值门槛")
    private Integer growthThreshold;

    @Schema(description = "等级标识样式KEY")
    private String badgeStyleKey;

    @Schema(description = "是否启用")
    private Boolean enabled;
}
