package org.jeecg.modules.content.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;

/**
 * 内容社区积分兑换商品实体。
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("content_user_exchange_goods")
@Schema(description = "内容社区积分兑换商品")
public class ContentUserExchangeGoods extends JeecgEntity {

    @Schema(description = "商品编码")
    private String goodsCode;

    @Schema(description = "商品名称")
    private String goodsName;

    @Schema(description = "商品类型")
    private String goodsType;

    @Schema(description = "积分价格")
    private Integer pointPrice;

    @Schema(description = "库存数量")
    private Integer stockQuantity;

    @Schema(description = "权益发放配置JSON")
    private String benefitConfigJson;

    @Schema(description = "是否启用")
    private Boolean enabled;
}
