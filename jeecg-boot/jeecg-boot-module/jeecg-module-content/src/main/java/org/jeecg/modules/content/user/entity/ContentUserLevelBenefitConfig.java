package org.jeecg.modules.content.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;

/**
 * 内容社区用户等级权益配置实体。
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("content_user_level_benefit_config")
@Schema(description = "内容社区用户等级权益配置")
public class ContentUserLevelBenefitConfig extends JeecgEntity {

    @Schema(description = "适用等级")
    private Integer level;

    @Schema(description = "权益编码")
    private String benefitKey;

    @Schema(description = "权益值")
    private String benefitValue;

    @Schema(description = "权益扩展配置JSON")
    private String benefitConfigJson;

    @Schema(description = "是否启用")
    private Boolean enabled;
}
