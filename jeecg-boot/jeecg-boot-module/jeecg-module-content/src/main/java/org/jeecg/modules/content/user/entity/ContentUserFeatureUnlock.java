package org.jeecg.modules.content.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;

import java.util.Date;

/**
 * 内容社区用户功能解锁实体。
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("content_user_feature_unlock")
@Schema(description = "内容社区用户功能解锁")
public class ContentUserFeatureUnlock extends JeecgEntity {

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "功能编码")
    private String featureCode;

    @Schema(description = "来源兑换订单ID")
    private String sourceOrderId;

    @Schema(description = "生效时间")
    private Date validFrom;

    @Schema(description = "失效时间")
    private Date validUntil;

    @Schema(description = "是否有效")
    private Boolean enabled;
}
